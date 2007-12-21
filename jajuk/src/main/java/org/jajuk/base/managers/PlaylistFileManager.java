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

package org.jajuk.base.managers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.jajuk.base.Event;
import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.Observer;
import org.jajuk.base.items.Directory;
import org.jajuk.base.items.File;
import org.jajuk.base.items.PlaylistFile;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.Resources.Details;
import org.jajuk.util.Resources.Extensions;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage playlists files
 */
public class PlaylistFileManager extends ItemManager<PlaylistFile> implements Observer {
  /** Self instance */
  private static PlaylistFileManager singleton;

  /**
   * No constructor available, only static access
   */
  public PlaylistFileManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new MetaProperty(XML.ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new MetaProperty(XML.NAME, false, true, true, true, false,
        String.class, null));
    // Hashcode
    registerProperty(new MetaProperty(XML.HASHCODE, false, false, false, false, false,
        String.class, null));
    // Directory
    registerProperty(new MetaProperty(XML.DIRECTORY, false, true, true, false, false,
        String.class, null));
  }

  /**
   * Register an PlaylistFile with a known id
   *
   * @param fio
   * @param dParentDirectory
   */
  public PlaylistFile registerPlaylistFile(final java.io.File fio, final Directory dParentDirectory)
      throws Exception {
    synchronized (getLock()) {
      final String sId = createID(fio.getName(), dParentDirectory);
      return registerPlaylistFile(sId, fio.getName(), dParentDirectory);
    }
  }

  /**
   * @param sName
   * @param dParentDirectory
   * @return ItemManager ID
   */
  public static String createID(final String sName, final Directory dParentDirectory) {
    return MD5Processor.hash(new StringBuilder(dParentDirectory.getDevice().getName()).append(
        dParentDirectory.getRelativePath()).append(sName).toString());
  }

  /**
   * Delete a playlist file
   *
   */
  public void removePlaylistFile(final PlaylistFile plf) {
    synchronized (getLock()) {
      final String sFileToDelete = plf.getDirectory().getFio().getAbsoluteFile().toString()
          + java.io.File.separatorChar + plf.getName();
      final java.io.File fileToDelete = new java.io.File(sFileToDelete);
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
      ((PlaylistManager) ItemType.Playlist.getManager()).removePlaylistFile(plf);
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
  public synchronized PlaylistFile registerPlaylistFile(final String sId, final String sName,
      final Directory dParentDirectory) throws Exception {
    synchronized (getLock()) {
      if (!getItems().containsKey(sId)) {
        PlaylistFile playlistFile = null;
        playlistFile = new PlaylistFile(sId, sName, dParentDirectory);
        getItems().put(sId, playlistFile);
        if (dParentDirectory.getDevice().isRefreshing()) {
          Log.debug("Registered new playlist file: " + playlistFile);
        }
      }
      return getItems().get(sId);
    }
  }

  /**
   * Clean all references for the given device
   *
   * @param sId :
   *          Device id
   */
  public void cleanDevice(final String sId) {
    synchronized (getLock()) {
      final Iterator<PlaylistFile> it = getItems().values().iterator();
      while (it.hasNext()) {
        final PlaylistFile plf = it.next();

        if ((plf.getDirectory() == null) || plf.getDirectory().getDevice().getID().equals(sId)) {
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
  @Override
  public String getLabel() {
    return XML.PLAYLIST_FILES;
  }

  /**
   * Change a playlist file name
   *
   * @param plfOld
   * @param sNewName
   * @return new playlist file
   */
  public PlaylistFile changePlaylistFileName(final PlaylistFile plfOld, final String sNewName)
      throws JajukException {
    synchronized (getLock()) {
      // check given name is different
      if (plfOld.getName().equals(sNewName)) {
        return plfOld;
      }
      // check if this file still exists
      if (!plfOld.getFio().exists()) {
        throw new JajukException(135);
      }
      final java.io.File ioNew = new java.io.File(plfOld.getFio().getParentFile().getAbsolutePath()
          + java.io.File.separator + sNewName);
      // recalculate file ID
      plfOld.getDirectory();
      final String sNewId = PlaylistFileManager.createID(sNewName, plfOld.getDirectory());
      // create a new playlist file (with own fio and sAbs)
      final PlaylistFile plfNew = new PlaylistFile(sNewId, sNewName, plfOld.getDirectory());
      plfNew.setProperties(plfOld.getProperties()); // transfert all
      // properties
      // (inc id and
      // name)
      plfNew.getProperties().set(XML.ID, sNewId); // reset new id and name
      plfNew.getProperties().set(XML.NAME, sNewName); // reset new id and name
      // check file name and extension
      if ((plfNew.getName().lastIndexOf('.') != plfNew.getName().indexOf('.')// just
)
          // one
          // '.'
          || !(Util.getExtension(ioNew).equals(Extensions.PLAYLIST))) { // check
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
      } catch (final Exception e) {
        throw new JajukException(134);
      }
      // OK, remove old file and register this new file
      getItems().remove(plfOld.getID());
      if (!getItems().containsKey(sNewId)) {
        getItems().put(sNewId, plfNew);
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
  public void update(final Event event) {
    synchronized (getLock()) {
      final EventSubject subject = event.getSubject();
      if (EventSubject.EVENT_FILE_NAME_CHANGED.equals(subject)) {
        final Properties properties = event.getDetails();
        final File fNew = (File) properties.get(Details.NEW);
        final File fileOld = (File) properties.get(Details.OLD);
        // search references in playlists
        final Iterator it = getItems().values().iterator();
        for (int i = 0; it.hasNext(); i++) {
          final PlaylistFile plf = (PlaylistFile) it.next();
          if (plf.isReady()) { // check only in mounted
            // playlists, note that we can't
            // change unmounted playlists
            try {
              if (plf.getFiles().contains(fileOld)) {
                plf.replaceFile(fileOld, fNew);
              }
            } catch (final Exception e) {
              Log.error(17, e);
            }
          }
        }
        // refresh UI
        ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
      }
    }
  }

  public Set<EventSubject> getRegistrationKeys() {
    final HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_NAME_CHANGED);
    return eventSubjectSet;
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public PlaylistFile getPlaylistFileByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }

  /**
   *
   * @return playlist files list
   */
  public Set<PlaylistFile> getPlaylistFiles() {
    final Set<PlaylistFile> playListFileSet = new LinkedHashSet<PlaylistFile>();

    synchronized (getLock()) {
      for (final PlaylistFile item : getItems().values()) {
        playListFileSet.add(item);
      }
    }
    return playListFileSet;
  }
}
