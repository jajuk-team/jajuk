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
package org.jajuk.services.osd;

import ext.ProcessLauncher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * 
 */
public class OSDSupportImpl implements Observer {

  /** 
   * The number of milliseconds to display the OSD note 
   */
  private static final String DISPLAY_TIME_MSECS = "8000";

  /**
   * The static instance of the support implementation.
   */
  private static OSDSupportImpl support;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    final Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  @Override
  public void update(JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      String id = (String) ObservationManager.getDetail(event, Const.DETAIL_CURRENT_FILE_ID);
      File file = FileManager.getInstance().getFileByID(id);

      Log.debug("Got update for new file launched, item: " + file);

      displayOSD("Now playing", UtilString.buildTitle(file));
    } else if (subject.equals(JajukEvents.WEBRADIO_LAUNCHED)) {
      WebRadio radio = (WebRadio) (event.getDetails().get(Const.DETAIL_CONTENT));

      displayOSD("WebRadio", radio.getName());
    }
  }

  /** Method to do the actual call to "notify-send"
   * 
   * @param string The string to display via "notify-send"
   */
  private void displayOSD(String title, String string) {
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
    list.add(string);

    // create streams for catching stdout and stderr
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int ret = 0;
    Log.debug("Using this OSD command: {{" + list.toString() + "}}");
    final ProcessLauncher launcher = new ProcessLauncher(out, err, 10000);
    try {
      ret = launcher.exec(list.toArray(new String[list.size()]));
    } catch (IOException e) {
      ret = -1;
      Log.error(e);
    }

    // log out the results
    Log.debug("OSD command returned to out(" + ret + "): " + out.toString());
    Log.debug("OSD command returned to err: " + err.toString());
  }

  public static boolean isOSDAvailable() {
    // not possible on Windows right now
    if (UtilSystem.isUnderWindows()) {
      return false;
    }

    // don't do any further checking if it is disabled in the configuration
    if(!Conf.getBoolean(Const.CONF_UI_SHOW_OSD)) {
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
      Log.debug("Exception while checking for 'notify-send', cannot use OSD functionality: "
          + e.getMessage());
    }
    
    // if we do not find the application or if we got an error, log some details and disable OSD support
    if (ret != 0) {
      // log out the results
      Log.debug("OSD command returned to out(" + ret + "): " + out.toString());
      Log.debug("OSD command returned to err: " + err.toString());

      Log
          .info("Cannot use OSD functionality, application 'notify-send' seems to be not available correctly.");
      return false;
    }

    // OSD is enabled and seems to be supported by the OS
    return true;
  }

  /** Register support for OSD. The code should use "isOSDAvailable" before to check for availability.
   * 
   * @throws IllegalArgumentException If support for OSD is already registered.
   */
  public static void registerOSDSupport() {
    if (support != null) {
      throw new IllegalArgumentException("Cannot register OSD Support twice!");
    }

    support = new OSDSupportImpl();
    ObservationManager.register(support);
  }

  /** Unregisters support for OSD functionality.
   * 
   * If OSD support is not registered it still returns gracefully! 
   */
  public static void unregisterOSDSupport() {
    if (support == null) {
      return;
    }

    ObservationManager.unregister(support);
    support = null;
  }
}
