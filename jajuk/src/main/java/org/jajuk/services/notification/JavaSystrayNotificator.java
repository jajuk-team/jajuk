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

/**
 * Implementation of @link ISystemNotificator which uses the standard Java
 * Systray for displaying notifications to the user.
 * 
 */
public class JavaSystrayNotificator implements ISystemNotificator {
  // the Systray is used to display the notification
  TrayIcon trayIcon;

  /**
   * Creates an instance, the link to tray provides the necessary Java Systray
   * implementation.
   * 
   * @param tray
   *          The initialized system tray. isAvailable() will return false, if
   *          this is passed null.
   */
  public JavaSystrayNotificator(TrayIcon trayIcon) {
    super();
    this.trayIcon = trayIcon;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.ISystemNotificator#isAvailable()
   */
  @Override
  public boolean isAvailable() {
    // cannot work on an empty
    if (trayIcon == null) {
      return false;
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.jajuk.services.notification.ISystemNotificator#notify(java.lang.String
   * , java.lang.String)
   */
  @Override
  public void notify(String title, String text) {
    // simplay call the display method on the tray icon that is provided
    trayIcon.displayMessage(title, text, TrayIcon.MessageType.INFO);
  }

}
