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
package org.jajuk;

import java.io.File;

import junit.framework.TestCase;

import org.jajuk.base.Collection;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.Const;
import org.jajuk.util.UtilSystem;

/**
 * .
 */
public abstract class JajukTestCase extends TestCase {
  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // Make sure to use a test workspace
    SessionService.setTestMode(true);
    // let's clean up before we begin any test
    JUnitHelpers.waitForAllWorkToFinishAndCleanup();
    // do the cleanup twice as we have to ensure to clean up things once again when the threads are finally stopped
    JUnitHelpers.waitForAllWorkToFinishAndCleanup();
    // assert to find cases where we do not clean up correctly
    assertEquals(-1, QueueModel.getIndex());
    assertEquals(0, QueueModel.getQueueSize());
    // Clean the collection
    StartupCollectionService.registerItemManagers();
    Collection.clearCollection();
    WebRadioManager.getInstance().clear();
    // And use a specific workspace
    File basedir = new File(ConstTest.BASE_DIRECTORY_PATH);
    File workspace = new File(ConstTest.SAMPLE_WORKSPACE_PATH);
    File sample_devices = new File(ConstTest.DEVICES_BASE_PATH);
    File tech_tests = new File(ConstTest.TECH_TESTS_PATH);
    // Make sure to clear totally the workspace and sample devices and recreate it
    if (basedir.exists()) {
      UtilSystem.deleteDir(basedir);
    }
    SessionService.setWorkspace(workspace.getAbsolutePath());
    workspace.mkdirs();
    sample_devices.mkdirs();
    tech_tests.mkdirs();
    //create cache directory and expected conf files
    SessionService.getConfFileByPath(Const.FILE_CACHE).mkdirs();
    org.jajuk.util.Conf.commit();
    History.commit();
    // Create a tmp directory as a music folder or tmp trash
    SessionService.getConfFileByPath("tests").mkdirs();
    super.setUp();
  }
}
