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
package org.jajuk.services.players;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.File;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * .
 */
public class TestMPlayerPlayerImpl extends JajukTestCase {
  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    JUnitHelpers.waitForThreadToFinish("MPlayer reader thread");
    JUnitHelpers.waitForThreadToFinish("MPlayer writer thread");
    Log.info("Tearing down testcase");
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#stop()}.
   *
   * @throws Exception the exception
   */
  public void testStop() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file1", false);
      impl.play(file, 0, 2, 10);
      // sleep a bit to let threads do some work
      Thread.sleep(2000);
    } finally {
      impl.stop();
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#setVolume(float)}.
   *
   * @throws Exception the exception
   */
  public void testSetVolume() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file2", false);
      impl.play(file, 0, 2, 10);
      impl.setVolume(10);
      assertEquals(10.0, impl.getCurrentVolume(), 0.0001);
    } finally {
      impl.stop();
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#getCurrentLength()}.
   *
   * @throws Exception the exception
   */
  public void testGetCurrentLength() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file3", false);
      impl.play(file, 0, 2, 10);
      // returns duration from tag if there is one
      assertEquals(120000l, impl.getDurationSec());
    } finally {
      impl.stop();
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#getCurrentPosition()}.
   *
   * @throws Exception the exception
   */
  public void testGetCurrentPosition() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file4", false);
      impl.play(file, 0, 2, 10);
      //float vbrCorrection = 120000f / (DummyMPlayerImpl.LENGTH*1000);
      // returns duration from tag if there is one
      //assertEquals("Had: CurPos: " + impl.getCurrentPosition() + " Pos: " + DummyMPlayerImpl.POSITION + " Corr: " + vbrCorrection,
      //    DummyMPlayerImpl.POSITION * vbrCorrection / 120000f, impl.getCurrentPosition(), 0.0001f);
      // CurrentPos is related to: System.currentTimeMillis() - dateStart - pauseCount
      assertNotNull(impl.getCurrentPosition());
    } finally {
      impl.stop();
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#getElapsedTime()}.
   *
   * @throws Exception the exception
   */
  public void testGetElapsedTime() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file5", false);
      impl.play(file, 0, 2, 10);
      // returns duration from tag if there is one
      // assertEquals(DummyMPlayerImpl.POSITION, impl.getElapsedTime(), 0.0001f);
      // elapsed time is: System.currentTimeMillis() - dateStart - pauseCount;
      assertNotNull(impl.getElapsedTimeMillis());
    } finally {
      impl.stop();
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#play(org.jajuk.services.webradio.WebRadio, float)}.
   *
   * @throws Exception the exception
   */
  public void testPlayWebRadioFloat() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    WebRadio radio = JUnitHelpers.getWebRadio();
    impl.play(radio, 1);
    // does not really start anything here: impl.stop();
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#seek(float)}.
   *
   * @throws Exception the exception
   */
  public void testSeek() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file6", false);
      impl.play(file, 0, 2, 10);
      impl.seek(20);
      // wait a bit to let threads do some work
      Thread.sleep(2000);
    } finally {
      impl.stop();
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#getState()}.
   *
   * @throws Exception the exception
   */
  public void testGetState() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file7", false);
      impl.play(file, 0, 2, 10);
      assertEquals("Returns -1 when not fading.", -1, impl.getState());
    } finally {
      impl.stop();
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#resume()}.
   *
   * @throws Exception the exception
   */
  public void testResume() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file8", false);
      impl.play(file, 0, 2, 10);
      impl.pause();
      // sleep a bit to lead reader thread do some work
      Thread.sleep(2000);
      // also try to adjust volume here as it is handled differently
      impl.setVolume(1);
      impl.resume();
    } finally {
      impl.stop();
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.MPlayerPlayerImpl#play(org.jajuk.base.File, float, long, float)}.
   *
   * @throws Exception the exception
   */
  public void testPlayFileFloatLongFloat() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file9", false);
      impl.play(file, 0, 2, 10);
    } finally {
      impl.stop();
    }
  }

  /**
   * Test play environment variables.
   * 
   *
   * @throws Exception the exception
   */
  public void testPlayEnvironmentVariables() throws Exception {
    Conf.setProperty(Const.CONF_ENV_VARIABLES, "ENV1=23423 ENV2=23423");
    try {
      testPlayFileFloatLongFloat();
    } finally {
      Conf.setProperty(Const.CONF_ENV_VARIABLES, "");
    }
  }

  /**
   * Test play environment variables null.
   * 
   *
   * @throws Exception the exception
   */
  public void testPlayEnvironmentVariablesNull() throws Exception {
    Conf.setProperty(Const.CONF_ENV_VARIABLES, "ENV3= ENV4=123");
    try {
      // works, only reports an error to the logfile and ignores any following environment variables
      testPlayFileFloatLongFloat();
    } finally {
      Conf.setProperty(Const.CONF_ENV_VARIABLES, "");
    }
  }

  /**
   * Test play position.
   * 
   *
   * @throws Exception the exception
   */
  public void testPlayPosition() throws Exception {
    MPlayerPlayerImpl impl = new MPlayerPlayerImpl();
    try {
      File file = JUnitHelpers.getFile("file10", false);
      impl.play(file, 3, 20, 1);
    } finally {
      impl.stop();
    }
  }
}
