/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
 *  $Revision$
 */

package ext.services.lastfm;

/**
 * Interface for all audio objects (e.g. AudioFile, Radio, PodcastFeedEntry)
 */
public interface AudioObject {

  /**
   * Gets the album.
   * 
   * @return the album
   */
  public String getAlbum();

  /**
   * Gets the album artist.
   * 
   * @return the album artist
   */
  public String getAlbumArtist();

  /**
   * Gets the artist.
   * 
   * @return the artist
   */
  public String getArtist();

  /**
   * Gets the bitrate.
   * 
   * @return the bitrate
   */
  public long getBitrate();

  /**
   * Gets the composer.
   * 
   * @return the composer
   */
  public String getComposer();

  /**
   * Gets the duration.
   * 
   * @return the duration
   */
  public long getDuration();

  /**
   * Gets the frequency.
   * 
   * @return the frequency
   */
  public int getFrequency();

  /**
   * Gets the genre.
   * 
   * @return the genre
   */
  public String getGenre();

  /**
   * Gets the lyrics.
   * 
   * @return the lyrics
   */
  public String getLyrics();

  /**
   * Gets the stars.
   * 
   * @return the stars
   */
  public int getStars();

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle();

  /**
   * Gets the title or file name.
   * 
   * @return the title or file name
   */
  public String getTitleOrFileName();

  /**
   * Gets the track number.
   * 
   * @return the track number
   */
  public Integer getTrackNumber();

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl();

  /**
   * Gets the year.
   * 
   * @return the year
   */
  public String getYear();

  /**
   * Sets the stars.
   * 
   * @param stars the new stars
   */
  public void setStars(int stars);

  /**
   * Checks if is seekable.
   * 
   * @return true, if is seekable
   */
  public boolean isSeekable();

  /**
   * Gets the disc number.
   * 
   * @return the disc number
   */
  public Integer getDiscNumber();
}
