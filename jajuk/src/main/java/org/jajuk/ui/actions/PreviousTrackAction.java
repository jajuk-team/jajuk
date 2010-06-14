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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
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

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new previous track action.
   */
  PreviousTrackAction() {
    super(Messages.getString("JajukWindow.13"), IconLoader
        .getIcon(JajukIcons.PLAYER_PREVIOUS_SMALL), "F9", false, true);
    setShortDescription(Messages.getString("CommandJPanel.8"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent evt) {
    // check modifiers to see if it is a movement inside track, between
    // tracks or between albums
    if (evt != null &&
    // evt == null when using hotkeys
        (evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
      // CTRL + previous = replay album
      ActionManager.getAction(JajukActions.REPLAY_ALBUM).actionPerformed(evt);
    } else if (evt != null
        && (evt.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
      // SHIFT + previous = replay album
      ActionManager.getAction(JajukActions.PREVIOUS_ALBUM).actionPerformed(evt);
    } else {
      // if playing a radio, launch next radio station
      if (QueueModel.isPlayingRadio()) {
        final List<WebRadio> radios = new ArrayList<WebRadio>(WebRadioManager.getInstance()
            .getWebRadios());
        int index = radios.indexOf(QueueModel.getCurrentRadio());
        if (index == 0) {
          index = radios.size() - 1;
        } else {
          index--;
        }
        final int i = index;
        new Thread("Previous Track Thread") {
          @Override
          public void run() {
            QueueModel.launchRadio(radios.get(i));
          }
        }.start();
      } else {
        new Thread("Previous Track Thread") {
          @Override
          public void run() {
            synchronized (QueueModel.class) {
              // ALT + previous = replay track
              if (evt != null
                  && (evt.getModifiers() == 4332424 )) {
                // replay the entire file
                Player.seek(0);
              } else {
                // No key modifier : play previous track
                try {
                  QueueModel.playPrevious();
                } catch (Exception e) {
                  Log.error(e);
                }
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
