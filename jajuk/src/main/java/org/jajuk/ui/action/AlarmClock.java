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
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import org.jajuk.ui.widgets.AlarmClockDialog;

import java.sql.Time;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

public class AlarmClock extends ActionBase {

  private static final long serialVersionUID = 1L;
  
  private static int hours, minutes, seconds;
  
  private static long alarmTime, currentTime;
   
  AlarmClock() {
    super(Messages.getString("AlarmClock.0"), IconLoader.ICON_ALARM, true);
    setShortDescription(Messages.getString("AlarmClock.0"));
  }
  
  public void perform(ActionEvent evt) throws JajukException {
    AlarmClockDialog acDialog = new AlarmClockDialog();
    if(!acDialog.getChoice())
      return;
        
    hours = ConfigurationManager.getInt(ALARM_TIME_HOUR);
    minutes = ConfigurationManager.getInt(ALARM_TIME_MINUTES);
    seconds = ConfigurationManager.getInt(ALARM_TIME_SECONDS);
    Calendar cal = Calendar.getInstance();
    alarmTime = Time.valueOf(hours+":"+minutes+":"+seconds).getTime();
    currentTime = Time.valueOf(cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND)).getTime();
    
    if ((alarmTime - currentTime) < 0){
      Messages.showWarningMessage("Time already elapsed!");
    }
    Log.debug(hours+":"+minutes+":"+seconds);
    Log.debug(cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
    Log.debug("Alarm Time: " + alarmTime);
    Log.debug("Current Time : " + currentTime);
    new AlarmThread().start();
  }
  
  public void wakeUpSleeper(){
    File file = FileManager.getInstance().getShuffleFile();
    List<File> alFiles = new ArrayList<File>();
    alFiles.add(file);
    FIFO.getInstance().push(Util.createStackItems(alFiles, ConfigurationManager.getBoolean(CONF_STATE_REPEAT), false),false);
  }
  
  class AlarmThread extends Thread{
    AlarmThread(){
      super();
    }
    public void run(){
      try{
        sleep(alarmTime - currentTime);
      }catch (InterruptedException e){}
      wakeUpSleeper();
    }
  }
}

