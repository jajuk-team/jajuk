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

import junit.framework.TestCase;
import org.jajuk.services.lastfm.model.AlbumInfo;
import org.jajuk.services.lastfm.model.LastFmAlbum;
import org.jajuk.services.lastfm.model.LastFmAlbumList;

import java.util.ArrayList;
import java.util.List;

/**
 * .
 */
public class TestLastFmAlbumList extends TestCase {

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbumList#getAlbums()}.
   */
  public void testGetAlbums() {
    LastFmAlbumList list = new LastFmAlbumList();
    List<AlbumInfo> info = new ArrayList<AlbumInfo>();
    LastFmAlbum album = new LastFmAlbum();
    album.setTitle("testtitle");
    info.add(album);
    list.setAlbums(info);
    assertNotNull(list.getAlbums());
    list.setAlbums(null);
    assertNull(list.getAlbums());
  }

  /**
   * Test method for {@link org.jajuk.services.lastfm.model.LastFmAlbumList#getArtist()}.
   */
  public void testGetArtist() {
    LastFmAlbumList list = new LastFmAlbumList();
    assertNull(list.getArtist());
    list.setArtist("testartist");
    assertEquals("testartist", list.getArtist());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.lastfm.model.LastFmAlbumList#setAlbums(java.util.List)}.
   */
  public void testSetAlbums() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.lastfm.model.LastFmAlbumList#setArtist(java.lang.String)}.
   */
  public void testSetArtist() {
    // tested above
  }
}
