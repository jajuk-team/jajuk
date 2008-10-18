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
 * Action for displaying the tip of the day.
 */
public class QualityAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  QualityAction() {
    super(Messages.getString("JajukJMenuBar.19"), IconLoader.getIcon(JajukIcons.EDIT), true);
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param evt
   */
  @Override
  public void perform(ActionEvent evt) {
    String sBody = "";
    sBody += "Version: " + Const.JAJUK_VERSION + '\n';
    sBody += UtilString.getAnonymizedSystemProperties().toString() + '\n';
    sBody += UtilString.getAnonymizedJajukProperties().toString() + '\n';
    for (String line : Log.getSpool()) {
      sBody += line + '\n';
    }
    // if it is a bug, copy logs into the clipboard
    StringSelection data = new StringSelection(sBody);
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
