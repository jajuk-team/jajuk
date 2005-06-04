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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.jajuk.util.log.Log;

/**
 * Convenient class to manage playlists files
 * @Author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class PlaylistFileManager {
	/** PlaylistFiles collection* */
	static HashMap hmPlaylistFiles = new HashMap(100);
	/** Map ids and properties, survives to a refresh, is used to recover old properties after refresh */
	static HashMap hmIdProperties = new HashMap(100);
	
	/**
	 * No constructor available, only static access
	 */
	private PlaylistFileManager() {
		super();
	}

	/**
	 * Register an PlaylistFile with a known id
	 * 
	 * @param sName
	 */
	public static synchronized PlaylistFile registerPlaylistFile(String sId, String sName, String sHashcode, Directory dParentDirectory) {
		PlaylistFile playlistFile = new PlaylistFile(sId, sName, sHashcode, dParentDirectory);
		if ( !hmPlaylistFiles.containsKey(sId)){
			hmPlaylistFiles.put(sId, playlistFile);
			if ( dParentDirectory.getDevice().isRefreshing()){
				Log.debug("Registered new playlist file: "+ playlistFile); //$NON-NLS-1$
			}
			Properties properties = (Properties)hmIdProperties.get(sId); 
			if ( properties  == null){  //new file
				hmIdProperties.put(sId,playlistFile.getProperties());
			}
			else{
				playlistFile.setProperties(properties);
			}
		}
		return playlistFile;
	}

	/**
	 * Clean all references for the given device
	 * 
	 * @param sId :
	 *                   Device id
	 */
	public static synchronized  void cleanDevice(String sId) {
		Iterator it = hmPlaylistFiles.values().iterator();
		while (it.hasNext()) {
			PlaylistFile plf = (PlaylistFile) it.next();
			if ( plf.getDirectory()== null 
                    || plf.getDirectory().getDevice().getId().equals(sId)) {
				it.remove();
			}
		}
	}

	/** Return all registred PlaylistFiles */
	public static synchronized ArrayList getPlaylistFiles() {
		ArrayList alOut = new ArrayList(hmPlaylistFiles.values());
        Collections.sort(alOut);
        return alOut;
	}

	/**
	 * Return PlaylistFile by id
	 * 
	 * @param sId
	 * @return
	 */
	public static synchronized PlaylistFile getPlaylistFile(String sId) {
		return (PlaylistFile) hmPlaylistFiles.get(sId);
	}
	
	/**
	 * Delete a playlist file from collection
	 * @param sId
	 */
	public static synchronized void delete(String sId){
		hmPlaylistFiles.remove(sId);
	}
	
	/**
	 * Return properties assiated to an id
	 * @param sId the id
	 * @return
	 */
	public static synchronized Properties getProperties(String sId){
		return (Properties)hmIdProperties.get(sId);
	}

}
