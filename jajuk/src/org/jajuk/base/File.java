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
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 *  A music file to be played
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class File {
	private Type type;
	private String sPath;
	private Properties properties;

	public File(String sPath, Type type) {
		this.type = type;
		this.sPath = sPath;
	}

	/**
	 * @return
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
		 * @return
		 */
	public String getProperty(String sKey) {
		return properties.get(sKey).toString();
	}

	/**
	 * @return
	 */
	public String getPath() {
		return sPath;
	}

	/**
	 * @return
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param map
	 */
	public void setProperty(String sKey, String sValue) {
		properties.put(sKey,sValue);
	}

	public String toString(){
		return "[Path="+sPath+" ; Type="+type+"]";	
	}
}
