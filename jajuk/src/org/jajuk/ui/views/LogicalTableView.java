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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import layout.TableLayout;

/**
 * Logical table view
 * 
 * @author bflorat 
 * @created 13 dec. 2003
 */
public class LogicalTableView extends ViewAdapter implements ActionListener{

	/** Self instance */
	private static LogicalTableView ltv;

	
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

	/** Return singleton */
	public static LogicalTableView getInstance() {
		if (ltv == null) {
			ltv = new LogicalTableView();
		}
		return ltv;
	}

	/** Constructor */
	public LogicalTableView(){
		//Control panel
		jpControl = new JPanel();
		jpControl.setBorder(BorderFactory.createEtchedBorder());
		int iXspace = 5;
		double sizeControl[][] =
			{{0.1,iXspace,0.3,iXspace,10,iXspace,0.3,3*iXspace,100},
			{25}};
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
		jbApplyFilter = new JButton(new ImageIcon(ICON_APPLY_FILTER));
		jbApplyFilter.setToolTipText("Apply filter");
		jbClearFilter = new JButton(new ImageIcon(ICON_CLEAR_FILTER));
		jbClearFilter.setToolTipText("Clear the filter");
		jbAdvancedFilter = new JButton(new ImageIcon(ICON_ADVANCED_FILTER));
		jbAdvancedFilter.setToolTipText("Apply an advanced filter");
		jtbControl.add(jbApplyFilter);
		jtbControl.add(jbClearFilter);
		jtbControl.add(jbAdvancedFilter);
		
		jpControl.add(jlFilter,"0,0");
		jpControl.add(jcbProperty,"2,0");
		jpControl.add(jlEquals,"4,0");
		jpControl.add(jtfValue,"6,0");
		jpControl.add(jtbControl,"8,0");
		
		//table
		TableModel model = new TableModel() {
			public int getColumnCount() {
				return 2;
			}

			public int getRowCount() {
				return 10;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}

			public Class getColumnClass(int columnIndex) {
				return String.class;
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				return "a";
			}

			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			}

			public String getColumnName(int columnIndex) {
				return "a";
			}

			public void addTableModelListener(TableModelListener l) {
			}

			public void removeTableModelListener(TableModelListener l) {
			}
		};
		jtable = new JTable(model);
		
		jtable.setSize(100,100);
		//add 
		int iYspace = 5;
		double size[][] =
					{{0.99},
					{30,300}};
		setLayout(new TableLayout(size));
		
		add(jpControl,"0,0");
		add(new JScrollPane(jtable),"0,1");
	}
	
	/**Fill the tree */
	public void populate(){
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	}
}


