/*
 *  Jajuk
 *  Copyright (C) 2006 david
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

public enum EventSubject {
	// exit has be required
    EVENT_EXIT, 
	// repeat mode changed
    EVENT_REPEAT_MODE_STATUS_CHANGED, 
	// shuffle mode changed
    EVENT_SHUFFLE_MODE_STATUS_CHANGED, 
	// continue mode changed
    EVENT_CONTINUE_MODE_STATUS_CHANGED, 
	// intro mode changed
    EVENT_INTRO_MODE_STATUS_CHANGED, 
	// new device
    EVENT_DEVICE_NEW, 
	// removed device
    EVENT_DEVICE_DELETE, 
	// device properties display
    EVENT_DEVICE_PROPERTIES, 
	// mount device
    EVENT_DEVICE_MOUNT, 
	// unmount a device
    EVENT_DEVICE_UNMOUNT, 
	// test a device
    EVENT_DEVICE_TEST, 
	// refresh a device
    EVENT_DEVICE_REFRESH, 
	// sync. a device
    EVENT_DEVICE_SYNCHRO, 
	// refresh a view is required, used in the device view
    EVENT_VIEW_REFRESH_REQUEST, 
    // close a view
    EVENT_VIEW_CLOSE_REQUEST, 
	// show a view
    EVENT_VIEW_SHOW_REQUEST, 
	// change
    EVENT_VIEW_SHOW_STATUS_CHANGED_REQUEST, 
	// The help should be displayed
    EVENT_VIEW_RESTORE_DEFAULTS, 
    EVENT_VIEW_COMMAND_SELECT_HISTORY_ITEM, 
    EVENT_HELP_REQUIRED, 
    // the cover should be refreshed
    EVENT_COVER_REFRESH, 
	// Request for a cover change
    EVENT_COVER_CHANGE, 
	// the stop button has been pressed
    EVENT_PLAYER_STOP, 
	// the play button has been pressed
    EVENT_PLAYER_PLAY, 
	// the pause button has been pressed
    EVENT_PLAYER_PAUSE, 
	// the resume button has been pressed
    EVENT_PLAYER_RESUME, 
	EVENT_PLAYLIST_REFRESH, 
    EVENT_PLAYLIST_SELECTION_CHANGED, 
    // a file has been lauched by the fifo
    EVENT_FILE_LAUNCHED, 
    // heart beat for geenral use to refresh subscribers
    // every n secs
    EVENT_HEART_BEAT, 
    // a reinit has been required
    EVENT_ZERO, 
	// a new element has been added in the history
    EVENT_ADD_HISTORY_ITEM, 
	// special mode (global shuffle, novelties, bestof...)
    EVENT_SPECIAL_MODE, 
	// an error occured during a play
    EVENT_PLAY_ERROR, 
	// send at the end of a track
    EVENT_PLAY_FINISHED, 
	// mute state changed
    EVENT_MUTE_STATE, 
	// sync table and tree views
    EVENT_SYNC_TREE_TABLE, 
	// clear history
    EVENT_CLEAR_HISTORY, 
	// launch first time wizard
    EVENT_WIZARD, 
	// quality feedback agent
    EVENT_QUALITY, 
	// volume changed
    EVENT_VOLUME_CHANGED, 
	// create a new property
    EVENT_CREATE_PROPERTY, 
	// delete property
    EVENT_DELETE_PROPERTY, 
	// new custom property
    EVENT_CUSTOM_PROPERTIES_ADD, 
	// remove custom property
    EVENT_CUSTOM_PROPERTIES_REMOVE, 
	// file name change
    EVENT_FILE_NAME_CHANGED, 
	// Style name change
    EVENT_STYLE_NAME_CHANGED, 
	// file rate change
    EVENT_RATE_CHANGED, 
	// show tip of the day
    EVENT_TIP_OF_THE_DAY, 
	// Cddb wizard required
    EVENT_CDDB_WIZARD, 
	// logical tree osrt method changed
    EVENT_LOGICAL_TREE_SORT, 
	// cover default changed
    EVENT_COVER_DEFAULT_CHANGED, 
	// clear table selection
    EVENT_TABLE_CLEAR_SELECTION, 
	// DJ creation or removal
    EVENT_DJS_CHANGE, 
	// One or more ambiences have been
    // removed/added/changed
    EVENT_AMBIENCES_CHANGE, 
	// user changed current ambience
    EVENT_AMBIENCES_SELECTION_CHANGE, 
	// An ambience has been removed
    EVENT_AMBIENCE_REMOVED, 
	// Current played track author name has been
    // changed
    EVENT_AUTHOR_CHANGED, 
	// Language changed
    EVENT_LANGUAGE_CHANGED,
	//Perspective changed
    EVENT_PERPECTIVE_CHANGED
}
