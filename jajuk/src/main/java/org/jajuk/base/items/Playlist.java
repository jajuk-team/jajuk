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
package org.jajuk.base.items;

import static org.jajuk.util.Resources.XML.PLAYLIST;
import static org.jajuk.util.Resources.XML.PLAYLIST_FILES;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;

import org.jajuk.base.LogicalItem;
import org.jajuk.util.Messages;

/**
 * A playlist
 * <p>
 * Logical item
 */
public class Playlist extends LogicalItem implements Comparable {

  private static final long serialVersionUID = 1L;

  /** Associated playlist files* */
  private ArrayList<PlaylistFile> alPlaylistFiles = new ArrayList<PlaylistFile>(2);

  /**
   * Playlist constructor
   *
   * @param sId
   * @param file
   *          :an associated playlist file
   */
  public Playlist(final String sId, final PlaylistFile plFile) {
    super(sId, null);
    alPlaylistFiles.add(plFile);
    getProperties().set(PLAYLIST_FILES, plFile.getID());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return PLAYLIST;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    final StringBuilder sbOut = new StringBuilder("Playlist[ID=" + getID() + "]");
    for (int i = 0; i < alPlaylistFiles.size(); i++) {
      sbOut.append('\n').append(alPlaylistFiles.get(i).toString());
    }
    return sbOut.toString();
  }

  /**
   * Add a playlist file
   *
   * @return
   */
  public ArrayList<PlaylistFile> getPlaylistFiles() {
    return alPlaylistFiles;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(final String sKey) {
    if (PLAYLIST_FILES.equals(sKey)) {
      final StringBuilder sbOut = new StringBuilder();
      final Iterator it = alPlaylistFiles.iterator();
      while (it.hasNext()) {
        final PlaylistFile plf = (PlaylistFile) it.next();
        sbOut.append(plf.getAbsolutePath() + ",");
      }
      return sbOut.substring(0, sbOut.length() - 1); // remove last
      // ','
    } else {// default
      return super.getHumanValue(sKey);
    }
  }

  /**
   * @return an available playlist file to play
   */
  public PlaylistFile getPlayeablePlaylistFile() {
    PlaylistFile plfOut = null;
    final Iterator it = alPlaylistFiles.iterator();
    while (it.hasNext()) {
      final PlaylistFile plf = (PlaylistFile) it.next();
      if (plf.isReady()) {
        plfOut = plf;
      }
    }
    return plfOut;
  }

  /**
   * @return
   */
  public void addFile(final PlaylistFile plFile) {
    if (!alPlaylistFiles.contains(plFile)) {
      alPlaylistFiles.add(plFile);
      String sPlaylistFiles = plFile.getID();
      if (getProperties().contains(PLAYLIST_FILES)) {
        sPlaylistFiles += "," + getValue(PLAYLIST_FILES); // add
        // previous
        // playlist
        // files
        //
      }
      getProperties().set(PLAYLIST_FILES, sPlaylistFiles);
    }
  }

  public void removePlaylistFile(final PlaylistFile plf) {
    if (alPlaylistFiles.contains(plf)) {
      alPlaylistFiles.remove(plf);
      rebuildProperty();
    }
  }

  /**
   * Rebuild playlist files property
   *
   * @return
   */
  public void rebuildProperty() {
    String sPlaylistFiles = "";
    if (alPlaylistFiles.size() > 0) {
      sPlaylistFiles += alPlaylistFiles.get(0).getID();
      for (int i = 1; i < alPlaylistFiles.size(); i++) {
        sPlaylistFiles += "," + alPlaylistFiles.get(i).getID();
      }
    }
    getProperties().set(PLAYLIST_FILES, sPlaylistFiles);
  }

  /**
   * Get playlist name
   *
   * @return playlist name
   */
  @Override
  public String getName() {
    String sOut = "";
    if (alPlaylistFiles.size() > 0) {
      sOut = alPlaylistFiles.get(0).getName();
    }
    return sOut;
  }

  /**
   * Alphabetical comparator used to display ordered lists of playlists
   * <p>
   * Sort ignoring cases but different items with different cases should be
   * distinct before being added into bidimap
   * </p>
   *
   * @param other
   *          playlist to be compared
   * @return comparaison result
   */
  public int compareTo(final Object o) {
    // Perf: leave if items are equals
    if (o.equals(this)) {
      return 0;
    }
    final Playlist otherPlaylist = (Playlist) o;
    // use id in compare because 2 different playlists can have the same
    // name
    final String sAbs = getName() + getID();
    final String sOtherAbs = otherPlaylist.getName() + otherPlaylist.getID();
    // never return 0 here, because bidimap needs to distinct items
    final int comp = sAbs.compareToIgnoreCase(sOtherAbs);
    if (comp == 0) {
      return sAbs.compareTo(sOtherAbs);
    }
    return comp;
  }

  /**
   * Get item description
   */
  @Override
  public String getDescription() {
    return Messages.getString("Item_Playlist") + " : " + getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIcon()
   */
  @Override
  public ImageIcon getIcon() {
    return null;
  }
}
