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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Item;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * DOCUMENT_ME.
 */
public class NewFolderAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new new folder action.
   */
  NewFolderAction() {
    super(Messages.getString("NewFolderAction.0"),
        IconLoader.getIcon(JajukIcons.DIRECTORY_SYNCHRO), true);
    setShortDescription(Messages.getString("NewFolderAction.0"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void perform(final ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    // Get required data from the tree (selected node and node type)
    final List<Item> alSelected = (ArrayList<Item>) source
        .getClientProperty(Const.DETAIL_SELECTION);
    final Item currentItem = alSelected.get(0);

    final String folderName = JOptionPane.showInputDialog(null,
        Messages.getString("NewFolderAction.1") + "\n\n");
    if ((folderName != null) && (folderName.length() > 0)) {
      // If selected item is a directory, extract the associated root
      // directory
      // from the device and use it
      final Directory dir;
      if (currentItem instanceof Device) {
        dir = ((Device) currentItem).getRootDirectory();
      } else if (currentItem instanceof Directory) {
        dir = (Directory) currentItem;
      } else {
        Log.debug("Wrong item type");
        return;
      }
      try {
        java.io.File newFolder = new java.io.File(dir.getAbsolutePath() + "/" + folderName);
        if (!newFolder.exists()) {
          if (newFolder.mkdir()) {
            DirectoryManager.getInstance().registerDirectory(folderName, dir, dir.getDevice());
            ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
          } else {
            Messages.showErrorMessage(136);
            return;
          }
        } else {
          Messages.showWarningMessage(Messages.getString("NewFolderAction.2"));
          return;
        }
      } catch (Exception er) {
        Log.error(er);
        Messages.showErrorMessage(136);
      }
    }
  }
}
