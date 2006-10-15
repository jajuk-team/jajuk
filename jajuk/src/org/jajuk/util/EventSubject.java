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
    EVENT_EXIT,  //exit has be required
    EVENT_REPEAT_MODE_STATUS_CHANGED, //repeat mode changed
    EVENT_SHUFFLE_MODE_STATUS_CHANGED, //shuffle mode changed
    EVENT_CONTINUE_MODE_STATUS_CHANGED, //continue mode changed
    EVENT_INTRO_MODE_STATUS_CHANGED, //intro mode changed
    EVENT_DEVICE_NEW, //new device
    EVENT_DEVICE_DELETE, //removed device
    EVENT_DEVICE_PROPERTIES, //device properties display
    EVENT_DEVICE_MOUNT, //mount device
    EVENT_DEVICE_UNMOUNT, //unmount a device
    EVENT_DEVICE_TEST, //test a device
    EVENT_DEVICE_REFRESH, //refresh a device
    EVENT_DEVICE_SYNCHRO, //sync. a device
    EVENT_VIEW_REFRESH_REQUEST, //refresh a view is required, used in the device view
    EVENT_VIEW_CLOSE_REQUEST, //close a view
    EVENT_VIEW_SHOW_REQUEST, //show a view
    EVENT_VIEW_SHOW_STATUS_CHANGED_REQUEST, //change 
    EVENT_VIEW_RESTORE_DEFAULTS,
    EVENT_VIEW_COMMAND_SELECT_HISTORY_ITEM,
    EVENT_HELP_REQUIRED,//The help should be displayed
    EVENT_COVER_REFRESH,//the cover should be refreshed
    EVENT_COVER_CHANGE,//Request for a cover change
    EVENT_PLAYER_STOP,//the stop button has been pressed
    EVENT_PLAYER_PLAY,  //the play button has been pressed
    EVENT_PLAYER_PAUSE,//the pause button has been pressed
    EVENT_PLAYER_RESUME,//the resume button has been pressed
    EVENT_PLAYLIST_REFRESH,
    EVENT_PLAYLIST_SELECTION_CHANGED,
    EVENT_FILE_LAUNCHED,//a file has been lauched by the fifo
    EVENT_HEART_BEAT,//heart beat for geenral use to refresh subscribers every n secs
    EVENT_ZERO, //a reinit has been required
    EVENT_ADD_HISTORY_ITEM, //a new element has been added in the history
    EVENT_SPECIAL_MODE, //special mode (global shuffle, novelties, bestof...) changed
    EVENT_PLAY_ERROR, //an error occured during a play
    EVENT_PLAY_FINISHED, //send at the end of a track
    EVENT_MUTE_STATE, //mute state changed
    EVENT_SYNC_TREE_TABLE, //sync table and tree views
    EVENT_CLEAR_HISTORY, //clear history
    EVENT_WIZARD, //launch first time wizard
    EVENT_QUALITY, //quality feedback agent
    EVENT_VOLUME_CHANGED, //volume changed
    EVENT_CREATE_PROPERTY, //create a new property
    EVENT_DELETE_PROPERTY, //delete property
    EVENT_CUSTOM_PROPERTIES_ADD, //new custom property
    EVENT_CUSTOM_PROPERTIES_REMOVE, //remove custom property
    EVENT_FILE_NAME_CHANGED, //file name change
    EVENT_STYLE_NAME_CHANGED, //Style name change
    EVENT_RATE_CHANGED, //file rate change
    EVENT_TIP_OF_THE_DAY, // show tip of the day
    EVENT_CDDB_WIZARD , //Cddb wizard required
    EVENT_LOGICAL_TREE_SORT , //logical tree osrt method changed
    EVENT_COVER_DEFAULT_CHANGED , //cover default changed
    EVENT_TABLE_CLEAR_SELECTION , //clear table selection
    EVENT_DJS_CHANGE , //DJ creation or removal
    EVENT_AMBIENCES_CHANGE , //One or more ambiences have been removed/added/changed
    EVENT_AMBIENCES_SELECTION_CHANGE , //user changed current ambience
    EVENT_AMBIENCE_REMOVED , //An ambience has been removed
    EVENT_AUTHOR_CHANGED , //Current played track author name has been changed
    EVENT_LANGUAGE_CHANGED //Language changed
}
