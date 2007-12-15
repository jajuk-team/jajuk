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

import org.jajuk.util.Messages;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;

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
  public Playlist(String sId, PlaylistFile plFile) {
    super(sId, null);
    this.alPlaylistFiles.add(plFile);
    setProperty(XML_PLAYLIST_FILES, plFile.getID());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  final public String getLabel() {
    return XML_PLAYLIST;
  }

  /**
   * toString method
   */
  public String toString() {
    StringBuilder sbOut = new StringBuilder("Playlist[ID=" + getID() + "]");
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
  public String getHumanValue(String sKey) {
    if (XML_PLAYLIST_FILES.equals(sKey)) {
      StringBuilder sbOut = new StringBuilder();
      Iterator it = alPlaylistFiles.iterator();
      while (it.hasNext()) {
        PlaylistFile plf = (PlaylistFile) it.next();
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
    Iterator it = alPlaylistFiles.iterator();
    while (it.hasNext()) {
      PlaylistFile plf = (PlaylistFile) it.next();
      if (plf.isReady()) {
        plfOut = plf;
      }
    }
    return plfOut;
  }

  /**
   * @return
   */
  public void addFile(PlaylistFile plFile) {
    if (!alPlaylistFiles.contains(plFile)) {
      alPlaylistFiles.add(plFile);
      String sPlaylistFiles = plFile.getID();
      if (this.containsProperty(XML_PLAYLIST_FILES)) {
        sPlaylistFiles += "," + getValue(XML_PLAYLIST_FILES); // add
        // previous
        // playlist
        // files
        // 
      }
      setProperty(XML_PLAYLIST_FILES, sPlaylistFiles);
    }
  }

  protected void removePlaylistFile(PlaylistFile plf) {
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
    setProperty(XML_PLAYLIST_FILES, sPlaylistFiles);
  }

  /**
   * Get playlist name
   * 
   * @return playlist name
   */
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
  public int compareTo(Object o) {
    // Perf: leave if items are equals
    if (o.equals(this)) {
      return 0;
    }
    Playlist otherPlaylist = (Playlist) o;
    // use id in compare because 2 different playlists can have the same
    // name
    String sAbs = getName() + getID();
    String sOtherAbs = otherPlaylist.getName() + otherPlaylist.getID();
    // never return 0 here, because bidimap needs to distinct items
    int comp = sAbs.compareToIgnoreCase(sOtherAbs);
    if (comp == 0) {
      return sAbs.compareTo(sOtherAbs);
    }
    return comp;
  }

  /**
   * Get item description
   */
  public String getDesc() {
    return Messages.getString("Item_Playlist") + " : " + getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    return null;
  }
}
