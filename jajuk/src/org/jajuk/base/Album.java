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
 */
package org.jajuk.base;

import java.util.ArrayList;

import org.jajuk.util.Util;

/**
 *  An Album
 * *<p> Logical item
 * @Author     bflorat
 * @created    17 oct. 2003
 */
public class Album extends PropertyAdapter {

	/** Album ID. Ex:1,2,3...*/
	private String sId;
	/**Album name */
	private String sName;
	/**Tracks for this album*/
	private ArrayList alTracks = new ArrayList(10);
	

	/**
	 * Album constructor
	 * @param id
	 * @param sName
	 */
	public Album(String sId, String sName) {
		super();
		this.sId = sId;
		this.sName = sName;
	}

	/**
	 * @return
	 */
	public String getName() {
		return sName;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Album[ID="+getId()+" Name=" + getName() +"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<album id='" + sId);//$NON-NLS-1$
		sb.append("' name='");//$NON-NLS-1$
		sb.append(Util.formatXML(sName)).append("' ");//$NON-NLS-1$
		sb.append(getPropertiesXml());
		sb.append("/>\n");//$NON-NLS-1$
		return sb.toString();
	}

	/**
	* @return
	 */
	public String getId() {
		return sId;
	}

	
	/**
	 * Equal method to check two albums are identical
	 * @param otherAlbum
	 * @return
	 */
	public boolean equals(Object otherAlbum){
		return this.getId().equals(((Album)otherAlbum).getId() );
	}
	
	/**
	 * Album hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
	}

	/**
	 * @return
	 */
	public ArrayList getTracks() {
		return alTracks;
	}

	/**
	 * @param list
	 */
	public void addTrack(Track track) {
		alTracks.add(track);
	}

}
