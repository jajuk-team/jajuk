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

package org.jajuk.ui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.log.Log;

/**
 * Scan CD to build the collection as fast as possible
 * <p>
 * Configuration perspective *.
 */
public class CDScanView extends ViewAdapter implements ActionListener {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  JTextField jtfName;

  /** DOCUMENT_ME. */
  JLabel jlMountPoint;

  /** DOCUMENT_ME. */
  JTextField jtfMountPoint;

  /** DOCUMENT_ME. */
  JButton jbScan;

  /** DOCUMENT_ME. */
  JButton jbUrl;

  /**
   * Constructor.
   */
  public CDScanView() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jbScan) {
      if ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
        try {
          UtilGUI.showPictureDialog("http://repository.jajuk.info/images/no2.jpg");
        } catch (MalformedURLException me) {
          Log.debug("Ignoring exception in CD-Scan-View: ", me);
        }
        return;
      }

      SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
        @Override
        public Void doInBackground() {
          if (!"".equals(jtfName.getText().trim()) && !"".equals(jtfMountPoint.getText().trim())) {
            Device device = null;
            device = DeviceManager.getInstance().registerDevice(jtfName.getText().trim(),
                Device.Type.FILES_CD, jtfMountPoint.getText().trim());
            try {
              device.mount(true);
              // refresh synchronously
              device.refresh(false, false, false, null);
              device.unmount(true, true);
            } catch (Exception ex) {
              DeviceManager.getInstance().removeDevice(device);
              Messages.showErrorMessage(16);
              // refresh views
              ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
            }
          }
          return null;
        }

        @Override
        public void done() {
          jtfName.setText("");
          jtfName.requestFocusInWindow();
        }
      };
      sw.execute();
    } else if (e.getSource() == jbUrl) {
      final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(
          DirectoryFilter.getInstance()));
      jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));
      jfc.setMultiSelectionEnabled(false);
      jfc.setAcceptDirectories(true);
      final String sMountPoint = jtfMountPoint.getText();
      if (!"".equals(sMountPoint)) { // if url is already set, use it
        // as root directory
        //
        jfc.setCurrentDirectory(new File(sMountPoint));
      }
      final int returnVal = jfc.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        final java.io.File file = jfc.getSelectedFile();
        jtfMountPoint.setText(file.getAbsolutePath());
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("CDScanView.12");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  @Override
  public void initUI() {
    JLabel jlName = new JLabel(Messages.getString("CDScanView.0"));
    jlName.setToolTipText(Messages.getString("CDScanView.1"));
    jtfName = new JTextField();
    jtfName.setToolTipText(Messages.getString("CDScanView.2"));
    jlMountPoint = new JLabel(Messages.getString("CDScanView.3"));
    jlMountPoint.setToolTipText(Messages.getString("CDScanView.4"));
    jtfMountPoint = new JTextField();
    jtfMountPoint.setToolTipText(Messages.getString("CDScanView.5"));
    jbScan = new JButton(Messages.getString("CDScanView.6"), IconLoader.getIcon(JajukIcons.REFRESH));
    jbScan.setToolTipText(Messages.getString("CDScanView.18"));
    jbScan.addActionListener(this);
    jbUrl = new JButton(IconLoader.getIcon(JajukIcons.OPEN_FILE));
    jbUrl.setToolTipText(Messages.getString("CDScanView.19"));
    jbUrl.addActionListener(this);
    setLayout(new MigLayout("insets 10, gapy 15", "[][grow]"));
    add(jlName);
    add(jtfName, "wrap,grow");
    add(jlMountPoint);
    add(jtfMountPoint, "split 2,grow");
    add(jbUrl, "wrap");
    add(jbScan, "right,span");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.Event)
   */
  @Override
  public void update(JajukEvent event) {
    // nothing to do here...
  }
}
