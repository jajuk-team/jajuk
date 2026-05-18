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
package org.jajuk.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.log.Log;

import ext.services.network.NetworkUtils;
import ext.services.network.Proxy;

/**
 * Manages network downloads.
 */
public final class DownloadManager {
  private static Proxy proxy;

  private static final int BUFFER_SIZE = 8000;

  // Rate limiting configuration
  private static final int MAX_CONCURRENT_REQUESTS = 3; // Limit simultaneous requests
  private static final long MIN_DELAY_BETWEEN_REQUESTS_MS = 2500; // Delay min between each request, 2.5s for wikimedia
  private static final int MAX_RETRIES_FOR_429 = 4; // Number of retries for 429 response status
  private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 second

  // Semaphore to limit active threads
  private static final Semaphore rateLimiter = new Semaphore(MAX_CONCURRENT_REQUESTS);
  private static volatile long lastRequestTime = 0;

  /**
   * private constructor to avoid instantiating utility class.
   */
  private DownloadManager() {
  }

  /**
   * Acquires a permit from the rate limiter and ensures minimum delay between requests.
   * Uses tryAcquire with timeout to avoid indefinite blocking.
   */
  private static boolean acquireRateLimit() throws IOException {
    try {
      // Try to acquire with a reasonable timeout (30 seconds max wait)
      boolean acquired = rateLimiter.tryAcquire(15, java.util.concurrent.TimeUnit.SECONDS);

      if (!acquired) {
        throw new IOException("Rate limiter timeout: could not acquire permit after 30 seconds");
      }

      long now = System.currentTimeMillis();
      long elapsed = now - lastRequestTime;
      if (elapsed < MIN_DELAY_BETWEEN_REQUESTS_MS) {
        Thread.sleep(MIN_DELAY_BETWEEN_REQUESTS_MS - elapsed);
      }
      lastRequestTime = System.currentTimeMillis();

      return true;
    } catch (InterruptedException e) {
      // Restore the interrupt status - best practice
      Thread.currentThread().interrupt();
      throw new IOException("Download interrupted", e);
    }
  }

  /**
   * Gets the remote covers list.
   *
   * @return a list of urls
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List<URL> getRemoteCoversList(String search) throws IOException {
    List<URL> alOut = new ArrayList<>(20); // URL list
    // check void searches
    if (search == null || search.trim().equals("")) {
      return alOut;
    }
    URL url = new URL("https://www.google.com/search?q=" + URLEncoder.encode(search, "ISO-8859-1")
            + "&tbm=isch&biw=1092&source=lnms");
    final URLConnection connection = url.openConnection();
    // User-Agent is required to avoid 403 Google response
    connection.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");

    // Retrieve response
    String line;
    final StringBuilder builder = new StringBuilder();
    final BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream()));
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }

    // Analyse response with a pattern to extract image url
    final Pattern pattern = Pattern.compile("src=\"https://[^ ]*\"");
    final Matcher matcher = pattern.matcher(builder);
    while (matcher.find()) {
      final String sUrl = matcher.group();
      if (sUrl.length() > 6) {
        url = new URL(sUrl.substring(5, sUrl.length() - 1));
        // Remove duplicates
        if (!alOut.contains(url)) {
          alOut.add(url);
        }
      }
    }
    return alOut;
  }

  /**
   * Download the resource at the given url with rate limiting and retry logic.
   *
   * @param url          url to download
   * @param fDestination destination file
   * @throws IOException If a network problem occurs.
   */
  public static void download(URL url, File fDestination) throws IOException {
    // Validate URL first to prevent MD5/hash errors
    if (!isValidURL(url)) {
      throw new IOException("Invalid URL: " + (url != null ? url.toString() : "null"));
    }
    boolean permitAcquired = false;
    int attempt = 0;
    long retryDelay = INITIAL_RETRY_DELAY_MS;

    while (attempt < MAX_RETRIES_FOR_429) {
      // Apply global rate limiting
      if (!acquireRateLimit()) {
        throw new IOException("Failed to acquire rate limit permit");
      }
      permitAcquired = true;
      HttpURLConnection connection = null;
      try {
        connection = NetworkUtils.getConnection(url, proxy);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        int statusCode = connection.getResponseCode();

        if (statusCode == 429) {
          attempt++;
          if (attempt >= MAX_RETRIES_FOR_429) {
            throw new IOException("Server returned HTTP 429 (Too Many Requests) after " +
                    MAX_RETRIES_FOR_429 + " attempts. URL: " + url);
          }

          // Respect Retry-After header if present
          String retryAfterHeader = connection.getHeaderField("Retry-After");
          if (retryAfterHeader != null) {
            try {
              retryDelay = Long.parseLong(retryAfterHeader) * 1000;
            } catch (NumberFormatException e) {
              // Ignore, use default exponential backoff
            }
          }

          Log.warn("Rate limit hit (429) for " + url + ". Retrying in " + retryDelay + "ms (Attempt " + attempt + "/" + MAX_RETRIES_FOR_429 + ")");

          // Sleep for retry delay - MUST handle InterruptedException here
          try {
            Thread.sleep(retryDelay);
          } catch (InterruptedException e) {
            // Restore interrupt status and abort
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted during retry wait", e);
          }

          retryDelay *= 2; // Exponential backoff
          continue;
        }

        if (statusCode != 200) {
          throw new IOException("HTTP Error: " + statusCode + " - " + connection.getResponseMessage() + " for URL: " + url);
        }

        // Validate content length before downloading
        long contentLength = connection.getContentLengthLong();
        if (contentLength == -1) {
          Log.warn("Unknown content length for " + url + ", proceeding with download");
        } else if (contentLength == 0) {
          throw new IOException("Empty response from server for URL: " + url);
        } else if (contentLength > 10 * 1024 * 1024) { // 10MB max
          throw new IOException("Response too large (" + contentLength + " bytes) for URL: " + url);
        }

        // Success - download the file
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fDestination))) {
          try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())) {
            int bytesRead;
            long totalBytes = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = bis.read(buffer)) != -1) {
              bos.write(buffer, 0, bytesRead);
              totalBytes += bytesRead;
            }
            // Validate downloaded file size
            if (totalBytes == 0) {
              throw new IOException("Downloaded 0 bytes for URL: " + url);
            }
          }
          bos.flush();
        }

        // Update last request time
        lastRequestTime = System.currentTimeMillis();
        return; // Success

      } finally {
        if (connection != null) {
          connection.disconnect();
        }
        // Free the permit after use
        if (permitAcquired) {
          rateLimiter.release();
          permitAcquired = false;
        }
      }
    }
    throw new IOException("Download failed after " + MAX_RETRIES_FOR_429 + " retries for URL: " + url);
  }

  /**
   * Download the resource at the given url and cache it <br>
   * If the file is already in cache, it is returned immediately <br>
   *
   * @param url url to download
   * @return cached file or null if a problem occurred
   *
   * @throws IOException If a network problem occurs or a temporary file cannot be
   *                     written.
   */
  public static File downloadToCache(URL url) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    File file = SessionService.getCachePath(url);
    // We synchronize the (interned) name of the cached file to avoid
    // probable collisions between views
    synchronized (file.getName().intern()) {
      // check if file is not already downloaded or being downloaded
      if (file.exists() && file.length() > 0) {
        return file;
      }
      download(url,file);
      return file;
    }
  }

  /**
   * Download the cover list.
   *
   * @param url     to download
   * @param charset the character encoding to use for the received text
   * @return result as an array of bytes, null if a problem occurred
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String downloadText(URL url, String charset) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    HttpURLConnection connection = NetworkUtils.getConnection(url, proxy);
    try {
      return NetworkUtils.readURL(connection, charset);
    } finally {
      connection.disconnect();
    }
  }

  /**
   * Download text with the default charset UTF-8.
   *
   * @return the string
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String downloadText(URL url) throws IOException {
    return downloadText(url, "UTF-8");
  }

  /**
   * Return a string for a given URL and encoding, used to retrieve text from a
   * cached file.
   *
   * @param url      url to read
   * @param encoding encoding of the content of the file
   * @return a string for a given URL and encoding
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String getTextFromCachedFile(URL url, String encoding) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    File file = downloadToCache(url);
    StringBuilder builder = new StringBuilder();
    try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
      byte[] array = new byte[1024];
      int read;
      while ((read = input.read(array)) > 0) {
        builder.append(new String(array, 0, read, encoding));
      }
    }
    return builder.toString();
  }

  /**
   * Validates URL before processing to prevent MD5 errors.
   */
  private static boolean isValidURL(URL url) {
    if (url == null) {
      return false;
    }
    String protocol = url.getProtocol();
    return "http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);
  }

  /**
   * Set default proxy settings, used by cobra for ie.
   */
  public synchronized static void setDefaultProxySettings() {
    String sProxyHost = Conf.getString(Const.CONF_NETWORK_PROXY_HOSTNAME);
    int iProxyPort = Conf.getInt(Const.CONF_NETWORK_PROXY_PORT);
    String sProxyLogin = Conf.getString(Const.CONF_NETWORK_PROXY_LOGIN);
    String sProxyPwd = Conf.getString(Const.CONF_NETWORK_PROXY_PWD);
    Type proxyType = Type.DIRECT;
    if (Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY)) {
      // Set default proxy value
      if (Const.PROXY_TYPE_HTTP.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
        proxyType = Type.HTTP;
      } else if (Const.PROXY_TYPE_SOCKS.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
        proxyType = Type.SOCKS;
      }
      try {
        proxy = new Proxy(proxyType, sProxyHost, iProxyPort, sProxyLogin, sProxyPwd);
      } catch (Exception e) {
        Log.error(e);
        return;
      }
    }
    // Set system defaults proxy values, if we don't use DownloadManager
    // methods
    // see http://java.sun.com/j2se/1.4.2/docs/guide/net/properties.html
    if (Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY)) {
      System.getProperties().put("proxySet", "true");
      if (Const.PROXY_TYPE_HTTP.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
        System.setProperty("http.proxyHost", sProxyHost);
        System.setProperty("http.proxyPort", Integer.toString(iProxyPort));
      } else if (Const.PROXY_TYPE_SOCKS.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
        System.setProperty("socksProxyHost", sProxyHost);
        System.setProperty("socksProxyPort ", Integer.toString(iProxyPort));
      }
      Authenticator.setDefault(new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          String user = Conf.getString(Const.CONF_NETWORK_PROXY_LOGIN);
          char[] pwd = UtilString.rot13(Conf.getString(Const.CONF_NETWORK_PROXY_PWD)).toCharArray();
          return new PasswordAuthentication(user, pwd);
        }
      });
    }
  }

  /**
   * Gets the proxy.
   *
   * @return the proxy
   */
  public static Proxy getProxy() {
    return proxy;
  }
}
