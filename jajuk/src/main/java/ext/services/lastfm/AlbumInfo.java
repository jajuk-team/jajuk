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

import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * DOCUMENT_ME.
 */
public interface AlbumInfo {

  /**
   * Gets the artist.
   * 
   * @return the artist
   */
  public String getArtist();

  /**
   * Gets the artist url.
   * 
   * @return the artist url
   */
  public String getArtistUrl();

  /**
   * Gets the big cover url.
   * 
   * @return the bigCoverURL
   */
  public String getBigCoverURL();

  /**
   * Gets the cover.
   * 
   * @return the cover
   */
  public ImageIcon getCover();

  /**
   * Gets the cover url.
   * 
   * @return the cover url
   */
  public String getCoverURL();

  /**
   * Gets the release date.
   * 
   * @return the release date
   */
  public Date getReleaseDate();

  /**
   * Gets the release date string.
   * 
   * @return the releaseDateString
   */
  public String getReleaseDateString();

  /**
   * Gets the small cover url.
   * 
   * @return the small cover url
   */
  public String getSmallCoverURL();

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle();

  /**
   * Gets the tracks.
   * 
   * @return the tracks
   */
  public List<TrackInfo> getTracks();

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
   * Sets the artist.
   * 
   * @param artist the artist to set
   */
  public void setArtist(String artist);

  /**
   * Sets the big cover url.
   * 
   * @param bigCoverURL the bigCoverURL to set
   */
  public void setBigCoverURL(String bigCoverURL);

  /**
   * Sets the cover.
   * 
   * @param cover the cover to set
   */
  public void setCover(ImageIcon cover);

  /**
   * Sets the cover url.
   * 
   * @param coverURL the coverURL to set
   */
  public void setCoverURL(String coverURL);

  /**
   * Sets the release date string.
   * 
   * @param releaseDateString the releaseDateString to set
   */
  public void setReleaseDateString(String releaseDateString);

  /**
   * Sets the small cover url.
   * 
   * @param smallCoverURL the smallCoverURL to set
   */
  public void setSmallCoverURL(String smallCoverURL);

  /**
   * Sets the title.
   * 
   * @param title the title to set
   */
  public void setTitle(String title);

  /**
   * Sets the tracks.
   * 
   * @param tracks the tracks to set
   */
  public void setTracks(List<? extends TrackInfo> tracks);

  /**
   * Sets the url.
   * 
   * @param url the url to set
   */
  public void setUrl(String url);

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  /**
   * To string.
   * DOCUMENT_ME
   * 
   * @return the string
   */
  @Override
  public String toString();

}
