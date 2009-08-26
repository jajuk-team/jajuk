/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;

import junit.framework.TestCase;

/**
 * 
 */
public class TestAuthor extends TestCase {

  /**
   * Test method for {@link org.jajuk.base.Author#getDesc()}.
   */
  public final void testGetDesc() {
    Author author = new Author("1", "name");
    assertTrue(StringUtils.isNotBlank(author.getDesc()));
  }

  /**
   * Test method for {@link org.jajuk.base.Author#getLabel()}.
   */
  public final void testGetLabel() {
    Author author = new Author("1", "name");
    assertEquals(Const.XML_AUTHOR, author.getLabel());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Author#getHumanValue(java.lang.String)}.
   */
  public final void testGetHumanValue() {
    // need AuthorManager for MetaInformation here...
    StartupCollectionService.registerItemManagers();
    
    Author author = new Author("1", "name");
    assertEquals("name", author.getHumanValue(Const.XML_NAME));
    assertEquals("1", author.getHumanValue(Const.XML_ID));
  }

  /**
   * Test method for {@link org.jajuk.base.Author#getIconRepresentation()}.
   */
  public final void testGetIconRepresentation() {
    Author author = new Author("1", "name");
    assertNotNull(author.getIconRepresentation());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Author#Author(java.lang.String, java.lang.String)}.
   */
  public final void testAuthor() {
    new Author("1", "name");
  }

  /**
   * Test method for {@link org.jajuk.base.Author#getName2()}.
   */
  public final void testGetName2() {
    Author author = new Author("1", "name");
    
    // usually equal to getName()
    assertEquals("name", author.getName2());
    
    // only different for unknown_author
    author = new Author("2", Const.UNKNOWN_AUTHOR);
    // should be replaced by some localized string
    assertFalse(author.getName2().equals(Const.UNKNOWN_AUTHOR));
  }

  /**
   * Test method for {@link org.jajuk.base.Author#toString()}.
   */
  public final void testToString() {
    Author author = new Author("1", "name");
    JUnitHelpers.ToStringTest(author);
    
    author = new Author("1", null);
    JUnitHelpers.ToStringTest(author);
  }

  /**
   * Test method for
   * {@link org.jajuk.base.Author#compareTo(org.jajuk.base.Author)}.
   */
  public final void testCompareTo() {
    Author author = new Author("1", "name");
    Author equal = new Author("1", "name");
    Author notequal1 = new Author("1", "name2");
    Author notequal2 = new Author("2", "name");
    
    JUnitHelpers.CompareToTest(author, equal, notequal1);
    JUnitHelpers.CompareToTest(author, equal, notequal2);
  }

  /**
   * Test method for {@link org.jajuk.base.Author#isUnknown()}.
   */
  public final void testIsUnknown() {
    Author author = new Author("1", "name");
    assertFalse(author.isUnknown());
    
    author = new Author("1", Const.UNKNOWN_AUTHOR);
    assertTrue(author.isUnknown());
  }

}
