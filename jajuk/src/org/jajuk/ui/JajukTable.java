/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.sun.TableSorter;

/**
 *  JTable with followinf features: 
 * <p>Sortable
 * <p>Tooltips on each cell
 * @author     bflorat
 * @created    21 feb. 2004
 */
public class JajukTable extends JTable {
	
	/** Table sorter*/
	TableSorter ts;

	/**
	 * Constructor
	 * @param model : model to use
	 * @param bSortable : is this table sortable
	 * */
	public JajukTable(TableModel model,boolean bSortable) {
		super(new TableSorter(model));
		if ( bSortable ){
			ts = (TableSorter)getModel();
			ts.addMouseListenerToHeaderInTable(this);
		}
	}
	
	/**
	 * Constructor
	 * @param model : model to use
	 */
	public JajukTable(TableModel model) {
		this(model,true);
	}
	
	
	/**
	 * add tooltips to each cell
	*/
	public String getToolTipText(MouseEvent e) {
		String tip = null;
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		int realColumnIndex = convertColumnIndexToModel(colIndex);
		TableModel model = getModel();
		String sTip = model.getValueAt(rowIndex,colIndex).toString();
		return sTip;
	}
	
	/**
	 * @return Returns the sorting model.
	 */
	public TableSorter getSortingModel() {
		return ts;
	}

}


