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
import java.text.Format;
import java.util.Date;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jajuk.base.Event;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.i18n.Messages;
import org.jdesktop.swingx.JXDatePicker;


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
    JCheckBox jcbDefault;
    JXDatePicker jdpDefault;
    JLabel jlFormat;
    JComboBox jcbFormat;
    /**
     * Constructor
     */
    public NewPropertyWizard() {
        super(Messages.getString("NewPropertyWizard.0"));
        populate();//create default UI
        //name
        jlName = new JLabel(Messages.getString("NewPropertyWizard.2"));
        jtfName = new JTextField();
        jtfName.addKeyListener(this);
        //Type, class
        jlClass= new JLabel(Messages.getString("NewPropertyWizard.3"));
        jcbClass = new JComboBox();
        jcbClass.addItem(Messages.getString(FORMAT_STRING));
        jcbClass.addItem(Messages.getString(FORMAT_NUMBER));
        jcbClass.addItem(Messages.getString(FORMAT_FLOAT));
        jcbClass.addItem(Messages.getString(FORMAT_BOOLEAN));
        jcbClass.addItem(Messages.getString(FORMAT_DATE));
        jcbClass.addItemListener(this);
        //Format
        jlFormat = new JLabel(Messages.getString("NewPropertyWizard.4"));
        jcbFormat = new JComboBox();
        //add supported date formats
        for (String s:PropertyMetaInformation.getSupportedDateFormatsDesc()){
            jcbFormat.addItem(s);
        }
        jcbFormat.setEnabled(false);
        //Default
        jlDefault= new JLabel(Messages.getString("NewPropertyWizard.5"));
        jtfDefault = new JTextField(40);
        jcbDefault = new JCheckBox();
        jcbDefault.setEnabled(false);
        jdpDefault = new JXDatePicker();
        jdpDefault.setEnabled(false);
        JPanel jpDefault = new JPanel();
        double[][] d = {{TableLayout.PREFERRED,10,TableLayout.PREFERRED,10,TableLayout.PREFERRED},{20}};
        jpDefault.setLayout(new TableLayout(d));
        jpDefault.add(jtfDefault,"0,0");
        jpDefault.add(jcbDefault,"2,0");
        jpDefault.add(jdpDefault,"4,0");
        //main
        int iXSeparator = 10;
        int iYSeparator = 20;
        double[][] dSize = {
                {iXSeparator,TableLayout.PREFERRED,iXSeparator,TableLayout.PREFERRED,iXSeparator},
                {iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator} };
        jpMain.setLayout(new TableLayout(dSize));
        jpMain.add(jlItemChoice,"1,1");
        jpMain.add(jcbItemChoice,"3,1");
        jpMain.add(jlName,"1,3");
        jpMain.add(jtfName,"3,3");
        jpMain.add(jlClass,"1,5");
        jpMain.add(jcbClass,"3,5");
        jpMain.add(jlDefault,"1,7");
        jpMain.add(jpDefault,"3,7");
        jpMain.add(jlFormat,"1,9");
        jpMain.add(jcbFormat,"3,9");
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
             Class cType = null;
            switch(jcbClass.getSelectedIndex()){
                case 0:
                   cType =  String.class;
                   break;
                case 1:
                    cType =  Long.class;
                    break;
                case 2:
                    cType =  Double.class;
                    break;
                case 3:
                    cType =  Boolean.class;
                    break;
                case 4:
                    cType =  Date.class;
                    break;
            }
            String sProperty = jtfName.getText();
            String sFormat = null;
            if (jcbClass.getSelectedIndex() == 4){
                sFormat = (String)jcbFormat.getSelectedItem();
            }
            Object oDefault = jtfDefault.getText();
            //Check number and float formats (others are safe)
            try{
                if (cType.equals(Long.class)){
                    Long.parseLong(jtfDefault.getText());
                }
                else if (cType.equals(Double.class)){
                    Double.parseDouble(jtfDefault.getText());
                }
            }
            catch(Exception e){
               Messages.showErrorMessage("137");
               return;     
            }
            //set default
            if (cType.equals(Boolean.class)){
                oDefault = jcbDefault.isSelected();
            }
            else if (cType.equals(Date.class)){
                oDefault = jdpDefault.getDate();
            }
            Format format = null;
            if (sFormat!= null && !sFormat.trim().equals("")) {
                format = PropertyMetaInformation.getDateFormat(sFormat);
            }
            PropertyMetaInformation meta = new PropertyMetaInformation(
                    sProperty,true,false,true,true,false,cType,format,oDefault);
            im.registerProperty(meta);
            // im.applyNewProperty(meta);
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
        //Date format
        if (jcbClass.getSelectedIndex() == 4){ 
            jcbFormat.setEnabled(true);
            jdpDefault.setEnabled(true);
        }
        else{
            jcbFormat.setEnabled(false);
            jdpDefault.setEnabled(false);
        }
        //Boolean format
        if (jcbClass.getSelectedIndex() == 3){ 
            jcbDefault.setEnabled(true);
        }
        else{
            jcbDefault.setEnabled(false);
        }
        //Others formats
        if (jcbClass.getSelectedIndex() != 3 && jcbClass.getSelectedIndex() != 4){ 
            jtfDefault.setEnabled(true);
        }
        else{
            jtfDefault.setEnabled(false);
        }
        //Ok button
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