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
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jajuk.base.Collection;
import org.jajuk.base.TypeManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.players.DummyMPlayerImpl;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * .
 */
public abstract class JajukTestCase extends TestCase {
  /** The Constant JAVA_PROCESS.   */
  private static final String JAVA_PROCESS = "java";
  /** The Constant MAIN_CLASS.   */
  private static final String MAIN_CLASS = DummyMPlayerImpl.class.getName();
  java.io.File scriptFile;
  /** Property which is used to find the current installation location of java. */
  protected static final String PROPERTY_JAVA_HOME = "java.home";
  /* Need to initialize workspace here because some src classes call SessionService.getConfFileByPath() 
   * from class init and then override the workspace path, then use the user home directory instead (and even worst, it's cached in 
   * SessionService.getConfFileByPath())*/
  static {
    // Make sure to use a test workspace
    SessionService.setTestMode(true);
    File workspace = new File(ConstTest.SAMPLE_WORKSPACE_PATH);
    SessionService.setWorkspace(workspace.getAbsolutePath());
  }

  /**
   * Find java executable.
   * 
   *
   * @return the string
   */
  private String findJavaExecutable() {
    assertNotNull("Need to have a property 'java.home' to run this test!",
        System.getProperty(PROPERTY_JAVA_HOME));
    return "\"" + System.getProperty(PROPERTY_JAVA_HOME) + java.io.File.separator + "bin"
        + java.io.File.separator + JAVA_PROCESS + "\"";
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    Log.info("Setting up testcase: " + getClass() + "." + getName() + "()");
    // Set the exiting state flag to force still running threads to suspend
    TestHelpers.forceExitState();
    // let's clean up before we begin any test
    TestHelpers.cleanup();
    // do the cleanup twice as we have to ensure to clean up things once again when the threads are finally stopped
    TestHelpers.waitForAllThreadToFinish();
    // stop any Player from previous tests
    Player.stop(true);
    // assert to find cases where we do not clean up correctly
    assertEquals(-1, QueueModel.getIndex());
    assertEquals(0, QueueModel.getQueueSize());
    // Clean the collection
    StartupCollectionService.registerItemManagers();
    TypeManager.getInstance().clear();
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
    workspace.mkdirs();
    sample_devices.mkdirs();
    tech_tests.mkdirs();
    //create cache directory and expected conf files
    SessionService.getConfFileByPath(Const.FILE_CACHE).mkdirs();
    History.commit();
    // Create a tmp directory as a music folder or tmp trash
    SessionService.getConfFileByPath("tests").mkdirs();
    // Force dummy player
    scriptFile = java.io.File.createTempFile("dummy", "mplayer.sh", new java.io.File(
        ConstTest.TECH_TESTS_PATH));
    scriptFile.setExecutable(true);
    URL thisClassAbsUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
    String thisClassAbsPath = new java.io.File(thisClassAbsUrl.toURI()).getAbsolutePath();
    FileUtils.writeStringToFile(scriptFile, "#!/bin/sh\n\n" + findJavaExecutable() + " -cp \""
        + thisClassAbsPath + "\" " + MAIN_CLASS);
    Conf.setProperty(Const.CONF_MPLAYER_PATH_FORCED, scriptFile.getAbsolutePath());
    super.setUp();
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
    Iterator<Thread> i = traces.keySet().iterator();
    while (i.hasNext()) {
      Thread thd = i.next();
      if (thd.getName().contains("MPlayer reader thread")
          || thd.getName().contains("MPlayer writer thread")) {
        TestHelpers.dumpThreads();
        throw new IllegalStateException("Had leftover MPlayer thread: " + thd.getName());
      }
    }
    super.tearDown();
  }
}
