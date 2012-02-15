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

import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * DOCUMENT_ME.
 */
public class StopTrackAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new stop track action.
   */
  StopTrackAction() {
    super(Messages.getString("JajukWindow.27"), IconLoader.getIcon(JajukIcons.STOP_16X16),
        "ctrl S", false, false);
    setShortDescription(Messages.getString("JajukWindow.27"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) {
    new Thread("StopTrackAction") {
      @Override
      public void run() {
        try {
          QueueModel.stopRequest();

          // Save the stopped state (do not do it in the QueueModel.stopRequest() 
          // method because it must be set only on a human request, not at jajuk engine shutdown      
          Conf.setProperty(Const.CONF_STARTUP_STOPPED, "true");

        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
