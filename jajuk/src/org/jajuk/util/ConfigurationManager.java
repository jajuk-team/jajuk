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
 * Revision 1.4  2003/10/21 20:43:06  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 * Revision 1.3  2003/10/17 20:43:56  bflorat
 * 17/10/2003
 *
 * Revision 1.2  2003/10/10 15:23:08  sgringoi
 * Ajout des propriétés d'erreur
 *
 */
package org.jajuk.util;

import java.util.Properties;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;

/**
 * Manage all the configuration and user preferences of jajuk.
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public class ConfigurationManager implements ITechnicalStrings{
	
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
	
	
	private static void createProperties() {
		properties = new Properties();
		
		// Default parameters
		properties.put(CONF_VIEW_PHYSICAL, "org.jajuk.ui.views.PhysicalTreeView");//,org.jajuk.ui.views.TrackListView"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// User preferences
		properties.put(CONF_PERSPECTIVE_DEFAULT, "org.jajuk.ui.perspectives.PhysicalPerspective"); //$NON-NLS-1$ //$NON-NLS-2$
	
		//Modes
		properties.put(CONF_STATE_REPEAT,"false"); //$NON-NLS-1$
		properties.put(CONF_ICON_REPEAT,ICON_REPEAT_OFF);  
		properties.put(CONF_STATE_SHUFFLE,"false"); //$NON-NLS-1$
		properties.put(CONF_ICON_SHUFFLE,ICON_SHUFFLE_OFF);  
		properties.put(CONF_STATE_CONTINUE,"true"); //$NON-NLS-1$
		properties.put(CONF_ICON_CONTINUE,ICON_CONTINUE_ON);  
		properties.put(CONF_STATE_INTRO,"false"); //$NON-NLS-1$
		properties.put(CONF_ICON_INTRO,ICON_INTRO_OFF);  
		//TODO get it from xml property file
		
	}
	
	
	public static void setProperty(String sName,String sValue){
		properties.setProperty(sName,sValue);
	}
}