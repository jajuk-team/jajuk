/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.base;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.jajuk.ConstTest;
import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestDeviceManager extends JajukTestCase {
  @Override
  protected void specificSetUp() throws Exception {
    // avoid UI:
    Conf.setProperty(Const.CONF_CONFIRMATIONS_REMOVE_DEVICE, "false");
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getXMLTag()}.
   */
  @Test
  public final void testGetLabel() {
    assertEquals(Const.XML_DEVICES, DeviceManager.getInstance().getXMLTag());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.DeviceManager#startAutoRefreshThread()}.
   */
  @Test
  public final void testStartAutoRefreshThread() {
    DeviceManager.getInstance().startAutoRefreshThread();
    // what happens if done twice?
    DeviceManager.getInstance().startAutoRefreshThread();
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getInstance()}.
   */
  @Test
  public final void testGetInstance() {
    assertNotNull(DeviceManager.getInstance());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.DeviceManager#registerDevice(java.lang.String, long, java.lang.String)}
   * .
   */
  @Test
  public final void testRegisterDeviceStringLongString() {
    assertNotNull(TestHelpers.getDevice());
    assertNotNull(DeviceManager.getInstance().getDeviceByName("sample_device"));
  }

  /**
   * Test register device twice.
   * 
   */
  @Test
  public final void testRegisterDeviceTwice() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev"));
    assertNotNull(DeviceManager.getInstance().registerDevice("device", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev"));
    assertNotNull(DeviceManager.getInstance().getDeviceByName("device"));
  }

  /**
  * Test method for.
  *
  * {@link org.jajuk.base.DeviceManager#createID(java.lang.String)}.
  */
  @Test
  public final void testCreateID() {
    assertNotNull(ItemManager.createID("device123"));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.DeviceManager#checkDeviceAvailablity(java.lang.String, int, java.lang.String, boolean)}
   * .
   */
  @Test
  public final void testCheckDeviceAvailablityCD() {
    assertEquals(
        0,
        DeviceManager.getInstance().checkDeviceAvailablity("device3", Device.Type.FILES_CD,
            ConstTest.DEVICES_BASE_PATH + "/dev", true));
  }

  /**
   * Test check device availablity existing name.
   * 
   */
  @Test
  public final void testCheckDeviceAvailablityExistingName() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device4", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev"));
    // error, name already exists
    assertEquals(
        19,
        DeviceManager.getInstance().checkDeviceAvailablity("device4", Device.Type.DIRECTORY,
            ConstTest.DEVICES_BASE_PATH + "/dev", true));
  }

  /**
   * Test check device availability existing name not new.
   **/
  @Test
  public final void testCheckDeviceAvailablityExistingNameNotNew() {
    assertNotNull(TestHelpers.getDevice("device4", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev"));
    // error, name already exists
    assertEquals(
        0,
        DeviceManager.getInstance().checkDeviceAvailablity("device4", Device.Type.DIRECTORY,
            ConstTest.DEVICES_BASE_PATH + "/dev", false));
  }

  /**
   * Test check device availability parent or descendant.
   * 
   */
  @Test
  public final void testCheckDeviceAvailablityParentOrDescendant() {
    assertNotNull(TestHelpers.getDevice("device5", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev"));
    // error, same url
    assertEquals(
        29,
        DeviceManager.getInstance().checkDeviceAvailablity("device3", Device.Type.DIRECTORY,
            ConstTest.DEVICES_BASE_PATH + "/dev", true));
    // error, descendant url
    assertEquals(
        29,
        DeviceManager.getInstance().checkDeviceAvailablity("device3", Device.Type.DIRECTORY,
            ConstTest.DEVICES_BASE_PATH + "/dev/subdir", true));
    // error, parent url
    assertEquals(
        29,
        DeviceManager.getInstance().checkDeviceAvailablity("device3", Device.Type.DIRECTORY,
            ConstTest.DEVICES_BASE_PATH, true));
  }

  /**
   * Test check device availability not exists.
   * 
   */
  @Test
  public final void testCheckDeviceAvailablityNotExists() {
    assertEquals(
        143,
        DeviceManager.getInstance().checkDeviceAvailablity("device3", Device.Type.DIRECTORY,
            "notexistingpath", true));
  }

  /**
   * Test check device availability exists.
   * 
   */
  @Test
  public final void testCheckDeviceAvailablityExists() {
    assertEquals(
        0,
        DeviceManager.getInstance().checkDeviceAvailablity("device3", Device.Type.DIRECTORY,
            ConstTest.TEMP_PATH, true));
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getDeviceTypes()}.
   */
  @Test
  public final void testGetDeviceTypes() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getDeviceType(long)}.
   */
  @Test
  public final void testGetDeviceType() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.DeviceManager#removeDevice(org.jajuk.base.Device)}.
   */
  @Test
  public final void testRemoveDevice() {
    assertNotNull(DeviceManager.getInstance().registerDevice("5", "device5", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev"));
    assertNotNull(DeviceManager.getInstance().getDeviceByID("5"));
    DeviceManager.getInstance().removeDevice(DeviceManager.getInstance().getDeviceByID("5"));
    assertNull(DeviceManager.getInstance().getDeviceByID("5"));
  }

  /**
   * Test remove device mounted.
   * 
   *
   * @throws Exception the exception
   */
  @Test
  public final void testRemoveDeviceMounted() throws Exception {
    TestHelpers.getDevice();
    String id = DeviceManager.createID("sample_device");
    assertNotNull(DeviceManager.getInstance().getDeviceByID(id));
    DeviceManager.getInstance().removeDevice(DeviceManager.getInstance().getDeviceByID(id));
    assertNull(DeviceManager.getInstance().getDeviceByID(id));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.DeviceManager#isAnyDeviceRefreshing()}.
   */
  @Test
  public final void testIsAnyDeviceRefreshing() {
    assertNotNull(DeviceManager.getInstance().registerDevice("device8", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev8"));
    assertNotNull(DeviceManager.getInstance().registerDevice("device9", Device.Type.FILES_CD,
        ConstTest.DEVICES_BASE_PATH + "/dev9"));
    assertFalse(DeviceManager.getInstance().isAnyDeviceRefreshing());
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#cleanAllDevices()}.
   */
  @Test
  public final void testCleanAllDevices() {
    DeviceManager.getInstance().registerDevice("device6", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev6");
    DeviceManager.getInstance().registerDevice("device7", Device.Type.FILES_CD,
        ConstTest.DEVICES_BASE_PATH + "/dev7");
    TestHelpers.cleanAllDevices();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.DeviceManager#getDateLastGlobalRefresh()}.
   */
  @Test
  public final void testGetDateLastGlobalRefresh() {
    StartupCollectionService.registerItemManagers();
    DeviceManager.getInstance().registerDevice("device6", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev6");
    DeviceManager.getInstance().registerDevice("device7", Device.Type.FILES_CD,
        ConstTest.DEVICES_BASE_PATH + "/dev7");
    // first do a refresh
    DeviceManager.getInstance().refreshAllDevices();
    // then the timestamp should be non-zero
    assertTrue(0 != DeviceManager.getInstance().getDateLastGlobalRefresh());
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#refreshAllDevices()}.
   */
  @Test
  public final void testRefreshAllDevices() {
    StartupCollectionService.registerItemManagers();
    DeviceManager.getInstance().registerDevice("device6", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev6");
    DeviceManager.getInstance().registerDevice("device7", Device.Type.FILES_CD,
        ConstTest.DEVICES_BASE_PATH + "/dev7");
    DeviceManager.getInstance().refreshAllDevices();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.DeviceManager#getDeviceByID(java.lang.String)}.
   */
  @Test
  public final void testGetDeviceByID() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.DeviceManager#getDeviceByName(java.lang.String)}.
   */
  @Test
  public final void testGetDeviceByName() {
    DeviceManager.getInstance().registerDevice("device10", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev10");
    DeviceManager.getInstance().registerDevice("device11", Device.Type.FILES_CD,
        ConstTest.DEVICES_BASE_PATH + "/dev11");
    assertNotNull(DeviceManager.getInstance().getDeviceByName("device10"));
    assertNull(DeviceManager.getInstance().getDeviceByName("notexistingdevice"));
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getDevices()}.
   */
  @Test
  public final void testGetDevices() {
    assertEquals(0, DeviceManager.getInstance().getDevices().size());
    DeviceManager.getInstance().registerDevice("device12", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev12");
    DeviceManager.getInstance().registerDevice("device13", Device.Type.FILES_CD,
        ConstTest.DEVICES_BASE_PATH + "/dev13");
    assertEquals(2, DeviceManager.getInstance().getDevices().size());
  }

  /**
   * Test method for {@link org.jajuk.base.DeviceManager#getDevicesIterator()}.
   */
  @Test
  public final void testGetDevicesIterator() {
    assertFalse(DeviceManager.getInstance().getDevicesIterator().hasNext());
    DeviceManager.getInstance().registerDevice("device12", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/dev12");
    DeviceManager.getInstance().registerDevice("device13", Device.Type.FILES_CD,
        ConstTest.DEVICES_BASE_PATH + "/dev13");
    assertTrue(DeviceManager.getInstance().getDevicesIterator().hasNext());
  }
}
