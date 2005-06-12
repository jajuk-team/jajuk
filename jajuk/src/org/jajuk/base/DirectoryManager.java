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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.jajuk.util.MD5Processor;
import org.jajuk.util.SequentialMap;

/**
 * Convenient class to manage directories
 * @Author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class DirectoryManager {
	/** Directories collection stored in a arraylist to conserve creation order when parsing at startup* */
	static ArrayList alDirectories = new ArrayList(100);
	/** Directories collection ID */
	static ArrayList alIds = new ArrayList(100);
	/** Map ids and properties, survives to a refresh, is used to recover old properties after refresh */
	static HashMap hmIdProperties = new HashMap(100);

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
		StringBuffer sbAbs = new StringBuffer(device.getUrl());
		if (dParent != null) {
			sbAbs.append(dParent.getRelativePath());
		}
		sbAbs.append(File.separatorChar).append(sName);
		String sId = MD5Processor.hash(sbAbs.insert(0,device.getName()).toString());
		return registerDirectory(sId, sName, dParent, device);
	}

	/**
	 * Register a root device directory
	 * 
	 * @param device
	 */
	public static synchronized Directory registerDirectory(Device device) {
		String sId = device.getId();
		return registerDirectory(sId, "", null, device); //$NON-NLS-1$
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
        SequentialMap properties = (SequentialMap)hmIdProperties.get(sId); 
		if ( properties  == null){  //new file
			hmIdProperties.put(sId,directory.getProperties());
		}
		else{
			directory.setProperties(properties);
		}
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
	
	/**
	 * Return properties assiated to an id
	 * @param sId the id
	 * @return
	 */
	public static synchronized Properties getProperties(String sId){
		return (Properties)hmIdProperties.get(sId);
	}

}
