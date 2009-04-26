/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.dbus;

import org.freedesktop.dbus.DBusInterface;

/**
 * This class describes the interface that we publish to D-Bus, each method is
 * available via D-Bus and invokes the corresponding Action
 * 
 * TODO: Audio player usually also provide the following type of information
 * - current track that is playing including additional information that is available
 * - next planned track(s)
 * - position in the current track
 * - remaining time in the current track
 * - current Volume
 *  
 */
@org.freedesktop.DBus.Description("Methods to remotely control Jajuk including play/pause/next/prev track and some other useful actions.")
public interface DBusSupport extends DBusInterface {

  @org.freedesktop.DBus.Description("Switches to the previous track.")
  void previous() throws Exception;

  @org.freedesktop.DBus.Description("Switches to the next  track.")
  void next() throws Exception;

  @org.freedesktop.DBus.Description("Moves back in the currently played track.")
  void rewind() throws Exception;

  @org.freedesktop.DBus.Description("Toggles playing/pausing the current track.")
  void playPause() throws Exception;

  @org.freedesktop.DBus.Description("Stops playing.")
  void stop() throws Exception;

  @org.freedesktop.DBus.Description("Moves back in the currently played track.")
  void forward() throws Exception;

  @org.freedesktop.DBus.Description("Shuts off Jajuk, depending on configuration this can show a message box that needs to be confirmed with 'Yes'.")
  void exit() throws Exception;

  @org.freedesktop.DBus.Description("Shuffles all planned tracks.")
  void shuffleGlobal() throws Exception;

  @org.freedesktop.DBus.Description("Plays the previous album.")
  void previousAlbum() throws Exception;

  @org.freedesktop.DBus.Description("Plays the next album.")
  void nextAlbum() throws Exception;

  @org.freedesktop.DBus.Description("Increase volume by 5 percent.")
  void increaseVolume() throws Exception;

  @org.freedesktop.DBus.Description("Decrease volume by 5 percent.")
  void decreaseVolume() throws Exception;

  @org.freedesktop.DBus.Description("Toggles Mute on/off.")
  void mute() throws Exception;

  /*
   * Actions that are not supported (yet): 
   * REPEAT_MODE, SHUFFLE_MODE, CONTINUE_MODE, INTRO_MODE,
   * DEVICE_NEW, DEVICE_DELETE, DEVICE_PROPERTIES, DEVICE_MOUNT, DEVICE_UNMOUNT,
   * DEVICE_TEST, DEVICE_REFRESH, DEVICE_SYNCHRO, VIEW_REFRESH_REQUEST,
   * VIEW_CLOSE_REQUEST, VIEW_SHOW_REQUEST, VIEW_SHOW_STATUS_CHANGED_REQUEST,
   * VIEW_RESTORE_DEFAULTS, ALL_VIEW_RESTORE_DEFAULTS,
   * VIEW_COMMAND_SELECT_HISTORY_ITEM, HELP_REQUIRED, SHOW_TRACES,
   * COVER_REFRESH, COVER_CHANGE, PLAYLIST_REFRESH, PLAYLIST_CHANGED,
   * FILE_LAUNCHED, HEART_BEAT, ZERO, ADD_HISTORY_ITEM, SPECIAL_MODE, BEST_OF,
   * DJ, NOVELTIES, FINISH_ALBUM, PLAY_ERROR, SYNC_TREE_TABLE,
   * CLEAR_HISTORY, SIMPLE_DEVICE_WIZARD, QUALITY, VOLUME_CHANGED,
   * CREATE_PROPERTY, DELETE_PROPERTY, CUSTOM_PROPERTIES_ADD,
   * CUSTOM_PROPERTIES_REMOVE, FILE_NAME_CHANGED, RATE_CHANGED, TIP_OF_THE_DAY,
   * CHECK_FOR_UPDATES, SHOW_ABOUT, REPLAY_ALBUM, INC_RATE, CONFIGURE_DJS,
   * CONFIGURE_AMBIENCES, CONFIGURE_WEBRADIOS, OPTIONS, UNMOUNTED,
   * CREATE_REPORT, COPY_TO_CLIPBOARD, LAUNCH_IN_BROWSER, WEB_RADIO, DELETE,
   * PASTE, REFRESH, FIND_DUPLICATE_FILES, ALARM_CLOCK, SHOW_PROPERTIES,
   * PLAY_SELECTION, PLAY_SHUFFLE_SELECTION, PLAY_REPEAT_SELECTION,
   * PUSH_SELECTION, PUSH_FRONT_SELECTION, BOOKMARK_SELECTION,
   * PLAY_ALBUM_SELECTION, PLAY_AUTHOR_SELECTION, PLAY_DIRECTORY_SELECTION,
   * CDDB_SELECTION, SHOW_ALBUM_DETAILS, CUT, COPY, RENAME, NEW_FOLDER,
   * SLIM_JAJUK, SAVE_AS, BAN, BAN_SELECTION, UN_BAN_SELECTION,
   * PREFERENCE_ADORE, PREFERENCE_LOVE, PREFERENCE_LIKE, PREFERENCE_AVERAGE,
   * PREFERENCE_POOR, PREFERENCE_HATE, PREFERENCE_UNSET, PREPARE_PARTY
   */
}
