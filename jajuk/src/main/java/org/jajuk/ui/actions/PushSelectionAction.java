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
 *  $Revision$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

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
 * Push a selection
 * <p>
 * Action emitter is responsible to ensure all items provided share the same
 * type
 * </p>
 * <p>
 * Selection data is provided using the swing properties DETAIL_SELECTION
 * </p>.
 */
public class PushSelectionAction extends SelectionAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -8078402652430413821L;

  /**
   * Instantiates a new push selection action.
   */
  PushSelectionAction() {
    super(Messages.getString("TracksTableView.8"), IconLoader.getIcon(JajukIcons.PUSH), true);
    setShortDescription(Messages.getString("TracksTableView.8"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws Exception {
    new Thread("PushSelectionAction") {
      @Override
      public void run() {
        try {
          PushSelectionAction.super.perform(e);
          List<File> files = UtilFeatures.getPlayableFiles(selection);
          QueueModel.push(
              UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(files),
                  Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), true), true);
        } catch (Exception e1) {
          Log.error(e1);
        }
      }
    }.start();
  }
}
