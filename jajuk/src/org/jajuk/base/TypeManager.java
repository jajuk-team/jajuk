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
 * Revision 1.5  2003/11/03 06:08:05  bflorat
 * 03/11/2003
 *
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.log.Log;

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
	public static synchronized Type registerType(String sName,String sExtension, String sPlayerImpl,String sTagImpl,boolean bIsMusic) {
		String sId = new Integer(hmSupportedTypes.size()).toString();
		return registerType(sId,sName,sExtension,sPlayerImpl,sTagImpl,bIsMusic);
	}
	
	
	/**
	 * Register a type jajuk can read with a known id
	 * @param type
	 */
	public static synchronized Type registerType(String sId,String sName,String sExtension, String sPlayerImpl,String sTagImpl,boolean bIsMusic) {
		Type type = null;
		try{
			type = new Type(sId,sName,sExtension,sPlayerImpl,sTagImpl,bIsMusic);
			hmSupportedTypes.put(type.getExtension(), type);
		}
		catch(Exception e){
			Log.error("109","sPlayerImpl="+sPlayerImpl+" sTagImpl="+sTagImpl,e );
		}
		return type;
	}


	/**
	 * Tells if the type is supported
	 * @param type
	 * @return
	 */
	public static synchronized boolean isExtensionSupported(String sExt) {
		return hmSupportedTypes.containsKey(sExt);
	}

	/**Return all registred types*/
	public static synchronized ArrayList getTypes() {
		return new ArrayList(hmSupportedTypes.values());
	}
	
	/**Return a registred type by its id*/
		public static synchronized Type getType(String sId) {
			Iterator it = hmSupportedTypes.values().iterator();
			while (it.hasNext()){
				Type type = (Type)it.next();
				if (type.getId().equals(sId)){
					return type;
				}
			}
			return null;
		}

	/**
	 * Return type for a given extension
	 * @param sExtension
	 * @return
	 */
	public static synchronized Type getTypeByExtension(String sExtension) {
		return (Type) hmSupportedTypes.get(sExtension);
	}

	/**
	 * Return a list "a,b,c" of registered extensions, used by FileChooser
	 * @return
	 */
	public static synchronized String getTypeListString() {
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
