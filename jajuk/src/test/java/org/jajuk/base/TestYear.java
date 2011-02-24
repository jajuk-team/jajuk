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
package org.jajuk.base;

import junit.framework.TestCase;

import org.jajuk.JUnitHelpers;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestYear extends TestCase {

  /**
   * Test method for {@link org.jajuk.base.Year#getDesc()}.
   */
  public void testGetDesc() {
    Year year = new Year("1", "1998");
    assertNotNull(year.getDesc());
    assertTrue(year.getDesc().contains("1998"));
  }

  /**
   * Test method for {@link org.jajuk.base.Year#getLabel()}.
   */
  public void testGetLabel() {
    Year year = new Year("1", "1998");
    assertEquals(Const.XML_YEAR, year.getLabel());
  }

  /**
   * Test method for {@link org.jajuk.base.Year#getIconRepresentation()}.
   */
  public void testGetIconRepresentation() {
    Year year = new Year("1", "1998");
    assertNotNull(year.getIconRepresentation());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Year#Year(java.lang.String, java.lang.String)}.
   */
  public void testYear() {
    Year year = new Year("1", "1998");
    assertNotNull(year);
  }

  /**
   * Test method for {@link org.jajuk.base.Year#toString()}.
   */
  public void testToString() {
    JUnitHelpers.ToStringTest(new Year("1", "1998"));
    JUnitHelpers.ToStringTest(new Year("1", null));
    JUnitHelpers.ToStringTest(new Year(null, "1998"));
    JUnitHelpers.ToStringTest(new Year(null, null));
  }

  /**
   * Test method for {@link org.jajuk.base.Year#getValue()}.
   */
  public void testGetValue() {
    Year year = new Year("1", "1998");
    assertEquals(1998l, year.getValue());
  }

  /**
   * Test method for {@link org.jajuk.base.Year#compareTo(org.jajuk.base.Year)}.
   */
  public void testCompareTo() {
    Year year = new Year("1", "1998");
    Year yeareq = new Year("2", "1998");
    Year yearne = new Year("3", "1997");
    JUnitHelpers.CompareToTest(year, yeareq, yearne);
  }

  /**
   * Test method for {@link org.jajuk.base.Year#getName2()}.
   */
  public void testGetName2() {
    Year year = new Year("1", "1998");
    assertNotNull(year.getName2());

    // test with zero-year
    year = new Year("1", "0");
    assertNotNull(year.getName2());
  }

  /**
   * Test method for {@link org.jajuk.base.Year#looksValid()}.
   */
  public void testLooksValid() {
    // we currently check > 1000 and < 3000
    assertTrue(new Year("1", "1998").looksValid());
    assertTrue(new Year("1", "1001").looksValid());
    assertTrue(new Year("1", "2999").looksValid());

    assertFalse(new Year("1", "1000").looksValid());
    assertFalse(new Year("1", "-340").looksValid());
    assertFalse(new Year("1", "10000").looksValid());
  }

}
