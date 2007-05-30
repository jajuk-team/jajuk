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

import java.util.ArrayList;

/**
 * A music style ( jazz, rock...)
 * <p>
 * Logical item
 */
public class Style extends LogicalItem implements Comparable {

	private static final long serialVersionUID = 1L;

	/**
	 * Style constructor
	 * 
	 * @param id
	 * @param sName
	 */
	public Style(String sId, String sName) {
		super(sId, sName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIdentifier()
	 */
	public String getIdentifier() {
		return XML_STYLE;
	}

	/**
	 * Return style name, dealing with unkwnown for any language
	 * 
	 * @return author name
	 */
	public String getName2() {
		String sOut = getName();
		if (sOut.equals(UNKNOWN_STYLE)) {
			sOut = Messages.getString(UNKNOWN_STYLE);
		}
		return sOut;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Style[ID=" + sId + " Name={{" + getName() + "}}]";   
	}

	/**
	 * Alphabetical comparator used to display ordered lists
	 * 
	 * @param other
	 *            item to be compared
	 * @return comparaison result
	 */
	public int compareTo(Object o) {
		Style otherStyle = (Style) o;
		// compare using name and id to differenciate unknown items
		return (getName2() + getId()).compareToIgnoreCase(otherStyle.getName2()
				+ otherStyle.getId());
	}

	/**
	 * @return Number of tracks for this style from the collection
	 */
	public int getCount() {
		return TrackManager.getInstance().getAssociatedTracks(this).size();
	}

	/**
	 * @return whether the style is Unknown or not
	 */
	public boolean isUnknown() {
		return this.getName().equals(UNKNOWN_STYLE);
	}

	/**
	 * Get item description
	 */
	public String getDesc() {
		return Messages.getString("Item_Style") + " : " + getName2();  
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
	 */
	final public String getHumanValue(String sKey) {
		if (XML_NAME.equals(sKey)) {
			return getName2();
		} else {// default
			return super.getHumanValue(sKey);
		}
	}

	/**
	 * 
	 * @return all tracks associated with this style
	 */
	public ArrayList<Track> getTracksRecursively() {
		ArrayList<Track> alTracks = new ArrayList<Track>(1000);
		for (Item item : TrackManager.getInstance().getTracks()) {
			Track track = (Track) item;
			if (track.getStyle().equals(this)) {
				alTracks.add(track);
			}
		}
		return alTracks;
	}

}
