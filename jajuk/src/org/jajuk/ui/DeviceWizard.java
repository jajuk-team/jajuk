/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.jajuk.Main;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Device creation wizzard
 * @author Bertrand Florat 
 * @created 9 nov. 2003
 */
public class DeviceWizard extends JDialog implements ActionListener,ITechnicalStrings {
	JPanel jpMain;
	JPanel jp1;
	JLabel jlType;
	JComboBox jcbType;
	JLabel jlName;
	JTextField jtfName;
	JLabel jlUrl;
	JTextField jtfUrl;
	JButton jbUrl;
	JButton jbUrlMountPoint;
	JLabel jlMountPoint;
	JTextField jtfMountPoint;
	JCheckBox jcbRefresh;
	JCheckBox jcbAutoMount;
	JCheckBox jcbAutoRefresh;
	JCheckBox jcboxSynchronized;
	JComboBox jcbSynchronized;
	JPanel jp2;
	ButtonGroup bgSynchro;
	JRadioButton jrbBidirSynchro;
	JRadioButton jrbUnidirSynchro;
	JPanel jpButtons;
	JButton jbOk;
	JButton jbCancel;
	
	/**New device flag*/
	private boolean bNew = true;
	
	/**Current device*/
	Device device;
	
	/** All devices expect itself */
	ArrayList alDevices = new ArrayList(10);
	
	/**
	 * Device wizard by default, is used for void configuration
	 */
	public DeviceWizard() {
		super(Main.getWindow(),true); //make it modal
		setTitle(Messages.getString("DeviceWizard.0"));//$NON-NLS-1$
		setLocation(org.jajuk.Main.getWindow().getX()+100,org.jajuk.Main.getWindow().getY()+100);
		jpMain = new JPanel();
		jpMain.setLayout(new BoxLayout(jpMain,BoxLayout.Y_AXIS));
		jp1 = new JPanel();
		jp1.setBorder(BorderFactory.createEmptyBorder(25, 15, 0, 15));
		int iX_SEPARATOR = 5;
		double size1[][] = { { 0.5,iX_SEPARATOR,0.45,iX_SEPARATOR,40 }, {
			20, 20, 20, 20, 20,20,20, 20, 20, 20, 20,20,20 }
		};
		jp1.setLayout(new TableLayout(size1));
		jlType = new JLabel(Messages.getString("DeviceWizard.1")); //$NON-NLS-1$
		jcbType = new JComboBox();
		Iterator itDevicesTypes = DeviceManager.getDeviceTypes();
		while (itDevicesTypes.hasNext()){
			jcbType.addItem((String)itDevicesTypes.next());
		}
		jlName = new JLabel(Messages.getString("DeviceWizard.2")); //$NON-NLS-1$
		jtfName = new JTextField();
		jtfName.setToolTipText(Messages.getString("DeviceWizard.45")); //$NON-NLS-1$
		jlUrl = new JLabel(Messages.getString("DeviceWizard.3")); //$NON-NLS-1$
		jtfUrl = new JTextField();
		jtfUrl.setToolTipText(Messages.getString("DeviceWizard.46")); //$NON-NLS-1$
		jbUrl = new JButton(Util.getIcon(ICON_OPEN_FILE));
		jbUrl.setToolTipText(Messages.getString("DeviceWizard.43")); //$NON-NLS-1$
		jbUrl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		jbUrl.addActionListener(this);
		jbUrlMountPoint = new JButton(Util.getIcon(ICON_OPEN_FILE));
		jbUrlMountPoint.setToolTipText(Messages.getString("DeviceWizard.47")); //$NON-NLS-1$
		jbUrlMountPoint.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		jbUrlMountPoint.addActionListener(this);
		jlMountPoint = new JLabel(Messages.getString("DeviceWizard.4")); //$NON-NLS-1$
		jtfMountPoint = new JTextField();
		jtfMountPoint.setToolTipText(Messages.getString("DeviceWizard.47")); //$NON-NLS-1$
		//mount point notion is unknown under Windows
		if (Util.isUnderWindows()){
			jlMountPoint.setEnabled(false);
			jtfMountPoint.setEnabled(false);
			jbUrlMountPoint.setEnabled(false);
		}
		jcbRefresh = new JCheckBox(Messages.getString("DeviceWizard.7")); //$NON-NLS-1$
		jcbRefresh.setToolTipText(Messages.getString("DeviceWizard.48")); //$NON-NLS-1$
		jcbRefresh.addActionListener(this);
		jcbAutoMount = new JCheckBox(Messages.getString("DeviceWizard.8")); //$NON-NLS-1$
		jcbAutoMount.setToolTipText(Messages.getString("DeviceWizard.49")); //$NON-NLS-1$
		jcbAutoMount.addActionListener(this);
		jcbAutoRefresh = new JCheckBox(Messages.getString("DeviceWizard.9")); //$NON-NLS-1$
		jcbAutoRefresh.setToolTipText(Messages.getString("DeviceWizard.50")); //$NON-NLS-1$
		jcboxSynchronized = new JCheckBox(Messages.getString("DeviceWizard.10")); //$NON-NLS-1$
		jcboxSynchronized.setToolTipText(Messages.getString("DeviceWizard.51")); //$NON-NLS-1$
		jcboxSynchronized.addActionListener(this);
		jcbSynchronized = new JComboBox();
		//populate combo
		Iterator it = DeviceManager.getDevices().iterator();
		while (it.hasNext()) {
			Device device2 = (Device) it.next();
			alDevices.add(device2);
			jcbSynchronized.addItem(device2.getName());
		}
		jcbSynchronized.setEnabled(false);
		jcbSynchronized.setToolTipText(Messages.getString("DeviceWizard.52")); //$NON-NLS-1$
		//Default automount behavior
		jcbType.addActionListener(this);
		bgSynchro = new ButtonGroup();
		jrbUnidirSynchro = new JRadioButton(Messages.getString("DeviceWizard.11")); //$NON-NLS-1$
		jrbUnidirSynchro.setToolTipText(Messages.getString("DeviceWizard.12")); //$NON-NLS-1$
		jrbUnidirSynchro.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		jrbUnidirSynchro.setEnabled(false);
		jrbUnidirSynchro.addActionListener(this);
		jrbBidirSynchro = new JRadioButton(Messages.getString("DeviceWizard.13")); //$NON-NLS-1$
		jrbBidirSynchro.setToolTipText(Messages.getString("DeviceWizard.14")); //$NON-NLS-1$
		jrbBidirSynchro.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		jrbBidirSynchro.setEnabled(false);
		jrbBidirSynchro.addActionListener(this);
		bgSynchro.add(jrbBidirSynchro);
		bgSynchro.add(jrbUnidirSynchro);
		jp1.add(jlType, "0,0"); //$NON-NLS-1$
		jp1.add(jcbType, "2,0"); //$NON-NLS-1$
		jp1.add(jlName, "0,2"); //$NON-NLS-1$
		jp1.add(jtfName, "2,2"); //$NON-NLS-1$
		jp1.add(jlUrl, "0,4"); //$NON-NLS-1$
		jp1.add(jtfUrl, "2,4"); //$NON-NLS-1$
		jp1.add(jbUrl, "4,4"); //$NON-NLS-1$
		jp1.add(jbUrlMountPoint, "4,6"); //$NON-NLS-1$
		jp1.add(jlMountPoint, "0,6"); //$NON-NLS-1$
		jp1.add(jtfMountPoint, "2,6"); //$NON-NLS-1$
		jp1.add(jcbRefresh, "0,8"); //$NON-NLS-1$
		jp1.add(jcbAutoMount, "0,10"); //$NON-NLS-1$
		jp1.add(jcbAutoRefresh, "2,10"); //$NON-NLS-1$
		jp1.add(jcboxSynchronized, "0,12"); //$NON-NLS-1$
		jp1.add(jcbSynchronized, "2,12"); //$NON-NLS-1$
		double size2[][] = { { 0.99 }, {
			20, 20, 20, 20, 20, 20, 20 }
		};
		jp2 = new JPanel();
		jp2.setLayout(new TableLayout(size2));
		jp2.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
		jp2.add(jrbUnidirSynchro, "0,1"); //$NON-NLS-1$
		jp2.add(jrbBidirSynchro, "0,3"); //$NON-NLS-1$
		if (jcbSynchronized.getItemCount()==0){
			jcboxSynchronized.setEnabled(false);
			jcbSynchronized.setEnabled(false);
			jrbBidirSynchro.setEnabled(false);
		}
		
		//buttons
		jpButtons = new JPanel();
		jpButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		jbOk = new JButton(Messages.getString("OK")); //$NON-NLS-1$
		jbOk.requestFocusInWindow();
		jbOk.addActionListener(this);
		jbCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
		jbCancel.addActionListener(this);
		jpButtons.add(jbOk);
		jpButtons.add(jbCancel);
		
		jpMain.add(jp1);
		jpMain.add(jp2);
		jpMain.add(Box.createVerticalGlue());
		jpMain.add(jpButtons);
		setContentPane(jpMain);
		pack();
	}
	
	
	/**
	 * Update widgets for default state
	 */
	public void updateWidgetsDefault(){
		jcbRefresh.setSelected(true);
		jcbAutoMount.setSelected(true);
		jcbAutoRefresh.setEnabled(true);
		jcbAutoRefresh.setSelected(false);
		jrbUnidirSynchro.setSelected(true);//default synchro mode
		jrbBidirSynchro.setEnabled(false);
	}
	
	
	/**
	 * Update widgets for device property state 
	 */
	public void updateWidgets(final Device device){
		bNew = false;
		setTitle(Messages.getString("DeviceWizard.0")+" : "+device.getName());//$NON-NLS-1$ //$NON-NLS-2$
		this.device = device;
		jcbSynchronized.removeAllItems();
		alDevices.clear();
		Iterator it = DeviceManager.getDevices().iterator();
		while (it.hasNext()) {
			Device device2 = (Device) it.next();
			if ( !device2.equals(device)){
				alDevices.add(device2);
				jcbSynchronized.addItem(device2.getName());
			}
		}
		//set default values for widgets
		updateWidgetsDefault();
		//then, specifics
		jcbType.setSelectedItem(device.getDeviceTypeS());
		jcbType.setEnabled(false); //device type cannot be changed
		jtfName.setText(device.getName());
		jtfName.setEnabled(false); //device name cannot be changed
		jtfUrl.setText(device.getUrl());
		jtfUrl.setEnabled(false); //device url cannot be changed
		jbUrl.setEnabled(false);//device url cannot be changed
		jtfMountPoint.setText(device.getMountPoint());
		jtfMountPoint.setEnabled(false); //mount point cannot be changed
		jbUrlMountPoint.setEnabled(false);//mount point cannot be changed
		jcbRefresh.setEnabled(false); //no instant refresh for updates
		jcbRefresh.setSelected(false);
		jcbAutoMount.setSelected(true);
		if (TRUE.equals(device.getProperty(DEVICE_OPTION_AUTO_MOUNT))){
			jcbAutoMount.setSelected(true);
			jcbAutoRefresh.setEnabled(true); //refresh at startup is only if it is auto-mounted
		}
		else{
		    jcbAutoMount.setSelected(false);
			jcbAutoRefresh.setEnabled(false); //refresh at startup is only if it is auto-mounted
		}
		if (TRUE.equals(device.getProperty(DEVICE_OPTION_AUTO_REFRESH))){
			jcbAutoRefresh.setSelected(true);
		}
		else{
		    jcbAutoRefresh.setSelected(false);
		}
		if (jcbSynchronized.getItemCount()==0){
			jcboxSynchronized.setEnabled(false);
			jcbSynchronized.setEnabled(false);
			jrbBidirSynchro.setEnabled(false);
		}
		String sSynchroSource = device.getProperty(DEVICE_OPTION_SYNCHRO_SOURCE); 
		if ( sSynchroSource != null){
			jrbBidirSynchro.setEnabled(true);
			jrbUnidirSynchro.setEnabled(true);
			jcboxSynchronized.setSelected(true);
			jcboxSynchronized.setEnabled(true);
			jcbSynchronized.setEnabled(true);
			jcbSynchronized.setSelectedIndex(alDevices.indexOf(DeviceManager.getDevice(sSynchroSource)));
			if (DEVICE_OPTION_SYNCHRO_MODE_BI.equals(device.getProperty(DEVICE_OPTION_SYNCHRO_MODE))){
				jrbBidirSynchro.setSelected(true);
			}
			else{
				jrbUnidirSynchro.setSelected(true);
			}
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == jcbAutoMount) {
			if (jcbAutoMount.isSelected()) {
				jcbAutoRefresh.setEnabled(true);
			} else {
				jcbAutoRefresh.setSelected(false);
				jcbAutoRefresh.setEnabled(false);
			}
		} else if (e.getSource() == jcboxSynchronized) {
			if (jcboxSynchronized.isSelected()) {
				jcbSynchronized.setEnabled(true);
				jrbBidirSynchro.setEnabled(true);
				jrbUnidirSynchro.setEnabled(true);
			} else {
				jcbSynchronized.setEnabled(false);
				jrbBidirSynchro.setEnabled(false);
				jrbUnidirSynchro.setEnabled(false);
			}
		} 
		else if (e.getSource() == jbOk){
			//surface checks
			if ( jtfUrl.getText().trim().equals("")){ //$NON-NLS-1$
				Messages.showErrorMessage("021"); //$NON-NLS-1$
				this.setVisible(true);
				return;
			}
			if ( jtfName.getText().trim().equals("")){ //$NON-NLS-1$
				Messages.showErrorMessage("022"); //$NON-NLS-1$
				this.setVisible(true);
				return;
			}
			if (bNew){
				//check device availibility 
				String sCode = DeviceManager.checkDeviceAvailablity(jtfName.getText(),jcbType.getSelectedIndex(),jtfUrl.getText(),jtfMountPoint.getText());
				if (!sCode.equals("0")){ //$NON-NLS-1$
				    Messages.showErrorMessage(sCode);
				    this.setVisible(true); //display wizzard window which has been hiden by the error window
				    return;
				}
				device = DeviceManager.registerDevice(jtfName.getText(),jcbType.getSelectedIndex(),jtfUrl.getText(),jtfMountPoint.getText());
			}
			device.setProperty(DEVICE_OPTION_AUTO_MOUNT,Boolean.toString(jcbAutoMount.isSelected()));
			device.setProperty(DEVICE_OPTION_AUTO_REFRESH,Boolean.toString(jcbAutoRefresh.isSelected()));
			if (jcbSynchronized.isEnabled() && jcbSynchronized.getSelectedItem() != null){
				device.setProperty(DEVICE_OPTION_SYNCHRO_SOURCE,((Device)alDevices.get(jcbSynchronized.getSelectedIndex())).getId());
				if (jrbBidirSynchro.isSelected()){
					device.setProperty(DEVICE_OPTION_SYNCHRO_MODE,DEVICE_OPTION_SYNCHRO_MODE_BI);
				}
				else{
					device.setProperty(DEVICE_OPTION_SYNCHRO_MODE,DEVICE_OPTION_SYNCHRO_MODE_UNI);
				}
			}
			else{  //no synchro
				device.removeProperty(DEVICE_OPTION_SYNCHRO_MODE);
				device.removeProperty(DEVICE_OPTION_SYNCHRO_OPT1);
				device.removeProperty(DEVICE_OPTION_SYNCHRO_SOURCE);
			}
			if (jcbRefresh.isSelected() && bNew ){
				try{
					device.refresh(true);
				}
				catch(Exception e2){
					Log.error("112",device.getName(),e2); //$NON-NLS-1$
					Messages.showErrorMessage("112",device.getName()); //$NON-NLS-1$
				}
			}
			else{
				ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH));  //refresh trees with empty device
			}
			dispose();
			if (bNew){
				InformationJPanel.getInstance().setMessage(Messages.getString("DeviceWizard.44"),InformationJPanel.INFORMATIVE);  //$NON-NLS-1$
			}
		}
		else if (e.getSource() == jbCancel){
			dispose();  //close window
		}
		else if (e.getSource() == jbUrl){
			JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(true,false));
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));//$NON-NLS-1$
			jfc.setMultiSelectionEnabled(false);
			String sUrl =jtfUrl.getText(); 
			if (!sUrl.equals("")){  //if url is already set, use it as root directory //$NON-NLS-1$
			    jfc.setCurrentDirectory(new File(sUrl));
			}
			int returnVal = jfc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File file = jfc.getSelectedFile();
				jtfUrl.setText(file.getAbsolutePath());	
			}
		}
		else if (e.getSource() == jbUrlMountPoint){
			JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(true,false));
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle(Messages.getString("DeviceWizard.47"));//$NON-NLS-1$
			jfc.setMultiSelectionEnabled(false);
			String sMountPoint = jtfMountPoint.getText(); 
			if (!sMountPoint.equals("")){  //if url is already set, use it as root directory //$NON-NLS-1$
			    jfc.setCurrentDirectory(new File(sMountPoint));
			}
			int returnVal = jfc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File file = jfc.getSelectedFile();
				jtfMountPoint.setText(file.getAbsolutePath());	
			}
		}
		else if(e.getSource() == jcbType){
			switch(jcbType.getSelectedIndex()){
			case 0: //directory
				jcbAutoMount.setSelected(true);
				break;
			case 1: //file cd
				jcbAutoMount.setSelected(false);
				break;
			case 2: //network drive
				jcbAutoMount.setSelected(true);
				break;
			case 3: //ext dd
				jcbAutoMount.setSelected(true);
				break;
			case 4: //player
				jcbAutoMount.setSelected(false);
				break;
            case 5: //P2P
               jcbAutoMount.setSelected(false);
                break;
			}
		}
	}
}
