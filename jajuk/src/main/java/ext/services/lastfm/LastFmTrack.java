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
