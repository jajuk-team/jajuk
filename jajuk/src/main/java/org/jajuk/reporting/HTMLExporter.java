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
import org.jajuk.base.Year;
import org.jajuk.util.ITechnicalStrings;

import java.util.ArrayList;

/**
 * This class exports music contents to HTML.
 * 
 * @author Ronak Patel
 * @created Aug 22, 2006
 */
public class HTMLExporter extends Exporter implements ITechnicalStrings {

	/** PUBLIC METHODS */

	/**
	 * @see Exporter.processColllection
	 */
	public String processCollection(int type, ArrayList<Item> collection) {
		String content = null;
		// Get an instance of the XMLExporter.
		XMLExporter xmlExporter = (XMLExporter) ExporterFactory.createExporter("xml");
		// If we are exporting the physical collection...
		if (type == PHYSICAL_COLLECTION) {
			// Create an xml tagging of the collection.
			String xml = xmlExporter.processCollection(PHYSICAL_COLLECTION, null);
			if (xml != null) {
				// content = XMLTransformer.xmlToHTML(xml, COLLECTION_XSLT);
			}
			// Else if we are exporting the logical genre collection...
		} else if (type == LOGICAL_COLLECTION) {
			// Create an xml tagging of the collection.
			String xml = xmlExporter.processCollection(LOGICAL_COLLECTION, null);
			if (xml != null) {
				content = XMLTransformer.xmlToHTML(xml, XSLT_COLLECTION_LOGICAL);
			}
		}
		// Partial report on a given item
		else {
			// Create an xml tagging of this item
			String xml = xmlExporter.processCollection(type, collection);
			if (xml != null) {
				Item first = collection.get(0);
				if (first instanceof Style) {
					content = XMLTransformer.xmlToHTML(xml, XSLT_STYLE);
				} else if (first instanceof Author) {
					content = XMLTransformer.xmlToHTML(xml, XSLT_AUTHOR);
				} else if (first instanceof Year) {
					content = XMLTransformer.xmlToHTML(xml, XSLT_YEAR);
				} else if (first instanceof Album) {
					content = XMLTransformer.xmlToHTML(xml, XSLT_ALBUM);
				} else if (first instanceof Device) {
					content = XMLTransformer.xmlToHTML(xml, XSLT_DEVICE);
				} else if (first instanceof Directory) {
					content = XMLTransformer.xmlToHTML(xml, XSLT_DIRECTORY);
				}
			}
		}
		return content;
	}
}
