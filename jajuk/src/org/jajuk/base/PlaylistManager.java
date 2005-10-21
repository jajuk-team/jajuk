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

/**
 *  Convenient class to manage playlists
 * @Author    Bertrand Florat
 * @created    17 oct. 2003
 */
public class PlaylistManager extends ItemManager{
    /**Self instance*/
    private static PlaylistManager singleton;

	/**
	 * No constructor available, only static access
	 */
	private PlaylistManager() {
		super();
        //---register properties---
        //ID
        registerProperty(new PropertyMetaInformation(XML_ID,false,true,false,false,false,String.class,null,null));
        //Playlist file
        registerProperty(new PropertyMetaInformation(XML_PLAYLIST_FILES,false,true,true,false,false,String.class,null,null));
   }

    /**
     * @return singleton
     */
    public static PlaylistManager getInstance(){
      if (singleton == null){
          singleton = new PlaylistManager();
      }
        return singleton;
    }
    
    
	/**
	 * Register an Playlist
	 *@param file : playlist file
	 */	
	public synchronized Playlist registerPlaylist(PlaylistFile plFile) {
		return registerPlaylist(plFile.getHashcode(),plFile);
	}
	
	/**
	 * Register an Playlist with a known id
	 *@param file : playlist file
	 */	
	public  synchronized Playlist registerPlaylist(String sId,PlaylistFile plFile) {
		if (hmItems.containsKey(sId)){ //playlist already exist, add a file
			Playlist playlist = (Playlist)hmItems.get(sId);
			playlist.addFile(plFile);
			return playlist;
		}
		else { //new playlist
            Playlist playlist = null;
            if (hmIdSaveItems.containsKey(sId)){
                playlist = (Playlist)hmIdSaveItems.get(sId);
            }
            else{
                playlist = new Playlist(sId,plFile);
                saveItem(playlist);
            }
			playlist.removeProperty(XML_NAME);//no name attribute for playlists
            hmItems.put(sId,playlist);
            return playlist;
		}
	}

	/**
		 * Perform a playlist cleanup : delete useless items
		 *  
		 */
		public synchronized void cleanup() {
			Iterator itPlaylists = hmItems.values().iterator();
			while (itPlaylists.hasNext()) {
				Playlist playlist= (Playlist)itPlaylists.next();
				Iterator itPlaylistFiles = playlist.getPlaylistFiles().iterator();
				while ( itPlaylistFiles.hasNext()){
					PlaylistFile plf = (PlaylistFile)itPlaylistFiles.next();
					if (PlaylistFileManager.getInstance().getItem(plf.getId()) == null){
						itPlaylistFiles.remove();	
					}
				}
				if ( playlist.getPlaylistFiles().size() == 0){
					itPlaylists.remove();
				}
			}
		}
        
        /**
         * 
         * @param plf
         * @return pl   ylist for a given playlist file
         */
		public Playlist getPlayList(PlaylistFile plf){
		    Iterator it = hmItems.values().iterator();
            while (it.hasNext()){
                Playlist pl = (Playlist)it.next();
                if (pl.getPlaylistFiles().contains(plf)){
                    return pl;
                }
            }
            return null;
        }
	
/* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_PLAYLISTS;
    }
  
	
}
