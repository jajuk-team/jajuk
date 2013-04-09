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

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.windows.JajukSlimbar;
import org.jajuk.ui.windows.WindowStateDecorator;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Action to hide slim bar.
 */
public class SlimbarAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new slimbar action.
   */
  SlimbarAction() {
    super(Messages.getString("JajukSlimWindow.0"), IconLoader.getIcon(JajukIcons.SLIM_WINDOW), true);
    setShortDescription(Messages.getString("JajukSlimWindow.0"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws Exception {
    /*
     * If slimbar is visible, hide it and show the main window. Note that both main window and
     * slimbar can be displayed at the same time: If the slimbar is visible and user display main
     * window by right clicking on the tray, the main window is displayed, this is a normal behavior
     */
    WindowStateDecorator sdSlimbar = JajukSlimbar.getInstance().getWindowStateDecorator();
    WindowStateDecorator sdMainWindow = JajukMainWindow.getInstance().getWindowStateDecorator();
    if (sdSlimbar.isDisplayed()) {
      // close the previous window before displaying the other
      sdSlimbar.display(false);
      sdMainWindow.display(true);
      // Update the icon according to status
      setIcon(IconLoader.getIcon(JajukIcons.SLIM_WINDOW));
    } else {
      sdMainWindow.display(false);
      sdSlimbar.display(true);
      // Update the icon according to status
      setIcon(IconLoader.getIcon(JajukIcons.FULL_SCREEN));
    }
    // Notify that slimbar visibility change (menu bar is interested in it)
    ObservationManager.notify(new JajukEvent(JajukEvents.SLIMBAR_VISIBILTY_CHANGED));
  }
}