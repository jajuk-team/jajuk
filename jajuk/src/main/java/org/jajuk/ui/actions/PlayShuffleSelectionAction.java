/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jajuk.base.File;
import org.jajuk.services.players.FIFO;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * Play shuffle a selection
 * <p>
 * Action emitter is responsible to ensure all items provided share the same
 * type
 * </p>
 * <p>
 * Selection data is provided using the swing properties DETAIL_SELECTION
 * </p>
 */
public class PlayShuffleSelectionAction extends SelectionAction {

  private static final long serialVersionUID = -8078402652430413821L;

  PlayShuffleSelectionAction() {
    super(Messages.getString("TracksTableView.9"), IconLoader.ICON_SHUFFLE, true);
    setShortDescription(Messages.getString("TracksTableView.9"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.ActionBase#perform(java.awt.event.ActionEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void perform(ActionEvent e) throws Exception {
    super.perform(e);
    List<File> files = Util.getPlayableFiles(selection);
    Collections.shuffle(files, new Random());
    FIFO.getInstance().push(
        Util.createStackItems(Util.applyPlayOption(files), ConfigurationManager
            .getBoolean(CONF_STATE_REPEAT), true), false);
  }

}
