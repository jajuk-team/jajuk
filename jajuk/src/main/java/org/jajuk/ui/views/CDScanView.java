/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ext.SwingWorker;

/**
 * Scan CD to build the collection as fast as possible
 * <p>
 * Configuration perspective *
 * <p>
 * Singleton
 */
public class CDScanView extends ViewAdapter implements ActionListener {

	private static final long serialVersionUID = 1L;

	/** Self instance */
	private static CDScanView cds;

	JLabel jlName;

	JTextField jtfName;

	JLabel jlMountPoint;

	JTextField jtfMountPoint;

	JButton jbScan;

	JButton jbUrl;

	/** Return self instance */
	public static synchronized CDScanView getInstance() {
		if (cds == null) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#display()
	 */
	public void initUI() {
		double p = TableLayout.PREFERRED;
		double[][] dSize = {
				{ 10,p,p,p },
				{ 10, p,p,p } };
		TableLayout layout = new TableLayout(dSize);
		layout.setHGap(20);
		layout.setVGap(20);
		setLayout(layout);
		jlName = new JLabel(Messages.getString("CDScanView.0")); 
		jlName.setToolTipText(Messages.getString("CDScanView.1")); 
		jtfName = new JTextField(10);
		jtfName.setToolTipText(Messages.getString("CDScanView.2")); 
		jlMountPoint = new JLabel(Messages.getString("CDScanView.3")); 
		jlMountPoint.setToolTipText(Messages.getString("CDScanView.4")); 
		jtfMountPoint = new JTextField(10);
		jtfMountPoint.setToolTipText(Messages.getString("CDScanView.5")); 
		jbScan = new JButton(
				Messages.getString("CDScanView.6"), IconLoader.ICON_REFRESH); 
		jbScan.setToolTipText(Messages.getString("CDScanView.18")); 
		jbScan.addActionListener(this);
		jbUrl = new JButton(IconLoader.ICON_OPEN_FILE); 
		jbUrl.setToolTipText(Messages.getString("CDScanView.19")); 
		jbUrl.addActionListener(this);
		//Add items
		add(jlName, "1,1"); 
		add(jtfName, "2,1"); 
		add(jlMountPoint, "1,2"); 
		add(jtfMountPoint, "2,2"); 
		add(jbUrl, "3,2");
		add(jbScan, "1,3,3,3");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("CDScanView.12"); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jbScan) {
			SwingWorker sw = new SwingWorker() {
				public Object construct() {
					if (!"".equals(jtfName.getText().trim()) && !"".equals(jtfMountPoint.getText().trim())) {  
						Device device = null;
						device = DeviceManager.getInstance().registerDevice(
								jtfName.getText().trim(), 1,
								jtfMountPoint.getText().trim());
						device.setProperty(XML_DEVICE_MOUNT_POINT,
								jtfMountPoint.getText().trim());
						try {
							device.mount();
							device.refresh(false); // refresh synchronously
							device.unmount(true, true);
						} catch (Exception ex) {
							DeviceManager.getInstance().removeDevice(device);
							Messages.showErrorMessage("016"); 
							// refresh views
							ObservationManager.notify(new Event(
									EventSubject.EVENT_DEVICE_REFRESH));
						}
					}
					return null;

				}

				public void finished() {
					jtfName.setText(""); 
					jtfName.requestFocusInWindow();
				}
			};
			sw.start();
		} else if (e.getSource() == jbUrl) {
			JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(
					JajukFileFilter.DirectoryFilter.getInstance()));
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));
			jfc.setMultiSelectionEnabled(false);
			String sMountPoint = jtfMountPoint.getText();
			if (!sMountPoint.equals("")) { // if url is already set, use it
				// as root directory
				// 
				jfc.setCurrentDirectory(new File(sMountPoint));
			}
			int returnVal = jfc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File file = jfc.getSelectedFile();
				jtfMountPoint.setText(file.getAbsolutePath());
			}
		}
	}

}
