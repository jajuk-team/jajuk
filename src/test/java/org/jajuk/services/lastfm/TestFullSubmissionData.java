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
import org.jajuk.services.lastfm.model.FullSubmissionData;

/**
 * .
 */
public class TestFullSubmissionData extends TestCase {
  /**
   * Test method for.
   *
   * {@link FullSubmissionData(java.lang.String, java.lang.String, java.lang.String, int, int, java.lang.String, long)}
   * .
   */
  public void testFullSubmissionData() {
    new FullSubmissionData("artist", "title", "album", 120, 3, 4);
  }

  /**
   * Test method for {@link FullSubmissionData#getArtist()}.
   */
  public void testGetAndSetArtist() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, 4);
    assertEquals("artist", sub.getArtist());
    sub.setArtist("artist2");
    assertEquals("artist2", sub.getArtist());
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#setArtist(java.lang.String)}.
   */
  public void testSetArtist() {
    // tested above
  }

  /**
   * Test method for {@link FullSubmissionData#getTitle()}.
   */
  public void testGetTitle() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, 4);
    assertEquals("title", sub.getTitle());
    sub.setTitle("title2");
    assertEquals("title2", sub.getTitle());
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#setTitle(java.lang.String)}.
   */
  public void testSetTitle() {
    // tested above
  }

  /**
   * Test method for {@link FullSubmissionData#getAlbum()}.
   */
  public void testGetAlbum() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, 4);
    assertEquals("album", sub.getAlbum());
    sub.setAlbum("album2");
    assertEquals("album2", sub.getAlbum());
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#setAlbum(java.lang.String)}.
   */
  public void testSetAlbum() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#getDuration()}.
   */
  public void testGetDuration() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, 4);
    assertEquals(120, sub.getDuration());
    sub.setDuration(99);
    assertEquals(99, sub.getDuration());
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#setDuration(int)}.
   */
  public void testSetDuration() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#getTrackNumber()}.
   */
  public void testGetTrackNumber() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, 4);
    assertEquals(3, sub.getTrackNumber());
    sub.setTrackNumber(5);
    assertEquals(5, sub.getTrackNumber());
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#setTrackNumber(int)}.
   */
  public void testSetTrackNumber() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#getStartTime()}.
   */
  public void testGetStartTime() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, 4);
    assertEquals(4, sub.getStartTime());
    sub.setStartTime(7);
    assertEquals(7, sub.getStartTime());
  }

  /**
   * Test method for.
   *
   * {@link FullSubmissionData#setStartTime(long)}.
   */
  public void testSetStartTime() {
    // tested above
  }
}
