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
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.players.Player;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

public class PlayPauseAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  PlayPauseAction() {
    super(Messages.getString("JajukWindow.10"), IconLoader.ICON_PAUSE, "ctrl P", false, true);
    setShortDescription(Messages.getString("JajukWindow.26"));
  }

  public void perform(ActionEvent evt) {
    if (Player.isPaused()) { // player was paused, resume it
      Player.resume();
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYER_RESUME)); // notify
      // of
      // this
      // event
      setIcon(IconLoader.ICON_PAUSE);
      setName(Messages.getString("JajukWindow.10"));
    } else { // player is not paused, pause it
      Player.pause();
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYER_PAUSE));
      // notify of this event
      setIcon(IconLoader.ICON_PLAY);
      setName(Messages.getString("JajukWindow.12"));
    }
  }
}
