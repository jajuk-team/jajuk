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

import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.widgets.JajukJMenuBar;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

/**
 * .
 */
public class ContinueModeAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new continue mode action.
   */
  ContinueModeAction() {
    super(Messages.getString("JajukJMenuBar.12"), IconLoader.getIcon(JajukIcons.CONTINUE), true);
    setShortDescription(Messages.getString("CommandJPanel.3"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws JajukException {
    boolean b = Conf.getBoolean(Const.CONF_STATE_CONTINUE);
    Conf.setProperty(Const.CONF_STATE_CONTINUE, Boolean.toString(!b));
    JajukJMenuBar.getInstance().setContinueSelected(!b);
    if (!b) { // enabled button
      if (QueueModel.isStopped()) {
        // if nothing playing, play next track if possible
        StackItem item = QueueModel.getLastPlayed();
        if (item != null) { //NOSONAR
          QueueModel.push(new StackItem(FileManager.getInstance().getNextFile(item.getFile())),
              false);
        }
      }
    }
    // computes planned tracks
    QueueModel.computesPlanned(false);
    // Refresh Queue View
    ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
  }
}
