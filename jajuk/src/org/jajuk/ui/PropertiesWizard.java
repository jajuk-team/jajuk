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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jajuk.base.Event;
import org.jajuk.base.FileManager;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Track;
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
public class PropertiesWizard extends JFrame implements ITechnicalStrings {

	
	/**
	 * Constructor
	 * 
	 * @param pa  the item to display
	 */
	public PropertiesWizard(IPropertyable pa) {
	    super(pa.getValue(XML_NAME));
		setIconImage(Util.getIcon(ICON_LOGO).getImage());
		getContentPane().add(new PropertiesPanel(pa));
		Util.setShuffleLocation(this, 400, 400);
		pack();
		setVisible(true);
	}
	
	/**
	 * Constructor
	 * 
	 * @param pa  the item to display
	 */
	public PropertiesWizard(ArrayList alProperties) {
		//use first item for title
		super(((IPropertyable)alProperties.get(0)).getValue(XML_NAME));
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		setIconImage(Util.getIcon(ICON_LOGO).getImage());
		Iterator it = alProperties.iterator();
		while (it.hasNext()){
			getContentPane().add(new PropertiesPanel((IPropertyable)it.next()));	
			getContentPane().add(Box.createHorizontalStrut(30));
		}
		Util.setShuffleLocation(this, 400, 400);
		pack();
		setVisible(true);
	}

	/**
	 * 
	 * A properties panel
	 * @author Bertrand Florat
	 *
	 */
	class PropertiesPanel extends JPanel implements TableModelListener {
		/** Item to show */
		IPropertyable pa;
		
		JPanel jpTable;
		
		/**Item description*/
		JLabel jlDesc;
		
		/** Properties table */
		JajukTable jtable;
		
		/**Model*/
		PropertiesTableModel model;
		
		/**
		 * Property panel
		 * @param pa
		 */
		PropertiesPanel(IPropertyable pa) {
			this.pa = pa;
			int iX_SEPARATOR = 5;
			int iY_SEPARATOR = 10;
			int iY_ROW_HEIGHT = 20;
			//desc
			jlDesc = new JLabel(pa.getDesc());
			//add panels
			model = new PropertiesTableModel(pa);
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
							String sProperty = (String) model.getValueAt(iRow,5);
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
			//Hide all album columns is not required
			if (!(pa instanceof Track)) {
				TableColumnExt col = jtable.getColumnExt(3);
				col.setVisible(false);
			}
			jtable.packAll();
   	}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
		 */
		public void tableChanged(TableModelEvent e) {
			if (e.getColumn() == 2){
				String sKey = (String)jtable.getModel().getValueAt(e.getFirstRow(),5);
				String sValue = (String)jtable.getModel().getValueAt(e.getFirstRow(),2);
		        IPropertyable newItem = ItemManager.changeItem(pa,sKey,sValue);
                if (newItem != null){ //null means same item but with others custom properties, no need to refresh
                    this.pa = newItem;
                    PropertiesTableModel newModel = new PropertiesTableModel(newItem);
                    jtable.setModel(newModel);
                    jtable.packAll();
                    newModel.addTableModelListener(this);
                    jlDesc.setText(pa.getDesc());
                    ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH)); //TBI see later for a smarter event
                }
			}
		}
	}
}
