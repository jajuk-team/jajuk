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

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.jajuk.Main;
import org.jajuk.base.BasicFile;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.StackItem;
import org.jajuk.base.TypeManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.IconLabel;
import org.jajuk.ui.JajukCellRender;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.ui.JajukTable;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.ui.PlaylistTransferHandler;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;


/**
 * Adapter for playlists editors 
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
public abstract class AbstractPlaylistEditorView extends ViewAdapter implements Observer,MouseListener,ActionListener,ListSelectionListener {
    
    
    JPanel jpControl;
    JButton jbRun;
    JButton jbSave;
    JButton jbAdd;
    JButton jbRemove;
    JButton jbUp;
    JButton jbDown;
    JButton jbAddShuffle;
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
    protected int iColNum = 3;
    
    /**Rows number*/
    protected int iRowNum;
    
    /**Cell editable table**/
    protected boolean[][] bCellEditable;
    
    /**Values*/
    private ArrayList alItems = new ArrayList(10);
    
    /**Values planned*/
    private ArrayList alPlanned = new ArrayList(10);
    
    /** Refresh time in ms**/
    private final int REFRESH_TIME = 1500;
    
    /**Columns names table**/
    protected String[] sColName = null;
    
    /**Selection set flag*/
    private boolean bSettingSelection = false;
    
    /**Last selected directory using add button*/
    private java.io.File fileLast;
   
    /**Refresh timer*/
    private Timer timer = new Timer(REFRESH_TIME,new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            update(EVENT_PLAYLIST_REFRESH);
        }
    });
   
    /*Cashed icons*/
    private static final ImageIcon iconNormal = Util.getIcon(ICON_TRACK_FIFO_NORM);
    private static final ImageIcon iconRepeat  = Util.getIcon(ICON_TRACK_FIFO_REPEAT);
    private static final ImageIcon iconPlanned = Util.getIcon(ICON_TRACK_FIFO_PLANNED);
    
    
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
           //check if fifo is void, so there is nothing to do
            if ( alItems.size() == 0 ){
                return null;
            }
            boolean bPlanned = false;
            Color color = null; //default background color
            if (FIFO.getInstance().getIndex() == rowIndex){ //if it is the currently played track, change color
                color = Color.ORANGE;
            }
            Font font = null;
            StackItem item = getItem(rowIndex);
            if (item == null){
                return null;
            }
            if( item.isPlanned() ){ //it is a planned file
                bPlanned = true;
                font = new Font("serif",Font.ITALIC,12); //font for planned items
            }
            File bf = (File)item.getFile();
            if ( columnIndex == 0){
                if (bPlanned){
                    return new IconLabel(iconPlanned,"",color,null,font,Messages.getString("AbstractPlaylistEditorView.20"));
                }
                else {
                    if (item.isRepeat()){
                        return new IconLabel(iconRepeat,"",color,null,font,Messages.getString("AbstractPlaylistEditorView.19")); //normal file, repeated
                    }
                    else{ 
                        return new IconLabel(iconNormal,"",color,null,font,Messages.getString("AbstractPlaylistEditorView.18")); //normal file, not repeated
                    }
                }
            }
            else if ( columnIndex == 1){
                return new IconLabel(null,bf.getTrack().getName(),color,null,font,bf.getTrack().getName());
            }
            else if ( columnIndex == 2){
                return new IconLabel(null,bf.getAbsolutePath(),color,null,font,bf.getAbsolutePath());
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
    public void populate(){
        //pre-compute column name for perfs
        if (sColName == null){
            sColName = new String[]{"",Messages.getString("AbstractPlaylistEditorView.0"),Messages.getString("AbstractPlaylistEditorView.1")}; //$NON-NLS-1$ //$NON-NLS-2$
        }
        //Control panel
        jpControl = new JPanel();
        jpControl.setBorder(BorderFactory.createEtchedBorder());
        int iXspace = 2;
        //Note : we don't use toolbar because it's buggy in Metal look and feel : icon get bigger
        double sizeControl[][] =
        {{iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,0.99,iXspace},
                {25,0.99}};
        jpControl.setLayout(new TableLayout(sizeControl));
        jbRun = new JButton(Util.getIcon(ICON_RUN));
        jbRun.setToolTipText(Messages.getString("AbstractPlaylistEditorView.2")); //$NON-NLS-1$
        jbRun.addActionListener(this);
        jbSave = new JButton(Util.getIcon(ICON_SAVE));
        jbSave.setToolTipText(Messages.getString("AbstractPlaylistEditorView.3")); //$NON-NLS-1$
        jbSave.addActionListener(this);
        jbAdd = new JButton(Util.getIcon(ICON_ADD));
        jbAdd.setToolTipText(Messages.getString("AbstractPlaylistEditorView.4")); //$NON-NLS-1$
        jbAdd.addActionListener(this);
        jbRemove = new JButton(Util.getIcon(ICON_REMOVE));
        jbRemove.setToolTipText(Messages.getString("AbstractPlaylistEditorView.5")); //$NON-NLS-1$
        jbRemove.addActionListener(this);
        jbUp = new JButton(Util.getIcon(ICON_UP));
        jbUp.setToolTipText(Messages.getString("AbstractPlaylistEditorView.6")); //$NON-NLS-1$
        jbUp.addActionListener(this);
        jbDown = new JButton(Util.getIcon(ICON_DOWN));
        jbDown.setToolTipText(Messages.getString("AbstractPlaylistEditorView.7")); //$NON-NLS-1$
        jbDown.addActionListener(this);
        jbAddShuffle = new JButton(Util.getIcon(ICON_ADD_SHUFFLE));
        jbAddShuffle.setToolTipText(Messages.getString("AbstractPlaylistEditorView.10")); //$NON-NLS-1$
        jbAddShuffle.addActionListener(this);
        jbCurrent = new JButton(Util.getIcon(ICON_CURRENT_PLAYLIST));
        jbCurrent.setToolTipText(Messages.getString("AbstractPlaylistEditorView.8")); //$NON-NLS-1$
        jbCurrent.addActionListener(this);
        jbClear = new JButton(Util.getIcon(ICON_CLEAR));
        jbClear.setToolTipText(Messages.getString("AbstractPlaylistEditorView.9")); //$NON-NLS-1$
        jbClear.addActionListener(this);
        jlTitle = new JLabel(""); //$NON-NLS-1$
        jpControl.add(jbRun,"1,0");
        jpControl.add(jbSave,"3,0");
        jpControl.add(jbAdd,"5,0");
        jpControl.add(jbRemove,"7,0");
        jpControl.add(jbAddShuffle,"9,0");
        jpControl.add(jbUp,"11,0");
        jpControl.add(jbDown,"13,0");
        jpControl.add(jbCurrent,"15,0");
        jpControl.add(jbClear,"17,0");
        jpControl.add(Util.getCentredPanel(jlTitle),"19,0"); //$NON-NLS-1$
        jtable = new JajukTable(model,false);
        jtable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //multi-row selection
        Enumeration enum = jtable.getColumnModel().getColumns();
        JajukCellRender jcr = new JajukCellRender();
        while (enum.hasMoreElements()){
            TableColumn col = (TableColumn)enum.nextElement();
            col.setCellRenderer(jcr);
        }
        jtable.getColumnModel().getColumn(0).setPreferredWidth(20); //just an icon
        jtable.getColumnModel().getColumn(0).setMaxWidth(20);
        jtable.addMouseListener(this);
       //selection listener to hide some buttons to planned tracks
        ListSelectionModel lsm = jtable.getSelectionModel();
        lsm.addListSelectionListener(this);
        double size[][] =
        {{0.99},
                {30,0.99}};
        setLayout(new TableLayout(size));
        add(jpControl,"0,0"); //$NON-NLS-1$
        add(new JScrollPane(jtable),"0,1"); //$NON-NLS-1$
        ObservationManager.register(EVENT_PLAYLIST_REFRESH,this);
        ObservationManager.register(EVENT_PLAYER_STOP,this);
        ObservationManager.register(EVENT_FILE_LAUNCHED,this);
        ObservationManager.register(EVENT_PLAYLIST_CHANGED,this);
        timer.start();  //start own heartbeat system, we don't use fifo one to continue to work during stops
        //DND
        new PlaylistTransferHandler(this,DnDConstants.ACTION_COPY_OR_MOVE);
        new PlaylistTransferHandler(jtable,DnDConstants.ACTION_COPY_OR_MOVE);
        //force refresh
        update(EVENT_PLAYLIST_CHANGED);
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getDesc()
     */
    public String getDesc() {
        return "AbstractPlaylistEditorView.15";	 //$NON-NLS-1$
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(String subject) {
        if (EVENT_PLAYLIST_CHANGED.equals(subject)){
         	//clear planned
        	alPlanned = new ArrayList(0);  //make sure planned is voided if not in Queue
         	jtable.getSelectionModel().clearSelection(); //remove selection 
        	PlaylistFileItem plfi = (PlaylistFileItem)ObservationManager.getDetail(subject,DETAIL_SELECTION);
            this.iType = plfi.getType();
            this.plfi =plfi;
            //set title label
            jlTitle.setText(plfi.getName());
            jlTitle.setToolTipText(plfi.getName());
            //set buttons
            if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                jbAdd.setEnabled(true); //add at the FIFO end by default even with no selection
                jbClear.setEnabled(true);
                jbUp.setEnabled(false); //set it to false just for startup because nothing is selected
                jbDown.setEnabled(false); //set it to false just for startup because nothing is selected 
                jbAddShuffle.setEnabled(true);//add at the FIFO end by default even with no selection
                jbRemove.setEnabled(false); //set it to false just for startup because cursor is over first track and it can't be removed in queue mode
                jbRun.setEnabled(false);
            }
            else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BESTOF){
                jbAdd.setEnabled(false);
                jbClear.setEnabled(false);
                jbDown.setEnabled(false);
                jbAddShuffle.setEnabled(false);
                jbRemove.setEnabled(false);
                jbRun.setEnabled(true);
                jbUp.setEnabled(false);
            }
            else{
                jbAdd.setEnabled(true);//add at the FIFO end by default even with no selection
                jbClear.setEnabled(true);
                jbDown.setEnabled(false);//set it to false just for startup because nothing is selected
                jbUp.setEnabled(false);//set it to false just for startup because nothing is selected
                jbAddShuffle.setEnabled(true);//add at the FIFO end by default even with no selection
                jbRemove.setEnabled(false);//set it to false just for startup because nothing is selected
                jbRun.setEnabled(true);
            }
            Util.stopWaiting(); //stop waiting
        }
        else if ( EVENT_PLAYLIST_REFRESH.equals(subject)){
            if ( plfi == null ){  //nothing ? leave
                iRowNum = 0;
            	return;
            }
            try{
                if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                    alItems = FIFO.getInstance().getFIFO();
                    alPlanned = FIFO.getInstance().getPlanned();
                }
                else{
                    //TBI see repeat in normal playlists
                    alItems = Util.createStackItems(plfi.getPlaylistFile().getBasicFiles(),false,false); //PERF
                }
                iRowNum = alItems.size() + alPlanned.size();
            }
            catch(JajukException je){ //don't trace because it is called in a loop
            } 
            int[] rows = jtable.getSelectedRows();  //save selection
            model.fireTableDataChanged();//refresh
            if (rows.length > 0) {
                bSettingSelection = true;
                jtable.getSelectionModel().setSelectionInterval(rows[0],rows[rows.length-1]); //set saved selection after a refresh
                bSettingSelection = false;
            }
        }
        else if ( EVENT_PLAYER_STOP.equals(subject)){
            alItems.clear();
            alPlanned.clear();
            model.fireTableDataChanged();
        }
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
        if ( e.getClickCount() == 2){ //double clic, launches selected track and all after
            StackItem item = getItem(jtable.getSelectedRow());
            if (item != null ){
                if ( item.isPlanned()){ //we can't lauch a planned track, leave
                    return;
                }
                else{ //normal item
                    FIFO.getInstance().goTo(jtable.getSelectedRow());
                    //remove selection for planned tracks
                    ListSelectionModel lsm = jtable.getSelectionModel();
                    bSettingSelection = true;
                    jtable.getSelectionModel().removeSelectionInterval(lsm.getMinSelectionIndex(),lsm.getMaxSelectionIndex());
                    bSettingSelection = false;
                }
            }
        }
    }
    
    /**
     * Return right stack item in normal or planned stacks
     * @param index
     * @return
     */
    private StackItem getItem(int index){
        if (index < alItems.size()){
            return (StackItem)alItems.get(index);
        }
        else if (index < (alItems.size() + alPlanned.size() )){
            return (StackItem)alPlanned.get(index-alItems.size());
        }
        else{
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    }
    
    
    public void actionPerformed(ActionEvent ae){
        if ( ae.getSource() == jbRun){
            plfi.getPlaylistFile().play();
        }
        else if (ae.getSource() == jbSave){
            //normal playlist
            if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NORMAL){
                if ( this instanceof LogicalPlaylistEditorView){ //if logical editor, warning message
                    StringBuffer sbOut = new StringBuffer(Messages.getString("AbstractPlaylistEditorView.17")); //$NON-NLS-1$
                    Playlist pl = PlaylistManager.getPlaylist(plfi.getPlaylistFile().getHashcode());
                    if ( pl != null){
                        ArrayList alPlaylistFiles = pl.getPlaylistFiles(); 
                        Iterator it = alPlaylistFiles.iterator();
                        while ( it.hasNext()){
                            PlaylistFile plf = (PlaylistFile)it.next();
                            sbOut.append('\n').append(plf.getAbsolutePath());
                        }
                        int i = JOptionPane.showConfirmDialog(Main.getWindow(),sbOut.toString(),Messages.getString("Warning"),JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
                        if ( i == JOptionPane.OK_OPTION){
                            it = alPlaylistFiles.iterator();
                            while ( it.hasNext()){
                                PlaylistFile plf = (PlaylistFile)it.next();
                                plf.setModified(true);
                                try{
                                    plf.setBasicFiles(plfi.getPlaylistFile().getBasicFiles()); //set same files for all playlist files
                                    plf.commit();
                                }
                                catch(JajukException je){
                                    Log.error(je);
                                }
                            }
                        }
                    }
                }
                else{ //in physical perspective
                    try{
                        plfi.getPlaylistFile().commit();
                    }
                    catch(JajukException je){
                        Log.error(je);
                        Messages.showErrorMessage(je.getCode(),je.getMessage());
                    }
                }
            }
            else{  //specfial playlist, same behavior than a save as
                plfi.getPlaylistFile().saveAs();
            }
        }
        else if (ae.getSource() == jbCurrent){
            Iterator it = getRepositoryCurrentPlaylistFileItem().iterator();
            File file = FIFO.getInstance().getCurrentFile(); //look at current file playlist property
            String sId = file.getProperty(OPTION_PLAYLIST);
            if ( sId != null){ //if null, it means this file has not been launched from a playlist
                while ( it.hasNext()){
                    PlaylistFileItem plfi = (PlaylistFileItem)it.next();
                    if ( sId.equals(plfi.getPlaylistFile().getId())){ //if the id for this playlist is the same, match
                        Properties properties = new Properties();
    					properties.put(DETAIL_SELECTION,plfi);
    					properties.put(DETAIL_ORIGIN,this);
    					ObservationManager.notify(EVENT_PLAYLIST_CHANGED,properties);
    			        break;
                    }
                }
            }
        }
        else if (ae.getSource() == jbClear){
            plfi.getPlaylistFile().clear();
            if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){ //if it is the queue playlist, stop the selection
                FIFO.getInstance().stopRequest();
            }
        }
        else if (ae.getSource() == jbDown || ae.getSource() == jbUp){
            int iRow = jtable.getSelectedRow();
            if ( iRow != -1 ){ //-1 means nothing is selected
                if ( ae.getSource() == jbDown){
                    plfi.getPlaylistFile().down(iRow);
                }
                else if ( ae.getSource() == jbUp){
                    plfi.getPlaylistFile().up(iRow);
                }
            }
        }
        else if (ae.getSource() == jbRemove){
            int[] iRows = jtable.getSelectedRows();
        	for (int i=0;i<iRows.length;i++){
        		plfi.getPlaylistFile().remove(iRows[i]);
        		iRowNum --;
        	}
        }
        else if (ae.getSource() == jbAdd || ae.getSource() == jbAddShuffle){
        	int iRow = jtable.getSelectedRow();
        	if ( iRow < 0 ){ //no row is selected, take fifo last position as a default
        		iRow = FIFO.getInstance().getFIFO().size()-1; 
        	}
        	File file = null;
        	if (ae.getSource() == jbAdd){
        		JajukFileChooser jfchooser = new JajukFileChooser(new JajukFileFilter(true,TypeManager.getAllMusicTypes())); 
        		if (fileLast != null){//restore last selected directory
        			jfchooser.setCurrentDirectory(fileLast.getParentFile());
        		}
        		int returnVal = jfchooser.showOpenDialog(this);
        		if (returnVal == JFileChooser.APPROVE_OPTION) {
        			file = new BasicFile(jfchooser.getSelectedFile());
        			fileLast = jfchooser.getSelectedFile(); //store current file
        		}
        	}
        	else if (ae.getSource() == jbAddShuffle){
        		file = FileManager.getShuffleFile(); 
        	}
        	try{
        		plfi.getPlaylistFile().addBasicFile(iRow,new BasicFile(file));
        		iRowNum ++;	
        	}
        	catch(JajukException je){
        		Messages.showErrorMessage(je.getCode());
        		Log.error(je);
        	}
        }
    }
    
    /** 
     * Get the playlist file items in the associated playlist repository view 
     * @return
     */
    abstract ArrayList getRepositoryCurrentPlaylistFileItem();
    
    /**
     * Set the current playlist file item in the playlist repository view
     * @param plfi
     */
    abstract void setRepositoryPlayListFileItem(PlaylistFileItem plfi);
    
    /**
     * @return Returns current playlist file item
     */
    public PlaylistFileItem getCurrentPlaylistFileItem() {
        return plfi;
    }
    
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() ||  bSettingSelection){ //leave during normal refresh
            return;
        }
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (!lsm.isSelectionEmpty()) {
            int selectedRow = lsm.getMaxSelectionIndex();
            boolean bPlanned = false; //true if selected line is a planned track
            if (selectedRow > alItems.size()-1){ //means it is a planned track
                bPlanned = true;
            }
           //now analyze each button
           //add button
           if (bPlanned //no add for planned track
                   || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF  //neither for bestof playlist
                   || lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex() ){ //multiple selection not supported
                   jbAdd.setEnabled(false);
           }
           else{
               jbAdd.setEnabled(true);
           }
           //Remove button
           if (bPlanned ){//not for planned track
               jbRemove.setEnabled(true);
           }
           else{
               //check for first row remove case : we can't remove currently played track
               if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && lsm.getMinSelectionIndex() == 0
                   || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF ){ //neither for bestof playlist
                   jbRemove.setEnabled(false);
               }
               else{
                   jbRemove.setEnabled(true);
               }
           }
           //Add shuffle button
           if (bPlanned //not for planned track
                   || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF  //neither for bestof playlist
                   || lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex() ){ //multiple selection not supported
               jbAddShuffle.setEnabled(false);
           }
           else{
               jbAddShuffle.setEnabled(true);
           }
           //Up button
           if (lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex() //check if several rows have been selected : doesn't supported yet 
                   || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && FIFO.getInstance().containsRepeat()) //check if we are in the queue with repeated tracks : not supported yet
                   || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF ){ //neither for bestof playlist
               jbUp.setEnabled(false);
           }
           else{ //yet here ?
               if (bPlanned){
	               if (lsm.getMaxSelectionIndex() > alItems.size()){ //a planned track can't go to normal files stack
	                   jbUp.setEnabled(true);    
	               }
	               else{
	                   jbUp.setEnabled(false);
	               }
               }
	           else{ //normal item
	               if (lsm.getMinSelectionIndex() == 0
	                       || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && lsm.getMinSelectionIndex() == 1)){//check if we selected second track just after current tracks
	                   jbUp.setEnabled(false);  //already at the top
	               }
	               else{
	                   jbUp.setEnabled(true);
	               }
	           }
           }
           //Down button
           if (lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex() //check if several rows have been selected : doesn't supported yet 
                   || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && FIFO.getInstance().containsRepeat()) //check if we are in the queue with repeated tracks : not supported yet
                   || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF ){ //neither for bestof playlist
               jbDown.setEnabled(false);
           }
           else{ //yet here ?
               if (bPlanned){
	               if (lsm.getMaxSelectionIndex() < alItems.size()+alPlanned.size()-1){ //can't go further
	                   jbDown.setEnabled(true);    
	               }
	               else{
	                   jbDown.setEnabled(false);
	               }
               }
	           else{ //normal item
	               if (lsm.getMaxSelectionIndex() < alItems.size()-1){ //a normal item can't go in the planned items
	                   jbDown.setEnabled(true);    
	               }
	               else{
	                   jbDown.setEnabled(false);
	               }
	           }
           }
	    }     
    }
}
