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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.util.Util;

import layout.TableLayout;


/**
 *Scan CD to build the collection as fast as possible
 * <p>Configuration perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
public class CDScanView extends ViewAdapter implements ActionListener {

	/**Self instance*/
	private static CDScanView cds;
	
	JLabel jlName;
	JTextField jtfName;
	JLabel jlMountPoint;
	JTextField jtfMountPoint;
	JButton jbScan;
	
	/**Return self instance*/
	public static CDScanView getInstance(){
		if (cds == null){
			cds = new CDScanView();
		}
		return cds;
	}
	
	/**
	 * Constructor
	 */
	public CDScanView() {
		cds = this;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		float fXSeparator = 0.05f;
		float fYSeparator = 0.15f;
		double[][] dSize={
			{fXSeparator,0.3,fXSeparator,0.5,fXSeparator},
			{fYSeparator,20,fYSeparator,20,fYSeparator,20,fYSeparator}
		};
		setLayout(new TableLayout(dSize));
		jlName = new JLabel("CD label : ");
		jlName.setToolTipText("Enter CD label. Ex: CD1, ROCK...");
		jtfName =  new JTextField(10);
		jtfName.setToolTipText("Enter CD label. Ex: CD1, ROCK...");
		jlMountPoint = new JLabel("Mount point : ");
		jlMountPoint.setToolTipText("Mount point where CD can be found. Ex: /cdrom on unix, e: on MS Windows...");
		jtfMountPoint = new JTextField(10);
		jtfMountPoint.setToolTipText("Mount point where CD can be found. Ex: /cdrom on unix, e: on MS Windows...");
		jbScan = new JButton("Scan",Util.getIcon(ICON_REFRESH));
		jbScan.addActionListener(this);
		add(jlName,"1,1");
		add(jtfName,"3,1");
		add(jlMountPoint,"1,3");
		add(jtfMountPoint,"3,3");
		add(jbScan,"1,5");
		
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "CD Scan view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.CDScanView";
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if ( !"".equals(jtfName.getText().trim()) && !"".equals(jtfMountPoint.getText().trim())){
			Device device = DeviceManager.registerDevice(jtfName.getText().trim(),1,jtfMountPoint.getText().trim(),jtfMountPoint.getText().trim());
			try{
				device.mount();
				ObservationManager.notify(EVENT_DEVICE_MOUNT);
				device.refresh();
				ObservationManager.notify(EVENT_DEVICE_REFRESH);
				do{
					Thread.sleep(500); //sleep to get sure refresh thread is realy started
				}
				while(!device.isRefreshing());
				synchronized(Device.bLock){  //wait refresh is done
					device.unmount(true);
				}
				jtfName.setName("");
				jtfName.requestFocus();
				ObservationManager.notify(EVENT_DEVICE_UNMOUNT);
				
			}
			catch(Exception ex){
				DeviceManager.removeDevice(device);
				Messages.showErrorMessage("016");
			}
		}
	}
}
