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
 *  $Revision: 3132 $
 */
package ext.services.lastfm;

import java.util.ArrayList;
import java.util.List;

import net.roarsoftware.lastfm.Album;

import org.jajuk.JajukTestCase;
import org.jajuk.util.UtilString;

/**
 * 
 */
public class TestLastFmAlbumList extends JajukTestCase {
  private static final String API_KEY = "711591ss6q695ps349o6681pr1oq1467";

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbumList#getAlbumList(java.util.Collection, java.lang.String)}
   * .
   */
  public void testGetAlbumList() {
    List<Album> list = getAlbumList();

    AlbumListInfo info = LastFmAlbumList.getAlbumList(list, "Red Hot Chilli Peppers");
    assertNotNull(info);
    assertEquals(2, info.getAlbums().size());
  }

  /**
   * @return
   */
  private List<Album> getAlbumList() {
    Album a1 = Album.getInfo("Red Hot Chilli Peppers", "By The Way", UtilString.rot13(API_KEY));
    Album a2 = Album.getInfo("Red Hot Chilli Peppers", "Stadium Arcadium", UtilString
        .rot13(API_KEY));
    List<Album> list = new ArrayList<Album>();
    list.add(a1);
    list.add(a2);
    return list;
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbumList#getAlbums()}.
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
   * Test method for {@link ext.services.lastfm.LastFmAlbumList#getArtist()}.
   */
  public void testGetArtist() {
    LastFmAlbumList list = new LastFmAlbumList();
    assertNull(list.getArtist());

    list.setArtist("testartist");
    assertEquals("testartist", list.getArtist());
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbumList#setAlbums(java.util.List)}.
   */
  public void testSetAlbums() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbumList#setArtist(java.lang.String)}.
   */
  public void testSetArtist() {
    // tested above
  }
}
