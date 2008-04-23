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
 *  $$Revision$$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

public class ExitAction extends ActionBase {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  ExitAction() {
    super(Messages.getString("JajukWindow.4"), IconLoader.ICON_EXIT, "alt X", true, false);
    setShortDescription(Messages.getString("JajukWindow.21"));
  }

  public void perform(ActionEvent evt) {
    // Ask if a confirmation is required
    if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_EXIT)) {
      int iResu = Messages.getChoice(Messages.getString("Confirmation_exit"),
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        return;
      }
    }
    // Exit Jajuk
    new Thread() {
      public void run() {
        Main.exit(0);
      }
    }.start();
  }
}
