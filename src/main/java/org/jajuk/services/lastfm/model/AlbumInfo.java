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

import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * .
 */
public interface AlbumInfo extends LastFmInfo {
  /**
   * Gets the artist.
   *
   * @return the artist
   */
  String getArtist();

  /**
   * Gets the artist url.
   *
   * @return the artist url
   */
  String getArtistUrl();

  /**
   * Gets the big cover url.
   *
   * @return the bigCoverURL
   */
  String getBigCoverURL();

  /**
   * Gets the cover.
   *
   * @return the cover
   */
  ImageIcon getCover();

  /**
   * Gets the cover url.
   *
   * @return the cover url
   */
  String getCoverURL();

  /**
   * Gets the images.
   *
   * @return the images
   */
  List<LastFmAlbum.ImageData> getImages();

  /**
   * Gets the release date.
   *
   * @return the release date
   */
  Date getReleaseDate();

  /**
   * Gets the release date string.
   *
   * @return the releaseDateString
   */
  String getReleaseDateString();

  /**
   * Gets the small cover url.
   *
   * @return the small cover url
   */
  String getSmallCoverURL();

  /**
   * Gets the title.
   *
   * @return the title
   */
  String getTitle();

  /**
   * Gets the tracks.
   *
   * @return the tracks
   */
  List<TrackInfo> getTracks();

  /**
   * Gets the url.
   *
   * @return the url
   */
  String getUrl();

  /**
   * Gets the year.
   *
   * @return the year
   */
  String getYear();

  /**
   * Sets the artist.
   *
   * @param artist the artist to set
   */
  void setArtist(String artist);

  /**
   * Sets the cover.
   *
   * @param cover the cover to set
   */
  void setCover(ImageIcon cover);

  /**
   * Sets the images.
   *
   * @param images the images to set
   */
  void setImages(List<LastFmAlbum.ImageData> images);

  /**
   * Sets the release date string.
   *
   * @param releaseDateString the releaseDateString to set
   */
  void setReleaseDateString(String releaseDateString);

  /**
   * Sets the title.
   *
   * @param title the title to set
   */
  void setTitle(String title);

  /**
   * Sets the tracks.
   *
   * @param tracks the tracks to set
   */
  void setTracks(List<TrackInfo> tracks);

  /**
   * Sets the artist url.
   *
   * @param artistUrl the url of the artist to set
   */
  void setArtistUrl(String artistUrl);

  /**
   * Sets the url.
   *
   * @param url the url to set
   */
  void setUrl(String url);

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  String toString();
}
