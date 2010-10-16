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
import java.text.NumberFormat;
import java.text.ParseException;
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
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;

/**
 * Device creation wizard.
 */
public class DeviceWizard extends JajukJDialog implements ActionListener, Const {

  /** The Constant WRAP. */
  private static final String WRAP = "wrap";

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Device type combo. */
  private final JComboBox jcbType;

  /** Device name text field. */
  private final JTextField jtfName;

  /** Device url text field. */
  private final JTextField jtfUrl;

  /** Device url path selector button. */
  private final JButton jbUrl;

  /** Auto-refresh device checkbox. */
  private final JCheckBox jcbRefresh;

  /** Auto-mount checkbox. */
  private final JCheckBox jcbAutoMount;

  /** Auto-refresh interval. */
  private final JTextField jtfAutoRefresh;

  /** Device sync checkbox */
  private final JCheckBox jcboxSynchronized;

  /** Other device combo */
  private final JComboBox jcbSynchronized;

  /** Bidi sync choice. */
  private final JRadioButton jrbBidirSynchro;

  /** Unidir sync choice. */
  private final JRadioButton jrbUnidirSynchro;

  /** Ok Cancel panel. */
  private final OKCancelPanel okp;

  /** New device flag. */
  private boolean bNew = true;

  /** Current device. */
  private Device device;

  /** All devices expect itself. */
  private final List<Device> devices;

  /** Initial URL*. */
  private String sInitialURL;

  /** A convenient NumberFormat instance */
  private NumberFormat nformat = NumberFormat.getInstance();

  /** Validation group */
  private ValidationGroup vg;

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
    jtfAutoRefresh.setName(Messages.getString("DeviceWizard.54"));
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
    ButtonGroup bgSynchro = new ButtonGroup();
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

    // Validation
    ValidationPanel vp = new ValidationPanel();
    vg = vp.getValidationGroup();

    installValidators();

    // buttons
    okp = new OKCancelPanel(this);
    okp.getOKButton().setEnabled(false);

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
    add(vp, "height 25!,span,wrap");
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

  /**
   * Install validators.
   */
  @SuppressWarnings("unchecked")
  private void installValidators() {
    // Auto-refresh interval validation : should be 0 or a double >= 0.5
    vg.add(jtfAutoRefresh, Validators.REQUIRE_NON_NEGATIVE_NUMBER, Validators.NO_WHITESPACE,
        Validators.REQUIRE_VALID_NUMBER, new Validator<String>() {
          @Override
          public boolean validate(Problems problems, String compName, String model) {
            try {
              double value = nformat.parse(model).doubleValue();
              // If value is zero, validate the user input
              boolean resu = (value == 0 || value >= 0.5d);
              // If a problem occurred, add this problem to the problem stack
              if (!resu) {
                Problem problem = new Problem(Messages.getString("DeviceWizard.55"), Severity.FATAL);
                problems.add(problem);
              }
              // Disable the wizard OK button if user selection is not in the
              // right interval or if previous validators thrown an error
              // already
              okp.getOKButton().setEnabled(resu && problems.isEmpty());
              return resu;
            } catch (Exception e) {
              // This happen when the text field is not yet populated (model is
              // void). Note that wrong number format issues are already handled
              // by the previous Validators
              okp.getOKButton().setEnabled(false);
              return true;
            }
          }
        });

    // Validate device name
    vg.add(jtfName, Validators.REQUIRE_NON_EMPTY_STRING);
    vg.add(jtfName, new Validator<String>() {
      @Override
      public boolean validate(Problems problems, String compName, String model) {
        // By default, we disable the OK button, we re-enable it only if the
        // name is OK
        okp.getOKButton().setEnabled(false);
        for (Device deviceToCheck : DeviceManager.getInstance().getDevices()) {
          // check for a new device with an existing name
          if (bNew && (jtfName.getText().equalsIgnoreCase(deviceToCheck.getName()))) {
            problems.add(new Problem(Messages.getErrorMessage(19), Severity.FATAL));
            return false;
          }
        }
        okp.getOKButton().setEnabled(problems.isEmpty());
        return true;
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
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
        jtfAutoRefresh.setText(nformat.format(Const.DEFAULT_REFRESH_INTERVAL_DIRECTORY));
      }
      break;
    case 1: // file cd
      jcbAutoMount.setSelected(false);
      if (bNew) {
        jtfAutoRefresh.setText(nformat.format(Const.DEFAULT_REFRESH_INTERVAL_CD));
      }
      break;
    case 2: // network drive
      jcbAutoMount.setSelected(true);
      // no auto-refresh by default for network drive
      if (bNew) {
        jtfAutoRefresh.setText(nformat.format(Const.DEFAULT_REFRESH_INTERVAL_NETWORK_DRIVE));
      }
      break;
    case 3: // ext dd
      jcbAutoMount.setSelected(true);
      if (bNew) {
        jtfAutoRefresh.setText(nformat.format(Const.DEFAULT_REFRESH_INTERVAL_EXTERNAL_DRIVE));
      }
      break;
    case 4: // player
      jcbAutoMount.setSelected(false);
      if (bNew) {
        jtfAutoRefresh.setText(nformat.format(Const.DEFAULT_REFRESH_INTERVAL_PLAYER));
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
        if (bNew) {
          device = DeviceManager.getInstance().registerDevice(jtfName.getText(),
              jcbType.getSelectedIndex(), jtfUrl.getText());
        }
        device.setProperty(Const.XML_DEVICE_AUTO_MOUNT, jcbAutoMount.isSelected());
        try {
          device.setProperty(Const.XML_DEVICE_AUTO_REFRESH, nformat.parse(jtfAutoRefresh.getText())
              .doubleValue());
        } catch (ParseException e) {
          // Should not happen thanks GUI validators
          Log.error(e);
        }
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
              boolean mounted = device.mount(true);
              // Leave if user canceled device mounting
              if (!mounted) {
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
              InformationJPanel.MessageType.INFORMATIVE);
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
    jtfAutoRefresh.setText(nformat.format(device1.getDoubleValue(Const.XML_DEVICE_AUTO_REFRESH)));
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
    jtfAutoRefresh.setText(nformat.format(Const.DEFAULT_REFRESH_INTERVAL_DIRECTORY));
    jcboxSynchronized.setSelected(false);
    jrbUnidirSynchro.setSelected(true);// default synchro mode
    jrbBidirSynchro.setEnabled(false);
  }
}
