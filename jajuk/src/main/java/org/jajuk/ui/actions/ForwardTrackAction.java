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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import org.jajuk.services.players.Player;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Action class for fast forwarding the current track. Installed keystroke:
 * <code>CTRL + ALT + RIGHT ARROW</code>.
 */
public class ForwardTrackAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant JUMP_SIZE.  DOCUMENT_ME */
  private static final float JUMP_SIZE = 0.1f;

  /**
   * Instantiates a new forward track action.
   */
  ForwardTrackAction() {
    super(IconLoader.getIcon(JajukIcons.FWD), "altGraph F10", false, true);
    setShortDescription(Messages.getString("CommandJPanel.13"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) {
    new Thread("ForwardTrackAction") {
      @Override
      public void run() {
        try {
          float fCurrentPosition = Player.getCurrentPosition();
          Player.seek(fCurrentPosition + JUMP_SIZE);
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
