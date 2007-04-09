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
 *  $Revision: 2118 $
 */
package org.jajuk.base;

import org.jajuk.i18n.Messages;

/**
 * 
 * Year object
 * 
 * @author Bertrand Florat
 * @created 09 april 2007
 */
public class Year extends Item implements Comparable {

	private static final long serialVersionUID = 1L;
	
	private final long value;

	/**
	 * Year constructor
	 * 
	 * @param id
	 * @param sName
	 */
	public Year(String sId, String sValue) {
		super(sId, sValue);
		this.value = Long.parseLong(sValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIdentifier()
	 */
	final public String getIdentifier() {
		return XML_YEAR;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Year[ID=" + sId + " Value=" + sName + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Equal method to check two years are identical
	 * 
	 * @param otherYear
	 * @return
	 */
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		return getValue() == ((Year)other).getValue();
	}

	/**
	 * hashcode ( used by the equals method )
	 */
	public int hashCode() {
		return getId().hashCode();
	}
	
	/**
	 * 
	 * @return year as a long
	 */
	public long getValue(){
		return value;
	}

	/**
	 * Alphabetical comparator used to display ordered lists
	 * 
	 * @param other
	 *            item to be compared
	 * @return comparaison result
	 */
	public int compareTo(Object o) {
		Year other = (Year) o;
		return (int)(getValue() - other.getValue());
	}

	/**
	 * Get item description
	 */
	public String getDesc() {
		return Messages.getString("Property_year") + " : " + getName();
	}

}
