/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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

package org.jajuk.base.managers;

import static org.jajuk.util.Resources.AUTO_REFRESH_DELAY;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.items.Device;
import org.jajuk.base.items.Item;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.Resources.ConfKeys;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage devices
 */
public class DeviceManager extends ItemManager<Device> {
  /** Supported device types names */
  private ArrayList<String> alDevicesTypes = new ArrayList<String>(10);

  /** Date last global refresh */
  private long lDateLastGlobalRefresh = 0;

  /** List of deep-refresh devices after an upgrade */
  private Set<Device> devicesDeepRefreshed = new HashSet<Device>();

  /** Auto-refresh thread */
  private Thread tAutoRefresh = new Thread() {
    @Override
    public void run() {
      while (!Main.isExiting()) {
        try {
          Thread.sleep(AUTO_REFRESH_DELAY);
          refreshAllDevices();
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }
  };

  private boolean bGlobalRefreshing = false;

  private static MetaProperty[]  infos = new MetaProperty[] {
    // ID
    new MetaProperty(XML.ID,   false, true, false, false, false, String.class, null),
    // name
    new MetaProperty(XML.NAME, false, true, true,  false, false, String.class, null),
    // type
    new MetaProperty(XML.TYPE, false, true, true,  false, false, Long.class,   null),
    // URL
    new MetaProperty(XML.URL,  false, true, true,  false, false, Long.class,   null),
    // auto-mount
    new MetaProperty(XML.DEVICE_AUTO_MOUNT,    false, true, true, false, false, Boolean.class, null),
    // auto-refresh
    new MetaProperty(XML.DEVICE_AUTO_REFRESH,  false, true, true, false, false, Double.class,  0d),
    // expand
    new MetaProperty(XML.EXPANDED, false, false, false, false, true, Boolean.class, false),
    // synchro-source
    new MetaProperty(XML.DEVICE_SYNCHRO_SOURCE, false, false, true, false, false, String.class, null),
    // synchro-mode
    new MetaProperty(XML.DEVICE_SYNCHRO_MODE, false, false, true, false, false, String.class, null)
  };

  /**
   * No constructor available, only static access
   */
  public DeviceManager() {
    super();
    // register properties
    for (final MetaProperty i : infos) {
      registerProperty(i);
    }
  }

  public void startAutoRefreshThread() {
    tAutoRefresh.setPriority(Thread.MIN_PRIORITY);
    tAutoRefresh.start();
  }

  /**
   * Register a device
   *
   * @param sName
   * @return device
   */
  public Device registerDevice(final String sName, final long lDeviceType, final String sUrl) {
    final String sId = createID(sName);
    return registerDevice(sId, sName, lDeviceType, sUrl);
  }

  /**
   * Register a device with a known id
   *
   * @param sName
   * @return device
   */
  public Device registerDevice(final String sId, final String sName, final long lDeviceType, final String sUrl) {
    synchronized (getLock()) {
      final Device device = new Device(sId, sName);

      device.getProperties().set(XML.TYPE, lDeviceType);
      device.setUrl(sUrl);
      getItems().put(sId, device);
      return device;
    }
  }

  /**
   * Process to compute a device id
   *
   * @param sName
   * @return An id
   */
  public static String createID(final String sName) {
    return MD5Processor.hash(sName); // reprocess id;
  }

  /**
   * Check none device already has this name or is a parent directory
   *
   * @param sName
   * @param iDeviceType
   * @param sUrl
   * @param bNew:
   *          is it a new device ?
   * @return 0:ok or error code
   */
  public int checkDeviceAvailablity(final String sName, final int iDeviceType, final String sUrl, final boolean bNew) {
    synchronized (getLock()) {
      // don't check if it is a CD as all CDs may use the same mount point
      if (iDeviceType == Device.TYPE_CD) {
        return 0;
      }
      // check name and path
      final Iterator it = getItems().values().iterator();
      while (it.hasNext()) {
        final Device deviceToCheck = (Device) it.next();
        // If we check an existing device unchanged, just leave
        if (!bNew && sUrl.equals(deviceToCheck.getUrl())) {
          continue;
        }
        if (bNew && (sName.toLowerCase().equals(deviceToCheck.getName().toLowerCase()))) {
          return 19;
        }
        final String sUrlChecked = deviceToCheck.getUrl();
        // check it is not a sub-directory of an existing device
        final File fNew = new File(sUrl);
        final File fChecked = new File(sUrlChecked);
        if (fNew.equals(fChecked) || Util.isDescendant(fNew, fChecked)
            || Util.isAncestor(fNew, fChecked)) {
          return 29;
        }
      }
      // check availability
      if (iDeviceType != 2) { // not a remote device, TBI for remote
        // test directory is available
        final File file = new File(sUrl);
        // check if the url exists and is readable
        if (!file.exists() || !file.canRead()) {
          return 143;
        }
      }
      return 0;
    }
  }

  /**
   * Register a device type
   *
   * @param sDeviceType
   */
  public void registerDeviceType(final String sDeviceType) {
    alDevicesTypes.add(sDeviceType);
  }

  /**
   * @return number of registered devices
   */
  public int getDeviceTypesNumber() {
    return alDevicesTypes.size();
  }

  /**
   * @return Device types iteration
   */
  public Iterator getDeviceTypes() {
    return alDevicesTypes.iterator();
  }

  /**
   * Get a device type name for a given index
   *
   * @param index
   * @return device name for a given index
   */
  public String getDeviceType(final long index) {
    return alDevicesTypes.get((int) index);
  }

  /**
   * Remove a device
   *
   * @param device
   */
  public void removeDevice(final Device device) {
    synchronized (getLock()) {
      // show confirmation message if required
      if (ConfigurationManager.getBoolean(ConfKeys.CONFIRMATIONS_REMOVE_DEVICE)) {
        final int iResu = Messages.getChoice(Messages.getString("Confirmation_remove_device"),
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (iResu != JOptionPane.YES_OPTION) {
          return;
        }
      }
      // if device is refreshing or synchronizing, just leave
      if (device.isSynchronizing() || device.isRefreshing()) {
        Messages.showErrorMessage(13);
        return;
      }
      // check if device can be unmounted
      if (!FIFO.canUnmount(device)) {
        Messages.showErrorMessage(121);
        return;
      }
      // if it is mounted, try to unmount it
      if (device.isMounted()) {
        try {
          device.unmount();
        } catch (final Exception e) {
          Messages.showErrorMessage(13);
          return;
        }
      }
      getItems().remove(device.getID());
      ((DirectoryManager) ItemType.Directory.getManager()).cleanDevice(device.getID());
      ((FileManager) ItemType.File.getManager()).cleanDevice(device.getID());
      ((PlaylistFileManager) ItemType.PlaylistFile.getManager()).cleanDevice(device.getID());
      // Clean the collection up
      org.jajuk.base.Collection.cleanup();
      // remove synchronization if another device was synchronized
      // with this device
      final Iterator it = getItems().values().iterator();
      while (it.hasNext()) {
        final Device deviceToCheck = (Device) it.next();
        if (deviceToCheck.getProperties().contains(XML.DEVICE_SYNCHRO_SOURCE)) {
          final String sSyncSource = deviceToCheck.getStringValue(XML.DEVICE_SYNCHRO_SOURCE);
          if (sSyncSource.equals(device.getID())) {
            deviceToCheck.removeProperty(XML.DEVICE_SYNCHRO_SOURCE);
          }
        }
      }
    }
  }

  /**
   * @return whether any device is currently refreshing
   */
  public boolean isAnyDeviceRefreshing() {
    synchronized (getLock()) {
      boolean bOut = false;
      final Iterator it = getCarbonItems().iterator();
      while (it.hasNext()) {
        final Device device = (Device) it.next();
        if (device.isRefreshing()) {
          bOut = true;
          break;
        }
      }
      return bOut;
    }
  }

  /**
   * Clean all devices
   */
  public synchronized void cleanAllDevices() {
    synchronized (getLock()) {
      final Iterator it = getItems().values().iterator();
      while (it.hasNext()) {
        final Device device = (Device) it.next();
        // Do not auto-refresh CD as several CD may share the same mount
        // point
        if (device.getType() == Device.TYPE_CD) {
          continue;
        }
        ((FileManager) ItemType.File.getManager()).cleanDevice(device.getName());
        ((DirectoryManager) ItemType.Directory.getManager()).cleanDevice(device.getName());
        ((PlaylistFileManager) ItemType.PlaylistFile.getManager()).cleanDevice(device.getName());
      }
      getItems().clear();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML.DEVICES;
  }

  public long getDateLastGlobalRefresh() {
    return lDateLastGlobalRefresh;
  }

  /**
   * Refresh of all devices with auto-refresh enabled (used in automatic mode)
   * Must be the shortest possible
   */
  public void refreshAllDevices() {
    try {
      // check thread is not already refreshing
      if (bGlobalRefreshing) {
        return;
      }
      bGlobalRefreshing = true;
      long l = System.currentTimeMillis();
      lDateLastGlobalRefresh = System.currentTimeMillis();
      boolean bNeedUIRefresh = false;
      for (final Item item : getCarbonItems()) {
        final Device device = (Device) item;
        // Do not auto-refresh CD as several CD may share the same mount
        // point
        if (device.getType() == Device.TYPE_CD) {
          continue;
        }
        final double frequency = 60000 * device.getDoubleValue(XML.DEVICE_AUTO_REFRESH);
        // check if this device needs auto-refresh
        if ((frequency == 0d)
            || (device.getDateLastRefresh() > (System.currentTimeMillis() - frequency))) {
          continue;
        }
        // Check of mounted device contains files, otherwise it is not
        // mounted
        // we have to check this because of the automatic cleaner thread
        // musn't remove all references
        final File[] files = new File(device.getUrl()).listFiles();
        if (!device.isRefreshing() && (files != null) && (files.length > 0)) {
          // Check if this device should be deep-refresh after an
          // upgrade
          final boolean bNeedDeepAfterUpgrade = Main.isUpgradeDetected()
              && !devicesDeepRefreshed.contains(device);
          if (bNeedDeepAfterUpgrade) {
            // Store this device to avoid duplicate deep refreshes
            devicesDeepRefreshed.add(device);
          }
          // cleanup device
          bNeedUIRefresh = bNeedUIRefresh | device.cleanRemovedFiles();
          // logical or, not an error ! refresh it
          bNeedUIRefresh = bNeedUIRefresh | device.refreshCommand(bNeedDeepAfterUpgrade, false);
        }
      }

      // //cleanup logical items
      if (bNeedUIRefresh) {
        ItemType.Track.getManager().cleanup();
        ItemType.Style.getManager().cleanup();
        ItemType.Album.getManager().cleanup();
        ItemType.Author.getManager().cleanup();
        ItemType.Playlist.getManager().cleanup();
        // notify views to refresh
        ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
      }
      // Display end of refresh message with stats
      l = System.currentTimeMillis() - l;
    } catch (final Exception e) {
      Log.error(e);
    } finally {
      bGlobalRefreshing = false;
    }
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Device getDeviceByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }
}
