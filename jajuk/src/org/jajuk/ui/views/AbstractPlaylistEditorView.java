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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;

import layout.TableLayout;

import org.jajuk.base.BasicFile;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukTable;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;


/**
 * Adapter for playlists editors 
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
public abstract class AbstractPlaylistEditorView extends ViewAdapter implements Observer,MouseListener {
	
	
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
	private ArrayList alFiles = new ArrayList(10);
	
	/**Previous selection ( used to check if a refresh is needed */
	private ArrayList alPrevious;
	
	/**Columns names table**/
	protected String[] sColName = new String[]{Messages.getString("AbstractPlaylistEditorView.0"),Messages.getString("AbstractPlaylistEditorView.1")}; //$NON-NLS-1$ //$NON-NLS-2$
	
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
			Object o =getValueAt(0, columnIndex);
			if ( o == null){
				return String.class;
			}
			return o.getClass();
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			if ( alFiles.size() == 0){
				return null;
			}
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
	PlayListEditorTableModel model = new PlayListEditorTableModel();
	
	
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
		jbRun.setToolTipText(Messages.getString("AbstractPlaylistEditorView.2")); //$NON-NLS-1$
		jbSave = new JButton(Util.getIcon(ICON_SAVE));
		jbSave.setToolTipText(Messages.getString("AbstractPlaylistEditorView.3")); //$NON-NLS-1$
		jbAdd = new JButton(Util.getIcon(ICON_ADD));
		jbAdd.setToolTipText(Messages.getString("AbstractPlaylistEditorView.4")); //$NON-NLS-1$
		jbRemove = new JButton(Util.getIcon(ICON_REMOVE));
		jbRemove.setToolTipText(Messages.getString("AbstractPlaylistEditorView.5")); //$NON-NLS-1$
		jbUp = new JButton(Util.getIcon(ICON_UP));
		jbUp.setToolTipText(Messages.getString("AbstractPlaylistEditorView.6")); //$NON-NLS-1$
		jbDown = new JButton(Util.getIcon(ICON_DOWN));
		jbDown.setToolTipText(Messages.getString("AbstractPlaylistEditorView.7")); //$NON-NLS-1$
		jbCurrent = new JButton(Util.getIcon(ICON_CURRENT_PLAYLIST));
		jbCurrent.setToolTipText(Messages.getString("AbstractPlaylistEditorView.8")); //$NON-NLS-1$
		jbClear = new JButton(Util.getIcon(ICON_CLEAR));
		jbClear.setToolTipText(Messages.getString("AbstractPlaylistEditorView.9")); //$NON-NLS-1$
		jlTitle = new JLabel(""); //$NON-NLS-1$
		jtb.add(jbRun);
		jtb.add(jbSave);
		jtb.add(jbAdd);
		jtb.add(jbRemove);
		jtb.add(jbUp);
		jtb.add(jbDown);
		jtb.add(jbCurrent);
		jtb.add(jbClear);
		
		jpControl.add(jtb,"1,0"); //$NON-NLS-1$
		jpControl.add(jlTitle,"2,0"); //$NON-NLS-1$
		
		jtable = new JajukTable(model);
		jtable.addMouseListener(this);
		double size[][] =
			{{0.99},
				{30,0.99}};
		setLayout(new TableLayout(size));
		add(jpControl,"0,0"); //$NON-NLS-1$
		add(new JScrollPane(jtable),"0,1"); //$NON-NLS-1$
		ObservationManager.register(EVENT_PLAYLIST_REFRESH,this);
		ObservationManager.register(EVENT_PLAYER_STOP,this);
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("AbstractPlaylistEditorView.15");	 //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public abstract String getViewName();
	
	/**
	 * Populate table with tracks in this playlist : track name, author and file url
	 */
	private void populate(){
		//save previous data
		alPrevious = (ArrayList)alFiles.clone();
		//clean data
		alFiles = new ArrayList(10);
		Iterator it = null;
		//regular or new playlist
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_NORMAL || iType == PlaylistFileItem.PLAYLIST_TYPE_NEW ){ 
			ArrayList al = null;
			try{
				al = plfi.getPlaylistFile().getBasicFiles();
			}
			catch(JajukException je){}; //don't show any message because this is called recursively by fifo, error messages would cascade
			if ( al != null){ //set alFiles to a void ArrayList if an error occurs when reading playlist to avoid some null pointer exc.
				alFiles = new ArrayList(al);  //duplicate arraylist to work separetly and detect changes
			}
		}
		//bookmarks
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){  
			ArrayList alBookmarks = Bookmarks.getInstance().getFiles(); 
			it = alBookmarks.iterator();
			while (it.hasNext()){
				alFiles.add(new BasicFile((File)it.next()));
			}
		}
		//bestof
		else if( iType == PlaylistFileItem.PLAYLIST_TYPE_BESTOF){  
			if ( FileManager.hasRateChanged()){
				ArrayList alBestof = FileManager.getBestOfFiles(); 
				it = alBestof.iterator();
				while (it.hasNext()){
					alFiles.add(new BasicFile((File)it.next()));
				}
				FileManager.setRateHasChanged(false);
			}
			else{
				alFiles = alPrevious;
			}
		}
		//queue
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){  
			if ( !FIFO.isStopped()){
				//add currently played file
				File file = FIFO.getInstance().getCurrentFile();
				if ( file != null){
					alFiles.add(new BasicFile(file));  
				}
				//next files
				ArrayList alQueue = (ArrayList)FIFO.getInstance().getFIFO().clone();
				it = alQueue.iterator();
				while (it.hasNext()){
					alFiles.add(new BasicFile((File)it.next()));
				}
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
		if ( EVENT_PLAYLIST_REFRESH.equals(subject)){
			if ( plfi != null){
				jlTitle.setText(plfi.getName());
				jlTitle.setToolTipText(plfi.getName());
				populate();
				//check if a refresh is really needed
				iRowNum = alFiles.size();
				boolean bNeedRefresh = false;
				if ( iRowNum != alPrevious.size()){  //if total numbers of files has changed, no doubt, need refresh
					bNeedRefresh = true;
				}
				else{  //else, check file by file a difference in the selection
					Iterator it = alPrevious.iterator();
					Iterator it2 = alFiles.iterator();
					while ( it.hasNext()){
						File file = (File)it.next();
						if ( !file.equals(it2.next())){
							bNeedRefresh = true;
							break;
						}
					}
				}
				if ( bNeedRefresh){
					model.fireTableDataChanged();
					Util.stopWaiting();
				}
				//select currently played track
				Iterator it = alFiles.iterator();
				for (int i=0;it.hasNext();i++){
					File file = (File)it.next();
					if ( file.equals(FIFO.getInstance().getCurrentFile())){
						if ( i != jtable.getSelectedRow()){  //selection change
							
						}
						if ( i > jtable.getSelectedRow()){  //check this to get sure we select right row if a playlist contains several times the same file 
							jtable.getSelectionModel().setSelectionInterval(i,i);
							break;
						}
					}
				}
				/*set colunm size, doen't work, try to find something better 
				int iTrackColWidth = jtable.getColumnModel().getColumn(0).getPreferredWidth();
				int iLocationColWidth = jtable.getColumnModel().getColumn(0).getPreferredWidth();
				jtable.getColumnModel().getColumn(0).setPreferredWidth((int)((iTrackColWidth+iLocationColWidth)*0.2)); // track name
				jtable.getColumnModel().getColumn(1).setPreferredWidth((int)((iTrackColWidth+iLocationColWidth)*0.8)); //location*/
				//set buttons
				if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
					jbAdd.setEnabled(false);
					jbClear.setEnabled(false);
					jbDown.setEnabled(false);
					jbRemove.setEnabled(false);
					jbRun.setEnabled(false);
					jbUp.setEnabled(false);
				}
				else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BESTOF){
					jbAdd.setEnabled(false);
					jbClear.setEnabled(false);
					jbDown.setEnabled(false);
					jbRemove.setEnabled(false);
					jbRun.setEnabled(true);
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
		else if ( EVENT_PLAYER_STOP.equals(subject)){
			alFiles = new ArrayList(0);
			model.fireTableDataChanged();
		}
	}
	
	
	/**
	 * Set playlist displayed in the editor ( can be regular or special playlist files
	 * @param plf : playlist to display
	 * @param iType: type for playlist
	 */
	public void setCurrentPlayListFile(PlaylistFileItem plfi){
		this.iType = plfi.getType();
		this.plfi =plfi;
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
		if ( e.getClickCount() == 2){ //double clic, lauche selected track and all after
			ArrayList alFilesToPlay = new ArrayList(alFiles.subList(jtable.getSelectedRow(),alFiles.size()));
			FIFO.getInstance().push(alFilesToPlay,false);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}
	
	
}
