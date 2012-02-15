/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.services.alarm;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.jajuk.base.File;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;

/**
 * An Alarm.
 */
public class Alarm {

  /** The files to play. */
  private List<File> alToPlay;

  /** The webradio to play. */
  private WebRadio radio;

  /** DOCUMENT_ME. */
  private String alarmAction;

  /** DOCUMENT_ME. */
  private Date aTime;

  /**
   * Instantiates a new alarm.
   * 
   * @param aTime DOCUMENT_ME
   * @param alFiles DOCUMENT_ME
   * @param mode DOCUMENT_ME
   */
  public Alarm(java.util.Date aTime, List<File> alFiles, String mode) {
    this.aTime = aTime;
    this.alToPlay = alFiles;
    this.alarmAction = mode;
  }

  /**
   * Instantiates a new alarm.
   * 
   * @param aTime DOCUMENT_ME
   * @param radio DOCUMENT_ME
   * @param mode DOCUMENT_ME
   */
  public Alarm(java.util.Date aTime, WebRadio radio, String mode) {
    this.aTime = aTime;
    this.radio = radio;
    this.alarmAction = mode;
  }

  /**
   * Effective action to perform by the alarm.
   */
  public void wakeUpSleeper() {
    Log.debug("Wake up at " + new Date());
    if (alarmAction.equals(Const.ALARM_START_ACTION)) {
      if (alToPlay != null) {
        QueueModel.push(UtilFeatures.createStackItems(alToPlay,
            Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), false), false);
      } else if (radio != null) {
        QueueModel.launchRadio(radio);

      }
    } else {
      QueueModel.stopRequest();
    }
  }

  /**
   * Gets the alarm time.
   * 
   * @return the alarm time
   */
  public Date getAlarmTime() {
    return this.aTime;
  }

  /**
   * Add 24 hours to current alarm.
   */
  public void nextDay() {
    aTime = DateUtils.addDays(aTime, 1);
  }

}
