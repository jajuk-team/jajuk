/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jvnet.substance.SubstanceDefaultTableCellRenderer;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Cell renderer to support cells color and icons
 */
public class JajukCellRender extends SubstanceDefaultTableCellRenderer implements ITechnicalStrings {

	private static final long serialVersionUID = 154545454L;

	public Component getTableCellRendererComponent(JTable table, Object oValue, boolean selected,
			boolean focused, int row, int column) {
		Component c = super.getTableCellRendererComponent(table, oValue, selected, focused, row,
				column);
		if (oValue instanceof IconLabel){
			((JLabel)c).setIcon(((IconLabel)oValue));
			((JLabel)c).setToolTipText(((IconLabel)oValue).getTooltip());
			((JLabel)c).setFont(((IconLabel)oValue).getFont());
			((JLabel)c).setText(((IconLabel)oValue).getText());
		}
		c.setEnabled(table == null || table.isEnabled());
		c.setFont(new Font("Dialog", Font.PLAIN, ConfigurationManager.getInt(CONF_FONTS_SIZE))); 
		return c;
	}

}
