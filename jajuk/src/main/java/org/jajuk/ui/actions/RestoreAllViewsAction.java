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
import java.io.File;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

public class RestoreAllViewsAction extends ActionBase implements ITechnicalStrings {

  private static final long serialVersionUID = 1L;

  public static boolean fullRestore = false;

  RestoreAllViewsAction() {
    super(Messages.getString("JajukJMenuBar.26"), IconLoader.ICON_RESTORE_ALL_VIEWS, true);
    setShortDescription(Messages.getString("JajukJMenuBar.26"));
  }

  public void perform(final ActionEvent e) throws JajukException {
    new Thread() {
      public void run() {
        // display a confirmation message
        int i = Messages.getChoice(Messages.getString("Confirmation_restore_all"),
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (i != JOptionPane.YES_OPTION) {
          return;
        }
        // Drop all perspectives conf
        for (IPerspective perspective : PerspectiveManager.getPerspectives()) {
          File loadFile = Util.getConfFileByPath(perspective.getClass().getSimpleName() + ".xml");
          loadFile.delete();
        }
        // Delete toolbars configuration too
        Util.getConfFileByPath(FILE_TOOLBARS_CONF).delete();
        // Indicates to not commiting current configuration
        fullRestore = true;
        // Exit Jajuk
        new Thread() {
          public void run() {
            Main.exit(0);
          }
        }.start();
      }
    }.start();

  }
}
