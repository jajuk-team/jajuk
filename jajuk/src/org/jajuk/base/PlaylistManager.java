/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *  Convenient class to manage playlists
 * @Author    Bertrand Florat
 * @created    17 oct. 2003
 */
public class PlaylistManager {
	/**Playlists collection**/
	static HashMap hmPlaylists = new HashMap(100);

	/**
	 * No constructor available, only static access
	 */
	private PlaylistManager() {
		super();
	}

	/**
	 * Register an Playlist
	 *@param file : playlist file
	 */	
	public static synchronized Playlist registerPlaylist(PlaylistFile plFile) {
		return registerPlaylist(plFile.getHashcode(),plFile);
	}
	
	/**
	 * Register an Playlist with a known id
	 *@param file : playlist file
	 */	
	public static synchronized Playlist registerPlaylist(String sId,PlaylistFile plFile) {
		if (hmPlaylists.containsKey(sId)){ //playlist already exist, add a file
			Playlist playlist = (Playlist)hmPlaylists.get(sId);
			playlist.addFile(plFile);
			return playlist;
		}
		else { //new playlist
			Playlist playlist = new Playlist(sId,plFile);
			hmPlaylists.put(sId,playlist);
			return playlist;
		}
	}


	/**Return all registred Playlists*/
	public static synchronized ArrayList getPlaylists() {
		return new ArrayList(hmPlaylists.values());
	}

	/**
		 * Perform a playlist cleanup : delete useless items
		 *  
		 */
		public static synchronized void cleanup() {
			Iterator itPlaylists = hmPlaylists.values().iterator();
			while (itPlaylists.hasNext()) {
				Playlist playlist= (Playlist)itPlaylists.next();
				Iterator itPlaylistFiles = playlist.getPlaylistFiles().iterator();
				while ( itPlaylistFiles.hasNext()){
					PlaylistFile plf = (PlaylistFile)itPlaylistFiles.next();
					if (PlaylistFileManager.getPlaylistFile(plf.getId()) == null){
						itPlaylistFiles.remove();	
					}
				}
				if ( playlist.getPlaylistFiles().size() == 0){
					itPlaylists.remove();
				}
			}
		}
	
	/**
	 * Return Playlist by id
	 * @param sId
	 * @return
	 */
	public static Playlist getPlaylist(String sId) {
		return (Playlist) hmPlaylists.get(sId);
	}

	
}
