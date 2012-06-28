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

import javax.swing.ImageIcon;

/**
 * .
 */
public interface ArtistInfo {
  /**
   * Gets the image.
   * 
   * @return the image
   */
  public ImageIcon getImage();

  /**
   * Gets the image url.
   * 
   * @return the image url
   */
  public String getImageUrl();

  /**
   * Gets the match.
   * 
   * @return the match
   */
  public String getMatch();

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName();

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl();

  /**
   * Sets the image.
   * 
   * @param image the new image
   */
  public void setImage(ImageIcon image);

  /**
   * Sets the image url.
   * 
   * @param imageUrl the imageUrl to set
   */
  public void setImageUrl(String imageUrl);

  /**
   * Sets the match.
   * 
   * @param match the match to set
   */
  public void setMatch(String match);

  /**
   * Sets the name.
   * 
   * @param name the name to set
   */
  public void setName(String name);

  /**
   * Sets the url.
   * 
   * @param url the url to set
   */
  public void setUrl(String url);

  /**
   * Sets the available property.
   * 
   * @param available 
   */
  public void setAvailable(boolean available);

  /**
   * Returns if available.
   * 
   * @return true if the available property is set
   */
  public boolean isAvailable();
}
