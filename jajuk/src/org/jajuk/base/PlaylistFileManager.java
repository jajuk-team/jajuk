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

import org.jajuk.util.log.Log;

/**
 * Convenient class to manage playlists files
 * @Author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class PlaylistFileManager extends ItemManager implements Observer{
	/**Self instance*/
    private static PlaylistFileManager singleton;
	
	/**
	 * No constructor available, only static access
	 */
	private PlaylistFileManager() {
		super();
          //---register properties---
        //ID
        registerProperty(new PropertyMetaInformation(XML_ID,false,true,false,false,String.class));
        //Name
        registerProperty(new PropertyMetaInformation(XML_NAME,false,true,true,true,String.class));
        //Hashcode
        registerProperty(new PropertyMetaInformation(XML_HASHCODE,false,true,false,false,String.class));
        //Directory
        registerProperty(new PropertyMetaInformation(XML_DIRECTORY,false,true,true,false,String.class));
   }

    /**
     * @return singleton
     */
    public static PlaylistFileManager getInstance(){
      if (singleton == null){
          singleton = new PlaylistFileManager();
      }
        return singleton;
    }
    
    
	/**
	 * Register an PlaylistFile with a known id
	 * 
	 * @param sName
	 */
	public synchronized PlaylistFile registerPlaylistFile(String sId, String sName, String sHashcode, Directory dParentDirectory) {
		if ( !hmItems.containsKey(sId)){
			PlaylistFile playlistFile = new PlaylistFile(sId, sName, sHashcode, dParentDirectory);
			hmItems.put(sId, playlistFile);
			if ( dParentDirectory.getDevice().isRefreshing()){
				Log.debug("Registered new playlist file: "+ playlistFile); //$NON-NLS-1$
			}
            restorePropertiesAfterRefresh(playlistFile);
       }
       return (PlaylistFile)hmItems.get(sId);
	}

	/**
	 * Clean all references for the given device
	 * 
	 * @param sId :
	 *                   Device id
	 */
	public synchronized  void cleanDevice(String sId) {
		Iterator it = hmItems.values().iterator();
		while (it.hasNext()) {
			PlaylistFile plf = (PlaylistFile) it.next();
			if ( plf.getDirectory()== null 
                    || plf.getDirectory().getDevice().getId().equals(sId)) {
				it.remove();
			}
		}
	}
 
 /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_PLAYLIST_FILES;
    }
    
 /* (non-Javadoc)
     * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if (EVENT_FILE_NAME_CHANGED.equals(subject)){
            Properties properties = event.getDetails();
            File fNew = (File)properties.get(DETAIL_NEW);
            File fileOld = (File)properties.get(DETAIL_OLD);
            //search references in playlists
            Iterator it = getItems().iterator();
            for (int i=0; it.hasNext(); i++){
                PlaylistFile plf = (PlaylistFile)it.next();
                if (plf.isReady()){ //check only in mounted playlists, note that we can't change unmounted playlists
                    try{
                        if (plf.getFiles().contains(fileOld)){
                            plf.replaceFile(fileOld,fNew);
                        }
                    }
                    catch(Exception e){
                        Log.error("017",e);
                    }
                }
            }
            //refresh UI
            ObservationManager.notify(new Event(EVENT_PLAYLIST_REFRESH));
        }
    }
}
