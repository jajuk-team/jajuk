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
 *  
 */
package org.jajuk.services.lastfm;

import java.util.Set;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * DOCUMENT_ME.
 */
public class TestLastFmManager extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.lastfm.LastFmManager#getInstance()}.
   */
  public void testGetInstance() {
    assertNotNull(LastFmManager.getInstance());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.lastfm.LastFmManager#getRegistrationKeys()}.
   */
  public void testGetRegistrationKeys() {
    Set<JajukEvents> keys = LastFmManager.getInstance().getRegistrationKeys();
    assertTrue(keys.contains(JajukEvents.FILE_FINISHED));
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.LastFmManager#configure()}
   * .
   */
  public void testConfigure() {
    LastFmManager.getInstance().configure();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.lastfm.LastFmManager#update(org.jajuk.events.JajukEvent)}
   * .
   */
  public void testUpdate() throws Exception {
    // nothing happens if
    {
      Conf.setProperty(Const.CONF_LASTFM_AUDIOSCROBBLER_ENABLE, "false");
      LastFmManager.getInstance().update(new JajukEvent(JajukEvents.FILE_FINISHED, null));

      // wait for thread to finish
      JUnitHelpers.waitForThreadToFinish("LastFM Update Thread");
    }
  }

}
