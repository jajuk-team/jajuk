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

import org.jajuk.i18n.Messages;

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
	private ArrayList<PlaylistFile> alPlaylistFiles = new ArrayList<PlaylistFile>(
			2);

	/**
	 * Playlist constructor
	 * 
	 * @param sId
	 * @param file
	 *            :an associated playlist file
	 */
	public Playlist(String sId, PlaylistFile plFile) {
		super(sId, null);
		this.alPlaylistFiles.add(plFile);
		setProperty(XML_PLAYLIST_FILES, plFile.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIdentifier()
	 */
	final public String getIdentifier() {
		return XML_PLAYLIST;
	}

	/**
	 * toString method
	 */
	public String toString() {
		StringBuffer sbOut = new StringBuffer("Playlist[ID=" + sId + "]");   
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
			StringBuffer sbOut = new StringBuffer();
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
			String sPlaylistFiles = plFile.getId();
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
			sPlaylistFiles += alPlaylistFiles.get(0).getId();
			for (int i = 1; i < alPlaylistFiles.size(); i++) {
				sPlaylistFiles += "," + alPlaylistFiles.get(i).getId(); 
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
	 *            playlist to be compared
	 * @return comparaison result
	 */
	public int compareTo(Object o) {
		Playlist otherPlaylist = (Playlist) o;
		// use id in compare because 2 different playlists can have the same
		// name
		String sAbs = getName() + getId();
		String sOtherAbs = otherPlaylist.getName() + otherPlaylist.getId();
		if (sAbs.equalsIgnoreCase(sOtherAbs) && !sAbs.equals(sOtherAbs)) {
			return (sAbs + getId()).compareToIgnoreCase(sOtherAbs
					+ otherPlaylist.getId());
		} else {
			return sAbs.compareToIgnoreCase(sOtherAbs);
		}
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
