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
 * Revision 1.1  2003/10/17 20:36:45  bflorat
 * 17/10/2003
 *
 */

package org.jajuk.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *  Convenient class to manage styles
 * <p> Singleton
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
		Style style = new Style(hmStyles.size(),sName);
		hmStyles.put(sName,style);
	}


	/**Return all registred styles*/
	public static Collection getStyles() {
		return hmStyles.values();
	}

	/**
	 * Return style by name
	 * @param sName
	 * @return
	 */
	public static Style getStyle(String sName) {
		return (Style) hmStyles.get(sName);
	}

	
}
