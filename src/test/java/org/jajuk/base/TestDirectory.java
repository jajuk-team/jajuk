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

import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.ConstTest;
import org.jajuk.JajukTestCase;
import org.jajuk.MockPlayer;
import org.jajuk.TestHelpers;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * TODO: some more coverage is possible by enhancing the tests accordingly.
 */
public class TestDirectory extends JajukTestCase {
  @Override
  protected void specificSetUp() throws Exception {
    // reset some conf-options
    Conf.setProperty(Const.CONF_OPTIONS_HIDE_UNMOUNTED, "false");
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getTitle()}.
   */
  @Test
  public void testGetDesc() {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertTrue(StringUtils.isNotBlank(dir.getTitle()), dir.toString());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getXMLTag()}.
   */
  @Test
  public void testGetLabel() {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertEquals(Const.XML_DIRECTORY, dir.getXMLTag(), dir.toString());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getHumanValue(java.lang.String)}.
   */
  @Test
  public void testGetHumanValue() {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertEquals(dir.getParentDirectory().getFio(), new java.io.File(dir.getHumanValue(Const.XML_DIRECTORY_PARENT)), dir.toString());
    assertEquals("sample_device", dir.getHumanValue(Const.XML_DEVICE), dir.toString());
    assertTrue(StringUtils.isNotBlank(dir.getHumanValue(Const.XML_NAME)), dir.toString());
    assertEquals("", dir.getHumanValue("notexisting"), dir.toString());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getIconRepresentation()}.
   */
  @Test
  public void testGetIconRepresentation() {
    StartupCollectionService.registerItemManagers();
    Directory dir = TestHelpers.getDirectory("dir1");
    assertNotNull(dir.getIconRepresentation());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#Directory(java.lang.String, java.lang.String, org.jajuk.base.Directory, org.jajuk.base.Device)}.
   */
  @Test
  public void testDirectory() {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertNotNull(dir);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#toString()}.
   */
  @Test
  public void testToString() {
    Directory dir = TestHelpers.getDirectory("dir1");
    TestHelpers.ToStringTest(dir);
  }

  /**
   * Test to string parent.
   * 
   */
  @Test
  public void testToStringParent() {
    Directory dir = TestHelpers.getDirectory("dir1");
    TestHelpers.ToStringTest(dir);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getAbsolutePath()}.
   */
  @Test
  public void testGetAbsolutePath() {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertTrue(StringUtils.isNotBlank(dir.getAbsolutePath()), dir.toString());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDevice()}.
   */
  @Test
  public void testGetDevice() {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertNotNull(dir.getDevice());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getParentDirectory()}.
   */
  @Test
  public void testGetParentDirectory() {
    Directory dir = TestHelpers.getDevice().getRootDirectory();
    assertNull(dir.getParentDirectory());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDirectories()}.
   */
  @Test
  public void testGetDirectories() {
    Directory dir = TestHelpers.getDirectory("dir1");
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
  @Test
  public void testGetFiles() throws Exception {
    Directory dir = TestHelpers.getDirectory("dir1");
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
   * @param i 
   * @param dir 
   * @return the file in dir
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static org.jajuk.base.File getFileInDir(int i, Directory dir) throws Exception {
    Genre genre = TestHelpers.getGenre("name");
    Album album = TestHelpers.getAlbum("myalbum", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test
    Artist artist = TestHelpers.getArtist("name");
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
  @Test
  public void testGetPlaylistFiles() {
    Directory dir = TestHelpers.getDirectory("dir1");
    Set<Playlist> files = dir.getPlaylistFiles();
    // no files are available currently
    assertEquals(0, files.size());
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(TestHelpers.getDevice().getUrl() + java.io.File.separator + "testfile1"),
        dir);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(TestHelpers.getDevice().getUrl() + java.io.File.separator + "testfile2"),
        dir);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(TestHelpers.getDevice().getUrl() + java.io.File.separator + "testfile3"),
        dir);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(TestHelpers.getDevice().getUrl() + java.io.File.separator + "testfile4"),
        dir);
    files = dir.getPlaylistFiles();
    assertEquals(4, files.size());
  }

  /**
   * Test get playlist recursively.
   * 
   */
  @Test
  public void testGetPlaylistRecursively() {
    PlaylistManager.getInstance().clear();
    Directory dir = TestHelpers.getDirectory();
    List<Playlist> files = dir.getPlaylistsRecursively();
    // no files are available currently
    assertEquals(0, files.size());
    Directory dir1 = DirectoryManager.getInstance().registerDirectory("sub1", dir, dir.getDevice());
    Directory dir2 = DirectoryManager.getInstance().registerDirectory("sub2", dir, dir.getDevice());
    Directory dir3 = DirectoryManager.getInstance().registerDirectory("sub3", dir, dir.getDevice());
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(TestHelpers.getDevice().getUrl() + java.io.File.separator + "testfile1"),
        dir1);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(TestHelpers.getDevice().getUrl() + java.io.File.separator + "testfile2"),
        dir2);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(TestHelpers.getDevice().getUrl() + java.io.File.separator + "testfile3"),
        dir2);
    PlaylistManager.getInstance().registerPlaylistFile(
        new java.io.File(TestHelpers.getDevice().getUrl() + java.io.File.separator + "testfile4"),
        dir3);
    files = dir.getPlaylistsRecursively();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFilesFromFile(org.jajuk.base.File)}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetFilesFromFile() throws Exception {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertNull(dir.getFilesFromFile(null));
    getFileInDir(3, dir);
    getFileInDir(4, dir);
    File file = getFileInDir(5, dir);
    getFileInDir(6, dir);
    List<File> list = dir.getFilesFromFile(file);
    assertTrue(list.size() > 0, "Size: " + list.size());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFilesRecursively()}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetFilesRecursively() throws Exception {
    FileManager.getInstance().clear();
    Directory dir = TestHelpers.getDirectory();
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
  @Test
  public void testHasAncestor() throws Exception {
    Directory dir1 = TestHelpers.getDirectory("dir1");
    Directory dir2 = TestHelpers.getDirectory("dir2");
    Directory topdir1 = TestHelpers.getDevice().getRootDirectory();
    assertTrue(dir1.hasAncestor(topdir1));
    assertFalse(dir1.hasAncestor(dir2));
    assertFalse(dir1.hasAncestor(dir1));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getDirectoriesRecursively()}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDirectoriesRecursively() throws Exception {
    Directory topdir1 = TestHelpers.getDevice().getRootDirectory();
    Directory dir1 = TestHelpers.getDirectory("dir1");
    Directory dir2 = TestHelpers.getDirectory("dir2");
    List<Directory> dirs = topdir1.getDirectoriesRecursively();
    assertTrue(dirs.size() == 2);
    assertFalse(dirs.contains(topdir1));
    assertTrue(dirs.contains(dir1));
    assertTrue(dirs.contains(dir2));
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#scan(boolean, org.jajuk.ui.helpers.RefreshReporter)}.
   */
  @Test
  public void testScan() {
    Directory dir = TestHelpers.getDirectory("dir1");
    // this scan will not do much because there are no files in this dir
    dir.scan(true, null);
  }

  /**
   * Test scan actual.
   * 
   *
   * @throws Exception the exception
   */
  @Test
  public void testScanActual() throws Exception {
    StartupCollectionService.registerItemManagers();
    StartupCollectionService.registerTypes();
    // create temp file
    Directory dir = TestHelpers.getDirectory("dir1");
    File file = TestHelpers.getFile("test1.mp3", true);
    FileUtils.writeStringToFile(file.getFIO(), "teststring");
    dir.scan(true, null);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#reset()}.
   */
  @Test
  public void testReset() {
    Directory dir = TestHelpers.getDirectory("dir1");
    dir.reset();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getRelativePath()}.
   */
  @Test
  public void testGetRelativePath() {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertNotNull(dir.getRelativePath());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#getFio()}.
   */
  @Test
  public void testGetFio() {
    Directory dir = TestHelpers.getDirectory("dir1");
    assertNotNull(dir.getFio());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#compareTo(org.jajuk.base.Directory)}.
   */
  @Test
  public void testCompareTo() {
    Device device1 = TestHelpers.getDevice("1", Device.Type.DIRECTORY, ConstTest.DEVICES_BASE_PATH
        + "/1");
    Device device2 = TestHelpers.getDevice("2", Device.Type.DIRECTORY, ConstTest.DEVICES_BASE_PATH
        + "/2");
    Directory dir11 = DirectoryManager.getInstance().registerDirectory("dir11",
        device1.getRootDirectory(), device1);
    Directory dir12 = DirectoryManager.getInstance().registerDirectory("dir12",
        device1.getRootDirectory(), device1);
    Directory dir2 = DirectoryManager.getInstance().registerDirectory("dir2",
        device1.getRootDirectory(), device2);
    assertTrue(dir11.compareTo(dir12) < 0);
    assertTrue(dir2.compareTo(dir11) > 0);
    assertTrue(dir2.compareTo(dir12) > 0);
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#shouldBeHidden()}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testShouldBeHidden() throws Exception {
    Directory dir = TestHelpers.getDirectory("dir1");
    // not mounted by default
    assertFalse(dir.getDevice().isMounted());
    // false because option is not set
    assertFalse(dir.shouldBeHidden());
    Conf.setProperty(Const.CONF_OPTIONS_HIDE_UNMOUNTED, "true");
    // now true because option to hide unmounted is set
    assertTrue(dir.shouldBeHidden());
    dir.getDevice().setUrl(ConstTest.DEVICES_BASE_PATH);
    dir.getDevice().mount(true);
    // now false because device is mounted now
    assertFalse(dir.shouldBeHidden());
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#cleanRemovedFiles()}.
   */
  @Test
  public void testCleanRemovedFiles() {
    Directory dir = TestHelpers.getDirectory("dir1");
    dir.cleanRemovedFiles();
  }

  /**
   * Test method for {@link org.jajuk.base.Directory#isChildOf(org.jajuk.base.Directory)}.
   */
  @Test
  public void testIsChildOf() {
    Directory topdir1 = TestHelpers.getDevice().getRootDirectory();
    Directory dir2 = TestHelpers.getDirectory("dir2");
    assertFalse(topdir1.isChildOf(dir2));
    assertTrue(dir2.isChildOf(topdir1));
  }
}
