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
public interface Const {

  // Misc
  /** Information bar and command bar default vertical size in pixels */
  int BORDER_Y_SIZE = 112;

  /** Command bar default vertical size in pixels */
  int BORDER_X_SIZE = 55;

  /** panels precisions in percent */
  int PRECISION = 5;

  /** Generic border */
  int BORDER = 4;

  /** Maximum size for covers in KB */
  int MAX_COVER_SIZE = 1025;

  /** Maximum number of remote covers */
  int MAX_REMOTE_COVERS = 10;

  /** Special length for player meaning end of file */
  long TO_THE_END = -1;

  /** Time we wait after an error in ms */
  int WAIT_AFTER_ERROR = 2000;

  /** Default playlist name */
  String DEFAULT_PLAYLIST_FILE = "playlist";

  /** Max number of connection time out before stopping to look for covers */
  int STOP_TO_SEARCH = 5;

  /** Listening port to check others jajuk sessions */
  int PORT = 62322;

  /** Listening port to check others test jajuk sessions */
  int PORT_TEST = 62323;

  /** Quality agent e-mail */
  String FEEDBACK_EMAIL = "jajuk-support@lists.sourceforge.net";

  /** Number of lines in the feedback mail */
  int FEEDBACK_LINES = 200;

  /** Max history size */
  int MAX_HISTORY_SIZE = 1000;

  /** Autocommit delay in ms */
  int AUTO_COMMIT_DELAY = 3600000;

  /** Autorefresh thread delay in ms */
  int AUTO_REFRESH_DELAY = 30000;

  /** Minimum refresh fequency (ms) */
  int MIN_AUTO_REFRESH_DELAY = 30000;

  /** Garbager delay in ms */
  int GARBAGER_DELAY = 600000;

  /** Fading status code */
  int FADING_STATUS = 9999;

  /** Need full gc memory % */
  float NEED_FULL_GC_LEVEL = 0.90f;

  /** Play time to get rate +1 in secs */
  int INC_RATE_TIME = 20;

  /** Min Number of tracks in an AuthorNode */
  int MIN_TRACKS_NUMBER = 4;

  /** Min number of tracks in a DJ selection without track unicity */
  int MIN_TRACKS_NUMBER_WITHOUT_UNICITY = 100;

  /** Default number of tracks in a transition */
  int DEFAULT_TRANSITION_TRACK_NUMBER = 2;

  /** Rate Stars columns size in pixels */
  int RATE_COLUMN_SIZE = 75;

  /** Play icon columns size in pixels */
  int PLAY_COLUMN_SIZE = 20;

  /** Proportion of best tracks */
  float BESTOF_PROPORTION = 0.05f;

  /** Number of milliseconds in a second */
  int MILLISECONDS_IN_A_SECOND = 1000;

  /** Number of seconds in a minute */
  int SECONDS_IN_A_MINUTE = 60;

  /** Number of minutes in an hour */
  int MINUTES_IN_AN_HOUR = 60;

  /** Number of pixels around window at initial startup */
  int FRAME_INITIAL_BORDER = 60;

  /** Default webradio */
  String DEFAULT_WEBRADIO = "Bluemars [Ambient/Space-Music]";

  /** Default hostame used in case of problem */
  String DEFAULT_HOSTNAME = "localhost";

  /** Number of hours in a day */
  int HOURS_IN_A_DAY = 24;

  /** Number of album bestof items */
  int NB_BESTOF_ALBUMS = 25;

  /** Number of album novelties items */
  int NB_NOVELTIES_ALBUMS = 8;

  /* Number of tracks for global actions (global shuffle, bestof...) */
  int NB_TRACKS_ON_ACTION = 200;
  
  /**
   * The time we wait for mplayer to start up in secs. 
   * It can take some time on slow or
   * heavily loaded machines...
   */
  int MPLAYER_START_TIMEOUT = 10;

  /** Number of milliseconds in a day */
  int MILLISECONDS_IN_A_DAY = MILLISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTE * MINUTES_IN_AN_HOUR
      * HOURS_IN_A_DAY;

  /** Last alapsed time display mode format + 1 * */
  int FORMAT_TIME_ELAPSED_MAX = 4;

  /** Mplayer windows exe size in bytes */
  long MPLAYER_EXE_SIZE = 12818944l;

  String FILE_DEFAULT_MPLAYER_X86_OSX_PATH = "/Applications/MPlayer OSX.app/Contents/Resources/External_Binaries/mplayer_intel.app/Contents/MacOS/mplayer";

  String FILE_DEFAULT_MPLAYER_POWER_OSX_PATH = "/Applications/MPlayer OSX PPC.app/Contents/Resources/External_Binaries/mplayer_ppc.app/Contents/MacOS/mplayer";

  /** Wikipedia view default URL */
  String WIKIPEDIA_VIEW_DEFAULT_URL = "http://jajuk.info";

  /** Update URL = PAD file URL */
  String CHECK_FOR_UPDATE_URL = "http://jajuk.svn.sourceforge.net/svnroot/jajuk/trunk/jajuk/src/site/resources/jajuk_pad.xml";

  // Jajuk version
  String JAJUK_VERSION = "VERSION_REPLACED_BY_ANT";

  String JAJUK_CODENAME = "Firestarter";

  /** Extention to the .jajuk_test directory in test mode only */
  String TEST_VERSION = "1.7";

  String JAJUK_VERSION_DATE = "Build: DATE_REPLACED_BY_ANT";

  String JAJUK_COPYRIGHT = "Copyright 2004-2008 The Jajuk Team";

  // -- Files and paths --

  /** Bootstrap file that contains jajuk configuration user directory* */
  String FILE_BOOTSTRAP = System.getProperty("user.home") + "/.jajuk_bootstrap";

  String FILE_COLLECTION = "collection.xml";

  String FILE_REPORTING_CACHE_FILE = "cache/report";

  String FILE_CONFIGURATION = "conf.properties";

  String FILE_HISTORY = "history.xml";

  /** Directory containing all sessions using this workspace */
  String FILE_SESSIONS = "sessions";

  String FILE_DEFAULT_COVER = "cover";

  String FILE_DEFAULT_COVER_2 = "front";

  String FILE_ABSOLUTE_DEFAULT_COVER = "jajuk-default-cover.";

  // langpack name : jajuk_<locale>.properties
  String FILE_LANGPACK_PART1 = "jajuk";

  String FILE_LANGPACK_PART2 = ".properties";

  // logs
  String FILE_LOGS = "jajuk.log";

  URL FILE_LOG4J_CONF = UtilSystem.getResource("org/jajuk/util/log/jajuk-log4j-conf.xml");

  URL FILE_JRE_LOG_CONF = UtilSystem.getResource("org/jajuk/util/log/logging.properties");

  String FILE_CACHE = "cache";

  String FILE_INTERNAL_CACHE = "internal";

  String FILE_THUMBS = "thumbs";

  String FILE_THUMB_NO_COVER = "nocover.png";

  String FILE_DEFAULT_BESTOF_PLAYLIST = "bestof";

  String FILE_DEFAULT_NOVELTIES_PLAYLIST = "novelties";

  String FILE_DEFAULT_BOOKMARKS_PLAYLIST = "bookmarks";

  String FILE_DEFAULT_QUEUE_PLAYLIST = "jajuk-playlist_";

  String FILE_DJ_DIR = "djs";

  String FILE_WEB_RADIOS_REPOS = "webradios.xml";

  String FILE_FIFO = "fifo.lst";

  String FILE_MPLAYER_EXE = "mplayer.exe";

  String FILE_TOOLBARS_CONF = "toolbars.xml";

  String FILE_DEFAULT_PERSPECTIVES_PATH = "perspectives";

  String FILE_JAJUK_DOWNLOADED_FILES_SUFFIX = "_jajuk";

  String FILE_REPORTING_CSS_ALL_FILENAME = "report-all.css";

  URL FILE_REPORTING_CSS_ALL_PATH = UtilSystem.getResource("xslt/"
      + FILE_REPORTING_CSS_ALL_FILENAME);

  String FILE_REPORTING_CSS_PRINT_FILENAME = "report-print.css";

  URL FILE_REPORTING_CSS_PRINT_PATH = UtilSystem.getResource("xslt/"
      + FILE_REPORTING_CSS_PRINT_FILENAME);

  String FILE_BACKGROUND_IMAGE = "background.jpg";

  String URL_MPLAYER = "http://repository.jajuk.info/mplayer/1.0rc2/mplayer.exe";

  String URL_DEFAULT_WEBRADIOS = "http://svn2.assembla.com/svn/common-jukebox/common-jukebox/src/main/resources/preset_radios.xml";

  // About
  String ABOUT = "<html>Jajuk version " + JAJUK_VERSION + "</html>";

  // Properties
  String PROPERTY_SEQ = "sequence";// playlist item

  // sequence
  String AMBIENCE_PREFIX = "jajuk.ambience.";

  // directory path
  String PATH_RELATIVE_DOCS = "docs/";

  // Proxy type: http
  String PROXY_TYPE_HTTP = "http";

  // Proxy type: socks
  String PROXY_TYPE_SOCKS = "socks";

  // Unknown
  String UNKNOWN_AUTHOR = "unknown_author";

  String UNKNOWN_ALBUM = "unknown_album";

  String UNKNOWN_STYLE = "unknown_style";

  String UNKNOWN_YEAR = "unknown_year";

  String UNKNOWN = "unknown";

  // images
  URL IMAGES_SPLASHSCREEN = UtilSystem.getResource("images/included/jajuk-splashscreen.jpg");

  URL IMAGE_SEARCH = UtilSystem.getResource("images/included/search.png");

  URL IMAGE_TRAY_TITLE = UtilSystem.getResource("images/included/tray_title.png");

  // XSLT Files
  URL XSLT_AUTHOR = UtilSystem.getResource("xslt/author.xsl");

  URL XSLT_ALBUM = UtilSystem.getResource("xslt/album.xsl");

  URL XSLT_STYLE = UtilSystem.getResource("xslt/style.xsl");

  URL XSLT_YEAR = UtilSystem.getResource("xslt/style.xsl");

  URL XSLT_DIRECTORY = UtilSystem.getResource("xslt/directory.xsl");

  URL XSLT_DEVICE = UtilSystem.getResource("xslt/device.xsl");

  URL XSLT_COLLECTION_LOGICAL = UtilSystem.getResource("xslt/logical_collection.xsl");

  String COLLECTION_LOGICAL = "logical";

  String COLLECTION_PHYSICAL = "physical";

  String ALL = "all";

  String LOCAL = "local";

  // Command line options

  // Tells jajuk it is inside the IDE
  String CLI_IDE = "ide";

  // Tells jajuk to use a .jajuk_test repository
  String CLI_TEST = "test";

  // Tells jajuk to use Power Pack behavior
  String CLI_POWER_PACK = "powerpack";

  String FREE_MUSIC_DIR = "Music";

  String FREE_MUSIC_DEVICE_NAME = "Music-jukeboxPowerPack";

  // players impls
  String PLAYER_IMPL_JAVALAYER = "org.jajuk.services.players.JavaLayerPlayerImpl";

  String PLAYER_IMPL_MPLAYER = "org.jajuk.services.players.MPlayerPlayerImpl";

  String PLAYER_IMPL_WEBRADIOS = "org.jajuk.services.players.WebRadioPlayerImpl";

  // tag impls
  String TAG_IMPL_JID3LIB = "org.jajuk.services.tags.JID3LibTagImpl";

  String TAG_IMPL_MP3INFO = "org.jajuk.services.tags.MP3InfoTagImpl";

  String TAG_IMPL_JLGUI_MP3 = "org.jajuk.services.tags.JlGuiMP3TagImpl";

  String TAG_IMPL_JLGUI_OGG = "org.jajuk.services.tags.JlGuiOggTagImpl";

  String TAG_IMPL_NO_TAGS = "org.jajuk.services.tags.NoTagsTagImpl";

  String TAG_IMPL_ENTAGGED = "org.jajuk.services.tags.EntaggedTagImpl";

  String TAG_IMPL_JAUDIOTAGGER = "org.jajuk.services.tags.JAudioTaggerTagImpl";

  // device types
  String DEVICE_TYPE_DIRECTORY = "Device_type.directory";

  String DEVICE_TYPE_FILE_CD = "Device_type.file_cd";

  String DEVICE_TYPE_USBKEY = "Device_type.usbkey";

  // Devices sync mode
  String DEVICE_SYNCHRO_MODE_BI = "bi";

  String DEVICE_SYNCHRO_MODE_UNI = "uni";

  // views
  String VIEW_NAME_DEVICES = "org.jajuk.ui.views.DeviceView";

  String VIEW_NAME_PARAMETERS = "org.jajuk.ui.views.ParameterView";

  String VIEW_NAME_PHYSICAL_TREE = "org.jajuk.ui.views.PhysicalTreeView";

  String VIEW_NAME_PHYSICAL_TABLE = "org.jajuk.ui.views.PhysicalTableView";

  String VIEW_NAME_LOGICAL_TREE = "org.jajuk.ui.views.LogicalTreeView";

  String VIEW_NAME_LOGICAL_TABLE = "org.jajuk.ui.views.LogicalTableView";

  String VIEW_NAME_ABOUT = "org.jajuk.ui.views.AboutView";

  String VIEW_NAME_HELP = "org.jajuk.ui.views.HelpView";

  String VIEW_NAME_STAT = "org.jajuk.ui.views.StatView";

  String VIEW_NAME_COVER = "org.jajuk.ui.views.CoverView";

  String VIEW_NAME_PHYSICAL_PLAYLIST_EDITOR = "org.jajuk.ui.views.PhysicalPlaylistEditorView";

  String VIEW_NAME_PHYSICAL_NAVIGATION_BAR = "org.jajuk.ui.views.PhysicalNavigationBarView";

  String VIEW_NAME_LOGICAL_PLAYLIST_REPOSITORY = "org.jajuk.ui.views.LogicalPlaylistRepositoryView";

  String VIEW_NAME_LOGICAL_PLAYLIST_EDITOR = "org.jajuk.ui.views.LogicalPlaylistEditorView";

  String VIEW_NAME_LOGICAL_NAVIGATION_BAR = "org.jajuk.ui.views.LogicalNavigationBarView";

  String VIEW_NAME_CD_SCAN = "org.jajuk.ui.views.CDScanView";

  // extensions
  String EXT_MP3 = "mp3";
  String EXT_MP2 = "mp2";
  String EXT_PLAYLIST = "m3u";
  String EXT_OGG = "ogg";
  String EXT_WAV = "wav";
  String EXT_AU = "au";
  String EXT_AIFF = "aiff";
  String EXT_FLAC = "flac";
  String EXT_THUMB = "jpg";
  String EXT_MPC = "mpc";
  String EXT_MPPLUS = "mp+";
  String EXT_MPP = "mpp";
  String EXT_WMA = "wma";
  String EXT_APE = "ape";
  String EXT_APL = "apl";
  String EXT_MAC = "mac";
  String EXT_AAC = "aac";
  String EXT_M4A = "m4a";
  String EXT_REAL = "ram";
  String EXT_REAL_RM = "rm";
  String EXT_REAL_RA = "ra";
  String EXT_RADIO = "radio";
  String EXT_AVI = "avi";
  String EXT_MPG = "mpg";
  String EXT_MPEG = "mpeg";
  String EXT_MKV = "mkv";
  String EXT_ASF = "asf";
  String EXT_WMV = "wmv";
  String EXT_MOV = "mov";
  String EXT_OGM = "ogm";
  String EXT_MP4 = "mp4";
  String EXT_WV = "wv";

  // details keys
  String DETAIL_CURRENT_FILE_ID = "current file id";

  String DETAIL_CURRENT_FILE = "current file";

  String DETAIL_CURRENT_DATE = "current date";

  String DETAIL_CURRENT_POSITION = "current position";

  String DETAIL_TOTAL = "total time";

  String DETAIL_HISTORY_ITEM = "history item";

  String DETAIL_SPECIAL_MODE = "special mode";

  String DETAIL_SPECIAL_MODE_SHUFFLE = "shuffle";

  String DETAIL_SPECIAL_MODE_BESTOF = "bestof";

  String DETAIL_SPECIAL_MODE_NOVELTIES = "novelties";

  String DETAIL_SPECIAL_MODE_NORMAL = "norm";

  String DETAIL_SELECTION = "selection";

  String DETAIL_ENABLE = "enable";

  String DETAIL_ORIGIN = "origin";

  String DETAIL_REASON = "reason";

  String DETAIL_CONTENT = "content";

  String DETAIL_OLD = "old";

  String DETAIL_NEW = "new";

  String DETAIL_OPTION = "option";

  String DETAIL_TARGET = "target";

  // startup mode
  String STARTUP_MODE_NOTHING = "nothing";

  String STARTUP_MODE_SHUFFLE = "shuffle";

  String STARTUP_MODE_FILE = "file";

  String STARTUP_MODE_LAST = "last";

  String STARTUP_MODE_LAST_KEEP_POS = "last_keep_pos";

  String STARTUP_MODE_BESTOF = "bestof";

  String STARTUP_MODE_NOVELTIES = "novelties";

  // --- Configuration keys ---

  String CONF_RELEASE = "jajuk.release";

  String CONF_PERSPECTIVE_DEFAULT = "jajuk.preference.perspective.default";

  String CONF_STATE_REPEAT = "jajuk.state.mode.repeat";

  String CONF_STATE_SHUFFLE = "jajuk.state.mode.shuffle";

  String CONF_STATE_CONTINUE = "jajuk.state.mode.continue";

  String CONF_STATE_INTRO = "jajuk.state.mode.intro";

  String CONF_STARTUP_FILE = "jajuk.startup.file";

  String CONF_STARTUP_MODE = "jajuk.startup.mode";

  String CONF_CONFIRMATIONS_DELETE_FILE = "jajuk.confirmations.delete_file";

  String CONF_CONFIRMATIONS_EXIT = "jajuk.confirmations.exit";

  String CONF_CONFIRMATIONS_REMOVE_DEVICE = "jajuk.confirmations.remove_device";

  String CONF_CONFIRMATIONS_DELETE_COVER = "jajuk.confirmations.delete_cover";

  String CONF_CONFIRMATIONS_CLEAR_HISTORY = "jajuk.confirmations.clear_history";

  String CONF_CONFIRMATIONS_RESET_RATINGS = "jajuk.confirmations.reset_ratings";

  String CONF_CONFIRMATIONS_REFACTOR_FILES = "jajuk.confirmations.refactor_files";

  String CONF_OPTIONS_HIDE_UNMOUNTED = "jajuk.options.hide_unmounted";

  String CONF_OPTIONS_LOG_LEVEL = "jajuk.options.log_level";

  String CONF_OPTIONS_LANGUAGE = "jajuk.options.language";

  String CONF_OPTIONS_INTRO_BEGIN = "jajuk.options.intro.begin";

  String CONF_OPTIONS_INTRO_LENGTH = "jajuk.options.intro.length";

  String CONF_OPTIONS_LNF = "jajuk.options.lnf";

  String CONF_OPTIONS_NOVELTIES_AGE = "jajuk.options.novelties";

  // Look and Feel
  /** Substance default theme * */
  String LNF_DEFAULT_THEME = "Nebula";

  /** Number of visible planned tracks */
  String CONF_OPTIONS_VISIBLE_PLANNED = "jajuk.options.visible_planned";

  /** Default action (play or push) when clicking on an item */
  String CONF_OPTIONS_PUSH_ON_CLICK = "jajuk.options.default_action_click";

  /** Default action (play or push) when dropping on an item */
  String CONF_OPTIONS_DEFAULT_ACTION_DROP = "jajuk.options.default_action_drop";

  /** Synchronize table and tree views */
  String CONF_OPTIONS_SYNC_TABLE_TREE = "jajuk.options.sync_table_tree";

  /** Show popup */
  String CONF_UI_SHOW_BALLOON = "jajuk.options.show_popup";

  String CONF_P2P_SHARE = "jajuk.options.p2p.share";

  String CONF_P2P_ADD_REMOTE_PROPERTIES = "jajuk.options.p2p.add_remote_properties";

  String CONF_P2P_HIDE_LOCAL_PROPERTIES = "jajuk.options.p2p.hide_local_properties";

  String CONF_P2P_PASSWORD = "jajuk.options.p2p.password";

  String CONF_HISTORY = "jajuk.options.history";

  String CONF_TAGS_USE_PARENT_DIR = "jajuk.tags.use_parent_dir";

  /** Contains files id separated by a colon */
  String CONF_BOOKMARKS = "jajuk.bookmarks";

  /**
   * Startup display mode: 0= main window + tray, 1: tray only, 2: slimbar +
   * tray
   */
  String CONF_STARTUP_DISPLAY = "jajuk.startup_display_mode";

  int DISPLAY_MODE_WINDOW_TRAY = 0;

  int DISPLAY_MODE_TRAY = 1;

  int DISPLAY_MODE_SLIMBAR_TRAY = 2;

  /** Best of size */
  String CONF_BESTOF_TRACKS_SIZE = "jajuk.bestof_size";

  /** Slimbar position */
  String CONF_SLIMBAR_POSITION = "jajuk.slimbar_pos";

  /** Gain (float) */
  String CONF_VOLUME = "jajuk.volume";

  /** Use regular expressions ? */
  String CONF_REGEXP = "jajuk.regexp";

  /** Collection backup size in MB */
  String CONF_BACKUP_SIZE = "jajuk.backup_size";

  /** Collection file charset (utf-8 or utf-16) */
  String CONF_COLLECTION_CHARSET = "jajuk.collection_charset";

  String CONF_STARTUP_LAST_POSITION = "jajuk.startup.last_position";

  String CONF_NETWORK_USE_PROXY = "jajuk.network.use_proxy";

  String CONF_NETWORK_PROXY_TYPE = "jajuk.network.proxy_type";

  String CONF_NETWORK_PROXY_HOSTNAME = "jajuk.network.proxy_hostname";

  String CONF_NETWORK_PROXY_PORT = "jajuk.network.proxy_port";

  String CONF_NETWORK_PROXY_LOGIN = "jajuk.network.proxy_login";

  String CONF_NETWORK_PROXY_PWD = "jajuk.network.proxy_pwd";

  String CONF_LASTFM_ENABLE = "jajuk.network.audioscrobbler";

  String CONF_LASTFM_USER = "jajuk.network.ASUser";

  String CONF_LASTFM_PASSWORD = "jajuk.network.ASPassword";

  String CONF_COVERS_AUTO_COVER = "jajuk.covers.auto_cover";

  String CONF_COVERS_SHUFFLE = "jajuk.covers.shuffle";

  String CONF_COVERS_SIZE = "jajuk.covers.size";

  String CONF_COVERS_ACCURACY = "jajuk.covers.accuracy";

  String CONF_NETWORK_CONNECTION_TO = "jajuk.network.connection_timeout";

  /** Last Option selected tab */
  String CONF_OPTIONS_TAB = "jajuk.options.tab";

  /** Data buffer size in bytes */
  String CONF_BUFFER_SIZE = "jajuk.buffer_size";

  /** Audio buffer size in bytes */
  String CONF_AUDIO_BUFFER_SIZE = "jajuk.audio_buffer_size";

  /** Window position and size */
  String CONF_WINDOW_POSITION = "jajuk.window_position";

  /** Window position and size */
  String CONF_FRAME_TITLE_PATTERN = "jajuk.frame.title";

  /** Files table columns */
  String CONF_FILES_TABLE_COLUMNS = "jajuk.ui.physical_table_columns";

  /** Files table edition state */
  String CONF_FILES_TABLE_EDITION = "jajuk.ui.physical_table_edition";

  /** Tracks table columns */
  String CONF_TRACKS_TABLE_COLUMNS = "jajuk.ui.logical_table_columns";

  /** Tracks table edition state */
  String CONF_TRACKS_TABLE_EDITION = "jajuk.ui.logical_table_edition";

  /** Albums table edition state */
  String CONF_ALBUMS_TABLE_COLUMNS = "jajuk.ui.albums_table_columns";

  /** Albums table edition state */
  String CONF_ALBUMS_TABLE_EDITION = "jajuk.ui.albums_table_edition";

  /** Playlist editor columns to display */
  String CONF_PLAYLIST_EDITOR_COLUMNS = "jajuk.ui.playlist_editor_columns";

  /** Playlist repository columns to display */
  String CONF_PLAYLIST_REPOSITORY_COLUMNS = "jajuk.ui.playlist_repository_columns";

  /** Queue columns to display */
  String CONF_QUEUE_COLUMNS = "jajuk.ui.queue_columns";

  /** Catalog items size */
  String CONF_THUMBS_SIZE = "jajuk.ui.cover_catalog.thumbs_size";

  /** Catalog items size */
  String CONF_THUMBS_SHOW_WITHOUT_COVER = "jajuk.ui.cover_catalog.show_without_cover";

  /** Catalog sorter* */
  String CONF_THUMBS_SORTER = "jajuk.catalog.sorter";

  /** Catalog filter* */
  String CONF_THUMBS_FILTER = "jajuk.catalog.filter";

  /** Display tips on startup */
  String CONF_SHOW_TIP_ON_STARTUP = "jajuk.tip.show_on_startup";

  /** Index of current displayed tip */
  String CONF_TIP_OF_DAY_INDEX = "jajuk.tip.index";

  /** Wikipedia language* */
  String CONF_WIKIPEDIA_LANGUAGE = "jajuk.wikipedia.lang";

  /** Cross fade duration in secs */
  String CONF_FADE_DURATION = "jajuk.fade_duration";

  /** Logical tree sort order */
  String CONF_LOGICAL_TREE_SORT_ORDER = "jajuk.logical_tree_sort_order";

  /** Logical tree sort order* */
  String CONF_REFACTOR_PATTERN = "jajuk.refactor_pattern";

  /** Default dj */
  String CONF_DEFAULT_DJ = "jajuk.default_dj";

  /** Default web radio */
  String CONF_DEFAULT_WEB_RADIO = "jajuk.default_web_radio";

  /** List of ignored versions during update check */
  String CONF_IGNORED_RELEASES = "jajuk.update.ignored_releases";

  /** Check for update property */
  String CONF_CHECK_FOR_UPDATE = "jajuk.update.check_for_updates";

  /** Default ambience* */
  String CONF_DEFAULT_AMBIENCE = "jajuk.default_ambience";

  /* Wrong player not show again flag */
  String CONF_NOT_SHOW_AGAIN_PLAYER = "jajuk.not_show_again.player";

  /** Concurrent session not show again flag */
  String CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION = "jajuk.not_show_again.concurrent_session";

  /** Cross fade alert not show again flag * */
  String CONF_NOT_SHOW_AGAIN_CROSS_FADE = "jajuk.not_show_again.fade";

  /** Laf change not show again flag * */
  String CONF_NOT_SHOW_AGAIN_LAF_CHANGE = "jajuk.not_show_laf_change";

  /** Last.FM disabled not show again flag * */
  String CONF_NOT_SHOW_AGAIN_LASTFM_DISABLED = "jajuk.not_show_lastfm_disable";

  /** Global random mode: song or album level ? * */
  String CONF_GLOBAL_RANDOM_MODE = "jajuk.global_random.mode";

  /** Novelties random mode: song or album level ?* */
  String CONF_NOVELTIES_MODE = "jajuk.global_novelties.mode";

  /** Animation pattern* */
  String CONF_ANIMATION_PATTERN = "jajuk.animation_pattern";

  /** Initial frame size/position forced value * */
  String CONF_FRAME_POS_FORCED = "jajuk.frame.forced_position";

  // Forced mplayer path
  String CONF_MPLAYER_PATH_FORCED = "jajuk.mplayer.forced_path";

  /** Hotkeys flag * */
  String CONF_OPTIONS_HOTKEYS = "jajuk.options.use_hotkeys";

  /** MPLayer additional arguments * */
  String CONF_MPLAYER_ARGS = "jajuk.player.mplayer_args";

  /** MPlayer additional environment variables * */
  String CONF_ENV_VARIABLES = "jajuk.player.env_variables";

  /** Max Number of thumbs displayed at the same time in catalog view */
  String CONF_CATALOG_PAGE_SIZE = "jajuk.catalog.pages_size";

  /** Show Catalog popups */
  String CONF_SHOW_POPUPS = "jajuk.show_popups";

  /** Enable Last.FM information queries */
  String CONF_LASTFM_INFO = "jajuk.lastfm_information";

  /** Webradio playing at jajuk stop ? */
  String CONF_WEBRADIO_WAS_PLAYING = "jajuk.webradio.was_playing";

  /** Font size */
  String CONF_FONTS_SIZE = "jajuk.fonts_size";

  /** Increase rate value */
  String CONF_INC_RATING = "jajuk.inc_rating.step";

  /** Use file date as discovery date option */
  String CONF_FORCE_FILE_DATE = "jajuk.force_file_date";

  /** Perspective chooser icon size: 16x16, 32x32 or 40x40 */
  String CONF_PERSPECTIVE_ICONS_SIZE = "jajuk.ui.perspective_icons_size";

  /** Show duplicate playlists in playlist view */
  String CONF_SHOW_DUPLICATE_PLAYLISTS = "jajuk.ui.show_duplicate_playlists";

  /** Force hiding the tray */
  String CONF_FORCE_TRAY_SHUTDOWN = "jajuk.force_tray_shutdown";

  /** Smart mode selected in slimbar */
  String CONF_SLIMBAR_SMART_MODE = "jajuk.ui.slimbar.smart_mode";

  /** Shuffle/novelties mode */
  String MODE_ALBUM = "album";

  String MODE_TRACK = "track";

  String MODE_ALBUM2 = "album2";

  // Accuracy levels
  String ACCURACY_LOW = "low";

  String ACCURACY_NORMAL = "normal";

  String ACCURACY_HIGH = "high";

  // miscelanous
  String TRUE = "true";

  String FALSE = "false";

  // views identifiers
  /** Identifier of the physical tree view */
  String VIEW_PHYSICAL_TREE = "VIEW_PHYSICAL_TREE";

  /** Identifier of the track list view */
  String VIEW_TRACK_LIST = "VIEW_TRACK_LIST";

  // Date format
  String DATE_FILE = "yyyyMMdd";

  String ADDITION_DATE_FORMAT = "yyyyMMdd";

  String DATE_FORMAT_DEFAULT = "Date_Default";

  String DATE_FORMAT_1 = "dd/MM/yyyy";

  String DATE_FORMAT_2 = "yyyy/MM/dd";

  String DATE_FORMAT_3 = "yyyyMMdd";

  // Playlists
  String PLAYLIST_NOTE = "#Playlist generated by Jajuk " + Const.JAJUK_VERSION;

  // XML tags
  String XML_COLLECTION = "collection";

  String XML_VERSION = "jajuk_version";

  String XML_TYPES = "types";

  String XML_TYPE = "type";

  String XML_DEVICES = "devices";

  String XML_DEVICE = "device";

  String XML_STYLES = "styles";

  String XML_STYLE = "style";

  String XML_AUTHORS = "authors";

  String XML_AUTHOR = "author";

  String XML_ALBUMS = "albums";

  String XML_ALBUM = "album";

  String XML_TRACKS = "tracks";

  String XML_TRACK = "track";

  String XML_DIRECTORIES = "directories";

  String XML_DIRECTORY = "directory";

  String XML_DIRECTORY_DEFAULT_COVER = "default_cover";

  String XML_FILES = "files";

  String XML_FILE = "file";

  String XML_PLAYLIST_FILES = "playlist_files";

  String XML_PLAYLIST_FILE = "playlist_file";

  String XML_PLAYLISTS = "playlists";

  String XML_ID = "id";

  String XML_YEAR = "year";

  String XML_YEARS = "years";

  String XML_PLAY = "play";

  String XML_FILE_DATE = "date";

  String XML_TRACK_NAME = "name";

  String XML_TRACK_ALBUM = "album";

  String XML_TRACK_STYLE = "style";

  String XML_TRACK_AUTHOR = "author";

  String XML_TRACK_YEAR = "year";

  String XML_TRACK_LENGTH = "length";

  String XML_TRACK_TYPE = "type";

  String XML_TRACK_RATE = "rate";

  String XML_TRACK_HITS = "hits";

  String XML_TRACK_DISCOVERY_DATE = "added";

  String XML_TRACK_ORDER = "order";

  String XML_TRACK_PREFERENCE = "pf";

  String XML_TRACK_TOTAL_PLAYTIME = "tpt";

  String XML_TRACK_BANNED = "ban";

  String XML_PLAYLIST = "playlist";

  String XML_NAME = "name";

  String XML_FILE_NAME = "filename";

  String XML_PATH = "path";

  String XML_URL = "url";

  String XML_QUALITY = "quality";

  String XML_SIZE = "size";

  String XML_DEVICE_MOUNT_POINT = "mount_point";

  String XML_DEVICE_AUTO_REFRESH = "auto_refresh";

  String XML_DEVICE_AUTO_MOUNT = "auto_mount";

  String XML_DEVICE_SYNCHRO_SOURCE = "synchro_source";

  String XML_DEVICE_SYNCHRO_MODE = "synchro_mode";

  String XML_EXPANDED = "exp"; // can be 'y' or 'n'

  String XML_DIRECTORY_PARENT = "parent";

  String XML_DIRECTORY_SYNCHRONIZED = "sync";

  String XML_TYPE_EXTENSION = "extension";

  String XML_TYPE_PLAYER_IMPL = "player_impl";

  String XML_TYPE_TAG_IMPL = "tag_impl";

  String XML_TYPE_IS_MUSIC = "music";

  String XML_TYPE_SEEK_SUPPORTED = "seek";

  // icon used in the physical tree
  String XML_TYPE_ICON = "icon";

  // comment tag
  String XML_TRACK_COMMENT = "comment";

  // track number
  String XML_TRACK_NUMBER = "number";

  // "any" criteria
  String XML_ANY = "any";

  // constructor property flag
  String XML_CONSTRUCTOR = "constructor";

  // property should be displayed ?
  String XML_VISIBLE = "visible";

  // property editable ?
  String XML_EDITABLE = "editable";

  // Property unique ?
  String XML_UNIQUE = "unique";

  // custom property flag
  String XML_CUSTOM = "custom";

  // Property
  String XML_PROPERTY = "property";

  // default value
  String XML_DEFAULT_VALUE = "default_value";

  // general dj tag
  String XML_DJ_DJ = "dj";

  // general parameters
  String XML_DJ_GENERAL = "general_parameters";

  String XML_DJ_RATING_LEVEL = "rating_level";

  String XML_DJ_UNICITY = "unicity";

  String XML_DJ_FADE_DURATION = "fade_duration";

  String XML_DJ_PROPORTIONS = "proportions";

  String XML_DJ_PROPORTION = "proportion";

  String XML_DJ_AMBIENCES = "ambiences";

  String XML_DJ_AMBIENCE = "ambience";

  String XML_DJ_STYLES = "styles";

  String XML_DJ_VALUE = "values";

  String XML_DJ_PROPORTION_CLASS = "org.jajuk.services.dj.ProportionDigitalDJ";

  String XML_DJ_TRANSITION_CLASS = "org.jajuk.services.dj.TransitionDigitalDJ";

  String XML_DJ_AMBIENCE_CLASS = "org.jajuk.services.dj.AmbienceDigitalDJ";

  String XML_DJ_EXTENSION = "dj";

  String XML_DJ_TRANSITION = "transition";

  String XML_DJ_TRANSITIONS = "transitions";

  String XML_DJ_FROM = "from";

  String XML_DJ_TO = "to";

  String XML_DJ_NUMBER = "number";

  String XML_SUBMISSION = "submission";

  String XML_STREAMS = "streams";

  String XML_STREAM = "stream";

  /*
   * Reserved XML tags for property names (note that a user can choose a
   * property name equals to meta information attributes names without pbm)
   */
  String[] XML_RESERVED_ATTRIBUTE_NAMES = { XML_NAME, XML_ID, XML_TYPE_EXTENSION,
      XML_TYPE_PLAYER_IMPL, XML_TYPE_TAG_IMPL, XML_TYPE_SEEK_SUPPORTED, XML_TYPE_ICON,
      XML_TYPE_IS_MUSIC, XML_TYPE, XML_URL, XML_DEVICE_AUTO_MOUNT, XML_DEVICE_AUTO_REFRESH,
      XML_EXPANDED, XML_DEVICE_MOUNT_POINT, XML_ALBUM, XML_AUTHOR, XML_STYLE, XML_TRACK_LENGTH,
      XML_YEAR, XML_TRACK_RATE, XML_FILES, XML_TRACK_HITS, XML_TRACK_DISCOVERY_DATE,
      XML_DIRECTORY_PARENT, XML_DEVICE, XML_DIRECTORY, XML_TRACK, XML_SIZE, XML_QUALITY,
      XML_PLAYLIST_FILES, XML_TRACK_COMMENT, XML_ANY, XML_TRACK_ORDER, XML_DEVICE_SYNCHRO_MODE,
      XML_DEVICE_SYNCHRO_SOURCE, XML_FILE_DATE, XML_TRACK_TOTAL_PLAYTIME, XML_TRACK_PREFERENCE,
      XML_TRACK_BANNED };

  String PROPERTY_SEPARATOR = "Property_";

  // Formats
  String FORMAT_STRING = "Property_Format_String";

  String FORMAT_NUMBER = "Property_Format_Number";

  String FORMAT_BOOLEAN = "Property_Format_Boolean";

  String FORMAT_FLOAT = "Property_Format_Float";

  String FORMAT_DATE = "Property_Format_Date";

  // Thumbs
  String THUMBNAIL_SIZE_50X50 = "50x50";

  String THUMBNAIL_SIZE_100X100 = "100x100";

  String THUMBNAIL_SIZE_150X150 = "150x150";

  String THUMBNAIL_SIZE_200X200 = "200x200";

  String THUMBNAIL_SIZE_250X250 = "250x250";

  String THUMBNAIL_SIZE_300X300 = "300x300";

  // Patterns
  String PATTERN_AUTHOR = "%artist";

  String PATTERN_ALBUM = "%album";

  String PATTERN_STYLE = "%genre";

  String PATTERN_YEAR = "%year";

  String PATTERN_TRACKNAME = "%title";

  String PATTERN_TRACKORDER = "%n";

  String PATTERN_DEFAULT_REORG = PATTERN_AUTHOR + "/" + PATTERN_YEAR + " - " + PATTERN_ALBUM + "/"
      + PATTERN_TRACKORDER + " - " + PATTERN_TRACKNAME;

  String PATTERN_DEFAULT_ANIMATION = PATTERN_AUTHOR + " / " + PATTERN_ALBUM + " / "
      + PATTERN_TRACKNAME;

  // Actions
  String ACTION_NEXT = "next";

  String ACTION_PREV = "prev";

  String ACTION_FINISH = "finish";

  String ACTION_CANCEL = "cancel";

  // Strings
  String FRAME_MAXIMIZED = "max";

  // Alarm Clock
  String ALARM_TIME_HOUR = "hour";

  String ALARM_TIME_MINUTES = "minutes";

  String ALARM_TIME_SECONDS = "seconds";

  String ALARM_MESSAGE = "alarm.message";

  // Alarm mode
  String ALARM_START_MODE = "Start Playing";

  String ALARM_STOP_MODE = "Stop Playing";

  String CONF_ALARM_ACTION = "jajuk.alarm.action";

  String CONF_ALARM_FILE = "jajuk.alarm.file";

  String CONF_ALARM_MODE = "jajuk.alarm.mode";

  String CONF_ALARM_DAILY = "jajuk.alarm.daily";

  String CONF_FORMAT_TIME_ELAPSED = "jajuk.format_elapsed_time";

  // Alarms List

  String ALARMS_SET = "jajuk.alarms";
  
    // Preferences
  long PREFERENCE_ADORE=3;
  long PREFERENCE_LOVE=2;
  long PREFERENCE_LIKE=1;
  long PREFERENCE_UNSET=0;
  long PREFERENCE_AVERAGE=-1;
  long PREFERENCE_POOR=-2;
  long PREFERENCE_HATE=-3;

}