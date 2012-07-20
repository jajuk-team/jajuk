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
package org.jajuk.services.core;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.jajuk.ConstTest;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.util.Const;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukRuntimeException;

/**
 * .
 */
public class TestSessionService extends JajukTestCase {
  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.core.SessionService#checkOtherSession()}.
   */
  public void testCheckOtherSession() throws Exception {
    SessionService.checkOtherSession();
  }

  /**
   * Test method for {@link org.jajuk.services.core.SessionService#isIdeMode()}.
   */
  public void testIsIdeMode() {
    // depends on startup
    SessionService.isIdeMode();
  }

  /**
   * Test method for {@link org.jajuk.services.core.SessionService#isTestMode()}
   * .
   */
  public void testIsTestMode() {
    // depends on startup
    SessionService.isTestMode();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.core.SessionService#getWorkspace()}.
   */
  public void testGetWorkspace() throws Exception {
    assertNotNull(SessionService.getWorkspace());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.SessionService#setWorkspace(java.lang.String)}
   * .
   */
  public void testSetWorkspace() {
    SessionService.setWorkspace("testworkspace");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.SessionService#getSessionIdFile()}.
   */
  public void testGetSessionIdFile() {
    assertNotNull(SessionService.getSessionIdFile());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.SessionService#handleCommandline(java.lang.String[])}
   * .
   */
  public void testHandleCommandline() {
    SessionService.handleCommandline(new String[] {});
    SessionService.handleCommandline(new String[] { "-test", "-ide", "-something" });
    assertFalse(parseWorkspaceLocation(""));
    assertFalse(parseWorkspaceLocation("/foo"));
    String tmpDir = ConstTest.TEMP_PATH;
    String rightWorkspaceLocation = tmpDir;
    assertTrue(parseWorkspaceLocation(rightWorkspaceLocation));
    SessionService.handleCommandline(new String[] { "-test", "-ide", "-workspace=" + tmpDir,
        "-something" });
  }

  /**
   * Return true if the workspace location is valid.
   *
   * @param workspaceLocation 
   * @return true if the workspace location is valid
   */
  private boolean parseWorkspaceLocation(String workspaceLocation) {
    try {
      // next line should throw a runtime exception
      SessionService.handleCommandline(new String[] { "-test", "-ide", "-something",
          "-workspace=" + workspaceLocation });
      return true;
    } catch (JajukRuntimeException e) {
      return false;
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.SessionService#handleSystemProperties()}.
   */
  public void testHandleSystemProperties() {
    SessionService.handleSystemProperties();
    System.setProperty("ide", "true");
    System.setProperty("test", "true");
    SessionService.handleSystemProperties();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.SessionService#createSessionFile()}.
   */
  public void testCreateSessionFile() {
    SessionService.createSessionFile();
    SessionService.setWorkspace("/invalidpath");
    SessionService.createSessionFile();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.core.SessionService#discoverWorkspace()}.
   * @TODO : perform more tests
   */
  public void testDiscoverWorkspace() throws Exception {
    { // ensure that the base jajuk-directory exists, otherwise the
      // "first time wizard" is run, which blocks the test
      File bootstrap = new File(SessionService.getBootstrapPath());
      // try to create it if it is missing
      if (!bootstrap.exists()) {
        String content = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n"
            + "<properties><entry key='final'>" + UtilSystem.getUserHome() + "</entry>\n"
            + "<entry key='test'>" + UtilSystem.getUserHome() + "</entry>\n</properties>";
        FileUtils.writeStringToFile(bootstrap, content);
      }
      // needs to be a directory, needs to be readable, ...
      assertTrue(bootstrap.isFile());
      assertTrue(bootstrap.canRead());
    }
    // Reset CLI parameters
    SessionService.handleCommandline(new String[] { "-test", "-ide", "-something" });
    SessionService.discoverWorkspace();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.SessionService#notifyFirstTimeWizardClosed()}
   * .
   */
  public void testNotifyFirstTimeWizardClosed() {
    SessionService.notifyFirstTimeWizardClosed();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.core.SessionService#getCachePath(java.net.URL)}.
   */
  public void testGetCachePath() throws Exception {
    assertNotNull(SessionService.getCachePath(new URL("ftp://example.com/")));
    assertNotNull(SessionService.getCachePath(new URL("http://ww.example.com/")));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.SessionService#getConfFileByPath(java.lang.String)}
   * .
   */
  public void testGetConfFileByPath() {
    SessionService.getConfFileByPath("/tmp");
    SessionService.getConfFileByPath("/tmp");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.SessionService#getDefaultCollectionPath()}.
   */
  public void testGetDefaultWorkspace() {
    SessionService.getDefaultCollectionPath();
  }

  /**
   * Test method for {@link org.jajuk.services.core.SessionService#clearCache()}
   * .
   *
   * @throws Exception the exception
   */
  public void testClearCache() throws Exception {
    SessionService.clearCache();
    // create some dummy file
    File file = SessionService.getConfFileByPath(Const.FILE_CACHE);
    assertNotNull(file);
    File.createTempFile("cache", ".tst", file);
    SessionService.clearCache();
  }

  // helper method to emma-coverage of the unused constructor
  // For EMMA code-coverage tests
  /**
   * Test private constructor.
   * 
   *
   * @throws Exception the exception
   */
  public void testPrivateConstructor() throws Exception {
    JUnitHelpers.executePrivateConstructor(SessionService.class);
  }
}
