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
