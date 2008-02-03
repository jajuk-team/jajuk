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
 *  $$Revision: 2403 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.error.JajukException;

public class CheckForUpdateAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  CheckForUpdateAction() {
    super(Messages.getString("JajukJMenuBar.27"), IconLoader.ICON_UPDATE_MANAGER, true);
    setShortDescription(Messages.getString("JajukJMenuBar.27"));
  }

  public void perform(ActionEvent evt) throws JajukException {
    String newRelease = UpgradeManager.getNewVersionName();
    if (newRelease != null) {
      Messages.showInfoMessage(Messages.getString("UpdateManager.0") + newRelease
          + Messages.getString("UpdateManager.1"));
    } else {
      Messages.showInfoMessage(Messages.getString("UpdateManager.2"));
    }
  }
}
