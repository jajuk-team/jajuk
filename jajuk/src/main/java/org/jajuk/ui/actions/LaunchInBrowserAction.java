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
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JComponent;

import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Launch the URL from UtilFeatures.url in the default browser
 */
public class LaunchInBrowserAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new launch in browser action.
   */
  LaunchInBrowserAction() {
    // this action is available only under GNU/Linux and windows for now
    super(Messages.getString("LaunchInBrowserAction.0"), IconLoader.getIcon(JajukIcons.LAUNCH),
        UtilSystem.isBrowserSupported());
    setShortDescription(Messages.getString("LaunchInBrowserAction.0"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent evt) throws JajukException {
    new Thread("LaunchInBrowserAction") {
      @Override
      public void run() {
        try {
          JComponent source = (JComponent) evt.getSource();
          String url = (String) source.getClientProperty(Const.DETAIL_CONTENT);
          // If URL is a file, open the file with default editor
          File file = new File(url);
          if (file.exists()) {
            Desktop.getDesktop().open(file);
          } else {
            // Open a browser for HTTP URLs
            Desktop.getDesktop().browse(new URI(url));
          }
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
