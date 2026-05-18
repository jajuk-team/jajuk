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

import javax.swing.ImageIcon;

/**
 * .
 */
public interface ArtistInfo {
  /**
   * Gets the id.
   *
   * @return the id
   */
  String getId();

  /**
   * Gets the image.
   * 
   * @return the image
   */
  ImageIcon getImage();

  /**
   * Gets the image url.
   * 
   * @return the image url
   */
  String getImageUrl();

  /**
   * Gets the match.
   * 
   * @return the match
   */
  String getMatch();

  /**
   * Gets the name.
   * 
   * @return the name
   */
  String getName();

  /**
   * Gets the url.
   * 
   * @return the url
   */
  String getUrl();

  /**
   * Sets the image.
   * 
   * @param image the new image
   */
  void setImage(ImageIcon image);

  /**
   * Sets the image url.
   * 
   * @param imageUrl the imageUrl to set
   */
  void setImageUrl(String imageUrl);

  /**
   * Sets the match.
   * 
   * @param match the match to set
   */
  void setMatch(String match);

  /**
   * Sets the name.
   * 
   * @param name the name to set
   */
  void setName(String name);

  /**
   * Sets the url.
   * 
   * @param url the url to set
   */
  void setUrl(String url);

  /**
   * Sets the available property.
   * 
   * @param available 
   */
  void setAvailable(boolean available);

  /**
   * Returns if available.
   * 
   * @return true if the available property is set
   */
  boolean isAvailable();
}
