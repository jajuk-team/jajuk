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
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukTable;
import org.jajuk.ui.JajukTableModel;
import org.jajuk.ui.TableTransferHandler;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

import ext.SwingWorker;

/**
 * Abstract table view : common implementation for both physical and logical table views 
 * 
 * @author Bertrand Florat 
 * @created 13 dec. 2003
 */
public abstract class AbstractTableView extends ViewAdapter implements ActionListener,MouseListener,ItemListener,TableColumnModelListener{
    
    /** The logical table */
    JajukTable jtable;
    JPanel jpControl;
    JLabel jlFilter;
    JComboBox jcbProperty; 
    JLabel jlEquals;
    JTextField jtfValue;
    JButton jbApplyFilter;
    JButton jbClearFilter;
    JButton jbAdvancedFilter;
    
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
        // launches a thread used to perform dynamic filtering chen user is typing
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
                jlFilter = new JLabel(Messages.getString("AbstractTableView.0")); //$NON-NLS-1$
                //properties combo box, fill with colums names expect ID
                jcbProperty = new JComboBox();
                jcbProperty.addItem(Messages.getString("AbstractTableView.8")); //"any" criteria
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
                jbApplyFilter = new JButton(Util.getIcon(ICON_APPLY_FILTER));
                jbApplyFilter.addActionListener(AbstractTableView.this);
                jbClearFilter = new JButton(Util.getIcon(ICON_CLEAR_FILTER));
                jbClearFilter.addActionListener(AbstractTableView.this);
                jbAdvancedFilter = new JButton(Util.getIcon(ICON_ADVANCED_FILTER));
                jbAdvancedFilter.addActionListener(AbstractTableView.this);
                jbApplyFilter.setToolTipText(Messages.getString("AbstractTableView.4")); //$NON-NLS-1$
                jbClearFilter.setToolTipText(Messages.getString("AbstractTableView.5")); //$NON-NLS-1$
                jbAdvancedFilter.setToolTipText(Messages.getString("AbstractTableView.6")); //$NON-NLS-1$
                jbAdvancedFilter.setEnabled(false);  //TBI
                int iXspace = 5;
                double sizeControl[][] =
                {{iXspace,TableLayout.FILL,iXspace,0.3,TableLayout.FILL,TableLayout.FILL,iXspace,0.3,iXspace,20,iXspace,20,iXspace,20,iXspace},
                        {22}};
                jpControl.setLayout(new TableLayout(sizeControl));
                jpControl.add(jlFilter,"1,0"); //$NON-NLS-1$
                jpControl.add(jcbProperty,"3,0"); //$NON-NLS-1$
                jpControl.add(jlEquals,"5,0"); //$NON-NLS-1$
                jpControl.add(jtfValue,"7,0"); //$NON-NLS-1$
                jpControl.add(jbApplyFilter,"9,0"); //$NON-NLS-1$
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
                add(new JScrollPane(jtable),"0,1"); //$NON-NLS-1$
                new TableTransferHandler(jtable, DnDConstants.ACTION_COPY_OR_MOVE);
                jtable.addMouseListener(AbstractTableView.this);
                hideColumns();
                applyFilter(null,null);
                //Register on the list for subject we are interrested in
                ObservationManager.register(EVENT_DEVICE_MOUNT,AbstractTableView.this);
                ObservationManager.register(EVENT_DEVICE_UNMOUNT,AbstractTableView.this);
                ObservationManager.register(EVENT_DEVICE_REFRESH,AbstractTableView.this);
                ObservationManager.register(EVENT_SYNC_TREE_TABLE,AbstractTableView.this);
                ObservationManager.register(EVENT_CUSTOM_PROPERTIES_ADD,AbstractTableView.this);
                ObservationManager.register(EVENT_CUSTOM_PROPERTIES_REMOVE,AbstractTableView.this);
                //refresh columns conf in case of some attributes been removed or added before view instanciation
                Properties properties = ObservationManager.getDetailsLastOccurence(EVENT_CUSTOM_PROPERTIES_ADD); 
                Event event = new Event(EVENT_CUSTOM_PROPERTIES_ADD,properties);
                update(event);
            }
        };
        sw.start();
    }	
    
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
        //not in a thread because it is always called inside a thread created from sub-classes
        if ( e.getSource() == jbApplyFilter){
            this.sAppliedFilter = jtfValue.getText();
            this.sAppliedCriteria =getApplyCriteria();
            applyFilter(sAppliedCriteria,sAppliedFilter);
        }
        else if(e.getSource() == jbClearFilter){ //remove all filters
            jtfValue.setText(""); //clear value textfield //$NON-NLS-1$
            this.sAppliedFilter = null;
            this.sAppliedCriteria = null;
            applyFilter(sAppliedCriteria,sAppliedFilter);
        }
    }
    
    
    /**
     * Apply a filter, to be implemented by physical and logical tables, alter the model
     */
    public void applyFilter(String sPropertyName,String sPropertyValue) {
       model.populateModel(sPropertyName,sPropertyValue);
       model.fireTableDataChanged();
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(final Event event) {
        SwingUtilities. invokeLater(new Runnable() {
            public void run() {
                try{
                    bReloading = true; //flag reloading to avoid wrong column events
                    String subject = event.getSubject();
                    if ( EVENT_DEVICE_MOUNT.equals(subject) 
                            || EVENT_DEVICE_UNMOUNT.equals(subject) 
                            || EVENT_DEVICE_REFRESH.equals(subject)  
                            || EVENT_SYNC_TREE_TABLE.equals(subject)) {
                        applyFilter(sAppliedCriteria,sAppliedFilter); //force filter to refresh
                    }	
                    else if (EVENT_CUSTOM_PROPERTIES_ADD.equals(subject)){
                        Properties properties = event.getDetails();
                        if (properties == null){ //can be null at view populate
                        	return;
                        }
                        model = populateTable();//create a new model
                        jtable.setModel(model);
                        //add new item in configuration cols
                        if (AbstractTableView.this instanceof PhysicalTableView){
                            String sTableCols = ConfigurationManager.getProperty(CONF_PHYSICAL_TABLE_COLUMNS);
                            ConfigurationManager.setProperty(CONF_PHYSICAL_TABLE_COLUMNS,sTableCols+","+(model.getIdentifier(model.getColumnCount()-1)));    
                        }
                        else {
                            String sTableCols = ConfigurationManager.getProperty(CONF_LOGICAL_TABLE_COLUMNS);
                            ConfigurationManager.setProperty(CONF_LOGICAL_TABLE_COLUMNS,sTableCols+","+(model.getIdentifier(model.getColumnCount()-1)));
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
                        //store old conf
                        ArrayList al = getColumnsConf();
                        al.remove(properties.get(DETAIL_CONTENT));
                        model = populateTable();//create a new model
                        jtable.setModel(model);
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
    
    
    /**
     * Hide needed columns
     *
     */
    private void hideColumns(){
        //display columns
        ArrayList al = getColumnsConf(); 
        Iterator it = ((DefaultTableColumnModelExt)jtable.getColumnModel()).getAllColumns().iterator();
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
        Iterator it = ((DefaultTableColumnModelExt)jtable.getColumnModel()).getAllColumns().iterator();
        while (it.hasNext()){
            TableColumnExt col = (TableColumnExt)it.next();
            if (col.isVisible()){
                sb.append(model.getIdentifier(col.getModelIndex())+",");    
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
            sb.append((String)it.next()+",");
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
        StringTokenizer st = new StringTokenizer(sConf,",");
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
    
    
    
}
