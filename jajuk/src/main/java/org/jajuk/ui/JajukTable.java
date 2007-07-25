/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * JXTable with following features:
 * <p>
 * Remembers columns visibility
 * <p>
 * Tooltips on each cell
 */
public class JajukTable extends JXTable implements ITechnicalStrings {

	private static final long serialVersionUID = 1L;

	private String sConf;

	/**
	 * Constructor
	 * 
	 * @param model :
	 *            model to use
	 * @param bSortable :
	 *            is this table sortable
	 * @sConf: configuration variable used to store columns conf
	 */
	public JajukTable(TableModel model, boolean bSortable, String sConf) {
		super(model);
		this.sConf = sConf;
		setShowGrid(false);
		init(bSortable);
		// Force to use Jajuk cell render for all columns
		for (TableColumn col : getColumns()) {
			col.setCellRenderer(new JajukCellRender());
		}
		/*
		// Add alternate rows highlither
		ColorScheme colors = SubstanceLookAndFeel.getActiveColorScheme();
		if (SubstanceLookAndFeel.getTheme().getKind() == ThemeKind.DARK) {
			addHighlighter(new AlternateRowHighlighter(colors.getMidColor(),
					colors.getDarkColor(), colors.getForegroundColor()));
		} else {
			addHighlighter(new AlternateRowHighlighter(Color.WHITE, colors
					.getUltraLightColor(), colors.getForegroundColor()));
		}*/
	}

	/**
	 * Constructor
	 * 
	 * @param model :
	 *            model to use
	 * @sConf: configuration variable used to store columns conf
	 */
	public JajukTable(TableModel model, String sConf) {
		this(model, true, sConf);
	}

	private void init(boolean bSortable) {
		super.setSortable(bSortable);
		super.setColumnControlVisible(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		packAll();
	}

	/**
	 * Select columns to show colsToShow list of columns id to keep
	 */
	public void showColumns(ArrayList<String> colsToShow) {
		Iterator it = ((DefaultTableColumnModelExt) getColumnModel())
				.getColumns(false).iterator();
		while (it.hasNext()) {
			TableColumnExt col = (TableColumnExt) it.next();
			if (!colsToShow.contains(((JajukTableModel) getModel())
					.getIdentifier(col.getModelIndex()))) {
				col.setVisible(false);
			}
		}
	}

	/**
	 * 
	 * @return list of visible columns names as string
	 * @param Name
	 *            of the configuration key giving configuration
	 */
	public ArrayList<String> getColumnsConf() {
		ArrayList<String> alOut = new ArrayList<String>(10);
		String value = ConfigurationManager.getProperty(sConf);
		StringTokenizer st = new StringTokenizer(value, ","); 
		while (st.hasMoreTokens()) {
			alOut.add(st.nextToken());
		}
		return alOut;
	}

	/**
	 * Add a new property into columns conf
	 * 
	 * @param property
	 */
	public void addColumnIntoConf(String property) {
		if (sConf == null) {
			return;
		}
		ArrayList alOut = getColumnsConf();
		if (!alOut.contains(property)) {
			String value = ConfigurationManager.getProperty(sConf);
			ConfigurationManager.setProperty(sConf, value + "," + property); 
		}
	}

	/**
	 * Remove a property from columns conf
	 * 
	 * @param property
	 */
	public void removeColumnFromConf(String property) {
		if (sConf == null) {
			return;
		}
		ArrayList alOut = getColumnsConf();
		alOut.remove(property);
		ConfigurationManager.setProperty(sConf, getColumnsConf(alOut));
	}

	/**
	 * 
	 * Create the jtable visible columns conf
	 * 
	 */
	public void createColumnsConf() {
		StringBuffer sb = new StringBuffer();
		Iterator it = ((DefaultTableColumnModelExt) getColumnModel())
				.getColumns(true).iterator();
		while (it.hasNext()) {
			TableColumnExt col = (TableColumnExt) it.next();
			String sIdentifier = ((JajukTableModel) getModel())
					.getIdentifier(col.getModelIndex());
			if (col.isVisible()) {
				sb.append(sIdentifier + ","); 
			}
		}
		String value;
		// remove last coma
		if (sb.length() > 0) {
			value = sb.substring(0, sb.length() - 1);
		} else {
			value = sb.toString();
		}
		ConfigurationManager.setProperty(sConf, value);
	}

	/**
	 * 
	 * @return columns configuration from given list of columns identifiers
	 * 
	 */
	private String getColumnsConf(ArrayList alCol) {
		StringBuffer sb = new StringBuffer();
		Iterator it = alCol.iterator();
		while (it.hasNext()) {
			sb.append((String) it.next() + ","); 
		}
		// remove last coma
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return sb.toString();
		}
	}

	/**
	 * add tooltips to each cell
	 */
	public String getToolTipText(MouseEvent e) {
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		if (rowIndex < 0 || colIndex < 0) {
			return null;
		}
		Object o = getModel().getValueAt(convertRowIndexToModel(rowIndex),
				convertColumnIndexToModel(colIndex));
		if (o == null) {
			return null;
		} else if (o instanceof IconLabel) {
			return ((IconLabel) o).getTooltip();
		} else if (o instanceof Date) {
			return Util.getLocaleDateFormatter().format((Date) o);
		} else {
			return o.toString();
		}
	}

		/**
	 * Select a list of rows
	 * @param indexes list of row indexes to be selected 
	 */
	public void setSelectedrows(int[] indexes) {
		for (int i = 0; i < indexes.length; i++) {
			addRowSelectionInterval(indexes[i], indexes[i]);
		}
	}
}
