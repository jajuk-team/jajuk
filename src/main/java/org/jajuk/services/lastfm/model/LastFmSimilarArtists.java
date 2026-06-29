/*
 * aTunes 1.14.0 code adapted by Jajuk team
 *
 * Original copyright notice bellow :
 *
 * Copyright (C) 2006-2009 Alex Aranda, Sylvain Gaudard, Thomas Beckers and contributors
 *
 * See http://www.atunes.org/wiki/index.php?title=Contributing for information about contributors
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package org.jajuk.services.lastfm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class LastFmSimilarArtists.
 */
public class LastFmSimilarArtists implements SimilarArtistsInfo {
  /** The Constant MAX_SIMILAR_ARTISTS. */
  public static final int MAX_SIMILAR_ARTISTS = 15;
  /** The artist name. */
  private String artistName;
  /** The picture. */
  private String picture;
  /** The artists. */
  private List<ArtistInfo> artists;

  public static SimilarArtistsInfo getSimilarArtists(List<ArtistInfo> similarArtists, ArtistInfo artistInfo) {
    SimilarArtistsInfo similar = new LastFmSimilarArtists();
    similar.setArtistName(artistInfo.getName());
    similar.setArtists(new ArrayList<>(similarArtists));
    return similar;
  }

  /**
   * Gets the artist name.
   *
   * @return the artist name
   */
  @Override
  public String getArtistName() {
    return artistName;
  }

  /**
   * Gets the artists.
   *
   * @return the artists
   */
  @Override
  public List<ArtistInfo> getArtists() {
    // artists is null for void (unknown) similar artists
    if (artists != null) {
      // Sort similar artists ignoring case
      artists.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    }
    return artists;
  }

  /**
   * Gets the picture.
   *
   * @return the picture
   */
  //@Override
  //public String getPicture() {
  //  return picture;
  //}

  /**
   * Sets the artist name.
   *
   * @param artistName the artistName to set
   */
  @Override
  public void setArtistName(String artistName) {
    this.artistName = artistName;
  }

  /**
   * Sets the artists.
   *
   * @param artists the artists to set
   */
  @Override
  public void setArtists(List<ArtistInfo> artists) {
    this.artists = artists != null ? artists : new ArrayList<>();
  }

  /**
   * Sets the picture.
   *
   * @param picture the picture to set
   */
  //@Override
  //public void setPicture(String picture) {
  //  this.picture = picture;
  //}
}
