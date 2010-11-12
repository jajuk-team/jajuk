/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
package org.jajuk.services.startup;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.File;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * Tests for org.jajuk.services.StartupEngineService
 */
public class TestStartupEngineService extends JajukTestCase {

  private File file1;
  private File file2;
  private File file3;
  private WebRadio radio1;

  private static final float POSITION = 0.5f;

  /*
  * (non-Javadoc)
  * 
  * @see junit.framework.TestCase#setUp()
  */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Populate collection with a few files and associated items 
    // (automatically created with it)
    file1 = JUnitHelpers.getFile("file1", true);
    file2 = JUnitHelpers.getFile("file2", true);
    file3 = JUnitHelpers.getFile("file3", true);

    // Populate the history with these three files
    History.getInstance().addItem(file1.getID(), System.currentTimeMillis() - 120000);
    History.getInstance().addItem(file2.getID(), System.currentTimeMillis() - 110000);
    History.getInstance().addItem(file3.getID(), System.currentTimeMillis() - 100000);

    // Add last played radio
    radio1 = new WebRadio("myRadio", "http://foo.bar");
    WebRadioManager.getInstance().addWebRadio(radio1);
    Conf.setProperty(Const.CONF_DEFAULT_WEB_RADIO, "myRadio");

    // Populate FIFO
    java.io.File fifo = SessionService.getConfFileByPath(Const.FILE_FIFO);
    fifo.delete();
    BufferedWriter bw = new BufferedWriter(new FileWriter(fifo, false));
    bw.write(file1.getID() + "\n");
    bw.write(file2.getID() + "\n");
    bw.write(file3.getID() + "\n");
    bw.close();

    // Set others properties
    Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, POSITION + "");
    Conf.setProperty(Const.CONF_STARTUP_STOPPED, "false");
    Conf.setProperty(Const.CONF_STARTUP_ITEM, file3.getID());
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, "false");

    // Reset the queue
    QueueModel.reset();

    // Reset the Startup service
    Field alToPlay = StartupEngineService.class.getDeclaredField("alToPlay");
    alToPlay.setAccessible(true);
    alToPlay.set(null, new ArrayList<org.jajuk.base.File>());

    Field fileToPlay = StartupEngineService.class.getDeclaredField("fileToPlay");
    fileToPlay.setAccessible(true);
    fileToPlay.set(null, null);

    Field radio = StartupEngineService.class.getDeclaredField("radio");
    radio.setAccessible(true);
    radio.set(null, null);

    Field index = StartupEngineService.class.getDeclaredField("index");
    index.setAccessible(true);
    index.set(null, 0);

  }

  public final void testNothing() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_NOTHING);

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getPlayingFile(), null);

    // Check that queue is filled up
    assertTrue(QueueModel.getQueue().size() == 3);
  }

  public final void testLastItem() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST);

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getPlayingFile(), file3);
  }

  public final void testLastItemLastPos() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getPlayingFile(), file3);
    // Cannot test actual position, the mock player always return zero
    //assertTrue(Player.getCurrentPosition() >= POSITION);

  }

  public final void testNovelties() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_NOVELTIES);

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertTrue(QueueModel.isPlayingTrack());

  }

  public final void testBestof() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_BESTOF);

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertTrue(QueueModel.isPlayingTrack());
  }

  public final void testFirstSession() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_ITEM, "");
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    History.getInstance().clear();

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getPlayingFile(), null);
  }

  public final void testShuffle() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_SHUFFLE);

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertTrue(QueueModel.isPlayingTrack());
  }

  public final void testStoppedFile() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    Conf.setProperty(Const.CONF_STARTUP_STOPPED, "true");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertFalse(QueueModel.isPlayingRadio());
    assertFalse(QueueModel.isPlayingTrack());

    // Check that queue is filled up
    assertTrue(QueueModel.getQueue().size() == 3);
  }

  public final void testStoppedRadio() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    Conf.setProperty(Const.CONF_STARTUP_STOPPED, "true");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertFalse(QueueModel.isPlayingRadio());
    assertFalse(QueueModel.isPlayingTrack());
  }

  public final void testStartWebRadio() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, "true");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getCurrentRadio(), radio1);
  }

  /**
   * User selected a file to launch at startup 
   * @throws InterruptedException
   */
  public final void testStartGivenFile() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_ITEM);
    Conf.setProperty(Const.CONF_STARTUP_ITEM, SearchResultType.FILE.name() + "/" + file1.getID());

    // Radio was playing but we don't care, we should launch the file
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, "true");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getPlayingFile(), file1);

    // Same without playing radio
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, "false");
    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getPlayingFile(), file1);
  }

  /**
  * User selected a radio to launch at startup 
  * @throws InterruptedException
  */
  public final void testStartGivenRadio() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_ITEM);
    Conf.setProperty(Const.CONF_STARTUP_ITEM, SearchResultType.WEBRADIO.name() + "/"
        + radio1.getName());

    // Radio was playing but we don't care, we should launch the file
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, "true");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getCurrentRadio(), radio1);

    // Same without playing radio
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, "false");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getCurrentRadio(), radio1);
  }

  /**
  * User selected a radio to launch at startup but it leaved jajuk stopped 
  * @throws InterruptedException
  */
  public final void testStartGivenRadioStopped() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_ITEM);
    Conf.setProperty(Const.CONF_STARTUP_ITEM, SearchResultType.WEBRADIO.name() + "/"
        + radio1.getName());
    Conf.setProperty(Const.CONF_STARTUP_STOPPED, "true");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getCurrentRadio(), null);

  }

  /**
   * - Play last file / last position mode
   * - User left jajuk playing the radio
   * Check that the radio is playing and that the queue is still there
   */
  public final void test1() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, "true");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getPlayingFile(), null);
  }

  /**
   * Regression test for a 2010/11/01 bug :
   * If startup item is unset, last track doesn't work
   * @throws InterruptedException
   */
  public final void test2() throws InterruptedException {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    Conf.setProperty(Const.CONF_STARTUP_ITEM, "");

    StartupEngineService.launchInitialTrack();
    // Wait for track to be actually launched
    Thread.sleep(100);

    assertEquals(QueueModel.getPlayingFile(), file3);
  }

}
