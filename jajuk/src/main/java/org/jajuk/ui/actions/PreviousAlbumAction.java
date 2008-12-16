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
 *  $$Revision:3308 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.util.log.Log;

/**
 * Action class for jumping to the previous album. Installed keystroke:
 * <code>CTRL + SHIFT + LEFT ARROW</code>.
 */
public class PreviousAlbumAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  PreviousAlbumAction() {
    super("previous album", "shift F9", false, true);
  }

  @Override
  public void perform(ActionEvent evt) {
    new Thread() {
      @Override
      public void run() {
        synchronized (FIFO.class) {
          try {
            FIFO.playPreviousAlbum();
          } catch (Exception e) {
            Log.error(e);
          }
          if (Player.isPaused()) { // player was paused, reset
            // pause button
            // when changing of track
            Player.setPaused(false);
            ObservationManager.notify(new Event(JajukEvents.PLAYER_RESUME));
          }
        }
      }
    }.start();
  }
}
