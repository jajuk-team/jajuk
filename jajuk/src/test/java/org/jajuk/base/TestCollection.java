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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 */
public class TestCollection extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.base.Collection#startDocument()}.
   */
  public final void testStartDocument() {
    Collection coll = Collection.getInstance();
    assertNotNull(coll);

    // just call it, this is part of the SAX interfaces
    coll.startDocument();
  }

  /**
   * Test method for {@link org.jajuk.base.Collection#endDocument()}.
   */
  public final void testEndDocument() {
    Collection coll = Collection.getInstance();
    assertNotNull(coll);

    // just call it, this is part of the SAX interfaces
    coll.endDocument();
  }

  /**
   * Test method for {@link org.jajuk.base.Collection#getInstance()}.
   */
  public final void testGetInstance() {
    Collection coll = Collection.getInstance();
    assertNotNull(coll);
  }

  /**
   * Test method for {@link org.jajuk.base.Collection#commit(java.io.File)}.
   * 
   * @throws Exception
   */
  public final void testCommit() throws Exception {
    StartupCollectionService.registerItemManagers();

    Collection coll = Collection.getInstance();
    assertNotNull(coll);

    java.io.File file = java.io.File.createTempFile("testcoll", ".xml");

    // delete the file before writing the collection
    assertTrue(file.delete());

    // commit without any item
    Collection.commit(file);

    // now it should exist and have some content
    assertTrue(file.exists());
    String str = FileUtils.readFileToString(file);
    assertTrue(str, StringUtils.isNotBlank(str));
    assertTrue(str, str.contains("<" + Const.XML_COLLECTION));

    // now with some content
    DeviceManager.getInstance().registerDevice("testdevice", Device.Type.FILES_CD,
        System.getProperty("java.io.tmpdir"));
    GenreManager.getInstance().registerGenre("cooldown");
    {
      Genre genre = GenreManager.getInstance().registerGenre("name");
      Album album = AlbumManager.getInstance().registerAlbum("name", 23);
      album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
      // this test

      Artist artist = ArtistManager.getInstance().registerArtist("name");
      Year year = YearManager.getInstance().registerYear("2000");
      YearManager.getInstance().registerYear("2000");

      Type type = TypeManager.getInstance().registerType("MP3", "mp3", null, null);

      TrackManager.getInstance()
          .registerTrack("name5", album, genre, artist, 120, year, 1, type, 1);
    }
    YearManager.getInstance().registerYear("1900");
    Device device = new Device("6", System.getProperty("java.io.tmpdir"));
    device.setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir = JUnitHelpers.getDirectory();
    PlaylistManager.getInstance().registerPlaylistFile("4", "plf", dir);
    ArtistManager.getInstance().registerArtist("testartist");
    AlbumManager.getInstance().registerAlbum("album2", "artist1", 0);

    device = DeviceManager.getInstance().registerDevice("7", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    DirectoryManager.getInstance().registerDirectory(device);
    device = new Device("6", System.getProperty("java.io.tmpdir"));
    device.setUrl(System.getProperty("java.io.tmpdir"));
    FileManager.getInstance().registerFile("thisfile.mp3", dir,
        TrackManager.getInstance().getTracks().get(0), 120, 100);

    // delete the file before writing the collection
    assertTrue(file.delete());

    // commit without any item
    Collection.commit(file);

    // now it should exist and have some content
    assertTrue(file.exists());
    str = FileUtils.readFileToString(file);
    assertTrue(str, StringUtils.isNotBlank(str));
    assertTrue(str, str.contains("<" + Const.XML_COLLECTION));
    // it should also contain the content that we added
    assertTrue(str, str.contains("testdevice"));
    assertTrue(str, str.contains("cooldown"));

    // add test for strange error in this testcase on hudson
    assertNotNull(UtilString.getAdditionDateFormatter());

    // also test loading here
    Collection.load(file);

    // TODO: loading needs more testing and verification of results after
    // loading...
  }

  /**
   * Test method for {@link org.jajuk.base.Collection#load(java.io.File)}.
   */
  public final void testLoad() {
    // tested above
  }

  public final void testLoadNotExists() throws Exception {
    try {
      Collection.load(new java.io.File("Notexistingfile"));
      fail("Should throw an exception here.");
    } catch (JajukException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("Notexistingfile"));
    }
  }

  /**
   * Test method for {@link org.jajuk.base.Collection#cleanupLogical()}.
   */
  public final void testCleanupLogical() {
    Collection.cleanupLogical();
  }

  /**
   * Test method for {@link org.jajuk.base.Collection#clearCollection()}.
   */
  public final void testClearCollection() {
    Collection.clearCollection();
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Collection#warning(org.xml.sax.SAXParseException)}.
   */
  public final void testWarningSAXParseException() {
    Collection coll = Collection.getInstance();
    try {
      coll.warning(new SAXParseException("Testexception", null));
      fail("Should throw exception here...");
    } catch (SAXException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("Testexception"));
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Collection#error(org.xml.sax.SAXParseException)}.
   * 
   * @throws Exception
   */
  public final void testErrorSAXParseException() throws Exception {
    Collection coll = Collection.getInstance();
    try {
      coll.error(new SAXParseException("Testexception", null));
      fail("Should throw exception here...");
    } catch (SAXException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("Testexception"));
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Collection#fatalError(org.xml.sax.SAXParseException)}
   * .
   * 
   * @throws Exception
   */
  public final void testFatalErrorSAXParseException() throws Exception {
    Collection coll = Collection.getInstance();
    try {
      coll.fatalError(new SAXParseException("Testexception", null));
      fail("Should throw exception here...");
    } catch (SAXException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("Testexception"));
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Collection#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)}
   * .
   */
  public final void testStartElementStringStringStringAttributes() {
    // tested as part of commit/load
  }

  /**
   * Test method for {@link org.jajuk.base.Collection#getHmWrongRightFileID()}.
   */
  public final void testGetHmWrongRightFileID() {
    Collection coll = Collection.getInstance();
    assertNotNull(coll.getHmWrongRightFileID());
  }

  /**
   * Test method for {@link org.jajuk.base.Collection#getWrongRightAlbumIDs()}.
   */
  public final void testGetWrongRightAlbumIDs() {
    Collection coll = Collection.getInstance();
    assertNotNull(coll.getWrongRightAlbumIDs());
  }
}
