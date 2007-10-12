/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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

import org.jajuk.Main;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Items root container
 * <p>
 * Singletton
 */
public class Collection extends DefaultHandler implements ITechnicalStrings, ErrorHandler,
		Serializable {
	private static final long serialVersionUID = 1L;

	/** Self instance */
	private static Collection collection;

	private static long lTime;

	/** Current ItemManager manager */
	private ItemManager manager;

	/** upgrade for track IDs */
	private HashMap<String, String> hmWrongRightTrackID = new HashMap<String, String>();

	/** upgrade for album IDs */
	private HashMap<String, String> hmWrongRightAlbumID = new HashMap<String, String>();

	/** upgrade for author IDs */
	private HashMap<String, String> hmWrongRightAuthorID = new HashMap<String, String>();

	/** upgrade for style IDs */
	private HashMap<String, String> hmWrongRightStyleID = new HashMap<String, String>();

	/** upgrade for device IDs */
	private HashMap<String, String> hmWrongRightDeviceID = new HashMap<String, String>();

	/** upgrade for directory IDs */
	private HashMap<String, String> hmWrongRightDirectoryID = new HashMap<String, String>();

	/** upgrade for file IDs */
	private HashMap<String, String> hmWrongRightFileID = new HashMap<String, String>();

	/** upgrade for playlist file IDs */
	private HashMap<String, String> hmWrongRightPlaylistFileID = new HashMap<String, String>();
	
	/** Auto commit thread */
	private static Thread tAutoCommit = new Thread() {
		public void run() {
			while (!Main.isExiting()) {
				try {
					Thread.sleep(AUTO_COMMIT_DELAY);
					Log.debug("Auto commit");
					// commit collection at each refresh (can be useful if
					// application is closed brutally with control-C or
					// shutdown and that exit hook have no time to perform
					// commit)
					org.jajuk.base.Collection.commit(Util.getConfFileByPath(FILE_COLLECTION));
				} catch (Exception e) {
					Log.error(e);
				}
			}
		}
	};

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

	/**
	 * Write current collection to collection file for persistence between
	 * sessions
	 */
	public static void commit(File collectionFile) throws IOException {
		long lTime = System.currentTimeMillis();
		String sCharset = ConfigurationManager.getProperty(CONF_COLLECTION_CHARSET);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				collectionFile), sCharset), 1000000);
		bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n");
		bw.write("<" + XML_COLLECTION + " " + XML_VERSION + "='" + JAJUK_VERSION + "'>\n");
		// types
		bw.write(TypeManager.getInstance().toXML());
		Iterator it = null;
		it = TypeManager.getInstance().getTypes().iterator();
		while (it.hasNext()) {
			Type type = (Type) it.next();
			bw.write(type.toXml());
		}
		bw.write("\t</" + TypeManager.getInstance().getLabel() + ">\n");
		// devices
		bw.write(DeviceManager.getInstance().toXML());
		it = DeviceManager.getInstance().getDevices().iterator();
		while (it.hasNext()) {
			Device device = (Device) it.next();
			bw.write(device.toXml());
		}
		bw.write("\t</" + DeviceManager.getInstance().getLabel() + ">\n");
		// styles
		bw.write(StyleManager.getInstance().toXML());
		it = StyleManager.getInstance().getStyles().iterator();
		while (it.hasNext()) {
			Style style = (Style) it.next();
			bw.write(style.toXml());
		}
		bw.write("\t</" + StyleManager.getInstance().getLabel() + ">\n");
		// authors
		bw.write(AuthorManager.getInstance().toXML());
		it = AuthorManager.getInstance().getAuthors().iterator();
		while (it.hasNext()) {
			Author author = (Author) it.next();
			bw.write(author.toXml());
		}
		bw.write("\t</" + AuthorManager.getInstance().getLabel() + ">\n");
		// albums
		bw.write(AlbumManager.getInstance().toXML());
		it = AlbumManager.getInstance().getAlbums().iterator();
		while (it.hasNext()) {
			Album album = (Album) it.next();
			bw.write(album.toXml());
		}
		bw.write("\t</" + AlbumManager.getInstance().getLabel() + ">\n");
		// years
		bw.write(YearManager.getInstance().toXML());
		it = YearManager.getInstance().getYears().iterator();
		while (it.hasNext()) {
			Year year = (Year) it.next();
			bw.write(year.toXml());
		}
		bw.write("\t</" + YearManager.getInstance().getLabel() + ">\n");
		// tracks
		bw.write(TrackManager.getInstance().toXML());
		it = TrackManager.getInstance().getTracks().iterator();
		while (it.hasNext()) {
			Track track = (Track) it.next();
			if (track.getFiles().size() > 0) { // this way we clean up all
				// orphan tracks
				bw.write(track.toXml());
			}
		}
		bw.write("\t</" + TrackManager.getInstance().getLabel() + ">\n");
		// directories
		bw.write(DirectoryManager.getInstance().toXML());
		it = DirectoryManager.getInstance().getDirectories().iterator();
		while (it.hasNext()) {
			Directory directory = (Directory) it.next();
			bw.write(directory.toXml());
		}
		bw.write("\t</" + DirectoryManager.getInstance().getLabel() + ">\n");
		// files
		bw.write(FileManager.getInstance().toXML());
		it = FileManager.getInstance().getFiles().iterator();
		while (it.hasNext()) {
			org.jajuk.base.File file = (org.jajuk.base.File) it.next();
			bw.write(file.toXml());
		}
		bw.write("\t</" + FileManager.getInstance().getLabel() + ">\n");
		// playlist files
		bw.write(PlaylistFileManager.getInstance().toXML());
		it = PlaylistFileManager.getInstance().getPlaylistFiles().iterator();
		while (it.hasNext()) {
			PlaylistFile playlistFile = (PlaylistFile) it.next();
			bw.write(playlistFile.toXml());
		}
		bw.write("\t</" + PlaylistFileManager.getInstance().getLabel() + ">\n");
		// playlist
		bw.write(PlaylistManager.getInstance().toXML());
		it = PlaylistManager.getInstance().getPlayLists().iterator();
		while (it.hasNext()) {
			Playlist playlist = (Playlist) it.next();
			if (playlist.getPlaylistFiles().size() > 0) {
				// this way we clean up all orphan playlists
				bw.write(playlist.toXml());
			}
		}
		bw.write("\t</" + PlaylistManager.getInstance().getLabel() + ">\n");
		bw.write("</" + XML_COLLECTION + ">\n");
		bw.flush();
		bw.close();
		Log.debug("Collection commited in " + (System.currentTimeMillis() - lTime) + " ms");
	}

	/**
	 * Parse collection.xml file and put all collection information into memory
	 * 
	 */
	public static void load(File file) throws Exception {
		lTime = System.currentTimeMillis();
		// make sure to clean everything in memory
		cleanup();
		DeviceManager.getInstance().cleanAllDevices();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		spf.setNamespaceAware(false);
		SAXParser saxParser = spf.newSAXParser();
		File frt = file;
		if (!frt.exists()) {
			throw new JajukException(005);
		}
		saxParser.parse(frt.toURI().toURL().toString(), getInstance());
		// start auto commit thread
		tAutoCommit.start();
	}

	/**
	 * Perform a collection clean up for logical items ( delete orphan data )
	 * 
	 * @return
	 */
	public static synchronized void cleanup() {
		// Tracks cleanup
		TrackManager.getInstance().cleanup();
		// Styles cleanup
		StyleManager.getInstance().cleanup();
		// Authors cleanup
		AuthorManager.getInstance().cleanup();
		// albums cleanup
		AlbumManager.getInstance().cleanup();
		// Playlists cleanup
		PlaylistManager.getInstance().cleanup();
	}

	/**
	 * parsing warning
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void warning(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage(5)+ " / " + spe.getSystemId() + "/"
				+ spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * parsing error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void error(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage(5) + " / " + spe.getSystemId() + "/"
				+ spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * parsing fatal error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void fatalError(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage(5) + " / " + spe.getSystemId() + "/"
				+ spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
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
		long l = (System.currentTimeMillis() - lTime);
		Log.debug("Collection file parsing done : " + ((l < 1000) ? l + " ms" : (l / 1000) + " s"));
	}

	/**
	 * Called when we start an element
	 * 
	 */
	public void startElement(String sUri, String s, String sQName, Attributes attributes)
			throws SAXException {
		try {
			if (XML_DEVICES.equals(sQName)) {
				manager = DeviceManager.getInstance();
			} else if (XML_ALBUMS.equals(sQName)) {
				manager = AlbumManager.getInstance();
			} else if (XML_AUTHORS.equals(sQName)) {
				manager = AuthorManager.getInstance();
			} else if (XML_DIRECTORIES.equals(sQName)) {
				manager = DirectoryManager.getInstance();
			} else if (XML_FILES.equals(sQName)) {
				manager = FileManager.getInstance();
			} else if (XML_PLAYLISTS.equals(sQName)) {
				manager = PlaylistManager.getInstance();
			} else if (XML_PLAYLIST_FILES.equals(sQName)) {
				manager = PlaylistFileManager.getInstance();
			} else if (XML_STYLES.equals(sQName)) {
				manager = StyleManager.getInstance();
			} else if (XML_TRACKS.equals(sQName)) {
				manager = TrackManager.getInstance();
			} else if (XML_YEARS.equals(sQName)) {
				manager = YearManager.getInstance();
			} else if (XML_TYPES.equals(sQName)) {
				manager = TypeManager.getInstance();
			} else if (XML_PROPERTY.equals(sQName)) {
				// A property description
				String sPropertyName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				boolean bCustom = Boolean.parseBoolean(attributes.getValue(attributes
						.getIndex(XML_CUSTOM)));
				boolean bConstructor = Boolean.parseBoolean(attributes.getValue(attributes
						.getIndex(XML_CONSTRUCTOR)));
				boolean bShouldBeDisplayed = Boolean.parseBoolean(attributes.getValue(attributes
						.getIndex(XML_VISIBLE)));
				boolean bEditable = Boolean.parseBoolean(attributes.getValue(attributes
						.getIndex(XML_EDITABLE)));
				boolean bUnique = Boolean.parseBoolean(attributes.getValue(attributes
						.getIndex(XML_UNIQUE)));
				Class cType = Class.forName(attributes.getValue(attributes.getIndex(XML_TYPE)));
				String sDefaultValue = attributes.getValue(attributes.getIndex(XML_DEFAULT_VALUE))
						.intern();
				Object oDefaultValue = null;
				if (sDefaultValue != null && sDefaultValue.trim().length() > 0) {
					try {
						// Date format has changed from 1.3 (only yyyyMMdd
						// addition format is used)
						// so an exception will be thrown when upgrading from
						// 1.2
						// we reset default value to "today"
						oDefaultValue = Util.parse(sDefaultValue, cType);
					} catch (Exception e) {
						oDefaultValue = new Date();
					}
				}
				PropertyMetaInformation meta = new PropertyMetaInformation(sPropertyName, bCustom,
						bConstructor, bShouldBeDisplayed, bEditable, bUnique, cType, oDefaultValue);
				if (manager.getMetaInformation(sPropertyName) == null) {
					// standard properties are already loaded
					manager.registerProperty(meta);
				}
			} else if (XML_DEVICE.equals(sQName)) {
				Device device = null;
				String sId = attributes.getValue(attributes.getIndex(XML_ID));
				String sItemName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				long lType = Long.parseLong(attributes.getValue(attributes.getIndex(XML_TYPE)));
				// UPGRADE --For jajuk < 1.2 id changed
				String sRightID = DeviceManager.createID(sItemName).intern();
				String sURL = attributes.getValue(attributes.getIndex(XML_URL)).intern();
				device = DeviceManager.getInstance().registerDevice(sRightID, sItemName, lType,
						sURL);
				if (device != null) {
					device.populateProperties(attributes);
				}
				// display a message if Id had a problem
				if (!sId.equals(sRightID)) {
					Log.debug("** Wrong device Id, upgraded: " + device);
					hmWrongRightDeviceID.put(sId, sRightID);
				}
			} else if (XML_STYLE.equals(sQName)) {
				String sId = attributes.getValue(attributes.getIndex(XML_ID));
				String sItemName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				// UPGRADE --For jajuk == 1.0.1 to 1.0.2 : id changed
				String sRightID = StyleManager.createID(sItemName).intern();
				Style style = StyleManager.getInstance().registerStyle(sRightID, sItemName);
				if (style != null) {
					style.populateProperties(attributes);
				}
				// display a message if Id had a problem
				if (!sId.equals(sRightID)) {
					Log.debug("** Wrong style Id, upgraded: " + style);
					hmWrongRightStyleID.put(sId, sRightID);
				}
			} else if (XML_YEAR.equals(sQName)) {
				String sId = attributes.getValue(attributes.getIndex(XML_ID));
				String sItemName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				Year year = YearManager.getInstance().registerYear(sId, sItemName);
				if (year != null) {
					year.populateProperties(attributes);
				}
			} else if (XML_AUTHOR.equals(sQName)) {
				String sId = attributes.getValue(attributes.getIndex(XML_ID));
				String sItemName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				// UPGRADE --For jajuk == 1.0.1 to 1.0.2 : id changed
				String sRightID = AuthorManager.createID(sItemName).intern();
				Author author = AuthorManager.getInstance().registerAuthor(sRightID, sItemName);
				if (author != null) {
					author.populateProperties(attributes);
				}
				// display a message if Id had a problem
				if (!sId.equals(sRightID)) {
					Log.debug("** Wrong author Id, upgraded: " + author);
					hmWrongRightAuthorID.put(sId, sRightID);
				}
			} else if (XML_ALBUM.equals(sQName)) {
				String sId = attributes.getValue(attributes.getIndex(XML_ID));
				String sItemName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				// UPGRADE --For jajuk == 1.0.1 to 1.0.2 : id changed
				String sRightID = AlbumManager.createID(sItemName).intern();
				Album album = AlbumManager.getInstance().registerAlbum(sRightID, sItemName);
				if (album != null) {
					album.populateProperties(attributes);
				}
				// display a message if Id had a problem
				if (!sId.equals(sRightID)) {
					Log.debug("** Wrong album Id, upgraded: " + album);
					hmWrongRightAlbumID.put(sId, sRightID);
				}
			} else if (XML_TRACK.equals(sQName)) {
				String sId = attributes.getValue(attributes.getIndex(XML_ID));
				String sTrackName = attributes.getValue(attributes.getIndex(XML_TRACK_NAME))
						.intern();
				// album
				String sAlbumID = attributes.getValue(attributes.getIndex(XML_TRACK_ALBUM))
						.intern();
				if (hmWrongRightAlbumID.size() > 0) {
					if (hmWrongRightAlbumID.containsKey(sAlbumID)) {
						sAlbumID = hmWrongRightAlbumID.get(sAlbumID);
					}
				}
				Album album = AlbumManager.getInstance().getAlbumByID(sAlbumID);
				// Style
				String sStyleID = attributes.getValue(attributes.getIndex(XML_TRACK_STYLE))
						.intern();
				if (hmWrongRightStyleID.size() > 0) {
					if (hmWrongRightStyleID.containsKey(sStyleID)) {
						sStyleID = hmWrongRightStyleID.get(sStyleID);
					}
				}
				Style style = StyleManager.getInstance().getStyleByID(sStyleID);
				// Year
				String sYearID = attributes.getValue(attributes.getIndex(XML_TRACK_YEAR)).intern();
				Year year = YearManager.getInstance().getYearByID(sYearID);
				// For jajuk < 1.4
				if (year == null) {
					year = YearManager.getInstance().registerYear(sYearID, sYearID);
				}
				// Author
				String sAuthorID = attributes.getValue(attributes.getIndex(XML_TRACK_AUTHOR))
						.intern();
				if (hmWrongRightAuthorID.size() > 0) {
					if (hmWrongRightAuthorID.containsKey(sAuthorID)) {
						sAuthorID = hmWrongRightAuthorID.get(sAuthorID);
					}
				}
				Author author = AuthorManager.getInstance().getAuthorByID(sAuthorID);
				long length = Long.parseLong(attributes.getValue(attributes
						.getIndex(XML_TRACK_LENGTH)));
				//Type
				//upgrade from < 1.4, type id from index to extension
				HashMap<String,String> conversion = new HashMap<String, String>();
				conversion.put("0","mp3");
				conversion.put("1","m3u");
				conversion.put("2","ogg");
				conversion.put("3","wav");
				conversion.put("4","au");
				conversion.put("5","flac");
				conversion.put("6","wma");
				conversion.put("7","aac");
				conversion.put("8","m4a");
				conversion.put("9","ram");
				conversion.put("10","mp2");
				String typeID = attributes.getValue(attributes.getIndex(XML_TYPE)); 
				if (conversion.containsKey(typeID)){
					typeID = conversion.get(typeID);
				}
				Type type = TypeManager.getInstance().getTypeByID(typeID);
				// more checkups
				if (album == null || author == null || style == null || type == null) {
					return;
				}
				// Get year: we check number format mainly for the case of
				// upgrade from <1.0
				@SuppressWarnings("unused")
				long lYear = 0;
				try {
					lYear = Integer.parseInt(attributes.getValue(attributes.getIndex(XML_YEAR)));
				} catch (Exception e) {
					if (Log.isDebugEnabled()) {
						// wrong format
						Log.debug(Messages.getString("Error.137") + ":" + sTrackName);
					}
				}
				// Idem for order
				long lOrder = 0l;
				try {
					lOrder = Long.parseLong(attributes.getValue(attributes
							.getIndex(XML_TRACK_ORDER)));
				} catch (Exception e) {
					if (Log.isDebugEnabled()) {
						// wrong format
						Log.debug(Messages.getString("Error.137") + ":" + sTrackName); // wrong
					}
				}
				// UPGRADE --For jajuk == 1.0.1 to 1.0.2 : Track id changed and
				// used deep hash code, not used later
				String sRightID = TrackManager.createID(sTrackName, album, style, author, length,
						year, lOrder, type).intern();

				// Date format should be OK
				Date dAdditionDate = Util.getAdditionDateFormat().parse(
						attributes.getValue(attributes.getIndex(XML_TRACK_ADDED)));
				Track track = TrackManager.getInstance().registerTrack(sRightID, sTrackName, album,
						style, author, length, year, lOrder, type);
				track.setRate(Long.parseLong(attributes.getValue(attributes
						.getIndex(XML_TRACK_RATE))));
				track.setHits(Long.parseLong(attributes.getValue(attributes
						.getIndex(XML_TRACK_HITS))));
				track.setAdditionDate(dAdditionDate);
				String sComment = attributes.getValue(attributes.getIndex(XML_TRACK_COMMENT));
				if (sComment == null) {
					sComment = "";
				}
				track.setComment(sComment);
				track.populateProperties(attributes);
				// display a message if Id had a problem
				if (!sId.equals(sRightID)) {
					Log.debug("** Wrong Track Id, upgraded: " + track);
					hmWrongRightTrackID.put(sId, sRightID);
				}
			} else if (XML_DIRECTORY.equals(sQName)) {
				Directory dParent = null;
				String sParentId = attributes.getValue(attributes.getIndex(XML_DIRECTORY_PARENT))
						.intern();
				// UPGRADE --For jajuk < 1.2 id changed
				if (hmWrongRightDirectoryID.size() > 0) {
					if (hmWrongRightDirectoryID.containsKey(sParentId)) {
						sParentId = hmWrongRightDirectoryID.get(sParentId);
					}
				}
				if (!"-1".equals(sParentId)) {
					dParent = DirectoryManager.getInstance().getDirectoryByID(sParentId); // Parent
					// directory
					// should
					// be
					// already
					// referenced because of order
					// conservation
					if (dParent == null) { // check parent directory exists
						return;
					}
				}
				String sDeviceID = attributes.getValue(attributes.getIndex(XML_DEVICE));
				// take upgraded device ID if needed
				if (hmWrongRightDeviceID.size() > 0) {
					if (hmWrongRightDeviceID.containsKey(sDeviceID)) {
						sDeviceID = hmWrongRightDeviceID.get(sDeviceID);
					}
				}
				Device device = DeviceManager.getInstance().getDeviceByID(sDeviceID);
				if (device == null) { // check device exists
					return;
				}
				String sItemName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				String sID = attributes.getValue(attributes.getIndex(XML_ID));
				// UPGRADE --For jajuk < 1.2 id changed
				String sRightID = DirectoryManager.createID(sItemName, device, dParent).intern();
				Directory directory = DirectoryManager.getInstance().registerDirectory(sRightID,
						sItemName, dParent, device);
				directory.populateProperties(attributes);
				// display a message if Id had a problem
				if (!sID.equals(sRightID)) {
					Log.debug("** Wrong directory Id, upgraded: " + directory);
					hmWrongRightDirectoryID.put(sID, sRightID);
				}
			} else if (XML_FILE.equals(sQName)) {
				String sItemName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				// Check file type is still registrated, it can be useful for ie
				// if mplayer is no more available
				String ext = Util.getExtension(new File(sItemName));
				Type type = TypeManager.getInstance().getTypeByExtension(ext);
				if (type == null) {
					return;
				}
				String sTrackId = attributes.getValue(attributes.getIndex(XML_TRACK)).intern();
				// UPGRADE check if track Id is right
				if (hmWrongRightTrackID.size() > 0) {
					// replace wrong by right ID
					if (hmWrongRightTrackID.containsKey(sTrackId)) {
						sTrackId = hmWrongRightTrackID.get(sTrackId);
					}
				}
				Track track = TrackManager.getInstance().getTrackByID(sTrackId);
				String sParentID = attributes.getValue(attributes.getIndex(XML_DIRECTORY)).intern();
				// UPGRADE check parent ID is right
				if (hmWrongRightDirectoryID.size() > 0) {
					// replace wrong by right ID
					if (hmWrongRightDirectoryID.containsKey(sParentID)) {
						sParentID = hmWrongRightDirectoryID.get(sParentID);
					}
				}
				Directory dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
				if (dParent == null || track == null) { // more checkups
					return;
				}
				long lSize = Long.parseLong(attributes.getValue(attributes.getIndex(XML_SIZE)));
				// Quality analyze, handle format problems (mainly for upgrades)
				long lQuality = 0;
				try {
					lQuality = Long
							.parseLong(attributes.getValue(attributes.getIndex(XML_QUALITY)));
				} catch (Exception e) {
					if (Log.isDebugEnabled()) {
						// wrong format
						Log.debug(Messages.getString("Error.137") + ":" + sItemName); // wrong
					}
				}
				String sID = attributes.getValue(attributes.getIndex(XML_ID));
				// UPGRADE --For jajuk < 1.2 id changed
				String sRightID = FileManager.createID(sItemName, dParent).intern();
				org.jajuk.base.File file = FileManager.getInstance().registerFile(sRightID,
						sItemName, dParent, track, lSize, lQuality);
				file.populateProperties(attributes);
				// display a message if Id had a problem
				if (!sID.equals(sRightID)) {
					Log.debug("** Wrong file Id, upgraded: " + file);
					hmWrongRightFileID.put(sID, sRightID);
				}
			} else if (XML_PLAYLIST_FILE.equals(sQName)) {
				String sParentID = attributes.getValue(attributes.getIndex(XML_DIRECTORY)).intern();
				// UPGRADE check parent ID is right
				if (hmWrongRightDirectoryID.size() > 0) {
					// replace wrong by right ID
					if (hmWrongRightDirectoryID.containsKey(sParentID)) {
						sParentID = hmWrongRightDirectoryID.get(sParentID);
					}
				}
				Directory dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
				if (dParent == null) { // check directory is exists
					return;
				}
				String sID = attributes.getValue(attributes.getIndex(XML_ID));
				String sItemName = attributes.getValue(attributes.getIndex(XML_NAME)).intern();
				// UPGRADE --For jajuk < 1.2 id changed
				String sRightID = PlaylistFileManager.createID(sItemName, dParent).intern();
				PlaylistFile plf = PlaylistFileManager.getInstance().registerPlaylistFile(sRightID,
						sItemName, dParent);
				if (plf != null) {
					plf.populateProperties(attributes);
					dParent.addPlaylistFile(plf);
				}
				// display a message if Id had a problem
				if (!sID.equals(sRightID)) {
					Log.debug("** Wrong playlist file Id, upgraded: " + plf);
					hmWrongRightPlaylistFileID.put(sID, sRightID);
				}
			} else if (XML_PLAYLIST.equals(sQName)) {
				String sPlaylistFiles = attributes
						.getValue(attributes.getIndex(XML_PLAYLIST_FILES)).intern();
				// playlist file list with ','
				StringTokenizer st = new StringTokenizer(sPlaylistFiles, ",");
				Playlist playlist = null;
				if (st.hasMoreTokens()) {
					// if none mapped file, ignore
					// it so it will be removed at
					// next commit
					do {
						String sPlaylistFileID = (String) st.nextElement();
						// UPGRADE check parent ID is right
						if (hmWrongRightPlaylistFileID.size() > 0) {
							// replace wrong by right ID
							if (hmWrongRightPlaylistFileID.containsKey(sPlaylistFileID)) {
								sPlaylistFileID = hmWrongRightPlaylistFileID.get(sPlaylistFileID)
										.intern();
							}
						}
						PlaylistFile plFile = PlaylistFileManager.getInstance()
								.getPlaylistFileByID(sPlaylistFileID);
						if (plFile != null) {
							playlist = PlaylistManager.getInstance().registerPlaylist(plFile);
						}
					} while (st.hasMoreTokens());
					if (playlist != null) {
						playlist.populateProperties(attributes);
					}
				}
			}
		} catch (Exception re) {
			String sAttributes = "";
			for (int i = 0; i < attributes.getLength(); i++) {
				sAttributes += "\n" + attributes.getQName(i) + "=" + attributes.getValue(i);
			}
			Log.error(5, sAttributes, re);
		}
	}

	/**
	 * @return list of wrong file id (used by history)
	 */
	public HashMap<String, String> getHmWrongRightFileID() {
		return hmWrongRightFileID;
	}

}
