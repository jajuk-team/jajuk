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
 * Revision 1.2  2003/10/23 22:07:40  bflorat
 * 23/10/2003
 *
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

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
		sb.append(sName);
		sb.append("'' style='");//$NON-NLS-1$
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
	public boolean equals(Album otherAlbum){
		return this.getName().equals(otherAlbum.getName() );
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
