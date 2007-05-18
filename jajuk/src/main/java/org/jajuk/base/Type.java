/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
import org.jajuk.tag.ITagImpl;
import org.jajuk.util.log.Log;

/**
 * Music type
 */
public class Type extends PhysicalItem implements Comparable {

	private static final long serialVersionUID = 1L;

	/** Type extension ex:mp3,ogg */
	private String sExtension;

	/** Player impl */
	private Class cTagImpl;

	/** Player class */
	private Class cPlayerImpl;

	/**
	 * Constructor
	 * 
	 * @param sId
	 *            type id if given
	 * @param sName
	 *            type name
	 * @param sExtension
	 *            type file extension (.mp3...)
	 * @param sPlayerImpl
	 *            Type player implementation class
	 * @param sTagImpl
	 *            Type Tagger implementation class
	 * @throws Exception
	 */
	public Type(String sId, String sName, String sExtension, Class cPlayerImpl,
			Class cTagImpl) throws Exception {
		super(sId, sName);
		this.cPlayerImpl = cPlayerImpl;
		this.sExtension = sExtension;
		setProperty(XML_TYPE_EXTENSION, sExtension);
		setProperty(XML_TYPE_PLAYER_IMPL, cPlayerImpl);
		this.cTagImpl = cTagImpl;
		if (cTagImpl != null) { // can be null for playlists
			setProperty(XML_TYPE_TAG_IMPL, cTagImpl);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIdentifier()
	 */
	final public String getIdentifier() {
		return XML_TYPE;
	}

	/**
	 * @return Player class for this type
	 */
	public Class getPlayerClass() throws Exception {
		return cPlayerImpl;
	}

	/**
	 * @return Tagger class for this type
	 */
	public Class getTaggerClass() {
		return cTagImpl;
	}

	/**
	 * @return
	 */
	public String getExtension() {
		return sExtension;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Type[ID=" + sId + " Name=" + getName() + " ; Extension=" + sExtension + "]";    
	}

	/**
	 * Equal method to check two types are identical
	 * 
	 * @param otherType
	 * @return
	 */
	public boolean equals(Object otherType) {
		if (otherType == null) {
			return false;
		}
		return (hashcode == ((Type) otherType).hashcode);
	}

	/**
	 * @return Returns the tagImpl.
	 */
	public ITagImpl getTagImpl() {
		ITagImpl tagInstance = null;
		try {
			if (cTagImpl == null) {
				return tagInstance;
			}
			tagInstance = (ITagImpl) cTagImpl.newInstance();
		} catch (Exception e) {
			Log.error(e);
		}
		return tagInstance;
	}

	/**
	 * Get item description
	 */
	public String getDesc() {
		return Messages.getString("Type") + " : " + getName();  
	}

	/**
	 * Alphabetical comparator used to display ordered lists
	 * 
	 * @param other
	 *            item to be compared
	 * @return comparaison result
	 */
	public int compareTo(Object other) {
		return toString().compareTo(other.toString());
	}

}
