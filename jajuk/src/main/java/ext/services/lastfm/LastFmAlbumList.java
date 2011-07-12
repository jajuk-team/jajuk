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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.roarsoftware.lastfm.Album;

/**
 * The Class LastFmAlbumList.
 */
public class LastFmAlbumList implements AlbumListInfo {

  /** The artist. */
  private String artist;

  /** The albums. */
  private List<AlbumInfo> albums;

  /**
   * Gets the album list.
   * 
   * @param as DOCUMENT_ME
   * @param artist DOCUMENT_ME
   * 
   * @return the album list
   */
  public static AlbumListInfo getAlbumList(Collection<Album> as, String artist) {
    List<AlbumInfo> albums = new ArrayList<AlbumInfo>();
    AlbumListInfo albumList = new LastFmAlbumList();

    for (Album a : as) {
      AlbumInfo album = LastFmAlbum.getAlbum(a, null);
      albums.add(album);
    }

    albumList.setAlbums(albums);
    return albumList;
  }

  /**
   * Gets the albums.
   * 
   * @return the albums
   */
  @Override
  public List<AlbumInfo> getAlbums() {
    return albums;
  }

  /**
   * Gets the artist.
   * 
   * @return the artist
   */
  @Override
  public String getArtist() {
    return artist;
  }

  /**
   * Sets the albums.
   * 
   * @param albums the albums to set
   */
  @Override
  public void setAlbums(List<? extends AlbumInfo> albums) {
    this.albums = albums != null ? new ArrayList<AlbumInfo>(albums) : null;
  }

  /**
   * Sets the artist.
   * 
   * @param artist the artist to set
   */
  @Override
  public void setArtist(String artist) {
    this.artist = artist;
  }

}
