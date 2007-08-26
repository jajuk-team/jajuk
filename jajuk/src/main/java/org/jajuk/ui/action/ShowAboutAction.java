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
import org.jajuk.ui.wizard.AboutWindow;
import org.jajuk.util.IconLoader;

import java.awt.event.ActionEvent;

/**
 * Action for displaying the tip of the day.
 */
public class ShowAboutAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	ShowAboutAction() {
		super(
				Messages.getString("JajukWindow.5"), IconLoader.ICON_INFO, true); 
		setShortDescription(Messages.getString("JajukWindow.22")); 
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void perform(ActionEvent evt) {
		new AboutWindow();
	}
}
