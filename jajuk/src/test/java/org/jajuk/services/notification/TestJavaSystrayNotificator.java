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
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;

/**
 * 
 */
public class TestJavaSystrayNotificator extends JajukTestCase {

  /**
   * Test method for
   * {@link org.jajuk.services.notification.JavaSystrayNotificator#JavaSystrayNotificator(java.awt.TrayIcon)}
   * .
   */
  public void testJavaSystrayNotificator() {
    // should initialize with null correctly., but return false for
    // "isAvailable"
    JavaSystrayNotificator not = new JavaSystrayNotificator(null);
    assertFalse(not.isAvailable());

    try {
      // should initialize correctly and return true for valid TrayIcon
      TrayIcon tray = new JXTrayIcon(IconLoader.getIcon(JajukIcons.TRAY).getImage());
      not = new JavaSystrayNotificator(tray);
      assertTrue(not.isAvailable());
    } catch (ExceptionInInitializerError e) {
      // expected when run without UI support
    } catch (NoClassDefFoundError e) {
      // expected when run without UI support
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.services.notification.JavaSystrayNotificator#isAvailable()}
   * .
   */
  public void testIsAvailable() {
    // tested above
  }

  /**
   * Test method for
   * {@link org.jajuk.services.notification.JavaSystrayNotificator#notify(java.lang.String, java.lang.String)}
   * .
   */
  public void testNotifyStringString() {
    try {
      TrayIcon tray = new JXTrayIcon(IconLoader.getIcon(JajukIcons.TRAY).getImage());
      JavaSystrayNotificator not = new JavaSystrayNotificator(tray);

      not.notify("title", "text to display");
    } catch (NoClassDefFoundError e) {
      // expected when run without UI support
    }
  }

}
