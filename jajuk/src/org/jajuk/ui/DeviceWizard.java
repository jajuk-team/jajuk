/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,
 * USA. $Log$
 * USA. Revision 1.4  2003/11/18 18:58:07  bflorat
 * USA. 18/11/2003
 * USA.
 * USA. Revision 1.3  2003/11/16 17:57:18  bflorat
 * USA. 16/11/2003
 * USA.
 * USA. Revision 1.2  2003/11/13 18:56:56  bflorat
 * USA. 13/11/2003
 * USA.
 * USA. Revision 1.1  2003/11/11 20:35:43  bflorat
 * USA. 11/11/2003
 * USA.
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
import javax.swing.ImageIcon;
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
	JRadioButton jrbFullSynchro;
	JRadioButton jrbPartialSynchro;
	JCheckBox jcb1;
	JCheckBox jcb2;
	JCheckBox jcb3;
	JPanel jpButtons;
	JButton jbOk;
	JButton jbCancel;
	

	public DeviceWizard() {
		super("Device wizard");
		setSize(800, 600);
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
		jbUrl = new JButton(new ImageIcon(ICON_OPEN_FILE));
		jbUrl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		jbUrl.addActionListener(this);
		jlMountPoint = new JLabel("Unix mount Point : ");
		jtfMountPoint = new JTextField();
		if (System.getProperties().get("os.name").equals("windows")){
			jlMountPoint.setEnabled(false);
			jtfMountPoint.setEnabled(false);
		}
		jcbRefresh = new JCheckBox("Perform an immediate refresh");
		jcbRefresh.setSelected(true);
		jcbRefresh.addActionListener(this);
		jcbAutoMount = new JCheckBox("Auto mount at startup");
		jcbAutoMount.addActionListener(this);
		jcbAutoRefresh = new JCheckBox("Auto refresh at startup");
		jcbAutoRefresh.setEnabled(false);
		jcboxSynchronized = new JCheckBox("Synchronized with : ");
		jcboxSynchronized.addActionListener(this);
		jcbSynchronized = new JComboBox();
		jcbSynchronized.setEnabled(false);
		ArrayList alDevices = DeviceManager.getDevices();
		Iterator it = alDevices.iterator();
		while (it.hasNext()) {
			Device device = (Device) it.next();
			jcbSynchronized.addItem(device.getName());
		}
		bgSynchro = new ButtonGroup();
		jrbFullSynchro = new JRadioButton("Full synchronization");
		jrbFullSynchro.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		jrbFullSynchro.setEnabled(false);  
		jrbFullSynchro.setSelected(true);//default synchro mode
		jrbFullSynchro.addActionListener(this);
		jrbPartialSynchro = new JRadioButton("Partial synchronization");
		jrbPartialSynchro.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		jrbPartialSynchro.setEnabled(false);
		jrbPartialSynchro.addActionListener(this);
		bgSynchro.add(jrbFullSynchro);
		bgSynchro.add(jrbPartialSynchro);
		jcb1 = new JCheckBox("If a track is deleted from this device, delete it from source device");
		jcb1.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
		jcb1.setEnabled(false);
		jcb2 = new JCheckBox("If a track is deleted from source device, delete it from this device");
		jcb2.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
		jcb2.setEnabled(false);
		jcb3 = new JCheckBox("If a track is desynchronized from source device, delete it from this device");
		jcb3.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
		jcb3.setEnabled(false);
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
				20, 20, 20, 20, 20, 20, 20, 20, 20, 20 }
		};
		jp2 = new JPanel();
		jp2.setLayout(new TableLayout(size2));
		jp2.setBorder(BorderFactory.createEmptyBorder(0, 15, 25, 15));
		jp2.add(jrbFullSynchro, "0,1");
		jp2.add(jrbPartialSynchro, "0,3");
		jp2.add(jcb1, "0,5");
		jp2.add(jcb2, "0,7");
		jp2.add(jcb3, "0,9");

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
				jrbFullSynchro.setEnabled(true);
				jrbPartialSynchro.setEnabled(true);
			} else {
				jcbSynchronized.setEnabled(false);
				jrbFullSynchro.setEnabled(false);
				jrbFullSynchro.setSelected(true);
				jrbPartialSynchro.setEnabled(false);
				jrbFullSynchro.setSelected(false);
				jcb1.setEnabled(false);
				jcb1.setSelected(false);
				jcb2.setEnabled(false);
				jcb2.setSelected(false);
				jcb3.setEnabled(false);
				jcb3.setSelected(false);
			}
		} else if (e.getSource() == jrbFullSynchro) {
			if (jrbFullSynchro.isSelected()) {
				jcb1.setEnabled(false);
				jcb1.setSelected(false);
				jcb2.setEnabled(false);
				jcb2.setSelected(false);
				jcb3.setEnabled(false);
				jcb3.setSelected(false);
			}
		}
		else if (e.getSource() == jrbPartialSynchro) {
			if (jrbPartialSynchro.isSelected()) {
				jcb1.setEnabled(true);
				jcb2.setEnabled(true);
				jcb3.setEnabled(true);
			} else {
				jcb1.setEnabled(false);
				jcb1.setSelected(false);
				jcb2.setEnabled(false);
				jcb2.setSelected(false);
				jcb3.setEnabled(false);
				jcb3.setSelected(false);
			}
		}
		else if (e.getSource() == jbOk){
			Device device = DeviceManager.registerDevice(jtfName.getText(),jcbType.getSelectedIndex(),jtfUrl.getText(),jtfMountPoint.getText());
			if (jcbRefresh.isSelected()){
				device.refresh();
			}
			ViewManager.notify(EVENT_VIEW_REFRESH_REQUEST,DeviceView.getInstance());
			Messages.showInfoMessage("Device_created");//$NON-NLS-1$
			dispose();
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
				if (jtfMountPoint.isEnabled()){
					jtfMountPoint.setText(file.getAbsolutePath());
				}
			}
		
		}
	}
}
