/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

import javax.swing.ImageIcon;

/**
 * DOCUMENT_ME.
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
   * @param available DOCUMENT_ME
   */
  public void setAvailable(boolean available);

  /**
   * Returns if available.
   * 
   * @return true if the available property is set
   */
  public boolean isAvailable();

}
