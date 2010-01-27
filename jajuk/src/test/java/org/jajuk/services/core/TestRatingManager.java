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
package org.jajuk.services.core;

import java.util.Set;

import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.base.Year;
import org.jajuk.base.TestAlbumManager.MockPlayer;
import org.jajuk.base.TestAlbumManager.MyTagImpl;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.tags.ITagImpl;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestRatingManager extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.services.core.RatingManager#run()}.
   */
  public void testRun() {
    // cannot be tested, is an endless loop:
    // RatingManager.getInstance().run();
  }

  /**
   * Test method for {@link org.jajuk.services.core.RatingManager#getInstance()}
   * .
   */
  public void testGetInstance() {
    assertNotNull(RatingManager.getInstance());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.core.RatingManager#getMaxPlaycount()}.
   */
  public void testGetAndSetMaxPlaycount() {
    assertEquals(0, RatingManager.getMaxPlaycount());
    RatingManager.setMaxPlaycount(10);
    assertEquals(10, RatingManager.getMaxPlaycount());

    // set back to 0 as there is special handling
    RatingManager.setMaxPlaycount(0);
  }

  /**
   * Test method for
   * {@link org.jajuk.services.core.RatingManager#setMaxPlaycount(long)}.
   */
  public void testSetMaxPlaycount() {
    // tested above
  }

  /**
   * Test method for
   * {@link org.jajuk.services.core.RatingManager#hasRateChanged()}.
   */
  public void testHasAndSetRateChanged() {
    assertTrue(RatingManager.hasRateChanged());

    RatingManager.setRateHasChanged(false);

    assertFalse(RatingManager.hasRateChanged());

    RatingManager.setRateHasChanged(true);

    assertTrue(RatingManager.hasRateChanged());
  }

  /**
   * Test method for
   * {@link org.jajuk.services.core.RatingManager#setRateHasChanged(boolean)}.
   */
  public void testSetRateHasChanged() {
    // tested above
  }

  /**
   * Test method for
   * {@link org.jajuk.services.core.RatingManager#getRegistrationKeys()}.
   */
  public void testGetRegistrationKeys() {
    Set<JajukEvents> set = RatingManager.getInstance().getRegistrationKeys();

    assertTrue(set.toString(), set.contains(JajukEvents.RATE_RESET));
  }

  /**
   * Test method for
   * {@link org.jajuk.services.core.RatingManager#update(org.jajuk.events.JajukEvent)}
   * .
   * 
   * @throws Exception
   */
  public void testUpdate() throws Exception {
    StartupCollectionService.registerItemManagers();

    // update uses some Tracks
    getTrack(1);
    getTrack(2);

    RatingManager.getInstance().update(new JajukEvent(JajukEvents.RATE_RESET, null));
    RatingManager.getInstance().update(new JajukEvent(JajukEvents.PREFERENCES_RESET, null));
  }

  @SuppressWarnings("unchecked")
  private Track getTrack(int i) throws Exception {
    Genre genre = new Genre(Integer.valueOf(i).toString(), "name");
    Album album = new Album(Integer.valueOf(i).toString(), "name", 23);
    album.setProperty(Const.XML_ALBUM_COVER, Const.COVER_NONE); // don't read covers for
    // this test

    Artist artist = new Artist(Integer.valueOf(i).toString(), "name");
    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();
    ITagImpl tagimp = new MyTagImpl();
    Class<ITagImpl> tl = (Class<ITagImpl>) tagimp.getClass();

    Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", cl, tl);
    Track track = TrackManager.getInstance().registerTrack(Integer.valueOf(i).toString(), "name",
        album, genre, artist, 120, year, 1, type, 1);

    album.getTracksCache().add(track);

    Device device = new Device(Integer.valueOf(i).toString(), "name");
    device.setUrl(System.getProperty("java.io.tmpdir"));
    device.mount(true);

    Directory dir = new Directory(Integer.valueOf(i).toString(), "name", null, device);

    File file = new org.jajuk.base.File(Integer.valueOf(i).toString(), "test.tst", dir, track, 120,
        70);

    track.addFile(file);

    TypeManager.getInstance().registerType("test", "tst", cl, tl);

    return track;
  }

}
