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
 * $Log$
 * Revision 1.2  2003/10/24 15:44:25  bflorat
 * 24/10/2003
 *
 * Revision 1.1  2003/10/21 20:43:06  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 * Revision 1.6  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 * Revision 1.5  2003/10/17 20:36:45  bflorat
 * 17/10/2003
 *
 * Revision 1.4  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 * Revision 1.3  2003/10/10 22:32:13  bflorat
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/09 21:14:13  bflorat
 * new keys and suppressed hard path
 *
 * Revision 1.1  2003/10/07 21:02:22  bflorat
 * Initial commit
 *
 */
package org.jajuk.base;

import java.util.Locale;

/**
 *  Contains all technical/ non-translatable strings
 *
 * @author     bflorat
 * @created    5 oct. 2003
 */
public interface ITechnicalStrings {
	
		// directory path
	public static final String PATH_ICONS = System.getProperty("user.dir")+"/dist-files/icons/";
	
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
	public static final String ICON_PERSPECTIVE_PHYSICAL			= PATH_ICONS + "16x16/physical_perspective.png";
	public static final String ICON_PERSPECTIVE_LOGICAL			= PATH_ICONS + "16x16/logical_perspective.png";
	public static final String ICON_INFO	= PATH_ICONS + "16x16/info.png";
	public static final String ICON_PERSPECTIVE_STATISTICS		= PATH_ICONS + "16x16/percent.png";
	public static final String ICON_PERSPECTIVE_CONFIGURATION	= PATH_ICONS + "16x16/configure.png";
	public static final String ICON_OPEN_FILE	= PATH_ICONS + "16x16/fileopen.png";
	public static final String ICON_EXIT=  PATH_ICONS + "16x16/exit.png";
	public static final String ICON_NEW=  PATH_ICONS + "16x16/new.png";
	public static final String ICON_DELETE=  PATH_ICONS + "16x16/delete.png";

	//logs
	public static final String LOG_PATTERN="%d{HH:mm:ss} [%p] %m\n";
	public static final String LOG_FILE_SIZE="1MB";

	//files
	public static final String FILE_JAJUK_DIR = System.getProperty("user.home")+"/.jajuk";
	public static final String FILE_LOG = System.getProperty("user.home")+"/.jajuk/jajuk.log";
	public static final String FILE_COLLECTION = System.getProperty("user.home")+"/.jajuk/collection.xml";
	
	
	//players impls
	public static final String PLAYER_IMPL_JAVALAYER= "org.jajuk.players.JavaLayerPlayerImpl";
	
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
	
	//	configuration keys
	 public static final String CONF_VIEW_PHYSICAL="jajuk.preference.perspective.physical.views";
	 public static final String CONF_PERSPECTIVE_DEFAULT="jajuk.preference.perspective.default";
	 public static final String CONF_STATE_REPEAT="jajuk.state.mode.repeat";
	public static final String CONF_STATE_SHUFFLE="jajuk.state.mode.shuffle";
	public static final String CONF_STATE_CONTINUE="jajuk.state.mode.continue";
	public static final String CONF_STATE_INTRO="jajuk.state.mode.intro";
	 public static final String CONF_ICON_REPEAT= "jajuk.state.ui.icon.repeat";
 	public static final String CONF_ICON_SHUFFLE= "jajuk.state.ui.icon.shuffle";
 	public static final String CONF_ICON_CONTINUE= "jajuk.state.ui.icon.continue";
 	public static final String CONF_ICON_INTRO= "jajuk.state.ui.icon.intro";
	
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
	public static final String XML_PLAYLIST = "playlist";
	
}