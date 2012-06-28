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
 *  
 */
package org.jajuk.base;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.jajuk.util.Const;

/**
 * Convenient class to manage album-artists.
 */
public final class AlbumArtistManager extends ItemManager {
  /** Self instance. */
  private static AlbumArtistManager singleton = new AlbumArtistManager();
  /** List of all known album-artists. */
  private static Vector<String> albumArtistsList = new Vector<String>(100); // NOPMD

  /**
   * No constructor available, only static access.
   */
  private AlbumArtistManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(Const.XML_EXPANDED, false, false, false, false,
        true, Boolean.class, false));
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static AlbumArtistManager getInstance() {
    return singleton;
  }

  /**
   * Register an albumArtist.
   * 
   * @param sName The name of the albumArtist to search for.
   * 
   * @return the albumArtist
   */
  public AlbumArtist registerAlbumArtist(String sName) {
    String sId = createID(sName);
    return registerAlbumArtist(sId, sName);
  }

  /**
   * Register an albumArtist with a known id.
   *
   * @param sId the ID of the new albumArtist.
   * @param sName The name of the new albumArtist.
   * @return the albumArtist
   */
  synchronized AlbumArtist registerAlbumArtist(String sId, String sName) {
    AlbumArtist albumArtist = getAlbumArtistByID(sId);
    // if we have this albumArtist already, simply return the existing one
    if (albumArtist != null) {
      return albumArtist;
    }
    albumArtist = new AlbumArtist(sId, sName);
    registerItem(albumArtist);
    // add it in genres list if new
    if (!albumArtistsList.contains(sName)) {
      albumArtistsList.add(albumArtist.getName2());
      // Sort items ignoring case
      Collections.sort(albumArtistsList, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          return o1.compareToIgnoreCase(o2);
        }
      });
    }
    return albumArtist;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getXMLTag() {
    return Const.XML_ALBUM_ARTISTS;
  }

  /**
   * Gets the albumArtists list.
   * 
   * @return albumArtists as a string list (used for albumArtists combos)
   */
  public static Vector<String> getAlbumArtistsList() {
    return albumArtistsList;
  }

  /**
   * Gets the albumArtist by id.
   * 
   * @param sID Item ID
   * 
   * @return Element
   */
  AlbumArtist getAlbumArtistByID(String sID) {
    return (AlbumArtist) getItemByID(sID);
  }

  /**
   * Gets the albumArtists.
   * 
   * @return ordered albums list
   */
  @SuppressWarnings("unchecked")
  public List<AlbumArtist> getAlbumArtists() {
    return (List<AlbumArtist>) getItems();
  }
}
