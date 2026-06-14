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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jajuk.services.lastfm.model.ArtistInfo;
import org.jajuk.util.log.Log;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WikipediaClient {
  private final HttpClient client;
  private final ObjectMapper mapper;

  public WikipediaClient() {
    this.client = HttpClient.newHttpClient();
    this.mapper = new ObjectMapper();
  }

  /**
   * Looking for an image for an artist.
   * 1. First try with images Last.fm (filtering placeholders).
   * 2. If no valid image found, try Wikipedia.
   */
  public void checkArtistImageUrl(ArtistInfo artistInfo) {
    // Vérifier si c'est un placeholder (hash connu)
    if (isPlaceholderImage(artistInfo.getImageUrl())) {
      Log.debug(String.format("No valid image Last.fm for '%s'. Trying Wikipedia...", artistInfo.getName()));
      String url = fetchImageFromWikimediaDirect(artistInfo.getName());
      if (url != null) {
        artistInfo.setImageUrl(url);
      }
    }
  }

  private boolean isPlaceholderImage(String url) {
    if (url == null)
      return true;
    // List of known hashes of Last.fm placeholders
    return url.contains("2a96cbd8b46e442fc41c2b86b821562f") ||
            url.contains("default_artist");
  }

  private String fetchImageFromWikimediaDirect(String artistName) {
    // Languages to try
    List<String> languagesToTry = new ArrayList<>();

    // Get System Language
    String userLang = Locale.getDefault().getLanguage(); // "en", "de", "es", etc.
    if (!"fr".equals(userLang)) {
      languagesToTry.add(userLang);
    }

    // English as fallback
    languagesToTry.add("en");

    // French as second fallback
    if (!languagesToTry.contains("fr")) {
      languagesToTry.add("fr");
    }

    // Looking for image in each language until we find one
    for (String lang : languagesToTry) {
      String imageUrl = tryFetchFromLanguage(lang, artistName);
      if (imageUrl != null) {
        Log.debug("Found image from Wikipedia " + lang + ": " + imageUrl);
        return imageUrl;
      }
    }

    return null; // No image found
  }

  private String tryFetchFromLanguage(String lang, String artistName) {
    String imageQueryUrl = null;
    try {
      // Step 1 : Looking for Wikipedia page with search API
      String searchUrl = String.format("https://%s.wikipedia.org/w/api.php?action=query&list=search&srsearch=%s" +
              "&srlimit=1&format=json&origin=*", lang, LastFmUtils.encode(artistName));

      HttpRequest searchReq = HttpRequest.newBuilder()
              .uri(URI.create(searchUrl))
              .header("User-Agent", LastFmUtils.USER_AGENT)
              .timeout(java.time.Duration.ofSeconds(10))
              .GET()
              .build();

      HttpResponse<String> searchResp = client.send(searchReq, HttpResponse.BodyHandlers.ofString());

      if (searchResp.statusCode() != 200) {
        Log.warn("Error Wikipedia search : " + searchResp.statusCode());
        return null;
      }

      JsonNode json = mapper.readTree(searchResp.body());
      JsonNode searchResults = json.get("query").get("search");

      if (searchResults == null || searchResults.isEmpty()) {
        Log.debug("No result found from Wikipedia for : " + artistName);
        return null;
      }

      // Take the first result
      String wikiTitle = searchResults.get(0).get("title").asText();

      // Get the image as a thumb with 400 px height from the page
      // The thumb is calculated by wikipedia, thus reduce Jajuk work.
      String encodedTitle = wikiTitle.replace(" ", "_");
      imageQueryUrl = String.format(
              "https://%s.wikipedia.org/w/api.php?action=query&titles=%s"+
                      "&prop=pageimages&piprop=thumbnail&pithumbsize=%s"+
                      "&format=json&origin=*",
              lang, encodedTitle, "400"
      );
      HttpRequest imageReq = HttpRequest.newBuilder()
              .uri(URI.create(imageQueryUrl))
              .header("User-Agent", LastFmUtils.USER_AGENT)
              .timeout(java.time.Duration.ofSeconds(10))
              .GET()
              .build();

      HttpResponse<String> imageResp = client.send(imageReq, HttpResponse.BodyHandlers.ofString());

      if (imageResp.statusCode() == 200) {
        JsonNode imgJson = mapper.readTree(imageResp.body());
        JsonNode pages = imgJson.get("query").get("pages");

        if (pages != null && !pages.isEmpty()) {
          JsonNode firstPage = pages.iterator().next();
          // Check that the page exists (-1 is the error pageid)
          if (firstPage.has("pageid") && firstPage.get("pageid").asInt() != -1) {
            if (firstPage.has("original")) {
              return firstPage.get("original").get("source").asText();
            } else {
              // Essaie la version thumbnail (plus petite mais toujours utile)
              if (firstPage.has("thumbnail")) {
                return firstPage.get("thumbnail").get("source").asText();
              }
            }
          }
        }
      }

    } catch (Exception e) {
      Log.warn("Error Wikimedia : " + imageQueryUrl + " : " +  e.getMessage());
    }
    return null;
  }

}
