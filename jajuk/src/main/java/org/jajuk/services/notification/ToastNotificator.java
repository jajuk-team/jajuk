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
 *  $Revision$
 */
package org.jajuk.services.notification;

import org.jajuk.base.File;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.widgets.JajukToast;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;

/**
 * Notificator that displays a full Swing album toast (notification frame that
 * appears and disapears) with cover and text. Mainly reuses the JajukInformationDialog with
 * animation effects. *
 * <p>
 * Singleton
 * </p>
 */
public class ToastNotificator implements INotificator {

  /** Self instance *. */
  private static ToastNotificator self = new ToastNotificator();

  /**
   * Instantiates a new toast notificator.
   */
  private ToastNotificator() {
  }

  /**
   * Return an instance of this singleton.
   * 
   * @return an instance of this singleton
   */
  public static ToastNotificator getInstance() {
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#isAvailable()
   */
  @Override
  public boolean isAvailable() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#notify(org.jajuk.services. webradio.WebRadio)
   */
  @Override
  public void notify(WebRadio webradio) {
    String text = Messages.getString("Notificator.track_change.webradio_title")
        + webradio.getName();
    displayToast(text);
  }

  /**
   * Display toast.
   * 
   * @param text toast text
   */
  private void displayToast(String text) {
    // Useful for #1582 ([Linux] Void entry in task bar for information dialog)
    if (UtilGUI.getActiveWindow() != null // null if none window displayed
        && UtilGUI.getActiveWindow().equals(JajukMainWindow.getInstance())) {
      new JajukToast(text, null).display();
    } else {
      new JajukToast(text, UtilGUI.getActiveWindow()).display();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#notify(org.jajuk.base.Track)
   */
  @Override
  public void notify(File file) {
    String text = file.getHTMLFormatText();
    displayToast(text);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.notification.INotificator#notify(java.lang.String, java.lang.String)
   */
  @Override
  public void notify(String title, String status) {
    displayToast(status);
  }
}
