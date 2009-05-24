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
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.services.core.ExitService;
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
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Items root container
 */
public final class Collection extends DefaultHandler implements ErrorHandler {

  private static final String TAG_CLOSE_NEWLINE = ">\n";

  private static final String TAB_CLOSE_TAG_START = "</";

  /** Self instance */
  private static Collection collection = new Collection();

  private static long lTime;

  /** Current ItemManager manager */
  private ItemManager manager;

  /** upgrade for track IDs */
  private final Map<String, String> hmWrongRightTrackID = new HashMap<String, String>();

  /** upgrade for album IDs */
  private final Map<String, String> hmWrongRightAlbumID = new HashMap<String, String>();

  /** upgrade for author IDs */
  private final Map<String, String> hmWrongRightAuthorID = new HashMap<String, String>();

  /** upgrade for style IDs */
  private final Map<String, String> hmWrongRightStyleID = new HashMap<String, String>();

  /** upgrade for device IDs */
  private final Map<String, String> hmWrongRightDeviceID = new HashMap<String, String>();

  /** upgrade for directory IDs */
  private final Map<String, String> hmWrongRightDirectoryID = new HashMap<String, String>();

  /** upgrade for file IDs */
  private final Map<String, String> hmWrongRightFileID = new HashMap<String, String>();

  /** upgrade for playlist IDs */
  private final Map<String, String> hmWrongRightPlaylistFileID = new HashMap<String, String>();

  /** Conversion of types from < 1.4 */
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
  /** [Perf] flag used to accelerate conversion */
  private boolean needCheckConversions = true;

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

  private static final DateFormat ADDITION_FORMATTER = UtilString.getAdditionDateFormatter();

  /** Auto commit thread */
  private static Thread tAutoCommit = new Thread("Collection Auto Commit Thread") {
    @Override
    public void run() {
      while (!ExitService.isExiting()) {
        try {
          Thread.sleep(Const.AUTO_COMMIT_DELAY);
          Log.debug("Auto commit");
          // commit collection at each refresh (can be useful
          // if application is closed brutally with control-C or
          // shutdown and that exit hook have no time to perform
          // commit)
          org.jajuk.base.Collection.commit(SessionService.getConfFileByPath(Const.FILE_COLLECTION));
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }
  };

  /** Instance getter */
  public static Collection getInstance() {
    return collection;
  }

  /** Hidden constructor */
  private Collection() {
    super();
  }

  /**
   * Write current collection to collection file for persistence between
   * sessions
   */
  public static void commit(File collectionFile) throws IOException {
    long time = System.currentTimeMillis();
    String sCharset = Conf.getString(Const.CONF_COLLECTION_CHARSET);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
        collectionFile), sCharset), 1000000);
    try {
      bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n");
      bw.write("<" + Const.XML_COLLECTION + " " + Const.XML_VERSION + "='" + Const.JAJUK_VERSION
          + "'>\n");

      // Devices
      writeItemList(bw, DeviceManager.getInstance().toXML(), DeviceManager.getInstance()
          .getDevicesIterator(), DeviceManager.getInstance().getLabel(), 40);

      // Styles
      writeItemList(bw, StyleManager.getInstance().toXML(), StyleManager.getInstance()
          .getStylesIterator(), StyleManager.getInstance().getLabel(), 40);

      // Authors
      writeItemList(bw, AuthorManager.getInstance().toXML(), AuthorManager.getInstance()
          .getAuthorsIterator(), AuthorManager.getInstance().getLabel(), 40);

      // Albums
      writeItemList(bw, AlbumManager.getInstance().toXML(), AlbumManager.getInstance()
          .getAlbumsIterator(), AlbumManager.getInstance().getLabel(), 40);

      // Years
      writeItemList(bw, YearManager.getInstance().toXML(), YearManager.getInstance()
          .getYearsIterator(), YearManager.getInstance().getLabel(), 40);

      // Tracks
      // Cannot use method as we have a bit of special handling inside the loop
      // here
      // writeItemList(bw, TrackManager.getInstance().toXML(),
      // TrackManager.getInstance().getTracksIterator(),
      // TrackManager.getInstance().getLabel(), 200);
      ReadOnlyIterator<Track> tracks = TrackManager.getInstance().getTracksIterator();
      bw.write(TrackManager.getInstance().toXML());
      while (tracks.hasNext()) {
        Track track = tracks.next();
        // We clean up all orphan tracks
        if (track.getFiles().size() > 0) {
          bw.write(track.toXml());
        }
      }
      writeString(bw, TrackManager.getInstance().getLabel(), 200);

      // Directories
      writeItemList(bw, DirectoryManager.getInstance().toXML(), DirectoryManager.getInstance()
          .getDirectoriesIterator(), DirectoryManager.getInstance().getLabel(), 100);

      // Files
      writeItemList(bw, FileManager.getInstance().toXML(), FileManager.getInstance()
          .getFilesIterator(), FileManager.getInstance().getLabel(), 200);

      // Playlists
      writeItemList(bw, PlaylistManager.getInstance().toXML(), PlaylistManager.getInstance()
          .getPlaylistsIterator(), PlaylistManager.getInstance().getLabel(), 200);

      // end of collection
      bw.write("</" + Const.XML_COLLECTION + TAG_CLOSE_NEWLINE);
      bw.flush();
    } finally {
      bw.close();
    }
    Log.debug("Collection commited in " + (System.currentTimeMillis() - time) + " ms");
  }

  private static void writeItemList(BufferedWriter bw, String header,
      ReadOnlyIterator<? extends Item> items, String footer, int buffer) throws IOException {
    bw.write(header);

    while (items.hasNext()) {
      bw.write(items.next().toXml());
    }

    writeString(bw, footer, buffer);
  }

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
   * @throws SAXException
   * @throws ParserConfigurationException
   * @throws JajukException
   * @throws IOException
   * @throws MalformedURLException
   * 
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
      throw new JajukException(5);
    }
    saxParser.parse(file.toURI().toURL().toString(), getInstance());
    // start auto commit thread
    tAutoCommit.start();
  }

  /**
   * Perform a collection clean up for logical items ( delete orphan data ) Note
   * that we don't cleanup styles up because we want to keep styles even without
   * associated tracks for ambiences for instance
   */
  public static synchronized void cleanupLogical() {
    // Tracks cleanup
    TrackManager.getInstance().cleanup();
    // Authors cleanup
    AuthorManager.getInstance().cleanup();
    // albums cleanup
    AlbumManager.getInstance().cleanup();
    // years cleanup
    YearManager.getInstance().cleanup();
  }

  /**
   * Clear the full collection Note that we don't clear TypeManager as it is not
   * read from a file but filled programmatically
   */
  public static synchronized void clearCollection() {
    TrackManager.getInstance().clear();
    StyleManager.getInstance().clear();
    AuthorManager.getInstance().clear();
    AlbumManager.getInstance().clear();
    YearManager.getInstance().clear();
    FileManager.getInstance().clear();
    DirectoryManager.getInstance().clear();
    PlaylistManager.getInstance().clear();
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
   * Called when we start an element intern() method use policy : we use this
   * method when adding a new string into JVM that will probably be referenced
   * by several objects like the Style ID that is referenced by many tracks. In
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
          stage = STAGE_DEVICES;
          needCheckID = true;
        } else if (Const.XML_ALBUMS == sQName) {
          manager = AlbumManager.getInstance();
          stage = STAGE_ALBUMS;
          needCheckID = true;
        } else if (Const.XML_AUTHORS == sQName) {
          manager = AuthorManager.getInstance();
          stage = STAGE_AUTHORS;
          needCheckID = true;
        } else if (Const.XML_DIRECTORIES == sQName) {
          manager = DirectoryManager.getInstance();
          stage = STAGE_DIRECTORIES;
          needCheckID = true;
        } else if (Const.XML_FILES == sQName) {
          manager = FileManager.getInstance();
          stage = STAGE_FILES;
          needCheckID = true;
        } else if (Const.XML_PLAYLISTS == sQName) {
          // This code is here for Jajuk < 1.6 compatibility
          manager = PlaylistManager.getInstance();
          stage = STAGE_PLAYLISTS;
          needCheckID = true;
        } else if (Const.XML_PLAYLIST_FILES == sQName) {
          manager = PlaylistManager.getInstance();
          stage = STAGE_PLAYLIST_FILES;
          needCheckID = true;
        } else if (Const.XML_STYLES == sQName) {
          manager = StyleManager.getInstance();
          stage = STAGE_STYLES;
          needCheckID = true;
        } else if (Const.XML_TRACKS == sQName) {
          manager = TrackManager.getInstance();
          stage = STAGE_TRACKS;
          needCheckID = true;
        } else if (Const.XML_YEARS == sQName) {
          manager = YearManager.getInstance();
          stage = STAGE_YEARS;
          needCheckID = true;
        } else if (Const.XML_TYPES == sQName) {
          // This is here for pre-1.7 collections, after we don't commit types
          // anymore (they are set programmatically)
          manager = TypeManager.getInstance();
          stage = STAGE_TYPES;
          needCheckID = false;
        } else if (Const.XML_PROPERTY == sQName) {
          // A property description
          String sPropertyName = attributes.getValue(Const.XML_NAME).intern();
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
        String sRightID = null;
        long lSize = 0;
        switch (stage) {
        case STAGE_FILES:
          Type type = null;
          Directory dParent = null;
          Track track = null;
          sItemName = attributes.getValue(Const.XML_NAME);
          // Check file type is still registered, it can be
          // useful for ie if mplayer is no more available
          String ext = UtilSystem.getExtension(sItemName);
          type = TypeManager.getInstance().getTypeByExtension(ext);
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
          track = TrackManager.getInstance().getTrackByID(sTrackId);
          String sParentID = attributes.getValue(Const.XML_DIRECTORY).intern();
          // UPGRADE check parent ID is right
          if ((hmWrongRightDirectoryID.size() > 0) &&
          // replace wrong by right ID
              (hmWrongRightDirectoryID.containsKey(sParentID))) {
            sParentID = hmWrongRightDirectoryID.get(sParentID);
          }
          dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
          if (dParent == null || track == null) { // more checkups
            return;
          }

          String size = attributes.getValue(Const.XML_SIZE);
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
          sID = attributes.getValue(idIndex).intern();
          /*
           * UPGRADE test : if first element we check has the right ID, we avoid
           * wasting time checking others item one. If is is an upgrade, we
           * force the check.We always check id in debug mode.
           */
          sRightID = sID;
          if (needCheckID) {
            sRightID = FileManager.createID(sItemName, dParent).intern();
            if (sRightID == sID) {
              needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
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

          // dParent = null;
          sParentID = attributes.getValue(Const.XML_DIRECTORY_PARENT).intern();
          // UPGRADE
          if ((hmWrongRightDirectoryID.size() > 0)
              && (hmWrongRightDirectoryID.containsKey(sParentID))) {
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
          sItemName = attributes.getValue(Const.XML_NAME);
          sID = attributes.getValue(idIndex).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = DirectoryManager.createID(sItemName, device, dParent).intern();
            if (sRightID == sID) {
              needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
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
          Style style = null;
          Album album = null;
          Author author = null;
          sID = attributes.getValue(idIndex).intern();
          String sTrackName = attributes.getValue(Const.XML_TRACK_NAME);
          // album
          String sAlbumID = attributes.getValue(Const.XML_TRACK_ALBUM).intern();
          if ((hmWrongRightAlbumID.size() > 0) && (hmWrongRightAlbumID.containsKey(sAlbumID))) {
            sAlbumID = hmWrongRightAlbumID.get(sAlbumID);
          }
          album = AlbumManager.getInstance().getAlbumByID(sAlbumID);
          // Style
          String sStyleID = attributes.getValue(Const.XML_TRACK_STYLE).intern();
          if ((hmWrongRightStyleID.size() > 0) && (hmWrongRightStyleID.containsKey(sStyleID))) {
            sStyleID = hmWrongRightStyleID.get(sStyleID);
          }
          style = StyleManager.getInstance().getStyleByID(sStyleID);
          // Year
          String sYearID = attributes.getValue(Const.XML_TRACK_YEAR).intern();
          Year year = YearManager.getInstance().getYearByID(sYearID);
          // For jajuk < 1.4
          if (year == null) {
            year = YearManager.getInstance().registerYear(sYearID, sYearID);
          }
          // Author
          String sAuthorID = attributes.getValue(Const.XML_TRACK_AUTHOR).intern();
          if ((hmWrongRightAuthorID.size() > 0) && (hmWrongRightAuthorID.containsKey(sAuthorID))) {
            sAuthorID = hmWrongRightAuthorID.get(sAuthorID);
          }
          author = AuthorManager.getInstance().getAuthorByID(sAuthorID);
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
          type = TypeManager.getInstance().getTypeByID(typeID);
          // more checkups
          if (album == null || author == null) {
            return;
          }
          if (style == null || type == null) {
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

          // Idem for AlbumArtist
          String sAlbumArtist = null;
          try {
            sAlbumArtist = attributes.getValue(Const.XML_TRACK_ALBUM_ARTIST);
          } catch (Exception e) {
            if (Log.isDebugEnabled()) {
              // wrong format
              Log.debug(Messages.getString("Error.137") + ":" + sTrackName); // wrong
            }
          }
          if (sAlbumArtist == null) {
            sAlbumArtist = Const.UNKNOWN_AUTHOR;
          }

          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = TrackManager.createID(sTrackName, album, style, author, length, year,
                lOrder, type).intern();
            if (sRightID == sID) {
              needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
            } else {
              Log.debug("** Wrong Track Id, upgraded: " + sTrackName);
              hmWrongRightTrackID.put(sID, sRightID);
            }
          }
          // Date format should be OK
          Date dAdditionDate = ADDITION_FORMATTER.parse(attributes.getValue(attributes
              .getIndex(Const.XML_TRACK_DISCOVERY_DATE)));
          track = TrackManager.getInstance().registerTrack(sRightID, sTrackName, album, style,
              author, length, year, lOrder, type, sAlbumArtist);
          TrackManager.getInstance().changeTrackRate(track,
              UtilString.fastLongParser(attributes.getValue(Const.XML_TRACK_RATE)));
          track.setHits(UtilString.fastLongParser(attributes.getValue(Const.XML_TRACK_HITS)));
          track.setDiscoveryDate(dAdditionDate);
          String sComment = attributes.getValue(Const.XML_TRACK_COMMENT).intern();
          if (sComment == null) {
            sComment = "";
          }
          track.setComment(sComment);
          track.populateProperties(attributes);
          break;
        case STAGE_ALBUMS:
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(Const.XML_NAME).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = AlbumManager.createID(sItemName).intern();
            if (sRightID == sID) {
              needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
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
          sItemName = attributes.getValue(Const.XML_NAME).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = AuthorManager.createID(sItemName).intern();
            if (sRightID == sID) {
              needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
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
          sItemName = attributes.getValue(Const.XML_NAME).intern();
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = StyleManager.createID(sItemName).intern();
            if (sRightID == sID) {
              needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
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
          sParentID = attributes.getValue(Const.XML_DIRECTORY).intern();
          // UPGRADE check parent ID is right
          if ((hmWrongRightDirectoryID.size() > 0) &&
          // replace wrong by right ID
              (hmWrongRightDirectoryID.containsKey(sParentID))) {
            sParentID = hmWrongRightDirectoryID.get(sParentID);
          }
          dParent = DirectoryManager.getInstance().getDirectoryByID(sParentID);
          if (dParent == null) { // check directory is exists
            return;
          }
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(Const.XML_NAME);
          // UPGRADE test
          sRightID = sID;
          if (needCheckID) {
            sRightID = PlaylistManager.createID(sItemName, dParent).intern();
            if (sRightID == sID) {
              needCheckID = UpgradeManager.isUpgradeDetected() || SessionService.isTestMode();
            } else {
              Log.debug("** Wrong playlist Id, upgraded: " + sItemName);
              hmWrongRightPlaylistFileID.put(sID, sRightID);
            }
          }
          Playlist plf = PlaylistManager.getInstance().registerPlaylistFile(sRightID, sItemName,
              dParent);
          if (plf != null) {
            plf.populateProperties(attributes);
          }
          break;
        case STAGE_DEVICES:
          device = null;
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(Const.XML_NAME);
          long lType = UtilString.fastLongParser(attributes.getValue(Const.XML_TYPE));
          // UPGRADE test
          sRightID = sID;
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
          device = DeviceManager.getInstance().registerDevice(sRightID, sItemName, lType, sURL);
          if (device != null) {
            device.populateProperties(attributes);
          }
          break;
        case STAGE_YEARS:
          sID = attributes.getValue(idIndex).intern();
          sItemName = attributes.getValue(Const.XML_NAME).intern();
          year = YearManager.getInstance().registerYear(sID, sItemName);
          if (year != null) {
            year.populateProperties(attributes);
          }
          break;
        }
      }
    } catch (Exception re) {
      StringBuilder sAttributes = new StringBuilder();
      for (int i = 0; i < attributes.getLength(); i++) {
        sAttributes.append('\n').append(attributes.getQName(i)).append('=').append(
            attributes.getValue(i));
      }
      Log.error(5, sAttributes.toString(), re);
    }
  }

  /**
   * @return list of wrong file id (used by history)
   */
  public Map<String, String> getHmWrongRightFileID() {
    return hmWrongRightFileID;
  }

  public Map<String, String> getWrongRightAlbumIDs() {
    return this.hmWrongRightAlbumID;
  }
}
