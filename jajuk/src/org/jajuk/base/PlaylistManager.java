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
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

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
            if (!playlist.getPlaylistFiles().contains(plFile)){
                playlist.addFile(plFile);
            }
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
    
     public void removePlaylistFile(PlaylistFile plf) {
         Playlist pl = getPlayList(plf);
         if (pl == null){
             return;
         }
         pl.removePlaylistFile(plf);
         plf.getDirectory().removePlaylistFile(plf);
         if (pl.getPlaylistFiles().size() == 0){
             removeItem(pl.getId());
         }
     }
     
     public void removePlaylist(Playlist pl){
        if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE)){  //file delete confirmation
            String sFileToDelete = ""; //$NON-NLS-1$
            String sMessage = Messages.getString("Confirmation_delete"); //$NON-NLS-1$
            for (PlaylistFile plf:pl.getPlaylistFiles()){
              sFileToDelete = plf.getDirectory().getFio().getAbsoluteFile().toString()+java.io.File.separatorChar +pl.getName(); //$NON-NLS-1$
              sMessage += "\n"+sFileToDelete; //$NON-NLS-1$ //$NON-NLS-2$ 
            }
            int i = Messages.getChoice(sMessage,JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
            if ( i == JOptionPane.OK_OPTION){
                boolean bUnmountedItems = false;
                //take a shallow copy of the array to avoid concurrency exception
                ArrayList<PlaylistFile> alFiles = (ArrayList)pl.getPlaylistFiles().clone();
                for (int j=0;j<alFiles.size();j++){
                    PlaylistFile plf = alFiles.get(j);
                    java.io.File fileToDelete = plf.getFio();
                    if (fileToDelete.exists()){
                        fileToDelete.delete();
                        //check that file has been really deleted (sometimes, we get no exception)
                        if (fileToDelete.exists()){
                            Log.error("131",new JajukException("131")); //$NON-NLS-1$//$NON-NLS-2$
                            Messages.showErrorMessage("131"); //$NON-NLS-1$
                            continue;
                        }
                        PlaylistFileManager.getInstance().removeItem(plf.getId());
                        removePlaylistFile(plf);
                    }
                    else{
                        bUnmountedItems = true;
                    }
                }
                if (pl.getPlaylistFiles().size() == 0){
                    removeItem(pl.getId());
                }
                if (bUnmountedItems){
                    Messages.showErrorMessage("138"); //$NON-NLS-1$
                }
            }
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
     * Return playlist associated with a given playlist file
     * @param plfi
     * @return the playlist or null if none associated playlist
     */
    public synchronized Playlist getPlaylist(PlaylistFile plf){
       Iterator it = PlaylistManager.getInstance().getItems().iterator();
       while (it.hasNext()){
           Playlist pl = (Playlist)it.next();
           if (pl.getPlaylistFiles().contains(plf)){
               return pl;
           }
       }
       return null;
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
