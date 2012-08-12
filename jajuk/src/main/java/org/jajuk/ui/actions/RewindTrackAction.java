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

import org.jajuk.services.players.Player;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Action class for rewinding the current track. Installed keystroke:
 * <code>CTRL + ALT + LEFT ARROW</code>.
 */
public class RewindTrackAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** The Constant JUMP_SIZE.   */
  private static final float JUMP_SIZE = 0.1f;

  /**
   * Instantiates a new rewind track action.
   */
  RewindTrackAction() {
    super(IconLoader.getIcon(JajukIcons.REW), "altGraph F9", false, true);
    setShortDescription(Messages.getString("CommandJPanel.10"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent evt) {
    new Thread("RewindTrackAction") {
      @Override
      public void run() {
        try {
          /*
           * check modifiers to see if it is a movement inside track, between
           * tracks or between albums
           */
          if (evt != null
          // evt == null when using hotkeys
              && (evt.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
            // replay the entire file
            Player.seek(0);
          } else {
            float fCurrentPosition = Player.getCurrentPosition();
            Player.seek(fCurrentPosition - JUMP_SIZE);
          }
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
