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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Observer;
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.FilesTableModel;
import org.jajuk.ui.JajukTableModel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Logical table view
 * 
 * @author Bertrand Florat 
 * @created 13 dec. 2003
 */
public class PhysicalTableView extends AbstractTableView implements Observer, MouseListener{
	
	/** Self instance */
	private static PhysicalTableView ltv;
	
	JPopupMenu jmenuFile;
	JMenuItem jmiFilePlay;
	JMenuItem jmiFilePush;
	JMenuItem jmiFilePlayShuffle;
	JMenuItem jmiFilePlayRepeat;
	JMenuItem jmiFilePlayDirectory;
	JMenuItem jmiFileProperties;
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "PhysicalTableView.0"; //$NON-NLS-1$
	}
	
	
	/** Return singleton */
	public static synchronized PhysicalTableView getInstance() {
		if (ltv == null) {
			ltv = new PhysicalTableView();
		}
		return ltv;
	}
	
	/** Constructor */
	public PhysicalTableView(){
		super();
		ltv = this;
		//File menu
		jmenuFile = new JPopupMenu();
		jmiFilePlay = new JMenuItem(Messages.getString("PhysicalTableView.1")); //$NON-NLS-1$
		jmiFilePlay.addActionListener(this);
		jmiFilePush = new JMenuItem(Messages.getString("PhysicalTableView.2")); //$NON-NLS-1$
		jmiFilePush.addActionListener(this);
		jmiFilePlayShuffle = new JMenuItem(Messages.getString("PhysicalTableView.3")); //$NON-NLS-1$
		jmiFilePlayShuffle.addActionListener(this);
		jmiFilePlayRepeat = new JMenuItem(Messages.getString("PhysicalTableView.4")); //$NON-NLS-1$
		jmiFilePlayRepeat.addActionListener(this);
		jmiFilePlayDirectory = new JMenuItem(Messages.getString("PhysicalTableView.15")); //$NON-NLS-1$
		jmiFilePlayDirectory.addActionListener(this);
		jmiFileProperties = new JMenuItem(Messages.getString("PhysicalTableView.6")); //$NON-NLS-1$
		jmiFileProperties.setEnabled(false);
		jmiFileProperties.addActionListener(this);
		jmenuFile.add(jmiFilePlay);
		jmenuFile.add(jmiFilePush);
		jmenuFile.add(jmiFilePlayShuffle);
		jmenuFile.add(jmiFilePlayRepeat);
		jmenuFile.add(jmiFilePlayDirectory);
		jmenuFile.add(jmiFileProperties);
	}
	
	
	/**populate the table */
	public JajukTableModel populateTable(){
	    //model creation
        return new FilesTableModel();
   }
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getID()
	 */
	public String getID() {
	    return "org.jajuk.ui.views.PhysicalTableView"; //$NON-NLS-1$
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if ( e.getClickCount() == 2){ //double clic, can be only one file
            int iSelectedRow = jtable.getSelectedRow(); //selected row in view
            File file = (File)FileManager.getInstance().getItem(jtable.getModelValueAt(iSelectedRow,0).toString());
			if (!file.isScanned()){
				try{
				    FIFO.getInstance().push(new StackItem(file,ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),
                        ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));//launch it
				}
				catch(JajukException je){
				    Log.error(je);
				}
			}
			else{
				Messages.showErrorMessage("120",file.getDirectory().getDevice().getName()); //$NON-NLS-1$
			}
		}		
		else if ( e.getClickCount() == 1 
                && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
		    // if none or 1 node is selected, a right click on another node select it
            //if more than 1, we keep selection and display a popup for them
            if (jtable.getSelectedRowCount() < 2){
                int iSelection = jtable.rowAtPoint(e.getPoint());
                jtable.getSelectionModel().setSelectionInterval(iSelection,iSelection);
            }
            if ( jtable.getSelectedRowCount() > 1){
				jmiFileProperties.setEnabled(false); //can read a property from one sole file
			}
			else{
				jmiFileProperties.setEnabled(false); //TBI set to true when managing properties
			}
			jmenuFile.show(jtable,e.getX(),e.getY());
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		new Thread(){
			public void run(){
				//let super class to test common ( physical/logical ) events 
				if ( e.getSource() == jbApplyFilter || e.getSource() == jbClearFilter){
					PhysicalTableView.super.actionPerformed(e);
					return;
				}
				//then specifics
				//computes selected files
				ArrayList alFilesToPlay = new ArrayList(jtable.getSelectedRowCount());
				int[] indexes = jtable.getSelectedRows();
				for (int i=0;i<indexes.length;i++){ //each selected track
                    File file = (File)FileManager.getInstance().getItem(jtable.getModelValueAt(indexes[i],0).toString());
					ArrayList alFilesToPlay2 = new ArrayList(indexes.length);
					if (e.getSource() == jmiFilePlayDirectory){
					    alFilesToPlay2.addAll(FileManager.getInstance().getAllDirectory(file));   
					}
					else{
					    alFilesToPlay2.add(file);    
					}
					Iterator it = alFilesToPlay2.iterator();
					while (it.hasNext()){ //each selected file from the same directory 
					    File file2 = (File)it.next();
					    if (!file2.isScanned() && !alFilesToPlay.contains(file2)){
							 alFilesToPlay.add(file2);
						}
						else{
							Messages.showErrorMessage("120",file2.getDirectory().getDevice().getName()); //$NON-NLS-1$
							return;  //stop here to avoid cascading error messages 
						}
					}    
				}
				//simple play
				if ( e.getSource() == jmiFilePlay || e.getSource() == jmiFilePlayDirectory){
						FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
				}
				//push
				else if ( e.getSource() == jmiFilePush){
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),true);
				}
				//shuffle play
				else if ( e.getSource() == jmiFilePlayShuffle){
				    Collections.shuffle(alFilesToPlay);
					FIFO.getInstance().push(Util.createStackItems(alFilesToPlay,
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
				}
				//repeat play
				else if ( e.getSource() == jmiFilePlayRepeat){
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
							true,true),false);
				}
			}
		}.start();
	}
	
    	
}
