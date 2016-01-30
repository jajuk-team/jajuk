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

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Single repeat mode action
 */
@SuppressWarnings("serial")
public class RepeatModeAction extends JajukAction {
  /**
   * Instantiates a new repeat mode action.
   */
  RepeatModeAction() {
    super(Messages.getString("JajukJMenuBar.10"), IconLoader.getIcon(JajukIcons.REPEAT), "ctrl T",
        true, false);
    setShortDescription(Messages.getString("CommandJPanel.1"));
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param evt 
   */
  @Override
  public void perform(ActionEvent evt) {
    boolean newSingleRepeatState = !Conf.getBoolean(Const.CONF_STATE_REPEAT);
    Conf.setProperty(Const.CONF_STATE_REPEAT, Boolean.toString(newSingleRepeatState));
    //Disable repeat mode for every item (we will set it again later if required) 
    QueueModel.setRepeatModeToAll(false);
    if (newSingleRepeatState) {
      // single repeat and repeat all modes are mutually exclusive
      Conf.setProperty(Const.CONF_STATE_REPEAT_ALL, Const.FALSE);
      // if FIFO is not void, repeat over current item
      StackItem item = QueueModel.getCurrentItem();
      if (item != null) {
        item.setRepeat(true);
      }
    }
    // Computes planned tracks without clearing existing planned tracks if any
    QueueModel.computesPlanned(false);
    // Refresh mode buttons
    ObservationManager.notify(new JajukEvent(JajukEvents.MODE_STATUS_CHANGED));
    // Refresh Queue View
    ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
  }
}
