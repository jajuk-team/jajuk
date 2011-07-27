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
<<<<<<< HEAD
 *  $Revision$
=======
 *  $Revision: 3132 $
>>>>>>> hotfix/1.9.5
 */
package org.jajuk.base;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.junit.Test;

/**
<<<<<<< HEAD
 * DOCUMENT_ME.
 */
public class TestTrackManager extends JajukTestCase {

  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
=======
 * 
 */
public class TestTrackManager extends JajukTestCase {

>>>>>>> hotfix/1.9.5
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#getLabel()}.
   */
  @Test
  public void testGetLabel() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#cleanup()}.
   */
  @Test
  public void testCleanup() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#getInstance()}.
   */
  @Test
  public void testGetInstance() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#registerTrack(java.lang.String, org.jajuk.base.Album, org.jajuk.base.Genre, org.jajuk.base.Artist, long, org.jajuk.base.Year, long, org.jajuk.base.Type, long)}.
   */
  @Test
  public void testRegisterTrackStringAlbumGenreArtistLongYearLongTypeLong() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#createID(java.lang.String, org.jajuk.base.Album, org.jajuk.base.Genre, org.jajuk.base.Artist, long, org.jajuk.base.Year, long, org.jajuk.base.Type, long)}.
   */
  @Test
  public void testCreateIDStringAlbumGenreArtistLongYearLongTypeLong() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#registerTrack(java.lang.String, java.lang.String, org.jajuk.base.Album, org.jajuk.base.Genre, org.jajuk.base.Artist, long, org.jajuk.base.Year, long, org.jajuk.base.Type, long)}.
   */
  @Test
  public void testRegisterTrackStringStringAlbumGenreArtistLongYearLongTypeLong() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#commit()}.
   */
  @Test
  public void testCommit() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackAlbum(org.jajuk.base.Track, java.lang.String, java.util.Set)}.
   */
  @Test
  public void testChangeTrackAlbum() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackArtist(org.jajuk.base.Track, java.lang.String, java.util.Set)}.
   */
  @Test
  public void testChangeTrackArtist() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackGenre(org.jajuk.base.Track, java.lang.String, java.util.Set)}.
   */
  @Test
  public void testChangeTrackGenre() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackYear(org.jajuk.base.Track, java.lang.String, java.util.Set)}.
   */
  @Test
  public void testChangeTrackYear() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackComment(org.jajuk.base.Track, java.lang.String, java.util.Set)}.
   */
  @Test
  public void testChangeTrackComment() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackRate(org.jajuk.base.Track, long)}.
   */
  @Test
  public void testChangeTrackRate() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackOrder(org.jajuk.base.Track, long, java.util.Set)}.
   */
  @Test
  public void testChangeTrackOrder() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackName(org.jajuk.base.Track, java.lang.String, java.util.Set)}.
   */
  @Test
  public void testChangeTrackName() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackAlbumArtist(org.jajuk.base.Track, java.lang.String, java.util.Set)}.
   */
  @Test
  public void testChangeTrackAlbumArtist() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#changeTrackDiscNumber(org.jajuk.base.Track, long, java.util.Set)}.
   */
  @Test
  public void testChangeTrackDiscNumber() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#removeFile(org.jajuk.base.Track, org.jajuk.base.File)}.
   */
  @Test
  public void testRemoveFile() {
    // Set-up...
    File file = JUnitHelpers.getFile();

    // Remove the reference
    TrackManager.getInstance().removeFile(file);

    // Check if the collection no more contains the track (as it mapped a single file now removed)
    assertTrue(TrackManager.getInstance().getTrackByID(file.getTrack().getID()) == null);

    // Check if the associated track no more contains this file
    assertFalse(file.getTrack().getFiles().contains(file));
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#getAssociatedTracks(org.jajuk.base.Item, boolean)}.
   */
  @Test
  public void testGetAssociatedTracksItemBoolean() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#getAssociatedTracks(java.util.List, boolean)}.
   */
  @Test
  public void testGetAssociatedTracksListOfItemBoolean() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#getComparator()}.
   */
  @Test
  public void testGetComparator() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#getTrackByID(java.lang.String)}.
   */
  @Test
  public void testGetTrackByID() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#getTracks()}.
   */
  @Test
  public void testGetTracks() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#getTracksIterator()}.
   */
  @Test
  public void testGetTracksIterator() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#search(java.lang.String)}.
   */
  @Test
  public void testSearch() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#isAutocommit()}.
   */
  @Test
  public void testIsAutocommit() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.TrackManager#setAutocommit(boolean)}.
   */
  @Test
  public void testSetAutocommit() {
    //TODO To be implemented
  }

}
