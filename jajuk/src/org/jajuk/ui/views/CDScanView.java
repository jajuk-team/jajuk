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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import layout.TableLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;

import com.sun.SwingWorker;


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
	JButton jbUrl;
	
	/**Return self instance*/
	public static synchronized CDScanView getInstance(){
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
				{fXSeparator,0.25,fXSeparator,0.5,fXSeparator,40,fXSeparator},
				{fYSeparator,20,fYSeparator,20,fYSeparator,20,fYSeparator}
		};
		setLayout(new TableLayout(dSize));
		jlName = new JLabel(Messages.getString("CDScanView.0")); //$NON-NLS-1$
		jlName.setToolTipText(Messages.getString("CDScanView.1")); //$NON-NLS-1$
		jtfName =  new JTextField(10);
		jtfName.setToolTipText(Messages.getString("CDScanView.2")); //$NON-NLS-1$
		jlMountPoint = new JLabel(Messages.getString("CDScanView.3")); //$NON-NLS-1$
		jlMountPoint.setToolTipText(Messages.getString("CDScanView.4")); //$NON-NLS-1$
		jtfMountPoint = new JTextField(10);
		jtfMountPoint.setToolTipText(Messages.getString("CDScanView.5")); //$NON-NLS-1$
		jbScan = new JButton(Messages.getString("CDScanView.6"),Util.getIcon(ICON_REFRESH)); //$NON-NLS-1$
		jbScan.setToolTipText(Messages.getString("CDScanView.18")); //$NON-NLS-1$
		jbScan.addActionListener(this);
		jbUrl = new JButton(Util.getIcon(ICON_OPEN_FILE)); //$NON-NLS-1$
		jbUrl.setToolTipText(Messages.getString("CDScanView.19")); //$NON-NLS-1$
		jbUrl.addActionListener(this);
		add(jlName,"1,1"); //$NON-NLS-1$
		
		add(jtfName,"3,1"); //$NON-NLS-1$
		add(jlMountPoint,"1,3"); //$NON-NLS-1$
		add(jtfMountPoint,"3,3"); //$NON-NLS-1$
		add(jbScan,"1,5"); //$NON-NLS-1$
		add(jbUrl,"5,3"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("CDScanView.12");	 //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.CDScanView"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jbScan){
			SwingWorker sw = new SwingWorker() {
				public Object construct() {
					if ( !"".equals(jtfName.getText().trim()) && !"".equals(jtfMountPoint.getText().trim())){ //$NON-NLS-1$ //$NON-NLS-2$
						Device device = DeviceManager.registerDevice(jtfName.getText().trim(),1,jtfMountPoint.getText().trim(),jtfMountPoint.getText().trim());
						if (device == null){ //means device name is already token
							Messages.showErrorMessage("019"); //$NON-NLS-1$
							return null;
						}
						try{
							device.mount();
							device.refresh(false); //refresh synchronously
							device.unmount(true);
						}
						catch(Exception ex){
							DeviceManager.removeDevice(device);
							Messages.showErrorMessage("016"); //$NON-NLS-1$
						}
					}
					return null;
					
				}	
				public void finished() {
					jtfName.setText(""); //$NON-NLS-1$
					jtfName.requestFocus();
				}
			};
			sw.start();
		}
		else if (e.getSource() == jbUrl){
			JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(true,false));
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));//$NON-NLS-1$
			jfc.setMultiSelectionEnabled(false);
			int returnVal = jfc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File file = jfc.getSelectedFile();
				jtfMountPoint.setText(file.getAbsolutePath());	
			}
		}
	}
}
