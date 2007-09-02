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
 *  $Revision$
 */

package org.jajuk.ui;

import org.jajuk.base.Item;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;

import java.util.HashSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

/**
 * Jajuk table model, adds identifier to model
 */
public abstract class JajukTableModel extends DefaultTableModel implements ITechnicalStrings {

	/** Column identifiers */
	volatile public Vector<String> vId = new Vector<String>(10);

	/** Rows number */
	public int iRowNum;

	/** Values table* */
	public Object[][] oValues;

	// Play icon in cache
	public static final ImageIcon PLAY_ICON = IconLoader.ICON_PLAY_TABLE;

	// Unmount Play icon in cache
	public static final ImageIcon UNMOUNT_PLAY_ICON = IconLoader.ICON_UNKNOWN;

	/** Objects */
	public Item[] oItems;

	/** Number of standard columns */
	public int iNumberStandardCols;

	/** Cell editable flag */
	public boolean[][] bCellEditable;

	/** Column names */
	public Vector<String> vColNames = new Vector<String>(10);

	/** Last value used for undo */
	public Object oLast = null;

	/** Editable flag */
	boolean bEditable = false;
	
	/** Tree selection*/
	public HashSet<Item> treeSelection;

	/**
	 * 
	 * @param iNumberStandardCols
	 *            Number of columns of this model (without custom properties)
	 */
	public JajukTableModel(int iNumberStandardCols) {
		this.iNumberStandardCols = iNumberStandardCols;
	}

	/**
	 * 
	 * Default constructor
	 */
	public JajukTableModel() {
		this.iNumberStandardCols = 0;
	}

	/**
	 * @param sColName
	 * @return Column identifier for a given column title
	 */
	public String getIdentifier(String sColName) {
		return vId.get(vColNames.indexOf(sColName));
	}

	/**
	 * Return item at given position
	 * 
	 * @param iRow
	 * @return
	 */
	public Item getItemAt(int iRow) {
		return oItems[iRow];
	}

	/**
	 * Set item at given position
	 * 
	 * @param iRow
	 * @param IPropertyabe
	 *            item to set
	 */
	public void setItemAt(int iRow, Item item) {
		oItems[iRow] = item;
	}

	public synchronized Object getValueAt(int rowIndex, int columnIndex) {
		//We need to test this as UI may request it before table is populated
		if (oValues == null){
			return null;
		}
		return oValues[rowIndex][columnIndex];
	}

	public synchronized void setValueAt(Object oValue, int rowIndex, int columnIndex) {
		oLast = oValues[rowIndex][columnIndex];
		oValues[rowIndex][columnIndex] = oValue;
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return vColNames.size();
	}

	/**
	 * Undo last change
	 * 
	 */
	public void undo(int rowIndex, int columnIndex) {
		if (oLast != null) {
			oValues[rowIndex][columnIndex] = oLast;
		}
	}

	public String getColumnName(int column) {
		return vColNames.get(column);
	}

	public String getIdentifier(int column) {
		return vId.get(column);
	}

	public int getRowCount() {
		return iRowNum;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return bEditable && bCellEditable[rowIndex][columnIndex];
	}

	public Class<? extends Object> getColumnClass(int columnIndex) {
		Object o = getValueAt(0, columnIndex);
		if (o != null) {
			return o.getClass();
		} else {
			return null;
		}
	}

	/**
	 * Fill model with data using an optionnal filter property and pattern
	 * 
	 * @param sProperty
	 *            Property (column) to filter
	 * @param sPattern
	 *            pattern
	 */
	public abstract void populateModel(String sProperty, String sPattern);

	/**
	 * Fill model with data
	 */
	public void populateModel() {
		populateModel(null, null);
	}

	/**
	 * Set this model editable state
	 * 
	 * @param b
	 *            whether model is editable or not
	 */
	public void setEditable(boolean b) {
		this.bEditable = b;
	}

}
