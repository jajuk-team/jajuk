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
 * Revision 1.2  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */

package org.jajuk.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *  Convenient class to manage styles
 * @author     bflorat
 * @created    17 oct. 2003
 */
public class StyleManager {
	/**Styles collection**/
	static HashMap hmStyles = new HashMap(10);

	/**
	 * No constructor available, only static access
	 */
	private StyleManager() {
		super();
	}

	/**
	 * Register a style
	 *@param sName
	 */
	public static void registerStyle(String sName) {
		String sId = new Integer(hmStyles.size()).toString();
		Style style = new Style(sId, sName);
		hmStyles.put(sId, style);
	}

	/**
			 * Format the Style name to be normalized : 
			 * <p>-no underscores or other non-ascii characters
			 * <p>-no spaces at the begin and the end
			 * <p>-All in upper case 
			 * <p> exemple: "ROCK" 
			 *  @param sName
			 * @return
			 */
	private static String format(String sName) {
		String sOut;
		sOut = sName.trim(); //supress spaces at the begin and the end
		sOut.replace('-', ' '); //move - to space
		sOut.replace('_', ' '); //move _ to space
		sOut = sOut.toUpperCase();
		return sOut;
	}

	/**Return all registred styles*/
	public static Collection getStyles() {
		return hmStyles.values();
	}

	/**
	 * Return style by id
	 * @param sId
	 * @return
	 */
	public static Style getStyle(String sId) {
		return (Style) hmStyles.get(sId);
	}

}