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
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.TracksTableModel;
import org.jajuk.util.Util;

import com.sun.TableMap;

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
		return Messages.getString("PhysicalTableView.0"); //$NON-NLS-1$
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
	public void display(){
		populate();
		super.display();
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
	}	
	
	/**populate the table */
	public void populate(){
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
		Object[][] oValues = new Object[iSize][iColNum];
		//Track | Album | Author |  Length | Style | Device | File name | Rate
		for (int i = 0;it.hasNext();i++){
			File file = (File)it.next(); 
			oValues[i][0] = file.getTrack().getName();
			oValues[i][1] = file.getTrack().getAlbum().getName2();
			oValues[i][2] = file.getTrack().getAuthor().getName2();
			oValues[i][3] = new Long(file.getTrack().getLength());
			oValues[i][4] = file.getTrack().getStyle().getName2();
			oValues[i][5] = file.getDirectory().getDevice().getName();
			oValues[i][6] = file.getName();
			oValues[i][7] = new Long(file.getTrack().getRate());
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
		model.setValues(oValues,alToShow);
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
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
			TracksTableModel ttm = (TracksTableModel)(((TableMap)jtable.getModel()).getModel());
			ArrayList al = ttm.getValues();
			File file = (File)al.get(jtable.getSelectedRow());
			FIFO.getInstance().push(file,false);//launch it
		}		
		else if ( jtable.getSelectedRowCount() > 0 && e.getClickCount() == 1 && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
			if ( jtable.getSelectedRowCount() > 1){
				jmiFileProperties.setEnabled(false); //can read a property from one sole file
			}
			else{
				jmiFileProperties.setEnabled(true);
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
	public void actionPerformed(ActionEvent e) {
		//let super class to test common ( physical/logical ) events 
		super.actionPerformed(e);
		//then specifics
		//computes selected files
		ArrayList alFilesToPlay = new ArrayList(10);
		int[] indexes = jtable.getSelectedRows();
		TracksTableModel ttm = (TracksTableModel)(((TableMap)jtable.getModel()).getModel());
		ArrayList al = ttm.getValues();
		for (int i=0;i<indexes.length;i++){
			alFilesToPlay.add((File)al.get(indexes[i]));
		}
		//simple play
		if ( e.getSource() == jmiFilePlay){
			FIFO.getInstance().push(alFilesToPlay,false);
		}
		//push
		else if ( e.getSource() == jmiFilePush){
			FIFO.getInstance().push(alFilesToPlay,true);
		}
		//shuffle play
		else if ( e.getSource() == jmiFilePlayShuffle){
			FIFO.getInstance().push(Util.randomize(alFilesToPlay),false);
		}
		//repeat play
		else if ( e.getSource() == jmiFilePlayRepeat){
			FIFO.getInstance().push(alFilesToPlay,false,false,true);
		}
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
		int iSize = alFiles.size();
		int iColNum = 8;
		it = alToShow.iterator();
		Object[][] oValues = new Object[iSize][iColNum];
		//Track | Album | Author |  Length | Style | Device | File name | Rate
		int i=0;
		while (it.hasNext()){
			File file = (File)it.next();
			if ( file.shouldBeHidden()){
				continue;
			}
			if ( sPropertyName != null && sPropertyValue != null ){ //if name or value is null, means there is no filter
				String sValue = file.getProperty(sPropertyName);
				if ( sValue == null){ //try to filter on a unknown property, don't take this file
					continue;
				}
				if ( sValue.toLowerCase().indexOf(sPropertyValue.toLowerCase()) == -1){  // test if the file porperty contains this property value ( ignore case )
					it.remove(); //no? remove it
					continue;
				}
			}
			//else, populate this values
			oValues[i][0] = file.getTrack().getName();
			oValues[i][1] = file.getTrack().getAlbum().getName2();
			oValues[i][2] = file.getTrack().getAuthor().getName2();
			oValues[i][3] = new Long(file.getTrack().getLength());
			oValues[i][4] = file.getTrack().getStyle().getName2();
			oValues[i][5] = file.getDirectory().getDevice().getName();
			oValues[i][6] = file.getName();
			oValues[i][7] = new Long(file.getTrack().getRate());
			i++;
		}
		model.setValues(oValues,alToShow);
		model.fireTableDataChanged();
	}
	
	
}


