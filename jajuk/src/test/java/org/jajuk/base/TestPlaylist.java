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

import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.services.bookmark.Bookmarks;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestPlaylist extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.base.Playlist#hashCode()}.
   */
  public final void testHashCode() {
    Playlist play = new Playlist("1", "name", null);
    Playlist equ = new Playlist("1", "name", null);

    JUnitHelpers.HashCodeTest(play, equ);
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getDesc()}.
   */
  public final void testGetDesc() {
    Playlist play = new Playlist("1", "name", null);
    assertFalse(StringUtils.isBlank(play.getDesc()));

  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#equals(java.lang.Object)}.
   */
  public final void testEqualsObject() {
    Playlist play = new Playlist(Playlist.Type.NORMAL, "1", "name", null);
    Playlist equ = new Playlist(Playlist.Type.NORMAL, "1", "name", null);

    // equals looks at id and type
    Playlist nonequ1 = new Playlist(Playlist.Type.NORMAL, "2", "name", null);
    Playlist nonequ2 = new Playlist(Playlist.Type.BOOKMARK, "1", "name2", null);
    Playlist nonequ3 = new Playlist(Playlist.Type.NOVELTIES, "1", "name", new Directory("1",
        "name", null, new Device("9", "name")));

    JUnitHelpers.EqualsTest(play, equ, nonequ1);
    JUnitHelpers.EqualsTest(play, equ, nonequ2);
    JUnitHelpers.EqualsTest(play, equ, nonequ3);
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getLabel()}.
   */
  public final void testGetLabel() {
    Playlist play = new Playlist("1", "name", null);
    assertTrue(StringUtils.isNotBlank(play.getLabel()));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Playlist#getHumanValue(java.lang.String)}.
   * 
   * @throws Exception
   */
  public final void testGetHumanValue() throws Exception {
    Playlist play = getPlaylist();

    DirectoryManager.getInstance().registerDirectory(play.getDirectory().getDevice());

    // this is what we read here...
    play.setProperty(Const.XML_DIRECTORY, play.getDirectory().getDevice().getID());

    String str1 = System.getProperty("java.io.tmpdir");
    String str2 = play.getHumanValue(Const.XML_DIRECTORY);
    str1 = StringUtils.stripEnd(str1, java.io.File.separator);
    str2 = StringUtils.stripEnd(str2, java.io.File.separator);
    assertEquals(str1, str2);

    assertEquals("", play.getHumanValue("notexist"));

    // define property
    StartupCollectionService.registerItemManagers();
    ItemManager.getItemManager(Playlist.class).registerProperty(
        new PropertyMetaInformation("testkey", true, true, true, true, true, String.class,
            "defaultval"));

    play.setProperty("testkey", "testval");

    assertEquals("testval", play.getHumanValue("testkey"));

    play.removeProperty("testkey");

    assertEquals("defaultval", play.getHumanValue("testkey"));
  }

  /**
   * @return
   * @throws Exception
   */
  private Playlist getPlaylist() throws Exception {
    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);
    device.mount(true);

    Playlist play = new Playlist(Playlist.Type.NORMAL, "1", "playlist.txt", dir);

    List<File> list = new ArrayList<File>();
    list.add(getFile());
    list.add(getFile());

    play.setFiles(list);

    return play;
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getIconRepresentation()}.
   */
  public final void testGetIconRepresentation() {
    Playlist play = new Playlist("1", "name", null);
    assertNotNull(play.getIconRepresentation());
  }

  private File getFile() {
    Style style = new Style("5", "name");
    Album album = new Album("4", "name", "artis", 23);
    Author author = new Author("6", "name");
    Year year = new Year("7", "2000");
    Type type = new Type("8", "name", "mp3", null, null);
    Track track = new Track("3", "name", album, style, author, 120, year, 1, type, 1);
    track.setRate(2);
    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir"));
    Directory dir = new Directory("2", "name", null, device);

    // register files
    FileManager.getInstance().registerFile("1", "test.tst", dir, track, 120, 70);

    return new org.jajuk.base.File("1", "test.tst", dir, track, 120, 70);
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getRate()}.
   * 
   * @throws Exception
   */
  public final void testGetRate() throws Exception {
    Playlist play = getPlaylist();
    play.remove(0);
    play.remove(0);

    assertEquals(0, play.getRate());

    play.addFile(getFile());

    // we use 2 above
    assertEquals(2, play.getRate());

    // multiple files round the rate
    play.addFile(getFile());
    assertEquals(2, play.getRate());

    play.addFile(getFile());
    play.getFiles().get(0).getTrack().setRate(7);

    // (2+2+7)/3 = 3.666 => 4
    assertEquals(4, play.getRate());
  }

  public final void testGetRateNull() throws Exception {
    Playlist play = getPlaylist();
    play.setFiles(null);

    assertEquals(0, play.getRate());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Playlist#Playlist(org.jajuk.base.Playlist.Type, java.lang.String, java.lang.String, org.jajuk.base.Directory)}
   * .
   */
  public final void testPlaylistTypeStringStringDirectory() {
    new Playlist(Playlist.Type.BESTOF, "1", "name", null);
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Playlist#Playlist(java.lang.String, java.lang.String, org.jajuk.base.Directory)}
   * .
   */
  public final void testPlaylistStringStringDirectory() {
    new Playlist("1", "name", null);
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Playlist#addFile(org.jajuk.base.File)}.
   * 
   * @throws Exception
   */
  public final void testAddFileFile() throws Exception {
    Playlist play = getPlaylist();
    play.remove(0);
    play.remove(0);

    play.addFile(getFile());
    assertEquals(1, play.getFiles().size());
  }

  public final void testAddFileQueue() throws Exception {
    Playlist play = getPlaylistQueue();

    File file = getFile();
    file.getDirectory().getDevice().mount(true);
    play.addFile(file);

    // wait a bit to let the "push" be done in a separate thread
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(1, QueueModel.getQueueSize());

    file = getFile();
    file.getDirectory().getDevice().mount(true);
    play.addFile(1, file);

    // wait a bit to let the "push" be done in a separate thread
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(2, QueueModel.getQueueSize());
    assertEquals(2, play.getFiles().size());

    // test with repeat as well to see if we get repeat set for the new track as
    // well
    QueueModel.getItem(0).setRepeat(true);

    file = getFile();
    file.getDirectory().getDevice().mount(true);
    play.addFile(1, file);

    // wait a bit to let the "push" be done in a separate thread
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(3, QueueModel.getQueueSize());
    assertEquals(3, play.getFiles().size());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getType()}.
   */
  public final void testGetType() {
    Playlist play = getPlaylistBookmark();
    assertEquals(Playlist.Type.BOOKMARK, play.getType());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Playlist#addFile(int, org.jajuk.base.File)}.
   * 
   * @throws Exception
   */
  public final void testAddFileIntFile() throws Exception {
    Playlist play = getPlaylist();
    play.remove(0);
    play.remove(0);

    play.addFile(getFile());
    assertEquals(1, play.getFiles().size());

    File file = getFile();
    file.setName("othername");
    play.addFile(1, file);

    // this should now be at pos 1
    assertEquals("test.tst", play.getFiles().get(0).getName());
    assertEquals("othername", play.getFiles().get(1).getName());

    file = getFile();
    file.setName("yetanother");
    play.addFile(1, file);

    assertEquals("test.tst", play.getFiles().get(0).getName());
    assertEquals("yetanother", play.getFiles().get(1).getName());
    assertEquals("othername", play.getFiles().get(2).getName());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#addFiles(java.util.List)}.
   * 
   * @throws Exception
   */
  public final void testAddFiles() throws Exception {
    Playlist play = getPlaylist();
    play.remove(0);
    play.remove(0);

    List<File> files = new ArrayList<File>();

    // empty add does not do anything
    play.addFiles(files);

    assertEquals(0, play.getFiles().size());

    // add some files
    files.add(getFile());
    files.add(getFile());
    files.add(getFile());
    files.add(getFile());

    assertEquals(0, play.getFiles().size());
    play.addFiles(files);
    assertEquals(4, play.getFiles().size());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#clear()}.
   * 
   * @throws Exception
   */
  public final void testClear() throws Exception {
    Playlist play = getPlaylist();

    // nothing happens without content
    play.clear();

    play.addFile(getFile());
    play.addFile(getFile());
    play.addFile(getFile());
    play.addFile(getFile());
    assertEquals(4, play.getFiles().size());

    // now clear clears out the class
    play.clear();
    assertEquals(0, play.getFiles().size());
  }

  public final void testClearEmptyList() throws Exception {
    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);
    device.mount(true);

    Playlist play = new Playlist(Playlist.Type.NORMAL, "1", "playlist.txt", dir);

    play.clear();
  }

  public final void testClearQueue() {
    Playlist play = getPlaylistQueue();
    play.clear();
  }

  public final void testClearBookmark() {
    Playlist play = getPlaylistBookmark();
    play.clear();
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#commit()}.
   * 
   * @throws Exception
   */
  public final void testCommit() throws Exception {
    Playlist play = getPlaylist();

    // need to have URL set for device
    play.getFiles().get(0).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    play.getFiles().get(1).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));

    new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator + "testdir")
        .mkdir();

    play.setFIO(new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
        + "testdir" + java.io.File.separator + "playlist.txt"));

    play.commit();
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Playlist#compareTo(org.jajuk.base.Playlist)}.
   */
  public final void testCompareTo() {
    Playlist play = new Playlist("1", "name", null);

    Playlist equ = new Playlist("1", "name", null);
    Playlist equ2 = new Playlist("4", "name", null); // different id still
                                                     // compares as we just look
                                                     // at name and directory...

    Playlist nonequ1 = new Playlist("2", "name3", null);
    Playlist nonequ2 = new Playlist("5", "name2", null);
    Playlist nonequ3 = new Playlist("2", "name", new Directory("1", "name", null, new Device("9",
        "name")));
    nonequ3.getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));

    JUnitHelpers.CompareToTest(play, equ, nonequ1);
    JUnitHelpers.CompareToTest(play, equ, nonequ2);
    JUnitHelpers.CompareToTest(play, equ, nonequ3);

    JUnitHelpers.CompareToTest(play, equ2, nonequ1);
    JUnitHelpers.CompareToTest(play, equ2, nonequ2);
    JUnitHelpers.CompareToTest(play, equ2, nonequ3);
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#containsExtFiles()}.
   */
  public final void testContainsExtFiles() {
    Playlist play = new Playlist("1", "name", null);

    // false usually
    assertFalse(play.containsExtFiles());

    // TODO: add test that loads a playlist with unavailable files so that this
    // is set to true...
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#down(int)}.
   * 
   * @throws Exception
   */
  public final void testDown() throws Exception {
    Playlist play = getPlaylist();
    play.remove(0);
    play.remove(0);

    File file = getFile();
    file.setName("name1");
    play.addFile(file);

    file = getFile();
    file.setName("name2");
    play.addFile(file);

    file = getFile();
    file.setName("name3");
    play.addFile(file);

    file = getFile();
    file.setName("name4");
    play.addFile(file);

    assertEquals(4, play.getFiles().size());

    play.down(0);
    assertEquals("name2", play.getFiles().get(0).getName());
    assertEquals("name1", play.getFiles().get(1).getName());
    assertEquals("name3", play.getFiles().get(2).getName());
    assertEquals("name4", play.getFiles().get(3).getName());

    play.down(2);
    assertEquals("name2", play.getFiles().get(0).getName());
    assertEquals("name1", play.getFiles().get(1).getName());
    assertEquals("name4", play.getFiles().get(2).getName());
    assertEquals("name3", play.getFiles().get(3).getName());

    play.up(1);
    assertEquals("name1", play.getFiles().get(0).getName());
    assertEquals("name2", play.getFiles().get(1).getName());
    assertEquals("name4", play.getFiles().get(2).getName());
    assertEquals("name3", play.getFiles().get(3).getName());

    play.up(3);
    assertEquals("name1", play.getFiles().get(0).getName());
    assertEquals("name2", play.getFiles().get(1).getName());
    assertEquals("name3", play.getFiles().get(2).getName());
    assertEquals("name4", play.getFiles().get(3).getName());
  }

  public final void testDownBookmark() {
    Playlist play = getPlaylistBookmark();
    play.down(0);
    play.up(0);
  }

  /**
   * @return
   */
  private Playlist getPlaylistBookmark() {
    return new Playlist(Playlist.Type.BOOKMARK, "1", "name", null);
  }

  public final void testDownQueue() {
    Playlist play = getPlaylistQueue();
    play.down(-1);
    play.up(0);
  }

  /**
   * @return
   */
  private Playlist getPlaylistQueue() {
    // make sure the Queue is empty before creating a playlist on it
    QueueModel.clear();

    return new Playlist(Playlist.Type.QUEUE, "1", "name", null);
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#forceRefresh()}.
   * 
   * @throws Exception
   */
  public final void testForceRefresh() throws Exception {
    // make sure we have a playlist stored before
    {
      Playlist play = getPlaylist();

      // need to have URL set for device
      play.getFiles().get(0).getDirectory().getDevice()
          .setUrl(System.getProperty("java.io.tmpdir"));
      play.getFiles().get(1).getDirectory().getDevice()
          .setUrl(System.getProperty("java.io.tmpdir"));

      new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator + "testdir")
          .mkdir();

      play.setFIO(new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
          + "testdir" + java.io.File.separator + "playlist.txt"));

      play.commit();
    }

    Playlist play = getPlaylist();

    // need to have URL set for device
    play.getFiles().get(0).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    play.getFiles().get(1).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));

    play.forceRefresh();
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getAbsolutePath()}.
   * 
   * @throws Exception
   */
  public final void testGetAbsolutePath() throws Exception {
    Playlist play = getPlaylist();
    assertEquals(System.getProperty("java.io.tmpdir") + java.io.File.separator + "testdir"
        + java.io.File.separator + "playlist.txt", play.getAbsolutePath());

    // call it a second time to use the cached version
    assertEquals(System.getProperty("java.io.tmpdir") + java.io.File.separator + "testdir"
        + java.io.File.separator + "playlist.txt", play.getAbsolutePath());
  }

  public final void testGetAbsolutePathNotNormal() {
    Playlist play = new Playlist(Playlist.Type.BESTOF, "1", "name", null);
    assertTrue(StringUtils.isBlank(play.getAbsolutePath()));

    play.setFIO(new java.io.File("testfile"));
    assertTrue(StringUtils.isNotBlank(play.getAbsolutePath()));
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getDirectory()}.
   */
  public final void testGetDirectory() {
    Playlist play = new Playlist("1", "name", null);
    assertNull(play.getDirectory());

    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);

    play = new Playlist(Playlist.Type.NORMAL, "1", "playlist.txt", dir);
    assertNotNull(play.getDirectory());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getFiles()}.
   * 
   * @throws Exception
   */
  public final void testGetFiles() throws Exception {
    Playlist play = getPlaylist();
    play.remove(0);
    play.remove(0);

    assertEquals(0, play.getFiles().size());

    play.addFile(getFile());
    assertEquals(1, play.getFiles().size());
  }

  public final void testGetFilesNull() throws Exception {
    Playlist play = getPlaylist();
    play.setFiles(null); // null as list!

    assertEquals(2, play.getFiles().size());
  }

  public final void testGetFilesNovelities() throws Exception {
    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);
    device.mount(true);

    Playlist play = new Playlist(Playlist.Type.NOVELTIES, "1", "playlist.txt", dir);

    assertNotNull(play.getFiles());
  }

  public final void testGetFilesBestOf() throws Exception {
    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);
    device.mount(true);

    Playlist play = new Playlist(Playlist.Type.BESTOF, "1", "playlist.txt", dir);

    assertNotNull(play.getFiles());
  }

  public final void testGetFilesNew() throws Exception {
    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);
    device.mount(true);

    Playlist play = new Playlist(Playlist.Type.NEW, "1", "playlist.txt", dir);

    assertNotNull(play.getFiles());
    assertEquals(0, play.getFiles().size());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getFIO()}.
   */
  public final void testGetAndSetFIO() {
    Playlist play = new Playlist(Playlist.Type.BESTOF, "1", "name", null);
    assertNotNull(play.getFIO());

    play.setFIO(null);
    assertNotNull(play.getFIO()); // recreated...

    play.setFIO(new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
        + "testfio"));
    assertNotNull(play.getFIO());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#isReady()}.
   * 
   * @throws Exception
   */
  public final void testIsReady() throws Exception {
    Playlist play = getPlaylist();

    // mounted initially
    assertTrue(play.isReady());

    play.getDirectory().getDevice().unmount();

    assertFalse(play.isReady());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#load()}.
   * 
   * @throws Exception
   */
  public final void testLoad() throws Exception {
    // first commit a playlist
    {
      Playlist play = getPlaylist();

      play.addFile(getFile());

      // need to have URL set for device
      play.getFiles().get(0).getDirectory().getDevice()
          .setUrl(System.getProperty("java.io.tmpdir"));
      play.getFiles().get(1).getDirectory().getDevice()
          .setUrl(System.getProperty("java.io.tmpdir"));
      play.getFiles().get(2).getDirectory().getDevice()
          .setUrl(System.getProperty("java.io.tmpdir"));

      new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator + "testdir")
          .mkdir();

      play.setFIO(new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
          + "testdir" + java.io.File.separator + "playlist.txt"));

      play.commit();
    }

    Playlist play = getPlaylist();
    List<File> list = play.load();
    assertNotNull(list);

    assertEquals(3, list.size());

    assertEquals("test.tst", list.get(0).getName());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#play()}.
   * 
   * @throws Exception
   */
  public final void testPlay() throws Exception {
    Playlist play = getPlaylist();

    // some error without files
    play.play();

    play.addFile(getFile());

    // try again with files
    play.play();
  }

  public final void testPlayNull() throws Exception {
    Playlist play = getPlaylist();

    // some error without files
    play.setFiles(null);
    try {
      play.play();
    } catch (HeadlessException e) {
      // this tries to open a FileChooser...
    }
    play.setFiles(new ArrayList<File>());
    try {
      play.play();
    } catch (HeadlessException e) {
      // this tries to open a FileChooser...
    }
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#remove(int)}.
   * 
   * @throws Exception
   */
  public final void testRemove() throws Exception {
    Playlist play = getPlaylist();
    play.addFile(getFile());
    play.remove(0);
  }

  public final void testRemoveBookmark() {
    Bookmarks.getInstance().addFile(getFile());

    Playlist play = getPlaylistBookmark();
    play.remove(0);
  }

  public final void testRemoveQueue() {
    Playlist play = getPlaylistQueue();
    play.remove(0);
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Playlist#replaceFile(org.jajuk.base.File, org.jajuk.base.File)}
   * .
   * 
   * @throws Exception
   */
  public final void testReplaceFile() throws Exception {
    Playlist play = getPlaylist();

    play.addFile(getFile());

    // need to have URLs set for Devices here
    play.getFiles().get(0).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    play.getFiles().get(1).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    play.getFiles().get(2).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));

    File file = getFile();
    file.getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));

    play.setFIO(new java.io.File(System.getProperty("java.io.tmpdir") + java.io.File.separator
        + "testdir" + java.io.File.separator + "playlist.txt"));

    play.replaceFile(play.getFiles().get(0), file);
  }

  public final void testReplaceFileBookmark() throws Exception {
    Playlist play = getPlaylistBookmark();

    play.addFile(getFile());

    // wait for the thread to finish before doing this
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    play.replaceFile(play.getFiles().get(0), getFile());
  }

  public final void testReplaceFileQueue() throws Exception {
    // make sure Queue is empty
    QueueModel.clear();

    Playlist play = getPlaylistQueue();

    // for type Queue, we need to push to the Queue
    File file = getFile();
    file.getDirectory().getDevice().mount(true);
    QueueModel.insert(new StackItem(file), 0);

    assertEquals(1, play.getFiles().size());
    assertNotNull(play.getFiles().get(0));

    play.replaceFile(play.getFiles().get(0), getFile());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#reset()}.
   */
  public final void testReset() {
    Playlist play = new Playlist("1", "name", null);

    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);

    play.setParentDirectory(dir);

    play.setFIO(new java.io.File("testfile"));
    play.reset();
    assertNotNull(play.getFIO()); // recreated again...
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#saveAs()}.
   * 
   * @throws Exception
   */
  public final void testSaveAs() throws Exception {
    Playlist play = getPlaylist();

    // need to have URLs set for Devices here
    play.getFiles().get(0).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));
    play.getFiles().get(1).getDirectory().getDevice().setUrl(System.getProperty("java.io.tmpdir"));

    try {
      play.saveAs();
    } catch (InvocationTargetException e) {
      // this tries to open a FileChooser...
    } catch (HeadlessException e) {
      // this tries to open a FileChooser...
    }
  }

  public final void testSaveAsBestOf() throws Exception {
    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);
    device.mount(true);

    Playlist play = new Playlist(Playlist.Type.BESTOF, "1", "playlist.txt", dir);

    List<File> list = new ArrayList<File>();
    list.add(getFile());
    list.add(getFile());

    play.setFiles(list);

    try {
      play.saveAs();
    } catch (InvocationTargetException e) {
      // this tries to open a FileChooser...
    } catch (HeadlessException e) {
      // this tries to open a FileChooser...
    }
  }

  public final void testSaveAsBookmark() throws Exception {
    Playlist play = getPlaylistBookmark();

    try {
      play.saveAs();
    } catch (InvocationTargetException e) {
      // this tries to open a FileChooser...
    } catch (HeadlessException e) {
      // this tries to open a FileChooser...
    }
  }

  public final void testSaveAsNovelities() throws Exception {
    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);
    device.mount(true);

    Playlist play = new Playlist(Playlist.Type.NOVELTIES, "1", "playlist.txt", dir);

    List<File> list = new ArrayList<File>();
    list.add(getFile());
    list.add(getFile());

    play.setFiles(list);

    try {
      play.saveAs();
    } catch (InvocationTargetException e) {
      // this tries to open a FileChooser...
    } catch (HeadlessException e) {
      // this tries to open a FileChooser...
    }
  }

  public final void testSaveAsQueue() throws Exception {
    Playlist play = getPlaylistQueue();

    try {
      play.saveAs();
    } catch (InvocationTargetException e) {
      // this tries to open a FileChooser...
    } catch (HeadlessException e) {
      // this tries to open a FileChooser...
    }
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#setFiles(java.util.List)}.
   * 
   * @throws Exception
   */
  public final void testSetFiles() throws Exception {
    Playlist play = getPlaylist();

    List<File> list = new ArrayList<File>();
    list.add(getFile());

    play.setFiles(list);

    assertEquals(1, play.getFiles().size());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#setFIO(java.io.File)}.
   */
  public final void testSetFIO() {
    // tested above in getFIO();
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Playlist#setParentDirectory(org.jajuk.base.Directory)}
   * .
   */
  public final void testSetParentDirectory() {
    Playlist play = new Playlist("1", "name", null);
    assertNull(play.getDirectory());

    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);

    play.setParentDirectory(dir);

    assertNotNull(play.getDirectory());

    // also try setting it to null
    play.setParentDirectory(null);
    assertNull(play.getDirectory());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#shouldBeHidden()}.
   * 
   * @throws Exception
   */
  public final void testShouldBeHidden() throws Exception {

    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);

    Playlist play = new Playlist("1", "name", dir);

    // related configuration
    Conf.setProperty(Const.CONF_OPTIONS_HIDE_UNMOUNTED, "false");

    // always false as long as conf is set to "false"
    assertFalse(play.shouldBeHidden());

    // related configuration
    Conf.setProperty(Const.CONF_OPTIONS_HIDE_UNMOUNTED, "true");

    // now "true" because device is not mounted
    assertTrue(play.shouldBeHidden());

    // now mount the device
    device.mount(true);

    // now "false" again, as we have the device mounted
    assertFalse(play.shouldBeHidden());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#toString()}.
   */
  public final void testToString() {
    Playlist play = new Playlist("1", "name", null);

    // first test without directory
    JUnitHelpers.ToStringTest(play);

    Device device = new Device("9", "name");
    device.setUrl(System.getProperty("java.io.tmpdir")); // directory to use
    // for storage
    Directory dir = new Directory("2", "testdir", null, device);

    // then with a directory
    play = new Playlist("1", "name", dir);
    JUnitHelpers.ToStringTest(play);
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#up(int)}.
   */
  public final void testUp() {
    // tested as part of testDown()
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getHits()}.
   * 
   * @throws Exception
   */
  public final void testGetHits() throws Exception {
    Playlist play = getPlaylist();

    // first without files
    assertEquals(0, play.getHits());

    // then with some files
    play.addFile(getFile());

    // still zero as file has no hits set
    assertEquals(0, play.getHits());

    // now add a file with hit-count set
    File file = getFile();
    file.getTrack().setHits(3);
    play.addFile(file);

    // now hits are set
    assertEquals(3, play.getHits());

    // add another file with different hit-count
    file = getFile();
    file.getTrack().setHits(11);
    play.addFile(file);

    // now hits accumulate
    assertEquals(14, play.getHits());
  }

  public final void testGetHitsNull() throws Exception {
    Playlist play = getPlaylist();
    play.setFiles(null);

    // first without files
    assertEquals(0, play.getHits());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getDuration()}.
   * 
   * @throws Exception
   */
  public final void testGetDuration() throws Exception {
    Playlist play = getPlaylist();
    play.remove(0);
    play.remove(0);

    // at first no duration at all
    assertEquals(0, play.getDuration());

    // when we add tracks, duration accumulates
    play.addFile(getFile());

    // we use 120 seconds as length in "getFile()"
    assertEquals(120, play.getDuration());

    // another file
    play.addFile(getFile());

    // sums up two times 120
    assertEquals(240, play.getDuration());
  }

  public final void testGetDurationNull() throws Exception {
    Playlist play = getPlaylist();
    play.setFiles(null);

    // at first no duration at all
    assertEquals(0, play.getDuration());
  }

  /**
   * Test method for {@link org.jajuk.base.Playlist#getNbOfTracks()}.
   * 
   * @throws Exception
   */
  public final void testGetNbOfTracks() throws Exception {
    Playlist play = getPlaylist();
    play.remove(0);
    play.remove(0);

    assertEquals(0, play.getNbOfTracks());

    play.addFile(getFile());
    assertEquals(1, play.getNbOfTracks());

    // another file
    play.addFile(getFile());

    assertEquals(2, play.getNbOfTracks());
  }

  public final void testGetNbOfTracksNull() throws Exception {
    Playlist play = getPlaylist();
    play.setFiles(null);

    assertEquals(0, play.getNbOfTracks());
  }
}
