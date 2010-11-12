/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision$
 */
package org.jajuk.ui.actions;

/**
 * This enum contains a constant for all actions present in Jajuk.
 * <p/>
 */
public enum JajukActions {

  /** Used for application exit. */
  EXIT,

  /** Used when the repeat status was changed. */
  REPEAT_MODE,

  /** Used when the repeat all status was changed. */
  REPEAT_ALL_MODE,

  /** Used when the shuffle mode was changed. */
  SHUFFLE_MODE,

  /** Used when the continue mode was changed. */
  CONTINUE_MODE,

  /** Used when the intro mode was changed. */
  INTRO_MODE,

  /** Used when the karaoke mode was changed. */
  KARAOKE_MODE,

  /** Used when a new device is requested. */
  DEVICE_NEW,

  /** Used when a device is removed. */
  DEVICE_DELETE,

  /** Used when device properties are requested. */
  DEVICE_PROPERTIES,

  /** Used when a device mount is requested. */
  DEVICE_MOUNT,

  /** Used when a device unmount is requested. */
  DEVICE_UNMOUNT,

  /** Used when a device test is requested. */
  DEVICE_TEST,

  /** Used when a device refresh is requested. */
  DEVICE_REFRESH,

  /** Used when a device synchronization is requested. */
  DEVICE_SYNCHRO,

  /** Used when a view refresh is requested. This is used in device view. */
  VIEW_REFRESH_REQUEST,

  /** Used when a view needs to be closed. */
  VIEW_CLOSE_REQUEST,

  /** Used when view needs to be shown. */
  VIEW_SHOW_REQUEST,

  /** Used when the display status of a view needs to be changed. */
  VIEW_SHOW_STATUS_CHANGED_REQUEST,

  /** Used when defaults settings need to be restored. */
  VIEW_RESTORE_DEFAULTS,

  /** Used when defaults settings for all perspectives. */
  ALL_VIEW_RESTORE_DEFAULTS,

  /** Used when a command from the history is selected. */
  VIEW_COMMAND_SELECT_HISTORY_ITEM,

  /** Used when the help should be displayed. */
  HELP_REQUIRED,

  /** Used to see debug traces. */
  SHOW_TRACES,

  /** Used when the cover should be refreshed. */
  COVER_REFRESH,

  /** Used when a request for a cover change is made. */
  COVER_CHANGE,

  /** Used when a playlist refresh is requested. */
  PLAYLIST_REFRESH,

  /** Used when a playlist change is requested. */
  PLAYLIST_CHANGED,

  /** Used when a file has been launched by the fifo. */
  FILE_LAUNCHED,

  /** Used as heart beat for general use to refresh subscribers every n secs. */
  HEART_BEAT,

  /** Used when a reinit has is required. */
  ZERO,

  /** Used when a new element has been added in the history. */
  ADD_HISTORY_ITEM,

  /** Used when a special mode (global shuffle, novelties, bestof...) changed. */
  SPECIAL_MODE,

  /** Used when the global shuffle button is pressed. */
  SHUFFLE_GLOBAL,

  /** Used when the best of button is pressed. */
  BEST_OF,

  /** Used when the DJ button is pressed. */
  DJ,

  /** Used when the novelties button is pressed. */
  NOVELTIES,

  /** Used when the finish album button is pressed. Indicates the current album will play until the end. */
  FINISH_ALBUM,

  /** Used when an error occurred during a play. */
  PLAY_ERROR,

  /** Used when the mute state changed. */
  MUTE_STATE,

  /** Used when table and tree views need to be synchronized. */
  SYNC_TREE_TABLE,

  /** Used when history should be cleared. */
  CLEAR_HISTORY,

  /** Used when the first time wizard needs to be launched. */
  SIMPLE_DEVICE_WIZARD,

  /** Used to launch the quality feedback agent. */
  QUALITY,

  /** Used to change the volume. */
  VOLUME_CHANGED,

  /** Used when a new property is created. */
  CREATE_PROPERTY,

  /** Used when a property is deleted. */
  DELETE_PROPERTY,

  /** Used when a new custom property is added. */
  CUSTOM_PROPERTIES_ADD,

  /** Used when a custom property is removed. */
  CUSTOM_PROPERTIES_REMOVE,

  /** Used when a file name changes. */
  FILE_NAME_CHANGED,

  /** Used when file rate changes. */
  RATE_CHANGED,

  /** Used to display a tip of the day window. */
  TIP_OF_THE_DAY,

  /** Used to check for jajuk updates. */
  CHECK_FOR_UPDATES,

  /** Used to display an about dialog. */
  SHOW_ABOUT,

  /** Used to display an tag configure dialog. */
  EXTRA_TAGS_WIZARD,

  /** Used to jump to the previous track. */
  PREVIOUS_TRACK,

  /** Used to display the current queue in slim bar. */
  QUEUE_TO_SLIM,

  /** Replay current album. */
  REPLAY_ALBUM,

  /** Used to jump to the next track. */
  NEXT_TRACK,

  /** Used to rewind the current track. */
  REWIND_TRACK,

  /** Used to fast-forward the current track. */
  FORWARD_TRACK,

  /** Used to increase current track rate. */
  INC_RATE,

  /** Used to stop playing. */
  STOP_TRACK,

  /** Used to pause/resume playing. */
  PAUSE_RESUME_TRACK,

  /** Used to jump to the previous album. */
  PREVIOUS_ALBUM,

  /** Used to jump to the next album. */
  NEXT_ALBUM,

  /** Used to increase the volume. */
  INCREASE_VOLUME,

  /** Used to decrease the volume. */
  DECREASE_VOLUME,

  /** Used to configure djs. */
  CONFIGURE_DJS,

  /** Used to configure ambiences. */
  CONFIGURE_AMBIENCES,

  /** Used to configure webradios. */
  CONFIGURE_WEBRADIOS,

  /** Used to configure the application. */
  OPTIONS,

  /** Used to show or hide unmounted devices. */
  UNMOUNTED,

  /** Create a report. */
  CREATE_REPORT,

  /** Copy to clipboard data from UtilFeatures.copyData */
  COPY_TO_CLIPBOARD,

  /** Open directory in default explorer program. */
  OPEN_EXPLORER,

  /** Launch in an external browser the url given in UtilFeatures.url */
  LAUNCH_IN_BROWSER,

  /** Launch a web radio. */
  WEB_RADIO,

  /** Used to delete selected files/directories from disk. */
  DELETE,

  /** Used to move selected files and directories. */
  PASTE,

  /** Used to refresh a directory. */
  REFRESH,

  /** Used to find duplicate files. */
  FIND_DUPLICATE_FILES,

  /** Used to set alarm clock. */
  ALARM_CLOCK,

  /** Show properties on a single or a selection of items. */
  SHOW_PROPERTIES,

  /** Play a selection. */
  PLAY_SELECTION,

  /** Play randomly a selection. */
  PLAY_SHUFFLE_SELECTION,

  /** Play a selection in a loop. */
  PLAY_REPEAT_SELECTION,

  /** Push a selection. */
  PUSH_SELECTION,

  /** Push a selection to the front. */
  PUSH_FRONT_SELECTION,

  /** Bookmark selection. */
  BOOKMARK_SELECTION,

  /** Play album for selection. */
  PLAY_ALBUM_SELECTION,

  /** Play artist for selection. */
  PLAY_ARTIST_SELECTION,

  /** Play full directory for selection. */
  PLAY_DIRECTORY_SELECTION,

  /** Search online tags. */
  CDDB_SELECTION,

  /** Show album details. */
  SHOW_ALBUM_DETAILS,

  /** Used to cut and move Selected Items. */
  CUT,

  /** Used to copy and move Selected Items. */
  COPY,

  /** Used to rename files/directories. */
  RENAME,

  /** Used to create New Folder. */
  NEW_FOLDER,

  /** Used to create a slim interface for Jajuk. */
  SLIM_JAJUK,

  /** Used to perform GC. */
  GC,

  /** Used to manually save settings and collection. */
  COMMIT,

  /** Save as. */
  SAVE_AS,

  /** Ban current item. */
  BAN,

  /** Ban selection. */
  BAN_SELECTION,

  /** Un-Ban selection. */
  UN_BAN_SELECTION,

  /** Set adore preference to selection. */
  PREFERENCE_ADORE,

  /** Set love preference to selection. */
  PREFERENCE_LOVE,

  /** Set like preference to selection. */
  PREFERENCE_LIKE,

  /** Set average preference to selection. */
  PREFERENCE_AVERAGE,

  /** Set poor preference to selection. */
  PREFERENCE_POOR,

  /** Set hate preference to selection. */
  PREFERENCE_HATE,

  /** Set unset preference to selection. */
  PREFERENCE_UNSET,

  /** Prepare party. */
  PREPARE_PARTY,

  /** fullscreen. */
  FULLSCREEN_JAJUK,

  /** display donate dialog. */
  SHOW_DONATE

}
