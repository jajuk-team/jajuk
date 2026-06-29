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
package org.jajuk.services.network;

import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Singleton HTTP client service for managing all network requests.
 * Encapsulates GET, POST, image downloads, and proxy authentication.
 * Pure HTTP operations only.
 */
public class HttpClientService {

  private static volatile HttpClientService instance;
  private final Object lock = new Object();

  private HttpClient httpClient;
  private ProxyConfig proxyConfig;
  private boolean internetAccessAllowed = true;

  /**
   * Private constructor prevents direct instantiation.
   * Initializes default settings and timeout values.
   */
  private HttpClientService() {
    this.proxyConfig = null;
    this.httpClient = createHttpClient(null);
    updateInternetAccessSetting();
  }

  /**
   * Returns the singleton instance with thread-safe lazy initialization.
   * Uses double-checked locking pattern for performance.
   *
   * @return the singleton HttpClientService instance
   */
  public static HttpClientService getInstance() {
    if (instance == null) {
      synchronized (HttpClientService.class) {
        if (instance == null) {
          instance = new HttpClientService();
        }
      }
    }
    return instance;
  }

  public String getContent(String url) {
    try {
      // Le service gère automatiquement le proxy configuré via DownloadManager.setDefaultProxySettings()
      // et renvoie directement le corps de la réponse en String.
      String content = readUrl(url);

      if (content == null) {
        Log.warn("Unable to fetch content from '" + url + "': Null response or non-200 status.");
      }

      return content;
    } catch (IOException e) {
      // Gère les erreurs réseau, timeouts, etc.
      Log.warn("Error fetching from '" + url + "' : " + e.getMessage());
      return null;
    }
  }

  /**
   * Updates or sets the proxy configuration for all future requests.
   * Rebuilds the internal HttpClient with the new proxy settings.
   *
   * @param proxyConfig the proxy configuration (null to disable proxy)
   */
  public void setProxyConfig(ProxyConfig proxyConfig) {
    synchronized (lock) {
      this.proxyConfig = proxyConfig;
      this.httpClient = createHttpClient(proxyConfig);
      Log.info("Proxy config updated: " + (proxyConfig != null ? proxyConfig.toString() : "None"));
    }
  }

  /**
   * Gets the current proxy configuration object.
   *
   * @return the current ProxyConfig or null if none is set
   */
  public ProxyConfig getProxyConfig() {
    synchronized (lock) {
      return proxyConfig;
    }
  }

  /**
   * Refreshes the internet access flag from application config.
   * Call this when user changes network preferences in settings.
   */
  public void updateInternetAccessSetting() {
    this.internetAccessAllowed = !Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS);
  }

  /**
   * Checks if internet access is currently allowed by config.
   *
   * @return true if internet access is enabled
   */
  public boolean isInternetAccessAllowed() {
    return internetAccessAllowed;
  }

  /**
   * Creates a configured HttpClient instance with optional proxy support.
   * Sets connection timeout based on global configuration.
   *
   * @param proxyConfig the proxy configuration (may be null)
   * @return configured HttpClient instance
   */
  private HttpClient createHttpClient(ProxyConfig proxyConfig) {
    long timeoutMillis = 1000L * Conf.getInt(Const.CONF_NETWORK_CONNECTION_TO);

    HttpClient.Builder builder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(timeoutMillis))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_2);

    if (proxyConfig != null) {
      builder.proxy(proxyConfig.toProxySelector());
    }

    return builder.build();
  }

  public HttpRequest buildPostRequest(URI uri, String requestBody) {
    return buildRequest(uri, "POST", requestBody);
  }

  /**
   * Builds an HTTP request with standard headers and optional proxy auth.
   * Includes common headers like User-Agent and Accept-Language.
   *
   * @param uri         the request URI
   * @param method      the HTTP method (GET, POST, HEAD, etc.)
   * @param requestBody the request body content (null for non-POST)
   * @return configured HttpRequest ready to send
   */
  private HttpRequest buildRequest(URI uri, String method, String requestBody) {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofMillis(1000L * Conf.getInt(Const.CONF_NETWORK_CONNECTION_TO)))
            .header("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*")
            .header("Accept-Language", "en-us")
            .header("User-Agent", getUserAgent());

    // Inject Proxy-Authorization header if credentials exist in config
    if (proxyConfig != null && proxyConfig.getUsername().isPresent() &&
            proxyConfig.getPassword().isPresent()) {

      String userPass = proxyConfig.getUsername().get() + ":" + proxyConfig.getPassword().get();
      byte[] encoded = java.util.Base64.getEncoder()
              .encode(userPass.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      String authHeader = "Basic " + new String(encoded, java.nio.charset.StandardCharsets.UTF_8);
      requestBuilder.header("Proxy-Authorization", authHeader);
    }

    if (requestBody != null) {
      if ("POST".equals(method)) {
        requestBuilder.method("POST", HttpRequest.BodyPublishers.ofString(requestBody));
        // This content type is OK for LastFM, it may not be OK for other service
        requestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
      } else {
        requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(requestBody));
      }
    } else {
      requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
    }

    return requestBuilder.build();
  }

  /**
   * Sends a HEAD request to check response status and headers without downloading body.
   * Useful for checking availability or 429 status before full download.
   *
   * @param urlString the URL to fetch headers from
   * @return HttpResponse containing status code and headers only
   *
   * @throws IOException if an I/O error occurs
   */
  public HttpResponse<Void> headRequest(String urlString) throws IOException {
    if (!isInternetAccessAllowed()) {
      Log.debug("Internet access disabled - skipping headRequest : " + urlString);
      return null;
    }

    try {
      URI uri = URI.create(urlString);
      HttpRequest request = buildRequest(uri, "HEAD", null);
      return httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
  }

  /**
   * Fetches data from a URL as a string.
   * Uses the service's configured proxy automatically.
   *
   * @param urlString the URL to read from
   * @return the response body as String, or null if access is disabled
   *
   * @throws IOException if an I/O error occurs
   */
  public String readUrl(String urlString) throws IOException {
    if (!isInternetAccessAllowed()) {
      Log.debug("Internet access disabled - skipping readUrl : " + urlString);
      return null;
    }

    try {
      // Proxy Log
      ProxyConfig currentProxy = this.getProxyConfig();
      if (currentProxy != null && currentProxy.getUsername().isPresent()) {
        Log.info(">>> REQUEST VIA AUTHENTICATED PROXY: " +
                currentProxy.getHost() + ":" + currentProxy.getPort() +
                " for URL: " + urlString);
      } else if (currentProxy != null) {
        Log.info(">>> REQUEST VIA UNAUTHENTICATED PROXY: " +
                currentProxy.getHost() + ":" + currentProxy.getPort() +
                " for URL: " + urlString);
      } else {
        Log.info(">>> REQUEST DIRECT (NO PROXY): " + urlString);
      }

      URI uri = URI.create(urlString);
      HttpRequest request = buildRequest(uri, "GET", null);
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new IOException("HTTP error: " + response.statusCode());
      }

      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
  }

  /**
   * Performs an HTTP POST request with form data.
   * Uses the service's configured proxy automatically.
   *
   * @param urlString the URL to post to
   * @param postData  the POST data (URL-encoded format)
   * @return the response body as String
   *
   * @throws IOException if an I/O error occurs
   */
  public String postUrl(String urlString, String postData) throws IOException {
    if (!internetAccessAllowed) {
      Log.debug("Internet access disabled - skipping POST: " + urlString);
      return null;
    }

    try {
      URI uri = URI.create(urlString);
      HttpRequest request = buildRequest(uri, "POST", postData);
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new IOException("Invalid HTTP return code: " + response.statusCode());
      }

      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
  }

  /**
   * Downloads an image from a URL into memory.
   * Uses the service's configured proxy automatically.
   *
   * @param urlString the URL of the image
   * @return the loaded Image, or null if access is disabled or failed
   *
   * @throws IOException if an I/O error occurs
   */
  public Image getImage(String urlString) throws IOException {
    if (!internetAccessAllowed) {
      Log.debug("Internet access disabled - skipping image: " + urlString);
      return null;
    }

    try {
      URI uri = URI.create(urlString);
      HttpRequest request = buildRequest(uri, "GET", null);
      HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

      if (response.statusCode() != 200) {
        throw new IOException("Failed to download image: HTTP " + response.statusCode());
      }

      return ImageIO.read(new ByteArrayInputStream(response.body()));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
  }

  /**
   * Downloads a resource directly to a file on disk.
   * Streams the response to avoid high memory usage for large files.
   *
   * @param urlString   the URL to download from
   * @param destination the destination file
   * @throws IOException if an I/O error occurs
   */
  public void downloadToFile(String urlString, File destination) throws IOException {
    if (!internetAccessAllowed) {
      Log.debug("Internet access disabled - skipping download: " + urlString);
      return;
    }

    try {
      URI uri = URI.create(urlString);
      HttpRequest request = buildRequest(uri, "GET", null);
      HttpResponse<java.nio.file.Path> response = httpClient.send(request,
              HttpResponse.BodyHandlers.ofFile(destination.toPath()));

      if (response.statusCode() != 200) {
        if (destination.exists()) {
          if (!destination.delete()) {
            Log.warn("Failed to delete incomplete download file: " + destination.getAbsolutePath());
          }
        }
        throw new IOException("Download failed: HTTP " + response.statusCode());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
  }

  /**
   * Executes a generic GET request returning the full response object.
   * Useful for advanced cases needing status codes and headers.
   *
   * @param urlString the URL to fetch
   * @return HttpResponse object with status, headers, and body
   *
   * @throws IOException if an I/O error occurs
   */
  public HttpResponse<String> executeGetRequest(String urlString) throws IOException {
    if (!internetAccessAllowed) {
      return null;
    }

    try {
      URI uri = URI.create(urlString);
      HttpRequest request = buildRequest(uri, "GET", null);
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
  }

  /**
   * Executes a generic POST request returning the full response object.
   * Useful for APIs needing status codes and response body for error handling.
   *
   * @param urlString the URL to post to
   * @param postData  the POST body (URL-encoded)
   * @return HttpResponse with status, headers, and body, or null if disabled
   * @throws IOException if an I/O error occurs
   */
  public HttpResponse<String> executePostRequest(String urlString, String postData)
          throws IOException {
    if (!internetAccessAllowed) {
      Log.debug("Internet access disabled - skipping POST: " + urlString);
      return null;
    }

    try {
      URI uri = URI.create(urlString);
      HttpRequest request = buildRequest(uri, "POST", postData);
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
  }

  /**
   * Encodes a string for HTTP request parameters using UTF-8.
   * Handles null inputs gracefully by returning empty string.
   *
   * @param s the string to encode
   * @return the URL-encoded string
   */
  public String encode(String s) {
    if (s == null) {
      return "";
    }
    return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
  }

  /**
   * Resets the singleton instance (mainly for testing purposes).
   * Should not be called during normal production execution.
   */
  public static void resetInstance() {
    synchronized (HttpClientService.class) {
      instance = null;
    }
  }

  /**
   * Returns a modern and descriptive User-Agent string for Jajuk.
   * <p>
   * Format: {AppName}/{Version} ({OS}; {JavaVersion})
   * Example: Jajuk/2.0.0 (Windows 11; Java 17.0.9)
   * </p>
   *
   * @return The User-Agent string
   */
  public static String getUserAgent() {
    String appVersion = Const.JAJUK_VERSION;
    String osName = System.getProperty("os.name");
    String osVersion = System.getProperty("os.version");
    String javaVersion = System.getProperty("java.version");

    // Cleanup to avoid special characters
    String cleanOs = (osName + " " + osVersion).replaceAll("[^a-zA-Z0-9. _-]", "").trim();
    if (cleanOs.isEmpty()) {
      cleanOs = "UnknownOS";
    }

    return String.format("Jajuk/%s (%s; Java %s)", appVersion, cleanOs, javaVersion);
  }
}