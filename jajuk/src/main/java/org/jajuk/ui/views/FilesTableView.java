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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import javax.swing.JMenuItem;

import org.jajuk.base.File;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FilesTableModel;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Logical table view
 */
public class FilesTableView extends AbstractTableView {

  private static final long serialVersionUID = 1L;

  JMenuItem jmiFilePlayDirectory;

  public FilesTableView() {
    super();
    columnsConf = CONF_FILES_TABLE_COLUMNS;
    editableConf = CONF_FILES_TABLE_EDITION;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("FilesTableView.0");
  }

  public void initUI() {
    // Perform common table view initializations
    FilesTableView.super.construct();
    // File menu
    jmiFilePlayDirectory = new JMenuItem(ActionManager
        .getAction(JajukActions.PLAY_DIRECTORY_SELECTION));
    jmiFilePlayDirectory.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiFilePlayDirectory);
    // Add this generic menu item manually to ensure it's the last one in
    // the list for GUI reasons
    jtable.getMenu().add(jmiBookmark);
    jtable.getMenu().add(jmiProperties);

    // Add specific behavior on left click
    jtable.setCommand(new ILaunchCommand() {
      public void launch(int nbClicks) {
        int iSelectedCol = jtable.getSelectedColumn();

        if (jtable.getSelectedColumnCount() != 1) {
          return;
        }

        if (jtable.convertColumnIndexToModel(iSelectedCol) != 0) {
          return;
        }

        // selected column in view Test click on play icon
        // launch track only if only first column is selected (fixes issue
        // with Ctrl-A)
        if (// click on play icon
        nbClicks == 2 && !jtbEditable.isSelected()) {
          // double click on any column and edition state false
          // selected row in view
          File file = (File) model
              .getItemAt(jtable.convertRowIndexToModel(jtable.getSelectedRow()));
          try {
            // launch it
            FIFO.push(new StackItem(file, Conf.getBoolean(Const.CONF_STATE_REPEAT), true), Conf
                .getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));

          } catch (JajukException je) {
            Log.error(je);
          }
        }
      }
    });
    FilesTableView.super.finished();
  }

  /** populate the table */
  @Override
  public JajukTableModel populateTable() {
    // model creation
    return new FilesTableModel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTableView#initTable()
   */
  @Override
  void initTable() {
    boolean bEditable = Conf.getBoolean(Const.CONF_FILES_TABLE_EDITION);
    jtbEditable.setSelected(bEditable);
  }

}
