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
public class TestTrack extends JajukTestCase {

  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
=======
 * 
 */
public class TestTrack extends JajukTestCase {

>>>>>>> hotfix/1.9.5
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getDesc()}.
   */
  @Test
  public void testGetDesc() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getAny()}.
   */
  @Test
  public void testGetAny() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getLabel()}.
   */
  @Test
  public void testGetLabel() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getHumanValue(java.lang.String)}.
   */
  @Test
  public void testGetHumanValue() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getIconRepresentation()}.
   */
  @Test
  public void testGetIconRepresentation() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getRate()}.
   */
  @Test
  public void testGetRate() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#toString()}.
   */
  @Test
  public void testToString() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#Track(java.lang.String, java.lang.String, org.jajuk.base.Album, org.jajuk.base.Genre, org.jajuk.base.Artist, long, org.jajuk.base.Year, long, org.jajuk.base.Type, long)}.
   */
  @Test
  public void testTrack() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#compareTo(org.jajuk.base.Track)}.
   */
  @Test
  public void testCompareTo() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getAlbum()}.
   */
  @Test
  public void testGetAlbum() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getFiles()}.
   */
  @Test
  public void testGetFiles() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#removeFile(org.jajuk.base.File)}.
   */
  @Test
  public void testRemoveFile() {
    // Set-up...
    File file = JUnitHelpers.getFile();
    Track track = file.getTrack();

    // Remove the reference
    track.removeFile(file);

    // Check that associated track no more contains this file
    assertFalse(file.getTrack().getFiles().contains(file));
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getFiles(java.util.Set)}.
   */
  @Test
  public void testGetFilesSetOfFile() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getReadyFiles()}.
   */
  @Test
  public void testGetReadyFiles() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getReadyFiles(java.util.Set)}.
   */
  @Test
  public void testGetReadyFilesSetOfFile() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getTotalSize()}.
   */
  @Test
  public void testGetTotalSize() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getBestFile(boolean)}.
   */
  @Test
  public void testGetBestFile() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getHits()}.
   */
  @Test
  public void testGetHits() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getComment()}.
   */
  @Test
  public void testGetComment() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getOrder()}.
   */
  @Test
  public void testGetOrder() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getDiscNumber()}.
   */
  @Test
  public void testGetDiscNumber() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getAlbumArtist()}.
   */
  @Test
  public void testGetAlbumArtist() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getAlbumArtistOrArtist()}.
   */
  @Test
  public void testGetAlbumArtistOrArtist() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getYear()}.
   */
  @Test
  public void testGetYear() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getDuration()}.
   */
  @Test
  public void testGetDuration() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getDiscoveryDate()}.
   */
  @Test
  public void testGetDiscoveryDate() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getType()}.
   */
  @Test
  public void testGetType() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getArtist()}.
   */
  @Test
  public void testGetArtist() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getGenre()}.
   */
  @Test
  public void testGetGenre() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#addFile(org.jajuk.base.File)}.
   */
  @Test
  public void testAddFile() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#setHits(long)}.
   */
  @Test
  public void testSetHits() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#incHits()}.
   */
  @Test
  public void testIncHits() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#setPreference(long)}.
   */
  @Test
  public void testSetPreference() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#updateRate()}.
   */
  @Test
  public void testUpdateRate() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#setRate(long)}.
   */
  @Test
  public void testSetRate() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#setComment(java.lang.String)}.
   */
  @Test
  public void testSetComment() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#setAlbumArtist(org.jajuk.base.AlbumArtist)}.
   */
  @Test
  public void testSetAlbumArtist() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#setDiscoveryDate(java.util.Date)}.
   */
  @Test
  public void testSetDiscoveryDate() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#shouldBeHidden()}.
   */
  @Test
  public void testShouldBeHidden() {
    //TODO To be implemented
  }

  /**
   * Test method for {@link org.jajuk.base.Track#getFilesString()}.
   */
  @Test
  public void testGetFilesString() {
    //TODO To be implemented
  }

}
