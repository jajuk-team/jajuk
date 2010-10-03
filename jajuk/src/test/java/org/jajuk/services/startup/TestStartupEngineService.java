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

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * Tests for org.jajuk.services.StartupEngineService
 */
public class TestStartupEngineService extends JajukTestCase {

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
    JUnitHelpers.getFile("file1", true);
    JUnitHelpers.getFile("file2", true);
    JUnitHelpers.getFile("file3", true);
   
  }

  public final void testNothing() {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_NOTHING);
    StartupEngineService.launchInitialTrack();
    assertEquals(QueueModel.getPlayingFile(), null);
  }

  public final void testLastItem() {

  }

  public final void testLastItemLastPos() {

  }

  public final void testNovelties() {

  }

  public final void testBesof() {

  }

  public final void testShuffle() {

  }

  /**
   * - Play last file / last pos mode
   * - User left jajuk playing the radio
   * Check that the radio is playing and that the queue is still there
   */
  public final void test1() {
    Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, "0.5");
    Conf.setProperty(Const.CONF_WEBRADIO_WAS_PLAYING, "true");

    StartupEngineService.launchInitialTrack();
    assertEquals(QueueModel.getPlayingFile(), null);
  }

}
