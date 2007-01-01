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

import org.jajuk.i18n.Messages;

/**
 * An author *
 * <p>
 * Logical item
 * 
 * @author Bertrand Florat
 * @created 17 oct. 2003
 */
public class Author extends Item implements Comparable {

	private static final long serialVersionUID = 1L;

	/**
	 * Author constructor
	 * 
	 * @param id
	 * @param sName
	 */
	public Author(String sId, String sName) {
		super(sId, sName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIdentifier()
	 */
	final public String getIdentifier() {
		return XML_AUTHOR;
	}

	/**
	 * Return author name, dealing with unkwnown for any language
	 * 
	 * @return author name
	 */
	public String getName2() {
		String sOut = getName();
		if (sOut.equals(UNKNOWN_AUTHOR)) {
			sOut = Messages.getString(UNKNOWN_AUTHOR);
		}
		return sOut;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Author[ID=" + sId + " Name={{" + sName + "}}]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Equal method to check two authors are identical
	 * 
	 * @param otherAuthor
	 * @return
	 */
	public boolean equals(Object otherAuthor) {
		if (otherAuthor == null) {
			return false;
		}
		return this.getId().equals(((Author) otherAuthor).getId());
	}

	/**
	 * hashcode ( used by the equals method )
	 */
	public int hashCode() {
		return getId().hashCode();
	}

	/**
	 * Alphabetical comparator used to display ordered lists
	 * 
	 * @param other
	 *            item to be compared
	 * @return comparaison result
	 */
	public int compareTo(Object o) {
		Author otherAuthor = (Author) o;
		// compare using name and id to differenciate unknown items
		return (getName2() + getId()).compareToIgnoreCase(otherAuthor
				.getName2()
				+ otherAuthor.getId());
	}

	/**
	 * @return whether the author is Unknown or not
	 */
	public boolean isUnknown() {
		return this.getName().equals(UNKNOWN_AUTHOR);
	}

	/**
	 * Get item description
	 */
	public String getDesc() {
		return Messages.getString("Item_Author") + " : " + getName2(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
	 */
	public String getHumanValue(String sKey) {
		if (XML_NAME.equals(sKey)) {
			return getName2();
		}
		// default
		return super.getHumanValue(sKey);
	}

}
