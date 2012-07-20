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
import org.jajuk.base.GenreManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * .
 */
public class DJAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new dJ action.
   */
  DJAction() {
    super(Messages.getString("CommandJPanel.16"), IconLoader.getIcon(JajukIcons.DIGITAL_DJ), true);
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws JajukException {
    if (GenreManager.getInstance().getElementCount() == 0) {
      Messages.showErrorMessage(156); // void collection error
    } else {
      new Thread("DJ Thread") {
        @Override
        public void run() {
          DigitalDJ dj = DigitalDJManager.getInstance().getDJByID(
              Conf.getString(Const.CONF_DEFAULT_DJ));
          try {
            if (dj != null) {
              Conf.setProperty(Const.CONF_FADE_DURATION, Integer.toString(dj.getFadingDuration()));
              UtilGUI.waiting();
              List<File> al = dj.generatePlaylist();
              if (al != null && al.size() == 0) { // DJ constraints cannot be
                // respected
                Messages.showErrorMessage(158);
                return;
              }
              QueueModel.push(
                  UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(al),
                      Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), false), false);
            } else {
              Messages.showErrorMessage(157);
            }
          } catch (Exception e) {
            Log.debug(e);
            Messages.showErrorMessage(176);
          } finally {
            UtilGUI.stopWaiting();
          }
        }
      }.start();
    }
  }
}
