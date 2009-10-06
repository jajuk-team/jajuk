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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
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
import org.jajuk.services.core.SessionService;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.error.JajukException;

/**
 * 
 */
public class TestQueueModel extends TestCase {

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // reset before each test to have a clean start for each test as most
    // data is held statically for QueueModel
    QueueModel.reset();
    QueueModel.stopRequest();

    // reset conf changes to default
    Conf.setProperty(Const.CONF_STATE_CONTINUE, "false");

    // make sure we reset WebRadio
    QueueModel.launchRadio(null);

    // remove any registered files
    for (File file : FileManager.getInstance().getFiles()) {
      FileManager.getInstance().removeFile(file);
    }

    super.setUp();
  }

  // helper method to emma-coverage of the unused constructor
  public void testPrivateConstructor() throws Exception {
    // For EMMA code-coverage tests
    JUnitHelpers.executePrivateConstructor(QueueModel.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    // wait a bit to let background-threads finish
    Thread.sleep(200);

    super.tearDown();
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#reset()}.
   */

  public void testReset() throws Exception {
    // nothing to reset up-front
    QueueModel.reset();
    assertEquals(0, QueueModel.getQueueSize());

    // things are reset with queued items
    addItems(10);
    QueueModel.setIndex(4);
    QueueModel.reset();
    assertEquals(0, QueueModel.getQueueSize());
    assertEquals(0, QueueModel.getIndex());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#resetAround(int, org.jajuk.base.Album)}
   * .
   */

  public void testResetAround() throws Exception {
    addItems(10);
    QueueModel.resetAround(1, new Album("1", "name", "artist", 0));
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#setRepeatModeToAll(boolean)}.
   */

  public void testSetRepeatModeToAll() {
    QueueModel.setRepeatModeToAll(false);
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#push(java.util.List, boolean)}
   * .
   */

  public void testPushListOfStackItemBoolean() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(getFile(1)));

    QueueModel.push(list, true);

    // there is a thread started, so delay a bit to let that happen...
    Thread.sleep(400);

    assertEquals(1, QueueModel.getQueue().size());
  }

  public void testPushListOfStackItemBooleanNoPush() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(getFile(1)));

    QueueModel.push(list, false);

    // there is a thread started, so delay a bit to let that happen...
    Thread.sleep(400);

    assertEquals(1, QueueModel.getQueue().size());
  }

  public void testPushListOfStackItemBooleanNullItems() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(getFile(1)));
    list.add(null);
    list.add(new StackItem(getFile(3)));

    QueueModel.push(list, true);

    // there is a thread started, so delay a bit to let that happen...
    Thread.sleep(400);

    assertEquals(2, QueueModel.getQueue().size());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#push(java.util.List, boolean, boolean)}
   * .
   */

  public void testPushListOfStackItemBooleanBoolean() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(getFile(1)));

    QueueModel.push(list, true, true);

    // there is a thread started, so delay a bit to let that happen...
    Thread.sleep(200);

    assertEquals(1, QueueModel.getQueue().size());
  }

  public void testPushListOfStackItemBooleanBooleanNoPushNext() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(getFile(1)));

    QueueModel.push(list, false, false);

    // there is a thread started, so delay a bit to let that happen...
    Thread.sleep(200);

    assertEquals(1, QueueModel.getQueue().size());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#push(org.jajuk.services.players.StackItem, boolean)}
   * .
   */

  public void testPushStackItemBoolean() throws Exception {
    QueueModel.push(new StackItem(getFile(1)), true);

    // there is a thread started, so delay a bit to let that happen...
    Thread.sleep(200);

    assertEquals(1, QueueModel.getQueue().size());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#push(org.jajuk.services.players.StackItem, boolean, boolean)}
   * .
   */

  public void testPushStackItemBooleanBoolean() throws Exception {
    QueueModel.push(new StackItem(getFile(1)), true, true);

    // there is a thread started, so delay a bit to let that happen...
    Thread.sleep(200);

    assertEquals(1, QueueModel.getQueue().size());
  }

  /**
   * @param count
   *          number of items to create
   * @throws JajukException
   */
  private void addItems(int count) throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    for (int i = 0; i < count; i++) {
      list.add(new StackItem(getFile(i)));
    }
    QueueModel.insert(list, 0);
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
    Track track = new Track(Integer.valueOf(i).toString(), "name", album, style, author, 120, year, 1,
        type, 1);

    Device device = new Device(Integer.valueOf(i).toString(), "name");
    device.setUrl(System.getProperty("java.io.tmpdir"));
    device.mount(true);

    Directory dir = new Directory(Integer.valueOf(i).toString(), "name", null, device);

    return new org.jajuk.base.File(Integer.valueOf(i).toString(), "test.tst", dir, track, 120, 70);
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#launchRadio(org.jajuk.services.webradio.WebRadio)}
   * .
   */

  public void testLaunchRadio() {
    QueueModel.launchRadio(new WebRadio("name", "invalidurl"));
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#containsRepeat()}.
   */

  public void testContainsRepeat() throws Exception {
    addItems(2);

    assertFalse(QueueModel.containsRepeat());

    QueueModel.setRepeatModeToAll(true);
    assertTrue("Items: " + QueueModel.getQueue(), QueueModel.containsRepeat());

  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#finished()}.
   */

  public void testFinished() throws Exception {
    // without item it just returns
    QueueModel.finished();

    // with items, it will go to the next ine
    addItems(10);
    QueueModel.setIndex(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.finished();
    assertEquals(1, QueueModel.getIndex());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#finished(boolean)}.
   */

  public void testFinishedBoolean() throws Exception {
    // without item it just returns
    QueueModel.finished(true);

    // with items, it will go to the next ine
    addItems(10);
    QueueModel.setIndex(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.finished(true);
    assertEquals(1, QueueModel.getIndex());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#setIndex(int)}
   * .
   */

  public void testSetAndGetIndex() throws Exception {
    // with items, it will go to the next ine
    addItems(10);
    QueueModel.setIndex(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.setIndex(3);
    assertEquals(3, QueueModel.getIndex());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#computesPlanned(boolean)}.
   */

  public void testComputesPlanned() throws Exception {
    // without tracks it will not do much
    QueueModel.computesPlanned(false);

    // with tracks, it will look at planned items
    addItems(10);
    QueueModel.computesPlanned(true);

  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#clear()}.
   */

  public void testClear() throws Exception {
    // nothing to reset up-front
    QueueModel.clear();
    assertEquals(0, QueueModel.getQueueSize());

    // things are reset with queued items
    addItems(10);
    QueueModel.setIndex(4);
    QueueModel.clear();
    assertEquals(0, QueueModel.getQueueSize());
    assertEquals(0, QueueModel.getIndex());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#containsOnlyRepeat()}.
   */

  public void testContainsOnlyRepeat() throws Exception {
    assertTrue(QueueModel.containsOnlyRepeat());
    addItems(10);
    assertFalse(QueueModel.containsOnlyRepeat());
    QueueModel.setRepeatModeToAll(true);
    assertTrue(QueueModel.containsOnlyRepeat());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#playPrevious()}.
   */

  public void testPlayPrevious() throws Exception {
    // do nothing without items
    QueueModel.playPrevious();

    // with items:
    addItems(10);
    QueueModel.setIndex(2);
    QueueModel.playPrevious();
    assertEquals(1, QueueModel.getIndex());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#playPreviousAlbum()}.
   */

  public void testPlayPreviousAlbum() {
    QueueModel.playPreviousAlbum();
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#playNext()}.
   */

  public void testPlayNext() throws Exception {
    // do nothing without items
    QueueModel.playNext();

    // with items:
    addItems(10);
    QueueModel.setIndex(2);
    QueueModel.playNext();
    assertEquals(3, QueueModel.getIndex());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#playNextAlbum()}.
   */

  public void testPlayNextAlbum() {
    QueueModel.playNextAlbum();
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#getPlayingFile()}.
   */

  public void testGetPlayingFile() throws Exception {
    assertNull(QueueModel.getPlayingFile());

    addItems(10);
    // QueueModel.playNext();
    QueueModel.goTo(0);
    assertFalse(QueueModel.isStopped());
    assertNotNull(QueueModel.getPlayingFile());
    // we start at 0
    assertEquals("0", QueueModel.getPlayingFile().getID());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#getCurrentItem()}.
   */

  public void testGetCurrentItem() throws Exception {
    // no item without items
    assertNull(QueueModel.getCurrentItem());

    addItems(10);
    QueueModel.playNext();
    assertEquals("1", QueueModel.getCurrentItem().getFile().getID());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#getItem(int)}.
   */

  public void testGetItem() throws Exception {
    addItems(10);
    assertEquals("0", QueueModel.getItem(0).getFile().getID());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#canUnmount(org.jajuk.base.Device)}
   * .
   */

  public void testCanUnmount() throws Exception {
    assertTrue(QueueModel.canUnmount(new Device("0", "test")));

    addItems(10);

    // still true as we are not playing
    assertTrue(QueueModel.canUnmount(new Device("1", "test")));
    assertTrue(QueueModel.canUnmount(new Device("11", "test")));

    // try to start playing/planning
    QueueModel.playNext();
    assertFalse(QueueModel.canUnmount(QueueModel.getItem(1).getFile().getDevice()));
    assertTrue(QueueModel.canUnmount(new Device("11", "test")));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#stopRequest()}
   * .
   */

  public void testStopRequest() {
    QueueModel.stopRequest();
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#isStopped()}.
   */

  public void testIsStopped() throws Exception {
    assertTrue(QueueModel.isStopped());

    addItems(10);

    // try to start playing/planning
    QueueModel.playNext();

    assertFalse(QueueModel.isStopped());

    QueueModel.stopRequest();
    assertTrue(QueueModel.isStopped());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#getQueue()}.
   */

  public void testGetQueue() throws Exception {
    assertEquals(0, QueueModel.getQueue().size());

    addItems(10);

    assertEquals(10, QueueModel.getQueue().size());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#getQueueSize()}.
   */

  public void testGetQueueSize() throws Exception {
    assertEquals(0, QueueModel.getQueueSize());

    addItems(10);

    assertEquals(10, QueueModel.getQueueSize());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#shuffle()}.
   */

  public void testShuffle() throws Exception {
    // shuffle should not fail if queue is empty
    QueueModel.shuffle();

    addItems(10);

    // verify that we have them in order before
    assertEquals("0", QueueModel.getItem(0).getFile().getID());
    assertEquals("5", QueueModel.getItem(5).getFile().getID());
    assertEquals("9", QueueModel.getItem(9).getFile().getID());

    QueueModel.shuffle();

    // it's very unlikely that we have the same order afterwards
    assertFalse("Queue: " + QueueModel.getQueue(), QueueModel.getItem(0).getFile().getID().equals(
        "0")
        && QueueModel.getItem(5).getFile().getID().equals("5")
        && QueueModel.getItem(9).getFile().getID().equals("9"));
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#insert(org.jajuk.services.players.StackItem, int)}
   * .
   */

  public void testInsertStackItemInt() throws Exception {
    assertEquals(0, QueueModel.getQueueSize());
    QueueModel.insert(new StackItem(getFile(0)), 0);

    assertEquals(1, QueueModel.getQueueSize());

    // when we insert the next one at 0, the previous one should be moved
    QueueModel.insert(new StackItem(getFile(1)), 0);

    assertEquals(2, QueueModel.getQueueSize());
    assertEquals("1", QueueModel.getItem(0).getFile().getID());
    assertEquals("0", QueueModel.getItem(1).getFile().getID());

    // adding in between now, should again adjust the queue accordingly
    QueueModel.insert(new StackItem(getFile(2)), 1);

    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("1", QueueModel.getItem(0).getFile().getID());
    assertEquals("2", QueueModel.getItem(1).getFile().getID());
    assertEquals("0", QueueModel.getItem(2).getFile().getID());

    // and adding at the end should work as well
    QueueModel.insert(new StackItem(getFile(3)), 3);

    assertEquals(4, QueueModel.getQueueSize());
    assertEquals("1", QueueModel.getItem(0).getFile().getID());
    assertEquals("2", QueueModel.getItem(1).getFile().getID());
    assertEquals("0", QueueModel.getItem(2).getFile().getID());
    assertEquals("3", QueueModel.getItem(3).getFile().getID());

  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#insert(java.util.List, int)}.
   */

  public void testInsertListOfStackItemInt() throws Exception {
    assertEquals(0, QueueModel.getQueueSize());

    // tested with addItems
    addItems(256);

    assertEquals(256, QueueModel.getQueueSize());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#up(int)}.
   */

  public void testUp() throws Exception {
    // first one cannot be put up, returns immediately
    QueueModel.up(0);

    addItems(3);

    // check queue
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("0", QueueModel.getItem(0).getFile().getID());
    assertEquals("1", QueueModel.getItem(1).getFile().getID());
    assertEquals("2", QueueModel.getItem(2).getFile().getID());

    // now up one
    QueueModel.up(2);

    // check queue after move
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("0", QueueModel.getItem(0).getFile().getID());
    assertEquals("2", QueueModel.getItem(1).getFile().getID());
    assertEquals("1", QueueModel.getItem(2).getFile().getID());

    // up once more
    QueueModel.up(1);

    // check queue
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("2", QueueModel.getItem(0).getFile().getID());
    assertEquals("0", QueueModel.getItem(1).getFile().getID());
    assertEquals("1", QueueModel.getItem(2).getFile().getID());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#down(int)}.
   */

  public void testDown() throws Exception {
    // first one cannot be put up, returns immediately
    QueueModel.down(0);

    addItems(3);

    // check queue
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("0", QueueModel.getItem(0).getFile().getID());
    assertEquals("1", QueueModel.getItem(1).getFile().getID());
    assertEquals("2", QueueModel.getItem(2).getFile().getID());

    // now up one
    QueueModel.down(0);

    // check queue after move
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals(QueueModel.getQueue().toString(), "1", QueueModel.getItem(0).getFile().getID());
    assertEquals(QueueModel.getQueue().toString(), "0", QueueModel.getItem(1).getFile().getID());
    assertEquals(QueueModel.getQueue().toString(), "2", QueueModel.getItem(2).getFile().getID());

    // up once more
    QueueModel.down(1);

    // check queue
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("1", QueueModel.getItem(0).getFile().getID());
    assertEquals("2", QueueModel.getItem(1).getFile().getID());
    assertEquals("0", QueueModel.getItem(2).getFile().getID());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#goTo(int)}.
   */

  public void testGoTo() throws Exception {
    QueueModel.goTo(0);

    addItems(5);
    QueueModel.setIndex(2);
    QueueModel.playNext();

    QueueModel.goTo(4);

    assertEquals("4", QueueModel.getCurrentItem().getFile().getID());
  }

  public void testGoToRepeat() throws Exception {
    addItems(5);
    QueueModel.setIndex(2);
    QueueModel.playNext();

    { // first choose one that is not set to repeat
      // now set some repeat
      QueueModel.getItem(2).setRepeat(true);

      QueueModel.goTo(4);

      assertEquals("4", QueueModel.getCurrentItem().getFile().getID());

      // item 2 is now not repeated any more
      assertFalse(QueueModel.getItem(2).isRepeat());
    }

    { // and then try to go to a repeated one

      // now set some repeat
      QueueModel.getItem(2).setRepeat(true);

      QueueModel.goTo(2);

      assertEquals("2", QueueModel.getCurrentItem().getFile().getID());

      // item 2 is now still repeated
      assertTrue(QueueModel.getItem(2).isRepeat());
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#remove(int, int)}.
   */

  public void testRemove() throws Exception {
    QueueModel.remove(0, 0);

    addItems(10);

    QueueModel.remove(1, 3);

    assertEquals(QueueModel.getQueue().toString(), 7, QueueModel.getQueueSize());
  }

  public void testRemovePlanned() throws Exception {
    // now add some items
    addItems(5);

    // we also need to enable continuous play for tracks to be planned
    Conf.setProperty(Const.CONF_STATE_CONTINUE, "true");

    // register some file
    File file = getFile(11);
    FileManager.getInstance().registerFile(file.getID(), file.getName(), file.getDirectory(),
        file.getTrack(), file.getSize(), file.getQuality());
    file = getFile(12);
    FileManager.getInstance().registerFile(file.getID(), file.getName(), file.getDirectory(),
        file.getTrack(), file.getSize(), file.getQuality());

    QueueModel.computesPlanned(false);

    // now we have planned items
    assertEquals(10, QueueModel.getPlanned().size());

    QueueModel.remove(5, 6);

    // still 5 queue items
    assertEquals(QueueModel.getQueue().toString(), 5, QueueModel.getQueueSize());

    // again planned items are added now
    assertEquals(QueueModel.getPlanned().toString(), 10, QueueModel.getPlanned().size());
    // assertEquals("12", QueueModel.getPlanned().get(0).getFile().getID());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#getLast()}.
   */

  public void testGetLast() throws Exception {
    assertNull(QueueModel.getLast());

    addItems(10);

    assertEquals("9", QueueModel.getLast().getFile().getID());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#getLastPlayed()}.
   */

  public void testGetLastPlayed() throws Exception {
    assertNull(QueueModel.getLastPlayed());

    addItems(10);

    QueueModel.playNext();

    // maybe we have one now
    assertNotNull(QueueModel.getLastPlayed());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#getIndex()}.
   */

  public void testGetIndex() {
    // tested by tests above
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#getCountTracksLeft()}.
   */

  public void testGetCountTracksLeft() throws Exception {
    assertEquals(0, QueueModel.getCountTracksLeft());

    addItems(10);

    assertEquals(10, QueueModel.getCountTracksLeft());

    QueueModel.playNext();
    QueueModel.playNext();

    assertEquals(8, QueueModel.getCountTracksLeft());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#getPlanned()}.
   */

  public void testGetPlanned() throws Exception {
    assertEquals(0, QueueModel.getPlanned().size());

    QueueModel.computesPlanned(false);

    // no tracks are planned when queue is empty
    assertEquals(0, QueueModel.getPlanned().size());

    // now add some items
    addItems(5);

    // still no items because default configration states to not continue play
    QueueModel.computesPlanned(false);
    assertEquals(0, QueueModel.getPlanned().size());

    // we also need to enable continuous play for tracks to be planned
    Conf.setProperty(Const.CONF_STATE_CONTINUE, "true");

    QueueModel.computesPlanned(false);

    // still no items because we don't have any files to plan
    QueueModel.computesPlanned(false);
    assertEquals(0, QueueModel.getPlanned().size());

    File file = getFile(11);
    FileManager.getInstance().registerFile(file.getID(), file.getName(), file.getDirectory(),
        file.getTrack(), file.getSize(), file.getQuality());

    QueueModel.computesPlanned(false);

    assertTrue(QueueModel.getPlanned().size() > 0);
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#setFirstFile(boolean)}.
   */

  public void testSetFirstFile() {
    QueueModel.setFirstFile(true);

    // no way to test the effect right now...
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#commit()}.
   */

  public void testCommit() throws Exception {
    JUnitHelpers.createSessionDirectory();

    final java.io.File fifo = SessionService.getConfFileByPath(Const.FILE_FIFO);
    fifo.delete();
    assertFalse(fifo.exists()); // we should not have the file now...

    addItems(10);

    QueueModel.commit();

    // now the file should exist and have some size
    assertTrue(fifo.exists());
    assertNotNull(FileUtils.readFileToString(fifo).length() > 0);
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#isPlayingRadio()}.
   */

  public void testIsPlayingRadio() {
    assertFalse(QueueModel.isPlayingRadio());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#getCurrentRadio()}.
   */

  public void testGetCurrentRadio() {
    assertNull(QueueModel.getCurrentRadio());
    QueueModel.launchRadio(new WebRadio("name", "invalidurl"));
    assertNotNull(QueueModel.getCurrentRadio());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#isPlayingTrack()}.
   */

  public void testIsPlayingTrack() throws Exception {
    assertTrue(QueueModel.isStopped());

    assertFalse(QueueModel.isPlayingTrack());

    addItems(3);
    QueueModel.playNext();

    assertTrue(QueueModel.isPlayingTrack());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#getCurrentFileTitle()}.
   */

  public void testGetCurrentFileTitle() throws Exception {
    // always returns some string, without file "Read to play"
    // can be wrong with different settings assertEquals("Ready to play", QueueModel.getCurrentFileTitle());
    assertNotNull(QueueModel.getCurrentFileTitle());

    addItems(3);
    QueueModel.playNext();

    assertNotNull(QueueModel.getCurrentFileTitle());
    // should not be the same as before
    assertFalse(QueueModel.getCurrentFileTitle().equals("Ready to play"));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#clean()}.
   */

  public void testClean() throws Exception {
    // wait a bit to let background-threads finish
    Thread.sleep(200);
    
    // should work without any items
    QueueModel.clean();

    addItems(10);
    assertEquals(10, QueueModel.getQueueSize());

    // right now, cleaning will remove all of them as we don't have the tracks
    // registered with the FileManager
    QueueModel.clean();

    assertEquals(0, QueueModel.getQueueSize());
  }

  // needs to be public to be callable from the outside...
  public static class MockPlayer implements IPlayerImpl {
    public void stop() throws Exception {

    }

    public void setVolume(float fVolume) throws Exception {

    }

    public void seek(float fPosition) {

    }

    public void resume() throws Exception {

    }

    public void play(WebRadio radio, float fVolume) throws Exception {

    }

    public void play(File file, float fPosition, long length, float fVolume) throws Exception {

    }

    public void pause() throws Exception {

    }

    public int getState() {

      return 0;
    }

    public long getElapsedTime() {

      return 0;
    }

    public float getCurrentVolume() {

      return 0;
    }

    public float getCurrentPosition() {

      return 0;
    }

    public long getCurrentLength() {

      return 0;
    }
  }
}
