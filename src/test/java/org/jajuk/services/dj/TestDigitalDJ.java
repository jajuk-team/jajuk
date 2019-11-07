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
package org.jajuk.services.dj;

import ext.services.xml.XMLUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.util.Const;
import org.w3c.dom.Document;

/**
 * .
 */
public class TestDigitalDJ extends JajukTestCase {
  @Override
  protected void specificSetUp() throws Exception {
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#hashCode()}.
   */
  public final void testHashCode() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    DigitalDJ equ = new AmbienceDigitalDJ("3");
    equ.setName("ambience1");
    TestHelpers.HashCodeTest(dj, equ);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.DigitalDJ#DigitalDJ(java.lang.String)}.
   */
  public final void testDigitalDJ() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    assertNotNull(dj);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#toString()}.
   */
  public final void testToString() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    TestHelpers.ToStringTest(dj);
    // also test null values
    dj = new AmbienceDigitalDJ(null);
    dj.setName(null);
    TestHelpers.ToStringTest(dj);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.DigitalDJ#compareTo(org.jajuk.services.dj.DigitalDJ)}
   * .
   */
  public final void testCompareTo() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    DigitalDJ equ = new AmbienceDigitalDJ("3");
    equ.setName("ambience1");
    DigitalDJ notequ = new AmbienceDigitalDJ("3");
    notequ.setName("other");
    // only compares on name
    TestHelpers.CompareToTest(dj, equ, notequ);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#toXML()}.
   */
  public final void testToXML() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    // abstract in DigitalDJ anyway, so no need to test this in detail here...
    assertTrue(dj.toXML(), StringUtils.isNotBlank(dj.toXML()));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.DigitalDJ#toXMLGeneralParameters()}.
   */
  public final void testToXMLGeneralParameters() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    assertTrue(dj.toXMLGeneralParameters(), StringUtils.isNotBlank(dj.toXMLGeneralParameters()));
    // test xml-validity, need to add closing tag to build complete xml
    String xml = dj.toXMLGeneralParameters() + "</" + Const.XML_DJ_DJ + ">";
    Document document = XMLUtils.getDocument(xml);
    assertNotNull(document);
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.dj.DigitalDJ#filterFilesByRate(java.util.List)}.
   */
  public final void testFilterFilesByRate() throws Exception {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    List<File> files = new ArrayList<File>();
    File file = getFile(1);
    file.getTrack().setProperty(Const.XML_TRACK_RATE, 1l);
    files.add(file);
    file = getFile(2);
    file.getTrack().setProperty(Const.XML_TRACK_RATE, 26l);
    files.add(file);
    file = getFile(3);
    file.getTrack().setProperty(Const.XML_TRACK_RATE, 51l);
    files.add(file);
    // without rating level set nothing happens to the list
    dj.filterFilesByRate(files);
    assertEquals(3, files.size());
    // set rating level and see if tracks are removed
    dj.setRatingLevel(2);
    dj.filterFilesByRate(files);
    assertEquals(1, files.size());
  }

  /**
   * Test filter files by max track.
   * 
   *
   * @throws Exception the exception
   */
  public final void testFilterFilesByMaxTrack() throws Exception {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    List<File> files = new ArrayList<File>();
    File file = getFile(1);
    file.getTrack().setProperty(Const.XML_TRACK_RATE, 1l);
    files.add(file);
    file = getFile(2);
    file.getTrack().setProperty(Const.XML_TRACK_RATE, 26l);
    files.add(file);
    file = getFile(3);
    file.getTrack().setProperty(Const.XML_TRACK_RATE, 51l);
    files.add(file);
    // with max-tracks -1 nothing is cut off
    dj.setMaxTracks(-1);
    dj.filterFilesByMaxTrack(files);
    assertEquals(3, files.size());
    // nothing happens if max is higher
    dj.setMaxTracks(4);
    dj.filterFilesByMaxTrack(files);
    assertEquals(3, files.size());
    // set max level and see if tracks are removed
    dj.setMaxTracks(2);
    dj.filterFilesByMaxTrack(files);
    assertEquals(2, files.size());
  }

  /**
   * Gets the file.
   *
   * @param i 
   * @return the file
   * @throws Exception the exception
   */
  private File getFile(int i) throws Exception {
    Genre genre = TestHelpers.getGenre();
    Album album = TestHelpers.getAlbum("name" + i, 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test
    Artist artist = TestHelpers.getArtist("name");
    Year year = TestHelpers.getYear(2000);
    Type type = TestHelpers.getType();
    Track track = TrackManager.getInstance().registerTrack("name", album, genre, artist, 120, year,
        1, type, 1);
    Device device = TestHelpers.getDevice();
    if (!device.isMounted()) {
      device.mount(true);
    }
    Directory dir = DirectoryManager.getInstance().registerDirectory(device);
    return FileManager.getInstance().registerFile("test.tst", dir, track, 120, 70);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#getName()}.
   */
  public final void testGetAndSetName() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    assertNull(dj.getName());
    dj.setName("ambience1");
    assertEquals("ambience1", dj.getName());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.DigitalDJ#equals(java.lang.Object)}.
   */
  public final void testEqualsObject() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    DigitalDJ equ = new AmbienceDigitalDJ("3");
    equ.setName("ambience1");
    DigitalDJ notequ = new AmbienceDigitalDJ("3");
    notequ.setName("other");
    // only compares on name
    TestHelpers.EqualsTest(dj, equ, notequ);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.DigitalDJ#setName(java.lang.String)}.
   */
  public final void testSetName() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#getFadingDuration()}
   * .
   */
  public final void testGetAndSetFadingDuration() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    assertEquals(0, dj.getFadingDuration());
    dj.setFadingDuration(3);
    assertEquals(3, dj.getFadingDuration());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.DigitalDJ#setFadingDuration(int)}.
   */
  public final void testSetFadingDuration() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#getRatingLevel()}.
   */
  public final void testGetAndSetRatingLevel() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    assertEquals(0, dj.getRatingLevel());
    dj.setRatingLevel(3);
    assertEquals(3, dj.getRatingLevel());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#setRatingLevel(int)}
   * .
   */
  public final void testSetRatingLevel() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#generatePlaylist()}.
   */
  public final void testGeneratePlaylist() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    // abstract method, do not check result here
    dj.generatePlaylist();
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#getID()}.
   */
  public final void testGetID() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    assertEquals("3", dj.getID());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#isTrackUnicity()}.
   */
  public final void testIsAndSetTrackUnicity() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    assertFalse(dj.isTrackUnicity());
    dj.setTrackUnicity(true);
    assertTrue(dj.isTrackUnicity());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.DigitalDJ#setTrackUnicity(boolean)}.
   */
  public final void testSetTrackUnicity() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#getRatingLevel()}.
   */
  public final void testGetAndSetMaxTracks() {
    DigitalDJ dj = new AmbienceDigitalDJ("3");
    dj.setName("ambience1");
    assertEquals(-1, dj.getMaxTracks());
    dj.setMaxTracks(3);
    assertEquals(3, dj.getMaxTracks());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.DigitalDJ#setRatingLevel(int)}
   * .
   */
  public final void testSetMaxTracks() {
    // tested above
  }
}
