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

package ext.services.lastfm;

/**
 * DOCUMENT_ME.
 */
public interface TrackInfo {

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle();

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl();

  /**
   * Sets the title.
   * 
   * @param title the title to set
   */
  public void setTitle(String title);

  /**
   * Sets the url.
   * 
   * @param url the url to set
   */
  public void setUrl(String url);

}
