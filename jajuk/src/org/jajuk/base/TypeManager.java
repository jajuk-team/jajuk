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
 * Revision 1.3  2003/10/26 21:28:49  bflorat
 * 26/10/2003
 *
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *  Manages types ( mp3, ogg...) supported by jajuk
 * <p> static class
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class TypeManager {

	static HashMap hmSupportedTypes = new HashMap(5);

	/**
	 * No constructor available, only static access
	 */
	private TypeManager() {
	}

	/**
	 * Register a type jajuk can read
	 * @param type
	 */
	public static Type registerType(String sName,String sExtension, String sPlayerImpl,String sTagImpl,boolean bIsMusic)throws Exception {
		String sId = new Integer(hmSupportedTypes.size()).toString();
		Type type = new Type(sId,sName,sExtension,sPlayerImpl,sTagImpl,bIsMusic);
		hmSupportedTypes.put(type.getExtension(), type);
		return type;
	}

	/**
	 * Tells if the type is supported
	 * @param type
	 * @return
	 */
	public static boolean isExtensionSupported(String sExt) {
		return hmSupportedTypes.containsKey(sExt);
	}

	/**Return all registred types*/
	public static ArrayList getTypes() {
		return new ArrayList(hmSupportedTypes.values());
	}

	/**
	 * Return type for a given extension
	 * @param sExtension
	 * @return
	 */
	public static Type getTypeByExtension(String sExtension) {
		return (Type) hmSupportedTypes.get(sExtension);
	}

	/**
	 * Return a list "a,b,c" of registered extensions, used by FileChooser
	 * @return
	 */
	public static String getTypeListString() {
		StringBuffer sb = new StringBuffer();
		Iterator it = hmSupportedTypes.keySet().iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			sb.append(',');
		}
		sb.deleteCharAt(sb.length() - 1); //remove last ','
		return sb.toString();
	}

}
