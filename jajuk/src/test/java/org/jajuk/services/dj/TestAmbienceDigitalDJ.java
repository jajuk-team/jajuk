/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
package org.jajuk.services.dj;

import ext.services.xml.XMLUtils;
import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestAmbienceDigitalDJ extends TestCase {

  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceDigitalDJ#toXML()}.
   */
  public final void testToXML() {
    AmbienceDigitalDJ dj = new AmbienceDigitalDJ("3");
    assertTrue(StringUtils.isNotBlank(dj.toXML()));
    
    // try to parse the resulting XML
    XMLUtils.getDocument(dj.toXML());

    // set an Ambience
    StyleManager.getInstance().registerStyle("mystyle");
    dj.setAmbience(new Ambience("5", "ambience", new String[] {"mystyle"} ));

    // try to parse the resulting XML
    XMLUtils.getDocument(dj.toXML());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceDigitalDJ#generatePlaylist()}.
   * @throws Exception 
   */
  public final void testGeneratePlaylist() throws Exception {
    StartupCollectionService.registerItemManagers();
    
    AmbienceDigitalDJ dj = new AmbienceDigitalDJ("4");
    
    // empty without Ambience set
    assertEquals(0, dj.generatePlaylist().size());
    
    // set an Ambience
    Style style = StyleManager.getInstance().registerStyle("mystyle");
    dj.setAmbience(new Ambience("5", "ambience", new String[] {"mystyle"} ));
    
    getFile(6, style);
    
    assertEquals(Const.MIN_TRACKS_NUMBER_WITHOUT_UNICITY, dj.generatePlaylist().size());
    
    // once again with "unicity"
    dj.setTrackUnicity(true);
    assertEquals(1, dj.generatePlaylist().size());
    
  }

  private File getFile(int i, Style style) throws Exception {
    Album album = new Album(Integer.valueOf(i).toString(), "name", "artis", 23);
    album.setProperty(Const.XML_ALBUM_COVER, "none"); // don't read covers for
    // this test

    Author author = new Author(Integer.valueOf(i).toString(), "name");
    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    //IPlayerImpl imp = new MockPlayer();
    //Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

    Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", null, null);
    Track track = new Track(Integer.valueOf(i).toString(), "name", album, style, author, 120, year, 1,
        type, 1);

    Device device = new Device(Integer.valueOf(i).toString(), "name");
    device.setUrl(System.getProperty("java.io.tmpdir"));
    device.mount(true);

    Directory dir = new Directory(Integer.valueOf(i).toString(), "name", null, device);

    return FileManager.getInstance().registerFile(Integer.valueOf(i).toString(), "test.tst", dir, track, 120, 70);
  }

  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceDigitalDJ#AmbienceDigitalDJ(java.lang.String)}.
   */
  public final void testAmbienceDigitalDJ() {
    new AmbienceDigitalDJ("9");
  }

  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceDigitalDJ#getAmbience()}.
   */
  public final void testGetAndSetAmbience() {
    AmbienceDigitalDJ dj = new AmbienceDigitalDJ("4");
    
    // empty without Ambience set
    assertNull(dj.getAmbience());
    
    // set an Ambience
    StyleManager.getInstance().registerStyle("mystyle");
    dj.setAmbience(new Ambience("5", "ambience", new String[] {"mystyle"} ));
    
    assertNotNull(dj.getAmbience());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceDigitalDJ#setAmbience(org.jajuk.services.dj.Ambience)}.
   */
  public final void testSetAmbience() {
    // tested above
  }

}
