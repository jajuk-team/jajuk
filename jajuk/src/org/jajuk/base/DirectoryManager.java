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
 * USA. Revision 1.5  2003/11/03 06:08:05  bflorat
 * USA. 03/11/2003
 * USA. Revision 1.4 2003/10/31 13:05:06 bflorat 31/10/2003
 * 
 * Revision 1.3 2003/10/26 21:28:49 bflorat 26/10/2003
 * 
 * Revision 1.2 2003/10/23 22:07:40 bflorat 23/10/2003
 * 
 * Revision 1.1 2003/10/21 17:51:43 bflorat 21/10/2003
 *  
 */

package org.jajuk.base;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 * Convenient class to manage directories
 * 
 * @Author bflorat @created 17 oct. 2003
 */
public class DirectoryManager {
	/** Directories collection stored in a arraylist to conserve creation order* */
	static ArrayList alDirectories = new ArrayList(100);
	/** Directories collection ID */
	static ArrayList alIds = new ArrayList(100);

	/**
	 * No constructor available, only static access
	 */
	private DirectoryManager() {
		super();
	}

	/**
	 * Register a directory
	 * 
	 * @param sName
	 */
	public static synchronized Directory registerDirectory(String sName, Directory dParent, Device device) {
		String sAbs = device.getUrl();
		if (dParent != null) {
			sAbs += dParent.getAbsolutePath();
		}
		sAbs += File.separatorChar + sName;
		String sId = MD5Processor.hash(sAbs);
		return registerDirectory(sId, sName, dParent, device);
	}

	/**
	 * Register a root device directory
	 * 
	 * @param device
	 */
	public static synchronized Directory registerDirectory(Device device) {
		String sId = device.getId();
		return registerDirectory(sId, "", null, device);
	}

	/**
	 * Register a directory with a known id
	 * 
	 * @param sName
	 */
	public static synchronized Directory registerDirectory(String sId, String sName, Directory dParent, Device device) {
		Directory directory = new Directory(sId, sName, dParent, device);
		if (alIds.contains(sId)) {
			return directory;
		}
		alDirectories.add(directory);
		alIds.add(sId);
		return directory;
	}

	/**
	 * Clean all references for the given device
	 * 
	 * @param sId :
	 *                   Device id
	 */
	public static synchronized  void cleanDevice(String sId) {
		//we have to create a new list because we can't iterate on a moving size list
		Iterator it = alDirectories.iterator();
		while(it.hasNext()){
			Directory directory = (Directory)it.next();
			if (directory.getDevice().getId().equals(sId)) {
				it.remove();
				alIds.remove(directory.getId());
			}
		}
	}

	/**
	 * Remove a directory and all subdirectories from main directory repository. Remove reference from parent directories as well.
	 * 
	 * @param sId
	 */
	public static synchronized void removeDirectory(String sId) {
		int index = alDirectories.indexOf(sId);
		Directory dToBeRemoved = (Directory) alDirectories.get(index);
		ArrayList alDirsToBeRemoved = dToBeRemoved.getDirectories(); //list of sub directories to remove
		Iterator it = alDirsToBeRemoved.iterator();
		while (it.hasNext()) {
			Directory dCurrent = (Directory) it.next();
			removeDirectory(dCurrent.getId()); //self call
		}
		Directory dParent = dToBeRemoved.getParentDirectory(); //now del references from parent dir
		if (dParent != null) {
			dParent.removeDirectory(dToBeRemoved);
		}
		alDirectories.remove(index); //delete the dir itself
		alIds.remove(index);
	}

	/** Return all registred directories */
	public static synchronized ArrayList getDirectories() {
		return alDirectories;
	}

	/**
	 * Return directory by id
	 * 
	 * @param sName
	 * @return
	 */
	public static synchronized Directory getDirectory(String sId) {
		if (!alIds.contains(sId)){
			return null;
		}
		return (Directory) alDirectories.get(alIds.indexOf(sId));
	}

}
