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
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
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
	
	/**Contains files displayed in the model*/ 
	 ArrayList alFiles  = new ArrayList(1000);
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Physical table view";
	}
	

	/** Return singleton */
	public static PhysicalTableView getInstance() {
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
		jmiFilePlay = new JMenuItem("Play");
		jmiFilePlay.addActionListener(this);
		jmiFilePush = new JMenuItem("Push");
		jmiFilePush.addActionListener(this);
		jmiFilePlayShuffle = new JMenuItem("Play shuffle");
		jmiFilePlayShuffle.addActionListener(this);
		jmiFilePlayRepeat = new JMenuItem("Play repeat");
		jmiFilePlayRepeat.addActionListener(this);
		jmiFileSetProperty = new JMenuItem("Set a property");
		jmiFileSetProperty.setEnabled(false);
		jmiFileSetProperty.addActionListener(this);
		jmiFileProperties = new JMenuItem("Properties");
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
		super.display();
		populate();
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
		update(EVENT_DEVICE_REFRESH);  //force the first refresh
	}	
	
	/**Fill the tree */
	public void populate(){
		//Columns names
		sColName = new String[]{"Track","Album","Author","Length","Style","Directory","File","Rate"};
		//Values
		alFiles = FileManager.getSortedFiles();
		int iSize = alFiles.size();
		Iterator it = alFiles.iterator();
		oValues = new Object[iSize][iColNum];
		//Track | Album | Author |  Length | Style | Directory | File name | Rate
		for (int i = 0;it.hasNext();i++){
			File file = (File)it.next(); 
			oValues[i][0] = file.getTrack().getName();
			oValues[i][1] = file.getTrack().getAlbum().getName2();
			oValues[i][2] = file.getTrack().getAuthor().getName2();
			oValues[i][3] = new Long(file.getTrack().getLength());
			oValues[i][4] = file.getTrack().getStyle().getName2();
			oValues[i][5] = file.getDirectory().getName();
			oValues[i][6] = file.getName();
			oValues[i][7] = new Long(file.getTrack().getRate());
		}
		//row num
		iRowNum = iSize;
		//edtiable table  and class 
		bCellEditable = new boolean[8][iSize];
		for (int i =0;i<8;i++){
			for (int j=0;j<iSize;j++){
				bCellEditable[i][j]=false;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.PhysicalTableView";
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
			File file = (File)alFiles.get(jtable.getSelectedRow());
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
		for (int i=0;i<indexes.length;i++){
			alFilesToPlay.add((File)alFiles.get(indexes[i]));
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
	
	
}


