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

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.items.Item;
import org.jajuk.base.items.Playlist;
import org.jajuk.base.items.PlaylistFile;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Messages;
import org.jajuk.util.Resources.ConfKeys;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage playlists
 */
public class PlaylistManager extends ItemManager<Playlist> {
  /** Self instance */
  private static PlaylistManager singleton;

  /**
   * No constructor available, only static access
   */
  public PlaylistManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new MetaProperty(XML.ID, false, true, false, false, false,
        String.class, null));
    // Playlist file
    registerProperty(new MetaProperty(XML.PLAYLIST_FILES, false, true, true, false,
        false, String.class, null));
  }

  /**
   * Register an Playlist
   *
   * @param file :
   *          playlist file
   */
  public Playlist registerPlaylist(final PlaylistFile plFile) {
    return registerPlaylist(createID(plFile), plFile);
  }

  /**
   * @param plf
   *          playlist file
   * @return ItemManager ID
   */
  protected static String createID(final PlaylistFile plf) {
    return plf.getHashcode();
  }

  /**
   * Register an Playlist with a known id
   *
   * @param file :
   *          playlist file
   */
  public Playlist registerPlaylist(final String sId, final PlaylistFile plFile) {
    synchronized (getLock()) {
      if (getItems().containsKey(sId)) { // playlist already exist, add a
        // file
        final Playlist playlist = getItems().get(sId);
        if (!playlist.getPlaylistFiles().contains(plFile)) {
          playlist.addFile(plFile);
        }
        return playlist;
      } else { // new playlist
        // firstly, make sure the playlist file is not already
        // referenced by another playlist
        boolean bPresence = false;
        for (final Item item : getItems().values()) {
          final Playlist pl = (Playlist) item;
          if (pl.getPlaylistFiles().contains(plFile)) {
            bPresence = true;
          }
        }
        if (bPresence) {
          return null;
        }
        Playlist playlist = null;
        playlist = new Playlist(sId, plFile);
        playlist.removeProperty(XML.NAME);// no name attribute for
        // playlists
        getItems().put(sId, playlist);
        return playlist;
      }
    }
  }

  public void removePlaylistFile(final PlaylistFile plf) {
    synchronized (getLock()) {
      final Playlist pl = getPlayList(plf);
      if (pl == null) {
        return;
      }
      pl.removePlaylistFile(plf);
      plf.getDirectory().removePlaylistFile(plf);
      if (pl.getPlaylistFiles().size() == 0) {
        removeItem(pl.getID());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void removePlaylist(final Playlist pl) {
    synchronized (getLock()) {
      // file deletion confirmation
      if (ConfigurationManager.getBoolean(ConfKeys.CONFIRMATIONS_DELETE_FILE)) {
        String sFileToDelete = "";
        String sMessage = Messages.getString("Confirmation_delete");
        for (final PlaylistFile plf : pl.getPlaylistFiles()) {
          sFileToDelete = plf.getDirectory().getFio().getAbsoluteFile().toString()
              + java.io.File.separatorChar + pl.getName();
          sMessage += "\n" + sFileToDelete;
        }
        final int i = Messages.getChoice(sMessage, JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (i == JOptionPane.YES_OPTION) {
          boolean bUnmountedItems = false;
          // take a shallow copy of the array to avoid concurrency
          // exception
          final ArrayList<PlaylistFile> alFiles = (ArrayList<PlaylistFile>) pl.getPlaylistFiles().clone();
          for (int j = 0; j < alFiles.size(); j++) {
            final PlaylistFile plf = alFiles.get(j);
            final java.io.File fileToDelete = plf.getFio();
            if (fileToDelete.exists()) {
              fileToDelete.delete();
              // check that file has been really deleted
              // (sometimes, we get no exception)
              if (fileToDelete.exists()) {
                Log.error("131", new JajukException(131));
                Messages.showErrorMessage(131);
                continue;
              }
              ((PlaylistFileManager) ItemType.PlaylistFile.getManager()).removeItem(plf.getID());
              removePlaylistFile(plf);
            } else {
              bUnmountedItems = true;
            }
          }
          if (pl.getPlaylistFiles().size() == 0) {
            removeItem(pl.getID());
          }
          if (bUnmountedItems) {
            Messages.showErrorMessage(138);
          }
        }
      }
    }
  }

  /**
   * Update associated playlist after a change into the playlist. A playlist can
   * only map playlists with exactly the same content
   *
   * @param plf
   *          changed playlist file
   */
  public void refreshPlaylist(final PlaylistFile plf) {
    final Playlist pl = getPlayList(plf);
    // check if a change really occured
    if (pl.getID().equals(plf.getHashcode())) {
      return;
    }
    pl.removePlaylistFile(plf);
    // if no more mapped playlist files, remove this playlist
    if (pl.getPlaylistFiles().size() == 0) {
      removeItem(pl.getID());
    }
    // Register the new playlist
    registerPlaylist(plf);
  }

  /**
   * Perform a playlist cleanup : delete useless items
   *
   */
  @Override
  public void cleanup() {
    synchronized (getLock()) {
      final Iterator itPlaylists = getItems().values().iterator();

      while (itPlaylists.hasNext()) {
        final Playlist playlist = (Playlist) itPlaylists.next();
        final Iterator itPlaylistFiles = playlist.getPlaylistFiles().iterator();
        while (itPlaylistFiles.hasNext()) {
          final PlaylistFile plf = (PlaylistFile) itPlaylistFiles.next();
          if (((PlaylistFileManager) ItemType.PlaylistFile.getManager()).getPlaylistFileByID(plf.getID()) == null) {
            itPlaylistFiles.remove();
          }
        }
        if (playlist.getPlaylistFiles().size() == 0) {
          itPlaylists.remove();
        }
      }
    }
  }

  /**
   * Return playlist associated with a given playlist file
   *
   * @param plfi
   * @return the playlist or null if none associated playlist
   */
  public Playlist getPlaylist(final PlaylistFile plf) {
    synchronized (getLock()) {
      final Iterator it = getItems().values().iterator();
      while (it.hasNext()) {
        final Playlist pl = (Playlist) it.next();
        if (pl.getPlaylistFiles().contains(plf)) {
          return pl;
        }
      }
      return null;
    }
  }

  /**
   *
   * @param plf
   * @return pl ylist for a given playlist file
   */
  public Playlist getPlayList(final PlaylistFile plf) {
    synchronized (getLock()) {
      final Iterator it = getItems().values().iterator();

      while (it.hasNext()) {
        final Playlist pl = (Playlist) it.next();
        if (pl.getPlaylistFiles().contains(plf)) {
          return pl;
        }
      }
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML.PLAYLISTS;
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public Playlist getPlaylistByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }
}
