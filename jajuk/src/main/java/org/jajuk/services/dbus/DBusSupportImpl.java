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
package org.jajuk.services.dbus;

/**
 * Provides implementation of the D-Bus interface and implementation of the
 * D-Bus support code for connecting to D-Bus
 */
import static org.jajuk.ui.actions.JajukActions.DECREASE_VOLUME;
import static org.jajuk.ui.actions.JajukActions.EXIT;
import static org.jajuk.ui.actions.JajukActions.FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.INCREASE_VOLUME;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PAUSE_RESUME_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.SHUFFLE_GLOBAL;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;

import java.util.HashSet;
import java.util.Set;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * 
 */
public class DBusSupportImpl implements DBusSupport, Observer {

  /**
   * The D-Bus Path that is used
   */
  private static final String PATH = "/JajukDBus";

  /**
   * The D-Bus name of the Bus that we request
   */
  private static final String BUS = "org.jajuk.dbus.DBusSupport";
  DBusConnection conn;

  /**
   * Set up the D-Bus connection and export an object to allow other
   * applications to control Jajuk via D-Bus.
   * 
   * This will catch errors and report them to the logfile.
   * 
   * Scope is package protected to only let DBusManager have access to it.
   */
  void connect() {
    Log.info("Trying to start support for D-Bus on Linux with Bus: ");

    try {
      conn = DBusConnection.getConnection(DBusConnection.SESSION);
      conn.requestBusName(BUS);

      conn.exportObject(PATH, this);
    } catch (DBusException e) {
      Log.error(e);
    }

    // register to player events
    ObservationManager.register(this);
  }

  /**
   * Disconnects from D-Bus.
   * 
   * Scope is package protected to only let DBusManager have access to it.
   */
  void disconnect() {
    Log.info("Disconnecting from D-Bus");
    if (conn != null) {
      conn.disconnect();
    }
  }

  /*
   * Interface methods to react on D-Bus signals
   * 
   * These methods are invoked via D-Bus and trigger the corresponding action in
   * Jajuk
   * 
   */

  public void forward() throws Exception {
    Log.info("Invoking D-Bus action for 'forward'");
    ActionManager.getAction(FORWARD_TRACK).perform(null);
  }

  public void next() throws Exception {
    Log.info("Invoking D-Bus action for 'next'");
    ActionManager.getAction(NEXT_TRACK).perform(null);
  }

  public void playPause() throws Exception {
    Log.info("Invoking D-Bus action for 'play/pause'");
    ActionManager.getAction(PAUSE_RESUME_TRACK).perform(null);
  }

  public void previous() throws Exception {
    Log.info("Invoking D-Bus action for 'previous'");
    ActionManager.getAction(PREVIOUS_TRACK).perform(null);
  }

  public void rewind() throws Exception {
    Log.info("Invoking D-Bus action for 'rewind'");
    ActionManager.getAction(REWIND_TRACK).perform(null);
  }

  public void stop() throws Exception {
    Log.info("Invoking D-Bus action for 'stop'");
    ActionManager.getAction(STOP_TRACK).perform(null);
  }

  public void decreaseVolume() throws Exception {
    Log.info("Invoking D-Bus action for 'decreaseVolume'");
    ActionManager.getAction(DECREASE_VOLUME).perform(null);
  }

  public void exit() throws Exception {
    Log.info("Invoking D-Bus action for 'exit'");
    ActionManager.getAction(EXIT).perform(null);
  }

  public void increaseVolume() throws Exception {
    Log.info("Invoking D-Bus action for 'increaseVolume'");
    ActionManager.getAction(INCREASE_VOLUME).perform(null);
  }

  public void nextAlbum() throws Exception {
    Log.info("Invoking D-Bus action for 'nextAlbum'");
    ActionManager.getAction(NEXT_ALBUM).perform(null);
  }

  public void previousAlbum() throws Exception {
    Log.info("Invoking D-Bus action for 'previousAlbum'");
    ActionManager.getAction(PREVIOUS_ALBUM).perform(null);
  }

  public void shuffleGlobal() throws Exception {
    Log.info("Invoking D-Bus action for 'shuffleGlobal'");
    ActionManager.getAction(SHUFFLE_GLOBAL).perform(null);
  }

  public void mute() throws Exception {
    Log.info("Invoking D-Bus action for 'mute'");
    ActionManager.getAction(JajukActions.MUTE_STATE).perform(null);
  }

  public String currentHTML() throws Exception {
    Log.info("Invoking D-Bus action for 'currentHTML'");
    return QueueModel.getCurrentFileTitle();
  }

  public String current() throws Exception {
    Log.info("Invoking D-Bus action for 'current'");

    String title = null;
    File file = QueueModel.getPlayingFile();
    if (QueueModel.isPlayingRadio()) {
      title = QueueModel.getCurrentRadio().getName();
    } else if (file != null && !QueueModel.isStopped()) {
      title = file.getTrack().getAuthor().getName() + " - " + file.getTrack().getAlbum().getName() + " - " + file.getTrack().getName();      
    } else {
      title = "not playing right now...";
    }
    return title;
  }

  /**
   * Required method for DBusInterface
   */
  public boolean isRemote() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> keys = new HashSet<JajukEvents>();
//    keys.add(JajukEvents.PLAYER_STOP);
//    keys.add(JajukEvents.PLAYER_PAUSE);
//    keys.add(JajukEvents.PLAYER_RESUME);
    keys.add(JajukEvents.FILE_LAUNCHED);
    return keys;
  }

  /* (non-Javadoc)
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    // Reset rate and total play time (automatic part of rating system)
    if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      String id = (String)ObservationManager.getDetail(event, Const.DETAIL_CURRENT_FILE_ID);
      Item item = FileManager.getInstance().getItemByID(id);
      
      Log.debug("Got update for new file launched, item: " + item);

      try {
        conn.sendSignal(new DBusSignalImpl.FileChangedSignal("testfile: " + item, PATH));
      } catch (DBusException e) {
        Log.error(e);
      }
      
    }
    else
    {
      Log.warn("Unexpected subject received in Observer: " + event);
    }
    
  }
}
