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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.PatternSyntaxException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.TracksTableModel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;

/**
 * Logical table view
 * 
 * @author bflorat 
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
	JMenuItem jmiFileSetProperty;
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
		jmiFileSetProperty = new JMenuItem(Messages.getString("PhysicalTableView.5")); //$NON-NLS-1$
		jmiFileSetProperty.setEnabled(false);
		jmiFileSetProperty.addActionListener(this);
		jmiFileProperties = new JMenuItem(Messages.getString("PhysicalTableView.6")); //$NON-NLS-1$
		jmiFileProperties.setEnabled(false);
		jmiFileProperties.addActionListener(this);
		jmenuFile.add(jmiFilePlay);
		jmenuFile.add(jmiFilePush);
		jmenuFile.add(jmiFilePlayShuffle);
		jmenuFile.add(jmiFilePlayRepeat);
		jmenuFile.add(jmiFileSetProperty);
		jmenuFile.add(jmiFileProperties);
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void populate(){
	    populateTable();
		super.populate();
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
	}	
	
	/**populate the table */
	public void populateTable(){
		//Columns names
		String[] sColName = new String[]{Messages.getString("PhysicalTableView.7"),Messages.getString("PhysicalTableView.8"),Messages.getString("PhysicalTableView.9"),Messages.getString("PhysicalTableView.10"),Messages.getString("PhysicalTableView.11"),Messages.getString("PhysicalTableView.12"),Messages.getString("PhysicalTableView.13"),Messages.getString("PhysicalTableView.14")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		//Values
		ArrayList alFiles = FileManager.getSortedFiles();
		ArrayList alToShow = new ArrayList(alFiles.size());
		Iterator it = alFiles.iterator();
		while ( it.hasNext()){
			File file = (File)it.next(); 
			if ( !file.shouldBeHidden()){
				alToShow.add(file);
			}
		}
		int iSize = alToShow.size();
		int iColNum = 8;
		it = alToShow.iterator();
		Object[][] oValues = new Object[iSize][iColNum+1];
		//Track | Album | Author |  Length | Style | Device | File name | Rate
		for (int i = 0;it.hasNext();i++){
			File file = (File)it.next(); 
			oValues[i][0] = file.getTrack().getName();
			oValues[i][1] = file.getTrack().getAlbum().getName2();
			oValues[i][2] = file.getTrack().getAuthor().getName2();
			oValues[i][3] = Util.formatTimeBySec(file.getTrack().getLength(),true);
			oValues[i][4] = file.getTrack().getStyle().getName2();
			oValues[i][5] = file.getDirectory().getDevice().getName();
			oValues[i][6] = file.getName();
			oValues[i][7] = new Long(file.getTrack().getRate());
			oValues[i][8] = file.getId();
		}
		//edtiable table  and class 
		boolean[][] bCellEditable = new boolean[iColNum][iSize];
		for (int i =0;i<iColNum;i++){
			for (int j=0;j<iSize;j++){
				bCellEditable[i][j]=false;
			}
		}
		//model creation
		model = new TracksTableModel(iColNum,bCellEditable,sColName);
		model.setValues(oValues);
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
			File file = FileManager.getFile(jtable.getSortingModel().getValueAt(jtable.getSelectedRow(),jtable.getColumnCount()).toString());
			if (!file.isScanned()){
				FIFO.getInstance().push(file,false);//launch it
			}
			else{
				Messages.showErrorMessage("120",file.getDirectory().getDevice().getName()); //$NON-NLS-1$
			}
		}		
		else if ( jtable.getSelectedRowCount() > 0 && e.getClickCount() == 1 && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
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
				ArrayList alFilesToPlay = new ArrayList(10);
				int[] indexes = jtable.getSelectedRows();
				for (int i=0;i<indexes.length;i++){
					File file = FileManager.getFile(jtable.getSortingModel().getValueAt(indexes[i],jtable.getColumnCount()).toString());
					if (!file.isScanned()){
						alFilesToPlay.add(file);
					}
					else{
						Messages.showErrorMessage("120",file.getDirectory().getDevice().getName()); //$NON-NLS-1$
						return;  //stop here to avoid error messages 
					}
				}
				//simple play
				if ( e.getSource() == jmiFilePlay){
					FIFO.getInstance().push(Util.applyPlayOption(alFilesToPlay),false);
				}
				//push
				else if ( e.getSource() == jmiFilePush){
					FIFO.getInstance().push(Util.applyPlayOption(alFilesToPlay),true);
				}
				//shuffle play
				else if ( e.getSource() == jmiFilePlayShuffle){
				    Collections.shuffle(alFilesToPlay);
					FIFO.getInstance().push(alFilesToPlay,false);
				}
				//repeat play
				else if ( e.getSource() == jmiFilePlayRepeat){
					FIFO.getInstance().push(Util.applyPlayOption(alFilesToPlay),false,false,true);
				}
			}
		}.start();
	}
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.AbstractTableView#applyFilter()
	 */
	public void applyFilter(String sPropertyName,String sPropertyValue) {
		//Values
		ArrayList alFiles = FileManager.getSortedFiles();
		ArrayList alToShow = new ArrayList(alFiles.size());
		Iterator it = alFiles.iterator();
		while ( it.hasNext()){
			File file = (File)it.next(); 
			if ( !file.shouldBeHidden()){
				alToShow.add(file);
			}
		}
		int iColNum = 8;
		it = alToShow.iterator();
		//Track | Album | Author |  Length | Style | Device | File name | Rate
		if ( !ConfigurationManager.getBoolean(CONF_REGEXP) && sPropertyValue != null){ //do we use regular expression or not? if not, we allow user to use '*'
		    sPropertyValue = sPropertyValue.replaceAll("\\*",".*"); //$NON-NLS-1$ //$NON-NLS-2$
		    sPropertyValue = ".*"+sPropertyValue+".*"; //$NON-NLS-1$ //$NON-NLS-2$
		}	
	
		while (it.hasNext()){
			File file = (File)it.next();
			if ( sPropertyName != null && sPropertyValue != null ){ //if name or value is null, means there is no filter
				String sValue = file.getProperty(sPropertyName);
				if ( sValue == null){ //try to filter on a unknown property, don't take this file
					continue;
				}
				else { 
				    boolean bMatch = false;
				    try{  //test using regular expressions
							bMatch = sValue.toLowerCase().matches(sPropertyValue.toLowerCase());  // test if the file property contains this property value (ignore case)
					}
					catch(PatternSyntaxException pse){ //wrong pattern syntax
						bMatch = false;
					}
					if (!bMatch){
						it.remove(); //no? remove it
					}
				}	
			}
		}
		//populate this values
		Object[][] oValues = new Object[alToShow.size()][iColNum+1];
		for (int i=0;i<alToShow.size();i++){
			File file = (File)alToShow.get(i);
			oValues[i][0] = file.getTrack().getName();
			oValues[i][1] = file.getTrack().getAlbum().getName2();
			oValues[i][2] = file.getTrack().getAuthor().getName2();
			oValues[i][3] = Util.formatTimeBySec(file.getTrack().getLength(),true);
			oValues[i][4] = file.getTrack().getStyle().getName2();
			oValues[i][5] = file.getDirectory().getDevice().getName();
			oValues[i][6] = file.getName();
			oValues[i][7] = new Long(file.getTrack().getRate());
			oValues[i][8] = file.getId();
		}
		model.setValues(oValues);
		model.fireTableDataChanged();
	}
	
	
}


