/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Release$
*/
package org.jajuk.base;


/**
 *  Contains all technical/ non-translatable strings
 *
 * @author     bflorat
 * @created    5 oct. 2003
 */
public interface ITechnicalStrings {
	
		// directory path
	public static final String PATH_ICONS = System.getProperty("user.dir")+"/dist-files/icons/";
	public static final String PATH_IMAGES = System.getProperty("user.dir")+"/dist-files/images/";
	
	
	
		// paths to icons
	public static final String ICON_REPEAT_ON = PATH_ICONS + "16x16/repeat.png";
	public static final String ICON_REPEAT_OFF = PATH_ICONS + "16x16/repeat_off.png";
	public static final String ICON_SHUFFLE_ON = PATH_ICONS + "16x16/shuffle.png"; 
	public static final String ICON_SHUFFLE_OFF = PATH_ICONS + "16x16/shuffle_off.png"; 
	public static final String ICON_CONTINUE_ON = PATH_ICONS + "16x16/continue.png";
	public static final String ICON_CONTINUE_OFF = PATH_ICONS + "16x16/continue_off.png";
	public static final String ICON_INTRO_ON = PATH_ICONS + "16x16/filter.png";
	public static final String ICON_INTRO_OFF = PATH_ICONS + "16x16/filter_off.png";
	public static final String ICON_ROLL = PATH_ICONS + "16x16/roll.png";
	public static final String ICON_BESTOF = PATH_ICONS + "16x16/bestof.png";
	public static final String ICON_MUTE = PATH_ICONS + "16x16/mute.png";
	public static final String ICON_UP = PATH_ICONS + "16x16/up.png";
	public static final String ICON_DOWN = PATH_ICONS + "16x16/down.png";
	public static final String ICON_REW = PATH_ICONS + "16x16/player_rew.png";
	public static final String ICON_PLAY = PATH_ICONS + "16x16/player_end.png";
	public static final String ICON_STOP = PATH_ICONS + "16x16/player_stop.png";
	public static final String ICON_FWD = PATH_ICONS + "16x16/player_fwd.png";
	public static final String ICON_VOLUME = PATH_ICONS + "16x16/volume.png";
	public static final String ICON_POSITION = PATH_ICONS + "16x16/bottom.png";
	public static final String ICON_INFO	= PATH_ICONS + "16x16/info.png";
	public static final String ICON_PERSPECTIVE_PHYSICAL			= PATH_ICONS + "16x16/physical_perspective.png";
	public static final String ICON_PERSPECTIVE_LOGICAL			= PATH_ICONS + "16x16/logical_perspective.png";
	public static final String ICON_PERSPECTIVE_STATISTICS		= PATH_ICONS + "16x16/percent.png";
	public static final String ICON_PERSPECTIVE_CONFIGURATION	= PATH_ICONS + "16x16/configure.png";
	public static final String ICON_PERSPECTIVE_HELP	= PATH_ICONS + "16x16/info.png";
	public static final String ICON_OPEN_FILE	= PATH_ICONS + "16x16/fileopen.png";
	public static final String ICON_EXIT=  PATH_ICONS + "16x16/exit.png";
	public static final String ICON_NEW=  PATH_ICONS + "16x16/new.png";
	public static final String ICON_DELETE=  PATH_ICONS + "16x16/delete.png";
	public static final String ICON_PROPERTIES=  PATH_ICONS + "16x16/properties.png";
	public static final String ICON_MOUNT=  PATH_ICONS + "16x16/mount.png";
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
	public static final String ICON_OK =  PATH_ICONS + "22x22/ok.png";
	public static final String ICON_KO =  PATH_ICONS + "22x22/ko.png";
	
	//images
	public static final String IMAGES_SPLASHSCREEN =  PATH_IMAGES + "jajuk-logo.png";
	
	
	//logs
	public static final String LOG_PATTERN="%d{yyyy/MM/dd HH:mm:ss} [%p] %m\n";
	public static final String LOG_FILE_SIZE="1MB";

	//files
	public static final String FILE_JAJUK_DIR = System.getProperty("user.home")+"/.jajuk";
	public static final String FILE_LOG = System.getProperty("user.home")+"/.jajuk/jajuk.log";
	public static final String FILE_COLLECTION = System.getProperty("user.home")+"/.jajuk/collection.xml";
	public static final String FILE_PERSPECTIVES_CONF = System.getProperty("user.home")+"/.jajuk/perspectives.xml";
	public static final String FILE_CONFIGURATION = System.getProperty("user.home")+"/.jajuk/conf.properties";
	public static final String FILE_HISTORY = System.getProperty("user.home")+"/.jajuk/history.xml";
	
	//players impls
	public static final String PLAYER_IMPL_JAVALAYER= "org.jajuk.players.JavaLayerPlayerImpl";
	
	//tag impls
	public static final String TAG_IMPL_JID3LIB= "org.jajuk.tag.JID3LibTagImpl";
	public static final String TAG_IMPL_MP3INFO= "org.jajuk.tag.MP3InfoTagImpl";
	public static final String TAG_IMPL_RABBIT_FARM= "org.jajuk.tag.RabbitFarmTagImpl";
	
	
	//device types
	public static final String DEVICE_TYPE_DIRECTORY = "Device_type.directory";
	public static final String DEVICE_TYPE_FILE_CD = "Device_type.file_cd";
	public static final String DEVICE_TYPE_AUDIO_CD = "Device_type.audio_cd";
	public static final String DEVICE_TYPE_REMOTE = "Device_type.remote";
	public static final String DEVICE_TYPE_USBKEY = "Device_type.usbkey";
	
	//perspectives
	public static final String PERSPECTIVE_NAME_PHYSICAL = "org.jajuk.ui.perspectives.PhysicalPerspective";
	public static final String PERSPECTIVE_NAME_LOGICAL = "org.jajuk.ui.perspectives.LogicalPerspective";
	public static final String PERSPECTIVE_NAME_CONFIGURATION = "org.jajuk.ui.perspectives.ConfigurationPerspective";
	public static final String PERSPECTIVE_NAME_STATISTICS = "org.jajuk.ui.perspectives.StatPerspective";
	public static final String PERSPECTIVE_NAME_HELP = "org.jajuk.ui.perspectives.HelpPerspective";
	
	 //views
	 public static final String VIEW_NAME_DEVICES = "org.jajuk.ui.views.DeviceView";
	public static final String VIEW_NAME_PARAMETERS = "org.jajuk.ui.views.ParameterView";
	 	
	//extensions
	public static final String EXT_MP3 = "mp3";
	public static final String EXT_PLAYLIST = "m3u";
	public static final String EXT_OGG = "ogg";
	
	//event keys
	public static final String EVENT_EXIT ="exit";
	public static final String EVENT_OPEN_FILE ="open file";
	public static final String EVENT_REPEAT_MODE_STATUS_CHANGED="repeat status changed";
	public static final String EVENT_SHUFFLE_MODE_STATUS_CHANGED="shuffle status changed";
	public static final String EVENT_CONTINUE_MODE_STATUS_CHANGED="continue status changed";
	public static final String EVENT_INTRO_MODE_STATUS_CHANGED="intro status changed";
	public static final String EVENT_DEVICE_NEW="new device";
	public static final String EVENT_DEVICE_DELETE="delete device";
	public static final String EVENT_DEVICE_PROPERTIES="device properties";
	public static final String EVENT_DEVICE_MOUNT="mount device";
	public static final String EVENT_DEVICE_TEST="test device";
	public static final String EVENT_DEVICE_REFRESH="refresh device";
	public static final String EVENT_DEVICE_SYNCHRO="synchronize device";
	public static final String EVENT_VIEW_REFRESH_REQUEST="refresh view";
	public static final String EVENT_VIEW_CLOSE_REQUEST="close view";
	public static final String EVENT_VIEW_SHOW_REQUEST="show view";
	public static final String EVENT_VIEW_SHOW_STATUS_CHANGED_REQUEST="change status view";
	
	
		
	//	configuration keys
	 public static final String CONF_PERSPECTIVE_DEFAULT="jajuk.preference.perspective.default";
	 public static final String CONF_STATE_REPEAT="jajuk.state.mode.repeat";
	public static final String CONF_STATE_SHUFFLE="jajuk.state.mode.shuffle";
	public static final String CONF_STATE_CONTINUE="jajuk.state.mode.continue";
	public static final String CONF_STATE_INTRO="jajuk.state.mode.intro";
	public static final String CONF_ICON_REPEAT= "jajuk.state.ui.icon.repeat";
 	public static final String CONF_ICON_SHUFFLE= "jajuk.state.ui.icon.shuffle";
 	public static final String CONF_ICON_CONTINUE= "jajuk.state.ui.icon.continue";
 	public static final String CONF_ICON_INTRO= "jajuk.state.ui.icon.intro";
	public static final String CONF_STARTUP_FILE= "jajuk.startup.file";
	public static final String CONF_STARTUP_MODE= "jajuk.startup.mode";
	public static final String CONF_CONFIRMATIONS_DELETE_FILE= "jajuk.confirmations.delete_file";
	public static final String CONF_CONFIRMATIONS_EXIT= "jajuk.confirmations.exit";
	public static final String CONF_OPTIONS_HIDE_UNMOUNTED= "jajuk.options.hide_unmounted";
	public static final String CONF_OPTIONS_RESTART= "jajuk.options.restart";
	public static final String CONF_OPTIONS_LOG_LEVEL= "jajuk.options.log_level";
	public static final String CONF_OPTIONS_LANGUAGE= "jajuk.options.language";
	
	
	
 	
	//miscelanous
	public static final String TRUE= "true";
	public static final String FALSE= "false";
	
	
	//views identifiers 
	/** Identifier of the physical tree view */
	public static final String VIEW_PHYSICAL_TREE	= "VIEW_PHYSICAL_TREE";
	/** Identifier of the track list view */
	public static final String VIEW_TRACK_LIST		= "VIEW_TRACK_LIST";
	
	//Date format
	public static final String DATE_FILE = "dd/MM/yyyy";
	
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
	public static final String[] XML_RESERVED_ATTRIBUTE_NAMES = {"name","id","extension","player_impl","tag_impl","music",
			"type","url","album","style","author","length","year","added","rate","files","hits","directory","size",
			"quality","track","playlist_files","hashcode"};
		
	public static final String XML_PLAYLIST = "playlist";
	public static final String XML_PERSPECTIVES_CONF = 
		"<?xml version='1.0' encoding='UTF-8'?>\n"+
		 "<perspectives>\n"+
		"\t<perspective  class='"+PERSPECTIVE_NAME_PHYSICAL+"'>\n"+
		"\t\t<views>\n"+
	/*	"\t\t\t<view class='org.jajuk.ui.views.PhysicalTreeView' />\n"+
		"\t\t\t<view class='org.jajuk.ui.views.NavigationBarView' />\n"+
		"\t\t\t<view class='org.jajuk.ui.views.TrackListView' />\n"+
		"\t\t\t<view class='org.jajuk.ui.views.CoverView'  />\n"+
		"\t\t\t<view class='org.jajuk.ui.views.PlaylistRepositoryView'/>\n"+
		"\t\t\t<view class='org.jajuk.ui.views.PlaylistEditorView'  />\n"+*/
		"\t\t</views>\n"+
		"\t\t</perspective>\n"+
		"\t<perspective class='"+PERSPECTIVE_NAME_LOGICAL+"'>\n"+
		"\t\t<views>\n"+
	/*	"\t\t\t<view class='org.jajuk.ui.views.LogicalTreeView'/>\n"+
		"\t\t\t<view class='org.jajuk.ui.views.NavigationBarView' />\n"+
		"\t\t\t<view class='org.jajuk.ui.views.TrackListView'/>\n"+
		"\t\t\t<view class='org.jajuk.ui.views.CoverView'  />\n"+
		"\t\t\t<view class='org.jajuk.ui.views.PlaylistRepositoryView'/>\n"+
		"\t\t\t<view class='org.jajuk.ui.views.PlaylistEditorView'  />\n"+*/
		"\t\t</views>\n"+
		"\t</perspective>\n"+
		"\t<perspective class='"+PERSPECTIVE_NAME_CONFIGURATION+"'>\n"+
		"\t\t<views>\n"+
		"\t\t\t<view class='"+VIEW_NAME_PARAMETERS+"' width='50' height='100' x='0' y='0' />\n"+
		"\t\t\t<view class='"+VIEW_NAME_DEVICES+"' width='50' height='50' x='50' y='0' />\n"+
	//	"\t\t\t<view class='org.jajuk.ui.views.CdScanView'/>\n"+
		"\t\t</views>\n"+
		"\t</perspective>\n"+
		"\t<perspective class='"+PERSPECTIVE_NAME_STATISTICS+"'>\n"+
		"\t\t<views>\n"+
/*		"\t\t\t<view class='org.jajuk.ui.views.StatView'/>\n"+*/
		"\t\t</views>\n"+
		"\t</perspective>\n"+
		"\t<perspective  class='"+PERSPECTIVE_NAME_HELP+"'>\n"+
		"\t\t<views>\n"+
		/*"\t\t\t<view class='org.jajuk.ui.views.HelpView'/>\n"+
		"\t\t\t<view class='org.jajuk.ui.views.AboutView' />\n"+*/
		"\t\t</views>\n"+
		"\t</perspective>\n"+
		"</perspectives>";

	
}