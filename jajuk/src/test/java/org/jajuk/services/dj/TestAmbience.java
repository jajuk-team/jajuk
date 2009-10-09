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
package org.jajuk.services.dj;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;

import org.jajuk.JajukTestCase;

/**
 * 
 */
public class TestAmbience extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#hashCode()}.
   */
  public final void testHashCode() {
    Ambience amb = new Ambience("1", "name");
    Ambience equal = new Ambience("1", "name");
    
    JUnitHelpers.HashCodeTest(amb, equal);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#Ambience(java.lang.String, java.lang.String, java.util.Set)}.
   */
  public final void testAmbienceStringStringSetOfStyle() {
    Set<Style> styles = new HashSet<Style>();
    styles.add(new Style("3", "mystyle"));
    
    new Ambience("1", "name", styles);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#Ambience(java.lang.String, java.lang.String, java.lang.String[])}.
   */
  public final void testAmbienceStringStringStringArray() {
    StyleManager.getInstance().registerStyle("anotherstyle");
    StyleManager.getInstance().registerStyle("yetanotherstyle");
    
    // try with one unknown style here...
    new Ambience("1", "name", new String[] { "anotherstyle", "yetanotherstyle", "unknownstyle" });
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#Ambience(java.lang.String, java.lang.String)}.
   */
  public final void testAmbienceStringString() {
    new Ambience("1", "name");
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#Ambience()}.
   */
  public final void testAmbience() {
    new Ambience();
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#addStyle(org.jajuk.base.Style)}.
   */
  public final void testAddStyle() {
    Ambience amb = new Ambience("1", "name");
    
    amb.addStyle(new Style("1", "anotherstyle"));
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#removeStyle(org.jajuk.base.Style)}.
   */
  public final void testRemoveStyle() {
    Ambience amb = new Ambience("1", "name");
    
    Style style = new Style("1", "anotherstyle");

    assertEquals(0, amb.getStyles().size());
    
    amb.addStyle(style);

    assertEquals(1, amb.getStyles().size());

    amb.removeStyle(style);

    assertEquals(0, amb.getStyles().size());
    
    // try it again
    amb.removeStyle(style);

    assertEquals(0, amb.getStyles().size());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#getName()}.
   */
  public final void testGetName() {
    Ambience amb = new Ambience("7", "name123");
    assertEquals("name123", amb.getName());
    
    amb.setName("123n");
    assertEquals("123n", amb.getName());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#getID()}.
   */
  public final void testGetID() {
    Ambience amb = new Ambience("8", "name1234");
    assertEquals("8", amb.getID());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#setName(java.lang.String)}.
   */
  public final void testSetName() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#getStyles()}.
   */
  public final void testGetStyles() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#setStyles(java.util.Set)}.
   */
  public final void testSetStyles() {
    Ambience amb = new Ambience("1", "name");
  
    assertEquals(0, amb.getStyles().size());

    Set<Style> styles = new HashSet<Style>();
    styles.add(new Style("3", "mystyle"));
    styles.add(new Style("4", "mystyle2"));
    styles.add(new Style("5", "mystyle3"));

    amb.setStyles(styles);
    
    assertEquals(3, amb.getStyles().size());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#getStylesDesc()}.
   */
  public final void testGetStylesDesc() {
    Ambience amb = new Ambience("1", "name");
    
    // first an empty string results without any style set
    assertEquals("", amb.getStylesDesc());
    
    // then add some styles
    amb.addStyle(new Style("3", "mystyle"));
    amb.addStyle(new Style("4", "mystyle2"));
    amb.addStyle(new Style("5", "mystyle3"));

    assertTrue(StringUtils.isNotBlank(amb.getStylesDesc()));
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#toString()}.
   */
  public final void testToString() {
    Ambience amb = new Ambience("1", "name");
    JUnitHelpers.ToStringTest(amb);

    // also when some items are null
    amb = new Ambience(null, "name");
    JUnitHelpers.ToStringTest(amb);
    amb = new Ambience("1", null);
    JUnitHelpers.ToStringTest(amb);
    amb = new Ambience(null, null);
    JUnitHelpers.ToStringTest(amb);
    
    // also with styles
    amb = new Ambience("9", "name0987");
    amb.addStyle(new Style("3", "mystyle"));
    amb.addStyle(new Style("4", "mystyle2"));
    JUnitHelpers.ToStringTest(amb);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#equals(java.lang.Object)}.
   */
  public final void testEqualsObject() {
    Ambience amb = new Ambience("1", "name");
    Ambience equal = new Ambience("1", "name");
    
    // equals compares on name
    Ambience notequal1 = new Ambience("1", "name1");

    // and also compares on styles
    Ambience notequal2 = new Ambience("1", "name");
    notequal2.addStyle(new Style("4", "mystyle2"));
    
    JUnitHelpers.EqualsTest(amb, equal, notequal1);
    JUnitHelpers.EqualsTest(amb, equal, notequal2);
  }

  public final void testEqualsObject2() {
    Ambience amb = new Ambience("1", "name");
    amb.addStyle(new Style("4", "mystyle4"));
    Ambience equal = new Ambience("1", "name");
    equal.addStyle(new Style("4", "mystyle4"));
    
    // equals compares on name
    Ambience notequal1 = new Ambience("1", "name1");

    // and also compares on styles
    Ambience notequal2 = new Ambience("1", "name");
    notequal2.addStyle(new Style("5", "mystyle2"));
    
    JUnitHelpers.EqualsTest(amb, equal, notequal1);
    JUnitHelpers.EqualsTest(amb, equal, notequal2);
  }
  
  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#compareTo(org.jajuk.services.dj.Ambience)}.
   */
  public final void testCompareTo() {
    Ambience amb = new Ambience("1", "name");
    Ambience equal = new Ambience("1", "name");
    Ambience notequal = new Ambience("1", "name1");

    // only compares on name
    JUnitHelpers.CompareToTest(amb, equal, notequal);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#toXML()}.
   */
  public final void testToXML() {
    Ambience amb = new Ambience("1", "name");
    
    // just returns a comma-separated list, not a full XML here...

    // try without any styles, returns an empty string
    assertEquals("", amb.toXML());

    // then add some
    amb.addStyle(new Style("11", "mystyle4123"));
    assertTrue(StringUtils.isNotBlank(amb.toXML()));
    
    // and then some more
    amb.addStyle(new Style("12", "mystyle4234"));
    amb.addStyle(new Style("13", "mystyle834874"));
  }
}
