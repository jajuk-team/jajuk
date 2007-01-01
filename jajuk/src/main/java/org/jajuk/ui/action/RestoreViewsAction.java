/*
 *  Jajuk
 *  Copyright (C) 2006 Bertrand Florat
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

import org.jajuk.i18n.Messages;
import org.jajuk.ui.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

/**
 * @author Bertrand Florat
 */
public class RestoreViewsAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	RestoreViewsAction() {
		super(Messages.getString("JajukJMenuBar.17"), Util
				.getIcon(ICON_REFRESH), true); //$NON-NLS-1$
		setShortDescription(Messages.getString("JajukJMenuBar.17")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) throws JajukException {
		new Thread() {
			public void run() {
				IPerspective perspective = PerspectiveManager
						.getCurrentPerspective();
				perspective.restoreDefaults();
			}
		}.start();

	}
}
