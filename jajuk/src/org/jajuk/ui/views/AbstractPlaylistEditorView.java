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
 *  $Revision$
 */

package org.jajuk.ui.views;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import layout.TableLayout;

import org.jajuk.base.BasicFile;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.ui.JajukTable;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.util.Util;


/**
 * Adapter for playlists editors 
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
public abstract class AbstractPlaylistEditorView extends ViewAdapter implements Observer {

	
	JPanel jpControl;
		JToolBar jtb;
			JButton jbRun;
			JButton jbSave;
			JButton jbAdd;
			JButton jbRemove;
			JButton jbUp;
			JButton jbDown;
			JButton jbCurrent;
			JButton jbClear;
			JLabel jlTitle;
	JajukTable jtable;
	
	/**playlist editor title : playlist file or playlist name*/
	String sTitle;
	
	/**Current playlist file item*/
	private PlaylistFileItem plfi;
	
	/**Playlist file type*/
	private int iType;
	
	/**Columns number*/
	protected int iColNum = 2;
	
	/**Rows number*/
	protected int iRowNum;
	
	/**Cell editable table**/
	protected boolean[][] bCellEditable;
	
	/**Values*/
	protected ArrayList alFiles = new ArrayList(10);
	
	/**Columns names table**/
	protected String[] sColName = new String[]{"Track","Location"};

	/**Model for table*/
	class PlayListEditorTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return iColNum;
		}
		
		public int getRowCount() {
			return iRowNum;
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
	 	public Class getColumnClass(int columnIndex) {
			return getValueAt(0, columnIndex).getClass();
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			BasicFile bf = (BasicFile)alFiles.get(rowIndex);
			if ( columnIndex == 0){
				return bf.getTrack().getName();
			}
			else if ( columnIndex == 1){
				return bf.getAbsolutePath();
			}
			return null;
		}
		
		public void setValueAt(Object oValue, int rowIndex, int columnIndex) {
		}
		
		public String getColumnName(int columnIndex) {
			return sColName[columnIndex];
		}
	
	}
	
	
	
	/**
	 * Constructor
	 */
	public AbstractPlaylistEditorView() {
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		//Control panel
		jpControl = new JPanel();
		jtb = new JToolBar();
		jtb.setFloatable(false);
		jpControl.setBorder(BorderFactory.createEtchedBorder());
		int iXspace = 0;
		double sizeControl[][] =
			{{iXspace,0.50,0.50,iXspace},
			{25,0.99}};
		jpControl.setLayout(new TableLayout(sizeControl));
		jbRun = new JButton(Util.getIcon(ICON_RUN));
		jbRun.setToolTipText("Run this playlist");
		jbSave = new JButton(Util.getIcon(ICON_SAVE));
		jbSave.setToolTipText("Save this playlist");
		jbAdd = new JButton(Util.getIcon(ICON_ADD));
		jbAdd.setToolTipText("Add an item to this playlist");
		jbRemove = new JButton(Util.getIcon(ICON_REMOVE));
		jbRemove.setToolTipText("Remove an item from this playlist");
		jbUp = new JButton(Util.getIcon(ICON_UP));
		jbUp.setToolTipText("Set item position higher");
		jbDown = new JButton(Util.getIcon(ICON_DOWN));
		jbDown.setToolTipText("Set item position lower");
		jbCurrent = new JButton(Util.getIcon(ICON_CURRENT_PLAYLIST));
		jbCurrent.setToolTipText("Display current played playlist");
		jbClear = new JButton(Util.getIcon(ICON_CLEAR));
		jbClear.setToolTipText("Clear this playlist");
		jlTitle = new JLabel("");
		jtb.add(jbRun);
		jtb.add(jbSave);
		jtb.add(jbAdd);
		jtb.add(jbRemove);
		jtb.add(jbUp);
		jtb.add(jbDown);
		jtb.add(jbCurrent);
		jtb.add(jbClear);
		
		jpControl.add(jtb,"1,0");
		jpControl.add(jlTitle,"2,0");
		double size[][] =
		{{0.99},
		{30,0.99}};
		setLayout(new TableLayout(size));
		add(jpControl,"0,0");
		ObservationManager.register(EVENT_PLAYLIST_REFRESH,this);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Playlist editor view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public abstract String getViewName();
	
	/**
	 * Populate table with tracks in this playlist : track name, author and file url
	 */
	private void populate(){
		//clean data
		alFiles = new ArrayList(10);
		switch(iType){
			case 0:  //regular playlist
				alFiles = plfi.getPlaylistFile().getBasicFiles();
				break;
			case 1:  //new playlist
				break;
			case 2:  //bookmarks
				ArrayList alBookmarks = Bookmarks.getInstance().getFiles(); 
				Iterator it = alBookmarks.iterator();
				while (it.hasNext()){
					alFiles.add(new BasicFile((File)it.next()));
				}
				break;
			case 3:  //bestof
				ArrayList alBestof = FileManager.getBestOfFiles(); 
				it = alBestof.iterator();
				while (it.hasNext()){
					alFiles.add(new BasicFile((File)it.next()));
				}
				break;
			case 4:  //queue
				//add currently played file
				File file = FIFO.getInstance().getCurrentFile();
				if ( file != null){
					alFiles.add(new BasicFile(file));  
				}
				//next files
				ArrayList alQueue = FIFO.getInstance().getFIFO(); 
				it = alQueue.iterator();
				while (it.hasNext()){
					alFiles.add(new BasicFile((File)it.next()));
				}
				break;
		}
		iRowNum = alFiles.size();
				
	}
	
	
	
	
		/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
		if ( EVENT_PLAYLIST_REFRESH.equals(subject)){
			if ( plfi != null){
				if ( jtable != null){
					remove(jtable);
				}
				jlTitle.setText(plfi.getName());
				jlTitle.setToolTipText(plfi.getName());
				populate();
				//table
				PlayListEditorTableModel model = new PlayListEditorTableModel();
				jtable = new JajukTable(model);
				//set colunm size
				int iTrackColWidth = jtable.getColumnModel().getColumn(0).getPreferredWidth();
				int iLocationColWidth = jtable.getColumnModel().getColumn(0).getPreferredWidth();
				jtable.getColumnModel().getColumn(0).setPreferredWidth((int)((iTrackColWidth+iLocationColWidth)*0.2)); // track name
				jtable.getColumnModel().getColumn(1).setPreferredWidth((int)((iTrackColWidth+iLocationColWidth)*0.8)); //location
				add(new JScrollPane(jtable),"0,1");
				SwingUtilities.updateComponentTreeUI(jtable);
				//set buttons
				if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
					jbAdd.setEnabled(false);
					jbClear.setEnabled(false);
					jbDown.setEnabled(false);
					jbRemove.setEnabled(false);
					jbRun.setEnabled(false);
					jbUp.setEnabled(false);
				}
				else{
					jbAdd.setEnabled(true);
					jbClear.setEnabled(true);
					jbDown.setEnabled(true);
					jbRemove.setEnabled(true);
					jbRun.setEnabled(true);
					jbUp.setEnabled(true);
				}
			}
		}
	}
	
	
	
	/**
	 * Set playlist displayed in the editor ( can be regular or special playlist files
	 * @param plf : playlist to display
	 * @param iType: type for playlist
	 */
	public void setCurrentPlayListFile(PlaylistFileItem plfi){
		this.iType = plfi.getType();
		this.plfi = plfi;
		ObservationManager.notify(EVENT_PLAYLIST_REFRESH);
	}
	
	
}
