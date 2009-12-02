/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.DirectoryManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.log.Log;

/**
 * Device creation wizard.
 */
public class DeviceWizard extends JajukJDialog implements ActionListener, Const {

  /** The Constant WRAP. DOCUMENT_ME */
  private static final String WRAP = "wrap";

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private final JComboBox jcbType;

  /** DOCUMENT_ME. */
  private final JTextField jtfName;

  /** DOCUMENT_ME. */
  private final JTextField jtfUrl;

  /** DOCUMENT_ME. */
  private final JButton jbUrl;

  /** DOCUMENT_ME. */
  private final JCheckBox jcbRefresh;

  /** DOCUMENT_ME. */
  private final JCheckBox jcbAutoMount;

  /** DOCUMENT_ME. */
  private final JTextField jtfAutoRefresh;

  /** DOCUMENT_ME. */
  private final JCheckBox jcboxSynchronized;

  /** DOCUMENT_ME. */
  private final JComboBox jcbSynchronized;

  /** DOCUMENT_ME. */
  private final ButtonGroup bgSynchro;

  /** DOCUMENT_ME. */
  private final JRadioButton jrbBidirSynchro;

  /** DOCUMENT_ME. */
  private final JRadioButton jrbUnidirSynchro;

  /** DOCUMENT_ME. */
  private final OKCancelPanel okp;

  /** New device flag. */
  private boolean bNew = true;

  /** Current device. */
  private Device device;

  /** All devices expect itself. */
  private final List<Device> devices;

  /** Initial URL*. */
  private String sInitialURL;

  /**
   * Device wizard by default, is used for void configuration.
   */
  public DeviceWizard() {
    super();

    devices = DeviceManager.getInstance().getDevices();
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowActivated(final WindowEvent e) {
        jtfName.requestFocusInWindow();
      }
    });
    setTitle(Messages.getString("DeviceWizard.0"));
    setModal(true);
    setLocation(JajukMainWindow.getInstance().getX() + 100,
        JajukMainWindow.getInstance().getY() + 100);
    JLabel jlType = new JLabel(Messages.getString("DeviceWizard.1"));
    jcbType = new JComboBox();

    final Iterator<String> itDevicesTypes = DeviceManager.getInstance().getDeviceTypes();
    while (itDevicesTypes.hasNext()) {
      jcbType.addItem(itDevicesTypes.next());
    }
    JLabel jlName = new JLabel(Messages.getString("DeviceWizard.2"));
    jtfName = new JTextField();
    jtfName.setToolTipText(Messages.getString("DeviceWizard.45"));
    JLabel jlUrl = new JLabel(Messages.getString("DeviceWizard.3"));
    jtfUrl = new JTextField();
    jtfUrl.setToolTipText(Messages.getString("DeviceWizard.46"));
    jbUrl = new JButton(IconLoader.getIcon(JajukIcons.OPEN_FILE));
    jbUrl.setToolTipText(Messages.getString("DeviceWizard.43"));
    jbUrl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    jbUrl.addActionListener(this);
    // we disable focus for url and mount url buttons to facilitate
    // navigation
    jbUrl.setFocusable(false);
    jcbRefresh = new JCheckBox(Messages.getString("DeviceWizard.7"));
    jcbRefresh.setToolTipText(Messages.getString("DeviceWizard.48"));
    jcbRefresh.addActionListener(this);
    jcbAutoMount = new JCheckBox(Messages.getString("DeviceWizard.8"));
    jcbAutoMount.setToolTipText(Messages.getString("DeviceWizard.49"));
    jcbAutoMount.addActionListener(this);
    JLabel jlAutoRefresh = new JLabel(Messages.getString("DeviceWizard.53"));
    jlAutoRefresh.setToolTipText(Messages.getString("DeviceWizard.50"));
    JLabel jlMinutes = new JLabel(Messages.getString("DeviceWizard.54"));
    /* jtfAutoRefresh rules : Minimum delay is half a minute */
    jtfAutoRefresh = new JTextField();

    jtfAutoRefresh.setToolTipText(Messages.getString("DeviceWizard.50"));
    jcboxSynchronized = new JCheckBox(Messages.getString("DeviceWizard.10"));
    jcboxSynchronized.setToolTipText(Messages.getString("DeviceWizard.51"));
    jcboxSynchronized.addActionListener(this);
    jcbSynchronized = new JComboBox();
    // populate combo
    for (Device device1 : devices) {
      jcbSynchronized.addItem(device1.getName());
    }
    jcbSynchronized.setEnabled(false);
    jcbSynchronized.setToolTipText(Messages.getString("DeviceWizard.52"));
    // Default automount behavior
    jcbType.addActionListener(this);
    bgSynchro = new ButtonGroup();
    jrbUnidirSynchro = new JRadioButton(Messages.getString("DeviceWizard.11"));
    jrbUnidirSynchro.setToolTipText(Messages.getString("DeviceWizard.12"));
    jrbUnidirSynchro.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
    jrbUnidirSynchro.setEnabled(false);
    jrbUnidirSynchro.addActionListener(this);
    jrbBidirSynchro = new JRadioButton(Messages.getString("DeviceWizard.13"));
    jrbBidirSynchro.setToolTipText(Messages.getString("DeviceWizard.14"));
    jrbBidirSynchro.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
    jrbBidirSynchro.setEnabled(false);
    jrbBidirSynchro.addActionListener(this);
    bgSynchro.add(jrbBidirSynchro);
    bgSynchro.add(jrbUnidirSynchro);

    // buttons
    okp = new OKCancelPanel(this);

    // Add items
    setLayout(new MigLayout("insets 10,gapx 10, gapy 15", "[][grow]"));
    add(jlType);
    add(jcbType, "grow,wrap");
    add(jlName);
    add(jtfName, "grow,wrap");
    add(jlUrl);
    add(jtfUrl, "split 2,growx");
    add(jbUrl, WRAP);
    add(jlAutoRefresh);
    add(jtfAutoRefresh, "grow,split 2");
    add(jlMinutes, WRAP);
    add(jcbRefresh, WRAP);
    add(jcbAutoMount, WRAP);
    add(jcboxSynchronized);
    add(jcbSynchronized, "grow,wrap");
    add(jrbUnidirSynchro, "left,gap left 20,span,wrap");
    add(jrbBidirSynchro, "left,gap left 20,span,wrap");
    add(okp, "span,right");

    // Set default behaviors
    if (jcbSynchronized.getItemCount() == 0) {
      jcboxSynchronized.setEnabled(false);
      jcbSynchronized.setEnabled(false);
      jrbBidirSynchro.setEnabled(false);
    }
    getRootPane().setDefaultButton(okp.getOKButton());
    pack();
    okp.getOKButton().requestFocusInWindow();

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jcboxSynchronized) {
      handleSynchronized();
    } else if (e.getSource() == okp.getOKButton()) {
      handleOk();
    } else if (e.getSource() == okp.getCancelButton()) {
      dispose(); // close window
    } else if (e.getSource() == jbUrl) {
      handleUrl();
    } else if (e.getSource() == jcbType) {
      handleType();
    }
  }

  /**
   * Handle type. DOCUMENT_ME
   */
  private void handleType() {
    switch (jcbType.getSelectedIndex()) {
    case 0: // directory
      jcbAutoMount.setSelected(true);
      if (bNew) {
        jtfAutoRefresh.setText("1");
      }
      break;
    case 1: // file cd
      jcbAutoMount.setSelected(false);
      if (bNew) {
        jtfAutoRefresh.setText("0");
      }
      break;
    case 2: // network drive
      jcbAutoMount.setSelected(true);
      // no auto-refresh by default for network drive
      if (bNew) {
        jtfAutoRefresh.setText("0");
      }
      break;
    case 3: // ext dd
      jcbAutoMount.setSelected(true);
      if (bNew) {
        jtfAutoRefresh.setText("3");
      }
      break;
    case 4: // player
      jcbAutoMount.setSelected(false);
      if (bNew) {
        jtfAutoRefresh.setText("3");
      }
      break;
    }
  }

  /**
   * Handle url.
   * 
   * @throws HeadlessException
   *           the headless exception
   */
  private void handleUrl() throws HeadlessException {
    final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(DirectoryFilter
        .getInstance()));
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));
    jfc.setMultiSelectionEnabled(false);
    final String sUrl = jtfUrl.getText();
    if (!"".equals(sUrl)) {
      // if url is already set, use it as root directory
      jfc.setCurrentDirectory(new File(sUrl));
    }
    final int returnVal = jfc.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      final java.io.File file = jfc.getSelectedFile();
      jtfUrl.setText(file.getAbsolutePath());
    }
  }

  /**
   * Handle ok. DOCUMENT_ME
   */
  private void handleOk() {
    new Thread("Device Wizard Action Thread") {
      @Override
      public void run() {
        // check device availability (test name only if new device)
        final int code = DeviceManager.getInstance().checkDeviceAvailablity(jtfName.getText(),
            jcbType.getSelectedIndex(), jtfUrl.getText(), bNew);
        if (code != 0) {
          Messages.showErrorMessage(code);
          setVisible(true); // display wizard window which has been
          // hidden by the error window
          return;
        }
        if (bNew) {
          device = DeviceManager.getInstance().registerDevice(jtfName.getText(),
              jcbType.getSelectedIndex(), jtfUrl.getText());
        }
        device.setProperty(Const.XML_DEVICE_AUTO_MOUNT, jcbAutoMount.isSelected());
        device.setProperty(Const.XML_DEVICE_AUTO_REFRESH, Double.parseDouble(jtfAutoRefresh
            .getText()));
        device.setProperty(Const.XML_TYPE, Long.valueOf(jcbType.getSelectedIndex()));
        device.setUrl(jtfUrl.getText());
        if (jcbSynchronized.isEnabled() && (jcbSynchronized.getSelectedItem() != null)) {
          Device selected = DeviceManager.getInstance().getDeviceByName(
              (String) jcbSynchronized.getSelectedItem());
          device.setProperty(Const.XML_DEVICE_SYNCHRO_SOURCE, selected.getID());
          if (jrbBidirSynchro.isSelected()) {
            device.setProperty(Const.XML_DEVICE_SYNCHRO_MODE, Const.DEVICE_SYNCHRO_MODE_BI);
          } else {
            device.setProperty(Const.XML_DEVICE_SYNCHRO_MODE, Const.DEVICE_SYNCHRO_MODE_UNI);
          }
        } else { // no synchro
          device.removeProperty(Const.XML_DEVICE_SYNCHRO_MODE);
          device.removeProperty(Const.XML_DEVICE_SYNCHRO_SOURCE);
        }
        // Force deep refresh if it is a new device or if URL changed
        if (jcbRefresh.isSelected() && bNew) {
          try {
            // Drop existing directory to avoid phantom directories if
            // existing device
            DirectoryManager.getInstance().removeDirectory(device.getID());
            device.refresh(true, false, false);
          } catch (final Exception e2) {
            Log.error(112, device.getName(), e2);
            Messages.showErrorMessage(112, device.getName());
          }
        } else if (sInitialURL != null && !sInitialURL.equals(jtfUrl.getText())) {
          // If user changed the URL, force refresh
          try {
            // try to remount the device
            if (!device.isMounted()) {
              int resu = device.mount(true);
              // Leave if user canceled device mounting
              if (resu < 0) {
                dispose();
                return;
              }
            }
            // Keep previous references when changing device url
            device.refresh(true, false, true);
            ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
          } catch (final Exception e2) {
            Log.error(112, device.getName(), e2);
            Messages.showErrorMessage(112, device.getName());
          }
        }
        ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
        dispose();
        if (bNew) {
          InformationJPanel.getInstance().setMessage(Messages.getString("DeviceWizard.44"),
              InformationJPanel.INFORMATIVE);
        }
      }
    }.start();
  }

  /**
   * Handle synchronized. DOCUMENT_ME
   */
  private void handleSynchronized() {
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

  /**
   * Update widgets for device property state.
   * 
   * @param device1
   *          DOCUMENT_ME
   */
  public void updateWidgets(final Device device1) {
    bNew = false;
    setTitle(Messages.getString("DeviceWizard.0") + " : " + device1.getName());
    this.device = device1;
    jcbSynchronized.removeAllItems();
    devices.clear();
    // set default values for widgets
    updateWidgetsDefault();
    List<Device> devices2 = DeviceManager.getInstance().getDevices();
    for (Device device2 : devices2) {
      if (!device2.equals(device1)) {
        devices.add(device2);
        jcbSynchronized.addItem(device2.getName());
      }
    }
    // then, specifics
    jcbType.setSelectedItem(device1.getDeviceTypeS());
    jtfName.setText(device1.getName());
    jtfName.setEnabled(false); // device name cannot be changed
    jtfUrl.setText(device1.getUrl());
    sInitialURL = device1.getUrl();
    jcbRefresh.setEnabled(false); // no instant refresh for updates
    jcbRefresh.setSelected(false);
    jcbAutoMount.setSelected(true);
    if (device1.getBooleanValue(Const.XML_DEVICE_AUTO_MOUNT)) {
      jcbAutoMount.setSelected(true);
    } else {
      jcbAutoMount.setSelected(false);
    }
    jtfAutoRefresh.setText(device1.getStringValue(Const.XML_DEVICE_AUTO_REFRESH));
    if (jcbSynchronized.getItemCount() == 0) {
      jcboxSynchronized.setEnabled(false);
      jcbSynchronized.setEnabled(false);
      jrbBidirSynchro.setEnabled(false);
    }
    if (device1.containsProperty(Const.XML_DEVICE_SYNCHRO_SOURCE)) {
      final String sSynchroSource = device1.getStringValue(Const.XML_DEVICE_SYNCHRO_SOURCE);
      jrbBidirSynchro.setEnabled(true);
      jrbUnidirSynchro.setEnabled(true);
      jcboxSynchronized.setSelected(true);
      jcboxSynchronized.setEnabled(true);
      jcbSynchronized.setEnabled(true);
      Device toBeSelected = DeviceManager.getInstance().getDeviceByID(sSynchroSource);
      jcbSynchronized.setSelectedItem(toBeSelected.getName());
      if (Const.DEVICE_SYNCHRO_MODE_BI.equals(device1.getValue(Const.XML_DEVICE_SYNCHRO_MODE))) {
        jrbBidirSynchro.setSelected(true);
      } else {
        jrbUnidirSynchro.setSelected(true);
      }
    }
  }

  /**
   * Update widgets for default state.
   */
  public void updateWidgetsDefault() {
    jcbRefresh.setSelected(true);
    jcbAutoMount.setSelected(true);
    jtfAutoRefresh.setText("1");
    jcboxSynchronized.setSelected(false);
    jrbUnidirSynchro.setSelected(true);// default synchro mode
    jrbBidirSynchro.setEnabled(false);
  }
}
