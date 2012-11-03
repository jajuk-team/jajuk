/*
 *  Jajuk
 *  Copyright (C) 2003-2012 The Jajuk Team
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
 */
package org.jajuk.ui.actions;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jajuk.ConstTest;
import org.jajuk.TestHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Collection;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;
import org.jajuk.util.error.JajukException;
import org.xml.sax.SAXException;

public class TestRatingsImportExport extends JajukTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  public void testExportImport() throws IOException, SAXException, JajukException,
      ParserConfigurationException {
    ExportRatingsAction exportRatings = new ExportRatingsAction();
    ImportRatingsAction importRatings = new ImportRatingsAction();
    StartupCollectionService.registerItemManagers();
    Collection coll = Collection.getInstance();
    assertNotNull(coll);
    java.io.File file = java.io.File.createTempFile("testcoll", ".xml", new java.io.File(
        ConstTest.TECH_TESTS_PATH));
    // delete the file before writing the collection
    assertTrue(file.delete());
    // write ratings without any item
    exportRatings.exportRatings(file);
    // now it should exist and have some content
    assertTrue(file.exists());
    String str = FileUtils.readFileToString(file);
    assertTrue(str, StringUtils.isNotBlank(str));
    assertTrue(str, str.contains("<" + Const.XML_TRACKS));
    assertFalse(str, str.contains(" " + Const.XML_TRACK_HITS));
    // now with some content
    String id = TestHelpers.getTrack(5).getID();
    Track track = TrackManager.getInstance().getTrackByID(id);
    track.setHits(29);
    track.setProperty(Const.XML_TRACK_PREFERENCE, -2l);
    track.setProperty(Const.XML_TRACK_BANNED, true);
    // delete the file before writing the tracks
    assertTrue(file.delete());
    // commit without any item
    exportRatings.exportRatings(file);
    // now it should exist and have some content
    assertTrue(file.exists());
    str = FileUtils.readFileToString(file);
    assertTrue(str, StringUtils.isNotBlank(str));
    assertTrue(str, str.contains("<" + Const.XML_TRACKS));
    // it should contain the track id and the rate that we set
    assertFalse(str, str.contains("id=\"" + id + "\""));
    assertFalse(str, str.contains(" " + Const.XML_TRACK_HITS + "=\"29\""));
    // change the rate that we use internally and set it banned
    assertEquals(29l, track.getHits());
    assertEquals(true, track.getValue(Const.XML_TRACK_BANNED));
    assertEquals(-2l, track.getLongValue(Const.XML_TRACK_PREFERENCE));
    //Change values
    track.setProperty(Const.XML_TRACK_PREFERENCE, -3l);
    track.setHits(87);
    track.setProperty(Const.XML_TRACK_BANNED, true);
    assertEquals(87l, track.getHits());
    assertEquals(true, track.getProperties().remove(Const.XML_TRACK_BANNED));
    assertEquals(-3l, track.getProperties().remove(Const.XML_TRACK_PREFERENCE));
    // import
    importRatings.importRatings(file);
    // now we need to have the restored settings again
    assertEquals(29l, track.getHits());
    assertEquals(true, track.getValue(Const.XML_TRACK_BANNED));
    assertEquals(-2l, track.getLongValue(Const.XML_TRACK_PREFERENCE));
  }
}
