/*
 *  Jajuk
 *  Copyright (C) 2003-2012 The Jajuk Team
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
 */
package org.jajuk.services.players;

import static org.jajuk.util.Const.CONF_BIT_PERFECT;
import static org.jajuk.util.Const.CONF_VOLUME;

import java.io.IOException;
import java.net.URISyntaxException;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.File;
import org.jajuk.util.Conf;

public class TestPlayer extends JajukTestCase {
  private float sampleVolume = 0.91f;

  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  public void setUp() throws IOException, URISyntaxException {
    Conf.setProperty(CONF_VOLUME, "" + sampleVolume);
    Conf.setProperty(CONF_BIT_PERFECT, "false");
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
  }

  /**
   * Non regression test for this issue : when switching from bit perfect to normal mode, sound is set to zero
   */
  public void testIssueBitPerfect() throws Exception {
    assertEquals(0.91f, Conf.getFloat(CONF_VOLUME));
    //Set bit perfect
    Conf.setProperty(CONF_BIT_PERFECT, "true");
    try {
      File file = JUnitHelpers.getFile("file10", false);
      Player.play(file, 3, 20);
    } finally {
      Player.stop(true);
    }
    assertEquals(0.91f, Conf.getFloat(CONF_VOLUME));
    //Now switch to Non-bit-perfect mode
    Conf.setProperty(CONF_BIT_PERFECT, "false");
    try {
      File file = JUnitHelpers.getFile("file10", false);
      Player.play(file, 3, 20);
    } finally {
      Player.stop(true);
    }
    // If targeted bug is still there, volume should be zero now
    assertEquals(0.91f, Player.getCurrentVolume());
  }
}
