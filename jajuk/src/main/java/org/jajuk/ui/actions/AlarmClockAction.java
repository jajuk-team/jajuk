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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.services.alarm.Alarm;
import org.jajuk.services.alarm.AlarmManager;
import org.jajuk.ui.widgets.AlarmClockDialog;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

public class AlarmClockAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  private int hours, minutes, seconds;

  private String alarmTime;

  private List<File> alToPlay;

  private String alarmMessage;

  private boolean alarmDaily;

  AlarmClockAction() {
    super(Messages.getString("AlarmClock.0"), IconLoader.getIcon(JajukIcons.ALARM), true);
    setShortDescription(Messages.getString("AlarmClock.0"));
  }

  @Override
  public void perform(ActionEvent evt) throws JajukException {
    AlarmClockDialog acDialog = new AlarmClockDialog();
    if (!acDialog.getChoice()) {
      return;
    }

    hours = Conf.getInt(Const.CONF_ALARM_TIME_HOUR);
    minutes = Conf.getInt(Const.CONF_ALARM_TIME_MINUTES);
    seconds = Conf.getInt(Const.CONF_ALARM_TIME_SECONDS);

    alarmDaily = Conf.getBoolean(Const.CONF_ALARM_DAILY);

    alarmMessage = Conf.getString(Const.CONF_ALARM_MESSAGE);
    String alarmAction = Conf.getString(Const.CONF_ALARM_ACTION);

    if (alarmAction.equals(Const.CONF_ALARM_START_MODE)) {
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

    alarmTime = hours + ":" + minutes + ":" + seconds;
    try {
      new SimpleDateFormat("HH:mm:ss").parse(alarmTime);
    } catch (ParseException e) {
      Messages.showErrorMessage(177);
      return;
    }
    Alarm aAlarm = new Alarm(alarmTime, alarmDaily, alToPlay, alarmAction, alarmMessage);
    AlarmManager.getInstance().setAlarm(aAlarm);
  }
}
