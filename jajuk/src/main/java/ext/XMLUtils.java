/**
 * aTunes 1.6.6
 *
 * This file has been adapted to Jajuk by the Jajuk Team.
 * Jajuk Copyright (C) 2007 The Jajuk Team
 *
 * The original copyrights and license follow:
 *
 * Copyright (C) 2006-2007 Alex Aranda (fleax) alex@atunes.org
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package ext;

import java.io.CharArrayReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jajuk.util.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public final class XMLUtils {
  /**
   * private constructor to avoid instantiating utility class
   */
  private XMLUtils() {
  }

  public static Element getChildElement(Element el, String tagName) {
    if (el == null) {
      return null;
    }

    NodeList list = el.getElementsByTagName(tagName);
    if (list != null && list.getLength() > 0) {
      return (Element) list.item(0);
    }
    return null;
  }

  public static String getAttributeValue(Element el, String attributeName) {
    return (null == el ? null : el.getAttribute(attributeName));
  }

  public static String getChildElementContent(Element el, String tagName) {
    Element el2 = getChildElement(el, tagName);
    return el2 == null ? "" : el2.getTextContent();
  }

  /**
   * Return a DOM document for a given string <br>
   * In case of parsing error, this method handles the exception and null is
   * returned
   * 
   * @param xml
   *          the string to parse
   * @return a DOM document for a given string
   */
  public static Document getDocument(String xml) {
    Document out = null;
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Reader reader = new CharArrayReader(xml.toCharArray());
      out = builder.parse(new InputSource(reader));
    } catch (Exception e) {
      Log.error(e);
    }
    return out;
  }

}
