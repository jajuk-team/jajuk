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
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import layout.TableLayout;

import org.jajuk.Main;
import org.jajuk.base.BasicFile;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.StackItem;
import org.jajuk.base.Type;
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
    JToolBar jtb;
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
    
    /**Last selected row, used to re-select right row after each refresh*/
    private int iSelectedRow = 0;
    
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
   
    /**Refresh timer*/
    private Timer timer = new Timer(REFRESH_TIME,new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            update(EVENT_PLAYLIST_REFRESH);
        }
    });
   
    
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
            StackItem item = getIItem(rowIndex);
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
                    return new IconLabel(Util.getIcon(ICON_TRACK_FIFO_PLANNED),"",color,null,font);
                }
                else {
                    if (item.isRepeat()){
                        return new IconLabel(Util.getIcon(ICON_TRACK_FIFO_REPEAT),"",color,null,font); //normal file, repeated
                    }
                    else{ 
                        return new IconLabel(Util.getIcon(ICON_TRACK_FIFO_NORM),"",color,null,font); //normal file, not repeated
                    }
                }
            }
            else if ( columnIndex == 1){
                return new IconLabel(null,bf.getTrack().getName(),color,null,font);
            }
            else if ( columnIndex == 2){
                return new IconLabel(null,bf.getAbsolutePath(),color,null,font);
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
        jtb = new JToolBar();
        jtb.setFloatable(false);
        jpControl.setBorder(BorderFactory.createEtchedBorder());
        int iXspace = 0;
        double sizeControl[][] =
        {{iXspace,220,0.50,iXspace},
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
        jtb.add(jbRun);
        jtb.add(jbSave);
        jtb.add(jbAdd);
        jtb.add(jbRemove);
        jtb.add(jbAddShuffle);
        jtb.add(jbUp);
        jtb.add(jbDown);
        jtb.add(jbCurrent);
        jtb.add(jbClear);
        
        jpControl.add(jtb,"1,0"); //$NON-NLS-1$
        jpControl.add(Util.getCentredPanel(jlTitle),"2,0"); //$NON-NLS-1$
        
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
            PlaylistFileItem plfi = (PlaylistFileItem)ObservationManager.getDetail(subject,DETAIL_SELECTION);
            this.iType = plfi.getType();
            this.plfi =plfi;
            //set title label
            jlTitle.setText(plfi.getName());
            jlTitle.setToolTipText(plfi.getName());
            //set buttons
            if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                jbAdd.setEnabled(true);
                jbClear.setEnabled(true);
                jbUp.setEnabled(true);
                 jbDown.setEnabled(true);
                jbAddShuffle.setEnabled(true);
                jbRemove.setEnabled(true);
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
                jbAdd.setEnabled(true);
                jbClear.setEnabled(true);
                jbDown.setEnabled(true);
                jbAddShuffle.setEnabled(true);
                jbRemove.setEnabled(true);
                jbRun.setEnabled(true);
                jbUp.setEnabled(true);
            }
            Util.stopWaiting(); //stop waiting
        }
        else if ( EVENT_PLAYLIST_REFRESH.equals(subject)){
            if ( plfi == null ){  //nothing ? leave
                return;
            }
            try{
                if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                    alItems = FIFO.getInstance().getFIFO();
                    alPlanned = FIFO.getInstance().getPlanned();
                }
                else{
                    //TBI see repeat in normal playlists
                    alItems = Util.createStackItems(plfi.getPlaylistFile().getBasicFiles(),false,false);
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
            alItems = new ArrayList(0);
            alPlanned = new ArrayList(0);
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
            StackItem item = getIItem(jtable.getSelectedRow());
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
        else{ //edition mode
            iSelectedRow = jtable.getSelectedRow();
        }
    }
    
    /**
     * Return right stack item in normal or planned stacks
     * @param index
     * @return
     */
    private StackItem getIItem(int index){
        if (index < alItems.size()){
            return (StackItem)alItems.get(index);
        }
        else if (index < (alItems.size() +alPlanned.size() )){
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
                ObservationManager.notify(EVENT_PLAYLIST_REFRESH); //alert playlists editors ( queue playlist ) something changed for him
            }
        }
        else if (ae.getSource() == jbDown || ae.getSource() == jbUp){
            int iRow = jtable.getSelectedRow();
            if ( iRow != -1 ){ //-1 means nothing is selected
                if ( ae.getSource() == jbDown){
                    plfi.getPlaylistFile().down(iRow);
                    iSelectedRow = iSelectedRow+1; //keep selection on moving track
                }
                else if ( ae.getSource() == jbUp){
                    plfi.getPlaylistFile().up(iRow);
                    iSelectedRow = iSelectedRow-1; //keep selection on moving track
                }
            }
        }
        else if (ae.getSource() == jbRemove){
            int iRow = jtable.getSelectedRow();
            plfi.getPlaylistFile().remove(iRow);
            if ( iSelectedRow - 1 >= 0){
                iSelectedRow = iSelectedRow-1; //keep selection on moving track
            }
            iRowNum --;
        }
        else if (ae.getSource() == jbAdd){
            int iRow = jtable.getSelectedRow();
            if ( iRow < 0 ){ //no row is selected
                iRow = jtable.getRowCount();
            }
            JajukFileChooser jfchooser = new JajukFileChooser(new JajukFileFilter(true,new Type[]{TypeManager.getTypeByExtension(EXT_MP3)})); 
            int returnVal = jfchooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                java.io.File file = jfchooser.getSelectedFile();
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
        else if (ae.getSource() == jbAddShuffle){
            int iRow = jtable.getSelectedRow();
            if ( iRow < 0 ){ //no row is selected
                iRow = jtable.getRowCount();
            }
           File file = FileManager.getShuffleFile();
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
            if (selectedRow > alItems.size()-1){ //means it is a planned track
                jbAdd.setEnabled(false);
                jbRemove.setEnabled(true);
                jbUp.setEnabled(false);
                jbDown.setEnabled(false);
                jbAddShuffle.setEnabled(false);
            }
            else{
                jbAdd.setEnabled(true);
                jbRemove.setEnabled(true);
                jbUp.setEnabled(true);
                jbDown.setEnabled(true);
                jbAddShuffle.setEnabled(true);
            }
        }
    }
    
}
