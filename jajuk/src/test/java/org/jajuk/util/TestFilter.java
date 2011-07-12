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
package org.jajuk.util;

import java.util.ArrayList;
import java.util.List;

import org.jajuk.JajukTestCase;
import org.jajuk.base.Item;

/**
 * DOCUMENT_ME.
 */
public class TestFilter extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.Filter#Filter(java.lang.String, java.lang.String, boolean, boolean)}
   * .
   */
  public final void testFilter() {
    Filter filter = new Filter("test", "test", true, false);
    assertTrue(filter.isHuman());
    assertFalse(filter.isExact());

    filter = new Filter("test", "test", false, true);
    assertFalse(filter.isHuman());
    assertTrue(filter.isExact());
  }

  /**
   * Test method for {@link org.jajuk.util.Filter#getProperty()}.
   */
  public final void testGetProperty() {
    Filter filter = new Filter("test1", "test2", true, false);
    assertEquals("test1", filter.getProperty());
  }

  /**
   * Test method for {@link org.jajuk.util.Filter#getValue()}.
   */
  public final void testGetValue() {
    Filter filter = new Filter("test1", "test2", true, false);
    assertEquals("test2", filter.getValue());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.Filter#filterItems(java.util.List, org.jajuk.util.Filter)}
   * .
   */
  public final void testFilterItems() {
    // works with empty filter
    Filter.filterItems(null, null);

    // returns with empty expression
    Filter filter = new Filter("any", null, true, false);
    Filter.filterItems(null, filter);

    List<Item> list = new ArrayList<Item>();

    // try to trigger a regex error
    filter = new Filter("any", "asdfas(sasdfsa", true, false);
    Filter.filterItems(list, filter);

    // works with useful filter
    filter = new Filter("any", "test", true, false);
    Filter.filterItems(list, filter);

    filter = new Filter("something", "test", true, false);
    Filter.filterItems(list, filter);

    // TODO: more sophisticated testing is missing here
  }

}
