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
 *  
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

/**
 * DOCUMENT_ME.
 */
public class RestoreViewsAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new restore views action.
   */
  RestoreViewsAction() {
    super(Messages.getString("JajukJMenuBar.17"), IconLoader.getIcon(JajukIcons.REFRESH), true);
    setShortDescription(Messages.getString("JajukJMenuBar.17"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws JajukException {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        IPerspective perspective = PerspectiveManager.getCurrentPerspective();
        // Restore local or global views
        perspective.restoreDefaults();
      }

    });

  }
}
