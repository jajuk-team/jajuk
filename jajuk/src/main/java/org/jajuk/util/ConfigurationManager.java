/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */
package org.jajuk.util;

import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.jajuk.Main;
import org.jajuk.ui.perspectives.SimplePerspective;
import org.jajuk.util.log.Log;

/**
 * Manage all the configuration and user preferences of jajuk.
 * <p>
 * Singleton
 */
public class ConfigurationManager implements ITechnicalStrings {

  /** Properties in memory */
  private static volatile Properties properties = new Properties();

  /** Default properties cache */
  private static volatile Properties defaults = new Properties();

  /** Self instance* */
  static private ConfigurationManager cm;

  /** Singleton accessor */
  public static synchronized ConfigurationManager getInstance() {
    if (cm == null) {
      cm = new ConfigurationManager();
    }
    return cm;
  }

  /**
   * Constructor
   * 
   */
  private ConfigurationManager() {
    setDefaultProperties();
    properties = (Properties) properties.clone();
  }

  /**
   * Return the value of a property, or null if the property is not found.
   * 
   * @param pName
   *          Name of the property.
   * @return String Value of the property named pName.
   */
  public static String getProperty(String pName) {
    return properties.getProperty(pName);
  }

  /**
   * Return the value of a property as a boolean or default value or default
   * value if value cannot be parsed
   * 
   * @param pName
   *          Name of the property.
   * @return boolean value of the property named pName.
   */
  public static boolean getBoolean(String pName) {
    boolean out = false;
    try {
      out = Boolean.parseBoolean(properties.getProperty(pName));
    } catch (Exception e) {
      out = Boolean.parseBoolean(defaults.getProperty(pName));
      Log.debug("Cannot parse property: " + pName);
      Log.debug(e);
    }
    return out;
  }

  /**
   * Return the value of a property as a float or default value or default value
   * if value cannot be parsed
   * 
   * @param pName
   *          Name of the property.
   * @return float value of the property named pName.
   */
  public static float getFloat(String pName) {
    float out = 0f;
    try {
      out = Float.parseFloat(properties.getProperty(pName));
    } catch (Exception e) {
      out = Float.parseFloat(defaults.getProperty(pName));
      Log.debug("Cannot parse property: " + pName);
      Log.debug(e);
    }
    return out;
  }

  /**
   * Return the value of a property as an integer or default value if value
   * cannot be parsed
   * 
   * @param pName
   *          Name of the property.
   * @return int value of the property named pName.
   */
  public static int getInt(String pName) {
    int out = 0;
    try {
      out = Integer.parseInt(properties.getProperty(pName));
    } catch (Exception e) {
      out = Integer.parseInt(defaults.getProperty(pName));
      Log.debug("Cannot parse property: " + pName);
      Log.debug(e);
    }
    return out;
  }

  /**
   * Reset a given property to its defaults
   * 
   * @param property
   */
  public static void setDefaultProperty(String property) {
    String defaultValue = (String) defaults.get(property);
    if (defaultValue != null) {
      properties.put(property, defaultValue);
    } else {
      Log.debug("Cannot reset thsi property: " + property);
    }
  }

  /**
   * Set default values
   * 
   */
  public static void setDefaultProperties() {
    defaults.put(CONF_OPTIONS_LANGUAGE, Messages.getNativeLocale());
    // User preferences
    defaults.put(CONF_PERSPECTIVE_DEFAULT, SimplePerspective.class.getName());
    defaults.put(CONF_STATE_REPEAT, FALSE);
    defaults.put(CONF_STATE_SHUFFLE, FALSE);
    defaults.put(CONF_STATE_CONTINUE, FALSE);
    defaults.put(CONF_STATE_INTRO, FALSE);
    defaults.put(CONF_STATE_WAS_PLAYING, FALSE);
    // no startup file by default
    defaults.put(CONF_STARTUP_FILE, "");
    defaults.put(CONF_STARTUP_MODE, STARTUP_MODE_LAST_KEEP_POS);
    defaults.put(CONF_STARTUP_LAST_POSITION, "0");
    defaults.put(CONF_CONFIRMATIONS_DELETE_FILE, TRUE);
    defaults.put(CONF_CONFIRMATIONS_EXIT, FALSE);
    defaults.put(CONF_CONFIRMATIONS_REMOVE_DEVICE, TRUE);
    defaults.put(CONF_CONFIRMATIONS_DELETE_COVER, TRUE);
    defaults.put(CONF_CONFIRMATIONS_CLEAR_HISTORY, TRUE);
    defaults.put(CONF_CONFIRMATIONS_RESET_RATINGS, TRUE);
    defaults.put(CONF_CONFIRMATIONS_REFACTOR_FILES, TRUE);
    defaults.put(CONF_OPTIONS_HIDE_UNMOUNTED, FALSE);
    defaults.put(CONF_OPTIONS_PUSH_ON_CLICK, FALSE);
    defaults.put(CONF_OPTIONS_DEFAULT_ACTION_DROP, TRUE);
    defaults.put(CONF_OPTIONS_NOVELTIES_AGE, "30");
    defaults.put(CONF_OPTIONS_VISIBLE_PLANNED, "10");
    defaults.put(CONF_BUFFER_SIZE, "16000");
    // -1 : max available buffer set default trace level, debug in debug
    // mode and warning in normal mode
    defaults.put(CONF_AUDIO_BUFFER_SIZE, "-1");
    if (Main.bIdeMode) {
      defaults.put(CONF_OPTIONS_LOG_LEVEL, Integer.toString(Log.DEBUG));
    } else {
      defaults.put(CONF_OPTIONS_LOG_LEVEL, Integer.toString(Log.WARNING));
    }
    defaults.put(CONF_OPTIONS_TAB, "0");
    defaults.put(CONF_OPTIONS_INTRO_BEGIN, "0");
    defaults.put(CONF_OPTIONS_INTRO_LENGTH, "20");
    defaults.put(CONF_OPTIONS_SYNC_TABLE_TREE, FALSE);
    defaults.put(CONF_UI_SHOW_BALLOON, FALSE);
    defaults.put(CONF_P2P_SHARE, FALSE);
    defaults.put(CONF_P2P_ADD_REMOTE_PROPERTIES, FALSE);
    defaults.put(CONF_P2P_HIDE_LOCAL_PROPERTIES, TRUE);
    defaults.put(CONF_P2P_PASSWORD, "");
    defaults.put(CONF_HISTORY, "-1");
    defaults.put(CONF_TAGS_USE_PARENT_DIR, TRUE);
    defaults.put(CONF_BOOKMARKS, "");
    defaults.put(CONF_STARTUP_DISPLAY, Integer.toString(DISPLAY_MODE_WINDOW_TRAY));
    defaults.put(CONF_BESTOF_TRACKS_SIZE, "20");
    defaults.put(CONF_VOLUME, "0.5");
    defaults.put(CONF_REGEXP, FALSE);
    defaults.put(CONF_BACKUP_SIZE, "20");
    defaults.put(CONF_REFACTOR_PATTERN, PATTERN_DEFAULT_REORG);
    defaults.put(CONF_COLLECTION_CHARSET, "UTF-8");
    defaults.put(CONF_NETWORK_USE_PROXY, FALSE);
    // default proxy name, just a guess
    defaults.put(CONF_NETWORK_PROXY_HOSTNAME, "proxy");
    defaults.put(CONF_NETWORK_PROXY_PORT, "3128");
    defaults.put(CONF_NETWORK_PROXY_LOGIN, "");
    defaults.put(CONF_NETWORK_CONNECTION_TO, "10");
    defaults.put(CONF_NETWORK_PROXY_TYPE, PROXY_TYPE_HTTP);
    defaults.put(CONF_COVERS_AUTO_COVER, TRUE);
    defaults.put(CONF_COVERS_SHUFFLE, FALSE);
    defaults.put(CONF_COVERS_SIZE, "3"); // medium and large
    defaults.put(CONF_TRACKS_TABLE_EDITION, FALSE);
    defaults.put(CONF_FILES_TABLE_EDITION, FALSE);
    defaults.put(CONF_ALBUMS_TABLE_EDITION, FALSE);
    defaults.put(CONF_FILES_TABLE_COLUMNS, XML_PLAY + ',' + XML_TRACK + ',' + XML_ALBUM + ','
        + XML_AUTHOR + ',' + XML_TRACK_STYLE + ',' + XML_TRACK_RATE + ',' + XML_TRACK_LENGTH);
    defaults.put(CONF_TRACKS_TABLE_COLUMNS, XML_PLAY + ',' + XML_NAME + ',' + XML_ALBUM + ','
        + XML_AUTHOR + ',' + XML_TRACK_STYLE + ',' + XML_TRACK_LENGTH + ',' + ',' + XML_TRACK_RATE);
    defaults.put(CONF_PLAYLIST_EDITOR_COLUMNS, XML_PLAY + ',' + XML_TRACK_NAME + ',' + ','
        + XML_TRACK_AUTHOR + ',' + XML_TRACK_RATE);
    defaults.put(CONF_PLAYLIST_REPOSITORY_COLUMNS, XML_PLAY + ',' + XML_NAME);
    defaults.put(CONF_QUEUE_COLUMNS, XML_PLAY + ',' + XML_TRACK_NAME + ',' + ',' + XML_TRACK_AUTHOR
        + ',' + XML_TRACK_RATE);
    defaults.put(CONF_ALBUMS_TABLE_COLUMNS, XML_PLAY + ',' + XML_ALBUM + ',' + XML_AUTHOR + ','
        + XML_STYLE + ',' + XML_YEAR + ',' + XML_TRACK_RATE + ',' + XML_TRACK_LENGTH + ','
        + XML_TRACKS + ',' + XML_TRACK_DISCOVERY_DATE);
    // Default Window position: X,Y,X_size,Y_size
    int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
    // Limit initial screen size (reported as problematic by some users on dual
    // heads)
    if (width > 1400) {
      width = 1200;
    } else {
      width = width - 2 * FRAME_INITIAL_BORDER;
    }
    int height = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
    if (height > 1200) {
      height = 1000;
    } else {
      height = height - 2 * FRAME_INITIAL_BORDER;
    }
    defaults.put(CONF_WINDOW_POSITION, FRAME_INITIAL_BORDER + "," + FRAME_INITIAL_BORDER + ","
        + width + "," + height);
    defaults.put(CONF_THUMBS_SHOW_WITHOUT_COVER, TRUE);
    defaults.put(CONF_THUMBS_SIZE, THUMBNAIL_SIZE_150X150);
    defaults.put(CONF_THUMBS_SORTER, "2"); // sort by album
    // filter on albums
    defaults.put(CONF_THUMBS_FILTER, "0");
    defaults.put(CONF_TIP_OF_DAY_INDEX, "0");
    defaults.put(CONF_WIKIPEDIA_LANGUAGE, defaults.get(CONF_OPTIONS_LANGUAGE));
    defaults.put(CONF_FADE_DURATION, "6");
    // sort by style
    defaults.put(CONF_LOGICAL_TREE_SORT_ORDER, "0");
    defaults.put(CONF_DEFAULT_DJ, "");// default dj
    defaults.put(CONF_DEFAULT_AMBIENCE, "");// default ambience
    // wrong player show again boolean
    defaults.put(CONF_GLOBAL_RANDOM_MODE, MODE_TRACK);
    defaults.put(CONF_NOVELTIES_MODE, MODE_TRACK);
    defaults.put(CONF_ANIMATION_PATTERN, PATTERN_DEFAULT_ANIMATION);
    defaults.put(CONF_FRAME_POS_FORCED, "");
    defaults.put(CONF_OPTIONS_HOTKEYS, FALSE);
    defaults.put(CONF_MPLAYER_ARGS, "");
    defaults.put(CONF_ENV_VARIABLES, "");
    if (ConfigurationManager.getBoolean(CONF_SHOW_TIP_ON_STARTUP)) {
      // In that case, we keep previous value if it was false to avoid
      // displaying the tips after user makes a "defaults" in preferences view
      defaults.put(CONF_SHOW_TIP_ON_STARTUP, TRUE);
    }
    defaults.put(CONF_CATALOG_PAGE_SIZE, "100");
    defaults.put(CONF_SHOW_POPUPS, FALSE);
    defaults.put(CONF_FONTS_SIZE, "12");
    defaults.put(CONF_MPLAYER_PATH_FORCED, "");
    defaults.put(CONF_INC_RATING, "5");
    defaults.put(CONF_OPTIONS_WATERMARK, LNF_DEFAULT_WATERMARK);
    defaults.put(CONF_OPTIONS_LNF, LNF_DEFAULT_THEME);
    defaults.put(CONF_DEFAULT_WEB_RADIO, DEFAULT_WEBRADIO);
    defaults.put(CONF_CHECK_FOR_UPDATE, TRUE);
    defaults.put(CONF_IGNORED_RELEASES, "");
    defaults.put(CONF_AUDIOSCROBBLER_ENABLE, FALSE);
    defaults.put(CONF_LASTFM_INFO, TRUE);
    defaults.put(CONF_WEBRADIO_WAS_PLAYING, FALSE);
    defaults.put(CONF_PERSPECTIVE_ICONS_SIZE, "32");
    // NOT SHOW AGAIN
    defaults.put(CONF_NOT_SHOW_AGAIN_LASTFM_DISABLED, FALSE);
    defaults.put(CONF_NOT_SHOW_AGAIN_PLAYER, FALSE);
    defaults.put(CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION, FALSE);
    defaults.put(CONF_NOT_SHOW_AGAIN_CROSS_FADE, FALSE);
    defaults.put(CONF_NOT_SHOW_AGAIN_LAF_CHANGE, FALSE);
    defaults.put(CONF_FRAME_TITLE_PATTERN, '~' + PATTERN_TRACKNAME + " (" + PATTERN_AUTHOR + ")~");
    defaults.put(CONF_SHOW_DUPLICATE_PLAYLISTS, FALSE);
    defaults.put(CONF_FORCE_TRAY_SHUTDOWN, FALSE);
    defaults.put(CONF_FORMAT_TIME_ELAPSED, "0");
    defaults.put(CONF_SLIMBAR_POSITION, "0,0");
    // Make a copy of default values
    properties = (Properties) defaults.clone();
  }

  /**
   * Set a property
   * 
   * @param sName
   * @param sValue
   */
  public static void setProperty(String sName, String sValue) {
    properties.setProperty(sName, sValue);
  }

  /** Commit properties in a file */
  public static void commit() throws Exception {
    OutputStream str = new FileOutputStream(UtilSystem.getConfFileByPath(FILE_CONFIGURATION));
    try {
      properties.store(str, "User configuration");
    } finally {
      str.close();
    }
  }

  /**
   * 
   * @param property
   * @return whether the given property is known
   */
  public static boolean containsProperty(String property) {
    return properties.containsKey(property);
  }

  /** Load properties from in file */
  public static void load() {
    try {
      InputStream str = new FileInputStream(UtilSystem.getConfFileByPath(FILE_CONFIGURATION));
      try {
        properties.load(str);
      } finally {
        str.close();
      }
    } catch (IOException e) {
      e.printStackTrace(); // do not use log system here
      Messages.showErrorMessage(114);
    }
  }

  /**
   * @return Returns the properties.
   */
  public static Properties getProperties() {
    return properties;
  }

  /**
   * Remove a property
   * 
   * @param sKey
   *          property key to remove
   */
  public static void removeProperty(String sKey) {
    properties.remove(sKey);
  }
}