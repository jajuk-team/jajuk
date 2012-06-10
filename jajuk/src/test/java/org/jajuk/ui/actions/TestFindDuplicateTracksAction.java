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
package org.jajuk.ui.actions;

import java.util.List;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumArtist;
import org.jajuk.base.AlbumArtistManager;
import org.jajuk.base.Artist;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.util.Const;

/**
 * Test find dups action.
 */
public class TestFindDuplicateTracksAction extends JajukTestCase {

  Type type;

  Artist artist;

  Album album;

  Genre genre;

  org.jajuk.base.Year year;

  FindDuplicateTracksAction action;

  Directory dir;

  AlbumArtist albumArtist;

  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    // Populate tracks and files
    dir = JUnitHelpers.getDirectory();
    dir.getDevice().mount(false);
    type = JUnitHelpers.getType();
    artist = JUnitHelpers.getArtist();
    album = JUnitHelpers.getAlbum();
    genre = JUnitHelpers.getGenre();
    year = JUnitHelpers.getYear();
    action = new FindDuplicateTracksAction();
    albumArtist = AlbumArtistManager.getInstance().registerAlbumArtist(Const.UNKNOWN_ARTIST);

  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.actions.FindDuplicateTracksAction#perform(java.awt.event.ActionEvent)}
   * .
   * Test 1 : none dups
   */
  public void testPopulateDups1() {

    Track track11 = TrackManager.getInstance().registerTrack("track11", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track11.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file11", dir, track11, 10, 128);

    Track track12 = TrackManager.getInstance().registerTrack("track12", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track12.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file12", dir, track12, 10, 128);

    Track track21 = TrackManager.getInstance().registerTrack("track21", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track21.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file21", dir, track21, 10, 218);

    Track track22 = TrackManager.getInstance().registerTrack("track22", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track22.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file22", dir, track22, 10, 128);

    action.populateDups();
    List<List<File>> dups = action.duplicateTracksList;
    assertTrue(dups.size() == 0);

  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.actions.FindDuplicateTracksAction#perform(java.awt.event.ActionEvent)}
   * .
   * Test 2 : multi-files track : filex1 and filex2 are in the same track
   */
  public void testPopulateDups2() {

    Track track11 = TrackManager.getInstance().registerTrack("track11", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track11.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file11", dir, track11, 10, 128);

    FileManager.getInstance().registerFile("file12", dir, track11, 10, 128);

    Track track21 = TrackManager.getInstance().registerTrack("track21", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track21.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file21", dir, track21, 10, 218);

    FileManager.getInstance().registerFile("file22", dir, track21, 10, 128);

    action.populateDups();
    List<List<File>> dups = action.duplicateTracksList;
    assertTrue(dups.size() == 2);
    List<File> dup0 = dups.get(0);
    assertTrue(dup0.size() == 2);
    List<File> dup1 = dups.get(1);
    assertTrue(dup1.size() == 2);

  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.actions.FindDuplicateTracksAction#perform(java.awt.event.ActionEvent)}
   * .
   * Test 3 : single dup : track11 and track12 have fuzzy match
   */
  public void testPopulateDups3() {

    Track track11 = TrackManager.getInstance().registerTrack("track", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track11.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file11", dir, track11, 10, 128);

    Track track12 = TrackManager.getInstance().registerTrack("trAck", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track12.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file12", dir, track12, 10, 128);

    action.populateDups();
    List<List<File>> dups = action.duplicateTracksList;
    assertTrue(dups.size() == 1);
    List<File> dup0 = dups.get(0);
    assertTrue(dup0.size() == 2);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.actions.FindDuplicateTracksAction#perform(java.awt.event.ActionEvent)}
   * .
   * Test 4 : mix : file11/file12 have the same track : track1,
   * file21/file22 have the same track : track2,
   * track1 =~ track2
   */
  public void testPopulateDups4() {

    Track track1 = TrackManager.getInstance().registerTrack("track", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track1.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file11", dir, track1, 10, 128);
    FileManager.getInstance().registerFile("file12", dir, track1, 10, 128);

    Track track2 = TrackManager.getInstance().registerTrack("trAck", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track2.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file21", dir, track2, 10, 128);
    FileManager.getInstance().registerFile("file22", dir, track2, 10, 128);

    action.populateDups();
    List<List<File>> dups = action.duplicateTracksList;
    assertTrue(dups.size() == 1);
    List<File> dup0 = dups.get(0);
    assertTrue(dup0.size() == 4);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.actions.FindDuplicateTracksAction#perform(java.awt.event.ActionEvent)}
   * .
   * Test 5 : file11/file12 have the same track : track1,
   * file21/file22 have different tracks ; track21 and track 21,
   * track1 =~ track21 != track22
   */
  public void testPopulateDups5() {

    Track track1 = TrackManager.getInstance().registerTrack("track", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track1.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file11", dir, track1, 10, 128);
    FileManager.getInstance().registerFile("file12", dir, track1, 10, 128);

    Track track21 = TrackManager.getInstance().registerTrack("trAck", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track21.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file21", dir, track21, 10, 128);

    Track track22 = TrackManager.getInstance().registerTrack("trAck2", album, genre, artist, 10l,
        year, 1l, type, 1l);
    track22.setAlbumArtist(albumArtist);
    FileManager.getInstance().registerFile("file22", dir, track22, 10, 128);

    action.populateDups();
    List<List<File>> dups = action.duplicateTracksList;
    assertTrue(dups.size() == 1);
    List<File> dup0 = dups.get(0);
    assertTrue(dup0.size() == 3);

  }
}
