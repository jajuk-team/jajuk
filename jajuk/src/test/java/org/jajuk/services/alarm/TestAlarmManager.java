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

import java.util.Calendar;
import java.util.Set;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * DOCUMENT_ME.
 */
public class TestAlarmManager extends JajukTestCase {

  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // make sure the FileManager is registered correctly for each invocation
    // this is done during the first access to the singleton
    FileManager.getInstance();

    // clear File Manager to avoid files being left in there and causing trouble
    FileManager.getInstance().clear();

    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.services.alarm.AlarmManager#getInstance()}
   * .
   *
   * @throws Exception the exception
   */
  public void testGetInstance() throws Exception {
    Conf.setProperty(Const.CONF_ALARM_ENABLED, "true");

    assertNotNull(AlarmManager.getInstance());

    // sleep a bit to let internal thread do some work
    Thread.sleep(1100);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.alarm.AlarmManager#update(org.jajuk.events.JajukEvent)}
   * .
   */
  public void testUpdate() {
    AlarmManager.getInstance().update(new JajukEvent(JajukEvents.ALARMS_CHANGE));
  }

  /**
   * Test update2.
   * DOCUMENT_ME
   */
  public void testUpdate2() {
    Conf.setProperty(Const.CONF_ALARM_ENABLED, "true");

    AlarmManager.getInstance().update(new JajukEvent(JajukEvents.ALARMS_CHANGE));
  }

  /**
   * Test update3.
   * DOCUMENT_ME
   */
  public void testUpdate3() {
    Conf.setProperty(Const.CONF_ALARM_ENABLED, "true");
    Conf.setProperty(Const.CONF_ALARM_MODE, Const.STARTUP_MODE_ITEM);

    AlarmManager.getInstance().update(new JajukEvent(JajukEvents.ALARMS_CHANGE));
  }

  /**
   * Test update4.
   * DOCUMENT_ME
   */
  public void testUpdate4() {
    Conf.setProperty(Const.CONF_ALARM_ENABLED, "true");
    Conf.setProperty(Const.CONF_ALARM_MODE, Const.STARTUP_MODE_BESTOF);

    AlarmManager.getInstance().update(new JajukEvent(JajukEvents.ALARMS_CHANGE));
  }

  /**
   * Test update5.
   * DOCUMENT_ME
   */
  public void testUpdate5() {
    Conf.setProperty(Const.CONF_ALARM_ENABLED, "true");
    Conf.setProperty(Const.CONF_ALARM_MODE, Const.STARTUP_MODE_NOVELTIES);

    AlarmManager.getInstance().update(new JajukEvent(JajukEvents.ALARMS_CHANGE));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.alarm.AlarmManager#getRegistrationKeys()}.
   */
  public void testGetRegistrationKeys() {
    Set<JajukEvents> set = AlarmManager.getInstance().getRegistrationKeys();
    assertTrue(set.contains(JajukEvents.ALARMS_CHANGE));
  }

  /**
   * Test trigger alarm.
   * DOCUMENT_ME
   *
   * @throws Exception the exception
   */
  public void testTriggerAlarm() throws Exception {
    Calendar cal = Calendar.getInstance();
    // add one second to let it be triggered immedately
    cal.add(Calendar.SECOND, 1);

    Conf.setProperty(Const.CONF_ALARM_ENABLED, "true");
    Conf.setProperty(Const.CONF_ALARM_MODE, Const.STARTUP_MODE_ITEM);
    Conf.setProperty(Const.CONF_ALARM_TIME_HOUR, Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
    Conf.setProperty(Const.CONF_ALARM_TIME_MINUTES, Integer.toString(cal.get(Calendar.MINUTE)));
    Conf.setProperty(Const.CONF_ALARM_TIME_SECONDS, Integer.toString(cal.get(Calendar.SECOND)));

    AlarmManager.getInstance().update(new JajukEvent(JajukEvents.ALARMS_CHANGE));

    // sleep a bit to let internal thread do some work
    Thread.sleep(2000);
  }
}
