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

import org.jajuk.Main;
import org.jajuk.ui.perspectives.SimplePerspective;
import org.jajuk.util.log.Log;

import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manage all the configuration and user preferences of jajuk.
 * <p>
 * Singleton
 */
public class ConfigurationManager implements ITechnicalStrings {

	/** Properties in memory */
	private static Properties properties = new Properties();

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
	 *            Name of the property.
	 * @return String Value of the property named pName.
	 */
	public static String getProperty(String pName) {
		return properties.getProperty(pName);
	}

	/**
	 * Return the value of a property as a boolean or false if the property is
	 * not found
	 * 
	 * @param pName
	 *            Name of the property.
	 * @return boolean value of the property named pName.
	 */
	public static boolean getBoolean(String pName) {
		return Boolean.valueOf(properties.getProperty(pName)).booleanValue();
	}

	/**
	 * Return the value of a property as a boolean or specified value of the
	 * property is not found
	 * 
	 * @param pName
	 *            Name of the property.
	 * @param default
	 *            value
	 * @return boolean value of the property named pName.
	 */
	public static boolean getBoolean(String pName, boolean bDefault) {
		String s = properties.getProperty(pName);
		if (s != null) {
			return Boolean.valueOf(s).booleanValue();
		} else {
			return bDefault;
		}
	}

	/**
	 * Return the value of a property as a float or 0f if the property is not
	 * found
	 * 
	 * @param pName
	 *            Name of the property.
	 * @return float value of the property named pName.
	 */
	public static float getFloat(String pName) {
		return Float.valueOf(properties.getProperty(pName)).floatValue();
	}

	/**
	 * Return the value of a property as an integer or 0 if the property is not
	 * found
	 * 
	 * @param pName
	 *            Name of the property.
	 * @return int value of the property named pName.
	 */
	public static int getInt(String pName) {
		return Integer.valueOf(properties.getProperty(pName)).intValue();
	}

	/**
	 * Set default values
	 * 
	 */
	public static void setDefaultProperties() {
		//Set default language
		String sLanguage = System.getProperty("user.language");
		if (Messages.getLocales().contains(sLanguage)) {
			// user language exists in jajuk, take it as default
			properties.put(CONF_OPTIONS_LANGUAGE, sLanguage);
		} else { // user language is unknown, take English as a default,
			// user will be able to change it later anyway
			properties.put(CONF_OPTIONS_LANGUAGE, "en");
		}
		// User preferences
		properties.put(CONF_PERSPECTIVE_DEFAULT, SimplePerspective.class.getName());
		properties.put(CONF_STATE_REPEAT, FALSE);
		properties.put(CONF_STATE_SHUFFLE, FALSE);
		properties.put(CONF_STATE_CONTINUE, TRUE);
		properties.put(CONF_STATE_INTRO, FALSE);
		properties.put(CONF_STATE_WAS_PLAYING, TRUE);
		// no startup file by default
		properties.put(CONF_STARTUP_FILE, "");
		properties.put(CONF_STARTUP_MODE, STARTUP_MODE_LAST_KEEP_POS);
		properties.put(CONF_STARTUP_LAST_POSITION, "0");
		properties.put(CONF_CONFIRMATIONS_DELETE_FILE, TRUE);
		properties.put(CONF_CONFIRMATIONS_EXIT, FALSE);
		properties.put(CONF_CONFIRMATIONS_REMOVE_DEVICE, TRUE);
		properties.put(CONF_CONFIRMATIONS_DELETE_COVER, TRUE);
		properties.put(CONF_CONFIRMATIONS_CLEAR_HISTORY, TRUE);
		properties.put(CONF_CONFIRMATIONS_RESET_RATINGS, TRUE);
		properties.put(CONF_CONFIRMATIONS_REFACTOR_FILES, TRUE);
		properties.put(CONF_OPTIONS_HIDE_UNMOUNTED, FALSE);
		properties.put(CONF_OPTIONS_DEFAULT_ACTION_CLICK, FALSE);
		properties.put(CONF_OPTIONS_DEFAULT_ACTION_DROP, TRUE);
		properties.put(CONF_OPTIONS_NOVELTIES_AGE, "30");
		properties.put(CONF_OPTIONS_VISIBLE_PLANNED, "10");
		properties.put(CONF_BUFFER_SIZE, "16000");
		// -1 : max available buffer set default trace level, debug in debug
		// mode and warning in normal mode
		properties.put(CONF_AUDIO_BUFFER_SIZE, "-1");
		if (Main.bIdeMode) {
			properties.put(CONF_OPTIONS_LOG_LEVEL, Integer.toString(Log.DEBUG));
		} else {
			properties.put(CONF_OPTIONS_LOG_LEVEL, Integer.toString(Log.WARNING));
		}
		properties.put(CONF_OPTIONS_TAB, "0");
		properties.put(CONF_OPTIONS_INTRO_BEGIN, "0");
		properties.put(CONF_OPTIONS_INTRO_LENGTH, "20");
		properties.put(CONF_OPTIONS_SYNC_TABLE_TREE, FALSE);
		properties.put(CONF_UI_SHOW_BALLOON, TRUE);
		properties.put(CONF_P2P_SHARE, FALSE);
		properties.put(CONF_P2P_ADD_REMOTE_PROPERTIES, FALSE);
		properties.put(CONF_P2P_HIDE_LOCAL_PROPERTIES, TRUE);
		properties.put(CONF_P2P_PASSWORD, "");
		properties.put(CONF_HISTORY, "-1");
		properties.put(CONF_TAGS_USE_PARENT_DIR, TRUE);
		properties.put(CONF_BOOKMARKS, "");
		properties.put(CONF_UI_SHOW_AT_STARTUP, TRUE);
		properties.put(CONF_BESTOF_TRACKS_SIZE, "20");
		properties.put(CONF_VOLUME, "0.5");
		properties.put(CONF_REGEXP, FALSE);
		properties.put(CONF_BACKUP_SIZE, "20");
		properties.put(CONF_REFACTOR_PATTERN, PATTERN_DEFAULT_REORG);
		properties.put(CONF_COLLECTION_CHARSET, "UTF-8");
		properties.put(CONF_NETWORK_USE_PROXY, FALSE);
		// default proxy name, just a guess
		properties.put(CONF_NETWORK_PROXY_HOSTNAME, "proxy");
		properties.put(CONF_NETWORK_PROXY_PORT, "3128");
		properties.put(CONF_NETWORK_PROXY_LOGIN, "");
		properties.put(CONF_NETWORK_CONNECTION_TO, "10");
		properties.put(CONF_NETWORK_PROXY_TYPE, PROXY_TYPE_HTTP);
		properties.put(CONF_COVERS_AUTO_COVER, TRUE);
		properties.put(CONF_COVERS_SHUFFLE, FALSE);
		properties.put(CONF_COVERS_PRELOAD, FALSE);
		properties.put(CONF_COVERS_SIZE, "3"); // medium and large
		properties.put(CONF_COVERS_CHANGE_AT_EACH_TRACK, FALSE);
		properties.put(CONF_PHYSICAL_TABLE_COLUMNS, XML_PLAY + ',' + XML_TRACK + ',' + XML_ALBUM
				+ ',' + XML_AUTHOR + ',' + XML_TRACK_STYLE + ',' + XML_TRACK_RATE + ','
				+ XML_TRACK_LENGTH);
		properties.put(CONF_LOGICAL_TABLE_COLUMNS, XML_PLAY + ',' + XML_NAME + ',' + XML_ALBUM
				+ ',' + XML_AUTHOR + ',' + XML_TRACK_STYLE + ',' + XML_TRACK_LENGTH + ',' + ','
				+ XML_TRACK_RATE);
		properties.put(CONF_PLAYLIST_EDITOR_COLUMNS, "0" + ',' + XML_TRACK_NAME + ',' + ','
				+ XML_TRACK_AUTHOR + ',' + XML_TRACK_RATE);
		// Default Window position: X,Y,X_size,Y_size
		int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		// Limit initial screen size (reported anoying by some users on dual
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
		properties.put(CONF_WINDOW_POSITION, FRAME_INITIAL_BORDER + "," + FRAME_INITIAL_BORDER
				+ "," + width + "," + height);
		properties.put(CONF_LOGICAL_TABLE_EDITION, FALSE);
		properties.put(CONF_PHYSICAL_TABLE_EDITION, FALSE);
		properties.put(CONF_THUMBS_SHOW_WITHOUT_COVER, TRUE);
		properties.put(CONF_THUMBS_SIZE, THUMBNAIL_SIZE_150x150);
		properties.put(CONF_THUMBS_SORTER, "2"); // sort by album
		// filter on albums
		properties.put(CONF_THUMBS_FILTER, "0");
		properties.put(CONF_TIP_OF_DAY_INDEX, "0");
		properties.put(CONF_WIKIPEDIA_LANGUAGE, properties.get(CONF_OPTIONS_LANGUAGE));
		properties.put(CONF_FADE_DURATION, "0");
		// sort by style
		properties.put(CONF_LOGICAL_TREE_SORT_ORDER, "0");
		properties.put(CONF_DEFAULT_DJ, "");// default dj
		properties.put(CONF_DEFAULT_AMBIENCE, "");// default ambience
		// wrong player show again boolean
		properties.put(CONF_NOT_SHOW_AGAIN_PLAYER, FALSE);
		properties.put(CONF_GLOBAL_RANDOM_MODE, MODE_TRACK);
		properties.put(CONF_NOVELTIES_MODE, MODE_TRACK);
		properties.put(CONF_ANIMATION_PATTERN, PATTERN_DEFAULT_ANIMATION);
		properties.put(CONF_FRAME_POS_FORCED, "");
		properties.put(CONF_OPTIONS_HOTKEYS, FALSE);
		properties.put(CONF_MPLAYER_ARGS, "");
		properties.put(CONF_ENV_VARIABLES, "");
		properties.put(CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION, FALSE);
		properties.put(CONF_SHOW_TIP_ON_STARTUP, TRUE);
		properties.put(CONF_CATALOG_PAGE_SIZE, "100");
		properties.put(CONF_SHOW_POPUPS, FALSE);
		properties.put(CONF_FONTS_SIZE, "12");
		properties.put(CONF_MPLAYER_PATH_FORCED, "");
		properties.put(CONF_INC_RATING, "5");
		properties.put(CONF_OPTIONS_WATERMARK, LNF_DEFAULT_WATERMARK);
		properties.put(CONF_OPTIONS_LNF, LNF_DEFAULT_THEME);
		properties.put(CONF_DEFAULT_WEB_RADIO, DEFAULT_WEBRADIO);
		properties.put(CONF_NOT_SHOW_AGAIN_UPDATE, FALSE);
		properties.put(CONF_CHECK_FOR_UPDATE, TRUE);
		properties.put(CONF_IGNORED_RELEASES, "");
		properties.put(CONF_AUDIOSCROBBLER_ENABLE, FALSE);
		properties.put(CONF_NOT_SHOW_AGAIN_LASTFM_DISABLED, FALSE);
		properties.put(CONF_LASTFM_INFO, TRUE);
		properties.put(CONF_WEBRADIO_WAS_PLAYING, FALSE);
		properties.put(CONF_OPTIONS_WATERMARK_IMAGE, Util.getConfFileByPath(
				"cache/internal/" + FILE_BACKGROUND_IMAGE).getAbsolutePath());
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
	public static void commit() {
		try {
			properties.store(new FileOutputStream(Util.getConfFileByPath(FILE_CONFIGURATION)),
					"User configuration");
		} catch (IOException e) {
			Log.error(113, e);
			Messages.showErrorMessage(113);
		}

	}

	/** Load properties from in file */
	public static void load() {
		try {
			properties.load(new FileInputStream(Util.getConfFileByPath(FILE_CONFIGURATION)));
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
	 *            property key to remove
	 */
	public static void removeProperty(String sKey) {
		properties.remove(sKey);
	}
}