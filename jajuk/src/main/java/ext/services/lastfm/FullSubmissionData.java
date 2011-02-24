/*
 * Adapted by Jajuk team
 * Copyright (C) 2003-2011 the Jajuk Team
 * http://jajuk.info
 * 
 * aTunes 1.14.0
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

package ext.services.lastfm;

/**
 * DOCUMENT_ME.
 */
public class FullSubmissionData {

  /** DOCUMENT_ME. */
  private String artist;

  /** DOCUMENT_ME. */
  private String title;

  /** DOCUMENT_ME. */
  private String album;

  /** DOCUMENT_ME. */
  private int duration;

  /** DOCUMENT_ME. */
  private int trackNumber;

  /** DOCUMENT_ME. */
  private String source;

  /** DOCUMENT_ME. */
  private int startTime;

  /**
   * Instantiates a new full submission data.
   * 
   * @param artist DOCUMENT_ME
   * @param title DOCUMENT_ME
   * @param album DOCUMENT_ME
   * @param duration DOCUMENT_ME
   * @param trackNumber DOCUMENT_ME
   * @param source DOCUMENT_ME
   * @param startTime DOCUMENT_ME
   */
  public FullSubmissionData(String artist, String title, String album, int duration,
      int trackNumber, String source, int startTime) {
    this.album = album;
    this.artist = artist;
    this.duration = duration;
    this.source = source;
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
   * Gets the source.
   * 
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * Sets the source.
   * 
   * @param source the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * Gets the start time.
   * 
   * @return the startTime
   */
  public int getStartTime() {
    return startTime;
  }

  /**
   * Sets the start time.
   * 
   * @param startTime the startTime to set
   */
  public void setStartTime(int startTime) {
    this.startTime = startTime;
  }

}
