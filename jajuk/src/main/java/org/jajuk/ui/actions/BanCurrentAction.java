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

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * DOCUMENT_ME.
 */
public class BanCurrentAction extends SelectionAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Ban / Unban current track. The Ban action is used to ban a track so it is
   * never selected
   */
  BanCurrentAction() {
    super(Messages.getString("BanSelectionAction.0"), IconLoader.getIcon(JajukIcons.BAN), true);
    setShortDescription(Messages.getString("BanSelectionAction.1"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.SelectionAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent e) throws Exception {
    File current = QueueModel.getPlayingFile();
    if (current != null) {
      Track track = current.getTrack();
      boolean alreadyBanned = track.getBooleanValue(Const.XML_TRACK_BANNED);
      track.setProperty(Const.XML_TRACK_BANNED, !alreadyBanned);
      // Request a GUI refresh
      ObservationManager.notify(new JajukEvent(JajukEvents.RATE_CHANGED));
      // Alert GUI so we can switch buttons from ban icon to unban one
      ObservationManager.notify(new JajukEvent(JajukEvents.BANNED));
      // Go to next track if it is banned
      if (ActionManager.getAction(JajukActions.NEXT_TRACK).isEnabled()
          && track.getBooleanValue(Const.XML_TRACK_BANNED)) {
        ActionManager.getAction(JajukActions.NEXT_TRACK).perform(null);
      }
    }
  }

}
