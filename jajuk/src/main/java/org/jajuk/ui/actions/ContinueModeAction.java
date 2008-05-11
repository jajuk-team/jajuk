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

import javax.swing.BorderFactory;

import org.jajuk.base.FileManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.widgets.CommandJPanel;
import org.jajuk.ui.widgets.JajukJMenuBar;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

public class ContinueModeAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  ContinueModeAction() {
    super(Messages.getString("JajukJMenuBar.12"), IconLoader.ICON_CONTINUE, true);
    setShortDescription(Messages.getString("CommandJPanel.3"));
  }

  public void perform(ActionEvent evt) throws JajukException {
    boolean b = ConfigurationManager.getBoolean(CONF_STATE_CONTINUE);
    ConfigurationManager.setProperty(CONF_STATE_CONTINUE, Boolean.toString(!b));

    JajukJMenuBar.getInstance().jcbmiContinue.setSelected(!b);
    CommandJPanel.getInstance().jbContinue.setSelected(!b);

    if (!b) { // enabled button
      CommandJPanel.getInstance().jbContinue.setBorder(BorderFactory.createLoweredBevelBorder());
      if (FIFO.isStopped()) {
        // if nothing playing, play next track if possible
        StackItem item = FIFO.getInstance().getLastPlayed();
        if (item != null) {
          FIFO.getInstance().push(
              new StackItem(FileManager.getInstance().getNextFile(item.getFile())), false);
        }
      }
    }
    // computes planned tracks
    FIFO.getInstance().computesPlanned(false);
    // Refresh Queue View
    ObservationManager.notify(new Event(EventSubject.EVENT_QUEUE_NEED_REFRESH));
  }
}
