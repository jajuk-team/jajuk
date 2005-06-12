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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
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
    
    /** Properties table */
    JajukTable jTable;

    /** Item to show */
    PropertyAdapter pa;

    /** Table model */
    PropertiesTableModel ptmodel;
    
 JPanel jpButtons;
    JButton jbOk;
    JButton jbCancel;

    /**
     * Constructor
     * 
     * @param pa
     *            the item to display
     */
    public PropertiesWizard(PropertyAdapter pa) {
        super(pa.getProperty(XML_NAME));
        setIconImage(Util.getIcon(ICON_LOGO).getImage());
        this.pa = pa;
        jpTable = new JPanel();
        int iX_SEPARATOR = 5;
        int iY_SEPARATOR = 10;
        populateTable();
        jTable = new JajukTable(ptmodel);
        jpTable.add(new JScrollPane(jTable));
        //buttons
        jpButtons = new JPanel();
        jpButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        jbOk = new JButton(Messages.getString("OK")); //$NON-NLS-1$
        jbOk.setEnabled(false);
        jbOk.addActionListener(this);
        jbCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
        jbCancel.addActionListener(this);
        jpButtons.add(jbOk);
        jpButtons.add(jbCancel);
        //add panels
        jpMain = new JPanel();
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.Y_AXIS));
        jpMain.add(jpTable);
        jpMain.add(jpButtons);
        add(jpMain);
        pack();
        setVisible(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == jbCancel){
            dispose();
        }
    }

    /** Fill the tree */
    public void populateTable() {
        // col number
        int iColNum = 2;
        // Columns names
        String[] sColName = new String[] {
                Messages.getString("PropertiesWizard.1"), Messages.getString("PropertiesWizard.2")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
        // Values
        SequentialMap properties = pa.getProperties();
        int iSize = properties.size();
        Iterator it = properties.keys().iterator();
        Object[][] oValues = new Object[iSize][iColNum + 1];
        for (int i = 0; it.hasNext(); i++) {
            String sProperty = (String) it.next();
            oValues[i][0] = sProperty;
            oValues[i][1] = properties.get(sProperty);
        }
        // edtiable table and class
        boolean[][] bCellEditable = new boolean[8][iSize];
        for (int i = 0; i < iColNum; i++) {
            for (int j = 0; j < iSize; j++) {
                bCellEditable[i][j] = false;
            }
        }
        // model creation
        ptmodel = new PropertiesTableModel(iColNum, bCellEditable, sColName);
        ptmodel.setValues(oValues);
        ptmodel.fireTableDataChanged();
    }
}
