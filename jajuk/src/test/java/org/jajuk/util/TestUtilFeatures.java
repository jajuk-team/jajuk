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
package org.jajuk.util;

import java.util.ArrayList;
import java.util.List;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.services.players.StackItem;

/**
 * DOCUMENT_ME.
 */
public class TestUtilFeatures extends JajukTestCase {

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.util.UtilFeatures#forcedShuffle(List<StackItem>)}.
   */
  public void testCopyFileFile() throws Exception {
    StackItem si1 = new StackItem(JUnitHelpers.getFile("1", true));
    StackItem si2 = new StackItem(JUnitHelpers.getFile("2", true));
    List<StackItem> items = new ArrayList<StackItem>(2);
    items.add(si1);
    items.add(si2);
    UtilFeatures.forcedShuffle(items);
    assertFalse(items.get(0).equals(si1));
  }

  // helper method to emma-coverage of the unused constructor
  /**
   * Test private constructor.
   * DOCUMENT_ME
   *
   * @throws Exception the exception
   */
  public void testPrivateConstructor() throws Exception {
    // For EMMA code-coverage tests
    JUnitHelpers.executePrivateConstructor(UtilFeatures.class);
  }
}
