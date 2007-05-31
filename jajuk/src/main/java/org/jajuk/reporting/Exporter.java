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
 *  $Revision: 2118 $
 */

package org.jajuk.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.jajuk.base.Item;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * The base abstract class for exporting music contents to different formats.
 */
public abstract class Exporter implements ITechnicalStrings {

	/** Public Constants */
	public static final int PHYSICAL_COLLECTION = 0;

	public static final int LOGICAL_COLLECTION = 1;

	/**
	 * This method will export the String sContent to the specified sPath.
	 * 
	 * @param sContent
	 *            The content to export.
	 * @param sPath
	 *            The path of the file to export to. Will create it if it does
	 *            not exist.
	 * @return Returns true if the contents were saved successfully, false
	 *         otherwise.
	 */
	public boolean saveToFile(String sContent, String sPath) {
		boolean result = false;
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					new File(sPath)), "UTF-8"));
			// Writer the contents to the file.
			bw.write(sContent);
			// Close the BufferedWriter
			bw.close();
			// Copy CSS files
			String sCSSAll = new File(sPath).getParent() + '/' + FILE_REPORTING_CSS_ALL_FILENAME;
			String sCSSPrint = new File(sPath).getParent() + '/'
					+ FILE_REPORTING_CSS_PRINT_FILENAME;
			Util.copy(FILE_REPORTING_CSS_ALL_PATH, sCSSAll);
			Util.copy(FILE_REPORTING_CSS_PRINT_PATH, sCSSPrint);
			// The file wrote out successfully.
			result = true;
		} catch (Exception e) {
			Log.error(e);
		}
		return result;
	}

	/**
	 * This method will take a constant specifying what type of collection to
	 * export.
	 * 
	 * @param type
	 *            This XMLExporter constant specifies what type of collection
	 *            we're exporting.
	 * @return Returns a string containing the tagging of the collection, null
	 *         if no tagging was created.
	 */
	abstract public String processCollection(int type);

	/**
	 * This methods will create an html String of items
	 * 
	 * @param collection
	 *            An ArrayList of the items to export
	 * @return Returns a string containing the html markup, or null if an error
	 *         occurred.
	 */
	abstract public String process(ArrayList<Item> collection);
}
