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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.jajuk.JajukTestCase;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;

/**
   * Custom radios parser
   */
public class TestCustomPersistenceHelper extends JajukTestCase {

  private WebRadioManager man = WebRadioManager.getInstance();

  public void setUp() throws Exception {
    super.setUp();
    man.cleanup();
    writeSampleFile();
  }

  private void writeSampleFile() throws Exception {
    // Write down the sample custom radios file
    File fCustom = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_CUSTOM);
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fCustom));
    bos.write(sampleFile.getBytes());
    bos.flush();
    bos.close();
  }

  /**
   * Test the custom file load
   * @throws IOException 
   * @throws MalformedURLException 
   */
  public void testLoadRepository() throws Exception {
    // Make sure the repository has been cleared during the setUp()
    assertTrue(man.getElementCount() == 0);

    // Load the presets
    WebRadioHelper.loadCustomRadios();

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

    WebRadio radio = radioTest();

    // Check origin
    assertTrue(WebRadioOrigin.CUSTOM.equals(radio.getValue(Const.XML_ORIGIN)));
  }

  private WebRadio radioTest() throws Exception {
    String name = "LuNe Radio";
    WebRadio radio = man.getWebRadioByName(name);
    assertTrue(radio.getUrl().equals("http://broadcaster.infomaniak.ch/lune-high.mp3.m3u"));
    assertTrue(radio.getLabel().equals("Switzerland"));
    assertTrue(radio.getLongValue(Const.XML_BITRATE) == 128);
    assertTrue(radio.getLongValue(Const.XML_FREQUENCY) == 44100);
    assertTrue("talk;good".equals(radio.getKeywords()));
    assertTrue(radio.getID().equals(MD5Processor.hash(name.toLowerCase())));
    return radio;
  }

  public void testCustomRadiosCommit() throws Exception {
    // Load the sample file (written in setUp())
    WebRadioHelper.loadCustomRadios();
    // Write it down 
    CustomRadiosPersistenceHelper.commit();
    // load it again
    WebRadioHelper.loadCustomRadios();
  }

  /** Sample file content */
  private static final String sampleFile = "<?xml version='1.0' encoding='UTF-8'?>"
      + "<streams jajuk_version='"
      + Const.TEST_VERSION
      + "'>"
      + "<stream name='1449 AM URB' url='http://people.bath.ac.uk/su9urb/audio/urb-hi.m3u'/>"
      + "<stream name='LuNe Radio' url='http://broadcaster.infomaniak.ch/lune-high.mp3.m3u' label='Switzerland' bitrate='128' frequency='44100' keywords='talk;good'/>"
      + "<stream name='Bayern 1' url='http://streams.br-online.de/bayern1_1.asx'/>"
      + "<stream name='Bayern Aktuell' url='http://streams.br-online.de/b5aktuell_1.asx'/>"
      + "</streams>";
}
