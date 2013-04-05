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
package org.jajuk.services.core;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dbus.DBusManager;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * All code related to jajuk exit.
 */
public class ExitService extends Thread {
  /** Exit code. */
  private static int iExitCode = 0;
  /** Exiting flag. */
  private volatile static boolean bExiting = false;

  /**
   * Instantiates a new exit service.
   */
  public ExitService() {
    super("Exit hook thread");
  }

  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    Log.debug("Exit Hook begin");
    // Store webradio state
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, Boolean.toString(QueueModel.isPlayingRadio()));
    // stop sound ASAP
    Player.stop(true);
    ObservationManager.notifySync(new JajukEvent(JajukEvents.EXITING));
    try {
      // commit only if exit is safe (to avoid commiting empty collection) 
      if (iExitCode == 0) {
        // Disconnect Dbus if required
        DBusManager.disconnect();
        /* release keystrokes resources */
        JajukAction.cleanup();
      }
    } catch (Exception e) {
      Log.error(e);
    } finally {
      Log.debug("Exit Hook end");
    }
  }

  /**
   * Exit code, then system will execute the exit hook.
   * 
   * @param iExitCode exit code
   * <p>
   * 0 : normal exit
   * <p>
   * 1: unexpected error
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
   * Checks if is exiting.
   * 
   * @return Returns whether jajuk is in exiting state
   */
  public static boolean isExiting() {
    return bExiting;
  }
}
