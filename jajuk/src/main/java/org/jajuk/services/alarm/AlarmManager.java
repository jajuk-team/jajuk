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

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.log.Log;

/**
 * Manages alarms
 */

public class AlarmManager {
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
        if (alarm != null && (alarm.getAlarmMilliSeconds() < System.currentTimeMillis())) {
          alarm.wakeUpSleeper();
        }
      }
    }
  };

  public static AlarmManager getInstance() {
    if (singleton == null) {
      singleton = new AlarmManager();
      // Start the clock
      singleton.clock.start();
    }
    return singleton;
  }
  
  public Alarm getAlarm(){
    return alarm;
  }

  public void setAlarm(Alarm aAlarm) {
    alarm = aAlarm;
    ObservationManager.notify(new Event(JajukEvents.ALARMS_CHANGE));
  }

  public void removeAlarm(Alarm aAlarm) {
    alarm = aAlarm;
    ObservationManager.notify(new Event(JajukEvents.ALARMS_CHANGE));
  }

}