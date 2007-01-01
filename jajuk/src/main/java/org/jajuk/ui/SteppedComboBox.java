/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
 * $Revision$
 */
package org.jajuk.ui;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.ComboBoxUI;

/**
 * Stepped combo box allowing to display a long text in the history bar
 * 
 * @Author Bertrand Florat
 * @created 17 oct. 2003
 */

public class SteppedComboBox extends JComboBox {
	private static final long serialVersionUID = 1L;

	protected int popupWidth;

	public SteppedComboBox() {
		super();
		init();
	}

	public SteppedComboBox(ComboBoxModel aModel) {
		super(aModel);
		init();
	}

	public SteppedComboBox(final Object[] items) {
		super(items);
		init();
	}

	public SteppedComboBox(Vector items) {
		super(items);
		init();
	}

	public void setPopupWidth(int width) {
		popupWidth = width;
	}

	public Dimension getPopupSize() {
		Dimension size = getSize();
		if (popupWidth < 1)
			popupWidth = size.width;
		return new Dimension(popupWidth, size.height);
	}

	protected void init() {
		try {
			ComboBoxUI cbui = LNFManager.getSteppedComboBoxClass();
			if (cbui != null) {
				setUI(cbui);
				popupWidth = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
