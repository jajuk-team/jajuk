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
 * Revision 1.3  2003/10/24 15:44:25  bflorat
 * 24/10/2003
 *
 * Revision 1.2  2003/10/23 22:07:40  bflorat
 * 23/10/2003
 *
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */

package org.jajuk.base;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *  Convenient class to manage Albums
 * @Author    bflorat
 * @created    17 oct. 2003
 */
public class AlbumManager {
	/**Albums collection**/
	static HashMap hmAlbums = new HashMap(100);

	/**
	 * No constructor available, only static access
	 */
	private AlbumManager() {
		super();
	}

	/**
	 * Register an Album
	 *@param sName
	 */
	public static Album registerAlbum(String sName) {
		ArrayList al = new ArrayList(hmAlbums.values());
		Album album = null;
		int index = al.indexOf(new Album("",sName));
			if (index == -1){  //new album
				String sId = new Integer(hmAlbums.size()).toString();
				album = new Album(sId,format(sName));
				hmAlbums.put(new Integer(sId),album);
			}
			else{//album already exists, return it
				album = (Album)al.get(index);
			}
		return album;
	}


	/**Return all registred Albums*/
	public static Collection getAlbums() {
		return hmAlbums.values();
	}

	/**
	 * Return Album by id
	 * @param id
	 * @return
	 */
	public static Album getAlbum(String sId) {
		return (Album) hmAlbums.get(sId);
	}
	
	/**
		 * Format the album name to be normalized : 
		 * <p>-no underscores or other non-ascii characters
		 * <p>-no spaces at the begin and the end
		 * <p>-All in lower cas expect first letter of first word
		 * <p> exemple: "My album title" 
		 *  @param sName
		 * @return
		 */
		private static String format(String sName){
			String sOut;
			sOut = sName.trim(); //supress spaces at the begin and the end
			sOut.replace('-',' ');  //move - to space
			sOut.replace('_',' '); //move _ to space
			char c = sOut.charAt(0);
			sOut = sOut.toLowerCase();
			StringBuffer sb = new StringBuffer(sOut);
			sb.setCharAt(0,Character.toUpperCase(c));
			return sb.toString();
		}
	
}
