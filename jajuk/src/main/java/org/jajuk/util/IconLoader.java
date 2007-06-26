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
 *  $Revision$
 */

package org.jajuk.util;


/**
 * Load icons from this class
 * <p>
 * Use: IconLoader.ICON_LOGO
 * </p>
 */
public class IconLoader {

	// icons

	public static final UrlImageIcon ICON_NO_COVER = new UrlImageIcon(Util
			.getResource("images/included/" + ITechnicalStrings.FILE_THUMB_NO_COVER));

	public static final UrlImageIcon ICON_LOGO = new UrlImageIcon(Util
			.getResource("icons/64x64/jajuk-icon_64x64.png"));

	public static final UrlImageIcon ICON_TRAY = new UrlImageIcon(Util
			.getResource("icons/22x22/jajuk-icon_22x22.png"));

	// Correctly displayed under JRE 1.6, ugly under Linux/JRE 1.5
	public static final UrlImageIcon ICON_LOGO_FRAME = new UrlImageIcon(Util
			.getResource("icons/16x16/jajuk-icon_16x16.png"));

	public static final UrlImageIcon ICON_REPEAT = new UrlImageIcon(Util
			.getResource("icons/16x16/repeat.png"));

	public static final UrlImageIcon ICON_SHUFFLE = new UrlImageIcon(Util
			.getResource("icons/16x16/shuffle.png"));

	public static final UrlImageIcon ICON_CONTINUE = new UrlImageIcon(Util
			.getResource("icons/16x16/continue.png"));

	public static final UrlImageIcon ICON_INTRO = new UrlImageIcon(Util
			.getResource("icons/16x16/intro.png"));

	public static final UrlImageIcon ICON_SHUFFLE_GLOBAL = new UrlImageIcon(Util
			.getResource("icons/32x32/shuffle_global.png"));

	public static final UrlImageIcon ICON_BESTOF = new UrlImageIcon(Util
			.getResource("icons/32x32/bestof.png"));

	public static final UrlImageIcon ICON_MUTED = new UrlImageIcon(Util
			.getResource("icons/32x32/mute.png"));

	public static final UrlImageIcon ICON_UNMUTED = new UrlImageIcon(Util
			.getResource("icons/32x32/unmute.png"));

	public static final UrlImageIcon ICON_NOVELTIES = new UrlImageIcon(Util
			.getResource("icons/32x32/novelties.png"));

	public static final UrlImageIcon ICON_NEXT = new UrlImageIcon(Util
			.getResource("icons/16x16/next.png"));
	
	public static final UrlImageIcon ICON_PREVIOUS = new UrlImageIcon(Util
			.getResource("icons/16x16/previous.png"));

	public static final UrlImageIcon ICON_PLAYER_PREVIOUS = new UrlImageIcon(Util
			.getResource("icons/32x32/previous.png"));

	public static final UrlImageIcon ICON_PLAYER_NEXT = new UrlImageIcon(Util
			.getResource("icons/32x32/next.png"));

	public static final UrlImageIcon ICON_REW = new UrlImageIcon(Util
			.getResource("icons/32x32/player_rew.png"));

	public static final UrlImageIcon ICON_PLAY = new UrlImageIcon(Util
			.getResource("icons/32x32/player_play.png"));

	public static final UrlImageIcon ICON_PAUSE = new UrlImageIcon(Util
			.getResource("icons/32x32/player_pause.png"));

	public static final UrlImageIcon ICON_INC_RATING = new UrlImageIcon(Util
			.getResource("icons/16x16/inc_rating.png"));

	public static final UrlImageIcon ICON_STOP = new UrlImageIcon(Util
			.getResource("icons/32x32/player_stop.png"));

	public static final UrlImageIcon ICON_FWD = new UrlImageIcon(Util
			.getResource("icons/32x32/player_fwd.png"));

	public static final UrlImageIcon ICON_VOLUME = new UrlImageIcon(Util
			.getResource("icons/16x16/volume.png"));

	public static final UrlImageIcon ICON_POSITION = new UrlImageIcon(Util
			.getResource("icons/16x16/position.png"));

	public static final UrlImageIcon ICON_INFO = new UrlImageIcon(Util
			.getResource("icons/16x16/info.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_SIMPLE = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_simple.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_PHYSICAL = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_physic.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_LOGICAL = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_logic.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_STATISTICS = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_stat.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_CONFIGURATION = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_configuration.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_PLAYER = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_player.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_CATALOG = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_catalog.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_INFORMATION = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_information.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_HELP = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_help.png"));

	public static final UrlImageIcon ICON_OPEN_FILE = new UrlImageIcon(Util
			.getResource("icons/16x16/fileopen.png"));

	public static final UrlImageIcon ICON_EXIT = new UrlImageIcon(Util
			.getResource("icons/16x16/exit.png"));

	public static final UrlImageIcon ICON_NEW = new UrlImageIcon(Util.getResource("icons/16x16/new.png"));

	public static final UrlImageIcon ICON_SEARCH = new UrlImageIcon(Util
			.getResource("icons/16x16/search.png"));

	public static final UrlImageIcon ICON_DELETE = new UrlImageIcon(Util
			.getResource("icons/16x16/delete.png"));

	public static final UrlImageIcon ICON_PROPERTIES = new UrlImageIcon(Util
			.getResource("icons/16x16/fileopen.png"));

	public static final UrlImageIcon ICON_VOID = new UrlImageIcon(Util
			.getResource("icons/16x16/void.png"));

	public static final UrlImageIcon ICON_CONFIGURATION = new UrlImageIcon(Util
			.getResource("icons/16x16/configure.png"));

	public static final UrlImageIcon ICON_MOUNT = new UrlImageIcon(Util
			.getResource("icons/16x16/mount.png"));

	public static final UrlImageIcon ICON_UNMOUNT = new UrlImageIcon(Util
			.getResource("icons/16x16/unmount.png"));

	public static final UrlImageIcon ICON_TRACES = new UrlImageIcon(Util
			.getResource("icons/16x16/properties.png"));

	public static final UrlImageIcon ICON_TEST = new UrlImageIcon(Util
			.getResource("icons/16x16/test.png"));

	public static final UrlImageIcon ICON_REFRESH = new UrlImageIcon(Util
			.getResource("icons/16x16/refresh.png"));

	public static final UrlImageIcon ICON_SYNCHRO = new UrlImageIcon(Util
			.getResource("icons/16x16/synchro.png"));

	public static final UrlImageIcon ICON_DEVICE_NEW = new UrlImageIcon(Util
			.getResource("icons/64x64/new.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/cdrom_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/cdrom_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_AUDIO_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/cdaudio_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_AUDIO_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/cdaudio_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_EXT_DD_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/ext_dd_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_EXT_DD_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/ext_dd_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_DIRECTORY_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/folder_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_DIRECTORY_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/folder_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_PLAYER_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/player_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_PLAYER_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/player_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_REMOTE_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/remote_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_REMOTE_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/remote_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_NETWORK_DRIVE_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/nfs_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/nfs_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/cdrom_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/cdrom_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_AUDIO_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/cdaudio_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_AUDIO_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/cdaudio_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_EXT_DD_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/ext_dd_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/ext_dd_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_NETWORK_DRIVE_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/nfs_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/nfs_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_DIRECTORY_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/folder_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/folder_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_PLAYER_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/player_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_PLAYER_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/player_unmount.png"));

	public static final UrlImageIcon ICON_DEVICE_REMOTE_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/remote_mount.png"));

	public static final UrlImageIcon ICON_DEVICE_REMOTE_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/remote_unmount.png"));

	public static final UrlImageIcon ICON_OK = new UrlImageIcon(Util.getResource("icons/22x22/ok.png"));

	public static final UrlImageIcon ICON_OK_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/ok.png"));

	public static final UrlImageIcon ICON_KO = new UrlImageIcon(Util.getResource("icons/22x22/ko.png"));

	public static final UrlImageIcon ICON_TRACK = new UrlImageIcon(Util
			.getResource("icons/16x16/track.png"));

	public static final UrlImageIcon ICON_DIRECTORY_SYNCHRO = new UrlImageIcon(Util
			.getResource("icons/16x16/dir_synchro.png"));

	public static final UrlImageIcon ICON_DIRECTORY_DESYNCHRO = new UrlImageIcon(Util
			.getResource("icons/16x16/dir_desynchro.png"));

	public static final UrlImageIcon ICON_PLAYLIST_FILE = new UrlImageIcon(Util
			.getResource("icons/16x16/playlist.png"));

	public static final UrlImageIcon ICON_STYLE = new UrlImageIcon(Util
			.getResource("icons/16x16/style.png"));

	public static final UrlImageIcon ICON_AUTHOR = new UrlImageIcon(Util
			.getResource("icons/16x16/author.png"));

	public static final UrlImageIcon ICON_ALBUM = new UrlImageIcon(Util
			.getResource("icons/16x16/album.png"));

	public static final UrlImageIcon ICON_YEAR = new UrlImageIcon(Util
			.getResource("icons/16x16/clock.png"));

	public static final UrlImageIcon ICON_APPLY_FILTER = new UrlImageIcon(Util
			.getResource("icons/16x16/filter.png"));

	public static final UrlImageIcon ICON_DISCOVERY_DATE = new UrlImageIcon(Util
			.getResource("icons/16x16/filter.png"));

	public static final UrlImageIcon ICON_CLEAR_FILTER = new UrlImageIcon(Util
			.getResource("icons/16x16/clear.png"));

	public static final UrlImageIcon ICON_ADVANCED_FILTER = new UrlImageIcon(Util
			.getResource("icons/16x16/complex_search.png"));

	public static final UrlImageIcon ICON_PLAYLIST_QUEUE = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_queue.png"));

	public static final UrlImageIcon ICON_PLAYLIST_QUEUE_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/playlist_queue.png"));

	public static final UrlImageIcon ICON_PLAYLIST_NORMAL = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_normal.png"));

	public static final UrlImageIcon ICON_PLAYLIST_NEW = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_new.png"));

	public static final UrlImageIcon ICON_PLAYLIST_NEW_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/playlist_new.png"));

	public static final UrlImageIcon ICON_PLAYLIST_BOOKMARK = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_bookmark.png"));

	public static final UrlImageIcon ICON_PLAYLIST_BOOKMARK_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/playlist_bookmark.png"));

	public static final UrlImageIcon ICON_PLAYLIST_BESTOF = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_bestof.png"));

	public static final UrlImageIcon ICON_PLAYLIST_NOVELTIES = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_novelties.png"));

	public static final UrlImageIcon ICON_RUN = new UrlImageIcon(Util
			.getResource("icons/16x16/player_play.png"));

	public static final UrlImageIcon ICON_ADD = new UrlImageIcon(Util.getResource("icons/16x16/add.png"));

	public static final UrlImageIcon ICON_REMOVE = new UrlImageIcon(Util
			.getResource("icons/16x16/remove.png"));

	public static final UrlImageIcon ICON_UP = new UrlImageIcon(Util.getResource("icons/16x16/up.png"));

	public static final UrlImageIcon ICON_DOWN = new UrlImageIcon(Util
			.getResource("icons/16x16/down.png"));

	public static final UrlImageIcon ICON_ADD_SHUFFLE = new UrlImageIcon(Util
			.getResource("icons/16x16/add_shuffle.png"));

	public static final UrlImageIcon ICON_CLEAR = new UrlImageIcon(Util
			.getResource("icons/16x16/clear.png"));

	public static final UrlImageIcon ICON_SAVE = new UrlImageIcon(Util
			.getResource("icons/16x16/save.png"));

	public static final UrlImageIcon ICON_SAVE_AS = new UrlImageIcon(Util
			.getResource("icons/16x16/saveas.png"));

	public static final UrlImageIcon ICON_DEFAULT_COVER = new UrlImageIcon(Util
			.getResource("icons/16x16/ok.png"));

	public static final UrlImageIcon ICON_FINISH_ALBUM = new UrlImageIcon(Util
			.getResource("icons/32x32/finish_album.png"));

	public static final UrlImageIcon ICON_NET_SEARCH = new UrlImageIcon(Util
			.getResource("icons/16x16/netsearch.png"));

	public static final UrlImageIcon ICON_TRACK_FIFO_PLANNED = new UrlImageIcon(Util
			.getResource("icons/16x16/planned.png"));

	public static final UrlImageIcon ICON_TRACK_FIFO_NORM = new UrlImageIcon(Util
			.getResource("icons/16x16/player_perspective.png"));

	public static final UrlImageIcon ICON_TRACK_FIFO_REPEAT = new UrlImageIcon(Util
			.getResource("icons/16x16/repeat.png"));

	public static final UrlImageIcon ICON_WIZARD = new UrlImageIcon(Util
			.getResource("icons/16x16/wizard.png"));

	public static final UrlImageIcon ICON_TYPE_MP3 = new UrlImageIcon(Util
			.getResource("icons/16x16/type_mp3.png"));

	public static final UrlImageIcon ICON_TYPE_MP2 = new UrlImageIcon(Util
			.getResource("icons/16x16/type_mp2.png"));

	public static final UrlImageIcon ICON_TYPE_OGG = new UrlImageIcon(Util
			.getResource("icons/16x16/type_ogg.png"));

	public static final UrlImageIcon ICON_TYPE_AU = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wav.png"));

	public static final UrlImageIcon ICON_TYPE_AIFF = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wav.png"));

	public static final UrlImageIcon ICON_TYPE_FLAC = new UrlImageIcon(Util
			.getResource("icons/16x16/type_flac.png"));

	public static final UrlImageIcon ICON_TYPE_MPC = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wav.png"));

	public static final UrlImageIcon ICON_TYPE_WMA = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wma.png"));

	public static final UrlImageIcon ICON_TYPE_APE = new UrlImageIcon(Util
			.getResource("icons/16x16/type_ape.png"));

	public static final UrlImageIcon ICON_TYPE_AAC = new UrlImageIcon(Util
			.getResource("icons/16x16/type_aac.png"));

	public static final UrlImageIcon ICON_TYPE_WAV = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wav.png"));

	public static final UrlImageIcon ICON_TYPE_RAM = new UrlImageIcon(Util
			.getResource("icons/16x16/type_ram.png"));

	public static final UrlImageIcon ICON_NO_EDIT = new UrlImageIcon(Util
			.getResource("icons/16x16/stop.png"));

	public static final UrlImageIcon ICON_EDIT = new UrlImageIcon(Util
			.getResource("icons/16x16/edit.png"));

	public static final UrlImageIcon ICON_UNKNOWN = new UrlImageIcon(Util
			.getResource("icons/16x16/presence_unknown.png"));

	public static final UrlImageIcon ICON_TIP = new UrlImageIcon(Util.getResource("icons/40x40/tip.png"));

	public static final UrlImageIcon ICON_TIP_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/tip.png"));

	public static final UrlImageIcon ICON_OPEN_DIR = new UrlImageIcon(Util
			.getResource("icons/40x40/folder_open.png"));

	public static final UrlImageIcon ICON_STAR_1 = new UrlImageIcon(Util
			.getResource("icons/16x16/star1.png"));

	public static final UrlImageIcon ICON_STAR_2 = new UrlImageIcon(Util
			.getResource("icons/16x16/star2.png"));

	public static final UrlImageIcon ICON_STAR_3 = new UrlImageIcon(Util
			.getResource("icons/16x16/star3.png"));

	public static final UrlImageIcon ICON_STAR_4 = new UrlImageIcon(Util
			.getResource("icons/16x16/star4.png"));

	public static final UrlImageIcon ICON_DROP_DOWN_32x32 = new UrlImageIcon(Util
			.getResource("icons/32x32/dropdown.png"));

	public static final UrlImageIcon ICON_DROP_DOWN_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/dropdown.png"));

	public static final UrlImageIcon ICON_DIGITAL_DJ = new UrlImageIcon(Util
			.getResource("icons/32x32/ddj.png"));
	
	public static final UrlImageIcon ICON_DIGITAL_DJ_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/ddj.png"));

	public static final UrlImageIcon ICON_LIST = new UrlImageIcon(Util
			.getResource("icons/16x16/contents.png"));

	public static final UrlImageIcon ICON_PLAY2 = new UrlImageIcon(Util
			.getResource("icons/16x16/play.png"));

	public static final UrlImageIcon ICON_DEFAULTS = new UrlImageIcon(Util
			.getResource("icons/16x16/undo.png"));

	public static final UrlImageIcon ICON_DEFAULTS_BIG = new UrlImageIcon(Util
			.getResource("icons/22x22/undo.png"));

	public static final UrlImageIcon ICON_HELP = new UrlImageIcon(Util
			.getResource("icons/16x16/help.png"));

	public static final UrlImageIcon ICON_ACCURACY_LOW = new UrlImageIcon(Util
			.getResource("icons/16x16/accuracy_low.png"));

	public static final UrlImageIcon ICON_ACCURACY_MEDIUM = new UrlImageIcon(Util
			.getResource("icons/16x16/accuracy_medium.png"));

	public static final UrlImageIcon ICON_ACCURACY_HIGH = new UrlImageIcon(Util
			.getResource("icons/16x16/accuracy_high.png"));

	public static final UrlImageIcon ICON_REPORT = new UrlImageIcon(Util
			.getResource("icons/16x16/report.png"));

	public static final UrlImageIcon ICON_PUSH = new UrlImageIcon(Util
			.getResource("icons/16x16/push.png"));
	
	public static final UrlImageIcon ICON_COPY = new UrlImageIcon(Util
			.getResource("icons/16x16/editcopy.png"));
	
	public static final UrlImageIcon ICON_LAUNCH = new UrlImageIcon(Util
			.getResource("icons/16x16/launch.png"));
}

