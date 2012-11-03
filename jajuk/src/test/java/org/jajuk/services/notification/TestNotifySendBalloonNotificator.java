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

import org.jajuk.TestHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.File;
import org.jajuk.services.webradio.WebRadio;

/**
 * .
 */
public class TestNotifySendBalloonNotificator extends JajukTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.NotifySendBalloonNotificator#isAvailable()}
   * .
   */
  public void testIsAvailable() {
    // should be constructed correctly, however we cannot guarantee if
    // "isAvailable" will return true or false, it depends
    // on the "notify-send" to be available, which we cannot guarantee on all
    // machines running the tests
    NotifySendBalloonNotificator not = NotifySendBalloonNotificator.getInstance();
    // just call it to cover it, we cannot test if it is true or false
    not.isAvailable();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.NotifySendBalloonNotificator#notif(org.jajuk.base.File)}
   * .
   */
  public void testNotifyFile() {
    // should be constructed correctly, however we cannot guarantee if
    // "isAvailable" will return true or false, it depends
    // on the "notify-send" to be available, which we cannot guarantee on all
    // machines running the tests
    NotifySendBalloonNotificator notificator = NotifySendBalloonNotificator.getInstance();
    // only test this if it is available
    if (notificator.isAvailable()) {
      File file = TestHelpers.getFile();
      notificator.notify(file);
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.notification.JavaBalloonNotificator#notify(org.jajuk.services.webradio.WebRadio)}
   * .
   */
  public void testNotifyWebradio() {
    // should be constructed correctly, however we cannot guarantee if
    // "isAvailable" will return true or false, it depends
    // on the "notify-send" to be available, which we cannot guarantee on all
    // machines running the tests
    NotifySendBalloonNotificator notificator = NotifySendBalloonNotificator.getInstance();
    // only test this if it is available
    if (notificator.isAvailable()) {
      WebRadio webradio = TestHelpers.getWebRadio();
      notificator.notify(webradio);
    }
  }
}
