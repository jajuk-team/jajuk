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
package org.jajuk.util.log;

import java.util.List;

import org.jajuk.JajukTestCase;
import org.jajuk.util.error.JajukException;

/**
 * .
 */
public class TestLog extends JajukTestCase {
  @Override
  protected void specificSetUp() throws Exception {
    // make sure we have logging initialized for these tests
    Log.init();
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#getInstance()}.
   */
  public void testGetInstance() {
    Log.init();
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#debug(java.lang.String)}.
   */
  public void testDebugString() {
    Log.debug("testlog1");
    verifySpool("testlog1");
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#debug(java.lang.Throwable)}.
   */
  public void testDebugThrowable() {
    Log.debug(new Throwable("testthrowable2"));
    // this is anonymonized: verifySpool("testthrowable2");
    verifySpool("***");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.log.Log#debug(java.lang.String, java.lang.Throwable)}
   * .
   */
  public void testDebugStringThrowable() {
    Log.debug("testlog2", new Throwable("testthrowable2"));
    verifySpool("testlog2");
    // this is anonymonized: verifySpool("testthrowable2");
    verifySpool("***");
  }

  /**
   * Test debug string throwable null.
   * 
   */
  public void testDebugStringThrowableNull() {
    Log.debug(null, new Throwable("testthrowable2"));
    // verifySpool("testlog2");
    // this is anonymonized: verifySpool("testthrowable2");
    verifySpool("***");
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#info(java.lang.String)}.
   */
  public void testInfo() {
    Log.info("testloginfo3");
    verifySpool("testloginfo3");
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#warn(java.lang.String)}.
   */
  public void testWarnString() {
    Log.warn("testwarn4");
    verifySpool("testwarn4");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.log.Log#warn(java.lang.String, java.lang.String)}.
   */
  public void testWarnStringString() {
    Log.warn("warn5", "addinfo");
    verifySpool("warn5");
    verifySpool("addinfo");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.log.Log#warn(int, java.lang.String, java.lang.Throwable)}
   * .
   */
  public void testWarnIntStringThrowable() {
    Log.warn(10, "warntext6", new Throwable("testthrowable"));
    verifySpool("warntext6");
    // this is anonymonized: verifySpool("testthrowable2");
    verifySpool("***");
  }

  /**
   * Test warn int string throwable null.
   * 
   */
  public void testWarnIntStringThrowableNull() {
    Log.warn(10, null, new Throwable("testthrowable"));
    // this is anonymonized: verifySpool("testthrowable2");
    verifySpool("***");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.log.Log#error(int, java.lang.String, java.lang.Throwable)}
   * .
   */
  public void testErrorIntStringThrowable() {
    Log.error(30, "errortext7", new Throwable("errorthrowable"));
    verifySpool("errortext7");
    // this is anonymonized: verifySpool("testthrowable2");
    verifySpool("***");
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#error(int)}.
   */
  public void testErrorInt() {
    Log.error(31);
    verifySpool("31");
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#error(java.lang.Throwable)}.
   */
  public void testErrorThrowable() {
    Log.error(new Throwable("testerror8"));
    // this is anonymonized: verifySpool("testthrowable2");
    verifySpool("***");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.log.Log#error(int, java.lang.Throwable)}.
   */
  public void testErrorIntThrowable() {
    Log.error(32, new Throwable("testerror9"));
    verifySpool("32");
    // this is anonymonized: verifySpool("testthrowable2");
    verifySpool("***");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.log.Log#error(java.lang.String, org.jajuk.util.error.JajukException)}
   * .
   */
  public void testErrorStringJajukException() {
    Log.error("teststring", new JajukException(33));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.log.Log#error(org.jajuk.util.error.JajukException)}.
   */
  public void testErrorJajukException() {
    Log.error(new JajukException(34));
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#fatal(java.lang.String)}.
   */
  public void testFatal() {
    Log.fatal("testfataltext");
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#getVerbosity()}.
   */
  public void testGetVerbosity() {
    // set verbosity first as we can not rely on INFO being set because other
    // tests might have adjusted it somehow
    Log.setVerbosity(Log.INFO);
    assertEquals(Log.INFO, Log.getVerbosity());
    Log.setVerbosity(Log.DEBUG);
    assertEquals(Log.DEBUG, Log.getVerbosity());
    Log.setVerbosity(Log.INFO);
    assertEquals(Log.INFO, Log.getVerbosity());
    Log.setVerbosity(Log.WARNING);
    assertEquals(Log.WARNING, Log.getVerbosity());
    Log.setVerbosity(Log.ERROR);
    assertEquals(Log.ERROR, Log.getVerbosity());
    Log.setVerbosity(Log.FATAL);
    assertEquals(Log.FATAL, Log.getVerbosity());
    Log.setVerbosity(Log.INFO); // reset to info for other tests
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#setVerbosity(int)}.
   */
  public void testSetVerbosity() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#stack(java.lang.Exception)}.
   */
  public void testStack() {
    Log.stack(new Exception("teststacktraceexception"));
  }

  /**
   * Test method for {@link org.jajuk.util.log.Log#isDebugEnabled()}.
   */
  public void testIsDebugEnabled() {
    Log.setVerbosity(Log.DEBUG);
    assertTrue(Log.isDebugEnabled());
    Log.setVerbosity(Log.INFO);
    assertFalse(Log.isDebugEnabled());
  }

  private void verifySpool(String substring) {
    verifySpool(substring, true);
  }

  /**
   * Verify spool.
   * 
   *
   * @param substring 
   * @param expected 
   */
  private void verifySpool(String substring, boolean expected) {
    List<String> list = Log.getSpool(true);
    for (String str : list) {
      if (str.contains(substring)) {
        // expected => return, not expected => fail
        if (expected) {
          return;
        } else {
          fail("Should not find string '" + substring + "' in spool: " + list.toString());
        }
      }
    }
    // if we expected the string, but did not find it we need to fail here
    if (expected) {
      fail("List does not contain expected string '" + substring + "' in spool: " + list.toString());
    }
  }

  /**
   * Test anonymization.
   * 
   */
  public void testAnonymization() {
    // things in {{...}} are replaced in the spool. Verify that this happens
    Log.info("this is {{sensitive}} data...");
    verifySpool("this is");
    verifySpool("sensitive", false);
    verifySpool("data...");
    // replaced by "***"
    verifySpool("***");
  }

  /**
   * Test anonymization player state.
   * 
   */
  public void testAnonymizationPlayerState() {
    // special replacement that is done to not show personal data in the spool
    Log.info("Player state changed: OPENING this is secret personal information");
    verifySpool("Player");
    verifySpool("OPENING");
    verifySpool("secret", false);
  }
}
