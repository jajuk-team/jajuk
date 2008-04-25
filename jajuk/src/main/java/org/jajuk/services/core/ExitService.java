/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.services.core;

import java.io.File;
import java.net.InetAddress;

import org.jajuk.Main;
import org.jajuk.base.Collection;
import org.jajuk.base.DeviceManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.actions.ActionBase;
import org.jajuk.ui.actions.RestoreAllViewsAction;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.JajukSlimWindow;
import org.jajuk.ui.widgets.JajukSystray;
import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * All code related to jajuk exit
 */
public class ExitService extends Thread implements ITechnicalStrings {

    /** Exit code */
  private static int iExitCode = 0;
  
  /** Exiting flag */
  private static boolean bExiting = false;




  public ExitService() {
    super("Exit hook thread");
  }

  public void run() {
    Log.debug("Exit Hook begin");
    try {
      // stop sound ASAP
      Player.stop(true);

      // commit perspectives if no full restore
      // engaged. Perspective should be commited before the window
      // being closed to avoid a dead lock in VLDocking
      if (!RestoreAllViewsAction.fullRestore) {
        try {
          PerspectiveManager.commit();
        } catch (Exception e) {
          Log.error(e);
        }
      }
      // Store window/tray/slimbar configuration
      if (JajukSlimWindow.getInstance().isVisible()) {
        ConfigurationManager.setProperty(CONF_STARTUP_DISPLAY, Integer
            .toString(DISPLAY_MODE_SLIMBAR_TRAY));
      }
      if (JajukWindow.getInstance().isVisible()) {
        ConfigurationManager.setProperty(CONF_STARTUP_DISPLAY, Integer
            .toString(DISPLAY_MODE_WINDOW_TRAY));
      }

      if (!JajukSlimWindow.getInstance().isVisible() && !JajukWindow.getInstance().isVisible()) {
        ConfigurationManager.setProperty(CONF_STARTUP_DISPLAY, Integer.toString(DISPLAY_MODE_TRAY));
      }
      // hide window ASAP
      if (Main.getWindow() != null) {
        Main.getWindow().setVisible(false);
      }
      // hide systray
      if (JajukSystray.getInstance() != null) {
        JajukSystray.getInstance().closeSystray();
      }
      // Hide slimbar
      JajukSlimWindow.getInstance().setVisible(false);
    } catch (Exception e) {
      e.printStackTrace();
      // no log to make sure to reach collection
      // commit
    }
    try {
      if (iExitCode == 0) {
        // Store current FIFO for next session
        FIFO.getInstance().commit();
        // commit only if exit is safe (to avoid
        // commiting
        // empty collection) commit ambiences
        AmbienceManager.getInstance().commit();
        // Commit webradios
        WebRadioManager.getInstance().commit();
        // Store webradio state
        ConfigurationManager.setProperty(CONF_WEBRADIO_WAS_PLAYING, Boolean.toString(FIFO
            .getInstance().isPlayingRadio()));

        // commit configuration
        org.jajuk.util.ConfigurationManager.commit();
        // commit history
        History.commit();
        // Commit collection if not refreshing
        if (!DeviceManager.getInstance().isAnyDeviceRefreshing()) {
          Collection.commit(Util.getConfFileByPath(FILE_COLLECTION_EXIT));
          // create a proof file
          Util.createEmptyFile(Util.getConfFileByPath(FILE_COLLECTION_EXIT_PROOF));
        }
        /* release keystrokes resources */
        ActionBase.cleanup();

        // Remove localhost_<user> session files
        // (can occur when network is not available)
        File sessionUser = Util.getConfFileByPath(FILE_SESSIONS + "/localhost" + '_'
            + System.getProperty("user.name"));
        sessionUser.delete();
        // Remove session flag. Exception can be
        // thrown here if loopback interface is not
        // correctly set up, so should be the last
        // thing to do
        sessionUser = Util.getConfFileByPath(FILE_SESSIONS + '/'
            + InetAddress.getLocalHost().getHostName() + '_' + System.getProperty("user.name"));
        sessionUser.delete();

      }
    } catch (Exception e) {
      // don't use Log class here, it can cause freeze
      // if
      // workspace no more available
      e.printStackTrace();
    } finally {
      // don't use Log class here, it can cause freeze
      // if workspace is no more available
      System.out.println("Exit Hook end");
    }
  }
  
  /**
   * Exit code, then system will execute the exit hook
   * 
   * @param iExitCode
   *          exit code
   *          <p>
   *          0 : normal exit
   *          <p>
   *          1: unexpected error
   */
  public static void exit(final int iExitCode) {
    // set exiting flag
    bExiting = true;
    // store exit code to be read by the system hook
    ExitService.iExitCode = iExitCode;
    // display a message
    Log.debug("Exit with code: " + iExitCode);
    System.exit(iExitCode);
  }
  
   /**
   * @return Returns whether jajuk is in exiting state
   */
  public static boolean isExiting() {
    return bExiting;
  }

}
