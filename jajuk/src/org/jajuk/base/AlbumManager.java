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
 *
 */

package org.jajuk.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 *  Convenient class to manage Albums
 * @Author    Bertrand Florat
 * @created    17 oct. 2003
 */
public class AlbumManager extends ItemManager{
	/**Albums collection**/
	private static HashMap hmAlbums = new HashMap(100);
	    
    /**Self instance*/
    private static AlbumManager singleton;

	/**
	 * No constructor available, only static access
	 */
	private AlbumManager() {
    }
    
  /**
     * @return singleton
     */
    public static ItemManager getInstance(){
      if (singleton == null){
          singleton = new AlbumManager();
      }
        return singleton;
    }
    

	/**
	 * Register an Album
	 *@param sName
	 */
	public static synchronized  Album registerAlbum(String sName) {
		String sId = MD5Processor.hash(sName.trim().toLowerCase());
		return registerAlbum(sId,sName);
	}
	
	/**
	 * Perform an album cleanup : delete useless items
	 *  
	 */
	public static synchronized void cleanup() {
		Iterator itAlbums = hmAlbums.values().iterator();
		while (itAlbums.hasNext()) {
			Album album = (Album) itAlbums.next();
			if ( album.getTracks().size() == 0){
				itAlbums.remove();
			}
		}
	}
	
	/**
	 * Perform an album cleanup for a given album
	 *  
	 */
	public static synchronized void cleanup(Album album) {
		if ( album.getTracks().size() == 0){
			hmAlbums.remove(album.getId());
		}
	}
	
    /**
     * Chnage the item
     * @param old
     * @param sNewName
     * @return new album
     */
    public static synchronized Album changeAlbumName(Album old,String sNewName){
        Album newItem = registerAlbum(sNewName);
        Iterator it = TrackManager.getTracks().iterator();
        while (it.hasNext()){
            Track track = (Track)it.next();
            if (track.getAlbum().equals(old)){
                TrackManager.changeTrackAlbum(track,newItem.getId());
            }
        }
        cleanup();//remove useless albums if no more tracks use it
        return newItem;
    }
    
	/**
	 * Register an Album with a known id
	 * 
	 * @param sName
	 */
	public static synchronized Album registerAlbum(String sId, String sName) {
		if (hmAlbums.containsKey(sId)) {
			return (Album)hmAlbums.get(sId);
		}
		Album album = new Album(sId, sName);
		hmAlbums.put(sId, album);
		//try to recover some properties previous a refresh
		getInstance().restorePropertiesAfterRefresh(album,sId);
		//apply default custom properties
		getInstance().applyNewProperties();
		return album;
	}


	/**Return all registred Albums*/
	public static synchronized Collection getAlbums() {
		return hmAlbums.values();
	}

	/**
	 * Return Album by id
	 * @param id
	 * @return
	 */
	public static synchronized Album getAlbum(String sId) {
		return (Album) hmAlbums.get(sId);
	}
	
	/**
		 * Format the album name to be normalized : 
		 * <p>-no underscores or other non-ascii characters
		 * <p>-no spaces at the begin and the end
		 * <p>-All in lower cas expect first letter of first word
		 * <p> exemple: "My album title" 
		 *  @param sName
		 * @return
		 */
		private static synchronized String format(String sName){
			String sOut;
			sOut = sName.trim(); //supress spaces at the begin and the end
			sOut.replace('-',' ');  //move - to space
			sOut.replace('_',' '); //move _ to space
			char c = sOut.charAt(0);
			sOut = sOut.toLowerCase();
			StringBuffer sb = new StringBuffer(sOut);
			sb.setCharAt(0,Character.toUpperCase(c));
			return sb.toString();
		}

    /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_ALBUMS;
    }
   
}
