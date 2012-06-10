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
package org.jajuk.services.notification;

import org.jajuk.JajukTestCase;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestNotificatorFactory extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.NotificatorFactory#getNotificator()}
   * .
   */
  public void testNoneNotificator() {
    // enable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_NOTIFICATOR_TYPE, NotificatorTypes.NONE.name());

    // now try to get a balloon notificator, but we cannot be sure if this works
    // on all
    // machines so we on't assume not null.
    NotificatorFactory.getNotificator();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.NotificatorFactory#getNotificator()}
   * .
   */
  public void testBalloonNotificator() {
    // enable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_NOTIFICATOR_TYPE, NotificatorTypes.BALLOON.name());

    // now try to get a balloon notificator, but we cannot be sure if this works
    // on all
    // machines so we on't assume not null.
    NotificatorFactory.getNotificator();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.NotificatorFactory#getNotificator()}
   * .
   */
  public void testToastNotificator() {
    // enable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_NOTIFICATOR_TYPE, NotificatorTypes.TOAST.name());
    // This swing-implemented notificator should always be available
    assertNotNull(NotificatorFactory.getNotificator());
  }

  /**
   * Test get system notificator false.
   * 
   */
  public void testGetSystemNotificatorFalse() {
    // disable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_NOTIFICATOR_TYPE, NotificatorTypes.NONE.name());

    // here we need to get null back as it is disabled
    assertNull(NotificatorFactory.getNotificator());
  }
}
