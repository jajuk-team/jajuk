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
package org.jajuk.services.bookmark;

import org.jajuk.ConstTest;
import org.jajuk.TestHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestHistoryItem extends JajukTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.HistoryItem#HistoryItem(java.lang.String, long)}
   * .
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
   * Test method for {@link org.jajuk.services.bookmark.HistoryItem#getFileId()}
   * .
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
    TestHelpers.ToStringTest(item);
  }

  /**
   * Test to string file.
   * 
   */
  public final void testToStringFile() {
    File file;
    Genre genre = TestHelpers.getGenre("genrename");
    Album album = TestHelpers.getAlbum("myalbum", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test
    Artist artist = TestHelpers.getArtist("artistname");
    Year year = TestHelpers.getYear(2000);
    Type type = TestHelpers.getType();
    Track track = TrackManager.getInstance().registerTrack("trackname", album, genre, artist, 120,
        year, 1, type, 1);
    Device device = TestHelpers.getDevice("devicename", Device.Type.DIRECTORY,
        ConstTest.DEVICES_BASE_PATH + "/device1");
    Directory dir = DirectoryManager.getInstance().registerDirectory(device);
    file = FileManager.getInstance().registerFile("test.tst", dir, track, 120, 70);
    long date = System.currentTimeMillis();
    HistoryItem item = new HistoryItem(file.getID(), date);
    // verify toString in general
    TestHelpers.ToStringTest(item);
    // verify that the necessary information is contained
    assertTrue(item.toString(), item.toString().contains("trackname"));
    assertTrue(item.toString(), item.toString().contains("artistname"));
  }
}
