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
 *  
 */
package org.jajuk.services.reporting;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class will take a XML file and either convert it to HTML or PDF.
 */
public final class XMLTransformer {
  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private XMLTransformer() {
  }

  /**
   * This method will convert an XML file into an HTML file using an xslt
   * transformation.
   *
   * @param xml The XML file to convert.
   * @param html The HTML file to convert.
   * @param xsl The url of the XSLT genre sheet to use.
   * @throws ParserConfigurationException if the XML Parser can not be instantiated.
   * @throws SAXException If an exception during parsing the XML file occurs.
   * @throws IOException If the file cannot be opened.
   * @throws TransformerException If processing the XSL script causes an error.
   */
  public static void xmlToHTML(File xml, File html, URL xsl) throws ParserConfigurationException,
      SAXException, IOException, TransformerException {
    Source source = new SAXSource(
        new InputSource(new BufferedInputStream(new FileInputStream(xml))));
    // Create output file
    Result result = new StreamResult(html);
    // Transformer configuration
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    StreamSource genresource = new StreamSource(xsl.openStream());
    Transformer transformer = transformerFactory.newTransformer(genresource);
    transformer.setOutputProperty(OutputKeys.METHOD, "html");
    // Transformation
    transformer.transform(source, result);
  }
}
