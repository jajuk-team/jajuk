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

import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;

/**
 * Logical table view
 * 
 * @author bflorat 
 * @created 13 dec. 2003
 */
public class PhysicalTableView extends AbstractTableView{

	/** Self instance */
	private static PhysicalTableView ltv;

		
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
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		super.display();
		populate();
		setModel(this);
	}	
	
	/**Fill the tree */
	public void populate(){
		//Columns names
		sColName = new String[]{"Track","Album","Author","Length","Style","Directory","File","Rate"};
		//Values
		ArrayList alFiles = FileManager.getSortedFiles();
		int iSize = alFiles.size();
		Iterator it = alFiles.iterator();
		oValues = new Object[iSize][iColNum];
		//Track | Album | Author |  Length | Style | Directory | File name | Rate
		for (int i = 0;it.hasNext();i++){
			File file = (File)it.next(); 
			oValues[i][0] = file.getTrack().getName();
			oValues[i][1] = file.getTrack().getAlbum().getName2();
			oValues[i][2] = file.getTrack().getAuthor().getName2();
			oValues[i][3] = Long.toString(file.getTrack().getLength());
			oValues[i][4] = file.getTrack().getStyle().getName2();
			oValues[i][5] = file.getDirectory().getName();
			oValues[i][6] = file.getName();
			oValues[i][7] = Long.toString(file.getTrack().getRate());
			
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
}


