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
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.ui.widgets.CommandJPanel;
import org.jajuk.ui.widgets.JajukJMenuBar;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

public class IntroModeAction extends ActionBase {
  private static final long serialVersionUID = 1L;

  IntroModeAction() {
    super(Messages.getString("JajukJMenuBar.13"), IconLoader.ICON_INTRO, true);
    setShortDescription(Messages.getString("CommandJPanel.4"));
  }

  public void perform(ActionEvent evt) {
    boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_INTRO));
    ConfigurationManager.setProperty(CONF_STATE_INTRO, Boolean.toString(!b));
    JajukJMenuBar.getInstance().jcbmiIntro.setSelected(!b);
    CommandJPanel.getInstance().jbIntro.setSelected(!b);
  }
}
