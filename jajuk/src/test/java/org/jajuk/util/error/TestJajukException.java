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
 *  $Revision$
 */
package org.jajuk.util.error;

import org.jajuk.JajukTestCase;

/**
 * DOCUMENT_ME.
 */
public class TestJajukException extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.error.JajukException#JajukException(int)}.
   */
  public void testJajukExceptionInt() {
    new JajukException(25);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.error.JajukException#JajukException(int, java.lang.Throwable)}
   * .
   */
  public void testJajukExceptionIntThrowable() {
    new JajukException(26, new Throwable("Testexception"));
  }

  /**
   * Test method for {@link org.jajuk.util.error.JajukException#getCode()}.
   */
  public void testGetCode() {
    JajukException exc = new JajukException(27);
    assertEquals(27, exc.getCode());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.error.JajukException#JajukException(int, java.lang.String, java.lang.Throwable)}
   * .
   */
  public void testJajukExceptionIntStringThrowable() {
    JajukException exc = new JajukException(28, "testexceptiontext", new Throwable("Testthrowable"));
    assertTrue(exc.getMessage(), exc.getMessage().contains("testexceptiontext"));
  }

  /**
   * Test jajuk exception int string throwable null.
   * DOCUMENT_ME
   */
  public void testJajukExceptionIntStringThrowableNull() {
    new JajukException(28, null, new Throwable("Testthrowable"));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.error.JajukException#JajukException(int, java.lang.String)}
   * .
   */
  public void testJajukExceptionIntString() {
    JajukException exc = new JajukException(29, "testexceptiontext2");
    assertTrue(exc.getMessage(), exc.getMessage().contains("testexceptiontext2"));
  }

  /**
   * Test jajuk exception int string null.
   * DOCUMENT_ME
   */
  public void testJajukExceptionIntStringNull() {
    new JajukException(29, (String) null);
  }
}
