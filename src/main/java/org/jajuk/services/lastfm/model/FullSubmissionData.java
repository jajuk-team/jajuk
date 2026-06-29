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

/**
 * .
 */
public class FullSubmissionData {
  private String artist;
  private String title;
  private String album;
  private int duration; // The length of the track in seconds.
  private int trackNumber;
  private long startTime; // The time the track started playing, in UNIX timestamp format (integer number of seconds since 00:00:00, January 1st 1970 UTC). This must be in the UTC time zone.
  private String recommendationKey;
  private int length;
  private String rating;

  /**
   * Instantiates a new full submission data.
   *
   * @param artist
   * @param title
   * @param album
   * @param duration
   * @param trackNumber
   * @param startTime
   */
  public FullSubmissionData(String artist,
                            String title,
                            String album,
                            int duration,
                            int trackNumber,
                            long startTime) {
    this.album = album;
    this.artist = artist;
    this.duration = duration;
    this.startTime = startTime;
    this.title = title;
    this.trackNumber = trackNumber;
  }

  /**
   * Gets the artist.
   *
   * @return the artist
   */
  public String getArtist() {
    return artist;
  }

  /**
   * Sets the artist.
   *
   * @param artist the artist to set
   */
  public void setArtist(String artist) {
    this.artist = artist;
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   *
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the album.
   *
   * @return the album
   */
  public String getAlbum() {
    return album;
  }

  /**
   * Sets the album.
   *
   * @param album the album to set
   */
  public void setAlbum(String album) {
    this.album = album;
  }

  /**
   * Gets the duration.
   *
   * @return the duration
   */
  public int getDuration() {
    return duration;
  }

  /**
   * Sets the duration.
   *
   * @param duration the duration to set
   */
  public void setDuration(int duration) {
    this.duration = duration;
  }

  /**
   * Gets the track number.
   *
   * @return the trackNumber
   */
  public int getTrackNumber() {
    return trackNumber;
  }

  /**
   * Sets the track number.
   *
   * @param trackNumber the trackNumber to set
   */
  public void setTrackNumber(int trackNumber) {
    this.trackNumber = trackNumber;
  }

  /**
   * Gets the start time.
   *
   * @return the startTime
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Sets the start time.
   *
   * @param startTime the startTime to set
   */
  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }


  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public String getRating() {
    return rating;
  }

  public void setRating(String rating) {
    this.rating = rating;
  }

  public String getRecommendationKey() {
    return recommendationKey;
  }

  public void setRecommendationKey(String recommendationKey) {
    this.recommendationKey = recommendationKey;
  }
}
