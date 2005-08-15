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
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;


/**
 * 
 *  Set properties of a group of items
 *
 * @author     Bertrand Florat
 * @created    2005/07/15
 */
public class SetPropertyWizard extends JDialog 
    implements KeyListener,ItemListener,ActionListener,ITechnicalStrings{ 
    JPanel jpMain;
    JLabel jlProperty;
    JComboBox jcbProperty;
    JLabel jlValue;
    JTextField jtfValue;
    JCheckBox jcbValue;
    OKCancelPanel okp;
    ArrayList<IPropertyable> alItems;
    ArrayList alKeys;
    String sKey;
    String sValue;
    String sFormat;    
    
    /**
     * Constructor
     * @param alItems list of items (must be same class) to set
     */
    public SetPropertyWizard(ArrayList<IPropertyable> alItems) {
        super(Main.getWindow(),Messages.getString("SetPropertyWizard.0"));
        this.alItems = alItems;
        jlProperty = new JLabel(Messages.getString("SetPropertyWizard.1"));
        jcbProperty = new JComboBox();
        alKeys = new ArrayList(10);
        sKey = null;
        sValue = null;
        //fill properties
        IPropertyable item = alItems.get(0);
        Iterator it = item.getProperties().keySet().iterator();
        while (it.hasNext()){
            String sKey = (String)it.next();
            //display editable properties and hide name if several items were selected
            if (item.isPropertyEditable(sKey) && !(alItems.size() > 1 && XML_NAME.equals(sKey))){
                if (Messages.getInstance().contains(PROPERTY_SEPARATOR+sKey)){
                    jcbProperty.addItem(Messages.getString(PROPERTY_SEPARATOR+sKey)); //localized keys
                }
                else{
                    jcbProperty.addItem(sKey); //custom key
                }
                alKeys.add(sKey); //keys
            }
        }
        jlValue = new JLabel(Messages.getString("SetPropertyWizard.2"));
        jtfValue = new JTextField();
        jtfValue.setEnabled(false);
        jtfValue.addKeyListener(this);
        jcbValue = new JCheckBox(Messages.getString("SetPropertyWizard.3"));
        jcbValue.setSelected(false); //false by default
        jcbValue.setEnabled(false);
        okp = new OKCancelPanel(this);
        okp.getCancelButton().setText(Messages.getString("SetPropertyWizard.4"));
        jcbProperty.addActionListener(this);
        if (alKeys.size() > 0){ //select first property found
            jcbProperty.setSelectedIndex(0);
        }
        int iXSeparator = 10;
        int iYSeparator = 20;
        double[][] dSize = {
                {iXSeparator,0.4,iXSeparator,0.4,iXSeparator,0.2,iXSeparator},
                {iYSeparator,20,iYSeparator,20,iYSeparator} };
        jpMain = new JPanel();
        jpMain.setLayout(new TableLayout(dSize));
        jpMain.add(jlProperty,"1,1");
        jpMain.add(jcbProperty,"3,1");
        jpMain.add(jlValue,"1,3");
        jpMain.add(jtfValue,"3,3");
        jpMain.add(jcbValue,"5,3");
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        getContentPane().add(jpMain);
        getContentPane().add(okp);
        getContentPane().add(Box.createVerticalStrut(10));
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                jcbProperty.requestFocusInWindow();
            }
        });
        pack();
        setVisible(true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == jcbProperty){
            ItemManager im = ItemManager.getItemManager(alItems.get(0).getClass());
            /* A VOIR
            sFormat = im.getFormat((String)alKeys.get(jcbProperty.getSelectedIndex()));
            sKey = (String)alKeys.get(jcbProperty.getSelectedIndex());
            if (FORMAT_BOOLEAN.equals(sFormat)){
                jtfValue.setEnabled(false);
                jtfValue.setText("");
                jcbValue.setEnabled(true);
                jcbValue.setSelected(Boolean.parseBoolean(sValue));
            }
            else{
                jtfValue.setEnabled(true);
                jtfValue.setText(alItems.get(0).getHumanValue(sKey));
                jcbValue.setEnabled(false);
                jcbValue.setSelected(false);
            }*/
        }
        else if (ae.getSource().equals(this.okp.getOKButton())){ //OK
             process();
        }
        else if(ae.getSource().equals(this.okp.getCancelButton())){ //finished
            dispose();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e) {
        if (jcbProperty.getSelectedIndex() != -1 && jtfValue.getText().length() > 0){
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
        if (e.getKeyChar()==KeyEvent.VK_ENTER){
            process();
        }
    }

    /**apply changes*/
    private void process(){
        Iterator<IPropertyable> it = alItems.iterator();
        ArrayList<IPropertyable> alNewItems = new ArrayList<IPropertyable>(alItems.size());
        while (it.hasNext()){
            IPropertyable item = it.next();
            IPropertyable newItem = null;
            if (FORMAT_BOOLEAN.equals(sFormat)){
                newItem =  ItemManager.changeItem(item,sKey,Boolean.toString(jcbValue.isSelected()));
            }
            else {
                newItem =  ItemManager.changeItem(item,sKey,jtfValue.getText());
            }
            if (newItem == null){ //no change
                alNewItems.add(item);
            }
            else{
                alNewItems.add(newItem);
            }
        }
        //switch old and new items
        this.alItems = alNewItems;
        InformationJPanel.getInstance().setMessage(Messages.getString("SetPropertyWizard.5"),InformationJPanel.INFORMATIVE);
        ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH)); //refresh all UI
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
        if (jcbProperty.getSelectedIndex() != -1 && jtfValue.getText().length() > 0){
            okp.getOKButton().setEnabled(true);
        }
        else{
            okp.getOKButton().setEnabled(false);
        }
    }
}