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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.jajuk.base.Directory;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.error.JajukException;

public class FinishAlbumAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  FinishAlbumAction() {
    super(Messages.getString("JajukWindow.16"), IconLoader.getIcon(JajukIcons.FINISH_ALBUM), !FIFO
        .isStopped());
    setShortDescription(Messages.getString("JajukWindow.32"));
  }

  @Override
  public void perform(ActionEvent evt) throws JajukException {
    StackItem item = FIFO.getCurrentItem();// stores
    // current item
    FIFO.clear(); // clear fifo
    Directory dir = item.getFile().getDirectory();
    // then re-add current item
    FIFO.push(UtilFeatures.createStackItems(dir.getFilesFromFile(item.getFile()), item.isRepeat(),
        item.isUserLaunch()), true);
    FIFO.computesPlanned(true); // update planned list
    Properties properties = new Properties();
    properties.put(Const.DETAIL_ORIGIN, Const.DETAIL_SPECIAL_MODE_NORMAL);
    ObservationManager.notify(new JajukEvent(JajukEvents.SPECIAL_MODE, properties));
  }
}
