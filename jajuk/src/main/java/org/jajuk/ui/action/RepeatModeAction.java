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
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;

import java.awt.event.ActionEvent;

public class RepeatModeAction extends ActionBase {
	private static final long serialVersionUID = 1L;

	RepeatModeAction() {
		super(Messages.getString("JajukJMenuBar.10"),
				IconLoader.ICON_REPEAT, "ctrl T", true, false);  
		setShortDescription(Messages.getString("CommandJPanel.1")); 
	}

	/**
	 * Invoked when an action occurs.
	 * 
	 * @param evt
	 */
	public void perform(ActionEvent evt) {

		boolean b = ConfigurationManager.getBoolean(CONF_STATE_REPEAT);
		ConfigurationManager.setProperty(CONF_STATE_REPEAT, Boolean
				.toString(!b));

		JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(!b);
		CommandJPanel.getInstance().jbRepeat.setSelected(!b);

		if (!b) { // enabled button
			// if FIFO is not void, repeat over current item
			StackItem item = FIFO.getInstance().getCurrentItem();
			if (item != null && FIFO.getInstance().getIndex() == 0) { // only
				// non-repeated
				// items
				// need
				// to be
				// set
				// and
				// in
				// this
				// case,
				// index
				// =0 or
				// bug
				item.setRepeat(true);
			}
		} else {// disable repeat mode
			// remove repeat mode to all items
			FIFO.getInstance().setRepeatModeToAll(false);
			// remove tracks before current position
			FIFO.getInstance().remove(0, FIFO.getInstance().getIndex() - 1);
			FIFO.getInstance().setIndex(0); // select first track
		}
		// computes planned tracks
		FIFO.getInstance().computesPlanned(false);
	}
}
