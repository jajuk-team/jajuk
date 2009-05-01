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

import org.jajuk.ui.widgets.FullscreenPlayerFrame;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * 
 */
public class FullscreenAction extends JajukAction {

  /**
   * 
   */
  private static final long serialVersionUID = 1223773056757729079L;

  FullscreenAction() {
    super(Messages.getString("JajukFullscreen.0"), IconLoader.getIcon(JajukIcons.FULL_SCREEN), true);
    setShortDescription(Messages.getString("JajukFullscreen.0"));
  }

  @Override
  public void perform(ActionEvent evt) throws Exception {
   
    FullscreenPlayerFrame fspf = FullscreenPlayerFrame.getInstance();
    fspf.setFullScreen(!fspf.isFullscreen());
  }

}
