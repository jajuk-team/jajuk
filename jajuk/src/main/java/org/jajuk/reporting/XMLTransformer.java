/*
 *  Jajuk
 *  Copyright (C) 2006 The Jajuk Team
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
 *  $Revision: 2315 $
 */

package org.jajuk.reporting;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This class will take a XML file and either convert it to HTML or PDF.
 */
public class XMLTransformer {
	/**
	 * This method will convert an XML file into an HTML file using an xslt
	 * transformation
	 * 
	 * @param xml
	 *            The XML file to convert.
	 * @param html
	 *            The HTML file to convert.
	 * @param xsltPath
	 *            The path to the XSLT Transform to use.
	 */
	public static void xmlToHTML(File xml, File html, File xsl) throws Exception {
		// DOM source creation
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
		Document document = domBuilder.parse(xml);
		Source source = new SAXSource(new InputSource(new BufferedInputStream(new FileInputStream(xml))));

		// Create output file
		Result result = new StreamResult(html);

		// Transformer configuration
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		StreamSource stylesource = new StreamSource(xsl);
		Transformer transformer = transformerFactory.newTransformer(stylesource);
		transformer.setOutputProperty(OutputKeys.METHOD, "html");

		// Transformation
		transformer.transform(source, result);
	}

}
