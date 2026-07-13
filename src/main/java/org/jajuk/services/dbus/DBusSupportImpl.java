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
package org.jajuk.services.dbus;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.jajuk.base.Item;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.bookmark.Bookmarks;
import org.jajuk.services.notification.INotificator;
import org.jajuk.services.notification.NotificatorFactory;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import java.util.HashSet;
import java.util.Set;

public class DBusSupportImpl implements DBusSupport, Observer {

  private final DBusConnection connection;
  private static final String OBJECT_PATH = "/JajukDBus";

  public DBusSupportImpl(DBusConnection conn) {
    this.connection = conn;
  }

  @Override
  public String getObjectPath() {
    return OBJECT_PATH;
  }

  @Override
  public boolean isRemote() {
    return false;
  }

  public void cleanup() {
    ObservationManager.unregister(this);
  }

  /* --- Méthodes DBusSupport (identique) --- */

  @Override
  public void previous() throws Exception {
    invokeAction(JajukActions.PREVIOUS_TRACK);
  }

  @Override
  public void next() throws Exception {
    invokeAction(JajukActions.NEXT_TRACK);
  }

  @Override
  public void playPause() throws Exception {
    invokeAction(JajukActions.PAUSE_RESUME_TRACK);
  }

  @Override
  public void stop() throws Exception {
    invokeAction(JajukActions.STOP_TRACK);
  }

  @Override
  public void forward() throws Exception {
    invokeAction(JajukActions.FORWARD_TRACK);
  }

  @Override
  public void rewind() throws Exception {
    invokeAction(JajukActions.REWIND_TRACK);
  }

  @Override
  public void exit() throws Exception {
    invokeAction(JajukActions.EXIT);
  }

  @Override
  public void increaseVolume() throws Exception {
    invokeAction(JajukActions.INCREASE_VOLUME);
  }

  @Override
  public void decreaseVolume() throws Exception {
    invokeAction(JajukActions.DECREASE_VOLUME);
  }

  @Override
  public void mute() throws Exception {
    invokeAction(JajukActions.MUTE_STATE);
  }

  @Override
  public void shuffleGlobal() throws Exception {
    invokeAction(JajukActions.SHUFFLE_GLOBAL);
  }

  @Override
  public void previousAlbum() throws Exception {
    invokeAction(JajukActions.PREVIOUS_ALBUM);
  }

  @Override
  public void nextAlbum() throws Exception {
    invokeAction(JajukActions.NEXT_ALBUM);
  }

  @Override
  public String currentHTML() throws Exception {
    return QueueModel.getCurrentFileTitle();
  }

  @Override
  public String current() throws Exception {
    org.jajuk.base.File file = QueueModel.getPlayingFile();
    if (QueueModel.isPlayingRadio())
      return QueueModel.getCurrentRadio().getName();
    else if (file != null && !QueueModel.isStopped()) {
      String pattern = Conf.getString(Const.CONF_PATTERN_FRAME_TITLE);
      try {
        return UtilString.applyPattern(file, pattern, false, false);
      } catch (JajukException e) {
        Log.error("Error parsing track info", e);
      }
    }
    return "not playing right now...";
  }

  @Override
  public void banCurrent() throws Exception {
    invokeAction(JajukActions.BAN);
  }

  @Override
  public void showCurrentlyPlaying() throws Exception {
    ObservationManager.notify(new JajukEvent(JajukEvents.SHOW_CURRENTLY_PLAYING));
  }

  @Override
  public void bookmarkCurrentlyPlaying() throws Exception {
    org.jajuk.base.File file = QueueModel.getPlayingFile();
    if (!QueueModel.isPlayingRadio() && file != null && !QueueModel.isStopped()) {
      Bookmarks.getInstance().addFile(file);
      INotificator notifier = NotificatorFactory.getNotificator();
      if (notifier != null) {
        String pattern = Conf.getString(Const.CONF_PATTERN_BALLOON_NOTIFIER);
        String text = UtilString.applyPattern(file, pattern, false, false);
        notifier.notify("Bookmarked", text);
      }
    }
  }

  private void invokeAction(JajukActions action) {
    try {
      ActionManager.getAction(action).perform(null);
    } catch (Exception e) {
      Log.error("Failed to execute D-Bus action: " + e.getMessage(), e);
    }
  }

  /* --- Observer Implementation --- */

  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> keys = new HashSet<>();
    keys.add(JajukEvents.FILE_LAUNCHED);
    return keys;
  }

  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      String id = (String) ObservationManager.getDetail(event, Const.DETAIL_CURRENT_FILE_ID);
      Item item = org.jajuk.base.FileManager.getInstance().getItemByID(id);
      Log.debug("Got update for new file launched, item: " + item);

      if (connection != null && connection.isConnected()) {
        sendFileChangedSignal(item.toString());
      } else {
        Log.warn("Cannot send DBus Signal when not connected!");
      }
    } else {
      Log.warn("Unexpected subject received in Observer: " + event);
    }
  }

  /**
   * Alternative method: Build raw signal message manually.
   */
  private void sendFileChangedSignal(String filepath) {
    //try {
      Log.info("TO BE IMPLEMENTED -> " + filepath);
    //} catch (DBusException e) {
    //  Log.error("Failed to build/send raw signal", e);
    //}
  }
}