/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,
 * USA. $Log$
 * USA. Revision 1.6  2003/11/03 06:08:05  bflorat
 * USA. 03/11/2003
 * USA. Revision 1.5 2003/10/31 13:05:06 bflorat 31/10/2003
 * 
 * Revision 1.4 2003/10/28 21:34:37 bflorat 28/10/2003
 * 
 * Revision 1.3 2003/10/26 21:28:49 bflorat 26/10/2003
 * 
 * Revision 1.2 2003/10/23 22:07:40 bflorat 23/10/2003
 * 
 * Revision 1.1 2003/10/21 17:51:43 bflorat 21/10/2003
 *  
 */

package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 * Convenient class to manage playlists files
 * 
 * @Author bflorat @created 17 oct. 2003
 */
public class PlaylistFileManager {
	/** PlaylistFiles collection* */
	static HashMap hmPlaylistFiles = new HashMap(100);

	/**
	 * No constructor available, only static access
	 */
	private PlaylistFileManager() {
		super();
	}

	/**
	 * Register an PlaylistFile
	 * 
	 * @param sName
	 */
	public static synchronized PlaylistFile registerPlaylistFile(String sName, String sHashcode, Directory dParentDirectory) {
		String sId = MD5Processor.hash(dParentDirectory.getAbsolutePath() + sName);
		return registerPlaylistFile(sId, sName, sHashcode, dParentDirectory);
	}

	/**
	 * Register an PlaylistFile with a known id
	 * 
	 * @param sName
	 */
	public static synchronized PlaylistFile registerPlaylistFile(String sId, String sName, String sHashcode, Directory dParentDirectory) {
		PlaylistFile playlistFile = new PlaylistFile(sId, sName, sHashcode, dParentDirectory);
		hmPlaylistFiles.put(sId, playlistFile);
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
			if ( plf.getParentDirectory()== null || plf.getParentDirectory().getDevice().getId().equals(sId)) {
				it.remove();
			}
		}
	}

	/** Return all registred PlaylistFiles */
	public static synchronized ArrayList getPlaylistFiles() {
		return new ArrayList(hmPlaylistFiles.values());
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

}
