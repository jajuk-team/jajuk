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

import org.jajuk.base.Album;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.PopulatedAlbum;
import org.jajuk.base.PopulatedAuthor;
import org.jajuk.base.PopulatedStyle;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

/**
 *  This class exports music contents to XML.
 *
 * @author     Ronak Patel
 * @created    Aug 20, 2006
 */
public class XMLExporter extends Exporter implements ITechnicalStrings {
    /** Public Constants */
    public static int PHYSICAL_COLLECTION = 0;
    public static int LOGICAL_GENRE_COLLECTION = 1;
    public static int LOGICAL_ARTIST_COLLECTION = 2;
    public static int LOGICAL_ALBUM_COLLECTION = 3;
    
    /** Private Constants */
    private final static String NEWLINE = "\n";
    private final static String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>";
    
    /** Keep an instance of the class. */
    private static XMLExporter self = null;
    
    /**
     * This methods returns an instance of XMLExporter.
     * @return Returns an instance of XMLExporter.
     */
    public static XMLExporter getInstance() {
        if (self == null) {
            self = new XMLExporter();
        }
        return self;
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
     * This method will create a tagging of the specified album and its tracks.
     * @param album The album to tag.
     * @return Returns a string containing the tagging, or null if an error occurred.
     */
    public String process(PopulatedAlbum album) {
        String content = null;
        
        // Make sure we have an album.
        if (album != null) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(XML_HEADER + NEWLINE);
            sb.append(tagAlbum(album,0));
            
            content = sb.toString();
        }
        
        return content;
    }
    
    /**
     * This method will create a tagging of the specified author and its albums
     * and associated tracks.
     * @param author The author to tag.
     * @return Returns a string containing the tagging, or null if an error occurred.
     */
    public String process(PopulatedAuthor author) {
        String content = null;
        
        if (author != null) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(XML_HEADER + NEWLINE);
            sb.append(tagAuthor(author,0));
            
            content = sb.toString();
        }
        
        return content;
    }
    
    /**
     * This method will create a tagging of the specified style.
     * @param style The style to tag.
     * @return Returns a string containing the tagging, or null is an error occurred.
     */
    public String process(PopulatedStyle style) {
        String content = null;
        
        if (style != null) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(XML_HEADER + NEWLINE);
            sb.append(tagStyle(style,0));
            
            content = sb.toString();
        }
        
        return content;
    }
    
    /**
     * This method will create a tagging of a directory and all its children
     * files and directories.
     * @param directory The directory to start from.
     * @return Returns a string containing the tagging, or null if an error occurred.
     */
    public String process(Directory directory) {
        String content = null;
        
        // Make sure we have a directory.
        if (directory != null) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(XML_HEADER + NEWLINE);
            
            sb.append(Tag.openTag(XML_DIRECTORY) + NEWLINE);
    
            String sName = Util.formatXML(directory.getName());
            String sPath = Util.formatXML(directory.getAbsolutePath());
            
            // Tag directory data.
            sb.append(addTabs(1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
            sb.append(addTabs(1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);         
            
            // Tag directory children data.
            ListIterator itr1 = new ArrayList(directory.getDirectories()).listIterator();
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
     * This method will create a tagging of a device and all its children
     * files and directories.
     * @param device The device to start from.
     * @return Returns a string containing the tagging, or null if an error occurred.
     */
    public String process(Device device) {
        String content = null;
        
        if (device != null) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(XML_HEADER + NEWLINE);
            
            sb.append(Tag.openTag(XML_DEVICE) + NEWLINE);
            
            sb.append(addTabs(1) + Tag.tagData(XML_NAME, Util.formatXML(device.getName())) + NEWLINE);
            sb.append(addTabs(1) + Tag.tagData(XML_TYPE, Util.formatXML(device.getDeviceTypeS())) + NEWLINE);
            sb.append(addTabs(1) + Tag.tagData(XML_URL, Util.formatXML(device.getUrl())) + NEWLINE);
            sb.append(addTabs(1) + Tag.tagData(XML_DEVICE_MOUNT_POINT, Util.formatXML(device.getMountPoint())) + NEWLINE);          
            
            ListIterator itr;
            // Tag children directories of device.
            synchronized (DirectoryManager.getInstance().getLock()) {
                itr = new ArrayList(
                    DirectoryManager.getInstance()
                    .getDirectoryForIO(device.getFio()).getDirectories())
                .listIterator();
            }               
            while (itr.hasNext()) { 
                Directory directory = (Directory)itr.next();
                sb.append(exportDirectoryHelper(1,directory));
            }
            
            Iterator itr2;
            // Tag children files of device.
            synchronized (DirectoryManager.getInstance().getLock()) {               
                itr2 = DirectoryManager.getInstance().getDirectoryForIO(device.getFio()).getFiles().iterator();
            }
            while (itr2.hasNext()) {    
                org.jajuk.base.File file = (org.jajuk.base.File)itr2.next();
                sb.append(tagFile(file,1));
            }   
            
            sb.append(Tag.closeTag(XML_DEVICE) + NEWLINE);
            
            content = sb.toString();
        }
        
        return content;
    }
    
    /**
     * This method will take a constant specifying what type of collection to export. 
     * @param COLLECTION_TYPE This XMLExporter constant specifies what type of collection we're exporting.
     * @param collection An ArrayList of the collection to export. Should be null if exporting the physical collection.
     *      Just specify the COLLECTION_TYPE.
     * @return Returns a string containing the tagging of the collection, null if no tagging was created.
     */
    public String processCollection(int COLLECTION_TYPE, ArrayList collection) {
        String content = null;      
        
        // If we are tagging the physical collection...
        if (COLLECTION_TYPE == XMLExporter.PHYSICAL_COLLECTION) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(XML_HEADER + NEWLINE);
            
            sb.append(Tag.openTag(XML_COLLECTION) + NEWLINE);
            
            Iterator<Device> itr0;
            // Tag each device.
            synchronized (DeviceManager.getInstance().getLock()) {
                itr0 = DeviceManager.getInstance().getDevices().iterator();
            }
            while (itr0.hasNext()) {
                Device device = itr0.next();
                
                sb.append(addTabs(1) + Tag.openTag(XML_DEVICE) + NEWLINE);
                
                sb.append(addTabs(2) + Tag.tagData(XML_NAME, Util.formatXML(device.getName())) + NEWLINE);
                sb.append(addTabs(2) + Tag.tagData(XML_TYPE, Util.formatXML(device.getDeviceTypeS())) + NEWLINE);
                sb.append(addTabs(2) + Tag.tagData(XML_URL, Util.formatXML(device.getUrl())) + NEWLINE);
                sb.append(addTabs(2) + Tag.tagData(XML_DEVICE_MOUNT_POINT, Util.formatXML(device.getMountPoint())) + NEWLINE);          
                
                
                ListIterator itr1;
                // Tag children directories of device.              
                synchronized (DirectoryManager.getInstance().getLock()) {                   
                    itr1 =  new ArrayList(DirectoryManager.getInstance()
                        .getDirectoryForIO(device.getFio())
                        .getDirectories()).listIterator();
                }
                while (itr1.hasNext()) {    
                    Directory directory = (Directory)itr1.next();
                    sb.append(exportDirectoryHelper(2,directory));
                }
            
                Iterator itr2;
                // Tag children files of device.
                synchronized (DirectoryManager.getInstance().getLock()) {   
                    itr2 = DirectoryManager.getInstance().getDirectoryForIO(device.getFio()).getFiles().iterator();
                }
                while (itr2.hasNext()) {
                    org.jajuk.base.File file = (org.jajuk.base.File)itr2.next();
                    sb.append(tagFile(file,2));
                }                                   
                
                sb.append(addTabs(1) + Tag.closeTag(XML_DEVICE) + NEWLINE);
            }   
            
            sb.append(Tag.closeTag(XML_COLLECTION) + NEWLINE);
            
            content = sb.toString();
        // Else if we are exporting the genre collection...
        } else if (COLLECTION_TYPE == XMLExporter.LOGICAL_GENRE_COLLECTION) {
            if (collection != null) {
                StringBuffer sb = new StringBuffer();
                
                sb.append(XML_HEADER + NEWLINE);
                
                sb.append(Tag.openTag(XML_COLLECTION) + NEWLINE);
                
                ListIterator itr = collection.listIterator();
                while (itr.hasNext()) {
                    PopulatedStyle style = (PopulatedStyle)itr.next();
                    sb.append(tagStyle(style,1));
                }
                
                sb.append(Tag.closeTag(XML_COLLECTION) + NEWLINE);
                
                content = sb.toString();
            }
        // Else if we are exporting the author collection...
        } else if (COLLECTION_TYPE == XMLExporter.LOGICAL_ARTIST_COLLECTION) {
            if (collection != null) {
                StringBuffer sb = new StringBuffer();
                
                sb.append(XML_HEADER + NEWLINE);
                
                sb.append(Tag.openTag(XML_COLLECTION) + NEWLINE);
                
                ListIterator itr = collection.listIterator();
                while (itr.hasNext()) {
                    PopulatedAuthor author = (PopulatedAuthor)itr.next();
                    sb.append(tagAuthor(author,1));
                }
                
                sb.append(Tag.closeTag(XML_COLLECTION) + NEWLINE);
                
                content = sb.toString();
            }
        // Else if we are exporting the album collection...
        } else if (COLLECTION_TYPE == XMLExporter.LOGICAL_ALBUM_COLLECTION) {
            if (collection != null) {
                StringBuffer sb = new StringBuffer();
                
                sb.append(XML_HEADER + NEWLINE);
                
                sb.append(Tag.openTag(XML_COLLECTION) + NEWLINE);
                
                ListIterator itr = collection.listIterator();
                while (itr.hasNext()) {
                    PopulatedAlbum album = (PopulatedAlbum)itr.next();
                    sb.append(tagAlbum(album,1));
                }
                
                sb.append(Tag.closeTag(XML_COLLECTION) + NEWLINE);
                
                content = sb.toString();
            }
        }
        
        return content;
    }
    
    /** PRIVATE HELPER METHODS */
    
    private String exportDirectoryHelper(int level, Directory directory) {
        StringBuffer sb = new StringBuffer();
        
        // Get the children
        ArrayList children = new ArrayList<Directory>(directory.getDirectories());
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
        String sTrackGenre = Util.formatXML(file.getTrack().getStyle().getName2());
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
        
        sb.append(tagTrack(file.getTrack(),level+1));
        
        sb.append(addTabs(level) + Tag.closeTag(XML_FILE) + NEWLINE);
        
        return sb.toString();
    }
    
    private String tagTrack(Track track, int level) {
        StringBuffer sb = new StringBuffer();
        
        String sTrackName = Util.formatXML(track.getName());
        String sTrackGenre = Util.formatXML(track.getStyle().getName2());
        String sTrackAuthor = Util.formatXML(track.getAuthor().getName2());
        String sTrackAlbum = Util.formatXML(track.getAlbum().getName2());
        long lTrackLength = track.getLength();
        long lTrackRate = track.getRate();
        String sTrackComment = Util.formatXML(track.getComment());
        long lTrackOrder = track.getOrder();
        
        sb.append(addTabs(level) + Tag.openTag(XML_TRACK) + NEWLINE);
        
        sb.append(addTabs(level+1) + Tag.tagData(XML_TRACK_NAME, sTrackName) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_TRACK_GENRE, sTrackGenre) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_TRACK_ARTIST, sTrackAuthor) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_TRACK_ALBUM, sTrackAlbum) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_TRACK_LENGTH, lTrackLength) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_TRACK_RATE, lTrackRate) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_TRACK_COMMENT, sTrackComment) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_TRACK_ORDER, lTrackOrder) + NEWLINE);
        
        sb.append(addTabs(level) + Tag.closeTag(XML_TRACK) + NEWLINE);
        
        return sb.toString();
    }
    
    private String tagAlbum(PopulatedAlbum album, int level) {
        StringBuffer sb = new StringBuffer();
        
        String sAlbumName = Util.formatXML(album.getAlbum().getName2());
        String sGenreName = "";
        String sAuthorName = "";
        
        if (album.getTracks() != null && !album.getTracks().isEmpty()) {
            sGenreName = Util.formatXML(album.getTracks().get(0).getStyle().getName2());
            sAuthorName = Util.formatXML(album.getTracks().get(0).getAuthor().getName2());
        }
        
        sb.append(addTabs(level) + Tag.openTag(XML_ALBUM) + NEWLINE);                       
        
        sb.append(addTabs(level+1) + Tag.tagData(XML_NAME, sAlbumName) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_ARTIST, sAuthorName) + NEWLINE);
        sb.append(addTabs(level+1) + Tag.tagData(XML_GENRE, sGenreName) + NEWLINE);
        
        ListIterator itr = album.getTracks().listIterator();
        while (itr.hasNext()) {
            Track track = (Track)itr.next();
            
            sb.append(tagTrack(track,level+1));
        }
        
        sb.append(addTabs(level) + Tag.closeTag(XML_ALBUM) + NEWLINE);
        
        return sb.toString();
    }
    
    private String tagAuthor(PopulatedAuthor author, int level) {
        StringBuffer sb = new StringBuffer();
        
        String sAuthorName = Util.formatXML(author.getAuthor().getName2());
        
        sb.append(addTabs(level) + Tag.openTag(XML_ARTIST) + NEWLINE);
        
        sb.append(addTabs(level+1) + Tag.tagData(XML_NAME, sAuthorName) + NEWLINE);
        
        ListIterator itr = author.getAlbums().listIterator();
        while (itr.hasNext()) {
            PopulatedAlbum album = (PopulatedAlbum)itr.next();
            
            sb.append(tagAlbum(album,level+1));
        }
        
        sb.append(addTabs(level) + Tag.closeTag(XML_ARTIST) + NEWLINE);
        
        return sb.toString();   
    }
    
    private String tagStyle(PopulatedStyle style, int level) {
        StringBuffer sb = new StringBuffer();
        
        String sStyleName = Util.formatXML(style.getStyle().getName2());
        
        sb.append(addTabs(level) + Tag.openTag(XML_STYLE) + NEWLINE);
        
        sb.append(addTabs(level+1) + Tag.tagData(XML_NAME,sStyleName) + NEWLINE);
        
        ListIterator itr = style.getAuthors().listIterator();
        while (itr.hasNext()) {
            PopulatedAuthor author = (PopulatedAuthor)itr.next();
            
            sb.append(tagAuthor(author,level+1));
        }
        
        sb.append(addTabs(level) + Tag.closeTag(XML_STYLE) + NEWLINE);
        
        return sb.toString();
    }
    
    private String getAuthorOfAlbum(Album album) {
        String sAuthorName = null;
        Iterator<Track> itr;
        synchronized (TrackManager.getInstance().getLock()) {
            itr = TrackManager.getInstance().getTracks().iterator();
        }
        while (itr.hasNext()) {
            Track track = itr.next();
            
            if (track.getAlbum().getId().equals(album.getId())) {
                sAuthorName = track.getAuthor().getName2();
                break;
            }
        }   
        
        return sAuthorName;
    }
    
    private String getStyleOfAlbum(Album album) {
        String sStyleName = null;
        Iterator<Track> itr;
        synchronized (TrackManager.getInstance().getLock()) {
            itr = TrackManager.getInstance().getTracks().iterator();
        }
        while (itr.hasNext()) {
            Track track = itr.next();
            
            if (track.getAlbum().getId().equals(album.getId())) {
                sStyleName = track.getStyle().getName2();
                break;
            }
        }
        
        return sStyleName;
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

/**
 *  This class will create taggings. It will create either
 *  open tags, closed tags, or full tagging with data.
 *
 * @author     Ronak Patel
 * @created    Aug 20, 2006
 */
class Tag {
    public static String openTag(String tagname) {
        return "<"+tagname+">";
    }
    
    public static String closeTag(String tagname) {
        return "</"+tagname+">";
    }
    
    public static String tagData(String tagname, String data) {
        return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
    }
    
    public static String tagData(String tagname, long data) {
        return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
    }
    
    public static String tagData(String tagname, int data) {
        return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
    }
    
    public static String tagData(String tagname, double data) {
        return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
    }
}
