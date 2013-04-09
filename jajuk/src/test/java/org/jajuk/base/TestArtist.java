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
package org.jajuk.base;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestArtist extends JajukTestCase {
  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.base.Artist#getTitle()}.
   */
  public final void testGetDesc() {
    Artist artist = TestHelpers.getArtist();
    assertTrue(StringUtils.isNotBlank(artist.getTitle()));
  }

  /**
   * Test method for {@link org.jajuk.base.Artist#getXMLTag()}.
   */
  public final void testGetLabel() {
    Artist artist = TestHelpers.getArtist();
    assertEquals(Const.XML_ARTIST, artist.getXMLTag());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.Artist#getHumanValue(java.lang.String)}.
   */
  public final void testGetHumanValue() {
    // need ArtistManager for MetaInformation here...
    StartupCollectionService.registerItemManagers();
    Artist artist = TestHelpers.getArtist();
    assertEquals("name", artist.getHumanValue(Const.XML_NAME));
  }

  /**
   * Test method for {@link org.jajuk.base.Artist#getIconRepresentation()}.
   */
  public final void testGetIconRepresentation() {
    Artist artist = TestHelpers.getArtist();
    assertNotNull(artist.getIconRepresentation());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.Artist#Artist(java.lang.String, java.lang.String)}.
   */
  public final void testArtist() {
    TestHelpers.getArtist();
  }

  /**
   * Test method for {@link org.jajuk.base.Artist#getName2()}.
   */
  public final void testGetName2() {
    Artist artist = TestHelpers.getArtist();
    // usually equal to getName()
    assertEquals("name", artist.getName2());
    // only different for unknown_artist
    artist = TestHelpers.getArtist(Const.UNKNOWN_ARTIST);
    // should be replaced by some localized string
    assertFalse(artist.getName2().equals(Const.UNKNOWN_ARTIST));
  }

  /**
   * Test method for {@link org.jajuk.base.Artist#toString()}.
   */
  public final void testToString() {
    Artist artist = new Artist("1", "name");
    TestHelpers.ToStringTest(artist);
    artist = new Artist("1", null);
    TestHelpers.ToStringTest(artist);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.Artist#compareTo(org.jajuk.base.Artist)}.
   */
  public final void testCompareTo() {
    Artist artist = new Artist("1", "name");
    Artist equal = new Artist("1", "name");
    Artist notequal1 = new Artist("1", "name2");
    Artist notequal2 = new Artist("2", "name");
    TestHelpers.CompareToTest(artist, equal, notequal1);
    TestHelpers.CompareToTest(artist, equal, notequal2);
  }

  /**
   * Test method for {@link org.jajuk.base.Artist#isUnknown()}.
   */
  public final void testIsUnknown() {
    Artist artist = TestHelpers.getArtist();
    assertFalse(artist.isUnknown());
    artist = TestHelpers.getArtist(Const.UNKNOWN_ARTIST);
    assertTrue(artist.isUnknown());
  }
}
