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
 * Revision 1.5  2003/11/03 06:08:05  bflorat
 * 03/11/2003
 *
 * Revision 1.4  2003/10/28 21:34:37  bflorat
 * 28/10/2003
 *
 * Revision 1.3  2003/10/26 21:28:49  bflorat
 * 26/10/2003
 *
 * Revision 1.2  2003/10/23 22:07:40  bflorat
 * 23/10/2003
 *
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;

import org.jajuk.util.Util;

/**
 *  An author
 **<p> Logical item
 * @author     bflorat
 * @created    17 oct. 2003
 */
public class Author extends PropertyAdapter {

	/** author ID. Ex:1,2,3...*/
	private String sId;
	/**Author name */
	private String sName;
	/**Albums for this album*/
	private ArrayList alAlbums = new ArrayList(10);

	/**
	 * Author constructor
	 * @param id
	 * @param sName
	 */
	public Author(String sId, String sName) {
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
		return "Author[ID="+sId+" Name=" + sName + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<author id='" + sId);//$NON-NLS-1$
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
	 * Equal method to check two authors are identical
	 * @param otherAuthor
	 * @return
	 */
	public boolean equals(Object otherAuthor){
		return this.getId().equals(((Author)otherAuthor).getId() );
	}	

		/**
		 * @return
		 */
		public ArrayList getAlbums() {
			return alAlbums;
		}

		/**
		 * @param album
		 */
		public void addAlbum(Album album) {
			alAlbums.add(album);
		}

}
