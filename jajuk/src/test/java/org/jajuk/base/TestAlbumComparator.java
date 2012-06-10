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
package org.jajuk.base;

import java.util.Date;

import org.jajuk.JajukTestCase;

/**
 * TODO : most of this test class should rewritten as the case we test
 * here is not actually possible :
 * Album album = new Album("1", "name", 2);
 * Album equal = new Album("1", "name", 2);
 * Album notequal = new Album("1", "name", 2);
 * is an impossible state as two albums with the same name must
 * be the same album (endorsed by the AlbumManager)
 * 
 * We should also drop the direct items instantiations but use managers instead.
 */
public class TestAlbumComparator extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.base.AlbumComparator#AlbumComparator(int)}
   * .
   */
  public final void testAlbumComparator() {
    new AlbumComparator(0);
  }

  /*
   * 0 .. genre 1 .. artist 2 .. album 3 .. year 4 .. discovery date 5 .. rate 6
   * .. hits
   */
  /**
   * Test method for.
   *
   * {@link org.jajuk.base.AlbumComparator#compare(org.jajuk.base.Album, org.jajuk.base.Album)}
   * .
   */
  public final void testCompareGenre() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Genre
    album.getTracksCache().add(getTrack(album));
    equal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Genre("7", "name7"), new Artist("4", "name4"), 10,
            new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare genre2.
   * 
   */
  public final void testCompareGenre2() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Genre, this time we have the physical same genre
    Genre genre = new Genre("8", "name8");
    album.getTracksCache().add(getTrack(album, genre, new Year("5", "name5")));
    equal.getTracksCache().add(getTrack(album, genre, new Year("5", "name5")));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Genre("7", "name7"), new Artist("4", "name4"), 10,
            new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare genre4 album artist different.
   * 
   */
  public final void testCompareGenre4AlbumArtistDifferent() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Genre, this time we have the physical same genre
    Genre genre = new Genre("8", "name8");
    album.getTracksCache().add(getTrack(album, genre, new Year("5", "name5")));
    equal.getTracksCache().add(getTrack(album, genre, new Year("5", "name5")));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Genre("7", "name7"), new Artist("4", "name4"), 10,
            new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare genre3 same year.
   * 
   */
  public final void testCompareGenre3SameYear() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Genre, this time we have the physical same genre
    Genre genre = new Genre("8", "name8");
    Year year = new Year("5", "name5");
    album.getTracksCache().add(getTrack(album, genre, year));
    equal.getTracksCache().add(getTrack(album, genre, year));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Genre("7", "name7"), new Artist("4", "name4"), 10,
            new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare artist.
   * 
   */
  public final void testCompareArtist() {
    AlbumComparator compare = new AlbumComparator(1);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Artist
    album.getTracksCache().add(getTrack(album));
    equal.getTracksCache().add(getTrack(equal));
    notequal.getTracksCache().add(getTrack(notequal, new Artist("5", "name5")));
    /*
     * notequal.getTracksCache().add( new Track("2", "name2", album, new
     * Genre("3", "name3"), new Artist("7", "name7"), 10, new Year("5",
     * "name5"), 1, new Type("6", "name6", "ext", null, null), 3));
     */

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare artist same year.
   * 
   */
  public final void testCompareArtistSameYear() {
    AlbumComparator compare = new AlbumComparator(1);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Artist
    Genre genre = new Genre("8", "name8");
    Year year = new Year("5", "name5");
    album.getTracksCache().add(getTrack(album, genre, year));
    equal.getTracksCache().add(getTrack(equal, genre, year));
    notequal.getTracksCache().add(getTrack(notequal, new Artist("5", "name5")));
    /*
     * notequal.getTracksCache().add( new Track("2", "name2", album, new
     * Genre("3", "name3"), new Artist("7", "name7"), 10, new Year("5",
     * "name5"), 1, new Type("6", "name6", "ext", null, null), 3));
     */

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare album.
   * 
   */
  public final void testCompareAlbum() {
    AlbumComparator compare = new AlbumComparator(2);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("2", "name2", 2);

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare year.
   * 
   */
  public final void testCompareYear() {
    AlbumComparator compare = new AlbumComparator(3);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Year
    album.getTracksCache().add(getTrack(album));
    equal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Genre("3", "name3"), new Artist("4", "name4"), 10,
            new Year("7", "name7"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare year same year.
   * 
   */
  public final void testCompareYearSameYear() {
    AlbumComparator compare = new AlbumComparator(3);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Year
    Genre genre = new Genre("8", "name8");
    Year year = new Year("5", "name5");
    album.getTracksCache().add(getTrack(album, genre, year));
    equal.getTracksCache().add(getTrack(album, genre, year));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Genre("3", "name3"), new Artist("4", "name4"), 10,
            new Year("7", "name7"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare year same year diff artist.
   * 
   */
  public final void testCompareYearSameYearDiffArtist() {
    AlbumComparator compare = new AlbumComparator(3);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Year
    Genre genre = new Genre("8", "name8");
    Year year = new Year("5", "name5");
    album.getTracksCache().add(getTrack(album, genre, year));
    equal.getTracksCache().add(getTrack(album, genre, year));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Genre("3", "name3"), new Artist("5", "name5"), 10,
            new Year("7", "name7"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare discovery date.
   * 
   */
  public final void testCompareDiscoveryDate() {
    AlbumComparator compare = new AlbumComparator(4);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    Date date1 = new Date();
    Date date2 = new Date(12345); // needs to be different to date1

    // just differ in DiscoverDate
    album.getTracksCache().add(getTrack(album));
    album.getTracksCache().get(0).setDiscoveryDate(date1);
    equal.getTracksCache().add(getTrack(album));
    equal.getTracksCache().get(0).setDiscoveryDate(date1);
    notequal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().get(0).setDiscoveryDate(date2);

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  /**
   * Test compare rate.
   * 
   */
  public final void testCompareRate() {
    AlbumComparator compare = new AlbumComparator(5);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Rate
    album.getTracksCache().add(getTrack(album));
    album.getTracksCache().get(0).setRate(3);
    equal.getTracksCache().add(getTrack(album));
    equal.getTracksCache().get(0).setRate(3);
    notequal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().get(0).setRate(4);

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 > compare.compare(album, notequal));
  }

  /**
   * Test compare rate gt.
   * 
   */
  public final void testCompareRateGT() {
    AlbumComparator compare = new AlbumComparator(5);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Rate
    album.getTracksCache().add(getTrack(album));
    album.getTracksCache().get(0).setRate(4);
    equal.getTracksCache().add(getTrack(album));
    equal.getTracksCache().get(0).setRate(4);
    notequal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().get(0).setRate(3);

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal));
  }

  /**
   * Test compare hits.
   * 
   */
  public final void testCompareHits() {
    AlbumComparator compare = new AlbumComparator(6);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Hits
    album.getTracksCache().add(getTrack(album));
    album.getTracksCache().get(0).setHits(3);
    equal.getTracksCache().add(getTrack(album));
    equal.getTracksCache().get(0).setHits(3);
    notequal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().get(0).setHits(4);

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 > compare.compare(album, notequal));
  }

  /**
   * Test compare hits gt.
   * 
   */
  public final void testCompareHitsGT() {
    AlbumComparator compare = new AlbumComparator(6);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("1", "name", 2);

    // just differ in Hits
    album.getTracksCache().add(getTrack(album));
    album.getTracksCache().get(0).setHits(4);
    equal.getTracksCache().add(getTrack(album));
    equal.getTracksCache().get(0).setHits(4);
    notequal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().get(0).setHits(3);

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal));
  }

  /**
   * Gets the track.
   *
   * @param album 
   * @return the track
   */
  private Track getTrack(Album album) {
    return new Track("2", "name2", album, new Genre("3", "name3"), new Artist("4", "name4"), 10,
        new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3);
  }

  /**
   * Gets the track.
   *
   * @param album 
   * @param artist 
   * @return the track
   */
  private Track getTrack(Album album, Artist artist) {
    return new Track("2", "name2", album, new Genre("3", "name3"), artist, 10, new Year("5",
        "name5"), 1, new Type("6", "name6", "ext", null, null), 3);
  }

  /**
   * Gets the track.
   *
   * @param album 
   * @param genre 
   * @param year 
   * @return the track
   */
  private Track getTrack(Album album, Genre genre, Year year) {
    return new Track("2", "name2", album, genre, new Artist("4", "name4"), 10, year, 1, new Type(
        "6", "name6", "ext", null, null), 3);
  }

  /**
   * Test compare no track.
   * 
   */
  public final void testCompareNoTrack() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("2", "name2", 2);

    // without actual tracks, anything compares...
    assertEquals(0, compare.compare(album, equal));
    assertEquals(0, compare.compare(album, notequal));
  }

  /**
   * Test compare criteria outside.
   * 
   */
  public final void testCompareCriteriaOutside() {
    AlbumComparator compare = new AlbumComparator(99);
    Album album = new Album("1", "name", 2);
    Album equal = new Album("1", "name", 2);
    Album notequal = new Album("2", "name2", 2);

    // add the same type of track in all albums to not stop comparison early
    album.getTracksCache().add(getTrack(album));
    equal.getTracksCache().add(getTrack(equal));
    notequal.getTracksCache().add(getTrack(notequal));

    // with invalid "criteria", anything compares
    assertEquals(0, compare.compare(album, equal));
    assertEquals(0, compare.compare(album, notequal));
  }
}
