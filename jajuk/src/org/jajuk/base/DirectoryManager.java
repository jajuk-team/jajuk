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

import java.util.Iterator;
import java.util.Properties;

import org.jajuk.util.MD5Processor;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage directories
 * @Author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class DirectoryManager extends ItemManager implements Observer{
    /**Self instance*/
    private static DirectoryManager singleton;

	/**
	 * No constructor available, only static access
	 */
	private DirectoryManager() {
		super();
         //---register properties---
        //ID
        registerProperty(new PropertyMetaInformation(XML_ID,false,true,false,false,true,String.class,null,null));
        //Name test with (getParentDirectory() != null); //name editable only for standard directories, not root
        registerProperty(new PropertyMetaInformation(XML_NAME,false,true,true,false,true,String.class,null,null)); //edition to yet implemented TBI
        //Parent
        registerProperty(new PropertyMetaInformation(XML_DIRECTORY_PARENT,false,true,true,false,false,String.class,null,null));
        //Device
        registerProperty(new PropertyMetaInformation(XML_DEVICE,false,true,true,false,false,String.class,null,null));
        //Expand
        registerProperty(new PropertyMetaInformation(XML_EXPANDED,false,false,false,false,false,Boolean.class,null,false));
        //Synchonized directory
        registerProperty(new PropertyMetaInformation(XML_DIRECTORY_SYNCHRONIZED,false,false,true,false,false,Boolean.class,null,true));
        //Default cover
        registerProperty(new PropertyMetaInformation(XML_DIRECTORY_DEFAULT_COVER,false,false,true,false,false,String.class,null,null));
        //---Subscriptions---
        ObservationManager.register(EVENT_FILE_NAME_CHANGED,this);
	}
    
	/**
     * @return singleton
     */
    public static DirectoryManager getInstance(){
      if (singleton == null){
          singleton = new DirectoryManager();
      }
        return singleton;
    }
  
    
     /**
     * Change the item name
     * @param old old item
     * @param sNewName new item
     * @return new item
     */
    public synchronized Directory changeDirectoryName(Directory old,String sNewName){
        //check given name is different
        if (old.getName().equals(sNewName)){
            return old;
        }
        //try change dir on disk
        try{
            old.getFio().renameTo(new java.io.File(old.getParentDirectory().getFio().getAbsolutePath()+"/"+sNewName));
        }
        catch(Exception e){
            Log.error(e);
            return null;
        }
        //register new dir
        Directory newItem = registerDirectory(sNewName,old.getParentDirectory(),old.getDevice());
        //copy old properties except name and id
        newItem.setProperties(old.getProperties());
        newItem.setName(sNewName);
        newItem.setId(newItem.getId());
        //add dirs, files and playlist files to this new dir
        for (File file:old.getFiles()){
            newItem.addFile(file);
        }
        for (PlaylistFile plf:old.getPlaylistFiles()){
            newItem.addPlaylistFile(plf);
        }
        for (Directory dir:old.getDirectories()){
            newItem.addDirectory(dir);
        }
        //remove old dir from parent
        removeDirectory(old.getId());
        //Look for files under the old directory
        //for ()
        //add the new dir to the parent directory
        old.getParentDirectory().addDirectory(newItem);
        return newItem;
    }
    
	/**
	 * Register a directory
	 * 
	 * @param sName
	 */
	public synchronized Directory registerDirectory(String sName, Directory dParent, Device device) {
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
	public synchronized Directory registerDirectory(Device device) {
		String sId = device.getId();
		return registerDirectory(sId, "", null, device); //$NON-NLS-1$
	}

	/**
	 * Register a directory with a known id
	 * 
	 * @param sName
	 */
	public synchronized Directory registerDirectory(String sId, String sName, Directory dParent, Device device) {
		if (hmItems.containsKey(sId)) {
			return (Directory)hmItems.get(sId);
		}
		Directory directory = new Directory(sId, sName, dParent, device);
        if (dParent != null ){
            dParent.addDirectory(directory);//add the direcotry to parent collection
        }
		hmItems.put(sId,directory);
        restorePropertiesAfterRefresh(directory);
     	return directory;
	}

	/**
	 * Clean all references for the given device
	 * 
	 * @param sId :
	 *                   Device id
	 */
	public synchronized  void cleanDevice(String sId) {
		Iterator it = hmItems.keySet().iterator();
		while(it.hasNext()){
			Directory directory = (Directory)getItem((String)it.next());
			if (directory.getDevice().getId().equals(sId)) {
				it.remove();
			}
		}
	}

	/**
	 * Remove a directory and all subdirectories from main directory repository. 
     * Remove reference from parent directories as well.
	 * 
	 * @param sId
	 */
	public synchronized void removeDirectory(String sId) {
	    Directory dir = (Directory)getItem(sId);
	    //remove all files
	    for (File file:dir.getFiles()){
	        FileManager.getInstance().removeFile(file);
	    }
	    //remove all playlists
	    for (PlaylistFile plf:dir.getPlaylistFiles()){
	        PlaylistFileManager.getInstance().remove(plf.getId());
	    }
        //remove all sub dirs
        Iterator it = dir.getDirectories().iterator();
        while (it.hasNext()){
            Directory dSub = (Directory)it.next();
            removeDirectory(dSub.getId()); //self call
            //remove it 
            it.remove();
        }
        //remove this dir from collection
	    hmItems.remove(dir.getId());
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