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

import org.jajuk.i18n.Messages;

/**
 *  A playlist
 *<p> Logical item
 * @Playlist     Bertrand Florat
 * @created    17 oct. 2003
 */
public class Playlist extends Item implements Comparable{

	/**Associated playlist files**/
	private ArrayList<PlaylistFile> alPlaylistFiles = new ArrayList(2);
  	
	/**
	 * Playlist constructor
	 * @param sId
	 * @param file :an associated playlist file
	 */
	public Playlist(String sId,PlaylistFile plFile){
        super(sId,null);
        this.alPlaylistFiles.add(plFile);
        setProperty(XML_PLAYLIST_FILES,plFile.getId());
   }

/* (non-Javadoc)
     * @see org.jajuk.base.Item#getIdentifier()
     */
    final public String getIdentifier() {
        return XML_PLAYLIST;
    }
    
	/**
	 * toString method
	 */
	public String toString() {
		StringBuffer sbOut = new StringBuffer("Playlist[ID="+sId+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0;i<alPlaylistFiles.size();i++){
			sbOut.append('\n').append(alPlaylistFiles.get(i).toString());
		}
		return sbOut.toString();
	}


	/**
	 * Equal method to check two playlists are identical
	 * @param otherPlaylist
	 * @return
	 */
	public boolean equals(Object otherPlaylist){
		if (otherPlaylist == null){
            return false;
        }
        return this.getId().equals(((Playlist)otherPlaylist).getId());
	}	

	
	/**
	 * hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
	}


	/**
	 *  Add a playlist file 
	 * @return
	 */
	public ArrayList<PlaylistFile> getPlaylistFiles() {
		return alPlaylistFiles;
	}
	
    /* (non-Javadoc)
     * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
     */
	public String getHumanValue(String sKey){
	    if (XML_PLAYLIST_FILES.equals(sKey)){
	        StringBuffer sbOut = new StringBuffer();
	        Iterator it = alPlaylistFiles.iterator();
	        while (it.hasNext()){
	            PlaylistFile plf = (PlaylistFile)it.next();
	            sbOut.append(plf.getAbsolutePath()+","); //$NON-NLS-1$
	        }
	        return sbOut.substring(0,sbOut.length()-1); //remove last ','
	    }
	    else{//default
	        return super.getHumanValue(sKey);
	    }
	}
	
	/**
	 * @return an available playlist file to play
	 */
	public PlaylistFile getPlayeablePlaylistFile() {
		PlaylistFile plfOut = null;
		Iterator it = alPlaylistFiles.iterator();
		while ( it.hasNext()){
			PlaylistFile plf = (PlaylistFile)it.next();
			if ( plf.isReady()){
				plfOut = plf;
			}
		}
		return plfOut;
	}
	
	/**
	 * @return
	 */
	public void addFile(PlaylistFile plFile) {
		if (!alPlaylistFiles.contains(plFile)) {
			alPlaylistFiles.add(plFile);
            String sPlaylistFiles = plFile.getId();
            if (this.containsProperty(XML_PLAYLIST_FILES)){
                sPlaylistFiles += ","+getValue(XML_PLAYLIST_FILES); //add previous playlist files  //$NON-NLS-1$
            }
            setProperty(XML_PLAYLIST_FILES,sPlaylistFiles);
		}
	}

    
    protected void removePlaylistFile(PlaylistFile plf) {
        if (alPlaylistFiles.contains(plf)){
            alPlaylistFiles.remove(plf);
            rebuildProperty();
        }
    }
    
    
    /**
     * Rebuild playlist files property
     * @return
     */
	public void rebuildProperty() {
	    String sPlaylistFiles = ""; //$NON-NLS-1$
	    if (alPlaylistFiles.size() > 0){
	        sPlaylistFiles += alPlaylistFiles.get(0).getId();
	        for (int i=1; i<alPlaylistFiles.size();i++){
	            sPlaylistFiles += "," + alPlaylistFiles.get(i).getId(); //$NON-NLS-1$
	        }
	    }
	    setProperty(XML_PLAYLIST_FILES,sPlaylistFiles);
	}
	
	    
	/**
	 * Return true if this playlist map to specified playlist file id 
	 * @param sId
	 * @return
	 */
	private boolean mapPlaylistFile(String sId){
		for (int i=0;i<alPlaylistFiles.size();i++){
			if (((PlaylistFile)alPlaylistFiles.get(i)).getId().equals(sId)){
				return true;
			}	
		}
		return false;
	}	
	
	/**
	 * Get playlist name
	 * @return playlist name
	 */
	public String getName(){
		String sOut = ""; //$NON-NLS-1$
		if ( alPlaylistFiles.size() > 0){
			sOut =((PlaylistFile)alPlaylistFiles.get(0)).getName(); 
		}
		return sOut; 
	}
	
	/**
	 *Alphabetical comparator used to display ordered lists of playlists
	 *<p>Sort ignoring cases but different items with different cases should be distinct
     * before being added into bidimap</p>
     *@param other playlist to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
		Playlist otherPlaylist = (Playlist)o;
		//use id in compare because 2 different playlists can have the same name
        String sAbs = getName()+getId();
        String sOtherAbs = otherPlaylist.getName()+otherPlaylist.getId();
        if (sAbs.equalsIgnoreCase(sOtherAbs) && !sAbs.equals(sOtherAbs)){
            return (sAbs+getId()).compareToIgnoreCase(sOtherAbs+otherPlaylist.getId());
        }
        else{
            return sAbs.compareToIgnoreCase(sOtherAbs);
        }
  }

    
    /**
     * Get item description
     */
    public String getDesc(){
        return Messages.getString("Item_Playlist")+" : "+getName(); //$NON-NLS-1$ //$NON-NLS-2$
    }
       
}
