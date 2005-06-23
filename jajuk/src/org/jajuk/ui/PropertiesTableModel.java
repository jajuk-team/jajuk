/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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

import org.jajuk.base.IPropertyable;
import org.jajuk.util.ITechnicalStrings;

/**
 *  Table model used for properties table
 * @author     Bertrand Florat
 * @created    06 jun. 2005
 */
public class PropertiesTableModel extends AbstractTableModel implements ITechnicalStrings{
	
	/**Columns number*/
	protected int iColNum;
	
	/**Rows number*/
	protected int iRowNum;
	
	/**Values table**/
	protected Object[][] oValues;
	
	/**Columns names table**/
	protected String[] sColName;
	
    /**Item to display*/
    protected IPropertyable pa;
    
	/**
	 * Model constructor
	 * @param iColNum number of rows
	 * @param bCellEditable cell editability
	 * @param sColName columns names
	 */
	public PropertiesTableModel(int iColNum,String[] sColName,IPropertyable pa){
		this.iColNum = iColNum;
		this.sColName = sColName;
        this.pa = pa;
	}
	
	public synchronized int getColumnCount() {
		return iColNum;
	}
	
	public synchronized int getRowCount() {
		return iRowNum;
	}
	
	public synchronized boolean isCellEditable(int rowIndex, int columnIndex) {
		String sProperty = (String)oValues[rowIndex][0];
        return pa.isPropertyEditable(sProperty);
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
