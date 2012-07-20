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

import org.jajuk.base.File;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;

/**
 * Play repeat a selection
 * <p>
 * Action emitter is responsible to ensure all items provided share the same
 * type
 * </p>
 * <p>
 * Selection data is provided using the swing properties DETAIL_SELECTION
 * </p>.
 */
public class PlayRepeatSelectionAction extends SelectionAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -8078402652430413821L;

  /**
   * Instantiates a new play repeat selection action.
   */
  PlayRepeatSelectionAction() {
    super(Messages.getString("TracksTableView.10"), IconLoader.getIcon(JajukIcons.REPEAT), true);
    setShortDescription(Messages.getString("TracksTableView.10"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws Exception {
    new Thread("PlayRepeatSelectionAction") {
      @Override
      public void run() {
        try {
          PlayRepeatSelectionAction.super.perform(e);
          List<File> files = UtilFeatures.getPlayableFiles(selection);
          QueueModel
              .push(UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(files), true, true),
                  false);
          if (files.size() == 1) {
            UtilGUI.setRepeatSingleGui(true);
          } else {
            UtilGUI.setRepeatAllGui(true);
          }
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
