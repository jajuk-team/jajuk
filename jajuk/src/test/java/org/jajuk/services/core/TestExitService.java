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
package org.jajuk.services.core;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.services.startup.StartupCollectionService;

/**
 * .
 */
public class TestExitService extends JajukTestCase {
  /**
   * Test method for {@link org.jajuk.services.core.ExitService#run()}.
   *
   * @throws Exception the exception
   */
  public void testRun() throws Exception {
    StartupCollectionService.registerItemManagers();
    ExitService service = new ExitService();
    service.run();
    // once again with sessionidfile
    SessionService.getSessionIdFile().getParentFile().mkdirs();
    SessionService.getSessionIdFile().createNewFile();
    service.run();
  }

  /**
   * Test method for {@link org.jajuk.services.core.ExitService#ExitService()}.
   */
  public void testExitService() {
    new ExitService();
  }

  /**
   * Test method for {@link org.jajuk.services.core.ExitService#exit(int)}.
   */
  public void testExit() {
    ExitService service = new ExitService();
    assertNotNull(service);
    // don't run this as it stops the JVM! service.exit(1);
  }

  /**
   * Test method for {@link org.jajuk.services.core.ExitService#isExiting()}.
   */
  public void testIsExiting() {
    assertFalse(ExitService.isExiting());
  }
}
