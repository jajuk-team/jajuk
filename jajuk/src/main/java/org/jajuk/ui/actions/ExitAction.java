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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jajuk.services.core.ExitService;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.windows.JajukFullScreenWindow;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.windows.JajukSlimbar;
import org.jajuk.ui.windows.JajukSystray;
import org.jajuk.ui.windows.WindowState;
import org.jajuk.ui.windows.WindowStateDecorator;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

public class ExitAction extends JajukAction {
  private static final long serialVersionUID = 1L;

  ExitAction() {
    super(Messages.getString("JajukWindow.4"), IconLoader.getIcon(JajukIcons.EXIT), "alt X", true,
        false);
    setShortDescription(Messages.getString("JajukWindow.21"));
  }

  @Override
  public void perform(ActionEvent evt) {
    // Ask if a confirmation is required
    if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_EXIT)) {
      int iResu = Messages.getChoice(Messages.getString("Confirmation_exit"),
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        return;
      }
    }
    // IMPORTANT: all the following code must be done in EDT to avoid dead
    // locks.
    // Not not use SwingUtilities.invokeLater method in the ExitHook Thread,
    // this code may never be run

    if (SwingUtilities.isEventDispatchThread()) {
      // commit perspectives if no full restore
      // engaged. Perspective should be commited before the window
      // being closed to avoid a dead lock in VLDocking
      if (!RestoreAllViewsAction.isFullRestore()
          && JajukMainWindow.getInstance().getWindowStateDecorator().isDisplayed()) {
        try {
          PerspectiveManager.commit();
        } catch (Exception e) {
          Log.error(e);
        }
      }

      // Store window/tray/slimbar configuration

      WindowStateDecorator sdSlimbar = JajukSlimbar.getInstance().getWindowStateDecorator();
      WindowStateDecorator sdMainWindow = JajukMainWindow.getInstance().getWindowStateDecorator();
      WindowStateDecorator sdTray = JajukSystray.getInstance().getWindowStateDecorator();
      WindowStateDecorator sdfullscreen = JajukFullScreenWindow.getInstance()
          .getWindowStateDecorator();

      // Set main window display at next startup as a default
      Conf
          .setProperty(Const.CONF_STARTUP_DISPLAY, Integer.toString(Const.DISPLAY_MODE_MAIN_WINDOW));

      if (sdSlimbar.getWindowState() == WindowState.BUILD_DISPLAYED) {
        Conf.setProperty(Const.CONF_STARTUP_DISPLAY, Integer
            .toString(Const.DISPLAY_MODE_SLIMBAR_TRAY));
      }
      
      if (sdMainWindow.isDisplayed()) {
        Conf.setProperty(Const.CONF_STARTUP_DISPLAY, Integer
            .toString(Const.DISPLAY_MODE_MAIN_WINDOW));
      }
      
      // None window displayed ? set the tray only (if the show tray option is set)
      if (!sdSlimbar.isDisplayed() && !sdMainWindow.isDisplayed() && !sdfullscreen.isDisplayed()
          && Conf.getBoolean(Const.CONF_SHOW_SYSTRAY)) {
        Conf.setProperty(Const.CONF_STARTUP_DISPLAY, Integer.toString(Const.DISPLAY_MODE_TRAY));
      }

      if (sdfullscreen.getWindowState() == WindowState.BUILD_DISPLAYED) {
        Conf.setProperty(Const.CONF_STARTUP_DISPLAY, Integer
            .toString(Const.DISPLAY_MODE_FULLSCREEN));
      }

      // hide windows ASAP
      sdMainWindow.display(false);

      // hide systray
      sdTray.display(false);

      // Hide slimbar
      sdSlimbar.display(false);

      // Hide full screen
      sdfullscreen.display(false);

    }
    // Exit Jajuk
    ExitService.exit(0);
  }
}
