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

import org.jajuk.util.Util;

/**
 * A music style ( jazz, rock...)
 * <p>
 * Logical item
 * 
 * @author bflorat @created 17 oct. 2003
 */
public class Style extends PropertyAdapter {

	/** Style ID. Ex:1,2,3... */
	private String sId;
	/** Style name upper case. ex:ROCK, JAZZ */
	private String sName;
	/** Authors for this style */
	private ArrayList alAuthors = new ArrayList(10);

	/**
	 * Style constructor
	 * 
	 * @param id
	 * @param sName
	 */
	public Style(String sId, String sName) {
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
		return "Style[ID=" + sId + " Name=" + getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Return an XML representation of this item
	 * 
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<style id='" + sId);
		sb.append("' name='");
		sb.append(Util.formatXML(sName)).append("'/>\n");
		return sb.toString();
	}

	/**
	 * @return
	 */
	public String getId() {
		return sId;
	}

	/**
	 * Equal method to check two styles are identical
	 * 
	 * @param otherStyle
	 * @return
	 */
	public boolean equals(Object otherStyle) {
		return this.getId().equals(((Style)otherStyle).getId());
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
	public ArrayList getAuthors() {
		return alAuthors;
	}

	/**
	 * @param album
	 */
	public void addAuthor(Author author) {
		alAuthors.add(author);
	}

}
