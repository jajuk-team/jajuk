/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Manual preference change of current played track.
 */
public class ChangeTrackPreferenceAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new change track preference action.
   */
  ChangeTrackPreferenceAction() {
    super(Messages.getString("IncRateAction.0"), IconLoader.getIcon(JajukIcons.INC_RATING), true);
    setShortDescription(Messages.getString("IncRateAction.0"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws JajukException {
    new Thread("ChangeTrackPreferenceAction") {
      @Override
      public void run() {
        try {
          File file = QueueModel.getPlayingFile();
          if (file != null) {
            Track track = file.getTrack();
            track.setPreference(Conf.getInt(Const.CONF_INC_RATING));
          }
          // Force immediate rating refresh (without using the rating manager)
          ObservationManager.notify(new JajukEvent(JajukEvents.RATE_CHANGED));
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
