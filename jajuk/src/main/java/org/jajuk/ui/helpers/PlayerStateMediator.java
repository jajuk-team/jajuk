/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
package org.jajuk.ui.helpers;

import static org.jajuk.ui.actions.JajukActions.FINISH_ALBUM;
import static org.jajuk.ui.actions.JajukActions.FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PAUSE_RESUME_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;

import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.notification.INotificator;
import org.jajuk.services.notification.NotificatorFactory;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.MuteAction;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;

/**
 * This mediator observes events on player state and change actions (and player
 * buttons state) accordingly.
 */
public class PlayerStateMediator implements Observer {

  /** Singleton. */
  private static PlayerStateMediator self = new PlayerStateMediator();

  // Register this item, do not do this in the constructor as the instance is not yet available
  static {
    ObservationManager.register(self);
    // Update initial status
    UtilFeatures.updateStatus(self);
  }

  /**
   * Instantiates a new player state mediator.
   */
  private PlayerStateMediator() {
  }

  /**
   * Gets the single instance of PlayerStateMediator.
   * 
   * @return single instance of PlayerStateMediator
   */
  public static PlayerStateMediator getInstance() {
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.PLAYER_PLAY);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.PLAY_OPENING);
    eventSubjectSet.add(JajukEvents.PLAY_ERROR);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.VOLUME_CHANGED);
    eventSubjectSet.add(JajukEvents.MUTE_STATE);
    eventSubjectSet.add(JajukEvents.SHOW_CURRENTLY_PLAYING);

    // for notification display
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);

    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.Event)
   */
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.PLAYER_STOP.equals(subject)) {
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          // Enable the play button to allow restarting the queue but disable if
          // the queue is void
          boolean bQueueNotVoid = (QueueModel.getQueue().size() > 0);
          ActionManager.getAction(PAUSE_RESUME_TRACK).setEnabled(bQueueNotVoid);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(bQueueNotVoid);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(bQueueNotVoid);
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(bQueueNotVoid);
          ActionManager.getAction(NEXT_TRACK).setEnabled(bQueueNotVoid);

          setToPlay();
          ActionManager.getAction(STOP_TRACK).setEnabled(false);
          ActionManager.getAction(FORWARD_TRACK).setEnabled(false);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(false);
          // reset startup position
          Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, "0");
        } else if (JajukEvents.ZERO.equals(subject)) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(false);
          ActionManager.getAction(NEXT_TRACK).setEnabled(false);
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          ActionManager.getAction(PAUSE_RESUME_TRACK).setEnabled(false);
          ActionManager.getAction(STOP_TRACK).setEnabled(false);
          ActionManager.getAction(FORWARD_TRACK).setEnabled(false);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(false);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(false);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(false);
          setToPlay();
          // reset startup position
          Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, "0");
          ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
        } else if (JajukEvents.PLAYER_PLAY.equals(subject)) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(REWIND_TRACK).setEnabled(true);
          ActionManager.getAction(PAUSE_RESUME_TRACK).setEnabled(true);
          ActionManager.getAction(STOP_TRACK).setEnabled(true);
          ActionManager.getAction(FORWARD_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(true);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(true);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          setToPause();
        } else if (JajukEvents.PLAY_OPENING.equals(subject)
            || JajukEvents.PLAY_ERROR.equals(subject)) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          ActionManager.getAction(PAUSE_RESUME_TRACK).setEnabled(false);
          ActionManager.getAction(STOP_TRACK).setEnabled(true);
          ActionManager.getAction(FORWARD_TRACK).setEnabled(false);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(true);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(true);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
          setToPlay();
        } else if (JajukEvents.PLAYER_PAUSE.equals(subject)) {
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          ActionManager.getAction(FORWARD_TRACK).setEnabled(false);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          setToPlay();
        } else if (JajukEvents.PLAYER_RESUME.equals(subject)) {
          // Enable the volume when resuming (fix a mplayer issue, see
          // above)
          ActionManager.getAction(REWIND_TRACK).setEnabled(true);
          ActionManager.getAction(FORWARD_TRACK).setEnabled(true);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          setToPause();
        } else if (JajukEvents.WEBRADIO_LAUNCHED.equals(subject)) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(PAUSE_RESUME_TRACK).setEnabled(true);
          setToPause();
          ActionManager.getAction(STOP_TRACK).setEnabled(true);

          // display a system notification if specified
          INotificator notifier = NotificatorFactory.getNotificator();
          if (notifier != null) {
            WebRadio radio = (WebRadio) (event.getDetails().get(Const.DETAIL_CONTENT));
            Log.debug("Got update for new webradio launched, item: " + radio);
            notifier.notify(radio);
          }
        } else if (JajukEvents.VOLUME_CHANGED.equals(subject)) {
          MuteAction.setVolumeIcon(100 * Player.getCurrentVolume());
        } else if (JajukEvents.MUTE_STATE.equals(subject) &&
        // Update mute icon look when changing the volume
            !Player.isMuted()) {
          MuteAction.setVolumeIcon(Player.getCurrentVolume() * 100);
        } else if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
          INotificator notifier = NotificatorFactory.getNotificator();
          if (notifier != null) {
            String id = (String) ObservationManager.getDetail(event, Const.DETAIL_CURRENT_FILE_ID);
            if (id == null) {
              Log.debug("No id found on FILE_LAUNCHED");
              return;
            }

            File file = FileManager.getInstance().getFileByID(id);
            Log.debug("Got update for new file launched, item: {{" + file + "}}. Sending text: {{"
                + QueueModel.getCurrentFileTitle() + "}}");
            notifier.notify(file);
          }
        } else if (subject.equals(JajukEvents.SHOW_CURRENTLY_PLAYING)) {
          INotificator notifier = NotificatorFactory.getNotificator();
          if (notifier != null) {
            if (QueueModel.getCurrentRadio() != null) {
              Log.debug("Got request to notify with current webradio information: {{"
                  + QueueModel.getCurrentRadio() + "}}");
              notifier.notify(QueueModel.getCurrentRadio());
            } else {
              Log.debug("Got request to notify with current file information: {{"
                  + QueueModel.getCurrentItem().getFile() + "}}");
              notifier.notify(QueueModel.getCurrentItem().getFile());
            }
          }
        }

        // For all events except Volume Change/Mute, refresh the queue
        if (!JajukEvents.VOLUME_CHANGED.equals(subject) && !JajukEvents.MUTE_STATE.equals(subject)
            && !JajukEvents.FILE_LAUNCHED.equals(subject)
            && !JajukEvents.SHOW_CURRENTLY_PLAYING.equals(subject)) {
          ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
        }
      }
    });
  }

  /**
   * Set play/pause button label/icon to pause state.
   */
  private void setToPause() {
    ActionManager.getAction(PAUSE_RESUME_TRACK)
        .setIcon(IconLoader.getIcon(JajukIcons.PLAYER_PAUSE));
    ActionManager.getAction(PAUSE_RESUME_TRACK).setName(Messages.getString("JajukWindow.10"));
  }

  /**
   * Set play/pause button label/icon to play state.
   */
  private void setToPlay() {
    ActionManager.getAction(PAUSE_RESUME_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PLAYER_PLAY));
    ActionManager.getAction(PAUSE_RESUME_TRACK).setName(Messages.getString("JajukWindow.12"));
  }

}
