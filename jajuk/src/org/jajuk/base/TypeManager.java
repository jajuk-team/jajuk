/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 *  Manages types ( mp3, ogg...) supported by jajuk
 * <p> static class
 *
 * @author     Bertrand Florat
 * @created    12 oct. 2003
 */
public class TypeManager implements ITechnicalStrings{

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
	public static synchronized Type registerType(String sName,String sExtension, String sPlayerImpl,String sTagImpl) {
		String sId = Integer.toString(hmSupportedTypes.size());
		return registerType(sId,sName,sExtension,sPlayerImpl,sTagImpl);
	}
	
	
	/**
	 * Register a type jajuk can read with a known id
	 * @param type
	 */
	public static synchronized Type registerType(String sId,String sName,String sExtension, String sPlayerImpl,String sTagImpl) {
		if ( hmSupportedTypes.containsKey(sExtension)){ //if the type is already in memory, use it
			return (Type)hmSupportedTypes.get(sExtension);
		}	
		Type type = null;
		try{
			type = new Type(sId,sName,sExtension,sPlayerImpl,sTagImpl);
			hmSupportedTypes.put(type.getExtension(), type);
		}
		catch(Exception e){
			Log.error("109","sPlayerImpl="+sPlayerImpl+" sTagImpl="+sTagImpl,e ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	 * Return type for a given technical description
	 * @param sTechDesc
	 * @return associated type or null if none found
	 */
	public static synchronized Type getTypeByTechDesc(String sTechDesc) {
		Iterator it = hmSupportedTypes.values().iterator();
		while (it.hasNext()){
			Type type = (Type)it.next();
			if (type.getProperty(TYPE_PROPERTY_TECH_DESC).equalsIgnoreCase(sTechDesc)){
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Return all music types
	 * @return
	 */
	public static synchronized ArrayList getAllMusicTypes() {
		ArrayList alResu = new ArrayList(5);	
		Iterator it = hmSupportedTypes.values().iterator();
		while (it.hasNext()){
			Type type = (Type)it.next();
			if (type.getProperty(TYPE_PROPERTY_IS_MUSIC).equals(TRUE)){
				alResu.add(type);
			}
		}
		return alResu;
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
