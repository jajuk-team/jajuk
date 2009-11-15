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

package org.jajuk.events;

/**
 * DOCUMENT_ME.
 */
public enum JajukEvents {
  // repeat mode changed
  /** DOCUMENT_ME. */
  REPEAT_MODE_STATUS_CHANGED,
  // new device
  /** DOCUMENT_ME. */
  DEVICE_NEW,
  // removed device
  /** DOCUMENT_ME. */
  DEVICE_DELETE,
  // parameters change
  /** DOCUMENT_ME. */
  PARAMETERS_CHANGE,
  // device properties display
  /** DOCUMENT_ME. */
  DEVICE_PROPERTIES,
  // mount device
  /** DOCUMENT_ME. */
  DEVICE_MOUNT,
  // unmount a device
  /** DOCUMENT_ME. */
  DEVICE_UNMOUNT,
  // test a device
  /** DOCUMENT_ME. */
  DEVICE_TEST,
  // refresh a device
  /** DOCUMENT_ME. */
  DEVICE_REFRESH,
  // sync. a device
  /** DOCUMENT_ME. */
  DEVICE_SYNCHRO,
  // refresh a view is required
  /** DOCUMENT_ME. */
  VIEW_REFRESH_REQUEST,
  // the stop button has been pressed
  /** DOCUMENT_ME. */
  PLAYER_STOP,
  // the play button has been pressed
  /** DOCUMENT_ME. */
  PLAYER_PLAY,
  // the pause button has been pressed
  /** DOCUMENT_ME. */
  PLAYER_PAUSE,
  // the resume button has been pressed
  /** DOCUMENT_ME. */
  PLAYER_RESUME,
  // Queue should be refreshed
  /** DOCUMENT_ME. */
  QUEUE_NEED_REFRESH,
  // a file has been launched by the fifo
  /** DOCUMENT_ME. */
  FILE_LAUNCHED,
  // heart beat for general use to refresh subscribers
  // every n secs
  /** DOCUMENT_ME. */
  HEART_BEAT,
  // a web radio has been launched
  /** DOCUMENT_ME. */
  WEBRADIO_LAUNCHED,
  // a reinit has been required
  /** DOCUMENT_ME. */
  ZERO,
  // special mode (global shuffle, novelties, bestof...)
  /** DOCUMENT_ME. */
  SPECIAL_MODE,
  // an error occurred during a play
  /** DOCUMENT_ME. */
  PLAY_ERROR,
  // A track is opening
  /** DOCUMENT_ME. */
  PLAY_OPENING,
  // mute state changed
  /** DOCUMENT_ME. */
  MUTE_STATE,
  // sync table and tree views
  /** DOCUMENT_ME. */
  SYNC_TREE_TABLE,
  // clear history
  /** DOCUMENT_ME. */
  CLEAR_HISTORY,
  // launch first time wizard
  /** DOCUMENT_ME. */
  WIZARD,
  // volume changed
  /** DOCUMENT_ME. */
  VOLUME_CHANGED,
  // new custom property
  /** DOCUMENT_ME. */
  CUSTOM_PROPERTIES_ADD,
  // remove custom property
  /** DOCUMENT_ME. */
  CUSTOM_PROPERTIES_REMOVE,
  // file name change
  /** DOCUMENT_ME. */
  FILE_NAME_CHANGED,
  // Style name change
  /** DOCUMENT_ME. */
  STYLE_NAME_CHANGED,
  // file rate change
  /** DOCUMENT_ME. */
  RATE_CHANGED,
  // Cddb wizard required
  /** DOCUMENT_ME. */
  CDDB_WIZARD,
  // logical tree osrt method changed
  /** DOCUMENT_ME. */
  LOGICAL_TREE_SORT,
  // cover default changed
  /** DOCUMENT_ME. */
  COVER_DEFAULT_CHANGED,
  // clear table selection
  /** DOCUMENT_ME. */
  TABLE_CLEAR_SELECTION,
  // DJ creation or removal
  /** DOCUMENT_ME. */
  DJS_CHANGE,
  // One or more ambiences have been
  // removed/added/changed
  /** DOCUMENT_ME. */
  AMBIENCES_CHANGE,
  // One or more webradios have been
  // removed/added/changed
  /** DOCUMENT_ME. */
  WEBRADIOS_CHANGE,
  // user changed current ambience
  /** DOCUMENT_ME. */
  AMBIENCES_SELECTION_CHANGE,
  // An ambience has been removed
  /** DOCUMENT_ME. */
  AMBIENCE_REMOVED,
  // Current played track author name has been
  // changed
  /** DOCUMENT_ME. */
  AUTHOR_CHANGED,
  // Current played track album name has been
  // changed
  /** DOCUMENT_ME. */
  ALBUM_CHANGED,
  // Current played track album name has been
  // changed
  /** DOCUMENT_ME. */
  TRACK_CHANGED,
  // Language changed
  /** DOCUMENT_ME. */
  LANGUAGE_CHANGED,
  // Perspective changed
  /** DOCUMENT_ME. */
  PERSPECTIVE_CHANGED,
  // Current track is finished
  /** DOCUMENT_ME. */
  FILE_FINISHED,
  // Lyrics data has been downloaded
  /** DOCUMENT_ME. */
  LYRICS_DOWNLOADED,
  // A file has been copied (used by prepare party)
  /** DOCUMENT_ME. */
  FILE_COPIED,
  // Covers should be refreshed
  /** DOCUMENT_ME. */
  COVER_NEED_REFRESH,
  // Ratings have to be reseted
  /** DOCUMENT_ME. */
  RATE_RESET,
  // Preferences have been reset
  /** DOCUMENT_ME. */
  PREFERENCES_RESET,
  // Suggestion view should be refreshed
  /** DOCUMENT_ME. */
  SUGGESTIONS_REFRESH,
  // A table selection changed
  /** DOCUMENT_ME. */
  TABLE_SELECTION_CHANGED,
  // Playing track has been banned
  /** DOCUMENT_ME. */
  BANNED,
  // ALARMS CHANGED (REMOVED, ADDED)
  /** DOCUMENT_ME. */
  ALARMS_CHANGE,
  // Thumb created
  /** DOCUMENT_ME. */
  THUMB_CREATED,
  // Exiting Jajuk
  /** DOCUMENT_ME. */
  EXITING,
  // D-Bus command that shows the notification with the currently played file
  /** DOCUMENT_ME. */
  SHOW_CURRENTLY_PLAYING
}
