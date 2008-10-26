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
 *  $$Revision: 3902 $$
 */

package org.jajuk.events;

public enum JajukEvents {
  // repeat mode changed
  REPEAT_MODE_STATUS_CHANGED,
  // new device
  DEVICE_NEW,
  // removed device
  DEVICE_DELETE,
  // parameters change
  PARAMETERS_CHANGE,
  // device properties display
  DEVICE_PROPERTIES,
  // mount device
  DEVICE_MOUNT,
  // unmount a device
  DEVICE_UNMOUNT,
  // test a device
  DEVICE_TEST,
  // refresh a device
  DEVICE_REFRESH,
  // sync. a device
  DEVICE_SYNCHRO,
  // refresh a view is required
  VIEW_REFRESH_REQUEST,
  // the stop button has been pressed
  PLAYER_STOP,
  // the play button has been pressed
  PLAYER_PLAY,
  // the pause button has been pressed
  PLAYER_PAUSE,
  // the resume button has been pressed
  PLAYER_RESUME,
  // Queue should be refreshed
  QUEUE_NEED_REFRESH,
  // a file has been launched by the fifo
  FILE_LAUNCHED,
  // heart beat for general use to refresh subscribers
  // every n secs
  HEART_BEAT,
  // a web radio has been launched
  WEBRADIO_LAUNCHED,
  // a reinit has been required
  ZERO,
  // special mode (global shuffle, novelties, bestof...)
  SPECIAL_MODE,
  // an error occurred during a play
  PLAY_ERROR,
  // mute state changed
  MUTE_STATE,
  // sync table and tree views
  SYNC_TREE_TABLE,
  // clear history
  CLEAR_HISTORY,
  // launch first time wizard
  WIZARD,
  // volume changed
  VOLUME_CHANGED,
  // new custom property
  CUSTOM_PROPERTIES_ADD,
  // remove custom property
  CUSTOM_PROPERTIES_REMOVE,
  // file name change
  FILE_NAME_CHANGED,
  // Style name change
  STYLE_NAME_CHANGED,
  // file rate change
  RATE_CHANGED,
  // Cddb wizard required
  CDDB_WIZARD,
  // logical tree osrt method changed
  LOGICAL_TREE_SORT,
  // cover default changed
  COVER_DEFAULT_CHANGED,
  // clear table selection
  TABLE_CLEAR_SELECTION,
  // DJ creation or removal
  DJS_CHANGE,
  // One or more ambiences have been
  // removed/added/changed
  AMBIENCES_CHANGE,
  // One or more webradios have been
  // removed/added/changed
  WEBRADIOS_CHANGE,
  // user changed current ambience
  AMBIENCES_SELECTION_CHANGE,
  // An ambience has been removed
  AMBIENCE_REMOVED,
  // Current played track author name has been
  // changed
  AUTHOR_CHANGED,
  // Current played track album name has been
  // changed
  ALBUM_CHANGED,
  // Current played track album name has been
  // changed
  TRACK_CHANGED,
  // Language changed
  LANGUAGE_CHANGED,
  // Perspective changed
  PERPECTIVE_CHANGED,
  // Current track is finished
  FILE_FINISHED,
  // Lyrics data has been downloaded
  LYRICS_DOWNLOADED,
  // A file has been copied (used by prepare party)
  FILE_COPIED,
  // Covers should be refreshed
  COVER_NEED_REFRESH,
  // Ratings have to be reseted
  RATE_RESET,
  // Preferences have been reset
  PREFERENCES_RESET,
  // suggestion view should be refreshed
  SUGGESTIONS_REFRESH,

}
