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
package org.jajuk.ui.helpers;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.junit.Test;

public class TestDuration extends JajukTestCase {
  /**
   * Test method for {@link org.jajuk.ui.helpers.Duration#hashCode()}.
   */
  @Test
  public void testHashCode() {
    Duration dur = new Duration(123);
    Duration equ = new Duration(123);
    TestHelpers.HashCodeTest(dur, equ);
  }

  /**
   * Test method for {@link org.jajuk.ui.helpers.Duration#Duration(long)}.
   */
  @Test
  public void testDuration() {
    new Duration(23);
  }

  /**
   * Test method for {@link org.jajuk.ui.helpers.Duration#toString()}.
   */
  @Test
  public void testToString() {
    TestHelpers.ToStringTest(new Duration(993));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.helpers.Duration#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    Duration dur = new Duration(234);
    Duration equal = new Duration(234);
    Duration notequal = new Duration(233);
    TestHelpers.EqualsTest(dur, equal, notequal);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.helpers.Duration#compareTo(org.jajuk.ui.helpers.Duration)}
   * .
   */
  @Test
  public void testCompareTo() {
    Duration dur = new Duration(234);
    Duration equal = new Duration(234);
    Duration notequal = new Duration(233);
    TestHelpers.CompareToTest(dur, equal, notequal);
  }

  /**
   * Test method for {@link org.jajuk.ui.helpers.Duration#getDuration()}.
   */
  @Test
  public void testGetDuration() {
    Duration dur = new Duration(234);
    Duration dur2 = new Duration(233);
    assertEquals(234, dur.getDuration());
    assertEquals(233, dur2.getDuration());
  }
}
