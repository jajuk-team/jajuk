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

package ext.services.lastfm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.ImageSize;

/**
 * The Class LastFmSimilarArtists.
 */
public class LastFmSimilarArtists implements SimilarArtistsInfo {

  /** The Constant MAX_SIMILAR_ARTISTS. */
  private static final int MAX_SIMILAR_ARTISTS = 15;

  /** The artist name. */
  private String artistName;

  /** The picture. */
  private String picture;

  /** The artists. */
  private List<ArtistInfo> artists;

  /**
   * Gets the similar artists.
   * 
   * @param as DOCUMENT_ME
   * @param a DOCUMENT_ME
   * 
   * @return the similar artists
   */
  public static SimilarArtistsInfo getSimilarArtists(Collection<Artist> as, Artist a) {
    List<Artist> list = new ArrayList<Artist>(as);
    LastFmSimilarArtists similar = new LastFmSimilarArtists();

    similar.artistName = a.getName();
    similar.picture = a.getImageURL(ImageSize.LARGE);

    similar.artists = new ArrayList<ArtistInfo>();
    for (int i = 0; i < list.size(); i++) {
      if (i == MAX_SIMILAR_ARTISTS) {
        break;
      }
      similar.artists.add(LastFmArtist.getArtist(list.get(i)));
    }

    return similar;
  }

  /**
   * Gets the artist name.
   * 
   * @return the artist name
   */
  public String getArtistName() {
    return artistName;
  }

  /**
   * Gets the artists.
   * 
   * @return the artists
   */
  public List<ArtistInfo> getArtists() {
    // Sort similar artists ignoring case
    Collections.sort(artists, new Comparator<ArtistInfo>() {
      @Override
      public int compare(ArtistInfo o1, ArtistInfo o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });
    return artists;
  }

  /**
   * Gets the picture.
   * 
   * @return the picture
   */
  public String getPicture() {
    return picture;
  }

  /**
   * Sets the artist name.
   * 
   * @param artistName the artistName to set
   */
  public void setArtistName(String artistName) {
    this.artistName = artistName;
  }

  /**
   * Sets the artists.
   * 
   * @param artists the artists to set
   */
  public void setArtists(List<? extends ArtistInfo> artists) {
    this.artists = artists != null ? new ArrayList<ArtistInfo>(artists) : new ArrayList<ArtistInfo>();
  }

  /**
   * Sets the picture.
   * 
   * @param picture the picture to set
   */
  public void setPicture(String picture) {
    this.picture = picture;
  }

 
}
