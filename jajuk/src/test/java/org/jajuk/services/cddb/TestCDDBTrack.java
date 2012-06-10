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
package org.jajuk.services.cddb;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestCDDBTrack extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.cddb.CDDBTrack#CDDBTrack(org.jajuk.base.Track)}.
   */
  public final void testCDDBTrack() {
    new CDDBTrack(null);
  }

  /**
   * Test method for {@link org.jajuk.services.cddb.CDDBTrack#getLength()}.
   */
  public final void testGetLength() {
    CDDBTrack track = new CDDBTrack(getTrack(1));
    assertEquals(120, track.getLength());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.cddb.CDDBTrack#getPreciseLength()}.
   */
  public final void testGetPreciseLength() {
    CDDBTrack track = new CDDBTrack(getTrack(1));
    assertEquals(120f, track.getPreciseLength());
  }

  /**
   * Test method for {@link org.jajuk.services.cddb.CDDBTrack#getTrack()}.
   */

  public final void testGetTrack() {
    CDDBTrack track = new CDDBTrack(getTrack(1));
    assertNotNull(track.getTrack());
  }

  /**
   * Test method for {@link org.jajuk.services.cddb.CDDBTrack#toString()}.
   */

  public final void testToString() {
    StartupCollectionService.registerItemManagers();

    CDDBTrack track = new CDDBTrack(getTrack(1));
    JUnitHelpers.ToStringTest(track);
  }

  /**
   * Gets the track.
   *
   * @param i 
   * @return the track
   */
  private Track getTrack(int i) {
    Genre genre = JUnitHelpers.getGenre();
    Album album = JUnitHelpers.getAlbum("myalbum", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test

    Artist artist = JUnitHelpers.getArtist("name");
    Year year = JUnitHelpers.getYear(2000);

    Type type = JUnitHelpers.getType();
    return TrackManager.getInstance().registerTrack("name" + i, album, genre, artist, 120, year, 1,
        type, 1);
  }

}
