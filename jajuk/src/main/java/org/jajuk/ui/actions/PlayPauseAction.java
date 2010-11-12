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

import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * DOCUMENT_ME.
 */
public class PlayPauseAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private static boolean bAlreadyLaunching = false;

  /**
   * Instantiates a new play pause action.
   */
  PlayPauseAction() {
    super(Messages.getString("JajukWindow.10"), IconLoader.getIcon(JajukIcons.PLAYER_PAUSE),
        "ctrl P", false, true);
    setShortDescription(Messages.getString("JajukWindow.26"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) {
    // Note that we don't change here the label and icon, already done in
    // PlayerstateMediator

    if (QueueModel.isStopped()) {
      // We use here a flag to avoid launching the goTo() thread twice. In case
      // of playing error, this would create several looping threads trying to
      // play in concurrently and would broke the wait after an error contract
      if (bAlreadyLaunching) {
        return;
      }
      new Thread("PlayPause Thread") {
        @Override
        public void run() {
          try {
            bAlreadyLaunching = true;
            QueueModel.goTo(QueueModel.getIndex());
          } finally {
            bAlreadyLaunching = false;
          }
        }
      }.start();
    } else if (Player.isPaused()) { // player was paused, resume it
      Player.resume();
    } else { // player is not paused, pause it
      Player.pause();
    }
  }
}
