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
package org.jajuk.base;

import java.util.Date;

import org.jajuk.JajukTestCase;

/**
 * 
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
   * 0 .. style 1 .. author 2 .. album 3 .. year 4 .. discovery date 5 .. rate 6
   * .. hits
   */
  /**
   * Test method for
   * {@link org.jajuk.base.AlbumComparator#compare(org.jajuk.base.Album, org.jajuk.base.Album)}
   * .
   */
  public final void testCompareStyle() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

    // just differ in Style
    album.getTracksCache().add(getTrack(album));
    equal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Style("7", "name7"), new Author("4", "name4"), 10,
            new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareStyle2() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

    // just differ in Style, this time we have the physical same style
    Style style = new Style("8", "name8");
    album.getTracksCache().add(getTrack(album, style, new Year("5", "name5")));
    equal.getTracksCache().add(getTrack(album, style, new Year("5", "name5")));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Style("7", "name7"), new Author("4", "name4"), 10,
            new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareStyle4AlbumArtistDifferent() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist2", 2);
    Album notequal = new Album("1", "name", "artist", 2);

    // just differ in Style, this time we have the physical same style
    Style style = new Style("8", "name8");
    album.getTracksCache().add(getTrack(album, style, new Year("5", "name5")));
    equal.getTracksCache().add(getTrack(album, style, new Year("5", "name5")));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Style("7", "name7"), new Author("4", "name4"), 10,
            new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(-1, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareStyle3SameYear() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

    // just differ in Style, this time we have the physical same style
    Style style = new Style("8", "name8");
    Year year = new Year("5", "name5");
    album.getTracksCache().add(getTrack(album, style, year));
    equal.getTracksCache().add(getTrack(album, style, year));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Style("7", "name7"), new Author("4", "name4"), 10,
            new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareAuthor() {
    AlbumComparator compare = new AlbumComparator(1);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist2", 2);

    // just differ in Author
    album.getTracksCache().add(getTrack(album));
    equal.getTracksCache().add(getTrack(equal));
    notequal.getTracksCache().add(getTrack(notequal));
    /*
     * notequal.getTracksCache().add( new Track("2", "name2", album, new
     * Style("3", "name3"), new Author("7", "name7"), 10, new Year("5",
     * "name5"), 1, new Type("6", "name6", "ext", null, null), 3));
     */

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareAuthorSameYear() {
    AlbumComparator compare = new AlbumComparator(1);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist2", 2);

    // just differ in Author
    Style style = new Style("8", "name8");
    Year year = new Year("5", "name5");
    album.getTracksCache().add(getTrack(album, style, year));
    equal.getTracksCache().add(getTrack(equal, style, year));
    notequal.getTracksCache().add(getTrack(notequal));
    /*
     * notequal.getTracksCache().add( new Track("2", "name2", album, new
     * Style("3", "name3"), new Author("7", "name7"), 10, new Year("5",
     * "name5"), 1, new Type("6", "name6", "ext", null, null), 3));
     */

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareAlbum() {
    AlbumComparator compare = new AlbumComparator(2);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("2", "name2", "artist2", 2);

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareYear() {
    AlbumComparator compare = new AlbumComparator(3);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

    // just differ in Year
    album.getTracksCache().add(getTrack(album));
    equal.getTracksCache().add(getTrack(album));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Style("3", "name3"), new Author("4", "name4"), 10,
            new Year("7", "name7"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareYearSameYear() {
    AlbumComparator compare = new AlbumComparator(3);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

    // just differ in Year
    Style style = new Style("8", "name8");
    Year year = new Year("5", "name5");
    album.getTracksCache().add(getTrack(album, style, year));
    equal.getTracksCache().add(getTrack(album, style, year));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Style("3", "name3"), new Author("4", "name4"), 10,
            new Year("7", "name7"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(0, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareYearSameYearDiffArtist() {
    AlbumComparator compare = new AlbumComparator(3);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist2", 2);
    Album notequal = new Album("1", "name", "artist", 2);

    // just differ in Year
    Style style = new Style("8", "name8");
    Year year = new Year("5", "name5");
    album.getTracksCache().add(getTrack(album, style, year));
    equal.getTracksCache().add(getTrack(album, style, year));
    notequal.getTracksCache().add(
        new Track("2", "name2", album, new Style("3", "name3"), new Author("4", "name4"), 10,
            new Year("7", "name7"), 1, new Type("6", "name6", "ext", null, null), 3));

    assertEquals(-1, compare.compare(album, equal));
    assertTrue(0 < compare.compare(album, notequal) || 0 > compare.compare(album, notequal));
  }

  public final void testCompareDiscoveryDate() {
    AlbumComparator compare = new AlbumComparator(4);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

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

  public final void testCompareRate() {
    AlbumComparator compare = new AlbumComparator(5);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

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

  public final void testCompareRateGT() {
    AlbumComparator compare = new AlbumComparator(5);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

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

  public final void testCompareHits() {
    AlbumComparator compare = new AlbumComparator(6);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

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

  public final void testCompareHitsGT() {
    AlbumComparator compare = new AlbumComparator(6);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("1", "name", "artist", 2);

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
   * @param album
   * @return
   */
  private Track getTrack(Album album) {
    return new Track("2", "name2", album, new Style("3", "name3"), new Author("4", "name4"), 10,
        new Year("5", "name5"), 1, new Type("6", "name6", "ext", null, null), 3);
  }

  private Track getTrack(Album album, Style style, Year year) {
    return new Track("2", "name2", album, style, new Author("4", "name4"), 10, year, 1, new Type(
        "6", "name6", "ext", null, null), 3);
  }

  public final void testCompareNoTrack() {
    AlbumComparator compare = new AlbumComparator(0);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("2", "name2", "artist2", 2);

    // without actual tracks, anything compares...
    assertEquals(0, compare.compare(album, equal));
    assertEquals(0, compare.compare(album, notequal));
  }

  public final void testCompareCriteriaOutside() {
    AlbumComparator compare = new AlbumComparator(99);
    Album album = new Album("1", "name", "artist", 2);
    Album equal = new Album("1", "name", "artist", 2);
    Album notequal = new Album("2", "name2", "artist2", 2);

    // add the same type of track in all albums to not stop comparison early
    album.getTracksCache().add(getTrack(album));
    equal.getTracksCache().add(getTrack(equal));
    notequal.getTracksCache().add(getTrack(notequal));

    // with invalid "criteria", anything compares
    assertEquals(0, compare.compare(album, equal));
    assertEquals(0, compare.compare(album, notequal));
  }
}
