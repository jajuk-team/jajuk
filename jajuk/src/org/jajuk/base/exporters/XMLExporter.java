/*
 *  Jajuk Copyright (C) 2006 Ronak Patel
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

package org.jajuk.base.exporters;

import org.jajuk.util.log.Log;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.base.Style;
import org.jajuk.base.Author;
import org.jajuk.base.Album;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.StringBuffer;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

/**
 * Exports music data to an XML file.
 * <p> Singleton
 * 
 * @author     Ronak Patel
 * @created    23 dec. 2005
 */
public class XMLExporter implements ITechnicalStrings {
    /** Self instance */
	private static XMLExporter singleton;
	
	/** Instance getter */
	public static synchronized XMLExporter getInstance() {
		if (singleton == null) {
			singleton = new XMLExporter();
		} 
		return singleton;
	}
	
	/** Hidden constructor */
	private XMLExporter() {
		
	}
	
	/**
	 * Returns string containing XML markup for entire collection.
	 * @return xml for collection
	 */
	public String collectionToXML() {
		
		return "";
	}
	
	/**
	 * Returns string containing XML markup for device specified.
	 * @param device
	 * @return xml for device
	 */
	public String deviceToXML(Device device) {
		StringBuffer sb = new StringBuffer();
		
		// Gather information about the device.
		sb.append("<" + device.getIdentifier() + ">\n");			
		sb.append(createTabs(1) + "<name>" + device.getName() + "</name>\n");
		sb.append(createTabs(1) + "<type>" + device.getDeviceTypeS() + "</type>\n");
		sb.append(createTabs(1) + "<url>" + device.getUrl() + "</url>\n");
		sb.append(createTabs(1) + "<unix-mount-point>" + device.getMountPoint() + "</unix-mount-point>\n");
		
		// Retrieve the directory of the device to start traversal.
		Directory deviceDir = DirectoryManager.getInstance().getDirectoryForIO(device.getFio());
		sb.append(toXMLHelper(deviceDir,1));
		
		sb.append("</" + device.getIdentifier() + ">\n");
		
		return sb.toString();
	}
	
	/**
	 * Returns string containing XML markup for directory specified.
	 * @param dir
	 * @return xml for directory
	 */
	public String directoryToXML(Directory dir) {
		StringBuffer sb = new StringBuffer();
		
		// Gather information about the directory
		sb.append("<" + dir.getIdentifier() + ">\n");	
		sb.append(createTabs(1) + "<name>" + dir.getName() + "</name>\n");
		sb.append(createTabs(1) + "<id>" + dir.getId() + "</id>\n");
		sb.append(createTabs(1) + "<path>" + dir.getAbsolutePath() + "</path>\n");
		sb.append(createTabs(1) + "<device-type>" + dir.getDevice().getDeviceTypeS() + "</device-type>\n");
		sb.append(createTabs(1) + "<device-name>" + dir.getDevice().getName() + "</device-name>\n");
		sb.append(toXMLHelper(dir,1));	// Start recursive call for children of dir.
		sb.append("</" + dir.getIdentifier() + ">\n");
		
		return sb.toString();
	}
	
	// Recursive method to traverse directories.
	private String toXMLHelper(Directory dir, int level) {
		StringBuffer sb = new StringBuffer();
		
		// Retrieve child directories of current directory and gather information on them.
		ArrayList directories = dir.getDirectories();
		Iterator itr1 = directories.iterator();
		while (itr1.hasNext()) {
			Directory directory = (Directory)itr1.next();
			sb.append(createTabs(level) + "<" + dir.getIdentifier() + ">\n");	
			sb.append(createTabs(level+1) + "<name>" + directory.getName() + "</name>\n" );
			sb.append(createTabs(level+1) + "<id>" + dir.getId() + "</id>\n");
			sb.append(createTabs(level+1) + "<path>" + dir.getAbsolutePath() + "</path>\n");
			sb.append(toXMLHelper(directory, level+1));		// recursive call to traverse child directories
			sb.append(createTabs(level) + "</" + dir.getIdentifier() + ">\n");
		}
		
		// Retrieve child files of current directory and gather information on them.
		Set files = dir.getFiles();
		Iterator itr2 = files.iterator();
		while (itr2.hasNext()) {
			File file = (File)itr2.next();
			sb.append(createTabs(level) + "<" + file.getIdentifier() + ">\n");
			sb.append(createTabs(level+1) + "<name>" + file.getName() + "</name>\n");
			sb.append(createTabs(level+1) + "<id>" + file.getId() + "</id>\n");
			sb.append(createTabs(level+1) + "<path>" + file.getDirectory().getAbsolutePath() + "</path>\n");
			sb.append(createTabs(level+1) + "<size>" + file.getSize() + "</size>\n");
			sb.append(createTabs(level+1) + "<quality>" + file.getQuality() + "</quality>\n");
			sb.append(createTabs(level+1) + "<" + file.getTrack().getIdentifier() + ">\n");
			sb.append(createTabs(level+2) + "<name>" + file.getTrack().getName() + "</name>\n");
			sb.append(createTabs(level+2) + "<" + file.getTrack().getStyle().getIdentifier() + ">" + file.getTrack().getStyle().getName2() + "</" + file.getTrack().getStyle().getIdentifier() + ">\n");
			sb.append(createTabs(level+2) + "<" + file.getTrack().getAuthor().getIdentifier() + ">" + file.getTrack().getAuthor().getName2() + "</" + file.getTrack().getAuthor().getIdentifier() + ">\n");
			sb.append(createTabs(level+2) + "<" + file.getTrack().getAlbum().getIdentifier() + ">" + file.getTrack().getAlbum().getName2() + "</" + file.getTrack().getAlbum().getIdentifier() + ">\n");
			sb.append(createTabs(level+2) + "<length>" + file.getTrack().getLength() + "</length>\n");
			sb.append(createTabs(level+2) + "<rate>" + file.getTrack().getRate() + "</rate>\n");
			sb.append(createTabs(level+2) + "<comment>" + file.getTrack().getValue(XML_TRACK_COMMENT) + "</comment>\n");
			sb.append(createTabs(level+2) + "<order>" + file.getTrack().getValue(XML_TRACK_ORDER) + "</order>\n");
			sb.append(createTabs(level+1) + "</" + file.getTrack().getIdentifier() + ">\n");
			sb.append(createTabs(level) + "</" + file.getIdentifier() + ">\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Returns string containing XML markup for style specified.
	 * @param style
	 * @return xml for style
	 */
	public String styleToXML(Style style) {
		ArrayList tracks = style.getTracksRecursively();		// Retrieve all tracks of style
		Track track = null;
		Track previoustrack = null;
		StringBuffer sb = new StringBuffer();
		
		sb.append("<" + style.getIdentifier() + ">\n");
		sb.append(createTabs(1) + "<name>" + style.getName2() + "</name>\n");
		
		ListIterator itr1 = tracks.listIterator();
		while (itr1.hasNext()) {	
			if (itr1.hasPrevious()) {
				previoustrack = (Track)tracks.get(itr1.previousIndex());
				track = (Track)itr1.next();	
				// Check if current author is same as the previous author
				if (previoustrack.getAuthor().getId().equals(track.getAuthor().getId())) {
					// Check if current album is same as previous album
					if (previoustrack.getAlbum().getId().equals(track.getAlbum().getId())) {
						// Gather information on current track
						sb.append(createTabs(3) + "<" + track.getIdentifier() + ">\n");
						sb.append(createTabs(4) + "<name>" + track.getName() + "</name>\n");
						sb.append(createTabs(4) + "<length>" + track.getLength() + "</length>\n");
						sb.append(createTabs(4) + "<rate>" + track.getRate() + "</rate>\n");
						sb.append(createTabs(4) + "<comment>" + track.getValue(XML_TRACK_COMMENT) + "</comment>\n");
						sb.append(createTabs(4) + "<order>" + track.getValue(XML_TRACK_ORDER) + "</order>\n");
						sb.append(createTabs(3) + "</" + track.getIdentifier() + ">\n");							
					} else {
						// Gather information on current album
						sb.append(createTabs(2) + "</" + track.getAlbum().getIdentifier() + ">\n");
						sb.append(createTabs(2) + "<" + track.getAlbum().getIdentifier() + ">\n");
						sb.append(createTabs(3) + "<name>" + track.getAlbum().getName2() + "</name>\n");
						sb.append(createTabs(3) + "<" + track.getIdentifier() + ">\n");
						sb.append(createTabs(4) + "<name>" + track.getName() + "</name>\n");
						sb.append(createTabs(4) + "<length>" + track.getLength() + "</length>\n");
						sb.append(createTabs(4) + "<rate>" + track.getRate() + "</rate>\n");
						sb.append(createTabs(4) + "<comment>" + track.getValue(XML_TRACK_COMMENT) + "</comment>\n");
						sb.append(createTabs(4) + "<order>" + track.getValue(XML_TRACK_ORDER) + "</order>\n");
						sb.append(createTabs(3) + "</" + track.getIdentifier() + ">\n");						
					}
				} else {
					// Gather information on current author
					sb.append(createTabs(2) + "</" + track.getAlbum().getIdentifier() + ">\n");
					sb.append(createTabs(1) + "</" + track.getAuthor().getIdentifier() + ">\n");
					sb.append(createTabs(1) + "<" + track.getAuthor().getIdentifier() + ">\n");
					sb.append(createTabs(2) + "<name>" + track.getAuthor().getName2() + ">\n");
					sb.append(createTabs(2) + "<" + track.getAlbum().getIdentifier() + ">\n");
					sb.append(createTabs(3) + "<name>" + track.getAlbum().getName2() + ">\n");
					sb.append(createTabs(3) + "<" + track.getIdentifier() + ">\n");
					sb.append(createTabs(4) + "<name>" + track.getName() + "</name>\n");
					sb.append(createTabs(4) + "<length>" + track.getLength() + "</length>\n");
					sb.append(createTabs(4) + "<rate>" + track.getRate() + "</rate>\n");
					sb.append(createTabs(4) + "<comment>" + track.getValue(XML_TRACK_COMMENT) + "</comment>\n");
					sb.append(createTabs(4) + "<order>" + track.getValue(XML_TRACK_ORDER) + "</order>\n");
					sb.append(createTabs(3) + "</" + track.getIdentifier() + ">/n");
				}
			} else {
				// Executed on first track in the list.
				track = (Track)itr1.next();	
				
				sb.append(createTabs(1) + "<" + track.getAuthor().getIdentifier() + ">\n");
				sb.append(createTabs(2) + "<name>" + track.getAuthor().getName2() + "</name>\n");
				sb.append(createTabs(2) + "<" + track.getAlbum().getIdentifier() + ">\n");
				sb.append(createTabs(3) + "<name>" + track.getAlbum().getName2() + "</name>\n");
				sb.append(createTabs(3) + "<" + track.getIdentifier() + ">\n");
				sb.append(createTabs(4) + "<name>" + track.getName() + "</name>\n");
				sb.append(createTabs(4) + "<length>" + track.getLength() + "</length>\n");
				sb.append(createTabs(4) + "<rate>" + track.getRate() + "</rate>\n");
				sb.append(createTabs(4) + "<comment>" + track.getValue(XML_TRACK_COMMENT) + "</comment>\n");
				sb.append(createTabs(4) + "<order>" + track.getValue(XML_TRACK_ORDER) + "</order>\n");
				sb.append(createTabs(3) + "</" + track.getIdentifier() + ">\n");						
			}
			
			// Make sure the last album or author gets included.
			if (!itr1.hasNext()) {
				sb.append(createTabs(2) + "</" + track.getAlbum().getIdentifier() + ">\n");
				sb.append(createTabs(1) + "</" + track.getAuthor().getIdentifier() + ">\n");
			}
		}		
		
		sb.append("</" + style.getIdentifier() + ">");		
		
		return sb.toString();
	}
	
	/**
	 * Returns a string containing XML markup of author specified.
	 * @param author
	 * @param style
	 * @return xml for author
	 */
	public String authorToXML(Author author, Style style) {
		ArrayList styletracklist = style.getTracksRecursively();		// Retrieve all tracks of style of the author.
		ArrayList<Track> authortracklist = new ArrayList<Track>();
		Track track = null;
		Track previoustrack = null;
		StringBuffer sb = new StringBuffer();
		
		// Retrieve all tracks from track list of style that are of the author.
		ListIterator itr1 = styletracklist.listIterator();
		while (itr1.hasNext()) {
			track = (Track)itr1.next();
			if (track.getAuthor().getId().equals(author.getId())) {
				authortracklist.add(track);
			}
		}
		
		sb.append("<" + style.getIdentifier() + ">\n");
		sb.append(createTabs(1) + "<name>" + style.getName2() + "</name>\n");
		sb.append(createTabs(1) + "<" + author.getIdentifier() + ">\n");
		sb.append(createTabs(2) + "<name>" + author.getName2() + "</name>\n");
		
		itr1 = authortracklist.listIterator();
		while (itr1.hasNext()) {
			if (itr1.hasPrevious()) {
				previoustrack = (Track)authortracklist.get(itr1.previousIndex());
				track = (Track)itr1.next();
				// Check if current album is same as previous.
				if (previoustrack.getAlbum().getId().equals(track.getAlbum().getId())) {
					// Gather current track information.
					sb.append(createTabs(3) + "<" + track.getIdentifier() + ">\n");
					sb.append(createTabs(4) + "<name>" + track.getName() + "</name>\n");
					sb.append(createTabs(4) + "<length>" + track.getLength() + "</length>\n");
					sb.append(createTabs(4) + "<rate>" + track.getRate() + "</rate>\n");
					sb.append(createTabs(4) + "<comment>" + track.getValue(XML_TRACK_COMMENT) + "</comment>\n");
					sb.append(createTabs(4) + "<order>" + track.getValue(XML_TRACK_ORDER) + "</order>\n");
					sb.append(createTabs(3) + "</" + track.getIdentifier() + ">\n");					
				} else {
					// Gather current album and track information.
					sb.append(createTabs(2) + "</" + track.getAlbum().getIdentifier() + ">\n");
					sb.append(createTabs(2) + "<" + track.getAlbum().getIdentifier() + ">\n");
					sb.append(createTabs(3) + "<name>" + track.getAlbum().getName2() + "</name>\n");
					sb.append(createTabs(3) + "<" + track.getIdentifier() + ">\n");
					sb.append(createTabs(4) + "<name>" + track.getName() + "</name>\n");
					sb.append(createTabs(4) + "<length>" + track.getLength() + "</length>\n");
					sb.append(createTabs(4) + "<rate>" + track.getRate() + "</rate>\n");
					sb.append(createTabs(4) + "<comment>" + track.getValue(XML_TRACK_COMMENT) + "</comment>\n");
					sb.append(createTabs(4) + "<order>" + track.getValue(XML_TRACK_ORDER) + "</order>\n");
					sb.append(createTabs(3) + "</" + track.getIdentifier() + ">\n");
					
				}
			} else {
				// Executed for first track only.
				track = (Track)itr1.next();
				sb.append(createTabs(2) + "<" + track.getAlbum().getIdentifier() + ">\n");
				sb.append(createTabs(3) + "<name>" + track.getAlbum().getName2() + "</name>\n");
			}
			
			//	Make sure the last album or author gets included.
			if (!itr1.hasNext()) {
				sb.append(createTabs(2) + "</" + track.getAlbum().getIdentifier() + ">\n");
			}
		}
	
		sb.append(createTabs(1) + "</" + author.getIdentifier() + ">\n");
		sb.append("</" + style.getIdentifier() + ">\n");
		
		return sb.toString();
	}
	
	/**
	 * Returns string containing XML markup of album specified.
	 * @param album
	 * @param author
	 * @param style
	 * @return xml of album
	 */
	public String albumToXML(Album album, Author author, Style style) {
		ArrayList styletracklist = style.getTracksRecursively();		// Retrieve all tracks of style of the album
		ArrayList<Track> albumtracklist = new ArrayList<Track>();
		Track track = null;
		StringBuffer sb = new StringBuffer();
		
		//	Retrieve all tracks from track list of style that are of the album. 
		ListIterator itr1 = styletracklist.listIterator();
		while (itr1.hasNext()) {
			track = (Track)itr1.next();
			if (track.getAlbum().getId().equals(album.getId())) {
				albumtracklist.add(track);
			}
		}
				
		sb.append("<" + style.getIdentifier() + ">\n");
		sb.append(createTabs(1) + "<name>" + style.getName2() + "</name>\n");
		sb.append(createTabs(1) + "<" + author.getIdentifier() + ">\n");
		sb.append(createTabs(2) + "<name>" + author.getName2() + "</name>\n");
		sb.append(createTabs(2) + "<" + album.getIdentifier() + ">\n");
		sb.append(createTabs(3) + "<name>" + album.getName2() + "</name>\n");
		
		itr1 = albumtracklist.listIterator();
		while (itr1.hasNext()) {
			if (itr1.hasPrevious()) {
				// Retreive track information.
				track = (Track)itr1.next();
				
				sb.append(createTabs(3) + "<" + track.getIdentifier() + ">\n");
				sb.append(createTabs(4) + "<name>" + track.getName() + "</name>\n");
				sb.append(createTabs(4) + "<length>" + track.getLength() + "</length>\n");
				sb.append(createTabs(4) + "<rate>" + track.getRate() + "</rate>\n");
				sb.append(createTabs(4) + "<comment>" + track.getValue(XML_TRACK_COMMENT) + "</comment>\n");
				sb.append(createTabs(4) + "<order>" + track.getValue(XML_TRACK_ORDER) + "</order>\n");
				sb.append(createTabs(3) + "</" + track.getIdentifier() + ">\n");		
			} else {
				// Executed only on first track in list.
				track = (Track)itr1.next();
				
				sb.append(createTabs(3) + "<" + track.getIdentifier() + ">\n");
				sb.append(createTabs(4) + "<name>" + track.getName() + "</name>\n");
				sb.append(createTabs(4) + "<length>" + track.getLength() + "</length>\n");
				sb.append(createTabs(4) + "<rate>" + track.getRate() + "</rate>\n");
				sb.append(createTabs(4) + "<comment>" + track.getValue(XML_TRACK_COMMENT) + "</comment>\n");
				sb.append(createTabs(4) + "<order>" + track.getValue(XML_TRACK_ORDER) + "</order>\n");
				sb.append(createTabs(3) + "</" + track.getIdentifier() + ">\n");		
			}
		}
		
		sb.append(createTabs(2) + "<" + album.getIdentifier() + ">\n");
		sb.append(createTabs(1) + "<" + author.getIdentifier() + ">\n");
		sb.append("<" + style.getIdentifier() + ">\n");
		
		return sb.toString();
	}
	
	// Automates tab creation based on level.
	private String createTabs(int level) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < level; i++) {
			sb.append("\t");
		}
		
		return sb.toString();
	}
	
	/**
	 * Writes sContent to sFileName.
	 * @param sFileName
	 * @param sContent
	 */
	public void commit(String sFileName, String sContent) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(sFileName));
			bw.write(sContent);
			bw.close();
		} catch (IOException e) {
			Log.error(e.toString());
		}
		
	}
}