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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jajuk.Main;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  First time Wizard  
 *
 * @author     Bertrand Florat
 * @created    27 avr. 2005
 */
public class FirstTimeWizard extends JDialog implements ITechnicalStrings,ActionListener{
    JLabel jlLeftIcon;
    JPanel jpRightPanel;
    JLabel jlWelcome;
    JLabel jlFileSelection;
    JTextField jtfFileSelected;
    JButton jbFileSelection;
    JCheckBox jcbAutoCover;
    JCheckBox jcbHelp;
    JPanel jpButtons;
    JButton jbOk;
    JButton jbCancel;
    JPanel jpMain;
    
    /**Selected directory*/
    private File fDir;
    
    
    /**
     * First time wizard
     */
    public FirstTimeWizard() {
        super(Main.getWindow(),true); //make it modal
        setTitle(Messages.getString("FirstTimeWizard.0"));//$NON-NLS-1$
        setLocation(org.jajuk.Main.getWindow().getX()+100,org.jajuk.Main.getWindow().getY()+100);
        int iX_SEPARATOR = 5;
        int iY_SEPARATOR = 10;
        jlLeftIcon = new JLabel(Util.getResizedImage(
            Util.getIcon(IMAGES_SPLASHSCREEN),300,200));
        jpRightPanel = new JPanel();
        jlWelcome=new JLabel(Messages.getString("FirstTimeWizard.1")); //$NON-NLS-1$
        jlFileSelection = new JLabel(Messages.getString("FirstTimeWizard.2")); //$NON-NLS-1$
        jbFileSelection = new JButton(Util.getIcon(ICON_OPEN_FILE));
        jtfFileSelected = new JTextField(""); //$NON-NLS-1$
        jtfFileSelected.setForeground(Color.BLUE);
        jtfFileSelected.setEditable(false);
        jbFileSelection.addActionListener(this);
        jcbAutoCover = new JCheckBox(Messages.getString("FirstTimeWizard.3")); //$NON-NLS-1$
        //can't change auto-cover if not first connection
        if (ConfigurationManager.getBoolean(CONF_FIRST_CON)){
            jcbAutoCover.setSelected(true);
        }
        else{
            jcbAutoCover.setVisible(false);
        }
        jcbHelp = new JCheckBox(Messages.getString("FirstTimeWizard.4")); //$NON-NLS-1$
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
        double sizeRight[][] = { { 0.99,iX_SEPARATOR}, 
                {iY_SEPARATOR,60, iY_SEPARATOR, 30,iY_SEPARATOR, 20, iY_SEPARATOR, 40 ,iY_SEPARATOR,20, iY_SEPARATOR, 40 }};
        
        FlowLayout flSelection = new FlowLayout(FlowLayout.LEFT);
        JPanel jpFileSelection = new JPanel();
        jpFileSelection.setLayout(flSelection);
        jpFileSelection.add(jbFileSelection);
        jpFileSelection.add(Box.createHorizontalStrut(10));
        jpFileSelection.add(jlFileSelection);
        
        jpRightPanel.setLayout(new TableLayout(sizeRight));
        jpRightPanel.add(jlWelcome,"0,1"); //$NON-NLS-1$
        jpRightPanel.add(jpFileSelection,"0,3"); //$NON-NLS-1$
        jpRightPanel.add(jtfFileSelected,"0,5"); //$NON-NLS-1$
        jpRightPanel.add(jcbAutoCover,"0,7"); //$NON-NLS-1$
        jpRightPanel.add(jcbHelp,"0,9"); //$NON-NLS-1$
        jpRightPanel.add(jpButtons,"0,11"); //$NON-NLS-1$
        double size[][] = { { 0.4,30,0.6}, 
                {0.99 }};
        jpMain = (JPanel)getContentPane();
        jpMain.setLayout(new TableLayout(size));
        jpMain.add(jlLeftIcon,"0,0"); //$NON-NLS-1$
        jpMain.add(jpRightPanel,"2,0"); //$NON-NLS-1$
        getRootPane().setDefaultButton(jbOk);
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jbCancel){
            dispose();  //close window
        }
        else if(e.getSource() == jbFileSelection){
            JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(true,false));
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc.setDialogTitle(Messages.getString("FirstTimeWizard.5"));//$NON-NLS-1$
            jfc.setMultiSelectionEnabled(false);
            int returnVal = jfc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fDir = jfc.getSelectedFile();
                // check device availibility 
                String sCode = DeviceManager.getInstance().checkDeviceAvailablity(fDir.getName(),0,fDir.getAbsolutePath(),fDir.getAbsolutePath());
                if (!sCode.equals("0")){ //$NON-NLS-1$
                    Messages.showErrorMessage(sCode);
                    jbOk.setEnabled(false);
                    return;
                }
                jtfFileSelected.setText(fDir.getAbsolutePath());
                jbOk.setEnabled(true);
                jbOk.grabFocus();
            }
        }
        else if (e.getSource() == jbOk){
            /*Set perspective to display. We differentiate first connection or not because 
            during first connection, perspectives are not yet initialized, so we just tell it whish 
            perspective to use at startup*/
            if (ConfigurationManager.getBoolean(CONF_FIRST_CON)){
                if (jcbHelp.isSelected()){
                    //set parameter perspective
                    Main.setDefaultPerspective(PERSPECTIVE_NAME_HELP); 
                }
                else{
                    //set physical perspective
                    Main.setDefaultPerspective(PERSPECTIVE_NAME_PHYSICAL);
                }    
            }
            else{
                //go to help perspective if required
                if (jcbHelp.isSelected()){
                    //set parameter perspective
                    PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_HELP);
                }
            }
            
            //Set auto cover property
            ConfigurationManager.setProperty(
                CONF_COVERS_AUTO_COVER,Boolean.toString(jcbAutoCover.isSelected()));
            //Create a directory device
            Device device = DeviceManager.getInstance().registerDevice(fDir.getName(),0,fDir.getAbsolutePath());
            device.setProperty(XML_DEVICE_MOUNT_POINT,fDir.getAbsolutePath());
            device.setProperty(XML_DEVICE_AUTO_MOUNT,true);
            try{
                device.refresh(true);
            }
            catch(Exception e2){
                Log.error("112",device.getName(),e2); //$NON-NLS-1$
                Messages.showErrorMessage("112",device.getName()); //$NON-NLS-1$
            }
            //exit
            dispose();
        }
    }
   
}