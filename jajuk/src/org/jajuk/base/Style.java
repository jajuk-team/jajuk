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
 * Revision 1.1  2003/10/17 20:36:45  bflorat
 * 17/10/2003
 *
 */
package org.jajuk.base;

import java.util.Properties;

/**
 *  A music style ( jazz, rock...)
 *
 * @author     bflorat
 * @created    17 oct. 2003
 */
public class Style extends PropertyAdapter {

	/** Style ID. Ex:1,2,3...*/
	private int id;
	/**Style name upper case. ex:ROCK, JAZZ */
	private String sName;

	/**
	 * Style constructor
	 * @param id
	 * @param sName
	 */
	//TODO: see javadoc/arguments auto
	public Style(int id, String sName) {
		super();
		this.id = id;
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
		return "Style[Name=" + getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

		/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<type id='" + id);
		sb.append("' name='");
		sb.append(sName).append("'/>\n");
		return sb.toString();
	}

	/**
	* @return
	 */
	public int getId() {
		return id;
	}

}
