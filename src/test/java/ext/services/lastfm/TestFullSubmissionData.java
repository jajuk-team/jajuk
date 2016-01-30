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
package ext.services.lastfm;

import junit.framework.TestCase;

/**
 * .
 */
public class TestFullSubmissionData extends TestCase {
  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#FullSubmissionData(java.lang.String, java.lang.String, java.lang.String, int, int, java.lang.String, int)}
   * .
   */
  public void testFullSubmissionData() {
    new FullSubmissionData("artist", "title", "album", 120, 3, "source", 4);
  }

  /**
   * Test method for {@link ext.services.lastfm.FullSubmissionData#getArtist()}.
   */
  public void testGetAndSetArtist() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, "source", 4);
    assertEquals("artist", sub.getArtist());
    sub.setArtist("artist2");
    assertEquals("artist2", sub.getArtist());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#setArtist(java.lang.String)}.
   */
  public void testSetArtist() {
    // tested above
  }

  /**
   * Test method for {@link ext.services.lastfm.FullSubmissionData#getTitle()}.
   */
  public void testGetTitle() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, "source", 4);
    assertEquals("title", sub.getTitle());
    sub.setTitle("title2");
    assertEquals("title2", sub.getTitle());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#setTitle(java.lang.String)}.
   */
  public void testSetTitle() {
    // tested above
  }

  /**
   * Test method for {@link ext.services.lastfm.FullSubmissionData#getAlbum()}.
   */
  public void testGetAlbum() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, "source", 4);
    assertEquals("album", sub.getAlbum());
    sub.setAlbum("album2");
    assertEquals("album2", sub.getAlbum());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#setAlbum(java.lang.String)}.
   */
  public void testSetAlbum() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#getDuration()}.
   */
  public void testGetDuration() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, "source", 4);
    assertEquals(120, sub.getDuration());
    sub.setDuration(99);
    assertEquals(99, sub.getDuration());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#setDuration(int)}.
   */
  public void testSetDuration() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#getTrackNumber()}.
   */
  public void testGetTrackNumber() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, "source", 4);
    assertEquals(3, sub.getTrackNumber());
    sub.setTrackNumber(5);
    assertEquals(5, sub.getTrackNumber());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#setTrackNumber(int)}.
   */
  public void testSetTrackNumber() {
    // tested above
  }

  /**
   * Test method for {@link ext.services.lastfm.FullSubmissionData#getSource()}.
   */
  public void testGetSource() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, "source", 4);
    assertEquals("source", sub.getSource());
    sub.setSource("source2");
    assertEquals("source2", sub.getSource());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#setSource(java.lang.String)}.
   */
  public void testSetSource() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#getStartTime()}.
   */
  public void testGetStartTime() {
    FullSubmissionData sub = new FullSubmissionData("artist", "title", "album", 120, 3, "source", 4);
    assertEquals(4, sub.getStartTime());
    sub.setStartTime(7);
    assertEquals(7, sub.getStartTime());
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.FullSubmissionData#setStartTime(int)}.
   */
  public void testSetStartTime() {
    // tested above
  }
}
