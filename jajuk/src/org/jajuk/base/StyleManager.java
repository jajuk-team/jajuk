/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,
 * USA. $Log$
 * USA. Revision 1.6  2003/11/03 06:08:05  bflorat
 * USA. 03/11/2003
 * USA.
 */

package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 * Convenient class to manage styles
 * 
 * @author bflorat @created 17 oct. 2003
 */
public class StyleManager {
	/** Styles collection* */
	static HashMap hmStyles = new HashMap(10);

	/**
	 * No constructor available, only static access
	 */
	private StyleManager() {
		super();
	}

	/**
	 * Register a style
	 * 
	 * @param sName
	 */
	public static synchronized Style registerStyle(String sName) {
		String sId = MD5Processor.hash(sName.trim().toLowerCase());
		return registerStyle(sId, sName);
	}

	/**
	 * Register a style with a known id
	 * 
	 * @param sName
	 */
	public static synchronized Style registerStyle(String sId, String sName) {
		String sIdTest = MD5Processor.hash(sName.trim().toLowerCase());
		if (hmStyles.containsKey(sIdTest)) {
			return (Style) hmStyles.get(sIdTest);
		}
		Style style = new Style(sId, sName);
		hmStyles.put(sId, style);
		return style;
	}

	/**
	 * Remove a style
	 * 
	 * @param style
	 *                   id
	 */
	public static synchronized void remove(String sId) {
		hmStyles.remove(sId);
	}
	
	
	/**
	 * Perform a style cleanup : delete useless items
	 *
	 */
	public static synchronized void cleanup(){
		Iterator itStyles;
		Iterator itTracks;
		ArrayList alTrack = TrackManager.getTracks();
		itStyles = hmStyles.values().iterator();
		while (itStyles.hasNext()) {
			Style style = (Style) itStyles.next();
			boolean bUsed = false;
			itTracks = alTrack.iterator();
			while (itTracks.hasNext()) {
					Track track = (Track) itTracks.next();
					if (track.getStyle().equals(style)) {
						bUsed = true;
					}
				}
				if (!bUsed) { //clean this style
					itStyles.remove();
				}
			}
	}

	/**
	 * Format the Style name to be normalized :
	 * <p>
	 * -no underscores or other non-ascii characters
	 * <p>
	 * -no spaces at the begin and the end
	 * <p>
	 * -All in upper case
	 * <p>
	 * exemple: "ROCK"
	 * 
	 * @param sName
	 * @return
	 */
	private static String format(String sName) {
		String sOut;
		sOut = sName.trim(); //supress spaces at the begin and the end
		sOut.replace('-', ' '); //move - to space
		sOut.replace('_', ' '); //move _ to space
		sOut = sOut.toUpperCase();
		return sOut;
	}

	/** Return all registred styles */
	public static synchronized ArrayList getStyles() {
		return new ArrayList(hmStyles.values());
	}

	/**
	 * Return style by id
	 * 
	 * @param sId
	 * @return
	 */
	public static synchronized Style getStyle(String sId) {
		return (Style) hmStyles.get(sId);
	}

}