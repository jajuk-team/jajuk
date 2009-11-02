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

import java.awt.TrayIcon;

import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * 
 */
public class SystemNotificatorFactory {
  private static ISystemNotificator notification;
  private static TrayIcon trayIcon;

  /**
   * @param trayIcon
   *          the trayIcon to set
   */
  public static void setTrayIcon(TrayIcon trayIcon) {
    SystemNotificatorFactory.trayIcon = trayIcon;
  }

  /**
   * retrieve the system preferred notification system.
   * 
   * This will return null if notification is disabled in the configuration or
   * if the platform does not support any of the available notification methods.
   * 
   * @return
   */
  public synchronized static ISystemNotificator getSystemNotificator() {
    // first check show balloon option and return null
    if (!Conf.getBoolean(Const.CONF_UI_SHOW_SYSTEM_NOTIFICATION)) {
      return null;
    }

    // if already created, just return this instance
    if (notification != null) {
      return notification;
    }

    notification = new JajukBalloonNotificator();
    if (notification.isAvailable()) {
      Log.debug("JajukBalloon implementation is available for system notifications.");
      return notification;
    }

    // Try the NotifySend implementation first
    notification = new NotifySendNotificator();
    if (notification.isAvailable()) {
      Log.debug("'notify-send' implementation is available for system notifications.");
      return notification;
    }

    notification = new JavaSystrayNotificator(trayIcon);
    if (notification.isAvailable()) {
      Log.debug("Java systray implementation is available for system notifications.");
      return notification;
    } else {
      // reset member again to not keep an implementation that does not work...
      notification = null;
    }

    // none available, return null
    Log.debug("No implementation is available for system notifications.");
    return null;
  }
}
