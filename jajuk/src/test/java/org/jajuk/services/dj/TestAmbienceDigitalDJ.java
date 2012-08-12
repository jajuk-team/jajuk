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

import ext.services.xml.XMLUtils;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestAmbienceDigitalDJ extends JajukTestCase {
  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceDigitalDJ#toXML()}.
   */
  public final void testToXML() {
    AmbienceDigitalDJ dj = new AmbienceDigitalDJ("3");
    assertTrue(StringUtils.isNotBlank(dj.toXML()));
    // try to parse the resulting XML
    XMLUtils.getDocument(dj.toXML());
    // set an Ambience
    GenreManager.getInstance().registerGenre("mystyle");
    dj.setAmbience(new Ambience("5", "ambience", new String[] { "mystyle" }));
    // try to parse the resulting XML
    XMLUtils.getDocument(dj.toXML());
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.dj.AmbienceDigitalDJ#generatePlaylist()}.
   */
  public final void testGeneratePlaylist() throws Exception {
    StartupCollectionService.registerItemManagers();
    AmbienceDigitalDJ dj = new AmbienceDigitalDJ("4");
    // empty without Ambience set
    assertEquals(0, dj.generatePlaylist().size());
    // set an Ambience
    Genre genre = GenreManager.getInstance().registerGenre("mystyle");
    dj.setAmbience(new Ambience("5", "ambience", new String[] { "mystyle" }));
    getFile(6, genre);
    // assert a few conditions to find out why this test fails sometimes when
    // run in combination with others
    assertFalse(dj.isTrackUnicity());
    assertTrue(FileManager.getInstance().getGlobalShufflePlaylist().size() > 0);
    assertTrue(dj
        .getAmbience()
        .getGenres()
        .contains(FileManager.getInstance().getGlobalShufflePlaylist().get(0).getTrack().getGenre()));
    assertEquals(Const.MIN_TRACKS_NUMBER_WITHOUT_UNICITY, dj.generatePlaylist().size());
    // once again with "unicity"
    dj.setTrackUnicity(true);
    assertEquals(1, dj.generatePlaylist().size());
  }

  /**
   * Gets the file.
   *
   * @param i 
   * @param genre 
   * @return the file
   * @throws Exception the exception
   */
  private File getFile(int i, Genre genre) throws Exception {
    Album album = JUnitHelpers.getAlbum("myalbum", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test
    Artist artist = JUnitHelpers.getArtist("name");
    Year year = JUnitHelpers.getYear(2000);
    // IPlayerImpl imp = new MockPlayer();
    // Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();
    Type type = JUnitHelpers.getType();
    Track track = TrackManager.getInstance().registerTrack("name", album, genre, artist, 120, year,
        1, type, 1);
    Device device = JUnitHelpers.getDevice();
    device.mount(true);
    Directory dir = DirectoryManager.getInstance().registerDirectory(device);
    return FileManager.getInstance().registerFile(Integer.valueOf(i).toString(), "test.tst", dir,
        track, 120, 70);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceDigitalDJ#AmbienceDigitalDJ(java.lang.String)}
   * .
   */
  public final void testAmbienceDigitalDJ() {
    new AmbienceDigitalDJ("9");
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceDigitalDJ#getAmbience()}.
   */
  public final void testGetAndSetAmbience() {
    AmbienceDigitalDJ dj = new AmbienceDigitalDJ("4");
    // empty without Ambience set
    assertNull(dj.getAmbience());
    // set an Ambience
    GenreManager.getInstance().registerGenre("mystyle");
    dj.setAmbience(new Ambience("5", "ambience", new String[] { "mystyle" }));
    assertNotNull(dj.getAmbience());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceDigitalDJ#setAmbience(org.jajuk.services.dj.Ambience)}
   * .
   */
  public final void testSetAmbience() {
    // tested above
  }
}
