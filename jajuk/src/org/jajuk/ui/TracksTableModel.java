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

import javax.swing.table.AbstractTableModel;

/**
 *  Table model used for physical and logical table views
 * @author     Bertrand Florat
 * @created    29 feb. 2004
 */
public class TracksTableModel extends AbstractTableModel{
	
	/**Columns number*/
	protected int iColNum;
	
	/**Rows number*/
	protected int iRowNum;
	
	/**Cell editable table**/
	protected boolean[][] bCellEditable;
	
	/**Values table**/
	protected Object[][] oValues;
	
	/**Columns names table**/
	protected String[] sColName;
	
	/**
	 * Model constructor
	 * @param iColNum number of rows
	 * @param bCellEditable cell editability
	 * @param sColName columns names
	 */
	public TracksTableModel(int iColNum,boolean[][] bCellEditable,String[] sColName){
		this.iColNum = iColNum;
		this.bCellEditable = bCellEditable;
		this.sColName = sColName;
	}
	
	public synchronized int getColumnCount() {
		return iColNum;
	}
	
	public synchronized int getRowCount() {
		return iRowNum;
	}
	
	public synchronized boolean isCellEditable(int rowIndex, int columnIndex) {
		return bCellEditable[columnIndex][rowIndex];
	}
	
	public synchronized Class getColumnClass(int columnIndex) {
		Object o = getValueAt(0,columnIndex);
		if ( o != null){
			return o.getClass();
		}
		else{
			return null;
		}
	}
	
	public synchronized Object getValueAt(int rowIndex, int columnIndex) {
		return oValues[rowIndex][columnIndex];
	}
	
	public  synchronized void setValueAt(Object oValue, int rowIndex, int columnIndex) {
		oValues[rowIndex][columnIndex] = oValue;
	}
	
	/**
	 * Set all values used by this model
	 * @param oValues : cells values
	 */
	public synchronized void setValues(Object[][] oValues) {
		this.oValues = oValues;
		this.iRowNum = oValues.length;
	}
	
	public String getColumnName(int columnIndex) {
		return sColName[columnIndex];
	}
	

}
