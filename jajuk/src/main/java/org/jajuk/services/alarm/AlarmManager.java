/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 *  $Revision: 3156 $
 *
 */

package org.jajuk.services.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Manages alarms
 */

public class AlarmManager implements Observer {
  private static AlarmManager singleton;

  private Alarm alarm;

  /**
   * This thread looks alarms up and call weak up when it's time
   */
  private Thread clock = new Thread() {
    @Override
    public void run() {
      boolean bstop = false;
      while (!bstop) {
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

  public static AlarmManager getInstance() {
    if (singleton == null) {
      singleton = new AlarmManager();
      // Start the clock
      singleton.clock.start();
      ObservationManager.register(singleton);
      // force last event update
      singleton.update(new Event(JajukEvents.ALARMS_CHANGE));
    }
    return singleton;
  }

  public void update(Event event) {
    JajukEvents subject = event.getSubject();
    // Reset rate and total play time (automatic part of rating system)
    if (subject.equals(JajukEvents.ALARMS_CHANGE)) {
      Date alarmDate = null;
      if (Conf.getBoolean(Const.CONF_ALARM_ENABLED)) {
        int hours = Conf.getInt(Const.CONF_ALARM_TIME_HOUR);
        int minutes = Conf.getInt(Const.CONF_ALARM_TIME_MINUTES);
        int seconds = Conf.getInt(Const.CONF_ALARM_TIME_SECONDS);
        String alarmAction = Conf.getString(Const.CONF_ALARM_ACTION);
        Calendar cal = Calendar.getInstance();
        try {
          cal.set(Calendar.HOUR_OF_DAY, hours);
          cal.set(Calendar.MINUTE, minutes);
          cal.set(Calendar.SECOND, seconds);
          // If chosen date is already past, consider that user meant
          // tomorrow
          alarmDate = cal.getTime();
          if (alarmDate.before(new Date())) {
            alarmDate = new Date(alarmDate.getTime() + Const.DAY_MS);
          }
        } catch (Exception e) {
          Log.error(e);
          return;
        }
        // Compute playlist if required
        List<File> alToPlay = null;
        if (alarmAction.equals(Const.ALARM_START_MODE)) {
          alToPlay = new ArrayList<File>();
          if (Conf.getString(Const.CONF_ALARM_MODE).equals(Const.STARTUP_MODE_FILE)) {
            File fileToPlay = FileManager.getInstance().getFileByID(
                Conf.getString(Const.CONF_ALARM_FILE));
            alToPlay.add(fileToPlay);
          } else if (Conf.getString(Const.CONF_ALARM_MODE).equals(Const.STARTUP_MODE_SHUFFLE)) {
            alToPlay = FileManager.getInstance().getGlobalShufflePlaylist();
          } else if (Conf.getString(Const.CONF_ALARM_MODE).equals(Const.STARTUP_MODE_BESTOF)) {
            alToPlay = FileManager.getInstance().getGlobalBestofPlaylist();
          } else if (Conf.getString(Const.CONF_ALARM_MODE).equals(Const.STARTUP_MODE_NOVELTIES)) {
            alToPlay = FileManager.getInstance().getGlobalNoveltiesPlaylist();
          }
        }
        alarm = new Alarm(alarmDate, alToPlay, alarmAction);
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> keys = new HashSet<JajukEvents>();
    keys.add(JajukEvents.ALARMS_CHANGE);
    return keys;
  }

}