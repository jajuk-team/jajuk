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
 * $Log$
 * Revision 1.3  2003/10/21 20:43:06  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 */
package org.jajuk.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

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
 *  Items root container
 *<p> Singletton
 * @author     bflorat
 * @created    16 oct. 2003
 */
public class Collection extends DefaultHandler implements ITechnicalStrings,ErrorHandler {
	/**Self instance*/
	private static Collection collection;
	/**Modification flag, if false, the XML output file is not writted again on the disk*/
	private static boolean bModified = false;
	/**Instance getter*/
	public static Collection getInstance() {
		if (collection == null) {
			collection = new Collection();
		}
		return collection;
	}

	/**Hidden constructor*/
	private Collection() {
	}
	
	
	/** Write current collection to collection file for persistence between sessions*/
	public static void commit() throws IOException {
		BufferedWriter bw =
			new BufferedWriter(new FileWriter(new File(FILE_COLLECTION)));
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
		//albums
		bw.write("\t<authors>\n");
		it = AuthorManager.getAuthors().iterator();
		while (it.hasNext()) {
			Author author = (Author) it.next();
			bw.write(author.toXml());
		}
		bw.write("\t</authors>\n");
		//tracks
		bw.write("\t<tracks>\n");
		it = TrackManager.getTracks().iterator();
		while (it.hasNext()) {
			Track track = (Track) it.next();
			bw.write(track.toXml());
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
			PlaylistFile playlistFile = ( PlaylistFile) it.next();
			bw.write(playlistFile.toXml());
		}
		bw.write("\t</playlist_files>\n");
		//playlist
		bw.write("\t<playlist>\n");
		it = PlaylistManager.getPlaylists().iterator();
		while (it.hasNext()) {
			Playlist playlist = (Playlist) it.next();
			bw.write(playlist.toXml());
		}
		bw.write("\t</playlist>\n");
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
			throw new JajukException("005");
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
		 *  parsing warning
		 *@param  spe
		 *@exception  SAXException
		 */
		public void warning (SAXParseException spe) throws SAXException {
			throw  new SAXException(Messages.getErrorMessage("004") + spe.getSystemId()
					+ "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() +" : " + spe.getMessage());
	   }

		/**
		 *  parsing error
		 *
		 *@param  spe
		 *@exception  SAXException
		 */
		public void error (SAXParseException spe) throws SAXException {
			throw  new SAXException(Messages.getErrorMessage("005") + spe.getSystemId()
					+ "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() +" : " + spe.getMessage());
		}

		/**
		 *  parsing fatal error
		 *
		 *@param  spe
		 *@exception  SAXException
		 */
		public void fatalError (SAXParseException spe) throws SAXException {
			throw  new SAXException(Messages.getErrorMessage("006") + spe.getSystemId()
					+ "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() +" : " + spe.getMessage());
		}

	/**
	   * Called at parsing start
	   */
	  public void startDocument () {
	  	Log.debug("Starting collection file parsing...");
	  }

	  /**
	   *Called when we start an element
	   */
	  public void startElement (String sUri, String sName, String sQName, Attributes attributes) throws SAXException {
		if (sQName.equals(XML_DEVICE)) {  //device case
			DeviceManager.registerDevice(attributes.getValue(0),attributes.getValue(1),attributes.getValue(2));
			System.out.println(DeviceManager.getDevice("2"));
		}
	  }

	  /**
	   * Called when we reach the end of an element
	   */
	   public void endElement (String sUri, String sName, String sQName) throws SAXException {
		  
	  }


}
