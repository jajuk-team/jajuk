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
import javax.swing.JOptionPane;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

public class NewFolderAction extends ActionBase {
  private static final long serialVersionUID = 1L;

  NewFolderAction() {
    super(Messages.getString("NewFolderAction.0"), IconLoader.ICON_DIRECTORY_SYNCHRO, true);
    setShortDescription(Messages.getString("NewFolderAction.0"));
  }

  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    // Get required data from the tree (selected node and node type)
    final ArrayList<Item> alSelected = (ArrayList<Item>) source.getClientProperty(DETAIL_SELECTION);
    Item currentItem = alSelected.get(0);

    String folderName = JOptionPane.showInputDialog(null, Messages.getString("NewFolderAction.1")
        + "\n\n");
    if ((folderName != null) && (folderName.length() > 0)) {
      if (currentItem instanceof Directory) {
        try {
          java.io.File newFolder = new java.io.File(((Directory) currentItem).getAbsolutePath()
              + "/" + folderName);
          if (!newFolder.exists()) {
            newFolder.mkdir();
          } else {
            Messages.showWarningMessage(Messages.getString("NewFolderAction.2"));
            return;
          }
          ((Directory) currentItem).getDevice().refreshCommand(false, false);
        } catch (Exception er) {
          Log.error(er);
        }
      } else if (currentItem instanceof Device) {
        try {
          java.io.File newFolder = new java.io.File(((Device) currentItem).getRootDirectory()
              .getAbsolutePath()
              + "/" + folderName);
          if (!newFolder.exists()) {
            newFolder.mkdir();
          } else {
            Messages.showWarningMessage(Messages.getString("NewFolderAction.2"));
            return;
          }
          ((Device) currentItem).refreshCommand(false, false);
        } catch (Exception er) {
          Log.error(er);
        }
      }
      ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
    }
  }
}
