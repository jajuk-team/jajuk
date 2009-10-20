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

import org.jajuk.JajukTestCase;

/**
 * 
 */
public class TestNotifySendNotificator extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.services.notification.NotifySendNotificator#isAvailable()}.
   */
  public void testIsAvailable() {
    // should be constructed correctly, however we cannot guarantee if "isAvailable" will return true or false, it depends
    // on the "notify-send" to be available, which we cannot guarantee on all machines running the tests
    NotifySendNotificator not = new NotifySendNotificator();
    
    // just call it to cover it, we cannot test if it is true or false
    not.isAvailable();

  }

  /**
   * Test method for {@link org.jajuk.services.notification.NotifySendNotificator#notify(java.lang.String, java.lang.String)}.
   */
  public void testNotifyStringString() {
    // should be constructed correctly, however we cannot guarantee if "isAvailable" will return true or false, it depends
    // on the "notify-send" to be available, which we cannot guarantee on all machines running the tests
    NotifySendNotificator not = new NotifySendNotificator();
    
    // only test this if it is available
    if(not.isAvailable()) {
      not.notify("some title", "some text to display");
    }
  }

}
