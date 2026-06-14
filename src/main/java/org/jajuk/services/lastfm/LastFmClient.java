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
import org.jajuk.base.Track;
import org.jajuk.services.lastfm.model.*;
import org.jajuk.services.lastfm.scrobble.ScrobblerException;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class LastFmClient {
  /*
   * DO NOT USE THESE KEYS FOR OTHER APPLICATIONS THAN Jajuk!
   */
  /** The Constant API_KEY. */
  public static final String API_KEY = "8b8fc3115c8e40531393af7225ff5ee4";
  public static final String INVALID_API_KEY_FOR_LAST_FM = "Invalid API key for Last.fm. Please check your configuration.";

  private final HttpClient httpClient;
  private final ObjectMapper mapper;

  public LastFmClient() {
    this.httpClient = HttpClient.newHttpClient();
    this.mapper = new ObjectMapper();
  }

  public String getApiKey() {
    String personalLastFmApiKey = Conf.getString(Const.CONF_LASTFM_API_KEY);
    if (personalLastFmApiKey != null && !personalLastFmApiKey.isBlank()) {
      return personalLastFmApiKey;
    } else {
      // Jajuk Key - DO NOT USE THIS KEY FOR OTHER APPLICATIONS THAN JAJUK!
      Log.debug("Using default Jajuk API key for Last.fm. Please set your own API key in the configuration for better performance and to avoid hitting rate limits.");
      return API_KEY;
    }
  }

  /**
   * Search album info.
   *
   * @param artist the artist name
   * @param album  the album title
   * @return the album
   */
  public AlbumInfo getAlbumInfo(String artist, String album) throws LastFmInvalidKeyException, Exception {
    String url = LastFmUtils.BASE_URL +
            "?method=album.getinfo" +
            "&artist=" + LastFmUtils.encode(artist) +
            "&album=" + LastFmUtils.encode(album) +
            "&api_key=" + getApiKey() +
            "&format=json";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      if (response.statusCode() == 403) {
        throw new LastFmInvalidKeyException(INVALID_API_KEY_FOR_LAST_FM);
      } else {
        throw new RuntimeException("Erreur API Last.fm: " + response.statusCode() + " - " + response.body());
      }
    }

    // Last.fm returns a root object with a key "album"
    var rootNode = mapper.readTree(response.body());
    var albumNode = rootNode.get("album");

    if (albumNode == null) {
      throw new RuntimeException("Réponse invalide : champ 'album' manquant");
    }

    // Instancation of LastFmAlbum object
    LastFmAlbum lastFmAlbum = new LastFmAlbum();

    // Mapping manuel des champs simples
    lastFmAlbum.setId(albumNode.get("mbid").asText());
    lastFmAlbum.setArtist(albumNode.get("artist").asText());
    lastFmAlbum.setTitle(albumNode.get("name").asText());
    lastFmAlbum.setUrl(albumNode.get("url").asText());
    lastFmAlbum.setReleaseDateString(albumNode.get("releasedate") != null ? albumNode.get("releasedate").asText() : null);

    // Mapping wiki
    LastFmAlbum.WikiData wikiData = new LastFmAlbum.WikiData();
    if (albumNode.has("wiki")) {
      JsonNode wikiNode = albumNode.get("wiki");
      wikiData.setPublished(wikiNode.get("published").asText()); // This date it the wiki page publication date, not the release date of the album
      wikiData.setSummary(wikiNode.get("summary").asText());
      wikiData.setContent(wikiNode.get("content").asText());
    }

    // Mapping images
    List<LastFmAlbum.ImageData> imageList = new ArrayList<>();
    if (albumNode.has("image")) {
      for (JsonNode img : albumNode.get("image")) {
        LastFmAlbum.ImageData imageData = new LastFmAlbum.ImageData();
        imageData.setSize(img.get("size").asText());
        imageData.setUrl(img.get("#text").asText());
        imageList.add(imageData);
      }
    }
    lastFmAlbum.setImages(imageList);

    // Mapping tracks
    List<TrackInfo> trackList = new ArrayList<>();
    if (albumNode.has("tracks") && albumNode.get("tracks").has("track")) {
      for (JsonNode trackNode : albumNode.get("tracks").get("track")) {
        LastFmTrack track = new LastFmTrack();
        track.setTitle(trackNode.get("name").asText());
        track.setUrl(trackNode.get("url").asText());
        trackList.add(track);
      }
    }
    lastFmAlbum.setTracks(trackList);

    return lastFmAlbum;
  }

  /**
   * Searches for an artist and returns the top match.
   *
   * @return the artist
   */
  public ArtistInfo getArtist(String artistName) throws LastFmInvalidKeyException,
          Exception {
    // Building URL for artist.search
    // Limitation to one result to get better result
    String url = LastFmUtils.BASE_URL +
            "?method=artist.search" +
            "&artist=" + LastFmUtils.encode(artistName) +
            "&limit=1" +
            "&api_key=" + getApiKey() +
            "&format=json";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      if (response.statusCode() == 403) {
        throw new LastFmInvalidKeyException(INVALID_API_KEY_FOR_LAST_FM);
      } else {
        throw new RuntimeException("Error API Last.fm: " + response.statusCode() + " - " + response.body());
      }
    }

    JsonNode rootNode = mapper.readTree(response.body());

    // Checking results
    if (!rootNode.has("results") || !rootNode.get("results").has("artistmatches")) {
      throw new RuntimeException("No results found for artist: " + artistName);
    }

    JsonNode artistMatches = rootNode.get("results").get("artistmatches");

    // Checking minimum of one artist
    if (!artistMatches.has("artist") || artistMatches.get("artist").isArray() && artistMatches.get("artist").isEmpty()) {
      // Cas où artist est un objet unique ou une liste vide
      if (artistMatches.get("artist").isArray() && artistMatches.get("artist").isEmpty()) {
        throw new RuntimeException("No results found for artist: " + artistName);
      }
    }

    // Last.fm often returns an array even for limit=1
    JsonNode artistNode;
    if (artistMatches.get("artist").isArray()) {
      if (artistMatches.get("artist").isEmpty()) {
        throw new RuntimeException("No results found for artist: " + artistName);
      }
      artistNode = artistMatches.get("artist").get(0);
    } else {
      artistNode = artistMatches.get("artist");
    }

    // Instanciation of LastFmArtist object
    LastFmArtist artist = new LastFmArtist();

    // Simple mapping of simple fields
    if (artistNode.has("name")) {
      artist.setName(artistNode.get("name").asText());
    }
    artist.setId(artistNode.get("mbid").asText());

    String match;
    if (artistNode.has("match")) {
      // 'match' is a float like "98.5"
      match = artistNode.get("match").asText();
    } else {
      // if not found, we assume the result is exact 100%
      match = "100";
    }
    artist.setMatch(match);

    if (artistNode.has("url")) {
      artist.setUrl(artistNode.get("url").asText());
    }

    // Manual extraction of URL image
    // JSON Structure : "image": [ {"size": "small", "#text": "url"}, ... ]
    String extractedImageUrl = null;
    if (artistNode.has("image") && artistNode.get("image").isArray()) {
      for (JsonNode imgNode : artistNode.get("image")) {
        String size = imgNode.has("size") ? imgNode.get("size").asText() : "";
        String text = imgNode.has("#text") ? imgNode.get("#text").asText() : "";

        // Looking for size "large" or "medium"
        if ("large".equalsIgnoreCase(size) || "medium".equalsIgnoreCase(size)) {
          extractedImageUrl = text;
          break; // On prend le premier trouvé (priorité à large si l'ordre est respecté)
        }
      }

      // Fallback: if no size found, take the last one (often the greatest one in ascending order)
      if (extractedImageUrl == null && !artistNode.get("image").isEmpty()) {
        JsonNode lastImg = artistNode.get("image").get(artistNode.get("image").size() - 1);
        if (lastImg.has("#text")) {
          extractedImageUrl = lastImg.get("#text").asText();
        }
      }
    }

    artist.setImageUrl(extractedImageUrl);

    // Marked as available
    artist.setAvailable(true);

    return artist;
  }

  /**
   * Fetches detailed artist information from Last.fm using artist.getinfo.
   * Includes biography, tags, similar artists, and top tracks.
   *
   * @param artistName the name of the artist (or MBID if preferred)
   * @param useMbid    whether the input is an MBID instead of artist name
   * @return LastFmArtistDetail with complete artist information
   *
   * @throws Exception if API call fails or no results found
   */
  public LastFmArtistDetail getArtistDetail(String artistName, boolean useMbid) throws LastFmInvalidKeyException, Exception {
    // Validate input
    if (artistName == null || artistName.trim().isEmpty()) {
      throw new IllegalArgumentException("Artist name or MBID cannot be null or empty");
    }

    // Building URL for artist.getinfo
    String url = LastFmUtils.BASE_URL +
            "?method=artist.getinfo" +
            "&" + (useMbid ? "mbid=" : "artist=") + LastFmUtils.encode(artistName) +
            "&api_key=" + getApiKey() +
            "&format=json";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      if (response.statusCode() == 403) {
        throw new LastFmInvalidKeyException(INVALID_API_KEY_FOR_LAST_FM);
      } else {
        throw new RuntimeException("Error API Last.fm (getinfo): " + response.statusCode() + " - " + response.body());
      }
    }

    JsonNode rootNode = mapper.readTree(response.body());

    // Checking results structure: root -> artist
    if (!rootNode.has("artist")) {
      throw new RuntimeException("No artist found: " + artistName);
    }

    JsonNode artistNode = rootNode.get("artist");

    // Instanciation of LastFmArtistDetail object
    LastFmArtistDetail detail = new LastFmArtistDetail();

    // ==================== Basic Fields ====================
    if (artistNode.has("name")) {
      detail.setName(artistNode.get("name").asText());
    }

    if (artistNode.has("mbid")) {
      detail.setId(artistNode.get("mbid").asText());
    }

    if (artistNode.has("url")) {
      detail.setUrl(artistNode.get("url").asText());
    }

    if (artistNode.has("listeners")) {
      detail.setListeners(artistNode.get("listeners").asText());
    }

    if (artistNode.has("playcount")) {
      detail.setPlaycount(artistNode.get("playcount").asText());
    }

    // ==================== Image Extraction ====================
    // JSON Structure: "image": [{"size": "small", "#text": "url"}, ...]
    String extractedImageUrl = null;
    if (artistNode.has("image") && artistNode.get("image").isArray()) {
      for (JsonNode imgNode : artistNode.get("image")) {
        String size = imgNode.has("size") ? imgNode.get("size").asText() : "";
        String text = imgNode.has("#text") ? imgNode.get("#text").asText() : "";

        // Looking for size "extralarge" or "large"
        if ("extralarge".equalsIgnoreCase(size)) {
          extractedImageUrl = text;
          break;
        } else if ("large".equalsIgnoreCase(size) && extractedImageUrl == null) {
          extractedImageUrl = text;
        }
      }

      // Fallback: take the last one if no large found
      if (extractedImageUrl == null && !artistNode.get("image").isEmpty()) {
        JsonNode lastImg = artistNode.get("image").get(artistNode.get("image").size() - 1);
        if (lastImg.has("#text")) {
          extractedImageUrl = lastImg.get("#text").asText();
        }
      }
    }
    detail.setImageUrl(extractedImageUrl);
    detail.setAvailable(true);

    // ==================== Wiki/Biography ====================
    if (artistNode.has("bio")) {
      JsonNode bioNode = artistNode.get("bio");

      if (bioNode.has("summary")) {
        detail.setWikiSummary(bioNode.get("summary").asText());
      }

      if (bioNode.has("content")) {
        detail.setWikiContent(bioNode.get("content").asText());
      }

      if (bioNode.has("published")) {
        detail.setWikiPublished(bioNode.get("published").asText());
      }
    }

    // ==================== Tags ====================
    List<LastFmArtistDetail.TagInfo> tagList = new ArrayList<>();
    if (artistNode.has("tags") && artistNode.get("tags").has("tag")) {
      for (JsonNode tagNode : artistNode.get("tags").get("tag")) {
        LastFmArtistDetail.TagInfo tag = new LastFmArtistDetail.TagInfo();
        if (tagNode.has("name")) {
          tag.setName(tagNode.get("name").asText());
        }
        if (tagNode.has("url")) {
          tag.setUrl(tagNode.get("url").asText());
        }
        if (tagNode.has("count")) {
          tag.setCount(tagNode.get("count").asText());
        }
        tagList.add(tag);
      }
    }
    detail.setTags(tagList);

    // ==================== Similar Artists ====================
    List<LastFmArtistDetail.SimilarArtistInfo> similarList = new ArrayList<>();
    if (artistNode.has("similar") && artistNode.get("similar").has("artist")) {
      JsonNode similarNode = artistNode.get("similar").get("artist");

      // Handle both single object and array
      if (similarNode.isArray()) {
        for (JsonNode simNode : similarNode) {
          LastFmArtistDetail.SimilarArtistInfo similar = new LastFmArtistDetail.SimilarArtistInfo();
          if (simNode.has("name")) {
            similar.setName(simNode.get("name").asText());
          }
          if (simNode.has("url")) {
            similar.setUrl(simNode.get("url").asText());
          }
          if (simNode.has("match")) {
            similar.setMatch(simNode.get("match").asText());
          }
          // Similar artists may also have images
          if (simNode.has("image") && simNode.get("image").isArray()) {
            for (JsonNode imgNode : simNode.get("image")) {
              String size = imgNode.has("size") ? imgNode.get("size").asText() : "";
              if ("large".equalsIgnoreCase(size) || "extralarge".equalsIgnoreCase(size)) {
                similar.setImageUrl(imgNode.has("#text") ? imgNode.get("#text").asText() : null);
                break;
              }
            }
          }
          similarList.add(similar);
        }
      } else if (similarNode.isObject()) {
        // Single similar artist
        LastFmArtistDetail.SimilarArtistInfo similar = new LastFmArtistDetail.SimilarArtistInfo();
        if (similarNode.has("name")) {
          similar.setName(similarNode.get("name").asText());
        }
        if (similarNode.has("url")) {
          similar.setUrl(similarNode.get("url").asText());
        }
        if (similarNode.has("match")) {
          similar.setMatch(similarNode.get("match").asText());
        }
        similarList.add(similar);
      }
    }
    detail.setSimilarArtists(similarList);

    // ==================== Top Tracks ====================
    List<TrackInfo> trackList = new ArrayList<>();
    if (artistNode.has("toptracks") && artistNode.get("toptracks").has("track")) {
      for (JsonNode trackNode : artistNode.get("toptracks").get("track")) {
        LastFmTrack track = new LastFmTrack();
        if (trackNode.has("name")) {
          track.setTitle(trackNode.get("name").asText());
        }
        if (trackNode.has("url")) {
          track.setUrl(trackNode.get("url").asText());
        }
        trackList.add(track);
      }
    }
    detail.setTopTracks(trackList);

    return detail;
  }

  /**
   * Fetches images associated with an artist using their MusicBrainz ID (MBID).
   * Uses the Last.fm artist.getImages method.
   *
   * @param mbid the MusicBrainz ID of the artist
   * @return a list of LastFmImage objects containing image metadata
   *
   * @throws Exception if the API call fails or the MBID is invalid
   */
  public List<LastFmImage> getLastFmImages(String mbid, int limit) throws LastFmInvalidKeyException,
          Exception {
    // Validate input
    if (mbid == null || mbid.trim().isEmpty()) {
      throw new IllegalArgumentException("MBID cannot be null or empty");
    }

    // Building URL for artist.getimages
    // Note: Last.fm uses 'mbid' parameter for this method
    String url = LastFmUtils.BASE_URL +
            "?method=artist.getimages" +
            "&mbid=" + LastFmUtils.encode(mbid) +
            "&limit=" + limit +
            "&api_key=" + getApiKey() +
            "&format=json";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      if (response.statusCode() == 403) {
        throw new LastFmInvalidKeyException(INVALID_API_KEY_FOR_LAST_FM);
      } else {
        throw new RuntimeException("Error API Last.fm (getimages): " + response.statusCode() + " - " + response.body());
      }
    }

    JsonNode rootNode = mapper.readTree(response.body());

    // Checking results structure: root -> images -> image (array)
    if (!rootNode.has("images") || !rootNode.get("images").has("image")) {
      // It's possible for an artist to have no images
      return new ArrayList<>();
    }

    JsonNode imagesNode = rootNode.get("images").get("image");

    // Ensure we are dealing with an array (Last.fm returns an array even for single items sometimes)
    if (!imagesNode.isArray()) {
      // If it's a single object, wrap it in a list manually
      List<LastFmImage> singleImageList = new ArrayList<>();
      LastFmImage img = parseImageNode(imagesNode);
      if (img != null) {
        singleImageList.add(img);
      }
      return singleImageList;
    }

    List<LastFmImage> imageList = new ArrayList<>();
    for (JsonNode imgNode : imagesNode) {
      LastFmImage image = parseImageNode(imgNode);
      if (image != null) {
        imageList.add(image);
      }
    }

    return imageList;
  }

  /**
   * Helper method to map a JSON node to a LastFmImage object.
   *
   * @param node the JSON node representing an image
   * @return a populated LastFmImage object or null if parsing fails
   */
  private LastFmImage parseImageNode(JsonNode node) {
    if (node == null || !node.isObject()) {
      return null;
    }

    LastFmImage image = new LastFmImage();

    // Map 'title'
    if (node.has("title")) {
      image.setTitle(node.get("title").asText());
    }

    // Map 'url'
    if (node.has("url")) {
      image.setUrl(node.get("url").asText());
    }

    // Map 'format'
    if (node.has("format")) {
      image.setFormat(node.get("format").asText());
    }

    // Map 'owner'
    if (node.has("owner")) {
      image.setOwner(node.get("owner").asText());
    }

    // Map 'dateadded' (Last.fm often returns this as a string timestamp or date)
    // The API usually returns a Unix timestamp or a formatted date string.
    // We attempt to parse it into a Date object.
    if (node.has("dateadded")) {
      String dateStr = node.get("dateadded").asText();
      try {
        // Attempt to parse as Unix timestamp (seconds) if it looks like a number
        if (dateStr.matches("\\d+")) {
          long timestamp = Long.parseLong(dateStr) * 1000; // Convert seconds to milliseconds
          image.setDateAdded(new Date(timestamp));
        } else {
          // Fallback: try ISO format or just store as string if parsing fails
          // Since the field is Date, we try standard parsing
          // Note: Last.fm date format varies, this is a best-effort approach
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          try {
            image.setDateAdded(sdf.parse(dateStr));
          } catch (ParseException e) {
            // If parsing fails, we might leave it null or log a warning
            Log.warn("Could not parse dateadded for image: " + dateStr);
          }
        }
      } catch (NumberFormatException e) {
        Log.warn("Invalid dateadded format: " + dateStr);
      }
    }

    return image;
  }

  public List<AlbumInfo> getTopAlbums(String artist, int limit) throws LastFmInvalidKeyException,
          Exception {
    String url = LastFmUtils.BASE_URL +
            "?method=artist.getTopAlbums" +
            "&artist=" + LastFmUtils.encode(artist) +
            "&limit=" + limit +
            "&api_key=" + getApiKey() +
            "&format=json";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      if (response.statusCode() == 403) {
        throw new LastFmInvalidKeyException(INVALID_API_KEY_FOR_LAST_FM);
      } else {
        throw new RuntimeException("Error Last.fm API: " + response.statusCode() + " - " + response.body());
      }
    }

    // Last.fm returns a root object with a key "topalbums" containing an array of albums
    var rootNode = mapper.readTree(response.body());
    var topAlbumsNode = rootNode.get("topalbums");

    if (topAlbumsNode == null) {
      throw new RuntimeException("Invalid response: missing 'topalbums' field");
    }

    List<AlbumInfo> albumInfos = new ArrayList<>();

    // Iterate through each album in the top albums list
    if (topAlbumsNode.has("album")) {
      for (JsonNode albumNode : topAlbumsNode.get("album")) {
        LastFmAlbum lastFmAlbum = new LastFmAlbum();

        // Map simple fields
        if (albumNode.has("mbid")) {
          // mbid is not always present
          lastFmAlbum.setId(albumNode.get("mbid").asText());
        } else {
          lastFmAlbum.setId("");
        }
        lastFmAlbum.setArtist(albumNode.get("artist").get("name").asText());
        lastFmAlbum.setTitle(albumNode.get("name").asText());
        lastFmAlbum.setUrl(albumNode.get("url").asText());

        // Map images - find different sizes
        List<LastFmAlbum.ImageData> imageList = new ArrayList<>();
        if (albumNode.has("image")) {
          for (JsonNode img : albumNode.get("image")) {
            LastFmAlbum.ImageData imageData = new LastFmAlbum.ImageData();
            imageData.setSize(img.get("size").asText());
            imageData.setUrl(img.get("#text").asText());
            imageList.add(imageData);
          }
        }
        lastFmAlbum.setImages(imageList);

        // Map artist URL
        if (albumNode.has("artist") && albumNode.get("artist").has("url")) {
          lastFmAlbum.setArtistUrl(albumNode.get("artist").get("url").asText());
        }

        albumInfos.add(lastFmAlbum);
      }
    }

    return albumInfos;
  }

  /**
   * Retrieves a list of artists similar to the given artist.
   * Uses the artist.getSimilar method from Last.fm API.
   *
   * @param artist the LastFmArtist object containing the artist name
   * @return a list of LastFmArtist objects representing similar artists
   *
   * @throws Exception if the API call fails or no similar artists are found
   */
  public List<ArtistInfo> getSimilar(ArtistInfo artist, int limit) throws Exception, LastFmInvalidKeyException {
    // Build URL for artist.getSimilar method
    // Using the artist name from the provided LastFmArtist object
    String url = LastFmUtils.BASE_URL +
            "?method=artist.getSimilar" +
            "&artist=" + LastFmUtils.encode(artist.getName()) +
            "&api_key=" + getApiKey() +
            "&limit=" + limit +
            "&autocorrect=1" +  // Enable autocorrection for better matching
            "mbid=" + artist.getId() +
            "&format=json";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      if (response.statusCode() == 403) {
        throw new LastFmInvalidKeyException(INVALID_API_KEY_FOR_LAST_FM);
      } else {
        throw new RuntimeException("Error API Last.fm: " + response.statusCode() + " - " + response.body());
      }
    }

    JsonNode rootNode = mapper.readTree(response.body());

    // Check if the response contains similar artists
    if (!rootNode.has("similarartists") || !rootNode.get("similarartists").has("artist")) {
      throw new RuntimeException("No similar artists found for: " + artist.getName());
    }

    JsonNode similarArtistsNode = rootNode.get("similarartists").get("artist");

    // Handle both single artist object and array of artists
    List<ArtistInfo> similarArtists = new ArrayList<>();

    if (similarArtistsNode.isArray()) {
      // Multiple similar artists returned
      for (JsonNode artistNode : similarArtistsNode) {
        LastFmArtist similarArtist = mapArtistNode(artistNode);
        similarArtists.add(similarArtist);
      }
    } else {
      // Single similar artist returned
      LastFmArtist similarArtist = mapArtistNode(similarArtistsNode);
      similarArtists.add(similarArtist);
    }

    return similarArtists;
  }

  /**
   * Helper method to map a JSON node to a LastFmArtist object.
   * Extracts name, URL, match score, and image information.
   *
   * @param artistNode the JSON node containing artist information
   * @return a populated LastFmArtist object
   */
  private LastFmArtist mapArtistNode(JsonNode artistNode) {
    LastFmArtist similarArtist = new LastFmArtist();

    // Extract artist name
    if (artistNode.has("name")) {
      similarArtist.setName(artistNode.get("name").asText());
    }

    // Extract match score (similarity percentage)
    String match;
    if (artistNode.has("match")) {
      match = artistNode.get("match").asText();
    } else {
      // Default to 1 (= 100%) if match score is not provided
      match = "1";
    }
    similarArtist.setMatch(match);

    // Extract Last.fm URL
    if (artistNode.has("url")) {
      similarArtist.setUrl(artistNode.get("url").asText());
    }

    // Extract image URL (looking for large or medium size)
    String extractedImageUrl = null;
    if (artistNode.has("image") && artistNode.get("image").isArray()) {
      for (JsonNode imgNode : artistNode.get("image")) {
        String size = imgNode.has("size") ? imgNode.get("size").asText() : "";
        String text = imgNode.has("#text") ? imgNode.get("#text").asText() : "";

        // Prioritize large or medium sized images
        if ("large".equalsIgnoreCase(size) || "medium".equalsIgnoreCase(size)) {
          extractedImageUrl = text;
          break;
        }
      }

      // Fallback: if no preferred size found, take the last image (usually largest)
      if (extractedImageUrl == null && !artistNode.get("image").isEmpty()) {
        JsonNode lastImg = artistNode.get("image").get(artistNode.get("image").size() - 1);
        if (lastImg.has("#text")) {
          extractedImageUrl = lastImg.get("#text").asText();
        }
      }
    }
    similarArtist.setImageUrl(extractedImageUrl);

    // Mark as available
    similarArtist.setAvailable(true);

    return similarArtist;
  }

  /**
   * Submits a list of tracks to Last.fm using the track.scrobble method (Batch).
   * Last.fm accepts up to 50 tracks per request.
   *
   * @param submissions List of FullSubmissionData to submit.
   * @throws ScrobblerException if the submission fails.
   */
  public void scrobble(List<FullSubmissionData> submissions) throws LastFmInvalidKeyException, ScrobblerException, IOException {
    if (submissions == null || submissions.isEmpty()) {
      return;
    }

    // 1. Retrieve Credentials
    String sessionKey = Conf.getString(Const.CONF_LASTFM_SESSION_KEY);
    String apiKey = getApiKey();
    String apiSecret = Conf.getString(Const.CONF_LASTFM_SECRET);

    if (sessionKey == null || sessionKey.trim().isEmpty()) {
      throw new ScrobblerException("No session key found. Please authenticate first.");
    }

    // 2. Process in batches (Max 50 tracks per request)
    int batchSize = 50;
    for (int i = 0; i < submissions.size(); i += batchSize) {
      int end = Math.min(i + batchSize, submissions.size());
      List<FullSubmissionData> batch = submissions.subList(i, end);

      Log.info("Submitting batch of {} tracks to Last.fm..." + batch.size());
      submitBatch(batch, sessionKey, apiKey, apiSecret);
    }
  }

  /**
   * Internal method to submit a single batch of tracks.
   */
  private void submitBatch(List<FullSubmissionData> batch, String sessionKey, String apiKey, String apiSecret)
          throws LastFmInvalidKeyException, ScrobblerException {

    // Build parameters map
    // Note: We use a TreeMap for sorting keys for the signature, but we need to build the body carefully
    // because indices (a[0], t[0]) must match.

    // We will build the body string manually to ensure correct indexing,
    // but we need a separate map for signature calculation (sorted keys).

    StringBuilder bodyBuilder = new StringBuilder();
    Map<String, String> paramsForSignature = new TreeMap<>();

    // Base params
    bodyBuilder.append("method=track.scrobble");
    paramsForSignature.put("method", "track.scrobble");
    bodyBuilder.append("&api_key=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
    paramsForSignature.put("api_key", apiKey);
    bodyBuilder.append("&sk=").append(URLEncoder.encode(sessionKey, StandardCharsets.UTF_8));
    paramsForSignature.put("sk", sessionKey);

    // Add batch data
    for (int i = 0; i < batch.size(); i++) {
      FullSubmissionData data = batch.get(i);
      String idx = String.valueOf(i);
      // Required
      bodyBuilder.append("&artist[").append(idx).append("]=")
              .append(URLEncoder.encode(data.getArtist(), StandardCharsets.UTF_8));
      paramsForSignature.put("artist[" + idx + "]", data.getArtist());
      // Required
      bodyBuilder.append("&track[").append(idx).append("]=")
              .append(URLEncoder.encode(data.getTitle(), StandardCharsets.UTF_8));
      paramsForSignature.put("track[" + idx + "]", data.getTitle());
      // Required
      long startTime = data.getStartTime();
      if (data.getStartTime() <= 0) {
        // Prevent missing value / test
        startTime = System.currentTimeMillis();
      }
      bodyBuilder.append("&timestamp[").append(idx).append("]=")
              .append(startTime); // Unix timestamp
      paramsForSignature.put("timestamp[" + idx + "]", String.valueOf(startTime));
      // Optional
      if (data.getAlbum() != null && !data.getAlbum().isEmpty()) {
        bodyBuilder.append("&album[").append(idx).append("]=")
                .append(URLEncoder.encode(data.getAlbum(), StandardCharsets.UTF_8));
        paramsForSignature.put("album[" + idx + "]", data.getAlbum());
      }
      // Optional
      if (data.getDuration() > 0) {
        bodyBuilder.append("&duration[").append(idx).append("]=")
                .append(data.getDuration());
        paramsForSignature.put("duration[" + idx + "]", String.valueOf(data.getDuration()));
      }
      // Optional
      if (data.getTrackNumber() > 0) {
        bodyBuilder.append("&trackNumber[").append(idx).append("]=")
                .append(data.getTrackNumber());
        paramsForSignature.put("trackNumber[" + idx + "]", String.valueOf(data.getTrackNumber()));
      }
    }

    // Now, we need to calculate the signature.
    // The signature requires ALL parameters (including the indexed ones) sorted alphabetically.
    // However, building a TreeMap with keys like "a[0]", "a[1]" works fine for sorting.
    String apiSig = LastFmAuthenticator.generateSignature(paramsForSignature, apiSecret);

    // Add signature and format to the body
    bodyBuilder.append("&api_sig=").append(URLEncoder.encode(apiSig, StandardCharsets.UTF_8));
    bodyBuilder.append("&format=json");

    String body = bodyBuilder.toString();

    Log.debug("Sending scrobble batch. body=" + body);

    // Send Request
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(LastFmUtils.BASE_URL))
            .header("User-Agent", LastFmUtils.USER_AGENT)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .timeout(Duration.ofSeconds(15)) // Slightly longer for batch
            .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        if (response.statusCode() == 403) {
          throw new LastFmInvalidKeyException(INVALID_API_KEY_FOR_LAST_FM);
        } else {
          Log.warn(String.format("Last.fm Scrobble Error: %s - %s", response.statusCode(), response.body()));
          if (response.body().contains("\"error\":9") || response.body().contains("Invalid session key")) {
            throw new ScrobblerException("Invalid session key (Error 9).");
          }
          throw new ScrobblerException("Last.fm API Error: " + response.body());
        }
      }

      // Check response JSON
      String json = response.body();
      if (json.contains("\"error\":") && !json.contains("\"error\":0")) {
        Log.warn("Scrobble failed: " + json);
        throw new ScrobblerException("Scrobble failed: " + json);
      }

      Log.info("Successfully scrobbled " + batch.size() + " tracks. ");
    } catch (IOException | InterruptedException e) {
      Log.warn("Network error : " + e.getMessage());
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new ScrobblerException("Network error: " + e.getMessage());
    }
  }

  /**
   * Submits song to Last.fm using the modern API 2.0 (updateNowPlaying).
   *
   * @param track The track that was played
   * @throws ScrobblerException the scrobbler exception
   */
  public void updateNowPlaying(Track track) throws LastFmInvalidKeyException,
          ScrobblerException {
    // 1. Prepare data
    String artist = track.getAlbumArtistOrArtist();
    String title = track.getName();
    String album = track.getAlbum() != null ? track.getAlbum().getName() : "";
    long durationSecs = track.getDuration() / 1000;

    Log.info(String.format("Updating Now Playing: %s - %s (%ss)", artist, title, durationSecs));

    // 2. Retrieve Session Key (from config or memory)
    String sessionKey = Conf.getString(Const.CONF_LASTFM_SESSION_KEY);
    if (sessionKey == null || sessionKey.trim().isEmpty()) {
      Log.warn("No Last.fm session key found. Please authenticate first.");
      throw new ScrobblerException("No session key found. Authenticate in preferences.");
    }

    // 3. Prepare parameters for updateNowPlaying
    // Note: 'duration' is required for updateNowPlaying in seconds
    Map<String, String> params = new TreeMap<>();
    params.put("method", "track.updateNowPlaying");
    params.put("api_key", getApiKey());
    params.put("sk", sessionKey); // sk = session key
    params.put("artist", artist);
    params.put("track", title);
    params.put("duration", String.valueOf(durationSecs));
    if (!album.isEmpty()) {
      params.put("album", album); // The album name (optional)
    }
    // Optional: trackNumber, mbid, etc. if available

    // 5. Generate Signature (excluding 'format')
    String apiSecret = Conf.getString(Const.CONF_LASTFM_SECRET);
    String apiSig = LastFmAuthenticator.generateSignature(params, apiSecret);
    params.put("api_sig", apiSig);
    params.put("format", "json"); // Added for request, not for hash

    // 6. Build Request Body
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
    String body = bodyBuilder.toString();

    // 7. Send POST Request
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(LastFmUtils.BASE_URL))
            .header("User-Agent", LastFmUtils.USER_AGENT)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .timeout(Duration.ofSeconds(10))
            .build();

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      String responseBody = response.body();

      if (response.statusCode() != 200) {
        if (response.statusCode() == 403) {
          throw new LastFmInvalidKeyException(INVALID_API_KEY_FOR_LAST_FM);
        } else {
          Log.warn(String.format("Last.fm API Error (updateNowPlaying): %d - %s", response.statusCode(), responseBody));
          // Handle Invalid Session Key (Error 9)
          if (responseBody.contains("\"error\":9") || responseBody.contains("Invalid session key")) {
            Log.warn("Session key invalid. Clearing session and forcing re-authentication.");
            Conf.removeProperty(Const.CONF_LASTFM_SESSION_KEY);
            throw new ScrobblerException("Session expired. Please re-authenticate in preferences.");
          }
          throw new ScrobblerException("Last.fm API Error: " + responseBody);
        }
      }

      // Parse JSON response (expected: {"nowplaying":{"track":"...", "status":"Now playing..."}})
      if (responseBody.contains("\"nowplaying\"")) {
        Log.info("Now Playing updated successfully for: " + title);
      } else {
        Log.warn("Unexpected response from Last.fm: {}", responseBody);
        // Not throwing exception here as "ok" might be implicit in some responses
      }

    } catch (IOException | InterruptedException e) {
      Log.warn("Network error while updating Now Playing : " + e.getMessage());
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new ScrobblerException("Network error: " + e.getMessage());
    }
  }

}