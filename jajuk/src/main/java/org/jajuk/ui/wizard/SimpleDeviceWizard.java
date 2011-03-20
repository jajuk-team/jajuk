/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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

package org.jajuk.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.log.Log;

/**
 * Simple device creation wizard that creates a directory device given a
 * directory.
 */
public class SimpleDeviceWizard extends JajukJDialog implements ActionListener {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  JButton jbFileSelection;

  /** DOCUMENT_ME. */
  JLabel jlSelectedFile;

  /** DOCUMENT_ME. */
  JTextField jtfRefreshTime;

  /** DOCUMENT_ME. */
  OKCancelPanel okp;

  /** DOCUMENT_ME. */
  String deviceName;

  /** Selected directory. */
  private File fDir;

  /**
   * Instantiates a new simple device wizard.
   */
  public SimpleDeviceWizard() {
    setTitle(Messages.getString("SimpleDeviceWizard.0"));
    setAlwaysOnTop(true);

    okp = new OKCancelPanel(this);
    jbFileSelection = new JButton(IconLoader.getIcon(JajukIcons.OPEN_DIR));
    jbFileSelection.addActionListener(this);

    jlSelectedFile = new JLabel(Messages.getString("FirstTimeWizard.9"));
    jlSelectedFile.setBorder(new BevelBorder(BevelBorder.LOWERED));

    jtfRefreshTime = new JTextField(Const.DEFAULT_REFRESH_INTERVAL);

    // Add items
    setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[][grow]"));
    add(new JLabel(UtilGUI.getImage(Const.IMAGE_SEARCH)), "cell 0 0 0 3");
    add(new JLabel(Messages.getString("FirstTimeWizard.2")), "cell 1 0,split 2");
    add(jbFileSelection, ""); // please
    add(new JLabel(Messages.getString("FirstTimeWizard.8")), "split 2,cell 1 1");
    add(jlSelectedFile, "cell 1 1, grow");
    // select
    // music
    // location
    add(new JLabel(Messages.getString("DeviceWizard.53")), "cell 1 2,split 3"); // Refresh
    // device
    // every
    add(jtfRefreshTime, "grow");
    add(new JLabel(Messages.getString("DeviceWizard.54")), "wrap"); // mins
    add(okp, "right,cell 1 3");

    getRootPane().setDefaultButton(okp.getOKButton());
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == okp.getCancelButton()) {
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

        deviceName = fDir.getName();

        // First, check device *name* availability, otherwise, use a <name>~<nb>
        // name
        int code = DeviceManager.getInstance().checkDeviceAvailablity(deviceName, 0,
            fDir.getAbsolutePath(), true);
        int prefix = 1;
        while (code == 19) { // code 19 means a device already exists with this
          // name
          deviceName = fDir.getName() + '~' + prefix;
          code = DeviceManager.getInstance().checkDeviceAvailablity(deviceName, 0,
              fDir.getAbsolutePath(), true);
          prefix++;
        }
        // Now, test again to detected others availability issues like wrong URL
        code = DeviceManager.getInstance().checkDeviceAvailablity(deviceName, 0,
            fDir.getAbsolutePath(), true);
        if (code != 0 && code != 19) {
          Messages.showErrorMessage(code);
          okp.getOKButton().setEnabled(false);
          return;
        }

        okp.getOKButton().setEnabled(true);
        okp.getOKButton().grabFocus();

        jlSelectedFile.setText(fDir.getAbsolutePath());
        pack(); // repack as size of dialog can be exceeded now
      }
    } else if (e.getSource() == okp.getOKButton()) {
      try {
        if(fDir == null) {
          Messages.showErrorMessage(143);
          return;
        }

        // Create a directory device
        final Device device = DeviceManager.getInstance().registerDevice(deviceName, 0,
            fDir.getAbsolutePath());
        device.setProperty(Const.XML_DEVICE_AUTO_MOUNT, true);
        // Set refresh time
        double dRefreshTime;
        try {
          dRefreshTime = Double.parseDouble(jtfRefreshTime.getText());
          if (dRefreshTime < 0) {
            dRefreshTime = 0;
          }
        } catch (final NumberFormatException e1) {
          dRefreshTime = 0;
        }
        device.setProperty(Const.XML_DEVICE_AUTO_REFRESH, dRefreshTime);
        try {
          device.refresh(true, false, false, null);
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
