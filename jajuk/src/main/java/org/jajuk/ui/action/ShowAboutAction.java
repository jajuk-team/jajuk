/*
 *  Jajuk
 *  Copyright (C) 2005 Bart Cremers
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

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukWindow;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.Util;

/**
 * Action for displaying the tip of the day.
 * 
 * @author Bart Cremers
 * @version 12-dec-2005
 */
public class ShowAboutAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	ShowAboutAction() {
		super(
				Messages.getString("JajukWindow.5"), Util.getIcon(ICON_INFO), true); //$NON-NLS-1$
		setShortDescription(Messages.getString("JajukWindow.22")); //$NON-NLS-1$
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void perform(ActionEvent evt) {
		// set default perspective to show if UIi is not yet started
		if (Main.isUILaunched()) {
			PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_HELP);
		} else {
			Main.setDefaultPerspective(PERSPECTIVE_NAME_HELP);
		}
		// make frame visible
		if (!JajukWindow.getInstance().isWindowVisible()) {
			JajukWindow.getInstance().display(true);
		}
	}
}
