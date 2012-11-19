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
package org.jajuk.ui.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.services.webradio.WebRadioOrigin;
import org.jajuk.util.Const;
import org.junit.Test;

/**
 * Test methods for {@link org.jajuk.ui.helpers.WebRadioTableModel}.
 */
public class TestWebRadioTableModel extends JajukTestCase {
  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testModel() {
    // Load radios
    TestHelpers.getWebRadios();
    //Create and populate the model
    WebRadioTableModel model = new WebRadioTableModel();
    model.populateModel(getColumnsConf(Const.XML_PLAY + ',' + Const.XML_NAME + ','
        + Const.XML_KEYWORDS + Const.XML_GENRE + ',' + Const.XML_ORIGIN + ',' + Const.XML_BITRATE
        + ',' + Const.XML_URL));
    // Check the model (Note that we check that the rows are sorted at the same time)
    assertEquals(model.getRowCount(), 4);
    assertEquals(model.getColumnCount(), 9);
    assertEquals(model.getValueAt(0, 1), "Custom 1");
    assertEquals(model.getValueAt(0, 2), "a cool radio");
    assertEquals(model.getValueAt(0, 3), "http://custom1");
    assertEquals(model.getValueAt(0, 4), "foo,bar");
    assertEquals(model.getValueAt(0, 5), "Pop");
    assertEquals(model.getValueAt(0, 6), WebRadioOrigin.CUSTOM.name());
    assertEquals(model.getValueAt(0, 7), new Long(127));
    // Frequency is not shown
    assertEquals(model.getValueAt(0, 8), new Long(45000));
    assertEquals(model.getValueAt(3, 1), "Preset 2");
    assertEquals(model.getValueAt(3, 3), "http://preset2");
    assertEquals(model.getValueAt(3, 6), WebRadioOrigin.PRESET.name());
    assertEquals(model.getValueAt(3, 8), 0l);
  }

  private List<String> getColumnsConf(String sConf) {
    List<String> alOut = new ArrayList<String>(10);
    StringTokenizer st = new StringTokenizer(sConf, ",");
    while (st.hasMoreTokens()) {
      alOut.add(st.nextToken());
    }
    return alOut;
  }
}
