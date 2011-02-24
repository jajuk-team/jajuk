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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import org.jajuk.services.core.ExitService;
import org.jajuk.services.core.SessionService;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

/**
 * DOCUMENT_ME.
 */
public class RestoreAllViewsAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private static boolean fullRestore = false;

  /**
   * Instantiates a new restore all views action.
   */
  RestoreAllViewsAction() {
    super(Messages.getString("JajukJMenuBar.26"), IconLoader.getIcon(JajukIcons.RESTORE_ALL_VIEWS),
        true);
    setShortDescription(Messages.getString("JajukJMenuBar.26"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws JajukException {
    new Thread("Restore All Views Thread") {
      @Override
      public void run() {
        // display a confirmation message
        int i = Messages.getChoice(Messages.getString("Confirmation_restore_all"),
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (i != JOptionPane.YES_OPTION) {
          return;
        }
        // Drop all perspectives conf
        for (IPerspective perspective : PerspectiveManager.getPerspectives()) {
          File loadFile = SessionService.getConfFileByPath(perspective.getClass().getSimpleName()
              + ".xml");
          // Lazy deletion, this file have already been removed by a previous reset
          loadFile.delete();
        }
        // Indicates to not commiting current configuration
        fullRestore = true;
        // Exit Jajuk
        new Thread("Restore All Views - Exit Thread") {
          @Override
          public void run() {
            ExitService.exit(0);
          }
        }.start();
      }
    }.start();

  }

  /**
   * Checks if is full restore.
   * 
   * @return true, if is full restore
   */
  public static boolean isFullRestore() {
    return fullRestore;
  }
}
