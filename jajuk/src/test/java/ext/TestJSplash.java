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
package ext;

import java.awt.HeadlessException;
import java.net.URL;

import org.jajuk.JajukTestCase;

/**
 * .
 */
public class TestJSplash extends JajukTestCase {
  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.JSplash#JSplash(java.net.URL, boolean, boolean, boolean, java.lang.String, java.lang.String, java.awt.Font)}
   * .
   */
  public void testJSplash() throws Exception {
    try {
      new JSplash(new URL("http://www.example.com"), true, true, true, "copyright", "version", null);
    } catch (HeadlessException e) {
      // expected when tests are executed without UI support
    }
  }

  /**
   * Test j splash2.
   * 
   *
   * @throws Exception the exception
   */
  public void testJSplash2() throws Exception {
    try {
      new JSplash(new URL("http://www.example.com"), true, false, false, "copyright", "version",
          null);
    } catch (HeadlessException e) {
      // expected when tests are executed without UI support
    }
  }

  /**
   * Test j splash null url.
   * 
   *
   * @throws Exception the exception
   */
  public void testJSplashNullURL() throws Exception {
    try {
      new JSplash(null, true, false, false, "copyright", "version", null);
      fail("Should throw exception with null-URL");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("Invalid URL specified for Splashscreen"));
    } catch (HeadlessException e) {
      // expected when tests are executed without UI support
    }
  }

  /**
   * Test method for {@link ext.JSplash#splashOn()}.
   *
   * @throws Exception the exception
   */
  public void testSplashOn() throws Exception {
    try {
      JSplash splash = new JSplash(new URL("http://www.example.com"), true, true, true,
          "copyright", "version", null);
      splash.splashOn();
      splash.splashOff();
    } catch (HeadlessException e) {
      // expected when tests are executed without UI support
    }
  }

  /**
   * Test method for {@link ext.JSplash#splashOff()}.
   *
   * @throws Exception the exception
   */
  public void testSplashOff() throws Exception {
    try {
      JSplash splash = new JSplash(new URL("http://www.example.com"), true, true, true,
          "copyright", "version", null);
      splash.splashOff();
    } catch (HeadlessException e) {
      // expected when tests are executed without UI support
    }
  }

  /**
   * Test method for {@link ext.JSplash#setProgress(int)}.
   *
   * @throws Exception the exception
   */
  public void testSetProgressInt() throws Exception {
    try {
      JSplash splash = new JSplash(new URL("http://www.example.com"), true, true, true,
          "copyright", "version", null);
      splash.setProgress(30);
      splash.splashOff();
    } catch (HeadlessException e) {
      // expected when tests are executed without UI support
    }
  }

  /**
   * Test method for {@link ext.JSplash#setProgress(int, java.lang.String)}.
   *
   * @throws Exception the exception
   */
  public void testSetProgressIntString() throws Exception {
    try {
      JSplash splash = new JSplash(new URL("http://www.example.com"), true, true, true,
          "copyright", "version", null);
      splash.setProgress(30, "testmessage");
      splash.splashOff();
    } catch (HeadlessException e) {
      // expected when tests are executed without UI support
    }
  }

  /**
   * Test set progress int string2.
   * 
   *
   * @throws Exception the exception
   */
  public void testSetProgressIntString2() throws Exception {
    try {
      JSplash splash = new JSplash(new URL("http://www.example.com"), true, true, false,
          "copyright", "version", null);
      splash.setProgress(30, "testmessage");
      splash.splashOff();
    } catch (HeadlessException e) {
      // expected when tests are executed without UI support
    }
  }
}
