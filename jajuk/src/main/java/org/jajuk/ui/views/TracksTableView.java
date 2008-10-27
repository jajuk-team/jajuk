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
import org.jajuk.base.Track;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.TracksTableModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Logical table view
 */
public class TracksTableView extends AbstractTableView {

  private static final long serialVersionUID = 1L;

  private JMenuItem jmiTrackPlayAlbum;

  private JMenuItem jmiTrackPlayAuthor;
  
  public TracksTableView() {
    super();
    columnsConf = CONF_TRACKS_TABLE_COLUMNS;
    editableConf = CONF_TRACKS_TABLE_EDITION;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("TracksTableView.0");
  }

  public void initUI() {
    // Perform common table view initializations
    TracksTableView.super.construct();
    // Track menu
    jmiTrackPlayAlbum = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_ALBUM_SELECTION));
    jmiTrackPlayAlbum.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jmiTrackPlayAuthor = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_AUTHOR_SELECTION));
    jmiTrackPlayAuthor.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiTrackPlayAlbum,4);
    jtable.getMenu().add(jmiTrackPlayAuthor,5);
    // Add this generic menu item manually to ensure it's the last one in
    // the list for GUI reasons
    jtable.getMenu().addSeparator();
    jtable.getMenu().add(jmiBookmark);
    jtable.getMenu().add(pjmTracks);
    jtable.getMenu().addSeparator();
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
            || (nbClicks == 2 && !jtbEditable.isSelected())) {
          // selected row in view
          Track track = (Track) jtable.getSelection().get(0);
          File file = track.getPlayeableFile(false);
          if (file != null) {
            try {
              // launch it
              FIFO.push(new StackItem(file, Conf.getBoolean(Const.CONF_STATE_REPEAT)), Conf
                  .getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));

            } catch (JajukException je) {
              Log.error(je);
            }
          } else {
            Messages.showErrorMessage(10, track.getName());
          }
        }
      }
    });
    TracksTableView.super.finished();
  }

  /** Fill the table */
  @Override
  public JajukTableModel populateTable() {
    // model creation
    return new TracksTableModel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTableView#initTable()
   */
  @Override
  void initTable() {
    boolean bEditable = Conf.getBoolean(Const.CONF_TRACKS_TABLE_EDITION);
    jtbEditable.setSelected(bEditable);
  }

}
