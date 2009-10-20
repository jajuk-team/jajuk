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
package org.jajuk.services.notification;

import ext.JXTrayIcon;

import java.awt.TrayIcon;

import org.jajuk.JajukTestCase;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;

/**
 * 
 */
public class TestSystemNotificatorFactory extends JajukTestCase {

  /**
   * Test method for
   * {@link org.jajuk.services.notification.SystemNotificatorFactory#setTrayIcon(java.awt.TrayIcon)}
   * .
   */
  public void testSetTrayIcon() {
    try {
      // just try to set a tray icon, should work in all cases
      TrayIcon tray = new JXTrayIcon(IconLoader.getIcon(JajukIcons.TRAY).getImage());
      SystemNotificatorFactory.setTrayIcon(tray);
    } catch (NoClassDefFoundError e) {
      // expected when run without UI support
    } catch (ExceptionInInitializerError e) {
      // expected when run without UI support
    }

    // enable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_SHOW_BALLOON, "true");

    // now try to get a notificator, but we cannot be sure if this works on all
    // machines
    SystemNotificatorFactory.getSystemNotificator();
  }

  /**
   * Test method for
   * {@link org.jajuk.services.notification.SystemNotificatorFactory#getSystemNotificator()}
   * .
   */
  public void testGetSystemNotificator() {
    // enable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_SHOW_BALLOON, "true");

    // now try to get a notificator, but we cannot be sure if this works on all
    // machines
    SystemNotificatorFactory.getSystemNotificator();
  }

  public void testGetSystemNotificatorFalse() {
    // disable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_SHOW_BALLOON, "false");

    // here we need to get null back as it is disabled
    assertNull(SystemNotificatorFactory.getSystemNotificator());
  }
}
