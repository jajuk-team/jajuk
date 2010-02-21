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
package org.jajuk.base;

import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.JUnitHelpers.MockPlayer;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * TODO: some more coverage is possible by enhancing the tests accordingly.
 */
public class TestDirectory extends JajukTestCase {

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // reset some conf-options
    Conf.setProperty(Const.CONF_OPTIONS_HIDE_UNMOUNTED, "false");

    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDesc()}.
   */

  public void testGetDesc() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    assertTrue(dir.toString(), StringUtils.isNotBlank(dir.getDesc()));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getLabel()}.
   */

  public void testGetLabel() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    assertEquals(dir.toString(), Const.XML_DIRECTORY, dir.getLabel());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getHumanValue(java.lang.String)}.
   */

  public void testGetHumanValue() {
    Device dev = DeviceManager.getInstance().registerDevice("2", Device.TYPE_DIRECTORY, "test");
    assertNotNull(dev);

    Directory dir = new Directory("1", "dir", null, dev);
    assertEquals(dir.toString(), "", dir.getHumanValue(Const.XML_DIRECTORY_PARENT));
    assertEquals(dir.toString(), "2", dir.getHumanValue(Const.XML_DEVICE));
    assertTrue(dir.toString(), StringUtils.isNotBlank(dir.getHumanValue(Const.XML_NAME)));
    assertEquals(dir.toString(), "", dir.getHumanValue("notexisting"));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getIconRepresentation()}.
   */

  public void testGetIconRepresentation() {
    StartupCollectionService.registerItemManagers();

    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    assertNotNull(dir.getIconRepresentation());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#Directory(java.lang.String, java.lang.String, org.jajuk.base.Directory, org.jajuk.base.Device)}.
   */

  public void testDirectory() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    assertNotNull(dir);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#toString()}.
   */

  public void testToString() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    JUnitHelpers.ToStringTest(dir);
  }

  public void testToStringParent() {
    Directory parent = new Directory("2", "dir2", null, new Device("2", "test"));
    Directory dir = new Directory("1", "dir", parent, new Device("2", "test"));
    JUnitHelpers.ToStringTest(dir);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getAbsolutePath()}.
   */

  public void testGetAbsolutePath() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    dir.getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    assertTrue(dir.toString(), StringUtils.isNotBlank(dir.getAbsolutePath()));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDevice()}.
   */

  public void testGetDevice() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    assertNotNull(dir.getDevice());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getParentDirectory()}.
   */

  public void testGetParentDirectory() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    assertNull(dir.getParentDirectory());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDirectories()}.
   */

  public void testGetDirectories() {
    Device dev = DeviceManager.getInstance().registerDevice("2", "test", Device.TYPE_DIRECTORY,
        "/tmp");
    // This is a root directory = the device root itself, its name should be ""
    Directory dir = new Directory("1", "", null, dev);
    Set<Directory> dirs = dir.getDirectories();

    // no dirs without registered directories
    assertEquals(0, dirs.size());

    DirectoryManager.getInstance().registerDirectory("sub1", dir, dev);
    DirectoryManager.getInstance().registerDirectory("sub2", dir, dev);
    DirectoryManager.getInstance().registerDirectory("sub3", dir, dev);

    dirs = dir.getDirectories();
    assertEquals(3, dirs.size());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFiles()}.
   * @throws Exception 
   */

  public void testGetFiles() throws Exception {
    Device dev = new Device("2", "test");
    dev.setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir = new Directory("1", "dir", null, dev);

    Set<File> files = dir.getFiles();

    // no files are available currently
    assertEquals(0, files.size());

    getFileInDir(3, dir);
    getFileInDir(4, dir);

    files = dir.getFiles();
    assertEquals(2, dir.getFiles().size());
  }

  @SuppressWarnings("unchecked")
  public static org.jajuk.base.File getFileInDir(int i, Directory dir) throws Exception {
    Genre genre = new Genre(Integer.valueOf(i).toString(), "name");
    Album album = new Album(Integer.valueOf(i).toString(), "name", 23);
    album.setProperty(Const.XML_ALBUM_COVER, Const.COVER_NONE); // don't read covers for
    // this test

    Artist artist = new Artist(Integer.valueOf(i).toString(), "name");
    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

    Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", cl, null);
    Track track = new Track(Integer.valueOf(i).toString(), "name", album, genre, artist, 120, year,
        1, type, 1);

    return FileManager.getInstance().registerFile(Integer.valueOf(i).toString(), "test.tst", dir,
        track, 120, 70);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getPlaylistFiles()}.
   */

  public void testGetPlaylistFiles() {
    Device dev = new Device("2", "test");
    dev.setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir = new Directory("1", "dir", null, dev);

    Set<Playlist> files = dir.getPlaylistFiles();

    // no files are available currently
    assertEquals(0, files.size());

    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
            + "testfile1"), dir);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
            + "testfile2"), dir);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
            + "testfile3"), dir);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
            + "testfile4"), dir);

    files = dir.getPlaylistFiles();
    assertEquals(4, files.size());
  }

  public void testGetPlaylistRecursively() {
    PlaylistManager.getInstance().clear();

    Device dev = new Device("2", "test");
    dev.setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir = new Directory("1", "dir", null, dev);

    List<Playlist> files = dir.getPlaylistsRecursively();

    // no files are available currently
    assertEquals(0, files.size());

    Directory dir1 = DirectoryManager.getInstance().registerDirectory("sub1", dir, dev);
    Directory dir2 = DirectoryManager.getInstance().registerDirectory("sub2", dir, dev);
    Directory dir3 = DirectoryManager.getInstance().registerDirectory("sub3", dir, dev);

    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
            + "testfile1"), dir1);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
            + "testfile2"), dir2);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
            + "testfile3"), dir2);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
            + "testfile4"), dir3);

    files = dir.getPlaylistsRecursively();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFilesFromFile(org.jajuk.base.File)}.
   * @throws Exception 
   */

  public void testGetFilesFromFile() throws Exception {
    Device dev = new Device("2", "test");
    dev.setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir = new Directory("1", "dir", null, dev);
    assertNull(dir.getFilesFromFile(null));

    getFileInDir(3, dir);
    getFileInDir(4, dir);
    File file = getFileInDir(5, dir);
    getFileInDir(6, dir);

    List<File> list = dir.getFilesFromFile(file);
    assertTrue("Size: " + list.size(), list.size() > 0);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFilesRecursively()}.
   * @throws Exception 
   */

  public void testGetFilesRecursively() throws Exception {
    FileManager.getInstance().clear();

    Device dev = new Device("3", "test2");
    // This is a root directory = the device root itself, its name should be ""
    Directory dir = new Directory("99", "", null, dev);
    List<File> files = dir.getFilesRecursively();

    // no files are available currently
    assertEquals(0, files.size());

    Directory dir1 = DirectoryManager.getInstance().registerDirectory("sub1", dir, dev);
    Directory dir2 = DirectoryManager.getInstance().registerDirectory("sub2", dir, dev);
    Directory dir3 = DirectoryManager.getInstance().registerDirectory("sub3", dir, dev);

    getFileInDir(3, dir1);
    getFileInDir(4, dir2);
    getFileInDir(5, dir2);
    getFileInDir(6, dir3);

    files = dir.getFilesRecursively();
    assertEquals(4, files.size());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#scan(boolean, org.jajuk.ui.helpers.RefreshReporter)}.
   */

  public void testScan() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));

    // this scan will not do much because there are no files in this dir
    dir.scan(true, null);
  }

  public void testScanActual() throws Exception {
    StartupCollectionService.registerItemManagers();
    StartupCollectionService.registerTypes();

    // create temp file
    Device dev = DeviceManager.getInstance().registerDevice("test1", Device.TYPE_DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    Directory dir = new Directory("1", "testScan", null, dev);

    new java.io.File(dev.getUrl()).mkdirs();
    FileUtils.writeStringToFile(new java.io.File(dev.getUrl() + java.io.File.separator + "testScan"
        + java.io.File.separator + "test1.mp3"), "teststring");

    dir.scan(true, null);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#reset()}.
   */

  public void testReset() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    dir.reset();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getRelativePath()}.
   */

  public void testGetRelativePath() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    assertNotNull(dir.getRelativePath());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFio()}.
   */

  public void testGetFio() {
    Directory dir = new Directory("1", "dir", null, new Device("2", "test"));
    assertNotNull(dir.getFio());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#compareTo(org.jajuk.base.Directory)}.
   */

  public void testCompareTo() {
    Directory dir1 = new Directory("1", "dir", null, new Device("2", "test"));
    dir1.getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir2 = new Directory("1", "dir", null, new Device("2", "test"));
    dir2.getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir3a = new Directory("3", "dir", null, new Device("2", "test3"));
    dir3a.getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir3b = new Directory("2", "dir", null, new Device("2", "test"));
    dir3b.getDevice().setUrl(System.getProperty("java.io.tmpdir") + java.io.File.separator + "1");

    JUnitHelpers.CompareToTest(dir1, dir2, dir3a);
    JUnitHelpers.CompareToTest(dir1, dir2, dir3b);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#shouldBeHidden()}.
   * @throws Exception 
   */

  public void testShouldBeHidden() throws Exception {
    Directory dir = new Directory("1", "dir1", null, new Device("2", "test"));
    // not mounted by default
    assertFalse(dir.getDevice().isMounted());

    // false because option is not set
    assertFalse(dir.shouldBeHidden());

    Conf.setProperty(Const.CONF_OPTIONS_HIDE_UNMOUNTED, "true");

    // now true because option to hide unmounted is set
    assertTrue(dir.shouldBeHidden());

    dir.getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    dir.getDevice().mount(true);

    // now false because device is mounted now
    assertFalse(dir.shouldBeHidden());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#setName(java.lang.String)}.
   */

  public void testSetName() {
    Directory dir = new Directory("1", "dir1", null, new Device("2", "test"));

    assertEquals("dir1", dir.getName());

    dir.setName("newname");

    assertEquals("newname", dir.getName());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#refresh(boolean, org.jajuk.ui.helpers.RefreshReporter)}.
   * @throws Exception 
   */

  public void testRefresh() throws Exception {
    Directory dir = new Directory("1", "dir1", null, new Device("2", "test"));
    dir.refresh(true, null);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#manualRefresh(boolean, boolean)}.
   */

  public void testManualRefresh() {
    Directory dir = new Directory("1", "dir1", null, new Device("2", "test"));
    dir.manualRefresh(false, false);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#cleanRemovedFiles()}.
   */

  public void testCleanRemovedFiles() {
    Directory dir = new Directory("1", "dir1", null, new Device("2", "test"));
    dir.cleanRemovedFiles();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#isChildOf(org.jajuk.base.Directory)}.
   */

  public void testIsChildOf() {
    Device dev = new Device("2", "test");
    Directory dir = new Directory("1", "dir1", null, dev);
    assertFalse(dir.isChildOf(new Directory("2", "dir2", null, dev)));

    Directory dir2 = new Directory("3", "dir3", dir, dev);

    assertTrue(dir2.isChildOf(dir));
  }

}
