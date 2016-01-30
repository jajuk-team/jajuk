/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.Observer;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage playlists.
 */
public final class PlaylistManager extends ItemManager implements Observer {
  /** Self instance. */
  private static PlaylistManager singleton = new PlaylistManager();

  /**
   * No constructor available, only static access.
   */
  private PlaylistManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    // Directory
    registerProperty(new PropertyMetaInformation(Const.XML_DIRECTORY, false, true, true, false,
        false, String.class, null));
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static PlaylistManager getInstance() {
    return singleton;
  }

  /**
   * Register an Playlist with a known id.
   * 
   * @param fio 
   * @param dParentDirectory 
   * 
   * @return the playlist
   */
  Playlist registerPlaylistFile(java.io.File fio, Directory dParentDirectory) {
    String sId = createID(fio.getName(), dParentDirectory);
    return registerPlaylistFile(sId, fio.getName(), dParentDirectory);
  }

  /**
   * Creates the id.
   * 
   * @param sName 
   * @param dParentDirectory 
   * 
   * @return ItemManager ID
   */
  protected static String createID(String sName, Directory dParentDirectory) {
    return MD5Processor.hash(new StringBuilder(dParentDirectory.getDevice().getName())
        .append(dParentDirectory.getRelativePath()).append(sName).toString());
  }

  /**
   * Remove a playlist.
   * 
   * @param plf the playlist
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void removePlaylistFile(Playlist plf) throws IOException {
    String sFileToDelete = plf.getDirectory().getFio().getAbsoluteFile().toString()
        + java.io.File.separatorChar + plf.getName();
    lock.writeLock().lock();
    try {
      java.io.File fileToDelete = new java.io.File(sFileToDelete);
      if (fileToDelete.exists()) {
        UtilSystem.deleteFile(fileToDelete);
      }
      // remove playlist
      removeItem(plf);
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Delete physically a playlist.
   * 
   * @param plf the playlist
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void deletePlaylistFile(Playlist plf) throws IOException {
    lock.writeLock().lock();
    try {
      String sFileToDelete = plf.getDirectory().getFio().getAbsoluteFile().toString()
          + java.io.File.separatorChar + plf.getName();
      java.io.File fileToDelete = new java.io.File(sFileToDelete);
      if (fileToDelete.exists()) {
        UtilSystem.deleteFile(fileToDelete);
      }
      // remove playlist
      removePlaylistFile(plf);
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Register an Playlist with a known id.
   *
   * @param sId 
   * @param sName 
   * @param dParentDirectory 
   * @return the playlist
   */
  public Playlist registerPlaylistFile(String sId, String sName, Directory dParentDirectory) {
    Playlist playlistFile = getPlaylistByID(sId);
    if (playlistFile != null) {
      return playlistFile;
    }
    playlistFile = new Playlist(sId, sName, dParentDirectory);
    registerItem(playlistFile);
    return playlistFile;
  }

  /**
   * Clean all references for the given device.
   * 
   * @param sId :
   * Device id
   */
  public void cleanDevice(String sId) {
    for (Playlist plf : getPlaylists()) {
      if (plf.getDirectory() == null || plf.getDirectory().getDevice().getID().equals(sId)) {
        removeItem(plf);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getXMLTag() {
    return Const.XML_PLAYLIST_FILES;
  }

  /**
   * Change a playlist name.
   * 
   * @param plfOld 
   * @param sNewName 
   * 
   * @return new playlist
   * 
   * @throws JajukException the jajuk exception
   */
  Playlist changePlaylistFileName(Playlist plfOld, String sNewName) throws JajukException {
    lock.writeLock().lock();
    try {
      // check given name is different
      if (plfOld.getName().equals(sNewName)) {
        return plfOld;
      }
      // check if this file still exists
      if (!plfOld.getFIO().exists()) {
        throw new JajukException(135);
      }
      java.io.File ioNew = new java.io.File(plfOld.getFIO().getParentFile().getAbsolutePath()
          + java.io.File.separator + sNewName);
      // recalculate file ID
      plfOld.getDirectory();
      String sNewId = PlaylistManager.createID(sNewName, plfOld.getDirectory());
      // create a new playlist (with own fio and sAbs)
      Playlist plfNew = new Playlist(sNewId, sNewName, plfOld.getDirectory());
      // Transfer all properties (id and name)
      plfNew.setProperties(plfOld.getProperties());
      plfNew.setProperty(Const.XML_ID, sNewId); // reset new id and name
      plfNew.setProperty(Const.XML_NAME, sNewName); // reset new id and name
      // check file name and extension
      if (plfNew.getName().lastIndexOf('.') != plfNew.getName().indexOf('.')// just
          // one
          // '.'
          || !(UtilSystem.getExtension(ioNew).equals(Const.EXT_PLAYLIST))) { // check
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
        boolean result = plfOld.getFIO().renameTo(ioNew);
        if (!result) {
          throw new IOException();
        }
      } catch (Exception e) {
        throw new JajukException(134, e);
      }
      // OK, remove old file and register this new file
      removeItem(plfOld);
      registerItem(plfNew);
      return plfNew;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.FILE_NAME_CHANGED.equals(subject)) {
      lock.writeLock().lock();
      try {
        Properties properties = event.getDetails();
        File fNew = (File) properties.get(Const.DETAIL_NEW);
        File fileOld = (File) properties.get(Const.DETAIL_OLD);
        // search references in playlists
        ReadOnlyIterator<Playlist> it = getPlaylistsIterator();
        while (it.hasNext()) {
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
      } finally {
        lock.writeLock().unlock();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_NAME_CHANGED);
    return eventSubjectSet;
  }

  /**
   * Gets the playlist by id.
   * 
   * @param sID Item ID
   * 
   * @return item
   */
  public Playlist getPlaylistByID(String sID) {
    return (Playlist) getItemByID(sID);
  }

  /**
   * Gets the playlists.
   * 
   * @return ordered playlists list
   */
  @SuppressWarnings("unchecked")
  public List<Playlist> getPlaylists() {
    return (List<Playlist>) getItems();
  }

  /**
   * Gets the playlists iterator.
   * 
   * @return playlists iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<Playlist> getPlaylistsIterator() {
    return new ReadOnlyIterator<Playlist>((Iterator<Playlist>) getItemsIterator());
  }

  /**
   * Returns the first playlist with the given name.
   * 
   * @param name The name of the Playlist to search
   * 
   * @return The playlist if found, null otherwise.
   */
  public Playlist getPlaylistByName(String name) {
    for (Playlist pl : getPlaylists()) {
      // if this is the correct playlist, return it
      if (pl.getName().equals(name)) {
        return pl;
      }
    }
    // none found
    return null;
  }
}
