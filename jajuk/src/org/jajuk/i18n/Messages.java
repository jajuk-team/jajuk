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
 * Revision 1.6  2003/11/11 20:35:43  bflorat
 * 11/11/2003
 *
 * Revision 1.5  2003/10/26 21:28:49  bflorat
 * 26/10/2003
 *
 * Revision 1.4  2003/10/23 22:07:40  bflorat
 * 23/10/2003
 *
 * Revision 1.3  2003/10/17 20:37:18  bflorat
 * 17/10/2003
 *
 * Revision 1.2  2003/10/10 17:37:21  bflorat
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/07 21:02:21  bflorat
 * Initial commit
 *
 */
package org.jajuk.i18n;

import java.awt.Label;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.util.log.Log;

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
		String sOut = key;
			try{
				sOut = rb.getString(key); //$NON-NLS-1$
			}
			catch(Exception e){
				Log.error("105","key: "+key,e);
			}
			return sOut;
	}


	public static void setLocal(String sLocal){
		Messages.sLocal = sLocal;
		rb = ResourceBundle.getBundle(BUNDLE_NAME,new Locale(sLocal));
	}
	
	/**
		 * Return the message display to the user corresponding to the error code.
		 * 
		 * @param pCode Error code.
		 * @return String Message corresponding to the error code.
		 */
		public static String getErrorMessage(String pCode) {
			String sOut = pCode;
			try{
				sOut = rb.getString("Error." + pCode); //$NON-NLS-1$
			}
			catch(Exception e){
				Log.error("105","code: "+pCode,e);
			}
			return sOut;
		}
		
		
		/**
		 * Show a dialog with specified error message
		 * @param sCode
		 */
		public static void showErrorMessage(String sCode){
			JOptionPane.showMessageDialog(Main.jframe,Messages.getErrorMessage(sCode),Messages.getErrorMessage("102"),JOptionPane.ERROR_MESSAGE);
		}
		
		/**
		 * Show a dialog with specified error message
		 * @param sMessage
		 */
		public static void showInfoMessage(String sMessage){
			JOptionPane.showMessageDialog(Main.jframe,Messages.getString(sMessage),Messages.getString("Info"),JOptionPane.INFORMATION_MESSAGE);
		}
	

}
