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
 * @author bflorat 
 * @created 16 oct. 2003
 */
public class Collection extends DefaultHandler implements ITechnicalStrings, ErrorHandler,Serializable {
	/** Self instance */
	private static Collection collection;
	private static long lTime;
	/* XML tags hashcodes */
	private static final int HASHCODE_TYPE = 3575610;
	private static final int HASHCODE_DEVICE =  -1335157162;
	private static final int HASHCODE_STYLE = 109780401;
	private static final int HASHCODE_AUTHOR = -1406328437;
	private static final int HASHCODE_ALBUM = 92896879;
	private static final int HASHCODE_TRACK = 110621003;
	private static final int HASHCODE_DIRECTORY =  -962584979;
	private static final int HASHCODE_FILE = 3143036;
	private static final int HASHCODE_PLAYLIST_FILE = 816218057;
	private static final int HASHCODE_PLAYLIST = 1879474642;
	

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
	public static void commit() throws IOException {
	    String sCharset = ConfigurationManager.getProperty(CONF_COLLECTION_CHARSET);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_COLLECTION), sCharset)); //$NON-NLS-1$
		bw.write("<?xml version='1.0' encoding='"+sCharset+"'?>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		bw.write("<collection jajuk_version='"+JAJUK_VERSION+"'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		//types
		bw.write("\t<types>\n"); //$NON-NLS-1$
		Iterator it = TypeManager.getTypes().iterator();
		while (it.hasNext()) {
			Type type = (Type) it.next();
			bw.write(type.toXml());
		}
		bw.write("\t</types>\n"); //$NON-NLS-1$
		//devices
		bw.write("\t<devices>\n"); //$NON-NLS-1$
		it = DeviceManager.getDevices().iterator();
		while (it.hasNext()) {
			Device device = (Device) it.next();
			bw.write(device.toXml());
		}
		bw.write("\t</devices>\n"); //$NON-NLS-1$
		//styles
		bw.write("\t<styles>\n"); //$NON-NLS-1$
		it = StyleManager.getStyles().iterator();
		while (it.hasNext()) {
			Style style = (Style) it.next();
			bw.write(style.toXml());
		}
		bw.write("\t</styles>\n"); //$NON-NLS-1$
		//authors
		bw.write("\t<authors>\n"); //$NON-NLS-1$
		it = AuthorManager.getAuthors().iterator();
		while (it.hasNext()) {
			Author author = (Author) it.next();
			bw.write(author.toXml());
		}
		bw.write("\t</authors>\n"); //$NON-NLS-1$
		//albums
		bw.write("\t<albums>\n"); //$NON-NLS-1$
		it = AlbumManager.getAlbums().iterator();
		while (it.hasNext()) {
			Album album = (Album) it.next();
			bw.write(album.toXml());
		}
		bw.write("\t</albums>\n"); //$NON-NLS-1$
		//tracks
		bw.write("\t<tracks>\n"); //$NON-NLS-1$
		it = TrackManager.getTracks().iterator();
		while (it.hasNext()) {
			Track track = (Track) it.next();
			if (track.getFiles().size() > 0) { //this way we clean up all orphan tracks
				bw.write(track.toXml());
			}
		}
		bw.write("\t</tracks>\n"); //$NON-NLS-1$
		//directories
		bw.write("\t<directories>\n"); //$NON-NLS-1$
		it = DirectoryManager.getDirectories().iterator();
		while (it.hasNext()) {
			Directory directory = (Directory) it.next();
			bw.write(directory.toXml());
		}
		bw.write("\t</directories>\n"); //$NON-NLS-1$
		//files
		bw.write("\t<files>\n"); //$NON-NLS-1$
		it = FileManager.getFiles().iterator();
		while (it.hasNext()) {
			org.jajuk.base.File file = (org.jajuk.base.File) it.next();
			bw.write(file.toXml());
		}
		bw.write("\t</files>\n"); //$NON-NLS-1$
		//playlist files
		bw.write("\t<playlist_files>\n"); //$NON-NLS-1$
		it = PlaylistFileManager.getPlaylistFiles().iterator();
		while (it.hasNext()) {
			PlaylistFile playlistFile = (PlaylistFile) it.next();
			bw.write(playlistFile.toXml());
		}
		bw.write("\t</playlist_files>\n"); //$NON-NLS-1$
		//playlist
		bw.write("\t<playlists>\n"); //$NON-NLS-1$
		it = PlaylistManager.getPlaylists().iterator();
		while (it.hasNext()) {
			Playlist playlist = (Playlist) it.next();
			if (playlist.getPlaylistFiles().size() > 0) { //this way we clean up all orphan playlists
				bw.write(playlist.toXml());
			}
		}
		bw.write("\t</playlists>\n"); //$NON-NLS-1$
		bw.write("</collection>\n"); //$NON-NLS-1$
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
			spf.setNamespaceAware(false);
			SAXParser saxParser = spf.newSAXParser();
			File frt = new File(FILE_COLLECTION);
			saxParser.parse(frt.toURL().toString(),getInstance());
            //Sort collection
    		FileManager.sortFiles();//resort collection in case of
		} catch (Exception e) {
			Log.error(e);
			throw new JajukException("005"); //$NON-NLS-1$
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
	    switch(sQName.hashCode()){
			case HASHCODE_DEVICE : //device case
			    Device device = null;
			    device = DeviceManager.registerDevice(attributes.getValue(0), attributes.getValue(1), Integer.parseInt(attributes.getValue(2)), attributes.getValue(3), attributes.getValue(4));
			    device.populateProperties(attributes, 5);
			break;
			case HASHCODE_STYLE :
				Style style = StyleManager.registerStyle(attributes.getValue(0), attributes.getValue(1));
				style.populateProperties(attributes, 2);
			break; 
			case HASHCODE_AUTHOR: 
				Author author = AuthorManager.registerAuthor(attributes.getValue(0), attributes.getValue(1));
				author.populateProperties(attributes, 2);
			break;
			case HASHCODE_ALBUM:
				Album album = AlbumManager.registerAlbum(attributes.getValue(0), attributes.getValue(1));
				album.populateProperties(attributes, 2);
			break;
			case HASHCODE_TRACK :
				String sId = attributes.getValue(0);
				String sTrackName = attributes.getValue(1);
				album = AlbumManager.getAlbum(attributes.getValue(2));
				style = StyleManager.getStyle(attributes.getValue(3));
				author = AuthorManager.getAuthor(attributes.getValue(4));
				long length = Long.parseLong(attributes.getValue(5));
				Type type = TypeManager.getType(attributes.getValue(6));
				String sYear = attributes.getValue(7);
				Track track = TrackManager.registerTrack(sId, sTrackName, album, style, author, length, sYear, type);
				track.setRate(Long.parseLong(attributes.getValue(8)));
				track.setHits(Integer.parseInt(attributes.getValue(10)));
				track.setAdditionDate(attributes.getValue(11));
				track.populateProperties(attributes, 12);
			break;
			case HASHCODE_DIRECTORY:
				Directory dParent = null;
				String sParentId = attributes.getValue(2);
				if (!"-1".equals(sParentId)) { //$NON-NLS-1$
					dParent = DirectoryManager.getDirectory(sParentId); //We know the parent directory is already referenced because of order conservation
					if (dParent == null){ //check directory is exists
					    break;
					}				
				}
				device = DeviceManager.getDevice(attributes.getValue(3));
				if (device == null){ //check device exists
				    break;
				}
				Directory directory = DirectoryManager.registerDirectory(attributes.getValue(0), attributes.getValue(1), dParent, device);
				directory.populateProperties(attributes, 4);
			break;
			case HASHCODE_FILE:
			    dParent = DirectoryManager.getDirectory(attributes.getValue(2));
				if (dParent == null){ //check directory is exists
				    break;
				}
			    track = TrackManager.getTrack(attributes.getValue(3));
				long lSize = Long.parseLong(attributes.getValue(4));
				org.jajuk.base.File file = FileManager.registerFile(attributes.getValue(0), attributes.getValue(1), dParent, track, lSize, attributes.getValue(5));
				file.populateProperties(attributes, 6);
				track.addFile(file);
				file.getDirectory().addFile(file);
			break;
			case HASHCODE_PLAYLIST_FILE:
				dParent = DirectoryManager.getDirectory(attributes.getValue(3));
				if (dParent == null){ //check directory is exists
				    break;
				}
				PlaylistFile plf = PlaylistFileManager.registerPlaylistFile(attributes.getValue(0), attributes.getValue(1), attributes.getValue(2), dParent);
				plf.populateProperties(attributes, 4);
			break;
			case HASHCODE_PLAYLIST:
				StringTokenizer st = new StringTokenizer(attributes.getValue(1), ","); //playlist file list with ',' //$NON-NLS-1$
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
			break;
			case HASHCODE_TYPE:
				sId = attributes.getValue(0);
				String sTypeName = attributes.getValue(1);
				String sExtension = attributes.getValue(2);
				String sPlayer = attributes.getValue(3);
				String sTag = attributes.getValue(4);
				if ("".equals(sTag)) { //$NON-NLS-1$
					sTag = null;
				}
				type = TypeManager.registerType(sId, sTypeName, sExtension, sPlayer, sTag);
				type.populateProperties(attributes, 5);
			break;	
		}
	}
}
