/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.services.notification;

import ext.ProcessLauncher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * An implementation of ISystemNotificator which uses the notify-send
 * functionality which is available on Linux/Unix systems.
 * 
 */
public class NotifySendNotificator implements ISystemNotificator {
  /**
   * The number of milliseconds to display the note
   */
  private static final String DISPLAY_TIME_MSECS = "8000";

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.ISystemNotificator#isAvailable()
   */
  @Override
  public boolean isAvailable() {
    // not possible on Windows right now
    if (UtilSystem.isUnderWindows()) {
      return false;
    }

    // check if we have "notify-send"
    List<String> list = new ArrayList<String>();
    list.add("notify-send");
    list.add("--help");

    // create streams for catching stdout and stderr
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int ret = 0;
    final ProcessLauncher launcher = new ProcessLauncher(out, err, 10000);
    try {
      ret = launcher.exec(list.toArray(new String[list.size()]));
    } catch (IOException e) {
      ret = -1;
      Log
          .debug("Exception while checking for 'notify-send', cannot use notification functionality: "
              + e.getMessage());
    }

    // if we do not find the application or if we got an error, log some details
    // and disable notification support
    if (ret != 0) {
      // log out the results
      Log.debug("notify-send command returned to out(" + ret + "): " + out.toString());
      Log.debug("notify-send command returned to err: " + err.toString());

      Log
          .info("Cannot use notify-send functionality, application 'notify-send' seems to be not available correctly.");
      return false;
    }

    // notify-send is enabled and seems to be supported by the OS
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.jajuk.services.notification.ISystemNotificator#notify(java.lang.String
   * , java.lang.String)
   */
  @Override
  public void notify(String title, String text) {
    // first build the commandline for "notify-send"

    // see http://www.galago-project.org/specs/notification/0.9/x344.html
    // and the manual page of "notify-send"
    List<String> list = new ArrayList<String>();
    list.add("notify-send");
    // show it for 5 seconds
    list.add("--expire-time=" + DISPLAY_TIME_MSECS);
    // use a non-standard category as there is currently none for media playing
    // events
    list.add("--category=music.started");
    list.add("--urgency=normal");

    // not sure if this works, it would disable any system-sound for this as it
    // is useless to play additional sound in this case, but it is just a hint
    // anyway,
    // Furthermore it should be "boolean" according to the spec, but Ubuntu
    // reports an error if I try to use that...
    list.add("--hint=byte:suppress-sound:1");

    // now add the actual information to the commandline
    list.add(title);
    list.add(text);

    // create streams for catching stdout and stderr
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int ret = 0;
    Log.debug("Using this notify-send command: {{" + list.toString() + "}}");
    final ProcessLauncher launcher = new ProcessLauncher(out, err, 10000);
    try {
      ret = launcher.exec(list.toArray(new String[list.size()]));
    } catch (IOException e) {
      ret = -1;
      Log.error(e);
    }

    // log out the results
    if (!out.toString().isEmpty()) {
      Log.debug("notify-send command returned to out(" + ret + "): " + out.toString());
    } else {
      Log.debug("notify-send command returned: " + ret);
    }

    if (!err.toString().isEmpty()) {
      Log.debug("notify-send command returned to err: " + err.toString());
    }
  }
}
