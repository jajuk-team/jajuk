/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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
 *  
 */
package org.jajuk.ui.helpers;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.jajuk.base.Year;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.Const;
import org.jajuk.util.UtilString;
import org.jvnet.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 * Cell renderer to support cells color and icons
 * <p>
 * Note that by swing design, this renderer applies to an entire column. It is
 * useless to change a specific cell rendering according row or column number
 * See
 * http://java.sun.com/docs/books/tutorial/uiswing/components/table.html#editrender
 * </p>
 */
public class JajukCellRenderer extends SubstanceDefaultTableCellRenderer implements Const {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 154545454L;
  private final Color color;

  /**
   * The Constructor.
   * 
   * @param color background color for cells or null if default
   */
  public JajukCellRenderer(Color color) {
    super();
    this.color = color;
  }

  /**
   * Instantiates a new jajuk cell renderer.
   */
  public JajukCellRenderer() {
    this(null);
  }

  private final SubstanceDefaultTableCellRenderer.BooleanRenderer booleanRenderer = new SubstanceDefaultTableCellRenderer.BooleanRenderer();

  /* (non-Javadoc)
   * @see org.jvnet.substance.api.renderers.SubstanceDefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object oValue, boolean selected,
      boolean focused, int row, int column) {
    Component c = super
        .getTableCellRendererComponent(table, oValue, selected, focused, row, column);
    if (oValue instanceof IconLabel) {
      ((JLabel) c).setOpaque(false);
      ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
      ((JLabel) c).setIcon(((IconLabel) oValue));
      ((JLabel) c).setToolTipText(((IconLabel) oValue).getTooltip());
      ((JLabel) c).setFont(((IconLabel) oValue).getFont());
      ((JLabel) c).setText(((IconLabel) oValue).getText());
    } else if (oValue instanceof Date) {
      ((JLabel) c).setText(UtilString.getLocaleDateFormatter().format(((Date) oValue)));
    } else if (oValue instanceof Boolean) {
      c = booleanRenderer.getTableCellRendererComponent(table, oValue, selected, focused, row,
          column);
    } else if (oValue instanceof Duration) {
      ((JLabel) c).setText(((Duration) oValue).toString());
    } else if (oValue instanceof Year) {
      Long year = ((Year) oValue).getValue();
      ((JLabel) c).setText(year.toString());
    }
    c.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
    if (color != null) {
      c.setBackground(color);
    }
    return c;
  }
}
