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

package org.jajuk.ui.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import layout.TableLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.DeviceWizard;
import org.jajuk.ui.IView;
import org.jajuk.ui.ViewManager;

/**
 *  Device view used to create and modify Jajuk devices
 * <p>Configuration perspective
 * <p>Singleton
 *
 * @author     bflorat
 * @created    8 nov. 2003
 */
public class DeviceView extends ViewAdapter implements IView,ITechnicalStrings,ActionListener {

	static private DeviceView dv; //self instance
	
	JToolBar jtbButtons;
		JButton jbNew;
		JButton jbDelete;
		JButton jbProperties;
		JButton jbMount;
		JButton jbUnmount;
		JButton jbTest;
		JButton jbRefresh;
		JButton jbSynchro;
	JPanel jpDevices;
			
	JPopupMenu jpmenu;
	JMenuItem jmiDelete;
	JMenuItem jmiProperties;
	JMenuItem jmiMount;
	JMenuItem jmiUnmount;
	JMenuItem jmiTest;
	JMenuItem jmiRefresh;
	JMenuItem jmiSynchronize;
	
	Device dSelected;
	DeviceItem diSelected;
	
	public DeviceView(){
		//buttons
		jtbButtons = new JToolBar();
		jtbButtons.setRollover(true);
		jtbButtons.setFloatable(false);
		jtbButtons.setBorder(BorderFactory.createEtchedBorder());
		
		jbNew = new JButton(new ImageIcon(ICON_NEW));
		jbNew.setActionCommand(EVENT_DEVICE_NEW);
		jbNew.addActionListener(this);
		jbNew.setToolTipText("Add a device");
		
		jbDelete = new JButton(new ImageIcon(ICON_DELETE));
		jbDelete.setActionCommand(EVENT_DEVICE_DELETE);
		jbDelete.addActionListener(this);
		jbDelete.setToolTipText("Remove a device");
		
		jbProperties = new JButton(new ImageIcon(ICON_PROPERTIES));
		jbProperties.setActionCommand(EVENT_DEVICE_PROPERTIES);
		jbProperties.addActionListener(this);
		jbProperties.setToolTipText("Selected device properties");
		
		jbMount = new JButton(new ImageIcon(ICON_MOUNT));
		jbMount.setActionCommand(EVENT_DEVICE_MOUNT);
		jbMount.addActionListener(this);
		jbMount.setToolTipText("Mount selected device");
		
		jbUnmount = new JButton(new ImageIcon(ICON_UNMOUNT));
		jbUnmount.setActionCommand(EVENT_DEVICE_UNMOUNT);
		jbUnmount.addActionListener(this);
		jbUnmount.setToolTipText("Unmount selected device");
			
		jbTest = new JButton(new ImageIcon(ICON_TEST));
		jbTest.setActionCommand(EVENT_DEVICE_TEST);
		jbTest.addActionListener(this);
		jbTest.setToolTipText("Test selected device availability");
		
		jbRefresh = new JButton(new ImageIcon(ICON_REFRESH));
		jbRefresh.setActionCommand(EVENT_DEVICE_REFRESH);
		jbRefresh.addActionListener(this);
		jbRefresh.setToolTipText("Refresh selected device");
		
		jbSynchro = new JButton(new ImageIcon(ICON_SYNCHRO));
		jbSynchro.setActionCommand(EVENT_DEVICE_SYNCHRO);
		jbSynchro.addActionListener(this);
		jbSynchro.setToolTipText("Synchronize selected device");
		
		jtbButtons.add(jbNew);
		jtbButtons.addSeparator();
		jtbButtons.add(jbDelete);
		jtbButtons.addSeparator();
		jtbButtons.add(jbProperties);
		jtbButtons.addSeparator();
		jtbButtons.add(jbMount);
		jtbButtons.addSeparator();
		jtbButtons.add(jbUnmount);
		jtbButtons.addSeparator();
		jtbButtons.add(jbTest);
		jtbButtons.addSeparator();
		jtbButtons.add(jbRefresh);
		jtbButtons.addSeparator();
		jtbButtons.add(jbSynchro);
		jtbButtons.addSeparator();
		//devices
		jpDevices = new JPanel();
		jpDevices.setPreferredSize(new Dimension(200,1000));
		jpDevices.setLayout(new FlowLayout(FlowLayout.LEFT));
		jpDevices.setBorder(BorderFactory.createEtchedBorder());
		
		//Popup menus
		jpmenu =  new JPopupMenu();
	
		jmiMount = new JMenuItem("Mount",new ImageIcon(ICON_MOUNT)); 
		jmiMount.addActionListener(this);
		jmiMount.setActionCommand(EVENT_DEVICE_MOUNT);
		jpmenu.add(jmiMount);
		
		jmiUnmount = new JMenuItem("Unmount",new ImageIcon(ICON_UNMOUNT)); 
		jmiUnmount.addActionListener(this);
		jmiUnmount.setActionCommand(EVENT_DEVICE_UNMOUNT);
		jpmenu.add(jmiUnmount);
		
		jmiTest = new JMenuItem("Test",new ImageIcon(ICON_TEST));
		jmiTest.addActionListener(this);
		jmiTest.setActionCommand(EVENT_DEVICE_TEST);
		jpmenu.add(jmiTest);
		
		jmiRefresh =new JMenuItem("Refresh",new ImageIcon(ICON_REFRESH)); 
		jmiRefresh.addActionListener(this);
		jmiRefresh.setActionCommand(EVENT_DEVICE_REFRESH);
		jpmenu.add(jmiRefresh);
		
		jmiSynchronize =new JMenuItem("Synchronize",new ImageIcon(ICON_SYNCHRO)); 
		jmiSynchronize.addActionListener(this);
		jmiSynchronize.setActionCommand(EVENT_DEVICE_SYNCHRO);
		jpmenu.add(jmiSynchronize);
		
		jmiDelete = new JMenuItem("Delete device",new ImageIcon(ICON_DELETE));
		jmiDelete.addActionListener(this);
		jmiDelete.setActionCommand(EVENT_DEVICE_DELETE);
		jpmenu.add(jmiDelete);
	
		jmiProperties = new JMenuItem("Get properties",new ImageIcon(ICON_PROPERTIES));
		jmiProperties.addActionListener(this);
		jmiProperties.setActionCommand(EVENT_DEVICE_PROPERTIES);
		jpmenu.add(jmiProperties);
		
		//New device
		DeviceItem diNew = new DeviceItem(ICON_DEVICE_NEW,"New");
		diNew.setToolTipText("Add a device");
		jpDevices.add(diNew);
		diNew.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
					DeviceWizard dw = new DeviceWizard();
					dw.updateWidgetsDefault();
			}
		});
		
		//Add devices
		ArrayList alDevices = DeviceManager.getDevices();
		Iterator it = alDevices.iterator();
		while (it.hasNext()){
			final Device device = (Device)it.next();
			String sIcon = ICON_DEVICE_DIRECTORY_MOUNTED;
			switch (device.getDeviceType()){
				case 0 : 
					if ( device.isMounted()){
						sIcon = ICON_DEVICE_DIRECTORY_MOUNTED;
					}
					else{
						sIcon = ICON_DEVICE_DIRECTORY_UNMOUNTED;
					}
					break;
				case 1 : 
					if ( device.isMounted()){
						sIcon = ICON_DEVICE_CD_MOUNTED;
					}
					else{
						sIcon = ICON_DEVICE_CD_UNMOUNTED;
					}
					break;
				case 2 : 
					if ( device.isMounted()){
						sIcon = ICON_DEVICE_CD_AUDIO_MOUNTED;
					}
					else{
						sIcon = ICON_DEVICE_CD_AUDIO_UNMOUNTED;
					}
				break;
				case 3 : 
					if ( device.isMounted()){
						sIcon = ICON_DEVICE_REMOTE_MOUNTED;
					}
					else{
						sIcon = ICON_DEVICE_REMOTE_UNMOUNTED;
					}
				break;
				case 4 : 
					if ( device.isMounted()){
						sIcon = ICON_DEVICE_EXT_DD_MOUNTED;
					}
					else{
						sIcon = ICON_DEVICE_EXT_DD_UNMOUNTED;
					}
					break;
				case 5 : 
					if ( device.isMounted()){
						sIcon = ICON_DEVICE_PLAYER_MOUNTED;
					}
					else{
						sIcon = ICON_DEVICE_PLAYER_UNMOUNTED;
					}
					break;
			}
			final DeviceItem di = new DeviceItem(sIcon,device.getName());
			di.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (di == diSelected){
						if (e.getButton() == 3){  //left button
							jpmenu.show(e.getComponent(),e.getX(),e.getY());
							return;
						}
						else {
							DeviceWizard dw = new DeviceWizard();
							dw.updateWidgets(dSelected);
						}
					}
					//remove old device item border
					if (diSelected!=null){
						diSelected.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
					}
					//set new device item
					diSelected = di;
					dSelected = device;
					diSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
				}
			});
			di.setToolTipText(device.getDeviceTypeS());
			jpDevices.add(di);
		}
		
		//add 
	double size[][] =
							{{0.99},
							 {30,0.99}};
		setLayout(new TableLayout(size));
		add(jtbButtons,"0,0");
		add(new JScrollPane(jpDevices),"0,1");
	}
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#getName()
	 */
	public String getName() {
		return VIEW_NAME_DEVICES;
	}
	

	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#setVisible(boolean)
	 */
	public void setVisible(boolean pVisible) {
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#getComponent()
	 */
	public Component getComponent() {
		return this;
	}
	
	/**
	 * Singleton implementation
	 * @return
	 */
	public static DeviceView getInstance(){
		if ( dv == null){
			dv = new DeviceView();
		}
		return dv;
	}

	
	public void actionPerformed(ActionEvent ae){
		if (ae.getActionCommand().equals(EVENT_DEVICE_NEW)){
			DeviceWizard dw = new DeviceWizard();
			dw.updateWidgetsDefault();
		}
		else if (ae.getActionCommand().equals(EVENT_DEVICE_DELETE)){
			DeviceManager.removeDevice(dSelected);
			jpDevices.remove(diSelected);
			dSelected = null;
			ViewManager.notify(EVENT_VIEW_REFRESH_REQUEST,this);
		}
		else if (ae.getActionCommand().equals(EVENT_DEVICE_MOUNT)){
			try{
				dSelected.mount();
				ViewManager.notify(EVENT_VIEW_REFRESH_REQUEST,this);
			}
			catch(Exception e){
				Messages.showErrorMessage("112");
			}
		}
		else if (ae.getActionCommand().equals(EVENT_DEVICE_UNMOUNT)){
			try{
				dSelected.unmount();
				ViewManager.notify(EVENT_VIEW_REFRESH_REQUEST,this);
			}
			catch(Exception e){
				Messages.showErrorMessage("121");
			}
		}
		else if (ae.getActionCommand().equals(EVENT_DEVICE_PROPERTIES)){
			DeviceWizard dw = new DeviceWizard();
			dw.updateWidgets(dSelected);
		}
		else if (ae.getActionCommand().equals(EVENT_DEVICE_REFRESH)){
			dSelected.refresh();
			//TODO refresh window with progress bar, infos and OK/Cancel buttons
		}
		else if (ae.getActionCommand().equals(EVENT_DEVICE_SYNCHRO)){
			dSelected.synchronize();
		}
		else if (ae.getActionCommand().equals(EVENT_DEVICE_TEST)){
			if (dSelected.test()){
				Messages.showInfoMessage("Test_OK",new ImageIcon(ICON_OK));
			}
			else{
				Messages.showInfoMessage("Test_NO",new ImageIcon(ICON_KO));
			}
		}
	}	


	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("View_Description_Devices");
	}
	
		
}

	
/**
 * A device icon + text
 *  Type description
 *
 * @author     bflorat
 * @created    8 nov. 2003
 */
class DeviceItem extends JPanel{
	
	/** Associated device */
	Device device;
	
	/**
	 * Constructor
	 */
	DeviceItem(String sIcon,String sName){
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		JLabel jlIcon = new JLabel(new ImageIcon(sIcon)); 
		add(jlIcon);
		JLabel jlName = new JLabel(sName);
		add(jlName);
	}
	
		
	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @param device The device to set.
	 */
	public void setDevice(Device device) {
		this.device = device;
	}

}
