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

import java.util.Comparator;

/**
 * Compares albums
 * 
 * @TODO Convert criteria from int to an enum
 */
public class AlbumComparator implements Comparator<Album> {

  private int criteria = 0;

  public AlbumComparator(int criteria) {
    this.criteria = criteria;
  }

  public int compare(Album album1, Album album2) {
    // for albums, perform a fast compare
    if (criteria == 2) {
      return album1.compareTo(album2);
    }
    // get a track for each album
    // TODO: get two tracks of album and compare Author,
    // if
    // !=, set Author to "Various Artist"
    Track track1 = album1.getAnyTrack();
    Track track2 = album2.getAnyTrack();

    // check tracks (normally useless)
    if (track1 == null || track2 == null) {
      return 0;
    }
    switch (criteria) {
    case 0: // style
      // Sort on Genre/Author/Year/Title
      if (track1.getStyle() == track2.getStyle()) {
        if (album1.getAlbumArtist2() == album2.getAlbumArtist2()) {
          if (track1.getYear() == track2.getYear()) {
            return album1.compareTo(album2);
          } else {
            return track1.getYear().compareTo(track2.getYear());
          }
        } else {
          return album1.getAlbumArtist2().compareTo(album2.getAlbumArtist2());
        }
      } else {
        return track1.getStyle().compareTo(track2.getStyle());
      }
    case 1: // author
      // Sort on Author/Year/Title
      // we use now the album artist
      if (album1.getAlbumArtist2() == album2.getAlbumArtist2()) {
        if (track1.getYear() == track2.getYear()) {
          return album1.compareTo(album2);
        } else {
          return track1.getYear().compareTo(track2.getYear());
        }
      } else {
        return album1.getAlbumArtist2().compareTo(album2.getAlbumArtist2());
      }
    case 3: // year
      // Sort on: Year/Author/Title
      if (track1.getYear() == track2.getYear()) {
        if (album1.getAlbumArtist2() == album2.getAlbumArtist2()) {
          return album1.compareTo(album2);
        } else {
          return album1.getAlbumArtist2().compareTo(album2.getAlbumArtist2());
        }
      } else {
        return track1.getYear().compareTo(track2.getYear());
      }
    case 4: // Discovery date
      return track2.getDiscoveryDate().compareTo(track1.getDiscoveryDate());
    case 5: // Rate
      if (album1.getRate() < album2.getRate()) {
        return 1;
      } else {
        return 0;
      }
    case 6: // Hits
      if (album1.getHits() < album2.getHits()) {
        return 1;
      } else {
        return 0;
      }
    }
    return 0;
  }
}
