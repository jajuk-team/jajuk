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
 *  $Revision$
 */
package org.jajuk.base;

import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JUnitHelpers.MockPlayer;
import org.jajuk.JajukTestCase;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * TODO: some more coverage is possible by enhancing the tests accordingly.
 */
public class TestDirectory extends JajukTestCase {

  /** Sample device named "device1" and located at /tmp/device1 *. */
  Device device1;
  
  /** Sample top dir named "topdir1" and located at /tmp/device1/topdir1 *. */
  Directory topdir1;
  
  /** Sample dir named "dir1" and located at /tmp/device1/topdir1/dir1 *. */
  Directory dir1;
  
  /** Sample dir named "dir2" and located at /tmp/device1/topdir1/dir2 *. */
  Directory dir2;

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

    // Create generic items
    String pathDevice1 = System.getProperty("java.io.tmpdir") + "/device1";
    // Cleanup any existing directory and create again the directory
    new java.io.File(pathDevice1).delete();
    new java.io.File(pathDevice1).mkdirs();
    // create 2 directories to avoid getting a wanring dialog that device is void
    new java.io.File(pathDevice1 + "/dir1").mkdirs();
    new java.io.File(pathDevice1 + "/dir2").mkdirs();

    device1 = DeviceManager.getInstance().registerDevice("device1", Device.Type.DIRECTORY,
        pathDevice1);
    topdir1 = DirectoryManager.getInstance().registerDirectory(device1);
    dir1 = DirectoryManager.getInstance().registerDirectory("dir1", topdir1, device1);
    dir2 = DirectoryManager.getInstance().registerDirectory("dir2", topdir1, device1);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDesc()}.
   */

  public void testGetDesc() {
    Directory dir = dir1;
    assertTrue(dir.toString(), StringUtils.isNotBlank(dir.getDesc()));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getXMLTag()}.
   */

  public void testGetLabel() {
    Directory dir = dir1;
    assertEquals(dir.toString(), Const.XML_DIRECTORY, dir.getXMLTag());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getHumanValue(java.lang.String)}.
   */

  public void testGetHumanValue() {
    Device dev = device1;
    assertNotNull(dev);

    Directory dir = dir1;
    assertEquals(dir.toString(), dir.getParentDirectory().getFio(),
        new java.io.File(dir.getHumanValue(Const.XML_DIRECTORY_PARENT)));
    assertEquals(dir.toString(), "device1", dir.getHumanValue(Const.XML_DEVICE));
    assertTrue(dir.toString(), StringUtils.isNotBlank(dir.getHumanValue(Const.XML_NAME)));
    assertEquals(dir.toString(), "", dir.getHumanValue("notexisting"));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getIconRepresentation()}.
   */

  public void testGetIconRepresentation() {
    StartupCollectionService.registerItemManagers();

    Directory dir = dir1;
    assertNotNull(dir.getIconRepresentation());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#Directory(java.lang.String, java.lang.String, org.jajuk.base.Directory, org.jajuk.base.Device)}.
   */

  public void testDirectory() {
    Directory dir = dir1;
    assertNotNull(dir);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#toString()}.
   */

  public void testToString() {
    Directory dir = dir1;
    JUnitHelpers.ToStringTest(dir);
  }

  /**
   * Test to string parent.
   * DOCUMENT_ME
   */
  public void testToStringParent() {
    Directory dir = dir1;
    JUnitHelpers.ToStringTest(dir);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getAbsolutePath()}.
   */

  public void testGetAbsolutePath() {
    Directory dir = dir1;
    assertTrue(dir.toString(), StringUtils.isNotBlank(dir.getAbsolutePath()));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDevice()}.
   */

  public void testGetDevice() {
    Directory dir = dir1;
    assertNotNull(dir.getDevice());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getParentDirectory()}.
   */

  public void testGetParentDirectory() {
    Directory dir = topdir1;
    assertNull(dir.getParentDirectory());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDirectories()}.
   */

  public void testGetDirectories() {
    Directory dir = dir1;
    Set<Directory> dirs = dir.getDirectories();

    // no dirs without registered directories
    assertEquals(0, dirs.size());

    DirectoryManager.getInstance().registerDirectory("sub1", dir, dir.getDevice());
    DirectoryManager.getInstance().registerDirectory("sub2", dir, dir.getDevice());
    DirectoryManager.getInstance().registerDirectory("sub3", dir, dir.getDevice());

    dirs = dir.getDirectories();
    assertEquals(3, dirs.size());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFiles()}.
   *
   * @throws Exception the exception
   */

  public void testGetFiles() throws Exception {
    Directory dir = dir1;

    Set<File> files = dir.getFiles();

    // no files are available currently
    assertEquals(0, files.size());

    getFileInDir(3, dir);
    getFileInDir(4, dir);

    files = dir.getFiles();
    assertEquals(2, dir.getFiles().size());
  }

  /**
   * Gets the file in dir.
   *
   * @param i DOCUMENT_ME
   * @param dir DOCUMENT_ME
   * @return the file in dir
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static org.jajuk.base.File getFileInDir(int i, Directory dir) throws Exception {
    Genre genre = JUnitHelpers.getGenre("name");
    Album album = JUnitHelpers.getAlbum("myalbum", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test

    Artist artist = JUnitHelpers.getArtist("name");
    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

    Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", cl, null);
    Track track = new Track(Integer.valueOf(i).toString(), "name", album, genre, artist, 120, year,
        1, type, 1);

    return FileManager.getInstance().registerFile("test_" + i + ".tst", dir, track, 120, 70);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getPlaylistFiles()}.
   */

  public void testGetPlaylistFiles() {
    Directory dir = dir1;

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

  /**
   * Test get playlist recursively.
   * DOCUMENT_ME
   */
  public void testGetPlaylistRecursively() {
    PlaylistManager.getInstance().clear();

    Directory dir = dir1;

    List<Playlist> files = dir.getPlaylistsRecursively();

    // no files are available currently
    assertEquals(0, files.size());

    Directory dir1 = DirectoryManager.getInstance().registerDirectory("sub1", dir, dir.getDevice());
    Directory dir2 = DirectoryManager.getInstance().registerDirectory("sub2", dir, dir.getDevice());
    Directory dir3 = DirectoryManager.getInstance().registerDirectory("sub3", dir, dir.getDevice());

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
   *
   * @throws Exception the exception
   */

  public void testGetFilesFromFile() throws Exception {
    Directory dir = dir1;
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
   *
   * @throws Exception the exception
   */

  public void testGetFilesRecursively() throws Exception {
    FileManager.getInstance().clear();

    Directory dir = JUnitHelpers.getDirectory();
    List<File> files = dir.getFilesRecursively();

    // no files are available currently
    assertEquals(0, files.size());

    Directory dir1 = DirectoryManager.getInstance().registerDirectory("sub1", dir, dir.getDevice());
    Directory dir2 = DirectoryManager.getInstance().registerDirectory("sub2", dir, dir.getDevice());
    Directory dir3 = DirectoryManager.getInstance().registerDirectory("sub3", dir, dir.getDevice());

    getFileInDir(3, dir1);
    getFileInDir(4, dir2);
    getFileInDir(5, dir2);
    getFileInDir(6, dir3);

    files = dir.getFilesRecursively();
    assertEquals(4, files.size());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#hasAncestor(org.jajuk.base.Directory)}.
   *
   * @throws Exception the exception
   */
  public void testHasAncestor() throws Exception {
    assertTrue(dir1.hasAncestor(topdir1));
    assertFalse(dir1.hasAncestor(dir2));
    assertFalse(dir1.hasAncestor(dir1));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDirectoriesRecursively()}.
   *
   * @throws Exception the exception
   */
  public void testGetDirectoriesRecursively() throws Exception {
    List<Directory> dirs = topdir1.getDirectoriesRecursively();
    assertTrue(dirs.size() == 2);
    assertFalse(dirs.contains(topdir1));
    assertTrue(dirs.contains(dir1));
    assertTrue(dirs.contains(dir2));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#scan(boolean, org.jajuk.ui.helpers.RefreshReporter)}.
   */

  public void testScan() {
    Directory dir = dir1;

    // this scan will not do much because there are no files in this dir
    dir.scan(true, null);
  }

  /**
   * Test scan actual.
   * DOCUMENT_ME
   *
   * @throws Exception the exception
   */
  public void testScanActual() throws Exception {
    StartupCollectionService.registerItemManagers();
    StartupCollectionService.registerTypes();

    // create temp file
    Device dev = DeviceManager.getInstance().registerDevice("test1", Device.Type.DIRECTORY,
        System.getProperty("java.io.tmpdir"));
    Directory dir = dir1;

    new java.io.File(dev.getUrl()).mkdirs();
    FileUtils.writeStringToFile(new java.io.File(dev.getUrl() + java.io.File.separator + "testScan"
        + java.io.File.separator + "test1.mp3"), "teststring");

    dir.scan(true, null);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#reset()}.
   */

  public void testReset() {
    Directory dir = dir1;
    dir.reset();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getRelativePath()}.
   */

  public void testGetRelativePath() {
    Directory dir = dir1;
    assertNotNull(dir.getRelativePath());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFio()}.
   */

  public void testGetFio() {
    Directory dir = dir1;
    assertNotNull(dir.getFio());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#compareTo(org.jajuk.base.Directory)}.
   */
  public void testCompareTo() {
    Directory dir1 = new Directory("1", "", null, new Device("2", "test"));
    dir1.getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir2 = new Directory("1", "", null, new Device("2", "test"));
    dir2.getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir3a = new Directory("3", "", null, new Device("2", "test3"));
    dir3a.getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir3b = new Directory("2", "", null, new Device("2", "test"));
    dir3b.getDevice().setUrl(System.getProperty("java.io.tmpdir") + java.io.File.separator + "1");
    JUnitHelpers.CompareToTest(dir1, dir2, dir3a);
    JUnitHelpers.CompareToTest(dir1, dir2, dir3b);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#shouldBeHidden()}.
   *
   * @throws Exception the exception
   */

  public void testShouldBeHidden() throws Exception {
    Directory dir = dir1;
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
   * Test method for {@link org.jajuk.base.Directory#cleanRemovedFiles()}.
   */

  public void testCleanRemovedFiles() {
    Directory dir = dir1;
    dir.cleanRemovedFiles();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#isChildOf(org.jajuk.base.Directory)}.
   */

  public void testIsChildOf() {
    assertFalse(topdir1.isChildOf(dir2));
    assertTrue(dir2.isChildOf(topdir1));
  }

}
