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
 *  $$Revision: 2920 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComponent;

import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

public class RefreshDirectoryAction extends ActionBase {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  RefreshDirectoryAction() {
    super(Messages.getString("ActionRefresh.0"), IconLoader.ICON_REFRESH, true);
    setShortDescription(Messages.getString("ActionRefresh.0"));
  }

  @SuppressWarnings("unchecked")
  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    // Get required data from the tree (selected node and node type)
    final ArrayList<Item> alSelected = (ArrayList<Item>) source.getClientProperty(DETAIL_SELECTION);
    Item item = alSelected.get(0);
    final Directory dir;
    if (item instanceof Directory) {
      dir = (Directory) item;
    } else {
      dir = ((File) item).getDirectory();
    }
    new Thread() {
      public void run() {
        Util.waiting();
        InformationJPanel.getInstance().setMessage(
            Messages.getString("ActionRefresh.1") + ": " + dir.getName(), 1);
        DirectoryManager.refreshDirectory(dir);
        ObservationManager.notify(new Event(JajukEvents.EVENT_DEVICE_REFRESH));
        InformationJPanel.getInstance().setMessage(Messages.getString("ActionRefresh.2"), 1);
        Util.stopWaiting();
      }
    }.start();

  }
}