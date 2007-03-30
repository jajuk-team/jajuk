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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * JTable with followinf features:
 * <p>
 * Sortable
 * <p>
 * Tooltips on each cell
 * 
 * @author Bertrand Florat
 * @created 21 feb. 2004
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
	public JajukTable(TableModel model, TableColumnModel colModel, boolean bSortable, String sConf) {
		super(model, colModel);
		this.sConf = sConf;
		setShowGrid(false);
		setOpaque(false);
		init(bSortable);
		setfont();
	}

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
		setOpaque(false);
		init(bSortable);
		setfont();
	}

	/**
	 * Set font by setting a default cell renderer on the table
	 *
	 */
	private void setfont(){
		Iterator it = ((DefaultTableColumnModelExt) getColumnModel()).getColumns(true)
				.iterator();
		while (it.hasNext()) {
			TableColumnExt col = (TableColumnExt) it.next();
						col.setCellRenderer(new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 3566323371751785978L;

					public Component getTableCellRendererComponent(JTable table, Object value,
							boolean isSelected, boolean hasFocus, int row, int column) {
						super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
						setFont(new Font(
								"Dialog", Font.PLAIN, ConfigurationManager.getInt(CONF_FONTS_SIZE))); //$NON-NLS-1$
						return this;
					}
				});
		}
			
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
		Highlighter highlighter = new RolloverHighlighter(Color.LIGHT_GRAY, Color.BLACK);
		Highlighter hAlternate = new AlternateRowHighlighter();
		HighlighterPipeline pipeHighlight = new HighlighterPipeline(new Highlighter[] { hAlternate,
				highlighter });
		setHighlighters(pipeHighlight);
		setRolloverEnabled(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		packAll();
	}

	/**
	 * Select columns to show colsToShow list of columns id to keep
	 */
	public void showColumns(ArrayList<String> colsToShow) {
		Iterator it = ((DefaultTableColumnModelExt) getColumnModel()).getColumns(false).iterator();
		while (it.hasNext()) {
			TableColumnExt col = (TableColumnExt) it.next();
			if (!colsToShow.contains(((JajukTableModel) getModel()).getIdentifier(col
					.getModelIndex()))) {
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
		StringTokenizer st = new StringTokenizer(value, ","); //$NON-NLS-1$
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
			ConfigurationManager.setProperty(sConf, value + "," + property); //$NON-NLS-1$
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
		Iterator it = ((DefaultTableColumnModelExt) getColumnModel()).getColumns(true).iterator();
		while (it.hasNext()) {
			TableColumnExt col = (TableColumnExt) it.next();
			String sIdentifier = ((JajukTableModel) getModel()).getIdentifier(col.getModelIndex());
			if (col.isVisible()) {
				sb.append(sIdentifier + ","); //$NON-NLS-1$
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
			sb.append((String) it.next() + ","); //$NON-NLS-1$
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

}
