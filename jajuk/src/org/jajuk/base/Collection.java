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
 * USA. 
 * $Revision$
 */
package org.jajuk.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Items root container
 * <p>
 * Singletton
 * 
 * @author bflorat @created 16 oct. 2003
 */
public class Collection extends DefaultHandler implements ITechnicalStrings, ErrorHandler {
	/** Self instance */
	private static Collection collection;
	private static long lTime;

	/** Instance getter */
	public static Collection getInstance() {
		if (collection == null) {
			collection = new Collection();
		}
		return collection;
	}

	/** Hidden constructor */
	private Collection() {
	}

	/** Write current collection to collection file for persistence between sessions */
	public static void commit() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_COLLECTION), "UTF-8"));
		bw.write("<?xml version='1.0' encoding='UTF-8'?>\n");
		bw.write("<collection>\n");
		//types
		bw.write("\t<types>\n");
		Iterator it = TypeManager.getTypes().iterator();
		while (it.hasNext()) {
			Type type = (Type) it.next();
			bw.write(type.toXml());
		}
		bw.write("\t</types>\n");
		//devices
		bw.write("\t<devices>\n");
		it = DeviceManager.getDevices().iterator();
		while (it.hasNext()) {
			Device device = (Device) it.next();
			bw.write(device.toXml());
		}
		bw.write("\t</devices>\n");
		//styles
		bw.write("\t<styles>\n");
		it = StyleManager.getStyles().iterator();
		while (it.hasNext()) {
			Style style = (Style) it.next();
			bw.write(style.toXml());
		}
		bw.write("\t</styles>\n");
		//authors
		bw.write("\t<authors>\n");
		it = AuthorManager.getAuthors().iterator();
		while (it.hasNext()) {
			Author author = (Author) it.next();
			bw.write(author.toXml());
		}
		bw.write("\t</authors>\n");
		//albums
		bw.write("\t<albums>\n");
		it = AlbumManager.getAlbums().iterator();
		while (it.hasNext()) {
			Album album = (Album) it.next();
			bw.write(album.toXml());
		}
		bw.write("\t</albums>\n");
		//tracks
		bw.write("\t<tracks>\n");
		it = TrackManager.getTracks().iterator();
		while (it.hasNext()) {
			Track track = (Track) it.next();
			if (track.getFiles().size() > 0) { //this way we clean up all orphan tracks
				bw.write(track.toXml());
			}
		}
		bw.write("\t</tracks>\n");
		//directories
		bw.write("\t<directories>\n");
		it = DirectoryManager.getDirectories().iterator();
		while (it.hasNext()) {
			Directory directory = (Directory) it.next();
			bw.write(directory.toXml());
		}
		bw.write("\t</directories>\n");
		//files
		bw.write("\t<files>\n");
		it = FileManager.getFiles().iterator();
		while (it.hasNext()) {
			org.jajuk.base.File file = (org.jajuk.base.File) it.next();
			bw.write(file.toXml());
		}
		bw.write("\t</files>\n");
		//playlist files
		bw.write("\t<playlist_files>\n");
		it = PlaylistFileManager.getPlaylistFiles().iterator();
		while (it.hasNext()) {
			PlaylistFile playlistFile = (PlaylistFile) it.next();
			bw.write(playlistFile.toXml());
		}
		bw.write("\t</playlist_files>\n");
		//playlist
		bw.write("\t<playlists>\n");
		it = PlaylistManager.getPlaylists().iterator();
		while (it.hasNext()) {
			Playlist playlist = (Playlist) it.next();
			if (playlist.getFiles().size() > 0) { //this way we clean up all orphan playlists
				bw.write(playlist.toXml());
			}
		}
		bw.write("\t</playlists>\n");
		bw.write("</collection>\n");
		bw.flush();
		bw.close();
	}

	/**
	 * Parse collection.xml file and put all collection information into memory
	 *  
	 */
	public static void load() throws JajukException {
		try {
			lTime = System.currentTimeMillis();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			SAXParser saxParser = spf.newSAXParser();
			File frt = new File(FILE_COLLECTION);
			saxParser.parse(frt.toURL().toString(),getInstance());
		} catch (Exception e) {
			Log.error(e);
			throw new JajukException("005");
		}
	}

	/**
	 * Perform a collection clean up for logical items ( delete orphan data )
	 * 
	 * @return
	 */
	public static synchronized void cleanup() {
		//	Tracks cleanup
		TrackManager.cleanup();
		//Styles cleanup
		StyleManager.cleanup();
		// Authors cleanup
		AuthorManager.cleanup();
		//albums cleanup
		AlbumManager.cleanup();
		//Playlists cleanup
		PlaylistManager.cleanup();
		System.gc(); //force garbage collection after cleanup

	}

	
	/**
	 * parsing warning
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void warning(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("004") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * parsing error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void error(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("005") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * parsing fatal error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void fatalError(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("006") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * Called at parsing start
	 */
	public void startDocument() {
		Log.debug("Starting collection file parsing...");
	}

	/**
	 * Called at parsing end
	 */
	public void endDocument() {
		Log.debug("Collection file parsing done : " + (System.currentTimeMillis() - lTime) + " ms");
	}

	/**
	 * Called when we start an element
	 *  
	 */
	public void startElement(String sUri, String sName, String sQName, Attributes attributes) throws SAXException {
		if (sQName.equals(XML_DEVICE)) { //device case
			Device device = DeviceManager.registerDevice(attributes.getValue(0), attributes.getValue(1), Integer.parseInt(attributes.getValue(2)), attributes.getValue(3), attributes.getValue(4));
			device.populateProperties(attributes, 5);
		} else if (sQName.equals(XML_STYLE)) {
			Style style = StyleManager.registerStyle(attributes.getValue(0), attributes.getValue(1));
			style.populateProperties(attributes, 2);
		} else if (sQName.equals(XML_AUTHOR)) {
			Author author = AuthorManager.registerAuthor(attributes.getValue(0), attributes.getValue(1));
			author.populateProperties(attributes, 2);
		} else if (sQName.equals(XML_ALBUM)) {
			Album album = AlbumManager.registerAlbum(attributes.getValue(0), attributes.getValue(1));
			album.populateProperties(attributes, 2);
		} else if (sQName.equals(XML_TRACK)) {
			String sId = attributes.getValue(attributes.getIndex(XML_ID));
			String sTrackName = attributes.getValue(attributes.getIndex(XML_TRACK_NAME));
			Album album = AlbumManager.getAlbum(attributes.getValue(attributes.getIndex(XML_TRACK_ALBUM)));
			Style style = StyleManager.getStyle(attributes.getValue(attributes.getIndex(XML_TRACK_STYLE)));
			Author author = AuthorManager.getAuthor(attributes.getValue(attributes.getIndex(XML_TRACK_AUTHOR)));
			long length = Long.parseLong(attributes.getValue(attributes.getIndex(XML_TRACK_LENGTH)));
			String sYear = attributes.getValue(attributes.getIndex(XML_TRACK_YEAR));
			Type type = TypeManager.getType(attributes.getValue(attributes.getIndex(XML_TRACK_TYPE)));
			Track track = TrackManager.registerTrack(sId, sTrackName, album, style, author, length, sYear, type);
			track.setRate(Long.parseLong(attributes.getValue(attributes.getIndex(XML_TRACK_RATE))));
			track.setHits(Integer.parseInt(attributes.getValue(attributes.getIndex(XML_TRACK_HITS))));
			track.setAdditionDate(attributes.getValue(attributes.getIndex(XML_TRACK_ADDED)));
			track.populateProperties(attributes, 12);
		} else if (sQName.equals(XML_DIRECTORY)) {
			Directory dParent = null;
			String sParentId = attributes.getValue(2);
			if (!sParentId.equals("-1")) {
				dParent = DirectoryManager.getDirectory(sParentId); //We know the parent directory is already referenced because of order conservation
			}
			Device device = DeviceManager.getDevice(attributes.getValue(3));
			Directory directory = DirectoryManager.registerDirectory(attributes.getValue(0), attributes.getValue(1), dParent, device);
			directory.populateProperties(attributes, 4);
		} else if (sQName.equals(XML_FILE)) {
			Directory dParent = DirectoryManager.getDirectory(attributes.getValue(2));
			Track track = TrackManager.getTrack(attributes.getValue(3));
			long lSize = Long.parseLong(attributes.getValue(4));
			org.jajuk.base.File file = FileManager.registerFile(attributes.getValue(0), attributes.getValue(1), dParent, track, lSize, attributes.getValue(5));
			file.populateProperties(attributes, 6);
			track.addFile(file);
		} else if (sQName.equals(XML_PLAYLIST_FILE)) {
			Directory dParent = DirectoryManager.getDirectory(attributes.getValue(3));
			PlaylistFile plf = PlaylistFileManager.registerPlaylistFile(attributes.getValue(0), attributes.getValue(1), attributes.getValue(2), dParent);
			plf.populateProperties(attributes, 4);
		} else if (sQName.equals(XML_PLAYLIST)) {
			StringTokenizer st = new StringTokenizer(attributes.getValue(1), ","); //playlist file list with ','
			Playlist playlist = null;
			if (st.hasMoreTokens()) { //if none mapped file, ignore it so it will be removed at next commit
				do{
					PlaylistFile plFile = PlaylistFileManager.getPlaylistFile((String) st.nextElement());
					if (plFile != null){
						PlaylistManager.registerPlaylist(plFile);
					}
				}
				while (st.hasMoreTokens());
				if ( playlist != null ) playlist.populateProperties(attributes, 2);
			}
		} else if (sQName.equals(XML_TYPE)) {
			String sId = attributes.getValue(0);
			String sTypeName = attributes.getValue(1);
			String sExtension = attributes.getValue(2);
			String sPlayer = attributes.getValue(3);
			String sTag = attributes.getValue(4);
			if (sTag.equals("")) {
				sTag = null;
			}
			boolean bIsMusic = Boolean.valueOf(attributes.getValue(5)).booleanValue();
			Type type = TypeManager.registerType(sId, sTypeName, sExtension, sPlayer, sTag, bIsMusic);
			type.populateProperties(attributes, 6);
		}
	}

	/**
	 * Called when we reach the end of an element
	 */
	public void endElement(String sUri, String sName, String sQName) throws SAXException {

	}

}
