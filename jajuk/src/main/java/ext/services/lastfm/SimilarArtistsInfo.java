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

import java.util.List;

/**
 * DOCUMENT_ME.
 */
public interface SimilarArtistsInfo {

  /**
   * Gets the artist name.
   * 
   * @return the artist name
   */
  public String getArtistName();

  /**
   * Gets the artists.
   * 
   * @return the artists
   */
  public List<ArtistInfo> getArtists();

  /**
   * Gets the picture.
   * 
   * @return the picture
   */
  public String getPicture();

  /**
   * Sets the artist name.
   * 
   * @param artistName the artistName to set
   */
  public void setArtistName(String artistName);

  /**
   * Sets the artists.
   * 
   * @param artists the artists to set
   */
  public void setArtists(List<ArtistInfo> artists);

  /**
   * Sets the picture.
   * 
   * @param picture the picture to set
   */
  public void setPicture(String picture);

}
