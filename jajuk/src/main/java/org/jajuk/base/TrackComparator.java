/*
 *  Jajuk
 *  Copyright (C) 2004 The Jajuk Team
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
 * 
 * Multi-method track comparator
 */
public class TrackComparator implements Comparator<Track>, Serializable {
  private static final long serialVersionUID = -4735723947400147134L;

  /**
   * Sorting method
   */
  private final TrackComparatorType comparatorType;

  /** Sorting methods constants */
  public enum TrackComparatorType {
    STYLE_AUTHOR_ALBUM, AUTHOR_ALBUM, ALBUM, YEAR_ALBUM, DISCOVERY_ALBUM, RATE_ALBUM, HITS_ALBUM, ORDER
  }
  
  private static final DateFormat FORMATTER = UtilString.getAdditionDateFormatter();

  /**
   * Constructor
   * 
   * @param iSortingMethod
   *          Sorting method
   */
  public TrackComparator(TrackComparatorType comparatorType) {
    this.comparatorType = comparatorType;
  }

  /**
   * 
   * @param track
   * @return Hashcode string used to compare two tracks in accordance with the
   *         sorting method
   */
  private String getCompareString(Track track) {
    String sHashCompare = null;
    // comparison based on style, author, album, name and year to
    // differentiate 2 tracks with all the same attributes
    // note we need to use year because in sorted set, we must differentiate
    // 2 tracks with different years
    // Style/author/album
    if (comparatorType == TrackComparatorType.STYLE_AUTHOR_ALBUM) {
      sHashCompare = new StringBuilder().append(track.getStyle().getName2()).append(
          track.getAuthor().getName2()).append(track.getAlbum().getName2()).append(
          UtilString.padNumber(track.getOrder(), 5)).append(track.getName()).toString();
    }// Author/album
    else if (comparatorType == TrackComparatorType.AUTHOR_ALBUM) {
      sHashCompare = new StringBuilder().append(track.getAuthor().getName2()).append(
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

    return sHashCompare;
  }

  /**
   * Tracks compare
   * 
   * @param arg0
   * @param arg1
   * @return
   */
  public int compare(Track track1, Track track2) {
    String sHashCompare = getCompareString(track1);
    String sHashCompareOther = getCompareString(track2);
    return sHashCompare.compareToIgnoreCase(sHashCompareOther);
  }
}
