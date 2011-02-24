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
package org.jajuk.base;

/**
 * A smart playlist a special "logical" playlist built automatically by Jajuk
 * <p>Example of smart playlists includes : bestof, novelties, new and bookmarks </p>
 * This class is only a wrapper to Playlist object. It brings a better way to identify
 * smart playlist from regular ones and it provides a public constructor required
 * because we can't store this kind of playlist in the collection itself so
 * we call the constructor directly without performing any item registration.
 */
public class SmartPlaylist extends Playlist {

  /**
   * The Constructor.
   * 
   * @param sId DOCUMENT_ME
   * @param sName DOCUMENT_ME
   * @param dParentDirectory DOCUMENT_ME
   */
  public SmartPlaylist(String sId, String sName, Directory dParentDirectory) {
    super(sId, sName, dParentDirectory);
  }

  /**
   * The Constructor.
   * 
   * @param type DOCUMENT_ME
   * @param sId DOCUMENT_ME
   * @param sName DOCUMENT_ME
   * @param dParentDirectory DOCUMENT_ME
   */
  public SmartPlaylist(Type type, String sId, String sName, Directory dParentDirectory) {
    super(type, sId, sName, dParentDirectory);
  }

}
