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

import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage playlists files
 */
public class PlaylistFileManager extends ItemManager implements Observer {
  /** Self instance */
  private static PlaylistFileManager singleton;

  /**
   * No constructor available, only static access
   */
  private PlaylistFileManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, true, false,
        String.class, null));
    // Hashcode
    registerProperty(new PropertyMetaInformation(XML_HASHCODE, false, false, false, false, false,
        String.class, null));
    // Directory
    registerProperty(new PropertyMetaInformation(XML_DIRECTORY, false, true, true, false, false,
        String.class, null));
  }

  /**
   * @return singleton
   */
  public static PlaylistFileManager getInstance() {
    if (singleton == null) {
      singleton = new PlaylistFileManager();
    }
    return singleton;
  }

  /**
   * Register an PlaylistFile with a known id
   * 
   * @param fio
   * @param dParentDirectory
   */
  public PlaylistFile registerPlaylistFile(java.io.File fio, Directory dParentDirectory)
      throws Exception {
    synchronized (PlaylistFileManager.getInstance().getLock()) {
      String sId = createID(fio.getName(), dParentDirectory);
      return registerPlaylistFile(sId, fio.getName(), dParentDirectory);
    }
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
   * Delete a playlist file
   * 
   */
  public void removePlaylistFile(PlaylistFile plf) {
    synchronized (PlaylistFileManager.getInstance().getLock()) {
      String sFileToDelete = plf.getDirectory().getFio().getAbsoluteFile().toString()
          + java.io.File.separatorChar + plf.getName();
      java.io.File fileToDelete = new java.io.File(sFileToDelete);
      if (fileToDelete.exists()) {
        fileToDelete.delete();
        // check that file has been really deleted (sometimes, we get no
        // exception)
        if (fileToDelete.exists()) {
          Log.error("131", new JajukException(131));
          Messages.showErrorMessage(131);
          return;
        }
      }
      // remove reference from playlist
      PlaylistManager.getInstance().removePlaylistFile(plf);
      plf.getDirectory().removePlaylistFile(plf);
      // remove playlist file
      removeItem(plf.getID());
    }
  }

  /**
   * Register an PlaylistFile with a known id
   * 
   * @param sName
   */
  public synchronized PlaylistFile registerPlaylistFile(String sId, String sName,
      Directory dParentDirectory) throws Exception {
    synchronized (PlaylistFileManager.getInstance().getLock()) {
      if (!hmItems.containsKey(sId)) {
        PlaylistFile playlistFile = null;
        playlistFile = new PlaylistFile(sId, sName, dParentDirectory);
        hmItems.put(sId, playlistFile);
        if (dParentDirectory.getDevice().isRefreshing()) {
          Log.debug("Registered new playlist file: " + playlistFile);
        }
      }
      return (PlaylistFile) hmItems.get(sId);
    }
  }

  /**
   * Clean all references for the given device
   * 
   * @param sId :
   *          Device id
   */
  public void cleanDevice(String sId) {
    synchronized (PlaylistFileManager.getInstance().getLock()) {
      Iterator it = hmItems.values().iterator();
      while (it.hasNext()) {
        PlaylistFile plf = (PlaylistFile) it.next();
        if (plf.getDirectory() == null || plf.getDirectory().getDevice().getID().equals(sId)) {
          it.remove();
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  public String getLabel() {
    return XML_PLAYLIST_FILES;
  }

  /**
   * Change a playlist file name
   * 
   * @param plfOld
   * @param sNewName
   * @return new playlist file
   */
  public PlaylistFile changePlaylistFileName(PlaylistFile plfOld, String sNewName)
      throws JajukException {
    synchronized (PlaylistFileManager.getInstance().getLock()) {
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
      String sNewId = PlaylistFileManager.createID(sNewName, plfOld.getDirectory());
      // create a new playlist file (with own fio and sAbs)
      PlaylistFile plfNew = new PlaylistFile(sNewId, sNewName, plfOld.getDirectory());
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
          || !(Util.getExtension(ioNew).equals(EXT_PLAYLIST))) { // check
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
        throw new JajukException(134);
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
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(Event event) {
    synchronized (getLock()) {
      EventSubject subject = event.getSubject();
      if (EventSubject.EVENT_FILE_NAME_CHANGED.equals(subject)) {
        Properties properties = event.getDetails();
        File fNew = (File) properties.get(DETAIL_NEW);
        File fileOld = (File) properties.get(DETAIL_OLD);
        // search references in playlists
        Iterator it = hmItems.values().iterator();
        for (int i = 0; it.hasNext(); i++) {
          PlaylistFile plf = (PlaylistFile) it.next();
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
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_NAME_CHANGED);
    return eventSubjectSet;
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public PlaylistFile getPlaylistFileByID(String sID) {
    return (PlaylistFile) hmItems.get(sID);
  }

  /**
   * 
   * @return playlist files list
   */
  public Set<PlaylistFile> getPlaylistFiles() {
    Set<PlaylistFile> playListFileSet = new LinkedHashSet<PlaylistFile>();
    synchronized (getLock()) {
      for (Item item : getItems()) {
        playListFileSet.add((PlaylistFile) item);
      }
    }
    return playListFileSet;
  }
}
