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
package org.jajuk.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	private static Properties properties = new Properties();
	
	/**Self instance**/
	static private ConfigurationManager cm;
	
	
	/**Singleton accessor */
	public static synchronized ConfigurationManager getInstance(){
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
		setDefaultProperties();
		properties = (Properties)properties.clone();
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
	 * Return the value of a property as a boolean or false if the property is not found
	 * 
	 * @param pName Name of the property.
	 * @return boolean value of the property named pName.
	 */
	public static boolean getBoolean(String pName) {
		return Boolean.valueOf(properties.getProperty(pName)).booleanValue();
	}
	
	/**
	 * Return the value of a property as a boolean or specified value of the property is not found
	 * 
	 * @param pName Name of the property.
	 * @param default value
	 * @return boolean value of the property named pName.
	 */
	public static boolean getBoolean(String pName,boolean bDefault) {
		String s = properties.getProperty(pName);
		if ( s != null){
			return Boolean.valueOf(s).booleanValue();
		}
		else{
			return bDefault;
		}
	}

	
	/**
	 * Return the value of a property as a float or 0f if the property is not found
	 * 
	 * @param pName Name of the property.
	 * @return float value of the property named pName.
	 */
	public static float getFloat(String pName) {
		return Float.valueOf(properties.getProperty(pName)).floatValue();
	}
	
	/**
	 * Return the value of a property as an integer or 0 if the property is not found
	 * 
	 * @param pName Name of the property.
	 * @return int value of the property named pName.
	 */
	public static int getInt(String pName) {
		return Integer.valueOf(properties.getProperty(pName)).intValue();
	}
	
	
	/**
	 * Set default values
	 *
	 */
	public static void setDefaultProperties() {
		// User preferences
		properties.put(CONF_PERSPECTIVE_DEFAULT,PERSPECTIVE_NAME_CONFIGURATION); 
		properties.put(CONF_STATE_REPEAT,FALSE); 
		properties.put(CONF_STATE_SHUFFLE,FALSE); 
		properties.put(CONF_STATE_CONTINUE,TRUE);
		properties.put(CONF_STATE_INTRO,FALSE); 
		properties.put(CONF_STARTUP_FILE,""); //no startup file by default 
		properties.put(CONF_STARTUP_MODE,STARTUP_MODE_LAST);
		properties.put(CONF_CONFIRMATIONS_DELETE_FILE,TRUE);
		properties.put(CONF_CONFIRMATIONS_EXIT,FALSE);
		properties.put(CONF_OPTIONS_HIDE_UNMOUNTED,FALSE);
		properties.put(CONF_OPTIONS_RESTART,TRUE);
		properties.put(CONF_OPTIONS_LOG_LEVEL,Integer.toString(Log.WARNING));
		//set default language without properties file available (normaly only at install)
		String sLanguage = System.getProperty("user.language"); //$NON-NLS-1$
		if (Messages.getInstance().getLocals().contains(sLanguage)){ //user language exists in jajuk, take it as default
			properties.put(CONF_OPTIONS_LANGUAGE,sLanguage);
		}
		else{ //user language is unknown, take english as a default, user will be able to change it later anyway
			properties.put(CONF_OPTIONS_LANGUAGE,"en"); //$NON-NLS-1$
		}
		properties.put(CONF_OPTIONS_INTRO_BEGIN,"0"); //$NON-NLS-1$
		properties.put(CONF_OPTIONS_INTRO_LENGTH,"20"); //$NON-NLS-1$
		properties.put(CONF_OPTIONS_LNF,LNF_LIQUID);
		properties.put(CONF_OPTIONS_P2P_SHARE,FALSE);
		properties.put(CONF_OPTIONS_P2P_ADD_REMOTE_PROPERTIES,FALSE);
		properties.put(CONF_OPTIONS_P2P_HIDE_LOCAL_PROPERTIES,TRUE);
		properties.put(CONF_OPTIONS_P2P_PASSWORD,""); //$NON-NLS-1$
		properties.put(CONF_HISTORY,"-1"); //$NON-NLS-1$
		properties.put(CONF_FIRST_CON,TRUE);
		properties.put(CONF_TAGS_DEEP_SCAN,FALSE);
		properties.put(CONF_TAGS_USE_PARENT_DIR,TRUE);
		properties.put(CONF_BOOKMARKS,"");
		properties.put(CONF_SHOW_AT_STARTUP,TRUE);
		properties.put(CONF_BESTOF_SIZE,"20"); //$NON-NLS-1$
		properties.put(CONF_VOLUME,"0.5"); //$NON-NLS-1$
		properties.put(CONF_REGEXP,FALSE); //$NON-NLS-1$
		properties.put(CONF_BACKUP_SIZE,"30");//$NON-NLS-1$
		properties.put(CONF_COLLECTION_CHARSET,"UTF-8");//$NON-NLS-1$
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
			properties.store(new FileOutputStream(FILE_CONFIGURATION),"User configuration"); //$NON-NLS-1$
		} catch (IOException e) {
			Log.error("113",e); //$NON-NLS-1$
			Messages.showErrorMessage("113"); //$NON-NLS-1$
		}
		
	}
	
	/** Load properties from in file */
	public static void load() {
		try {
			properties.load(new FileInputStream(FILE_CONFIGURATION));
		} catch (IOException e) {
			Log.error("114", e); //$NON-NLS-1$
			Messages.showErrorMessage("114"); //$NON-NLS-1$
		}

	}
	
}