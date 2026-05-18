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

import org.jajuk.services.lastfm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestLastFmClient {

  public static final String HTTPS_LASTFM = "https://lastfm";
  private final LastFmClient client = new LastFmClient();

  @Test
  public void testGetAlbum() throws Exception {
    // 1. Call the new client directly (replaces Album.getInfo)
    // Note: UtilString.rot13 is no longer necessary as the Last.fm API expects the raw key
    String albumTitle = "By the Way";
    String albumArtist = "Red Hot Chili Peppers";
    AlbumInfo a = client.getAlbumInfo(albumArtist, albumTitle);

    // 2. JUnit 5 Assertions - Validate all fields

    // General checks
    assertNotNull(a, "Album object must not be null");

    // Id check
    assertNotNull(a.getId(), "Album id must not be null");

    // Title check
    assertEquals(albumTitle, a.getTitle(), "Album title must match 'By The Way'");

    // Artist check
    assertEquals(albumArtist, a.getArtist(), "Artist name must match 'Red Hot Chili Peppers'");

    // URL check (should be a valid Last.fm URL)
    assertNotNull(a.getUrl(), "Album URL must not be null");
    assertTrue(a.getUrl().contains("last.fm"), "Album URL must contain 'last.fm'");

    // Release Date check (often present, but can be null for some albums)
    // We assert it's not null for this specific known album, or check format if present
    if (a.getReleaseDateString() != null) {
      assertFalse(a.getReleaseDateString().isEmpty(), "Release date string should not be empty if present");
      System.out.println("Release Date: " + a.getReleaseDateString());
    } else {
      // Optional: Some albums might not have a release date in the API response
      System.out.println("Warning: Release date is null for this album.");
    }

    // Cover Images checks
    // Big Cover URL (extralarge)
    String bigCover = a.getBigCoverURL();
    assertNotNull(bigCover, "Big Cover URL (extralarge) must not be null");
    assertTrue(bigCover.contains(HTTPS_LASTFM), "Big Cover URL must be valid");
    System.out.println("Big Cover URL: " + bigCover);

    // Cover URL (large)
    String cover = a.getCoverURL();
    assertNotNull(cover, "Cover URL (large) must not be null");
    System.out.println("Cover URL: " + cover);

    // Small Cover URL (small)
    String smallCover = a.getSmallCoverURL();
    assertNotNull(smallCover, "Small Cover URL (small) must not be null");
    System.out.println("Small Cover URL: " + smallCover);

    // Tracks check
    assertNotNull(a.getTracks(), "Tracks list must not be null");
    assertFalse(a.getTracks().isEmpty(), "Tracks list must not be empty for 'By The Way'");

    // Validate at least the first track
    String firstTrackName = a.getTracks().get(0).getTitle();
    assertNotNull(firstTrackName, "First track name must not be null");
    System.out.println("First Track: " + firstTrackName);

    // By The Way has 16 tracks on lastfm
    assertEquals(16, a.getTracks().size(), "Album " + albumTitle + " should have 16 tracks");

    if (a instanceof LastFmAlbum) {
      ((LastFmAlbum) a).loadCoverImage();
      // ImageIcon check loaded by the client
      assertNotNull(a.getCover(), "Cover ImageIcon should not be null initially");
    }
  }

  @Test
  public void testGetArtist() throws Exception {
    // 1. Client Call
    ArtistInfo artist = client.getArtist("Red Hot Chili Peppers");

    // 2. Assertions
    assertNotNull(artist, "Artist object must not be null");

    assertEquals("Red Hot Chili Peppers", artist.getName(), "Artist name must match");
    assertNotNull(artist.getMatch(), "Match score should be present in search results");
    assertTrue(Double.parseDouble(artist.getMatch()) > 0, "Match score should be positive");

    assertNotNull(artist.getUrl(), "Artist URL must not be null");
    assertTrue(artist.getUrl().contains("last.fm"), "URL must be a Last.fm link");

    assertNotNull(artist.getImageUrl(), "Image URL should be extracted");
    assertTrue(artist.getImageUrl().contains(HTTPS_LASTFM), "Image URL must be valid");

    assertTrue(artist.isAvailable(), "Artist should be marked as available");

    // 3. Test of image loading (optional)
    if (artist instanceof LastFmArtist) {
      ((LastFmArtist) artist).loadImage();
      // Image may be null if network fails, but the field should be accessible
      System.out.println("Image loaded: " + (artist.getImage() != null));
    }

    System.out.println("Test passed for artist: " + artist.getName());
  }

  @Test
  public void testGetArtistDetail() throws Exception {
    // 1. Client Call
    LastFmArtistDetail artist = client.getArtistDetail("Red Hot Chili Peppers", false);

    // 2. Assertions
    assertNotNull(artist, "Artist object must not be null");
  }

    @Test
  public void testGetSimilar() throws Exception {
    // 1. Get artist
    ArtistInfo artist = client.getArtist("Red Hot Chili Peppers");
    assertNotNull(artist, "Artist object must not be null before calling getSimilar");

    // 2. Call client
    List<ArtistInfo> similarArtists = client.getSimilar(artist, 50);

    // 3. Assertions
    assertNotNull(similarArtists, "List should not be null");
    // Assume that for a popular artist there is as many as 50 similar artists returned by the API
    assertEquals(50, similarArtists.size());
    for (ArtistInfo similarArtist : similarArtists) {
      System.out.println(similarArtist.getName() + " " + similarArtist.getId() + " " + similarArtist.getImageUrl() + " " + similarArtist.getMatch());
    }

    System.out.println("Test passed for similar artist for : " + artist.getName());
  }

  @Test
  public void testGetTopAlbums() throws Exception {
    // 1. Call the new client directly (replaces Album.getTopAlbums)
    // Note: Using a well-known artist with sufficient discography
    String artist = "Red Hot Chili Peppers";
    List<AlbumInfo> albums = client.getTopAlbums(artist, 10);

    // 2. JUnit 5 Assertions - Validate collection and album fields

    // Collection checks
    assertNotNull(albums, "Albums collection must not be null");
    assertFalse(albums.isEmpty(), "Albums collection must not be empty for " + artist);

    // Get first album for detailed validation
    AlbumInfo firstAlbum = albums.iterator().next();
    assertNotNull(firstAlbum, "First album in collection must not be null");

    // Artist check - all albums should belong to the queried artist
    assertEquals(artist, firstAlbum.getArtist(), "Album artist must match queried artist '" + artist + "'");

    // Title check - album title should not be null or empty
    assertNotNull(firstAlbum.getTitle(), "Album title must not be null");
    assertFalse(firstAlbum.getTitle().isEmpty(), "Album title must not be empty");
    System.out.println("Top Album: " + firstAlbum.getTitle());

    // URL check (should be a valid Last.fm URL)
    assertNotNull(firstAlbum.getUrl(), "Album URL must not be null");
    assertTrue(firstAlbum.getUrl().contains("last.fm"), "Album URL must contain 'last.fm'");

    // Artist URL check
    assertNotNull(firstAlbum.getArtistUrl(), "Artist URL must not be null");
    assertTrue(firstAlbum.getArtistUrl().contains("last.fm"), "Artist URL must contain 'last.fm'");

    // Cover Images checks
    // At least one cover size should be present
    boolean hasAnyCover = firstAlbum.getBigCoverURL() != null ||
            firstAlbum.getCoverURL() != null ||
            firstAlbum.getSmallCoverURL() != null;
    assertTrue(hasAnyCover, "At least one cover image URL must be present");

    // Big Cover URL (extralarge) - if present, validate format
    String bigCover = firstAlbum.getBigCoverURL();
    if (bigCover != null) {
      assertTrue(bigCover.contains(HTTPS_LASTFM) || bigCover.contains("lastfm"),
              "Big Cover URL must be valid Last.fm URL");
      System.out.println("Big Cover URL: " + bigCover);
    }

    // Cover URL (large) - if present, validate format
    String cover = firstAlbum.getCoverURL();
    if (cover != null) {
      System.out.println("Cover URL: " + cover);
    }

    // Small Cover URL (small) - if present, validate format
    String smallCover = firstAlbum.getSmallCoverURL();
    if (smallCover != null) {
      assertTrue(smallCover.contains(HTTPS_LASTFM) || smallCover.contains("lastfm"),
              "Small Cover URL must be valid Last.fm URL");
      System.out.println("Small Cover URL: " + smallCover);
    }

    // Release Date check - note: artist.getTopAlbums typically doesn't include release dates
    // This is expected behavior, not a failure
    if (firstAlbum.getReleaseDateString() != null) {
      assertFalse(firstAlbum.getReleaseDateString().isEmpty(),
              "Release date string should not be empty if present");
      System.out.println("Release Date: " + firstAlbum.getReleaseDateString());
    } else {
      // Expected: top albums endpoint doesn't include release dates
      System.out.println("Note: Release date is null (expected for artist.getTopAlbums endpoint)");
    }

    // Tracks check - note: artist.getTopAlbums doesn't include track listings
    // This is expected behavior, not a failure
    if (firstAlbum.getTracks() != null) {
      System.out.println("Tracks count: " + firstAlbum.getTracks().size());
    } else {
      // Expected: top albums endpoint doesn't include tracks
      System.out.println("Note: Tracks list is null (expected for artist.getTopAlbums endpoint)");
    }

    // Validate multiple albums in collection
    int albumCount = 0;
    for (AlbumInfo album : albums) {
      assertNotNull(album.getTitle(), "Each album must have a title");
      assertNotNull(album.getArtist(), "Each album must have an artist");
      albumCount++;
    }

    // Red Hot Chili Peppers should have at least 5 top albums
    assertTrue(albumCount >= 5, "Expected at least 5 top albums, got " + albumCount);
    System.out.println("Total top albums retrieved: " + albumCount);

    // Optional: Load cover image if LastFmAlbum implementation
    if (firstAlbum instanceof LastFmAlbum) {
      try {
        ((LastFmAlbum) firstAlbum).loadCoverImage();
        assertNotNull(firstAlbum.getCover(), "Cover ImageIcon should load successfully");
        System.out.println("Cover ImageIcon loaded successfully");
      } catch (Exception e) {
        System.out.println("Warning: Could not load cover image: " + e.getMessage());
      }
    }
  }
}

