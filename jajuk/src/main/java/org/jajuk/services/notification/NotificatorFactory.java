/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
package org.jajuk.services.notification;

import org.apache.commons.lang.StringUtils;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Factory returning a INotifictor according to user choice.
 */
public class NotificatorFactory {

  /** DOCUMENT_ME. */
  private static INotificator notificator;

  /**
   * retrieve the system preferred notificator system.
   * 
   * This will return null if notificator is disabled in the configuration or if
   * the platform does not support any of the available notificator methods.
   * 
   * @return the notificator
   */
  public synchronized static INotificator getNotificator() {

    // first check show balloon option and return null
    String optionValue = Conf.getString(Const.CONF_UI_NOTIFICATOR_TYPE);
    if (StringUtils.isBlank(optionValue) || NotificatorTypes.NONE.name().equals(optionValue)) {
      return null;
    }

    // Balloon
    if (NotificatorTypes.BALLOON.name().equals(optionValue)) {
      // Try the NotifySend implementation first
      notificator = NotifySendBalloonNotificator.getInstance();
      if (notificator.isAvailable()) {
        Log.debug("'notify-send' implementation is available for system notifications.");
      } else {
        // OK, let's try the java build-in balloon system
        notificator = JavaBalloonNotificator.getInstance();
        if (notificator.isAvailable()) {
          Log.debug("Java systray implementation is available for system notifications.");
        } else {
          Log.debug("No implementation is available for system notifications.");
          // reset member again to not keep an implementation that does not
          // work and don't try to find the right implementation gain (costly
          // for some notifiers)
          notificator = null;
        }
      }
    }

    // Animated popup
    else if (NotificatorTypes.TOAST.name().equals(optionValue)) {
      notificator = ToastNotificator.getInstance();
      if (notificator.isAvailable()) {
        Log.debug("JajukInformationDialog implementation is available for system notifications.");
      } else {
        notificator = null;
      }
    } else {
      Log.debug("Unknown notifier type: " + optionValue);
      notificator = null;
    }
    return notificator;
  }

}
