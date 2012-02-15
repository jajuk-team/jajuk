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
package org.jajuk.util.filters;

import java.io.File;

import org.jajuk.JajukTestCase;

/**
 * DOCUMENT_ME.
 */
public class TestAnyFileFilter extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.filters.AnyFileFilter#getDescription()}.
   */
  public void testGetDescription() {
    AnyFileFilter filter = AnyFileFilter.getInstance();
    assertEquals("*.*", filter.getDescription());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.filters.AnyFileFilter#isKnownExtension(java.io.File)}
   * .
   */
  public void testIsKnownExtension() {
    AnyFileFilter filter = AnyFileFilter.getInstance();
    assertTrue(filter.isKnownExtension(new File("anyfile.something")));
    assertTrue(filter.isKnownExtension(new File("anyfilewithoutextension")));
  }

  /**
   * Test method for {@link org.jajuk.util.filters.AnyFileFilter#getInstance()}.
   */
  public void testGetInstance() {
    assertNotNull(AnyFileFilter.getInstance());
  }
}
