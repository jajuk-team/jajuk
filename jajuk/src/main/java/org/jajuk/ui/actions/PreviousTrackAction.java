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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Action class for jumping to the previous track. Installed keystroke:
 * <code>CTRL + LEFT ARROW</code>.
 */
public class PreviousTrackAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  PreviousTrackAction() {
    super(Messages.getString("JajukWindow.13"), IconLoader.getIcon(JajukIcons.PLAYER_PREVIOUS),
        "F9", false, true);
    setShortDescription(Messages.getString("CommandJPanel.8"));
  }

  @Override
  public void perform(ActionEvent evt) {
    // check modifiers to see if it is a movement inside track, between
    // tracks or between albums
    if (evt != null &&
    // evt == null when using hotkeys
        (evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
      ActionManager.getAction(JajukActions.REPLAY_ALBUM).actionPerformed(evt);
    } else if (evt != null &&
    // evt == null when using hotkeys
        (evt.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
      ActionManager.getAction(JajukActions.PREVIOUS_ALBUM).actionPerformed(evt);
   } else {
      // if playing a radio, launch next radio station
      if (FIFO.isPlayingRadio()) {
        final List<WebRadio> radios = new ArrayList<WebRadio>(WebRadioManager.getInstance()
            .getWebRadios());
        int index = radios.indexOf(FIFO.getCurrentRadio());
        if (index == 0) {
          index = radios.size() - 1;
        } else {
          index--;
        }
        final int i = index;
        new Thread() {
          @Override
          public void run() {
            FIFO.launchRadio(radios.get(i));
          }
        }.start();
      } else {
        new Thread() {
          @Override
          public void run() {
            synchronized (FIFO.class) {
              try {
                FIFO.playPrevious();
              } catch (Exception e) {
                Log.error(e);
              }
              // Player was paused, reset pause button when
              // changing of track
              if (Player.isPaused()) {
                Player.setPaused(false);
                ObservationManager.notify(new Event(JajukEvents.PLAYER_RESUME));
              }
            }
          }
        }.start();

      }
    }
  }
}
