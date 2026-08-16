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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

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
 * Common behavior for every jajuk test.
 * <p>EVERY JAJUK UNIT TEST *MUST* EXTEND THIS CLASS (except from the "ext" package).</p>
 */
public abstract class JajukTestCase {
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
    assertNotNull(System.getProperty(PROPERTY_JAVA_HOME),
        "Need to have a property 'java.home' to run this test!");
    return "\"" + System.getProperty(PROPERTY_JAVA_HOME) + java.io.File.separator + "bin"
        + java.io.File.separator + JAVA_PROCESS + "\"";
  }

  /**
   * Every UT must extend JajukTestCase and implement this method for specific initialization if any
   */
  protected void specificSetUp() throws Exception {
    // Does nothing by default
  }

  /**
   * Wipe the test workspace, retrying a few times.
   * <p>waitForAllThreadToFinish() only joins threads whose class lives in an org.jajuk
   * package, so a plain {@code new Thread(runnable)} started by a previous test can still
   * be writing under the workspace. When that happens the recursive delete walks a
   * directory that gets repopulated underneath it and fails with
   * DirectoryNotEmptyException, which used to make an unrelated test fail at random.</p>
   *
   * @param basedir the test workspace root
   * @throws Exception if the workspace could not be removed after several attempts
   */
  private static void deleteWorkspace(File basedir) throws Exception {
    for (int attempt = 1; basedir.exists(); attempt++) {
      try {
        UtilSystem.deleteDir(basedir);
      } catch (java.io.IOException e) {
        if (attempt >= 5) {
          throw e;
        }
        Log.warn("Could not wipe the test workspace (attempt " + attempt + "), retrying: "
            + e.getMessage());
        Thread.sleep(200);
      }
    }
  }

  /**
   * Counterpart of {@link #specificSetUp()} for specific clean up if any.
   * <p>Subclasses must use this hook rather than declaring their own {@code @AfterEach}
   * method named tearDown(): overriding an annotated superclass lifecycle method silently
   * prevents JUnit 5 from running it.</p>
   */
  protected void specificTearDown() throws Exception {
    // Does nothing by default
  }

  @BeforeEach
  protected final void setUp(TestInfo testInfo) throws Exception {
    Log.info("Setting up testcase: " + getClass() + "." + testInfo.getDisplayName());
    // Set the exiting state flag to force still running threads to suspend
    TestHelpers.forceExitState(true);
    // Wait for all threads to finish
    TestHelpers.waitForAllThreadToFinish();
    // let's clean up before we begin any test
    TestHelpers.cleanup();
    // do the cleanup twice as the cleanup itself may launch some threads
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
    deleteWorkspace(basedir);
    assertTrue(workspace.mkdirs());
    assertTrue(sample_devices.mkdirs());
    assertTrue(tech_tests.mkdirs());
    //create cache directory and expected conf files
    assertTrue(SessionService.getConfFileByPath(Const.FILE_CACHE).mkdirs());
    History.commit();
    // Create a tmp directory as a music folder or tmp trash
    assertTrue(SessionService.getConfFileByPath("tests").mkdirs());
    // Force dummy player
    scriptFile = java.io.File.createTempFile("dummy", "mplayer.sh", new java.io.File(
        ConstTest.TECH_TESTS_PATH));
    assertTrue(scriptFile.setExecutable(true));
    URL thisClassAbsUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
    String thisClassAbsPath = new java.io.File(thisClassAbsUrl.toURI()).getAbsolutePath();
    FileUtils.writeStringToFile(scriptFile, "#!/bin/sh\n\n" + findJavaExecutable() + " -cp \""
        + thisClassAbsPath + "\" " + MAIN_CLASS);
    Conf.setProperty(Const.CONF_MPLAYER_PATH_FORCED, scriptFile.getAbsolutePath());
    // Unset exiting state
    TestHelpers.forceExitState(false);
    specificSetUp();
  }

  @AfterEach
  protected final void tearDown() throws Exception {
    specificTearDown();
    Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
    for (Thread thd : traces.keySet()) {
      if (thd.getName().contains("MPlayer reader thread")
              || thd.getName().contains("MPlayer writer thread")) {
        TestHelpers.dumpThreads();
        throw new IllegalStateException("Had leftover MPlayer thread: " + thd.getName());
      }
    }
  }
}
