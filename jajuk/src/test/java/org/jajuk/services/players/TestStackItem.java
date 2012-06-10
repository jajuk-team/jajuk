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
 *  
 */
package org.jajuk.services.players;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.File;
import org.jajuk.util.error.JajukException;

/**
 * .
 */
public class TestStackItem extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#hashCode()}.
   *
   * @throws Exception the exception
   */
  public void testHashCode() throws Exception {
    File file = JUnitHelpers.getFile("file1", true);
    StackItem item1 = new StackItem(file);
    StackItem item2 = new StackItem(file);

    JUnitHelpers.HashCodeTest(item1, item2);
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.players.StackItem#StackItem(org.jajuk.base.File)}
   * .
   */
  public void testStackItemFile() throws Exception {
    new StackItem(JUnitHelpers.getFile("file1", true));

    // test null input
    try {
      new StackItem(null);
      fail("Should throw exception here.");
    } catch (JajukException e) {
      assertEquals(0, e.getCode());
    }
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.players.StackItem#StackItem(org.jajuk.base.File, boolean)}
   * .
   */
  public void testStackItemFileBoolean() throws Exception {
    new StackItem(JUnitHelpers.getFile("file2", true), true);

    // test null input
    try {
      new StackItem(null, true);
      fail("Should throw exception here.");
    } catch (JajukException e) {
      assertEquals(0, e.getCode());
    }
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.players.StackItem#StackItem(org.jajuk.base.File, boolean, boolean)}
   * .
   */
  public void testStackItemFileBooleanBoolean() throws Exception {
    new StackItem(JUnitHelpers.getFile("file2", true), true, true);

    // test null input
    try {
      new StackItem(null, true, true);
      fail("Should throw exception here.");
    } catch (JajukException e) {
      assertEquals(0, e.getCode());
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#isRepeat()}.
   *
   * @throws Exception the exception
   */
  public void testIsAndSetRepeat() throws Exception {
    StackItem item = new StackItem(JUnitHelpers.getFile("file1", true));
    assertFalse(item.isRepeat());
    item.setRepeat(true);
    assertTrue(item.isRepeat());
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#JUnitHelpers.getFile()}.
   *
   * @throws Exception the exception
   */
  public void testgetFile() throws Exception {
    StackItem item = new StackItem(JUnitHelpers.getFile("file1", true));
    assertNotNull(item.getFile());
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#isUserLaunch()}
   * .
   *
   * @throws Exception the exception
   */
  public void testIsAndSetUserLaunch() throws Exception {
    StackItem item = new StackItem(JUnitHelpers.getFile("file1", true));
    assertFalse(item.isUserLaunch());
    item.setUserLaunch(true);
    assertTrue(item.isUserLaunch());
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#isPlanned()}.
   *
   * @throws Exception the exception
   */
  public void testIsAndSetPlanned() throws Exception {
    StackItem item = new StackItem(JUnitHelpers.getFile("file1", true));
    assertFalse(item.isPlanned());
    item.setPlanned(true);
    assertTrue(item.isPlanned());
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#clone()}.
   *
   * @throws Exception the exception
   */
  public void testClone() throws Exception {
    StackItem item = new StackItem(JUnitHelpers.getFile("file1", true));
    JUnitHelpers.CloneTest(item);
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.players.StackItem#equals(java.lang.Object)}.
   */
  public void testEqualsObject() throws Exception {
    File file = JUnitHelpers.getFile("file1", true);
    StackItem item1 = new StackItem(file);
    StackItem item2 = new StackItem(file);
    StackItem item3 = new StackItem(JUnitHelpers.getFile("file2", true));
    JUnitHelpers.EqualsTest(item1, item2, item3);
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#toString()}.
   *
   * @throws Exception the exception
   */
  public void testToString() throws Exception {
    StackItem item = new StackItem(JUnitHelpers.getFile("file1", true));
    JUnitHelpers.ToStringTest(item);
  }

}
