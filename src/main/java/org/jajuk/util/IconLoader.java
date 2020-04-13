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

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Load icons from this class
 * <p>
 * Use: IconLoader.getIcon(JajukIcons.<icon name>)
 * </p>
 */
final public class IconLoader {
  /** Icons cache *. */
  private static Map<JajukIcons, ImageIcon> cache = new HashMap<JajukIcons, ImageIcon>(200);

  /**
   * No instantiation.
   */
  private IconLoader() {
  }

  /**
   * Gets the no cover icon.
   * 
   * @param size The size of the icon to return.
   * 
   * @return the nocover icon for specified size
   */
  public static ImageIcon getNoCoverIcon(int size) {
    String constant = new StringBuilder("NO_COVER_").append(size).append('X').append(size)
        .toString();
    return getIcon(JajukIcons.valueOf(constant));
  }

  /**
   * Gets the icon.
   * 
   * @param icon the JajukIcons name
   * 
   * @return an image icon for given icon name.Note that all images are cached
   * before being returned.
   */
  public static ImageIcon getIcon(JajukIcons icon) {
    // Try to recover the icon from the cache first
    if (cache.containsKey(icon)) {
      return cache.get(icon);
    }
    ImageIcon out = null;
    if (icon == JajukIcons.NO_COVER) {
      out = new ImageIcon(UtilSystem.getResource("images/" + Const.FILE_THUMB_NO_COVER));
    } else if (icon == JajukIcons.NONE) {
      out = new ImageIcon(UtilSystem.getResource("images/" + Const.FILE_THUMB_NONE));
    } else if (icon == JajukIcons.NO_COVER_50X50) {
      out = UtilGUI.getResizedImage(getIcon(JajukIcons.NO_COVER), 50, 50);
    } else if (icon == JajukIcons.NO_COVER_100X100) {
      out = UtilGUI.getResizedImage(getIcon(JajukIcons.NO_COVER), 100, 100);
    } else if (icon == JajukIcons.NO_COVER_150X150) {
      out = UtilGUI.getResizedImage(getIcon(JajukIcons.NO_COVER), 150, 150);
    } else if (icon == JajukIcons.NO_COVER_200X200) {
      out = UtilGUI.getResizedImage(getIcon(JajukIcons.NO_COVER), 200, 200);
    } else if (icon == JajukIcons.NO_COVER_250X250) {
      out = UtilGUI.getResizedImage(getIcon(JajukIcons.NO_COVER), 250, 250);
    } else if (icon == JajukIcons.NO_COVER_300X300) {
      out = UtilGUI.getResizedImage(getIcon(JajukIcons.NO_COVER), 300, 300);
    } else if (icon == JajukIcons.LOGO) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/jajuk-icon_64x64.png"));
    } else if (icon == JajukIcons.COVER_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/thumbnail_16x16.png"));
    } else if (icon == JajukIcons.LOGO_FRAME) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/jajuk-icon_16x16.png"));
    } else if (icon == JajukIcons.REPEAT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/repeat_16x16.png"));
    } else if (icon == JajukIcons.REPEAT_ALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/repeat_all_16x16.png"));
    } else if (icon == JajukIcons.SHUFFLE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/shuffle_16x16.png"));
    } else if (icon == JajukIcons.CONTINUE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/continue_16x16.png"));
    } else if (icon == JajukIcons.INTRO) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/intro_16x16.png"));
    } else if (icon == JajukIcons.KARAOKE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/karaoke_16x16.png"));
    } else if (icon == JajukIcons.SHUFFLE_GLOBAL) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/shuffle_global_32x32.png"));
    } else if (icon == JajukIcons.SHUFFLE_GLOBAL_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/shuffle_global_16x16.png"));
    } else if (icon == JajukIcons.BESTOF) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/bestof_32x32.png"));
    } else if (icon == JajukIcons.BESTOF_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/bestof_16x16.png"));
    } else if (icon == JajukIcons.MUTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/mute_32x32.png"));
    } else if (icon == JajukIcons.VOLUME_LEVEL1) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/volume1.png"));
    } else if (icon == JajukIcons.VOLUME_LEVEL2) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/volume2.png"));
    } else if (icon == JajukIcons.VOLUME_LEVEL3) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/volume3.png"));
    } else if (icon == JajukIcons.WEBRADIO) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/webradio_32x32.png"));
    } else if (icon == JajukIcons.NOVELTIES) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/novelties_32x32.png"));
    } else if (icon == JajukIcons.NOVELTIES_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/novelties_16x16.png"));
    } else if (icon == JajukIcons.PLAYER_NEXT_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_next_16x16.png"));
    } else if (icon == JajukIcons.SAVE_AS) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/saveas_16x16.png"));
    } else if (icon == JajukIcons.PLAYER_PREVIOUS_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_previous_16x16.png"));
    } else if (icon == JajukIcons.PLAYER_PREVIOUS) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/player_previous_32x32.png"));
    } else if (icon == JajukIcons.PLAYER_PREVIOUS_BIG) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_previous_64x64.png"));
    } else if (icon == JajukIcons.PLAYER_NEXT) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/player_next_32x32.png"));
    } else if (icon == JajukIcons.PLAYER_NEXT_BIG) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_next_64x64.png"));
    } else if (icon == JajukIcons.INC_RATING) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/inc_rating_16x16.png"));
    } else if (icon == JajukIcons.REW) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/player_rew_32x32.png"));
    } else if (icon == JajukIcons.PLAYER_REWIND_BIG) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_rew_64x64.png"));
    } else if (icon == JajukIcons.AUTOSCROLL) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/autoscroll.png"));
    } else if (icon == JajukIcons.REW_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_rew_16x16.png"));
    } else if (icon == JajukIcons.PLAYER_PLAY) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/player_play_32x32.png"));
    } else if (icon == JajukIcons.PLAYER_PLAY_BIG) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_play_64x64.png"));
    } else if (icon == JajukIcons.PLAY_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_play_16x16.png"));
    } else if (icon == JajukIcons.PLAYER_PAUSE) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/player_pause_32x32.png"));
    } else if (icon == JajukIcons.PLAYER_PAUSE_BIG) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_pause_64x64.png"));
    } else if (icon == JajukIcons.PAUSE_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_pause_16x16.png"));
    } else if (icon == JajukIcons.PLAYER_STOP) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/player_stop_32x32.png"));
    } else if (icon == JajukIcons.PLAYER_STOP_BIG) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_stop_64x64.png"));
    } else if (icon == JajukIcons.STOP_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_stop_16x16.png"));
    } else if (icon == JajukIcons.FWD) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/player_fwd_32x32.png"));
    } else if (icon == JajukIcons.PLAYER_FORWARD_BIG) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_fwd_64x64.png"));
    } else if (icon == JajukIcons.FWD_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_fwd_16x16.png"));
    } else if (icon == JajukIcons.VOLUME) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/volume_16x16.png"));
    } else if (icon == JajukIcons.CLOSE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/close_16x16.png"));
    } else if (icon == JajukIcons.SLIM_WINDOW) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/slim_window_16x16.png"));
    } else if (icon == JajukIcons.FULL_SCREEN) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/full_window_16x16.png"));
    } else if (icon == JajukIcons.STOP_AFTER) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/position_16x16.png"));
    } else if (icon == JajukIcons.INFO) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/info_16x16.png"));
    } else if (icon == JajukIcons.BOOKMARK_FOLDERS) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/bookmark_16x16.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_SIMPLE) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_simple_40x40.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_PHYSICAL) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_physic_40x40.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_LOGICAL) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_logic_40x40.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_STATISTICS) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_stat_40x40.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_CONFIGURATION) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_configuration_40x40.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_PLAYER) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_player_40x40.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_CATALOG) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_catalog_40x40.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_PLAYLISTS) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_playlists_40x40.png"));
    } else if (icon == JajukIcons.PERSPECTIVE_INFORMATION) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/perspective_information_40x40.png"));
    } else if (icon == JajukIcons.OPEN_FILE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/fileopen_16x16.png"));
    } else if (icon == JajukIcons.EXIT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/exit_16x16.png"));
    } else if (icon == JajukIcons.NEW) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/new_16x16.png"));
    } else if (icon == JajukIcons.SEARCH) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/search_16x16.png"));
    } else if (icon == JajukIcons.DELETE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/delete_16x16.png"));
    } else if (icon == JajukIcons.PROPERTIES) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/properties_16x16.png"));
    } else if (icon == JajukIcons.VOID) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/void_16x16.png"));
    } else if (icon == JajukIcons.CONFIGURATION) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/configure_16x16.png"));
    } else if (icon == JajukIcons.MOUNT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/mount_16x16.png"));
    } else if (icon == JajukIcons.UPDATE_MANAGER) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/update_manager_16x16.png"));
    } else if (icon == JajukIcons.UNMOUNT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/unmount_16x16.png"));
    } else if (icon == JajukIcons.TRACES) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/traces_16x16.png"));
    } else if (icon == JajukIcons.TEST) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/test_16x16.png"));
    } else if (icon == JajukIcons.REORGANIZE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/reorganize_16x16.png"));
    } else if (icon == JajukIcons.REFRESH) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/refresh_16x16.png"));
    } else if (icon == JajukIcons.RESTORE_ALL_VIEWS) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/refresh_all_16x16.png"));
    } else if (icon == JajukIcons.SYNCHRO) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/synchro_16x16.png"));
    } else if (icon == JajukIcons.DEVICE_NEW) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/new_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_CD_MOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/cdrom_mount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_CD_UNMOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/cdrom_unmount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_EXT_DD_MOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/ext_dd_mount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_EXT_DD_UNMOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/ext_dd_unmount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_DIRECTORY_MOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/folder_mount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_DIRECTORY_UNMOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/folder_unmount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_PLAYER_MOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_mount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_PLAYER_UNMOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/player_unmount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_NETWORK_DRIVE_MOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/nfs_mount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_NETWORK_DRIVE_UNMOUNTED) {
      out = new ImageIcon(UtilSystem.getResource("icons/64x64/nfs_unmount_64x64.png"));
    } else if (icon == JajukIcons.DEVICE_CD_MOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/cdrom_mount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_CD_UNMOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/cdrom_unmount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_EXT_DD_MOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/ext_dd_mount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_EXT_DD_UNMOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/ext_dd_unmount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_NETWORK_DRIVE_MOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/nfs_mount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/nfs_unmount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_DIRECTORY_MOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/folder_mount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_DIRECTORY_UNMOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/folder_unmount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_PLAYER_MOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/player_mount_22x22.png"));
    } else if (icon == JajukIcons.DEVICE_PLAYER_UNMOUNTED_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/player_unmount_22x22.png"));
    } else if (icon == JajukIcons.OK) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/ok_22x22.png"));
    } else if (icon == JajukIcons.OK_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/ok_16x16.png"));
    } else if (icon == JajukIcons.KO) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/ko_22x22.png"));
    } else if (icon == JajukIcons.TRACK) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/track_16x16.png"));
    } else if (icon == JajukIcons.DIRECTORY_SYNCHRO) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/dir_synchro_16x16.png"));
    } else if (icon == JajukIcons.DIRECTORY_DESYNCHRO) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/dir_desynchro_16x16.png"));
    } else if (icon == JajukIcons.PLAYLIST_FILE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/playlist_16x16.png"));
    } else if (icon == JajukIcons.GENRE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/genre_16x16.png"));
    } else if (icon == JajukIcons.EMPTY) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/empty_16x16.png"));
    } else if (icon == JajukIcons.ARTIST) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/artist_16x16.png"));
    } else if (icon == JajukIcons.ALBUM) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/album_16x16.png"));
    } else if (icon == JajukIcons.YEAR) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/clock_16x16.png"));
    } else if (icon == JajukIcons.APPLY_FILTER) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/filter_16x16.png"));
    } else if (icon == JajukIcons.DISCOVERY_DATE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/calendar_16x16.png"));
    } else if (icon == JajukIcons.CLEAR_FILTER) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/clear_16x16.png"));
    } else if (icon == JajukIcons.ADVANCED_FILTER) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/complex_search_16x16.png"));
    } else if (icon == JajukIcons.PLAYLIST_NEW) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/playlist_new_32x32.png"));
    } else if (icon == JajukIcons.PLAYLIST_NEW_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/new_16x16.png"));
    } else if (icon == JajukIcons.PLAYLIST_BOOKMARK) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/playlist_bookmark_32x32.png"));
    } else if (icon == JajukIcons.PLAYLIST_BOOKMARK_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/bookmark_16x16.png"));
    } else if (icon == JajukIcons.PLAYLIST_BESTOF) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/bestof_32x32.png"));
    } else if (icon == JajukIcons.PLAYLIST_NOVELTIES) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/novelties_32x32.png"));
    } else if (icon == JajukIcons.RUN) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_play_16x16.png"));
    } else if (icon == JajukIcons.ADD) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/add_16x16.png"));
    } else if (icon == JajukIcons.REMOVE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/remove_16x16.png"));
    } else if (icon == JajukIcons.UP) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/up_16x16.png"));
    } else if (icon == JajukIcons.DOWN) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/down_16x16.png"));
    } else if (icon == JajukIcons.ADD_SHUFFLE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/add_shuffle_16x16.png"));
    } else if (icon == JajukIcons.CLEAR) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/clear_16x16.png"));
    } else if (icon == JajukIcons.SAVE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/save_16x16.png"));
    } else if (icon == JajukIcons.EXT_DRIVE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/ext_drive_16x16.png"));
    } else if (icon == JajukIcons.DEFAULT_COVER) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/ok_16x16.png"));
    } else if (icon == JajukIcons.FINISH_ALBUM) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/finish_album_32x32.png"));
    } else if (icon == JajukIcons.FINISH_ALBUM_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/finish_album_16x16.png"));
    } else if (icon == JajukIcons.NET_SEARCH) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/netsearch_16x16.png"));
    } else if (icon == JajukIcons.TRACK_FIFO_PLANNED) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/clock_16x16.png"));
    } else if (icon == JajukIcons.TRACK_FIFO_NORM) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_play_16x16.png"));
    } else if (icon == JajukIcons.TRACK_FIFO_REPEAT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/repeat_16x16.png"));
    } else if (icon == JajukIcons.WIZARD) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/wizard_16x16.png"));
    } else if (icon == JajukIcons.TYPE_MP3) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_mp3_16x16.png"));
    } else if (icon == JajukIcons.TYPE_MP2) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_mp2_16x16.png"));
    } else if (icon == JajukIcons.TYPE_OGG) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_ogg_16x16.png"));
    } else if (icon == JajukIcons.TYPE_AU) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_wav_16x16.png"));
    } else if (icon == JajukIcons.TYPE_AIFF) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_wav_16x16.png"));
    } else if (icon == JajukIcons.TYPE_FLAC) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_flac_16x16.png"));
    } else if (icon == JajukIcons.TYPE_MPC) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_wav_16x16.png"));
    } else if (icon == JajukIcons.TYPE_WMA) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_wma_16x16.png"));
    } else if (icon == JajukIcons.TYPE_APE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_ape_16x16.png"));
    } else if (icon == JajukIcons.TYPE_AAC) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_aac_16x16.png"));
    } else if (icon == JajukIcons.TYPE_WAV) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_wav_16x16.png"));
    } else if (icon == JajukIcons.TYPE_RAM) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/type_ram_16x16.png"));
    } else if (icon == JajukIcons.NO_EDIT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/stop_16x16.png"));
    } else if (icon == JajukIcons.EDIT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/edit_16x16.png"));
    } else if (icon == JajukIcons.UNKNOWN) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/presence_unknown_16x16.png"));
    } else if (icon == JajukIcons.TIP) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/tip_40x40.png"));
    } else if (icon == JajukIcons.TIP_SMALL) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/tip_16x16.png"));
    } else if (icon == JajukIcons.OPEN_DIR) {
      out = new ImageIcon(UtilSystem.getResource("icons/40x40/folder_open_40x40.png"));
    } else if (icon == JajukIcons.STAR_0) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/star0_16x16.png"));
    } else if (icon == JajukIcons.STAR_1) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/star1_16x16.png"));
    } else if (icon == JajukIcons.STAR_2) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/star2_16x16.png"));
    } else if (icon == JajukIcons.STAR_3) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/star3_16x16.png"));
    } else if (icon == JajukIcons.STAR_4) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/star4_16x16.png"));
    } else if (icon == JajukIcons.DROP_DOWN_32X32) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/dropdown_32x32.png"));
    } else if (icon == JajukIcons.DROP_DOWN_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/dropdown_16x16.png"));
    } else if (icon == JajukIcons.DIGITAL_DJ) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/ddj_32x32.png"));
    } else if (icon == JajukIcons.DIGITAL_DJ_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/ddj_16x16.png"));
    } else if (icon == JajukIcons.WEBRADIO_16X16) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/webradio_16x16.png"));
    } else if (icon == JajukIcons.LIST) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/contents_16x16.png"));
    } else if (icon == JajukIcons.CDDB) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/cddb_16x16.png"));
    } else if (icon == JajukIcons.PLAY_TABLE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/player_play_16x16.png"));
    } else if (icon == JajukIcons.DEFAULTS) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/undo_16x16.png"));
    } else if (icon == JajukIcons.DEFAULTS_BIG) {
      out = new ImageIcon(UtilSystem.getResource("icons/22x22/undo_22x22.png"));
    } else if (icon == JajukIcons.ACCURACY_LOW) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/accuracy_low_16x16.png"));
    } else if (icon == JajukIcons.ACCURACY_MEDIUM) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/accuracy_medium_16x16.png"));
    } else if (icon == JajukIcons.ACCURACY_HIGH) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/accuracy_high_16x16.png"));
    } else if (icon == JajukIcons.REPORT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/report_16x16.png"));
    } else if (icon == JajukIcons.PUSH) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/push_16x16.png"));
    } else if (icon == JajukIcons.COPY) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/editcopy_16x16.png"));
    } else if (icon == JajukIcons.CUT) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/editcut_16x16.png"));
    } else if (icon == JajukIcons.PASTE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/editpaste_16x16.png"));
    } else if (icon == JajukIcons.LAUNCH) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/launch_16x16.png"));
    } else if (icon == JajukIcons.HISTORY) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/history_16x16.png"));
    } else if (icon == JajukIcons.POPUP) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/popup_16x16.png"));
    } else if (icon == JajukIcons.ALARM) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/alarm_16x16.png"));
    } else if (icon == JajukIcons.BAN) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/ban_16x16.png"));
    } else if (icon == JajukIcons.UNBAN) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/ok_16x16.png"));
    } else if (icon == JajukIcons.PREFERENCE_ADORE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/preference-adore_16x16.png"));
    } else if (icon == JajukIcons.PREFERENCE_AVERAGE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/preference-average_16x16.png"));
    } else if (icon == JajukIcons.PREFERENCE_POOR) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/preference-poor_16x16.png"));
    } else if (icon == JajukIcons.PREFERENCE_HATE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/preference-hate_16x16.png"));
    } else if (icon == JajukIcons.PREFERENCE_LIKE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/preference-like_16x16.png"));
    } else if (icon == JajukIcons.PREFERENCE_LOVE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/preference-love_16x16.png"));
    } else if (icon == JajukIcons.PREFERENCE_UNSET) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/preference-unknown_16x16.png"));
    } else if (icon == JajukIcons.PREPARE_PARTY) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/ext_drive_16x16.png"));
    } else if (icon == JajukIcons.PREPARE_PARTY_32X32) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/ext_drive_32x32.png"));
    } else if (icon == JajukIcons.COPY_TO_CLIPBOARD) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/copy_url_clipboard_16x16.png"));
    } else if (icon == JajukIcons.AMBIENCE) {
      out = new ImageIcon(UtilSystem.getResource("icons/32x32/ambience_32x32.png"));
    } else if (icon == JajukIcons.OPEN_EXPLORER) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/explorer_16x16.png"));
    } else if (icon == JajukIcons.TREE_TABLE_SYNC) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/tree-table-sync_16x16.png"));
    } else if (icon == JajukIcons.DONATE) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/donate_16x16.png"));
    } else if (icon == JajukIcons.DISC_NUMBER) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/disc_number_16x16.png"));
    } else if (icon == JajukIcons.ALBUM_ARTIST) {
      out = new ImageIcon(UtilSystem.getResource("icons/16x16/album_artist_16x16.png"));
    }
    // Cache the result
    if (!cache.containsKey(icon)) {
      cache.put(icon, out);
    }
    return out;
  }
}
