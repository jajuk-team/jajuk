/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA. 
 * $Revision$
 */

package org.jajuk.ui.views;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import layout.TableLayout;

import org.jajuk.util.Util;

/**
 * Abstract table view : common implementation for both physical and logical table views 
 * 
 * @author bflorat 
 * @created 13 dec. 2003
 */
public abstract class AbstractTableView extends ViewAdapter implements ActionListener{

	/*protected *Columns number*/
	protected int iColNum = 8;
	
	/**Rows number*/
	protected int iRowNum;
	
	/**Cell editable table**/
	protected boolean[][] bCellEditable;
	
	/**Values table**/
	protected Object[][] oValues;
	
	/**Columns names table**/
	protected String[] sColName;
	
	/** The logical table */
	JTable jtable;
	JPanel jpControl;
		JLabel jlFilter;
		JComboBox jcbProperty; 
		JLabel jlEquals;
		JTextField jtfValue;
		JToolBar jtbControl;
			JButton jbApplyFilter;
			JButton jbClearFilter;
			JButton jbAdvancedFilter;
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Logical table view";
	}
	
	
	/** Constructor */
	public AbstractTableView(){
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display() {
		//Control panel
		jpControl = new JPanel();
		jpControl.setBorder(BorderFactory.createEtchedBorder());
		int iXspace = 5;
		double sizeControl[][] =
			{{0.15,iXspace,0.4,iXspace,10,iXspace,0.4,2*iXspace,80},
			{22}};
		jpControl.setLayout(new TableLayout(sizeControl));
		jlFilter = new JLabel("Filter: ");
		jcbProperty = new JComboBox();
		jcbProperty.setToolTipText("Property to filter");
		jcbProperty.setMinimumSize(new Dimension(150,20));
		jcbProperty.setPreferredSize(new Dimension(200,20));
		jcbProperty.setMaximumSize(new Dimension(200,20));
		jlEquals = new JLabel("=");
		jtfValue = new JTextField();
		jtfValue.setToolTipText("Value to be used by the filter");
		jtfValue.setMinimumSize(new Dimension(150,20));
		jtfValue.setPreferredSize(new Dimension(200,20));
		jtfValue.setMaximumSize(new Dimension(200,20));
		//buttons
		jtbControl = new JToolBar();
		jtbControl.setRollover(true);
		jtbControl.setFloatable(false);
		jbApplyFilter = new JButton(Util.getIcon(ICON_APPLY_FILTER));
		jbClearFilter = new JButton(Util.getIcon(ICON_CLEAR_FILTER));
		jbAdvancedFilter = new JButton(Util.getIcon(ICON_ADVANCED_FILTER));
		jbApplyFilter.setToolTipText("Apply filter");
		jbClearFilter.setToolTipText("Clear the filter");
		jbAdvancedFilter.setToolTipText("Apply an advanced filter");
		jbAdvancedFilter.setEnabled(false);  //TBI
		jtbControl.add(jbApplyFilter);
		jtbControl.add(jbClearFilter);
		jtbControl.add(jbAdvancedFilter);
		
		jpControl.add(jlFilter,"0,0");
		jpControl.add(jcbProperty,"2,0");
		jpControl.add(jlEquals,"4,0");
		jpControl.add(jtfValue,"6,0");
		jpControl.add(jtbControl,"8,0");
		
		
		jtable = new JTable();
		//add 
		double size[][] =
		{{0.99},
		{30,0.99}};
		setLayout(new TableLayout(size));
		add(jpControl,"0,0");
		add(new JScrollPane(jtable),"0,1");
	}	
	
	/**Fill the tree */
	abstract public void populate();
		
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	}

	/**Set model to table*/
	void setModel(final AbstractTableView atv){
		//table
		TableModel model = new AbstractTableModel() {
			public int getColumnCount() {
				return atv.iColNum;
			}

			public int getRowCount() {
				return atv.iRowNum;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return atv.bCellEditable[columnIndex][rowIndex];
			}

			public Class getColumnClass(int columnIndex) {
				return getValueAt(0,columnIndex).getClass();
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				return atv.oValues[rowIndex][columnIndex];
			}

			public void setValueAt(Object oValue, int rowIndex, int columnIndex) {
				oValues[rowIndex][columnIndex] = oValue;
			}

			public String getColumnName(int columnIndex) {
				return atv.sColName[columnIndex];
			}
		};
		jtable.setModel(model);
	}


	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	abstract public String getViewName();
}


