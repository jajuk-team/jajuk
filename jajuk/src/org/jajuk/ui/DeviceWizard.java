/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import layout.TableLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.views.DeviceView;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Device creation wizzard
 * 
 * @author bflorat @created 9 nov. 2003
 */
public class DeviceWizard extends JFrame implements ActionListener,ITechnicalStrings {
	JPanel jpMain;
	JPanel jp1;
	JLabel jlType;
	JComboBox jcbType;
	JLabel jlName;
	JTextField jtfName;
	JLabel jlUrl;
	JTextField jtfUrl;
	JButton jbUrl;
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
	JCheckBox jcb1;
	JPanel jpButtons;
	JButton jbOk;
	JButton jbCancel;
	
	/**New device flag*/
	private boolean bNew = true;
	
	/**Current device*/
	Device device;
	
	/** All devices expect itself */
	ArrayList alDevices = new ArrayList(10);
	
	
	public DeviceWizard() {
		super("Device wizard");
		setSize(800, 500);
		setLocation(org.jajuk.Main.jframe.getX()+100,org.jajuk.Main.jframe.getY()+100);
		jpMain = new JPanel();
		jpMain.setLayout(new BoxLayout(jpMain,BoxLayout.Y_AXIS));
		jp1 = new JPanel();
		jp1.setBorder(BorderFactory.createEmptyBorder(25, 15, 0, 15));
		double size1[][] = { { 0.5, 0.45,0.05 }, {
				20, 20, 20, 20, 20,20,20, 20, 20, 20, 20,20,20 }
		};
		jp1.setLayout(new TableLayout(size1));
		jlType = new JLabel("Device Type : ");
		jcbType = new JComboBox();
		for (int i = 0; i < Device.sDeviceTypes.length; i++) {
			jcbType.addItem(Device.sDeviceTypes[i]);
		}
		jlName = new JLabel("Device name : ");
		jtfName = new JTextField();
		jlUrl = new JLabel("Device url : ");
		jtfUrl = new JTextField();
		jbUrl = new JButton(Util.getIcon(ICON_OPEN_FILE));
		jbUrl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		jbUrl.addActionListener(this);
		jlMountPoint = new JLabel("Unix mount Point : ");
		jtfMountPoint = new JTextField();
		String sOS = (String)System.getProperties().get("os.name");
		if (sOS.trim().toLowerCase().lastIndexOf("windows")!=-1){
			jlMountPoint.setEnabled(false);
			jtfMountPoint.setEnabled(false);
		}
		jcbRefresh = new JCheckBox("Perform an instant refresh");
		jcbRefresh.addActionListener(this);
		jcbAutoMount = new JCheckBox("Auto mount at startup");
		jcbAutoMount.addActionListener(this);
		jcbAutoRefresh = new JCheckBox("Auto refresh at startup");
		jcboxSynchronized = new JCheckBox("Synchronized with : ");
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
		//Default automount behavior
		jcbType.addActionListener(this);
		bgSynchro = new ButtonGroup();
		jrbUnidirSynchro = new JRadioButton("Unidirectional synchronization");
		jrbUnidirSynchro.setToolTipText("All new files found on the source device are copied into this device. Nothing will be written to source device");
		jrbUnidirSynchro.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		jrbUnidirSynchro.addActionListener(this);
		jrbBidirSynchro = new JRadioButton("Bidirectional synchronization");
		jrbBidirSynchro.setToolTipText("All new files found on the one device are copied to the other one");
		jrbBidirSynchro.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		jrbBidirSynchro.setEnabled(false);
		jrbBidirSynchro.addActionListener(this);
		bgSynchro.add(jrbBidirSynchro);
		bgSynchro.add(jrbUnidirSynchro);
		jcb1 = new JCheckBox("If a track is desynchronized from source device, delete it from this device (USE WITH CARE) ");
		jcb1.setToolTipText("If a track is desynchronized from source device, it will be deleted from this device (USE WITH CARE)");
		jcb1.setEnabled(false);
		jcb1.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		jp1.add(jlType, "0,0");
		jp1.add(jcbType, "1,0");
		jp1.add(jlName, "0,2");
		jp1.add(jtfName, "1,2");
		jp1.add(jlUrl, "0,4");
		jp1.add(jtfUrl, "1,4");
		jp1.add(jbUrl, "2,4");
		jp1.add(jlMountPoint, "0,6");
		jp1.add(jtfMountPoint, "1,6");
		jp1.add(jcbRefresh, "0,8");
		jp1.add(jcbAutoMount, "0,10");
		jp1.add(jcbAutoRefresh, "1,10");
		jp1.add(jcboxSynchronized, "0,12");
		jp1.add(jcbSynchronized, "1,12");
		double size2[][] = { { 0.99 }, {
			20, 20, 20, 20, 20, 20, 20 }
		};
		jp2 = new JPanel();
		jp2.setLayout(new TableLayout(size2));
		jp2.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
		jp2.add(jrbUnidirSynchro, "0,1");
		jp2.add(jrbBidirSynchro, "0,3");
		//jp2.add(jcb1, "0,5");   //not featured for the moment
		if (jcbSynchronized.getItemCount()==0){
			jcboxSynchronized.setEnabled(false);
			jcbSynchronized.setEnabled(false);
			jrbBidirSynchro.setEnabled(false);
		}
		
		//buttons
		jpButtons = new JPanel();
		jpButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		jbOk = new JButton("OK");
		jbOk.requestFocus();
		jbOk.addActionListener(this);
		jbCancel = new JButton("Cancel");
		jbCancel.addActionListener(this);
		jpButtons.add(jbOk);
		jpButtons.add(jbCancel);
		
		jpMain.add(jp1);
		jpMain.add(jp2);
		jpMain.add(Box.createVerticalGlue());
		jpMain.add(jpButtons);
		setContentPane(jpMain);
		show();
	}
	
	/**
	 * Update widgets for default state
	 */
	public void updateWidgetsDefault(){
		jcbRefresh.setSelected(true);
		jcbAutoRefresh.setEnabled(false);
		jcbAutoMount.setSelected(true);
		jrbUnidirSynchro.setSelected(true);//default synchro mode
		jrbBidirSynchro.setEnabled(false);
		jcb1.setEnabled(false);
		jcb1.setSelected(false);
		
	}
	
	/**
	 * Update widgets for device property state 
	 */
	public void updateWidgets(Device device){
		bNew = false;
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
		updateWidgetsDefault();
		jcbType.setSelectedItem(device.getDeviceTypeS());
		jtfName.setText(device.getName());
		jtfUrl.setText(device.getUrl());
		jtfMountPoint.setText(device.getMountPoint());
		jcbRefresh.setEnabled(false); //no instant refresh for updates
		jcbRefresh.setSelected(false);
		if (TRUE.equals(device.getProperty(DEVICE_OPTION_AUTO_MOUNT))){
			jcbAutoMount.setSelected(true);
			jcbAutoRefresh.setEnabled(true);
		}
		if (TRUE.equals(device.getProperty(DEVICE_OPTION_AUTO_REFRESH))){
			jcbAutoRefresh.setEnabled(true);
			jcbAutoRefresh.setSelected(true);
		}
		if (jcbSynchronized.getItemCount()==0){
			jcboxSynchronized.setEnabled(false);
			jcbSynchronized.setEnabled(false);
			jrbBidirSynchro.setEnabled(false);
			jcb1.setEnabled(false);
		}
		String sSynchroSource = device.getProperty(DEVICE_OPTION_SYNCHRO_SOURCE); 
		if ( sSynchroSource != null){
			jrbBidirSynchro.setEnabled(true);
			jrbUnidirSynchro.setEnabled(true);
			jcb1.setEnabled(true);
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
			if (TRUE.equals(device.getProperty(DEVICE_OPTION_SYNCHRO_OPT1))){
				jcb1.setSelected(true);
			}
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
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
				jcb1.setEnabled(true);
			} else {
				jcbSynchronized.setEnabled(false);
				jrbBidirSynchro.setEnabled(false);
				jrbUnidirSynchro.setEnabled(false);
				jcb1.setEnabled(false);
			}
		} 
		else if (e.getSource() == jbOk){
			//surface checks
			if ( jtfUrl.getText().trim().equals("")){
				Messages.showErrorMessage("021");
				this.setVisible(true);
				return;
			}
			if ( jtfName.getText().trim().equals("")){
				Messages.showErrorMessage("022");
				this.setVisible(true);
				return;
			}
			if (bNew){
				device = DeviceManager.registerDevice(jtfName.getText(),jcbType.getSelectedIndex(),jtfUrl.getText(),jtfMountPoint.getText());
				if (device == null){ //means device name is already token
					Messages.showErrorMessage("019");
					this.setVisible(true); //display wizzard window whish has been hiden by the error window
					return;
				}
			}
			else{
				device.setDeviceType(jcbType.getSelectedIndex());
				device.setName(jtfName.getText());
				device.setUrl(jtfUrl.getText());
				device.setMountPoint(jtfMountPoint.getText());
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
					if (jcb1.isSelected()){
						device.setProperty(DEVICE_OPTION_SYNCHRO_OPT1,TRUE);
					}
					else{
						device.setProperty(DEVICE_OPTION_SYNCHRO_OPT1,FALSE);
					}
				}
			}
			else{  //no synchro
				device.removeProperty(DEVICE_OPTION_SYNCHRO_MODE);
				device.removeProperty(DEVICE_OPTION_SYNCHRO_OPT1);
				device.removeProperty(DEVICE_OPTION_SYNCHRO_SOURCE);
			}
			if (jcbRefresh.isSelected()){
				try{
					device.mount();
					device.refresh(true);
				}
				catch(Exception e2){
					Log.error("112",device.getName(),e2);
					Messages.showErrorMessage("112",device.getName());
				}
			}
			else{
				ObservationManager.notify(EVENT_DEVICE_REFRESH);  //refresh trees with empty device
			}
			ViewManager.notify(EVENT_VIEW_REFRESH_REQUEST,DeviceView.getInstance());
			dispose();
			if (bNew){
				Messages.showInfoMessage("Device_created");//$NON-NLS-1$
			}
		}
		else if (e.getSource() == jbCancel){
			dispose();  //close window
		}
		else if (e.getSource() == jbUrl){
			JFileChooser jfc = new JFileChooser("Please choose a directory");
			jfc.setMultiSelectionEnabled(false);
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = jfc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File file = jfc.getSelectedFile();
				jtfUrl.setText(file.getAbsolutePath());	
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
					case 2: //remote
						jcbAutoMount.setSelected(false);
						break;
					case 3: //ext dd
						jcbAutoMount.setSelected(false);
						break;
					case 4: //player
						jcbAutoMount.setSelected(false);
						break;
				}
		}
	}
}
