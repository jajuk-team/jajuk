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
 * $Release$
 */

package org.jajuk.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.sun.TableSorter;

/**
 *  JTable with followinf features: 
 * <p>Sortable
 * <p>Tooltips on each cell
 *
 * @author     bflorat
 * @created    21 févr. 2004
 */
public class JajukTable extends JTable {

	/**
	 * Constructor
	 * @param model : model to use
	 */
	public JajukTable(TableModel model) {
		super(new TableSorter(model));
		TableSorter ts = (TableSorter)getModel();
		ts.addMouseListenerToHeaderInTable(this);
	}
	
	/**
	 * add tooltips to each cell
	*/
	public Component prepareRenderer(TableCellRenderer renderer,
			int rowIndex, int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if (c instanceof JComponent) {
			JComponent jc = (JComponent)c;
			Object o = getValueAt(rowIndex, vColIndex);
			String s ="";
			if ( !(o instanceof String) ){
				if ( o instanceof Long){
					s = o.toString();
				}
			}
			else{
				s = (String)o;
			}
			jc.setToolTipText(s);
		}
		return c;
	}
	
}


