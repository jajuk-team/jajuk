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

	public static final UrlImageIcon ICON_NO_COVER = new UrlImageIcon(Util
			.getResource("images/included/" + ITechnicalStrings.FILE_THUMB_NO_COVER));

	public static final UrlImageIcon ICON_BACKGROUND = new UrlImageIcon(Util
			.getResource("images/included/" + ITechnicalStrings.FILE_BACKGROUND_IMAGE));

	public static final UrlImageIcon ICON_LOGO = new UrlImageIcon(Util
			.getResource("icons/64x64/jajuk-icon_64x64.png"));

	public static final UrlImageIcon ICON_TRAY = new UrlImageIcon(Util
			.getResource("icons/22x22/jajuk-icon_22x22.png"));
	
	public static final UrlImageIcon ICON_COVER_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/thumbnail_16x16.png"));
	
	// Correctly displayed under JRE 1.6, ugly under Linux/JRE 1.5
	public static final UrlImageIcon ICON_LOGO_FRAME = new UrlImageIcon(Util
			.getResource("icons/16x16/jajuk-icon_16x16.png"));

	public static final UrlImageIcon ICON_REPEAT = new UrlImageIcon(Util
			.getResource("icons/16x16/repeat_16x16.png"));

	public static final UrlImageIcon ICON_SHUFFLE = new UrlImageIcon(Util
			.getResource("icons/16x16/shuffle_16x16.png"));

	public static final UrlImageIcon ICON_CONTINUE = new UrlImageIcon(Util
			.getResource("icons/16x16/continue_16x16.png"));

	public static final UrlImageIcon ICON_INTRO = new UrlImageIcon(Util
			.getResource("icons/16x16/intro_16x16.png"));

	public static final UrlImageIcon ICON_SHUFFLE_GLOBAL = new UrlImageIcon(Util
			.getResource("icons/32x32/shuffle_global_32x32.png"));

	public static final UrlImageIcon ICON_BESTOF = new UrlImageIcon(Util
			.getResource("icons/32x32/bestof_32x32.png"));

	public static final UrlImageIcon ICON_BESTOF_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/bestof.png"));

	public static final UrlImageIcon ICON_MUTED = new UrlImageIcon(Util
			.getResource("icons/32x32/mute_32x32.png"));

	public static final UrlImageIcon ICON_WEBRADIO = new UrlImageIcon(Util
			.getResource("icons/32x32/webradio_32x32.png"));

	public static final UrlImageIcon ICON_UNMUTED = new UrlImageIcon(Util
			.getResource("icons/32x32/unmute_32x32.png"));

	public static final UrlImageIcon ICON_NOVELTIES = new UrlImageIcon(Util
			.getResource("icons/32x32/novelties_32x32.png"));
	
	public static final UrlImageIcon ICON_NOVELTIES_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/novelties.png"));

	public static final UrlImageIcon ICON_NEXT = new UrlImageIcon(Util
			.getResource("icons/16x16/next_16x16.png"));
	
	public static final UrlImageIcon ICON_PREVIOUS = new UrlImageIcon(Util
			.getResource("icons/16x16/previous_16x16.png"));

	public static final UrlImageIcon ICON_PLAYER_PREVIOUS = new UrlImageIcon(Util
			.getResource("icons/32x32/previous_32x32.png"));

	public static final UrlImageIcon ICON_PLAYER_NEXT = new UrlImageIcon(Util
			.getResource("icons/32x32/next_32x32.png"));

	public static final UrlImageIcon ICON_INC_RATING = new UrlImageIcon(Util
			.getResource("icons/16x16/inc_rating_16x16.png"));

	public static final UrlImageIcon ICON_REW = new UrlImageIcon(Util
			.getResource("icons/32x32/player_rew_32x32.png"));

	public static final UrlImageIcon ICON_REW_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/player_rew_16x16.png"));

	public static final UrlImageIcon ICON_PLAY = new UrlImageIcon(Util
			.getResource("icons/32x32/player_play_32x32.png"));

	public static final UrlImageIcon ICON_PLAY_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/player_play_16x16.png"));

	public static final UrlImageIcon ICON_PAUSE = new UrlImageIcon(Util
			.getResource("icons/32x32/player_pause_32x32.png"));

	public static final UrlImageIcon ICON_PAUSE_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/player_pause_16x16.png"));
	
	public static final UrlImageIcon ICON_STOP = new UrlImageIcon(Util
			.getResource("icons/32x32/player_stop_32x32.png"));

	public static final UrlImageIcon ICON_STOP_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/player_stop_16x16.png"));

	public static final UrlImageIcon ICON_FWD = new UrlImageIcon(Util
			.getResource("icons/32x32/player_fwd_32x32.png"));

	public static final UrlImageIcon ICON_FWD_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/player_fwd_16x16.png"));

	public static final UrlImageIcon ICON_NEXT_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/player_next_16x16.png"));

	public static final UrlImageIcon ICON_PREV_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/player_prev_16x16.png"));
	
	public static final UrlImageIcon ICON_VOLUME = new UrlImageIcon(Util
			.getResource("icons/16x16/volume_16x16.png"));

	public static final UrlImageIcon ICON_POSITION = new UrlImageIcon(Util
			.getResource("icons/16x16/position_16x16.png"));

	public static final UrlImageIcon ICON_INFO = new UrlImageIcon(Util
			.getResource("icons/16x16/info_16x16.png"));

	public static final UrlImageIcon ICON_BOOKMARK_FOLDERS = new UrlImageIcon(Util
			.getResource("icons/16x16/bookmark_folder2_16x16.png"));
	
	public static final UrlImageIcon ICON_PERSPECTIVE_SIMPLE = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_simple_40x40.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_PHYSICAL = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_physic_40x40.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_LOGICAL = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_logic_40x40.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_STATISTICS = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_stat_40x40.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_CONFIGURATION = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_configuration_40x40.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_PLAYER = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_player_40x40.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_CATALOG = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_catalog_40x40.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_INFORMATION = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_information_40x40.png"));

	public static final UrlImageIcon ICON_PERSPECTIVE_HELP = new UrlImageIcon(Util
			.getResource("icons/40x40/perspective_help_40x40.png"));

	public static final UrlImageIcon ICON_OPEN_FILE = new UrlImageIcon(Util
			.getResource("icons/16x16/fileopen_16x16.png"));

	public static final UrlImageIcon ICON_EXIT = new UrlImageIcon(Util
			.getResource("icons/16x16/exit_16x16.png"));

	public static final UrlImageIcon ICON_NEW = new UrlImageIcon(Util.getResource("icons/16x16/new.png"));

	public static final UrlImageIcon ICON_SEARCH = new UrlImageIcon(Util
			.getResource("icons/16x16/search_16x16.png"));

	public static final UrlImageIcon ICON_DELETE = new UrlImageIcon(Util
			.getResource("icons/16x16/delete_16x16.png"));

	public static final UrlImageIcon ICON_PROPERTIES = new UrlImageIcon(Util
			.getResource("icons/16x16/fileopen_16x16.png"));

	public static final UrlImageIcon ICON_VOID = new UrlImageIcon(Util
			.getResource("icons/16x16/void_16x16.png"));

	public static final UrlImageIcon ICON_CONFIGURATION = new UrlImageIcon(Util
			.getResource("icons/16x16/configure.png"));

	public static final UrlImageIcon ICON_MOUNT = new UrlImageIcon(Util
			.getResource("icons/16x16/mount_16x16.png"));

	public static final UrlImageIcon ICON_UNMOUNT = new UrlImageIcon(Util
			.getResource("icons/16x16/unmount_16x16.png"));

	public static final UrlImageIcon ICON_TRACES = new UrlImageIcon(Util
			.getResource("icons/16x16/properties_16x16.png"));

	public static final UrlImageIcon ICON_TEST = new UrlImageIcon(Util
			.getResource("icons/16x16/test_16x16.png"));

	public static final UrlImageIcon ICON_REORGANIZE = new UrlImageIcon(Util
			.getResource("icons/16x16/reorganize_16x16.png"));

	public static final UrlImageIcon ICON_REFRESH = new UrlImageIcon(Util
			.getResource("icons/16x16/refresh_16x16.png"));

	public static final UrlImageIcon ICON_RESTORE_ALL_VIEWS = new UrlImageIcon(Util
			.getResource("icons/16x16/refresh_all_16x16.png"));

	public static final UrlImageIcon ICON_SYNCHRO = new UrlImageIcon(Util
			.getResource("icons/16x16/synchro_16x16.png"));

	public static final UrlImageIcon ICON_DEVICE_NEW = new UrlImageIcon(Util
			.getResource("icons/64x64/new_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/cdrom_mount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/cdrom_unmount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_AUDIO_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/cdaudio_mount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_AUDIO_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/cdaudio_unmount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_EXT_DD_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/ext_dd_mount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_EXT_DD_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/ext_dd_unmount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_DIRECTORY_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/folder_mount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_DIRECTORY_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/folder_unmount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_PLAYER_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/player_mount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_PLAYER_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/player_unmount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_REMOTE_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/remote_mount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_REMOTE_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/remote_unmount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_NETWORK_DRIVE_MOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/nfs_mount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED = new UrlImageIcon(Util
			.getResource("icons/64x64/nfs_unmount_64x64.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/cdrom_mount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/cdrom_unmount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_AUDIO_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/cdaudio_mount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_CD_AUDIO_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/cdaudio_unmount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_EXT_DD_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/ext_dd_mount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/ext_dd_unmount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_NETWORK_DRIVE_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/nfs_mount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/nfs_unmount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_DIRECTORY_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/folder_mount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/folder_unmount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_PLAYER_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/player_mount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_PLAYER_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/player_unmount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_REMOTE_MOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/remote_mount_22x22.png"));

	public static final UrlImageIcon ICON_DEVICE_REMOTE_UNMOUNTED_SMALL = new UrlImageIcon(Util
			.getResource("icons/22x22/remote_unmount_22x22.png"));

	public static final UrlImageIcon ICON_OK = new UrlImageIcon(Util.getResource("icons/22x22/ok_22x22.png"));

	public static final UrlImageIcon ICON_OK_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/ok_16x16.png"));

	public static final UrlImageIcon ICON_KO = new UrlImageIcon(Util.getResource("icons/22x22/ko_22x22.png"));

	public static final UrlImageIcon ICON_TRACK = new UrlImageIcon(Util
			.getResource("icons/16x16/track_16x16.png"));

	public static final UrlImageIcon ICON_DIRECTORY_SYNCHRO = new UrlImageIcon(Util
			.getResource("icons/16x16/dir_synchro_16x16.png"));

	public static final UrlImageIcon ICON_DIRECTORY_DESYNCHRO = new UrlImageIcon(Util
			.getResource("icons/16x16/dir_desynchro_16x16.png"));

	public static final UrlImageIcon ICON_PLAYLIST_FILE = new UrlImageIcon(Util
			.getResource("icons/16x16/playlist.png"));

	public static final UrlImageIcon ICON_STYLE = new UrlImageIcon(Util
			.getResource("icons/16x16/style_16x16.png"));

	public static final UrlImageIcon ICON_EMPTY = new UrlImageIcon(Util
			.getResource("icons/16x16/empty_16x16.png"));

	public static final UrlImageIcon ICON_AUTHOR = new UrlImageIcon(Util
			.getResource("icons/16x16/author_16x16.png"));

	public static final UrlImageIcon ICON_ALBUM = new UrlImageIcon(Util
			.getResource("icons/16x16/album_16x16.png"));

	public static final UrlImageIcon ICON_YEAR = new UrlImageIcon(Util
			.getResource("icons/16x16/clock_16x16.png"));

	public static final UrlImageIcon ICON_APPLY_FILTER = new UrlImageIcon(Util
			.getResource("icons/16x16/filter_16x16.png"));

	public static final UrlImageIcon ICON_DISCOVERY_DATE = new UrlImageIcon(Util
			.getResource("icons/16x16/filter_16x16.png"));

	public static final UrlImageIcon ICON_CLEAR_FILTER = new UrlImageIcon(Util
			.getResource("icons/16x16/clear_16x16.png"));

	public static final UrlImageIcon ICON_ADVANCED_FILTER = new UrlImageIcon(Util
			.getResource("icons/16x16/complex_search_16x16.png"));

	public static final UrlImageIcon ICON_PLAYLIST_QUEUE = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_queue_40x40.png"));

	public static final UrlImageIcon ICON_PLAYLIST_QUEUE_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/playlist_queue_16x16.png"));

	public static final UrlImageIcon ICON_PLAYLIST_NORMAL = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_normal_40x40.png"));

	public static final UrlImageIcon ICON_PLAYLIST_NEW = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_new_40x40.png"));

	public static final UrlImageIcon ICON_PLAYLIST_NEW_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/playlist_new_16x16.png"));

	public static final UrlImageIcon ICON_PLAYLIST_BOOKMARK = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_bookmark_40x40.png"));

	public static final UrlImageIcon ICON_PLAYLIST_BOOKMARK_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/playlist_bookmark_16x16.png"));

	public static final UrlImageIcon ICON_PLAYLIST_BESTOF = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_bestof_40x40.png"));

	public static final UrlImageIcon ICON_PLAYLIST_NOVELTIES = new UrlImageIcon(Util
			.getResource("icons/40x40/playlist_novelties_40x40.png"));

	public static final UrlImageIcon ICON_RUN = new UrlImageIcon(Util
			.getResource("icons/16x16/player_play_16x16.png"));

	public static final UrlImageIcon ICON_ADD = new UrlImageIcon(Util.getResource("icons/16x16/add_16x16.png"));

	public static final UrlImageIcon ICON_REMOVE = new UrlImageIcon(Util
			.getResource("icons/16x16/remove_16x16.png"));

	public static final UrlImageIcon ICON_UP = new UrlImageIcon(Util.getResource("icons/16x16/up_16x16.png"));

	public static final UrlImageIcon ICON_DOWN = new UrlImageIcon(Util
			.getResource("icons/16x16/down_16x16.png"));

	public static final UrlImageIcon ICON_ADD_SHUFFLE = new UrlImageIcon(Util
			.getResource("icons/16x16/add_shuffle_16x16.png"));

	public static final UrlImageIcon ICON_CLEAR = new UrlImageIcon(Util
			.getResource("icons/16x16/clear_16x16.png"));

	public static final UrlImageIcon ICON_SAVE = new UrlImageIcon(Util
			.getResource("icons/16x16/save_16x16.png"));

	public static final UrlImageIcon ICON_SAVE_AS = new UrlImageIcon(Util
			.getResource("icons/16x16/saveas.png"));

	public static final UrlImageIcon ICON_DEFAULT_COVER = new UrlImageIcon(Util
			.getResource("icons/16x16/ok_16x16.png"));

	public static final UrlImageIcon ICON_FINISH_ALBUM = new UrlImageIcon(Util
			.getResource("icons/32x32/finish_album_32x32.png"));

	public static final UrlImageIcon ICON_NET_SEARCH = new UrlImageIcon(Util
			.getResource("icons/16x16/netsearch_16x16.png"));

	public static final UrlImageIcon ICON_TRACK_FIFO_PLANNED = new UrlImageIcon(Util
			.getResource("icons/16x16/planned_16x16.png"));

	public static final UrlImageIcon ICON_TRACK_FIFO_NORM = new UrlImageIcon(Util
			.getResource("icons/16x16/player_perspective_16x16.png"));

	public static final UrlImageIcon ICON_TRACK_FIFO_REPEAT = new UrlImageIcon(Util
			.getResource("icons/16x16/repeat_16x16.png"));

	public static final UrlImageIcon ICON_WIZARD = new UrlImageIcon(Util
			.getResource("icons/16x16/wizard_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_MP3 = new UrlImageIcon(Util
			.getResource("icons/16x16/type_mp3_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_MP2 = new UrlImageIcon(Util
			.getResource("icons/16x16/type_mp2_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_OGG = new UrlImageIcon(Util
			.getResource("icons/16x16/type_ogg_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_AU = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wav_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_AIFF = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wav_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_FLAC = new UrlImageIcon(Util
			.getResource("icons/16x16/type_flac_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_MPC = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wav_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_WMA = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wma_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_APE = new UrlImageIcon(Util
			.getResource("icons/16x16/type_ape_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_AAC = new UrlImageIcon(Util
			.getResource("icons/16x16/type_aac_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_WAV = new UrlImageIcon(Util
			.getResource("icons/16x16/type_wav_16x16.png"));

	public static final UrlImageIcon ICON_TYPE_RAM = new UrlImageIcon(Util
			.getResource("icons/16x16/type_ram_16x16.png"));

	public static final UrlImageIcon ICON_NO_EDIT = new UrlImageIcon(Util
			.getResource("icons/16x16/stop_16x16.png"));

	public static final UrlImageIcon ICON_EDIT = new UrlImageIcon(Util
			.getResource("icons/16x16/edit_16x16.png"));

	public static final UrlImageIcon ICON_UNKNOWN = new UrlImageIcon(Util
			.getResource("icons/16x16/presence_unknown_16x16.png"));

	public static final UrlImageIcon ICON_TIP = new UrlImageIcon(Util.getResource("icons/40x40/tip_40x40.png"));

	public static final UrlImageIcon ICON_TIP_SMALL = new UrlImageIcon(Util
			.getResource("icons/16x16/tip_16x16.png"));

	public static final UrlImageIcon ICON_OPEN_DIR = new UrlImageIcon(Util
			.getResource("icons/40x40/folder_open_40x40.png"));

	public static final UrlImageIcon ICON_STAR_1 = new UrlImageIcon(Util
			.getResource("icons/16x16/star1_16x16.png"));

	public static final UrlImageIcon ICON_STAR_2 = new UrlImageIcon(Util
			.getResource("icons/16x16/star2_16x16.png"));

	public static final UrlImageIcon ICON_STAR_3 = new UrlImageIcon(Util
			.getResource("icons/16x16/star3_16x16.png"));

	public static final UrlImageIcon ICON_STAR_4 = new UrlImageIcon(Util
			.getResource("icons/16x16/star4_16x16.png"));

	public static final UrlImageIcon ICON_DROP_DOWN_32x32 = new UrlImageIcon(Util
			.getResource("icons/32x32/dropdown_32x32.png"));

	public static final UrlImageIcon ICON_DROP_DOWN_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/dropdown_16x16.png"));

	public static final UrlImageIcon ICON_DIGITAL_DJ = new UrlImageIcon(Util
			.getResource("icons/32x32/ddj_32x32.png"));
	
	public static final UrlImageIcon ICON_DIGITAL_DJ_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/ddj_16x16.png"));

	public static final UrlImageIcon ICON_WEBRADIO_16x16 = new UrlImageIcon(Util
			.getResource("icons/16x16/webradio_16x16.png"));

	public static final UrlImageIcon ICON_LIST = new UrlImageIcon(Util
			.getResource("icons/16x16/contents_16x16.png"));

	public static final UrlImageIcon ICON_PLAY_TABLE = new UrlImageIcon(Util
			.getResource("icons/16x16/play_table_16x16.png"));

	public static final UrlImageIcon ICON_DEFAULTS = new UrlImageIcon(Util
			.getResource("icons/16x16/undo_16x16.png"));

	public static final UrlImageIcon ICON_DEFAULTS_BIG = new UrlImageIcon(Util
			.getResource("icons/22x22/undo_22x22.png"));

	public static final UrlImageIcon ICON_HELP = new UrlImageIcon(Util
			.getResource("icons/16x16/help_16x16.png"));

	public static final UrlImageIcon ICON_ACCURACY_LOW = new UrlImageIcon(Util
			.getResource("icons/16x16/accuracy_low_16x16.png"));

	public static final UrlImageIcon ICON_ACCURACY_MEDIUM = new UrlImageIcon(Util
			.getResource("icons/16x16/accuracy_medium_16x16.png"));

	public static final UrlImageIcon ICON_ACCURACY_HIGH = new UrlImageIcon(Util
			.getResource("icons/16x16/accuracy_high_16x16.png"));

	public static final UrlImageIcon ICON_REPORT = new UrlImageIcon(Util
			.getResource("icons/16x16/report_16x16.png"));

	public static final UrlImageIcon ICON_PUSH = new UrlImageIcon(Util
			.getResource("icons/16x16/push_16x16.png"));
	
	public static final UrlImageIcon ICON_COPY = new UrlImageIcon(Util
			.getResource("icons/16x16/editcopy_16x16.png"));
	
	public static final UrlImageIcon ICON_LAUNCH = new UrlImageIcon(Util
			.getResource("icons/16x16/launch_16x16.png"));
}

