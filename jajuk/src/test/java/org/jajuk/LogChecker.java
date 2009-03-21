/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk;

import junit.framework.TestCase;

import org.jajuk.util.UtilSystem;

/**
 * This Test is used to check conveniently jajuk log content to find special
 * warnings or errors that may imply a problem Note that this test only work
 * when redirecting stdin et stderr to a file named /tmp/jajuk_out.log
 */
public class LogChecker extends TestCase {

  private static final String FILE_PATH = "/tmp/jajuk_out.log";

  private String logs;

  @Override
  public void setUp() throws Exception {
    logs = UtilSystem.readFile(FILE_PATH).toString();
  }

  /**
   * Check for "Overflow" string
   */
  public void testOverflow() {
    assertFalse(logs.matches(".*Event overflow for.*"));
  }
  
  /**
   * Check for playtime rate issue
   */
  public void testPreferences() {
    assertFalse(logs.matches(".*Playtime rate > 1 for.*"));
  }
  
   /**
   * Check for play time outs
   */
  public void testPlayOOT() {
    assertFalse(logs.matches("OOT Mplayer process.*"));
  }
  
  

}
