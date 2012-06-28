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
 *  
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.error.JajukException;

/**
 * .
 */
public class GlobalRandomAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new global random action.
   */
  GlobalRandomAction() {
    super(Messages.getString("JajukWindow.6"), IconLoader.getIcon(JajukIcons.SHUFFLE_GLOBAL), true);
    String sTooltip = Messages.getString("JajukWindow.23");
    Ambience ambience = AmbienceManager.getInstance().getAmbience(
        Conf.getString(Const.CONF_DEFAULT_AMBIENCE));
    if (ambience != null) {
      String sAmbience = ambience.getName();
      sTooltip = "<html>" + Messages.getString("JajukWindow.23") + "<p><b>" + sAmbience
          + "</b></p></html>";
    }
    setShortDescription(sTooltip);
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws JajukException {
    new Thread("Global Random Thread") {
      @Override
      public void run() {
        Ambience ambience = AmbienceManager.getInstance().getSelectedAmbience();
        List<File> alToPlay = UtilFeatures.filterByAmbience(FileManager.getInstance()
            .getGlobalShufflePlaylist(), ambience);
        // For perfs (mainly playlist editor view refresh), we set a ceil for
        // tracks number
        if (alToPlay.size() > Const.NB_TRACKS_ON_ACTION) {
          alToPlay = alToPlay.subList(0, Const.NB_TRACKS_ON_ACTION);
        }
        // Push them
        QueueModel.push(UtilFeatures.createStackItems(alToPlay,
            Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), false), false);
      }
    }.start();
  }
}
