/*
 *  Jajuk Copyright (C) 2006 Ronak Patel
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
 *  $Revision$
 */

package org.jajuk.base.exporters;

import javax.swing.filechooser.FileFilter;

/**
 * ExportFileFilter
 * @author 		Ronak Patel
 * @created		26 dec. 2005
*/
public class ExportFileFilter extends FileFilter {
	private String sFilterType;
	
	/** 
	 * Constructor
	 * @param Takes a String that represents the file to be filtered. Example: ".htm" or ".pdf"
	 */
	public ExportFileFilter(String filtertype) throws NullPointerException, IllegalArgumentException {
		if (filtertype != null) {
			if (filtertype.length() != 0) {
				if (filtertype.charAt(0) != '.') {
					filtertype = "." + filtertype;
				}
				sFilterType = filtertype;
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new NullPointerException();
		}		
	}
	
	/**
	 * @return Returns true if file is accepted by filter, false otherwise.
	 */
	public boolean accept(java.io.File f) {
		if (f.isDirectory()) {
			return true;
		}
		
		String filename = f.getName().toLowerCase();
		if (filename != null) {
			if (filename.endsWith(sFilterType)) {
				return true;
			}	
		} 
		
		return false;
	}
	
	/**
	 * @return Returns description of filter.
	 */
	public String getDescription() {
		return sFilterType + " File";
	}
	
	/**
	 * 
	 * @return Returns the file extension.
	 */
	public String getExtension() {
		return sFilterType.substring(1);
	}
}