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
 *  $$Revision: 2403 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.JComponent;

import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.desktop.Desktop;

/**
 * 
 * Launch the URL from Util.url in the default browser
 */
public class LaunchInBrowserAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  LaunchInBrowserAction() {
    // this action is available only under linux and windows for now
    super(Messages.getString("LaunchInBrowserAction.0"), IconLoader.ICON_LAUNCH, (Util
        .isUnderLinux() || Util.isUnderWindows()));
    setShortDescription(Messages.getString("LaunchInBrowserAction.0"));
  }

  public void perform(ActionEvent evt) throws JajukException {
    try {
      JComponent source = (JComponent) evt.getSource();
      String url = (String) source.getClientProperty(DETAIL_CONTENT);
      Desktop.browse(new URL(url));
    } catch (Exception e) {
      Log.error(e);
    }
  }
}
