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
package org.jajuk.services.dbus;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.jajuk.services.mpris.MprisService;
import org.jajuk.services.mpris.MprisService2;
import org.jajuk.util.log.Log;

import java.io.IOException;

/**
 * Base class to connect/disconnect to the session-wide DBus daemon.
 * Adapted for <a href="https://github.com/hypfvieh/dbus-java">dbus-java 5.2</a>
 */
public final class DBusManager {

  private static DBusConnection sessionConnection;
  private static DBusSupportImpl serviceImplementation;

  //private static final String BUS_NAME = "org.jajuk.dbus.DBusSupport";
  private static final String BUS_NAME = "org.mpris.MediaPlayer2.jajuk";
  private static MprisService2 mprisService;

  /**
   * Initialize D-Bus connection to the session bus.
   */
  public static synchronized void connect() throws IOException, InterruptedException {
    if (sessionConnection != null && sessionConnection.isConnected()) {
      Log.info("D-Bus already connected");
      return;
    }

    Log.info("Attempting to establish D-Bus session connection...");

    // Build and get session connection using builder pattern
    //
    try (DBusConnection sessionConnection = DBusConnectionBuilder.forSessionBus().build()) {
      // Request unique bus name
      sessionConnection.requestBusName(BUS_NAME);

      // Create service implementation instance
      serviceImplementation = new DBusSupportImpl(sessionConnection);

      // Export object - use registerRemoteObject instead of exportObject
      //sessionConnection.registerRemoteObject( OBJECT_PATH,              DBusSupport.class,              serviceImplementation      );

      Log.info("D-Bus support started successfully on Session Bus (" + BUS_NAME + ")");

    } catch (DBusException e) {
      Log.error("Failed to initialize D-Bus connection: " + e.getMessage(), e);
      throw new IOException("DBCbus initialization failed", e);
    }
  }

  public static synchronized void connect2() throws IOException, DBusException {
    try {
      Log.info("Attempting to establish D-Bus session connection...");

      // Build session connection
      sessionConnection = DBusConnectionBuilder.forSessionBus().build();

      // Export BOTH Media Player 2 interfaces at once
      mprisService = new MprisService2("Jajuk", sessionConnection);

      Log.info("D-Bus support started successfully on Session Bus (" + BUS_NAME + ")");
    } catch (Exception e) {
      Log.error("Failed to initialize D-Bus connection: " + e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Disconnect cleanly from D-Bus.
   */
  public static synchronized void disconnect() {
    if (mprisService != null) {
      mprisService.cleanup();
      mprisService = null;
    }

    if (sessionConnection != null) {
      try {
        Log.info("Disconnecting from D-Bus...");
        try {
          sessionConnection.releaseBusName(BUS_NAME);
        } catch (org.freedesktop.dbus.exceptions.DBusException e) {
          if ("Not Connected".equals(e.getMessage())) {
            // Normal : proxy distant déjà fermé, release automatique au disconnect
            Log.debug("Bus name already released or connection closing");
          } else {
            Log.error("Error releasing bus name: " + e.getMessage(), e);
          }
        }
        if (sessionConnection.isConnected()) {
          sessionConnection.disconnect();
        }
        sessionConnection.close();
      } catch (Exception e) {
        Log.warn("Error during D-Bus disconnection: " + e.getMessage());
      } finally {
        sessionConnection = null;
      }

    /*
    if (serviceImplementation != null) {
      serviceImplementation.cleanup();
      serviceImplementation = null;
    }
    */
      Log.info("D-Bus disconnected");
    }
  }

}