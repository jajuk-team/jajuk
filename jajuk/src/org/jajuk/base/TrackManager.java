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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 * Convenient class to manage Tracks
 * 
 * @author bflorat @created 17 oct. 2003
 */
public class TrackManager implements ITechnicalStrings {
	/** Tracks collection maps: ID -> track* */
	static HashMap hmTracks = new HashMap(100);

	
	/**
	 * No constructor available, only static access
	 */
	private TrackManager() {
		super();
	}

	/**
	 * Register an Track
	 * 
	 * @param sName
	 */
	public static synchronized Track registerTrack(String sName, Album album, Style style, Author author, long length, String sYear, Type type) {
		String sId = MD5Processor.hash(style.getName() + author.getName() + sYear + length + type.getName() + sName);
		return registerTrack(sId, sName, album, style, author, length, sYear, type);
	}

	/**
	 * Register an Track with a known id
	 * 
	 * @param sName
	 */
	public static synchronized Track registerTrack(String sId, String sName, Album album, Style style, Author author, long length, String sYear, Type type) {
		Track track = null;
		if (!hmTracks.containsKey(sId)) {
			String sAdditionDate = new SimpleDateFormat(DATE_FILE).format(new Date());
			track = new Track(sId, sName, album, style, author, length, sYear, type);
			track.setAdditionDate(sAdditionDate);
			hmTracks.put(sId, track);
			return track;
		}
		else{
			return (Track)hmTracks.get(sId);
		}
		
	}

	/**
	 * Remove a track
	 * 
	 * @param style
	 *                   id
	 */
	public static synchronized void remove(String sId) {
		hmTracks.remove(sId);
	}

	/**
	 * Perform a track cleanup : delete useless items
	 *  
	 */
	public static synchronized void cleanup() {
		Iterator itTracks = hmTracks.values().iterator();
		while (itTracks.hasNext()) {
			Track track = (Track) itTracks.next();
			if ( track.getFiles().size() == 0){ //no associated file
				itTracks.remove();
				continue;
			}
			ArrayList alFiles = track.getFiles();
			Iterator itFiles = track.getFiles().iterator();
			while (itFiles.hasNext()) {
				org.jajuk.base.File file = (org.jajuk.base.File) itFiles.next();
				if (FileManager.getFile(file.getId()) == null) { //test if the file exists in the main file repository
					itFiles.remove();//no? remove it from the track
				}
			}
			if (track.getFiles().size() == 0) { //the track don't map anymore to any physical item, just remove it
				itTracks.remove();
			}
		}
	}

	/** Return all registred Tracks */
	public static synchronized ArrayList getTracks() {
		return new ArrayList(hmTracks.values());
	}
	
	
	/** Return sorted registred Tracks */
		public static synchronized ArrayList getSortedTracks() {
			ArrayList alTracks = new ArrayList(hmTracks.values());
			Collections.sort(alTracks);
			return alTracks;
		}

	/**
	 * Return Track by id
	 * 
	 * @param sName
	 * @return
	 */
	public static synchronized Track getTrack(String sId) {
		return (Track) hmTracks.get(sId);
	}

	/**
	 * Format the tracks names to be normalized :
	 * <p>
	 * -no underscores or other non-ascii characters
	 * <p>
	 * -no spaces at the begin and the end
	 * <p>
	 * -All in lower cas expect first letter of first word
	 * <p>
	 * exemple: "My track title"
	 * 
	 * @param sName
	 * @return
	 */
	private static synchronized String format(String sName) {
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
