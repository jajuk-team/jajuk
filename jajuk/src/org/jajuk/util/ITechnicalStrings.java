/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
package org.jajuk.util;




/**
 *  Contains all technical/ non-translatable strings
 * @author     Bertrand Florat
 * @created    5 oct. 2003
 */
public interface ITechnicalStrings {
	
	/**Information bar and command bar default vertical size in pixels*/
	public static final int BORDER_Y_SIZE = 112;
	/**Command bar default vertical size in pixels*/
	public static final int BORDER_X_SIZE = 55;
	/**panels precisions in percent*/
	public static final int PRECISION = 5;
	/**Generic border*/
	public static final int BORDER = 4;
	/**Maximum size for covers in KB*/
    public static final int MAX_COVER_SIZE=500;
    /**Maximum number of remote covers*/
    public static final int MAX_REMOTE_COVERS=10;
    /**Special length for player meaning end of file*/
    public static final long TO_THE_END=-1;
    /**Time we wait after an error in ms*/
    public static final int WAIT_AFTER_ERROR= 2000;
    
    		
	// Jajuk version
	public static final String JAJUK_VERSION = "0.3.3";
	public static final String JAJUK_VERSION_DATE = "Build 2005-04-02";
	
	//About 
	public static final String ABOUT = "<html>Jajuk version "+JAJUK_VERSION+"</html>";
	
	// directory path
	public static final String PATH_ICONS = "jar:"+Util.getExecLocation()+"!/org/jajuk/icons/";
	public static final String PATH_IMAGES = "jar:"+Util.getExecLocation()+"!/org/jajuk/images/";
	public static final String PATH_RELATIVE_DOCS = "docs/";
	public static final String PATH_DOCS = "jar:"+Util.getExecLocation()+"!/org/jajuk/"+PATH_RELATIVE_DOCS;
			
	//Unkwnown
	public static final String UNKNOWN_AUTHOR = "unknown_author";
	public static final String UNKNOWN_ALBUM = "unknown_album";
	public static final String UNKNOWN_STYLE = "unknown_style";
	public static final String UNKNOWN_QUALITY = "unknown_quality";
	
	// icons
	public static final String ICON_LOGO = PATH_ICONS + "64x64/jajuk-logo.png";
	public static final String ICON_LOGO_TRAY = PATH_ICONS + "22x22/jajuk-logo.png";
	public static final String ICON_LOGO_ICO = PATH_IMAGES + "jajuk-logo.ico";
	public static final String ICON_LOGO_FRAME = PATH_ICONS + "16x16/jajuk-logo.png";
	public static final String ICON_REPEAT = PATH_ICONS + "16x16/repeat.png";
	public static final String ICON_SHUFFLE = PATH_ICONS + "16x16/shuffle.png"; 
	public static final String ICON_CONTINUE = PATH_ICONS + "16x16/continue.png";
	public static final String ICON_INTRO = PATH_ICONS + "16x16/intro.png";
	public static final String ICON_SHUFFLE_GLOBAL = PATH_ICONS + "16x16/shuffle_global.png";
	public static final String ICON_BESTOF = PATH_ICONS + "16x16/bestof.png";
	public static final String ICON_MUTE = PATH_ICONS + "16x16/mute.png";
	public static final String ICON_UNMUTE = PATH_ICONS + "16x16/unmute.png";
	public static final String ICON_NOVELTIES =  PATH_ICONS + "16x16/novelties.png";
	public static final String ICON_PREVIOUS = PATH_ICONS + "16x16/previous.png";
	public static final String ICON_NEXT = PATH_ICONS + "16x16/next.png";
	public static final String ICON_REW = PATH_ICONS + "16x16/player_rew.png";
	public static final String ICON_PLAY = PATH_ICONS + "16x16/player_play.png";
	public static final String ICON_PAUSE = PATH_ICONS + "16x16/player_pause.png";
	public static final String ICON_STOP = PATH_ICONS + "16x16/player_stop.png";
	public static final String ICON_FWD = PATH_ICONS + "16x16/player_fwd.png";
	public static final String ICON_VOLUME = PATH_ICONS + "16x16/volume.png";
	public static final String ICON_POSITION = PATH_ICONS + "16x16/position.png";
	public static final String ICON_INFO	= PATH_ICONS + "16x16/info.png";
	public static final String ICON_PERSPECTIVE_PHYSICAL = "/org/jajuk/icons/16x16/physical_perspective.png"; //path inside jar file
	public static final String ICON_PERSPECTIVE_LOGICAL	= "/org/jajuk/icons/16x16/logical_perspective.png";
	public static final String ICON_PERSPECTIVE_STATISTICS	= "/org/jajuk/icons/16x16/statistics_perspective.png";
	public static final String ICON_PERSPECTIVE_CONFIGURATION	= "/org/jajuk/icons/16x16/configuration_perspective.png";
	public static final String ICON_PERSPECTIVE_PLAYER	= "/org/jajuk/icons/16x16/player_perspective.png";
	public static final String ICON_PERSPECTIVE_HELP	= "/org/jajuk/icons/16x16/help_perspective.png";
	public static final String ICON_OPEN_FILE	= PATH_ICONS + "16x16/fileopen.png";
	public static final String ICON_EXIT=  PATH_ICONS + "16x16/exit.png";
	public static final String ICON_NEW=  PATH_ICONS + "16x16/new.png";
	public static final String ICON_DELETE=  PATH_ICONS + "16x16/delete.png";
	public static final String ICON_PROPERTIES=  PATH_ICONS + "16x16/properties.png";
	public static final String ICON_MOUNT=  PATH_ICONS + "16x16/mount.png";
	public static final String ICON_UNMOUNT=  PATH_ICONS + "16x16/unmount.png";
	public static final String ICON_TEST=  PATH_ICONS + "16x16/test.png";
	public static final String ICON_REFRESH=  PATH_ICONS + "16x16/refresh.png";
	public static final String ICON_SYNCHRO=  PATH_ICONS + "16x16/synchro.png";
	public static final String ICON_DEVICE_NEW=  PATH_ICONS + "64x64/new.png";
	public static final String ICON_DEVICE_CD_MOUNTED =  PATH_ICONS + "64x64/cdrom_mount.png";
	public static final String ICON_DEVICE_CD_UNMOUNTED =  PATH_ICONS + "64x64/cdrom_unmount.png";
	public static final String ICON_DEVICE_CD_AUDIO_MOUNTED =  PATH_ICONS + "64x64/cdaudio_mount.png";
	public static final String ICON_DEVICE_CD_AUDIO_UNMOUNTED =  PATH_ICONS + "64x64/cdaudio_unmount.png";
	public static final String ICON_DEVICE_EXT_DD_MOUNTED =  PATH_ICONS + "64x64/ext_dd_mount.png";
	public static final String ICON_DEVICE_EXT_DD_UNMOUNTED =  PATH_ICONS + "64x64/ext_dd_unmount.png";
	public static final String ICON_DEVICE_DIRECTORY_MOUNTED =  PATH_ICONS + "64x64/folder_mount.png";
	public static final String ICON_DEVICE_DIRECTORY_UNMOUNTED =  PATH_ICONS + "64x64/folder_unmount.png";
	public static final String ICON_DEVICE_PLAYER_MOUNTED =  PATH_ICONS + "64x64/player_mount.png";
	public static final String ICON_DEVICE_PLAYER_UNMOUNTED =  PATH_ICONS + "64x64/player_unmount.png";
	public static final String ICON_DEVICE_REMOTE_MOUNTED =  PATH_ICONS + "64x64/remote_mount.png";
	public static final String ICON_DEVICE_REMOTE_UNMOUNTED =  PATH_ICONS + "64x64/remote_unmount.png";
	public static final String ICON_DEVICE_CD_MOUNTED_SMALL =  PATH_ICONS + "22x22/cdrom_mount.png";
	public static final String ICON_DEVICE_CD_UNMOUNTED_SMALL =  PATH_ICONS + "22x22/cdrom_unmount.png";
	public static final String ICON_DEVICE_CD_AUDIO_MOUNTED_SMALL =  PATH_ICONS + "22x22/cdaudio_mount.png";
	public static final String ICON_DEVICE_CD_AUDIO_UNMOUNTED_SMALL =  PATH_ICONS + "22x22/cdaudio_unmount.png";
	public static final String ICON_DEVICE_EXT_DD_MOUNTED_SMALL =  PATH_ICONS + "22x22/ext_dd_mount.png";
	public static final String ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL =  PATH_ICONS + "22x22/ext_dd_unmount.png";
	public static final String ICON_DEVICE_DIRECTORY_MOUNTED_SMALL =  PATH_ICONS + "22x22/folder_mount.png";
	public static final String ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL =  PATH_ICONS + "22x22/folder_unmount.png";
	public static final String ICON_DEVICE_PLAYER_MOUNTED_SMALL =  PATH_ICONS + "22x22/player_mount.png";
	public static final String ICON_DEVICE_PLAYER_UNMOUNTED_SMALL =  PATH_ICONS + "22x22/player_unmount.png";
	public static final String ICON_DEVICE_REMOTE_MOUNTED_SMALL =  PATH_ICONS + "22x22/remote_mount.png";
	public static final String ICON_DEVICE_REMOTE_UNMOUNTED_SMALL =  PATH_ICONS + "22x22/remote_unmount.png";
	public static final String ICON_OK =  PATH_ICONS + "22x22/ok.png";
	public static final String ICON_KO =  PATH_ICONS + "22x22/ko.png";
	public static final String ICON_FILE =  PATH_ICONS + "16x16/track.png";
	public static final String ICON_DIRECTORY_SYNCHRO =  PATH_ICONS + "16x16/dir_synchro.png";
	public static final String ICON_DIRECTORY_DESYNCHRO =  PATH_ICONS + "16x16/dir_desynchro.png";
	public static final String ICON_PLAYLIST_FILE =  PATH_ICONS + "16x16/playlist.png";
	public static final String ICON_STYLE =  PATH_ICONS + "16x16/style.png";
	public static final String ICON_AUTHOR =  PATH_ICONS + "16x16/author.png";
	public static final String ICON_ALBUM =  PATH_ICONS + "16x16/album.png";
	public static final String ICON_APPLY_FILTER =  PATH_ICONS + "16x16/filter.png";
	public static final String ICON_CLEAR_FILTER =  PATH_ICONS + "16x16/clear.png";
	public static final String ICON_ADVANCED_FILTER =  PATH_ICONS + "16x16/complex_search.png";
	public static final String ICON_PLAYLIST_QUEUE =  PATH_ICONS + "40x40/playlist_queue.png";
	public static final String ICON_PLAYLIST_NORMAL =  PATH_ICONS + "40x40/playlist_normal.png";
	public static final String ICON_PLAYLIST_NEW =  PATH_ICONS + "40x40/playlist_new.png";
	public static final String ICON_PLAYLIST_BOOKMARK =  PATH_ICONS + "40x40/playlist_bookmark.png";
	public static final String ICON_PLAYLIST_BESTOF =  PATH_ICONS + "40x40/playlist_bestof.png";
	public static final String ICON_RUN =  PATH_ICONS + "16x16/player_play.png";
	public static final String ICON_ADD =  PATH_ICONS + "16x16/add.png";
	public static final String ICON_REMOVE =  PATH_ICONS + "16x16/remove.png";
	public static final String ICON_UP =  PATH_ICONS + "16x16/up.png";
	public static final String ICON_DOWN =  PATH_ICONS + "16x16/down.png";
	public static final String ICON_ADD_SHUFFLE =  PATH_ICONS + "16x16/add_shuffle.png";
	public static final String ICON_CLEAR =  PATH_ICONS + "16x16/clear.png";
	public static final String ICON_SAVE =  PATH_ICONS + "16x16/save.png";
	public static final String ICON_SAVE_AS =  PATH_ICONS + "16x16/saveas.png";
	public static final String ICON_DEFAULT_COVER =  PATH_ICONS + "16x16/ok.png";
	public static final String ICON_MODE_NORMAL =  PATH_ICONS + "16x16/norm.png";
	public static final String ICON_NET_SEARCH =  PATH_ICONS + "16x16/netsearch.png";
	public static final String ICON_TRACK_FIFO_PLANNED =  PATH_ICONS + "16x16/clock.png";
	public static final String ICON_TRACK_FIFO_NORM =  PATH_ICONS + "16x16/player_perspective.png";
	public static final String ICON_TRACK_FIFO_REPEAT =  PATH_ICONS + "16x16/repeat.png";
			
	//images
	public static final String IMAGES_SPLASHSCREEN =  PATH_IMAGES + "jajuk-splashscreen.png";
	public static final String IMAGES_STAT_PAPER =  PATH_IMAGES + "No-Ones-Laughing-3.jpg";
	
	//files
	public static final String FILE_JAJUK_DIR = System.getProperty("user.home")+"/.jajuk";
	public static final String FILE_COLLECTION = System.getProperty("user.home")+"/.jajuk/collection.xml";
	public static final String FILE_PERSPECTIVES_CONF = System.getProperty("user.home")+"/.jajuk/perspectives.xml";
	public static final String FILE_PHYSICAL_PERSPECTIVE = System.getProperty("user.home")+"/.jajuk/physicalPerspectives.ser";
	public static final String FILE_LOGICAL_PERSPECTIVE = System.getProperty("user.home")+"/.jajuk/logicalPerspectives.ser";
	public static final String FILE_PLAYER_PERSPECTIVE = System.getProperty("user.home")+"/.jajuk/playerPerspectives.ser";
	public static final String FILE_STAT_PERSPECTIVE = System.getProperty("user.home")+"/.jajuk/statPerspectives.ser";
	public static final String FILE_HELP_PERSPECTIVE = System.getProperty("user.home")+"/.jajuk/helpPerspectives.ser";
	public static final String FILE_CONFIGURATION_PERSPECTIVE = System.getProperty("user.home")+"/.jajuk/configurationPerspectives.ser";
	public static final String FILE_CONFIGURATION = System.getProperty("user.home")+"/.jajuk/conf.properties";
	public static final String FILE_HISTORY = System.getProperty("user.home")+"/.jajuk/history.xml";
	public static final String FILE_LOCK = System.getProperty("user.home")+"/.jajuk/.lock";
	public static final String FILE_ABOUT = "about.html";
	public static final String FILE_DEFAULT_COVER = "cover";
	public static final String FILE_DEFAULT_COVER_2 = "front";
	public static final String FILE_ABSOLUTE_DEFAULT_COVER = "jajuk-default-cover.";
	public static final String FILE_LANGPACK_PART1 = "jajuk"; //langpack name : jajuk_<locale>.properties
	public static final String FILE_LANGPACK_PART2 = ".properties"; //langpack name : jajuk_<locale>.properties
	
	//players impls
	public static final String PLAYER_IMPL_JAVALAYER= "org.jajuk.players.JavaLayerPlayerImpl";
	
	//tag impls
	public static final String TAG_IMPL_JID3LIB= "org.jajuk.tag.JID3LibTagImpl";
	public static final String TAG_IMPL_MP3INFO= "org.jajuk.tag.MP3InfoTagImpl";
	public static final String TAG_IMPL_RABBIT_FARM= "org.jajuk.tag.RabbitFarmTagImpl";
	public static final String TAG_IMPL_JLGUI_MP3= "org.jajuk.tag.JlGuiMP3TagImpl";
	public static final String TAG_IMPL_JLGUI_OGG= "org.jajuk.tag.JlGuiOggTagImpl";
	public static final String TAG_IMPL_NO_TAGS= "org.jajuk.tag.NoTagsTagImpl";
	
	//device types
	public static final String DEVICE_TYPE_DIRECTORY = "Device_type.directory";
	public static final String DEVICE_TYPE_FILE_CD = "Device_type.file_cd";
	public static final String DEVICE_TYPE_AUDIO_CD = "Device_type.audio_cd";
	public static final String DEVICE_TYPE_REMOTE = "Device_type.remote";
	public static final String DEVICE_TYPE_USBKEY = "Device_type.usbkey";
	
	//Types properties
	public static final String TYPE_PROPERTY_IS_MUSIC = "music";  
	public static final String TYPE_PROPERTY_SEEK_SUPPORTED = "seek";  
	public static final String TYPE_PROPERTY_TECH_DESC = "tech_desc"; //type description as given in the steam 
	public static final String TYPE_PROPERTY_TECH_DESC_MP3 = "mp3"; 
	public static final String TYPE_PROPERTY_TECH_DESC_OGG = "ogg"; 
	public static final String TYPE_PROPERTY_TECH_DESC_AIFF = "aiff"; 
	public static final String TYPE_PROPERTY_TECH_DESC_WAVE = "wave"; 
	public static final String TYPE_PROPERTY_TECH_DESC_AU = "au"; 
	public static final String TYPE_PROPERTY_TECH_DESC_SPEEX = "speex"; 
		
	//Devices properties
	public static final String DEVICE_OPTION_AUTO_REFRESH = "auto_refresh";
	public static final String DEVICE_OPTION_AUTO_MOUNT = "auto_mount";
	public static final String DEVICE_OPTION_SYNCHRO_SOURCE = "synchro_source";
	public static final String DEVICE_OPTION_SYNCHRO_MODE = "synchro_mode";
	public static final String DEVICE_OPTION_SYNCHRO_MODE_BI = "bi";
	public static final String DEVICE_OPTION_SYNCHRO_MODE_UNI = "uni";
	public static final String DEVICE_OPTION_SYNCHRO_OPT1 = "opt1";
	
	//Directories properties
	public static final String DIRECTORY_OPTION_SYNCHRO_MODE = "sync";  //can be 'y' or 'n'
	public static final String OPTION_EXPANDED = "exp";  //can be 'y' or 'n'
	public static final String OPTION_PLAYLIST = "plf";  //associated playlist file
		
	//perspectives
	public static final String PERSPECTIVE_NAME_PHYSICAL = "org.jajuk.ui.perspectives.PhysicalPerspective";
	public static final String PERSPECTIVE_NAME_LOGICAL = "org.jajuk.ui.perspectives.LogicalPerspective";
	public static final String PERSPECTIVE_NAME_CONFIGURATION = "org.jajuk.ui.perspectives.ConfigurationPerspective";
	public static final String PERSPECTIVE_NAME_STATISTICS = "org.jajuk.ui.perspectives.StatPerspective";
	public static final String PERSPECTIVE_NAME_HELP = "org.jajuk.ui.perspectives.HelpPerspective";
	public static final String PERSPECTIVE_NAME_PLAYER = "org.jajuk.ui.perspectives.PlayerPerspective";
	
	 //views
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
	 	
	//extensions
	public static final String EXT_MP3 = "mp3";
	public static final String EXT_PLAYLIST = "m3u";
	public static final String EXT_OGG = "ogg";
	public static final String EXT_WAV = "wav";
	public static final String EXT_AU = "au";
	public static final String EXT_AIFF = "aiff";
		
	//event keys
	public static final String EVENT_EXIT ="exit"; //exit has be required
	public static final String EVENT_OPEN_FILE ="open file"; //opening a file in the command jpanel
	public static final String EVENT_REPEAT_MODE_STATUS_CHANGED="repeat status changed"; //repeat mode changed
	public static final String EVENT_SHUFFLE_MODE_STATUS_CHANGED="shuffle status changed"; //shuffle mode changed
	public static final String EVENT_CONTINUE_MODE_STATUS_CHANGED="continue status changed"; //continue mode changed
	public static final String EVENT_INTRO_MODE_STATUS_CHANGED="intro status changed"; //intro mode changed
	public static final String EVENT_DEVICE_NEW="new device"; //new device
	public static final String EVENT_DEVICE_DELETE="delete device"; //removed device
	public static final String EVENT_DEVICE_PROPERTIES="device properties"; //device properties display
	public static final String EVENT_DEVICE_MOUNT="mount device"; //mount device
	public static final String EVENT_DEVICE_UNMOUNT="unmount device"; //unmount a device
	public static final String EVENT_DEVICE_TEST="test device"; //test a device
	public static final String EVENT_DEVICE_REFRESH="refresh device"; //refresh a device
	public static final String EVENT_DEVICE_SYNCHRO="synchronize device"; //sync. a device
	public static final String EVENT_VIEW_REFRESH_REQUEST="refresh view"; //refresh a view is required, used in the device view
	public static final String EVENT_VIEW_CLOSE_REQUEST="close view"; //close a view
	public static final String EVENT_VIEW_SHOW_REQUEST="show view"; //show a view
	public static final String EVENT_VIEW_SHOW_STATUS_CHANGED_REQUEST="change status view"; //change 
	public static final String EVENT_VIEW_RESTORE_DEFAULTS="restore default views";
	public static final String EVENT_VIEW_COMMAND_SELECT_HISTORY_ITEM="select history item";
	public static final String EVENT_HELP_REQUIRED="help required";//The help should be displayed
	public static final String EVENT_COVER_REFRESH="cover refresh";//the cover should be refreshed
	public static final String EVENT_COVER_CHANGE="cover change";//Request for a cover change
    public static final String EVENT_PLAYER_STOP="player stop";//the stop button has been pressed
	public static final String EVENT_PLAYER_PLAY="player play";  //the play button has been pressed
	public static final String EVENT_PLAYER_PAUSE="player pause";//the pause button has been pressed
	public static final String EVENT_PLAYER_RESUME="player resume";//the resume button has been pressed
	public static final String EVENT_PLAYLIST_REFRESH="playlist refresh";
	public static final String EVENT_PLAYLIST_CHANGED="playlist changed";
	public static final String EVENT_FILE_LAUNCHED="file launched";//a file has been lauched by the fifo
	public static final String EVENT_HEART_BEAT="heart beat";//heart beat for geenral use to refresh subscribers every n secs
	public static final String EVENT_ZERO="zero"; //a reinit has been required
	public static final String EVENT_ADD_HISTORY_ITEM="add history item"; //a new element has been added in the history
	public static final String EVENT_SPECIAL_MODE="special mode changed"; //special mode (global shuffle, novelties, bestof...) changed
	public static final String EVENT_PLAY_ERROR="error"; //an error occured during a play
	public static final String EVENT_MUTE_STATE="mute_state"; //mute state changed
	public static final String EVENT_SYNC_TREE_TABLE="sync_tree_table"; //sync table and tree views
    public static final String EVENT_CLEAR_HISTORY="clear history"; //clear history
    
	//details keys
	public static final String DETAIL_CURRENT_FILE_ID="current file id";
	public static final String DETAIL_CURRENT_FILE="current file";
	public static final String DETAIL_CURRENT_DATE="current date";
	public static final String DETAIL_CURRENT_POSITION="current position";
	public static final String DETAIL_TOTAL="total time";
	public static final String DETAIL_HISTORY_ITEM="history item";
	public static final String DETAIL_SPECIAL_MODE="special mode";
	public static final String DETAIL_SPECIAL_MODE_SHUFFLE="shuffle";
	public static final String DETAIL_SPECIAL_MODE_BESTOF="bestof";
	public static final String DETAIL_SPECIAL_MODE_NOVELTIES="novelties";
	public static final String DETAIL_SPECIAL_MODE_NORMAL="norm";
	public static final String DETAIL_SELECTION="selection";
	public static final String DETAIL_ENABLE="enable";
	public static final String DETAIL_ORIGIN="origin";
				
	//Look and feel
	public static final String LNF_METAL = "Metal";
	public static final String LNF_METAL_CLASS = "javax.swing.plaf.metal.MetalLookAndFeel";
	public static final String LNF_METAL_CBUI = "org.jajuk.ui.laf.MetalComboBoxUI";
	public static final String LNF_GTK = "Gtk";
	public static final String LNF_GTK_CLASS = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
	public static final String LNF_GTK_CBUI = "";
	public static final String LNF_WINDOWS = "Windows";
	public static final String LNF_WINDOWS_CLASS = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	public static final String LNF_WINDOWS_CBUI = "";
	public static final String LNF_KUNSTSTOFF = "Kunststoff";
	public static final String LNF_KUNSTSTOFF_CLASS = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";
	public static final String LNF_KUNSTSTOFF_CBUI = "org.jajuk.ui.laf.KunststoffComboBoxUI";
	public static final String LNF_LIQUID = "Liquid";
	public static final String LNF_LIQUID_CLASS = "com.birosoft.liquid.LiquidLookAndFeel";
	public static final String LNF_LIQUID_CBUI = "org.jajuk.ui.laf.LiquidComboBoxUI";
	public static final String LNF_METOUIA = "Metouia";
	public static final String LNF_METOUIA_CLASS = "net.sourceforge.mlf.metouia.MetouiaLookAndFeel";
	public static final String LNF_COMPIERE = "Compiere";
	public static final String LNF_COMPIERE_CLASS = "org.compiere.plaf.CompiereLookAndFeel";
	public static final String LNF_NAPKIN = "Napkin";
	public static final String LNF_NAPKIN_CLASS = "napkin.NapkinLookAndFeel";
	public static final String LNF_PLASTIC = "Plastic";
	public static final String LNF_PLASTIC_CLASS = "com.jgoodies.plaf.plastic.PlasticLookAndFeel";
	public static final String LNF_PLASTICXP = "Plastic XP";
	public static final String LNF_PLASTICXP_CLASS = "com.jgoodies.plaf.plastic.PlasticXPLookAndFeel";
	public static final String LNF_PLASTIC3D = "Plastic 3D";
	public static final String LNF_PLASTIC3D_CLASS = "com.jgoodies.plaf.plastic.Plastic3DLookAndFeel";
		
	//statup mode
	public static final String STARTUP_MODE_NOTHING = "nothing";
	public static final String STARTUP_MODE_SHUFFLE = "shuffle";
	public static final String STARTUP_MODE_FILE = "file";
	public static final String STARTUP_MODE_LAST = "last";
	public static final String STARTUP_MODE_LAST_KEEP_POS = "last_keep_pos";
	public static final String STARTUP_MODE_BESTOF = "bestof";
	public static final String STARTUP_MODE_NOVELTIES = "novelties";
	
	//	configuration keys
	public static final String CONF_PERSPECTIVE_DEFAULT = "jajuk.preference.perspective.default";
	public static final String CONF_STATE_REPEAT = "jajuk.state.mode.repeat";
	public static final String CONF_STATE_SHUFFLE = "jajuk.state.mode.shuffle";
	public static final String CONF_STATE_CONTINUE = "jajuk.state.mode.continue";
	public static final String CONF_STATE_INTRO = "jajuk.state.mode.intro";
	public static final String CONF_STATE_WAS_PLAYING = "jajuk.state.was_playing"; //wether user exited jajuk in stop state or playing state
	public static final String CONF_STARTUP_FILE = "jajuk.startup.file";
	public static final String CONF_STARTUP_MODE = "jajuk.startup.mode";
	public static final String CONF_CONFIRMATIONS_DELETE_FILE = "jajuk.confirmations.delete_file";
	public static final String CONF_CONFIRMATIONS_EXIT = "jajuk.confirmations.exit";
	public static final String CONF_CONFIRMATIONS_REMOVE_DEVICE = "jajuk.confirmations.remove_device";
	public static final String CONF_CONFIRMATIONS_DELETE_COVER = "jajuk.confirmations.delete_cover";
	public static final String CONF_OPTIONS_HIDE_UNMOUNTED = "jajuk.options.hide_unmounted";
	public static final String CONF_OPTIONS_RESTART = "jajuk.options.restart";
	public static final String CONF_OPTIONS_LOG_LEVEL = "jajuk.options.log_level";
	public static final String CONF_OPTIONS_LANGUAGE = "jajuk.options.language";
	public static final String CONF_OPTIONS_INTRO_BEGIN = "jajuk.options.intro.begin";
	public static final String CONF_OPTIONS_INTRO_LENGTH = "jajuk.options.intro.length";
	public static final String CONF_OPTIONS_SEARCH_ONLY_MOUNTED = "jajuk.options.search_only_mounted";
	public static final String CONF_OPTIONS_LNF = "jajuk.options.lnf";
	public static final String CONF_OPTIONS_NOVELTIES_AGE = "jajuk.options.novelties";
	public static final String CONF_OPTIONS_VISIBLE_PLANNED = "jajuk.options.visible_planned"; //number of visible planned tracks
	public static final String CONF_OPTIONS_DEFAULT_ACTION_CLICK = "jajuk.options.default_action_click"; //default action (play or push) when clicking on an item
	public static final String CONF_OPTIONS_DEFAULT_ACTION_DROP = "jajuk.options.default_action_drop"; //default action (play or push) when droping on an item
	public static final String CONF_OPTIONS_SYNC_TABLE_TREE = "jajuk.options.sync_table_tree"; //synchronize table and tree views
	public static final String CONF_P2P_SHARE = "jajuk.options.p2p.share";
	public static final String CONF_P2P_ADD_REMOTE_PROPERTIES = "jajuk.options.p2p.add_remote_properties";
	public static final String CONF_P2P_HIDE_LOCAL_PROPERTIES = "jajuk.options.p2p.hide_local_properties";
	public static final String CONF_P2P_PASSWORD = "jajuk.options.p2p.password";
	public static final String CONF_HISTORY = "jajuk.options.history";
	public static final String CONF_FIRST_CON = "jajuk.first_con";
	public static final String CONF_TAGS_DEEP_SCAN = "jajuk.tags.deep_scan";
	public static final String CONF_TAGS_USE_PARENT_DIR = "jajuk.tags.use_parent_dir";
	public static final String CONF_BOOKMARKS = "jajuk.bookmarks"; //contains files id separated by a colon
	public static final String CONF_SHOW_AT_STARTUP = "jajuk.show_at_startup"; //show jajuk window at startup
	public static final String CONF_BESTOF_SIZE = "jajuk.bestof_size"; //best of size
	public static final String CONF_VOLUME = "jajuk.volume"; //gain (float)
	public static final String CONF_REGEXP = "jajuk.regexp"; //use regular expressions ?
	public static final String CONF_BACKUP_SIZE = "jajuk.backup_size"; //backup size for collection.xml in MB
	public static final String CONF_COLLECTION_CHARSET = "jajuk.collection_charset";//collection file charset (utf-8 or utf-16)
	public static final String CONF_STARTUP_LAST_POSITION = "jajuk.startup.last_position";
	public static final String CONF_NETWORK_USE_PROXY = "jajuk.network.use_proxy";
	public static final String CONF_NETWORK_PROXY_HOSTNAME = "jajuk.network.proxy_hostname";
	public static final String CONF_NETWORK_PROXY_PORT = "jajuk.network.proxy_port";
	public static final String CONF_NETWORK_PROXY_LOGIN = "jajuk.network.proxy_login";
	public static final String CONF_COVERS_AUTO_COVER = "jajuk.covers.auto_cover";
	public static final String CONF_COVERS_SHUFFLE = "jajuk.covers.shuffle";
	public static final String CONF_COVERS_PRELOAD = "jajuk.covers.preload";
	public static final String CONF_COVERS_RESIZE = "jajuk.covers.resize";
	public static final String CONF_COVERS_MIN_SIZE = "jajuk.covers.min_size";
	public static final String CONF_COVERS_MAX_SIZE = "jajuk.covers.max_size";
	public static final String CONF_COVERS_ACCURACY = "jajuk.covers.accuracy";
	public static final String CONF_COVERS_CHANGE_AT_EACH_TRACK = "jajuk.covers.change_on_each_track"; //Load cover at each track 
	public static final String CONF_NETWORK_CONNECTION_TO = "jajuk.network.connection_timeout";
	public static final String CONF_NETWORK_TRANSFERT_TO = "jajuk.network.transfert_timeout";
	public static final String CONF_OPTIONS_TAB = "jajuk.options.tab"; //Last Option selected tab 
    public static final String CONF_BUFFER_SIZE = "jajuk.buffer_size"; //data buffer size in bytes
    public static final String CONF_AUDIO_BUFFER_SIZE = "jajuk.audio_buffer_size"; //Audio buffer size in bytes
    	
	//Accuracy levels
	public static final String ACCURACY_LOW = "low";
	public static final String ACCURACY_NORMAL = "normal";
	public static final String ACCURACY_HIGH = "high";
	
	//miscelanous
	public static final String TRUE= "true";
	public static final String FALSE= "false";
		
	//views identifiers 
	/** Identifier of the physical tree view */
	public static final String VIEW_PHYSICAL_TREE	= "VIEW_PHYSICAL_TREE";
	/** Identifier of the track list view */
	public static final String VIEW_TRACK_LIST		= "VIEW_TRACK_LIST";
	
	//Date format
	public static final String DATE_FILE = "yyyyMMdd";
	
	//Playlists
	public static final String PLAYLIST_NOTE = "#Playlist generated by Jajuk "+JAJUK_VERSION;
	
	//XML tags
	public static final String XML_TYPES = "types";
	public static final String XML_TYPE = "type";
	public static final String XML_DEVICES = "devices";
	public static final String XML_DEVICE = "device";
	public static final String XML_STYLES = "styles";
	public static final String XML_STYLE = "style";
	public static final String XML_AUTHORS = "author";
	public static final String XML_AUTHOR = "author";
	public static final String XML_ALBUMS = "albums";
	public static final String XML_ALBUM = "album";
	public static final String XML_TRACKS = "tracks";
	public static final String XML_TRACK = "track";
	public static final String XML_DIRECTORIES = "directory";
	public static final String XML_DIRECTORY = "directory";
	public static final String XML_FILES = "files";
	public static final String XML_FILE = "file";
	public static final String XML_PLAYLIST_FILES = "playlist_files";
	public static final String XML_PLAYLIST_FILE = "playlist_file";
	public static final String XML_PLAYLISTS = "playlists";
	public static final String[] XML_RESERVED_ATTRIBUTE_NAMES = {"name","id","extension","player_impl","tag_impl",
			"type","url","album","style","author","length","year","added","rate","files","hits","directory","size",
			"quality","track","playlist_files","hashcode"}; //contains variables names
	public static final String[] XML_RESERVED_PROPERTIES_NAMES = {"seek","music","mount_point","synchro_source"
			,"synchro_mode","auto_refresh","auto_mount","exp","sync","plf_index","plf","default_cover"}; //contains jajuk-reserved properties
	public static final String XML_ID = "id";
	public static final String XML_TRACK_NAME = "name";
	public static final String XML_TRACK_ALBUM = "album";
	public static final String XML_TRACK_STYLE = "style";
	public static final String XML_TRACK_AUTHOR = "author";
	public static final String XML_TRACK_LENGTH = "length";
	public static final String XML_TRACK_YEAR = "year";
	public static final String XML_TRACK_TYPE = "type";
	public static final String XML_TRACK_RATE = "rate";
	public static final String XML_TRACK_HITS = "hits";
	public static final String XML_TRACK_ADDED = "added";
			
	public static final String XML_PLAYLIST = "playlist";
	
}