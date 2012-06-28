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
package org.jajuk;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * This Test is used to check conveniently jajuk log content to find special
 * warnings or errors that may imply a problem Note that this test only work
 * when redirecting stdin et stderr to a file named /tmp/jajuk_out.log
 */
public class LogChecker extends TestCase {
  /** The Constant FILE_PATH.   */
  private static final File FILE_PATH = SessionService.getConfFileByPath("jajuk.log");
  private String logs;

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    try {
      logs = UtilSystem.readFile(FILE_PATH.getAbsolutePath()).toString();
    } catch (JajukException e) {
      // if an exception occurs, ensure it is a "FileNotFound"
      assertNotNull("Should have an underlying cause when catching JajukException", e.getCause());
      assertTrue("We only accept FileNotFoundException as valid exception in this test",
          e.getCause() instanceof FileNotFoundException);
      // set string to empty to not fail any of the tests in this case
      logs = "";
      // also log a warning to indicate that this test did not do anything
      Log.warn("File " + FILE_PATH + " not found, cannot run checks on log file.");
    }
  }

  /**
   * Check for "Overflow" string.
   */
  public void testOverflow() {
    assertFalse(logs.matches(".*Event overflow for.*"));
  }

  /**
   * Check for playtime rate issue.
   */
  public void testPreferences() {
    assertFalse(logs.matches(".*Playtime rate > 1 for.*"));
  }

  /**
   * Check for play time outs.
   */
  public void testPlayOOT() {
    assertFalse(logs.matches("OOT Mplayer process.*"));
  }

  /**
   * Check for EDT violations (this test is required but far not enough as most
   * of the time, we don't log this kind of errors).
   */
  public void testOutEDT() {
    assertFalse(logs.matches("creation must be done on Event Dispatch Thread "));
  }
}
