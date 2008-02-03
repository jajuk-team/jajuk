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

import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

public class RenameAction extends ActionBase {
  private static final long serialVersionUID = 1L;

  RenameAction() {
    super(Messages.getString("RenameAction.0"), IconLoader.ICON_EDIT, true);
    setShortDescription(Messages.getString("RenameAction.0"));
  }

  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    // Get required data from the tree (selected node and node type)
    final ArrayList<Item> alSelected = (ArrayList<Item>) source.getClientProperty(DETAIL_SELECTION);
    Item currentItem = alSelected.get(0);

    if (currentItem instanceof File) {
      String newName = JOptionPane.showInputDialog(null, Messages.getString("RenameAction.1")
          + "\n\n", ((File) currentItem).getName());
      if ((newName != null) && (newName.length() > 0)) {
        try {
          FileManager.getInstance().changeFileName((File) currentItem, newName);
          DirectoryManager.refreshDirectory(((File) currentItem).getDirectory());
        } catch (Exception er) {
          Log.error(er);
        }
      }
    } else if (currentItem instanceof Directory) {
      String newName = JOptionPane.showInputDialog(null, Messages.getString("RenameAction.2")
          + "\n\n", ((Directory) currentItem).getName());
      if ((newName != null) && (newName.length() > 0)) {
        try {
          java.io.File newFile = new java.io.File(((Directory) currentItem).getParentDirectory()
              .getAbsolutePath()
              + "/" + newName);
          ((Directory) currentItem).getFio().renameTo(newFile);
          DirectoryManager.refreshDirectory(((Directory) currentItem).getParentDirectory());
        } catch (Exception er) {
          Log.error(er);
        }
      }
    }
  }
}
