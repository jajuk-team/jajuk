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
package org.jajuk.services.dbus;

import junit.framework.TestCase;

/**
 * 
 */
public class TestDBusManager extends TestCase {

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusManager#getInstance()}.
   */
  public final void testGetInstance() {
    // DBus requires the environment variable DBUS_SESSION_BUS_ADDRESS
    // but this is typically only set on machines that support D-Bus
    
    // cannot check this for null as it won't work in some installations
    DBusManager.getInstance();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusManager#disconnect()}.
   */
  public final void testDisconnect() {
    // first run it without connecting in getInstance()
    DBusManager.disconnect();
    
    // then run getInstance() which tries to connect 
    DBusManager.getInstance();
    
    // now again, although getInstance() might not have worked... 
    DBusManager.disconnect();
  }

}
