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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import org.jajuk.base.Bookmarks;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.StackItem;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.IconLabel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukCellRender;
import org.jajuk.ui.JajukTable;
import org.jajuk.ui.JajukTableModel;
import org.jajuk.ui.PlaylistEditorTransferHandler;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.ui.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.ConditionalHighlighter;


/**
 * Adapter for playlists editors 
 *  * <p>Singleton
 * @author     Bertrand Florat
 * @created   29 dec. 2003
 */
public abstract class AbstractPlaylistEditorView extends ViewAdapter implements Observer,MouseListener,ActionListener,ListSelectionListener,TableColumnModelListener {
    
    JPanel jpControl;
    JButton jbRun;
    JButton jbSave;
    JButton jbRemove;
    JButton jbUp;
    JButton jbDown;
    JButton jbAddShuffle;
    JButton jbClear;
    JLabel jlTitle;
    JajukTable jtable;
    
    JPopupMenu jmenuFile;
    JMenuItem jmiFilePlay;
    JMenuItem jmiFilePush;
    JMenuItem jmiFileAddFavorites;
    JMenuItem jmiFileProperties;
    
    /**playlist editor title : playlist file or playlist name*/
    String sTitle;
    
    /**Current playlist file item*/
    PlaylistFileItem plfi;
    
    /**Playlist file type*/
    int iType;
    
    /**Values*/
    List<StackItem> alItems = new ArrayList<StackItem>(10);
    
    /**Values planned*/
    ArrayList<StackItem> alPlanned = new ArrayList<StackItem>(10);
    
    /**Selection set flag*/
    boolean bSettingSelection = false;
    
    /**Last selected directory using add button*/
    java.io.File fileLast;
    
    /**Model refreshing flag*/
    boolean bReloading = false;
    
    /*Cashed icons*/
    static final ImageIcon iconNormal = Util.getIcon(ICON_TRACK_FIFO_NORM);
    static final ImageIcon iconRepeat  = Util.getIcon(ICON_TRACK_FIFO_REPEAT);
    static final ImageIcon iconPlanned = Util.getIcon(ICON_TRACK_FIFO_PLANNED);
    static final ImageIcon iconPlaylist = Util.getIcon(ICON_PLAYLIST_FILE);
    
    /**Model*/
    private JajukTableModel model;
    
    /**Model for table*/
    class PlayListEditorTableModel extends JajukTableModel {
        
        private static final long serialVersionUID = 1L;
        final Font fontPlanned = new Font("serif",Font.ITALIC,12); //font for planned items //$NON-NLS-1$
        
        public PlayListEditorTableModel(){
            super(15); 
            setEditable(false); //table not editable
            prepareColumns();
            populateModel();
        }
        
        /**
         * Need to overwrite this method for drag and drop
         */
        public Item getItemAt(int iRow){
            return AbstractPlaylistEditorView.this.getItem(iRow).getFile();
        }
        
        /**
         * Create columbs configuration
         *
         */
        public synchronized void prepareColumns(){
            vColNames.clear();
            vId.clear();
            
            // State icon (play/repeat/planned) 
            vColNames.add("");//$NON-NLS-1$
            vId.add("0");//$NON-NLS-1$
            
            // Track name
            //Note we display "title" and not "name" for this property for clearness
            vColNames.add(Messages.getString("AbstractPlaylistEditorView.0")); //$NON-NLS-1$
            vId.add(XML_TRACK_NAME); //$NON-NLS-1$
            
            // Album
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_ALBUM)); //$NON-NLS-1$
            vId.add(XML_TRACK_ALBUM); //$NON-NLS-1$
            
            // Author
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_AUTHOR)); //$NON-NLS-1$
            vId.add(XML_TRACK_AUTHOR); //$NON-NLS-1$
            
            // Style
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_STYLE)); //$NON-NLS-1$
            vId.add(XML_TRACK_STYLE); //$NON-NLS-1$
            
            // Stars
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_RATE)); //$NON-NLS-1$
            vId.add(XML_TRACK_RATE); //$NON-NLS-1$
            
            // Year
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_YEAR)); //$NON-NLS-1$
            vId.add(XML_TRACK_YEAR); //$NON-NLS-1$
            
            // Length
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_LENGTH));
            vId.add(XML_TRACK_LENGTH);
            
            // comments
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_COMMENT));
            vId.add(XML_TRACK_COMMENT);
            
            // Added date
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_ADDED));
            vId.add(XML_TRACK_ADDED);
            
            // order
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_ORDER));
            vId.add(XML_TRACK_ORDER);
            
            //Device
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_DEVICE));
            vId.add(XML_DEVICE);
            
            //Directory
            vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_DIRECTORY));
            vId.add(XML_DIRECTORY);
            
            // File name
            vColNames.add(Messages.getString("Property_filename")); //$NON-NLS-1$
            vId.add(XML_FILE);
            
            // Hits
            vColNames.add(Messages.getString("Property_hits")); //$NON-NLS-1$
            vId.add(XML_TRACK_HITS);
            
            //custom properties now
            //for tracks
            Iterator it = TrackManager.getInstance().getCustomProperties().iterator();
            while (it.hasNext()){
                PropertyMetaInformation meta = (PropertyMetaInformation)it.next();
                vColNames.add(meta.getName());
                vId.add(meta.getName());
            }   
            //for files
            it = FileManager.getInstance().getCustomProperties().iterator();
            while (it.hasNext()){
                PropertyMetaInformation meta = (PropertyMetaInformation)it.next();
                vColNames.add(meta.getName());
                vId.add(meta.getName());
            }
        }
        
        
        /**
         * Fill model with data using an optionnal filter property
         */
        public synchronized void populateModel(String sPropertyName,String sPattern){
            iRowNum =  alItems.size() + alPlanned.size();
            oValues = new Object[iRowNum][iNumberStandardCols
                                          +TrackManager.getInstance().getCustomProperties().size()
                                          +FileManager.getInstance().getCustomProperties().size()];
            for (int iRow = 0;iRow < iRowNum;iRow++){
                boolean bPlanned = false;
                Font font = null;
                StackItem item = getItem(iRow);
                if( item.isPlanned() ){ //it is a planned file
                    bPlanned = true;
                    font = fontPlanned;
                }
                File bf = item.getFile();
                
                //Play
                if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                    if (bPlanned){
                        oValues[iRow][0] = new IconLabel(iconPlanned,"",null,null,font,Messages.getString("AbstractPlaylistEditorView.20")); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    else {
                        if (item.isRepeat()){
                            oValues[iRow][0] = new IconLabel(iconRepeat,"",null,null,font,Messages.getString("AbstractPlaylistEditorView.19")); //normal file, repeated //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        else{ 
                            oValues[iRow][0] = new IconLabel(iconNormal,"",null,null,font,Messages.getString("AbstractPlaylistEditorView.18")); //normal file, not repeated //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
                else{
                    oValues[iRow][0] = new IconLabel(iconPlaylist,"",null,null,font,Messages.getString("AbstractPlaylistEditorView.21")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                //Track name
                oValues[iRow][1] = bf.getTrack().getName();
                //Album
                oValues[iRow][2] = bf.getTrack().getAlbum().getName2();
                //Author
                oValues[iRow][3] = bf.getTrack().getAuthor().getName2();
                //Style
                oValues[iRow][4] = bf.getTrack().getStyle().getName2();
                //Rate
                oValues[iRow][5] = bf.getTrack().getStars();
                //Year
                oValues[iRow][6] = bf.getTrack().getYear();
                //Length
                oValues[iRow][7] = Util.formatTimeBySec(bf.getTrack().getLength(),false);
                //Comment
                oValues[iRow][8] = bf.getTrack().getStringValue(XML_TRACK_COMMENT);
                //Date discovery
                oValues[iRow][9] = bf.getTrack().getAdditionDate(); //show date using default local format and not technical representation
                //Order
                oValues[iRow][10] = bf.getTrack().getOrder(); 
                //Device name
                oValues[iRow][11] = bf.getDevice().getName(); 
                //directory name
                oValues[iRow][12] = bf.getDirectory().getName(); 
                //file name
                oValues[iRow][13] = bf.getName(); 
                //Hits
                oValues[iRow][14] = bf.getTrack().getHits();
                //Custom properties now
                //for tracks
                Iterator it2 = TrackManager.getInstance().getCustomProperties().iterator();
                for (int i=0;it2.hasNext();i++){
                    PropertyMetaInformation meta = (PropertyMetaInformation)it2.next();
                    LinkedHashMap properties = bf.getTrack().getProperties();
                    Object o = properties.get(meta.getName());
                    if (o != null){
                        oValues[iRow][iNumberStandardCols+i] = o;    
                    }
                    else{
                        oValues[iRow][iNumberStandardCols+i] = meta.getDefaultValue();
                    }
                }  
                //for files
                it2 = FileManager.getInstance().getCustomProperties().iterator();
                //note that index lust start at custom track properties size
                for (int i=TrackManager.getInstance().getCustomProperties().size();it2.hasNext();i++){
                    PropertyMetaInformation meta = (PropertyMetaInformation)it2.next();
                    LinkedHashMap properties = bf.getProperties();
                    Object o = properties.get(meta.getName());
                    if (o != null){
                        oValues[iRow][iNumberStandardCols+i] = o;    
                    }
                    else{
                        oValues[iRow][iNumberStandardCols+i] = meta.getDefaultValue();
                    }
                }  
            }
        }
    }
    
    /** 
     * Return item at given position
     * @param iRow
     * @return
     */
    public Item getItemAt(int iRow){
        StackItem item = getItem(iRow);
        return item.getFile();
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#display()
     */
    public void initUI(){
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
        model = new PlayListEditorTableModel();
        jtable = new JajukTable(model,CONF_PLAYLIST_EDITOR_COLUMNS);
        jtable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //multi-row selection
        jtable.setSortable(false);
        jtable.setDragEnabled(true);
        jtable.setTransferHandler(new PlaylistEditorTransferHandler(jtable));
        setRenderers();
        jtable.getColumnModel().getColumn(0).setPreferredWidth(20); //just an icon
        jtable.getColumnModel().getColumn(0).setMaxWidth(20);
        jtable.getTableHeader().setPreferredSize(new Dimension(0,20));
        jtable.addHighlighter(new ConditionalHighlighter(Color.ORANGE,Color.BLACK,-1,-1){
            
            @Override
            protected boolean test(ComponentAdapter adapter) {
                StackItem item = getItem(adapter.row);
                StackItem itemCurrent = FIFO.getInstance().getCurrentItem();
                if (itemCurrent != null  && itemCurrent.equals(item)){ //if it is the currently played track, change color
                    if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){ //for queue playlist, only highlight real current track
                        return (adapter.row == FIFO.getInstance().getIndex());
                    }
                    else{ //for others, we can't guess whish one to highlight if several times the same EVO
                        return true;
                    }
                }
                return false;
            }
            
            
        });
        jtable.addMouseListener(this);
        jtable.showColumns(jtable.getColumnsConf());
        jtable.getColumnModel().addColumnModelListener(this); //add this listener after hiding columns
        //selection listener to hide some buttons when selecting planned tracks
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
        jmiFileAddFavorites = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.25")); //$NON-NLS-1$
        jmiFileAddFavorites.addActionListener(this);
        jmiFileProperties = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.26")); //$NON-NLS-1$
        jmiFileProperties.addActionListener(this);
        jmenuFile.add(jmiFilePlay);
        jmenuFile.add(jmiFilePush);
        jmenuFile.add(jmiFileAddFavorites);
        jmenuFile.add(jmiFileProperties);
        //register events
        ObservationManager.register(this);
        //DND
        //force a refresh
        update(new Event(EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED,ObservationManager.getDetailsLastOccurence(EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED)));
        update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH)); //force first refresh
        //refresh columns if new property
    }
    
    public Set<EventSubject> getRegistrationKeys(){
        HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
        eventSubjectSet.add(EventSubject.EVENT_PLAYLIST_REFRESH);
        eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
        eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
        eventSubjectSet.add(EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED);
        eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_ADD);
        eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE);
        return eventSubjectSet;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getDesc()
     */
    public String getDesc() {
        return "AbstractPlaylistEditorView.15";	 //$NON-NLS-1$
    }
    
    private void columnChange(){
        if (!bReloading){ //ignore this column change when reloading model
            jtable.createColumnsConf();
        }
    }
    
    public void columnAdded(TableColumnModelEvent arg0) {
        columnChange();
    }
    
    public void columnRemoved(TableColumnModelEvent arg0) {
        columnChange();
    }
    
    
    public void columnMoved(TableColumnModelEvent arg0) {
    }
    
    public void columnMarginChanged(ChangeEvent arg0) {
    }
    
    public void columnSelectionChanged(ListSelectionEvent arg0) {
    }
    
    
    private void setRenderers(){
        //set right cell renderer for play and rate icons
        TableColumn col = jtable.getColumnModel().getColumn(0);
        col.setCellRenderer(new JajukCellRender());
        col = jtable.getColumnModel().getColumn(5); //rate
        col.setCellRenderer(new JajukCellRender());
        col.setMinWidth(RATE_COLUMN_SIZE);
        col.setMaxWidth(RATE_COLUMN_SIZE);
        col = jtable.getColumnModel().getColumn(0); //icon
        col.setMinWidth(PLAY_COLUMN_SIZE);
        col.setMaxWidth(PLAY_COLUMN_SIZE);
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(final Event event) {
        SwingUtilities.invokeLater(new Runnable() {
            public synchronized void run() { //NEED TO SYNC to avoid out out bound exceptions
                try{
                    EventSubject subject = event.getSubject();
                    bReloading = true; //flag reloading to avoid wrong column events
                    Object origin = ObservationManager.getDetail(event,DETAIL_ORIGIN);
                    //changed of playlist
                    if (EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED.equals(subject) && event.getDetails() != null){
                        //test mapping between editor and repository, to be refactored
                        if ((AbstractPlaylistEditorView.this instanceof PhysicalPlaylistEditorView && !(origin instanceof PhysicalPlaylistRepositoryView))
                                || (AbstractPlaylistEditorView.this instanceof LogicalPlaylistEditorView && !(origin instanceof LogicalPlaylistRepositoryView))){
                            return;
                        }
                        //clear planned
                        alPlanned = new ArrayList<StackItem>(0);  //make sure planned is voided if not in Queue
                        jtable.getSelectionModel().clearSelection(); //remove selection 
                        PlaylistFileItem plfi = (PlaylistFileItem)ObservationManager.getDetail(event,DETAIL_SELECTION);
                        AbstractPlaylistEditorView.this.iType = plfi.getType();
                        AbstractPlaylistEditorView.this.plfi =plfi;
                        //set title label
                        jlTitle.setText(plfi.getName());
                        jlTitle.setToolTipText(plfi.getName());
                        setDefaultButtonState();
                        update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH,ObservationManager.getDetailsLastOccurence(EventSubject.EVENT_PLAYLIST_REFRESH))); //force refresh
                        Util.stopWaiting(); //stop waiting
                    }
                    //current playlist has changed
                    else if ( EventSubject.EVENT_PLAYLIST_REFRESH.equals(subject) || EventSubject.EVENT_DEVICE_REFRESH.equals(subject)){
                        if ( plfi == null ){  //nothing ? leave
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
                            ((JajukTableModel)jtable.getModel()).populateModel();
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
                        jtable.packTable(5);
                    }
                    else if ( EventSubject.EVENT_PLAYER_STOP.equals(subject) 
                            && plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE ){
                        alItems.clear();
                        alPlanned.clear();
                        update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH)); //refresh playlist editor
                    }
                    else if (EventSubject.EVENT_CUSTOM_PROPERTIES_ADD.equals(subject)){
                        Properties properties = event.getDetails();
                        if (properties == null){ //can be null at view populate
                            return;
                        }
                        model = new PlayListEditorTableModel();//create a new model
                        jtable.setModel(model);
                        setRenderers();
                        jtable.addColumnIntoConf((String)properties.get(DETAIL_CONTENT));
                        jtable.showColumns(jtable.getColumnsConf());
                    }
                    else if (EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE.equals(subject)){
                        Properties properties = event.getDetails();
                        if (properties == null){ //can be null at view populate
                            return;
                        }
                        model = new PlayListEditorTableModel();//create a new model
                        jtable.setModel(model);
                        setRenderers();
                        //remove item from configuration cols
                        jtable.removeColumnFromConf((String)properties.get(DETAIL_CONTENT));
                        jtable.showColumns(jtable.getColumnsConf());
                    }
                }
                catch(Exception e){
                    Log.error(e);
                }
                finally{
                    bReloading = false; //make sure to remove this flag
                }
            }
        });
        
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
        else if ( e.getClickCount() == 1 ){
            int iSelectedRow = jtable.rowAtPoint(e.getPoint());
            //if no multiple previous selection, select row before displaying popup
            if (jtable.getSelectedRowCount() < 2){
                jtable.getSelectionModel().setSelectionInterval(iSelectedRow,iSelectedRow);
            }
            if (e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
                jmenuFile.show(jtable,e.getX(),e.getY());
            }
        }
    }
    
    /**
     * Return right stack item in normal or planned stacks
     * @param index
     * @return
     */
    StackItem getItem(int index){
        if (alItems.size() == 0){
            return null;
        }
        if (index < alItems.size()){
            return alItems.get(index);
        }
        else if (index < (alItems.size() + alPlanned.size() )){
            return alPlanned.get(index-alItems.size());
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
    private ArrayList<StackItem> getItemsFrom(int index){
        if (index < alItems.size()){
            return new ArrayList<StackItem>(alItems.subList(index,alItems.size()));
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
                        Playlist pl = PlaylistManager.getInstance().getPlaylistByID(plfi.getPlaylistFile().getHashcode());
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
                ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH)); //notify playlist repository to refresh
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
                        if (iRow < jtable.getModel().getRowCount() -1){
                            update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH,ObservationManager.getDetailsLastOccurence(EventSubject.EVENT_PLAYLIST_REFRESH))); //force immediate table refresh
                            jtable.getSelectionModel().setSelectionInterval(iRow+1,iRow+1);
                        }
                    }
                    else if ( ae.getSource() == jbUp){
                        plfi.getPlaylistFile().up(iRow);
                        if (iRow > 0){
                            update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH,ObservationManager.getDetailsLastOccurence(EventSubject.EVENT_PLAYLIST_REFRESH))); //force immediate table refresh
                            jtable.getSelectionModel().setSelectionInterval(iRow-1,iRow-1);
                        }
                    }
                }
            }
            else if (ae.getSource() == jbRemove){
                int[] iRows = jtable.getSelectedRows();
                if (iRows.length > 1){//if multiple selection, remove selection
                    jtable.getSelectionModel().removeIndexInterval(0,jtable.getRowCount()-1);
                }
                for (int i=0;i<iRows.length;i++){
                    plfi.getPlaylistFile().remove(iRows[i]-i); //don't forget that index changes when removing
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
                    jbRemove.setEnabled(true);
                }
                catch(JajukException je){
                    Messages.showErrorMessage(je.getCode());
                    Log.error(je);
                }
            }
            else if ( ae.getSource() == jmiFilePlay ||  ( ae.getSource() == jmiFilePush)){
                //computes selected items
                ArrayList<StackItem> alItemsToPlay = new ArrayList<StackItem>(jtable.getSelectedRowCount());
                int[] indexes = jtable.getSelectedRows();
                for (int i=0;i<indexes.length;i++){
                    alItemsToPlay.add(getItem(indexes[i]));
                }
                FIFO.getInstance().push(alItemsToPlay,ae.getSource() == jmiFilePush);
            }
            else if ( ae.getSource() == jmiFileAddFavorites ){
                //computes selected items
                ArrayList<StackItem> alItemsToPlay = new ArrayList<StackItem>(jtable.getSelectedRowCount());
                int[] indexes = jtable.getSelectedRows();
                for (int i=0;i<indexes.length;i++){
                    alItemsToPlay.add(getItem(indexes[i]));
                }                
                ArrayList<File> alFiles = new ArrayList<File>(alItemsToPlay.size());
                Iterator it = alItemsToPlay.iterator();
                while (it.hasNext()){
                    alFiles.add(((StackItem)it.next()).getFile());
                }
                Bookmarks.getInstance().addFiles(alFiles);
            }
            else if ( ae.getSource() == jmiFileProperties ){
                ArrayList<Item> alItems1 = new ArrayList<Item>(1); //file items
                ArrayList<Item> alItems2 = new ArrayList<Item>(1); //tracks items
                if (jtable.getSelectedRowCount() == 1){ //mono selection
                    File file = (File)model.getItemAt(
                        jtable.convertRowIndexToModel(jtable.getSelectedRow()));
                    //show file and associated track properties
                    alItems1.add(file);
                    alItems2.add(file.getTrack());
                }
                else{//multi selection
                    for (int i=0;i<=jtable.getRowCount();i++){
                        if (jtable.getSelectionModel().isSelectedIndex(i)){
                            File file = (File)model.getItemAt(
                                jtable.convertRowIndexToModel(i));
                            alItems1.add(file);
                            alItems2.add(file.getTrack());
                        }
                    }
                }
                new PropertiesWizard(alItems1,alItems2);
            }
        }
        catch(Exception e2){
            Log.error(e2);
        }
        finally{
            ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH)); //refresh playlist editor
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
    
    /**
     * Select the current playlist file item
     * @param plfi
     */
    public void setCurrentPlaylistFileItem(PlaylistFileItem plfi){
        this.plfi = plfi;
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
