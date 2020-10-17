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
    /** Compare to find identical tracks. */
    ALMOST_IDENTICAL
  }

  /** The Constant FORMATTER. Used to correctly compare dates. */
  private static final DateFormat FORMATTER = UtilString.getAdditionDateFormatter();

  /**
   * Constructor.
   *
   * @param comparatorType Specifies the type of comparison that should be done.
   */
  public TrackComparator(TrackComparatorType comparatorType) {
    this.comparatorType = comparatorType;
  }

  /**
   * Return a footprint used to find almost-identical track.
   *
   * @param track The Track to use for building the footprint.
   *
   * @return a footprint used to find almost-identical track
   */
  public String buildIdenticalTestFootprint(Track track) {
    StringBuilder sb = new StringBuilder();
    if (!track.getGenre().seemsUnknown()) {
      sb.append(track.getGenre().getID());
    }
    if (!track.getArtist().seemsUnknown()) {
      sb.append(track.getArtist().getID());
    }
    if (!track.getAlbum().seemsUnknown()) {
      sb.append(track.getAlbum().getName());
    }
    sb.append(track.getName());
    if (track.getYear().looksValid()) {
      sb.append(track.getYear().getValue());
    }
    sb.append(track.getDuration());
    sb.append(track.getOrder());
    sb.append(track.getType().getID());
    sb.append(track.getDiscNumber());
    if (!track.getAlbumArtist().seemsUnknown()) {
      sb.append(track.getAlbumArtist().getName());
    }
    return sb.toString();
  }

  /**
   * Compares two tracks according to the type selected during constructing of
   * the comparator..
   *
   * @param track1 The first track for comparison.
   * @param track2 The second track for comparison.
   *
   * @return the value <code>0</code> if track1 is equal to track2; a value less
   * than <code>0</code> if track1 is less than track2; and a value
   * greater than <code>0</code> if track1 is greater than track2.
   */
  @Override
  public int compare(Track track1, Track track2) {
    if (track1 == null && track2 == null) {
      return 0;
    } else if (track1 == null) {
      return -1;
    } else if (track2 == null) {
      return 1;
    }

    // comparison based on genre, artist, album, name and year to
    // differentiate 2 tracks with all the same attributes
    // note we need to use year because in sorted set, we must differentiate
    // 2 tracks with different years

    // Genre/artist/album
    if (comparatorType == TrackComparatorType.GENRE_ARTIST_ALBUM) {
      int ret = track1.getGenre().getName2().compareToIgnoreCase(track2.getGenre().getName2());
      if(ret != 0) {
        return ret;
      }

      ret = track1.getArtist().getName2().compareToIgnoreCase(track2.getArtist().getName2());
      if(ret != 0) {
        return ret;
      }

      return compareAlbumOrderAndTrackName(track1, track2);
    }

    // Artist/album
    if (comparatorType == TrackComparatorType.ARTIST_ALBUM) {
      int ret = track1.getArtist().getName2().compareToIgnoreCase(track2.getArtist().getName2());
      if(ret != 0) {
        return ret;
      }

      return compareAlbumOrderAndTrackName(track1, track2);
    }

    // Album
    if (comparatorType == TrackComparatorType.ALBUM) {
      return compareAlbumOrderAndTrackName(track1, track2);
    }

    // Year / album
    if (comparatorType == TrackComparatorType.YEAR_ALBUM) {
      if(track1.getYear().getValue() != track2.getYear().getValue()) {
        // reverse order here to sort year in the proper order
        return Long.compare(track2.getYear().getValue(), track1.getYear().getValue());
      }

      return compareOrderAndTrackName(track1, track2);
    }

    // discovery date / album
    if (comparatorType == TrackComparatorType.DISCOVERY_ALBUM) {
      int ret = FORMATTER.format(track1.getDiscoveryDate()).compareTo(
              FORMATTER.format(track2.getDiscoveryDate()));
      if(ret != 0) {
        return ret;
      }

      return compareAlbumOrderAndTrackName(track1, track2);
    }

    // Rate / album
    if (comparatorType == TrackComparatorType.RATE_ALBUM) {
      if(track1.getRate() != track2.getRate()) {
        // reverse order to for proper order
        return Long.compare(track2.getRate(), track1.getRate());
      }

      return compareOrderAndTrackName(track1, track2);
    }

    // Hits / album
    if (comparatorType == TrackComparatorType.HITS_ALBUM) {
      if(track1.getHits() != track2.getHits()) {
        // reverse order to get proper sorting
        return Long.compare(track2.getHits(), track1.getHits());
      }
      int ret = track1.getAlbum().getName2().compareToIgnoreCase(track2.getAlbum().getName2());
      if(ret != 0) {
        return ret;
      }

      return track1.getName().compareToIgnoreCase(track2.getName());
    }
    // Disc number / Order / track name
    else if (comparatorType == TrackComparatorType.ORDER) {
      if(track1.getDiscNumber() != track2.getDiscNumber()) {
        return Long.compare(track1.getDiscNumber(), track2.getDiscNumber());
      }

      return compareOrderAndTrackName(track1, track2);
    }
    // We want to find identical tracks but using album name, not album id.
    // We only use set tags, not unknown ones
    else if (comparatorType == TrackComparatorType.ALMOST_IDENTICAL) {
      return buildIdenticalTestFootprint(track1).compareToIgnoreCase(buildIdenticalTestFootprint(track2));
    }

    throw new IllegalArgumentException("Unknown type of comparator: " + comparatorType);
  }

  private int compareAlbumOrderAndTrackName(Track track1, Track track2) {
    int ret;
    ret = track1.getAlbum().getName2().compareToIgnoreCase(track2.getAlbum().getName2());
    if (ret != 0) {
      return ret;
    }

    return compareOrderAndTrackName(track1, track2);
  }

  private int compareOrderAndTrackName(Track track1, Track track2) {
    if (track1.getOrder() != track2.getOrder()) {
      return Long.compare(track1.getOrder(), track2.getOrder());
    }

    return track1.getName().compareToIgnoreCase(track2.getName());
  }
}
