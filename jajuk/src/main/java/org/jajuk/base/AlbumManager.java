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
 *
 */

package org.jajuk.base;

import org.jajuk.util.MD5Processor;
import org.jajuk.util.error.JajukException;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Convenient class to manage Albums
 * 
 */
public class AlbumManager extends ItemManager {
	/** Self instance */
	private static AlbumManager singleton;

	/**
	 * No constructor available, only static access
	 */
	private AlbumManager() {
		super();
		// register properties
		// ID
		registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
				String.class, null));
		// Name
		registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, true, false,
				String.class, null));
		// Expand
		registerProperty(new PropertyMetaInformation(XML_EXPANDED, false, false, false, false,
				true, Boolean.class, false));
	}

	/**
	 * @return singleton
	 */
	public static AlbumManager getInstance() {
		if (singleton == null) {
			singleton = new AlbumManager();
		}
		return singleton;
	}

	/**
	 * Register an Album
	 * 
	 * @param sName
	 */
	public Album registerAlbum(String sName) {
		String sId = getID(sName);
		return registerAlbum(sId, sName);
	}

	/**
	 * Return hashcode for this item
	 * 
	 * @param sName
	 *            item name
	 * @return ItemManager ID
	 */
	protected static String getID(String sName) {
		return MD5Processor.hash(sName);
	}

	/**
	 * Register an Album with a known id
	 * 
	 * @param sName
	 */
	public Album registerAlbum(String sId, String sName) {
		synchronized (TrackManager.getInstance().getLock()) {
			if (hmItems.containsKey(sId)) {
				Album album = (Album) hmItems.get(sId);
				return album;
			}
			Album album = null;
			album = new Album(sId, sName);
			hmItems.put(sId, album);
			return album;
		}
	}

	/**
	 * Change the item
	 * 
	 * @param old
	 * @param sNewName
	 * @return new album
	 */
	public Album changeAlbumName(Album old, String sNewName) throws JajukException {
		// check there is actually a change
		if (old.getName2().equals(sNewName)) {
			return old;
		}
		Album newItem = registerAlbum(sNewName);
		// re apply old properties from old item
		newItem.cloneProperties(old);
		// update tracks
		for (Track track : TrackManager.getInstance().getTracks()) {
			if (track.getAlbum().equals(old)) {
				TrackManager.getInstance().changeTrackAlbum(track, sNewName, null);
			}
		}
		return newItem;
	}

	/**
	 * Format the album name to be normalized :
	 * <p>
	 * -no underscores or other non-ascii characters
	 * <p>
	 * -no spaces at the begin and the end
	 * <p>
	 * -All in lower cas expect first letter of first word
	 * <p>
	 * exemple: "My album title"
	 * 
	 * @param sName
	 * @return
	 */
	public static synchronized String format(String sName) {
		String sOut;
		sOut = sName.trim(); // supress spaces at the begin and the end
		sOut.replace('-', ' '); // move - to space
		sOut.replace('_', ' '); // move _ to space
		char c = sOut.charAt(0);
		sOut = sOut.toLowerCase();
		StringBuffer sb = new StringBuffer(sOut);
		sb.setCharAt(0, Character.toUpperCase(c));
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ItemManager#getIdentifier()
	 */
	public String getIdentifier() {
		return XML_ALBUMS;
	}

	/**
	 * @param sID
	 *            Item ID
	 * @return Element
	 */
	public Album getAlbumByID(String sID) {
		synchronized (getLock()) {
			return (Album) hmItems.get(sID);
		}
	}

	/**
	 * 
	 * @return albums list
	 */
	public Set<Album> getAlbums() {
		Set<Album> albumSet = new LinkedHashSet<Album>();
		synchronized (getLock()) {
			for (Item item : getItems()) {
				albumSet.add((Album) item);
			}
		}
		return albumSet;
	}

	/**
	 * Get albums associated with this item
	 * 
	 * @param item
	 * @return
	 */
	public Set<Album> getAssociatedAlbums(Item item) {
		synchronized (AlbumManager.getInstance().getLock()) {
			Set<Album> out = new TreeSet<Album>();
			for (Object item2 : hmItems.values()) {
				Album album = (Album) item2;
				if (item instanceof Track && ((Track) item).getAlbum().equals(album)){
					out.add(album);
				}
				else{
					Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item);
					for (Track track: tracks){
						out.add(track.getAlbum());
					}
				}
			}
			return out;
		}
	}

}
