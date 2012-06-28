/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

import org.jajuk.base.File;
import org.jajuk.services.webradio.WebRadio;

/**
 * System notification in Jajuk means informing the user about things like newly
 * started tracks via a system specific notification mechanism.
 * 
 * On Windows the System Tray can take care of this.
 * 
 * On Linux, especially Linux there are more sophisticated notification
 * mechanisms available which can be used as well.
 * 
 * Base interface for multiple different notification services that we can use.
 */
public interface INotificator {
  /**
   * Indicates if this notificator is available on the current platform.
   * 
   * @return true if this notificator can display notifications on this
   * installation, false otherwise.
   */
  boolean isAvailable();

  /**
   * Require the notificator to notify web radio change.
   * 
   * Note that the text itself is got by the notificator itself.
   * 
   * This method should only be called if @link isAvailable() returns true!
   * 
   * @param webradio 
   */
  void notify(WebRadio webradio);

  /**
   * Require the notificator to notify a track change.
   * 
   * Note that the text itself is got by the notificator itself.
   * 
   * This method should only be called if @link isAvailable() returns true!
   * 
   * @param file 
   */
  void notify(File file);

  /**
   * Ask the notificator to provide some arbitrary status information.
   *
   * @param title 
   * @param status The string to print.
   */
  void notify(String title, String status);
}
