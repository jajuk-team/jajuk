/*
 *  Jajuk
 *  Copyright (C) 2006 Ronak Patel
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

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jajuk.util.log.Log;

/**
 * This class will take a XML file and either convert it to HTML or PDF.
 * 
 * @author Ronak Patel
 * @created Aug 22, 2006
 */
public class XMLTransformer {
	/**
	 * This method will convert an XML String into an HTML String using 
	 * an xslt transformation
	 * 
	 * @param xml
	 *            The XML String to convert.
	 * @param xsltPath
	 *            The path to the XSLT Transform to use.
	 * @return Returns a string containing HTML markup.
	 */
	public static String xmlToHTML(String xml, URL xsltPath) {
		String content = null;

		try {
			StringWriter writer = new StringWriter();
			StringReader reader = new StringReader(xml);

			File xslt = new File(xsltPath.getFile());

			// Create streams for the XML String and XSLT File
			StreamSource xmlStream = new StreamSource(reader);
			StreamSource xsltStream = new StreamSource(xslt);

			// Get an instance of the TransformerFactory
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer;

			// Load the XSLT File Stream.
			transformer = transformerFactory.newTransformer(xsltStream);
			// Do the transformation.
			transformer.transform(xmlStream, new StreamResult(writer));

			// Save output.
			content = writer.toString();
		} catch (TransformerConfigurationException e) {
			Log.error(e);
		} catch (TransformerException e) {
			Log.error(e);
		}

		return content;
	}
	
}
