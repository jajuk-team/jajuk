/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui.wizard;

import org.jajuk.Main;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.ui.JajukJDialog;
import org.jajuk.ui.PathSelector;
import org.jajuk.ui.ToggleLink;
import org.jajuk.ui.perspectives.HelpPerspective;
import org.jajuk.ui.perspectives.SimplePerspective;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * First time Wizard
 */
public class FirstTimeWizard extends JajukJDialog implements ITechnicalStrings, ActionListener {
	private static final long serialVersionUID = 1L;

	JLabel jlLeftIcon;

	JPanel jpRightPanel;

	JLabel jlWelcome;

	JLabel jlFileSelection;

	JTextField jtfFileSelected;

	JButton jbFileSelection;

	PathSelector workspacePath;

	JLabel jlRefreshTime;

	JTextField jtfRefreshTime;

	JLabel jlMins;

	JCheckBox jcbHelp;

	JXCollapsiblePane advanced;

	JPanel jpButtons;

	JButton jbOk;

	JButton jbCancel;

	JPanel jpMain;

	/** Selected directory */
	private File fDir;

	/**
	 * First time wizard
	 */
	public FirstTimeWizard() {
		setTitle(Messages.getString("FirstTimeWizard.0"));
		int iX_SEPARATOR = 10;
		int iY_SEPARATOR = 10;
		double p = TableLayout.PREFERRED;
		jlLeftIcon = new JLabel(Util.getImage(IMAGE_SEARCH));
		jpRightPanel = new JPanel();
		jlWelcome = new JLabel(Messages.getString("FirstTimeWizard.1"));
		jlFileSelection = new JLabel(Messages.getString("FirstTimeWizard.2"));
		jbFileSelection = new JButton(IconLoader.ICON_OPEN_DIR);
		jtfFileSelected = new JTextField("");
		jtfFileSelected.setForeground(Color.BLUE);
		jtfFileSelected.setEditable(false);
		jbFileSelection.addActionListener(this);
		JLabel jlWorkspace = new JLabel(Messages.getString("FirstTimeWizard.7"));
		jlWorkspace.setToolTipText(Messages.getString("FirstTimeWizard.7"));
		workspacePath = new PathSelector(new JajukFileFilter(JajukFileFilter.DirectoryFilter
				.getInstance()), System.getProperty("user.home"));
		workspacePath.setToolTipText(Messages.getString("FirstTimeWizard.7"));

		jcbHelp = new JCheckBox(Messages.getString("FirstTimeWizard.4"));
		// Refresh time
		jlRefreshTime = new JLabel(Messages.getString("DeviceWizard.53"));
		jtfRefreshTime = new JTextField("5");// 5 mins by default
		jlMins = new JLabel(Messages.getString("DeviceWizard.54"));
		JPanel jpRefresh = new JPanel();

		double sizeRefresh[][] = { { p, 10, TableLayout.FILL, 10, p }, { p } };
		jpRefresh.setLayout(new TableLayout(sizeRefresh));
		jpRefresh.add(jlRefreshTime, "0,0");
		jpRefresh.add(jtfRefreshTime, "2,0");
		jpRefresh.add(jlMins, "4,0");
		// buttons
		jpButtons = new JPanel();
		jpButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		jbOk = new JButton(Messages.getString("OK"));
		jbOk.setEnabled(false);
		jbOk.addActionListener(this);
		jbCancel = new JButton(Messages.getString("Cancel"));
		jbCancel.addActionListener(this);
		jpButtons.add(jbOk);
		jpButtons.add(jbCancel);
		JPanel jpFileSelection = new JPanel();
		jpFileSelection.setLayout(new HorizontalLayout(iX_SEPARATOR));
		jpFileSelection.add(jbFileSelection);
		jpFileSelection.add(jlFileSelection);
		advanced = new JXCollapsiblePane();
		// Build the toggle link used to expand / collapse the panel
		ToggleLink toggle = new ToggleLink(Messages.getString("FirstTimeWizard.6"), advanced);
		advanced.setLayout(new VerticalLayout(iY_SEPARATOR));
		advanced.setCollapsed(true);
		advanced.add(jlWorkspace);
		advanced.add(workspacePath);
		advanced.add(jcbHelp);
	
		double[][] size = new double[][] { { iX_SEPARATOR, p, iX_SEPARATOR },
				{ iY_SEPARATOR, p, 60, p, p, p, p, p, iY_SEPARATOR } };
		TableLayout layout = new TableLayout(size);
		layout.setHGap(iX_SEPARATOR);
		layout.setVGap(iY_SEPARATOR);

		jpMain = (JPanel) getContentPane();
		jpMain.setLayout(layout);
		jpMain.add(jlWelcome, "1,1");
		jpMain.add(jpFileSelection, "1,2");
		jpMain.add(jtfFileSelected, "1,3");
		jpMain.add(jpRefresh, "1,4");
		jpMain.add(toggle, "1,5");
		jpMain.add(advanced, "1,6");
		jpMain.add(jpButtons, "1,7");

		getRootPane().setDefaultButton(jbOk);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jbCancel) {
			dispose(); // close window
		} else if (e.getSource() == jbFileSelection) {
			JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(
					JajukFileFilter.DirectoryFilter.getInstance()));
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle(Messages.getString("FirstTimeWizard.5"));
			jfc.setMultiSelectionEnabled(false);
			int returnVal = jfc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				fDir = jfc.getSelectedFile();
				// check device availability
				String sCode = DeviceManager.getInstance().checkDeviceAvailablity(fDir.getName(),
						0, fDir.getAbsolutePath(), fDir.getAbsolutePath(), true);
				if (!sCode.equals("0")) {
					Messages.showErrorMessage(sCode);
					jbOk.setEnabled(false);
					return;
				}
				jtfFileSelected.setText(fDir.getAbsolutePath());
				jbOk.setEnabled(true);
				jbOk.grabFocus();
			}
		} else if (e.getSource() == jbOk) {
			if (jcbHelp.isSelected()) {
				// set parameter perspective
				Main.setDefaultPerspective(HelpPerspective.class.getName());
			} else {
				// set Simple perspective
				Main.setDefaultPerspective(SimplePerspective.class.getName());
			}
			// Check workspace directory
			if (!workspacePath.getUrl().trim().equals("")) {
				if (!new File(workspacePath.getUrl()).canRead()) {
					Messages.showErrorMessage("165");
					return;
				}
			}
			// Set Workspace directory
			try {
				java.io.File bootstrap = new java.io.File(FILE_BOOTSTRAP);
				BufferedWriter bw = new BufferedWriter(new FileWriter(bootstrap));
				bw.write(workspacePath.getUrl());
				bw.flush();
				bw.close();
				// Store the workspace PATH
				Main.workspace = workspacePath.getUrl();
			} catch (Exception ex) {
				Messages.showErrorMessage("024");
				Log.debug("Cannot write bootstrap file");
			}
			// We have to create a device and to launch immediate refresh but
			// the environment
			// is still far from being operational at this startup state, so
			// Main will unlock it later
			new Thread() {
				public void run() {
					synchronized (Main.canLaunchRefresh) {
						try {
							Main.canLaunchRefresh.wait();
						} catch (InterruptedException e) {
							Log.error(e);
						}
					}
					// Create a directory device
					Device device = DeviceManager.getInstance().registerDevice(fDir.getName(), 0,
							fDir.getAbsolutePath());
					device.setProperty(XML_DEVICE_MOUNT_POINT, fDir.getAbsolutePath());
					device.setProperty(XML_DEVICE_AUTO_MOUNT, true);
					// Set refresh time
					double dRefreshTime = 5d;
					try {
						dRefreshTime = Double.parseDouble(jtfRefreshTime.getText());
						if (dRefreshTime < 0) {
							dRefreshTime = 0;
						}
					} catch (NumberFormatException e1) {
						dRefreshTime = 0;
					}
					device.setProperty(XML_DEVICE_AUTO_REFRESH, dRefreshTime);
					try {
						device.refresh(true, false);
					} catch (Exception e2) {
						Log.error("112", device.getName(), e2);
						Messages.showErrorMessage("112", device.getName());
					}
				}
			}.start();

			// exit
			dispose();
		}
	}

}