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
 *  $Revision$
 */
package ext.services.lastfm;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;

/**
 * DOCUMENT_ME.
 */
public class TestLastFmCache extends JajukTestCase {

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // make sure the cache is cleaned up front to not find items from previous
    // runs
    LastFmCache cache = new LastFmCache();
    cache.clearCache();

    super.setUp();
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmCache#clearCache()}.
   */
  public void testClearCache() {
    LastFmCache cache = new LastFmCache();
    cache.clearCache();
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#retrieveAlbumCover(ext.services.lastfm.AlbumInfo)}
   * .
   */
  public void testRetrieveAlbumCover() {
    LastFmCache cache = new LastFmCache();

    LastFmAlbum album = new LastFmAlbum();
    album.setBigCoverURL("testurl");
    assertNull(cache.retrieveAlbumCover(album));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#retrieveAlbumInfo(java.lang.String, java.lang.String)}
   * .
   */
  public void testRetrieveAlbumInfo() {
    LastFmCache cache = new LastFmCache();

    assertNull(cache.retrieveAlbumInfo("Red Hot Chili Peppers", "By the way"));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#retrieveArtistInfo(java.lang.String)}
   * .
   */
  public void testRetrieveArtistInfo() {
    LastFmCache cache = new LastFmCache();

    assertNull(cache.retrieveArtistInfo("Red Hot Chili Peppers"));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#retrieveArtistImage(ext.services.lastfm.SimilarArtistsInfo)}
   * .
   */
  public void testRetrieveArtistImage() {
    LastFmCache cache = new LastFmCache();

    LastFmSimilarArtists artists = new LastFmSimilarArtists();
    artists.setArtistName("Red Hot Chili Peppers");

    assertNull(cache.retrieveArtistImage(artists));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#retrieveAlbumList(java.lang.String)}
   * .
   */
  public void testRetrieveAlbumList() {
    LastFmCache cache = new LastFmCache();

    assertNull(cache.retrieveAlbumList("Red Hot Chili Peppers"));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#retrieveArtistSimilar(java.lang.String)}
   * .
   */
  public void testRetrieveArtistSimilar() {
    LastFmCache cache = new LastFmCache();

    assertNull(cache.retrieveArtistSimilar("Red Hot Chili Peppers"));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#retrieveArtistThumbImage(ext.services.lastfm.ArtistInfo)}
   * .
   */
  public void testRetrieveArtistThumbImage() {
    LastFmCache cache = new LastFmCache();

    LastFmArtist artist = new LastFmArtist();
    artist.setName("Red Hot Chili Peppers");
    assertNull(cache.retrieveArtistThumbImage(artist));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#retrieveArtistWiki(java.lang.String)}
   * .
   */
  public void testRetrieveArtistWiki() {
    LastFmCache cache = new LastFmCache();

    assertNull(cache.retrieveArtistWiki("Red Hot Chili Peppers"));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#storeAlbumCover(ext.services.lastfm.AlbumInfo, java.awt.Image)}
   * .
   */
  public void testStoreAlbumCover() {
    LastFmCache cache = new LastFmCache();

    LastFmAlbum album = new LastFmAlbum();
    album.setBigCoverURL("testurl");

    Image cover = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

    cache.storeAlbumCover(album, cover);

    assertNotNull(cache.retrieveAlbumCover(album));
    assertEquals(10, cache.retrieveAlbumCover(album).getHeight(null));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#storeAlbumInfo(java.lang.String, java.lang.String, ext.services.lastfm.AlbumInfo)}
   * .
   */
  public void testStoreAlbumInfo() {
    LastFmCache cache = new LastFmCache();

    LastFmAlbum album = new LastFmAlbum();
    album.setBigCoverURL("testurl");

    cache.storeAlbumInfo("Red Hot Chili Peppers", "By the way", album);

    assertNotNull(cache.retrieveAlbumInfo("Red Hot Chili Peppers", "By the way"));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#storeArtistInfo(java.lang.String, ext.services.lastfm.ArtistInfo)}
   * .
   */
  public void testStoreArtistInfo() {
    LastFmCache cache = new LastFmCache();

    LastFmArtist artist = new LastFmArtist();
    artist.setName("Red Hot Chili Peppers");

    cache.storeArtistInfo("Red Hot Chili Peppers", artist);

    assertNotNull(cache.retrieveArtistInfo("Red Hot Chili Peppers"));
    assertEquals("Red Hot Chili Peppers", cache.retrieveArtistInfo("Red Hot Chili Peppers")
        .getName());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#storeArtistImage(ext.services.lastfm.SimilarArtistsInfo, java.awt.Image)}
   * .
   */
  public void testStoreArtistImage() {
    LastFmCache cache = new LastFmCache();

    LastFmSimilarArtists artists = new LastFmSimilarArtists();
    artists.setArtistName("Red Hot Chili Peppers");

    Image cover = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    cache.storeArtistImage(artists, cover);

    assertNotNull(cache.retrieveArtistImage(artists));
    assertEquals(10, cache.retrieveArtistImage(artists).getHeight(null));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#storeAlbumList(java.lang.String, ext.services.lastfm.AlbumListInfo)}
   * .
   */
  public void testStoreAlbumList() {
    LastFmCache cache = new LastFmCache();

    LastFmAlbumList list = new LastFmAlbumList();

    LastFmAlbum album1 = new LastFmAlbum();
    album1.setTitle("Test1");
    LastFmAlbum album2 = new LastFmAlbum();
    album2.setTitle("Test2");
    List<AlbumInfo> alist = new ArrayList<AlbumInfo>();
    alist.add(album1);
    alist.add(album2);

    list.setAlbums(alist);

    cache.storeAlbumList("Red Hot Chili Peppers", list);

    assertNotNull(cache.retrieveAlbumList("Red Hot Chili Peppers"));
    assertNotNull(cache.retrieveAlbumList("Red Hot Chili Peppers").getAlbums());
    assertEquals(cache.retrieveAlbumList("Red Hot Chili Peppers").getAlbums().toString(), 2, cache
        .retrieveAlbumList("Red Hot Chili Peppers").getAlbums().size());

    /*
     * assertTrue(cache.retrieveAlbumList("Red Hot Chili Peppers").getAlbums().toString
     * (),
     * cache.retrieveAlbumList("Red Hot Chili Peppers").getAlbums().contains(
     * album1));
     * assertTrue(cache.retrieveAlbumList("Red Hot Chili Peppers").getAlbums
     * ().toString(),
     * cache.retrieveAlbumList("Red Hot Chili Peppers").getAlbums(
     * ).contains(album2));
     */
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#storeArtistSimilar(java.lang.String, ext.services.lastfm.SimilarArtistsInfo)}
   * .
   */
  public void testStoreArtistSimilar() {
    LastFmCache cache = new LastFmCache();

    LastFmSimilarArtists artists = new LastFmSimilarArtists();
    artists.setArtistName("Hed Rot Phili Ceppers");

    cache.storeArtistSimilar("Red Hot Chili Peppers", artists);

    assertNotNull(cache.retrieveArtistSimilar("Red Hot Chili Peppers"));
    assertEquals("Hed Rot Phili Ceppers", cache.retrieveArtistSimilar("Red Hot Chili Peppers")
        .getArtistName());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#storeArtistThumbImage(ext.services.lastfm.ArtistInfo, java.awt.Image)}
   * .
   */
  public void testStoreArtistThumbImage() {
    LastFmCache cache = new LastFmCache();

    LastFmArtist artist = new LastFmArtist();
    artist.setName("Red Hot Chili Peppers");
    Image cover = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

    cache.storeArtistThumbImage(artist, cover);

    assertNotNull(cache.retrieveArtistThumbImage(artist));
    assertEquals(10, cache.retrieveArtistThumbImage(artist).getHeight(null));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#storeArtistWiki(java.lang.String, java.lang.String)}
   * .
   */
  public void testStoreArtistWiki() {
    LastFmCache cache = new LastFmCache();

    cache.storeArtistWiki("Red Hot Chili Peppers", "TestWikiText");

    assertNotNull(cache.retrieveArtistWiki("Red Hot Chili Peppers"));
    assertEquals("TestWikiText", cache.retrieveArtistWiki("Red Hot Chili Peppers"));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#addSubmissionData(ext.services.lastfm.FullSubmissionData)}
   * .
   */
  public void testAddSubmissionData() {
    LastFmCache cache = new LastFmCache();

    FullSubmissionData data = new FullSubmissionData("Red Hot Chili Peppers", "title",
        "By The Way", 10, 1, "Source", 10);

    cache.addSubmissionData(data);
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmCache#getSubmissionData()}
   * .
   */
  public void testGetSubmissionData() {
    // TODO: currently this fails because library xpp3 is not found for XML Pull
    // Parsing, not sure why that is not part of
    // Jajuk distribution. Are these methods used at all??
    /*
     * LastFmCache cache = new LastFmCache();
     * 
     * FullSubmissionData data = new FullSubmissionData("Red Hot Chili Peppers",
     * "title", "By The Way", 10, 1, "Source", 10);
     * 
     * cache.addSubmissionData(data);
     * 
     * assertEquals(1, cache.getSubmissionData().size());
     * assertEquals(data.getArtist(),
     * cache.getSubmissionData().get(0).getArtist());
     */
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmCache#removeSubmissionData()}.
   */
  public void testRemoveSubmissionData() {
    // TODO: currently this fails because library xpp3 is not found for XML Pull
    // Parsing, not sure why that is not part of
    // Jajuk distribution. Are these methods used at all??
    /*
     * LastFmCache cache = new LastFmCache();
     * 
     * FullSubmissionData data = new FullSubmissionData("Red Hot Chili Peppers",
     * "title", "By The Way", 10, 1, "Source", 10);
     * 
     * cache.addSubmissionData(data);
     * 
     * assertEquals(1, cache.getSubmissionData().size());
     * assertEquals(data.getArtist(),
     * cache.getSubmissionData().get(0).getArtist());
     * 
     * cache.removeSubmissionData();
     * 
     * assertEquals(0, cache.getSubmissionData().size());
     */
  }

}
