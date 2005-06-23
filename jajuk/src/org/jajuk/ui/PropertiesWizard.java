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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jajuk.base.PropertyAdapter;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.SequentialMap;
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
    PropertyAdapter pa;

    /** Table model */
    PropertiesTableModel ptmodel;
    
    OKCancelPanel okp;
 
    /**
     * Constructor
     * 
     * @param pa
     *            the item to display
     */
    public PropertiesWizard(PropertyAdapter pa) {
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
        populateTable();
        jtable = new JajukTable(ptmodel);
        jtable.setRowHeight(20);
        jpMain.add(jlDesc,"0,0");
        jpMain.add(new JScrollPane(jtable),"0,2");
        jpMain.add(okp,"0,4");
        add(jpMain);
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

    /** Fill the tree */
    public void populateTable() {
        // col number
        int iColNum = 3;
        // Columns names
        String[] sColName = new String[] {
                Messages.getString("PropertiesWizard.1"), //$NON-NLS-1$
                Messages.getString("PropertiesWizard.2"),//$NON-NLS-2$
                Messages.getString("PropertiesWizard.3")};  //$NON-NLS-3$ 
        // Values
        //set ignored attributes we won't display
        ArrayList alIgnored = new ArrayList(10);
        alIgnored.add(XML_ID);//ID
        alIgnored.add(XML_EXPANDED);//expanded state
        alIgnored.add(XML_HASHCODE);//hashcode
        SequentialMap propertiesOrig = pa.getProperties();
        ArrayList properties = new ArrayList(10); //properties after cleaning
        //remove hiden attributes
        Iterator it  = propertiesOrig.keys().iterator();
        int ignored = 0;
        while (it.hasNext()){
            String sProperty = (String)it.next();
            if (!alIgnored.contains(sProperty)){
                properties.add(sProperty);
            }
            else{
                ignored ++;
            }
        }
        int iSize = propertiesOrig.size()-ignored;
        it = properties.iterator();
        Object[][] oValues = new Object[iSize][iColNum];
        for (int i = 0; it.hasNext(); i++) { //we don't display first attribute (ID)
            String sKey = (String) it.next();
            oValues[i][0] = Messages.getInstance().contains("Property_"+sKey)?
                    Messages.getString("Property_"+sKey):sKey; //check if property name is translated (for custom properties)
            oValues[i][1] = pa.getHumanValue(sKey);
            if (pa.isPropertyEditable(sKey)){
                oValues[i][2] = Util.getIcon(ICON_EDIT);    
            }
            else{
                oValues[i][2] = Util.getIcon(ICON_NO_EDIT);
            }
        }
        // model creation
        ptmodel = new PropertiesTableModel(iColNum,  sColName, pa);
        ptmodel.setValues(oValues);
    }

}

