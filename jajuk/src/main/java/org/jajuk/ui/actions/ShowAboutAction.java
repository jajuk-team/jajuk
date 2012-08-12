/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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

import org.jajuk.ui.wizard.AboutWindow;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Action for displaying the tip of the day.
 */
public class ShowAboutAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new show about action.
   */
  ShowAboutAction() {
    super(Messages.getString("JajukWindow.5"), IconLoader.getIcon(JajukIcons.INFO), true);
    setShortDescription(Messages.getString("JajukWindow.22"));
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param evt 
   */
  @Override
  public void perform(ActionEvent evt) {
    new AboutWindow();
  }
}
