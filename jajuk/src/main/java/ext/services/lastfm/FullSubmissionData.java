/*
 * Adapted by Jajuk team
 * Copyright (C) 2003-2009 the Jajuk Team
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

public class FullSubmissionData {

    private String artist;
    private String title;
    private String album;
    private int duration;
    private int trackNumber;
    private String source;
    private int startTime;

    public FullSubmissionData(String artist, String title, String album, int duration, int trackNumber, String source, int startTime) {
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.source = source;
        this.startTime = startTime;
        this.title = title;
        this.trackNumber = trackNumber;
    }

    /**
     * @return the artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @param artist
     *            the artist to set
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the album
     */
    public String getAlbum() {
        return album;
    }

    /**
     * @param album
     *            the album to set
     */
    public void setAlbum(String album) {
        this.album = album;
    }

    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @param duration
     *            the duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * @return the trackNumber
     */
    public int getTrackNumber() {
        return trackNumber;
    }

    /**
     * @param trackNumber
     *            the trackNumber to set
     */
    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the startTime
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

}
