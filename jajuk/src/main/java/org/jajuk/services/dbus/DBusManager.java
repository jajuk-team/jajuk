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
 *  $Revision$
 */
package org.jajuk.services.dbus;

/**
 * Base class to connect/disconnect to the session wide DBus daemon.
 */
public final class DBusManager {

  /** Support for D-Bus remote control of Jajuk. */
  private static DBusSupportImpl dbus;

  /**
   * Gets the instance. This is usually called during startup to initialize the
   * connection to D-Bus.
   * 
   * This call will usually not return an exception if there is a problem with
   * D-Bus, but will only report problems to the Log.
   * 
   * @see disconnect()
   */
  public static void connect() throws Exception {
    try {
      if (dbus == null) {
        dbus = new DBusSupportImpl();

        // the connect method will internally catch errors and report them to the
        // logfile
        dbus.connect();
      }
    } catch (Throwable t) {
      throw new Exception("DBus not available", t);
    }
  }

  /**
   * De-initialize the D-Bus connection. Nothing is done here if the connection
   * is not established (yet).
   * 
   * @see connect()
   */
  public static void disconnect() {
    if (dbus != null) {
      dbus.disconnect();

      // reset to let initialize work correctly in all cases
      dbus = null;
    }
  }
}
