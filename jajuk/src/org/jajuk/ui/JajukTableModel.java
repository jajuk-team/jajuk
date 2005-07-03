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
 *  $Revision$
 */

package org.jajuk.ui;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.jajuk.util.ITechnicalStrings;

/**
 *  Jajuk table model, adds identifier to model
 *
 * @author     Administrateur
 * @created    22 juin 2005
 */
public abstract class JajukTableModel extends DefaultTableModel  implements ITechnicalStrings{

    /**Column identifiers*/
    Vector vId = new Vector(10);
    
    /**Rows number*/
    int iRowNum;
    
    /**Values table**/
    Object[][] oValues;
    
    /**Number of standard rows*/
    int iNumberStandardRows;
    
    /**Cell editable flag*/
    boolean[][] bCellEditable;
    
    /**Column names*/
    Vector vColNames  = new Vector(10);
    
    
    public JajukTableModel(int iNumberStandardRows){
        this.iNumberStandardRows = iNumberStandardRows;
    }
    
     /**
     * @param sColName
     * @return Column identifier for a given column title
     */
    public String getIdentifier(String sColName){
        return (String)vId.get(vColNames.indexOf(sColName));
    }
    
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        return oValues[rowIndex][columnIndex];
    }
    
    public  synchronized void setValueAt(Object oValue, int rowIndex, int columnIndex) {
        oValues[rowIndex][columnIndex] = oValue;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return vColNames.size();
    }
    
    public String getColumnName(int column){
        return (String)vColNames.get(column);
    }
    
    public String getIdentifier(int column){
        return (String)vId.get(column);
    }
    
    public synchronized int getRowCount() {
        return iRowNum;
    }
    
    public synchronized boolean isCellEditable(int rowIndex, int columnIndex) {
        return bCellEditable[rowIndex][columnIndex];
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
    
    /**Filter table with following criterias
    * @param sProperty Property (column) to filter
    * @param sPattern pattern*/
    public abstract  void populateModel(String sProperty,String sPattern); 
                
}
