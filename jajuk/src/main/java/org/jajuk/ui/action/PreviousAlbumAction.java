/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */
package org.jajuk.ui.action;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Player;
import org.jajuk.util.EventSubject;
import org.jajuk.util.log.Log;

import java.awt.event.ActionEvent;

/**
 * Action class for jumping to the previous album. Installed keystroke:
 * <code>CTRL + SHIFT + LEFT ARROW</code>.
 */
public class PreviousAlbumAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  PreviousAlbumAction() {
    super("previous album", "shift F9", false, true);
  }

  public void perform(ActionEvent evt) {
    new Thread() {
      public void run() {
        synchronized (FIFO.MUTEX) {
          try {
            FIFO.getInstance().playPreviousAlbum();
          } catch (Exception e) {
            Log.error(e);
          }
          if (Player.isPaused()) { // player was paused, reset
            // pause button
            // when changing of track
            Player.setPaused(false);
            ObservationManager.notify(new Event(EventSubject.EVENT_PLAYER_RESUME)); // notify
            // of
            // this event
          }
        }
      }
    }.start();
  }
}
