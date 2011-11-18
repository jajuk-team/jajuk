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
package org.jajuk.services.webradio;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.MD5Processor;
import org.xml.sax.SAXException;

/**
   * Preset radios parser
   */
public class TestPresetPersistenceHelper extends JajukTestCase {

  private WebRadioManager man = WebRadioManager.getInstance();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    man.cleanup();
  }

  /**
   * Test the preset file download
   * @throws IOException 
   * @throws MalformedURLException 
   * @throws ParserConfigurationException 
   * @throws SAXException 
   */
  public void testDownloadPresets() throws MalformedURLException, IOException, SAXException,
      ParserConfigurationException {
    // Check for preset file, delete it if it exist
    File fwebradios = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_PRESET);
    if (fwebradios.exists()) {
      fwebradios.delete();
    }
    downloadPresets();

    File cachedPreset = DownloadManager.downloadToCache(new URL(Const.URL_WEBRADIO_PRESETS));
    WebRadioHelper.loadPresetsRadios(cachedPreset);

    //Check the repo is not void
    WebRadioManager man = WebRadioManager.getInstance();
    assertTrue(man.getElementCount() > 0);
  }

  /**
   * Test the preset file load
   * @throws IOException 
   * @throws MalformedURLException 
   * @throws ParserConfigurationException 
   * @throws SAXException 
   */
  public void testLoadRepository() throws MalformedURLException, IOException, SAXException,
      ParserConfigurationException {
    // Make sure the repository has been cleared during the setUp()
    assertTrue(man.getElementCount() == 0);

    // Check for preset file, delete it if it exist
    File cachedPreset = DownloadManager.downloadToCache(new URL(Const.URL_WEBRADIO_PRESETS));
    if (cachedPreset.exists()) {
      cachedPreset.delete();
    }
    downloadPresets();

    // Load the presets
    WebRadioHelper.loadPresetsRadios(cachedPreset);

    //Check the repo is not void
    assertTrue(man.getElementCount() > 0);

    // Check item are sorted
    String previous = null;
    List<WebRadio> radios = man.getWebRadios();
    for (WebRadio radio : radios) {
      if (previous != null) {
        // >0, not >= because dups are forbidden
        assertTrue(radio.getName().compareTo(previous) > 0);
        previous = radio.getName();
      }
    }

    /* Check a sample radio :  
                <name>LuNe Radio</name>
                <url>http://broadcaster.infomaniak.ch/lune-high.mp3.m3u</url>
                <label>Switzerland</label>
                <keywords>foo;bar</keywords>
                <isRemoved>false</isRemoved>  //not used by Jajuk
                <bitrate>128</bitrate>
                <frequency>44100</frequency>
    */
    String name = "LuNe Radio";
    WebRadio radio = man.getWebRadioByName(name);
    assertTrue(radio.getUrl().equals("http://broadcaster.infomaniak.ch/lune-high.mp3.m3u"));
    assertTrue(radio.getLabel().equals("Switzerland"));
    assertTrue(radio.getLongValue(Const.XML_BITRATE) == 128);
    assertTrue(radio.getLongValue(Const.XML_FREQUENCY) == 44100);
    assertTrue(radio.getID().equals(MD5Processor.hash(name.toLowerCase())));

    // Check origin
    assertTrue(WebRadioOrigin.PRESET.equals(radio.getValue(Const.XML_ORIGIN)));
  }

  private File downloadPresets() throws MalformedURLException, IOException {
    // Download and load it it
    File cachedPreset = DownloadManager.downloadToCache(new URL(Const.URL_WEBRADIO_PRESETS));
    return cachedPreset;
  }

  public void testPresetRadiosCommit() throws Exception {
    // Add a few radios and commit the preset file
    // Fill few radio
    JUnitHelpers.getWebRadio("Preset1", "http://preset1", WebRadioOrigin.PRESET);
    JUnitHelpers.getWebRadio("Preset2", "http://preset2", WebRadioOrigin.PRESET);
    PresetRadiosPersistenceHelper.commit();
    // Load the sample file
    WebRadioHelper
        .loadPresetsRadios(SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_PRESET));
    //Make sure keywords will not be lost
    WebRadio radio1 = WebRadioManager.getInstance().getWebRadioByName("Preset1");
    radio1.setProperty(Const.XML_KEYWORDS, "foo;bar");
    // Write it down 
    PresetRadiosPersistenceHelper.commit();
    // Cleanup radios
    WebRadioManager.getInstance().cleanup();
    // load it again
    WebRadioHelper
        .loadPresetsRadios(SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_PRESET));
    // Check that the preset keywords are not lost
    radio1 = WebRadioManager.getInstance().getWebRadioByName("Preset1");
    assertEquals(radio1.getKeywords(), "foo;bar");
  }
}
