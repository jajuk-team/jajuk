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
 * Revision 1.4  2003/10/31 13:05:06  bflorat
 * 31/10/2003
 *
 * Revision 1.3  2003/10/26 21:28:49  bflorat
 * 26/10/2003
 *
 * Revision 1.2  2003/10/23 22:07:40  bflorat
 * 23/10/2003
 *
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */

package org.jajuk.base;

import java.util.ArrayList;
import java.util.Collection;

import org.jajuk.util.MD5Processor;

/**
 *  Convenient class to manage directories
 * @Author    bflorat
 * @created    17 oct. 2003
 */
public class DirectoryManager {
	/**Directories collection stored in a arraylist to conserve creation order**/
	static ArrayList alDirectories = new ArrayList(100);
	/**Directories collection ID */
	static ArrayList alIds = new ArrayList(100);

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
	public static Directory registerDirectory(String sName,Directory dParent,Device device) {
		String sAbs = device.getUrl();
		if (dParent != null){
			sAbs = dParent.getAbsolutePath();
		}
		sAbs += sName;
		String sId = MD5Processor.hash(sAbs);
		return registerDirectory(sId,sName,dParent,device);
	}
	
	/**
		 * Register a root device directory
		 *@param device
		 */
		public static Directory registerDirectory(Device device) {
			String sId = device.getId();
			return registerDirectory(sId,"",null,device);
		}
	
	/**
		 * Register a directory with a known id
		 *@param sName
		 */
		public static Directory registerDirectory(String sId,String sName,Directory dParent,Device device) {
			Directory directory = new Directory(sId,sName,dParent,device);
			alDirectories.add(directory);
			alIds.add(sId);
			return directory;
		}


	/**Return all registred directories*/
	public static ArrayList getDirectories() {
		return alDirectories;
	}

	/**
	 * Return directory by id
	 * @param sName
	 * @return
	 */
	public static Directory getDirectory(String sId) {
		return (Directory) alDirectories.get(alIds.indexOf(sId));
	}
	
}
