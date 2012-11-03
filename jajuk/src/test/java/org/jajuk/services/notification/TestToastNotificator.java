/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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

import java.awt.HeadlessException;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.File;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.webradio.WebRadio;

/**
 * .
 */
public class TestToastNotificator extends JajukTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.ToastNotificator#ToastNotificator()}
   * .
   */
  public void testToastNotificator() {
    try {
      INotificator notificator = ToastNotificator.getInstance();
      assertTrue(notificator.isAvailable());
    } catch (ExceptionInInitializerError e) {
      // expected when run without UI support
    } catch (NoClassDefFoundError e) {
      // expected when run without UI support
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.JavaBalloonNotificator#isAvailable()}
   * .
   */
  public void testIsAvailable() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.JavaBalloonNotificator#notify(org.jajuk.base.File)}
   * .
   */
  public void testNotifyFile() {
    try {
      StartupCollectionService.registerItemManagers();
      ToastNotificator notificator = ToastNotificator.getInstance();
      File file = JUnitHelpers.getFile();
      notificator.notify(file);
    } catch (NoClassDefFoundError e) {
      // expected when run without UI support
    } catch (HeadlessException e) {
      // expected when run without UI support
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.JavaBalloonNotificator#notify(org.jajuk.services.webradio.WebRadio)}
   * .
   */
  public void testNotifyWebradio() {
    try {
      ToastNotificator notificator = ToastNotificator.getInstance();
      WebRadio webradio = JUnitHelpers.getWebRadio();
      notificator.notify(webradio);
    } catch (NoClassDefFoundError e) {
      // expected when run without UI support
    } catch (HeadlessException e) {
      // expected when run without UI support
    }
  }
}
