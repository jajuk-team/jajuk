/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Log$
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */
package org.jajuk.base;

import java.util.Properties;

/**
 *  A playlist
 *<p> Logical item
 * @Playlist     bflorat
 * @created    17 oct. 2003
 */
public class Playlist extends PropertyAdapter {

	/**ID. Ex:1,2,3...*/
	private String sId;
	/**Associated playlist files**/
	private PlaylistFile[] files;
	
	/**
	 * Playlist constructor
	 * @param sId
	 * @param files : associated playlist files
	 */
	public Playlist(String sId,PlaylistFile[] files){
		this.sId = sId;
		this.files = files;
	}

	/**
	 * toString method
	 */
	public String toString() {
		StringBuffer sbOut = new StringBuffer("Playlist[ID="+sId+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0;i<files.length;i++){
			sbOut.append('\n').append(files[i].toString());
		}
		return sbOut.toString();
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<playlist id='" + sId);
		sb.append("'' playlist_files='");
		for (int i=0;i<files.length;i++){
			sb.append(files[i].getId()).append(',');
		}
		sb.deleteCharAt(sb.length()-1); //remove the last ','
		sb.append("' ");
		sb.append(getPropertiesXml());
		sb.append("/>\n");
		return sb.toString();
	}

	
	/**
	 * Equal method to check two playlists are identical
	 * @param otherPlaylist
	 * @return
	 */
	public boolean equals(Playlist otherPlaylist){
		boolean bOut = true;
		PlaylistFile[] otherFiles = otherPlaylist.getFiles();
		if (otherFiles.length != files.length){ //if length is not the same, just leave
			return false;
		}	
		for (int i=0;i<otherFiles.length;i++){ //check for each file that we contain it
			if (!mapPlaylistFile(otherFiles[i].getId())){
				return false;
			}
		}
		return true;
	}	


	/**
	 * @return
	 */
	public PlaylistFile[] getFiles() {
		return files;
	}

	/**
	 * @return
	 */
	public String getId() {
		return sId;
	}
	
	/**
	 * Return true if this playlist map to specified playlist file id 
	 * @param sId
	 * @return
	 */
	private boolean mapPlaylistFile(String sId){
		for (int i=0;i<files.length;i++){
			if (files[i].getId().equals(sId)){
				return true;
			}	
		}
		return false;
	}	

}
