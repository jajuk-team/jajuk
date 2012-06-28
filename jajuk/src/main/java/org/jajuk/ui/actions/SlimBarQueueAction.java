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

import org.jajuk.ui.windows.JajukSlimbar;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * .
 */
public class SlimBarQueueAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1223773056757729079L;

  /**
   * Instantiates a new slim bar queue action.
   */
  SlimBarQueueAction() {
    super(Messages.getString("PhysicalPlaylistRepositoryView.9"), IconLoader
        .getIcon(JajukIcons.PLAYLIST_FILE), true);
    setShortDescription(Messages.getString("PhysicalPlaylistRepositoryView.9"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws Exception {
    JajukSlimbar slimBar = JajukSlimbar.getInstance();
    slimBar.setDisplayQueue(!slimBar.isDisplayQueue());
  }
}
