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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.base.File;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestAlarm extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.alarm.Alarm#Alarm(java.util.Date, java.util.List, java.lang.String)}
   * .
   */

  public void testAlarm() {
    new Alarm(new Date(), new ArrayList<File>(), "mode");
  }

  /**
   * Test method for {@link org.jajuk.services.alarm.Alarm#wakeUpSleeper()}.
   */

  public void testWakeUpSleeper() {
    Alarm alarm = new Alarm(new Date(), new ArrayList<File>(), "mode");
    alarm.wakeUpSleeper();

    List<File> list = new ArrayList<File>();
    alarm = new Alarm(new Date(), list, Const.ALARM_START_ACTION);
    alarm.wakeUpSleeper();
  }

  /**
   * Test method for {@link org.jajuk.services.alarm.Alarm#getAlarmTime()}.
   */

  public void testGetAlarmTime() {
    Date date = new Date();
    Alarm alarm = new Alarm(date, new ArrayList<File>(), "mode");
    assertEquals(date, alarm.getAlarmTime());
  }

  /**
   * Test method for {@link org.jajuk.services.alarm.Alarm#nextDay()}.
   */

  public void testNextDay() {
    Date date = new Date();
    Alarm alarm = new Alarm(date, new ArrayList<File>(), "mode");
    assertEquals(date, alarm.getAlarmTime());
    alarm.nextDay();

    Date datenew = alarm.getAlarmTime();
    assertEquals(DateUtils.addDays(date, 1), datenew);
  }
}
