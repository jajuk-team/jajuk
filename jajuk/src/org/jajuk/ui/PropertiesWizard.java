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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jajuk.base.IPropertyable;
import org.jajuk.base.ItemManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

/**
 * Item properties wizard for any jajuk item
 * 
 * @author Bertrand Florat
 * @created 6 juin 2005
 */
public class PropertiesWizard extends JFrame implements ITechnicalStrings, ActionListener {

    JPanel jpMain;
    
    JPanel jpTable;
    
    /**Item description*/
    JLabel jlDesc;
    
    /** Properties table */
    JajukTable jtable;

    /** Item to show */
    IPropertyable pa;
   
   /**OK / cancel panel*/
    OKCancelPanel okp;
 
    /**
     * Constructor
     * 
     * @param pa
     *            the item to display
     */
    public PropertiesWizard(IPropertyable pa) {
        super(pa.getValue(XML_NAME));
        setIconImage(Util.getIcon(ICON_LOGO).getImage());
        setVisible(true);
        this.pa = pa;
        int iX_SEPARATOR = 5;
        int iY_SEPARATOR = 10;
        //desc
        jlDesc = new JLabel(pa.getDesc());
        //buttons
        okp = new OKCancelPanel(this);
        okp.getOKButton().setEnabled(false);
        //add panels
        jpMain = new JPanel();
        double[][] dSize = {{0.99},
        {40,iY_SEPARATOR,0.99,iY_SEPARATOR,20}};
        jpMain.setLayout(new TableLayout(dSize));
        PropertiesTableModel model = new PropertiesTableModel(pa); 
        jtable = new JajukTable(model);
        jtable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (jtable.getSelectedColumn() == 3){
                    int iRow = jtable.getSelectedRow();
                    PropertiesTableModel model =(PropertiesTableModel)jtable.getModel(); 
                    if (model.isLinkable(iRow)){
                        IPropertyable pa = ItemManager.getItemByID((String)model.getValueAt(iRow,4),
                            (String)model.getValueAt(iRow,5));
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
        jpMain.add(okp,"0,4");
        getContentPane().add(jpMain);
        jtable.packAll();
        pack();
        Util.setShuffleLocation(this,400,400);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okp.getCancelButton()){
            dispose();
        }
    }

}

