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

import java.util.List;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;

public class TestWebRadioManager extends JajukTestCase {
  private WebRadioManager man = WebRadioManager.getInstance();
  WebRadio radio1;
  WebRadio radio2;
  WebRadio radio3;
  WebRadio radio4;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    man.cleanup();
    // Fill few radio
    radio1 = JUnitHelpers.getWebRadio("Preset1", "http://preset1", WebRadioOrigin.PRESET);
    radio2 = JUnitHelpers.getWebRadio("Preset2", "http://preset2", WebRadioOrigin.PRESET);
    radio3 = JUnitHelpers.getWebRadio("Custom1", "http://custom1", WebRadioOrigin.CUSTOM);
    radio4 = JUnitHelpers.getWebRadio("Custom2", "http://custom2", WebRadioOrigin.CUSTOM);
  }

  public void testGetWebRadiosByOrigin() throws Exception {
    List<WebRadio> shouldBeCustom = man.getWebRadiosByOrigin(WebRadioOrigin.CUSTOM);
    List<WebRadio> shouldBePreset = man.getWebRadiosByOrigin(WebRadioOrigin.PRESET);
    assertTrue(shouldBeCustom.size() == 2 && shouldBeCustom.contains(radio3));
    assertTrue(shouldBePreset.size() == 2 && shouldBePreset.contains(radio2));
  }
}
