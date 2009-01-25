/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
package org.jajuk.ui.helpers;

import static org.jajuk.ui.actions.JajukActions.FAST_FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.FINISH_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;

import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.MuteAction;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;

/**
 * This mediator observes events on player state and change actions (and player
 * buttons state) accordingly
 */
public class PlayerStateMediator implements Observer {

  private static PlayerStateMediator self;

  private PlayerStateMediator() {
  }

  public static PlayerStateMediator getInstance() {
    if (self == null) {
      self = new PlayerStateMediator();
      ObservationManager.register(self);
      // Update initial status
      UtilFeatures.updateStatus(self);
    }
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.PLAYER_PLAY);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.PLAY_OPENING);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.VOLUME_CHANGED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.Event)
   */
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.PLAYER_STOP.equals(subject)) {
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          // Enable the play button to allow restarting the queue but disable if
          // the queue is void
          boolean bQueueNotVoid = (FIFO.getFIFO().size() > 0);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(bQueueNotVoid);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(bQueueNotVoid);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(bQueueNotVoid);
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(bQueueNotVoid);
          ActionManager.getAction(NEXT_TRACK).setEnabled(bQueueNotVoid);

          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PLAY));
          ActionManager.getAction(PLAY_PAUSE_TRACK).setName(Messages.getString("JajukWindow.12"));
          ActionManager.getAction(STOP_TRACK).setEnabled(false);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(false);
          // reset startup position
          Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, "0");
        } else if (JajukEvents.ZERO.equals(subject)) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(false);
          ActionManager.getAction(NEXT_TRACK).setEnabled(false);
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(false);
          ActionManager.getAction(STOP_TRACK).setEnabled(false);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(false);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(false);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(false);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PLAY));
          // reset startup position
          Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, "0");
          ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
        } else if (JajukEvents.PLAYER_PLAY.equals(subject)) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(REWIND_TRACK).setEnabled(true);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(true);
          ActionManager.getAction(STOP_TRACK).setEnabled(true);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(true);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(true);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PAUSE));
        } else if (JajukEvents.PLAY_OPENING.equals(subject) || JajukEvents.PLAY_ERROR.equals(subject)) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(false);
          ActionManager.getAction(STOP_TRACK).setEnabled(true);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(true);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(true);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PLAY));
        } else if (JajukEvents.PLAYER_PAUSE.equals(subject)) {
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PLAY));
        } else if (JajukEvents.PLAYER_RESUME.equals(subject)) {
          // Enable the volume when resuming (fix a mplayer issue, see
          // above)
          ActionManager.getAction(REWIND_TRACK).setEnabled(true);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PAUSE));
        } else if (JajukEvents.WEBRADIO_LAUNCHED.equals(event.getSubject())) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(true);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PAUSE));
          ActionManager.getAction(STOP_TRACK).setEnabled(true);
        } else if (JajukEvents.VOLUME_CHANGED.equals(event.getSubject())) {
          MuteAction.setVolumeIcon(100 * Player.getCurrentVolume());
        } else if (JajukEvents.MUTE_STATE.equals(event.getSubject()) &&
        // Update mute icon look when changing the volume
            !Player.isMuted()) {
          MuteAction.setVolumeIcon(Player.getCurrentVolume() * 100);
        }
        // For all events, refresh the queue
        ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
      }
    });
  }

}
