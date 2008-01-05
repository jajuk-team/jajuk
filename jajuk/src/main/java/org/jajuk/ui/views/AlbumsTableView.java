/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui.views;

import ext.SwingWorker;

import java.util.List;

import org.jajuk.base.Album;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.ui.helpers.AlbumsTableModel;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * List collection albums as a table
 */
public class AlbumsTableView extends AbstractTableView {

  private static final long serialVersionUID = 7576455252866971945L;

  public AlbumsTableView() {
    super();
    columnsConf = CONF_ALBUMS_TABLE_COLUMNS;
    editableConf = CONF_ALBUMS_TABLE_EDITION;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("AlbumsTableView.0");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#initUI()
   */
  public void initUI() {
    SwingWorker sw = new SwingWorker() {
      public Object construct() {
        AlbumsTableView.super.construct();
        // Add this generic menu item manually to ensure it's the last one in
        // the list for GUI reasons
        jtable.getMenu().add(jmiDelete);
        jtable.getMenu().add(jmiBookmark);
        jtable.getMenu().add(jmiProperties);
        // Add specific behavior on left click
        jtable.setCommand(new ILaunchCommand() {
          public void launch(int nbClicks) {
            int iSelectedCol = jtable.getSelectedColumn();
            // selected column in view Test click on play icon launch track only
            // if only first column is selected (fixes issue with
            // Ctrl-A)
            if (jtable.getSelectedColumnCount() == 1
            // click on play icon
                && (jtable.convertColumnIndexToModel(iSelectedCol) == 0)
                // double click on any column and edition state false
                || nbClicks == 2) {
              // selected row in view
              Album album = (Album) jtable.getSelection().get(0);
              List<File> alFiles = Util.getPlayableFiles(album);
              if (alFiles.size() > 0) {
                // launch it
                FIFO.getInstance().push(
                    Util.createStackItems(alFiles, ConfigurationManager
                        .getBoolean(CONF_STATE_REPEAT), true),
                    ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));

              } else {
                Messages.showErrorMessage(10, album.getName2());
              }
            }
          }
        });
        return null;
      }

      public void finished() {
        AlbumsTableView.super.finished();
      }
    };
    sw.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTableView#initTable()
   */
  @Override
  void initTable() {
    jtbEditable.setSelected(ConfigurationManager.getBoolean(CONF_ALBUMS_TABLE_EDITION));
    // Disable edit button, edition not yet implemented
    jtbEditable.setEnabled(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTableView#populateTable()
   */
  @Override
  JajukTableModel populateTable() {
    // model creation
    AlbumsTableModel model = new AlbumsTableModel();
    return model;
  }

}
