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
package ext;

import junit.framework.TestCase;

import org.jajuk.JUnitHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class TestXMLUtils extends TestCase {

//helper method to emma-coverage of the unused constructor
  public void testSerializableUtilitiesPrivateConstructor() throws Exception
  {
     //For EMMA code-coverage tests
     JUnitHelpers.executePrivateConstructor(XMLUtils.class);
  }
  
  /**
   * Test method for {@link ext.XMLUtils#getChildElement(org.w3c.dom.Element, java.lang.String)}.
   */

  public void testGetChildElement() {
    Document doc = XMLUtils.getDocument("<xml><test/></xml>");
    assertNotNull(doc);
    
    assertNotNull(XMLUtils.getChildElement(doc.getDocumentElement(), "test"));
  }

  public void testGetChildElementNull() {
    assertNull(XMLUtils.getChildElement(null, "test"));
  }
  
  public void testGetChildElementNotExisting() {
    Document doc = XMLUtils.getDocument("<xml><test/></xml>");
    assertNotNull(doc);
    
    assertNull(XMLUtils.getChildElement(doc.getDocumentElement(), "notexist"));
  }
  
  /**
   * Test method for {@link ext.XMLUtils#getAttributeValue(org.w3c.dom.Element, java.lang.String)}.
   */

  public void testGetAttributeValue() {
    Document doc = XMLUtils.getDocument("<xml><test value=\"1\"/></xml>");
    assertNotNull(doc);
    
    Element test = XMLUtils.getChildElement(doc.getDocumentElement(), "test");
    assertNotNull(test);
    
    assertNotNull(XMLUtils.getAttributeValue(test, "value"));
    assertEquals("", XMLUtils.getAttributeValue(test, "notexist"));
    assertEquals("", XMLUtils.getAttributeValue(doc.getDocumentElement(), "value"));
  }

  public void testGetAttributeValueNull() {
    assertNull(XMLUtils.getAttributeValue(null, "value"));
  }
  
  /**
   * Test method for {@link ext.XMLUtils#getChildElementContent(org.w3c.dom.Element, java.lang.String)}.
   */

  public void testGetChildElementContent() {
    Document doc = XMLUtils.getDocument("<xml><test value=\"1\">testcontent</test></xml>");
    assertNotNull(doc);
    
    assertEquals("testcontent", XMLUtils.getChildElementContent(doc.getDocumentElement(), "test"));
  }

  public void testGetChildElementContentNull() {
    assertEquals("", XMLUtils.getChildElementContent(null, "test"));
  }

  /**
   * Test method for {@link ext.XMLUtils#getDocument(java.lang.String)}.
   */

  public void testGetDocument() {
    Document doc = XMLUtils.getDocument("<xml><test value=\"1\"/></xml>");
    assertNotNull(doc);
    
    assertEquals("xml", doc.getDocumentElement().getTagName());
  }

  public void testGetDocumentParseError() {
    Document doc = XMLUtils.getDocument("<xmlinvalid>adsasd<asdksdtest value=\"1\"/></xml>");
    assertNull(doc);
  }
  
}
