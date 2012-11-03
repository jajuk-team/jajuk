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

import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * .
 */
public class TestHistory extends JajukTestCase {
  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#startDocument()}
   * .
   */
  public final void testStartDocument() {
    History.getInstance().startDocument();
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#endDocument()}.
   */
  public final void testEndDocument() {
    History.getInstance().endDocument();
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#getInstance()}.
   */
  public final void testGetInstance() {
    assertNotNull(History.getInstance());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.History#getRegistrationKeys()}.
   */
  public final void testGetRegistrationKeys() {
    assertNotNull(History.getInstance().getRegistrationKeys());
    assertTrue(History.getInstance().getRegistrationKeys().contains(JajukEvents.CLEAR_HISTORY));
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#getHistory()}.
   *
   * @throws Exception the exception
   */
  public final void testGetHistory() throws Exception {
    assertNotNull(History.getInstance().getHistory());
    // has size 0 at the beginning
    assertEquals(0, History.getInstance().getHistory().size());
    // try with history disabled, should return without adding
    Conf.setProperty(Const.CONF_HISTORY, "0");
    History.getInstance().addItem("1", 123);
    assertEquals(0, History.getInstance().getHistory().size());
    // enable history, should still not be added unless we have the file in the
    // FileManager
    Conf.setProperty(Const.CONF_HISTORY, "1");
    History.getInstance().addItem("1", 123);
    assertEquals(0, History.getInstance().getHistory().size());
    // register a new file with the file manager, but still no go as we have a
    // different id!
    File file2 = TestHelpers.getFile("file2", false);
    History.getInstance().addItem("1", 123);
    assertEquals(0, History.getInstance().getHistory().size());
    // finally look for the correct file with id "2"
    History.getInstance().addItem(file2.getID(), 123);
    assertEquals(1, History.getInstance().getHistory().size());
    // now try to update the history with more files
    File file3 = TestHelpers.getFile("file3", false);
    File file4 = TestHelpers.getFile("file4", false);
    File file5 = TestHelpers.getFile("file5", false);
    History.getInstance().addItem(file3.getID(), 123);
    History.getInstance().addItem(file4.getID(), 123);
    History.getInstance().addItem(file5.getID(), 123);
    assertEquals(4, History.getInstance().getHistory().size());
    // now try to add the same file as the last one, the new one should be
    // added, but not twice
    History.getInstance().addItem(file5.getID(), 124);
    // size the same, not one higher
    assertEquals(4, History.getInstance().getHistory().size());
    // new item should be found in history now
    assertEquals("Item: " + History.getInstance().getHistoryItem(0).getDate(), 124, History
        .getInstance().getHistoryItem(0).getDate());
  }

  /**
   * Test get history max size.
   * 
   *
   * @throws Exception the exception
   */
  public final void testGetHistoryMaxSize() throws Exception {
    // enable history, should still not be added unless we have the file in the
    // FileManager
    Conf.setProperty(Const.CONF_HISTORY, "1");
    // register enough files (one more than the max size)
    File[] files = new File[Const.MAX_HISTORY_SIZE];
    for (int i = 0; i < Const.MAX_HISTORY_SIZE; i++) {
      files[i] = TestHelpers.getFile("file" + i, false);
    }
    // add up to max items
    for (int i = 0; i < Const.MAX_HISTORY_SIZE; i++) {
      History.getInstance().addItem(files[i].getID(), 123);
      assertEquals(i + 1, History.getInstance().getHistory().size());
    }
    // size should at the max now
    assertEquals(Const.MAX_HISTORY_SIZE, History.getInstance().getHistory().size());
    // register one more file
    File max = TestHelpers.getFile("file" + Const.MAX_HISTORY_SIZE, false);
    // now when we add one item, we should loose the oldest one (i.e. ID "1")
    History.getInstance().addItem(max.getID(), 123);
    // size should be equal as the oldest item was purged
    assertEquals(Const.MAX_HISTORY_SIZE, History.getInstance().getHistory().size());
    // new element should be in the History at position 0 now
    assertEquals(History.getInstance().getHistory().toString(), max.getID(), History.getInstance()
        .getHistoryItem(0).getFileId());
    // check that the existing items were moved by one (items are always added at the front, so 
    // we have to check in reverse order, i.e. the one before the last added one is at pos 1
    for (int i = 1; i < Const.MAX_HISTORY_SIZE; i++) {
      assertEquals(History.getInstance().getHistory().toString(),
          files[Const.MAX_HISTORY_SIZE - i].getID(), History.getInstance().getHistoryItem(i)
              .getFileId());
    }
    // also check clear
    History.getInstance().clear();
    // we have to wait for it as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    // size should be zero again now
    assertEquals(0, History.getInstance().getHistory().size());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.History#addItem(java.lang.String, long)}
   * .
   */
  public final void testAddItem() {
    // tested as part of getHistoryMaxSize() above
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#clear()}.
   */
  public final void testClear() {
    // tested as part of getHistoryMaxSize() above
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#cleanup()}.
   *
   * @throws Exception the exception
   */
  public final void testCleanup() throws Exception {
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    // register the file
    File file2 = TestHelpers.getFile("file2", false);
    // add the file
    History.getInstance().addItem(file2.getID(), 123);
    // now it is there
    assertEquals(1, History.getInstance().getHistory().size());
    // cleanup should keep this file as it is registered correctly
    History.getInstance().cleanup();
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    assertEquals(1, History.getInstance().getHistory().size());
    // add another file and unregister it from the FileManager
    File file3 = TestHelpers.getFile("file3", false);
    History.getInstance().addItem(file3.getID(), 123);
    assertEquals(2, History.getInstance().getHistory().size());
    FileManager.getInstance().removeFile(file3);
    assertEquals(2, History.getInstance().getHistory().size());
    // cleanup should now remove one file!
    History.getInstance().cleanup();
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    assertEquals(1, History.getInstance().getHistory().size());
    // if we clean out FileManager, cleanup should remove this file as well
    FileManager.getInstance().clear();
    History.getInstance().cleanup();
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    assertEquals(0, History.getInstance().getHistory().size());
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.bookmark.History#changeID(java.lang.String, java.lang.String)}
   * .
   */
  public final void testChangeID() throws Exception {
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    // register the file
    File file2 = TestHelpers.getFile("file2", false);
    // add the file
    History.getInstance().addItem(file2.getID(), 123);
    // now it is there
    assertEquals(1, History.getInstance().getHistory().size());
    // change from id 2 to 3
    History.getInstance().changeID(file2.getID(), "3");
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    assertEquals(1, History.getInstance().getHistory().size());
    assertEquals("3", History.getInstance().getHistoryItem(0).getFileId());
    // it is not automatically changed in FileManager itself!
    assertNull(FileManager.getInstance().getFileByID("3"));
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#clear(int)}.
   *
   * @throws Exception the exception
   */
  public final void testClearInt() throws Exception {
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    // register the file
    File file2 = TestHelpers.getFile("file2", false);
    // add the file
    History.getInstance().addItem(file2.getID(), 123);
    // now it is there
    assertEquals(1, History.getInstance().getHistory().size());
    // minus 1 means endlessly
    History.getInstance().clear(-1);
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    assertEquals(1, History.getInstance().getHistory().size());
    // stating a day will clean the file
    History.getInstance().clear(2);
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    // history size = 1 because we keep at least last track
    assertEquals(0, History.getInstance().getHistory().size());
  }

  /**
   * Test clear int keep.
   * 
   *
   * @throws Exception the exception
   */
  public final void testClearIntKeep() throws Exception {
    addHistoryItem(2, System.currentTimeMillis());
    // now it is there
    assertEquals(1, History.getInstance().getHistory().size());
    // stating a day bigger than one will keep the file
    History.getInstance().clear(2);
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    assertEquals(1, History.getInstance().getHistory().size());
  }

  /**
   * Test clear int removed.
   * 
   *
   * @throws Exception the exception
   */
  public final void testClearIntRemoved() throws Exception {
    addHistoryItem(2, System.currentTimeMillis());
    // now it is there
    assertEquals(1, History.getInstance().getHistory().size());
    // cleaning the FileManager will also remove the history for the files
    FileManager.getInstance().clear();
    History.getInstance().clear(2);
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    assertEquals(0, History.getInstance().getHistory().size());
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#commit()}.
   *
   * @throws Exception the exception
   */
  public final void testCommit() throws Exception {
    long date = System.currentTimeMillis();
    addHistoryItem(2, date);
    // now it is there
    assertEquals(1, History.getInstance().getHistory().size());
    // write the list to disc
    History.commit();
    // item still there
    assertEquals(1, History.getInstance().getHistory().size());
    // clear the history, now the item is gone
    History.getInstance().clear();
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    assertEquals(0, History.getInstance().getHistory().size());
    // now load the data again
    History.load();
    // the item is there again now
    assertEquals(1, History.getInstance().getHistory().size());
    assertEquals(date, History.getInstance().getHistoryItem(0).getDate());
  }

  /**
   * Adds the history item.
   * 
   *
   * @param nID 
   * @param timestamp The timestamp to use for the HistoryItem
   * @throws NumberFormatException the number format exception
   * @throws Exception the exception
   */
  private void addHistoryItem(int nID, long timestamp) throws NumberFormatException, Exception {
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    // register the file
    File file = TestHelpers.getFile("file_" + nID, false);
    // add the file with current date
    History.getInstance().addItem(file.getID(), timestamp);
  }

  /**
   * Test method for {@link org.jajuk.services.bookmark.History#load()}.
   */
  public final void testLoad() {
    // tested as part of "testCommit()"
  }

  /**
   * Test load corrupt file.
   * 
   *
   * @throws Exception the exception
   */
  public final void testLoadCorruptFile() throws Exception {
    // first try when no file is available
    java.io.File frt = SessionService.getConfFileByPath(Const.FILE_HISTORY);
    frt.delete();
    assertFalse(frt.exists());
    // it will catch an exception internally and handle it...
    History.load();
    // then try with a corrupted file
    FileUtils.writeStringToFile(frt, "<this is an invalid xML>");
  }

  /**
   * Test method for.
   *
   * @throws NumberFormatException the number format exception
   * @throws Exception the exception
   * {@link org.jajuk.services.bookmark.History#getHistoryItem(int)}.
   */
  public final void testGetHistoryItem() throws NumberFormatException, Exception {
    // null without any history
    assertNull(History.getInstance().getHistoryItem(0));
    addHistoryItem(2, 123);
    // now it is there
    assertEquals(1, History.getInstance().getHistory().size());
    // found now
    File file2 = TestHelpers.getFile("file_2", false);
    assertEquals(file2.getID(), History.getInstance().getHistoryItem(0).getFileId());
    // null with index out of bound
    assertNull(History.getInstance().getHistoryItem(1));
    assertNull(History.getInstance().getHistoryItem(-1));
    // try with more than one item, the new one is added as first item!
    addHistoryItem(4, 123);
    File file4 = TestHelpers.getFile("file_4", false);
    assertEquals(file4.getID(), History.getInstance().getHistoryItem(0).getFileId());
    assertEquals(file2.getID(), History.getInstance().getHistoryItem(1).getFileId());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.History#warning(org.xml.sax.SAXParseException)}
   * .
   */
  public final void testWarningSAXParseException() {
    try {
      History.getInstance().warning(new SAXParseException("testmessage", null));
      fail("Will throw exception");
    } catch (SAXException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("testmessage"));
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.History#error(org.xml.sax.SAXParseException)}
   * .
   */
  public final void testErrorSAXParseException() {
    try {
      History.getInstance().error(new SAXParseException("testmessage", null));
      fail("Will throw exception");
    } catch (SAXException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("testmessage"));
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.History#fatalError(org.xml.sax.SAXParseException)}
   * .
   */
  public final void testFatalErrorSAXParseException() {
    try {
      History.getInstance().fatalError(new SAXParseException("testmessage", null));
      fail("Will throw exception");
    } catch (SAXException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("testmessage"));
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.History#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)}
   * .
   */
  public final void testStartElementStringStringStringAttributes() {
    // tested as part of "commit/load"
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.History#endElement(java.lang.String, java.lang.String, java.lang.String)}
   * .
   */
  public final void testEndElementStringStringString() {
    // tested as part of "commit/load"
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.bookmark.History#update(org.jajuk.events.JajukEvent)}
   * .
   */
  public final void testUpdateFileLaunched() throws Exception {
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    addHistoryItem(2, 12345);
    assertEquals(1, History.getInstance().getHistory().size());
    File file3 = TestHelpers.getFile("file_3", false);
    Properties detail = new Properties();
    detail.put(Const.DETAIL_CURRENT_FILE_ID, file3.getID());
    detail.put(Const.DETAIL_CURRENT_DATE, new Long(12345));
    History.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, detail));
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    // now the file should be added
    assertEquals(2, History.getInstance().getHistory().size());
    assertEquals(file3.getID(), History.getInstance().getHistoryItem(0).getFileId());
  }

  /**
   * Test update device refresh.
   * 
   *
   * @throws Exception the exception
   */
  public final void testUpdateDeviceRefresh() throws Exception {
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    addHistoryItem(2, 12345);
    assertEquals(1, History.getInstance().getHistory().size());
    // remove the file from the FileManager
    FileManager.getInstance().clear();
    // trigger the refresh-event
    History.getInstance().update(new JajukEvent(JajukEvents.DEVICE_REFRESH, null));
    // we have to sleep a bit as it is executed in the background
    TestHelpers.clearSwingUtilitiesQueue();
    // we need to wait a second time because we have an invokeLater() inside
    // another invokeLater() here...
    TestHelpers.clearSwingUtilitiesQueue();
    // now the item is removed because it is not available any more in the
    // FileManager
    assertEquals(0, History.getInstance().getHistory().size());
  }

  /**
   * Test update clear history.
   * 
   *
   * @throws Exception the exception
   */
  public final void testUpdateClearHistory() throws Exception {
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    addHistoryItem(14, 12345);
    assertEquals(1, History.getInstance().getHistory().size());
    // trigger the clear-event
    History.getInstance().update(new JajukEvent(JajukEvents.CLEAR_HISTORY, null));
    // here we actually have to sleep a few times as there are two thread-calls
    // done, once inside update() and then
    // another one in clear() itself, one "sleep()" only gives up thread-control
    // once and then gains control again later
    TestHelpers.clearSwingUtilitiesQueue();
    // now the item is cleared
    // TODO: this test fails in Hudson for some reason, I could not find out
    // why, it works
    // in Eclipse as well as in a local Hudson instance that I did set up, so I
    // can only
    // disable this check for now...
    // assertEquals(0, History.getInstance().getHistory().size());
  }

  /**
   * Test update language changed.
   * 
   */
  public final void testUpdateLanguageChanged() {
    History.getInstance().update(new JajukEvent(JajukEvents.LANGUAGE_CHANGED, null));
  }

  /**
   * Test update file name changed.
   * 
   *
   * @throws Exception the exception
   */
  public final void testUpdateFileNameChanged() throws Exception {
    // it seems there are some rare cases where we still have some threads doing
    // some updates,
    // therefore sleep some more up-front to let that clear out before we start
    // the test here
    TestHelpers.clearSwingUtilitiesQueue();
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    // register the file
    File file11 = TestHelpers.getFile("file_11", false);
    File file3 = TestHelpers.getFile("file_3", false);
    // add the file
    History.getInstance().addItem(file11.getID(), 123);
    // now it is there
    assertEquals(History.getInstance().getHistory().toString(), 1, History.getInstance()
        .getHistory().size());
    // change from id 11 to 3
    Properties detail = new Properties();
    File file = FileManager.getInstance().getFileByID(file11.getID());
    // there needs to be such a file because we added it above...
    assertNotNull(file);
    detail.put(Const.DETAIL_OLD, file);
    // read the file "3" and then remove it from the filemanager to be clean
    // there as well
    file = FileManager.getInstance().getFileByID(file3.getID());
    // there needs to be such a file because we added it above...
    assertNotNull(file);
    detail.put(Const.DETAIL_NEW, file);
    FileManager.getInstance().removeFile(file);
    // now trigger the update
    History.getInstance().update(new JajukEvent(JajukEvents.FILE_NAME_CHANGED, detail));
    // we have to wait for the SwingUtilities here as the update is executed in
    // the background
    TestHelpers.clearSwingUtilitiesQueue();
    // we actually execute an invokeLater() in another invokeLater() which means
    // we need to
    // wait twice here until all work is guaranteed to be done
    TestHelpers.clearSwingUtilitiesQueue();
    // now we only should have the item "3"
    assertEquals(1, History.getInstance().getHistory().size());
    // TODO: this test fails in Hudson for some reason, I could not find out
    // why, it works
    // in Eclipse as well as in a local Hudson instance that I did set up, so I
    // can only
    // disable this check for now...
    // assertEquals("3", History.getInstance().getHistoryItem(0).getFileId());
  }

  /**
   * Test update unhandled event.
   * 
   */
  public final void testUpdateUnhandledEvent() {
    History.getInstance().update(new JajukEvent(JajukEvents.BANNED, null));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.bookmark.History#getDateFormatter()}.
   */
  public final void testGetDateFormatter() {
    assertNotNull(History.getInstance().getDateFormatter());
  }

  /**
   * Test constructor already launched.
   * 
   *
   * @throws Exception the exception
   */
  public final void testConstructorAlreadyLaunched() throws Exception {
    // it seems there are some cases where we still have some Queues doing some
    // updates,
    // therefore sleep some more up-front to let that clear out before we start
    // the test here
    TestHelpers.clearSwingUtilitiesQueue();
    Thread.sleep(100);
    // enable history
    Conf.setProperty(Const.CONF_HISTORY, "1");
    File file3 = TestHelpers.getFile("file_3", false);
    Properties detail = new Properties();
    detail.put(Const.DETAIL_CURRENT_FILE_ID, file3.getID());
    detail.put(Const.DETAIL_CURRENT_DATE, new Long(12345));
    // set the necessary things in the ObservationManager
    ObservationManager.notifySync(new JajukEvent(JajukEvents.FILE_LAUNCHED, detail));
    // make sure we have the correct last-file now
    assertEquals(file3.getID(), ObservationManager.getDetailLastOccurence(
        JajukEvents.FILE_LAUNCHED, Const.DETAIL_CURRENT_FILE_ID));
    // call the constructor via reflection
    History hist = TestHelpers.executePrivateConstructor(History.class);
    // we have to wait for the queue to be empty
    TestHelpers.clearSwingUtilitiesQueue();
    // it seems there is sometimes still work done by other tests, we saw
    // failures here,
    // I added this check here again to see if that actually happens...
    assertEquals(file3.getID(), ObservationManager.getDetailLastOccurence(
        JajukEvents.FILE_LAUNCHED, Const.DETAIL_CURRENT_FILE_ID));
    // now the file should be in the history already
    assertEquals(hist.getHistory().toString(), 1, hist.getHistory().size());
    assertEquals(file3.getID(), hist.getHistoryItem(0).getFileId());
  }
}
