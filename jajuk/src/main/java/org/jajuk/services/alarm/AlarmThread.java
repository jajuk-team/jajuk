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
 *  $$Revision: 3156 $$
 */
package org.jajuk.services.alarm;

import java.sql.Time;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.services.players.FIFO;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * Class for Alarm Threads
 */
public class AlarmThread extends Thread implements ITechnicalStrings {
  private String alarmTime;
  private List<File> alToPlay;
  private String alarmAction;
  private String alarmMessage;
  private boolean alarmDaily;
  private long alarmMilliSeconds;

  public AlarmThread(String aTime, boolean daily, List<File> alFiles, String mode, String message) {
    super();
    alarmTime = aTime;
    alarmMilliSeconds = Time.valueOf(alarmTime).getTime();
    alToPlay = alFiles;
    alarmAction = mode;
    alarmMessage = message;
    alarmDaily = daily;
  }

  public void wakeUpSleeper() {
    if (alarmAction.equals(ITechnicalStrings.ALARM_START_MODE)) {
      FIFO.getInstance()
          .push(
              Util.createStackItems(alToPlay, ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
                  false), false);
    } else {
      FIFO.getInstance().stopRequest();
    }
    if (!isDaily()) {
      AlarmThreadManager.getInstance().removeAlarm(this);
    } else {
      this.alarmMilliSeconds += 24 * 3600 * 1000;
    }
    if (!Util.isVoid(alarmMessage))
      Messages.showWarningMessage(Messages.getString("AlarmClock.5") + " \n" + getAlarmTime() + " "
          + alarmMessage);
  }

  public String getAlarmTime() {
    return this.alarmTime;
  }

  public long getAlarmMilliSeconds() {
    return alarmMilliSeconds;
  }

  public String getAlarmText() {
    if (!"".equals(alarmMessage))
      return Messages.getString("Stop") + ": " + alarmMessage + " "
          + (isDaily() ? Messages.getString("AlarmDialog.8") : "") + " "
          + Messages.getString("AlarmClock.3") + " @ " + getAlarmTime();
    else
      return Messages.getString("Stop") + ": "
          + (isDaily() ? Messages.getString("AlarmDialog.8") : "") + " "
          + Messages.getString("AlarmClock.3") + " @ " + getAlarmTime();
  }

  public boolean isDaily() {
    return alarmDaily;
  }
}