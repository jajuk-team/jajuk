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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.jajuk.util.log.Log;

/**
 * The base abstract class for exporting music contents to different formats.
 * 
 * @author Ronak Patel
 * @created Aug 20, 2006
 */
public abstract class Exporter {

	/**
	 * Default Exporter Constructor
	 * 
	 */
	public Exporter() {
	}

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
			FileWriter fw = new FileWriter(sPath);
			BufferedWriter bw = new BufferedWriter(fw);
			// Writer the contents to the file.
			bw.write(sContent);
			// Close the BufferedWriter
			bw.close();
			// The file wrote out successfully.
			result = true;
		} catch (IOException e) {
			Log.error(e);
		}

		return result;
	}
}
