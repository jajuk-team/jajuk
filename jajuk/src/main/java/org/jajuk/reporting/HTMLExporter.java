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
 *  $Revision: 2118 $
 */

package org.jajuk.reporting;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.Style;
import org.jajuk.util.ITechnicalStrings;

import java.util.ArrayList;

/**
 * This class exports music contents to HTML.
 * 
 * @author Ronak Patel
 * @created Aug 22, 2006
 */
public class HTMLExporter extends Exporter implements ITechnicalStrings {
	/** Public Constants */
	public static final int PHYSICAL_COLLECTION = 0;

	public static final int LOGICAL_COLLECTION = 1;

	/** PUBLIC METHODS */

	
	/**
	 * This methods will create an html String of items
	 * 
	 * @param collection
	 *            An ArrayList of the items to export
	 * @return Returns a string containing the html markup, or null if an error
	 *         occurred.
	 */
	public String processCollection(ArrayList<Item> collection) {
		return processCollection(-1,collection);
	}
	
	/**
	 * This methods will create an html String of a collection and all its
	 * children devices, directories, and files.
	 * 
	 * @param COLLECTION_TYPE
	 *            The type of collection to export.
	 * @param collection
	 *            An ArrayList of the collection to export. Should be null if
	 *            exporting the physical collection. Just specify the
	 *            COLLECTION_TYPE.
	 * @return Returns a string containing the html markup, or null if an error
	 *         occurred.
	 */
	public String processCollection(int COLLECTION_TYPE, ArrayList<Item> collection) {
		String content = null;
		return content;

	/*	// If we are exporting the physical collection...
		if (COLLECTION_TYPE == HTMLExporter.PHYSICAL_COLLECTION) {
			// Get an instance of the XMLExporter.
			XMLExporter xmlExporter = (XMLExporter)ExporterFactory.createExporter("xml");
			// Create an xml tagging of the collection.
			String xml = xmlExporter.processCollection(XMLExporter.PHYSICAL_COLLECTION, null);
			if (xml != null) {
				content = XMLTransformer.xmlToHTML(xml, COLLECTION_XSLT);
			}
			// Else if we are exporting the logical genre collection...
		} else if (COLLECTION_TYPE == HTMLExporter.LOGICAL_COLLECTION) {
			// Get an instance of the XMLExporter.
			XMLExporter xmlExporter = (XMLExporter)ExporterFactory.createExporter("xml");
			// Create an xml tagging of the collection.
			String xml = xmlExporter.processCollection(XMLExporter.LOGICAL_GENRE_COLLECTION,
					collection);
			if (xml != null) {
				content = XMLTransformer.xmlToHTML(xml, XSLT_LOGICALSTYLE_COLLECTION);
			}
			return content;
		}*/
	}
}
