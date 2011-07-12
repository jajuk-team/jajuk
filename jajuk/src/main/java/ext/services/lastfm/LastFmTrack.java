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

import net.roarsoftware.lastfm.Track;

/**
 * The Class LastFmTrack.
 */
public class LastFmTrack implements TrackInfo {

  /** The title. */
  private String title;

  /** The url. */
  private String url;

  /**
   * Gets the track.
   * 
   * @param t DOCUMENT_ME
   * 
   * @return the track
   */
  protected static LastFmTrack getTrack(Track t) {
    LastFmTrack track = new LastFmTrack();

    track.title = t.getName();
    track.url = t.getUrl();

    return track;
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

}
