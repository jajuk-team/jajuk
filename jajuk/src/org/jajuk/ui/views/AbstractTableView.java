/*
 * Jajuk Copyright (C) 2003 Bertrand Florat
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

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jajuk.base.Event;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukTable;
import org.jajuk.ui.TableTransferHandler;
import org.jajuk.ui.TracksTableModel;
import org.jajuk.util.Util;

/**
 * Abstract table view : common implementation for both physical and logical table views 
 * 
 * @author Bertrand Florat 
 * @created 13 dec. 2003
 */
public abstract class AbstractTableView extends ViewAdapter implements ActionListener,MouseListener{
	
	/** The logical table */
	JajukTable jtable;
	JPanel jpControl;
	JLabel jlFilter;
	JComboBox jcbProperty; 
	JLabel jlEquals;
	JTextField jtfValue;
	JButton jbApplyFilter;
	JButton jbClearFilter;
	JButton jbAdvancedFilter;
	
	/**Table model*/
	TracksTableModel model;
	
	/** Currently applied filter*/
	String sAppliedFilter = ""; //$NON-NLS-1$
	
	/** Currently applied criteria*/
	String sAppliedCriteria;
	
	
	/** Constructor */
	public AbstractTableView(){
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void populate() {
		//Control panel
		jpControl = new JPanel();
		jpControl.setBorder(BorderFactory.createEtchedBorder());
		jlFilter = new JLabel(Messages.getString("AbstractTableView.0")); //$NON-NLS-1$
		//properties combo box, fill with colums names
		jcbProperty = new JComboBox();
		for (int i=0;i<model.getColumnCount();i++){
			jcbProperty.addItem(model.getColumnName(i));	
		}
		jcbProperty.setToolTipText(Messages.getString("AbstractTableView.1")); //$NON-NLS-1$
		jlEquals = new JLabel(Messages.getString("AbstractTableView.7")); //$NON-NLS-1$
		jtfValue = new JTextField();
		jtfValue.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyChar()==KeyEvent.VK_ENTER){ //user typed enter
					sAppliedFilter = jtfValue.getText();
					sAppliedCriteria = jcbProperty.getSelectedItem().toString();
					applyFilter(sAppliedCriteria,sAppliedFilter);
				}
			}
		});
		jtfValue.setToolTipText(Messages.getString("AbstractTableView.3")); //$NON-NLS-1$
		//buttons
		jbApplyFilter = new JButton(Util.getIcon(ICON_APPLY_FILTER));
		jbApplyFilter.addActionListener(this);
		jbClearFilter = new JButton(Util.getIcon(ICON_CLEAR_FILTER));
		jbClearFilter.addActionListener(this);
		jbAdvancedFilter = new JButton(Util.getIcon(ICON_ADVANCED_FILTER));
		jbAdvancedFilter.addActionListener(this);
		jbApplyFilter.setToolTipText(Messages.getString("AbstractTableView.4")); //$NON-NLS-1$
		jbClearFilter.setToolTipText(Messages.getString("AbstractTableView.5")); //$NON-NLS-1$
		jbAdvancedFilter.setToolTipText(Messages.getString("AbstractTableView.6")); //$NON-NLS-1$
		jbAdvancedFilter.setEnabled(false);  //TBI
		
		int iXspace = 5;
		double sizeControl[][] =
			{{iXspace,TableLayout.FILL,iXspace,0.3,TableLayout.FILL,TableLayout.FILL,iXspace,0.3,iXspace,20,iXspace,20,iXspace,20,iXspace},
				{22}};
		jpControl.setLayout(new TableLayout(sizeControl));
	
		jpControl.add(jlFilter,"1,0"); //$NON-NLS-1$
		jpControl.add(jcbProperty,"3,0"); //$NON-NLS-1$
		jpControl.add(jlEquals,"5,0"); //$NON-NLS-1$
		jpControl.add(jtfValue,"7,0"); //$NON-NLS-1$
		jpControl.add(jbApplyFilter,"9,0"); //$NON-NLS-1$
		jpControl.add(jbClearFilter,"11,0"); //$NON-NLS-1$
		jpControl.add(jbAdvancedFilter,"13,0"); //$NON-NLS-1$
		jpControl.setMinimumSize(new Dimension(0,0)); //allow resing with info node
		
	
		
		//add 
		double size[][] =
			{{0.99},
				{30,0.99}};
		setLayout(new TableLayout(size));
		add(jpControl,"0,0"); //$NON-NLS-1$
		jtable = new JajukTable(model);
		add(new JScrollPane(jtable),"0,1"); //$NON-NLS-1$
		new TableTransferHandler(jtable, DnDConstants.ACTION_COPY_OR_MOVE);
		jtable.addMouseListener(this);
	}	
	
	/**Fill the tree */
	abstract public void populateTable();
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		//not in a thread because it is always called inside a thread created from sub-classes
		if ( e.getSource() == jbApplyFilter){
			this.sAppliedFilter = jtfValue.getText();
			this.sAppliedCriteria = jcbProperty.getSelectedItem().toString();
			applyFilter(sAppliedCriteria,sAppliedFilter);
		}
		else if(e.getSource() == jbClearFilter){ //remove all filters
			jtfValue.setText(""); //clear value textfield //$NON-NLS-1$
			this.sAppliedFilter = null;
			this.sAppliedCriteria = null;
			applyFilter(sAppliedCriteria,sAppliedFilter);
		}
	}
	
	
	/**
	 * Apply a filter, to be implemented by physical and logical tables, alter the model
	 */
	abstract public void applyFilter(String sPropertyName,String sPropertyValue) ;
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		String subject = event.getSubject();
		if ( EVENT_DEVICE_MOUNT.equals(subject) || EVENT_DEVICE_UNMOUNT.equals(subject) 
		        || EVENT_DEVICE_REFRESH.equals(subject)  || EVENT_SYNC_TREE_TABLE.equals(subject)) {
			applyFilter(sAppliedCriteria,sAppliedFilter); //force filter to refresh
		}	
	}
}


