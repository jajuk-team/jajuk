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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * DOCUMENT_ME.
 */
public class RefreshAction extends SelectionAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new refresh action.
   */
  RefreshAction() {
    super(Messages.getString("ActionRefresh.0"), IconLoader.getIcon(JajukIcons.REFRESH), true);
    setShortDescription(Messages.getString("ActionRefresh.0"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent e) {
    try {
      RefreshAction.super.perform(e);
    } catch (Exception ex) {
      Log.error(ex);
    }
    // Note that we already tested void or mixed-up selection in 
    // FilesTreeView.isRefreshSelectionValid() method.
    // The GUI allows only single device selection.
    if (selection.get(0) instanceof Device) {
      // ask user if he wants to make deep or fast scan
      Device device = (Device) selection.get(0);
      device.refresh(true, true, false, null);
    } else {
      // Directory selection, we have to group directories of the same device
      HashMap<Device, List<Directory>> devicesDirectories = new HashMap<Device, List<Directory>>(
          selection.size());
      for (Item item : selection) {
        Directory dir = (Directory) item;
        Device device = dir.getDevice();
        List<Directory> dirs = devicesDirectories.get(device);
        if (dirs == null) {
          dirs = new ArrayList<Directory>();
          devicesDirectories.put(device, dirs);
        }
        dirs.add(dir);
      }
      for (Device device : devicesDirectories.keySet()) {
        List<Directory> dirs = devicesDirectories.get(device);
        device.refresh(true, true, false, dirs);
      }
    }
  }
}