/**
 * aTunes 1.6.6
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

package ext.services.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLUtils {

  public static Element getChildElement(Element el, String tagName) {
    if (el == null)
      return null;
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

}
