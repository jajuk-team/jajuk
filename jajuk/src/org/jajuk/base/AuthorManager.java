/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,USA
 * $Revision$
 **/

package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 * Convenient class to manage authors
 * 
 * @author bflorat @created 17 oct. 2003
 */
public class AuthorManager {
	/** Authors collection* */
	static HashMap hmAuthors = new HashMap(100);

	/**
	 * No constructor available, only static access
	 */
	private AuthorManager() {
		super();
	}

	/**
	 * Register an author
	 * 
	 * @param sName
	 */
	public static  synchronized Author registerAuthor(String sName) {
		String sId = MD5Processor.hash(sName.trim().toLowerCase());
		return registerAuthor(sId, sName);
	}

	/**
	 * Register an author with a known id
	 * 
	 * @param sName
	 */
	public static  synchronized Author registerAuthor(String sId, String sName) {
		String sIdTest = MD5Processor.hash(sName.trim().toLowerCase());
		if (hmAuthors.containsKey(sIdTest)) {
			return (Author) hmAuthors.get(sIdTest);
		}
		Author author = new Author(sId, sName);
		hmAuthors.put(sId, author);
		return author;
	}
	
	/**
		 * Perform an authors cleanup : delete useless items
		 *
		 */
	public static synchronized void cleanup(){
		Iterator itAuthors = hmAuthors.values().iterator();
		ArrayList alTrack = TrackManager.getTracks();
		Iterator itTracks = null;
			while (itAuthors.hasNext()) {
				Author author = (Author) itAuthors.next();
				boolean bUsed = false;
				itTracks = alTrack.iterator();
				while (itTracks.hasNext()) {
					Track track = (Track) itTracks.next();
					if (track.getAuthor().equals(author)) {
						bUsed = true;
					}
				}
				if (!bUsed) { //clean this author
					itAuthors.remove();
				}
			}
	}

	/**
	 * Remove an author
	 * 
	 * @param style  id
	 */
	public static  synchronized void remove(String sId) {
		hmAuthors.remove(sId);
	}

	/** Return all registred Authors */
	public static synchronized ArrayList getAuthors() {
		return new ArrayList(hmAuthors.values());
	}

	/**
	 * Return author by id
	 * 
	 * @param sName
	 * @return
	 */
	public static  synchronized Author getAuthor(String sId) {
		return (Author) hmAuthors.get(sId);
	}

	/**
	 * Format the author name to be normalized :
	 * <p>
	 * -no underscores or other non-ascii characters
	 * <p>
	 * -no spaces at the begin and the end
	 * <p>
	 * -All in lower cas expect first letter of first word
	 * <p>
	 * exemple: "My author"
	 * 
	 * @param sName
	 * @return
	 */
	private static String format(String sName) {
		String sOut;
		sOut = sName.trim(); //supress spaces at the begin and the end
		sOut.replace('-', ' '); //move - to space
		sOut.replace('_', ' '); //move _ to space
		char c = sOut.charAt(0);
		sOut = sOut.toLowerCase();
		StringBuffer sb = new StringBuffer(sOut);
		sb.setCharAt(0, Character.toUpperCase(c));
		return sb.toString();
	}

}
