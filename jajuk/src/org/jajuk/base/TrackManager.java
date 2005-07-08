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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.jajuk.util.MD5Processor;
import org.jajuk.util.SequentialMap;

/**
 * Convenient class to manage Tracks
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class TrackManager extends ItemManager implements Observer{
	/** Tracks collection maps: ID -> track* */
	static HashMap hmTracks = new HashMap(100);
    /**Self instance*/
    static TrackManager singleton;
	
	/**
	 * No constructor available, only static access
	 */
	private TrackManager() {
		super();
		//      subscriptions
        ObservationManager.register(EVENT_FILE_NAME_CHANGED,this);
	}
    
    /**
     * @return singleton
     */
    public static ItemManager getInstance(){
      if (singleton == null){
          singleton = new TrackManager();
      }
        return singleton;
    }

	/**
	 * Register an Track
	 * 
	 * @param sName
	 */
	public static synchronized Track registerTrack(String sName, Album album, Style style, Author author, long length, String sYear, Type type) {
		String sId = MD5Processor.hash(style.getName() + author.getName() +album.getName() + sYear + length + type.getName() + sName);
		return registerTrack(sId, sName, album, style, author, length, sYear, type);
	}
    
    /**
     * Change a track property
     * @param old
     * @param sProperty
     * @param sValue
     */
    public static synchronized void changeTrackProperty(Track old,String  sProperty, String sValue) {
        SequentialMap smap = old.getProperties(); //store old properties
        Track newItem = new Track(null,null,null,null,null,0l,null,null); //create any empty item
        newItem.setProperties(smap);//apply stored properties
        newItem.setProperty(sProperty,sValue); //overwrite the given parameter
        registerTrack(newItem.getName(),newItem.getAlbum(),newItem.getStyle(),newItem.getAuthor(),newItem.getLength(),newItem.getYear(),newItem.getType());//register it
        remove(old.getId());//remove old
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
			Iterator itFiles = track.getFiles().iterator();
			while (itFiles.hasNext()) {
				org.jajuk.base.File file = (org.jajuk.base.File) itFiles.next();
				if (FileManager.getFileById(file.getId()) == null) { //test if the file exists in the main file repository
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
    
    /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_TRACKS;
    }    
    
    /**
     * Get tracks properties (all the same)
     * @return
     */
    public static ArrayList getTracksProperties(){
        Track track = null;
        if (getTracks().size() > 0){
            track = (Track)TrackManager.getTracks().get(0);
        }
        else{
            track = new Track("","",null,null,null,0,"",null); 
        }
        return (ArrayList)track.getProperties().keys();
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if (EVENT_FILE_NAME_CHANGED.equals(subject)){
            Properties properties = event.getDetails();
            File fNew = (File)properties.get(DETAIL_NEW);
            File fileOld = (File)properties.get(DETAIL_OLD);
            Track track =fileOld.getTrack(); 
            track.removeFile(fileOld);
            track.addFile(fNew);
        }
    }
    
}
