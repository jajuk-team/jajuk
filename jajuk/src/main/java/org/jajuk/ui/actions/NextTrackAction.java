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

import org.jajuk.events.JajukEvent;
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
 * Action class for jumping to the next track. Installed keystroke:
 * <code>CTRL + RIGHT ARROW</code>.
 */
public class NextTrackAction extends JajukAction {
  private static final long serialVersionUID = 1L;

  NextTrackAction() {
    super(Messages.getString("JajukWindow.14"), IconLoader.getIcon(JajukIcons.PLAYER_NEXT), "F10",
        false, true);
    setShortDescription(Messages.getString("CommandJPanel.9"));
  }

  @Override
  public void perform(ActionEvent evt) {
    // check modifiers to see if it is a movement inside track, between
    // tracks or between albums
    if (evt != null
    // evt == null when using hotkeys
        && (evt.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
      ActionManager.getAction(JajukActions.NEXT_ALBUM).actionPerformed(evt);
    } else {
      // if playing a radio, launch next radio station
      if (FIFO.isPlayingRadio()) {
        final List<WebRadio> radios = new ArrayList<WebRadio>(WebRadioManager.getInstance()
            .getWebRadios());
        int index = radios.indexOf(FIFO.getCurrentRadio());
        if (index == radios.size() - 1) {
          index = 0;
        } else {
          index++;
        }
        final int i = index;
        new Thread() {
          @Override
          public void run() {
            FIFO.launchRadio(radios.get(i));
          }
        }.start();

      } else {
        // Playing a track
        new Thread() {
          @Override
          public void run() {
            synchronized (FIFO.class) {
              try {
                FIFO.playNext();
              } catch (Exception e) {
                Log.error(e);
              }
              // Player was paused, reset pause button when
              // changing of track
              if (Player.isPaused()) {
                Player.setPaused(false);
                ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_RESUME));
              }
            }
          }
        }.start();
      }
    }
  }
}
