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
import java.util.Properties;

import org.jajuk.util.MD5Processor;
import org.jajuk.util.SequentialMap;

/**
 * Convenient class to manage directories
 * @Author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class DirectoryManager extends ItemManager implements Observer{
	/** Id->Directories, note  that we have to conserve creation order when parsing at startup*/
	static SequentialMap smIdDirectories = new SequentialMap();
	/** Map ids and properties, survives to a refresh, is used to recover old properties after refresh */
	static HashMap hmIdProperties = new HashMap(100);
    /**Self instance*/
    static DirectoryManager singleton;

	/**
	 * No constructor available, only static access
	 */
	private DirectoryManager() {
		super();
        //      subscriptions
        ObservationManager.register(EVENT_FILE_NAME_CHANGED,this);
	}
    
	/**
     * @return singleton
     */
    public static ItemManager getInstance(){
      if (singleton == null){
          singleton = new DirectoryManager();
      }
        return singleton;
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
		sbAbs.append(java.io.File.separatorChar).append(sName);
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
		if (smIdDirectories.containsKey(sId)) {
			return (Directory)smIdDirectories.get(sId);
		}
		smIdDirectories.put(sId,directory);
        //now reapply stored properties before refresh
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
		Iterator it = smIdDirectories.keys().iterator();
		while(it.hasNext()){
			Directory directory = getDirectory((String)it.next());
			if (directory.getDevice().getId().equals(sId)) {
				it.remove();
			}
		}
	}

	/**
	 * Remove a directory and all subdirectories from main directory repository. Remove reference from parent directories as well.
	 * 
	 * @param sId
	 */
	public static synchronized void removeDirectory(String sId) {
		Directory dToBeRemoved = getDirectory(sId);
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
		smIdDirectories.remove(sId);
     }

	/** Return all registred directories */
	public static synchronized ArrayList getDirectories() {
		return (ArrayList)smIdDirectories.values();
	}

	/**
	 * Return directory by id
	 * 
	 * @param sName
	 * @return
	 */
	public static synchronized Directory getDirectory(String sId) {
		return (Directory) smIdDirectories.get(sId);
	}
	
	/**
	 * Return properties assiated to an id
	 * @param sId the id
	 * @return
	 */
	public static synchronized Properties getProperties(String sId){
		return (Properties)hmIdProperties.get(sId);
	}
    
 /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_DIRECTORIES;
    }
    
  /* (non-Javadoc)
     * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if (EVENT_FILE_NAME_CHANGED.equals(subject)){
            Properties properties = event.getDetails();
            File fNew  = (File)properties.get(DETAIL_NEW);
            File fileOld = (File)properties.get(DETAIL_OLD);
            Directory dir = fileOld.getDirectory();
            // change directory references
            dir.changeFile(fileOld,fNew);
        }
    }
}