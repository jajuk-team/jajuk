/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision: 2321 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;

import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Messages;

/**
 * Fast access to hide/show unmounted devices option
 */
public class HideShowMountedDevicesAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  HideShowMountedDevicesAction() {
    super(Messages.getString("JajukJMenuBar.24"), true);
  }

  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    Object o = source.getClientProperty(DETAIL_ORIGIN);
    JCheckBoxMenuItem jmiUnmounted = (JCheckBoxMenuItem) o;
    boolean bHideUnmountedStatus = ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED);
    ConfigurationManager.setProperty(CONF_OPTIONS_HIDE_UNMOUNTED, Boolean
        .toString(!bHideUnmountedStatus));
    jmiUnmounted.setSelected(!bHideUnmountedStatus);
    ObservationManager.notify(new Event(EventSubject.EVENT_PARAMETERS_CHANGE));
    ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
  }
}
