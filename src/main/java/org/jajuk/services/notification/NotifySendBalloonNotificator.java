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
package org.jajuk.services.notification;

import ext.ProcessLauncher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * An implementation of INotificator which uses the notify-send functionality
 * which is available on Linux/Unix systems.
 * 
 * <p>
 * Singleton
 * </p>
 */
public class NotifySendBalloonNotificator implements INotificator {
  /** The number of milliseconds to display the note. */
  private static final String DISPLAY_TIME_MSECS = "8000";
  /** Self instance *. */
  private static NotifySendBalloonNotificator self = new NotifySendBalloonNotificator();
  /** Availability state [perf] *. */
  private boolean availability = false;

  /**
   * Instantiates a new notify send balloon notificator.
   */
  private NotifySendBalloonNotificator() {
    // Get availability once for all
    populateAvailability();
  }

  /**
   * Return an instance of this singleton.
   * 
   * @return an instance of this singleton
   */
  public static NotifySendBalloonNotificator getInstance() {
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#isAvailable()
   */
  @Override
  public boolean isAvailable() {
    return availability;
  }

  /**
   * Computes notificator availability.
   */
  private void populateAvailability() {
    // not possible on Windows right now
    if (UtilSystem.isUnderWindows()) {
      availability = false;
      return;
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
      Log.debug("Exception while checking for 'notify-send', cannot use notification functionality: "
          + e.getMessage());
    }
    // if we do not find the application or if we got an error, log some details
    // and disable notification support
    if (ret != 0) {
      // log out the results
      Log.debug("notify-send command returned to out(" + ret + "): " + out.toString());
      Log.debug("notify-send command returned to err: " + err.toString());
      Log.info("Cannot use notify-send functionality, application 'notify-send' seems to be not available correctly.");
      availability = false;
      return;
    }
    // notify-send is enabled and seems to be supported by the OS
    availability = true;
  }

  /*
   * Notification from two strings (code shared between webradio and track notifications)
   */
  /**
   * Notify. 
   * 
   * @param title 
   * @param pText 
   */
  @Override
  public void notify(String title, String pText) {
    // workaround: notify-send cannot handle IMG-SRC with "file:"
    String text = pText.replace("<img src='file:/", "<img src='/");
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
    } else if (ret != 0) {
      Log.debug("notify-send command returned: " + ret);
    }
    if (!err.toString().isEmpty()) {
      Log.debug("notify-send command returned to err: " + err.toString());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#notify(org.jajuk.services. webradio.WebRadio)
   */
  @Override
  public void notify(WebRadio webradio) {
    String title = Messages.getString("Notificator.track_change.webradio_title");
    String text = webradio.getName();
    notify(title, text);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#notify(org.jajuk.base.File)
   */
  @Override
  public void notify(File file) {
    String title = Messages.getString("Notificator.track_change.track_title");
    String pattern = Conf.getString(Const.CONF_PATTERN_BALLOON_NOTIFIER);
    String text;
    try {
      text = UtilString.applyPattern(file, pattern, false, false);
      notify(title, text);
    } catch (JajukException e) {
      Log.error(e);
    }
  }
}
