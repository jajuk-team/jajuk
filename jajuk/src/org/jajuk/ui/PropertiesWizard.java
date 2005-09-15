/*
 *  Jajuk
 *  Copyright (C) 2005 bertrand florat
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

package org.jajuk.ui;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.Event;
import org.jajuk.base.FileManager;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.ShuttleSorter;
import org.jdesktop.swingx.decorator.Sorter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Item properties wizard for any jajuk item
 * 
 * @author Bertrand Florat
 * @created 6 juin 2005
 */
public class PropertiesWizard extends JDialog implements ITechnicalStrings,ActionListener {
    
    /**Close button*/
    JButton jbClose;
    
    /** Layout dimensions*/
    double[][] dSize = { { 0.99 }, { 0.99,20,20,20}};
        
	
	/**
	 * Constructor
	 * 
	 * @param pa the item to display
	 */
	public PropertiesWizard(IPropertyable pa) {
	    super(Main.getWindow(), pa.getStringValue(XML_NAME),true); //modal
	    setLayout(new TableLayout(dSize));
	    getContentPane().add(new PropertiesPanel(new PropertiesTableModel(pa),pa.getDesc()),"0,0"); 
        display();
   }
	
	/**
	 * Constructor for normal wizard with only one wizard panel and n items to display 
	 * 
	 * @param alItems items to display
    */
	public PropertiesWizard(ArrayList<IPropertyable> alItems) {
        //windows title: name of the element of only one item, or "selection" word otherwise
		super(Main.getWindow(),alItems.size()==1 ? ((IPropertyable)alItems.get(0)).getDesc():Messages.getString("PropertiesWizard.6"),true); //modal
        setLayout(new TableLayout(dSize));
        getContentPane().add(new PropertiesPanel(new PropertiesTableModel(alItems),Messages.getString("PropertiesWizard.6")),"0,0");    
        display();
    }
    
    
    /**
     * Constructor for file wizard for ie with 2 wizard panels and n items to display 
     * 
     * @param alItems1 items to display in the first wizard panel (file for ie)
     * @param alItems2 items to display in the second panel (associated track for ie )
    */
    public PropertiesWizard(ArrayList<IPropertyable> alItems1,ArrayList<IPropertyable> alItems2) {
        //windows title: name of the element of only one item, or "selection" word otherwise
        super(Main.getWindow(),alItems1.size()==1 ? ((IPropertyable)alItems1.get(0)).getDesc():Messages.getString("PropertiesWizard.6"),true); //modal
        setLayout(new TableLayout(dSize));
        JPanel jpProperties = new JPanel();
        jpProperties.setLayout(new BoxLayout(jpProperties,BoxLayout.Y_AXIS));
        if (alItems1.size() == 1){
            jpProperties.add(new PropertiesPanel(new PropertiesTableModel(alItems1.get(0)),alItems1.get(0).getDesc()));    
            jpProperties.add(Box.createVerticalStrut(20));
            jpProperties.add(new PropertiesPanel(new PropertiesTableModel(alItems2.get(0)),alItems2.get(0).getDesc()));    
        }
        else{
            jpProperties.add(new PropertiesPanel(new PropertiesTableModel(alItems1),Util.formatPropertyDesc(Messages.getString("Property_files"))));    
            jpProperties.add(Box.createVerticalStrut(20));
            jpProperties.add(new PropertiesPanel(new PropertiesTableModel(alItems2),Util.formatPropertyDesc(Messages.getString("Property_tracks"))));
        }
        getContentPane().add(jpProperties,"0,0");
        display();
    }

    private void display(){
        //Close button
        jbClose = new JButton(Messages.getString("Close"));
        jbClose.addActionListener(this);
        getContentPane().add(jbClose,"0,2");
        pack();
        Util.setCenteredLocation(this);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jbClose){
            dispose();
        }
    }
    
	/**
	 * 
	 * A properties panel
	 * @author Bertrand Florat
	 *
	 */
	class PropertiesPanel extends JPanel implements TableModelListener {
		        
		/**table*/
        JPanel jpTable;
		
		/**Item description*/
		JLabel jlDesc;
		
    	/** Properties table */
		JajukTable jtable;
		
		/**Model*/
		PropertiesTableModel model;
        
                        
		/**
		 * Property panel
		 * @param model properties model
         * @param sDesc Description (title)
         */
		PropertiesPanel(PropertiesTableModel model,String sDesc) {
		    this.model = model;
			int iX_SEPARATOR = 5;
			int iY_SEPARATOR = 10;
			int iY_ROW_HEIGHT = 20;
			//desc
			jlDesc = new JLabel(Util.formatPropertyDesc(sDesc));
			//add panels
			jtable = new JajukTable(model);
			//listen for table changes
			model.addTableModelListener(this);
			double[][] dSize = { { 0.99 }, { 20, iY_SEPARATOR, iY_ROW_HEIGHT*(model.getRowCount()+1)} };
			setLayout(new TableLayout(dSize));
			Sorter sorter = new ShuttleSorter(0, true);
			FilterPipeline pipe = new FilterPipeline(new Filter[] { sorter });
			jtable.setFilters(pipe);
			jtable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					if (jtable.convertColumnIndexToModel(jtable.getSelectedColumn()) == 4) {
						int iRow = jtable.convertRowIndexToModel(jtable.getSelectedRow());
						PropertiesTableModel model = (PropertiesTableModel) jtable.getModel();
						if (model.isLinkable(iRow)) {
							//display given property wizard. files properties in track have to display one window by file 
							PropertyMetaInformation meta = (PropertyMetaInformation) model.getValueAt(iRow,5);
                            String sProperty = meta.getName();
							String sValue = (String) model.getValueAt(iRow, 6);
							if (XML_FILES.equals(sProperty)) {
								StringTokenizer st = new StringTokenizer(sValue, ",");
								while (st.hasMoreTokens()) {
									String sFile = st.nextToken();
									IPropertyable pa = FileManager.getInstance().getItem(sFile);
									if (pa != null) {
										new PropertiesWizard(pa); //show properties window for this item
									}
								}
							} else {
								IPropertyable pa = ItemManager.getItemManager(sProperty).getItem(sValue);
								if (pa != null) {
									new PropertiesWizard(pa); //show properties window for this item
								}
							}
						}
					}
				}
			});
			jtable.setRowHeight(iY_ROW_HEIGHT);
		    add(jlDesc, "0,0");
        	add(new JScrollPane(jtable), "0,2");
			//Show all album columns only for single track panel
			if (!(model.getPa() instanceof Track) || model.isMultiple()) {
				TableColumnExt col = jtable.getColumnExt(3);
				col.setVisible(false);
			}
            if (model.isMultiple()){//if multiple selection, hide links and editable
               jtable.getColumnExt(0).setVisible(false);
               jtable.getColumnExt(2).setVisible(false); //after hiding previous columns, links col index is now 2
            }
			jtable.packAll();
   	}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
		 */
		public void tableChanged(TableModelEvent e) {
		    PropertyMetaInformation meta = (PropertyMetaInformation)jtable.getModel().getValueAt(e.getFirstRow(),5);
            String sKey = meta.getName();
		    String sValue = (String)jtable.getModel().getValueAt(e.getFirstRow(),2);
		    if (e.getColumn() == 2){
		        if (model.isMultiple()){ 
                    /*multiple items case, in this case, we just change a non-constructor attribute on the same item
                    because in multiple mode, we cannot change constructor methods*/      
                   for (IPropertyable pa : model.getItems()){
                       ItemManager.changeItem(pa,sKey,sValue);
                   }
		        }
                else{ //single item case, in this case, we can change constructor attributes so we can overwrite current item by a new one
                    //Is full album option is set for current line ?
                    boolean bFullAlbum = (Boolean)model.getValueAt(e.getFirstRow(),3);
                    //Apply to full album if selected (do it first because after, current item could be changed and checkbox reseted)
                    if (model.getPa() instanceof Track && bFullAlbum){
                        Track track = (Track)model.getPa(); 
                        Album album = track.getAlbum();
                        ArrayList<Track> alTracksToChange = album.getTracks();
                        alTracksToChange.remove(model.getPa()); //we treat the current item separetly
                        //now change property for each matching item
                        for (Track trackToChange:alTracksToChange){
                            ItemManager.changeItem(trackToChange,sKey,sValue);
                        }
                    }
                    //then change current item
                    IPropertyable newItem = ItemManager.changeItem(model.getPa(),sKey,sValue);
                    if (!newItem.equals(model.getPa())){ //check if item has change, if so, change current item 
                        this.model = new PropertiesTableModel(newItem);
                        //reset current full album values
                        this.model.setValueAt(bFullAlbum,e.getFirstRow(),3); 
                        jtable.setModel(this.model);
                        jtable.packAll();
                        this.model.addTableModelListener(this);
                        jlDesc.setText(model.getPa().getDesc());
                    }
                }
		        //UI refresh
		        ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH)); //TBI see later for a smarter event
		        
			}
        }
	}
}
