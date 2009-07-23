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

import com.thoughtworks.xstream.XStream;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class XMLUtils {
  /** The x stream. */
  private static XStream xStream = new XStream();

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
   * Writes an object to an XML file.
   * 
   * @param bean
   *          the bean
   * @param filename
   *          the filename
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void writeBeanToFile(Object bean, String filename) throws IOException {
    XMLEncoder encoder = null;
    try {
      encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(filename)));
      encoder.writeObject(bean);
    } finally {
      if (encoder != null) {
        encoder.close();
      }
    }
  }

  /**
   * Reads an object from an XML file.
   * 
   * @param filename
   *          the filename
   * 
   * @return the object
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static Object readBeanFromFile(String filename) throws IOException {
    XMLDecoder decoder = null;
    try {
      decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(filename)));
      Object bean = decoder.readObject();
      return bean;
    } finally {
      if (decoder != null) {
        decoder.close();
      }
    }
  }

  /**
   * Reads an object from a file as xml.
   * 
   * @param filename
   *          filename
   * 
   * @return The object read from the xml file
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static Object readObjectFromFile(String filename) throws IOException {
    InputStreamReader inputStreamReader = null;
    try {
      inputStreamReader = new InputStreamReader(new FileInputStream(filename), "UTF-8");
      return xStream.fromXML(inputStreamReader);
    } finally {
      if (inputStreamReader != null) {
        inputStreamReader.close();
      }
    }
  }

  /**
   * Reads an object from a String as xml.
   * 
   * @param string
   *          the string
   * 
   * @return The object read from the xml string
   */
  public static Object readObjectFromString(String string) {
    return xStream.fromXML(string);
  }

  /**
   * Writes an object to a file as xml.
   * 
   * @param object
   *          Object that should be writen to a xml file
   * @param filename
   *          filename
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void writeObjectToFile(Object object, String filename) throws IOException {
    OutputStreamWriter outputStreamWriter = null;
    try {
      outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
      xStream.toXML(object, outputStreamWriter);
    } finally {
      if (outputStreamWriter != null) {
        outputStreamWriter.flush();
        outputStreamWriter.close();
      }
    }
  }

}
