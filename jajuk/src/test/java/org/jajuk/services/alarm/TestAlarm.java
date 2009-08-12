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
package org.jajuk.services.alarm;

import java.util.Date;

import org.jajuk.util.Const;

import junit.framework.TestCase;

/**
 * 
 */
public class TestAlarm extends TestCase {

  /**
   * Test method for {@link org.jajuk.services.alarm.Alarm#Alarm(java.util.Date, java.util.List, java.lang.String)}.
   */

  public void testAlarm() {
    new Alarm(new Date(), null, "mode");
  }

  /**
   * Test method for {@link org.jajuk.services.alarm.Alarm#wakeUpSleeper()}.
   */

  public void testWakeUpSleeper() {
    Alarm alarm = new Alarm(new Date(), null, "mode");
    alarm.wakeUpSleeper();
    
    alarm = new Alarm(new Date(), null, Const.ALARM_START_ACTION);
    
  }

  /**
   * Test method for {@link org.jajuk.services.alarm.Alarm#getAlarmTime()}.
   */

  public void testGetAlarmTime() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.alarm.Alarm#nextDay()}.
   */

  public void testNextDay() {
    // TODO: implement test
  }

}
