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

import ext.FlowScrollPanel;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.ui.wizard.DeviceWizard;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;

/**
 * Device view used to create and modify Jajuk devices
 * <p>
 * Configuration perspective
 */
public class DeviceView extends ViewAdapter implements IView, Const, ActionListener, Observer,
    MouseListener {
  private static final long serialVersionUID = 1L;

  private static DeviceView dv; // self instance

  FlowScrollPanel jpDevices;

  JPopupMenu jpmenu;

  JMenuItem jmiDelete;

  JMenuItem jmiProperties;

  JMenuItem jmiMount;

  JMenuItem jmiUnmount;

  JMenuItem jmiTest;

  JMenuItem jmiRefresh;

  JMenuItem jmiSynchronize;

  DeviceItem diSelected;

  volatile boolean popupTrigger = false;

  public DeviceView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    // devices
    jpDevices = new FlowScrollPanel();
    JScrollPane jsp = new JScrollPane(jpDevices, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jsp.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));

    jpDevices.setScroller(jsp);

    jpDevices.setLayout(new FlowLayout(FlowLayout.LEFT));

    // Popup menus
    jpmenu = new JPopupMenu();

    jmiMount = new JMenuItem(Messages.getString("DeviceView.8"), IconLoader
        .getIcon(JajukIcons.MOUNT));
    jmiMount.addActionListener(this);
    jmiMount.setActionCommand(JajukEvents.DEVICE_MOUNT.toString());
    jpmenu.add(jmiMount);

    jmiUnmount = new JMenuItem(Messages.getString("DeviceView.9"), IconLoader
        .getIcon(JajukIcons.UNMOUNT));
    jmiUnmount.addActionListener(this);
    jmiUnmount.setActionCommand(JajukEvents.DEVICE_UNMOUNT.toString());
    jpmenu.add(jmiUnmount);

    jmiRefresh = new JMenuItem(Messages.getString("DeviceView.11"), IconLoader
        .getIcon(JajukIcons.REFRESH));
    jmiRefresh.addActionListener(this);
    jmiRefresh.setActionCommand(JajukEvents.DEVICE_REFRESH.toString());
    jpmenu.add(jmiRefresh);

    jmiTest = new JMenuItem(Messages.getString("DeviceView.10"), IconLoader
        .getIcon(JajukIcons.TEST));
    jmiTest.addActionListener(this);
    jmiTest.setActionCommand(JajukEvents.DEVICE_TEST.toString());
    jpmenu.add(jmiTest);

    jmiSynchronize = new JMenuItem(Messages.getString("DeviceView.12"), IconLoader
        .getIcon(JajukIcons.SYNCHRO));
    jmiSynchronize.addActionListener(this);
    jmiSynchronize.setActionCommand(JajukEvents.DEVICE_SYNCHRO.toString());
    jpmenu.add(jmiSynchronize);

    jmiDelete = new JMenuItem(Messages.getString("DeviceView.13"), IconLoader
        .getIcon(JajukIcons.DELETE));
    jmiDelete.addActionListener(this);
    jmiDelete.setActionCommand(JajukEvents.DEVICE_DELETE.toString());
    jpmenu.add(jmiDelete);

    jmiProperties = new JMenuItem(Messages.getString("DeviceView.14"), IconLoader
        .getIcon(JajukIcons.CONFIGURATION));
    jmiProperties.addActionListener(this);
    jmiProperties.setActionCommand(JajukEvents.DEVICE_PROPERTIES.toString());
    jpmenu.add(jmiProperties);

    // add devices
    refreshDevices();

    // add components
    double size[][] = { { TableLayout.FILL }, { TableLayout.FILL } };
    setLayout(new TableLayout(size));
    add(jsp, "0,0");
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.DEVICE_MOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_UNMOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_NEW);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    return eventSubjectSet;
  }

  private void refreshDevices() {
    // remove all devices
    if (jpDevices.getComponentCount() > 0) {
      jpDevices.removeAll();
    }
    // New device
    DeviceItem diNew = new DeviceItem(IconLoader.getIcon(JajukIcons.DEVICE_NEW), Messages
        .getString("DeviceView.17"), null);
    diNew.setToolTipText(Messages.getString("DeviceView.18"));
    jpDevices.add(diNew);
    diNew.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        DeviceWizard dw = new DeviceWizard();
        dw.updateWidgetsDefault();
        dw.pack();
        dw.setVisible(true);
      }
    });
    // Add devices
    Iterator<Device> it = DeviceManager.getInstance().getDevices().iterator();
    while (it.hasNext()) {
      final Device device = it.next();
      ImageIcon icon = IconLoader.getIcon(JajukIcons.DEVICE_DIRECTORY_MOUNTED);
      String sTooltip = "";
      switch ((int) device.getType()) {
      case 0:
        sTooltip = Messages.getString("Device_type.directory");
        if (device.isMounted()) {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_DIRECTORY_MOUNTED);
        } else {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_DIRECTORY_UNMOUNTED);
        }
        break;
      case 1:
        sTooltip = Messages.getString("Device_type.file_cd");
        if (device.isMounted()) {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_CD_MOUNTED);
        } else {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_CD_UNMOUNTED);
        }
        break;
      case 2:
        sTooltip = Messages.getString("Device_type.network_drive");
        if (device.isMounted()) {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_NETWORK_DRIVE_MOUNTED);
        } else {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_NETWORK_DRIVE_UNMOUNTED);
        }
        break;
      case 3:
        sTooltip = Messages.getString("Device_type.extdd");
        if (device.isMounted()) {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_EXT_DD_MOUNTED);
        } else {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_EXT_DD_UNMOUNTED);
        }
        break;
      case 4:
        sTooltip = Messages.getString("Device_type.player");
        if (device.isMounted()) {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_PLAYER_MOUNTED);
        } else {
          icon = IconLoader.getIcon(JajukIcons.DEVICE_PLAYER_UNMOUNTED);
        }
        break;
      }
      DeviceItem di = new DeviceItem(icon, device.getName(), device);
      di.setToolTipText(sTooltip);
      di.addMouseListener(this);
      di.setToolTipText(device.getDeviceTypeS());
      jpDevices.add(di);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean pVisible) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#getComponent()
   */
  @Override
  public Component getComponent() {
    return this;
  }

  /**
   * Singleton implementation
   * 
   * @return
   */
  public static DeviceView getInstance() {
    if (dv == null) {
      dv = new DeviceView();
    }
    return dv;
  }

  public void actionPerformed(final ActionEvent ae) {
    if (ae.getActionCommand().equals(JajukEvents.DEVICE_NEW.toString())) {
      DeviceWizard dw = new DeviceWizard();
      dw.updateWidgetsDefault();
      dw.pack();
      dw.setVisible(true);
      return;
    }

    if (diSelected == null) { // test a device is selected
      return;
    }

    if (ae.getActionCommand().equals(JajukEvents.DEVICE_DELETE.toString())) {
      DeviceManager.getInstance().removeDevice(diSelected.getDevice());
      jpDevices.remove(diSelected);
      // refresh views
      ObservationManager.notify(new Event(JajukEvents.DEVICE_REFRESH));
    } else if (ae.getActionCommand().equals(JajukEvents.DEVICE_MOUNT.toString())) {
      try {
        diSelected.getDevice().mount();
      } catch (Exception e) {
        Messages.showErrorMessage(11);
      }
    } else if (ae.getActionCommand().equals(JajukEvents.DEVICE_UNMOUNT.toString())) {
      try {
        diSelected.getDevice().unmount();
      } catch (Exception e) {
        Messages.showErrorMessage(12);
      }
    } else if (ae.getActionCommand().equals(JajukEvents.DEVICE_PROPERTIES.toString())) {
      DeviceWizard dw = new DeviceWizard();
      dw.updateWidgets(diSelected.getDevice());
      dw.pack();
      dw.setVisible(true);
    } else if (ae.getActionCommand().equals(JajukEvents.DEVICE_REFRESH.toString())) {
      diSelected.getDevice().refresh(true, true); // ask deep or fast scan
    } else if (ae.getActionCommand().equals(JajukEvents.DEVICE_SYNCHRO.toString())) {
      diSelected.getDevice().synchronize(true);
    } else if (ae.getActionCommand().equals(JajukEvents.DEVICE_TEST.toString())) {
      new Thread("Asynchronouse device test thread") {// test asynchronously in
        // case of delay (samba
        // pbm for ie)
        @Override
        public void run() {
          if (diSelected.getDevice().test()) {
            Messages.showInfoMessage(Messages.getString("DeviceView.21"), IconLoader
                .getIcon(JajukIcons.OK));
          } else {
            Messages.showInfoMessage(Messages.getString("DeviceView.22"), IconLoader
                .getIcon(JajukIcons.KO));
          }
        }
      }.start();
    } /*
       * else if (ae.getActionCommand().equals(JajukEvents.WIZARD.toString())) {
       *  }
       */
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("DeviceView.23");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(Event event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.DEVICE_MOUNT.equals(subject) || JajukEvents.DEVICE_UNMOUNT.equals(subject)
        || JajukEvents.DEVICE_REFRESH.equals(subject)) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          UtilGUI.waiting();
          refreshDevices();
          jpDevices.revalidate();
          jpDevices.repaint();
          UtilGUI.stopWaiting();
        }
      });
    }
  }

  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      popupTrigger = true;
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger() || popupTrigger) {
      popupTrigger = false;
      handlePopup(e);
    } else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
      popupTrigger = false;
      boolean bSameDevice = ((diSelected != null) && e.getSource().equals(diSelected));// be
      selectItem(e);
      if (bSameDevice) {
        // one device already selected + right click
        DeviceWizard dw = new DeviceWizard();
        dw.updateWidgets(diSelected.getDevice());
        dw.pack();
        dw.setVisible(true);
      } else {
        // a new device is selected
        diSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
      }

    }
  }

  public void handlePopup(final MouseEvent e) {
    selectItem(e);
    // a new device is selected
    diSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
    jpmenu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void selectItem(final MouseEvent e) {
    boolean bSameDevice = ((diSelected != null) && e.getSource().equals(diSelected));// be
    // remove old device item border if needed
    if (!bSameDevice && diSelected != null) {
      diSelected.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    diSelected = (DeviceItem) e.getSource();
    // Test if it is the "NEW" device
    if (((DeviceItem) e.getSource()).getDevice() == null) {
      return;
    }
    // remove options for non synchronized devices
    if (diSelected.getDevice().containsProperty(XML_DEVICE_SYNCHRO_SOURCE)) {
      jmiSynchronize.setEnabled(true);
    } else {
      jmiSynchronize.setEnabled(false);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent arg0) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {
  }

}

/**
 * A device icon + text Type description
 */
class DeviceItem extends JPanel {
  private static final long serialVersionUID = 1L;

  /** Associated device */
  Device device;

  /**
   * Constructor
   */
  DeviceItem(ImageIcon icon, String sName, Device device) {
    this.device = device;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JLabel jlIcon = new JLabel(icon);
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
   * @param device
   *          The device to set.
   */
  public void setDevice(Device device) {
    this.device = device;
  }

}
