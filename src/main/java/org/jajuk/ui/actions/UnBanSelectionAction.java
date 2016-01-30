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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * .
 */
public class UnBanSelectionAction extends SelectionAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * The UnBan action is used to un-ban a set of tracks
   * <p>
   * Selection action
   * </p>.
   */
  UnBanSelectionAction() {
    super(Messages.getString("UnBanSelectionAction.0"), IconLoader.getIcon(JajukIcons.UNBAN), true);
    setShortDescription(Messages.getString("UnBanSelectionAction.1"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.SelectionAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws Exception {
    new Thread("UnBanSelectionAction") {
      @Override
      public void run() {
        try {
          UnBanSelectionAction.super.perform(e);
          // Check selection is not void
          if (selection.size() == 0) {
            return;
          }
          // Extract tracks of each item
          List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(selection, false);
          // Then ban them all !
          for (Track track : tracks) {
            track.setProperty(Const.XML_TRACK_BANNED, false);
          }
          // Request a GUI refresh
          ObservationManager.notify(new JajukEvent(JajukEvents.RATE_CHANGED));
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
