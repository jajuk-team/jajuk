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

import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * Load icons from this class
 * <p>
 * Use: IconLoader.ICON_LOGO
 * </p>
 */
public class IconLoader implements ITechnicalStrings {

  /** No covers image cache : size:default icon */
  public static final HashMap<String, ImageIcon> noCoversCache = new HashMap<String, ImageIcon>(10);

  public static final ImageIcon ICON_NO_COVER = new ImageIcon(Util.getResource("images/included/"
      + ITechnicalStrings.FILE_THUMB_NO_COVER));

  public static final ImageIcon ICON_LOGO = new ImageIcon(Util
      .getResource("icons/64x64/jajuk-icon_64x64.png"));

  public static final ImageIcon ICON_TRAY = new ImageIcon(Util
      .getResource("icons/22x22/jajuk-icon_22x22.png"));

  public static final ImageIcon ICON_COVER_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/thumbnail_16x16.png"));

  // Correctly displayed under JRE 1.6, ugly under Linux/JRE 1.5
  public static final ImageIcon ICON_LOGO_FRAME = new ImageIcon(Util
      .getResource("icons/16x16/jajuk-icon_16x16.png"));

  public static final ImageIcon ICON_REPEAT = new ImageIcon(Util
      .getResource("icons/16x16/repeat_16x16.png"));

  public static final ImageIcon ICON_SHUFFLE = new ImageIcon(Util
      .getResource("icons/16x16/shuffle_16x16.png"));

  public static final ImageIcon ICON_CONTINUE = new ImageIcon(Util
      .getResource("icons/16x16/continue_16x16.png"));

  public static final ImageIcon ICON_INTRO = new ImageIcon(Util
      .getResource("icons/16x16/intro_16x16.png"));

  public static final ImageIcon ICON_SHUFFLE_GLOBAL = new ImageIcon(Util
      .getResource("icons/32x32/shuffle_global_32x32.png"));

  public static final ImageIcon ICON_BESTOF = new ImageIcon(Util
      .getResource("icons/32x32/bestof_32x32.png"));

  public static final ImageIcon ICON_BESTOF_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/bestof_16x16.png"));

  public static final ImageIcon ICON_MUTED = new ImageIcon(Util
      .getResource("icons/32x32/mute_32x32.png"));

  public static final ImageIcon ICON_WEBRADIO = new ImageIcon(Util
      .getResource("icons/32x32/webradio_32x32.png"));

  public static final ImageIcon ICON_UNMUTED = new ImageIcon(Util
      .getResource("icons/32x32/unmute_32x32.png"));

  public static final ImageIcon ICON_NOVELTIES = new ImageIcon(Util
      .getResource("icons/32x32/novelties_32x32.png"));

  public static final ImageIcon ICON_NOVELTIES_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/novelties_16x16.png"));

  public static final ImageIcon ICON_NEXT = new ImageIcon(Util
      .getResource("icons/16x16/next_16x16.png"));

  public static final ImageIcon ICON_PREVIOUS = new ImageIcon(Util
      .getResource("icons/16x16/previous_16x16.png"));

  public static final ImageIcon ICON_PLAYER_PREVIOUS = new ImageIcon(Util
      .getResource("icons/32x32/previous_32x32.png"));

  public static final ImageIcon ICON_PLAYER_NEXT = new ImageIcon(Util
      .getResource("icons/32x32/next_32x32.png"));

  public static final ImageIcon ICON_INC_RATING = new ImageIcon(Util
      .getResource("icons/16x16/inc_rating_16x16.png"));

  public static final ImageIcon ICON_REW = new ImageIcon(Util
      .getResource("icons/32x32/player_rew_32x32.png"));

  public static final ImageIcon ICON_REW_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/player_rew_16x16.png"));

  public static final ImageIcon ICON_PLAY = new ImageIcon(Util
      .getResource("icons/32x32/player_play_32x32.png"));

  public static final ImageIcon ICON_PLAY_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/player_play_16x16.png"));

  public static final ImageIcon ICON_PAUSE = new ImageIcon(Util
      .getResource("icons/32x32/player_pause_32x32.png"));

  public static final ImageIcon ICON_PAUSE_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/player_pause_16x16.png"));

  public static final ImageIcon ICON_STOP = new ImageIcon(Util
      .getResource("icons/32x32/player_stop_32x32.png"));

  public static final ImageIcon ICON_STOP_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/player_stop_16x16.png"));

  public static final ImageIcon ICON_FWD = new ImageIcon(Util
      .getResource("icons/32x32/player_fwd_32x32.png"));

  public static final ImageIcon ICON_FWD_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/player_fwd_16x16.png"));

  public static final ImageIcon ICON_VOLUME = new ImageIcon(Util
      .getResource("icons/16x16/volume_16x16.png"));

  public static final ImageIcon ICON_POSITION = new ImageIcon(Util
      .getResource("icons/16x16/position_16x16.png"));

  public static final ImageIcon ICON_INFO = new ImageIcon(Util
      .getResource("icons/16x16/info_16x16.png"));

  public static final ImageIcon ICON_BOOKMARK_FOLDERS = new ImageIcon(Util
      .getResource("icons/16x16/bookmark_16x16.png"));

  public static final ImageIcon ICON_PERSPECTIVE_SIMPLE = new ImageIcon(Util
      .getResource("icons/40x40/perspective_simple_40x40.png"));

  public static final ImageIcon ICON_PERSPECTIVE_PHYSICAL = new ImageIcon(Util
      .getResource("icons/40x40/perspective_physic_40x40.png"));

  public static final ImageIcon ICON_PERSPECTIVE_LOGICAL = new ImageIcon(Util
      .getResource("icons/40x40/perspective_logic_40x40.png"));

  public static final ImageIcon ICON_PERSPECTIVE_STATISTICS = new ImageIcon(Util
      .getResource("icons/40x40/perspective_stat_40x40.png"));

  public static final ImageIcon ICON_PERSPECTIVE_CONFIGURATION = new ImageIcon(Util
      .getResource("icons/40x40/perspective_configuration_40x40.png"));

  public static final ImageIcon ICON_PERSPECTIVE_PLAYER = new ImageIcon(Util
      .getResource("icons/40x40/perspective_player_40x40.png"));

  public static final ImageIcon ICON_PERSPECTIVE_CATALOG = new ImageIcon(Util
      .getResource("icons/40x40/perspective_catalog_40x40.png"));

  public static final ImageIcon ICON_PERSPECTIVE_INFORMATION = new ImageIcon(Util
      .getResource("icons/40x40/perspective_information_40x40.png"));

  public static final ImageIcon ICON_OPEN_FILE = new ImageIcon(Util
      .getResource("icons/16x16/fileopen_16x16.png"));

  public static final ImageIcon ICON_EXIT = new ImageIcon(Util
      .getResource("icons/16x16/exit_16x16.png"));

  public static final ImageIcon ICON_NEW = new ImageIcon(Util
      .getResource("icons/16x16/new_16x16.png"));

  public static final ImageIcon ICON_SEARCH = new ImageIcon(Util
      .getResource("icons/16x16/search_16x16.png"));

  public static final ImageIcon ICON_DELETE = new ImageIcon(Util
      .getResource("icons/16x16/delete_16x16.png"));

  public static final ImageIcon ICON_PROPERTIES = new ImageIcon(Util
      .getResource("icons/16x16/properties_16x16.png"));

  public static final ImageIcon ICON_VOID = new ImageIcon(Util
      .getResource("icons/16x16/void_16x16.png"));

  public static final ImageIcon ICON_CONFIGURATION = new ImageIcon(Util
      .getResource("icons/16x16/configure_16x16.png"));

  public static final ImageIcon ICON_MOUNT = new ImageIcon(Util
      .getResource("icons/16x16/mount_16x16.png"));

  public static final ImageIcon ICON_UPDATE_MANAGER = new ImageIcon(Util
      .getResource("icons/16x16/update_manager_16x16.png"));

  public static final ImageIcon ICON_UNMOUNT = new ImageIcon(Util
      .getResource("icons/16x16/unmount_16x16.png"));

  public static final ImageIcon ICON_TRACES = new ImageIcon(Util
      .getResource("icons/16x16/traces_16x16.png"));

  public static final ImageIcon ICON_TEST = new ImageIcon(Util
      .getResource("icons/16x16/test_16x16.png"));

  public static final ImageIcon ICON_REORGANIZE = new ImageIcon(Util
      .getResource("icons/16x16/reorganize_16x16.png"));

  public static final ImageIcon ICON_REFRESH = new ImageIcon(Util
      .getResource("icons/16x16/refresh_16x16.png"));

  public static final ImageIcon ICON_RESTORE_ALL_VIEWS = new ImageIcon(Util
      .getResource("icons/16x16/refresh_all_16x16.png"));

  public static final ImageIcon ICON_SYNCHRO = new ImageIcon(Util
      .getResource("icons/16x16/synchro_16x16.png"));

  public static final ImageIcon ICON_DEVICE_NEW = new ImageIcon(Util
      .getResource("icons/64x64/new_64x64.png"));

  public static final ImageIcon ICON_DEVICE_CD_MOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/cdrom_mount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_CD_UNMOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/cdrom_unmount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_EXT_DD_MOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/ext_dd_mount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_EXT_DD_UNMOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/ext_dd_unmount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_DIRECTORY_MOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/folder_mount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_DIRECTORY_UNMOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/folder_unmount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_PLAYER_MOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/player_mount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_PLAYER_UNMOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/player_unmount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_NETWORK_DRIVE_MOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/nfs_mount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED = new ImageIcon(Util
      .getResource("icons/64x64/nfs_unmount_64x64.png"));

  public static final ImageIcon ICON_DEVICE_CD_MOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/cdrom_mount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_CD_UNMOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/cdrom_unmount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_EXT_DD_MOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/ext_dd_mount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/ext_dd_unmount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_NETWORK_DRIVE_MOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/nfs_mount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/nfs_unmount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_DIRECTORY_MOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/folder_mount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/folder_unmount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_PLAYER_MOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/player_mount_22x22.png"));

  public static final ImageIcon ICON_DEVICE_PLAYER_UNMOUNTED_SMALL = new ImageIcon(Util
      .getResource("icons/22x22/player_unmount_22x22.png"));

  public static final ImageIcon ICON_OK = new ImageIcon(Util
      .getResource("icons/22x22/ok_22x22.png"));

  public static final ImageIcon ICON_OK_SMALL = new ImageIcon(Util
      .getResource("icons/16x16/ok_16x16.png"));

  public static final ImageIcon ICON_KO = new ImageIcon(Util
      .getResource("icons/22x22/ko_22x22.png"));

  public static final ImageIcon ICON_TRACK = new ImageIcon(Util
      .getResource("icons/16x16/track_16x16.png"));

  public static final ImageIcon ICON_DIRECTORY_SYNCHRO = new ImageIcon(Util
      .getResource("icons/16x16/dir_synchro_16x16.png"));

  public static final ImageIcon ICON_DIRECTORY_DESYNCHRO = new ImageIcon(Util
      .getResource("icons/16x16/dir_desynchro_16x16.png"));

  public static final ImageIcon ICON_PLAYLIST_FILE = new ImageIcon(Util
      .getResource("icons/16x16/playlist_16x16.png"));

  public static final ImageIcon ICON_STYLE = new ImageIcon(Util
      .getResource("icons/16x16/style_16x16.png"));

  public static final ImageIcon ICON_EMPTY = new ImageIcon(Util
      .getResource("icons/16x16/empty_16x16.png"));

  public static final ImageIcon ICON_AUTHOR = new ImageIcon(Util
      .getResource("icons/16x16/author_16x16.png"));

  public static final ImageIcon ICON_ALBUM = new ImageIcon(Util
      .getResource("icons/16x16/album_16x16.png"));

  public static final ImageIcon ICON_YEAR = new ImageIcon(Util
      .getResource("icons/16x16/clock_16x16.png"));

  public static final ImageIcon ICON_APPLY_FILTER = new ImageIcon(Util
      .getResource("icons/16x16/filter_16x16.png"));

  public static final ImageIcon ICON_DISCOVERY_DATE = new ImageIcon(Util
      .getResource("icons/16x16/calendar_16x16.png"));

  public static final ImageIcon ICON_CLEAR_FILTER = new ImageIcon(Util
      .getResource("icons/16x16/clear_16x16.png"));

  public static final ImageIcon ICON_ADVANCED_FILTER = new ImageIcon(Util
      .getResource("icons/16x16/complex_search_16x16.png"));

  public static final ImageIcon ICON_PLAYLIST_QUEUE = new ImageIcon(Util
      .getResource("icons/40x40/playlist_queue_40x40.png"));

  public static final ImageIcon ICON_PLAYLIST_QUEUE_SMALL = new ImageIcon(Util
      .getResource("icons/16x16/playlist_queue_16x16.png"));

  public static final ImageIcon ICON_PLAYLIST_NORMAL = new ImageIcon(Util
      .getResource("icons/40x40/playlist_normal_40x40.png"));

  public static final ImageIcon ICON_PLAYLIST_NEW = new ImageIcon(Util
      .getResource("icons/40x40/playlist_new_40x40.png"));

  public static final ImageIcon ICON_PLAYLIST_NEW_SMALL = new ImageIcon(Util
      .getResource("icons/16x16/new_16x16.png"));

  public static final ImageIcon ICON_PLAYLIST_BOOKMARK = new ImageIcon(Util
      .getResource("icons/40x40/bookmark_40x40.png"));

  public static final ImageIcon ICON_PLAYLIST_BOOKMARK_SMALL = new ImageIcon(Util
      .getResource("icons/16x16/bookmark_16x16.png"));

  public static final ImageIcon ICON_PLAYLIST_BESTOF = new ImageIcon(Util
      .getResource("icons/40x40/playlist_bestof_40x40.png"));

  public static final ImageIcon ICON_PLAYLIST_NOVELTIES = new ImageIcon(Util
      .getResource("icons/40x40/playlist_novelties_40x40.png"));

  public static final ImageIcon ICON_RUN = new ImageIcon(Util
      .getResource("icons/16x16/player_play_16x16.png"));

  public static final ImageIcon ICON_ADD = new ImageIcon(Util
      .getResource("icons/16x16/add_16x16.png"));

  public static final ImageIcon ICON_REMOVE = new ImageIcon(Util
      .getResource("icons/16x16/remove_16x16.png"));

  public static final ImageIcon ICON_UP = new ImageIcon(Util
      .getResource("icons/16x16/up_16x16.png"));

  public static final ImageIcon ICON_DOWN = new ImageIcon(Util
      .getResource("icons/16x16/down_16x16.png"));

  public static final ImageIcon ICON_ADD_SHUFFLE = new ImageIcon(Util
      .getResource("icons/16x16/add_shuffle_16x16.png"));

  public static final ImageIcon ICON_CLEAR = new ImageIcon(Util
      .getResource("icons/16x16/clear_16x16.png"));

  public static final ImageIcon ICON_SAVE = new ImageIcon(Util
      .getResource("icons/16x16/save_16x16.png"));

  public static final ImageIcon ICON_EXT_DRIVE = new ImageIcon(Util
      .getResource("icons/16x16/ext_drive_16x16.png"));

  public static final ImageIcon ICON_DEFAULT_COVER = new ImageIcon(Util
      .getResource("icons/16x16/ok_16x16.png"));

  public static final ImageIcon ICON_FINISH_ALBUM = new ImageIcon(Util
      .getResource("icons/32x32/finish_album_32x32.png"));

  public static final ImageIcon ICON_NET_SEARCH = new ImageIcon(Util
      .getResource("icons/16x16/netsearch_16x16.png"));

  public static final ImageIcon ICON_TRACK_FIFO_PLANNED = new ImageIcon(Util
      .getResource("icons/16x16/clock_16x16.png"));

  public static final ImageIcon ICON_TRACK_FIFO_NORM = new ImageIcon(Util
      .getResource("icons/16x16/player_play_16x16.png"));

  public static final ImageIcon ICON_TRACK_FIFO_REPEAT = new ImageIcon(Util
      .getResource("icons/16x16/repeat_16x16.png"));

  public static final ImageIcon ICON_WIZARD = new ImageIcon(Util
      .getResource("icons/16x16/wizard_16x16.png"));

  public static final ImageIcon ICON_TYPE_MP3 = new ImageIcon(Util
      .getResource("icons/16x16/type_mp3_16x16.png"));

  public static final ImageIcon ICON_TYPE_MP2 = new ImageIcon(Util
      .getResource("icons/16x16/type_mp2_16x16.png"));

  public static final ImageIcon ICON_TYPE_OGG = new ImageIcon(Util
      .getResource("icons/16x16/type_ogg_16x16.png"));

  public static final ImageIcon ICON_TYPE_AU = new ImageIcon(Util
      .getResource("icons/16x16/type_wav_16x16.png"));

  public static final ImageIcon ICON_TYPE_AIFF = new ImageIcon(Util
      .getResource("icons/16x16/type_wav_16x16.png"));

  public static final ImageIcon ICON_TYPE_FLAC = new ImageIcon(Util
      .getResource("icons/16x16/type_flac_16x16.png"));

  public static final ImageIcon ICON_TYPE_MPC = new ImageIcon(Util
      .getResource("icons/16x16/type_wav_16x16.png"));

  public static final ImageIcon ICON_TYPE_WMA = new ImageIcon(Util
      .getResource("icons/16x16/type_wma_16x16.png"));

  public static final ImageIcon ICON_TYPE_APE = new ImageIcon(Util
      .getResource("icons/16x16/type_ape_16x16.png"));

  public static final ImageIcon ICON_TYPE_AAC = new ImageIcon(Util
      .getResource("icons/16x16/type_aac_16x16.png"));

  public static final ImageIcon ICON_TYPE_WAV = new ImageIcon(Util
      .getResource("icons/16x16/type_wav_16x16.png"));

  public static final ImageIcon ICON_TYPE_RAM = new ImageIcon(Util
      .getResource("icons/16x16/type_ram_16x16.png"));

  public static final ImageIcon ICON_NO_EDIT = new ImageIcon(Util
      .getResource("icons/16x16/stop_16x16.png"));

  public static final ImageIcon ICON_EDIT = new ImageIcon(Util
      .getResource("icons/16x16/edit_16x16.png"));

  public static final ImageIcon ICON_UNKNOWN = new ImageIcon(Util
      .getResource("icons/16x16/presence_unknown_16x16.png"));

  public static final ImageIcon ICON_TIP = new ImageIcon(Util
      .getResource("icons/40x40/tip_40x40.png"));

  public static final ImageIcon ICON_TIP_SMALL = new ImageIcon(Util
      .getResource("icons/16x16/tip_16x16.png"));

  public static final ImageIcon ICON_OPEN_DIR = new ImageIcon(Util
      .getResource("icons/40x40/folder_open_40x40.png"));

  public static final ImageIcon ICON_STAR_1 = new ImageIcon(Util
      .getResource("icons/16x16/star1_16x16.png"));

  public static final ImageIcon ICON_STAR_2 = new ImageIcon(Util
      .getResource("icons/16x16/star2_16x16.png"));

  public static final ImageIcon ICON_STAR_3 = new ImageIcon(Util
      .getResource("icons/16x16/star3_16x16.png"));

  public static final ImageIcon ICON_STAR_4 = new ImageIcon(Util
      .getResource("icons/16x16/star4_16x16.png"));

  public static final ImageIcon ICON_DROP_DOWN_32x32 = new ImageIcon(Util
      .getResource("icons/32x32/dropdown_32x32.png"));

  public static final ImageIcon ICON_DROP_DOWN_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/dropdown_16x16.png"));

  public static final ImageIcon ICON_DIGITAL_DJ = new ImageIcon(Util
      .getResource("icons/32x32/ddj_32x32.png"));

  public static final ImageIcon ICON_DIGITAL_DJ_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/ddj_16x16.png"));

  public static final ImageIcon ICON_WEBRADIO_16x16 = new ImageIcon(Util
      .getResource("icons/16x16/webradio_16x16.png"));

  public static final ImageIcon ICON_LIST = new ImageIcon(Util
      .getResource("icons/16x16/contents_16x16.png"));

  public static final ImageIcon ICON_CDDB = new ImageIcon(Util
      .getResource("icons/16x16/cddb_16x16.png"));

  public static final ImageIcon ICON_PLAY_TABLE = new ImageIcon(Util
      .getResource("icons/16x16/player_play_16x16.png"));

  public static final ImageIcon ICON_DEFAULTS = new ImageIcon(Util
      .getResource("icons/16x16/undo_16x16.png"));

  public static final ImageIcon ICON_DEFAULTS_BIG = new ImageIcon(Util
      .getResource("icons/22x22/undo_22x22.png"));

  public static final ImageIcon ICON_ACCURACY_LOW = new ImageIcon(Util
      .getResource("icons/16x16/accuracy_low_16x16.png"));

  public static final ImageIcon ICON_ACCURACY_MEDIUM = new ImageIcon(Util
      .getResource("icons/16x16/accuracy_medium_16x16.png"));

  public static final ImageIcon ICON_ACCURACY_HIGH = new ImageIcon(Util
      .getResource("icons/16x16/accuracy_high_16x16.png"));

  public static final ImageIcon ICON_REPORT = new ImageIcon(Util
      .getResource("icons/16x16/report_16x16.png"));

  public static final ImageIcon ICON_PUSH = new ImageIcon(Util
      .getResource("icons/16x16/push_16x16.png"));

  public static final ImageIcon ICON_COPY = new ImageIcon(Util
      .getResource("icons/16x16/editcopy_16x16.png"));

  public static final ImageIcon ICON_CUT = new ImageIcon(Util
      .getResource("icons/16x16/editcut_16x16.png"));

  public static final ImageIcon ICON_PASTE = new ImageIcon(Util
      .getResource("icons/16x16/editpaste_16x16.png"));

  public static final ImageIcon ICON_LAUNCH = new ImageIcon(Util
      .getResource("icons/16x16/launch_16x16.png"));

  public static final ImageIcon ICON_HISTORY = new ImageIcon(Util
      .getResource("icons/16x16/history_16x16.png"));

  public static final ImageIcon ICON_POPUP = new ImageIcon(Util
      .getResource("icons/16x16/popup_16x16.png"));

  public static final ImageIcon ICON_ALARM = new ImageIcon(Util
      .getResource("icons/16x16/alarm_16x16.png"));

  static {
    IconLoader.noCoversCache.put(ITechnicalStrings.THUMBNAIL_SIZE_50x50, Util.getResizedImage(
        IconLoader.ICON_NO_COVER, 50, 50));
    IconLoader.noCoversCache.put(ITechnicalStrings.THUMBNAIL_SIZE_100x100, Util.getResizedImage(
        IconLoader.ICON_NO_COVER, 100, 100));
    IconLoader.noCoversCache.put(ITechnicalStrings.THUMBNAIL_SIZE_150x150, Util.getResizedImage(
        IconLoader.ICON_NO_COVER, 150, 150));
    IconLoader.noCoversCache.put(ITechnicalStrings.THUMBNAIL_SIZE_200x200, Util.getResizedImage(
        IconLoader.ICON_NO_COVER, 200, 200));
    IconLoader.noCoversCache.put(ITechnicalStrings.THUMBNAIL_SIZE_250x250, Util.getResizedImage(
        IconLoader.ICON_NO_COVER, 250, 250));
    IconLoader.noCoversCache.put(ITechnicalStrings.THUMBNAIL_SIZE_300x300, Util.getResizedImage(
        IconLoader.ICON_NO_COVER, 300, 300));
  }
}
