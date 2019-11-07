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

import org.jajuk.services.players.QueueModel;
import org.jajuk.util.log.Log;

/**
 * Action class for jumping to the previous album. Installed keystroke:
 * <code>CTRL + SHIFT + LEFT ARROW</code>.
 */
public class PreviousAlbumAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new previous album action.
   */
  PreviousAlbumAction() {
    super("previous album", "shift F9", false);
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) {
    new Thread("Previous Album Thread") {
      @Override
      public void run() {
        synchronized (QueueModel.class) {
          try {
            QueueModel.playPreviousAlbum();
          } catch (Exception e) {
            Log.error(e);
          }
        }
      }
    }.start();
  }
}
