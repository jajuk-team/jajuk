/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.jajuk.services.lastfm;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Manages Last.fm authentication (Desktop Auth) for Jajuk.
 * First attempts automatic authentication via a local server.
 * If it fails (firewall, blocked port), falls back to manual mode.
 */
public class LastFmAuthenticator {
  private final String apiKey;
  private final String apiSecret;
  private final HttpClient httpClient;

  // Variables for the local server
  private volatile String receivedToken = null;
  private volatile boolean serverRunning = false;

  public LastFmAuthenticator(String apiKey, String apiSecret) {
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
  }

  /**
   * Launches the authentication process.
   * This method must be called from a non-EDT thread (e.g., SwingWorker).
   *
   * @return The session_key if successful, null otherwise.
   *
   * @throws IOException          if a network error occurs during token exchange.
   * @throws InterruptedException if the thread is interrupted while waiting.
   * @throws TimeoutException     if the automatic authentication times out.
   */
  public String authenticate() throws IOException, InterruptedException, TimeoutException {
    Log.info("Starting Last.fm authentication...");

    // 1. Automatic Attempt
    String token;
    try {
      token = attemptAutomaticAuth();
      if (token != null) {
        Log.info("Automatic authentication successful.");
        return exchangeTokenForSession(token);
      }
    } catch (IOException | InterruptedException e) {
      // If automatic auth fails due to network issues or interruption,
      // we fall back to manual mode ONLY if the error is not critical (like interruption).
      // However, for simplicity and robustness, we often let the exception propagate
      // if it's a real network failure, or catch it here to trigger manual mode.

      // Let's catch specifically to allow fallback to manual mode if the server couldn't start
      Log.warn("Automatic authentication failed (likely cause: firewall or blocked port). Falling back to manual mode. " + e.getMessage());
      // Continue to manual mode
    }

    // 2. Fallback: Manual Mode
    Log.info("Switching to manual authentication mode.");
    String manualToken = attemptManualAuth();

    if (manualToken != null) {
      // This call can still throw IOException/InterruptedException, which is now allowed by the method signature
      return exchangeTokenForSession(manualToken);
    }

    return null; // Cancelled or total failure
  }

  /**
   * Attempts authentication via a local server.
   */
  private String attemptAutomaticAuth() throws IOException, InterruptedException, TimeoutException {
    int port = findFreePort();
    HttpServer server = null;

    try {
      server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
      server.createContext("/callback", new CallbackHandler());
      server.setExecutor(null);
      server.start();
      serverRunning = true;

      Log.info("Local server started on port " + port);

      String authUrl = String.format(
              "https://www.last.fm/api/auth?api_key=%s&cb=http://localhost:%d/callback",
              apiKey, port
      );

      // Open the browser
      if (!openBrowser(authUrl)) {
        throw new IOException("Unable to open browser automatically.");
      }

      // Wait for the token (5 min timeout)
      long startTime = System.currentTimeMillis();
      while (receivedToken == null) {
        if (System.currentTimeMillis() - startTime > TimeUnit.MINUTES.toMillis(5)) {
          throw new TimeoutException("Timeout waiting for user (5 min).");
        }
        Thread.sleep(500);
      }

      return receivedToken;

    } finally {
      if (server != null) {
        server.stop(0);
        serverRunning = false;
      }
    }
  }

  /**
   * Manual mode: Displays a dialog box to copy/paste the token.
   */
  private String attemptManualAuth() {
    String authUrl = String.format("https://www.last.fm/api/auth?api_key=%s", apiKey);

    String message = String.format(
            """
                    Automatic browser opening failed (firewall or restriction?).
                    
                    Please perform the following steps:
                    
                    1. Copy this URL and paste it into your browser:
                    %s
                    
                    2. Log in to Last.fm and click "Allow".
                    3. You will be redirected to a page containing a code (token).
                    4. Copy this code and paste it below:""",
            authUrl
    );

    JTextField textField = new JTextField(30);
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel(message), BorderLayout.NORTH);
    panel.add(textField, BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(
            null,
            panel,
            "Last.fm Authentication - Manual Mode",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
      String token = textField.getText().trim();
      if (token.isEmpty()) {
        Messages.showInfoMessage("Token is empty. Authentication cancelled.", "Error");
        return null;
      }
      return token;
    }
    return null;
  }

  /**
   * Exchanges the temporary token for the permanent session_key.
   */
  private String exchangeTokenForSession(String token) throws IOException, InterruptedException {
    // TRIM all values to remove invisible whitespace
    if (token == null || token.trim().isEmpty()) {
      throw new IOException("Token is empty or null.");
    }
    token = token.trim();

    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new IOException("API Key is missing.");
    }
    String cleanApiKey = apiKey.trim();

    if (apiSecret == null || apiSecret.trim().isEmpty()) {
      throw new IOException("API Secret is missing.");
    }
    String cleanApiSecret = apiSecret.trim();

    // Prepare parameters map (RAW values, no encoding yet)
    Map<String, String> params = new TreeMap<>();
    params.put("method", "auth.getSession");
    params.put("token", token);
    params.put("api_key", cleanApiKey);
    //params.put("format", "json");

    // DEBUG: Log the parameters before hashing
    Log.info("Parameters for signature: " + params);

    // Generate signature
    String apiSig = generateSignature(params, cleanApiSecret);
    Log.info("Calculated API signature: " + apiSig);

    // Build body string with URL-ENCODED values
    StringBuilder bodyBuilder = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (!first)
        bodyBuilder.append("&");
      bodyBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
              .append("=")
              .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
      first = false;
    }
    // Add api_sig (also encoded)
    bodyBuilder.append("&api_sig=").append(URLEncoder.encode(apiSig, StandardCharsets.UTF_8));
    // Specify response format, do not include in MD5 signature
    bodyBuilder.append("&format=json");

    String body = bodyBuilder.toString();

    // USE EXPLICIT URL (not external constant)
    Log.info("Sending POST request to: " + LastFmUtils.BASE_URL);
    Log.info("Request body: " + body);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(LastFmUtils.BASE_URL))
            .header("User-Agent", LastFmUtils.USER_AGENT)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    Log.info("Response Status: " + response.statusCode());
    Log.info("Response Body: " + response.body());

    if (response.statusCode() != 200) {
      Log.warn("Last.fm API error (token exchange): " + response.statusCode() + " - " + response.body());
      throw new IOException("Last.fm API Error: " + response.body());
    }

    String json = response.body();
    int keyStart = json.indexOf("\"key\":\"");
    if (keyStart == -1) {
      Log.warn("Unable to find session_key in JSON response: " + json);
      throw new IOException("Invalid JSON response from Last.fm: " + json);
    }

    keyStart += 7;
    int keyEnd = json.indexOf("\"", keyStart);
    if (keyEnd == -1) {
      Log.warn("Unable to find end of session_key in JSON response: " + json);
      throw new IOException("Malformed JSON response.");
    }

    String sessionKey = json.substring(keyStart, keyEnd);
    Log.info("Session Key obtained successfully: " + sessionKey);
    return sessionKey;
  }

  /**
   * Generates the API signature (api_sig) required by Last.fm API 2.0.
   * Follows the exact algorithm from <a href="https://www.last.fm/api/desktopauth">https://www.last.fm/api/desktopauth</a>
   *
   * @param params    Map of parameters (excluding api_sig).
   * @param apiSecret The API secret key.
   * @return The MD5 signature string.
   */
  public static String generateSignature(Map<String, String> params, String apiSecret) {
    // 1. Sort parameters alphabetically by key
    TreeMap<String, String> sortedParams = new TreeMap<>(params);

    // 2. DEBUG: Log the string that will be hashed
    StringBuilder debugSb = new StringBuilder();
    for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
      debugSb.append(entry.getKey()).append("=").append(entry.getValue()).append(" | ");
    }
    debugSb.append("SECRET=").append(apiSecret);
    Log.info("String to hash: " + debugSb);

    // 3. Concatenate key+value pairs WITHOUT any separator (RAW values)
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
      sb.append(entry.getKey());
      sb.append(entry.getValue());
    }
    sb.append(apiSecret);

    String stringToHash = sb.toString();
    Log.info("Raw string for MD5: " + stringToHash);

    // 4. Calculate MD5 hash
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(stringToHash.getBytes(StandardCharsets.UTF_8));

      // 5. Convert to lowercase hex string
      StringBuilder hexString = new StringBuilder();
      for (byte b : digest) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1)
          hexString.append('0');
        hexString.append(hex);
      }
      String signature = hexString.toString().toLowerCase();
      Log.info("MD5 Hash (lowercase): " + signature);
      return signature;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not found", e);
    }
  }

  /**
   * Opens the default browser.
   */
  private boolean openBrowser(String url) {
    try {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI.create(url));
        return true;
      }
    } catch (IOException e) {
      Log.info("Error opening browser : " + e.getMessage());
    }
    return false;
  }

  /**
   * Finds a free port (>1024) to avoid conflicts and admin requirements.
   */
  private int findFreePort() {
    try (var socket = new java.net.ServerSocket(0)) {
      int port = socket.getLocalPort();
      // Ensure the port is > 1024 (normally the case with 0, but safety)
      return Math.max(port, 1025);
    } catch (IOException e) {
      Log.info("Unable to find a free port" + e.getMessage());
      return 12345; // Fallback (risky but better than nothing)
    }
  }

  /**
   * HTTP Handler to capture the Last.fm callback.
   */
  private class CallbackHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String query = exchange.getRequestURI().getQuery();
      if (query != null && query.startsWith("token=")) {
        receivedToken = query.substring(6);

        // HTML success response
        String responseHtml = "<html><body style='font-family:sans-serif;text-align:center;padding:50px;'>" +
                "<h1 style='color:green;'>Authentication successful!</h1>" +
                "<p>You can close this window and return to Jajuk.</p>" +
                "</body></html>";

        byte[] bytes = responseHtml.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(bytes);
        }
      } else {
        exchange.sendResponseHeaders(400, -1);
      }
      exchange.close();
    }
  }

}