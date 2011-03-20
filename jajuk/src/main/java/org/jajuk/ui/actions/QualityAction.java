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
 *  $Revision$
 */
package org.jajuk.ui.actions;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.net.URI;

import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * Action for collecting some system information and opening a browser-window
 * with the URL to report a new ticket. The user can then simply paste the
 * information into the ticket.
 */
public class QualityAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new quality action.
   */
  QualityAction() {
    super(Messages.getString("JajukJMenuBar.19"), IconLoader.getIcon(JajukIcons.EDIT), true);
  }

  /**
   * Invoked when the user chooses to report a ticket.
   * 
   * @param evt The event, not used currently.
   */
  @Override
  public void perform(ActionEvent evt) {
    StringBuilder sBody = new StringBuilder();
    sBody.append("Version: ").append(Const.JAJUK_VERSION).append('\n');
    sBody.append(UtilString.getAnonymizedSystemProperties().toString()).append('\n');
    sBody.append(UtilString.getAnonymizedJajukProperties().toString()).append('\n');
    for (String line : Log.getSpool()) {
      sBody.append(line).append('\n');
    }
    // if it is a bug, copy logs into the clipboard
    StringSelection data = new StringSelection(sBody.toString());
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(data, data);
    try {
      // Show Trac ticket creation page in an external browser
      Desktop.getDesktop().browse(new URI("http://trac.jajuk.info/newticket"));
      // Display a message
      Messages.showInfoMessage(Messages.getString("QualityFeedbackWizard.20"));
    } catch (Exception e) {
      Messages.showErrorMessage(136);
      Log.error(e);
    }

  }
}
