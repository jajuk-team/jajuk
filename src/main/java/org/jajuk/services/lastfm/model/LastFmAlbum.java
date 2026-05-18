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
package org.jajuk.services.lastfm.model;

import org.jajuk.util.UtilString;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LastFmAlbum implements AlbumInfo {
  /** The Constant DF. */
  private static final ThreadLocal<SimpleDateFormat> DF = ThreadLocal.withInitial(() -> new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.US));

  /** The artist. */
  private String artist;

  /** The artist url. */
  private String artistUrl;

  /** The id. */
  private String id;

  /** The title. */
  private String title;

  /** The url. */
  private String url;

  /** The release date string. */
  private String releaseDateString;

  /** Image : cover uro, big cover url. */
  private List<ImageData> images; // Last.fm returns a list of images

  /** The tracks. */
  private TrackListWrapper tracksWrapper; // Wrapper because tracks is an object containing a list

  // Used by renderers
  /** The cover. */
  private ImageIcon cover;

  // --- Internal Helper Classes for JSON Mapping ---

  /**
   * Helper class to map the 'image' array from Last.fm JSON.
   * Structure: [{"size": "small", "#text": "url"}, ...]
   */
  public static class ImageData {
    private String size;

    private String url;

    public String getSize() {
      return size;
    }

    public void setSize(String size) {
      this.size = size;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }

  public static class WikiData {
    private String summary;
    private String content;
    private String published;

    public String getSummary() {
      return summary;
    }

    public void setSummary(String summary) {
      this.summary = summary;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getPublished() {
      return published;
    }

    public void setPublished(String published) {
      this.published = published;
    }
  }

  /**
   * Wrapper for the 'tracks' object which contains a 'track' list.
   * Structure: { "track": [ {...}, {...} ] }
   */
  private static class TrackListWrapper {
    private List<TrackInfo> trackList;

    public List<TrackInfo> getTrackList() {
      return trackList;
    }

    public void setTrackList(List<TrackInfo> trackList) {
      this.trackList = trackList;
    }
  }

  /**
   * Returns the URL for the large cover image.
   * Searches the images list for size "extralarge".
   *
   * @return the bigCoverURL
   */
  @Override
  public String getBigCoverURL() {
    if (images != null) {
      for (ImageData img : images) {
        if ("extralarge".equalsIgnoreCase(img.getSize())) {
          return img.getUrl();
        }
      }
    }
    return null;
  }

  /**
   * Returns the URL for the medium cover image.
   * Searches the images list for size "large".
   *
   * @return the cover url
   */
  @Override
  public String getCoverURL() {
    if (images != null) {
      for (ImageData img : images) {
        if ("large".equalsIgnoreCase(img.getSize())) {
          return img.getUrl();
        }
      }
    }
    return null;
  }

  @Override
  public String getId() {
    return id;
  }

  /**
   * Returns the URL for the small cover image.
   * Searches the images list for size "small".
   *
   * @return the small cover url
   */
  @Override
  public String getSmallCoverURL() {
    if (images != null) {
      for (ImageData img : images) {
        if ("small".equalsIgnoreCase(img.getSize())) {
          return img.getUrl();
        }
      }
    }
    return null;
  }

  /**
   * Gets the tracks.
   *
   * @return the tracks
   */
  @Override
  public List<TrackInfo> getTracks() {
    return tracksWrapper != null ? tracksWrapper.getTrackList() : null;
  }

  public void setTracks(List<TrackInfo> tracks) {
    if (this.tracksWrapper == null) {
      this.tracksWrapper = new TrackListWrapper();
    }
    this.tracksWrapper.setTrackList(tracks);
  }

  /**
   * Gets the album.
   *
   * @param a
   * @param pl
   * @return the album

  public static AlbumInfo getAlbum(Album a, Playlist pl) {
    LastFmAlbum album = new LastFmAlbum();
    album.artist = a.getArtist();
    album.title = a.getName();
    album.url = a.getUrl();
    album.releaseDateString = a.getReleaseDate() != null ? a.getReleaseDate().toString() : "";
    album.bigCoverURL = a.getImageURL(ImageSize.LARGE);
    album.coverURL = a.getImageURL(ImageSize.ORIGINAL);
    album.smallCoverURL = a.getImageURL(ImageSize.SMALL);
    if (pl != null) {
      List<TrackInfo> ts = new ArrayList<TrackInfo>();
      for (Track t : pl.getTracks()) {
        ts.add(LastFmTrack.getTrack(t));
      }
      // Process track list: if all tracks have a common string between (), [],
      // {} as "(Live)" then it's removed from all of them
      // In this way track names are more accurate
      if (!ts.isEmpty()) {
        handleTracks(ts);
      }
      album.tracks = ts;
    }
    return album;
  }
   */

  /**
   * Handle tracks.
   *
   * @param ts
   */
  private static void handleTracks(List<TrackInfo> ts) {
    String firstTrackTitle = ts.get(0).getTitle();
    // Get all text between () [] {}
    List<String> tokensOfFirstTrackTitle = UtilString
            .getTextBetweenChars(firstTrackTitle, '(', ')');
    tokensOfFirstTrackTitle.addAll(UtilString.getTextBetweenChars(firstTrackTitle, '[', ']'));
    tokensOfFirstTrackTitle.addAll(UtilString.getTextBetweenChars(firstTrackTitle, '{', '}'));
    // Check what tokens are present in all track titles
    List<String> commonTokens = new ArrayList<String>();
    for (String token : tokensOfFirstTrackTitle) {
      boolean common = true;
      for (int i = 1; i < ts.size() && common; i++) {
        if (!ts.get(i).getTitle().contains(token)) {
          common = false;
        }
      }
      if (common) {
        commonTokens.add(token);
      }
    }
    // Then remove common tokens from all titles
    for (TrackInfo ti : ts) {
      for (String token : commonTokens) {
        ti.setTitle(ti.getTitle().replace(token, ""));
      }
      ti.setTitle(ti.getTitle().trim());
    }
  }

  /**
   * Gets the artist.
   *
   * @return the artist
   */
  @Override
  public String getArtist() {
    return artist;
  }

  /**
   * Gets the artist url.
   *
   * @return the artist url
   */
  @Override
  public String getArtistUrl() {
    if (url == null) {
      return null;
    }
    if (!url.contains("/")) {
      return url;
    }
    return url.substring(0, url.lastIndexOf('/'));
  }

  /**
   * Gets the cover.
   *
   * @return the cover
   */
  @Override
  public ImageIcon getCover() {
    return cover;
  }


  /**
   * Gets the release date.
   *
   * @return the release date
   */
  @Override
  public Date getReleaseDate() {
    if (releaseDateString == null) {
      return null;
    }
    try {
      return DF.get().parse(releaseDateString);
    } catch (ParseException e) {
      return null;
    }
  }

  /**
   * Gets the release date string.
   *
   * @return the releaseDateString
   */
  @Override
  public String getReleaseDateString() {
    return releaseDateString;
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Gets the url.
   *
   * @return the url
   */
  @Override
  public String getUrl() {
    return url;
  }

  /**
   * Gets the year.
   *
   * @return the year
   */
  @Override
  public String getYear() {
    Date releaseDate = getReleaseDate();
    if (releaseDate == null) {
      return "";
    }
    Calendar c = Calendar.getInstance();
    c.setTime(releaseDate);
    return Integer.toString(c.get(Calendar.YEAR));
  }

  /**
   * Sets the artist.
   *
   * @param artist the artist to set
   */
  @Override
  public void setArtist(String artist) {
    this.artist = artist;
  }


  @Override
  public void setArtistUrl(String artistUrl) {
    this.artistUrl = artistUrl;
  }

  /**
   * Sets the cover.
   *
   * @param cover the cover to set
   */
  @Override
  public void setCover(ImageIcon cover) {
    this.cover = cover;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public void setImages(List<ImageData> images) {
    this.images = images;
  }

  /**
   * Sets the release date string.
   *
   * @param releaseDateString the releaseDateString to set
   */
  @Override
  public void setReleaseDateString(String releaseDateString) {
    this.releaseDateString = releaseDateString;
  }

  /**
   * Sets the title.
   *
   * @param title the title to set
   */
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Sets the url.
   *
   * @param url the url to set
   */
  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return UtilString.concat(artist, " - ", title);
  }

  public void loadCoverImage() {
    String url = getBigCoverURL();
    if (url != null) {
      try {
        this.cover = new ImageIcon(new java.net.URL(url));
      } catch (Exception e) {
        System.err.println("Failed to load cover image: " + e.getMessage());
        this.cover = null;
      }
    }
  }
}
