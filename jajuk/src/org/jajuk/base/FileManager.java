/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,USA
 * $Revision$
 **/

package org.jajuk.base;

import java.util.ArrayList;
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
	static ArrayList alFilesId = new ArrayList(1000);
	/** Files collection* */
	static ArrayList alFiles = new ArrayList(1000);
	
	
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
		if ( !alFilesId.contains(sId)){
			alFilesId.add(sId);
			alFiles.add(file);
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
		Iterator it = alFiles.iterator();
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
		return new ArrayList(alFiles);
	} 
	
	/**
	   * Return file by id
	   * 
	   * @param id
	   * @return
	   */
	public static synchronized File getFile(String sId) {
		return (File) alFiles.get(alFilesId.indexOf(sId));
	}
	
	/**
	 * Return a shuffle mounted file from the entire collection
	 * @return
	 */
	public static synchronized File getShuffleFile(){
		//create a tempory table to remove unmounted files
		ArrayList alEligibleFiles = new ArrayList(1000);
		Iterator it = alFiles.iterator();
		while ( it.hasNext()){
			File file = (File)it.next();
			if (file.getDirectory().getDevice().isMounted()){
				alEligibleFiles.add(file);
			}
		}
		if (alEligibleFiles.size() ==0 ){
			return null;
		}
		return (File)alEligibleFiles.get((int)(Math.random()*alEligibleFiles.size()));
	}
	
	/** Return next mounted file ( used in continue mode )
	 * @param file : a file
	 * @return next file from entire collection
	 */
	public static synchronized File getNextFile(File file){
		File fileNext = null;
		do{
			fileNext = (File)alFiles.get(alFiles.indexOf(file)+1);
		}
		while ( fileNext != null && !fileNext.getDirectory().getDevice().isMounted());
		return fileNext;
	}

}
