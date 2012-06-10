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
import org.jajuk.util.UtilGUI;

/**
 * .
 */
public class RepeatModeAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

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
    boolean b = Conf.getBoolean(Const.CONF_STATE_REPEAT);
    UtilGUI.setRepeatSingleGui(!b);
    // disabling repeat for an item forced unset for all items and enabling single repeat unset
    // repeat for all items and then set it only for current track
    QueueModel.setRepeatModeToAll(false);

    if (!b) { // enabled button
      // if FIFO is not void, repeat over current item
      StackItem item = QueueModel.getCurrentItem();
      if (item != null) {
        item.setRepeat(true);
      }
    }
    // computes planned tracks
    QueueModel.computesPlanned(false);
    // Refresh Queue View
    ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
  }
}
