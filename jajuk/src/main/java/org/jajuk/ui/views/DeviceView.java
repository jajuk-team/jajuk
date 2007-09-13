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
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.IView;
import org.jajuk.ui.wizard.DeviceWizard;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;

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

import ext.FlowScrollPanel;

/**
 * Device view used to create and modify Jajuk devices
 * <p>
 * Configuration perspective
 */
public class DeviceView extends ViewAdapter implements IView, ITechnicalStrings, ActionListener,
		Observer, MouseListener {
	private static final long serialVersionUID = 1L;

	static private DeviceView dv; // self instance

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
		jpDevices.setScroller(jsp);

		jpDevices.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Popup menus
		jpmenu = new JPopupMenu();

		jmiMount = new JMenuItem(Messages.getString("DeviceView.8"), IconLoader.ICON_MOUNT);
		jmiMount.addActionListener(this);
		jmiMount.setActionCommand(EventSubject.EVENT_DEVICE_MOUNT.toString());
		jpmenu.add(jmiMount);

		jmiUnmount = new JMenuItem(Messages.getString("DeviceView.9"), IconLoader.ICON_UNMOUNT);
		jmiUnmount.addActionListener(this);
		jmiUnmount.setActionCommand(EventSubject.EVENT_DEVICE_UNMOUNT.toString());
		jpmenu.add(jmiUnmount);

		jmiRefresh = new JMenuItem(Messages.getString("DeviceView.11"), IconLoader.ICON_REFRESH);
		jmiRefresh.addActionListener(this);
		jmiRefresh.setActionCommand(EventSubject.EVENT_DEVICE_REFRESH.toString());
		jpmenu.add(jmiRefresh);

		jmiTest = new JMenuItem(Messages.getString("DeviceView.10"), IconLoader.ICON_TEST);
		jmiTest.addActionListener(this);
		jmiTest.setActionCommand(EventSubject.EVENT_DEVICE_TEST.toString());
		jpmenu.add(jmiTest);

		jmiSynchronize = new JMenuItem(Messages.getString("DeviceView.12"), IconLoader.ICON_SYNCHRO);
		jmiSynchronize.addActionListener(this);
		jmiSynchronize.setActionCommand(EventSubject.EVENT_DEVICE_SYNCHRO.toString());
		jpmenu.add(jmiSynchronize);

		jmiDelete = new JMenuItem(Messages.getString("DeviceView.13"), IconLoader.ICON_DELETE);
		jmiDelete.addActionListener(this);
		jmiDelete.setActionCommand(EventSubject.EVENT_DEVICE_DELETE.toString());
		jpmenu.add(jmiDelete);

		jmiProperties = new JMenuItem(Messages.getString("DeviceView.14"),
				IconLoader.ICON_CONFIGURATION);
		jmiProperties.addActionListener(this);
		jmiProperties.setActionCommand(EventSubject.EVENT_DEVICE_PROPERTIES.toString());
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

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_MOUNT);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_UNMOUNT);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_NEW);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
		return eventSubjectSet;
	}

	private void refreshDevices() {
		synchronized (DeviceManager.getInstance().getLock()) {
			// remove all devices
			if (jpDevices.getComponentCount() > 0) {
				jpDevices.removeAll();
			}
			// New device
			DeviceItem diNew = new DeviceItem(IconLoader.ICON_DEVICE_NEW, Messages
					.getString("DeviceView.17"), null);
			diNew.setToolTipText(Messages.getString("DeviceView.18"));
			jpDevices.add(diNew);
			diNew.addMouseListener(new MouseAdapter() {
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
				ImageIcon icon = IconLoader.ICON_DEVICE_DIRECTORY_MOUNTED;
				String sTooltip = "";
				switch ((int) device.getType()) {
				case 0:
					sTooltip = Messages.getString("Device_type.directory");
					if (device.isMounted()) {
						icon = IconLoader.ICON_DEVICE_DIRECTORY_MOUNTED;
					} else {
						icon = IconLoader.ICON_DEVICE_DIRECTORY_UNMOUNTED;
					}
					break;
				case 1:
					sTooltip = Messages.getString("Device_type.file_cd");
					if (device.isMounted()) {
						icon = IconLoader.ICON_DEVICE_CD_MOUNTED;
					} else {
						icon = IconLoader.ICON_DEVICE_CD_UNMOUNTED;
					}
					break;
				case 2:
					sTooltip = Messages.getString("Device_type.network_drive");
					if (device.isMounted()) {
						icon = IconLoader.ICON_DEVICE_NETWORK_DRIVE_MOUNTED;
					} else {
						icon = IconLoader.ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED;
					}
					break;
				case 3:
					sTooltip = Messages.getString("Device_type.extdd");
					if (device.isMounted()) {
						icon = IconLoader.ICON_DEVICE_EXT_DD_MOUNTED;
					} else {
						icon = IconLoader.ICON_DEVICE_EXT_DD_UNMOUNTED;
					}
					break;
				case 4:
					sTooltip = Messages.getString("Device_type.player");
					if (device.isMounted()) {
						icon = IconLoader.ICON_DEVICE_PLAYER_MOUNTED;
					} else {
						icon = IconLoader.ICON_DEVICE_PLAYER_UNMOUNTED;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#setVisible(boolean)
	 */
	public void setVisible(boolean pVisible) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#getComponent()
	 */
	public Component getComponent() {
		return this;
	}

	/**
	 * Singleton implementation
	 * 
	 * @return
	 */
	public static synchronized DeviceView getInstance() {
		if (dv == null) {
			dv = new DeviceView();
		}
		return dv;
	}

	public void actionPerformed(final ActionEvent ae) {
		if (ae.getActionCommand().equals(EventSubject.EVENT_DEVICE_NEW.toString())) {
			DeviceWizard dw = new DeviceWizard();
			dw.updateWidgetsDefault();
			dw.pack();
			dw.setVisible(true);
			return;
		}
		if (diSelected == null) { // test a device is selected
			return;
		}
		if (ae.getActionCommand().equals(EventSubject.EVENT_DEVICE_DELETE.toString())) {
			DeviceManager.getInstance().removeDevice(diSelected.getDevice());
			jpDevices.remove(diSelected);
			ObservationManager.notify(new Event(EventSubject.EVENT_VIEW_REFRESH_REQUEST));
			// refresh views
			ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
		} else if (ae.getActionCommand().equals(EventSubject.EVENT_DEVICE_MOUNT.toString())) {
			try {
				diSelected.getDevice().mount();
			} catch (Exception e) {
				Messages.showErrorMessage(011);
			}
		} else if (ae.getActionCommand().equals(EventSubject.EVENT_DEVICE_UNMOUNT.toString())) {
			try {
				diSelected.getDevice().unmount();
			} catch (Exception e) {
				Messages.showErrorMessage(012);
			}
		} else if (ae.getActionCommand().equals(EventSubject.EVENT_DEVICE_PROPERTIES.toString())) {
			DeviceWizard dw = new DeviceWizard();
			dw.updateWidgets(diSelected.getDevice());
			dw.pack();
			dw.setVisible(true);
		} else if (ae.getActionCommand().equals(EventSubject.EVENT_DEVICE_REFRESH.toString())) {
			diSelected.getDevice().refresh(true, true); // ask deep or fast scan
		} else if (ae.getActionCommand().equals(EventSubject.EVENT_DEVICE_SYNCHRO.toString())) {
			diSelected.getDevice().synchronize(true);
		} else if (ae.getActionCommand().equals(EventSubject.EVENT_DEVICE_TEST.toString())) {
			new Thread() {// test asynchronously in case of delay (samba
				// pbm for ie)
				public void run() {
					if (diSelected.getDevice().test()) {
						Messages.showInfoMessage(Messages.getString("DeviceView.21"),
								IconLoader.ICON_OK);
					} else {
						Messages.showInfoMessage(Messages.getString("DeviceView.22"),
								IconLoader.ICON_KO);
					}
				}
			}.start();
		} else if (ae.getActionCommand().equals(EventSubject.EVENT_WIZARD.toString())) {

		}
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
		EventSubject subject = event.getSubject();
		if (EventSubject.EVENT_DEVICE_MOUNT.equals(subject)
				|| EventSubject.EVENT_DEVICE_UNMOUNT.equals(subject)
				|| EventSubject.EVENT_DEVICE_REFRESH.equals(subject)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Util.waiting();
					refreshDevices();
					jpDevices.revalidate();
					jpDevices.repaint();
					Util.stopWaiting();
				}
			});
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			handlePopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			handlePopup(e);
		} else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
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
	 *            The device to set.
	 */
	public void setDevice(Device device) {
		this.device = device;
	}

}
