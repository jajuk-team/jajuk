/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 *  Convenient class to manage Albums
 * @Author    bflorat
 * @created    17 oct. 2003
 */
public class AlbumManager {
	/**Albums collection**/
	static HashMap hmAlbums = new HashMap(100);

	/**
	 * No constructor available, only static access
	 */
	private AlbumManager() {
		super();
	}

	/**
	 * Register an Album
	 *@param sName
	 */
	public static synchronized  Album registerAlbum(String sName) {
		String sId = MD5Processor.hash(sName.trim().toLowerCase());
		return registerAlbum(sId,sName);
	}
	
	/**
	 * Perform an album cleanup : delete useless items
	 *  
	 */
	public static synchronized void cleanup() {
		Iterator itAlbums = hmAlbums.values().iterator();
		ArrayList alTrack = TrackManager.getTracks();
		Iterator itTracks = null;
		while (itAlbums.hasNext()) {
			Album album = (Album) itAlbums.next();
			boolean bUsed = false;
			itTracks = alTrack.iterator();
			while (itTracks.hasNext()) {
				Track track = (Track) itTracks.next();
				if (track.getAlbum().equals(album)) {
					bUsed = true;
				}
			}
			if (!bUsed) { //clean this album
				itAlbums.remove();
			}
		}
	}
	
	
	/**
	 * Register an Album with a known id
	 * 
	 * @param sName
	 */
	public static synchronized Album registerAlbum(String sId, String sName) {
		String sIdTest = MD5Processor.hash(sName.trim().toLowerCase());
		if (hmAlbums.containsKey(sIdTest)) {
			return (Album) hmAlbums.get(sIdTest);
		}
		Album album = new Album(sId, sName);
		hmAlbums.put(sId, album);
		return album;
	}


	/**Return all registred Albums*/
	public static synchronized ArrayList getAlbums() {
		return new ArrayList(hmAlbums.values());
	}

	/**
	 * Return Album by id
	 * @param id
	 * @return
	 */
	public static synchronized Album getAlbum(String sId) {
		return (Album) hmAlbums.get(sId);
	}
	
	/**
		 * Format the album name to be normalized : 
		 * <p>-no underscores or other non-ascii characters
		 * <p>-no spaces at the begin and the end
		 * <p>-All in lower cas expect first letter of first word
		 * <p> exemple: "My album title" 
		 *  @param sName
		 * @return
		 */
		private static String format(String sName){
			String sOut;
			sOut = sName.trim(); //supress spaces at the begin and the end
			sOut.replace('-',' ');  //move - to space
			sOut.replace('_',' '); //move _ to space
			char c = sOut.charAt(0);
			sOut = sOut.toLowerCase();
			StringBuffer sb = new StringBuffer(sOut);
			sb.setCharAt(0,Character.toUpperCase(c));
			return sb.toString();
		}
	
}
