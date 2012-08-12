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
 *  $Revision: 3132 $
 */
package org.jajuk.services.webradio;

import java.util.List;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.util.Const;

public class TestWebRadioHelper extends JajukTestCase {
  private WebRadioManager man = WebRadioManager.getInstance();
  WebRadio radio1;
  WebRadio radio2;
  WebRadio custom1;
  WebRadio custom2;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    man.cleanup();
    // Fill few radio
    radio1 = JUnitHelpers.getWebRadio("Preset1", "http://preset1", WebRadioOrigin.PRESET);
    radio2 = JUnitHelpers.getWebRadio("Preset2", "http://preset2", WebRadioOrigin.PRESET);
    custom1 = JUnitHelpers.getWebRadio("Custom1", "http://custom1", WebRadioOrigin.CUSTOM);
    custom2 = JUnitHelpers.getWebRadio("Custom2", "http://custom2", WebRadioOrigin.CUSTOM);
  }

  public void testForcePresetsRefresh() throws Exception {
    //Make sure keywords will not be lost
    radio1.setProperty(Const.XML_KEYWORDS, "foo;bar");
    custom1.setProperty(Const.XML_KEYWORDS, "foo2;bar2");
    WebRadioHelper.forcePresetsRefresh();
    //check custom radios are still there
    List<WebRadio> shouldBeCustom = man.getWebRadiosByOrigin(WebRadioOrigin.CUSTOM);
    assertTrue(shouldBeCustom.size() == 2 && shouldBeCustom.contains(custom1));
    // Check that the preset keywords are not lost
    assertEquals(radio1.getKeywords(), "foo;bar");
    // Same thing for custom radios
    assertEquals(custom1.getKeywords(), "foo2;bar2");
  }
}
