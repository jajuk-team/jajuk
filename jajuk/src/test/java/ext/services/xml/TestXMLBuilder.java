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
package ext.services.xml;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;

/**
 * .
 */
public class TestXMLBuilder extends JajukTestCase {
  /**
   * Test method for.
   *
   * {@link ext.services.xml.XMLBuilder#getXMLDocument(java.lang.String)}.
   */
  public void testGetXMLDocument() {
    // simple xml...
    assertNotNull(XMLBuilder.getXMLDocument("<xml></xml>"));
    // empty values should be silently ignored
    assertNull(XMLBuilder.getXMLDocument(null));
    assertNull(XMLBuilder.getXMLDocument(""));
    // some invalid XML should cause an exception internally but report null
    assertNull(XMLBuilder.getXMLDocument("<xml>invliad document without end tag..."));
  }

  // helper method to emma-coverage of the unused constructor
  /**
   * Test private constructor.
   * 
   *
   * @throws Exception the exception
   */
  public void testPrivateConstructor() throws Exception {
    TestHelpers.executePrivateConstructor(XMLBuilder.class);
  }
}
