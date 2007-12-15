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
 *  $$Revision: 2321 $$
 */

package org.jajuk.ui.wizard;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Simple device creation wiard that creates a directory device given a
 * directory
 */
public class SimpleDeviceWizard extends JajukJDialog implements ITechnicalStrings, ActionListener {
  private static final long serialVersionUID = 1L;

  JLabel jlLeftIcon;

  JPanel jpRightPanel;

  JLabel jlFileSelection;

  JTextField jtfFileSelected;

  JButton jbFileSelection;

  JLabel jlRefreshTime;

  JTextField jtfRefreshTime;

  JLabel jlMins;

  JPanel jpButtons;

  JButton jbOk;

  JButton jbCancel;

  JPanel jpMain;

  /** Selected directory */
  private File fDir;

  /**
   * First time wizard
   */
  public SimpleDeviceWizard() {
    setTitle(Messages.getString("SimpleDeviceWizard.0"));
    final int iX_SEPARATOR = 10;
    final int iY_SEPARATOR = 10;
    jlLeftIcon = new JLabel(Util.getImage(ITechnicalStrings.IMAGE_SEARCH));
    jpRightPanel = new JPanel();
    jlFileSelection = new JLabel(Messages.getString("FirstTimeWizard.2"));
    jbFileSelection = new JButton(IconLoader.ICON_OPEN_DIR);
    jtfFileSelected = new JTextField("");
    jtfFileSelected.setForeground(Color.BLUE);
    jtfFileSelected.setEditable(false);
    jbFileSelection.addActionListener(this);

    // Refresh time
    jlRefreshTime = new JLabel(Messages.getString("DeviceWizard.53"));
    jtfRefreshTime = new JTextField("5");// 5 mins by default
    jlMins = new JLabel(Messages.getString("DeviceWizard.54"));
    final JPanel jpRefresh = new JPanel();
    final double sizeRefresh[][] = {
        { TableLayoutConstants.PREFERRED, iX_SEPARATOR, 100, iX_SEPARATOR,
            TableLayoutConstants.PREFERRED }, { 20 } };
    jpRefresh.setLayout(new TableLayout(sizeRefresh));
    jpRefresh.add(jlRefreshTime, "0,0");
    jpRefresh.add(jtfRefreshTime, "2,0");
    jpRefresh.add(jlMins, "4,0");
    // buttons
    jpButtons = new JPanel();
    jpButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
    jbOk = new JButton(Messages.getString("Ok"));
    jbOk.setEnabled(false);
    jbOk.addActionListener(this);
    jbCancel = new JButton(Messages.getString("Cancel"));
    jbCancel.addActionListener(this);
    jpButtons.add(jbOk);
    jpButtons.add(jbCancel);
    final FlowLayout flSelection = new FlowLayout(FlowLayout.LEFT);
    final JPanel jpFileSelection = new JPanel();
    jpFileSelection.setLayout(flSelection);
    jpFileSelection.add(jbFileSelection);
    jpFileSelection.add(Box.createHorizontalStrut(10));
    jpFileSelection.add(jlFileSelection);

    jpRightPanel.setLayout(new VerticalLayout(iY_SEPARATOR));
    jpRightPanel.add(jpFileSelection, "0,3");
    jpRightPanel.add(jtfFileSelected, "0,5");
    jpRightPanel.add(jpRefresh, "0,7");
    jpRightPanel.add(jpButtons, "0,11");
    final double size[][] = {
        { 20, TableLayoutConstants.PREFERRED, 30, TableLayoutConstants.PREFERRED }, { 0.99 } };
    jpMain = (JPanel) getContentPane();
    jpMain.setLayout(new TableLayout(size));
    jpMain.add(jlLeftIcon, "1,0");
    jpMain.add(jpRightPanel, "3,0");
    getRootPane().setDefaultButton(jbOk);
    setAlwaysOnTop(true);
  }

  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jbCancel) {
      dispose(); // close window
    } else if (e.getSource() == jbFileSelection) {
      final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(DirectoryFilter
          .getInstance()));
      jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      jfc.setDialogTitle(Messages.getString("FirstTimeWizard.5"));
      jfc.setMultiSelectionEnabled(false);
      final int returnVal = jfc.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        fDir = jfc.getSelectedFile();
        // check device availability
        final int code = DeviceManager.getInstance().checkDeviceAvailablity(fDir.getName(), 0,
            fDir.getAbsolutePath(), true);
        if (code != 0) {
          Messages.showErrorMessage(code);
          jbOk.setEnabled(false);
          return;
        }
        jtfFileSelected.setText(fDir.getAbsolutePath());
        jbOk.setEnabled(true);
        jbOk.grabFocus();
      }
    } else if (e.getSource() == jbOk) {
      try {
        // Create a directory device
        final Device device = DeviceManager.getInstance().registerDevice(fDir.getName(), 0,
            fDir.getAbsolutePath());
        device.setProperty(ITechnicalStrings.XML_DEVICE_AUTO_MOUNT, true);
        // Set refresh time
        double dRefreshTime = 5d;
        try {
          dRefreshTime = Double.parseDouble(jtfRefreshTime.getText());
          if (dRefreshTime < 0) {
            dRefreshTime = 0;
          }
        } catch (final NumberFormatException e1) {
          dRefreshTime = 0;
        }
        device.setProperty(ITechnicalStrings.XML_DEVICE_AUTO_REFRESH, dRefreshTime);
        try {
          device.refresh(true, false);
        } catch (final Exception e2) {
          Log.error(112, device.getName(), e2);
          Messages.showErrorMessage(112, device.getName());
        }
      } finally {
        // exit
        dispose();
      }
    }
  }
}
