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
 * USA. Revision 1.6  2003/10/31 13:05:06  bflorat
 * USA. 31/10/2003
 * USA.
 */
package org.jajuk.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
	/** Modification flag, if false, the XML output file is not writted again on the disk */
	private static boolean bModified = false;
	private long lTime;
	
	
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
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_COLLECTION),"UTF-8"));
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
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			XMLReader xmlr;
			SAXParser saxParser = spf.newSAXParser();
			xmlr = saxParser.getXMLReader();
			xmlr.setContentHandler(getInstance());
			xmlr.setErrorHandler(getInstance());
			File frt = new File(FILE_COLLECTION);
			xmlr.parse(frt.toURL().toString());
		} catch (Exception e) {
			Log.error(e);
			throw new JajukException("005");
		}
	}

	/**
	 * Perform a collection clean up ( delete orphan data )
	 * 
	 * @return
	 */
	public static void cleanup() {
		//Styles cleanup
		ArrayList alStyles = StyleManager.getStyles();
		Iterator itStyles;
		Iterator itTracks;
		ArrayList alTrack = TrackManager.getTracks();
		itStyles = alStyles.iterator();
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
				StyleManager.remove(style.getId());
			}
		}

		// Authors cleanup
		ArrayList alAuthors = AuthorManager.getAuthors();
		Iterator itAuthors;
		itAuthors = alAuthors.iterator();
		while (itAuthors.hasNext()) {
			Author style = (Author) itAuthors.next();
			boolean bUsed = false;
			itTracks = alTrack.iterator();
			while (itTracks.hasNext()) {
				Track track = (Track) itTracks.next();
				if (track.getAuthor().equals(style)) {
					bUsed = true;
				}
			}
			if (!bUsed) { //clean this author
				AuthorManager.remove(style.getId());
			}
		}

	}

	/**
	 * @return
	 */
	public static boolean isModified() {
		return bModified;
	}

	/**
	 * @param b
	 */
	public static void setModified(boolean b) {
		bModified = b;
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
		lTime = System.currentTimeMillis();
		Log.debug("Starting collection file parsing...");
	}
	
	/**
		 * Called at parsing end
		 */
		public void endDocument() {
			lTime = System.currentTimeMillis();
			Log.debug("Collection file parsing done : "+(System.currentTimeMillis()-lTime)+" ms");
		}

	/**
	 * Called when we start an element
	
 <types>
  <type id='1' name='mp3' extension='mp3'/>
  <type id='2' name='playlist' extension='m3u'/>
  <type id='3' name='ogg vorbis' extension='ogg'/>
 </types>

 <devices>
  <device id='2' name='cd jazz' type='directory' url='/home/foo/music'  myproperty_on_device='1234'/>
 </devices>

 <styles>
  <style id='0' name='ROCK' myproperty_on_style='foo'/>
 </styles>

 <authors>
  <author id='4' name='my author' myproperty_on_author='false'/>
 </authors>

 <albums>
  <album id='12' name='my rock album'  myproperty='good'/>
 </albums>
 <tracks>
  <track id='162' name='my rock track'  album='12' style='0' author='4' 
   length='345' year='2004' rate='10' type='1' files='2,56,566' hits='2' 
   added='20041203' myproperty_on_track='good'/>
 </tracks>
 <directories>
  <directory id='1' name='rock' parent='-1' device='2' myproperty_on_directory='foo'/>
  <directory id='3' name='MyAuthor' parent='1' myproperty_on_directory='1234'/>
 </directories>

 <files>
  <file id='56'  name='mytrack.mp3' directory='3' track='162' 
  size='342435' quality='192'  myproperty_on_file='good'/>
 </files>

	 *
	 *  */
	public void startElement(String sUri, String sName, String sQName, Attributes attributes) throws SAXException {
		if (sQName.equals(XML_DEVICE)) { //device case
			DeviceManager.registerDevice(attributes.getValue(0), attributes.getValue(1), attributes.getValue(2),attributes.getValue(3));
		}
		else if (sQName.equals(XML_STYLE)) { 
					StyleManager.registerStyle(attributes.getValue(0), attributes.getValue(1));
		}
		else if (sQName.equals(XML_AUTHOR)) { 
					AuthorManager.registerAuthor(attributes.getValue(0), attributes.getValue(1));
		}
		else if (sQName.equals(XML_ALBUM)) { 
			AlbumManager.registerAlbum(attributes.getValue(0), attributes.getValue(1));
		}
		/*<track id='162' name='my rock track'  album='12' style='0' author='4' 
   length='345' year='2004' rate='10' type='1' files='2,56,566' hits='2' 
   added='20041203' myproperty_on_track='good'/>*/
		
		else if (sQName.equals(XML_TRACK)) { 
			String sId = attributes.getValue(0);
			String sTrackName = attributes.getValue(1);
			Album album = AlbumManager.getAlbum(attributes.getValue(2));
			Style style = StyleManager.getStyle(attributes.getValue(3));
			Author author = AuthorManager.getAuthor(attributes.getValue(4));
			long length = Long.parseLong(attributes.getValue(5));
			String sYear = attributes.getValue(6);
			Type type = TypeManager.getType(attributes.getValue(8));
			Track track = TrackManager.registerTrack(sId,sTrackName,album,style,author,length,sYear,type);
			track.setRate(Long.parseLong(attributes.getValue(7)));
			track.setHits(Integer.parseInt(attributes.getValue(9)));
			track.setAdditionDate(attributes.getValue(10));
		}
		else if (sQName.equals(XML_DIRECTORY)) { 
			Directory dParent = null;
			String sParentId = attributes.getValue(2);
			if (!sParentId.equals("-1")){
				dParent = DirectoryManager.getDirectory(sParentId); //We know the parent directory is already referenced because of order conservation	
			}
			Device device = DeviceManager.getDevice(attributes.getValue(3));
			DirectoryManager.registerDirectory(attributes.getValue(0), attributes.getValue(1), dParent,device);
		}
		else if (sQName.equals(XML_FILE)) { 
			Directory dParent = DirectoryManager.getDirectory(attributes.getValue(2)); 
			Track track = TrackManager.getTrack(attributes.getValue(3)); 
			long lSize = Long.parseLong(attributes.getValue(4));
			org.jajuk.base.File file = FileManager.registerFile(attributes.getValue(0), attributes.getValue(1), dParent,track,lSize,attributes.getValue(5));
			track.addFile(file);
		}
		else if (sQName.equals(XML_PLAYLIST_FILE)) { 
			Directory dParent = DirectoryManager.getDirectory(attributes.getValue(3));
			PlaylistFileManager.registerPlaylistFile(attributes.getValue(0), attributes.getValue(1), attributes.getValue(2),dParent);
		}
		else if (sQName.equals(XML_PLAYLIST)) { 
			StringTokenizer st = new StringTokenizer(attributes.getValue(1),","); //playlist file list with ','
			if (st.hasMoreTokens()){
				PlaylistFile plFile = PlaylistFileManager.getPlaylistFile((String)st.nextElement());	
				Playlist playlist = PlaylistManager.registerPlaylist(attributes.getValue(0), plFile);
				while (st.hasMoreTokens()){
					playlist.addFile(PlaylistFileManager.getPlaylistFile((String)st.nextElement()));
				}
			}
		}
		else if (sQName.equals(XML_TYPE)) { 
			String sId = 	attributes.getValue(0);
			String sTypeName = attributes.getValue(1);
			String sExtension = attributes.getValue(2);
			String sPlayer= attributes.getValue(3);
			String sTag = attributes.getValue(4);
			if (sTag.equals("")){
				sTag = null;
			}
			boolean bIsMusic = Boolean.valueOf(attributes.getValue(5)).booleanValue();
			TypeManager.registerType(sId,sTypeName,sExtension,sPlayer,sTag,bIsMusic);
		}
	}

	/**
	 * Called when we reach the end of an element
	 */
	public void endElement(String sUri, String sName, String sQName) throws SAXException {

	}

}
