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

package org.jajuk.base;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage devices.
 */
public final class DeviceManager extends ItemManager {

  /** Self instance. */
  private static DeviceManager singleton = new DeviceManager();

  /** Date last global refresh. */
  private long lDateLastGlobalRefresh = 0;

  /** List of deep-refresh devices after an upgrade. */
  private final Set<Device> devicesDeepRefreshed = new HashSet<Device>();

  /** Auto-refresh thread. */
  private final Thread tAutoRefresh = new Thread("Device Auto Refresh Thread") {
    @Override
    public void run() {
      while (!ExitService.isExiting()) {
        try {
          Thread.sleep(Const.AUTO_REFRESH_DELAY);
          refreshAllDevices();
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }
  };

  /** DOCUMENT_ME. */
  private volatile boolean bGlobalRefreshing = false;

  /**
   * No constructor available, only static access.
   */
  private DeviceManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, false, false,
        String.class, null));
    // Type
    registerProperty(new PropertyMetaInformation(Const.XML_TYPE, false, true, true, false, false,
        Long.class, null));
    // URL
    registerProperty(new PropertyMetaInformation(Const.XML_URL, false, true, true, false, false,
        Long.class, null));
    // Auto-mount
    registerProperty(new PropertyMetaInformation(Const.XML_DEVICE_AUTO_MOUNT, false, true, true,
        false, false, Boolean.class, null));
    // Auto-refresh
    registerProperty(new PropertyMetaInformation(Const.XML_DEVICE_AUTO_REFRESH, false, true, true,
        false, false, Double.class, 0d));
    // Expand
    registerProperty(new PropertyMetaInformation(Const.XML_EXPANDED, false, false, false, false,
        true, Boolean.class, false));
    // Synchro source
    registerProperty(new PropertyMetaInformation(Const.XML_DEVICE_SYNCHRO_SOURCE, false, false,
        true, false, false, String.class, null));
    // Synchro mode
    registerProperty(new PropertyMetaInformation(Const.XML_DEVICE_SYNCHRO_MODE, false, false, true,
        false, false, String.class, null));
  }

  /**
   * Start auto refresh thread.
   * DOCUMENT_ME
   */
  public void startAutoRefreshThread() {
    if (!tAutoRefresh.isAlive()) {
      tAutoRefresh.setPriority(Thread.MIN_PRIORITY);
      tAutoRefresh.start();
    }
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static DeviceManager getInstance() {
    return singleton;
  }

  /**
   * Register a device.
   * 
   * @param sName DOCUMENT_ME
   * @param deviceType DOCUMENT_ME
   * @param sUrl DOCUMENT_ME
   * 
   * @return device
   */
  public Device registerDevice(String sName, Device.Type deviceType, String sUrl) {
    String sId = createID(sName);
    return registerDevice(sId, sName, deviceType, sUrl);
  }

  /**
   * Register a device with a known id.
   *
   * @param sId DOCUMENT_ME
   * @param sName DOCUMENT_ME
   * @param deviceType DOCUMENT_ME
   * @param sUrl DOCUMENT_ME
   * @return device
   */
  Device registerDevice(String sId, String sName, Device.Type deviceType, String sUrl) {
    Device device = getDeviceByID(sId);
    if (device != null) {
      return device;
    }
    device = new Device(sId, sName);
    device.setProperty(Const.XML_TYPE, (long) deviceType.ordinal());
    device.setUrl(sUrl);
    registerItem(device);
    return device;
  }

  /**
   * Check none device already has this name or is a parent directory.
   *
   * @param sName DOCUMENT_ME
   * @param deviceType DOCUMENT_ME
   * @param sUrl DOCUMENT_ME
   * @param bNew DOCUMENT_ME
   * @return 0:ok or error code
   */
  public int checkDeviceAvailablity(String sName, Device.Type deviceType, String sUrl, boolean bNew) {
    // don't check if it is a CD as all CDs may use the same mount point
    if (deviceType == Device.Type.FILES_CD) {
      return 0;
    }
    // check name and path
    for (Device deviceToCheck : DeviceManager.getInstance().getDevices()) {
      // If we check an existing device unchanged, just leave
      if (!bNew && sUrl.equals(deviceToCheck.getUrl())) {
        continue;
      }
      // check for a new device with an existing name
      if (bNew && (sName.equalsIgnoreCase(deviceToCheck.getName()))) {
        return 19;
      }
      String sUrlChecked = deviceToCheck.getUrl();
      // check it is not a sub-directory of an existing device
      File fNew = new File(sUrl);
      File fChecked = new File(sUrlChecked);
      if (fNew.equals(fChecked) || UtilSystem.isDescendant(fNew, fChecked)
          || UtilSystem.isAncestor(fNew, fChecked)) {
        return 29;
      }
    }
    // check availability
    if (deviceType != Device.Type.EXTDD) { // not a remote device, TBI for remote
      // test directory is available
      File file = new File(sUrl);
      // check if the url exists and is readable
      if (!file.exists() || !file.canRead()) {
        return 143;
      }
    }
    return 0;
  }

  /**
   * Return first device found being parent of the provided path.
   * 
   * @param path DOCUMENT_ME
   * 
   * @return  first device found being parent of the provided path
   */
  Device getDeviceByPath(File path) {
    for (Device device : getDevices()) {
      if (UtilSystem.isAncestor(device.getFIO(), path)) {
        return device;
      }
    }
    return null;
  }

  /**
  * Remove a device.
  * 
  * @param device DOCUMENT_ME
  */
  public void removeDevice(Device device) {
    lock.writeLock().lock();
    try {
      // show confirmation message if required
      if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_REMOVE_DEVICE)) {
        int iResu = Messages.getChoice(Messages.getString("Confirmation_remove_device"),
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
      if (!QueueModel.canUnmount(device)) {
        Messages.showErrorMessage(121);
        return;
      }
      // if it is mounted, try to unmount it
      if (device.isMounted()) {
        try {
          device.unmount();
        } catch (Exception e) {
          Messages.showErrorMessage(13);
          return;
        }
      }
      removeItem(device);
      DirectoryManager.getInstance().cleanDevice(device.getID());
      FileManager.getInstance().cleanDevice(device.getID());
      PlaylistManager.getInstance().cleanDevice(device.getID());
      // Clean the collection up
      org.jajuk.base.Collection.cleanupLogical();
      // remove synchronization if another device was synchronized
      // with this device
      for (Device deviceToCheck : getDevices()) {
        if (deviceToCheck.containsProperty(Const.XML_DEVICE_SYNCHRO_SOURCE)) {
          String sSyncSource = deviceToCheck.getStringValue(Const.XML_DEVICE_SYNCHRO_SOURCE);
          if (sSyncSource.equals(device.getID())) {
            deviceToCheck.removeProperty(Const.XML_DEVICE_SYNCHRO_SOURCE);
          }
        }
      }
      // Force suggestion view refresh to avoid showing removed albums
      ObservationManager.notify(new JajukEvent(JajukEvents.SUGGESTIONS_REFRESH));
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Checks if is any device refreshing.
   * 
   * @return whether any device is currently refreshing
   */
  public boolean isAnyDeviceRefreshing() {
    boolean bOut = false;
    for (Device device : DeviceManager.getInstance().getDevices()) {
      if (device.isRefreshing()) {
        bOut = true;
        break;
      }
    }
    return bOut;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return Const.XML_DEVICES;
  }

  /**
   * Gets the date last global refresh.
   * 
   * @return the date last global refresh
   */
  public long getDateLastGlobalRefresh() {
    return lDateLastGlobalRefresh;
  }

  /**
   * Refresh of all devices with auto-refresh enabled (used in automatic mode)
   * Must be the shortest possible.
   */
  void refreshAllDevices() {
    try {
      // check thread is not already refreshing
      if (bGlobalRefreshing) {
        return;
      }
      bGlobalRefreshing = true;
      lDateLastGlobalRefresh = System.currentTimeMillis();
      boolean bNeedUIRefresh = false;
      for (Device device : getDevices()) {
        // Do not auto-refresh CD as several CD may share the same mount
        // point
        if (device.getType() == Device.Type.FILES_CD) {
          continue;
        }
        double frequency = 60000 * device.getDoubleValue(Const.XML_DEVICE_AUTO_REFRESH);
        // check if this device needs auto-refresh
        if (frequency == 0d
            || device.getDateLastRefresh() > (System.currentTimeMillis() - frequency)) {
          continue;
        }
        /*
         * Check if devices contains files, otherwise it is not mounted we have to check this
         * because of the automatic cleaner thread musn't remove all references
         */
        File[] files = new File(device.getUrl()).listFiles();
        if (!device.isRefreshing() && files != null && files.length > 0) {
          /*
           * Check if this device should be deep-refresh after an upgrade
           */
          boolean bNeedDeepAfterUpgrade = UpgradeManager.isMajorMigration()
              && !devicesDeepRefreshed.contains(device);
          if (bNeedDeepAfterUpgrade) {
            // Store this device to avoid duplicate deep refreshes
            devicesDeepRefreshed.add(device);
          }
          // cleanup device
          bNeedUIRefresh = bNeedUIRefresh | device.cleanRemovedFiles(null);
          // refresh the device (deep refresh forced after an upgrade)
          bNeedUIRefresh = bNeedUIRefresh
              | device.refreshCommand(bNeedDeepAfterUpgrade, false, null);

          // UI refresh if required
          if (bNeedUIRefresh) {
            // Cleanup logical items
            Collection.cleanupLogical();
            /*
             * Notify views to refresh once the device is refreshed, do not wait all devices
             * refreshing as it may be tool long
             */
            ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
          }
        }
      }
      // Display end of refresh message with stats
    } catch (Exception e) {
      Log.error(e);
    } finally {
      bGlobalRefreshing = false;
    }
  }

  /**
   * Gets the device by id.
   * 
   * @param sID Item ID
   * 
   * @return Element
   */
  public Device getDeviceByID(String sID) {
    return (Device) getItemByID(sID);
  }

  /**
   * Gets the device by name.
   * 
   * @param sName device name
   * 
   * @return device by given name or null if no match
   */
  public Device getDeviceByName(String sName) {
    for (Device device : getDevices()) {
      if (device.getName().equals(sName)) {
        return device;
      }
    }
    return null;
  }

  /**
   * Gets the devices.
   * 
   * @return ordered devices list
   */
  @SuppressWarnings("unchecked")
  public List<Device> getDevices() {
    return (List<Device>) getItems();
  }

  /**
   * Gets the devices iterator.
   * 
   * @return devices iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<Device> getDevicesIterator() {
    return new ReadOnlyIterator<Device>((Iterator<Device>) getItemsIterator());
  }
}
