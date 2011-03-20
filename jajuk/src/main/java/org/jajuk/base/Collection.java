/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Items root container.
 */
public final class Collection extends DefaultHandler implements ErrorHandler {

  /** The Constant TAG_CLOSE_NEWLINE. DOCUMENT_ME */
  private static final String TAG_CLOSE_NEWLINE = ">\n";

  /** The Constant TAB_CLOSE_TAG_START. DOCUMENT_ME */
  private static final String TAB_CLOSE_TAG_START = "</";

  /** Self instance. */
  private static Collection coll = new Collection();

  /** DOCUMENT_ME. */
  private static long lTime;

  /** Current ItemManager manager. */
  private ItemManager manager;

  /** upgrade for track IDs. */
  private final Map<String, String> hmWrongRightTrackID = new HashMap<String, String>();

  /** upgrade for album IDs. */
  private final Map<String, String> hmWrongRightAlbumID = new HashMap<String, String>();

  /** upgrade for artist IDs. */
  private final Map<String, String> hmWrongRightArtistID = new HashMap<String, String>();

  /** upgrade for album-artists IDs. */
  private final Map<String, String> hmWrongRightAlbumArtistID = new HashMap<String, String>();

  /** upgrade for genre IDs. */
  private final Map<String, String> hmWrongRightGenreID = new HashMap<String, String>();

  /** upgrade for device IDs. */
  private final Map<String, String> hmWrongRightDeviceID = new HashMap<String, String>();

  /** upgrade for directory IDs. */
  private final Map<String, String> hmWrongRightDirectoryID = new HashMap<String, String>();

  /** upgrade for file IDs. */
  private final Map<String, String> hmWrongRightFileID = new HashMap<String, String>();

  /** upgrade for playlist IDs. */
  private final Map<String, String> hmWrongRightPlaylistFileID = new HashMap<String, String>();

  /** Conversion of types from Jajuk < 1.4 */
  private final static Map<String, String> CONVERSION;
  static {
    CONVERSION = new HashMap<String, String>(12);
    CONVERSION.put("0", "mp3");
    CONVERSION.put("1", "m3u");
    CONVERSION.put("2", "ogg");
    CONVERSION.put("3", "wav");
    CONVERSION.put("4", "au");
    CONVERSION.put("5", "flac");
    CONVERSION.put("6", "wma");
    CONVERSION.put("7", "aac");
    CONVERSION.put("8", "m4a");
    CONVERSION.put("9", "ram");
    CONVERSION.put("10", "mp2");
  }

  /** [Perf] flag used to accelerate conversion. */
  private boolean needCheckConversions = true;

  /** [PERF] Does the type has been checked once for ID computation change ? Indeed, we check only one element of each type to check if this computation changed for perfs. */
  private boolean needCheckID = false;

  // Constants value, use lower value for mist numerous items to parse
  /**
   * DOCUMENT_ME.
   */
  private enum Stage {
    
    /** DOCUMENT_ME. */
    STAGE_NONE,

    /** The Constant STAGE_FILES. DOCUMENT_ME */
    STAGE_FILES,

    /** The Constant STAGE_DIRECTORIES. DOCUMENT_ME */
    STAGE_DIRECTORIES,

    /** The Constant STAGE_TRACKS. DOCUMENT_ME */
    STAGE_TRACKS,

    /** The Constant STAGE_ALBUMS. DOCUMENT_ME */
    STAGE_ALBUMS,

    /** The Constant STAGE_ARTISTS. DOCUMENT_ME */
    STAGE_ARTISTS,

    /** The Constant STAGE_GENRES. DOCUMENT_ME */
    STAGE_GENRES,

    /** The Constant STAGE_PLAYLIST_FILES. DOCUMENT_ME */
    STAGE_PLAYLIST_FILES,

    /** The Constant STAGE_PLAYLISTS. DOCUMENT_ME */
    STAGE_PLAYLISTS,

    /** The Constant STAGE_TYPES. DOCUMENT_ME */
    STAGE_TYPES,

    /** The Constant STAGE_DEVICES. DOCUMENT_ME */
    STAGE_DEVICES,

    /** The Constant STAGE_YEARS. DOCUMENT_ME */
    STAGE_YEARS,

    /** STAGE_ALBUM_ARTIST. */
    STAGE_ALBUM_ARTIST
  }

  /** *************************************************************************** [PERF] provide current stage (files, tracks...) used to optimize switch when parsing the collection ************************************************************************** */
  private Stage stage = Stage.STAGE_NONE;

  /** The Constant ADDITION_FORMATTER. DOCUMENT_ME */
  private static final DateFormat ADDITION_FORMATTER = UtilString.getAdditionDateFormatter();

  /**
   * Instance getter.
   * 
   * @return the instance
   */
  public static Collection getInstance() {
    return coll;
  }

  /**
   * Hidden constructor.
   */
  private Collection() {
    super();
  }

  /**
   * Write current collection to collection file for persistence between
   * sessions.
   * 
   * @param collectionFile DOCUMENT_ME
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static synchronized void commit(File collectionFile) throws IOException {
    long time = System.currentTimeMillis();
    String sCharset = Conf.getString(Const.CONF_COLLECTION_CHARSET);
    final BufferedWriter bw;
    if (collectionFile.getAbsolutePath().endsWith(".zip")) {
      bw = new BufferedWriter(new OutputStreamWriter(new ZipOutputStream(new FileOutputStream(
          collectionFile)), sCharset), 1000000);
    } else {
      bw = new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(collectionFile), sCharset), 1000000);
    }
    try {
      bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n");
      bw.write("<" + Const.XML_COLLECTION + " " + Const.XML_VERSION + "='" + Const.JAJUK_VERSION
          + "'>\n");

      // Devices
      writeItemList(bw, DeviceManager.getInstance().toXML(), DeviceManager.getInstance()
          .getDevices(), DeviceManager.getInstance().getLabel(), 40);
      Log.debug("Devices committed.");

      // Genres
      writeItemList(bw, GenreManager.getInstance().toXML(), GenreManager.getInstance().getGenres(),
          GenreManager.getInstance().getLabel(), 40);
      Log.debug("Genres committed.");

      // Artists
      writeItemList(bw, ArtistManager.getInstance().toXML(), ArtistManager.getInstance()
          .getArtists(), ArtistManager.getInstance().getLabel(), 40);
      Log.debug("Artists committed.");

      // Album artists
      writeItemList(bw, AlbumArtistManager.getInstance().toXML(), AlbumArtistManager.getInstance()
          .getAlbumArtists(), AlbumArtistManager.getInstance().getLabel(), 40);
      Log.debug("Album-artists committed.");

      // Albums
      writeItemList(bw, AlbumManager.getInstance().toXML(), AlbumManager.getInstance().getAlbums(),
          AlbumManager.getInstance().getLabel(), 40);
      Log.debug("Albums committed.");

      // Years
      writeItemList(bw, YearManager.getInstance().toXML(), YearManager.getInstance().getYears(),
          YearManager.getInstance().getLabel(), 40);
      Log.debug("Years committed.");

      // Tracks
      // Cannot use writeItemList() method as we have a bit of special handling inside the loop here
      TrackManager.getInstance().getLock().readLock().lock();
      try {
        ReadOnlyIterator<Track> tracks = TrackManager.getInstance().getTracksIterator();
        bw.write(TrackManager.getInstance().toXML());
        while (tracks.hasNext()) {
          Track track = tracks.next();
          // We clean up all orphan tracks
          if (track.getFiles().size() > 0) {
            bw.write(track.toXml());
          }
        }
      } finally {
        TrackManager.getInstance().getLock().readLock().unlock();
      }
      writeString(bw, TrackManager.getInstance().getLabel(), 200);
      Log.debug("Tracks committed.");

      // Directories
      writeItemList(bw, DirectoryManager.getInstance().toXML(), DirectoryManager.getInstance()
          .getDirectories(), DirectoryManager.getInstance().getLabel(), 100);
      Log.debug("Directories committed.");

      // Files
      writeItemList(bw, FileManager.getInstance().toXML(), FileManager.getInstance().getFiles(),
          FileManager.getInstance().getLabel(), 200);
      Log.debug("Files committed.");

      // Playlists
      writeItemList(bw, PlaylistManager.getInstance().toXML(), PlaylistManager.getInstance()
          .getPlaylists(), PlaylistManager.getInstance().getLabel(), 200);
      Log.debug("Playlists committed.");

      // end of collection
      bw.write("</" + Const.XML_COLLECTION + TAG_CLOSE_NEWLINE);
      bw.flush();
    } finally {
      bw.close();
    }
    Log.debug("Collection commited in " + (System.currentTimeMillis() - time) + " ms");
  }

  /**
   * Write item list. DOCUMENT_ME
   * 
   * @param bw DOCUMENT_ME
   * @param header DOCUMENT_ME
   * @param items DOCUMENT_ME
   * @param footer DOCUMENT_ME
   * @param buffer DOCUMENT_ME
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void writeItemList(BufferedWriter bw, String header, List<? extends Item> items,
      String footer, int buffer) throws IOException {
    bw.write(header);
    for (Item item : items) {
      bw.write(item.toXml());
    }

    writeString(bw, footer, buffer);
  }

  /**
   * Write string. DOCUMENT_ME
   * 
   * @param bw DOCUMENT_ME
   * @param toWrite DOCUMENT_ME
   * @param buffer DOCUMENT_ME
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void writeString(BufferedWriter bw, String toWrite, int buffer) throws IOException {
    StringBuilder sb = new StringBuilder(buffer);
    sb.append(TAB_CLOSE_TAG_START);
    sb.append(toWrite);
    sb.append(TAG_CLOSE_NEWLINE);
    bw.write(sb.toString());
  }

  /**
   * Parse collection.xml file and put all collection information into memory
   * 
   * @param file DOCUMENT_ME
   * 
   * @throws SAXException the SAX exception
   * @throws ParserConfigurationException the parser configuration exception
   * @throws JajukException the jajuk exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void load(File file) throws SAXException, ParserConfigurationException,
      JajukException, IOException {
    Log.debug("Loading: " + file.getName());
    lTime = System.currentTimeMillis();
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(false);
    spf.setNamespaceAware(false);
    // See http://xerces.apache.org/xerces-j/features.html for details
    spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    spf.setFeature("http://xml.org/sax/features/string-interning", true);
    SAXParser saxParser = spf.newSAXParser();
    if (!file.exists()) {
      throw new JajukException(5, file.toString());
    }
    if (file.getAbsolutePath().endsWith(".zip")) {
      InputSource input = new InputSource(new ZipInputStream(new FileInputStream(file)));
      saxParser.parse(input, getInstance());
    } else {
      saxParser.parse(file.toURI().toURL().toString(), getInstance());
    }
  }

  /**
   * Perform a collection clean up for logical items ( delete orphan data ) Note
   * that we don't cleanup genres up because we want to keep genres even without
   * associated tracks for ambiences for instance.
   */
  public static synchronized void cleanupLogical() {
    // Tracks cleanup
    TrackManager.getInstance().cleanup();
    // Artists cleanup
    ArtistManager.getInstance().cleanup();
    // Album-artist cleanup
    AlbumArtistManager.getInstance().cleanup();
    // albums cleanup
    AlbumManager.getInstance().cleanup();
    // years cleanup
    YearManager.getInstance().cleanup();
  }

  /**
   * Clear the full collection Note that we don't clear TypeManager as it is not
   * read from a file but filled programmatically.
   */
  public static synchronized void clearCollection() {
    TrackManager.getInstance().clear();
    GenreManager.getInstance().clear();
    ArtistManager.getInstance().clear();
    AlbumArtistManager.getInstance().clear();
    AlbumManager.getInstance().clear();
    YearManager.getInstance().clear();
    FileManager.getInstance().clear();
    DirectoryManager.getInstance().clear();
    PlaylistManager.getInstance().clear();
    DeviceManager.getInstance().clear();
  }

  /**
   * parsing warning.
   * 
   * @param spe DOCUMENT_ME
   * 
   * @throws SAXException the SAX exception
   * 
   * @exception SAXException
   */
  @Override
  public void warning(SAXParseException spe) throws SAXException {
    throw new SAXException(Messages.getErrorMessage(5) + " / " + spe.getSystemId() + "/"
        + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
  }

  /**
   * parsing error.
   * 
   * @param spe DOCUMENT_ME
   * 
   * @throws SAXException the SAX exception
   * 
   * @exception SAXException
   */
  @Override
  public void error(SAXParseException spe) throws SAXException {
    throw new SAXException(Messages.getErrorMessage(5) + " / " + spe.getSystemId() + "/"
        + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
  }

  /**
   * parsing fatal error.
   * 
   * @param spe DOCUMENT_ME
   * 
   * @throws SAXException the SAX exception
   * 
   * @exception SAXException
   */
  @Override
  public void fatalError(SAXParseException spe) throws SAXException {
    throw new SAXException(Messages.getErrorMessage(5) + " / " + spe.getSystemId() + "/"
        + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
  }

  /**
   * Called at parsing start.
   */
  @Override
  public void startDocument() {
    Log.debug("Starting collection file parsing...");
  }

  /**
   * Called at parsing end.
   */
  @Override
  public void endDocument() {
    long l = (System.currentTimeMillis() - lTime);
    Log.debug("Collection file parsing done : " + l + " ms");
  }

  /**
   * Called when we start an element intern() method use policy : we use this
   * method when adding a new string into JVM that will probably be referenced
   * by several objects like the Genre ID that is referenced by many tracks. In
   * this case, all the String objects share the same char[]. On another hand,
   * it musn't be used for strings that have low probability to be used several
   * times (like raw names) as it uses a lot of CPU (equals() is called) and we
   * want startup to be as fast as possible. Note that the use of intern() save
   * around 1/4 of overall heap memory
   * 
   * We use sax-interning for the main items sections (<styles> for ie). For all
   * raw items, we don't perform equals on item name but we compare the string
   * hashcode
   * 
   * @param sUri DOCUMENT_ME
   * @param s DOCUMENT_ME
   * @param sQName DOCUMENT_ME
   * @param attributes DOCUMENT_ME
   * 
   * @throws SAXException the SAX exception
   */
  @Override
  public void startElement(String sUri, String s, String sQName, Attributes attributes)
      throws SAXException {
    try {
      int idIndex = attributes.getIndex(Const.XML_ID);
      // [PERF] Manage top tags to set current stage. Manages 'properties'
      // tags as well
      if (idIndex == -1) {
        if (Const.XML_DEVICES == sQName) {
          manager = DeviceManager.getInstance();
          stage = Stage.STAGE_DEVICES;
          needCheckID = true;
        } else if (Const.XML_ALBUMS == sQName) {
          manager = AlbumManager.getInstance();
          stage = Stage.STAGE_ALBUMS;
          needCheckID = true;
        } else if (Const.XML_ARTISTS == sQName) {
          manager = ArtistManager.getInstance();
          stage = Stage.STAGE_ARTISTS;
          needCheckID = true;
        } else if (Const.XML_ALBUM_ARTISTS == sQName) {
          manager = AlbumArtistManager.getInstance();
          stage = Stage.STAGE_ALBUM_ARTIST;
          needCheckID = true;
        } else if (Const.XML_DIRECTORIES == sQName) {
          manager = DirectoryManager.getInstance();
          stage = Stage.STAGE_DIRECTORIES;
          needCheckID = true;
        } else if (Const.XML_FILES == sQName) {
          manager = FileManager.getInstance();
          stage = Stage.STAGE_FILES;
          needCheckID = true;
        } else if (Const.XML_PLAYLISTS == sQName) {
          // This code is here for Jajuk < 1.6 compatibility
          manager = PlaylistManager.getInstance();
          stage = Stage.STAGE_PLAYLISTS;
          needCheckID = true;
        } else if (Const.XML_PLAYLIST_FILES == sQName) {
          manager = PlaylistManager.getInstance();
          stage = Stage.STAGE_PLAYLIST_FILES;
          needCheckID = true;
        } else if (Const.XML_GENRES == sQName) {
          manager = GenreManager.getInstance();
          stage = Stage.STAGE_GENRES;
          needCheckID = true;
        } else if (Const.XML_TRACKS == sQName) {
          manager = TrackManager.getInstance();
          stage = Stage.STAGE_TRACKS;
          needCheckID = true;
        } else if (Const.XML_YEARS == sQName) {
          manager = YearManager.getInstance();
          stage = Stage.STAGE_YEARS;
          needCheckID = true;
        } else if (Const.XML_TYPES == sQName) {
          // This is here for pre-1.7 collections, after we don't commit types
          // anymore (they are set programmatically)
          manager = TypeManager.getInstance();
          stage = Stage.STAGE_TYPES;
          needCheckID = false;
        } else if (Const.XML_PROPERTY == sQName) {
          // A property description
          boolean bCustom = Boolean.parseBoolean(attributes.getValue(attributes
              .getIndex(Const.XML_CUSTOM)));
          boolean bConstructor = Boolean.parseBoolean(attributes.getValue(attributes
              .getIndex(Const.XML_CONSTRUCTOR)));
          boolean bShouldBeDisplayed = Boolean.parseBoolean(attributes.getValue(attributes
              .getIndex(Const.XML_VISIBLE)));
          boolean bEditable = Boolean.parseBoolean(attributes.getValue(attributes
              .getIndex(Const.XML_EDITABLE)));
          boolean bUnique = Boolean.parseBoolean(attributes.getValue(attributes
              .getIndex(Const.XML_UNIQUE)));
          Class<?> cType = Class.forName(attributes.getValue(Const.XML_TYPE));
          String sDefaultValue = attributes.getValue(Const.XML_DEFAULT_VALUE).intern();
          Object oDefaultValue = null;
          if (sDefaultValue != null && sDefaultValue.length() > 0) {
            try {
              // Date format has changed from 1.3 (only yyyyMMdd
              // addition format is used)
              // so an exception will be thrown when upgrading
              // from 1.2
              // we reset default value to "today"
              oDefaultValue = UtilString.parse(sDefaultValue, cType);
            } catch (Exception e) {
              oDefaultValue = new Date();
            }
          }
          String sPropertyName = attributes.getValue(Const.XML_NAME).intern();
          if (manager.getMetaInformation(sPropertyName) == null) {
            PropertyMetaInformation meta = new PropertyMetaInformation(sPropertyName, bCustom,
                bConstructor, bShouldBeDisplayed, bEditable, bUnique, cType, oDefaultValue);
            // standard properties are already loaded
            manager.registerProperty(meta);
          }
        }

        if (Const.XML_PROPERTY == sQName) {
          Log.debug("Found property: " + attributes.getValue(Const.XML_NAME));
        } else {
          Log.debug("Starting stage: '" + stage + "' with property: '" + sQName + "' manager: "
              + (manager != null ? manager.getLabel() : "<null>"));
        }
      } else {
        // Manage elements themselves using a switch for performances
        switch (stage) {
        case STAGE_FILES:
          handleFiles(attributes, idIndex);
          break;
        case STAGE_DIRECTORIES:
          handleDirectories(attributes, idIndex);
          break;
        case STAGE_TRACKS:
          handleTracks(attributes, idIndex);
          break;
        case STAGE_ALBUMS:
          handleAlbums(attributes, idIndex);
          break;
        case STAGE_ARTISTS:
          handleArtists(attributes, idIndex);
          break;
        case STAGE_ALBUM_ARTIST:
          handleAlbumArtists(attributes, idIndex);
          break;
        case STAGE_GENRES:
          handleGenres(attributes, idIndex);
          break;
        case STAGE_PLAYLIST_FILES:
          handlePlaylistFiles(attributes, idIndex);
          break;
        case STAGE_DEVICES:
          handleDevices(attributes, idIndex);
          break;
        case STAGE_YEARS:
          handleYears(attributes, idIndex);
          break;
        case STAGE_TYPES:
          Log.warn("Unexpected Stage: STAGE_TYPES");
          break;
        default:
          Log.warn("Unexpected Stage: " + stage);
        }
      }
    } catch (Throwable e) {
      // Make sure to catch every issue here (including runtime exceptions) so we make sure to start
      // jajuk
      StringBuilder sAttributes = new StringBuilder();
      for (int i = 0; i < attributes.getLength(); i++) {
        sAttributes.append('\n').append(attributes.getQName(i)).append('=').append(
            attributes.getValue(i));
      }
      Log.error(5, sAttributes.toString(), e);
    }
  }

  /**
   * Handle files. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handleFiles(Attributes attributes, int idIndex) {
    String sItemName = attributes.getValue(Const.XML_NAME);
    // Check file type is still registered, it can be
    // useful for ie if mplayer is no more available
    String ext = UtilSystem.getExtension(sItemName);
    Type type = TypeManager.getInstance().getTypeByExtension(ext);
    if (type == null) {
      return;
    }
    String sTrackId = attributes.getValue(Const.XML_TRACK).intern();
    // UPGRADE check if track Id is right
    if ((hmWrongRightTrackID.size() > 0) &&
    // replace wrong by right ID
        (hmWrongRightTrackID.containsKey(sTrackId))) {
      sTrackId = hmWrongRightTrackID.get(sTrackId);
    }
    Track track = TrackManager.getInstance().getTrackByID(sTrackId);
    String sParentID = attributes.getValue(Const.XML_DIRECTORY).intern();
    // UPGRADE check parent ID is right
    if ((hmWrongRightDirectoryID.size() > 0) &&
    // replace wrong by right ID
        (hmWrongRightDirectoryID.containsKey(sParentID))) {
      sParentID = hmWrongRightDirectoryID.get(sParentID);
    }
    Directory dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
    if (dParent == null || track == null) { // more checkups
      return;
    }

    String size = attributes.getValue(Const.XML_SIZE);
    long lSize = 0;
    if (size != null) {
      lSize = Long.parseLong(size);
    }

    // Quality analyze, handle format problems (mainly for
    // upgrades)
    long lQuality = 0;
    try {
      String sQuality = attributes.getValue(Const.XML_QUALITY);
      if (sQuality != null) {
        lQuality = UtilString.fastLongParser(sQuality);
      }
    } catch (Exception e) {
      if (Log.isDebugEnabled()) {
        // wrong format
        Log.debug(Messages.getString("Error.137") + ":" + sItemName + " Value: "
            + attributes.getValue(Const.XML_QUALITY) + " Error:" + e.getMessage());
      }
    }
    String sID = attributes.getValue(idIndex).intern();
    /*
     * UPGRADE test : if first element we check has the right ID, we avoid wasting time checking
     * others item one. If is is an upgrade, we force the check.We always check id in debug mode.
     */
    String sRightID = sID;
    if (needCheckID) {
      sRightID = FileManager.createID(sItemName, dParent).intern();
      if (sRightID == sID) {
        needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
      } else {
        Log.debug("** Wrong file Id, upgraded: " + sItemName);
        hmWrongRightFileID.put(sID, sRightID);
      }
    }
    org.jajuk.base.File file = FileManager.getInstance().registerFile(sRightID, sItemName, dParent,
        track, lSize, lQuality);
    file.populateProperties(attributes);
  }

  /**
   * Handle directories. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handleDirectories(Attributes attributes, int idIndex) {
    Directory dParent = null;

    // dParent = null;
    String sParentID = attributes.getValue(Const.XML_DIRECTORY_PARENT).intern();
    // UPGRADE
    if ((hmWrongRightDirectoryID.size() > 0) && (hmWrongRightDirectoryID.containsKey(sParentID))) {
      sParentID = hmWrongRightDirectoryID.get(sParentID);
    }
    // We use intern() here for performances
    if (sParentID != "-1") {
      // Parent directory should be already referenced
      // because of order conservation
      dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
      // check parent directory exists
      if (dParent == null) {
        return;
      }
    }
    String sDeviceID = attributes.getValue(Const.XML_DEVICE).intern();
    // take upgraded device ID if needed
    if ((hmWrongRightDeviceID.size() > 0) && (hmWrongRightDeviceID.containsKey(sDeviceID))) {
      sDeviceID = hmWrongRightDeviceID.get(sDeviceID);
    }
    Device device = DeviceManager.getInstance().getDeviceByID(sDeviceID);
    if (device == null) { // check device exists
      return;
    }
    String sItemName = attributes.getValue(Const.XML_NAME);
    String sID = attributes.getValue(idIndex).intern();
    // UPGRADE test
    String sRightID = sID;
    if (needCheckID) {
      sRightID = DirectoryManager.createID(sItemName, device, dParent).intern();
      if (sRightID == sID) {
        needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
      } else {
        Log.debug("** Wrong directory Id, upgraded: " + sItemName);
        hmWrongRightDirectoryID.put(sID, sRightID);
      }
    }
    Directory directory = DirectoryManager.getInstance().registerDirectory(sRightID, sItemName,
        dParent, device);
    directory.populateProperties(attributes);

    // also remember top-level directories at the device
    if (dParent == null) {
      device.addDirectory(directory);
    }
  }

  /**
   * Handle tracks. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   * 
   * @throws ParseException the parse exception
   */
  private void handleTracks(Attributes attributes, int idIndex) throws ParseException {
    String sID = attributes.getValue(idIndex).intern();
    String sTrackName = attributes.getValue(Const.XML_TRACK_NAME);
    // album
    String sAlbumID = attributes.getValue(Const.XML_TRACK_ALBUM).intern();
    if ((hmWrongRightAlbumID.size() > 0) && (hmWrongRightAlbumID.containsKey(sAlbumID))) {
      sAlbumID = hmWrongRightAlbumID.get(sAlbumID);
    }
    Album album = AlbumManager.getInstance().getAlbumByID(sAlbumID);
    // Genre
    String sGenreID = attributes.getValue(Const.XML_TRACK_GENRE).intern();
    if ((hmWrongRightGenreID.size() > 0) && (hmWrongRightGenreID.containsKey(sGenreID))) {
      sGenreID = hmWrongRightGenreID.get(sGenreID);
    }
    Genre genre = GenreManager.getInstance().getGenreByID(sGenreID);
    // Year
    String sYearID = attributes.getValue(Const.XML_TRACK_YEAR).intern();
    Year year = YearManager.getInstance().getYearByID(sYearID);
    // For jajuk < 1.4
    if (year == null) {
      year = YearManager.getInstance().registerYear(sYearID, sYearID);
    }
    // Artist
    String sArtistID = attributes.getValue(Const.XML_TRACK_ARTIST).intern();
    if ((hmWrongRightArtistID.size() > 0) && (hmWrongRightArtistID.containsKey(sArtistID))) {
      sArtistID = hmWrongRightArtistID.get(sArtistID);
    }
    Artist artist = ArtistManager.getInstance().getArtistByID(sArtistID);

    // Album-artist (not a constructor level property)
    String sAlbumArtist = attributes.getValue(Const.XML_ALBUM_ARTIST);
    if (StringUtils.isNotBlank(sAlbumArtist)) {
      sAlbumArtist = sAlbumArtist.intern();
    }
    if ((hmWrongRightAlbumArtistID.size() > 0)
        && (hmWrongRightAlbumArtistID.containsKey(sAlbumArtist))) {
      sAlbumArtist = hmWrongRightAlbumArtistID.get(sAlbumArtist);
    }
    // Note that when upgrading from jajuk < 1.9, album artists field is alway null, call on the
    // next line always return null
    AlbumArtist albumArtist = AlbumArtistManager.getInstance().getAlbumArtistByID(sAlbumArtist);
    if (albumArtist == null) {
      // we force album artist to this default, a deep scan will be required to get actual values
      albumArtist = AlbumArtistManager.getInstance().registerAlbumArtist(Const.UNKNOWN_ARTIST);
    }

    // Length
    long length = UtilString.fastLongParser(attributes.getValue(Const.XML_TRACK_LENGTH));
    // Type
    String typeID = attributes.getValue(Const.XML_TYPE).intern();
    if (needCheckConversions) {
      if (CONVERSION.containsKey(typeID)) {
        typeID = CONVERSION.get(typeID);
      } else {
        needCheckConversions = false;
      }
    }
    Type type = TypeManager.getInstance().getTypeByID(typeID);
    // more checkups
    if (album == null || artist == null) {
      return;
    }
    if (genre == null || type == null) {
      return;
    }
    // Idem for order
    long lOrder = 0l;
    try {
      lOrder = UtilString.fastLongParser(attributes.getValue(Const.XML_TRACK_ORDER));
    } catch (Exception e) {
      if (Log.isDebugEnabled()) {
        // wrong format
        Log.debug(Messages.getString("Error.137") + ":" + sTrackName); // wrong
      }
    }

    // Idem for disc number
    long lDiscNumber = 0l;
    if (attributes.getValue(Const.XML_TRACK_DISC_NUMBER) != null) {
      try {
        lDiscNumber = UtilString.fastLongParser(attributes.getValue(Const.XML_TRACK_DISC_NUMBER));
      } catch (Exception e) {
        if (Log.isDebugEnabled()) {
          // wrong format
          Log.debug(Messages.getString("Error.137") + ":" + sTrackName);
        }
      }
    }

    // UPGRADE test
    String sRightID = sID;
    if (needCheckID) {
      sRightID = TrackManager.createID(sTrackName, album, genre, artist, length, year, lOrder,
          type, lDiscNumber).intern();
      if (sRightID == sID) {
        needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
      } else {
        Log.debug("** Wrong Track Id, upgraded: " + sTrackName);
        hmWrongRightTrackID.put(sID, sRightID);
      }
    }
    Track track = TrackManager.getInstance().registerTrack(sRightID, sTrackName, album, genre,
        artist, length, year, lOrder, type, lDiscNumber);
    TrackManager.getInstance().changeTrackRate(track,
        UtilString.fastLongParser(attributes.getValue(Const.XML_TRACK_RATE)));
    track.setHits(UtilString.fastLongParser(attributes.getValue(Const.XML_TRACK_HITS)));
    // only set discovery date if it is available in the file
    if (attributes.getValue(Const.XML_TRACK_DISCOVERY_DATE) != null) {
      // Date format should be OK
      Date dAdditionDate = ADDITION_FORMATTER.parse(attributes
          .getValue(Const.XML_TRACK_DISCOVERY_DATE));
      track.setDiscoveryDate(dAdditionDate);
    }

    String sComment = attributes.getValue(Const.XML_TRACK_COMMENT);
    if (sComment == null) {
      sComment = "";
    }
    track.setComment(sComment.intern());
    track.setAlbumArtist(albumArtist);
    track.populateProperties(attributes);
  }

  /**
   * Handle albums. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handleAlbums(Attributes attributes, int idIndex) {
    String sID = attributes.getValue(idIndex).intern();
    String sItemName = attributes.getValue(Const.XML_NAME).intern();
    String sAttributeAlbumArtist = attributes.getValue(Const.XML_ALBUM_ARTIST);
    if (sAttributeAlbumArtist != null) {
      sAttributeAlbumArtist = sAttributeAlbumArtist.intern();
    }
    long lItemDiscID = 0;
    String sAttributeDiskId = attributes.getValue(Const.XML_ALBUM_DISC_ID);
    if (sAttributeDiskId != null) {
      lItemDiscID = Long.parseLong(sAttributeDiskId);
    }
    // UPGRADE test
    String sRightID = sID;
    if (needCheckID) {
      sRightID = AlbumManager.createID(sItemName, lItemDiscID).intern();
      if (sRightID == sID) {
        needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
      } else {
        Log.debug("** Wrong album Id, upgraded: " + sItemName);
        hmWrongRightAlbumID.put(sID, sRightID);
      }
    }
    Album album = AlbumManager.getInstance().registerAlbum(sRightID, sItemName, lItemDiscID);
    if (album != null) {
      album.populateProperties(attributes);
    }
  }

  /**
   * Handle artists. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handleArtists(Attributes attributes, int idIndex) {
    String sID = attributes.getValue(idIndex).intern();
    String sItemName = attributes.getValue(Const.XML_NAME).intern();
    // UPGRADE test
    String sRightID = sID;
    if (needCheckID) {
      sRightID = ArtistManager.createID(sItemName).intern();
      if (sRightID == sID) {
        needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
      } else {
        Log.debug("** Wrong artist Id, upgraded: " + sItemName);
        hmWrongRightArtistID.put(sID, sRightID);
      }
    }
    Artist artist = ArtistManager.getInstance().registerArtist(sRightID, sItemName);
    if (artist != null) {
      artist.populateProperties(attributes);
    }
  }

  /**
   * Handle genres. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handleGenres(Attributes attributes, int idIndex) {
    String sID = attributes.getValue(idIndex).intern();
    String sItemName = attributes.getValue(Const.XML_NAME).intern();
    // UPGRADE test
    String sRightID = sID;
    if (needCheckID) {
      sRightID = GenreManager.createID(sItemName).intern();
      if (sRightID == sID) {
        needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
      } else {
        Log.debug("** Wrong genre Id, upgraded: " + sItemName);
        hmWrongRightGenreID.put(sID, sRightID);
      }
    }
    Genre genre = GenreManager.getInstance().registerGenre(sRightID, sItemName);
    if (genre != null) {
      genre.populateProperties(attributes);
    }
  }

  /**
   * Handle playlist files. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handlePlaylistFiles(Attributes attributes, int idIndex) {
    String sParentID = attributes.getValue(Const.XML_DIRECTORY).intern();
    // UPGRADE check parent ID is right
    if ((hmWrongRightDirectoryID.size() > 0) &&
    // replace wrong by right ID
        (hmWrongRightDirectoryID.containsKey(sParentID))) {
      sParentID = hmWrongRightDirectoryID.get(sParentID);
    }
    Directory dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
    if (dParent == null) { // check directory is exists
      return;
    }
    String sID = attributes.getValue(idIndex).intern();
    String sItemName = attributes.getValue(Const.XML_NAME);
    // UPGRADE test
    String sRightID = sID;
    if (needCheckID) {
      sRightID = PlaylistManager.createID(sItemName, dParent).intern();
      if (sRightID == sID) {
        needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
      } else {
        Log.debug("** Wrong playlist Id, upgraded: " + sItemName);
        hmWrongRightPlaylistFileID.put(sID, sRightID);
      }
    }
    Playlist plf = PlaylistManager.getInstance().registerPlaylistFile(sRightID, sItemName, dParent);
    if (plf != null) {
      plf.populateProperties(attributes);
    }
  }

  /**
   * Handle devices. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handleDevices(Attributes attributes, int idIndex) {
    String sID = attributes.getValue(idIndex).intern();
    String sItemName = attributes.getValue(Const.XML_NAME);
    long lType = UtilString.fastLongParser(attributes.getValue(Const.XML_TYPE));
    // UPGRADE test
    String sRightID = sID;
    if (needCheckID) {
      sRightID = DeviceManager.createID(sItemName).intern();
      if (sRightID == sID) {
        needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
      } else {
        Log.debug("** Wrong device Id, upgraded: " + sItemName);
        hmWrongRightDeviceID.put(sID, sRightID);
      }
    }
    String sURL = attributes.getValue(Const.XML_URL);
    Device device = DeviceManager.getInstance().registerDevice(sRightID, sItemName, lType, sURL);
    if (device != null) {
      device.populateProperties(attributes);
    }
  }

  /**
   * Handle years. DOCUMENT_ME
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handleYears(Attributes attributes, int idIndex) {
    String sID = attributes.getValue(idIndex).intern();
    String sItemName = attributes.getValue(Const.XML_NAME).intern();
    Year year = YearManager.getInstance().registerYear(sID, sItemName);
    if (year != null) {
      year.populateProperties(attributes);
    }
  }

  /**
   * Handle album artists.
   * 
   * @param attributes DOCUMENT_ME
   * @param idIndex DOCUMENT_ME
   */
  private void handleAlbumArtists(Attributes attributes, int idIndex) {
    String sID = attributes.getValue(idIndex).intern();
    String sItemName = attributes.getValue(Const.XML_NAME).intern();
    AlbumArtist albumArtist = AlbumArtistManager.getInstance().registerAlbumArtist(sID, sItemName);
    if (albumArtist != null) {
      albumArtist.populateProperties(attributes);
    }
  }

  /**
   * Gets the hm wrong right file id.
   * 
   * @return list of wrong file id (used by history)
   */
  public Map<String, String> getHmWrongRightFileID() {
    return hmWrongRightFileID;
  }

  /**
   * Gets the wrong right album i ds.
   * 
   * @return the wrong right album i ds
   */
  public Map<String, String> getWrongRightAlbumIDs() {
    return this.hmWrongRightAlbumID;
  }
}
