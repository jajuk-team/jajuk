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

import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.dj.Ambience;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

import java.awt.event.ActionEvent;
import java.util.List;

public class NoveltiesAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	NoveltiesAction() {
		super(
				Messages.getString("JajukWindow.15"), Util.getIcon(ICON_NOVELTIES), true); //$NON-NLS-1$
		setShortDescription(Messages.getString("JajukWindow.31")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) throws JajukException {
		Ambience ambience = AmbienceManager.getInstance().getSelectedAmbience();
		List<File> alToPlay = Util.filterByAmbience(FileManager.getInstance()
				.getShuffleNoveltiesPlaylist(), ambience);
		if (alToPlay != null && alToPlay.size() > 0) {
			FIFO.getInstance().push(
					Util.createStackItems(Util.applyPlayOption(alToPlay),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
							false), false);
		} else { // none novelty found
			Messages.showWarningMessage(Messages.getString("Error.127")); //$NON-NLS-1$
		}
	}
}
