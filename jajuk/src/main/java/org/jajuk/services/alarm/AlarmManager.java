/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

package org.jajuk.services.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;

/**
 * Manages alarms
 * 
 * TODO: We could use Timer instead of implementing the Timer loop ourselves here!.
 * TODO : multi-alarms management
 */

public class AlarmManager implements Observer {

  /** DOCUMENT_ME. */
  private static AlarmManager singleton = new AlarmManager();

  static {
    // Start the clock
    singleton.clock.start();

    // register the instance so that it receives updates of changes to the configured Alarm
    ObservationManager.register(singleton);

    // force last event update
    singleton.update(new JajukEvent(JajukEvents.ALARMS_CHANGE));
  }

  /** DOCUMENT_ME. */
  private Alarm alarm;

  /** This thread looks alarms up and call weak up when it's time. */
  private final Thread clock = new Thread("Alarm manager Thread") {

    @Override
    public void run() {
      Log.debug("Starting Alarm thread");
      while (!ExitService.isExiting()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Log.error(e);
        }
        // Wake up if the alarm is enabled and if it's time
        if (Conf.getBoolean(Const.CONF_ALARM_ENABLED) && alarm != null
            && System.currentTimeMillis() > alarm.getAlarmTime().getTime()) {
          alarm.wakeUpSleeper();
          // Add 24 hours to current alarm
          alarm.nextDay();
        }
      }
    }
  };

  /**
   * Gets the single instance of AlarmManager.
   * 
   * @return single instance of AlarmManager
   */
  public static AlarmManager getInstance() {
    return singleton;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    // Reset rate and total play time (automatic part of rating system)
    if (subject.equals(JajukEvents.ALARMS_CHANGE)) {
      if (Conf.getBoolean(Const.CONF_ALARM_ENABLED)) {

        // construct a Date with the configured alarm-time
        int hours = Conf.getInt(Const.CONF_ALARM_TIME_HOUR);
        int minutes = Conf.getInt(Const.CONF_ALARM_TIME_MINUTES);
        int seconds = Conf.getInt(Const.CONF_ALARM_TIME_SECONDS);
        String alarmAction = Conf.getString(Const.CONF_ALARM_ACTION);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);

        // If chosen date is already past, consider that user meant
        // tomorrow
        Date alarmDate = cal.getTime();
        if (alarmDate.before(new Date())) {
          alarmDate = DateUtils.addDays(alarmDate, 1);
        }
        // Compute playlist if required
        List<File> alToPlay = null;
        WebRadio radio = null;
        Ambience ambience = AmbienceManager.getInstance().getSelectedAmbience();
        if (alarmAction.equals(Const.ALARM_START_ACTION)) {
          String mode = Conf.getString(Const.CONF_ALARM_MODE);
          String conf = Conf.getString(Const.CONF_ALARM_FILE);
          String item = conf.substring(conf.indexOf('/') + 1, conf.length());
          alToPlay = new ArrayList<File>();
          if (mode.equals(Const.STARTUP_MODE_ITEM)) {
            if (conf.matches(SearchResultType.FILE.name() + ".*")) {
              File file = FileManager.getInstance().getFileByID(item);
              if (file != null) {
                alToPlay.add(file);
              }
            } else if (conf.matches(SearchResultType.WEBRADIO.name() + ".*")) {
              radio = WebRadioManager.getInstance().getWebRadioByName(item);
            }

          } else if (mode.equals(Const.STARTUP_MODE_SHUFFLE)) {
            // Filter files by ambience or if none ambience matches, perform a global shuffle 
            // ignoring current ambience
            alToPlay = UtilFeatures.filterByAmbience(FileManager.getInstance()
                .getGlobalShufflePlaylist(), ambience);
            if (alToPlay.size() == 0) {
              alToPlay = FileManager.getInstance().getGlobalShufflePlaylist();
            }
          } else if (mode.equals(Const.STARTUP_MODE_BESTOF)) {
            alToPlay = UtilFeatures.filterByAmbience(FileManager.getInstance()
                .getGlobalBestofPlaylist(), ambience);
            if (alToPlay.size() == 0) {
              alToPlay = FileManager.getInstance().getGlobalBestofPlaylist();
            }

          } else if (mode.equals(Const.STARTUP_MODE_NOVELTIES)) {
            alToPlay = UtilFeatures.filterByAmbience(FileManager.getInstance()
                .getGlobalNoveltiesPlaylist(), ambience);
            if (alToPlay.size() == 0) {
              alToPlay = FileManager.getInstance().getGlobalNoveltiesPlaylist();
            }
          } else {
            Log.warn("Undefined alarm mode found: " + mode);
          }
        }
        // Instanciate either the files list or the web radio
        if (radio == null) {
          alarm = new Alarm(alarmDate, alToPlay, alarmAction);
        } else {
          alarm = new Alarm(alarmDate, radio, alarmAction);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> keys = new HashSet<JajukEvents>();
    keys.add(JajukEvents.ALARMS_CHANGE);
    return keys;
  }

}