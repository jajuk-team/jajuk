/*
 *  Jajuk
 *  Copyright (C) 2003 sgringoi
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
 * Revision 1.2  2003/11/14 11:02:18  bflorat
 * - Added user configuration persistence
 *
 * Revision 1.1  2003/10/24 14:56:29  sgringoi
 * Initial commit
 *
 *
 */

package org.jajuk.ui.perspectives;

import org.jajuk.util.ConfigurationManager;

/**
 * Provide statics methods to configure the perspectives management.
 *
 * @author     sgringoi
 * @created    14 oct. 2003
 */
public class PerspectivesConfiguration {

	/**
	 * Return the name of the perspective class corresponding to the name parameter.
	 * 
	 * @param name Name of the perspective.
	 * @return String Perspectve class name corresponding to the name parameter.
	 */
	public static String getPerspectiveClassname(String name) {
		return ConfigurationManager.getProperty("jajuk.preferences.perspectives." + name + ".classname");
	}
	
	/**
	 * Return the list names of available perspectives.
	 * @return String[] List of the available perspectives names.
	 */
	public static String[] getPerspectivesNames() {
		String lst = ConfigurationManager.getProperty("jajuk.preferences.perspectives.list");

		return lst.split(",");
	}
	
	/**
	 * Return the icon's name of a perspective. 
	 * @param name Perspective name.
	 * @return String Icon's name.
	 */
	public static String getPerspectiveIconName(String name) {
		return ConfigurationManager.getProperty("jajuk.preferences.perspectives." + name + ".icon");
	}

	/**
	 * Return the name of the view class corresponding to the name parameter.
	 * 
	 * @param name Name of the view.
	 * @return String View class name corresponding to the name parameter.
	 */
	public static String getViewClassname(String name) {
		return ConfigurationManager.getProperty("jajuk.preferences.views." + name + ".classname");
	}

	/**
	 * Return the list names of available views of a perspective.
	 * @param name Perspective's name.
	 * @return String[] List of the available views names.
	 */
	public static String[] getViewsNames(String name) {
		String lst = ConfigurationManager.getProperty("jajuk.preferences.perspectives." + name + ".views.list");
		return lst.split(",");
	}
	
}
