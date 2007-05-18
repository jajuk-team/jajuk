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
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;

import java.awt.event.ActionEvent;

public class ShuffleModeAction extends ActionBase {
	private static final long serialVersionUID = 1L;

	ShuffleModeAction() {
		super(Messages.getString("JajukJMenuBar.11"), IconLoader.ICON_SHUFFLE,
				"ctrl H", true, false);  
		setShortDescription(Messages.getString("CommandJPanel.2")); 
	}

	/**
	 * Invoked when an action occurs.
	 * 
	 * @param evt
	 */
	public void perform(ActionEvent evt) {
		boolean b = ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE);
		ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, Boolean.toString(!b));

		JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(!b);
		CommandJPanel.getInstance().jbRandom.setSelected(!b);
		if (!b) { // enabled button
			FIFO.getInstance().shuffle(); // shuffle current selection
			// now make sure we can't have a single repeated file after a
			// non-repeated file (by design)
			if (FIFO.getInstance().containsRepeat() && !FIFO.getInstance().containsOnlyRepeat()) {
				FIFO.getInstance().setRepeatModeToAll(false); // yes?
				// un-repeat all
			}
			// computes planned tracks
			FIFO.getInstance().computesPlanned(true);
		}

	}
}
