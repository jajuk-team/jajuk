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
 *  $Revision$
 */
package org.jajuk.base;

import java.util.Properties;

/**
 *  Interface for all items using properties ( files, tracks...) 
 *
 * @author     bflorat
 * @created    16 oct. 2003
 */
public interface IPropertyable {

	/**
	 * Get all properties for this item
	 **/
	public Properties getProperties();
	
	public void setProperties(Properties properties) ;
	
	/**
	 * Get a property
	 * @param sKey
	 * @return
	 */
	public String getProperty(String sKey);
	
	/**
	 * Set a property
	 * @param sKey
	 * @param sValue
	 */
	public void setProperty(String sKey, String sValue);
	
	/**
	 * Set a property if it doesn't exist yet
	 * @param sKey
	 * @param sValue
	 */
	public void setDefaultProperty(String sKey, String sValue);
	
	/**
	 * Remove a property
	 * @param sKey
	 */
	public void removeProperty(String sKey);
	
	/**
	 * Display a frame with item properties
	 *
	 */
	public void displayProperties();
	
	
	
	
}
