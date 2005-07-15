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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Items root container
 * <p>
 * Singletton
 * 
 * @author Bertrand Florat 
 * @created 16 oct. 2003
 */
public class Collection extends DefaultHandler implements ITechnicalStrings, ErrorHandler,Serializable {
	/** Self instance */
	private static Collection collection;
	private static long lTime;
	
	/** Instance getter */
	public static synchronized Collection getInstance() {
		if (collection == null) {
			collection = new Collection();
		}
		return collection;
	}

	/** Hidden constructor */
	private Collection() {
	}

	/** Write current collection to collection file for persistence between sessions */
	public static void commit(String sFileName) throws IOException {
	    long lTime = System.currentTimeMillis();
        String sCharset = ConfigurationManager.getProperty(CONF_COLLECTION_CHARSET);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sFileName), sCharset)); //$NON-NLS-1$
		bw.write("<?xml version='1.0' encoding='"+sCharset+"'?>\n"); //$NON-NLS-1$ //$NON-NLS-2$
	    bw.write("<"+XML_COLLECTION+" "+XML_VERSION+"='"+JAJUK_VERSION+"'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //types
        bw.write(TypeManager.getInstance().toXML()); //$NON-NLS-1$
        Iterator it = TypeManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			Type type = (Type) it.next();
			bw.write(type.toXml());
		}
		bw.write("\t</"+XML_TYPES+">\n"); //$NON-NLS-1$
        //devices
        bw.write(DeviceManager.getInstance().toXML()); //$NON-NLS-1$
       it = DeviceManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			Device device = (Device) it.next();
			bw.write(device.toXml());
		}
		bw.write("\t</"+XML_DEVICES+">\n"); //$NON-NLS-1$
		//styles
        bw.write(StyleManager.getInstance().toXML()); //$NON-NLS-1$
		it = StyleManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			Style style = (Style) it.next();
			bw.write(style.toXml());
		}
		bw.write("\t</"+XML_STYLES+">\n"); //$NON-NLS-1$
		//authors
        bw.write(AuthorManager.getInstance().toXML()); //$NON-NLS-1$
		it = AuthorManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			Author author = (Author) it.next();
			bw.write(author.toXml());
		}
		bw.write("\t</"+XML_AUTHORS+">\n"); //$NON-NLS-1$
		//albums
		bw.write(AlbumManager.getInstance().toXML()); //$NON-NLS-1$
		it = AlbumManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			Album album = (Album) it.next();
			bw.write(album.toXml());
		}
		bw.write("\t</"+XML_ALBUMS+">\n"); //$NON-NLS-1$
		//tracks
        bw.write(TrackManager.getInstance().toXML()); //$NON-NLS-1$
		it = TrackManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			Track track = (Track) it.next();
			if (track.getFiles().size() > 0) { //this way we clean up all orphan tracks
				bw.write(track.toXml());
			}
		}
		bw.write("\t</"+XML_TRACKS+">\n"); //$NON-NLS-1$
		//directories
        bw.write(DirectoryManager.getInstance().toXML()); //$NON-NLS-1$
		it = DirectoryManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			Directory directory = (Directory) it.next();
			bw.write(directory.toXml());
		}
		bw.write("\t</"+XML_DIRECTORIES+">\n"); //$NON-NLS-1$
		//files
        bw.write(FileManager.getInstance().toXML()); //$NON-NLS-1$
		it = FileManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			org.jajuk.base.File file = (org.jajuk.base.File) it.next();
			bw.write(file.toXml());
		}
		bw.write("\t</"+XML_FILES+">\n"); //$NON-NLS-1$
		//playlist files
        bw.write(PlaylistFileManager.getInstance().toXML()); //$NON-NLS-1$
		it = PlaylistFileManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			PlaylistFile playlistFile = (PlaylistFile) it.next();
			bw.write(playlistFile.toXml());
		}
		bw.write("\t</"+XML_PLAYLIST_FILES+">\n"); //$NON-NLS-1$
		//playlist
        bw.write(PlaylistManager.getInstance().toXML()); //$NON-NLS-1$
		it = PlaylistManager.getInstance().getItems().iterator();
		while (it.hasNext()) {
			Playlist playlist = (Playlist) it.next();
			if (playlist.getPlaylistFiles().size() > 0) { //this way we clean up all orphan playlists
				bw.write(playlist.toXml());
			}
		}
		bw.write("\t</"+XML_PLAYLISTS+">\n"); //$NON-NLS-1$
		bw.write("</"+XML_COLLECTION+">\n"); //$NON-NLS-1$
		bw.flush();
		bw.close();
        Log.debug("Collection commited in "+(System.currentTimeMillis()-lTime)+" ms");//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Parse collection.xml file and put all collection information into memory
	 *  
	 */
	public static void load(String sFile) throws Exception {
	    lTime = System.currentTimeMillis();
	    //make sure to clean everything in memory
	    cleanup();
	    DeviceManager.getInstance().cleanAllDevices();
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setValidating(false);
	    spf.setNamespaceAware(false);
	    SAXParser saxParser = spf.newSAXParser();
	    File frt = new File(sFile);
        if (!frt.exists()){
            throw new JajukException("005"); //$NON-NLS-1$
        }
	    saxParser.parse(frt.toURL().toString(),getInstance());
	    //Sort collection
	    FileManager.getInstance().sortFiles();//resort collection in case of
	}

	/**
	 * Perform a collection clean up for logical items ( delete orphan data )
	 * 
	 * @return
	 */
	public static synchronized void cleanup() {
		//Tracks cleanup
		TrackManager.getInstance().cleanup();
		//Styles cleanup
		StyleManager.getInstance().cleanup();
		//Authors cleanup
		AuthorManager.getInstance().cleanup();
		//albums cleanup
		AlbumManager.getInstance().cleanup();
		//Playlists cleanup
		PlaylistManager.getInstance().cleanup();
	}

	
	/**
	 * parsing warning
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void warning(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("005") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * parsing error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void error(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("005") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * parsing fatal error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void fatalError(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("005") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * Called at parsing start
	 */
	public void startDocument() {
		Log.debug("Starting collection file parsing..."); //$NON-NLS-1$
	}

	/**
	 * Called at parsing end
	 */
	public void endDocument() {
		Log.debug("Collection file parsing done : " + (System.currentTimeMillis() - lTime) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Called when we start an element
	 *  
	 */
	public void startElement(String sUri, String sName, String sQName, Attributes attributes) throws SAXException {
	    try{
	        if (XML_DEVICE.equals(sQName)){
	            Device device = null;
	            device = DeviceManager.getInstance().registerDevice(attributes.getValue(0), attributes.getValue(1), Integer.parseInt(attributes.getValue(2)), attributes.getValue(3), attributes.getValue(4));
	            if (device != null){
	                device.populateProperties(attributes,5);
	            }
	        }
	        else if (XML_STYLE.equals(sQName)){
	            Style style = StyleManager.getInstance().registerStyle(attributes.getValue(0), attributes.getValue(1));
	            if (style != null){
	                style.populateProperties(attributes,2);
	            }
	        } 
	        else if (XML_AUTHOR.equals(sQName)){
	            Author author = AuthorManager.getInstance().registerAuthor(attributes.getValue(0), attributes.getValue(1));
	            if (author != null){
	                author.populateProperties(attributes,2);
	            }
	        }
	        else if (XML_ALBUM.equals(sQName)){
	            Album album = AlbumManager.getInstance().registerAlbum(attributes.getValue(0), attributes.getValue(1));
	            if (album != null){
	                album.populateProperties(attributes,2);	
	            }
	        }
	        else if (XML_TRACK.equals(sQName)){
	            String sId = attributes.getValue(0);
	            String sTrackName = attributes.getValue(1);
	            Album album = (Album)AlbumManager.getInstance().getItem(attributes.getValue(2));
	            Style style = (Style)StyleManager.getInstance().getItem(attributes.getValue(3));
	            Author author =(Author) AuthorManager.getInstance().getItem(attributes.getValue(4));
	            long length = Long.parseLong(attributes.getValue(5));
	            Type type = (Type)TypeManager.getInstance().getItem(attributes.getValue(6));
	            //more checkups
	            if (album == null || author == null || style == null || type == null){
	                return;
	            }
	            String sYear = attributes.getValue(7);
	            Track track = TrackManager.getInstance().registerTrack(sId, sTrackName, album, style, author, length, sYear, type);
	            track.setRate(Long.parseLong(attributes.getValue(8)));
	            track.setHits(Integer.parseInt(attributes.getValue(10)));
	            track.setAdditionDate(attributes.getValue(11));
	            track.setComment(attributes.getValue(12));
                track.setOrder(Integer.parseInt(attributes.getValue(13)));
                track.populateProperties(attributes,14);
	        }
	        else if (XML_DIRECTORY.equals(sQName)){
	            Directory dParent = null;
	            String sParentId = attributes.getValue(2);
	            if (!"-1".equals(sParentId)) { //$NON-NLS-1$
	                dParent = (Directory)DirectoryManager.getInstance().getItem(sParentId); //Parent directory should be already referenced because of order conservation
	                if (dParent == null){ //check directory is exists
	                    return;
	                }				
	            }
	            Device device = (Device)DeviceManager.getInstance().getItem(attributes.getValue(3));
	            if (device == null){ //check device exists
	                return;
	            }
	            Directory directory = DirectoryManager.getInstance().registerDirectory(attributes.getValue(0), attributes.getValue(1), dParent, device);
	            directory.populateProperties(attributes,4);
	        }
	        else if (XML_FILE.equals(sQName)){
	            Directory dParent = (Directory)DirectoryManager.getInstance().getItem(attributes.getValue(2));
	            Track track = (Track)TrackManager.getInstance().getItem(attributes.getValue(3));
	            if (dParent == null || track == null){ //more checkups
	                return;
	            }
	            long lSize = Long.parseLong(attributes.getValue(4));
	            org.jajuk.base.File file = FileManager.getInstance().registerFile(attributes.getValue(0), attributes.getValue(1), dParent, track, lSize, attributes.getValue(5));
	            file.populateProperties(attributes,6);
	            track.addFile(file);
	            file.getDirectory().addFile(file);
	        }
	        else if (XML_PLAYLIST_FILE.equals(sQName)){
	            Directory dParent = (Directory)DirectoryManager.getInstance().getItem(attributes.getValue(3));
	            if (dParent == null){ //check directory is exists
	                return;
	            }
	            PlaylistFile plf = PlaylistFileManager.getInstance().registerPlaylistFile(attributes.getValue(0), attributes.getValue(1), attributes.getValue(2), dParent);
	            if (plf != null){
	                plf.populateProperties(attributes,4);
	                dParent.addPlaylistFile(plf);
	            }
	        }
	        else if (XML_PLAYLIST.equals(sQName)){
	            StringTokenizer st = new StringTokenizer(attributes.getValue(1), ","); //playlist file list with ',' //$NON-NLS-1$
	            Playlist playlist = null;
	            if (st.hasMoreTokens()) { //if none mapped file, ignore it so it will be removed at next commit
	                do{
	                    PlaylistFile plFile = (PlaylistFile)PlaylistFileManager.getInstance().getItem((String) st.nextElement());
	                    if (plFile != null){
	                        playlist = PlaylistManager.getInstance().registerPlaylist(plFile);
	                    }
	                }
	                while (st.hasMoreTokens());
	                if ( playlist != null ){
	                    playlist.populateProperties(attributes,2);
	                }
	            }
	        }
	        else if (XML_TYPE.equals(sQName)){
	            String sId = attributes.getValue(0);
	            String sTypeName = attributes.getValue(1);
	            String sExtension = attributes.getValue(2);
	            String sPlayer = attributes.getValue(3);
	            String sTag = attributes.getValue(4);
	            if ("".equals(sTag)) { //$NON-NLS-1$
	                sTag = null;
	            }
	            Type type = TypeManager.getInstance().registerType(sId, sTypeName, sExtension, sPlayer, sTag);
	            if (type != null){
	                type.populateProperties(attributes,5);
	            }
	        }
            else if (XML_DEVICES.equals(sQName)){
                DeviceManager.getInstance().populateProperties(attributes);
            }
            else if (XML_ALBUMS.equals(sQName)){
                AlbumManager.getInstance().populateProperties(attributes);
            }
            else if (XML_AUTHORS.equals(sQName)){
                AuthorManager.getInstance().populateProperties(attributes);
            }
            else if (XML_DIRECTORIES.equals(sQName)){
                DirectoryManager.getInstance().populateProperties(attributes);
            }
            else if (XML_FILES.equals(sQName)){
                FileManager.getInstance().populateProperties(attributes);
            }
            else if (XML_PLAYLISTS.equals(sQName)){
                PlaylistManager.getInstance().populateProperties(attributes);
            }
            else if (XML_PLAYLIST_FILES.equals(sQName)){
                PlaylistFileManager.getInstance().populateProperties(attributes);
            }
            else if (XML_STYLES.equals(sQName)){
                StyleManager.getInstance().populateProperties(attributes);
            }
            else if (XML_TRACKS.equals(sQName)){
                TrackManager.getInstance().populateProperties(attributes);
            }
            else if (XML_TYPES.equals(sQName)){
                TypeManager.getInstance().populateProperties(attributes);
            }
	    }
	    catch(RuntimeException re){
	        String sAttributes = ""; //$NON-NLS-1$
	        for (int i=0;i<attributes.getLength();i++){
	            sAttributes += "\n"+attributes.getQName(i)+"="+attributes.getValue(i); //$NON-NLS-1$ //$NON-NLS-2$
	        }
	        Log.error("005",sAttributes,re); //$NON-NLS-1$
	        throw re;
	    }
	}
}
