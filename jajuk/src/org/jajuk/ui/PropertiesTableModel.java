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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.jajuk.base.Device;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.SequentialMap;
import org.jajuk.util.Util;

/**
 *  Table model used for properties table
 * @author     Bertrand Florat
 * @created    06 jun. 2005
 */
public class PropertiesTableModel extends AbstractTableModel 
    implements ITechnicalStrings,TableModelListener,Observer{
	
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
	public PropertiesTableModel(IPropertyable pa){
		this.iColNum = 4;
	    this.pa = pa;
        // Columns names
        this.sColName= new String[] {
                Messages.getString("PropertiesWizard.3"), //$NON-NLS-1$
                Messages.getString("PropertiesWizard.1"),//$NON-NLS-2$
                Messages.getString("PropertiesWizard.2"),//$NON-NLS-2$
                Messages.getString("PropertiesWizard.4")};  //$NON-NLS-3$ 
        // Values
        //set ignored attributes we won't display
        ArrayList alIgnored = new ArrayList(10);
        alIgnored.add(XML_ID);//ID
        alIgnored.add(XML_EXPANDED);//expanded state
        alIgnored.add(XML_HASHCODE);//hashcode
        SequentialMap propertiesOrig = pa.getProperties();
        ArrayList properties = new ArrayList(10); //properties after cleaning
        //remove hiden attributes
        Iterator it  = propertiesOrig.keys().iterator();
        int ignored = 0;
        while (it.hasNext()){
            String sProperty = (String)it.next();
            if (!alIgnored.contains(sProperty)){
                properties.add(sProperty);
            }
            else{
                ignored ++;
            }
        }
        iRowNum = propertiesOrig.size()-ignored;
        it = properties.iterator();
        oValues = new Object[iRowNum][iColNum+2]; //two hidden columns for attribute name and ID element if any
        for (int i = 0; it.hasNext(); i++) { //we don't display first attribute (ID)
            String sKey = (String) it.next();
            if (pa.isPropertyEditable(sKey)){
                oValues[i][0] = Util.getIcon(ICON_EDIT);    
            }
            else{
                oValues[i][0] = Util.getIcon(ICON_NO_EDIT);
            }
            oValues[i][1] = Messages.getInstance().contains("Property_"+sKey)?
                    Messages.getString("Property_"+sKey):sKey; //check if property name is translated (for custom properties)
            oValues[i][2] = pa.getHumanValue(sKey);
            oValues[i][4] = sKey;
            oValues[i][5] = pa.getValue(sKey); //attrbute ID or value
               //link 
            if (isLinkable(i))      {
                oValues[i][3] = Util.getIcon(ICON_PROPERTIES);
            }
            else{
                oValues[i][3] = Util.getIcon(ICON_VOID);
            }
        }
        //listen for table changes
        addTableModelListener(this);
        //Add observers
        ObservationManager.register(EVENT_FILE_NAME_CHANGED,this);
     }
	
	
	public synchronized int getColumnCount() {
		return iColNum;
	}
	
	public synchronized int getRowCount() {
		return iRowNum;
	}
	
	public synchronized boolean isCellEditable(int rowIndex, int columnIndex) {
		String sProperty = (String)oValues[rowIndex][4];
        return pa != null && (pa.isPropertyEditable(sProperty) && (columnIndex==2));
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
    
    public boolean isLinkable(int iRow){
    String sKey = (String) getValueAt(iRow,4);
    return sKey.equals(XML_DEVICE) || sKey.equals(XML_TRACK)
      || sKey.equals(XML_DEVICE) || sKey.equals(XML_TRACK)
      ||     sKey.equals(XML_ALBUM) || sKey.equals(XML_AUTHOR) || sKey.equals(XML_STYLE)
      ||     sKey.equals(XML_DIRECTORY) || sKey.equals(XML_FILE)
      ||     sKey.equals(XML_PLAYLIST) || sKey.equals(XML_PLAYLIST_FILE)
      ||    ( sKey.equals(XML_TYPE) && !(pa instanceof Device)) ;   //avoid to confuse between music types and device types
    }
    
	public synchronized Object getValueAt(int rowIndex, int columnIndex) {
            return oValues[rowIndex][columnIndex];
   }
	
	public  synchronized void setValueAt(Object oValue, int rowIndex, int columnIndex) {
		oValues[rowIndex][columnIndex] = oValue;
        fireTableCellUpdated(rowIndex,columnIndex);
	}
	
	public String getColumnName(int columnIndex) {
		return sColName[columnIndex];
	}
	
 /* (non-Javadoc)
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(TableModelEvent e) {
        String sKey = (String)getValueAt(e.getFirstRow(),4);
        String sValue = (String)getValueAt(e.getFirstRow(),2);
        if (XML_NAME.equals(sKey)){
            if (pa instanceof File){
                File file = (File)pa;
                File fNew = FileManager.changeFileName(file,sValue);
                pa = fNew; 
            }
        }
        //others properties
        else{
            pa. setProperty(sKey,sValue);
        }
    }


    /* (non-Javadoc)
     * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if (EVENT_FILE_NAME_CHANGED.equals(subject)){
            Properties properties = event.getDetails();
            String sNewId = properties.getProperty(DETAIL_NEW);
            pa = FileManager.getFileById(sNewId);
        }
    }
    
    
    
}
