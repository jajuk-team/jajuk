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
 * Revision 1.9  2003/11/14 11:02:18  bflorat
 * - Added user configuration persistence
 *
 * Revision 1.8  2003/11/13 18:56:56  bflorat
 * 13/11/2003
 *
 * Revision 1.6  2003/10/26 21:28:49  bflorat
 * 26/10/2003
 *
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.log.Log;

/**
 * Manage all the configuration and user preferences of jajuk.
 * <p> Singleton
 * @author		bflorat
 * @version	1.0
 * @created		14 nov. 2003
 */
public class ConfigurationManager implements ITechnicalStrings{
	
	/**Properties in memory */
	private static Properties properties = null;
	
	/**Self instance**/
	static private ConfigurationManager cm;
	
	
	/**Singleton accessor */
	public static ConfigurationManager getInstance(){
		if (cm == null){
			cm = new ConfigurationManager();
		}
		return cm;
	}
	
	/**
	 * Constructor
	 *
	 */
	private ConfigurationManager(){
		properties = new Properties();
		setDefaultProperties();
	}
	
	
	/**
	 * Return the value of a property, or null if the property is not found.
	 * 
	 * @param pName Name of the property.
	 * @return String Value of the property named pName.
	 */
	public static String getProperty(String pName) {
		return properties.getProperty(pName);
	}


	/**
	 * Set default values
	 *
	 */
	private static void setDefaultProperties() {
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
		
	}
	
	
	/**
	 * Set a property
	 * @param sName
	 * @param sValue
	 */
	public static void setProperty(String sName,String sValue){
		properties.setProperty(sName,sValue);
	}
	
	/** Commit properties in a file */
	public static void commit(){
		try {
			properties.store(new FileOutputStream(FILE_CONFIGURATION),"User configuration");
		} catch (IOException e) {
			Log.error("113",e);
			Messages.showErrorMessage("113");
		}
		
	}
	
	/** Load properties from in file */
	public static void load() {
		try {
			properties.load(new FileInputStream(FILE_CONFIGURATION));
		} catch (IOException e) {
			Log.error("114", e);
			Messages.showErrorMessage("114");
		}

	}
	
	
	
}