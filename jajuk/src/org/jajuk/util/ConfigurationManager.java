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
 * Revision 1.2  2003/10/10 15:23:08  sgringoi
 * Ajout des propriétés d'erreur
 *
 */
package org.jajuk.util;

import java.util.Properties;

import org.jajuk.i18n.Messages;

/**
 * Manage all the configuration and user preferences of jajuk.
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public class ConfigurationManager {
	private static Properties properties = null;
	
	/**
	 * Return the value of a property, or null if the property is not found.
	 * 
	 * @param pName Name of the property.
	 * @return String Value of the property named pName.
	 */
	public static String getProperty(String pName) {
		if (properties == null) {
			createProperties();
		}
		
		return properties.getProperty(pName);
	}

	/**
	 * Return the value of a property, or a default value if the property is not found.
	 * 
	 * @param pName Name of the property.
	 * @param pDefault Default value returned if the property is not found.
	 * @return String Value of the property named pName.
	 */
	public static String getProperty(String pName, String pDefault) {
		String res = getProperty(pName);
		if (res == null) {
			res = pDefault;
		}
		
		return pDefault;
	}
	
	/**
	 * Return the message display to the user corresponding to the error code.
	 * 
	 * @param pCode Error code.
	 * @return String Message corresponding to the error code.
	 */
	public static String getErrorMessage(String pCode) {
		return getProperty("jajuk.error." + pCode); //$NON-NLS-1$
	}
	private static void createProperties() {
		properties = new Properties();
		
			// Default parameters
		properties.put("jajuk.preference.perspective.physical.views", "org.jajuk.ui.views.PhysicalTreeView");//,org.jajuk.ui.views.TrackListView"); //$NON-NLS-1$ //$NON-NLS-2$
		
			// User preferences
		properties.put("jajuk.preference.perspective.default", "org.jajuk.ui.perspectives.PhysicalPerspective"); //$NON-NLS-1$ //$NON-NLS-2$
		
			// Error code
		properties.put("jajuk.error.jajuk0001", Messages.getString("Error.The_first_perspective_is_not_found._7")); //$NON-NLS-2$ //$NON-NLS-1$
		properties.put("jajuk.error.jajuk0002", Messages.getString("Error.Can__t_open_the_view__9")); //$NON-NLS-2$ //$NON-NLS-1$
		properties.put("jajuk.error.jajuk0003", Messages.getString("Error.Can__t_instanciate_the_perspective_named__9")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
