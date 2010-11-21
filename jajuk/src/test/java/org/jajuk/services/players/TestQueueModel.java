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

import org.apache.commons.io.FileUtils;
import org.jajuk.JUnitHelpers;
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
import org.jajuk.services.core.SessionService;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.error.JajukException;

/**
 * 
 */
public class TestQueueModel extends JajukTestCase {

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
    QueueModel.itemLast = null;
    QueueModel.stopRequest();

    // reset conf changes to default
    Conf.setProperty(Const.CONF_STATE_CONTINUE, "false");
    Conf.setProperty(Const.CONF_DROP_PLAYED_TRACKS_FROM_QUEUE, "false");
    Conf.setProperty(Const.CONF_STATE_CONTINUE, "false");
    Conf.setProperty(Const.CONF_STATE_SHUFFLE, "false");

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
    // make sure that the SwingUtilities.invokeLater() are all done
    JUnitHelpers.clearSwingUtilitiesQueue();

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
    QueueModel.goTo(4);
    QueueModel.reset();
    assertEquals(0, QueueModel.getQueueSize());
    assertEquals(-1, QueueModel.getIndex());
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
    list.add(new StackItem(JUnitHelpers.getFile("file1", true)));

    QueueModel.push(list, true);

    // we try to wait for the thread started inside push() to finish
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(1, QueueModel.getQueue().size());
  }

  public void testPushListOfStackItemBooleanNoPush() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(JUnitHelpers.getFile("file1", true)));

    QueueModel.push(list, false);

    // we try to wait for the thread started inside push() to finish
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(1, QueueModel.getQueue().size());
  }

  /**
   * Check right behavior when pushing void list of items.
   * If run with GUI, you should get a warning popup
   * @throws Exception
   */
  public void testPushListOfStackItemVoid() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    QueueModel.push(list, false);

    // there is a thread started, so delay a bit to let that happen...
    Thread.sleep(400);

    assertEquals(0, QueueModel.getQueue().size());
  }

  public void testPushListOfStackItemBooleanNullItems() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(JUnitHelpers.getFile("file1", true)));
    list.add(null);
    list.add(new StackItem(JUnitHelpers.getFile("file3", true)));

    QueueModel.push(list, true);

    // we try to wait for the thread started inside push() to finish
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(2, QueueModel.getQueue().size());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#push(java.util.List, boolean, boolean)}
   * .
   */

  public void testPushListOfStackItemBooleanBoolean() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(JUnitHelpers.getFile("file1", true)));

    QueueModel.push(list, true, true);

    // we try to wait for the thread started inside push() to finish
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(1, QueueModel.getQueue().size());
  }

  public void testPushListOfStackItemBooleanBooleanNoPushNext() throws Exception {
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(JUnitHelpers.getFile("file1", true)));

    QueueModel.push(list, false, false);

    // we try to wait for the thread started inside push() to finish
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(1, QueueModel.getQueue().size());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#push(org.jajuk.services.players.StackItem, boolean)}
   * .
   */

  public void testPushStackItemBoolean() throws Exception {
    QueueModel.push(new StackItem(JUnitHelpers.getFile("file1", true)), true);

    // we try to wait for the thread started inside push() to finish
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

    assertEquals(1, QueueModel.getQueue().size());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#push(org.jajuk.services.players.StackItem, boolean, boolean)}
   * .
   */

  public void testPushStackItemBooleanBoolean() throws Exception {
    QueueModel.push(new StackItem(JUnitHelpers.getFile("file1", true)), true, true);

    // we try to wait for the thread started inside push() to finish
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

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
      list.add(new StackItem(JUnitHelpers.getFile("file" + i, true)));
    }
    QueueModel.insert(list, 0);
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
    QueueModel.goTo(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.finished();
    assertEquals(1, QueueModel.getIndex());
  }

  /**
  * Test method for
  * {@link org.jajuk.services.players.QueueModel#finished()}.
  * Test for feature #1441 (Repeat all shuffle mode) : in repeat 
  * all mode + shuffle mode, queue should be shuffled when reaching its end
  */
  public void testFinishedRepeatAndShuffle() throws Exception {
    QueueModel.clear();
    addItems(5);
    StackItem firstItem = QueueModel.getItem(0);
    QueueModel.setRepeatModeToAll(true);
    Conf.setProperty(Const.CONF_STATE_REPEAT, "false");
    Conf.setProperty(Const.CONF_STATE_REPEAT_ALL, "true");
    Conf.setProperty(Const.CONF_STATE_SHUFFLE, "true");
    assertTrue(QueueModel.containsOnlyRepeat());
    QueueModel.finished();
    QueueModel.finished();
    QueueModel.finished();
    QueueModel.finished();
    assertTrue(QueueModel.getItem(0).equals(firstItem));
    QueueModel.finished();
    // Make sure that first item is no more the same
    assertFalse("Item0: " + QueueModel.getItem(0) + "\nFirstItem: " + firstItem, !(QueueModel
        .getItem(0).equals(firstItem)));
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#finished(boolean)}.
   */
  public void testFinishedBoolean() throws Exception {
    // without item it just returns
    QueueModel.finished(true);

    // with items, it will go to the next line
    addItems(10);
    QueueModel.goTo(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.finished(true);
    assertEquals(1, QueueModel.getIndex());

    // still 10 as we do not remove items from queue in default setup
    assertEquals(10, QueueModel.getQueueSize());
  }

  public void testFinishedBooleanRemovePlayed() throws Exception {
    // set config option that we want to test
    Conf.setProperty(Const.CONF_DROP_PLAYED_TRACKS_FROM_QUEUE, "true");

    // without item it just returns
    QueueModel.finished(true);

    // with items, it will go to the next line
    addItems(10);
    QueueModel.goTo(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.finished(true);
    assertEquals(0, QueueModel.getIndex());

    // here we should have 9 now...
    assertEquals(9, QueueModel.getQueueSize());
  }

  public void testFinishedEndOfQueueNoPlanned() throws Exception {
    // without item it just returns
    QueueModel.finished(true);

    // with items, it will go to the next line
    addItems(2);
    QueueModel.goTo(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.finished(true);
    assertEquals(1, QueueModel.getIndex());

    // still 2 as we do not remove items from queue in default setup
    assertEquals(2, QueueModel.getQueueSize());

    // next time it will reset the index as we do not "plan" new tracks automatically
    QueueModel.finished(true);
    assertEquals(-1, QueueModel.getIndex());
  }

  public void testFinishedEndOfQueueWithPlanned() throws Exception {
    Conf.setProperty(Const.CONF_STATE_CONTINUE, "true");
    // without item it just returns
    QueueModel.finished(true);

    // with items, it will go to the next line
    addItems(2);
    QueueModel.goTo(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.finished(true);
    assertEquals(1, QueueModel.getIndex());

    // still 2 as we do not remove items from queue in default setup
    assertEquals(2, QueueModel.getQueueSize());

    { // start a track
      List<StackItem> list = new ArrayList<StackItem>();
      list.add(new StackItem(JUnitHelpers.getFile("file" + 21, true)));

      QueueModel.push(list, true);

      // we try to wait for the thread started inside push() to finish
      JUnitHelpers.waitForThreadToFinish("Queue Push Thread");

      assertEquals(3, QueueModel.getQueue().size());
    }

    // one more to finish now
    QueueModel.finished(true);

    // next time it will reset the index as we do not "plan" new tracks automatically
    QueueModel.finished(true);
    assertEquals(0, QueueModel.getIndex());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#finished(boolean)}.
   */
  public void testFinishedRepeatSingleItem() throws Exception {
    addItems(1);
    StackItem si = QueueModel.getItem(0);
    si.setRepeat(true);

    QueueModel.goTo(0);
    // Finish the track, should play again
    QueueModel.finished();

    StackItem newSi = QueueModel.getItem(0);
    assertEquals(0, QueueModel.getIndex());
    assertTrue(newSi.isRepeat());
    assertEquals(newSi, QueueModel.getCurrentItem());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#finished(boolean)}.
   */
  public void testFinishedRepeatLastItem() throws Exception {
    // We want to make sure that everything's ok when current item is in repeat
    // mode and the last in the queue
    addItems(10);
    StackItem si = QueueModel.getItem(9);
    si.setRepeat(true);
    QueueModel.goTo(9);
    // Finish the track, should play again
    QueueModel.finished();

    // same track should be played again as it is in repeat mode and the first
    // one at index 0 is not
    StackItem newSi = QueueModel.getCurrentItem();
    assertEquals(newSi, si);
    assertTrue(newSi.isRepeat());

    // Now the same with first track in repeat mode
    QueueModel.getItem(0).setRepeat(true);
    QueueModel.finished();
    newSi = QueueModel.getCurrentItem();
    assertEquals(newSi, QueueModel.getItem(0));
    assertTrue(newSi.isRepeat());
    assertTrue(si.isRepeat());
  }

  public void testFinishedRepeatLastItemNotLast() throws Exception {
    // We want to make sure that everything's ok when current item is in repeat
    // mode and the last in the queue
    addItems(10);
    StackItem si = QueueModel.getItem(5);
    si.setRepeat(true);
    QueueModel.goTo(5);
    // Finish the track, should play again
    QueueModel.finished();

    // same track should be played again as it is in repeat mode and the first
    // one at index 0 is not
    StackItem newSi = QueueModel.getCurrentItem();
    assertEquals(newSi, si);
    assertTrue(newSi.isRepeat());

    // Now the same with first track in repeat mode
    QueueModel.getItem(6).setRepeat(true);
    QueueModel.finished();
    newSi = QueueModel.getCurrentItem();
    assertEquals(newSi, QueueModel.getItem(6));
    assertTrue(newSi.isRepeat());
    assertTrue(si.isRepeat());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#finished(boolean)}.
   */
  public void testFinishedRepeatNotLastItem() throws Exception {
    // We want to make sure that everything's ok when current item is in repeat
    // mode and *not* the last in the queue
    addItems(10);
    StackItem si = QueueModel.getItem(5);
    si.setRepeat(true);
    QueueModel.goTo(5);
    // Finish the track, should play again
    QueueModel.finished();

    // same track should be played again as it is in repeat mode and the first
    // one at index 0 is not
    StackItem newSi = QueueModel.getCurrentItem();
    assertEquals(newSi, si);
    assertTrue(newSi.isRepeat());

    // Now the same with first track in repeat mode
    QueueModel.getItem(6).setRepeat(true);
    QueueModel.finished();
    newSi = QueueModel.getCurrentItem();
    assertEquals(newSi, QueueModel.getItem(6));
    assertTrue(newSi.isRepeat());
    assertTrue(si.isRepeat());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#goTo(int)}
   * .
   */

  public void testSetAndGetIndex() throws Exception {
    // with items, it will go to the next ine
    addItems(10);
    QueueModel.goTo(0);
    assertEquals(0, QueueModel.getIndex());
    QueueModel.goTo(3);
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

  public void testComputesPlannedClear() throws Exception {
    Conf.setProperty(Const.CONF_STATE_CONTINUE, "true");

    // without tracks it will not do much, but it will hit the "clearPlanned"
    QueueModel.computesPlanned(true);
  }

  public void testComputesPlannedShuffle() throws Exception {
    // set Property to hit the "Shuffle" branch
    Conf.setProperty(Const.CONF_STATE_SHUFFLE, "true");

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
    QueueModel.goTo(4);
    QueueModel.clear();
    assertEquals(0, QueueModel.getQueueSize());
    assertEquals(-1, QueueModel.getIndex());
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
    QueueModel.goTo(2);
    QueueModel.playPrevious();
    assertEquals(1, QueueModel.getIndex());
  }

  public void testPlayPreviousAtZero() throws Exception {
    // do nothing without items
    QueueModel.playPrevious();

    // with items:
    addItems(10);
    QueueModel.goTo(0);
    QueueModel.playPrevious();
    assertEquals(0, QueueModel.getIndex());
  }

  public void testPlayPreviousAtZeroWithRepeat() throws Exception {
    // do nothing without items
    QueueModel.playPrevious();

    // with items:
    addItems(10);
    QueueModel.goTo(0);
    QueueModel.getItem(0).setRepeat(true);
    QueueModel.playPrevious();
    assertEquals(0, QueueModel.getIndex());
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
    QueueModel.goTo(2);
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
    assertEquals("file0", QueueModel.getPlayingFile().getName());
  }

  public void testGetPlayingFileTitle() throws Exception {
    assertNull(QueueModel.getPlayingFileTitle());

    addItems(10);
    // QueueModel.playNext();
    QueueModel.goTo(0);
    assertFalse(QueueModel.isStopped());
    assertNotNull(QueueModel.getPlayingFileTitle());
    // we start at 0
    assertTrue(QueueModel.getPlayingFileTitle(), QueueModel.getPlayingFileTitle().contains("file"));
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#getCurrentItem()}.
   */

  public void testGetCurrentItem() throws Exception {
    // no item without items
    assertNull(QueueModel.getCurrentItem());
    addItems(10);
    QueueModel.goTo(2);
    assertEquals("file2", QueueModel.getCurrentItem().getFile().getName());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#getItem(int)}.
   */

  public void testGetItem() throws Exception {
    addItems(10);
    assertEquals("file0", QueueModel.getItem(0).getFile().getName());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#canUnmount(org.jajuk.base.Device)}
   * .
   */

  public void testCanUnmount() throws Exception {
    Device device = JUnitHelpers.getDevice("anydevice", Device.Type.DIRECTORY, "/foo");
    assertTrue(QueueModel.canUnmount(device));

    addItems(10);

    // still true as we are not playing
    assertTrue(QueueModel.canUnmount(device));

    // try to start playing/planning
    QueueModel.playNext();
    assertFalse(QueueModel.canUnmount(QueueModel.getItem(1).getFile().getDevice()));
    assertTrue(QueueModel.canUnmount(device));
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
    assertEquals("file0", QueueModel.getItem(0).getFile().getName());
    assertEquals("file5", QueueModel.getItem(5).getFile().getName());
    assertEquals("file9", QueueModel.getItem(9).getFile().getName());

    QueueModel.shuffle();

    assertFalse("Queue: " + QueueModel.getQueue(), QueueModel.getItem(0).getFile().getName()
        .equals("file0")
        && QueueModel.getItem(5).getFile().getName().equals("file5")
        && QueueModel.getItem(9).getFile().getName().equals("file9"));
  }

  /**
   * Test method for
   * {@link org.jajuk.services.players.QueueModel#insert(org.jajuk.services.players.StackItem, int)}
   * .
   */

  public void testInsertStackItemInt() throws Exception {
    assertEquals(0, QueueModel.getQueueSize());
    QueueModel.insert(new StackItem(JUnitHelpers.getFile("file0", true)), 0);

    assertEquals(1, QueueModel.getQueueSize());

    // when we insert the next one at 0, the previous one should be moved
    QueueModel.insert(new StackItem(JUnitHelpers.getFile("file1", true)), 0);

    assertEquals(2, QueueModel.getQueueSize());
    assertEquals("file1", QueueModel.getItem(0).getFile().getName());
    assertEquals("file0", QueueModel.getItem(1).getFile().getName());

    // adding in between now, should again adjust the queue accordingly
    QueueModel.insert(new StackItem(JUnitHelpers.getFile("file2", true)), 1);

    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("file1", QueueModel.getItem(0).getFile().getName());
    assertEquals("file2", QueueModel.getItem(1).getFile().getName());
    assertEquals("file0", QueueModel.getItem(2).getFile().getName());

    // and adding at the end should work as well
    QueueModel.insert(new StackItem(JUnitHelpers.getFile("file3", true)), 3);

    assertEquals(4, QueueModel.getQueueSize());
    assertEquals("file1", QueueModel.getItem(0).getFile().getName());
    assertEquals("file2", QueueModel.getItem(1).getFile().getName());
    assertEquals("file0", QueueModel.getItem(2).getFile().getName());
    assertEquals("file3", QueueModel.getItem(3).getFile().getName());

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
    assertEquals("file0", QueueModel.getItem(0).getFile().getName());
    assertEquals("file1", QueueModel.getItem(1).getFile().getName());
    assertEquals("file2", QueueModel.getItem(2).getFile().getName());

    // now up one
    QueueModel.up(2);

    // check queue after move
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("file0", QueueModel.getItem(0).getFile().getName());
    assertEquals("file2", QueueModel.getItem(1).getFile().getName());
    assertEquals("file1", QueueModel.getItem(2).getFile().getName());

    // up once more
    QueueModel.up(1);

    // check queue
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("file2", QueueModel.getItem(0).getFile().getName());
    assertEquals("file0", QueueModel.getItem(1).getFile().getName());
    assertEquals("file1", QueueModel.getItem(2).getFile().getName());
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
    assertEquals("file0", QueueModel.getItem(0).getFile().getName());
    assertEquals("file1", QueueModel.getItem(1).getFile().getName());
    assertEquals("file2", QueueModel.getItem(2).getFile().getName());

    // now up one
    QueueModel.down(0);

    // check queue after move
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals(QueueModel.getQueue().toString(), "file1", QueueModel.getItem(0).getFile()
        .getName());
    assertEquals(QueueModel.getQueue().toString(), "file0", QueueModel.getItem(1).getFile()
        .getName());
    assertEquals(QueueModel.getQueue().toString(), "file2", QueueModel.getItem(2).getFile()
        .getName());

    // up once more
    QueueModel.down(1);

    // check queue
    assertEquals(3, QueueModel.getQueueSize());
    assertEquals("file1", QueueModel.getItem(0).getFile().getName());
    assertEquals("file2", QueueModel.getItem(1).getFile().getName());
    assertEquals("file0", QueueModel.getItem(2).getFile().getName());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#goTo(int)}.
   */

  public void testGoTo() throws Exception {
    QueueModel.goTo(0);
    addItems(5);
    QueueModel.goTo(2);
    QueueModel.goTo(4);
    assertEquals("file4", QueueModel.getCurrentItem().getFile().getName());
  }

  public void testGoToRepeat() throws Exception {
    addItems(5);
    QueueModel.goTo(2);

    { // first choose one that is not set to repeat
      // now set some repeat
      QueueModel.getItem(2).setRepeat(true);

      QueueModel.goTo(4);

      assertEquals("file4", QueueModel.getCurrentItem().getFile().getName());
    }

    { // and then try to go to a repeated one

      // now set some repeat
      QueueModel.getItem(2).setRepeat(true);

      QueueModel.goTo(2);

      assertEquals("file2", QueueModel.getCurrentItem().getFile().getName());

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

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#getLast()}.
   */

  public void testGetLast() throws Exception {
    assertNull(QueueModel.getLast());

    addItems(10);

    assertEquals("file9", QueueModel.getLast().getFile().getName());
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
    QueueModel.goTo(0);
    QueueModel.playNext();
    QueueModel.playNext();
    assertEquals(8, QueueModel.getCountTracksLeft());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueModel#getPlanned()}.
   */

  public void testGetPlanned() throws Exception {
    StartupCollectionService.registerItemManagers();

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

    assertTrue(QueueModel.getPlanned().size() > 0);
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
    // make sure we reset WebRadio
    QueueModel.launchRadio(null);

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
    // can be wrong with different settings assertEquals("Ready to play",
    // QueueModel.getCurrentFileTitle());
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
    // should work without any items
    QueueModel.clean();

    addItems(10);
    assertEquals(10, QueueModel.getQueueSize());

    // here clean will not remove things as they are correctly listed in the FileManager
    QueueModel.clean();

    assertEquals(10, QueueModel.getQueueSize());

    // we can add a dummy-file and check that it is removed
    Genre genre = JUnitHelpers.getGenre();
    Album album = JUnitHelpers.getAlbum("name", 23);
    album.setProperty(Const.XML_ALBUM_COVER, Const.COVER_NONE); // don't read covers for
    // this test

    Artist artist = JUnitHelpers.getArtist("name");
    Year year = JUnitHelpers.getYear(2000);

    Type type = JUnitHelpers.getType();
    Track track = TrackManager.getInstance().registerTrack("name", album, genre, artist, 120, year,
        1, type, 1);

    Device device = JUnitHelpers.getDevice();

    Directory dir = DirectoryManager.getInstance().registerDirectory(device);

    File file = FileManager.getInstance().registerFile("test.tst", dir, track, 120, 70);

    QueueModel.insert(new StackItem(file), 0);

    // now we have 11 elements
    assertEquals(11, QueueModel.getQueueSize());

    FileManager.getInstance().removeFile(file);

    // here clean will remove one item that is not listed in the FileManager
    QueueModel.clean();

    // should be 10 again now
    assertEquals(10, QueueModel.getQueueSize());
  }
}
