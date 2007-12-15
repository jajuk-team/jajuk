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
package org.jajuk.ui.action;

import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.desktop.Desktop;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Action for displaying the tip of the day.
 */
public class QualityAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  QualityAction() {
    super(Messages.getString("JajukJMenuBar.19"), IconLoader.ICON_EDIT, true);
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param evt
   */
  public void perform(ActionEvent evt) {
    String sBody = "";
    sBody += "Version: " + JAJUK_VERSION + '\n';
    sBody += Util.getAnonymizedSystemProperties().toString() + '\n';
    sBody += Util.getAnonymizedJajukProperties().toString() + '\n';
    for (String line : Log.getSpool()) {
      sBody += line + '\n';
    }
    // if it is a bug, copy logs into the clipboard
    StringSelection data = new StringSelection(sBody);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(data, data);
    try {
      // Show Trac ticket creation page in an external browser
      URL url = new URL("http://trac.jajuk.info/newticket");
      Desktop.browse(url);
      // Display a message
      Messages.showInfoMessage(Messages.getString("QualityFeedbackWizard.20"));
    } catch (Exception e) {
      Messages.showErrorMessage(136);
      Log.error(e);
    }

  }
}
