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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.services.core.ExitService;
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
  public HashMap<String, String> hmWrongRightAlbumID = new HashMap<String, String>();

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

  /** upgrade for playlist IDs */
  private HashMap<String, String> hmWrongRightPlaylistFileID = new HashMap<String, String>();

  /** Conversion of types from < 1.4 */
  private static HashMap<String, String> conversion;
  static {
    conversion = new HashMap<String, String>(12);
    conversion.put("0", "mp3");
    conversion.put("1", "m3u");
    conversion.put("2", "ogg");
    conversion.put("3", "wav");
    conversion.put("4", "au");
    conversion.put("5", "flac");
    conversion.put("6", "wma");
    conversion.put("7", "aac");
    conversion.put("8", "m4a");
    conversion.put("9", "ram");
    conversion.put("10", "mp2");
  }

  /*****************************************************************************
   * [PERF] provide current stage (files, tracks...) used to optimize switch
   * when parsing the collection
   ****************************************************************************/
  private short stage = -1;

  /**
   * [PERF] Does the type has been checked once for ID computation change ?
   * Indeed, we check only one element of each type to check if this computation
   * changed for perfs
   */
  private boolean needCheckID = false;

  // Constants value, use lower value for mist numerous items to parse
  private static final short STAGE_FILES = 0;

  private static final short STAGE_DIRECTORIES = 1;

  private static final short STAGE_TRACKS = 2;

  private static final short STAGE_ALBUMS = 3;

  private static final short STAGE_AUTHORS = 4;

  private static final short STAGE_STYLES = 5;

  private static final short STAGE_PLAYLIST_FILES = 6;

  private static final short STAGE_PLAYLISTS = 7;

  private static final short STAGE_TYPES = 8;

  private static final short STAGE_DEVICES = 9;

  private static final short STAGE_YEARS = 10;

  private static final DateFormat additionFormatter = Util.getAdditionDateFormatter();

  /** Auto commit thread */
  private static Thread tAutoCommit = new Thread("Collection Auto Commit Thread") {
    @Override
    public void run() {
      while (!ExitService.isExiting()) {
        try {
          Thread.sleep(AUTO_COMMIT_DELAY);
          Log.debug("Auto commit");
          // commit collection at each refresh (can be useful
          // if
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
    for (Type type : TypeManager.getInstance().getTypes()) {
      bw.write(type.toXml());
    }
    StringBuilder sb = new StringBuilder(40);
    sb.append("\t</");
    sb.append(TypeManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // devices
    bw.write(DeviceManager.getInstance().toXML());
    for (Device device : DeviceManager.getInstance().getDevices()) {
      bw.write(device.toXml());
    }
    sb = new StringBuilder(40);
    sb.append("\t</");
    sb.append(DeviceManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // styles
    bw.write(StyleManager.getInstance().toXML());
    for (Style style : StyleManager.getInstance().getStyles()) {
      bw.write(style.toXml());
    }
    sb = new StringBuilder(40);
    sb.append("\t</");
    sb.append(StyleManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // authors
    bw.write(AuthorManager.getInstance().toXML());
    for (Author author : AuthorManager.getInstance().getAuthors()) {
      bw.write(author.toXml());
    }
    sb = new StringBuilder(40);
    sb.append("\t</");
    sb.append(AuthorManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // albums
    bw.write(AlbumManager.getInstance().toXML());
    for (Album album : AlbumManager.getInstance().getAlbums()) {
      bw.write(album.toXml());
    }
    sb = new StringBuilder(40);
    sb.append("\t</");
    sb.append(AlbumManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // years
    bw.write(YearManager.getInstance().toXML());
    for (Year year : YearManager.getInstance().getYears()) {
      bw.write(year.toXml());
    }
    sb = new StringBuilder(40);
    sb.append("\t</");
    sb.append(YearManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // tracks
    bw.write(TrackManager.getInstance().toXML());
    for (Track track : TrackManager.getInstance().getTracks()) {
      if (track.getFiles().size() > 0) { // this way we clean up all
        // orphan tracks
        bw.write(track.toXml());
      }
    }
    sb = new StringBuilder(200);
    sb.append("\t</");
    sb.append(TrackManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // directories
    bw.write(DirectoryManager.getInstance().toXML());
    for (Directory directory : DirectoryManager.getInstance().getDirectories()) {
      bw.write(directory.toXml());
    }
    sb = new StringBuilder(100);
    sb.append("\t</");
    sb.append(DirectoryManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // files
    bw.write(FileManager.getInstance().toXML());
    for (org.jajuk.base.File file : FileManager.getInstance().getFiles()) {
      bw.write(file.toXml());
    }
    sb = new StringBuilder(200);
    sb.append("\t</");
    sb.append(FileManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // playlists
    bw.write(PlaylistManager.getInstance().toXML());
    for (Playlist playlistFile : PlaylistManager.getInstance().getPlaylists()) {
      bw.write(playlistFile.toXml());
    }
    sb = new StringBuilder(200);
    sb.append("\t</");
    sb.append(PlaylistManager.getInstance().getLabel());
    sb.append(">\n");
    bw.write(sb.toString());
    // end of collection
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
  }

  /**
   * parsing warning
   * 
   * @param spe
   * @exception SAXException
   */
  @Override
  public void warning(SAXParseException spe) throws SAXException {
    throw new SAXException(Messages.getErrorMessage(5) + " / " + spe.getSystemId() + "/"
        + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
  }

  /**
   * parsing error
   * 
   * @param spe
   * @exception SAXException
   */
  @Override
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
  @Override
  public void fatalError(SAXParseException spe) throws SAXException {
    throw new SAXException(Messages.getErrorMessage(5) + " / " + spe.getSystemId() + "/"
        + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
  }

  /**
   * Called at parsing start
   */
  @Override
  public void startDocument() {
    Log.debug("Starting collection file parsing...");
  }

  /**
   * Called at parsing end
   */
  @Override
  public void endDocument() {
    long l = (System.currentTimeMillis() - lTime);
    Log.debug("Collection file parsing done : " + l + " ms");
  }

  /**
   * Called when we start an element
   * 
   */
  @Override
  public void startElement(String sUri, String s, String sQName, Attributes attributes)
      throws SAXException {
    try {
      int idIndex = attributes.getIndex(XML_ID);
      // [PERF] Manage top tags to set current stage. Manages 'properties'
      // tags as well
      if (idIndex == -1) {
        if (XML_DEVICES.equals(sQName)) {
          manager = DeviceManager.getInstance();
          stage = STAGE_DEVICES;
          needCheckID = true;
        } else if (XML_ALBUMS.equals(sQName)) {
          manager = AlbumManager.getInstance();
          stage = STAGE_ALBUMS;
          needCheckID = true;
        } else if (XML_AUTHORS.equals(sQName)) {
          manager = AuthorManager.getInstance();
          stage = STAGE_AUTHORS;
          needCheckID = true;
        } else if (XML_DIRECTORIES.equals(sQName)) {
          manager = DirectoryManager.getInstance();
          stage = STAGE_DIRECTORIES;
          needCheckID = true;
        } else if (XML_FILES.equals(sQName)) {
          manager = FileManager.getInstance();
          stage = STAGE_FILES;
          needCheckID = true;
        } else if (XML_PLAYLISTS.equals(sQName)) {
          //This code is here for JAjuk < 1.6 compatibility
          manager = PlaylistManager.getInstance();
          stage = STAGE_PLAYLISTS;
          needCheckID = true;
        } else if (XML_PLAYLIST_FILES.equals(sQName)) {
          manager = PlaylistManager.getInstance();
          stage = STAGE_PLAYLIST_FILES;
          needCheckID = true;
        } else if (XML_STYLES.equals(sQName)) {
          manager = StyleManager.getInstance();
          stage = STAGE_STYLES;
          needCheckID = true;
        } else if (XML_TRACKS.equals(sQName)) {
          manager = TrackManager.getInstance();
          stage = STAGE_TRACKS;
          needCheckID = true;
        } else if (XML_YEARS.equals(sQName)) {
          manager = YearManager.getInstance();
          stage = STAGE_YEARS;
          needCheckID = true;
        } else if (XML_TYPES.equals(sQName)) {
          manager = TypeManager.getInstance();
          stage = STAGE_TYPES;
          needCheckID = true;
        } else if (XML_PROPERTY.equals(sQName)) {
          // A property description
          String sPropertyName = attributes.getValue(XML_NAME).intern();
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
          Class<?> cType = Class.forName(attributes.getValue(XML_TYPE));
          String sDefaultValue = attributes.getValue(XML_DEFAULT_VALUE)
              .intern();
          Object oDefaultValue = null;
          if (sDefaultValue != null && sDefaultValue.length() > 0) {
            try {
              // Date format has changed from 1.3 (only yyyyMMdd
              // addition format is used)
              // so an exception will be thrown when upgrading
              // from 1.2
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
        }
      } else {
        // Manage elements themselves using a switch for performances
        String sItemName = null;
        String sID = null;
        Directory dParent = null;
        String sRightID = null;
        Type type = null;
        Track track = null;
        Album album = null;
        Author author = null;
        Style style = null;
        String sTrackId = null;
        String sParentID = null;
        String sDeviceID = null;
        String sTrackName = null;
        long lSize = 0;
        switch (stage) {
        case STAGE_FILES:
          sItemName = attributes.getValue(XML_NAME).intern();
          // Check file type is still registered, it can be
          // useful for ie if mplayer is no more available
          String ext = Util.getExtension(sItemName);
          type = TypeManager.getInstance().getTypeByExtension(ext);
          if (type == null) {
            return;
          }
          sTrackId = attributes.getValue(XML_TRACK).intern();
          // UPGRADE check if track Id is right
          if (hmWrongRightTrackID.size() > 0) {
            // replace wrong by right ID
            if (hmWrongRightTrackID.containsKey(sTrackId)) {
              sTrackId = hmWrongRightTrackID.get(sTrackId);
            }
          }
          track = TrackManager.getInstance().getTrackByID(sTrackId);
          sParentID = attributes.getValue(XML_DIRECTORY).intern();
          // UPGRADE check parent ID is right
          if (hmWrongRightDirectoryID.size() > 0) {
            // replace wrong by right ID
            if (hmWrongRightDirectoryID.containsKey(sParentID)) {
              sParentID = hmWrongRightDirectoryID.get(sParentID);
            }
          }
          dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
          if (dParent == null || track == null) { // more checkups
            return;
          }
          if(attributes.getValue(XML_SIZE) != null){
            lSize = Long.parseLong(attributes.getValue(XML_SIZE));
          }
         
          // Quality analyze, handle format problems (mainly for
          // upgrades)
          long lQuality = 0;
          try {
            lQuality = Integer.parseInt(attributes.getValue(XML_QUALITY));
          } catch (Exception e) {
            if (Log.isDebugEnabled()) {
              // wrong format
              Log.debug(Messages.getString("Error.137") + ":" + sItemName); // wrong
            }
          }
          sID = attributes.getValue(idIndex).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = FileManager.createID(sItemName, dParent);
            if (sRightID.equals(sID)) {
              needCheckID = false;
            } else {
              Log.debug("** Wrong file Id, upgraded: " + sItemName);
              hmWrongRightFileID.put(sID, sRightID);
            }
          }
          org.jajuk.base.File file = FileManager.getInstance().registerFile(sRightID, sItemName,
              dParent, track, lSize, lQuality);
          file.populateProperties(attributes);
          break;
        case STAGE_DIRECTORIES:
          dParent = null;
          sParentID = attributes.getValue(XML_DIRECTORY_PARENT).intern();
          // UPGRADE
          if (hmWrongRightDirectoryID.size() > 0) {
            if (hmWrongRightDirectoryID.containsKey(sParentID)) {
              sParentID = hmWrongRightDirectoryID.get(sParentID);
            }
          }
          if (!"-1".equals(sParentID)) {
            // Parent directory should be already referenced
            // because
            // of
            // order conservation
            dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
            if (dParent == null) { // check parent directory
              // exists
              return;
            }
          }
          sDeviceID = attributes.getValue(XML_DEVICE).intern();
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
          sItemName = attributes.getValue(XML_NAME).intern();
          sID = attributes.getValue(idIndex).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = DirectoryManager.createID(sItemName, device, dParent);
            if (sRightID.equals(sID)) {
              needCheckID = false;
            } else {
              Log.debug("** Wrong directory Id, upgraded: " + sItemName);
              hmWrongRightDirectoryID.put(sID, sRightID);
            }
          }
          Directory directory = DirectoryManager.getInstance().registerDirectory(sRightID,
              sItemName, dParent, device);
          directory.populateProperties(attributes);
          break;
        case STAGE_TRACKS:
          sID = attributes.getValue(idIndex).intern();
          sTrackName = attributes.getValue(XML_TRACK_NAME).intern();
          // album
          String sAlbumID = attributes.getValue(XML_TRACK_ALBUM).intern();
          if (hmWrongRightAlbumID.size() > 0) {
            if (hmWrongRightAlbumID.containsKey(sAlbumID)) {
              sAlbumID = hmWrongRightAlbumID.get(sAlbumID);
            }
          }
          album = AlbumManager.getInstance().getAlbumByID(sAlbumID);
          // Style
          String sStyleID = attributes.getValue(XML_TRACK_STYLE).intern();
          if (hmWrongRightStyleID.size() > 0) {
            if (hmWrongRightStyleID.containsKey(sStyleID)) {
              sStyleID = hmWrongRightStyleID.get(sStyleID);
            }
          }
          style = StyleManager.getInstance().getStyleByID(sStyleID);
          // Year
          String sYearID = attributes.getValue(XML_TRACK_YEAR).intern();
          Year year = YearManager.getInstance().getYearByID(sYearID);
          // For jajuk < 1.4
          if (year == null) {
            year = YearManager.getInstance().registerYear(sYearID, sYearID);
          }
          // Author
          String sAuthorID = attributes.getValue(XML_TRACK_AUTHOR).intern();
          if (hmWrongRightAuthorID.size() > 0) {
            if (hmWrongRightAuthorID.containsKey(sAuthorID)) {
              sAuthorID = hmWrongRightAuthorID.get(sAuthorID);
            }
          }
          author = AuthorManager.getInstance().getAuthorByID(sAuthorID);
          long length = Long.parseLong(attributes.getValue(XML_TRACK_LENGTH));
          // Type
          String typeID = attributes.getValue(XML_TYPE);
          if (conversion.containsKey(typeID)) {
            typeID = conversion.get(typeID);
          }
          type = TypeManager.getInstance().getTypeByID(typeID);
          // more checkups
          if (album == null || author == null || style == null || type == null) {
            return;
          }
          long lYear = 0;
          try {
            lYear = Integer.parseInt(attributes.getValue(XML_YEAR));
          } catch (Exception e) {
            if (Log.isDebugEnabled()) {
              // wrong format
              Log.debug(Messages.getString("Error.137") + ":" + sTrackName);
            }
          }
          // Idem for order
          long lOrder = 0l;
          try {
            lOrder = Integer.parseInt(attributes.getValue(XML_TRACK_ORDER));
          } catch (Exception e) {
            if (Log.isDebugEnabled()) {
              // wrong format
              Log.debug(Messages.getString("Error.137") + ":" + sTrackName); // wrong
            }
          }
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = TrackManager.createID(sTrackName, album, style, author, length, year,
                lOrder, type);
            if (sRightID.equals(sID)) {
              needCheckID = false;
            } else {
              Log.debug("** Wrong Track Id, upgraded: " + sTrackName);
              hmWrongRightTrackID.put(sID, sRightID);
            }
          }
          // Date format should be OK
          Date dAdditionDate = additionFormatter.parse(attributes.getValue(attributes
              .getIndex(XML_TRACK_DISCOVERY_DATE)));
          track = TrackManager.getInstance().registerTrack(sRightID, sTrackName, album, style,
              author, length, year, lOrder, type);
          track.setRate(Long.parseLong(attributes.getValue(XML_TRACK_RATE)));
          track.setHits(Long.parseLong(attributes.getValue(XML_TRACK_HITS)));
          track.setDiscoveryDate(dAdditionDate);
          String sComment = attributes.getValue(XML_TRACK_COMMENT).intern();
          if (sComment == null) {
            sComment = "";
          }
          track.setComment(sComment);
          track.populateProperties(attributes);
          break;
        case STAGE_ALBUMS:
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(XML_NAME).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = AlbumManager.createID(sItemName);
            if (sRightID.equals(sID)) {
              needCheckID = false;
            } else {
              Log.debug("** Wrong album Id, upgraded: " + sItemName);
              hmWrongRightAlbumID.put(sID, sRightID);
            }
          }
          album = AlbumManager.getInstance().registerAlbum(sRightID, sItemName);
          if (album != null) {
            album.populateProperties(attributes);
          }
          break;
        case STAGE_AUTHORS:
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(XML_NAME).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = AuthorManager.createID(sItemName);
            if (sRightID.equals(sID)) {
              needCheckID = false;
            } else {
              Log.debug("** Wrong author Id, upgraded: " + sItemName);
              hmWrongRightAuthorID.put(sID, sRightID);
            }
          }
          author = AuthorManager.getInstance().registerAuthor(sRightID, sItemName);
          if (author != null) {
            author.populateProperties(attributes);
          }
          break;
        case STAGE_STYLES:
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(XML_NAME).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = StyleManager.createID(sItemName);
            if (sRightID.equals(sID)) {
              needCheckID = false;
            } else {
              Log.debug("** Wrong style Id, upgraded: " + sItemName);
              hmWrongRightStyleID.put(sID, sRightID);
            }
          }
          style = StyleManager.getInstance().registerStyle(sRightID, sItemName);
          if (style != null) {
            style.populateProperties(attributes);
          }
          break;
        case STAGE_PLAYLIST_FILES:
          sParentID = attributes.getValue(XML_DIRECTORY).intern();
          // UPGRADE check parent ID is right
          if (hmWrongRightDirectoryID.size() > 0) {
            // replace wrong by right ID
            if (hmWrongRightDirectoryID.containsKey(sParentID)) {
              sParentID = hmWrongRightDirectoryID.get(sParentID);
            }
          }
          dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
          if (dParent == null) { // check directory is exists
            return;
          }
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(XML_NAME).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = PlaylistManager.createID(sItemName, dParent).intern();
            if (sRightID.equals(sID)) {
              needCheckID = false;
            } else {
              Log.debug("** Wrong playlist Id, upgraded: " + sItemName);
              hmWrongRightPlaylistFileID.put(sID, sRightID);
            }
          }
          Playlist plf = PlaylistManager.getInstance().registerPlaylistFile(sRightID,
              sItemName, dParent);
          if (plf != null) {
            plf.populateProperties(attributes);
            dParent.addPlaylistFile(plf);
          }
          break;
        case STAGE_DEVICES:
          device = null;
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(XML_NAME).intern();
          long lType = Long.parseLong(attributes.getValue(XML_TYPE));
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = DeviceManager.createID(sItemName);
            if (sRightID.equals(sID)) {
              needCheckID = false;
            } else {
              Log.debug("** Wrong device Id, upgraded: " + sItemName);
              hmWrongRightDeviceID.put(sID, sRightID);
            }
          }
          String sURL = attributes.getValue(XML_URL).intern();
          device = DeviceManager.getInstance().registerDevice(sRightID, sItemName, lType, sURL);
          if (device != null) {
            device.populateProperties(attributes);
          }
          break;
        case STAGE_YEARS:
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(XML_NAME).intern();
          year = YearManager.getInstance().registerYear(sID, sItemName);
          if (year != null) {
            year.populateProperties(attributes);
          }
          break;
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
