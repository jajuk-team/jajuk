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
 * Revision 1.2  2003/10/10 17:37:21  bflorat
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/07 21:02:21  bflorat
 * Initial commit
 *
 */
package org.jajuk.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class to get strings from localized property files
 *
 * @author     bflorat
 * @created    5 oct. 2003
 */
public class Messages {

	private static final String BUNDLE_NAME = "org.jajuk.i18n.jajuk"; //$NON-NLS-1$
	/**Local ( language) to be used, default is english */
	private static String sLocal;

	private static ResourceBundle rb =
		ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * 
	 */
	private Messages() {
	}
	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		try {
			return rb.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}


	public static void setLocal(String sLocal){
		Messages.sLocal = sLocal;
		rb = ResourceBundle.getBundle(BUNDLE_NAME,new Locale(sLocal));
	}
	

}
