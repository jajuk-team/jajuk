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

import java.net.URL;

/**
 * Contains all technical/ non-translatable strings
 */
public interface ITechnicalStrings {

	// Misc
	/** Information bar and command bar default vertical size in pixels */
	public static final int BORDER_Y_SIZE = 112;

	/** Command bar default vertical size in pixels */
	public static final int BORDER_X_SIZE = 55;

	/** panels precisions in percent */
	public static final int PRECISION = 5;

	/** Generic border */
	public static final int BORDER = 4;

	/** Maximum size for covers in KB */
	public static final int MAX_COVER_SIZE = 1000;

	/** Maximum number of remote covers */
	public static final int MAX_REMOTE_COVERS = 10;

	/** Special length for player meaning end of file */
	public static final long TO_THE_END = -1;

	/** Time we wait after an error in ms */
	public static final int WAIT_AFTER_ERROR = 3000;

	/** Default playlist file name */
	public static final String DEFAULT_PLAYLIST_FILE = "playlist";

	/** Max number of connection time out before stoping to look for covers */
	public static final int STOP_TO_SEARCH = 5;

	/** Listening port to check others jajuk sessions */
	public static final int PORT = 62322;

	/** Quality agent e-mail */
	public static final String FEEDBACK_EMAIL = "jajuk-support@lists.sourceforge.net";

	/** Number of lines in the feedback mail */
	public static final int FEEDBACK_LINES = 200;

	/** Max history size */
	public static final int MAX_HISTORY_SIZE = 1000;

	/** Autocommit delay in ms */
	public static final int AUTO_COMMIT_DELAY = 3600000;
	
	/** Autorefresh thread delay in ms */
	public static final int AUTO_REFRESH_DELAY = 30000;

	/** Minimum reresh fequency (ms) */
	public static final int MIN_AUTO_REFRESH_DELAY = 30000;

	/** Garbager delay in ms */
	public static final int GARBAGER_DELAY = 600000;

	/** Fading status code */
	public static final int FADING_STATUS = 9999;

	/** Need full gc memory % */
	public static final float NEED_FULL_GC_LEVEL = 0.90f;

	/** Play time to get rate +1 in secs */
	public static final int INC_RATE_TIME = 20;

	/** Min Number of tracks in an AuthorNode */
	public static final int MIN_TRACKS_NUMBER = 4;

	/** Min number of tracks in a DJ selection without track unicity */
	public static final int MIN_TRACKS_NUMBER_WITHOUT_UNICITY = 200;

	/** Default number of tracks in a transition */
	public static final int DEFAULT_TRANSITION_TRACK_NUMBER = 2;

	/** Rate Stars columns size in pixels */
	public static final int RATE_COLUMN_SIZE = 75;

	/** Play icon columns size in pixels */
	public static final int PLAY_COLUMN_SIZE = 20;

	/** Proporion of best tracks */
	public static final float BESTOF_PROPORTION = 0.05f;

	/** Number of milliseconds in a second */
	public static final int MILLISECONDS_IN_A_SECOND = 1000;

	/** Number of seconds in a minute */
	public static final int SECONDS_IN_A_MINUTE = 60;

	/** Number of minutes in an hour */
	public static final int MINUTES_IN_AN_HOUR = 60;
	
	/** Number of pixels around window at initial startup */
	public static final int FRAME_INITIAL_BORDER = 60;

	/** Default webradio */
	public static final String DEFAULT_WEBRADIO = "Bluemars [Ambient/Space-Music]";
	
	/** Default hostame used in case of problem */
	public static final String DEFAULT_HOSTNAME = "localhost";

	/** Number of hours in a day */
	public static final int HOURS_IN_A_DAY = 24;
	
	/** Number of album bestof items */
	public static final int NB_BESTOF_ALBUMS = 8;
	
	/** Number of album novelties items */
	public static final int NB_NOVELTIES_ALBUMS = 8;
	
	/** Number of milliseconds in a day */
	public static final int MILLISECONDS_IN_A_DAY = MILLISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTE
			* MINUTES_IN_AN_HOUR * HOURS_IN_A_DAY;

	/** Mplayer windows exe size in bytes */
	public static final long MPLAYER_EXE_SIZE = 3284992l;
	
	public static final String FILE_DEFAULT_MPLAYER_X86_OSX_PATH = "/Applications/MPlayer OSX.app/Contents/Resources/External_Binaries/mplayer_intel.app/Contents/MacOS/mplayer";
	
	public static final String FILE_DEFAULT_MPLAYER_POWER_OSX_PATH = "/Applications/MPlayer OSX.app/Contents/Resources/External_Binaries/mplayer_ppc.app/Contents/MacOS/mplayer";
	
	/** Wikipedia view default URL */
	public static final String WIKIPEDIA_VIEW_DEFAULT_URL = "http://jajuk.info";

	/** Update URL = PAD file URL*/
	public static final String CHECK_FOR_UPDATE_URL = "http://jajuk.sourceforge.net/jajuk_pad.xml";

	// Jajuk version
	public static final String JAJUK_VERSION_TEST = "VERSION_REPLACED_BY_ANT";

	public static final String JAJUK_VERSION = JAJUK_VERSION_TEST;

	public static final String JAJUK_VERSION_DATE = "Build: DATE_REPLACED_BY_ANT";

	public static final String JAJUK_COPYRIGHT = "Copyright 2004, 2007 The Jajuk Team";

	// Files and paths
	/** Bootstrap file that contains jajuk configuration user directory* */
	public static final String FILE_BOOTSTRAP = System.getProperty("user.home")
			+ "/.jajuk_bootstrap";

	public static final String FILE_COLLECTION = "collection.xml";

	// File written by the exit hook
	public static final String FILE_COLLECTION_EXIT = "collection_exit.xml";

	// Void file created after exit collection file
	public static final String FILE_COLLECTION_EXIT_PROOF = "exit_proof";

	public static final String FILE_REPORTING_CACHE_FILE = "cache/report";
	
	public static final String FILE_CONFIGURATION = "conf.properties";

	public static final String FILE_HISTORY = "history.xml";

	/** Directory containing all sessions using this workspace */
	public static final String FILE_SESSIONS = "sessions";

	public static final String FILE_DEFAULT_COVER = "cover";

	public static final String FILE_DEFAULT_COVER_2 = "front";

	public static final String FILE_ABSOLUTE_DEFAULT_COVER = "jajuk-default-cover.";

	// langpack name : jajuk_<locale>.properties
	public static final String FILE_LANGPACK_PART1 = "jajuk";

	public static final String FILE_LANGPACK_PART2 = ".properties";
	
	/**Extention to the .jajuk_test directory in test mode only*/
	public static final String TEST_VERSION = "1.4";
	
	// logs
	public static final String FILE_LOGS = "jajuk.log";

	public static final URL FILE_LOG4j_CONF = Util
			.getResource("org/jajuk/util/log/jajuk-log4j-conf.xml");

	public static final String FILE_CACHE = "cache";
	
	public static final String FILE_INTERNAL_CACHE = "internal";

	public static final String FILE_THUMBS = "thumbs";

	public static final String FILE_THUMB_NO_COVER = "nocover.png";

	public static final String FILE_DEFAULT_BESTOF_PLAYLIST = "bestof";

	public static final String FILE_DEFAULT_NOVELTIES_PLAYLIST = "novelties";

	public static final String FILE_DEFAULT_BOOKMARKS_PLAYLIST = "bookmarks";

	public static final String FILE_DEFAULT_QUEUE_PLAYLIST = "queue";

	public static final String FILE_DJ_DIR = "djs";

	public static final String FILE_WEB_RADIOS_REPOS = "webradios.xml";

	public static final String FILE_FIFO = "fifo.lst";

	public static final String FILE_MPLAYER_EXE = "mplayer.exe";

	public static final String FILE_TOOLBARS_CONF = "toolbars.xml";

	public static final String FILE_DEFAULT_PERSPECTIVES_PATH = "perspectives";

	public static final String FILE_JAJUK_DOWNLOADED_FILES_SUFFIX = "_jajuk";

	public static final String FILE_REPORTING_CSS_ALL_FILENAME = "report-all.css";

	public static final URL FILE_REPORTING_CSS_ALL_PATH = Util.getResource("xslt/"+FILE_REPORTING_CSS_ALL_FILENAME);

	public static final String FILE_REPORTING_CSS_PRINT_FILENAME = "report-print.css";

	public static final URL FILE_REPORTING_CSS_PRINT_PATH = Util.getResource("xslt/"+FILE_REPORTING_CSS_PRINT_FILENAME);
	
	public static final String FILE_BACKGROUND_IMAGE = "background.jpg";

	public static final String URL_MPLAYER = "http://jajuk.info/mplayer/1.0pre8/mplayer.exe";
	
	public static final String URL_DEFAULT_WEBRADIOS_1 = "http://jajuk.svn.sourceforge.net/viewvc/*checkout*/jajuk/trunk/jajuk/src/main/resources/xml/default_webradios.xml";

	public static final String URL_DEFAULT_WEBRADIOS_2 = "http://repository.jajuk.info/default_webradios.xml";
	
	// About
	public static final String ABOUT = "<html>Jajuk version " + JAJUK_VERSION + "</html>";

	// Properties
	public static final String PROPERTY_SEQ = "sequence";// playlist item

	// sequence
	public static final String AMBIENCE_PREFIX = "jajuk.ambience.";

	// directory path
	public static final String PATH_RELATIVE_DOCS = "docs/";

	//Proxy type: http
	public static final String PROXY_TYPE_HTTP = "http";

	//Proxy type: socks
	public static final String PROXY_TYPE_SOCKS = "socks";

	// Unknown
	public static final String UNKNOWN_AUTHOR = "unknown_author";

	public static final String UNKNOWN_ALBUM = "unknown_album";

	public static final String UNKNOWN_STYLE = "unknown_style";

	public static final String UNKNOWN_YEAR = "unknown_year";

	public static final String UNKNOWN = "unknown";

	// images
	public static final URL IMAGES_SPLASHSCREEN = Util
			.getResource("images/included/jajuk-splashscreen.jpg");

	public static final URL IMAGE_SEARCH = Util.getResource("images/included/search.png");

	public static final URL IMAGE_DJ = Util.getResource("images/included/dj.jpg");

	public static final URL IMAGE_WEBRADIO = Util.getResource("images/included/webradios.jpg");
	
	public static final URL IMAGE_TRAY_TITLE = Util.getResource("images/included/tray_title.png");

	// XSLT Files
	public static final URL XSLT_AUTHOR = Util.getResource("xslt/author.xsl");

	public static final URL XSLT_ALBUM = Util.getResource("xslt/album.xsl");

	public static final URL XSLT_STYLE = Util.getResource("xslt/style.xsl");

	public static final URL XSLT_YEAR = Util.getResource("xslt/style.xsl");

	public static final URL XSLT_DIRECTORY = Util.getResource("xslt/directory.xsl");

	public static final URL XSLT_DEVICE = Util.getResource("xslt/device.xsl");

	public static final URL XSLT_COLLECTION_LOGICAL = Util
			.getResource("xslt/logical_collection.xsl");
	
	public static final String COLLECTION_LOGICAL = "logical";
	
	public static final String COLLECTION_PHYSICAL = "physical";
	
	public static final String ALL = "all";
	
	public static final String LOCAL = "local";
		
	// Command line options
	
	// Tells jajuk it is inside the IDE
	public static final String CLI_IDE = "ide";

	// Tells jajuk to use a .jajuk_test repository
	public static final String CLI_TEST = "test";

	// players impls
	public static final String PLAYER_IMPL_JAVALAYER = "org.jajuk.players.JavaLayerPlayerImpl";

	public static final String PLAYER_IMPL_MPLAYER = "org.jajuk.players.MPlayerPlayerImpl";

	public static final String PLAYER_IMPL_WEBRADIOS = "org.jajuk.players.WebRadioPlayerImpl";

	// tag impls
	public static final String TAG_IMPL_JID3LIB = "org.jajuk.tag.JID3LibTagImpl";

	public static final String TAG_IMPL_MP3INFO = "org.jajuk.tag.MP3InfoTagImpl";

	public static final String TAG_IMPL_RABBIT_FARM = "org.jajuk.tag.RabbitFarmTagImpl";

	public static final String TAG_IMPL_JLGUI_MP3 = "org.jajuk.tag.JlGuiMP3TagImpl";

	public static final String TAG_IMPL_JLGUI_OGG = "org.jajuk.tag.JlGuiOggTagImpl";

	public static final String TAG_IMPL_NO_TAGS = "org.jajuk.tag.NoTagsTagImpl";

	public static final String TAG_IMPL_ENTAGGED = "org.jajuk.tag.EntaggedTagImpl";

	// device types
	public static final String DEVICE_TYPE_DIRECTORY = "Device_type.directory";

	public static final String DEVICE_TYPE_FILE_CD = "Device_type.file_cd";

	public static final String DEVICE_TYPE_USBKEY = "Device_type.usbkey";

	// Types properties
	public static final String TYPE_PROPERTY_TECH_DESC_MP3 = "mp3";

	public static final String TYPE_PROPERTY_TECH_DESC_MP2 = "mp2";
	
	public static final String TYPE_PROPERTY_TECH_DESC_OGG = "ogg";

	public static final String TYPE_PROPERTY_TECH_DESC_AIFF = "aiff";

	public static final String TYPE_PROPERTY_TECH_DESC_WAVE = "wave";

	public static final String TYPE_PROPERTY_TECH_DESC_AU = "au";

	public static final String TYPE_PROPERTY_TECH_DESC_SPEEX = "speex";

	public static final String TYPE_PROPERTY_TECH_DESC_FLAC = "flac";

	public static final String TYPE_PROPERTY_TECH_DESC_MPC = "mpc";

	public static final String TYPE_PROPERTY_TECH_DESC_WMA = "wma";

	public static final String TYPE_PROPERTY_TECH_DESC_APE = "ape";

	public static final String TYPE_PROPERTY_TECH_DESC_AAC = "aac";

	public static final String TYPE_PROPERTY_TECH_DESC_RAM = "ram";

	public static final String TYPE_PROPERTY_TECH_DESC_RADIO = "radio";
	
	// Devices sync mode
	public static final String DEVICE_SYNCHRO_MODE_BI = "bi";

	public static final String DEVICE_SYNCHRO_MODE_UNI = "uni";

	// views
	public static final String VIEW_NAME_DEVICES = "org.jajuk.ui.views.DeviceView";

	public static final String VIEW_NAME_PARAMETERS = "org.jajuk.ui.views.ParameterView";

	public static final String VIEW_NAME_PHYSICAL_TREE = "org.jajuk.ui.views.PhysicalTreeView";

	public static final String VIEW_NAME_PHYSICAL_TABLE = "org.jajuk.ui.views.PhysicalTableView";

	public static final String VIEW_NAME_LOGICAL_TREE = "org.jajuk.ui.views.LogicalTreeView";

	public static final String VIEW_NAME_LOGICAL_TABLE = "org.jajuk.ui.views.LogicalTableView";

	public static final String VIEW_NAME_ABOUT = "org.jajuk.ui.views.AboutView";

	public static final String VIEW_NAME_HELP = "org.jajuk.ui.views.HelpView";

	public static final String VIEW_NAME_STAT = "org.jajuk.ui.views.StatView";

	public static final String VIEW_NAME_COVER = "org.jajuk.ui.views.CoverView";

	public static final String VIEW_NAME_PHYSICAL_PLAYLIST_REPOSITORY = "org.jajuk.ui.views.PhysicalPlaylistRepositoryView";

	public static final String VIEW_NAME_PHYSICAL_PLAYLIST_EDITOR = "org.jajuk.ui.views.PhysicalPlaylistEditorView";

	public static final String VIEW_NAME_PHYSICAL_NAVIGATION_BAR = "org.jajuk.ui.views.PhysicalNavigationBarView";

	public static final String VIEW_NAME_LOGICAL_PLAYLIST_REPOSITORY = "org.jajuk.ui.views.LogicalPlaylistRepositoryView";

	public static final String VIEW_NAME_LOGICAL_PLAYLIST_EDITOR = "org.jajuk.ui.views.LogicalPlaylistEditorView";

	public static final String VIEW_NAME_LOGICAL_NAVIGATION_BAR = "org.jajuk.ui.views.LogicalNavigationBarView";

	public static final String VIEW_NAME_CD_SCAN = "org.jajuk.ui.views.CDScanView";

	// extensions
	public static final String EXT_MP3 = "mp3";

	public static final String EXT_MP2 = "mp2";

	public static final String EXT_PLAYLIST = "m3u";

	public static final String EXT_OGG = "ogg";

	public static final String EXT_WAV = "wav";

	public static final String EXT_AU = "au";

	public static final String EXT_AIFF = "aiff";

	public static final String EXT_FLAC = "flac";

	public static final String EXT_THUMB = "jpg";

	public static final String EXT_MPC = "mpc";

	public static final String EXT_WMA = "wma";

	public static final String EXT_APE = "ape";

	public static final String EXT_APL = "apl";

	public static final String EXT_MAC = "mac";

	public static final String EXT_AAC = "aac";

	public static final String EXT_M4A = "m4a";

	public static final String EXT_REAL = "ram";
	
	public static final String EXT_RADIO = "radio";

	// details keys
	public static final String DETAIL_CURRENT_FILE_ID = "current file id";

	public static final String DETAIL_CURRENT_FILE = "current file";

	public static final String DETAIL_CURRENT_DATE = "current date";

	public static final String DETAIL_CURRENT_POSITION = "current position";

	public static final String DETAIL_TOTAL = "total time";

	public static final String DETAIL_HISTORY_ITEM = "history item";

	public static final String DETAIL_SPECIAL_MODE = "special mode";

	public static final String DETAIL_SPECIAL_MODE_SHUFFLE = "shuffle";

	public static final String DETAIL_SPECIAL_MODE_BESTOF = "bestof";

	public static final String DETAIL_SPECIAL_MODE_NOVELTIES = "novelties";

	public static final String DETAIL_SPECIAL_MODE_NORMAL = "norm";

	public static final String DETAIL_SELECTION = "selection";

	public static final String DETAIL_ENABLE = "enable";

	public static final String DETAIL_ORIGIN = "origin";

	public static final String DETAIL_REASON = "reason";

	public static final String DETAIL_CONTENT = "content";

	public static final String DETAIL_OLD = "old";

	public static final String DETAIL_NEW = "new";

	public static final String DETAIL_OPTION = "option";

	public static final String DETAIL_TARGET = "target";

	// startup mode
	public static final String STARTUP_MODE_NOTHING = "nothing";

	public static final String STARTUP_MODE_SHUFFLE = "shuffle";

	public static final String STARTUP_MODE_FILE = "file";

	public static final String STARTUP_MODE_LAST = "last";

	public static final String STARTUP_MODE_LAST_KEEP_POS = "last_keep_pos";

	public static final String STARTUP_MODE_BESTOF = "bestof";

	public static final String STARTUP_MODE_NOVELTIES = "novelties";

	// --- Configuration keys ---

	public static final String CONF_RELEASE = "jajuk.release";

	public static final String CONF_PERSPECTIVE_DEFAULT = "jajuk.preference.perspective.default";

	public static final String CONF_STATE_REPEAT = "jajuk.state.mode.repeat";

	public static final String CONF_STATE_SHUFFLE = "jajuk.state.mode.shuffle";

	public static final String CONF_STATE_CONTINUE = "jajuk.state.mode.continue";

	public static final String CONF_STATE_INTRO = "jajuk.state.mode.intro";

	/** Whether user exited jajuk in stop state or playing state */
	public static final String CONF_STATE_WAS_PLAYING = "jajuk.state.was_playing";

	public static final String CONF_STARTUP_FILE = "jajuk.startup.file";

	public static final String CONF_STARTUP_MODE = "jajuk.startup.mode";

	public static final String CONF_CONFIRMATIONS_DELETE_FILE = "jajuk.confirmations.delete_file";

	public static final String CONF_CONFIRMATIONS_EXIT = "jajuk.confirmations.exit";

	public static final String CONF_CONFIRMATIONS_REMOVE_DEVICE = "jajuk.confirmations.remove_device";

	public static final String CONF_CONFIRMATIONS_DELETE_COVER = "jajuk.confirmations.delete_cover";

	public static final String CONF_CONFIRMATIONS_CLEAR_HISTORY = "jajuk.confirmations.clear_history";

	public static final String CONF_CONFIRMATIONS_RESET_RATINGS = "jajuk.confirmations.reset_ratings";

	public static final String CONF_CONFIRMATIONS_REFACTOR_FILES = "jajuk.confirmations.refactor_files";

	public static final String CONF_OPTIONS_HIDE_UNMOUNTED = "jajuk.options.hide_unmounted";

	public static final String CONF_OPTIONS_LOG_LEVEL = "jajuk.options.log_level";

	public static final String CONF_OPTIONS_LANGUAGE = "jajuk.options.language";

	public static final String CONF_OPTIONS_INTRO_BEGIN = "jajuk.options.intro.begin";

	public static final String CONF_OPTIONS_INTRO_LENGTH = "jajuk.options.intro.length";

	public static final String CONF_OPTIONS_LNF = "jajuk.options.lnf";

	public static final String CONF_OPTIONS_WATERMARK = "jajuk.options.watermark";

	public static final String CONF_OPTIONS_WATERMARK_IMAGE = "jajuk.options.watermark.image";

	public static final String CONF_OPTIONS_NOVELTIES_AGE = "jajuk.options.novelties";

	// Look and Feel
	/** Substance Look and feel class * */
	public static final String LNF_SUBSTANCE_CLASS = "org.jvnet.substance.SubstanceLookAndFeel";

	/** Default substance theme * */
	public static final String LNF_DEFAULT_THEME = "Orange";

	/** Default substance watermark * */
	public static final String LNF_DEFAULT_WATERMARK = "None";

	/** Image watermark*/
	public static final String LNF_WATERMARK_IMAGE = "Image";

	/** Number of visible planned tracks */
	public static final String CONF_OPTIONS_VISIBLE_PLANNED = "jajuk.options.visible_planned";

	/** Default action (play or push) when clicking on an item */
	public static final String CONF_OPTIONS_DEFAULT_ACTION_CLICK = "jajuk.options.default_action_click";

	/** Default action (play or push) when dropping on an item */
	public static final String CONF_OPTIONS_DEFAULT_ACTION_DROP = "jajuk.options.default_action_drop";

	/** Synchronize table and tree views */
	public static final String CONF_OPTIONS_SYNC_TABLE_TREE = "jajuk.options.sync_table_tree";

	/** Show popup */
	public static final String CONF_UI_SHOW_BALLOON = "jajuk.options.show_popup";

	public static final String CONF_P2P_SHARE = "jajuk.options.p2p.share";

	public static final String CONF_P2P_ADD_REMOTE_PROPERTIES = "jajuk.options.p2p.add_remote_properties";

	public static final String CONF_P2P_HIDE_LOCAL_PROPERTIES = "jajuk.options.p2p.hide_local_properties";

	public static final String CONF_P2P_PASSWORD = "jajuk.options.p2p.password";

	public static final String CONF_HISTORY = "jajuk.options.history";

	public static final String CONF_TAGS_USE_PARENT_DIR = "jajuk.tags.use_parent_dir";

	/** Contains files id separated by a colon */
	public static final String CONF_BOOKMARKS = "jajuk.bookmarks";

	/** Show jajuk window at startup */
	public static final String CONF_UI_SHOW_AT_STARTUP = "jajuk.show_at_startup";

	/** Best of size */
	public static final String CONF_BESTOF_TRACKS_SIZE = "jajuk.bestof_size";
	
	/** Gain (float) */
	public static final String CONF_VOLUME = "jajuk.volume";

	/** Use regular expressions ? */
	public static final String CONF_REGEXP = "jajuk.regexp";

	/** Collection backup size in MB */
	public static final String CONF_BACKUP_SIZE = "jajuk.backup_size";

	/** Collection file charset (utf-8 or utf-16) */
	public static final String CONF_COLLECTION_CHARSET = "jajuk.collection_charset";

	public static final String CONF_STARTUP_LAST_POSITION = "jajuk.startup.last_position";

	public static final String CONF_NETWORK_USE_PROXY = "jajuk.network.use_proxy";

	public static final String CONF_NETWORK_PROXY_TYPE = "jajuk.network.proxy_type";

	public static final String CONF_NETWORK_PROXY_HOSTNAME = "jajuk.network.proxy_hostname";

	public static final String CONF_NETWORK_PROXY_PORT = "jajuk.network.proxy_port";

	public static final String CONF_NETWORK_PROXY_LOGIN = "jajuk.network.proxy_login";
	
	public static final String CONF_NETWORK_PROXY_PWD = "jajuk.network.proxy_pwd";

	public static final String CONF_AUDIOSCROBBLER_ENABLE = "jajuk.network.audioscrobbler";

	public static final String CONF_AUDIOSCROBBLER_USER = "jajuk.network.ASUser";

	public static final String CONF_AUDIOSCROBBLER_PASSWORD = "jajuk.network.ASPassword";

	public static final String CONF_COVERS_AUTO_COVER = "jajuk.covers.auto_cover";

	public static final String CONF_COVERS_SHUFFLE = "jajuk.covers.shuffle";

	public static final String CONF_COVERS_PRELOAD = "jajuk.covers.preload";

	public static final String CONF_COVERS_SIZE = "jajuk.covers.size";

	public static final String CONF_COVERS_ACCURACY = "jajuk.covers.accuracy";

	/** Load cover at each track */
	public static final String CONF_COVERS_CHANGE_AT_EACH_TRACK = "jajuk.covers.change_on_each_track";

	public static final String CONF_NETWORK_CONNECTION_TO = "jajuk.network.connection_timeout";

	/** Last Option selected tab */
	public static final String CONF_OPTIONS_TAB = "jajuk.options.tab";

	/** Data buffer size in bytes */
	public static final String CONF_BUFFER_SIZE = "jajuk.buffer_size";

	/** Audio buffer size in bytes */
	public static final String CONF_AUDIO_BUFFER_SIZE = "jajuk.audio_buffer_size";

	/** Window position and size */
	public static final String CONF_WINDOW_POSITION = "jajuk.window_position";

	/** Files table columns */
	public static final String CONF_FILES_TABLE_COLUMNS = "jajuk.ui.physical_table_columns";

	/** Files table edition state */
	public static final String CONF_FILES_TABLE_EDITION = "jajuk.ui.physical_table_edition";

	/** Tracks table columns */
	public static final String CONF_TRACKS_TABLE_COLUMNS = "jajuk.ui.logical_table_columns";

		/** Tracks table edition state */
	public static final String CONF_TRACKS_TABLE_EDITION = "jajuk.ui.logical_table_edition";

	/** Albums table edition state */
	public static final String CONF_ALBUMS_TABLE_COLUMNS = "jajuk.ui.albums_table_columns";
	
	/** Albums table edition state */
	public static final String CONF_ALBUMS_TABLE_EDITION = "jajuk.ui.albums_table_edition";

	/** Playlist editor columns to display */
	public static final String CONF_PLAYLIST_EDITOR_COLUMNS = "jajuk.ui.playlist_editor_columns";


	/** Catalog items size */
	public static final String CONF_THUMBS_SIZE = "jajuk.ui.cover_catalog.thumbs_size";

	/** Catalog items size */
	public static final String CONF_THUMBS_SHOW_WITHOUT_COVER = "jajuk.ui.cover_catalog.show_without_cover";

	/** Catalog sorter* */
	public static final String CONF_THUMBS_SORTER = "jajuk.catalog.sorter";

	/** Catalog filter* */
	public static final String CONF_THUMBS_FILTER = "jajuk.catalog.filter";

	/** Display tips on startup */
	public static final String CONF_SHOW_TIP_ON_STARTUP = "jajuk.tip.show_on_startup";

	/** Index of current displayed tip */
	public static final String CONF_TIP_OF_DAY_INDEX = "jajuk.tip.index";

	/** Wikipedia language* */
	public static final String CONF_WIKIPEDIA_LANGUAGE = "jajuk.wikipedia.lang";

	/** Cross fade duration in secs */
	public static final String CONF_FADE_DURATION = "jajuk.fade_duration";

	/** Logical tree sort order */
	public static final String CONF_LOGICAL_TREE_SORT_ORDER = "jajuk.logical_tree_sort_order";

	/** Logical tree sort order* */
	public static final String CONF_REFACTOR_PATTERN = "jajuk.refactor_pattern";

	/** Default dj */
	public static final String CONF_DEFAULT_DJ = "jajuk.default_dj";

	/** Default web radio */
	public static final String CONF_DEFAULT_WEB_RADIO = "jajuk.default_web_radio";

	/** List of ignored versions during update check */
	public static final String CONF_IGNORED_RELEASES = "jajuk.update.ignored_releases";

	/** Check for update property */
	public static final String CONF_CHECK_FOR_UPDATE = "jajuk.update.check_for_updates";

	/** Default ambience* */
	public static final String CONF_DEFAULT_AMBIENCE = "jajuk.default_ambience";

	/* New release don't show again flag */
	public static final String CONF_NOT_SHOW_AGAIN_UPDATE = "jajuk.not_show_again.update";

	/* Wrong player not show again flag */
	public static final String CONF_NOT_SHOW_AGAIN_PLAYER = "jajuk.not_show_again.player";

	/** Concurrent session not show again flag */
	public static final String CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION = "jajuk.not_show_again.concurrent_session";

	/** Cross fade alert not show again flag * */
	public static final String CONF_NOT_SHOW_AGAIN_CROSS_FADE = "jajuk.not_show_again.fade";

	/** Laf change not show again flag * */
	public static final String CONF_NOT_SHOW_AGAIN_LAF_CHANGE = "jajuk.not_show_laf_change";

	/** Last.FM disabled not show again flag * */
	public static final String CONF_NOT_SHOW_AGAIN_LASTFM_DISABLED = "jajuk.not_show_lastfm_disable";

	/** Global random mode: song or album level ? * */
	public static final String CONF_GLOBAL_RANDOM_MODE = "jajuk.global_random.mode";

	/** Novelties random mode: song or album level ?* */
	public static final String CONF_NOVELTIES_MODE = "jajuk.global_novelties.mode";

	/** Animation pattern* */
	public static final String CONF_ANIMATION_PATTERN = "jajuk.animation_pattern";

	/** Initial frame size/position forced value* */
	public static final String CONF_FRAME_POS_FORCED = "jajuk.frame.forced_position";

	// Forced mplayer path
	public static final String CONF_MPLAYER_PATH_FORCED = "jajuk.mplayer.forced_path";

	/** Hotkeys flag * */
	public static final String CONF_OPTIONS_HOTKEYS = "jajuk.options.use_hotkeys";

	/** MPLayer additional arguments * */
	public static final String CONF_MPLAYER_ARGS = "jajuk.player.mplayer_args";

	/** MPlayer additional environment variables * */
	public static final String CONF_ENV_VARIABLES = "jajuk.player.env_variables";

	/** Max Number of thumbs displayed at the same time in catalog view */
	public static final String CONF_CATALOG_PAGE_SIZE = "jajuk.catalog.pages_size";

	/** Show Catalog popups */
	public static final String CONF_SHOW_POPUPS = "jajuk.show_popups";
	
	/** Enable Last.FM information queries */
	public static final String CONF_LASTFM_INFO = "jajuk.lastfm_information";

	/** Webradio playing at jajuk stop ?  */
	public static final String CONF_WEBRADIO_WAS_PLAYING = "jajuk.webradio.was_playing";

	/** Font size */
	public static final String CONF_FONTS_SIZE = "jajuk.fonts_size";

	/** Increase rate value */
	public static final String CONF_INC_RATING = "jajuk.inc_rating.step";

	/** Shuffle/novelties mode */
	public static final String MODE_ALBUM = "album";

	public static final String MODE_TRACK = "track";

	public static final String MODE_ALBUM2 = "album2";

	// Accuracy levels
	public static final String ACCURACY_LOW = "low";

	public static final String ACCURACY_NORMAL = "normal";

	public static final String ACCURACY_HIGH = "high";

	// miscelanous
	public static final String TRUE = "true";

	public static final String FALSE = "false";

	// views identifiers
	/** Identifier of the physical tree view */
	public static final String VIEW_PHYSICAL_TREE = "VIEW_PHYSICAL_TREE";

	/** Identifier of the track list view */
	public static final String VIEW_TRACK_LIST = "VIEW_TRACK_LIST";

	// Date format
	public static final String DATE_FILE = "yyyyMMdd";

	public static final String ADDITION_DATE_FORMAT = "yyyyMMdd";

	public static final String DATE_FORMAT_DEFAULT = "Date_Default";

	public static final String DATE_FORMAT_1 = "dd/MM/yyyy";

	public static final String DATE_FORMAT_2 = "yyyy/MM/dd";

	public static final String DATE_FORMAT_3 = "yyyyMMdd";

	// Playlists
	public static final String PLAYLIST_NOTE = "#Playlist generated by Jajuk " + JAJUK_VERSION;

	// XML tags
	public static final String XML_COLLECTION = "collection";

	public static final String XML_VERSION = "jajuk_version";

	public static final String XML_TYPES = "types";

	public static final String XML_TYPE = "type";

	public static final String XML_DEVICES = "devices";

	public static final String XML_DEVICE = "device";

	public static final String XML_STYLES = "styles";

	public static final String XML_STYLE = "style";

	public static final String XML_AUTHORS = "authors";

	public static final String XML_AUTHOR = "author";

	public static final String XML_ALBUMS = "albums";

	public static final String XML_ALBUM = "album";

	public static final String XML_TRACKS = "tracks";

	public static final String XML_TRACK = "track";

	public static final String XML_DIRECTORIES = "directories";

	public static final String XML_DIRECTORY = "directory";

	public static final String XML_DIRECTORY_DEFAULT_COVER = "default_cover";

	public static final String XML_FILES = "files";

	public static final String XML_FILE = "file";

	public static final String XML_PLAYLIST_FILES = "playlist_files";

	public static final String XML_PLAYLIST_FILE = "playlist_file";

	public static final String XML_PLAYLISTS = "playlists";

	public static final String XML_ID = "id";

	public static final String XML_YEAR = "year";

	public static final String XML_YEARS = "years";

	public static final String XML_PLAY = "play";

	public static final String XML_FILE_DATE = "date";

	public static final String XML_TRACK_NAME = "name";

	public static final String XML_TRACK_ALBUM = "album";

	public static final String XML_TRACK_STYLE = "style";

	public static final String XML_TRACK_AUTHOR = "author";

	public static final String XML_TRACK_YEAR = "year";

	public static final String XML_TRACK_LENGTH = "length";

	public static final String XML_TRACK_TYPE = "type";

	public static final String XML_TRACK_RATE = "rate";

	public static final String XML_TRACK_HITS = "hits";

	public static final String XML_TRACK_ADDED = "added";

	public static final String XML_TRACK_ORDER = "order";

	public static final String XML_PLAYLIST = "playlist";

	public static final String XML_NAME = "name";

	public static final String XML_FILE_NAME = "filename";

	public static final String XML_PATH = "path";

	public static final String XML_URL = "url";

	public static final String XML_QUALITY = "quality";

	public static final String XML_SIZE = "size";

	public static final String XML_DEVICE_MOUNT_POINT = "mount_point";

	public static final String XML_DEVICE_AUTO_REFRESH = "auto_refresh";

	public static final String XML_DEVICE_AUTO_MOUNT = "auto_mount";

	public static final String XML_DEVICE_SYNCHRO_SOURCE = "synchro_source";

	public static final String XML_DEVICE_SYNCHRO_MODE = "synchro_mode";

	public static final String XML_EXPANDED = "exp"; // can be 'y' or 'n'

	public static final String XML_DIRECTORY_PARENT = "parent";

	public static final String XML_DIRECTORY_SYNCHRONIZED = "sync";

	public static final String XML_HASHCODE = "hashcode";

	public static final String XML_TYPE_EXTENSION = "extension";

	public static final String XML_TYPE_PLAYER_IMPL = "player_impl";

	public static final String XML_TYPE_TAG_IMPL = "tag_impl";

	public static final String XML_TYPE_IS_MUSIC = "music";

	public static final String XML_TYPE_SEEK_SUPPORTED = "seek";

	// type description as given in the steam
	public static final String XML_TYPE_TECH_DESC = "tech_desc";

	// icon used in the physical tree
	public static final String XML_TYPE_ICON = "icon";

	// comment tag
	public static final String XML_TRACK_COMMENT = "comment";

	// track number
	public static final String XML_TRACK_NUMBER = "number";

	// "any" criteria
	public static final String XML_ANY = "any";

	// constructor property flag
	public static final String XML_CONSTRUCTOR = "constructor";

	// property should be displayed ?
	public static final String XML_VISIBLE = "visible";

	// property editable ?
	public static final String XML_EDITABLE = "editable";

	// Property unique ?
	public static final String XML_UNIQUE = "unique";

	// custom property flag
	public static final String XML_CUSTOM = "custom";

	// Property
	public static final String XML_PROPERTY = "property";

	// default value
	public static final String XML_DEFAULT_VALUE = "default_value";

	// general dj tag
	public static final String XML_DJ_DJ = "dj";

	// general parameters
	public static final String XML_DJ_GENERAL = "general_parameters";

	public static final String XML_DJ_RATING_LEVEL = "rating_level";

	public static final String XML_DJ_UNICITY = "unicity";

	public static final String XML_DJ_FADE_DURATION = "fade_duration";

	public static final String XML_DJ_PROPORTIONS = "proportions";

	public static final String XML_DJ_PROPORTION = "proportion";

	public static final String XML_DJ_AMBIENCES = "ambiences";

	public static final String XML_DJ_AMBIENCE = "ambience";

	public static final String XML_DJ_STYLES = "styles";

	public static final String XML_DJ_VALUE = "values";

	public static final String XML_DJ_PROPORTION_CLASS = "org.jajuk.dj.ProportionDigitalDJ";

	public static final String XML_DJ_TRANSITION_CLASS = "org.jajuk.dj.TransitionDigitalDJ";

	public static final String XML_DJ_AMBIENCE_CLASS = "org.jajuk.dj.AmbienceDigitalDJ";

	public static final String XML_DJ_EXTENSION = "dj";

	public static final String XML_DJ_TRANSITION = "transition";

	public static final String XML_DJ_TRANSITIONS = "transitions";

	public static final String XML_DJ_FROM = "from";

	public static final String XML_DJ_TO = "to";

	public static final String XML_DJ_NUMBER = "number";

	public static final String XML_DJ_STARTUP_STYLE = "startup_style";

	public static final String XML_SUBMISSION = "submission";
	
	public static final String XML_STREAMS = "streams";
	
	public static final String XML_STREAM = "stream";

	/*
	 * Reserved XML tags for property names (note that a user can choose a
	 * property name equals to meta information attributes names without pbm)
	 */
	public static final String[] XML_RESERVED_ATTRIBUTE_NAMES = { XML_NAME, XML_ID,
			XML_TYPE_EXTENSION, XML_TYPE_PLAYER_IMPL, XML_TYPE_TAG_IMPL, XML_TYPE_TECH_DESC,
			XML_TYPE_SEEK_SUPPORTED, XML_TYPE_ICON, XML_TYPE_IS_MUSIC, XML_TYPE, XML_URL,
			XML_DEVICE_AUTO_MOUNT, XML_DEVICE_AUTO_REFRESH, XML_EXPANDED, XML_DEVICE_MOUNT_POINT,
			XML_ALBUM, XML_AUTHOR, XML_STYLE, XML_TRACK_LENGTH, XML_YEAR, XML_TRACK_RATE,
			XML_FILES, XML_TRACK_HITS, XML_TRACK_ADDED, XML_DIRECTORY_PARENT, XML_DEVICE,
			XML_DIRECTORY, XML_TRACK, XML_SIZE, XML_QUALITY, XML_HASHCODE, XML_PLAYLIST_FILES,
			XML_TRACK_COMMENT, XML_ANY, XML_TRACK_ORDER, XML_DEVICE_SYNCHRO_MODE,
			XML_DEVICE_SYNCHRO_SOURCE, XML_FILE_DATE }; // contains

	// variables
	// names

	public static final String PROPERTY_SEPARATOR = "Property_";

	// Formats
	public static final String FORMAT_STRING = "Property_Format_String";

	public static final String FORMAT_NUMBER = "Property_Format_Number";

	public static final String FORMAT_BOOLEAN = "Property_Format_Boolean";

	public static final String FORMAT_FLOAT = "Property_Format_Float";

	public static final String FORMAT_DATE = "Property_Format_Date";

	// Thumbs
	public static final String THUMBNAIL_SIZE_50x50 = "50x50";

	public static final String THUMBNAIL_SIZE_100x100 = "100x100";

	public static final String THUMBNAIL_SIZE_150x150 = "150x150";

	public static final String THUMBNAIL_SIZE_200x200 = "200x200";

	public static final String THUMBNAIL_SIZE_250x250 = "250x250";

	public static final String THUMBNAIL_SIZE_300x300 = "300x300";

	// Patterns
	public static final String PATTERN_AUTHOR = "%artist";

	public static final String PATTERN_ALBUM = "%album";

	public static final String PATTERN_STYLE = "%genre";

	public static final String PATTERN_YEAR = "%year";

	public static final String PATTERN_TRACKNAME = "%title";

	public static final String PATTERN_TRACKORDER = "%n";

	public static final String PATTERN_DEFAULT_REORG = PATTERN_AUTHOR + "/" + PATTERN_YEAR + " - "
			+ PATTERN_ALBUM + "/" + PATTERN_TRACKORDER + " - " + PATTERN_TRACKNAME;

	public static final String PATTERN_DEFAULT_ANIMATION = PATTERN_AUTHOR + " / " + PATTERN_ALBUM
			+ " / " + PATTERN_TRACKNAME;

	// Actions
	public static final String ACTION_NEXT = "next";

	public static final String ACTION_PREV = "prev";

	public static final String ACTION_FINISH = "finish";

	public static final String ACTION_Cancel = "cancel";

	// Strings
	public static final String FRAME_MAXIMIZED = "max";

}