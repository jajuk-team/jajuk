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
package org.jajuk.services.core;

import java.util.Set;

import org.jajuk.JajukTestCase;
import org.jajuk.MockPlayer;
import org.jajuk.TestHelpers;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.TestAlbumManager.MyTagImpl;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.base.Year;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.tags.ITagImpl;
import org.jajuk.util.Const;

public class TestRatingManager extends JajukTestCase {
  /**
   * Test method for {@link org.jajuk.services.core.RatingService#run()}.
   */
  public void testRun() {
    // cannot be tested, is an endless loop:
  }

  /**
   * Test method for {@link org.jajuk.services.core.RatingService#getInstance()}
   * .
   */
  public void testGetInstance() {
    assertNotNull(RatingService.getInstance());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.RatingService#getMaxPlaycount()}.
   */
  public void testGetAndSetMaxPlaycount() {
    // Reset the rating manager
    RatingService.getInstance().update(new JajukEvent(JajukEvents.RATE_RESET, null));
    assertEquals(0, RatingService.getMaxPlaycount());
    RatingService.setMaxPlaycount(10);
    assertEquals(10, RatingService.getMaxPlaycount());
    // set back to 0 as there is special handling
    RatingService.setMaxPlaycount(0);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.RatingService#setMaxPlaycount(long)}.
   */
  public void testSetMaxPlaycount() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.core.RatingService#getRegistrationKeys()}.
   */
  public void testGetRegistrationKeys() {
    Set<JajukEvents> set = RatingService.getInstance().getRegistrationKeys();
    assertTrue(set.toString(), set.contains(JajukEvents.RATE_RESET));
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.core.RatingService#update(org.jajuk.events.JajukEvent)}
   * .
   */
  public void testUpdate() throws Exception {
    StartupCollectionService.registerItemManagers();
    // update uses some Tracks
    getTrack(1);
    getTrack(2);
    RatingService.getInstance().update(new JajukEvent(JajukEvents.RATE_RESET, null));
    RatingService.getInstance().update(new JajukEvent(JajukEvents.PREFERENCES_RESET, null));
  }

  /**
   * Gets the track.
   *
   * @param i 
   * @return the track
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private Track getTrack(int i) throws Exception {
    Genre genre = TestHelpers.getGenre();
    Album album = TestHelpers.getAlbum("name", 23);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test
    Artist artist = TestHelpers.getArtist("name");
    Year year = TestHelpers.getYear(2000);
    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();
    ITagImpl tagimp = new MyTagImpl();
    Class<ITagImpl> tl = (Class<ITagImpl>) tagimp.getClass();
    Type type = TestHelpers.getType();
    Track track = TrackManager.getInstance().registerTrack(Integer.valueOf(i).toString(), "name",
        album, genre, artist, 120, year, 1, type, 1);
    album.getTracksCache().add(track);
    Device device = TestHelpers.getDevice();
    Directory dir = DirectoryManager.getInstance().registerDirectory(device);
    File file = FileManager.getInstance().registerFile("test.tst", dir, track, 120, 70);
    track.addFile(file);
    TypeManager.getInstance().registerType("test", "tst", cl, tl);
    return track;
  }

  public void testGetRateForPreference() {
    assertEquals(RatingService.getRateForPreference(-3l), 0);
    assertEquals(RatingService.getRateForPreference(-2l), 17);
    assertEquals(RatingService.getRateForPreference(-1l), 33);
    assertEquals(RatingService.getRateForPreference(0l), 50);
    assertEquals(RatingService.getRateForPreference(1l), 67);
    assertEquals(RatingService.getRateForPreference(2l), 83);
    assertEquals(RatingService.getRateForPreference(3l), 100);
  }
}
