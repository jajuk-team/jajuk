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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.services.alarm.AlarmThread;
import org.jajuk.services.alarm.AlarmThreadManager;
import org.jajuk.ui.widgets.AlarmClockDialog;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

public class AlarmClockAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  private static int hours, minutes, seconds;

  private static String alarmTime, currentTime;

  private List<File> alToPlay;

  private static String alarmMessage;

  private static boolean alarmDaily;

  AlarmClockAction() {
    super(Messages.getString("AlarmClock.0"), IconLoader.ICON_ALARM, true);
    setShortDescription(Messages.getString("AlarmClock.0"));
  }

  public void perform(ActionEvent evt) throws JajukException {
    AlarmClockDialog acDialog = new AlarmClockDialog();
    if (!acDialog.getChoice())
      return;

    hours = ConfigurationManager.getInt(ALARM_TIME_HOUR);
    minutes = ConfigurationManager.getInt(ALARM_TIME_MINUTES);
    seconds = ConfigurationManager.getInt(ALARM_TIME_SECONDS);

    alarmDaily = ConfigurationManager.getBoolean(CONF_ALARM_DAILY);

    alarmMessage = ConfigurationManager.getProperty(ALARM_MESSAGE);
    String alarmAction = ConfigurationManager.getProperty(CONF_ALARM_ACTION);

    if (alarmAction.equals(ITechnicalStrings.ALARM_START_MODE)) {
      alToPlay = new ArrayList<File>();
      if (ConfigurationManager.getProperty(CONF_ALARM_MODE).equals(STARTUP_MODE_FILE)) {
        File fileToPlay = FileManager.getInstance().getFileByID(
            ConfigurationManager.getProperty(CONF_ALARM_FILE));
        alToPlay.add(fileToPlay);
      } else if (ConfigurationManager.getProperty(CONF_ALARM_MODE).equals(STARTUP_MODE_SHUFFLE)) {
        alToPlay = FileManager.getInstance().getGlobalShufflePlaylist();
      } else if (ConfigurationManager.getProperty(CONF_ALARM_MODE).equals(STARTUP_MODE_BESTOF)) {
        alToPlay = FileManager.getInstance().getGlobalBestofPlaylist();
      } else if (ConfigurationManager.getProperty(CONF_ALARM_MODE).equals(STARTUP_MODE_NOVELTIES)) {
        alToPlay = FileManager.getInstance().getGlobalNoveltiesPlaylist();
      }
    }

    Calendar cal = Calendar.getInstance();
    alarmTime = hours + ":" + minutes + ":" + seconds;
    currentTime = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
        + cal.get(Calendar.SECOND);

    if ((Time.valueOf(alarmTime).getTime() - Time.valueOf(currentTime).getTime()) < 0
        && !alarmDaily) {
      Messages.showWarningMessage(Messages.getString("AlarmClock.4"));
    }

    AlarmThread aAlarm = new AlarmThread(alarmTime, alarmDaily, alToPlay, alarmAction, alarmMessage);
    aAlarm.start();
    AlarmThreadManager.getInstance().addAlarm(aAlarm);
  }
}
