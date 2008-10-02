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

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.widgets.CommandJPanel;
import org.jajuk.ui.widgets.JajukJMenuBar;
import org.jajuk.util.Conf;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

public class ShuffleModeAction extends JajukAction {
  private static final long serialVersionUID = 1L;

  ShuffleModeAction() {
    super(Messages.getString("JajukJMenuBar.11"), IconLoader.getIcon(JajukIcons.SHUFFLE), "ctrl H", true, false);
    setShortDescription(Messages.getString("CommandJPanel.2"));
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param evt
   */
  @Override
  public void perform(ActionEvent evt) {
    boolean b = Conf.getBoolean(CONF_STATE_SHUFFLE);
    Conf.setProperty(CONF_STATE_SHUFFLE, Boolean.toString(!b));

    JajukJMenuBar.getInstance().setShuffleSelected(!b);
    CommandJPanel.getInstance().setRandomSelected(!b);
    if (!b) { // enabled button
      FIFO.shuffle(); // shuffle current selection
      // now make sure we can't have a single repeated file after a
      // non-repeated file (by design)
      if (FIFO.containsRepeat() && !FIFO.containsOnlyRepeat()) {
        FIFO.setRepeatModeToAll(false); // yes?
        // un-repeat all
      }
      // computes planned tracks
      FIFO.computesPlanned(true);
      // Refresh Queue View
      ObservationManager.notify(new Event(JajukEvents.QUEUE_NEED_REFRESH));
    }

  }
}
