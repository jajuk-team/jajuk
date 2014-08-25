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
package org.jajuk.services.dj;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;

/**
 * .
 */
public class TestAmbience extends JajukTestCase {
  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#hashCode()}.
   */
  public final void testHashCode() {
    Ambience amb = new Ambience("1", "name");
    Ambience equal = new Ambience("1", "name");
    TestHelpers.HashCodeTest(amb, equal);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#Ambience(java.lang.String, java.lang.String, java.util.Set)}
   * .
   */
  public final void testAmbienceStringStringSetOfGenre() {
    Set<Genre> genres = new HashSet<Genre>();
    genres.add(TestHelpers.getGenre("mygenre"));
    new Ambience("1", "name", genres);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#Ambience(java.lang.String, java.lang.String, java.lang.String[])}
   * .
   */
  public final void testAmbienceStringStringStringArray() {
    GenreManager.getInstance().registerGenre("anothergenre");
    GenreManager.getInstance().registerGenre("yetanothergenre");
    // try with one unknown genre here...
    new Ambience("1", "name", new String[] { "anothergenre", "yetanothergenre", "unknowngenre" });
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#Ambience(java.lang.String, java.lang.String)}
   * .
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
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#addGenre(org.jajuk.base.Genre)}.
   */
  public final void testAddGenre() {
    Ambience amb = new Ambience("1", "name");
    amb.addGenre(TestHelpers.getGenre("anothergenre"));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#removeGenre(org.jajuk.base.Genre)}.
   */
  public final void testRemoveGenre() {
    Ambience amb = new Ambience("1", "name");
    Genre genre = TestHelpers.getGenre("anothergenre");
    assertEquals(0, amb.getGenres().size());
    amb.addGenre(genre);
    assertEquals(1, amb.getGenres().size());
    amb.removeGenre(genre);
    assertEquals(0, amb.getGenres().size());
    // try it again
    amb.removeGenre(genre);
    assertEquals(0, amb.getGenres().size());
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
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#setName(java.lang.String)}.
   */
  public final void testSetName() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#getGenres()}.
   */
  public final void testGetGenres() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#setGenres(java.util.Set)}.
   */
  public final void testSetGenres() {
    Ambience amb = new Ambience("1", "name");
    assertEquals(0, amb.getGenres().size());
    Set<Genre> genres = new HashSet<Genre>();
    genres.add(TestHelpers.getGenre("mygenre"));
    genres.add(TestHelpers.getGenre("mygenre2"));
    genres.add(TestHelpers.getGenre("mygenre3"));
    amb.setGenres(genres);
    assertEquals(3, amb.getGenres().size());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#getGenresDesc()}.
   */
  public final void testGetGenresDesc() {
    Ambience amb = new Ambience("1", "name");
    // first an empty string results without any genre set
    assertEquals("", amb.getGenresDesc());
    // then add some genres
    amb.addGenre(TestHelpers.getGenre("mygenre"));
    amb.addGenre(TestHelpers.getGenre("mygenre2"));
    amb.addGenre(TestHelpers.getGenre("mygenre3"));
    assertTrue(StringUtils.isNotBlank(amb.getGenresDesc()));
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#toString()}.
   */
  public final void testToString() {
    Ambience amb = new Ambience("1", "name");
    TestHelpers.ToStringTest(amb);
    // also when some items are null
    amb = new Ambience(null, "name");
    TestHelpers.ToStringTest(amb);
    amb = new Ambience("1", null);
    TestHelpers.ToStringTest(amb);
    amb = new Ambience(null, null);
    TestHelpers.ToStringTest(amb);
    // also with genres
    amb = new Ambience("9", "name0987");
    amb.addGenre(TestHelpers.getGenre("mygenre"));
    amb.addGenre(TestHelpers.getGenre("mygenre2"));
    TestHelpers.ToStringTest(amb);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#equals(java.lang.Object)}.
   */
  public final void testEqualsObject() {
    Ambience amb = new Ambience("1", "name");
    Ambience equal = new Ambience("1", "name");
    // equals compares on name
    Ambience notequal1 = new Ambience("1", "name1");
    // and also compares on genres
    Ambience notequal2 = new Ambience("1", "name");
    notequal2.addGenre(TestHelpers.getGenre("mygenre2"));
    TestHelpers.EqualsTest(amb, equal, notequal1);
    TestHelpers.EqualsTest(amb, equal, notequal2);
  }

  /**
   * Test equals object2.
   * 
   */
  public final void testEqualsObject2() {
    Ambience amb = new Ambience("1", "name");
    amb.addGenre(TestHelpers.getGenre("mygenre4"));
    Ambience equal = new Ambience("1", "name");
    equal.addGenre(TestHelpers.getGenre("mygenre4"));
    // equals compares on name
    Ambience notequal1 = new Ambience("1", "name1");
    // and also compares on genres
    Ambience notequal2 = new Ambience("1", "name");
    notequal2.addGenre(TestHelpers.getGenre("mygenre2"));
    TestHelpers.EqualsTest(amb, equal, notequal1);
    TestHelpers.EqualsTest(amb, equal, notequal2);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.Ambience#compareTo(org.jajuk.services.dj.Ambience)}
   * .
   */
  public final void testCompareTo() {
    Ambience amb = new Ambience("1", "name");
    Ambience equal = new Ambience("1", "name");
    Ambience notequal = new Ambience("1", "name1");
    // only compares on name
    TestHelpers.CompareToTest(amb, equal, notequal);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.Ambience#toXML()}.
   */
  public final void testToXML() {
    Ambience amb = new Ambience("1", "name");
    // just returns a comma-separated list, not a full XML here...
    // try without any genres, returns an empty string
    assertEquals("", amb.toXML());
    // then add some
    amb.addGenre(TestHelpers.getGenre("mygenre4123"));
    assertTrue(StringUtils.isNotBlank(amb.toXML()));
    // and then some more
    amb.addGenre(TestHelpers.getGenre("mygenre4234"));
    amb.addGenre(TestHelpers.getGenre("mygenre834874"));
  }
}
