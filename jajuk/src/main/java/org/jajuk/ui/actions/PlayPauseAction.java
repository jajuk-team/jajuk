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

import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

public class PlayPauseAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  PlayPauseAction() {
    super(Messages.getString("JajukWindow.10"), IconLoader.getIcon(JajukIcons.PAUSE), "ctrl P", false, true);
    setShortDescription(Messages.getString("JajukWindow.26"));
  }

  @Override
  public void perform(ActionEvent evt) {
    if (FIFO.isStopped()) {
      FIFO.goTo(0);
      // ObservationManager.notify(new Event(JajukEvents.PLAYER_RESUME));
      setIcon(IconLoader.getIcon(JajukIcons.PAUSE));
      setName(Messages.getString("JajukWindow.12"));
    } else if (Player.isPaused()) { // player was paused, resume it
      Player.resume();
      setIcon(IconLoader.getIcon(JajukIcons.PAUSE));
      setName(Messages.getString("JajukWindow.10"));
    } else { // player is not paused, pause it
      Player.pause();
      // notify of this event
      setIcon(IconLoader.getIcon(JajukIcons.PLAY));
      setName(Messages.getString("JajukWindow.12"));
    }
  }
}
