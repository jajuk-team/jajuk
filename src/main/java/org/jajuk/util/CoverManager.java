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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.services.network.HttpClientService;
import org.jajuk.util.log.Log;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages album cover discovery from multiple external sources.
 * <p>
 * Uses a fallback strategy:
 * 1. Wikimedia Commons (via API query parsing)
 * 2. Deezer API (public endpoint)
 * </p>
 */
public final class CoverManager {

  private static final String WIKIMEDIA_QUERY_URL =
          "https://commons.wikimedia.org/w/api.php?action=query";

  /**
   * Private constructor to prevent instantiation.
   */
  private CoverManager() {
  }

  /**
   * Retrieves a list of potential cover URLs using structured artist/album parts.
   * <p>
   *
   * @param artistParts List with [artist, album] where each may be empty string
   * @param limit       Maximum number of results to return
   * @return List of URLs pointing to images. Empty list if none found.
   */
  public static List<URL> getRemoteCoversList(List<String> artistParts, int limit) throws IOException {
    // Validation: ensure we have at least some content
    if (artistParts == null || artistParts.size() < 2) {
      Log.warn("Invalid artistParts provided to getRemoteCoversList");
      return new ArrayList<>();
    }
    List<URL> results = new ArrayList<>();

    // Strategy 1: Deezer Public API (no key required for basic info)
    getRemoteCoversListFromDeezer(artistParts, limit, results);

    // Strategy 2 : Fallback - Attempt to parse and use Wikimedia Commons
    if (results.size() < limit) {
      getRemoteCoversListFromWikimedia(artistParts, limit, results);
    }


    return results;
  }

  private static void getRemoteCoversListFromDeezer(List<String> artistParts, int limit, List<URL> results) {
    try {
      List<URL> deezerResults = fetchFromDeezer(artistParts.get(0),
              artistParts.size() > 1 ? artistParts.get(1) : null,
              limit - results.size());
      if (!deezerResults.isEmpty()) {
        Log.debug("Found " + deezerResults.size() + " results via Deezer API");
        results.addAll(deezerResults);
      }
    } catch (Exception e) {
      Log.warn(MessageFormat.format("Deezer API search failed for ''{0}'': {1}", artistParts, e.getMessage()));
    }
  }

  private static void getRemoteCoversListFromWikimedia(List<String> artistParts, int limit, List<URL> results) {
    String wikiQuery = constructWikiQuery(artistParts);
    if (StringUtils.isNotBlank(wikiQuery)) {
      try {
        String artist = artistParts.get(0);
        String album = artistParts.get(1);
        List<URL> wikiResults = fetchFromWikimedia(artist, album, limit - results.size());
        if (!wikiResults.isEmpty()) {
          Log.debug("Found " + wikiResults.size() + " results via Wikimedia Commons");
          results.addAll(wikiResults);
        }
      } catch (Exception e) {
        Log.warn(MessageFormat.format("Wikimedia search failed for ''{0}'': {1}", artistParts, e.getMessage()));
      }
    }
  }

  /**
   * Constructs a Wikimedia API query URL.
   */
  public static String constructWikiQuery(List<String> parts) {
    if (parts.isEmpty())
      return "";

    // Prefer searching "Album by Artist" format for precision
    if (parts.size() == 2) {
      return URLEncoder.encode(parts.get(1) + " by " + parts.get(0), java.nio.charset.StandardCharsets.UTF_8); // e.g. "Mezzanine by Massive Attack"
    }
    // Fallback to single term
    return URLEncoder.encode(parts.get(0), java.nio.charset.StandardCharsets.UTF_8);
  }

  /**
   * Fetches image URLs from Wikimedia Commons by searching specifically in the 'File' namespace.
   * Avoids text articles (like your Norway example) by targeting ns=6.
   */
  public static List<URL> fetchFromWikimedia(String artist, String album, int limit) {
    List<URL> urls = new ArrayList<>();

    // Build query term: "Artist Album" or just "Artist"
    String queryTerm = (album != null && !album.trim().isEmpty())
            ? String.format("\"%s\" %s", artist, album)
            : artist;

    if (queryTerm.trim().isEmpty()) {
      return urls;
    }

    // Uses generator=search with gsrnamespace=6 (File namespace)
    // Uses iiurlwidth=1200 to request a reasonably sized image
    String searchUrl = String.format(
            WIKIMEDIA_QUERY_URL +
                    "&format=json" +
                    "&origin=*" +
                    "&generator=search" +
                    "&gsrnamespace=6" +
                    "&gsrlimit=%d" +
                    "&gsrsearch=%s" +
                    "&prop=imageinfo" +
                    "&iiurlwidth=1200" +
                    "&iiprop=url",
            limit,
            URLEncoder.encode(queryTerm, StandardCharsets.UTF_8)
    );

    try {
      Log.debug("Fetching from Wikimedia Commons: " + searchUrl);
      String json = HttpClientService.getInstance().getContent(searchUrl);

      if (json == null || json.isEmpty()) {
        return urls;
      }

      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(json);

      // Handle warnings (optional but good for debugging)
      if (rootNode.has("warnings")) {
        Log.debug("Wikimedia API warnings: " + rootNode.get("warnings").toString());
      }

      JsonNode pagesNode = rootNode.path("query").path("pages");
      if (pagesNode.isMissingNode() || pagesNode.isNull()) {
        return urls;
      }

      Iterator<Map.Entry<String, JsonNode>> fields = pagesNode.properties().iterator();
      int count = 0;

      while (fields.hasNext() && count < limit) {
        Map.Entry<String, JsonNode> entry = fields.next();
        JsonNode page = entry.getValue();

        // Vérification standard
        if (!page.has("title"))
          continue;
        String title = page.get("title").asText();
        if (!title.startsWith("File:"))
          continue;

        Log.debug("Processing file: " + title);

        // Get URL From JSON data
        JsonNode imageInfo = page.path("imageinfo");
        if (imageInfo.isArray() && imageInfo.size() > 0) {
          JsonNode imgNode = imageInfo.get(0);
          String imageUrl;
          if (imgNode.has("thumburl")) {
            // Use thumbnail URL if provided (respectes iiurlwidth constraint)
            imageUrl = imgNode.get("thumburl").asText();
            Log.debug("Using thumbnail URL (resized): " + imageUrl);
          } else {
            // Fallback
            imageUrl = imgNode.path("url").asText();
          }
          if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
              // No need to replace \\/ because Jackson handles JSON decoding
              // But let's be safe against raw string issues if any
              String cleanUrl = imageUrl.replace("\\/", "/");

              urls.add(new URL(cleanUrl));
              count++;
              if (count >= limit) {
                break;
              }
            } catch (Exception e) {
              Log.warn("Invalid URL: " + imageUrl);
            }
          }
        }
      }
      if (count == 0) {
        Log.debug("No image files found for: " + queryTerm + " on Wikimedia Commons");
      } else {
        Log.info("Found " + count + " image(s) from Wikimedia Commons for: " + queryTerm);
      }
    } catch (Exception e) {
      Log.warn("Error fetching from Wikimedia Commons: " + e.getMessage() + " : " + e.getMessage());
    }

    return urls;
  }

  /**
   * Fetches high-res album art from Deezer's public API.
   * Endpoints are stable and reliable.
   */
  public static List<URL> fetchFromDeezer(String artist, String album, int limit) throws Exception {
    List<URL> urls = new ArrayList<>();

    StringBuilder query = new StringBuilder();
    query.append("https://api.deezer.com/search/album?q=");

    if (StringUtils.isNotBlank(album)) {
      query.append(URLEncoder.encode(album + " " + artist, StandardCharsets.UTF_8));
    } else {
      query.append(URLEncoder.encode(artist, StandardCharsets.UTF_8));
    }

    String jsonResponse = HttpClientService.getInstance().getContent(query.toString());
    if (jsonResponse == null) {
      return urls;
    }
    // Deezer returns: {"data": [{"cover_xl": "http://..."}]}
    // Extract cover_xl URL
    Pattern coverPattern = Pattern.compile("\"cover_xl\":\"([^\"]+)\"");
    Matcher matcher = coverPattern.matcher(jsonResponse);

    while (matcher.find()) {
      String rawUrl = matcher.group(1);
      // Replace escaped slashes (/ or \/) with actual slashes
      // In raw JSON strings read as plain text, \/ appears literally.
      String coverUrl = rawUrl.replace("\\/", "/")
              .replace("\\u002F", "/"); // Sometimes encoded as unicode

      if (coverUrl.startsWith("http")) {
        urls.add(new URL(coverUrl));
        if (urls.size() >= limit) {
          break;
        }
      }
    }

    return urls;
  }

  /**
   * Fetches image URLs from Wikimedia Commons by searching specifically in the 'File' namespace.
   * SINGLE REQUEST APPROACH: All data retrieved in one call with prop=imageinfo.
   */
  public static List<URL> fetchFromWikimediaOld(String artist, String album, int limit) {
    List<URL> urls = new ArrayList<>();

    String queryTerm = (album != null && !album.trim().isEmpty())
            ? String.format("%s %s", artist, album)
            : artist;

    if (queryTerm.trim().isEmpty()) {
      return urls;
    }

    // CRUCIAL CHANGE: Add &prop=imageinfo&iiprop=url to primary request
    String searchUrl = String.format(
            WIKIMEDIA_QUERY_URL +
                    "&format=json&origin=*&generator=search&gsrnamespace=6&gsrlimit=%d" +
                    "&gsrsearch=%s&prop=imageinfo&iiprop=url",
            limit,
            URLEncoder.encode(queryTerm, StandardCharsets.UTF_8)
    );

    try {
      Log.debug("Fetching from Wikimedia Commons: " + searchUrl);
      String json = HttpClientService.getInstance().getContent(searchUrl);

      if (json == null || json.isEmpty()) {
        return urls;
      }

      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(json);

      JsonNode pagesNode = rootNode.path("query").path("pages");

      if (pagesNode.isMissingNode() || pagesNode.isNull() || pagesNode.size() == 0) {
        Log.debug("No results found on Wikimedia Commons for: " + queryTerm);
        return urls;
      }

      int count = 0;
      Iterator<Map.Entry<String, JsonNode>> fields = pagesNode.properties().iterator();

      while (fields.hasNext() && count < limit) {
        Map.Entry<String, JsonNode> entry = fields.next();
        JsonNode page = entry.getValue();

        String title = page.path("title").asText();
        if (!title.startsWith("File:")) {
          continue;
        }

        Log.debug("Processing file: " + title);

        // IMAGEINFO NOW DIRECTLY AVAILABLE IN THIS RESPONSE
        JsonNode imageInfo = page.path("imageinfo");
        if (imageInfo.isArray() && imageInfo.size() > 0) {
          String imageUrl = imageInfo.get(0).path("url").asText();

          if (!imageUrl.isEmpty()) {
            String cleanUrl = imageUrl.replace("\\/", "/");

            try {
              urls.add(new URL(cleanUrl));
              count++;

              if (count >= limit)
                break;

              Log.info("Successfully extracted image URL for: " + title);
            } catch (Exception e) {
              Log.warn("Invalid URL constructed: " + cleanUrl + " : " + e.getMessage());
            }
          } else {
            Log.debug("No URL found in imageinfo for: " + title);
          }
        } else {
          Log.debug("No imageinfo property present for: " + title);
        }
      }

      if (count == 0) {
        Log.debug("No image files found for: " + queryTerm + " on Wikimedia Commons");
      } else {
        Log.info("Found " + count + " image(s) from Wikimedia Commons for: " + queryTerm);
      }
    } catch (Exception e) {
      Log.warn("Error fetching from Wikimedia Commons: " + e.getMessage());
    }

    return urls;
  }
}