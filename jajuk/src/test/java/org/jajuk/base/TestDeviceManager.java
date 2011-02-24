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
 *  $Revision: 3132 $
 */
package org.jajuk.base;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestDeviceManager extends JajukTestCase {

  @Override
  protected void setUp() throws Exception {
    // avoid UI:
    Conf.setProperty(Const.CONF_CONFIRMATIONS_REMOVE_DEVICE, "false");
    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getLabel()}.
   */
  public final void testGetLabel() {
    assertEquals(Const.XML_DEVICES, DeviceManager.getInstance().getLabel());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#startAutoRefreshThread()}.
   */
  public final void testStartAutoRefreshThread() {
    DeviceManager.getInstance().startAutoRefreshThread();

    // what happens if done twice?
    DeviceManager.getInstance().startAutoRefreshThread();
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getInstance()}.
   */
  public final void testGetInstance() {
    assertNotNull(DeviceManager.getInstance());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#registerDevice(java.lang.String, long, java.lang.String)}
   * .
   */
  public final void testRegisterDeviceStringLongString() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));

    assertNotNull(DeviceManager.getInstance().getDeviceByName("device"));
  }

  public final void testRegisterDeviceTwice() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));
    assertNotNull(DeviceManager.getInstance().registerDevice("device", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));

    assertNotNull(DeviceManager.getInstance().getDeviceByName("device"));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#registerDevice(java.lang.String, java.lang.String, long, java.lang.String)}
   * .
   */
  public final void testRegisterDeviceStringStringLongString() {
    assertNotNull(DeviceManager.getInstance().registerDevice("2", "device2", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));

    assertNotNull(DeviceManager.getInstance().getDeviceByName("device2"));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#createID(java.lang.String)}.
   */
  public final void testCreateID() {
    assertNotNull(DeviceManager.createID("device123"));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#checkDeviceAvailablity(java.lang.String, int, java.lang.String, boolean)}
   * .
   */
  public final void testCheckDeviceAvailablityCD() {
    assertEquals(0, DeviceManager.getInstance().checkDeviceAvailablity("device3",
        Device.Type.FILES_CD, System.getProperty("java.io.tmpdir"), true));

  }

  public final void testCheckDeviceAvailablityExistingName() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device4", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));

    // error, name already exists
    assertEquals(19, DeviceManager.getInstance().checkDeviceAvailablity("device4",
        Device.Type.DIRECTORY, System.getProperty("java.io.tmpdir"), true));
  }

  public final void testCheckDeviceAvailablityExistingNameNotNew() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device4", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));

    // error, name already exists
    assertEquals(0, DeviceManager.getInstance().checkDeviceAvailablity("device4",
        Device.Type.DIRECTORY, System.getProperty("java.io.tmpdir"), false));
  }

  public final void testCheckDeviceAvailablityParentOrDescendant() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device5", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));

    // error, same url
    assertEquals(29, DeviceManager.getInstance().checkDeviceAvailablity("device3",
        Device.Type.DIRECTORY, System.getProperty("java.io.tmpdir"), true));
    // error, descendant url
    assertEquals(29, DeviceManager.getInstance().checkDeviceAvailablity("device3",
        Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir") + java.io.File.separator + "test", true));
    // error, parent url
    assertEquals(29, DeviceManager.getInstance().checkDeviceAvailablity("device3",
        Device.Type.DIRECTORY, new java.io.File(System.getProperty("java.io.tmpdir")).getParent(),
        true));

  }

  public final void testCheckDeviceAvailablityNotExists() {
    assertEquals(143, DeviceManager.getInstance().checkDeviceAvailablity("device3",
        Device.Type.DIRECTORY, "notexistingpath", true));

  }

  public final void testCheckDeviceAvailablityExists() {
    assertEquals(0, DeviceManager.getInstance().checkDeviceAvailablity("device3",
        Device.Type.DIRECTORY, System.getProperty("java.io.tmpdir"), true));

  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getDeviceTypes()}.
   */
  public final void testGetDeviceTypes() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getDeviceType(long)}.
   */
  public final void testGetDeviceType() {
    // tested above
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#removeDevice(org.jajuk.base.Device)}.
   */
  public final void testRemoveDevice() {
    assertNotNull(DeviceManager.getInstance().registerDevice("5", "device5", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));

    assertNotNull(DeviceManager.getInstance().getDeviceByID("5"));

    DeviceManager.getInstance().removeDevice(DeviceManager.getInstance().getDeviceByID("5"));

    assertNull(DeviceManager.getInstance().getDeviceByID("5"));
  }

  public final void testRemoveDeviceMounted() throws Exception {
    Device device = DeviceManager.getInstance().registerDevice("14", "device14",
        Device.Type.DIRECTORY, System.getProperty("java.io.tmpdir"));
    device.mount(true);

    assertNotNull(DeviceManager.getInstance().getDeviceByID("14"));

    DeviceManager.getInstance().removeDevice(DeviceManager.getInstance().getDeviceByID("14"));

    assertNull(DeviceManager.getInstance().getDeviceByID("14"));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#isAnyDeviceRefreshing()}.
   */
  public final void testIsAnyDeviceRefreshing() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device8", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir")));
    assertNotNull(DeviceManager.getInstance().registerDevice("device9", Device.Type.FILES_CD,
        System.getProperty("java.io.tmpdir")));

    assertFalse(DeviceManager.getInstance().isAnyDeviceRefreshing());
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#cleanAllDevices()}.
   */
  public final void testCleanAllDevices() {
    DeviceManager.getInstance().registerDevice("device6", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    DeviceManager.getInstance().registerDevice("device7", Device.Type.FILES_CD,
        System.getProperty("java.io.tmpdir"));
    JUnitHelpers.cleanAllDevices();
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#getDateLastGlobalRefresh()}.
   */
  public final void testGetDateLastGlobalRefresh() {
    StartupCollectionService.registerItemManagers();

    DeviceManager.getInstance().registerDevice("device6", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    DeviceManager.getInstance().registerDevice("device7", Device.Type.FILES_CD,
        System.getProperty("java.io.tmpdir"));

    // first do a refresh
    DeviceManager.getInstance().refreshAllDevices();

    // then the timestamp should be non-zero
    assertTrue(0 != DeviceManager.getInstance().getDateLastGlobalRefresh());

  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#refreshAllDevices()}.
   */
  public final void testRefreshAllDevices() {
    StartupCollectionService.registerItemManagers();

    DeviceManager.getInstance().registerDevice("device6", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    DeviceManager.getInstance().registerDevice("device7", Device.Type.FILES_CD,
        System.getProperty("java.io.tmpdir"));

    DeviceManager.getInstance().refreshAllDevices();
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#getDeviceByID(java.lang.String)}.
   */
  public final void testGetDeviceByID() {
    // tested above
  }

  /**
   * Test method for
   * {@link org.jajuk.base.DeviceManager#getDeviceByName(java.lang.String)}.
   */
  public final void testGetDeviceByName() {
    DeviceManager.getInstance().registerDevice("device10", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    DeviceManager.getInstance().registerDevice("device11", Device.Type.FILES_CD,
        System.getProperty("java.io.tmpdir"));

    assertNotNull(DeviceManager.getInstance().getDeviceByName("device10"));
    assertNull(DeviceManager.getInstance().getDeviceByName("notexistingdevice"));
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getDevices()}.
   */
  public final void testGetDevices() {
    assertEquals(0, DeviceManager.getInstance().getDevices().size());
    DeviceManager.getInstance().registerDevice("device12", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    DeviceManager.getInstance().registerDevice("device13", Device.Type.FILES_CD,
        System.getProperty("java.io.tmpdir"));
    assertEquals(2, DeviceManager.getInstance().getDevices().size());
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getDevicesIterator()}.
   */
  public final void testGetDevicesIterator() {
    assertFalse(DeviceManager.getInstance().getDevicesIterator().hasNext());
    DeviceManager.getInstance().registerDevice("device12", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    DeviceManager.getInstance().registerDevice("device13", Device.Type.FILES_CD,
        System.getProperty("java.io.tmpdir"));
    assertTrue(DeviceManager.getInstance().getDevicesIterator().hasNext());
  }

}
