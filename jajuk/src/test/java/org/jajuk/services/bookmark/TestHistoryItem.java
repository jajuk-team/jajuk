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
package org.jajuk.services.bookmark;

import org.jajuk.JUnitHelpers;
import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.players.TestQueueModel.MockPlayer;
import org.jajuk.util.Const;

import org.jajuk.JajukTestCase;

/**
 * 
 */
public class TestHistoryItem extends JajukTestCase {

  /**
   * Test method for
   * {@link org.jajuk.services.bookmark.HistoryItem#HistoryItem(java.lang.String, long)}.
   */

  public final void testHistoryItem() {
    new HistoryItem("1", 123);
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.HistoryItem#getDate()}.
   */

  public final void testGetAndSetDate() {
    long date = System.currentTimeMillis();
    HistoryItem item = new HistoryItem("1", date);

    assertEquals(date, item.getDate());

    item.setDate(123);
    assertEquals(123, item.getDate());
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.HistoryItem#getFileId()}.
   */

  public final void testGetAndSetFileId() {
    long date = System.currentTimeMillis();
    HistoryItem item = new HistoryItem("1", date);

    assertEquals("1", item.getFileId());

    item.setFileId("2");
    assertEquals("2", item.getFileId());

  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.HistoryItem#toString()}.
   */

  public final void testToStringNull() {
    long date = System.currentTimeMillis();
    HistoryItem item = new HistoryItem("1", date);

    JUnitHelpers.ToStringTest(item);
  }

  @SuppressWarnings("unchecked")
  public final void testToStringFile() {
    File file;
    {
      Style style = new Style("3", "stylename");
      Album album = new Album("3", "albumname", "artist", 23);
      album.setProperty(Const.XML_ALBUM_COVER, "none"); // don't read covers for
      // this test

      Author author = new Author("3", "authorname");
      Year year = new Year("3", "2000");

      IPlayerImpl imp = new MockPlayer();
      Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

      Type type = new Type("3", "typename", "mp3", cl, null);
      Track track = new Track("3", "trackname", album, style, author, 120, year, 1, type, 1);

      Device device = new Device("3", "devicename");
      device.setUrl(System.getProperty("java.io.tmpdir"));
      // device.mount(true);

      Directory dir = new Directory("3", "dirname", null, device);

      file = new org.jajuk.base.File("3", "test.tst", dir, track, 120, 70);
      FileManager.getInstance().registerFile("3", "test.tst", dir, track, 120, 70);
    }

    long date = System.currentTimeMillis();
    HistoryItem item = new HistoryItem(file.getID(), date);

    // verify toString in general
    JUnitHelpers.ToStringTest(item);

    // verify that the necessary information is contained
    assertTrue(item.toString(), item.toString().contains("trackname"));
    assertTrue(item.toString(), item.toString().contains("authorname"));
  }
}
