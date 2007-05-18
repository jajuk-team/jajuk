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

import org.jajuk.i18n.Messages;
import org.jajuk.ui.perspectives.ConfigurationPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.IconLoader;

import java.awt.event.ActionEvent;

/**
 * Action for configurate the application
 */
public class ConfigurationRequiredAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	ConfigurationRequiredAction() {
		super(
				Messages.getString("JajukJMenuBar.22"), IconLoader.ICON_CONFIGURATION, true);  
	}

	public void perform(ActionEvent evt) {
		PerspectiveManager
				.setCurrentPerspective(ConfigurationPerspective.class.getName());
	}
}
