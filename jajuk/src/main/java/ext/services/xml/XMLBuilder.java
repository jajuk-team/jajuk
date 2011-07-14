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
 *  $Revision$
 */

package ext.services.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * DOCUMENT_ME.
 */
public final class XMLBuilder {

  /**
   * private constructor to avoid instantiating utility class.
   */
  private XMLBuilder() {
  }

  /**
   * Gets the xML document.
   * 
   * @param xml DOCUMENT_ME
   * 
   * @return the xML document
   */
  public static Document getXMLDocument(String xml) {
    if ((null != xml) && (xml.length() != 0)) {
      try {
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return parser.parse(new InputSource(new StringReader(xml)));
      } catch (SAXException e) {
        return null;
      } catch (IOException e) {
        return null;
      } catch (ParserConfigurationException e) {
        return null;
      }
    }
    return null;
  }
}
