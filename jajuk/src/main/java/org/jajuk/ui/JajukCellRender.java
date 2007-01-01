/*
 *  Jajuk
 *  Copyright (C) 2004 Bertrand Florat
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
 *  $Revision$
 */

package org.jajuk.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Cell renderer to support cells color and icons
 * 
 * @author Bertrand Florat
 * @created 12 nov. 2004
 */
public class JajukCellRender extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object oValue,
			boolean selected, boolean focused, int row, int column) {
		setEnabled(table == null || table.isEnabled()); 
		if (oValue instanceof IconLabel) {
			IconLabel iconLabel = (IconLabel) oValue;
			setIcon(iconLabel.getIcon());
			if (iconLabel.getText() != null) {
				setText(iconLabel.getText());
			}
			setBackground(iconLabel.getBackground());
			setForeground(iconLabel.getForeground());
			super.getTableCellRendererComponent(table, oValue, selected,
					focused, row, column); 
			if (iconLabel.getFont() != null) {
				setFont(iconLabel.getFont());
			}
		}
		return this;
	}

}
