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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.Main;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.i18n.Messages;
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
    JButton jbFileSelection;
    JCheckBox jcbAutoCover;
    JCheckBox jcbHelp;
    JPanel jpButtons;
    JButton jbOk;
    JButton jbCancel;
    
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
        jlLeftIcon = new JLabel(new ImageIcon(Util.getResizedImage(
            Util.getIcon(IMAGES_SPLASHSCREEN).getImage(),300,200)));
        jpRightPanel = new JPanel();
        jlWelcome=new JLabel(Messages.getString("FirstTimeWizard.1"));
        jlFileSelection = new JLabel(Messages.getString("FirstTimeWizard.2"));
        jbFileSelection = new JButton(Util.getIcon(ICON_OPEN_FILE));
        jbFileSelection.addActionListener(this);
        jcbAutoCover = new JCheckBox(Messages.getString("FirstTimeWizard.3"));
        jcbAutoCover.setSelected(true);
        jcbHelp = new JCheckBox(Messages.getString("FirstTimeWizard.4"));
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
        double sizeRight[][] = { { 0.99,iX_SEPARATOR, 20,iX_SEPARATOR }, 
                {iY_SEPARATOR,60, iY_SEPARATOR, 20, iY_SEPARATOR, 40 ,iY_SEPARATOR,20, iY_SEPARATOR, 40 }};
        jpRightPanel.setLayout(new TableLayout(sizeRight));
        jpRightPanel.add(jlWelcome,"0,1");
        jpRightPanel.add(jlFileSelection,"0,3");
        jpRightPanel.add(jbFileSelection,"2,3");
        jpRightPanel.add(jcbAutoCover,"0,5");
        jpRightPanel.add(jcbHelp,"0,7");
        jpRightPanel.add(jpButtons,"0,9");
        double size[][] = { { 0.4,30,0.6}, 
                {0.99 }};
        setLayout(new TableLayout(size));
        add(jlLeftIcon,"0,0");
        add(jpRightPanel,"2,0");
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
                String sCode = DeviceManager.checkDeviceAvailablity(fDir.getName(),0,fDir.getAbsolutePath(),fDir.getAbsolutePath());
                if (!sCode.equals("0")){ //$NON-NLS-1$
                    Messages.showErrorMessage(sCode);
                    jbOk.setEnabled(false);
                    return;
                }
                jbOk.setEnabled(true);
            }
        }
        else if (e.getSource() == jbOk){
            if (jcbHelp.isSelected()){
                //set parameter perspective
                Main.setDefaultPerspective(PERSPECTIVE_NAME_HELP);
            }
            else{
                //set physical perspective
                Main.setDefaultPerspective(PERSPECTIVE_NAME_PHYSICAL);
            }
            //Set auto cover property
            ConfigurationManager.setProperty(
                CONF_COVERS_AUTO_COVER,Boolean.toString(jcbAutoCover.isSelected()));
            //Create a directory device
            Device device = DeviceManager.registerDevice(fDir.getName(),0,fDir.getAbsolutePath(),fDir.getAbsolutePath());
            device.setProperty(DEVICE_OPTION_AUTO_MOUNT,TRUE);
            device.setProperty(DEVICE_OPTION_AUTO_REFRESH,FALSE);
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