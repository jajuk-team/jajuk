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

import java.awt.TrayIcon;

import org.jajuk.base.Album;
import org.jajuk.base.File;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.windows.JajukSystray;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Implementation of @link INotificator which uses the standard Java Systray for
 * displaying notifications to the user. *
 * <p>
 * Singleton
 * </p>
 */
public class JavaBalloonNotificator implements INotificator {
  // the Systray is used to display the notification
  TrayIcon trayIcon;
  /** Self instance *. */
  private static JavaBalloonNotificator self = new JavaBalloonNotificator();

  /**
   * Return an instance of this singleton.
   * 
   * @return an instance of this singleton
   */
  public static JavaBalloonNotificator getInstance() {
    return self;
  }

  /**
   * Creates an instance, the link to tray provides the necessary Java Systray
   * implementation.
   */
  private JavaBalloonNotificator() {
    this.trayIcon = JajukSystray.getInstance().getTrayIcon();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#isAvailable()
   */
  @Override
  public boolean isAvailable() {
    return (trayIcon != null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#notify(org.jajuk.services. webradio.WebRadio)
   */
  @Override
  public void notify(WebRadio webradio) {
    String title = Messages.getString("Notificator.track_change.webradio_title");
    String text = webradio.getName();
    // simply call the display method on the tray icon that is provided
    trayIcon.displayMessage(title, text, TrayIcon.MessageType.INFO);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#notify(org.jajuk.base.File)
   */
  @Override
  public void notify(File file) {
    // Force any new cover search before displaying it if the album is set "none" cover
    Album album = file.getTrack().getAlbum();
    album.resetCoverCache();
    String title = Messages.getString("Notificator.track_change.track_title");
    String pattern = Conf.getString(Const.CONF_PATTERN_BALLOON_NOTIFIER);
    String text;
    try {
      text = UtilString.applyPattern(file, pattern, false, false);
      // simply call the display method on the tray icon that is provided
      trayIcon.displayMessage(title, text, TrayIcon.MessageType.INFO);
    } catch (JajukException e) {
      Log.error(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#notify(String)
   */
  @Override
  public void notify(String title, String status) {
    // simply call the display method on the tray icon that is provided
    trayIcon.displayMessage(title, status, TrayIcon.MessageType.INFO);
  }
}
