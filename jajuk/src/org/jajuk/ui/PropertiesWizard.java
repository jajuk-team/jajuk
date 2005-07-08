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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jajuk.base.IPropertyable;
import org.jajuk.base.ItemManager;
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

    JPanel jpMain;
    
    JPanel jpTable;
    
    /**Item description*/
    JLabel jlDesc;
    
    /** Properties table */
    JajukTable jtable;

    /** Item to show */
    IPropertyable pa;
 
    /**
     * Constructor
     * 
     * @param pa
     *            the item to display
     */
    public PropertiesWizard(IPropertyable pa) {
        super(pa.getValue(XML_NAME));
        setIconImage(Util.getIcon(ICON_LOGO).getImage());
        this.pa = pa;
        int iX_SEPARATOR = 5;
        int iY_SEPARATOR = 10;
        //desc
        jlDesc = new JLabel(pa.getDesc());
        //add panels
        jpMain = new JPanel();
        double[][] dSize = {{0.99},
        {40,iY_SEPARATOR,0.99}};
        jpMain.setLayout(new TableLayout(dSize));
        PropertiesTableModel model = new PropertiesTableModel(pa); 
        jtable = new JajukTable(model);
        Sorter sorter = new ShuttleSorter(0,true);
        FilterPipeline pipe = new FilterPipeline(new Filter[]{sorter});
        jtable.setFilters(pipe);
        jtable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (jtable.convertColumnIndexToModel(jtable.getSelectedColumn()) == 4){
                    int iRow = jtable.convertRowIndexToModel(jtable.getSelectedRow());
                    PropertiesTableModel model =(PropertiesTableModel)jtable.getModel(); 
                    if (model.isLinkable(iRow)){
                        IPropertyable pa = ItemManager.getItemByID((String)model.getValueAt(iRow,5),
                            (String)model.getValueAt(iRow,6));
                        if (pa != null){
                            new PropertiesWizard(pa); //show properties window for this item
                        }
                    }
                }
            }
        });
        jtable.setRowHeight(20);
        jpMain.add(jlDesc,"0,0");
        jpMain.add(new JScrollPane(jtable),"0,2");
        getContentPane().add(jpMain);
        //Hide all album columns is not required
        if (!(this.pa instanceof Track)){
        	TableColumnExt col = jtable.getColumnExt(3); 
        	col.setVisible(false);
        }
        jtable.packAll();
        Util.setShuffleLocation(this,400,400);
        pack();
        setVisible(true);
    }
  
}

