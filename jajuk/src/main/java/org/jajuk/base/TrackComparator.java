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
 *  $Revision$
 */

package org.jajuk.base;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Comparator;

import org.jajuk.util.UtilString;

/**
 * Multi-method track comparator.
 */
public class TrackComparator implements Comparator<Track>, Serializable {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -4735723947400147134L;

  /** Sorting method. */
  private final TrackComparatorType comparatorType;

  /**
   * Sorting methods constants.
   */
  public enum TrackComparatorType {
    /** Compare first based on the genre, then on artist and then on album. */
    GENRE_ARTIST_ALBUM,
    /** Compare based on artist and then album. */
    ARTIST_ALBUM,
    /** Compare only on album. */
    ALBUM,
    /** Compare only on year. */
    YEAR_ALBUM,
    /** Compare only on the discovery date of the album. */
    DISCOVERY_ALBUM,
    /** Compare on the rate and then the album. */
    RATE_ALBUM,
    /** Compare on the number of hits and then on the album. */
    HITS_ALBUM,
    /** Compare on disc number and order of the track in the album. */
    ORDER,
    /** Compare to find identifical tracks */
    ALMOST_IDENTICAL
  }

  /** The Constant FORMATTER. Used to correctly compare dates. */
  private static final DateFormat FORMATTER = UtilString.getAdditionDateFormatter();

  /**
   * Constructor.
   * 
   * @param comparatorType
   *          Specifies the type of comparison that should be done.
   */
  public TrackComparator(TrackComparatorType comparatorType) {
    this.comparatorType = comparatorType;
  }

  /**
   * Gets the compare string based on the input-track and the type of comparison
   * that is selected when constructing the comparator.
   * 
   * @param track
   *          The track that should be used for constructing the string.
   * 
   * @return Hashcode string used to compare two tracks in accordance with the
   *         sorting method
   */
  private String getCompareString(Track track) {
    String sHashCompare = null;
    // comparison based on genre, artist, album, name and year to
    // differentiate 2 tracks with all the same attributes
    // note we need to use year because in sorted set, we must differentiate
    // 2 tracks with different years
    // Genre/artist/album
    if (comparatorType == TrackComparatorType.GENRE_ARTIST_ALBUM) {
      sHashCompare = new StringBuilder().append(track.getGenre().getName2()).append(
          track.getArtist().getName2()).append(track.getAlbum().getName2()).append(
          UtilString.padNumber(track.getOrder(), 5)).append(track.getName()).toString();
    }// Artist/album
    else if (comparatorType == TrackComparatorType.ARTIST_ALBUM) {
      sHashCompare = new StringBuilder().append(track.getArtist().getName2()).append(
          track.getAlbum().getName2()).append(UtilString.padNumber(track.getOrder(), 5)).append(
          track.getName()).toString();
    }
    // Album
    else if (comparatorType == TrackComparatorType.ALBUM) {
      sHashCompare = new StringBuilder().append(track.getAlbum().getName2()).append(
          UtilString.padNumber(track.getOrder(), 5)).append(track.getName()).toString();
    }
    // Year / album
    if (comparatorType == TrackComparatorType.YEAR_ALBUM) {
      sHashCompare = new StringBuilder().append(
          UtilString.padNumber(999999999 - track.getYear().getValue(), 10)).append(
          UtilString.padNumber(track.getOrder(), 5)).append(track.getName()).toString();
    }
    // discovery date / album
    else if (comparatorType == TrackComparatorType.DISCOVERY_ALBUM) {
      sHashCompare = new StringBuilder().append(FORMATTER.format(track.getDiscoveryDate())).append(
          track.getAlbum().getName2()).append(UtilString.padNumber(track.getOrder(), 5)).append(
          track.getName()).toString();
    }
    // Rate / album
    else if (comparatorType == TrackComparatorType.RATE_ALBUM) {
      sHashCompare = new StringBuilder().append(
          UtilString.padNumber(999999999 - track.getRate(), 10)).append(
          UtilString.padNumber(track.getOrder(), 5)).append(track.getName()).toString();
    }
    // Hits / album
    else if (comparatorType == TrackComparatorType.HITS_ALBUM) {
      sHashCompare = new StringBuilder().append(
          UtilString.padNumber(999999999 - track.getHits(), 10)).append(track.getName()).toString();
    }
    // Disc number / Order / track name
    else if (comparatorType == TrackComparatorType.ORDER) {
      sHashCompare = new StringBuilder().append(
          UtilString.padNumber(track.getDiscNumber(), 5)
              + UtilString.padNumber(track.getOrder(), 5) + track.getName()).toString();
    }
    // We want to find identical tracks but using album name, not album id.
    // We only use set tags, not unknown ones
    else if (comparatorType == TrackComparatorType.ALMOST_IDENTICAL) {
      sHashCompare = buildIdenticalTestFootprint(track);
    }

    return sHashCompare;
  }

  /**
   * Return a footprint used to find almost-identical track 
   * @param track
   * @return a footprint used to find almost-identical track 
   */
  private String buildIdenticalTestFootprint(Track track) {
    StringBuilder sb = new StringBuilder();
    if (!track.getGenre().isUnknown()) {
      sb.append(track.getGenre().getID());
    }
    if (!track.getArtist().isUnknown()) {
      sb.append(track.getArtist().getID());
    }
    if (!track.getAlbum().isUnknown()) {
      sb.append(track.getAlbum().getName());
    }
    sb.append(track.getName());
    if (track.getYear().looksValid()) {
      sb.append(track.getYear().getValue());
    }
    sb.append(track.getDuration());
    sb.append(track.getOrder());
    sb.append(track.getDiscNumber());
    if (!track.getAlbumArtist().isUnknown()) {
      sb.append(track.getAlbumArtist().getName());
    }
    return sb.toString();

  }

  /**
   * Compares two tracks according to the type selected during constructing of
   * the comparator..
   * 
   * @param track1
   *          The first track for comparison.
   * @param track2
   *          The second track for comparison.
   * 
   * @return the value <code>0</code> if track1 is equal to track2; a value less
   *         than <code>0</code> if track1 is less than track2; and a value
   *         greater than <code>0</code> if track1 is greater than track2.
   */
  public int compare(Track track1, Track track2) {
    String sHashCompare = getCompareString(track1);
    String sHashCompareOther = getCompareString(track2);
    return sHashCompare.compareToIgnoreCase(sHashCompareOther);
  }
}
