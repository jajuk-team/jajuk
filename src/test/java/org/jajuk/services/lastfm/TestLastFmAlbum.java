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
package org.jajuk.services.lastfm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.jajuk.services.lastfm.model.TrackInfo;
import org.jajuk.TestHelpers;
import org.jajuk.services.lastfm.model.LastFmAlbum;
import org.jajuk.util.IconLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * .
 */
public class TestLastFmAlbum {

  private LastFmAlbum getAlbumWithCover(String large, String url) {
    LastFmAlbum album = new LastFmAlbum();
    LastFmAlbum.ImageData bigCover = new LastFmAlbum.ImageData();
    bigCover.setSize(large);
    bigCover.setUrl(url);
    album.setImages(List.of(bigCover));
    return album;
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getArtist()}.
   */
  @Test
  public void testGetArtist() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getArtist());
    album.setArtist("artist");
    assertEquals("artist", album.getArtist());
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getArtistUrl()}.
   */
  @Test
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
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getBigCoverURL()}.
   */
  @Test
  public void testGetBigCoverURL() {
    LastFmAlbum album = getAlbumWithCover("extralarge", "bigurl");
    assertEquals("bigurl", album.getBigCoverURL());
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getCover()}.
   */
  @Test
  public void testGetCover() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getCover());
    assertNotNull(IconLoader.getNoCoverIcon(50));
    album.setCover(IconLoader.getNoCoverIcon(50));
    assertNotNull(album.getCover());
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getCoverURL()}.
   */
  @Test
  public void testGetCoverURL() {
    LastFmAlbum album = getAlbumWithCover("large", "coverurl");
    assertEquals("coverurl", album.getCoverURL());
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getReleaseDate()}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetReleaseDate() throws Exception {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getReleaseDate());
    album.setReleaseDateString("1 January 2009, 00:00");
    assertEquals(
        new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ENGLISH).parse("1 January 2009, 00:00"),
        album.getReleaseDate());
  }

  /**
   * Test get release date invalid.
   *
   */
  @Test
  public void testGetReleaseDateInvalid() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getReleaseDate());
    album.setReleaseDateString("Invalid date...");
    assertNull(album.getReleaseDate());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.lastfm.model.LastFmAlbum#getReleaseDateString()}.
   */
  @Test
  public void testGetReleaseDateString() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getReleaseDateString());
    album.setReleaseDateString("1 January 2009, 00:00");
    assertTrue(album.getReleaseDate().toString().contains("2009"));
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getSmallCoverURL()}.
   */
  @Test
  public void testGetSmallCoverURL() {
    LastFmAlbum album = getAlbumWithCover("small", "smallurl");
    assertEquals("smallurl", album.getSmallCoverURL());
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getTitle()}.
   */
  @Test
  public void testGetTitle() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getTitle());
    album.setTitle("title");
    assertEquals("title", album.getTitle());
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getTracks()}.
   */
  @Test
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
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getUrl()}.
   */
  @Test
  public void testGetUrl() {
    LastFmAlbum album = new LastFmAlbum();
    assertNull(album.getUrl());
    album.setUrl("url");
    assertEquals("url", album.getUrl());
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#getYear()}.
   */
  @Test
  public void testGetYear() {
    LastFmAlbum album = new LastFmAlbum();
    assertEquals("", album.getYear());
    album.setReleaseDateString("1 January 2009, 00:00");
    assertEquals("2009", album.getYear());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.lastfm.model.LastFmAlbum#setArtist(java.lang.String)}.
   */
  @Test
  public void testSetArtist() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbum#toString()}.
   */
  @Test
  public void testToString() {
    LastFmAlbum album = new LastFmAlbum();
    TestHelpers.ToStringTest(album);
    album.setArtist("artist");
    album.setUrl("url");
    TestHelpers.ToStringTest(album);
    album.setTitle("title");
    TestHelpers.ToStringTest(album);
  }
}
