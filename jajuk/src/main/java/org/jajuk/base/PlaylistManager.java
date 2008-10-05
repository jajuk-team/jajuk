/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.Observer;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage playlists
 */
public final class PlaylistManager extends ItemManager implements Observer {
  /** Self instance */
  private static PlaylistManager singleton;

  /**
   * No constructor available, only static access
   */
  private PlaylistManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, true, false,
        String.class, null));
    // Directory
    registerProperty(new PropertyMetaInformation(XML_DIRECTORY, false, true, true, false, false,
        String.class, null));
  }

  /**
   * @return singleton
   */
  public static PlaylistManager getInstance() {
    if (singleton == null) {
      singleton = new PlaylistManager();
    }
    return singleton;
  }

  /**
   * Register an Playlist with a known id
   * 
   * @param fio
   * @param dParentDirectory
   */
  public synchronized Playlist registerPlaylistFile(java.io.File fio, Directory dParentDirectory)
      throws Exception {
    String sId = createID(fio.getName(), dParentDirectory);
    return registerPlaylistFile(sId, fio.getName(), dParentDirectory);
  }

  /**
   * @param sName
   * @param dParentDirectory
   * @return ItemManager ID
   */
  protected static String createID(String sName, Directory dParentDirectory) {
    return MD5Processor.hash(new StringBuilder(dParentDirectory.getDevice().getName()).append(
        dParentDirectory.getRelativePath()).append(sName).toString());
  }

  /**
   * Delete a playlist
   * 
   */
  public synchronized void removePlaylistFile(Playlist plf) {
    String sFileToDelete = plf.getDirectory().getFio().getAbsoluteFile().toString()
        + java.io.File.separatorChar + plf.getName();
    java.io.File fileToDelete = new java.io.File(sFileToDelete);
    if (fileToDelete.exists()) {
      if (!fileToDelete.delete()) {
        Log.warn("Could not delete file: " + fileToDelete.toString());
      }
      // check that file has been really deleted (sometimes, we get no
      // exception)
      if (fileToDelete.exists()) {
        Log.error("131", new JajukException(131));
        Messages.showErrorMessage(131);
        return;
      }
    }
    plf.getDirectory().removePlaylistFile(plf);
    // remove playlist
    removeItem(plf.getID());
  }

  /**
   * Register an Playlist with a known id
   * 
   * @param sName
   */
  public synchronized Playlist registerPlaylistFile(String sId, String sName,
      Directory dParentDirectory) throws Exception {
    if (!hmItems.containsKey(sId)) {
      Playlist playlistFile = null;
      playlistFile = new Playlist(sId, sName, dParentDirectory);
      hmItems.put(sId, playlistFile);
      if (dParentDirectory.getDevice().isRefreshing()) {
        Log.debug("Registered new playlist: " + playlistFile);
      }
    }
    return (Playlist) hmItems.get(sId);
  }

  /**
   * Clean all references for the given device
   * 
   * @param sId :
   *          Device id
   */
  @SuppressWarnings("unchecked")
  public synchronized void cleanDevice(String sId) {
    Iterator<Playlist> it = hmItems.values().iterator();
    while (it.hasNext()) {
      Playlist plf = it.next();
      if (plf.getDirectory() == null || plf.getDirectory().getDevice().getID().equals(sId)) {
        it.remove();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML_PLAYLIST_FILES;
  }

  /**
   * Change a playlist name
   * 
   * @param plfOld
   * @param sNewName
   * @return new playlist
   */
  public synchronized Playlist changePlaylistFileName(Playlist plfOld, String sNewName)
      throws JajukException {
    // check given name is different
    if (plfOld.getName().equals(sNewName)) {
      return plfOld;
    }
    // check if this file still exists
    if (!plfOld.getFio().exists()) {
      throw new JajukException(135);
    }
    java.io.File ioNew = new java.io.File(plfOld.getFio().getParentFile().getAbsolutePath()
        + java.io.File.separator + sNewName);
    // recalculate file ID
    plfOld.getDirectory();
    String sNewId = PlaylistManager.createID(sNewName, plfOld.getDirectory());
    // create a new playlist (with own fio and sAbs)
    Playlist plfNew = new Playlist(sNewId, sNewName, plfOld.getDirectory());
    plfNew.setProperties(plfOld.getProperties()); // transfert all
    // properties
    // (inc id and
    // name)
    plfNew.setProperty(XML_ID, sNewId); // reset new id and name
    plfNew.setProperty(XML_NAME, sNewName); // reset new id and name
    // check file name and extension
    if (plfNew.getName().lastIndexOf('.') != plfNew.getName().indexOf('.')// just
        // one
        // '.'
        || !(UtilSystem.getExtension(ioNew).equals(EXT_PLAYLIST))) { // check
      // extension
      Messages.showErrorMessage(134);
      throw new JajukException(134);
    }
    // check if future file exists (under windows, file.exists
    // return true even with different case so we test file name is
    // different)
    if (!ioNew.getName().equalsIgnoreCase(plfOld.getName()) && ioNew.exists()) {
      throw new JajukException(134);
    }
    // try to rename file on disk
    try {
      plfOld.getFio().renameTo(ioNew);
    } catch (Exception e) {
      throw new JajukException(134, e);
    }
    // OK, remove old file and register this new file
    hmItems.remove(plfOld.getID());
    if (!hmItems.containsKey(sNewId)) {
      hmItems.put(sNewId, plfNew);
    }
    // change directory reference
    plfNew.getDirectory().changePlaylistFile(plfOld, plfNew);
    return plfNew;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  @SuppressWarnings("unchecked")
  public synchronized void update(Event event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.FILE_NAME_CHANGED.equals(subject)) {
      Properties properties = event.getDetails();
      File fNew = (File) properties.get(DETAIL_NEW);
      File fileOld = (File) properties.get(DETAIL_OLD);
      // search references in playlists
      Iterator<Playlist> it = hmItems.values().iterator();
      for (; it.hasNext();) {
        Playlist plf = it.next();
        if (plf.isReady()) { // check only in mounted
          // playlists, note that we can't
          // change unmounted playlists
          try {
            if (plf.getFiles().contains(fileOld)) {
              plf.replaceFile(fileOld, fNew);
            }
          } catch (Exception e) {
            Log.error(17, e);
          }
        }
      }
    }
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_NAME_CHANGED);
    return eventSubjectSet;
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public Playlist getPlaylistByID(String sID) {
    return (Playlist) hmItems.get(sID);
  }

  /**
   * 
   * @return playlists
   */
  public synchronized Set<Playlist> getPlaylists() {
    Set<Playlist> playListSet = new LinkedHashSet<Playlist>();
    for (Item item : getItems()) {
      playListSet.add((Playlist) item);
    }
    return playListSet;
  }
}
