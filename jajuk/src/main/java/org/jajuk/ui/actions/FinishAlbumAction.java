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
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * .
 */
public class FinishAlbumAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new finish album action.
   */
  FinishAlbumAction() {
    super(Messages.getString("JajukWindow.16"), IconLoader.getIcon(JajukIcons.FINISH_ALBUM),
        !QueueModel.isStopped());
    setShortDescription(Messages.getString("JajukWindow.32"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws JajukException {
    new Thread("FinishAlbumAction") {
      @Override
      public void run() {
        try {
          StackItem item = QueueModel.getCurrentItem();// stores
          // current item
          Directory dir = item.getFile().getDirectory();
          List<StackItem> stack = UtilFeatures.createStackItems(
              dir.getFilesFromFile(item.getFile()), item.isRepeat(), item.isUserLaunch());
          // Then re-add current item only if some more tracks are to be ran. Otherwise, just ignore
          // this command, better than displaying a bozing error message.
          if (stack != null && stack.size() > 0) {
            QueueModel.push(stack, true, true);
            QueueModel.computesPlanned(true); // update planned list
          }
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.start();
  }
}
