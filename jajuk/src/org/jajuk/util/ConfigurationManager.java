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
 * Revision 1.5  2003/10/24 12:46:22  sgringoi
 * Read the jajuk configuration file to initialize the ConfigurationManager
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

import java.util.Properties;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;

/**
 * Manage all the configuration and user preferences of jajuk.
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public class ConfigurationManager implements ITechnicalStrings {
	private static Properties properties = null;
	
	/**
	 * Initialize the jajuk configuration.
	 */
	public static void initJajukConfiguration() {
		if (properties == null) {
			properties = new Properties();
		}
		
		createErrorProperties();
		
		// Read the jajuk configuration file
		String jajukConfFile = "D:/DonneesSeb/workspace/jajuk/tests/sgringoire/properties/jajukConfiguration.xml";
		StringBuffer strXML = null;
		try {
			strXML = Util.readFile(jajukConfFile);

			Contexte ctx = Contexte.creerContexte(strXML.toString());
			
			properties.putAll(ctx.getPropertiesFormat());
		} catch (JajukException e) {
			e.display();
			return;
		}
	}
	
	/**
	 * Return the value of a property, or null if the property is not found.
	 * 
	 * @param pName Name of the property.
	 * @return String Value of the property named pName.
	 */
	public static String getProperty(String pName) {
		if (properties == null) {
			createErrorProperties();
		}

		return (String)properties.getProperty(pName);
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
		
		return res;
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
	
	
	private static void createErrorProperties() {
		if (properties == null) {
			properties = new Properties();
		}

			// Error code
		properties.setProperty("jajuk.error.jajuk0001", Messages.getString("Error.The_first_perspective_is_not_found._7")); //$NON-NLS-2$ //$NON-NLS-1$
		properties.setProperty("jajuk.error.jajuk0002", Messages.getString("Error.Can__t_open_the_view__9")); //$NON-NLS-2$ //$NON-NLS-1$
		properties.setProperty("jajuk.error.jajuk0003", Messages.getString("Error.Can__t_instanciate_the_perspective_named__9")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.setProperty("jajuk.error.jajuk0004", Messages.getString("Can't open the file ")); //$NON-NLS-1$
		properties.setProperty("jajuk.error.jajuk0005", Messages.getString("A problem appear during the connection to the file ")); //$NON-NLS-1$
		properties.setProperty("jajuk.error.jajuk0006", Messages.getString("File not found ")); //$NON-NLS-1$
		properties.setProperty("jajuk.error.jajuk0007", Messages.getString("Can't close the file ")); //$NON-NLS-1$
		properties.setProperty("jajuk.error.jajuk0008", Messages.getString("A problem occurs during the parsing of the file ")); //$NON-NLS-1$
	}
	public static void setProperty(String sName,String sValue){
		properties.setProperty(sName,sValue);
	}

}
