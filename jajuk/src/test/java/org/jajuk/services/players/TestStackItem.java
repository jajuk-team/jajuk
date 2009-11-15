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
 *  $Revision: 3132 $
 */
package org.jajuk.services.players;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.services.players.TestQueueModel.MockPlayer;
import org.jajuk.util.Const;
import org.jajuk.util.error.JajukException;

/**
 * 
 */
public class TestStackItem extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#hashCode()}.
   */
  public void testHashCode() throws Exception {
    File file = getFile(1);
    StackItem item1 = new StackItem(file);
    StackItem item2 = new StackItem(file);

    JUnitHelpers.HashCodeTest(item1, item2);
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.StackItem#StackItem(org.jajuk.base.File)}
   * .
   */
  public void testStackItemFile() throws Exception {
    new StackItem(getFile(1));

    // test null input
    try {
      new StackItem(null);
      fail("Should throw exception here.");
    } catch (JajukException e) {
      assertEquals(0, e.getCode());
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.StackItem#StackItem(org.jajuk.base.File, boolean)}
   * .
   */
  public void testStackItemFileBoolean() throws Exception {
    new StackItem(getFile(2), true);

    // test null input
    try {
      new StackItem(null, true);
      fail("Should throw exception here.");
    } catch (JajukException e) {
      assertEquals(0, e.getCode());
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.StackItem#StackItem(org.jajuk.base.File, boolean, boolean)}
   * .
   */
  public void testStackItemFileBooleanBoolean() throws Exception {
    new StackItem(getFile(2), true, true);

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
   */
  public void testIsAndSetRepeat() throws Exception {
    StackItem item = new StackItem(getFile(1));
    assertFalse(item.isRepeat());
    item.setRepeat(true);
    assertTrue(item.isRepeat());
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#getFile()}.
   */
  public void testGetFile() throws Exception {
    StackItem item = new StackItem(getFile(1));
    assertNotNull(item.getFile());
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#isUserLaunch()}
   * .
   */
  public void testIsAndSetUserLaunch() throws Exception {
    StackItem item = new StackItem(getFile(1));
    assertFalse(item.isUserLaunch());
    item.setUserLaunch(true);
    assertTrue(item.isUserLaunch());
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#isPlanned()}.
   */
  public void testIsAndSetPlanned() throws Exception {
    StackItem item = new StackItem(getFile(1));
    assertFalse(item.isPlanned());
    item.setPlanned(true);
    assertTrue(item.isPlanned());
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#clone()}.
   */
  public void testClone() throws Exception {
    StackItem item = new StackItem(getFile(1));
    JUnitHelpers.CloneTest(item);
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.StackItem#equals(java.lang.Object)}.
   */
  public void testEqualsObject() throws Exception {
    File file = getFile(1);
    StackItem item1 = new StackItem(file);
    StackItem item2 = new StackItem(file);
    StackItem item3 = new StackItem(getFile(2));
    JUnitHelpers.EqualsTest(item1, item2, item3);
  }

  /**
   * Test method for {@link org.jajuk.services.players.StackItem#toString()}.
   */
  public void testToString() throws Exception {
    StackItem item = new StackItem(getFile(1));
    JUnitHelpers.ToStringTest(item);
  }

  @SuppressWarnings("unchecked")
  private File getFile(int i) throws Exception {
    Style style = new Style(Integer.valueOf(i).toString(), "name");
    Album album = new Album(Integer.valueOf(i).toString(), "name", "artis", 23);
    album.setProperty(Const.XML_ALBUM_COVER, "none"); // don't read covers for
    // this test

    Author author = new Author(Integer.valueOf(i).toString(), "name");
    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

    Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", cl, null);
    Track track = new Track(Integer.valueOf(i).toString(), "name", album, style, author, 120, year,
        1, type, 1);

    Device device = new Device(Integer.valueOf(i).toString(), "name");
    device.setUrl(System.getProperty("java.io.tmpdir"));
    device.mount(true);

    Directory dir = new Directory(Integer.valueOf(i).toString(), "name", null, device);

    return new org.jajuk.base.File(Integer.valueOf(i).toString(), "test.tst", dir, track, 120, 70);
  }
}
