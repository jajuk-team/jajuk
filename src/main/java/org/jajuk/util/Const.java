/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
  long MPLAYER_WINDOWS_EXE_SIZE = 21718016l;
  /** Mplayer windows exe size in bytes. */
  long MPLAYER_OSX_EXE_SIZE = 21490476l;
  /** Update URL = PAD file URL. */
  String CHECK_FOR_UPDATE_URL = "http://jajuk.info/repository/pad/jajuk_pad.xml";
  // Jajuk version
  String JAJUK_VERSION = "@VERSION_REPLACED_BY_ANT@";
  String JAJUK_CODENAME = "TBD";
  /** Extention to the .jajuk_test directory in test mode only */
  String TEST_VERSION = "12dev";
  String JAJUK_VERSION_DATE = "Build: @DATE_REPLACED_BY_ANT@";
  String JAJUK_COPYRIGHT = "Copyright 2003-2019 The Jajuk Team";
  // -- Files and paths --
  String FILE_SAVING_FILE_EXTENSION = "saving";
  String FILE_SAVED_PROOF_FILE_EXTENSION = "proof";
  String FILE_COLLECTION = "collection.xml";
  String FILE_REPORTING_CACHE_FILE = "cache/report";
  /** The Constant XML_EXT.   */
  String FILE_XML_EXT = ".xml";
  /** Name of the preferences property file. */
  String FILE_CONFIGURATION = "conf.properties";
  String FILE_HISTORY = "history.xml";
  String FILE_DEFAULT_COVER = "jajuk.file.default.cover";
  String FILE_ABSOLUTE_DEFAULT_COVER = "default-cover.";
  // langpack name : jajuk_<locale>.properties
  String FILE_LANGPACK_PART1 = "jajuk";
  String FILE_LANGPACK_PART2 = ".properties";
  // logs
  String FILE_LOGS = "jajuk.log";
  String FILE_PLAYING_POSITION = "position";
  URL FILE_LOG4J_CONF = UtilSystem.getResource("jajuk-log4j-conf.xml");
  /** Cache directory name. */
  String FILE_CACHE = "cache";
  /** Bat Converter to DOS 8.3 format */
  String FILE_FILENAME_CONVERTER = "converter.bat";
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
  String FILE_INTERNAL_CACHE = "internal";
  String FILE_THUMBS = "thumbs";
  String FILE_THUMB_NO_COVER = "nocover.png";
  String FILE_THUMB_NONE = "none.png";
  String FILE_DEFAULT_BESTOF_PLAYLIST = "bestof";
  String FILE_DEFAULT_NOVELTIES_PLAYLIST = "novelties";
  String FILE_DEFAULT_BOOKMARKS_PLAYLIST = "bookmarks";
  String FILE_DEFAULT_QUEUE_PLAYLIST = "jajuk-playlist_";
  String FILE_DJ_DIR = "djs";
  /** jajuk web radios repository. */
  String FILE_WEB_RADIOS_CUSTOM = "custom_radios.xml";
  /** Preset web radios file. */
  String FILE_WEB_RADIOS_PRESET = "preset_radios.xml";
  String FILE_FIFO = "fifo.lst";
  /** Bootstrap file name. */
  String FILE_BOOTSTRAP = ".jajuk_bootstrap.xml";
  /** Old (pre-1.9) bootstrap file name */
  String FILE_BOOTSTRAP_OLD = ".jajuk_bootstrap";
  String FILE_MPLAYER_WINDOWS_EXE = "mplayer.exe";
  String FILE_MPLAYER_OSX_EXE = "mplayer";
  String FILE_DEFAULT_PERSPECTIVES_PATH = "perspectives";
  String FILE_JAJUK_DOWNLOADED_FILES_SUFFIX = "_jajuk";
  String FILE_REPORTING_CSS_ALL_FILENAME = "report-all.css";
  URL FILE_REPORTING_CSS_ALL_PATH = UtilSystem.getResource("xslt/"
      + FILE_REPORTING_CSS_ALL_FILENAME);
  String FILE_REPORTING_CSS_PRINT_FILENAME = "report-print.css";
  URL FILE_REPORTING_CSS_PRINT_PATH = UtilSystem.getResource("xslt/"
      + FILE_REPORTING_CSS_PRINT_FILENAME);
  /** Mplayer Windows exe provided by jajuk URL . */
  String URL_MPLAYER_WINDOWS = "http://repository.jajuk.info/mplayer/20130411/mplayer.exe";
  /** Mplayer OSX exe provided by jajuk URL . */
  String URL_MPLAYER_OSX = "http://repository.jajuk.info/mplayer/osx/1.0rc2/mplayer";
  String URL_WEBRADIO_PRESETS = "https://raw.githubusercontent.com/jajuk-team/resources/master/preset_radios.xml";
  // sequence
  String AMBIENCE_PREFIX = "jajuk.ambience.";
  // Proxy type: http
  String PROXY_TYPE_HTTP = "http";
  // Proxy type: socks
  String PROXY_TYPE_SOCKS = "socks";
  // Unknown
  String UNKNOWN_ARTIST = "unknown_artist";
  String UNKNOWN_ALBUM = "unknown_album";
  String UNKNOWN_GENRE = "unknown_genre";
  String UNKNOWN_YEAR = "unknown_year";
  String VARIOUS_ARTIST = "various_artist";
  String COVER_NONE = "none";
  // images
  URL IMAGES_SPLASHSCREEN = UtilSystem.getResource("images/jajuk-splashscreen.jpg");
  URL IMAGE_SEARCH = UtilSystem.getResource("images/search.png");
  // XSLT Files
  URL XSLT_ARTIST = UtilSystem.getResource("xslt/artist.xsl");
  URL XSLT_ALBUM = UtilSystem.getResource("xslt/album.xsl");
  URL XSLT_GENRE = UtilSystem.getResource("xslt/genre.xsl");
  URL XSLT_YEAR = UtilSystem.getResource("xslt/genre.xsl");
  URL XSLT_DIRECTORY = UtilSystem.getResource("xslt/directory.xsl");
  URL XSLT_DEVICE = UtilSystem.getResource("xslt/device.xsl");
  URL XSLT_COLLECTION_LOGICAL = UtilSystem.getResource("xslt/logical_collection.xsl");
  String COLLECTION_LOGICAL = "logical";
  String COLLECTION_PHYSICAL = "physical";
  // -- Command line options --
  /** Tells jajuk to use a .jajuk_test repository */
  String CLI_TEST = "test";
  /** Special forced workspace location CLI option. */
  String CLI_WORKSPACE_LOCATION = "workspace";
  // players impls
  String PLAYER_IMPL_JAVALAYER = "org.jajuk.services.players.JavaLayerPlayerImpl";
  String PLAYER_IMPL_MPLAYER = "org.jajuk.services.players.MPlayerPlayerImpl";
  String PLAYER_IMPL_WEBRADIOS = "org.jajuk.services.players.WebRadioPlayerImpl";
  // tag impls
  String TAG_IMPL_NO_TAGS = "org.jajuk.services.tags.NoTagsTagImpl";
  String TAG_IMPL_JAUDIOTAGGER = "org.jajuk.services.tags.JAudioTaggerTagImpl";
  // device types
  // Devices sync mode
  String DEVICE_SYNCHRO_MODE_BI = "bi";
  String DEVICE_SYNCHRO_MODE_UNI = "uni";
  // extensions
  String EXT_MP3 = "mp3";
  String EXT_MP2 = "mp2";
  String EXT_PLAYLIST = "m3u";
  String EXT_OGG = "ogg";
  /** Ogg Vorbis Audio (see http://en.wikipedia.org/wiki/Ogg, some applications start using this now, e.g. Soundkonverter */
  String EXT_OGA = "oga";
  String EXT_WAV = "wav";
  String EXT_AU = "au";
  String EXT_AIF = "aif";
  String EXT_AIFF = "aiff";
  String EXT_FLAC = "flac";
  /** Extension of the thumbnails. */
  String EXT_THUMB = "png";
  String EXT_MPC = "mpc";
  String EXT_MPPLUS = "mp+";
  String EXT_MPP = "mpp";
  String EXT_WMA = "wma";
  String EXT_APE = "ape";
  String EXT_MAC = "mac";
  String EXT_M4A = "m4a";
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
  String EXT_FLV = "flv";
  // details keys
  String DETAIL_CURRENT_FILE_ID = "current file id";
  String DETAIL_CURRENT_FILE = "current file";
  String DETAIL_CURRENT_DATE = "current date";
  String DETAIL_SELECTION = "selection";
  String DETAIL_ORIGIN = "origin";
  /** Provides details on the perspective that thrown the event. */
  String DETAIL_PERSPECTIVE = "perspective";
  /** Provides details on the view that thrown the event. */
  String DETAIL_VIEW = "view";
  String DETAIL_REASON = "reason";
  String DETAIL_CONTENT = "content";
  String DETAIL_OLD = "old";
  String DETAIL_NEW = "new";
  // startup mode
  String STARTUP_MODE_NOTHING = "nothing";
  String STARTUP_MODE_SHUFFLE = "shuffle";
  String STARTUP_MODE_ITEM = "file";
  String STARTUP_MODE_LAST = "last";
  String STARTUP_MODE_LAST_KEEP_POS = "last_keep_pos";
  String STARTUP_MODE_BESTOF = "bestof";
  String STARTUP_MODE_NOVELTIES = "novelties";
  // --- Configuration keys ---
  String CONF_RELEASE = "jajuk.release";
  String CONF_PERSPECTIVE_DEFAULT = "jajuk.preference.perspective.default";
  String CONF_STATE_REPEAT = "jajuk.state.mode.repeat";
  String CONF_STATE_REPEAT_ALL = "jajuk.state.mode.repeat.all";
  String CONF_STATE_SHUFFLE = "jajuk.state.mode.shuffle";
  String CONF_STATE_KARAOKE = "jajuk.state.karaoke";
  String CONF_STATE_CONTINUE = "jajuk.state.mode.continue";
  String CONF_STATE_INTRO = "jajuk.state.mode.intro";
  String CONF_STARTUP_ITEM = "jajuk.startup.file";
  String CONF_STARTUP_MODE = "jajuk.startup.mode";
  /** Tell if last jajuk session was closed in stopped mode. */
  String CONF_STARTUP_STOPPED = "jajuk.startup.stopped";
  String CONF_CONFIRMATIONS_DELETE_FILE = "jajuk.confirmations.delete_file";
  String CONF_CONFIRMATIONS_EXIT = "jajuk.confirmations.exit";
  String CONF_CONFIRMATIONS_REMOVE_DEVICE = "jajuk.confirmations.remove_device";
  String CONF_CONFIRMATIONS_DELETE_COVER = "jajuk.confirmations.delete_cover";
  String CONF_CONFIRMATIONS_CLEAR_HISTORY = "jajuk.confirmations.clear_history";
  String CONF_CONFIRMATIONS_RESET_RATINGS = "jajuk.confirmations.reset_ratings";
  String CONF_CONFIRMATIONS_REFACTOR_FILES = "jajuk.confirmations.refactor_files";
  String CONF_CONFIRMATIONS_BEFORE_TAG_WRITE = "jajuk.confirmations.write_tag";
  String CONF_OPTIONS_HIDE_UNMOUNTED = "jajuk.options.hide_unmounted";
  String CONF_OPTIONS_LOG_LEVEL = "jajuk.options.log_level";
  String CONF_OPTIONS_LANGUAGE = "jajuk.options.language";
  String CONF_OPTIONS_INTRO_BEGIN = "jajuk.options.intro.begin";
  String CONF_OPTIONS_INTRO_LENGTH = "jajuk.options.intro.length";
  String CONF_OPTIONS_LNF = "jajuk.options.lnf";
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
  String CONF_P2P_SHARE = "jajuk.options.p2p.share";
  String CONF_P2P_ADD_REMOTE_PROPERTIES = "jajuk.options.p2p.add_remote_properties";
  String CONF_P2P_HIDE_LOCAL_PROPERTIES = "jajuk.options.p2p.hide_local_properties";
  String CONF_HISTORY = "jajuk.options.history";
  String CONF_MANUAL_RATINGS = "jajuk.options.manual_ratings";
  String CONF_TAGS_USE_PARENT_DIR = "jajuk.tags.use_parent_dir";
  /** Contains files id separated by a colon. */
  String CONF_BOOKMARKS = "jajuk.bookmarks";
  /** Startup display mode: 0= main window, 1: deprecated, 2: slimbar,  3 : full screen. */
  String CONF_STARTUP_DISPLAY = "jajuk.startup_display_mode";
  int DISPLAY_MODE_MAIN_WINDOW = 0;
  int DISPLAY_MODE_SLIMBAR = 2;
  int DISPLAY_MODE_FULLSCREEN = 3;
  /** Catalog view cover mode. */
  int CATALOG_VIEW_COVER_MODE_ALL = 0;
  int CATALOG_VIEW_COVER_MODE_WITH = 1;
  int CATALOG_VIEW_COVER_MODE_WITHOUT = 2;
  /** Best of size. */
  String CONF_BESTOF_TRACKS_SIZE = "jajuk.bestof_size";
  /** Slimbar position. */
  String CONF_SLIMBAR_POSITION = "jajuk.slimbar_pos";
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
  /** Path of workspace the user wants to use but may be unavailable */
  String CONF_TARGET_WORKSPACE_PATH = "jajuk.target_workspace";
  String CONF_NETWORK_USE_PROXY = "jajuk.network.use_proxy";
  String CONF_NETWORK_PROXY_TYPE = "jajuk.network.proxy_type";
  String CONF_NETWORK_PROXY_HOSTNAME = "jajuk.network.proxy_hostname";
  String CONF_NETWORK_PROXY_PORT = "jajuk.network.proxy_port";
  String CONF_NETWORK_PROXY_LOGIN = "jajuk.network.proxy_login";
  String CONF_NETWORK_PROXY_PWD = "jajuk.network.proxy_pwd";
  String CONF_LASTFM_AUDIOSCROBBLER_ENABLE = "jajuk.network.audioscrobbler";
  String CONF_LASTFM_USER = "jajuk.network.ASUser";
  String CONF_LASTFM_PASSWORD = "jajuk.network.ASPassword";
  String CONF_COVERS_AUTO_COVER = "jajuk.covers.auto_cover";
  String CONF_COVERS_MIRROW_COVER = "jajuk.covers.mirrow_cover";
  String CONF_COVERS_MIRROW_COVER_FS_MODE = "jajuk.covers.mirrow_cover_fs_mode";
  String CONF_COVERS_SHUFFLE = "jajuk.covers.shuffle";
  String CONF_COVERS_SAVE_EXPLORER_FRIENDLY = "jajuk.covers.save.explorer.friendly";
  String CONF_COVERS_SIZE = "jajuk.covers.size";
  String CONF_COVERS_ACCURACY = "jajuk.covers.accuracy";
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
  String CONF_NOT_SHOW_AGAIN_PLAYER = "jajuk.not_show_again.player";
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
  String CONF_MPLAYER_PATH_FORCED = "jajuk.mplayer.forced_path";
  /** MPLayer additional arguments *. */
  String CONF_MPLAYER_ARGS = "jajuk.player.mplayer_args";
  /** MPlayer additional environment variables *. */
  String CONF_ENV_VARIABLES = "jajuk.player.env_variables";
  /** Max Number of thumbs displayed at the same time in catalog view. */
  String CONF_CATALOG_PAGE_SIZE = "jajuk.catalog.pages_size";
  /** Show Catalog popups. */
  String CONF_SHOW_POPUPS = "jajuk.show_popups";
   /** Enable Title view Animation. */
  String CONF_TITLE_ANIMATION = "jajuk.title_animation";
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
  /** Slashscreen flag. */
  String CONF_SPLASH_SCREEN = "jajuk.splashscreen";
  /** Current item index in fifo. */
  String CONF_STARTUP_QUEUE_INDEX = "jajuk.current_file_index";
  /** Current item index in fifo. */
  String CONF_STATS_MIN_VALUE_GENRE_DISPLAY = "jajuk.stats.min_value_genre_display";
  /** Shuffle/novelties mode. */
  String MODE_ALBUM = "album";
  String MODE_TRACK = "track";
  String MODE_ALBUM2 = "album2";
  // miscelanous
  String TRUE = "true";
  String FALSE = "false";
  // Date format
  String DATE_FILE = "yyyyMMdd";
  String ADDITION_DATE_FORMAT = "yyyyMMdd";
  // Playlists
  String PLAYLIST_NOTE = "#Playlist generated by Jajuk " + Const.JAJUK_VERSION;
  // XML tags
  String XML_COLLECTION = "collection";
  /** Webradio file XML tags static strings*/
  String XML_RADIO = "Radio";
  String XML_VERSION = "jajuk_version";
  String XML_TYPES = "types";
  String XML_TYPE = "type";
  String XML_DEVICES = "devices";
  String XML_DEVICE = "device";
  String XML_GENRES = "styles";
  String XML_GENRE = "style";
  String XML_ARTISTS = "authors";
  String XML_ALBUM_ARTISTS = "album-artists";
  String XML_ARTIST = "author";
  String XML_ALBUMS = "albums";
  String XML_ALBUM = "album";
  String XML_ALBUM_ARTIST = "album_artist";
  String XML_ALBUM_DISC_ID = "disc_id";
  String XML_TRACKS = "tracks";
  String XML_TRACK = "track";
  String XML_DIRECTORIES = "directories";
  String XML_DIRECTORY = "directory";
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
  String XML_TRACK_GENRE = "style";
  String XML_TRACK_ARTIST = "author";
  String XML_TRACK_YEAR = "year";
  String XML_TRACK_LENGTH = "length";
  String XML_TRACK_TYPE = "type";
  String XML_TRACK_RATE = "rate";
  String XML_TRACK_HITS = "hits";
  String XML_TRACK_DISCOVERY_DATE = "added";
  String XML_TRACK_ORDER = "order";
  String XML_TRACK_DISC_NUMBER = "disc_number";
  String XML_TRACK_PREFERENCE = "pf";
  String XML_TRACK_TOTAL_PLAYTIME = "tpt";
  String XML_TRACK_BANNED = "ban";
  String XML_TRACK_SCROBBLE = "scrobble";
  String XML_PLAYLIST = "playlist";
  String XML_NAME = "name";
  String XML_PATH = "path";
  String XML_URL = "url";
  String XML_ORIGIN = "origin";
  /** Keywords, separated by a ';'*/
  String XML_KEYWORDS = "keywords";
  /** Label*/
  String XML_DESC = "label";
  /** Bitrate */
  String XML_BITRATE = "bitrate";
  /** Frequency */
  String XML_FREQUENCY = "frequency";
  String XML_QUALITY = "quality";
  String XML_SIZE = "size";
  String XML_DEVICE_MOUNT_POINT = "mount_point";
  String XML_DEVICE_AUTO_REFRESH = "auto_refresh";
  String XML_DEVICE_AUTO_MOUNT = "auto_mount";
  String XML_DEVICE_SYNCHRO_SOURCE = "synchro_source";
  String XML_DEVICE_SYNCHRO_MODE = "synchro_mode";
  String XML_EXPANDED = "exp"; // can be 'y' or 'n'
  /** Cached cover. */
  String XML_ALBUM_DISCOVERED_COVER = "cover_cache";
  /** Selected cover. */
  String XML_ALBUM_SELECTED_COVER = "cover";
  String XML_DIRECTORY_PARENT = "parent";
  String XML_DIRECTORY_SYNCHRONIZED = "sync";
  String XML_TYPE_EXTENSION = "extension";
  String XML_TYPE_PLAYER_IMPL = "player_impl";
  String XML_TYPE_TAG_IMPL = "tag_impl";
  String XML_TYPE_IS_MUSIC = "music";
  String XML_TYPE_SEEK_SUPPORTED = "seek";
  // icon used in the physical tree
  String XML_TYPE_ICON = "icon";
  /** comment tag. */
  String XML_TRACK_COMMENT = "comment";
  /** "any" criteria. */
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
  String XML_DJ_MAX_TRACKS = "max_tracks";
  String XML_DJ_PROPORTIONS = "proportions";
  String XML_DJ_PROPORTION = "proportion";
  String XML_DJ_AMBIENCE = "ambience";
  String XML_DJ_GENRES = "styles";
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
  String XML_STREAMS = "streams";
  String XML_STREAM = "stream";
  /*
   * Reserved XML tags for property names (note that a user can choose a property name equals to
   * meta information attributes names without pbm)
   */
  String[] XML_RESERVED_ATTRIBUTE_NAMES = { XML_NAME, XML_ID, XML_TYPE_EXTENSION,
      XML_TYPE_PLAYER_IMPL, XML_TYPE_TAG_IMPL, XML_TYPE_SEEK_SUPPORTED, XML_TYPE_ICON,
      XML_TYPE_IS_MUSIC, XML_TYPE, XML_URL, XML_DEVICE_AUTO_MOUNT, XML_DEVICE_AUTO_REFRESH,
      XML_EXPANDED, XML_DEVICE_MOUNT_POINT, XML_ALBUM, XML_ARTIST, XML_GENRE, XML_TRACK_LENGTH,
      XML_YEAR, XML_TRACK_RATE, XML_FILES, XML_TRACK_HITS, XML_TRACK_DISCOVERY_DATE,
      XML_DIRECTORY_PARENT, XML_DEVICE, XML_DIRECTORY, XML_TRACK, XML_SIZE, XML_QUALITY,
      XML_PLAYLIST_FILES, XML_TRACK_COMMENT, XML_ANY, XML_TRACK_ORDER, XML_DEVICE_SYNCHRO_MODE,
      XML_DEVICE_SYNCHRO_SOURCE, XML_FILE_DATE, XML_TRACK_TOTAL_PLAYTIME, XML_TRACK_PREFERENCE,
      XML_TRACK_BANNED, XML_TRACK_DISC_NUMBER, XML_ALBUM_ARTIST, XML_ALBUM_DISC_ID,
      XML_TRACK_SCROBBLE, XML_ORIGIN };
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
  String THUMBNAIL_SIZE_50X50 = "50x50";
  String THUMBNAIL_SIZE_100X100 = "100x100";
  String THUMBNAIL_SIZE_150X150 = "150x150";
  String THUMBNAIL_SIZE_200X200 = "200x200";
  String THUMBNAIL_SIZE_250X250 = "250x250";
  String THUMBNAIL_SIZE_300X300 = "300x300";
  // Patterns
  String PATTERN_ARTIST = "%artist";
  String PATTERN_ALBUM_ARTIST = "%album_artist";
  String PATTERN_DISC = "%disc";
  String PATTERN_ALBUM = "%album";
  String PATTERN_GENRE = "%genre";
  String PATTERN_YEAR = "%year";
  String PATTERN_TRACKNAME = "%title";
  String PATTERN_TRACKORDER = "%n";
  String PATTERN_DEFAULT_REORG = PATTERN_YEAR + " - " + PATTERN_ALBUM + "/" + PATTERN_TRACKORDER
      + " - " + PATTERN_ARTIST + " - " + PATTERN_TRACKNAME;
  String PATTERN_DEFAULT_ANIMATION = PATTERN_TRACKNAME + " (" + PATTERN_ARTIST + ")";
  // Strings
  String FRAME_MAXIMIZED = "max";
  // Alarm Clock
  String CONF_ALARM_TIME_HOUR = "jajuk.alarm.hour";
  String CONF_ALARM_TIME_MINUTES = "jajuk.alarm.minutes";
  String CONF_ALARM_TIME_SECONDS = "jajuk.alarm.seconds";
  String CONF_ALARM_FILE = "jajuk.alarm.file";
  String CONF_ALARM_MODE = "jajuk.alarm.mode";
  String CONF_FORMAT_TIME_ELAPSED = "jajuk.format_elapsed_time";
  String CONF_ALARM_ACTION = "jajuk.alarm.action";
  String CONF_ALARM_ENABLED = "jajuk.alarm.enabled";
  /** Option name. */
  String CONF_AUTO_SCROLL = "jajuk.auto_scroll";
  /** Option name. */
  String CONF_EXPLORER_PATH = "jajuk.explorer_path";
  /** Option name. */
  String CONF_BIT_PERFECT = "jajuk.bit_perfect";
  String CONF_PRESERVE_FILE_DATES = "jajuk.preserve_date";
  // Alarm actions
  String ALARM_START_ACTION = "Start Playing";
  String ALARM_STOP_ACTION = "Stop Playing";
  // Preferences
  long PREFERENCE_ADORE = 3;
  long PREFERENCE_LOVE = 2;
  long PREFERENCE_LIKE = 1;
  long PREFERENCE_UNSET = 0;
  long PREFERENCE_AVERAGE = -1;
  long PREFERENCE_POOR = -2;
  long PREFERENCE_HATE = -3;
  String B_P_HTML = "</b></p></html>";
  String P_B = "<p><b>";
  String HTML = "<html>";
  /** File name where the tag cover is stored in. */
  String TAG_COVER_FILE = "tag_cover.png";
  /** Current radio track */
  String CURRENT_RADIO_TRACK = "current_radio_track";


}
