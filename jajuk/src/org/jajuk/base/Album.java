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
 *  $Revision$
 */
package org.jajuk.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 *  An Album
 * *<p> Logical item
 * @Author     bflorat
 * @created    17 oct. 2003
 */
public class Album extends PropertyAdapter implements Comparable{

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
	 * Return album name, dealing with unkwnown for any language
	 * @return album name
	 */
	public String getName2() {
		String sOut = getName();
		if (sOut.equals(UNKNOWN_ALBUM)){ 
			sOut = Messages.getString(UNKNOWN_ALBUM); 
		}
		return sOut;
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
	 * return tracks associated with this item
	 * @return tracks associated with this item
	 */
	public ArrayList getTracks() {
		ArrayList alTracks = new ArrayList(100);
		Iterator it = TrackManager.getTracks().iterator();
		while ( it.hasNext()){
			Track track = (Track)it.next();
			if ( track != null && track.getAlbum().equals(this)){
				alTracks.add(track);
			}
		}
		Collections.sort(alTracks);
		return alTracks;
	}
	
	/**
	 * @param list
	 */
	public void addTrack(Track track) {
		alTracks.add(track);
	}

	/**
	 *Alphabetical comparator used to display ordered lists
	 *@param other item to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
		Album otherAlbum = (Album)o;
		return  getName2().compareToIgnoreCase(otherAlbum.getName2());
	}
	
	/**
	 * @return whether the albumr is Unknown or not
	 */
	public boolean isUnknown(){
	    return this.getName().equals(UNKNOWN_ALBUM); 
   }
	
}
