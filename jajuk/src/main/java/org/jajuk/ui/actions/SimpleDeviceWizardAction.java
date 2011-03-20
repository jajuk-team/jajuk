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

import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.wizard.SimpleDeviceWizard;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Action for displaying the simple device creation wizard.
 */
public class SimpleDeviceWizardAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new simple device wizard action.
   */
  SimpleDeviceWizardAction() {
    super(Messages.getString("JajukJMenuBar.18"), IconLoader.getIcon(JajukIcons.WIZARD), true);
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param evt DOCUMENT_ME
   */
  @Override
  public void perform(ActionEvent evt) {
    SimpleDeviceWizard wizard = new SimpleDeviceWizard();
    wizard.pack();
    wizard.setLocationRelativeTo(JajukMainWindow.getInstance());
    wizard.setVisible(true);
  }
}
