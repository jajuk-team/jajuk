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
 *  
 */
package org.jajuk.util;

import java.net.URL;

/**
 * Contains all technical/ non-translatable strings Do not set static final to
 * these constants, it is implied by the fact you are in an interface.
 */
public interface Const {

  // Misc

  /** Maximum size for covers in KB. */
  int MAX_COVER_SIZE = 2048;

  /** Maximum number of remote covers. */
  int MAX_REMOTE_COVERS = 10;

  /** Special length for player meaning end of file. */
  long TO_THE_END = -1;

  /** Time we wait after an error in ms. */
  int WAIT_AFTER_ERROR = 2000;

  /** We need a fix size to have the same angle effect for every cover. */
  int MIRROW_COVER_SIZE = 600;

  /** Default playlist name. */
  String DEFAULT_PLAYLIST_FILE = "playlist";

  /** Max number of connection time out before stopping to look for covers. */
  int STOP_TO_SEARCH = 5;

  /** Number of lines in the feedback mail. */
  int FEEDBACK_LINES = 200;

  /** Max history size. */
  int MAX_HISTORY_SIZE = 1000;

  /** Autocommit delay in ms. */
  int AUTO_COMMIT_DELAY = 3600000;

  /** Autorefresh thread delay in ms. */
  int AUTO_REFRESH_DELAY = 30000;

  /** Default refresh interval in mins for unknown types devices *. */
  String DEFAULT_REFRESH_INTERVAL = "5";

  /** Fading status code. */
  int FADING_STATUS = 9999;

  /** Need full gc memory %. */
  float NEED_FULL_GC_LEVEL = 0.90f;

  /** Play time to get rate +1 in secs. */
  int INC_RATE_TIME = 20;

  /** Max number of concurrent executions by observation manager for a given event. */
  int MAX_EVENT_EXECUTIONS = 15;

  /** Min number of tracks in a DJ selection without track unicity. */
  int MIN_TRACKS_NUMBER_WITHOUT_UNICITY = 100;

  /** Default number of tracks in a transition. */
  int DEFAULT_TRANSITION_TRACK_NUMBER = 2;

  /** Rate Stars columns size in pixels. */
  int RATE_COLUMN_SIZE = 75;

  /** Play icon columns size in pixels. */
  int PLAY_COLUMN_SIZE = 20;

  /** Proportion of best tracks. */
  float BESTOF_PROPORTION = 0.05f;

  /** Number of milliseconds in a second. */
  int MILLISECONDS_IN_A_SECOND = 1000;

  /** Number of seconds in a minute. */
  int SECONDS_IN_A_MINUTE = 60;

  /** Number of minutes in an hour. */
  int MINUTES_IN_AN_HOUR = 60;

  /** Number of pixels around window at initial startup. */
  int FRAME_INITIAL_BORDER = 60;

  /** Maximum images cache size in MB. */
  int MAX_IMAGES_CACHE_SIZE = 100;

  /** Default webradio. */
  String DEFAULT_WEBRADIO = "Bluemars [Ambient/Space-Music]";

  /** Default hostame used in case of problem. */
  String DEFAULT_HOSTNAME = "localhost";

  /** Number of hours in a day. */
  int HOURS_IN_A_DAY = 24;

  /** Number of album bestof items. */
  int NB_BESTOF_ALBUMS = 25;

  /* Number of tracks for global actions (global shuffle, bestof...) */
  /** DOCUMENT_ME. */
  int NB_TRACKS_ON_ACTION = 200;

  /** Cache max age (in ms). */
  long CACHE_MAX_AGE = 10 * 24 * 3600 * 1000; // 10 days

  /** Default auto-refresh delay for directory-type devices (mins). */
  double DEFAULT_REFRESH_INTERVAL_DIRECTORY = 0.5d;

  /** Default auto-refresh delay for CD-type devices (mins). */
  double DEFAULT_REFRESH_INTERVAL_CD = 0.0d;

  /** Default auto-refresh delay for directory-type devices (mins). */
  double DEFAULT_REFRESH_INTERVAL_NETWORK_DRIVE = 0.0d;

  /** Default auto-refresh delay for directory-type devices (mins). */
  double DEFAULT_REFRESH_INTERVAL_EXTERNAL_DRIVE = 3.0d;

  /** Default auto-refresh delay for directory-type devices (mins). */
  double DEFAULT_REFRESH_INTERVAL_PLAYER = 3.0d;

  /** The time we wait for mplayer to start up in ms. It can take some time on slow or heavily loaded machines... */
  int MPLAYER_START_TIMEOUT = 15000;

  /** Number of milliseconds in a day. */
  int MILLISECONDS_IN_A_DAY = MILLISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTE * MINUTES_IN_AN_HOUR
      * HOURS_IN_A_DAY;

  /** Last elapsed time display mode format + 1 *. */
  int FORMAT_TIME_ELAPSED_MAX = 4;

  /** Mplayer windows exe size in bytes. */
  long MPLAYER_WINDOWS_EXE_SIZE = 15265280l;

  /** Mplayer windows exe size in bytes. */
  long MPLAYER_OSX_EXE_SIZE = 21490476l;

  /** Update URL = PAD file URL. */
  String CHECK_FOR_UPDATE_URL = "http://repository.jajuk.info/jajuk_pad.xml";

  // Jajuk version
  /** DOCUMENT_ME. */
  String JAJUK_VERSION = "VERSION_REPLACED_BY_ANT";

  /** DOCUMENT_ME. */
  String JAJUK_CODENAME = "Gute Laune";

  /** Extention to the .jajuk_test directory in test mode only */
  String TEST_VERSION = "1.10";

  /** DOCUMENT_ME. */
  String JAJUK_VERSION_DATE = "Build: DATE_REPLACED_BY_ANT";

  /** DOCUMENT_ME. */
  String JAJUK_COPYRIGHT = "Copyright 2003-2011 The Jajuk Team";

  /** DOCUMENT_ME. */
  String TYPE_VIDEO = "Type.video";

  // -- Files and paths --
  /** DOCUMENT_ME. */
  String FILE_COLLECTION = "collection.xml";

  // File written by the exit hook
  /** DOCUMENT_ME. */
  String FILE_COLLECTION_EXIT = "collection_exit.xml";

  // Void file created after exit collection file
  /** DOCUMENT_ME. */
  String FILE_COLLECTION_EXIT_PROOF = "exit_proof";

  /** DOCUMENT_ME. */
  String FILE_REPORTING_CACHE_FILE = "cache/report";

  /** Name of the preferences property file. */
  String FILE_CONFIGURATION = "conf.properties";

  /** Name of the temporary preferences property file. */
  String FILE_CONFIGURATION_TEMP = "temp_conf.properties";

  /** DOCUMENT_ME. */
  String FILE_HISTORY = "history.xml";

  /** Directory containing all sessions using this workspace. */
  String FILE_SESSIONS = "sessions";

  /** DOCUMENT_ME. */
  String FILE_DEFAULT_COVER = "jajuk.file.default.cover";

  /** DOCUMENT_ME. */
  String FILE_ABSOLUTE_DEFAULT_COVER = "jajuk-default-cover.";

  // langpack name : jajuk_<locale>.properties
  /** DOCUMENT_ME. */
  String FILE_LANGPACK_PART1 = "jajuk";

  /** DOCUMENT_ME. */
  String FILE_LANGPACK_PART2 = ".properties";

  // logs
  /** DOCUMENT_ME. */
  String FILE_LOGS = "jajuk.log";

  /** DOCUMENT_ME. */
  URL FILE_LOG4J_CONF = UtilSystem.getResource("org/jajuk/util/log/jajuk-log4j-conf.xml");

  /** Cache directory name. */
  String FILE_CACHE = "cache";

  /** Bat Converter to DOS 8.3 format */
  String FILE_FILENAME_CONVERTER = "converter.bat";

  /** DOCUMENT_ME. */
  String LASTFM_CACHE = "lastfm";

  /** Last.fm album cover cache dir. */
  String LAST_FM_ALBUM_COVER_CACHE_DIR = "album_covers";

  /** Last.fm album info cache dir. */
  String LAST_FM_ALBUM_INFO_CACHE_DIR = "album_info";

  /** Last.fm artist info cache dir. */
  String LAST_FM_ARTIST_INFO_CACHE_DIR = "artist_info";

  /** Last.fm artist image cache dir. */
  String LAST_FM_ARTIST_IMAGE_CACHE_DIR = "artist_images";

  /** Last.fm artist info cache dir. */
  String LAST_FM_ARTIST_SIMILAR_CACHE_DIR = "artist_similar";

  /** Last.fm artist thumb cache dir. */
  String LAST_FM_ARTIST_THUMB_CACHE_DIR = "artist_thumbs";

  /** Last.fm artist info cache dir. */
  String LAST_FM_ALBUM_LIST_CACHE_DIR = "album_list_info";

  /** Last.fm artist wiki cache dir. */
  String LAST_FM_ARTIST_WIKI_CACHE_DIR = "artist_wiki";

  /** Last.fm artist submission cache dir. */
  String LAST_FM_SUBMISSION_CACHE_DIR = "submission";

  /** DOCUMENT_ME. */
  String FILE_INTERNAL_CACHE = "internal";

  /** DOCUMENT_ME. */
  String FILE_THUMBS = "thumbs";

  /** DOCUMENT_ME. */
  String FILE_THUMB_NO_COVER = "nocover.png";

  /** DOCUMENT_ME. */
  String FILE_THUMB_NONE = "none.png";

  /** DOCUMENT_ME. */
  String FILE_DEFAULT_BESTOF_PLAYLIST = "bestof";

  /** DOCUMENT_ME. */
  String FILE_DEFAULT_NOVELTIES_PLAYLIST = "novelties";

  /** DOCUMENT_ME. */
  String FILE_DEFAULT_BOOKMARKS_PLAYLIST = "bookmarks";

  /** DOCUMENT_ME. */
  String FILE_DEFAULT_QUEUE_PLAYLIST = "jajuk-playlist_";

  /** DOCUMENT_ME. */
  String FILE_DJ_DIR = "djs";

  /** jajuk web radios repository. */
  String FILE_WEB_RADIOS_CUSTOM = "custom_radios.xml";

  /** Preset web radios file. */
  String FILE_WEB_RADIOS_PRESET = "preset_radios.xml";

  /** DOCUMENT_ME. */
  String FILE_FIFO = "fifo.lst";

  /** Bootstrap file name. */
  String FILE_BOOTSTRAP = ".jajuk_bootstrap.xml";

  /** Old (pre-1.9) bootstrap file name */
  String FILE_BOOTSTRAP_OLD = ".jajuk_bootstrap";

  /** DOCUMENT_ME. */
  String FILE_MPLAYER_WINDOWS_EXE = "mplayer.exe";

  /** DOCUMENT_ME. */
  String FILE_MPLAYER_OSX_EXE = "mplayer";

  /** DOCUMENT_ME. */
  String FILE_DEFAULT_PERSPECTIVES_PATH = "perspectives";

  /** DOCUMENT_ME. */
  String FILE_JAJUK_DOWNLOADED_FILES_SUFFIX = "_jajuk";

  /** DOCUMENT_ME. */
  String FILE_REPORTING_CSS_ALL_FILENAME = "report-all.css";

  /** DOCUMENT_ME. */
  URL FILE_REPORTING_CSS_ALL_PATH = UtilSystem.getResource("xslt/"
      + FILE_REPORTING_CSS_ALL_FILENAME);

  /** DOCUMENT_ME. */
  String FILE_REPORTING_CSS_PRINT_FILENAME = "report-print.css";

  /** DOCUMENT_ME. */
  URL FILE_REPORTING_CSS_PRINT_PATH = UtilSystem.getResource("xslt/"
      + FILE_REPORTING_CSS_PRINT_FILENAME);

  /** Mplayer Windows exe provided by jajuk URL . */
  String URL_MPLAYER_WINDOWS = "http://repository.jajuk.info/mplayer/20090114/mplayer.exe";

  /** Mplayer OSX exe provided by jajuk URL . */
  String URL_MPLAYER_OSX = "http://repository.jajuk.info/mplayer/osx/1.0rc2/mplayer";

  /** DOCUMENT_ME. */
  String URL_WEBRADIO_PRESETS = "http://svn2.assembla.com/svn/common-jukebox/common-jukebox/src/main/resources/preset_radios.xml";

  // sequence
  /** DOCUMENT_ME. */
  String AMBIENCE_PREFIX = "jajuk.ambience.";

  // Proxy type: http
  /** DOCUMENT_ME. */
  String PROXY_TYPE_HTTP = "http";

  // Proxy type: socks
  /** DOCUMENT_ME. */
  String PROXY_TYPE_SOCKS = "socks";

  // Unknown
  /** DOCUMENT_ME. */
  String UNKNOWN_ARTIST = "unknown_artist";

  /** DOCUMENT_ME. */
  String UNKNOWN_ALBUM = "unknown_album";

  /** DOCUMENT_ME. */
  String UNKNOWN_GENRE = "unknown_genre";

  /** DOCUMENT_ME. */
  String UNKNOWN_YEAR = "unknown_year";

  /** DOCUMENT_ME. */
  String VARIOUS_ARTIST = "various_artist";

  /** DOCUMENT_ME. */
  String COVER_NONE = "none";

  // images
  /** DOCUMENT_ME. */
  URL IMAGES_SPLASHSCREEN = UtilSystem.getResource("images/included/jajuk-splashscreen.jpg");

  /** DOCUMENT_ME. */
  URL IMAGE_SEARCH = UtilSystem.getResource("images/included/search.png");

  // XSLT Files
  /** DOCUMENT_ME. */
  URL XSLT_ARTIST = UtilSystem.getResource("xslt/artist.xsl");

  /** DOCUMENT_ME. */
  URL XSLT_ALBUM = UtilSystem.getResource("xslt/album.xsl");

  /** DOCUMENT_ME. */
  URL XSLT_GENRE = UtilSystem.getResource("xslt/genre.xsl");

  /** DOCUMENT_ME. */
  URL XSLT_YEAR = UtilSystem.getResource("xslt/genre.xsl");

  /** DOCUMENT_ME. */
  URL XSLT_DIRECTORY = UtilSystem.getResource("xslt/directory.xsl");

  /** DOCUMENT_ME. */
  URL XSLT_DEVICE = UtilSystem.getResource("xslt/device.xsl");

  /** DOCUMENT_ME. */
  URL XSLT_COLLECTION_LOGICAL = UtilSystem.getResource("xslt/logical_collection.xsl");

  /** DOCUMENT_ME. */
  String COLLECTION_LOGICAL = "logical";

  /** DOCUMENT_ME. */
  String COLLECTION_PHYSICAL = "physical";

  // -- Command line options --

  /** Tells jajuk it is inside the IDE. */
  String CLI_IDE = "ide";

  /** Tells jajuk to use a .jajuk_test repository */
  String CLI_TEST = "test";

  /** Special forced workspace location CLI option. */
  String CLI_WORKSPACE_LOCATION = "workspace";

  // players impls
  /** DOCUMENT_ME. */
  String PLAYER_IMPL_JAVALAYER = "org.jajuk.services.players.JavaLayerPlayerImpl";

  /** DOCUMENT_ME. */
  String PLAYER_IMPL_MPLAYER = "org.jajuk.services.players.MPlayerPlayerImpl";

  /** DOCUMENT_ME. */
  String PLAYER_IMPL_WEBRADIOS = "org.jajuk.services.players.WebRadioPlayerImpl";

  // tag impls
  /** DOCUMENT_ME. */
  String TAG_IMPL_NO_TAGS = "org.jajuk.services.tags.NoTagsTagImpl";

  /** DOCUMENT_ME. */
  String TAG_IMPL_JAUDIOTAGGER = "org.jajuk.services.tags.JAudioTaggerTagImpl";

  // device types

  // Devices sync mode
  /** DOCUMENT_ME. */
  String DEVICE_SYNCHRO_MODE_BI = "bi";

  /** DOCUMENT_ME. */
  String DEVICE_SYNCHRO_MODE_UNI = "uni";

  // extensions
  /** DOCUMENT_ME. */
  String EXT_MP3 = "mp3";

  /** DOCUMENT_ME. */
  String EXT_MP2 = "mp2";

  /** DOCUMENT_ME. */
  String EXT_PLAYLIST = "m3u";

  /** DOCUMENT_ME. */
  String EXT_OGG = "ogg";

  /** Ogg Vorbis Audio (see http://en.wikipedia.org/wiki/Ogg, some applications start using this now, e.g. Soundkonverter */
  String EXT_OGA = "oga";

  /** DOCUMENT_ME. */
  String EXT_WAV = "wav";

  /** DOCUMENT_ME. */
  String EXT_AU = "au";

  /** DOCUMENT_ME. */
  String EXT_AIF = "aif";

  /** DOCUMENT_ME. */
  String EXT_AIFF = "aiff";

  /** DOCUMENT_ME. */
  String EXT_FLAC = "flac";

  /** Extension of the thumbnails. */
  String EXT_THUMB = "png";

  /** DOCUMENT_ME. */
  String EXT_MPC = "mpc";

  /** DOCUMENT_ME. */
  String EXT_MPPLUS = "mp+";

  /** DOCUMENT_ME. */
  String EXT_MPP = "mpp";

  /** DOCUMENT_ME. */
  String EXT_WMA = "wma";

  /** DOCUMENT_ME. */
  String EXT_APE = "ape";

  /** DOCUMENT_ME. */
  String EXT_MAC = "mac";

  /** DOCUMENT_ME. */
  String EXT_M4A = "m4a";

  /** DOCUMENT_ME. */
  String EXT_REAL_RM = "rm";

  /** DOCUMENT_ME. */
  String EXT_REAL_RA = "ra";

  /** DOCUMENT_ME. */
  String EXT_RADIO = "radio";

  /** DOCUMENT_ME. */
  String EXT_AVI = "avi";

  /** DOCUMENT_ME. */
  String EXT_MPG = "mpg";

  /** DOCUMENT_ME. */
  String EXT_MPEG = "mpeg";

  /** DOCUMENT_ME. */
  String EXT_MKV = "mkv";

  /** DOCUMENT_ME. */
  String EXT_ASF = "asf";

  /** DOCUMENT_ME. */
  String EXT_WMV = "wmv";

  /** DOCUMENT_ME. */
  String EXT_MOV = "mov";

  /** DOCUMENT_ME. */
  String EXT_OGM = "ogm";

  /** DOCUMENT_ME. */
  String EXT_MP4 = "mp4";

  /** DOCUMENT_ME. */
  String EXT_WV = "wv";

  /** DOCUMENT_ME. */
  String EXT_FLV = "flv";

  // details keys
  /** DOCUMENT_ME. */
  String DETAIL_CURRENT_FILE_ID = "current file id";

  /** DOCUMENT_ME. */
  String DETAIL_CURRENT_FILE = "current file";

  /** DOCUMENT_ME. */
  String DETAIL_CURRENT_DATE = "current date";

  /** DOCUMENT_ME. */
  String DETAIL_SPECIAL_MODE_NORMAL = "norm";

  /** DOCUMENT_ME. */
  String DETAIL_SELECTION = "selection";

  /** DOCUMENT_ME. */
  String DETAIL_ORIGIN = "origin";

  /** Provides details on the perspective that thrown the event. */
  String DETAIL_PERSPECTIVE = "perspective";

  /** Provides details on the view that thrown the event. */
  String DETAIL_VIEW = "view";

  /** DOCUMENT_ME. */
  String DETAIL_REASON = "reason";

  /** DOCUMENT_ME. */
  String DETAIL_CONTENT = "content";

  /** DOCUMENT_ME. */
  String DETAIL_OLD = "old";

  /** DOCUMENT_ME. */
  String DETAIL_NEW = "new";

  // startup mode
  /** DOCUMENT_ME. */
  String STARTUP_MODE_NOTHING = "nothing";

  /** DOCUMENT_ME. */
  String STARTUP_MODE_SHUFFLE = "shuffle";

  /** DOCUMENT_ME. */
  String STARTUP_MODE_ITEM = "file";

  /** DOCUMENT_ME. */
  String STARTUP_MODE_LAST = "last";

  /** DOCUMENT_ME. */
  String STARTUP_MODE_LAST_KEEP_POS = "last_keep_pos";

  /** DOCUMENT_ME. */
  String STARTUP_MODE_BESTOF = "bestof";

  /** DOCUMENT_ME. */
  String STARTUP_MODE_NOVELTIES = "novelties";

  // --- Configuration keys ---

  /** DOCUMENT_ME. */
  String CONF_RELEASE = "jajuk.release";

  /** DOCUMENT_ME. */
  String CONF_PERSPECTIVE_DEFAULT = "jajuk.preference.perspective.default";

  /** DOCUMENT_ME. */
  String CONF_STATE_REPEAT = "jajuk.state.mode.repeat";

  /** DOCUMENT_ME. */
  String CONF_STATE_REPEAT_ALL = "jajuk.state.mode.repeat.all";

  /** DOCUMENT_ME. */
  String CONF_STATE_SHUFFLE = "jajuk.state.mode.shuffle";

  /** DOCUMENT_ME. */
  String CONF_STATE_KARAOKE = "jajuk.state.karaoke";

  /** DOCUMENT_ME. */
  String CONF_STATE_CONTINUE = "jajuk.state.mode.continue";

  /** DOCUMENT_ME. */
  String CONF_STATE_INTRO = "jajuk.state.mode.intro";

  /** DOCUMENT_ME. */
  String CONF_STARTUP_ITEM = "jajuk.startup.file";

  /** DOCUMENT_ME. */
  String CONF_STARTUP_MODE = "jajuk.startup.mode";

  /** Tell if last jajuk session was closed in stopped mode. */
  String CONF_STARTUP_STOPPED = "jajuk.startup.stopped";

  /** DOCUMENT_ME. */
  String CONF_CONFIRMATIONS_DELETE_FILE = "jajuk.confirmations.delete_file";

  /** DOCUMENT_ME. */
  String CONF_CONFIRMATIONS_EXIT = "jajuk.confirmations.exit";

  /** DOCUMENT_ME. */
  String CONF_CONFIRMATIONS_REMOVE_DEVICE = "jajuk.confirmations.remove_device";

  /** DOCUMENT_ME. */
  String CONF_CONFIRMATIONS_DELETE_COVER = "jajuk.confirmations.delete_cover";

  /** DOCUMENT_ME. */
  String CONF_CONFIRMATIONS_CLEAR_HISTORY = "jajuk.confirmations.clear_history";

  /** DOCUMENT_ME. */
  String CONF_CONFIRMATIONS_RESET_RATINGS = "jajuk.confirmations.reset_ratings";

  /** DOCUMENT_ME. */
  String CONF_CONFIRMATIONS_REFACTOR_FILES = "jajuk.confirmations.refactor_files";

  /** DOCUMENT_ME. */
  String CONF_OPTIONS_HIDE_UNMOUNTED = "jajuk.options.hide_unmounted";

  /** DOCUMENT_ME. */
  String CONF_OPTIONS_LOG_LEVEL = "jajuk.options.log_level";

  /** DOCUMENT_ME. */
  String CONF_OPTIONS_LANGUAGE = "jajuk.options.language";

  /** DOCUMENT_ME. */
  String CONF_OPTIONS_INTRO_BEGIN = "jajuk.options.intro.begin";

  /** DOCUMENT_ME. */
  String CONF_OPTIONS_INTRO_LENGTH = "jajuk.options.intro.length";

  /** DOCUMENT_ME. */
  String CONF_OPTIONS_LNF = "jajuk.options.lnf";

  /** DOCUMENT_ME. */
  String CONF_OPTIONS_NOVELTIES_AGE = "jajuk.options.novelties";

  // Look and Feel
  /** Substance default theme *. */
  String LNF_DEFAULT_THEME = "Nebula";

  /** Number of visible planned tracks. */
  String CONF_OPTIONS_VISIBLE_PLANNED = "jajuk.options.visible_planned";

  /** Default action (play or push) when clicking on an item. */
  String CONF_OPTIONS_PUSH_ON_CLICK = "jajuk.options.default_action_click";

  /** Default action (play or push) when dropping on an item. */
  String CONF_OPTIONS_PUSH_ON_DROP = "jajuk.options.default_action_drop";

  /** Table / tree sync option prefix. */
  String CONF_SYNC_TABLE_TREE = "jajuk.sync_table_tree";

  /** Notificator type. */
  String CONF_UI_NOTIFICATOR_TYPE = "jajuk.options.notificator_type";

  /** DOCUMENT_ME. */
  String CONF_P2P_SHARE = "jajuk.options.p2p.share";

  /** DOCUMENT_ME. */
  String CONF_P2P_ADD_REMOTE_PROPERTIES = "jajuk.options.p2p.add_remote_properties";

  /** DOCUMENT_ME. */
  String CONF_P2P_HIDE_LOCAL_PROPERTIES = "jajuk.options.p2p.hide_local_properties";

  /** DOCUMENT_ME. */
  String CONF_P2P_PASSWORD = "jajuk.options.p2p.password";

  /** DOCUMENT_ME. */
  String CONF_HISTORY = "jajuk.options.history";

  /** DOCUMENT_ME. */
  String CONF_TAGS_USE_PARENT_DIR = "jajuk.tags.use_parent_dir";

  /** DOCUMENT_ME. */
  String CONF_DROP_PLAYED_TRACKS_FROM_QUEUE = "jajuk.drop.played.tracks.from.queue";

  /** Contains files id separated by a colon. */
  String CONF_BOOKMARKS = "jajuk.bookmarks";

  /** Startup display mode: 0= main window + tray, 1: tray only, 2: slimbar + tray 3 : full screen. */
  String CONF_STARTUP_DISPLAY = "jajuk.startup_display_mode";

  /** DOCUMENT_ME. */
  int DISPLAY_MODE_MAIN_WINDOW = 0;

  /** DOCUMENT_ME. */
  int DISPLAY_MODE_TRAY = 1;

  /** DOCUMENT_ME. */
  int DISPLAY_MODE_SLIMBAR_TRAY = 2;

  /** DOCUMENT_ME. */
  int DISPLAY_MODE_FULLSCREEN = 3;

  /** Catalog view cover mode. */
  int CATALOG_VIEW_COVER_MODE_ALL = 0;

  /** DOCUMENT_ME. */
  int CATALOG_VIEW_COVER_MODE_WITH = 1;

  /** DOCUMENT_ME. */
  int CATALOG_VIEW_COVER_MODE_WITHOUT = 2;

  /** Best of size. */
  String CONF_BESTOF_TRACKS_SIZE = "jajuk.bestof_size";

  /** Slimbar position. */
  String CONF_SLIMBAR_POSITION = "jajuk.slimbar_pos";

  /** DOCUMENT_ME. */
  String CONF_SLIMBAR_DISPLAY_QUEUE = "jajuk.slimbar_display_queue";

  /** Gain (float). */
  String CONF_VOLUME = "jajuk.volume";

  /** Use regular expressions ?. */
  String CONF_REGEXP = "jajuk.regexp";

  /** Force mplayer to use short names *. */
  String CONF_SHORT_NAMES = "jajuk.short_names";

  /** Collection backup size in MB. */
  String CONF_BACKUP_SIZE = "jajuk.backup_size";

  /** Collection file charset (utf-8 or utf-16). */
  String CONF_COLLECTION_CHARSET = "jajuk.collection_charset";

  /** DOCUMENT_ME. */
  String CONF_STARTUP_LAST_POSITION = "jajuk.startup.last_position";

  /** DOCUMENT_ME. */
  String CONF_NETWORK_USE_PROXY = "jajuk.network.use_proxy";

  /** DOCUMENT_ME. */
  String CONF_NETWORK_PROXY_TYPE = "jajuk.network.proxy_type";

  /** DOCUMENT_ME. */
  String CONF_NETWORK_PROXY_HOSTNAME = "jajuk.network.proxy_hostname";

  /** DOCUMENT_ME. */
  String CONF_NETWORK_PROXY_PORT = "jajuk.network.proxy_port";

  /** DOCUMENT_ME. */
  String CONF_NETWORK_PROXY_LOGIN = "jajuk.network.proxy_login";

  /** DOCUMENT_ME. */
  String CONF_NETWORK_PROXY_PWD = "jajuk.network.proxy_pwd";

  /** DOCUMENT_ME. */
  String CONF_LASTFM_AUDIOSCROBBLER_ENABLE = "jajuk.network.audioscrobbler";

  /** DOCUMENT_ME. */
  String CONF_LASTFM_USER = "jajuk.network.ASUser";

  /** DOCUMENT_ME. */
  String CONF_LASTFM_PASSWORD = "jajuk.network.ASPassword";

  /** DOCUMENT_ME. */
  String CONF_COVERS_AUTO_COVER = "jajuk.covers.auto_cover";

  /** DOCUMENT_ME. */
  String CONF_COVERS_MIRROW_COVER = "jajuk.covers.mirrow_cover";

  /** DOCUMENT_ME. */
  String CONF_COVERS_MIRROW_COVER_FS_MODE = "jajuk.covers.mirrow_cover_fs_mode";

  /** DOCUMENT_ME. */
  String CONF_COVERS_SHUFFLE = "jajuk.covers.shuffle";

  /** DOCUMENT_ME. */
  String CONF_COVERS_SAVE_EXPLORER_FRIENDLY = "jajuk.covers.save.explorer.friendly";

  /** DOCUMENT_ME. */
  String CONF_COVERS_SIZE = "jajuk.covers.size";

  /** DOCUMENT_ME. */
  String CONF_COVERS_ACCURACY = "jajuk.covers.accuracy";

  /** DOCUMENT_ME. */
  String CONF_NETWORK_CONNECTION_TO = "jajuk.network.connection_timeout";

  /** Last Option selected tab. */
  String CONF_OPTIONS_TAB = "jajuk.options.tab";

  /** Data buffer size in bytes. */
  String CONF_BUFFER_SIZE = "jajuk.buffer_size";

  /** Audio buffer size in bytes. */
  String CONF_AUDIO_BUFFER_SIZE = "jajuk.audio_buffer_size";

  /** Window position and size. */
  String CONF_WINDOW_POSITION = "jajuk.window_position";

  /** is Window maximized. */
  String CONF_WINDOW_MAXIMIZED = "jajuk.window_MAXIMIZED";

  /** Window position and size. */
  String CONF_PATTERN_FRAME_TITLE = "jajuk.frame.title";

  /** Refactoring pattern*. */
  String CONF_PATTERN_REFACTOR = "jajuk.refactor_pattern";

  /** Animation pattern*. */
  String CONF_PATTERN_ANIMATION = "jajuk.animation_pattern";

  /** Balloon notifier pattern. */
  String CONF_PATTERN_BALLOON_NOTIFIER = "jajuk.pattern.balloon_pattern";

  /** Information view pattern. */
  String CONF_PATTERN_INFORMATION = "jajuk.pattern.information_pattern";

  /** Files table columns. */
  String CONF_FILES_TABLE_COLUMNS = "jajuk.ui.physical_table_columns";

  /** Files table edition state. */
  String CONF_FILES_TABLE_EDITION = "jajuk.ui.physical_table_edition";

  /** Tracks table columns. */
  String CONF_TRACKS_TABLE_COLUMNS = "jajuk.ui.logical_table_columns";

  /** Tracks table edition state. */
  String CONF_TRACKS_TABLE_EDITION = "jajuk.ui.logical_table_edition";

  /** Albums table edition state. */
  String CONF_ALBUMS_TABLE_COLUMNS = "jajuk.ui.albums_table_columns";

  /** Albums table edition state. */
  String CONF_ALBUMS_TABLE_EDITION = "jajuk.ui.albums_table_edition";

  /** Webradio table edition state. */
  String CONF_WEBRADIO_TABLE_EDITION = "jajuk.ui.webradio_table_edition";

  /** Playlist editor columns to display. */
  String CONF_PLAYLIST_EDITOR_COLUMNS = "jajuk.ui.playlist_editor_columns";

  /** Playlist repository columns to display. */
  String CONF_PLAYLIST_REPOSITORY_COLUMNS = "jajuk.ui.playlist_repository_columns";

  /** Queue columns to display. */
  String CONF_QUEUE_COLUMNS = "jajuk.ui.queue_columns";
  
   /** Web radios view columns to display. */
  String CONF_WEBRADIO_COLUMNS = "jajuk.ui.webradio_columns";

  /** Catalog items size. */
  String CONF_THUMBS_SIZE = "jajuk.ui.cover_catalog.thumbs_size";

  /** Catalog items cover filter. */
  String CONF_THUMBS_SHOW_COVER = "jajuk.ui.cover_catalog.show_cover";

  /** Catalog sorter*. */
  String CONF_THUMBS_SORTER = "jajuk.catalog.sorter";

  /** Catalog filter*. */
  String CONF_THUMBS_FILTER = "jajuk.catalog.filter";

  /** Display tips on startup. */
  String CONF_SHOW_TIP_ON_STARTUP = "jajuk.tip.show_on_startup";

  /** Wikipedia language*. */
  String CONF_WIKIPEDIA_LANGUAGE = "jajuk.wikipedia.lang";

  /** Cross fade duration in secs. */
  String CONF_FADE_DURATION = "jajuk.fade_duration";

  /** Logical tree sort order. */
  String CONF_LOGICAL_TREE_SORT_ORDER = "jajuk.logical_tree_sort_order";

  /** Default dj. */
  String CONF_DEFAULT_DJ = "jajuk.default_dj";

  /** Default web radio. */
  String CONF_DEFAULT_WEB_RADIO = "jajuk.default_web_radio";

  /** List of ignored versions during update check. */
  String CONF_IGNORED_RELEASES = "jajuk.update.ignored_releases";

  /** Check for update property. */
  String CONF_CHECK_FOR_UPDATE = "jajuk.update.check_for_updates";

  /** Default ambience*. */
  String CONF_DEFAULT_AMBIENCE = "jajuk.default_ambience";

  /* Wrong player not show again flag */
  /** DOCUMENT_ME. */
  String CONF_NOT_SHOW_AGAIN_PLAYER = "jajuk.not_show_again.player";

  /** Concurrent session not show again flag. */
  String CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION = "jajuk.not_show_again.concurrent_session";

  /** Cross fade alert not show again flag *. */
  String CONF_NOT_SHOW_AGAIN_CROSS_FADE = "jajuk.not_show_again.fade";

  /** Laf change not show again flag *. */
  String CONF_NOT_SHOW_AGAIN_LAF_CHANGE = "jajuk.not_show_laf_change";

  /** Global random mode: song or album level ? *. */
  String CONF_GLOBAL_RANDOM_MODE = "jajuk.global_random.mode";

  /** Novelties random mode: song or album level ?*. */
  String CONF_NOVELTIES_MODE = "jajuk.global_novelties.mode";

  /** Initial frame size/position forced value *. */
  String CONF_FRAME_POS_FORCED = "jajuk.frame.forced_position";

  // Forced mplayer path
  /** DOCUMENT_ME. */
  String CONF_MPLAYER_PATH_FORCED = "jajuk.mplayer.forced_path";

  /** Hotkeys flag *. */
  String CONF_OPTIONS_HOTKEYS = "jajuk.options.use_hotkeys";

  /** MPLayer additional arguments *. */
  String CONF_MPLAYER_ARGS = "jajuk.player.mplayer_args";

  /** MPlayer additional environment variables *. */
  String CONF_ENV_VARIABLES = "jajuk.player.env_variables";

  /** Max Number of thumbs displayed at the same time in catalog view. */
  String CONF_CATALOG_PAGE_SIZE = "jajuk.catalog.pages_size";

  /** Show Catalog popups. */
  String CONF_SHOW_POPUPS = "jajuk.show_popups";

  /** Show systray. */
  String CONF_SHOW_SYSTRAY = "jajuk.show_systray";

  /** Enable Title view Animation. */
  String CONF_TITLE_ANIMATION = "jajuk.title_animation";

  /** Minimize to tray. */
  String CONF_MINIMIZE_TO_TRAY = "jajuk.minimize_to_tray";

  /** Enable Last.FM information queries */
  String CONF_LASTFM_INFO = "jajuk.lastfm_information";

  /** Webradio playing at jajuk stop ?. */
  String CONF_WEBRADIO_WAS_PLAYING = "jajuk.webradio.was_playing";

  /** Font size. */
  String CONF_FONTS_SIZE = "jajuk.fonts_size";

  /** Increase rate value. */
  String CONF_INC_RATING = "jajuk.inc_rating.step";

  /** Use file date as discovery date option. */
  String CONF_FORCE_FILE_DATE = "jajuk.force_file_date";

  /** Perspective chooser icon size: 16x16, 32x32 or 40x40. */
  String CONF_PERSPECTIVE_ICONS_SIZE = "jajuk.ui.perspective_icons_size";

  /** Show duplicate playlists in playlist view. */
  String CONF_SHOW_DUPLICATE_PLAYLISTS = "jajuk.ui.show_duplicate_playlists";

  /** Smart mode selected in slimbar. */
  String CONF_SLIMBAR_SMART_MODE = "jajuk.ui.slimbar.smart_mode";

  /** volnorm option. */
  String CONF_USE_VOLNORM = "jajuk.mplayer.volnorm";

  /** None internet access switch. */
  String CONF_NETWORK_NONE_INTERNET_ACCESS = "jajuk.network.none_internet_access";

  /** Remembered directory for parties. */
  String CONF_PREPARE_PARTY = "jajuk.prepare_party.";
  
  /** Refreshing delay. */
  String CONF_REFRESHING_DELAY_MS = "jajuk.refreshing_delay_ms";

  /** Slashscreen flag. */
  String CONF_SPLASH_SCREEN = "jajuk.splashscreen";

  /** Shuffle/novelties mode. */
  String MODE_ALBUM = "album";

  /** DOCUMENT_ME. */
  String MODE_TRACK = "track";

  /** DOCUMENT_ME. */
  String MODE_ALBUM2 = "album2";

  // miscelanous
  /** DOCUMENT_ME. */
  String TRUE = "true";

  /** DOCUMENT_ME. */
  String FALSE = "false";

  // Date format
  /** DOCUMENT_ME. */
  String DATE_FILE = "yyyyMMdd";

  /** DOCUMENT_ME. */
  String ADDITION_DATE_FORMAT = "yyyyMMdd";

  // Playlists
  /** DOCUMENT_ME. */
  String PLAYLIST_NOTE = "#Playlist generated by Jajuk " + Const.JAJUK_VERSION;

  // XML tags
  /** DOCUMENT_ME. */
  String XML_COLLECTION = "collection";
  
  /** Webradio file XML tags static strings*/
  String XML_RADIO = "Radio";

  /** DOCUMENT_ME. */
  String XML_VERSION = "jajuk_version";

  /** DOCUMENT_ME. */
  String XML_TYPES = "types";

  /** DOCUMENT_ME. */
  String XML_TYPE = "type";

  /** DOCUMENT_ME. */
  String XML_DEVICES = "devices";

  /** DOCUMENT_ME. */
  String XML_DEVICE = "device";

  /** DOCUMENT_ME. */
  String XML_GENRES = "styles";

  /** DOCUMENT_ME. */
  String XML_GENRE = "style";

  /** DOCUMENT_ME. */
  String XML_ARTISTS = "authors";

  /** DOCUMENT_ME. */
  String XML_ALBUM_ARTISTS = "album-artists";

  /** DOCUMENT_ME. */
  String XML_ARTIST = "author";

  /** DOCUMENT_ME. */
  String XML_ALBUMS = "albums";

  /** DOCUMENT_ME. */
  String XML_ALBUM = "album";

  /** DOCUMENT_ME. */
  String XML_ALBUM_ARTIST = "album_artist";

  /** DOCUMENT_ME. */
  String XML_ALBUM_DISC_ID = "disc_id";

  /** DOCUMENT_ME. */
  String XML_TRACKS = "tracks";

  /** DOCUMENT_ME. */
  String XML_TRACK = "track";

  /** DOCUMENT_ME. */
  String XML_DIRECTORIES = "directories";

  /** DOCUMENT_ME. */
  String XML_DIRECTORY = "directory";

  /** DOCUMENT_ME. */
  String XML_FILES = "files";

  /** DOCUMENT_ME. */
  String XML_FILE = "file";

  /** DOCUMENT_ME. */
  String XML_PLAYLIST_FILES = "playlist_files";

  /** DOCUMENT_ME. */
  String XML_PLAYLIST_FILE = "playlist_file";

  /** DOCUMENT_ME. */
  String XML_PLAYLISTS = "playlists";

  /** DOCUMENT_ME. */
  String XML_ID = "id";

  /** DOCUMENT_ME. */
  String XML_YEAR = "year";

  /** DOCUMENT_ME. */
  String XML_YEARS = "years";

  /** DOCUMENT_ME. */
  String XML_PLAY = "play";

  /** DOCUMENT_ME. */
  String XML_FILE_DATE = "date";

  /** DOCUMENT_ME. */
  String XML_TRACK_NAME = "name";

  /** DOCUMENT_ME. */
  String XML_TRACK_ALBUM = "album";

  /** DOCUMENT_ME. */
  String XML_TRACK_GENRE = "style";

  /** DOCUMENT_ME. */
  String XML_TRACK_ARTIST = "author";

  /** DOCUMENT_ME. */
  String XML_TRACK_YEAR = "year";

  /** DOCUMENT_ME. */
  String XML_TRACK_LENGTH = "length";

  /** DOCUMENT_ME. */
  String XML_TRACK_TYPE = "type";

  /** DOCUMENT_ME. */
  String XML_TRACK_RATE = "rate";

  /** DOCUMENT_ME. */
  String XML_TRACK_HITS = "hits";

  /** DOCUMENT_ME. */
  String XML_TRACK_DISCOVERY_DATE = "added";

  /** DOCUMENT_ME. */
  String XML_TRACK_ORDER = "order";

  /** DOCUMENT_ME. */
  String XML_TRACK_DISC_NUMBER = "disc_number";

  /** DOCUMENT_ME. */
  String XML_TRACK_PREFERENCE = "pf";

  /** DOCUMENT_ME. */
  String XML_TRACK_TOTAL_PLAYTIME = "tpt";

  /** DOCUMENT_ME. */
  String XML_TRACK_BANNED = "ban";

  /** DOCUMENT_ME. */
  String XML_PLAYLIST = "playlist";

  /** DOCUMENT_ME. */
  String XML_NAME = "name";

  /** DOCUMENT_ME. */
  String XML_PATH = "path";

  /** DOCUMENT_ME. */
  String XML_URL = "url";

  /** DOCUMENT_ME. */
  String XML_ORIGIN = "origin";

  /** Keywords, separated by a ';'*/
  String XML_KEYWORDS = "keywords";

  /** Label*/
  String XML_DESC = "label";

  /** Bitrate */
  String XML_BITRATE = "bitrate";

  /** Frequency */
  String XML_FREQUENCY = "frequency";

  /** DOCUMENT_ME. */
  String XML_QUALITY = "quality";

  /** DOCUMENT_ME. */
  String XML_SIZE = "size";

  /** DOCUMENT_ME. */
  String XML_DEVICE_MOUNT_POINT = "mount_point";

  /** DOCUMENT_ME. */
  String XML_DEVICE_AUTO_REFRESH = "auto_refresh";

  /** DOCUMENT_ME. */
  String XML_DEVICE_AUTO_MOUNT = "auto_mount";

  /** DOCUMENT_ME. */
  String XML_DEVICE_SYNCHRO_SOURCE = "synchro_source";

  /** DOCUMENT_ME. */
  String XML_DEVICE_SYNCHRO_MODE = "synchro_mode";

  /** DOCUMENT_ME. */
  String XML_EXPANDED = "exp"; // can be 'y' or 'n'

  /** Cached cover. */
  String XML_ALBUM_DISCOVERED_COVER = "cover_cache";

  /** Selected cover. */
  String XML_ALBUM_SELECTED_COVER = "cover";

  /** DOCUMENT_ME. */
  String XML_DIRECTORY_PARENT = "parent";

  /** DOCUMENT_ME. */
  String XML_DIRECTORY_SYNCHRONIZED = "sync";

  /** DOCUMENT_ME. */
  String XML_TYPE_EXTENSION = "extension";

  /** DOCUMENT_ME. */
  String XML_TYPE_PLAYER_IMPL = "player_impl";

  /** DOCUMENT_ME. */
  String XML_TYPE_TAG_IMPL = "tag_impl";

  /** DOCUMENT_ME. */
  String XML_TYPE_IS_MUSIC = "music";

  /** DOCUMENT_ME. */
  String XML_TYPE_SEEK_SUPPORTED = "seek";

  // icon used in the physical tree
  /** DOCUMENT_ME. */
  String XML_TYPE_ICON = "icon";

  /** comment tag. */
  String XML_TRACK_COMMENT = "comment";

  /** "any" criteria. */
  String XML_ANY = "any";

  // constructor property flag
  /** DOCUMENT_ME. */
  String XML_CONSTRUCTOR = "constructor";

  // property should be displayed ?
  /** DOCUMENT_ME. */
  String XML_VISIBLE = "visible";

  // property editable ?
  /** DOCUMENT_ME. */
  String XML_EDITABLE = "editable";

  // Property unique ?
  /** DOCUMENT_ME. */
  String XML_UNIQUE = "unique";

  // custom property flag
  /** DOCUMENT_ME. */
  String XML_CUSTOM = "custom";

  // Property
  /** DOCUMENT_ME. */
  String XML_PROPERTY = "property";

  // default value
  /** DOCUMENT_ME. */
  String XML_DEFAULT_VALUE = "default_value";

  // general dj tag
  /** DOCUMENT_ME. */
  String XML_DJ_DJ = "dj";

  // general parameters
  /** DOCUMENT_ME. */
  String XML_DJ_GENERAL = "general_parameters";

  /** DOCUMENT_ME. */
  String XML_DJ_RATING_LEVEL = "rating_level";

  /** DOCUMENT_ME. */
  String XML_DJ_UNICITY = "unicity";

  /** DOCUMENT_ME. */
  String XML_DJ_FADE_DURATION = "fade_duration";

  /** DOCUMENT_ME. */
  String XML_DJ_MAX_TRACKS = "max_tracks";

  /** DOCUMENT_ME. */
  String XML_DJ_PROPORTIONS = "proportions";

  /** DOCUMENT_ME. */
  String XML_DJ_PROPORTION = "proportion";

  /** DOCUMENT_ME. */
  String XML_DJ_AMBIENCE = "ambience";

  /** DOCUMENT_ME. */
  String XML_DJ_GENRES = "styles";

  /** DOCUMENT_ME. */
  String XML_DJ_VALUE = "values";

  /** DOCUMENT_ME. */
  String XML_DJ_PROPORTION_CLASS = "org.jajuk.services.dj.ProportionDigitalDJ";

  /** DOCUMENT_ME. */
  String XML_DJ_TRANSITION_CLASS = "org.jajuk.services.dj.TransitionDigitalDJ";

  /** DOCUMENT_ME. */
  String XML_DJ_AMBIENCE_CLASS = "org.jajuk.services.dj.AmbienceDigitalDJ";

  /** DOCUMENT_ME. */
  String XML_DJ_EXTENSION = "dj";

  /** DOCUMENT_ME. */
  String XML_DJ_TRANSITION = "transition";

  /** DOCUMENT_ME. */
  String XML_DJ_TRANSITIONS = "transitions";

  /** DOCUMENT_ME. */
  String XML_DJ_FROM = "from";

  /** DOCUMENT_ME. */
  String XML_DJ_TO = "to";

  /** DOCUMENT_ME. */
  String XML_DJ_NUMBER = "number";

  /** DOCUMENT_ME. */
  String XML_STREAMS = "streams";

  /** DOCUMENT_ME. */
  String XML_STREAM = "stream";

  /*
   * Reserved XML tags for property names (note that a user can choose a property name equals to
   * meta information attributes names without pbm)
   */
  /** DOCUMENT_ME. */
  String[] XML_RESERVED_ATTRIBUTE_NAMES = { XML_NAME, XML_ID, XML_TYPE_EXTENSION,
      XML_TYPE_PLAYER_IMPL, XML_TYPE_TAG_IMPL, XML_TYPE_SEEK_SUPPORTED, XML_TYPE_ICON,
      XML_TYPE_IS_MUSIC, XML_TYPE, XML_URL, XML_DEVICE_AUTO_MOUNT, XML_DEVICE_AUTO_REFRESH,
      XML_EXPANDED, XML_DEVICE_MOUNT_POINT, XML_ALBUM, XML_ARTIST, XML_GENRE, XML_TRACK_LENGTH,
      XML_YEAR, XML_TRACK_RATE, XML_FILES, XML_TRACK_HITS, XML_TRACK_DISCOVERY_DATE,
      XML_DIRECTORY_PARENT, XML_DEVICE, XML_DIRECTORY, XML_TRACK, XML_SIZE, XML_QUALITY,
      XML_PLAYLIST_FILES, XML_TRACK_COMMENT, XML_ANY, XML_TRACK_ORDER, XML_DEVICE_SYNCHRO_MODE,
      XML_DEVICE_SYNCHRO_SOURCE, XML_FILE_DATE, XML_TRACK_TOTAL_PLAYTIME, XML_TRACK_PREFERENCE,
      XML_TRACK_BANNED, XML_TRACK_DISC_NUMBER, XML_ALBUM_ARTIST, XML_ALBUM_DISC_ID };

  /** Option name. */
  String PROPERTY_SEPARATOR = "Property_";

  // Formats
  /** Option name. */
  String FORMAT_STRING = "Property_Format_String";

  /** Option name. */
  String FORMAT_NUMBER = "Property_Format_Number";

  /** Option name. */
  String FORMAT_BOOLEAN = "Property_Format_Boolean";

  /** Option name.. */
  String FORMAT_FLOAT = "Property_Format_Float";

  /** Option name.. */
  String FORMAT_DATE = "Property_Format_Date";

  // Thumbs
  /** DOCUMENT_ME. */
  String THUMBNAIL_SIZE_50X50 = "50x50";

  /** DOCUMENT_ME. */
  String THUMBNAIL_SIZE_100X100 = "100x100";

  /** DOCUMENT_ME. */
  String THUMBNAIL_SIZE_150X150 = "150x150";

  /** DOCUMENT_ME. */
  String THUMBNAIL_SIZE_200X200 = "200x200";

  /** DOCUMENT_ME. */
  String THUMBNAIL_SIZE_250X250 = "250x250";

  /** DOCUMENT_ME. */
  String THUMBNAIL_SIZE_300X300 = "300x300";

  // Patterns
  /** DOCUMENT_ME. */
  String PATTERN_ARTIST = "%artist";

  /** DOCUMENT_ME. */
  String PATTERN_ALBUM_ARTIST = "%album_artist";

  /** DOCUMENT_ME. */
  String PATTERN_DISC = "%disc";

  /** DOCUMENT_ME. */
  String PATTERN_ALBUM = "%album";

  /** DOCUMENT_ME. */
  String PATTERN_GENRE = "%genre";

  /** DOCUMENT_ME. */
  String PATTERN_YEAR = "%year";

  /** DOCUMENT_ME. */
  String PATTERN_TRACKNAME = "%title";

  /** DOCUMENT_ME. */
  String PATTERN_TRACKORDER = "%n";

  /** DOCUMENT_ME. */
  String PATTERN_DEFAULT_REORG = PATTERN_ALBUM_ARTIST + "/" + PATTERN_YEAR + " - " + PATTERN_ALBUM
      + "/" + PATTERN_TRACKORDER + " - " + PATTERN_ARTIST + " - " + PATTERN_TRACKNAME;

  /** DOCUMENT_ME. */
  String PATTERN_DEFAULT_ANIMATION = PATTERN_TRACKNAME + " (" + PATTERN_ARTIST + ")";

  // Strings
  /** DOCUMENT_ME. */
  String FRAME_MAXIMIZED = "max";

  // Alarm Clock
  /** DOCUMENT_ME. */
  String CONF_ALARM_TIME_HOUR = "jajuk.alarm.hour";

  /** DOCUMENT_ME. */
  String CONF_ALARM_TIME_MINUTES = "jajuk.alarm.minutes";

  /** DOCUMENT_ME. */
  String CONF_ALARM_TIME_SECONDS = "jajuk.alarm.seconds";

  /** DOCUMENT_ME. */
  String CONF_ALARM_FILE = "jajuk.alarm.file";

  /** DOCUMENT_ME. */
  String CONF_ALARM_MODE = "jajuk.alarm.mode";

  /** DOCUMENT_ME. */
  String CONF_FORMAT_TIME_ELAPSED = "jajuk.format_elapsed_time";

  /** DOCUMENT_ME. */
  String CONF_ALARM_ACTION = "jajuk.alarm.action";

  /** DOCUMENT_ME. */
  String CONF_ALARM_ENABLED = "jajuk.alarm.enabled";

  /** Option name. */
  String CONF_AUTO_SCROLL = "jajuk.auto_scroll";

  /** Option name. */
  String CONF_EXPLORER_PATH = "jajuk.explorer_path";

  /** Option name. */
  String CONF_BIT_PERFECT = "jajuk.bit_perfect";
  
  String CONF_SHOW_VIDEOS = "jajuk.show_videos";

  // Alarm actions
  /** DOCUMENT_ME. */
  String ALARM_START_ACTION = "Start Playing";

  /** DOCUMENT_ME. */
  String ALARM_STOP_ACTION = "Stop Playing";

  // Preferences
  /** DOCUMENT_ME. */
  long PREFERENCE_ADORE = 3;

  /** DOCUMENT_ME. */
  long PREFERENCE_LOVE = 2;

  /** DOCUMENT_ME. */
  long PREFERENCE_LIKE = 1;

  /** DOCUMENT_ME. */
  long PREFERENCE_UNSET = 0;

  /** DOCUMENT_ME. */
  long PREFERENCE_AVERAGE = -1;

  /** DOCUMENT_ME. */
  long PREFERENCE_POOR = -2;

  /** DOCUMENT_ME. */
  long PREFERENCE_HATE = -3;

  /** DOCUMENT_ME. */
  String B_P_HTML = "</b></p></html>";

  /** DOCUMENT_ME. */
  String P_B = "<p><b>";

  /** DOCUMENT_ME. */
  String HTML = "<html>";

  /** File name where the tag cover is stored in. */
  String TAG_COVER_FILE = "tag_cover.png";

}
