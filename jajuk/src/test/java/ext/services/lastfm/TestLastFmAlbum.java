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
 *  $Revision: 3132 $
 */
package ext.services.lastfm;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.CallException;
import net.roarsoftware.lastfm.Playlist;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.util.IconLoader;
import org.jajuk.util.UtilString;

/**
 * 
 */
public class TestLastFmAlbum extends JajukTestCase {
  private static final String API_KEY = "711591ss6q695ps349o6681pr1oq1467";

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#getAlbum(net.roarsoftware.lastfm.Album, net.roarsoftware.lastfm.Playlist)}
   * .
   */
  public void testGetAlbum() {
    Album a = Album.getInfo("Red Hot Chilli Peppers", "By The Way", UtilString.rot13(API_KEY));
    assertNotNull(a);
    LastFmAlbum.getAlbum(a, null);
  }

  public void testGetAlbumPlaylist() {
    Album a = Album.getInfo("Red Hot Chilli Peppers", "By The Way", UtilString.rot13(API_KEY));
    assertNotNull(a);

    try { // may fail if internet is not available
      Playlist p = Playlist.fetchAlbumPlaylist(a.getId(), UtilString.rot13(API_KEY));
      assertNotNull(p);
    } catch (CallException e) {
      // ignore for now if it contains an UnknownHostException inside
      assertTrue(e.getMessage(), e.getCause() instanceof UnknownHostException);
    }

    /**
     * TODO: find out how to get a Session here...
     * 
     * <code>
    Session session = null;
    Playlist.addTrack(p.getId(), "Red Hot Chilli Peppers", "By The Way", null);
    LastFmAlbum.getAlbum(a, p);
    </code>
     */
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getArtist()}.
   */
  public void testGetArtist() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getArtist());
    album.setArtist("artist");
    assertEquals("artist", album.getArtist());
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getArtistUrl()}.
   */
  public void testGetArtistUrl() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getArtistUrl());

    album.setUrl("testurl");
    assertEquals("testurl", album.getArtistUrl());

    // cuts off after last path...
    album.setUrl("http://test.url/url1/url2/url3/test123.html");
    assertEquals("http://test.url/url1/url2/url3", album.getArtistUrl());

  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getBigCoverURL()}.
   */
  public void testGetBigCoverURL() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getBigCoverURL());
    album.setBigCoverURL("bigurl");
    assertEquals("bigurl", album.getBigCoverURL());

  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getCover()}.
   */
  public void testGetCover() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getCover());

    assertNotNull(IconLoader.getNoCoverIcon(50));
    album.setCover(IconLoader.getNoCoverIcon(50));
    assertNotNull(album.getCover());
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getCoverURL()}.
   */
  public void testGetCoverURL() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getCoverURL());
    album.setCoverURL("coverurl");
    assertEquals("coverurl", album.getCoverURL());
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getReleaseDate()}.
   * 
   * @throws Exception
   */
  public void testGetReleaseDate() throws Exception {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getReleaseDate());
    album.setReleaseDateString("1 January 2009, 00:00");
    assertEquals(new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ENGLISH)
        .parse("1 January 2009, 00:00"), album.getReleaseDate());

  }

  public void testGetReleaseDateInvalid() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getReleaseDate());
    album.setReleaseDateString("Invalid date...");
    assertNull(album.getReleaseDate());
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#getReleaseDateString()}.
   */
  public void testGetReleaseDateString() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getReleaseDateString());
    album.setReleaseDateString("1 January 2009, 00:00");
    assertTrue(album.getReleaseDate().toString().contains("2009"));
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getSmallCoverURL()}.
   */
  public void testGetSmallCoverURL() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getSmallCoverURL());
    album.setSmallCoverURL("smallurl");
    assertEquals("smallurl", album.getSmallCoverURL());
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getTitle()}.
   */
  public void testGetTitle() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getTitle());
    album.setTitle("title");
    assertEquals("title", album.getTitle());
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getTracks()}.
   */
  public void testGetTracks() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getTracks());
    ArrayList<TrackInfo> tracks = new ArrayList<TrackInfo>();
    album.setTracks(tracks);
    assertNotNull(album.getTracks());

    album.setTracks(null);
    assertNull(album.getTracks());
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getUrl()}.
   */
  public void testGetUrl() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getUrl());
    album.setUrl("url");
    assertEquals("url", album.getUrl());
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#getYear()}.
   */
  public void testGetYear() {
    LastFmAlbum album = new LastFmAlbum();
    assertEquals("", album.getYear());
    album.setReleaseDateString("1 January 2009, 00:00");
    assertEquals("2009", album.getYear());
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setArtist(java.lang.String)}.
   */
  public void testSetArtist() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setBigCoverURL(java.lang.String)}.
   */
  public void testSetBigCoverURL() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setCover(javax.swing.ImageIcon)}.
   */
  public void testSetCover() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setCoverURL(java.lang.String)}.
   */
  public void testSetCoverURL() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setReleaseDateString(java.lang.String)}
   * .
   */
  public void testSetReleaseDateString() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setSmallCoverURL(java.lang.String)}.
   */
  public void testSetSmallCoverURL() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setTitle(java.lang.String)}.
   */
  public void testSetTitle() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setTracks(java.util.List)}.
   */
  public void testSetTracks() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbum#setUrl(java.lang.String)}.
   */
  public void testSetUrl() {
    // tested above
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbum#toString()}.
   */
  public void testToString() {
    LastFmAlbum album = new LastFmAlbum();
    JUnitHelpers.ToStringTest(album);

    album.setArtist("artist");
    album.setBigCoverURL("url");
    JUnitHelpers.ToStringTest(album);

    album.setTitle("title");
    JUnitHelpers.ToStringTest(album);
  }
}
