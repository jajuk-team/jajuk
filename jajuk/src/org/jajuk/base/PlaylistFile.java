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
* $Revision$
 *
 */
package org.jajuk.base;

import org.jajuk.util.Util;


/**
 *  A playlist file
 * <p> Physical item
 * @Author     bflorat
 * @created    17 oct. 2003
 */
public class PlaylistFile extends PropertyAdapter {

	/**ID. Ex:1,2,3...*/
	private String  sId;
	/**Playlist name */
	private String sName;
	/**Playlist hashcode*/
	private String  sHashcode;
	/**Playlist parent directory*/
	private Directory dParentDirectory;
	
	
	/**
	 * Playlist file constructor
	 * @param sId
	 * @param sName
	 * @param sHashcode
	 * @param sParentDirectory
	 */
	public PlaylistFile(String sId, String sName,String sHashcode,Directory dParentDirectory) {
		this.sId = sId;
		this.sName = sName;
		this.sHashcode = sHashcode;
		this.dParentDirectory = dParentDirectory;
	}

	
	/**
	 * toString method
	 */
	public String toString() {
		return "Playlist file[ID="+sId+" Name=" + getName() + " Hashcode="+sHashcode+" Dir="+dParentDirectory.getId()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<playlist_file id='" + sId);
		sb.append("' name='");
		sb.append(Util.formatXML(sName));
		sb.append("' hashcode='");
		sb.append(sHashcode);
		sb.append("' directory='");
		sb.append(dParentDirectory.getId()).append("' ");
		sb.append(getPropertiesXml());
		sb.append("/>\n");
		return sb.toString();
	}


	/**
	 * Equal method to check two playlist files are identical
	 * @param otherPlaylistFile
	 * @return
	 */
	public boolean equals(Object otherPlaylistFile){
		return this.getHashcode().equals(((PlaylistFile)otherPlaylistFile).getHashcode());
	}	
	
	/**
	 * hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
	}

	/**
	 * @return
	 */
	public String getHashcode() {
		return sHashcode;
	}

	/**
	 * @return
	 */
	public String getId() {
		return sId;
	}

	/**
	 * @return
	 */
	public String getName() {
		return sName;
	}

	/**
	 * @return
	 */
	public Directory getDirectory() {
		return dParentDirectory;
	}

}
