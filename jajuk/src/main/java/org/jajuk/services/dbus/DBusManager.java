/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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

/**
 * 
 */
public final class DBusManager {
  /** Self instance */
  private static DBusManager self = null;

  /** Support for D-Bus remote control of Jajuk */
  private final DBusSupportImpl dbus;

  /**
   * 
   * @return singleton
   */
  public static DBusManager getInstance() {
    if (self == null) {
      self = new DBusManager();
    }
    return self;
  }

  private DBusManager() {
    dbus = new DBusSupportImpl();

    // the connect method will internally catch errors and report them to the
    // logfile
    dbus.connect();

    // add a shutdown hook so we disconnect during shutdown
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        dbus.disconnect();
      }
    });

  }

}
