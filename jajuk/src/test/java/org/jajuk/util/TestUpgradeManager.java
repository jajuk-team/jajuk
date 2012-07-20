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
package org.jajuk.util;

import org.jajuk.JajukTestCase;

/**
 * .
 */
public class TestUpgradeManager extends JajukTestCase {
  String v1 = "1.8.4";
  String v2 = "1.9.1";
  String v3 = "1.9.2RC3";
  String v4 = "1.8.5";
  String v5 = "1.8";
  String v6 = "1.9";
  String v7 = "1.9.2RC4";

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.util.UpgradeManager#getNumberRelease(String)}.
   */
  public void testGetNumberRelease() throws Exception {
    assertEquals(10804, UpgradeManager.getNumberRelease(v1));
    assertEquals(10901, UpgradeManager.getNumberRelease(v2));
    assertEquals(10902, UpgradeManager.getNumberRelease(v3));
    assertEquals(10805, UpgradeManager.getNumberRelease(v4));
    assertEquals(10800, UpgradeManager.getNumberRelease(v5));
    assertEquals(10900, UpgradeManager.getNumberRelease(v6));
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.util.UpgradeManager#isMajorMigration(String,String)}.
   */
  public void testIsMajorMigration() throws Exception {
    assertEquals(true, UpgradeManager.isMajorMigration(v1, v2));
    assertEquals(false, UpgradeManager.isMajorMigration(v1, v4));
    assertEquals(false, UpgradeManager.isMajorMigration(v2, v3));
    assertEquals(true, UpgradeManager.isMajorMigration(v5, v6));
    assertEquals(false, UpgradeManager.isMajorMigration(v3, v7));
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.util.UpgradeManager#isNewer(String,String)}.
   */
  public void testIsNewer() throws Exception {
    assertTrue(UpgradeManager.isNewer(v2, v1));
    assertTrue(UpgradeManager.isNewer(v4, v1));
    assertFalse(UpgradeManager.isNewer(v3, v3));
    assertFalse(UpgradeManager.isNewer(v1, v2));
    assertTrue(UpgradeManager.isNewer(v6, v5));
    assertTrue(UpgradeManager.isNewer("1.10dev", "1.7"));
    assertFalse(UpgradeManager.isNewer("1.7", "1.7"));
  }
}
