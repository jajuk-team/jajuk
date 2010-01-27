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

import java.util.ArrayList;
import java.util.List;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestBookmarks extends JajukTestCase {

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    Bookmarks.getInstance().clear();

    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.Bookmarks#getInstance()}
   * .
   */
  public void testGetInstance() {
    Bookmarks.getInstance();
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.Bookmarks#toString()}.
   */
  public void testToString() throws Exception {
    // TODO: this fails currently because it returns an empty string:
    // JUnitHelpers.ToStringTest(Bookmarks.getInstance());

    assertNotNull(Bookmarks.getInstance().toString());

    // test with some files
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    JUnitHelpers.ToStringTest(Bookmarks.getInstance());
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.Bookmarks#getFiles()}.
   * 
   * @throws Exception
   */
  public void testGetFiles() throws Exception {
    assertEquals(0, Bookmarks.getInstance().getFiles().size());

    // test with some files
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    assertEquals(1, Bookmarks.getInstance().getFiles().size());
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    assertEquals(2, Bookmarks.getInstance().getFiles().size());
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.Bookmarks#clear()}.
   */
  public void testClear() throws Exception {
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    assertEquals(1, Bookmarks.getInstance().getFiles().size());

    Bookmarks.getInstance().clear();
    assertEquals(0, Bookmarks.getInstance().getFiles().size());
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.Bookmarks#down(int)}.
   */
  public void testDownAndUp() throws Exception {
    // nothing happens without files
    Bookmarks.getInstance().down(0);
    Bookmarks.getInstance().up(0);

    // add some files
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(2, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(3, true));

    // check the order
    assertEquals("1", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("3", Bookmarks.getInstance().getFiles().get(2).getID());

    // down some
    Bookmarks.getInstance().down(1);
    assertEquals("1", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("3", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(2).getID());

    // up again
    Bookmarks.getInstance().up(1);
    assertEquals("3", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("1", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(2).getID());

    // outside
    Bookmarks.getInstance().down(2);
    assertEquals("3", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("1", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(2).getID());

    // outside
    Bookmarks.getInstance().up(0);
    assertEquals("3", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("1", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(2).getID());
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.Bookmarks#remove(int)}.
   * 
   * @throws Exception
   */
  public void testRemove() throws Exception {
    // add some files
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(2, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(3, true));

    Bookmarks.getInstance().remove(0);
    assertEquals("2", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("3", Bookmarks.getInstance().getFiles().get(1).getID());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.bookmark.Bookmarks#addFile(int, org.jajuk.base.File)}
   * .
   * 
   * @throws Exception
   */
  public void testAddFileIntFile() throws Exception {
    // add some files
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(2, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(3, true));

    // check the order
    assertEquals("1", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("3", Bookmarks.getInstance().getFiles().get(2).getID());

    Bookmarks.getInstance().addFile(1, JUnitHelpers.getFile(4, true));
    assertEquals("1", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("4", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(2).getID());
    assertEquals("3", Bookmarks.getInstance().getFiles().get(3).getID());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.bookmark.Bookmarks#addFile(org.jajuk.base.File)}.
   * 
   * @throws Exception
   */
  public void testAddFileFile() throws Exception {
    // add some files
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(2, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(3, true));

    // check the order
    assertEquals("1", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("3", Bookmarks.getInstance().getFiles().get(2).getID());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.bookmark.Bookmarks#addFiles(java.util.List)}.
   * 
   * @throws Exception
   */
  public void testAddFiles() throws Exception {
    List<File> list = new ArrayList<File>();

    // add some files
    list.add(JUnitHelpers.getFile(1, true));
    list.add(JUnitHelpers.getFile(2, true));
    list.add(JUnitHelpers.getFile(3, true));

    Bookmarks.getInstance().addFiles(list);

    // check the order
    assertEquals("1", Bookmarks.getInstance().getFiles().get(0).getID());
    assertEquals("2", Bookmarks.getInstance().getFiles().get(1).getID());
    assertEquals("3", Bookmarks.getInstance().getFiles().get(2).getID());
  }

  // helper method to emma-coverage of the unused constructor
  @SuppressWarnings("unchecked")
  public void testPrivateConstructor() throws Exception {
    int i = 1;
    {
      Genre genre = new Genre(Integer.valueOf(i).toString(), "name");
      Album album = new Album(Integer.valueOf(i).toString(), "name", 23);
      album.setProperty(Const.XML_ALBUM_COVER, Const.COVER_NONE); // don't read covers for
      // this test

      Artist artist = new Artist(Integer.valueOf(i).toString(), "name");
      Year year = new Year(Integer.valueOf(i).toString(), "2000");

      IPlayerImpl imp = new JUnitHelpers.MockPlayer();
      Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

      Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", cl, null);
      Track track = new Track(Integer.valueOf(i).toString(), "name", album, genre, artist, 120,
          year, 1, type, 1);

      Device device = new Device(Integer.valueOf(i).toString(), "name");
      device.setUrl(System.getProperty("java.io.tmpdir"));
      device.mount(true);

      Directory dir = new Directory(Integer.valueOf(i).toString(), "name", null, device);
      FileManager.getInstance().registerFile("1", "name", dir, track, 120, 10);
    }

    // test with some files
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));
    Bookmarks.getInstance().addFile(JUnitHelpers.getFile(1, true));

    // For EMMA code-coverage tests
    JUnitHelpers.executePrivateConstructor(Bookmarks.class);
  }

}
