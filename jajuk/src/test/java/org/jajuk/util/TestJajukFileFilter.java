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
 *  $Revision: 3132 $
 */
package org.jajuk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jajuk.util.filters.AnyFileFilter;
import org.junit.Test;

/**
 * 
 */
public class TestJajukFileFilter {

  /**
   * Test method for
   * {@link org.jajuk.util.JajukFileFilter#JajukFileFilter(boolean, org.jajuk.util.JajukFileFilter[])}
   * .
   */
  @Test
  public final void testJajukFileFilterBooleanJajukFileFilterArray() {
    new JajukFileFilter(true, new JajukFileFilter[] {});
  }

  /**
   * Test method for
   * {@link org.jajuk.util.JajukFileFilter#JajukFileFilter(org.jajuk.util.JajukFileFilter[])}
   * .
   */
  @Test
  public final void testJajukFileFilterJajukFileFilterArray() {
    new JajukFileFilter(new JajukFileFilter[] {});
  }

  /**
   * Test method for
   * {@link org.jajuk.util.JajukFileFilter#JajukFileFilter(java.lang.String[])}.
   */
  @Test
  public final void testJajukFileFilterStringArray() {
    new JajukFileFilter(new String[] {});
    new JajukFileFilter((String[]) null);
    new JajukFileFilter(new String[] { "mp3", "ogg" });
  }

  /**
   * Test method for {@link org.jajuk.util.JajukFileFilter#accept(java.io.File)}
   * .
   */
  @Test
  public final void testAcceptFileExtension() {
    JajukFileFilter filter = new JajukFileFilter(new String[] { "mp3", "ogg" });
    assertTrue(filter.accept(new File("test.mp3")));
    assertFalse(filter.accept(new File("test.mp2")));
  }

  /**
   * Test method for {@link org.jajuk.util.JajukFileFilter#accept(java.io.File)}
   * .
   */
  @Test
  public final void testAcceptFileFilter() {
    JajukFileFilter filter = new JajukFileFilter(true, new JajukFileFilter[] { AnyFileFilter
        .getInstance() });
    assertTrue(filter.accept(new File("test")));
  }

  /**
   * Test method for {@link org.jajuk.util.JajukFileFilter#getDescription()}.
   */
  @Test
  public final void testGetDescription() {
    JajukFileFilter filter = new JajukFileFilter(new String[] { "mp3", "ogg" });
    assertTrue(filter.getDescription(), filter.getDescription().contains("mp3"));
    assertTrue(filter.getDescription(), filter.getDescription().contains("ogg"));

    JajukFileFilter filter2 = new JajukFileFilter(true, new JajukFileFilter[] { AnyFileFilter
        .getInstance() });

    assertEquals("", filter2.getDescription());
  }

  /**
   * Test method for {@link org.jajuk.util.JajukFileFilter#getExtensions()}.
   */
  @Test
  public final void testGetExtensions() {
    JajukFileFilter filter = new JajukFileFilter(new String[] { "mp3", "ogg" });
    assertEquals(2, filter.getExtensions().length);
    assertEquals("mp3", filter.getExtensions()[0]);
    assertEquals("ogg", filter.getExtensions()[1]);

    JajukFileFilter filter2 = new JajukFileFilter(true, new JajukFileFilter[] { AnyFileFilter
        .getInstance() });

    assertEquals(0, filter2.getExtensions().length);
  }

  /**
   * Test method for {@link org.jajuk.util.JajukFileFilter#getFilters()}.
   */
  @Test
  public final void testGetFilters() {
    JajukFileFilter filter = new JajukFileFilter(new String[] { "mp3", "ogg" });
    assertEquals(0, filter.getFilters().length);

    JajukFileFilter filter2 = new JajukFileFilter(true, new JajukFileFilter[] { AnyFileFilter
        .getInstance() });

    assertEquals(1, filter2.getFilters().length);
  }

  /**
   * Test method for
   * {@link org.jajuk.util.JajukFileFilter#isKnownExtension(java.io.File)}.
   */
  @Test
  public final void testIsKnownExtension() {
    JajukFileFilter filter = new JajukFileFilter(new String[] { "mp3", "ogg" });
    assertTrue(filter.isKnownExtension(new File("test.mp3")));
    assertTrue(filter.isKnownExtension(new File("test.ogg")));
    assertFalse(filter.isKnownExtension(new File("test.mp2")));

    JajukFileFilter filter2 = new JajukFileFilter(true, new JajukFileFilter[] { AnyFileFilter
        .getInstance() });

    assertFalse(filter2.isKnownExtension(new File("test.mp3")));
    assertFalse(filter2.isKnownExtension(new File("test.ogg")));
    assertFalse(filter2.isKnownExtension(new File("test.mp2")));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.JajukFileFilter#setAcceptDirectories(boolean)}.
   */
  @Test
  public final void testSetAcceptDirectories() {
    {
      JajukFileFilter filter = new JajukFileFilter(new String[] { "mp3", "ogg" });

      filter.setAcceptDirectories(true);
      assertTrue(filter.accept(new File(System.getProperty("java.io.tmpdir"))));
      filter.setAcceptDirectories(false);
      assertFalse(filter.accept(new File(System.getProperty("java.io.tmpdir"))));
    }

    { // ignored for Filter based matching
      JajukFileFilter filter2 = new JajukFileFilter(true, new JajukFileFilter[] { AnyFileFilter
          .getInstance() });
      filter2.setAcceptDirectories(true);
      assertFalse(filter2.accept(new File(System.getProperty("java.io.tmpdir"))));
      filter2.setAcceptDirectories(false);
      assertFalse(filter2.accept(new File(System.getProperty("java.io.tmpdir"))));
    }
  }

  /**
   * Test method for {@link org.jajuk.util.JajukFileFilter#show(java.io.File)}.
   */
  @Test
  public final void testShow() {
    {
      JajukFileFilter filter = new JajukFileFilter(new String[] { "mp3", "ogg" });

      filter.setAcceptDirectories(true);
      assertTrue(filter.show(new File(System.getProperty("java.io.tmpdir"))));
      filter.setAcceptDirectories(false);
      assertFalse(filter.show(new File(System.getProperty("java.io.tmpdir"))));

      assertTrue(filter.show(new File("test.mp3")));
      assertTrue(filter.show(new File("test.ogg")));
      assertFalse(filter.show(new File("test.mp2")));
    }
  }
}
