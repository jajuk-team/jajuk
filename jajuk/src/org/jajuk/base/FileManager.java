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
 * USA. Revision 1.9  2003/11/13 18:56:55  bflorat
 * USA. 13/11/2003
 * USA.
 * USA. Revision 1.8  2003/11/07 23:57:45  bflorat
 * USA. 08/11/2003
 * USA.
 * USA. Revision 1.7  2003/11/03 06:08:05  bflorat
 * USA. 03/11/2003
 * USA. Revision 1.6 2003/10/31 13:05:06 bflorat 31/10/2003
 * 
 * Revision 1.5 2003/10/28 21:34:37 bflorat 28/10/2003
 * 
 * Revision 1.4 2003/10/26 21:28:49 bflorat 26/10/2003
 * 
 * Revision 1.3 2003/10/23 22:07:40 bflorat 23/10/2003
 * 
 * Revision 1.2 2003/10/21 20:37:54 bflorat 21/10/2003
 * 
 * Revision 1.1 2003/10/21 17:51:43 bflorat 21/10/2003
 *  
 */

package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage files
 * 
 * @Author bflorat @created 17 oct. 2003
 */
public class FileManager {
	/** Files collection* */
	static HashMap hmFiles = new HashMap(100);

	/**
	 * No constructor available, only static access
	 */
	private FileManager() {
		super();
	}

	/**
	 * Register an File
	 * 
	 * @param sName
	 */
	public static synchronized File registerFile(String sName, Directory directory, Track track, long lSize, String sQuality) {
		String sId = MD5Processor.hash(directory.getDevice().getUrl() + directory.getAbsolutePath() + sName);
		return registerFile(sId, sName, directory, track, lSize, sQuality);
	}

	/**
	 * Register an File with a known id
	 * 
	 * @param sName
	 */
	public static File registerFile(String sId, String sName, Directory directory, Track track, long lSize, String sQuality) {
		File file = new File(sId, sName, directory, track, lSize, sQuality);
		if ( !hmFiles.containsKey(sId)){
			hmFiles.put(sId, file);
			if ( Device.isRefreshing()){
				Log.debug("registrated new file: "+ file);
			}
		}
		return file;
	}

	/**
	 * Clean all references for the given device
	 * @param sId : Device id
	 */
	public static synchronized void cleanDevice(String sId) {
		//we have to create a new list because we can't iterate on a moving size list
		Iterator it = hmFiles.values().iterator();
		while (it.hasNext()) {
			File file = (File) it.next();
			if (file.getDirectory()==null || file.getDirectory().getDevice().getId().equals(sId)) {
				it.remove();
			}
		}
		System.gc(); //force garbage collection after cleanup
	}

	/** Return all registred files */
	public static synchronized ArrayList getFiles() {
		return new ArrayList(hmFiles.values());
	} 
	
	/**
	   * Return file by id
	   * 
	   * @param id
	   * @return
	   */
	public static synchronized File getFile(String sId) {
		return (File) hmFiles.get(sId);
	}

}
