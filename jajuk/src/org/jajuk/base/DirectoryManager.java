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
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */

package org.jajuk.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *  Convenient class to manage directories
 * @Author    bflorat
 * @created    17 oct. 2003
 */
public class DirectoryManager {
	/**Albums collection**/
	static HashMap hmDirectories = new HashMap(100);

	/**
	 * No constructor available, only static access
	 */
	private DirectoryManager() {
		super();
	}

	/**
	 * Register a directory
	 *@param sName
	 */
	public static void registerDirectory(String sName,Directory dParent,Device device) {
		String sId = new Integer(hmDirectories.size()).toString();
		Directory directory = new Directory(sId,sName,dParent,device);
		hmDirectories.put(sId,directory);
	}


	/**Return all registred directories*/
	public static Collection getDirectories() {
		return hmDirectories.values();
	}

	/**
	 * Return directory by id
	 * @param sName
	 * @return
	 */
	public static Directory getDirectory(String sId) {
		return (Directory) hmDirectories.get(sId);
	}
	
}
