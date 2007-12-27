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

package org.jajuk.ui.helpers;

import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jvnet.substance.SubstanceDefaultTableCellRenderer;

import java.awt.Component;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Cell renderer to support cells color and icons
 */
public class JajukCellRender extends SubstanceDefaultTableCellRenderer implements ITechnicalStrings {

  private static final long serialVersionUID = 154545454L;

  private SubstanceDefaultTableCellRenderer.BooleanRenderer booleanRenderer = new SubstanceDefaultTableCellRenderer.BooleanRenderer();

  public Component getTableCellRendererComponent(JTable table, Object oValue, boolean selected,
      boolean focused, int row, int column) {
    Component c = super
        .getTableCellRendererComponent(table, oValue, selected, focused, row, column);
    if (oValue instanceof IconLabel) {
      ((JLabel) c).setOpaque(false);
      ((JLabel) c).setIcon(((IconLabel) oValue));
      ((JLabel) c).setToolTipText(((IconLabel) oValue).getTooltip());
      ((JLabel) c).setFont(((IconLabel) oValue).getFont());
      ((JLabel) c).setText(((IconLabel) oValue).getText());
    } else if (oValue instanceof Date) {
      ((JLabel) c).setText(Util.getLocaleDateFormatter().format(((Date) oValue)));
    } else if (oValue instanceof Boolean) {
      c = booleanRenderer.getTableCellRendererComponent(table, oValue, selected, focused, row,
          column);
    } else if (oValue instanceof Duration) {
      ((JLabel) c).setText(((Duration)oValue).toString());
    }
    c.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
    return c;
  }

}
