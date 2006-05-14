/*
 * Jajuk Copyright (C) 2003 Bertrand Florat
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

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jajuk.base.AuthorManager;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.StyleManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukCellRender;
import org.jajuk.ui.JajukTable;
import org.jajuk.ui.JajukTableModel;
import org.jajuk.ui.JajukToggleButton;
import org.jajuk.ui.TableTransferHandler;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

import ext.SwingWorker;

/**
 * Abstract table view : common implementation for both physical and logical table views 
 * 
 * @author Bertrand Florat 
 * @created 13 dec. 2003
 */
public abstract class AbstractTableView extends ViewAdapter
    implements ActionListener,MouseListener,ItemListener,
        TableColumnModelListener,TableModelListener,ITechnicalStrings{
    
    /** The logical table */
    JajukTable jtable;
    JPanel jpControl;
    JajukToggleButton jtbEditable;
    JLabel jlFilter;
    JComboBox jcbProperty; 
    JLabel jlEquals;
    JTextField jtfValue;
    JButton jbClearFilter;
    JButton jbAdvancedFilter;
    JMenuItem jmiProperties;
    
    /**Table model*/
    JajukTableModel model;
    
    /** Currently applied filter*/
    String sAppliedFilter = ""; //$NON-NLS-1$
    
    /** Currently applied criteria*/
    String sAppliedCriteria;
    
    /**Do search panel need a search*/
    private boolean bNeedSearch = false;
    
    /**Default time in ms before launching a search automaticaly*/
    private static final int WAIT_TIME = 300;
    
    /**Date last key pressed*/
    private long lDateTyped;
    
    /**Model refreshing flag*/
    boolean bReloading = false;
    
    /** Constructor */
    public AbstractTableView(){
        // launches a thread used to perform dynamic filtering when user is typing
        new Thread(){
            public void run(){
                while (true){
                    try{
                        Thread.sleep(100);
                    }
                    catch(InterruptedException ie){
                        Log.error(ie);
                    }
                    if ( bNeedSearch && (System.currentTimeMillis()-lDateTyped >= WAIT_TIME)){
                        sAppliedFilter = jtfValue.getText();
                        sAppliedCriteria = getApplyCriteria();
                        applyFilter(sAppliedCriteria,sAppliedFilter);
                        bNeedSearch = false;
                    }
                }
            }
        }.start();
    }
    
    /**
     * 
     * @return Applied criteria
     */
    private String getApplyCriteria(){
        int indexCombo = jcbProperty.getSelectedIndex();
        if (indexCombo == 0){ //first criteria is special: any
            sAppliedCriteria = XML_ANY;    
        }
        else{ //otherwise, take criteria from model
            sAppliedCriteria = model.getIdentifier(indexCombo);
        }
        return sAppliedCriteria;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#display()
     */
    public void populate() {
        SwingWorker sw = new SwingWorker() {
            public Object construct() {
                model = populateTable();
                return null;
            }   
            public void finished() {
                //Control panel
                jpControl = new JPanel();
                jpControl.setBorder(BorderFactory.createEtchedBorder());
                jtbEditable = new JajukToggleButton(Util.getIcon(ICON_EDIT));
                jtbEditable.setToolTipText(Messages.getString("AbstractTableView.11"));
                jtbEditable.addActionListener(AbstractTableView.this);
                jlFilter = new JLabel(Messages.getString("AbstractTableView.0")); //$NON-NLS-1$
                //properties combo box, fill with colums names expect ID
                jcbProperty = new JComboBox();
                jcbProperty.addItem(Messages.getString("AbstractTableView.8")); //"any" criteria //$NON-NLS-1$
                for (int i=1;i<model.getColumnCount();i++){//Others columns except ID
                    jcbProperty.addItem(model.getColumnName(i));    
                }
                jcbProperty.setToolTipText(Messages.getString("AbstractTableView.1")); //$NON-NLS-1$
                jcbProperty.addItemListener(AbstractTableView.this);
                jlEquals = new JLabel(Messages.getString("AbstractTableView.7")); //$NON-NLS-1$
                jtfValue = new JTextField();
                jtfValue.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        bNeedSearch = true;
                        lDateTyped = System.currentTimeMillis();  
                    }
                });
                jtfValue.setToolTipText(Messages.getString("AbstractTableView.3")); //$NON-NLS-1$
                //buttons
                jbClearFilter = new JButton(Util.getIcon(ICON_CLEAR_FILTER));
                jbClearFilter.addActionListener(AbstractTableView.this);
                jbAdvancedFilter = new JButton(Util.getIcon(ICON_ADVANCED_FILTER));
                jbAdvancedFilter.addActionListener(AbstractTableView.this);
                jbClearFilter.setToolTipText(Messages.getString("AbstractTableView.5")); //$NON-NLS-1$
                jbAdvancedFilter.setToolTipText(Messages.getString("AbstractTableView.6")); //$NON-NLS-1$
                jbAdvancedFilter.setEnabled(false);  //TBI
                int iXspace = 5;
                double sizeControl[][] =
                {{iXspace,20,3*iXspace,TableLayout.FILL,iXspace,0.3,TableLayout.FILL,TableLayout.FILL,iXspace,0.3,iXspace,20,iXspace,20,iXspace},
                        {22}};
                jpControl.setLayout(new TableLayout(sizeControl));
                jpControl.add(jtbEditable,"1,0"); //$NON-NLS-1$
                jpControl.add(jlFilter,"3,0"); //$NON-NLS-1$
                jpControl.add(jcbProperty,"5,0"); //$NON-NLS-1$
                jpControl.add(jlEquals,"7,0"); //$NON-NLS-1$
                jpControl.add(jtfValue,"9,0"); //$NON-NLS-1$
                jpControl.add(jbClearFilter,"11,0"); //$NON-NLS-1$
                jpControl.add(jbAdvancedFilter,"13,0"); //$NON-NLS-1$
                jpControl.setMinimumSize(new Dimension(0,0)); //allow resing with info node
                //add 
                double size[][] =
                {{0.99},
                        {30,0.99}};
                setLayout(new TableLayout(size));
                add(jpControl,"0,0"); //$NON-NLS-1$
                jtable = new JajukTable(model,true);
                jtable.getColumnModel().addColumnModelListener(AbstractTableView.this);
                setRenderers();
                add(new JScrollPane(jtable),"0,1"); //$NON-NLS-1$
                new TableTransferHandler(jtable, DnDConstants.ACTION_COPY_OR_MOVE);
                jtable.addMouseListener(AbstractTableView.this);
                hideColumns();
                applyFilter(null,null);
                jtable.packAll();
                //Register on the list for subject we are interrested in
                ObservationManager.register(EVENT_DEVICE_MOUNT,AbstractTableView.this);
                ObservationManager.register(EVENT_DEVICE_UNMOUNT,AbstractTableView.this);
                ObservationManager.register(EVENT_DEVICE_REFRESH,AbstractTableView.this);
                ObservationManager.register(EVENT_SYNC_TREE_TABLE,AbstractTableView.this);
                ObservationManager.register(EVENT_CUSTOM_PROPERTIES_ADD,AbstractTableView.this);
                ObservationManager.register(EVENT_CUSTOM_PROPERTIES_REMOVE,AbstractTableView.this);
                ObservationManager.register(EVENT_RATE_CHANGED,AbstractTableView.this);
                ObservationManager.register(EVENT_TABLE_CLEAR_SELECTION,AbstractTableView.this);
                //refresh columns conf in case of some attributes been removed or added before view instanciation
                Properties properties = ObservationManager.getDetailsLastOccurence(EVENT_CUSTOM_PROPERTIES_ADD); 
                Event event = new Event(EVENT_CUSTOM_PROPERTIES_ADD,properties);
                update(event);
                initTable(); //perform type-specific init
             }
        };
        sw.start();
    }	
    
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
        //not in a thread because it is always called inside a thread created from sub-classes
        if(e.getSource() == jbClearFilter){ //remove all filters
            jtfValue.setText(""); //clear value textfield //$NON-NLS-1$
            this.sAppliedFilter = null;
            this.sAppliedCriteria = null;
            applyFilter(sAppliedCriteria,sAppliedFilter);
        }
        else if (e.getSource() == jbAdvancedFilter){
            //TBI
        }
        else{ //others events will be treated by child classes
            othersActionPerformed(e);
        }
    }
    
    /**
     * Apply a filter, to be implemented by physical and logical tables, alter the model
     */
    public void applyFilter(String sPropertyName,String sPropertyValue) {
       model.removeTableModelListener(AbstractTableView.this);
       model.populateModel(sPropertyName,sPropertyValue);
       model.fireTableDataChanged();
       model.addTableModelListener(AbstractTableView.this);
    }
    
    
    /**
     * Child actions
     * @param ae
     */
    abstract void othersActionPerformed(ActionEvent ae);
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(final Event event) {
        SwingUtilities. invokeLater(new Runnable() {
            public void run() {
                try{
                    bReloading = true; //flag reloading to avoid wrong column events
                    String subject = event.getSubject();
                    if ( EVENT_TABLE_CLEAR_SELECTION.equals(subject)){ 
                        jtable.clearSelection();
                    }
                    if ( EVENT_DEVICE_MOUNT.equals(subject) 
                            || EVENT_DEVICE_UNMOUNT.equals(subject) 
                            || EVENT_SYNC_TREE_TABLE.equals(subject)) {
                        jtable.clearSelection();
                        applyFilter(sAppliedCriteria,sAppliedFilter); //force filter to refresh
                    }	
                    else if ( EVENT_DEVICE_REFRESH.equals(subject)
                            || EVENT_RATE_CHANGED.equals(subject)) {
                        applyFilter(sAppliedCriteria,sAppliedFilter); //force filter to refresh
                    }   
                    else if (EVENT_CUSTOM_PROPERTIES_ADD.equals(subject)){
                        Properties properties = event.getDetails();
                        if (properties == null){ //can be null at view populate
                        	return;
                        }
                        model = populateTable();//create a new model
                        jtable.setModel(model);
                        setRenderers();
                        //add new item in configuration cols
                        if (AbstractTableView.this instanceof PhysicalTableView){
                            String sTableCols = ConfigurationManager.getProperty(CONF_PHYSICAL_TABLE_COLUMNS);
                            ConfigurationManager.setProperty(CONF_PHYSICAL_TABLE_COLUMNS,sTableCols+","+(model.getIdentifier(model.getColumnCount()-1)));     //$NON-NLS-1$
                        }
                        else {
                            String sTableCols = ConfigurationManager.getProperty(CONF_LOGICAL_TABLE_COLUMNS);
                            ConfigurationManager.setProperty(CONF_LOGICAL_TABLE_COLUMNS,sTableCols+","+(model.getIdentifier(model.getColumnCount()-1))); //$NON-NLS-1$
                        }
                        hideColumns();
                        applyFilter(sAppliedCriteria,sAppliedFilter);
                        jcbProperty.addItem(properties.get(DETAIL_CONTENT));
                    }
                    else if (EVENT_CUSTOM_PROPERTIES_REMOVE.equals(subject)){
                        Properties properties = event.getDetails();
                        if (properties == null){ //can be null at view populate
                        	return;
                        }
                        ArrayList al = getColumnsConf();
                        al.remove(properties.get(DETAIL_CONTENT));
                        model = populateTable();//create a new model
                        jtable.setModel(model);
                        setRenderers();
                        //remove item from configuration cols
                        if (AbstractTableView.this instanceof PhysicalTableView){
                            ConfigurationManager.setProperty(CONF_PHYSICAL_TABLE_COLUMNS,getColumnsConf(al));    
                        }
                        else {
                            ConfigurationManager.setProperty(CONF_LOGICAL_TABLE_COLUMNS,getColumnsConf(al));
                        }
                        hideColumns();
                        applyFilter(sAppliedCriteria,sAppliedFilter);
                        jcbProperty.removeItem(properties.get(DETAIL_CONTENT));
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
    
    /**Fill the table */
    abstract JajukTableModel populateTable();    
    
    
    private void setRenderers(){
        StringBuffer sb = new StringBuffer();
        Iterator it = ((DefaultTableColumnModelExt)jtable.getColumnModel()).getColumns(true).iterator();
        while (it.hasNext()){
            TableColumnExt col = (TableColumnExt)it.next();
            String sIdentifier = model.getIdentifier(col.getModelIndex());
            //create a combo box for styles, note that we can't add new styles dynamically
            if (XML_STYLE.equals(sIdentifier)){
                JComboBox jcb = new JComboBox(StyleManager.getInstance().getStylesList());
                jcb.setEditable(true);
                AutoCompleteDecorator.decorate(jcb);
                col.setCellEditor(new ComboBoxCellEditor(jcb));
            }
            //create a combo box for authors, note that we can't add new authors dynamically
            if (XML_AUTHOR.equals(sIdentifier)){
                JComboBox jcb = new JComboBox(AuthorManager.getAuthorsList());
                jcb.setEditable(true);
                AutoCompleteDecorator.decorate(jcb);
                col.setCellEditor(new ComboBoxCellEditor(jcb));
            }
            //create a button for playing
            else if (XML_PLAY.equals(sIdentifier)){
                col.setCellRenderer(new JajukCellRender());
            }
            else if (XML_TRACK_RATE.equals(sIdentifier)){
                col.setCellRenderer(new JajukCellRender());
                col.setMinWidth(67);
            }
        }
    }
    
    /**
     * Hide needed columns
     *
     */
    private void hideColumns(){
        //display columns
        ArrayList al = getColumnsConf(); 
        Iterator it = ((DefaultTableColumnModelExt)jtable.getColumnModel()).getColumns(false).iterator();
        while (it.hasNext()){
            TableColumnExt col = (TableColumnExt)it.next();
            if (!al.contains(model.getIdentifier(col.getModelIndex()))){
                col.setVisible(false);
            }
        }
    }
    
    /**
     * Detect property change
     */
    public void itemStateChanged(ItemEvent ie){
        if (ie.getSource() == jcbProperty){
            sAppliedFilter = jtfValue.getText();
            sAppliedCriteria = getApplyCriteria();
            applyFilter(sAppliedCriteria,sAppliedFilter);    
        }
        
    }
    
    /**
     * 
     * @return columns configuration
     * 
     */
    public String createColumnsConf(){
        StringBuffer sb = new StringBuffer();
        Iterator it = ((DefaultTableColumnModelExt)jtable.getColumnModel()).getColumns(true).iterator();
        while (it.hasNext()){
            TableColumnExt col = (TableColumnExt)it.next();
            String sIdentifier = model.getIdentifier(col.getModelIndex());
            if (col.isVisible()){
                sb.append(sIdentifier+",");     //$NON-NLS-1$
            }
        }
        //remove last coma
        if (sb.length()>0){
            return sb.substring(0,sb.length()-1);
        }
        else{
            return sb.toString();    
        }
    }
    
    /**
     * 
     * @return columns configuration from given list of columns identifiers
     * 
     */
    public String getColumnsConf(ArrayList alCol){
        StringBuffer sb = new StringBuffer();
        Iterator it = alCol.iterator();
        while (it.hasNext()){
            sb.append((String)it.next()+","); //$NON-NLS-1$
        }
        //remove last coma
        if (sb.length()>0){
            return sb.substring(0,sb.length()-1);
        }
        else{
            return sb.toString();    
        }
    }
    
    /**
     * 
     * @return list of visible columns names as string
     */
    public ArrayList getColumnsConf(){
        ArrayList alOut = new ArrayList(10);
        String sConf;
        if (this instanceof PhysicalTableView){
            sConf = ConfigurationManager.getProperty(CONF_PHYSICAL_TABLE_COLUMNS);
        }
        else {
            sConf = ConfigurationManager.getProperty(CONF_LOGICAL_TABLE_COLUMNS);
        }
        StringTokenizer st = new StringTokenizer(sConf,","); //$NON-NLS-1$
        while (st.hasMoreTokens()){
            alOut.add(st.nextToken());
        }
        return alOut;
    }
            
    
    private void columnChange(){
        if (!bReloading){ //ignore this column change when reloading model
            if (this instanceof PhysicalTableView){
                ConfigurationManager.setProperty(CONF_PHYSICAL_TABLE_COLUMNS,createColumnsConf());
            }
            else{
                ConfigurationManager.setProperty(CONF_LOGICAL_TABLE_COLUMNS,createColumnsConf());
            }
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
    
    
    
    /* (non-Javadoc)
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(TableModelEvent e) {
        String sKey = model.getIdentifier(e.getColumn());
        Object oValue = model.getValueAt(e.getFirstRow(),e.getColumn());//can be Boolean or String
        IPropertyable item = model.getItemAt(e.getFirstRow());
        try{
            //file filter used by physical table view to change only the file, not all files associated with the track
            HashSet filter = null;
            if (item instanceof File){
              filter = new HashSet();
              filter.add(item);
            }
            IPropertyable itemNew = ItemManager.changeItem(item,sKey,oValue,filter);
            model.setItemAt(e.getFirstRow(),itemNew); //update model
            //user message
            PropertyMetaInformation meta = itemNew.getMeta(sKey);
            InformationJPanel.getInstance().setMessage(
                    Messages.getString("PropertiesWizard.8")+": "+ItemManager.getHumanType(sKey), //$NON-NLS-1$ //$NON-NLS-2$
                    InformationJPanel.INFORMATIVE);
                ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH)); //TBI see later for a smarter event
        }
        catch(NoneAccessibleFileException none){
            Messages.showErrorMessage(none.getCode());
            ((JajukTableModel)jtable.getModel()).undo(e.getFirstRow(),e.getColumn());
        }
        catch(CannotRenameException cre){
            Messages.showErrorMessage(cre.getCode()); //$NON-NLS-1$
            ((JajukTableModel)jtable.getModel()).undo(e.getFirstRow(),e.getColumn());
        }
        catch(JajukException je){
            Log.error("104",je); //$NON-NLS-1$
            Messages.showErrorMessage("104",je.getMessage()); //$NON-NLS-1$
            ((JajukTableModel)jtable.getModel()).undo(e.getFirstRow(),e.getColumn());
        }
    }
    
    /**
     * Table initialization after table display
     *
     */
    abstract void initTable();
    
   
}
