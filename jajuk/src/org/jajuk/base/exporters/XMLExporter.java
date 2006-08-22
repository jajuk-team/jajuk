/*
 *  Jajuk
 *  Copyright (C) 2006 Ronak Patel
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.jajuk.base.exporters.Tag;
import org.jajuk.util.Util;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.base.Directory;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.DirectoryManager;

/**
 *  The class to export music contents to XML.
 *
 * @author     Ronak Patel
 * @created    Aug 20, 2006
 */
public class XMLExporter extends Exporter implements ITechnicalStrings {
	/** Public Constants */
	public static int PHYSICAL_COLLECTION = 0;
	public static int LOGICAL_GENRE_COLLECTION = 1;
	public static int LOGICAL_AUTHOR_COLLECTION = 2;
	public static int LOGICAL_ALBUM_COLLECTION = 3;
	
	/** Private Constants */
	private final static String NEWLINE = "\n";
	
	/** Keep an instance of the class. */
	private static XMLExporter xmlExporter = null;
	
	/**
	 * This methods returns an instance of XMLExporter.
	 * @return Returns an instance of XMLExporter.
	 */
	public static XMLExporter getInstance() {
		if (xmlExporter == null) {
			xmlExporter = new XMLExporter();
		}
		return xmlExporter;
	}
	
	/**
	 * Default private constructor.
	 *
	 */
	private XMLExporter() {
		super();
	}
	
	/** PUBLIC METHODS */
	
	/**
	 * This methods will create a tagging of a directory and all its children
	 * files and directories.
	 * @param directory The directory to start from.
	 * @return Returns a string containing the tagging.
	 */
	public String process(Directory directory) {
		String content = null;
		
		// Make sure we have a directory.
		if (directory != null) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
			
			sb.append(Tag.openTag(XML_DIRECTORY) + NEWLINE);
	
			String sName = Util.formatXML(directory.getName());
			String sPath = Util.formatXML(directory.getAbsolutePath());
			
			// Tag directory data.
			sb.append(addTabs(1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
			sb.append(addTabs(1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);			
			
			// Tag directory children data.
			ListIterator itr1 = directory.getDirectories().listIterator();
			while (itr1.hasNext()) {
				Directory d = (Directory)itr1.next();
					
				sb.append(exportDirectoryHelper(1,d));			
			}		
			
			// Tag directory file children data.
			Iterator itr2 = directory.getFiles().iterator();
			while (itr2.hasNext()) {
				org.jajuk.base.File file = (org.jajuk.base.File)itr2.next();
				
				sb.append(tagFile(file,1));
			}
			
			sb.append(Tag.closeTag(XML_DIRECTORY) + NEWLINE);
			
			content = sb.toString();
		}
		
		return content;
	}

	/**
	 * This methods will create a tagging of a device and all its children
	 * files and directories.
	 * @param device The device to start from.
	 * @return Returns a string containing the tagging.
	 */
	public String process(Device device) {
		String content = null;
		
		if (device != null) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
			
			sb.append(Tag.openTag(XML_DEVICE) + NEWLINE);
			
			sb.append(addTabs(1) + Tag.tagData(XML_NAME, Util.formatXML(device.getName())) + NEWLINE);
			sb.append(addTabs(1) + Tag.tagData(XML_TYPE, Util.formatXML(device.getDeviceTypeS())) + NEWLINE);
			sb.append(addTabs(1) + Tag.tagData(XML_URL, Util.formatXML(device.getUrl())) + NEWLINE);
			sb.append(addTabs(1) + Tag.tagData(XML_DEVICE_MOUNT_POINT, Util.formatXML(device.getMountPoint())) + NEWLINE);			
			
			synchronized (DirectoryManager.getInstance().getLock()) {
				// Tag children directories of device.
				ListIterator itr = DirectoryManager.getInstance().getDirectoryForIO(device.getFio()).getDirectories().listIterator();
				while (itr.hasNext()) {	
					Directory directory = (Directory)itr.next();
					sb.append(exportDirectoryHelper(1,directory));
				}
			}
			
			sb.append(Tag.closeTag(XML_DEVICE) + NEWLINE);
			
			content = sb.toString();
		}
		
		return content;
	}
	
	/**
	 * This method will take a constant specifying what type of collection to export. 
	 * @param EXPORTER_CONSTANT This XMLExporter constant specifies what type of collection we're exporting.
	 * @return Returns a string containing the tagging of the collection, null if no tagging was created.
	 */
	public String process(int EXPORTER_CONSTANT) {
		String content = null;		
		
		// If we are tagging the physical collection...
		if (EXPORTER_CONSTANT == XMLExporter.PHYSICAL_COLLECTION) {
			StringBuffer sb = new StringBuffer();
			sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
			sb.append(Tag.openTag(XML_COLLECTION) + NEWLINE);
			
			// Tag each device.
			synchronized (DeviceManager.getInstance().getLock()) {
				Iterator itr0 = DeviceManager.getInstance().getItems().iterator();
				while (itr0.hasNext()) {
					Device device = (Device)itr0.next();
					
					sb.append(addTabs(1) + Tag.openTag(XML_DEVICE) + NEWLINE);
					
					sb.append(addTabs(2) + Tag.tagData(XML_NAME, Util.formatXML(device.getName())) + NEWLINE);
					sb.append(addTabs(2) + Tag.tagData(XML_TYPE, Util.formatXML(device.getDeviceTypeS())) + NEWLINE);
					sb.append(addTabs(2) + Tag.tagData(XML_URL, Util.formatXML(device.getUrl())) + NEWLINE);
					sb.append(addTabs(2) + Tag.tagData(XML_DEVICE_MOUNT_POINT, Util.formatXML(device.getMountPoint())) + NEWLINE);			
					
					synchronized (DirectoryManager.getInstance().getLock()) {
						// Tag children directories of device.
						ListIterator itr1 = DirectoryManager.getInstance().getDirectoryForIO(device.getFio()).getDirectories().listIterator();
						while (itr1.hasNext()) {	
							Directory directory = (Directory)itr1.next();
							sb.append(exportDirectoryHelper(2,directory));
						}
						
						// Tag children files of device.
						Iterator itr2 = DirectoryManager.getInstance().getDirectoryForIO(device.getFio()).getFiles().iterator();
						while (itr2.hasNext()) {
							org.jajuk.base.File file = (org.jajuk.base.File)itr2.next();
							sb.append(tagFile(file,2));
						}
					}									
					
					sb.append(addTabs(1) + Tag.closeTag(XML_DEVICE) + NEWLINE);
				}
			}
			
			sb.append(Tag.closeTag(XML_COLLECTION) + NEWLINE);
			
			content = sb.toString();
		}
		
		return content;
	}
	
	/** PRIVATE HELPER METHODS */
	
	private String exportDirectoryHelper(int level, Directory directory) {
		StringBuffer sb = new StringBuffer();
		
		// Get the children
		ArrayList children = directory.getDirectories();
		ListIterator itr1 = children.listIterator();
		
		sb.append(addTabs(level) + Tag.openTag(XML_DIRECTORY) + NEWLINE);
		
		String sName = Util.formatXML(directory.getName());
		String sPath = Util.formatXML(directory.getAbsolutePath());
		
		// Tag directory data.
		sb.append(addTabs(level+1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
		sb.append(addTabs(level+1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);		
		
		// Tag children directories
		while (itr1.hasNext()) {
			Directory d = (Directory)itr1.next();
			
			sb.append(exportDirectoryHelper(level+1,d));
		}
		
		// Tag children files
		Iterator itr2 = directory.getFiles().iterator();
		while (itr2.hasNext()) {
			org.jajuk.base.File file = (org.jajuk.base.File)itr2.next();
			
			sb.append(tagFile(file,level+1));
		}
		
		sb.append(addTabs(level) + Tag.closeTag(XML_DIRECTORY) + NEWLINE);
		
		return sb.toString();
	}
	
	private String tagFile(org.jajuk.base.File file, int level) {
		StringBuffer sb = new StringBuffer();
		
		String sName = Util.formatXML(file.getName());
		String sPath = Util.formatXML(file.getAbsolutePath());
		long lSize = file.getSize();
		String sTrackName = Util.formatXML(file.getTrack().getName());
		String sTrackStyle = Util.formatXML(file.getTrack().getStyle().getName2());
		String sTrackAuthor = Util.formatXML(file.getTrack().getAuthor().getName2());
		String sTrackAlbum = Util.formatXML(file.getTrack().getAlbum().getName2());
		long lTrackLength = file.getTrack().getLength();
		long lTrackRate = file.getTrack().getRate();
		String sTrackComment = Util.formatXML(file.getTrack().getComment());
		long lTrackOrder = file.getTrack().getOrder();
		
		sb.append(addTabs(level) + Tag.openTag(XML_FILE) + NEWLINE);
		
		sb.append(addTabs(level+1) + Tag.tagData(XML_NAME,sName) + NEWLINE);
		sb.append(addTabs(level+1) + Tag.tagData(XML_PATH,sPath) + NEWLINE);
		sb.append(addTabs(level+1) + Tag.tagData(XML_SIZE,lSize) + NEWLINE);
		
		sb.append(addTabs(level+1) + Tag.openTag(XML_TRACK) + NEWLINE);
		
		sb.append(addTabs(level+2) + Tag.tagData(XML_TRACK_NAME, sTrackName) + NEWLINE);
		sb.append(addTabs(level+2) + Tag.tagData(XML_TRACK_STYLE, sTrackStyle) + NEWLINE);
		sb.append(addTabs(level+2) + Tag.tagData(XML_TRACK_AUTHOR, sTrackAuthor) + NEWLINE);
		sb.append(addTabs(level+2) + Tag.tagData(XML_TRACK_ALBUM, sTrackAlbum) + NEWLINE);
		sb.append(addTabs(level+2) + Tag.tagData(XML_TRACK_LENGTH, lTrackLength) + NEWLINE);
		sb.append(addTabs(level+2) + Tag.tagData(XML_TRACK_RATE, lTrackRate) + NEWLINE);
		sb.append(addTabs(level+2) + Tag.tagData(XML_TRACK_COMMENT, sTrackComment) + NEWLINE);
		sb.append(addTabs(level+2) + Tag.tagData(XML_TRACK_ORDER, lTrackOrder) + NEWLINE);
		
		sb.append(addTabs(level+1) + Tag.closeTag(XML_TRACK) + NEWLINE);
		
		sb.append(addTabs(level) + Tag.closeTag(XML_FILE) + NEWLINE);
		
		return sb.toString();
	}
	
	private String addTabs(int num) {
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < num) {
			sb.append('\t');
			i++;
		}
		return sb.toString();
	}
}
