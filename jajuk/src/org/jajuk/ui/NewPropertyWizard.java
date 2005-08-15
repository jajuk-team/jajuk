/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jajuk.base.Event;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.i18n.Messages;


/**
 * 
 *  New property wizard
 *
 * @author     Bertrand Florat
 * @created    20 juin 2005
 */
public class NewPropertyWizard extends CustomPropertyWizard implements KeyListener{ 
    JLabel jlName;
    JTextField jtfName;
    JLabel jlClass;
    JComboBox jcbClass;
    JLabel jlDefault;
    JTextField jtfDefault;
    JLabel jlFormat;
    JTextField jtfFormat;
    /**
     * Constructor
     */
    public NewPropertyWizard() {
        super(Messages.getString("NewPropertyWizard.0"));
        populate();//create default UI
        jlName = new JLabel(Messages.getString("NewPropertyWizard.2"));
        jlFormat = new JLabel(Messages.getString("NewPropertyWizard.3"));
        jlFormat= new JLabel(Messages.getString("NewPropertyWizard.4"));
        jlDefault= new JLabel(Messages.getString("NewPropertyWizard.5"));
        jtfName = new JTextField();
        jcbClass = new JComboBox();
        jcbClass.addItem(Messages.getString(FORMAT_STRING));
        jcbClass.addItem(Messages.getString(FORMAT_NUMBER));
        jcbClass.addItem(Messages.getString(FORMAT_FLOAT));
        jcbClass.addItem(Messages.getString(FORMAT_BOOLEAN));
        jcbClass.addItem(Messages.getString(FORMAT_DATE));
        jcbClass.addItemListener(this);
        jtfName.addKeyListener(this);
        int iXSeparator = 10;
        int iYSeparator = 20;
        double[][] dSize = {
                {iXSeparator,0.5,iXSeparator,0.5,iXSeparator},
                {iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator} };
        jpMain.setLayout(new TableLayout(dSize));
        jpMain.add(jlItemChoice,"1,1");
        jpMain.add(jcbItemChoice,"3,1");
        jpMain.add(jlName,"1,3");
        jpMain.add(jtfName,"3,3");
        jpMain.add(jlClass,"1,5");
        jpMain.add(jcbClass,"3,5");
        jpMain.add(jlFormat,"1,7");
        jpMain.add(jtfFormat,"3,7");
        jlDefault.add(jlFormat,"1,9");
        jtfDefault.add(jtfFormat,"3,9");
        getContentPane().add(jpMain);
        getContentPane().add(okp);
        getContentPane().add(Box.createVerticalStrut(10));
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                jtfName.requestFocusInWindow();
            }
        });
        getRootPane().setDefaultButton(okp.getOKButton());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(this.okp.getOKButton())){
            //check the property is not already used internaly
            for (int i=0;i<XML_RESERVED_ATTRIBUTE_NAMES.length;i++){
                     /*check user can't create a property that is the localized name of an existing standard
                      * attribute. Note that a potential bug can occur if user change language*/
                if (XML_RESERVED_ATTRIBUTE_NAMES[i].equalsIgnoreCase(jtfName.getText()) 
                        || jtfName.getText().matches(",")){
                    Messages.showErrorMessage("110");
                    return;
                }
            }
            //OK, store it
            ItemManager im = getItemManager();
            //get selected format
             Class cFormat = null;
            switch(jcbClass.getSelectedIndex()){
                case 0:
                   cFormat =  String.class;
                   break;
                case 1:
                    cFormat =  Long.class;
                    break;
                case 2:
                    cFormat =  Double.class;
                    break;
                case 3:
                    cFormat =  Boolean.class;
                    break;
                case 4:
                    cFormat =  Date.class;
                    break;
            }
            String sProperty = jtfName.getText();
            String sFormat = jtfFormat.getText();
            String sDefault = jtfDefault.getText();
            if (sFormat.trim() == "") {
                sFormat = null;
            }
            PropertyMetaInformation meta = new PropertyMetaInformation(sProperty,true,false,cFormat,sFormat,sDefault);
             im.registerProperty(meta);
            im.applyNewProperty(meta);
            Properties properties = new Properties();
            properties.put(DETAIL_CONTENT,sProperty);
            Event event = new Event(EVENT_CUSTOM_PROPERTIES_ADD,properties);
            ObservationManager.notify(event);
            dispose();
        }
        else if(ae.getSource().equals(this.okp.getCancelButton())){
            dispose();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e) {
        if (jcbItemChoice.getSelectedIndex() != -1 && jcbClass.getSelectedIndex()!=-1
                && jtfName.getText().length() > 0){
            okp.getOKButton().setEnabled(true);
        }
        else{
            okp.getOKButton().setEnabled(false);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
        if (jcbItemChoice.getSelectedIndex() != -1 && jcbClass.getSelectedIndex()!=-1
                && jtfName.getText().length() > 0){
            okp.getOKButton().setEnabled(true);
        }
        else{
            okp.getOKButton().setEnabled(false);
        }
    }
}