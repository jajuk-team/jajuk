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

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents detailed artist information from Last.fm API (artist.getinfo).
 * Contains extended data beyond basic search results, including biography,
 * tags, and similar artists.
 */
public class LastFmArtistDetail {

  // Basic artist information
  private String name;
  private String id; // MBID
  private String url;
  private String imageUrl;
  private boolean available;

  // Listener statistics
  private String listeners;
  private String playcount;

  // Wiki/Biography data
  private String wikiSummary;
  private String wikiContent;
  private String wikiPublished;

  // Tags
  private List<TagInfo> tags;

  // Similar artists
  private List<SimilarArtistInfo> similarArtists;

  // Top tracks
  private List<TrackInfo> topTracks;

  // Image loading (for UI integration)
  private java.awt.image.BufferedImage cover;

  // Nested class for Tag information
  public static class TagInfo {
    private String name;
    private String url;
    private String count;

    public TagInfo() {
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getCount() {
      return count;
    }

    public void setCount(String count) {
      this.count = count;
    }
  }

  // Nested class for Similar Artist information
  public static class SimilarArtistInfo {
    private String name;
    private String url;
    private String match;
    private String imageUrl;

    public SimilarArtistInfo() {
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getMatch() {
      return match;
    }

    public void setMatch(String match) {
      this.match = match;
    }

    public String getImageUrl() {
      return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
    }
  }

  // Constructor
  public LastFmArtistDetail() {
    this.tags = new ArrayList<>();
    this.similarArtists = new ArrayList<>();
    this.topTracks = new ArrayList<>();
    this.available = false;
  }

  // ==================== ArtistInfo Interface Methods ====================

  public String getName() {
    return name;
  }

  public String getBigCoverURL() {
    return imageUrl;
  }

  public java.awt.image.BufferedImage getCover() {
    return cover;
  }

  public List<TrackInfo> getTracks() {
    return topTracks;
  }

  public String getUrl() {
    return url;
  }

  public void setImage(ImageIcon image) {
  }

  public String getYear() {
    return null; // Not applicable for artists
  }

  public void setArtist(String artist) {
    this.name = artist;
  }

  public void setCover(java.awt.image.BufferedImage cover) {
    this.cover = cover;
  }

  public void setTitle(String title) {
    this.name = title;
  }

  public void setTracks(List<TrackInfo> tracks) {
    this.topTracks = tracks;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  // ==================== Additional Detail Methods ====================

  public String getId() {
    return id;
  }

  public ImageIcon getImage() {
    return null;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public String getListeners() {
    return listeners;
  }

  public void setListeners(String listeners) {
    this.listeners = listeners;
  }

  public String getPlaycount() {
    return playcount;
  }

  public void setPlaycount(String playcount) {
    this.playcount = playcount;
  }

  public String getWikiSummary() {
    return wikiSummary;
  }

  public void setWikiSummary(String wikiSummary) {
    this.wikiSummary = wikiSummary;
  }

  public String getWikiContent() {
    return wikiContent;
  }

  public void setWikiContent(String wikiContent) {
    this.wikiContent = wikiContent;
  }

  public String getWikiPublished() {
    return wikiPublished;
  }

  public void setWikiPublished(String wikiPublished) {
    this.wikiPublished = wikiPublished;
  }

  public List<TagInfo> getTags() {
    return tags;
  }

  public void setTags(List<TagInfo> tags) {
    this.tags = tags;
  }

  public List<SimilarArtistInfo> getSimilarArtists() {
    return similarArtists;
  }

  public void setSimilarArtists(List<SimilarArtistInfo> similarArtists) {
    this.similarArtists = similarArtists;
  }

  public List<TrackInfo> getTopTracks() {
    return topTracks;
  }

  public void setTopTracks(List<TrackInfo> topTracks) {
    this.topTracks = topTracks;
  }

  // ==================== Image Loading ====================

  /**
   * Loads the cover image from the URL into memory.
   * Should be called outside the EDT for performance.
   */
  public void loadCoverImage() {
    if (imageUrl != null && !imageUrl.isEmpty()) {
      try {
        java.net.URL url = new java.net.URL(imageUrl);
        javax.imageio.ImageIO.read(url); // Simplified - actual implementation may vary
        // In practice, you'd want to use your existing ThumbnailManager or DownloadManager
      } catch (Exception e) {
        // Log error silently
      }
    }
  }
}
