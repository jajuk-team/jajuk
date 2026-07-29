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
package org.jajuk.services.webradio;

import org.jajuk.services.network.HttpClientService;
import org.jajuk.util.error.WebRadioUnavailableException;
import org.jajuk.util.log.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Utility class to resolve web radio URLs including redirects and playlist parsing.
 * Handles common radio formats: .m3u, .pls, .asx, and direct streams.
 */
public final class WebRadioUrlResolver {

  private static final int DEFAULT_TIMEOUT_MS = 15000;

  private static final Pattern PLAIN_STREAM_PATTERN =
          Pattern.compile("^(https?://\\S+\\.(mp3|aac|ogg|wav|flac))$", Pattern.CASE_INSENSITIVE);

  /** Private constructor for utility class. */
  private WebRadioUrlResolver() {
  }

  /**
   * Resolves a radio URL, following redirects and parsing playlist files if necessary.
   * Throws WebRadioUnavailableException with user-friendly message on failure.
   *
   * @param originalUrl the original radio URL
   * @return the resolved direct stream URL
   *
   * @throws org.jajuk.util.error.WebRadioUnavailableException if resolution fails (includes user message)
   */
  public static String resolveUrl(String originalUrl) throws WebRadioUnavailableException {
    return resolveUrl(originalUrl, DEFAULT_TIMEOUT_MS);
  }

  public static String resolveUrl(String originalUrl, int maxTimeoutMs) throws WebRadioUnavailableException {
    if (originalUrl == null || originalUrl.trim().isEmpty()) {
      throw new WebRadioUnavailableException("unknown", "Empty radio URL provided");
    }

    String currentUrl = originalUrl.trim();

    // Force HTTPS
    if (currentUrl.startsWith("http://")) {
      currentUrl = currentUrl.replaceFirst("^http://", "https://");
      Log.info("[RADIO-RESOLVE] Upgraded to HTTPS: " + currentUrl);
    }

    // Check direct stream
    if (isDirectStream(currentUrl)) {
      Log.debug("[RADIO-RESOLVE] Direct stream detected: " + currentUrl);
      return currentUrl;
    }

    Log.info("[RADIO-RESOLVE] Resolving URL with HEAD request: " + currentUrl);
    HttpClientService httpClient = HttpClientService.getInstance();

    try {
      // Use HEAD REQUEST TO AVOID INFINITE STREAM TIMEOUT
      HttpResponse<Void> headResponse = httpClient.headRequest(currentUrl);
      int statusCode = headResponse.statusCode();

      if (statusCode == 200 || statusCode == 301 || statusCode == 302) {
        String finalUrl = headResponse.uri().toString();

        // Check if playlist file
        if (finalUrl.toLowerCase().matches(".*\\.(m3u|pls|asx)$")) {
          Log.debug("[RADIO-RESOLVE] Detected playlist, fetching content...");

          // NOW use GET only for playlist files (not streams)
          String content = httpClient.readUrl(finalUrl);
          if (content == null) {
            throw new WebRadioUnavailableException(originalUrl, "Failed to fetch playlist content");
          }

          String extractedUrl = extractStreamUrlFromPlaylist(content, finalUrl);
          if (extractedUrl != null) {
            Log.info("[RADIO-RESOLVE] Extracted stream URL: " + extractedUrl);
            // Recursive call to resolve extracted URL
            return resolveUrl(extractedUrl, maxTimeoutMs);
          } else {
            throw new WebRadioUnavailableException(originalUrl, "Playlist contains no valid stream URL");
          }
        }

        Log.info("[RADIO-RESOLVE] Successfully resolved to: " + finalUrl);
        return finalUrl;
      } else {
        throw new WebRadioUnavailableException(originalUrl,
                "HTTP error " + statusCode + " from radio server");
      }

    } catch (IOException e) {
      Log.error("[RADIO-RESOLVE] HEAD request failed: " + e.getMessage());
      throw new WebRadioUnavailableException(originalUrl,
              "Unable to reach radio server: " + e.getMessage());
    } catch (WebRadioUnavailableException e) {
      throw e;
    }
  }

  /**
   * Extract stream URL from playlist content.
   */
  private static String extractStreamUrlFromPlaylist(String content, String playlistUrl) {
    if (content == null || content.trim().isEmpty()) {
      return null;
    }

    Log.debug("[RADIO-RESOLVE] Playlist content preview (first 500 chars): " +
            content.substring(0, Math.min(500, content.length())));

    try (Scanner scanner = new Scanner(content)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();

        // Skip comments and empty lines
        if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
          continue;
        }

        // M3U format: direct URL on a line
        if (line.startsWith("http")) {
          Log.info("[RADIO-RESOLVE] Extracted M3U stream URL: " + line);
          return line;
        }

        // PLS format: File1=http://...
        if (line.toLowerCase().startsWith("file")) {
          int eqIndex = line.indexOf('=');
          if (eqIndex != -1) {
            String value = line.substring(eqIndex + 1).trim();
            if (value.startsWith("http")) {
              Log.info("[RADIO-RESOLVE] Extracted PLS stream URL: " + value);
              return value;
            }
          }
        }

        // ASX format: <REF HREF="...">
        if (line.toLowerCase().contains("<ref href")) {
          int start = line.indexOf('"');
          int end = line.lastIndexOf('"');
          if (start != -1 && end > start) {
            String value = line.substring(start + 1, end);
            if (value.startsWith("http")) {
              Log.info("[RADIO-RESOLVE] Extracted ASX stream URL: " + value);
              return value;
            }
          }
        }
      }
    }

    Log.warn("[RADIO-RESOLVE] No stream URL found in playlist content");
    return null;
  }

  /**
   * Checks if a URL is likely a direct stream or needs resolution.
   *
   * @param url the URL to check
   * @return true if URL points directly to an audio file
   */
  public static boolean isDirectStream(String url) {
    if (url == null || url.isEmpty()) {
      return false;
    }
    return PLAIN_STREAM_PATTERN.matcher(url).matches()
            && !url.toLowerCase().matches(".*\\.(m3u|pls|asx|m3u8)$");
  }
}