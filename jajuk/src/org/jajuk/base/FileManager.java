/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Log$
 * Revision 1.3  2003/10/23 22:07:40  bflorat
 * 23/10/2003
 *
 * Revision 1.2  2003/10/21 20:37:54  bflorat
 * 21/10/2003
 *
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */

package org.jajuk.base;

import java.util.Collection;
import java.util.HashMap;

/**
 *  Convenient class to manage files
 * @Author    bflorat
 * @created    17 oct. 2003
 */
public class FileManager {
	/**Files collection**/
	static HashMap hmFiles = new HashMap(100);

	/**
	 * No constructor available, only static access
	 */
	private FileManager() {
		super();
	}

	/**
	 * Register an File
	 *@param sName
	 */
	public static File registerFile(String sName,Directory directory,Track track,long lSize,String sQuality) {
		String sId = new Integer(hmFiles.size()).toString();
		File file = new File(sId,sName,directory,track,lSize,sQuality);
		hmFiles.put(new Integer(sId),file);
		return file;
	}


	/**Return all registred files*/
	public static Collection getFiles() {
		return hmFiles.values();
	}

	/**
	 * Return file by id
	 * @param id
	 * @return
	 */
	public static File getFile(String sId) {
		return (File) hmFiles.get(sId);
	}
	
	
	
}
