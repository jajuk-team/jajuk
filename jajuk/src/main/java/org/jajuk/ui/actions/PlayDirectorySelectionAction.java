/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
import java.util.List;

import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;

/**
 * Play directories for a selection of files. For now, jajuk only play the first
 * found full directory
 * <p>
 * Action emitter is responsible to ensure all items provided share the same
 * type
 * </p>
 * <p>
 * Selection data is provided using the swing properties DETAIL_SELECTION
 * </p>
 */
public class PlayDirectorySelectionAction extends SelectionAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -8078402652430413821L;

  /**
   * Instantiates a new play directory selection action.
   */
  PlayDirectorySelectionAction() {
    super(Messages.getString("FilesTableView.15"),
        IconLoader.getIcon(JajukIcons.DIRECTORY_SYNCHRO), true);
    setShortDescription(Messages.getString("FilesTableView.15"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws Exception {
    new Thread("PlayDirectorySelectionAction") {
      @Override
      public void run() {
        try {
          PlayDirectorySelectionAction.super.perform(e);
          if (selection.size() == 0 || !(selection.get(0) instanceof File)) {
            return;
          }
          // Select all files from the first found directory
          Directory dir = ((File) selection.get(0)).getDirectory();
          List<File> files = UtilFeatures.getPlayableFiles(dir);
          QueueModel.push(
              UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(files),
                  Conf.getBoolean(Const.CONF_STATE_REPEAT), true), false);
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
