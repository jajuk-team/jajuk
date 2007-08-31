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

import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Convenient class to manage directories
 */
public class DirectoryManager extends ItemManager {
	/** Self instance */
	private static DirectoryManager singleton;

	/**
	 * No constructor available, only static access
	 */
	private DirectoryManager() {
		super();
		// ---register properties---
		// ID
		registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
				String.class, null));
		// Name test with (getParentDirectory() != null); //name editable only
		// for standard
		// directories, not root
		registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, false, false,
				String.class, null)); // edition to
		// yet
		// implemented
		// TBI
		// Parent
		registerProperty(new PropertyMetaInformation(XML_DIRECTORY_PARENT, false, true, true,
				false, false, String.class, null));
		// Device
		registerProperty(new PropertyMetaInformation(XML_DEVICE, false, true, true, false, false,
				String.class, null));
		// Expand
		registerProperty(new PropertyMetaInformation(XML_EXPANDED, false, false, false, false,
				true, Boolean.class, false));
		// Synchonized directory
		registerProperty(new PropertyMetaInformation(XML_DIRECTORY_SYNCHRONIZED, false, false,
				true, false, false, Boolean.class, true));
		// Default cover
		registerProperty(new PropertyMetaInformation(XML_DIRECTORY_DEFAULT_COVER, false, false,
				true, false, false, String.class, null));
	}

	/**
	 * @return singleton
	 */
	public static DirectoryManager getInstance() {
		if (singleton == null) {
			singleton = new DirectoryManager();
		}
		return singleton;
	}

	/**
	 * Register a directory
	 * 
	 * @param sName
	 */
	public Directory registerDirectory(String sName, Directory dParent, Device device) {
		synchronized (DirectoryManager.getInstance().getLock()) {
			return registerDirectory(createID(sName, device, dParent), sName, dParent, device);
		}
	}

	/**
	 * Register a root device directory
	 * 
	 * @param device
	 */
	public Directory registerDirectory(Device device) {
		return registerDirectory(device.getId(), "", null, device);
	}

	/**
	 * Return hashcode for this item
	 * 
	 * @param sName
	 *            directory name
	 * @param device
	 *            device
	 * @param dParent
	 *            parent directory
	 * @return ItemManager ID
	 */
	protected static String createID(String sName, Device device, Directory dParent) {
		StringBuffer sbAbs = new StringBuffer(device.getName());
		// Under windows, all files/directories with different cases should get
		// the same ID
		if (Util.isUnderWindows()) {
			if (dParent != null) {
				sbAbs.append(dParent.getRelativePath().toLowerCase());
			}
			sbAbs.append(sName.toLowerCase());
		} else {
			if (dParent != null) {
				sbAbs.append(dParent.getRelativePath());
			}
			sbAbs.append(sName);
		}
		String sId = MD5Processor.hash(sbAbs.toString());
		return sId;
	}

	/**
	 * Register a directory with a known id
	 * 
	 * @param sName
	 */
	public Directory registerDirectory(String sId, String sName, Directory dParent, Device device) {
		synchronized (DirectoryManager.getInstance().getLock()) {
			if (hmItems.containsKey(sId)) {
				Directory dir = (Directory) hmItems.get(sId);
				// Set name again because under Windows, dir name case could
				// have changed but
				// we keep the same directory object
				dir.setName(sName);
				return dir;
			}
			Directory directory = null;
			directory = new Directory(sId, sName, dParent, device);
			if (dParent != null) {
				// add the direcotry to parent
				dParent.addDirectory(directory);
			}
			hmItems.put(sId, directory);
			return directory;
		}
	}

	/**
	 * Clean all references for the given device
	 * 
	 * @param sId :
	 *            Device id
	 */
	public void cleanDevice(String sId) {
		synchronized (DirectoryManager.getInstance().getLock()) {
			Iterator it = hmItems.keySet().iterator();
			while (it.hasNext()) {
				Directory directory = getDirectoryByID((String) it.next());
				if (directory.getDevice().getId().equals(sId)) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Remove a directory and all subdirectories from main directory repository.
	 * Remove reference from parent directories as well.
	 * 
	 * @param sId
	 */
	public void removeDirectory(String sId) {
		synchronized (DirectoryManager.getInstance().getLock()) {
			Directory dir = getDirectoryByID(sId);
			if (dir == null) {// check the directory has not already been
				// removed
				return;
			}
			synchronized (FileManager.getInstance().getLock()) {
				// remove all files
				// need to use a shallow copy to avoid concurent exceptions
				ArrayList<File> alFiles = new ArrayList<File>(dir.getFiles());
				for (File file : alFiles) {
					FileManager.getInstance().removeFile(file);
				}
			}
			synchronized (PlaylistFileManager.getInstance().getLock()) {
				// remove all playlists
				for (PlaylistFile plf : dir.getPlaylistFiles()) {
					PlaylistFileManager.getInstance().removeItem(plf.getId());
				}
			}
			// remove all sub dirs
			Iterator it = dir.getDirectories().iterator();
			while (it.hasNext()) {
				Directory dSub = (Directory) it.next();
				removeDirectory(dSub.getId()); // self call
				// remove it
				it.remove();
			}
			// remove this dir from collection
			hmItems.remove(dir.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ItemManager#getIdentifier()
	 */
	public String getLabel() {
		return XML_DIRECTORIES;
	}

	public Directory getDirectoryForIO(java.io.File fio) {
		synchronized (DirectoryManager.getInstance().getLock()) {
			Iterator it = hmItems.values().iterator();
			while (it.hasNext()) {
				Directory dir = (Directory) it.next();
				if (dir.getFio().equals(fio)) {
					return dir;
				}
			}
			return null;
		}
	}

	/**
	 * @param sID
	 *            Item ID
	 * @return Element
	 */
	public Directory getDirectoryByID(String sID) {
		synchronized (getLock()) {
			return (Directory) hmItems.get(sID);
		}
	}

	/**
	 * 
	 * @return directories list
	 */
	public Set<Directory> getDirectories() {
		Set<Directory> directorySet = new LinkedHashSet<Directory>();
		synchronized (getLock()) {
			for (Item item : getItems()) {
				directorySet.add((Directory) item);
			}
		}
		return directorySet;
	}
}