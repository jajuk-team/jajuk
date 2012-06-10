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

import org.jajuk.ui.windows.JajukFullScreenWindow;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.windows.WindowStateDecorator;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * .
 */
public class FullscreenAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1223773056757729079L;

  /**
   * Instantiates a new fullscreen action.
   */
  FullscreenAction() {
    super(Messages.getString("JajukFullscreen.0"), IconLoader.getIcon(JajukIcons.FULL_SCREEN), true);
    setShortDescription(Messages.getString("JajukFullscreen.0"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws Exception {
    /*
     * If full screen window is visible, hide it and show the main window. Note
     * that both main window and fsw can"t be displayed at the same time:
     */
    WindowStateDecorator sdFullscreen = JajukFullScreenWindow.getInstance()
        .getWindowStateDecorator();
    WindowStateDecorator sdMainWindow = JajukMainWindow.getInstance().getWindowStateDecorator();
    if (sdFullscreen.isDisplayed()) {
      // close the previous window before displaying the other
      sdFullscreen.display(false);
      sdMainWindow.display(true);
      // Update the icon according to status
      setIcon(IconLoader.getIcon(JajukIcons.FULL_SCREEN));
    } else {
      sdFullscreen.display(true);
      sdMainWindow.display(false);
      setIcon(IconLoader.getIcon(JajukIcons.SLIM_WINDOW));
    }
  }
}
