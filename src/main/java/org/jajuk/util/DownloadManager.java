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

import org.jajuk.services.core.SessionService;
import org.jajuk.services.network.HttpClientService;
import org.jajuk.util.log.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.concurrent.Semaphore;

/**
 * Manages network downloads with rate limiting and caching.
 * Uses HttpClientService for all HTTP operations.
 */
public final class DownloadManager {

  // Rate limiting configuration
  private static final int MAX_CONCURRENT_REQUESTS = 3; // Limit simultaneous requests
  private static final long MIN_DELAY_BETWEEN_REQUESTS_MS = 2500; // Delay min between each request, 2.5s for wikimedia
  private static final int MAX_RETRIES_FOR_429 = 4; // Number of retries for 429 response status
  private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 second

  // Semaphore to limit active threads
  private static final Semaphore rateLimiter = new Semaphore(MAX_CONCURRENT_REQUESTS);
  private static volatile long lastRequestTime = 0;

  // Content validation limits
  private static final long MAX_CONTENT_LENGTH = 10 * 1024 * 1024; // 10MB max

  /** Private constructor to avoid instantiation. */
  private DownloadManager() {
  }

  /** Acquires a permit and ensures minimum delay between requests. */
  private static void acquireRateLimit() throws IOException {
    try {
      boolean acquired = rateLimiter.tryAcquire(15, java.util.concurrent.TimeUnit.SECONDS);
      if (!acquired)
        throw new IOException("Rate limiter timeout");

      long now = System.currentTimeMillis();
      long elapsed = now - lastRequestTime;
      if (elapsed < MIN_DELAY_BETWEEN_REQUESTS_MS) {
        Thread.sleep(MIN_DELAY_BETWEEN_REQUESTS_MS - elapsed);
      }
      lastRequestTime = System.currentTimeMillis();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Download interrupted", e);
    }
  }

  /** Downloads a resource with rate limiting and retry logic for 429 errors. */
  public static void download(URL url, File fDestination) throws IOException {
    if (!HttpClientService.getInstance().isInternetAccessAllowed()) {
      return;
    }
    if (url == null || !isValidProtocol(url)) {
      throw new IOException("Invalid URL: " + (url != null ? url.toString() : "null"));
    }
    String urlString = url.toString();
    int attempt = 0;
    long retryDelay = INITIAL_RETRY_DELAY_MS;

    while (attempt < MAX_RETRIES_FOR_429) {
      acquireRateLimit();

      try {
        var headResponse = HttpClientService.getInstance().headRequest(urlString);

        if (headResponse == null) {
          throw new IOException("Request failed or access disabled");
        }

        if (headResponse.statusCode() == 429) {
          attempt++;
          if (attempt >= MAX_RETRIES_FOR_429) {
            throw new IOException("Server returned 429 after " + MAX_RETRIES_FOR_429 + " attempts");
          }

          // Respect Retry-After header if present
          String retryAfter = headResponse.headers().firstValue("Retry-After").orElse(null);
          if (retryAfter != null) {
            try {
              retryDelay = Long.parseLong(retryAfter) * 1000;
            } catch (NumberFormatException e) {
              // Ignore, use default exponential backoff
            }
          }

          Log.warn("Rate limit hit (429). Retrying in " + retryDelay + "ms (Attempt " + attempt + ")");

          // Sleep for retry delay - MUST handle InterruptedException here
          try {
            Thread.sleep(retryDelay);
          } catch (InterruptedException e) {
            // Restore interrupt status and abort
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted during retry wait", e);
          }

          retryDelay *= 2;
          continue;
        }

        if (headResponse.statusCode() != 200) {
          throw new IOException("HTTP Error: " + headResponse.statusCode() + " url=" + urlString);
        }

        long contentLength = headResponse.headers().firstValue("content-length")
                .map(Long::parseLong).orElse(-1L);
        if (contentLength > MAX_CONTENT_LENGTH) {
          throw new IOException("File too large (" + contentLength + " bytes)");
        }

        HttpClientService.getInstance().downloadToFile(urlString, fDestination);
        return;

      } catch (IOException e) {
        if (fDestination.exists())
          fDestination.delete();
        if (e.getMessage() != null && e.getMessage().contains("429")) {
          attempt++;
          if (attempt >= MAX_RETRIES_FOR_429)
            throw e;

          try {
            Thread.sleep(retryDelay);
          } catch (InterruptedException ie) {
            // Restore interrupt status and abort
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted during retry wait", e);
          }

          retryDelay *= 2;
          continue;
        }
        throw e;
      } finally {
        rateLimiter.release();
      }
    }
    throw new IOException("Download failed after retries for URL: " + url);
  }

  /** Validates URL protocol is http or https. */
  private static boolean isValidProtocol(URL url) {
    String p = url.getProtocol();
    return "http".equalsIgnoreCase(p) || "https".equalsIgnoreCase(p);
  }

  /** Downloads a resource to cache, returning the file if successful. */
  public static File downloadToCache(URL url) throws IOException {
    if (!HttpClientService.getInstance().isInternetAccessAllowed()) {
      return null;
    }

    File file = SessionService.getCachePath(url);
    synchronized (file.getName().intern()) {
      if (file.exists() && file.length() > 0) {
        return file;
      }
      download(url, file);
      return file;
    }
  }

  /** Downloads text content from a URL. */
  public static String downloadText(URL url) throws IOException {
    if (!HttpClientService.getInstance().isInternetAccessAllowed()) {
      return null;
    }
    if (url == null || !isValidProtocol(url)) {
      throw new IOException("Invalid URL: " + (url != null ? url.toString() : "null"));
    }
    String urlString = url.toString();
    int attempt = 0;
    long retryDelay = INITIAL_RETRY_DELAY_MS;

    while (attempt < MAX_RETRIES_FOR_429) {
      acquireRateLimit();

      try {
        HttpResponse<String> response = HttpClientService.getInstance().executeGetRequest(urlString);

        if (response == null) {
          throw new IOException("Request failed or internet access disabled");
        }

        if (response.statusCode() == 429) {
          attempt++;
          if (attempt >= MAX_RETRIES_FOR_429) {
            throw new IOException("Server returned 429 after " + MAX_RETRIES_FOR_429 + " attempts");
          }

          // Respect Retry-After header if present
          String retryAfter = response.headers().firstValue("Retry-After").orElse(null);
          if (retryAfter != null) {
            try {
              retryDelay = Long.parseLong(retryAfter) * 1000;
            } catch (NumberFormatException e) {
              // Ignore, use default exponential backoff
            }
          }

          Log.warn("Rate limit hit (429) for text download. Retrying in " + retryDelay + "ms (Attempt " + attempt + ")");

          // Sleep for retry delay - MUST handle InterruptedException here
          try {
            Thread.sleep(retryDelay);
          } catch (InterruptedException ie) {
            // Restore interrupt status and abort
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted during retry wait", ie);
          }

          retryDelay *= 2;
          continue;
        }

        if (response.statusCode() != 200) {
          throw new IOException("HTTP Error: " + response.statusCode() + " url=" + urlString);
        }

        return response.body();

      } catch (IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("429")) {
          attempt++;
          if (attempt >= MAX_RETRIES_FOR_429)
            throw e;

          try {
            Thread.sleep(retryDelay);
          } catch (InterruptedException ie) {
            // Restore interrupt status and abort
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted during retry wait", ie);
          }

          retryDelay *= 2;
          continue;
        }
        throw e;
      } finally {
        rateLimiter.release();
      }
    }
    throw new IOException("Download failed after retries for URL: " + url);
  }

  /** Reads text from a cached file. */
  public static String getTextFromCachedFile(URL url, String encoding) throws IOException {
    if (!HttpClientService.getInstance().isInternetAccessAllowed()) {
      return null;
    }
    File file = downloadToCache(url);

    // Defensive programming: ensure the file exists before attempting read
    // This replaces the 'assert' which is often disabled in production (-da flag)
    if (file == null || !file.exists() || file.length() == 0) {
      Log.warn("Cached file not found or empty for URL: " + url);
      return null;
    }

    // Java 11+ optimal solution: reads entire file into memory at once
    // Using Charset.forName(encoding) ensures correct resolution of the encoding name
    return Files.readString(file.toPath(), java.nio.charset.Charset.forName(encoding));
  }

}