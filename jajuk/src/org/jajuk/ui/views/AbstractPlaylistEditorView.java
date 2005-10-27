/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.IconLabel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukCellRender;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.ui.PlaylistTransferHandler;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;


/**
 * Adapter for playlists editors 
 *  * <p>Singleton
 * @author     Bertrand Florat
 * @created   29 dec. 2003
 */
public abstract class AbstractPlaylistEditorView extends ViewAdapter implements Observer,MouseListener,ActionListener,ListSelectionListener {
    
    
    JPanel jpControl;
    JButton jbRun;
    JButton jbSave;
    JButton jbRemove;
    JButton jbUp;
    JButton jbDown;
    JButton jbAddShuffle;
    JButton jbClear;
    JLabel jlTitle;
    JTable jtable;
    
    JPopupMenu jmenuFile;
    JMenuItem jmiFilePlay;
    JMenuItem jmiFilePush;

    
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
    
    /**Columns names table**/
    protected String[] sColName = null;
    
    /**Selection set flag*/
    private boolean bSettingSelection = false;
    
    /**Last selected directory using add button*/
    private java.io.File fileLast;
    
    /*Cashed icons*/
    private static final ImageIcon iconNormal = Util.getIcon(ICON_TRACK_FIFO_NORM);
    private static final ImageIcon iconRepeat  = Util.getIcon(ICON_TRACK_FIFO_REPEAT);
    private static final ImageIcon iconPlanned = Util.getIcon(ICON_TRACK_FIFO_PLANNED);
    private static final ImageIcon iconPlaylist = Util.getIcon(ICON_PLAYLIST_FILE);
    
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
                jbRemove.setEnabled(false);
                return null;
            }
            jbRemove.setEnabled(true);
            boolean bPlanned = false;
            Font font = null;
            StackItem item = getItem(rowIndex);
            if (item == null){
                return null;
            }
            Color color = null; //default background color
            StackItem itemCurrent = FIFO.getInstance().getCurrentItem();
            if (itemCurrent != null  && itemCurrent.equals(item)){ //if it is the currently played track, change color
                if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){ //for queue playlist, only highlight real current track
                    if ( FIFO.getInstance().getIndex() == rowIndex){
                        color = Color.ORANGE;
                    }
                }
                else{ //for others, we can't guess whish one to highlight if several times the same EVO
                    color = Color.ORANGE;    
                }
            }
            if( item.isPlanned() ){ //it is a planned file
                bPlanned = true;
                font = new Font("serif",Font.ITALIC,12); //font for planned items //$NON-NLS-1$
            }
            File bf = (File)item.getFile();
            if ( columnIndex == 0){
                if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                    if (bPlanned){
                        return new IconLabel(iconPlanned,"",color,null,font,Messages.getString("AbstractPlaylistEditorView.20")); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    else {
                        if (item.isRepeat()){
                            return new IconLabel(iconRepeat,"",color,null,font,Messages.getString("AbstractPlaylistEditorView.19")); //normal file, repeated //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        else{ 
                            return new IconLabel(iconNormal,"",color,null,font,Messages.getString("AbstractPlaylistEditorView.18")); //normal file, not repeated //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
                else{
                    return new IconLabel(iconPlaylist,"",color,null,font,Messages.getString("AbstractPlaylistEditorView.21")); //$NON-NLS-1$ //$NON-NLS-2$
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
            sColName = new String[]{"",Messages.getString("AbstractPlaylistEditorView.0"),Messages.getString("AbstractPlaylistEditorView.1")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        //Control panel
        jpControl = new JPanel();
        jpControl.setBorder(BorderFactory.createEtchedBorder());
        int iXspace = 2;
        //Note : we don't use toolbar because it's buggy in Metal look and feel : icon get bigger
        double sizeControl[][] =
        {{iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,TableLayout.FILL,iXspace},
                {25,0.99}};
        jpControl.setLayout(new TableLayout(sizeControl));
        jbRun = new JButton(Util.getIcon(ICON_RUN));
        jbRun.setToolTipText(Messages.getString("AbstractPlaylistEditorView.2")); //$NON-NLS-1$
        jbRun.addActionListener(this);
        jbSave = new JButton(Util.getIcon(ICON_SAVE));
        jbSave.setToolTipText(Messages.getString("AbstractPlaylistEditorView.3")); //$NON-NLS-1$
        jbSave.addActionListener(this);
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
        jbClear = new JButton(Util.getIcon(ICON_CLEAR));
        jbClear.setToolTipText(Messages.getString("AbstractPlaylistEditorView.9")); //$NON-NLS-1$
        jbClear.addActionListener(this);
        jlTitle = new JLabel(""); //$NON-NLS-1$
        jpControl.add(jbRun,"1,0"); //$NON-NLS-1$
        jpControl.add(jbSave,"3,0"); //$NON-NLS-1$
        jpControl.add(jbRemove,"5,0"); //$NON-NLS-1$
        jpControl.add(jbAddShuffle,"7,0"); //$NON-NLS-1$
        jpControl.add(jbUp,"9,0"); //$NON-NLS-1$
        jpControl.add(jbDown,"11,0"); //$NON-NLS-1$
        jpControl.add(jbClear,"13,0"); //$NON-NLS-1$
        jpControl.add(Util.getCentredPanel(jlTitle),"15,0"); //$NON-NLS-1$
        jtable = new JTable(model){//we don't use a JajukTable that lose current track layout (orange)
            /**
             * add tooltips to each cell
             */
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                TableModel model = getModel();
                if (rowIndex < 0 || colIndex < 0){
                    return null;
                }
                Object o = getModel().getValueAt(rowIndex,colIndex);
                if (o == null){
                    return null;
                }
                else if(o instanceof IconLabel){
                    return ((IconLabel)o).getTooltip(); 
                }
                else{
                    return o.toString();
                }
            }
        };
        jtable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //multi-row selection
        Enumeration enumeration = jtable.getColumnModel().getColumns();
        JajukCellRender jcr = new JajukCellRender();
        while (enumeration.hasMoreElements()){
            TableColumn col = (TableColumn)enumeration.nextElement();
            col.setCellRenderer(jcr);
        }
        jtable.getColumnModel().getColumn(0).setPreferredWidth(20); //just an icon
        jtable.getColumnModel().getColumn(0).setMaxWidth(20);
        jtable.getTableHeader().setPreferredSize(new Dimension(0,20));
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
        //menu items
        jmenuFile = new JPopupMenu();
        jmiFilePlay = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.23")); //$NON-NLS-1$
        jmiFilePlay.addActionListener(this);
        jmiFilePush = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.24")); //$NON-NLS-1$
        jmiFilePush.addActionListener(this);
        jmenuFile.add(jmiFilePlay);
        jmenuFile.add(jmiFilePush);
        //register events
        ObservationManager.register(EVENT_PLAYLIST_REFRESH,this);
        ObservationManager.register(EVENT_PLAYER_STOP,this);
        ObservationManager.register(EVENT_FILE_LAUNCHED,this);
        ObservationManager.register(EVENT_PLAYLIST_CHANGED,this);
        //DND
        new PlaylistTransferHandler(this,DnDConstants.ACTION_COPY_OR_MOVE);
        new PlaylistTransferHandler(jtable,DnDConstants.ACTION_COPY_OR_MOVE);
        //force a refresh
        update(new Event(EVENT_PLAYLIST_CHANGED,ObservationManager.getDetailsLastOccurence(EVENT_PLAYLIST_CHANGED)));
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
    public void update(Event event) {
        String subject = event.getSubject();
        Object origin = ObservationManager.getDetail(event,DETAIL_ORIGIN);
        if (EVENT_PLAYLIST_CHANGED.equals(subject) && event.getDetails() != null){
            //test mapping between editor and repository
            if ((this instanceof PhysicalPlaylistEditorView && !(origin instanceof PhysicalPlaylistRepositoryView))
                || (this instanceof LogicalPlaylistEditorView && !(origin instanceof LogicalPlaylistRepositoryView))){
                return;
            }
            //clear planned
            alPlanned = new ArrayList(0);  //make sure planned is voided if not in Queue
            jtable.getSelectionModel().clearSelection(); //remove selection 
            PlaylistFileItem plfi = (PlaylistFileItem)ObservationManager.getDetail(event,DETAIL_SELECTION);
            this.iType = plfi.getType();
            this.plfi =plfi;
            //set title label
            jlTitle.setText(plfi.getName());
            jlTitle.setToolTipText(plfi.getName());
            setDefaultButtonState();
            update(new Event(EVENT_PLAYLIST_REFRESH,ObservationManager.getDetailsLastOccurence(EVENT_PLAYLIST_REFRESH))); //force refresh
            Util.stopWaiting(); //stop waiting
        }
        else if ( EVENT_PLAYLIST_REFRESH.equals(subject)){
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if ( plfi == null ){  //nothing ? leave
                        iRowNum = 0;
                        return;
                    }
                    //when nothing is selected, set default button state
                    if (jtable.getSelectionModel().getMinSelectionIndex() == -1){
                        setDefaultButtonState();
                    }
                    try{
                        if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                            alItems = FIFO.getInstance().getFIFO();
                            alPlanned = FIFO.getInstance().getPlanned();
                        }
                        else{
                            alItems = Util.createStackItems(plfi.getPlaylistFile().getFiles(),
                                ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true); //PERF
                        }
                        iRowNum = alItems.size() + alPlanned.size();
                    }
                    catch(JajukException je){ //don't trace because it is called in a loop
                    } 
                    int[] rows = jtable.getSelectedRows();  //save selection
                    model.fireTableDataChanged();//refresh
                    bSettingSelection = true;
                    for (int i=0;i<rows.length;i++) {
                        jtable.getSelectionModel().addSelectionInterval(rows[i],rows[i]); //set saved selection after a refresh
                    }
                    bSettingSelection = false;
                }
            });
        }
        
        else if ( EVENT_PLAYER_STOP.equals(subject) 
                && plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE ){
            alItems.clear();
            alPlanned.clear();
            model.fireTableDataChanged();
        }
    }
    
    /**
     * Set default button state
     *
     */
    private void setDefaultButtonState(){
        //set buttons
        if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
            jbClear.setEnabled(true);
            jbUp.setEnabled(false); //set it to false just for startup because nothing is selected
            jbDown.setEnabled(false); //set it to false just for startup because nothing is selected 
            jbAddShuffle.setEnabled(true);//add at the FIFO end by default even with no selection
            jbRemove.setEnabled(false); //set it to false just for startup because cursor is over first track and it can't be removed in queue mode
            jbRun.setEnabled(false);
        }
        else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BESTOF ||  iType == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES){
            jbClear.setEnabled(false);
            jbDown.setEnabled(false);
            jbAddShuffle.setEnabled(false);
            jbRemove.setEnabled(false);
            jbRun.setEnabled(true);
            jbUp.setEnabled(false);
        }
        else{
            jbClear.setEnabled(true);
            jbDown.setEnabled(false);//set it to false just for startup because nothing is selected
            jbUp.setEnabled(false);//set it to false just for startup because nothing is selected
            jbAddShuffle.setEnabled(true);//add at the FIFO end by default even with no selection
            jbRemove.setEnabled(false);//set it to false just for startup because nothing is selected
            jbRun.setEnabled(true);
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
                //For the queue
                if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                    if ( item.isPlanned()){ //we can't lauch a planned track, leave
                        item.setPlanned(false);
                        item.setRepeat(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));
                        item.setUserLaunch(true);
                        FIFO.getInstance().push(item,false);
                    }
                    else{ //non planned items
                        FIFO.getInstance().goTo(jtable.getSelectedRow());
                        //remove selection for planned tracks
                        ListSelectionModel lsm = jtable.getSelectionModel();
                        bSettingSelection = true;
                        jtable.getSelectionModel().removeSelectionInterval(lsm.getMinSelectionIndex(),lsm.getMaxSelectionIndex());
                        bSettingSelection = false;
                    }
                }
                //For others playlists, we launch all tracks from this position to the end of playlist
                else{
                    FIFO.getInstance().push(getItemsFrom(jtable.getSelectedRow()),
                        ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
                }
            }
        }
        else if ( e.getClickCount() == 1 
                && e.getButton()==MouseEvent.BUTTON3
                && plfi.getType() != PlaylistFileItem.PLAYLIST_TYPE_QUEUE ){  //right clic on a selected node set
            // if none or 1 node is selected, a right click on another node select it
            //if more than 1, we keep selection and display a popup for them
            if (jtable.getSelectedRowCount() < 2){
                int iSelection = jtable.rowAtPoint(e.getPoint());
                jtable.getSelectionModel().setSelectionInterval(iSelection,iSelection);
            }
            jmenuFile.show(jtable,e.getX(),e.getY());
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
    
    /**
     * Return all stack items from this value to the end of selection
     * @param index
     * @return an arraylist of stackitems or null if index is out of bounds
     */
    private ArrayList getItemsFrom(int index){
        ArrayList alOut = new ArrayList(alItems.size());
        if (index < alItems.size()){
            return new ArrayList(alItems.subList(index,alItems.size()));
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
        try{
            if ( ae.getSource() == jbRun){
                plfi.getPlaylistFile().play();
            }
            else if (ae.getSource() == jbSave){
                //normal playlist
                if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NORMAL){
                    if ( this instanceof LogicalPlaylistEditorView){ //if logical editor, warning message
                        StringBuffer sbOut = new StringBuffer(Messages.getString("AbstractPlaylistEditorView.17")); //$NON-NLS-1$
                        Playlist pl = (Playlist)PlaylistManager.getInstance().getItem(plfi.getPlaylistFile().getHashcode());
                        if ( pl != null){
                            ArrayList alPlaylistFiles = pl.getPlaylistFiles(); 
                            Iterator it = alPlaylistFiles.iterator();
                            while ( it.hasNext()){
                                PlaylistFile plf = (PlaylistFile)it.next();
                                sbOut.append('\n').append(plf.getAbsolutePath());
                            }
                            int i = Messages.getChoice(sbOut.toString(),JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
                            if ( i == JOptionPane.OK_OPTION){
                                it = alPlaylistFiles.iterator();
                                while ( it.hasNext()){
                                    PlaylistFile plf = (PlaylistFile)it.next();
                                    plf.setModified(true);
                                    try{
                                        plf.setFiles(plfi.getPlaylistFile().getFiles()); //set same files for all playlist files
                                        plf.commit();
                                        InformationJPanel.getInstance().setMessage(Messages.getString("AbstractPlaylistEditorView.22"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
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
                            InformationJPanel.getInstance().setMessage(Messages.getString("AbstractPlaylistEditorView.22"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
                        }
                        catch(JajukException je){
                            Log.error(je);
                            Messages.showErrorMessage(je.getCode(),je.getMessage());
                        }
                    }
                }
                else{  //special playlist, same behavior than a save as
                    plfi.getPlaylistFile().saveAs();
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
                        if (iRow < iRowNum-1){
                            update(new Event(EVENT_PLAYLIST_REFRESH,ObservationManager.getDetailsLastOccurence(EVENT_PLAYLIST_REFRESH))); //force immediate table refresh
                            jtable.getSelectionModel().setSelectionInterval(iRow+1,iRow+1);
                        }
                    }
                    else if ( ae.getSource() == jbUp){
                        plfi.getPlaylistFile().up(iRow);
                        if (iRow > 0){
                            update(new Event(EVENT_PLAYLIST_REFRESH,ObservationManager.getDetailsLastOccurence(EVENT_PLAYLIST_REFRESH))); //force immediate table refresh
                            jtable.getSelectionModel().setSelectionInterval(iRow-1,iRow-1);
                        }
                    }
                }
            }
            else if (ae.getSource() == jbRemove){
                int[] iRows = jtable.getSelectedRows();
                for (int i=0;i<iRows.length;i++){
                    plfi.getPlaylistFile().remove(iRows[i]);
                    iRowNum --;
                }
                //set selection to last line if end reached
                int iLastRow = jtable.getRowCount()-1;
                if (iRows[0] == jtable.getRowCount()){
                    jtable.getSelectionModel().setSelectionInterval(iLastRow,iLastRow);
                }
            }
            else if ( ae.getSource() == jbAddShuffle){
                int iRow = jtable.getSelectedRow();
                if ( iRow < 0 ){ //no row is selected, add to the end
                    if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                        iRow = FIFO.getInstance().getFIFO().size();
                    }
                    else{
                        iRow = jtable.getRowCount();
                    }
                }
                File file = FileManager.getInstance().getShuffleFile(); 
                try{
                    plfi.getPlaylistFile().addFile(iRow,file);
                    iRowNum ++;	
                    jbRemove.setEnabled(true);
                }
                catch(JajukException je){
                    Messages.showErrorMessage(je.getCode());
                    Log.error(je);
                }
            }
            else if ( ae.getSource() == jmiFilePlay ||  ( ae.getSource() == jmiFilePush)){
                //computes selected items
                ArrayList alItemsToPlay = new ArrayList(jtable.getSelectedRowCount());
                int[] indexes = jtable.getSelectedRows();
                for (int i=0;i<indexes.length;i++){
                    alItemsToPlay.add(alItems.get(indexes[i]));
                }
                FIFO.getInstance().push(alItemsToPlay,ae.getSource() == jmiFilePush);
            }
        }
        catch(Exception e2){
            Log.error(e2);
        }
        finally{
            ObservationManager.notify(new Event(EVENT_PLAYLIST_REFRESH)); //refresh playlist editor
        }
    }
    
    /** 
     * Get the playlist file items in the associated playlist repository view 
     * @return
     */
    abstract ArrayList getRepositoryCurrentPlaylistFileItem();
    
       
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
            //Remove button
            if (bPlanned ){//not for planned track
                jbRemove.setEnabled(true);
            }
            else{
                //check for first row remove case : we can't remove currently played track
                if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && lsm.getMinSelectionIndex() == 0
                        || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF 
                        || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES){ //neither for bestof or novelties playlist
                    jbRemove.setEnabled(false);
                }
                else{
                    jbRemove.setEnabled(true);
                }
            }
            //Add shuffle button
            if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF  //neither for bestof playlist
                    || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES
                    || lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex() //multiple selection not supported
                    || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && lsm.getMinSelectionIndex() == 0 )//can't add track at current track position
                    || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && selectedRow > FIFO.getInstance().getFIFO().size() )//no add for planned track but user can add over first planned track to extand FIFO
                ){ 
                jbAddShuffle.setEnabled(false);
            }
            else{
                jbAddShuffle.setEnabled(true);
            }
            //Up button
            if (lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex() //check if several rows have been selected : doesn't supported yet 
                    || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && FIFO.getInstance().containsRepeat()) //check if we are in the queue with repeated tracks : not supported yet
                    || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF 
                    || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES){ //neither for bestof or novelties playlist
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
                    || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF 
                    || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES){ //neither for bestof playlist nor novelties
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
                    if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && lsm.getMaxSelectionIndex() == 0){ //current track can't go down
                        jbDown.setEnabled(false);
                    }
                    else if ( lsm.getMaxSelectionIndex() < alItems.size()-1){ //a normal item can't go in the planned items
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
