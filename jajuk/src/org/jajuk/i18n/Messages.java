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
package org.jajuk.i18n;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
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
	/**Supported Locals*/
	public static ArrayList alLocals = new ArrayList(10);
	/**Locals description */
	public static ArrayList alDescs = new ArrayList(10);
	/**Used ressource bundle*/
	private static ResourceBundle rb = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Private Constructor
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
			sOut = rb.getString(key); 
		}
		catch(Exception e){
			Log.error("105","key: "+key,e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return sOut;
	}
	
	/**
	 * Register a local
	 * @param sName : local name like "english"
	 * @param sLocal : local name like "en"
	 */
	public static void registerLocal(String sLocal ,String sDesc){
		alLocals.add(sLocal);
		alDescs.add(sDesc);
	}
	
	/**
	 * Return list of available locals
	 * @return
	 */
	public static ArrayList getLocals(){
		return alLocals;
	}
	
	/**
	 * Return list of available locals
	 * @return
	 */
	public static ArrayList getDescs(){
		return alDescs;
	}
	
	/**
	 * Change current local
	 * @param sLocal
	 */
	public static void setLocal(String sLocal){
		Messages.sLocal = sLocal;
		if ( sLocal.equals("en")){ //take english as an exception because it uses the base properties file //$NON-NLS-1$
			rb = ResourceBundle.getBundle(BUNDLE_NAME,new Locale(""));	 //$NON-NLS-1$
		}
		else{
			rb = ResourceBundle.getBundle(BUNDLE_NAME,new Locale(sLocal));
		}
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
			Log.error("105","code: "+pCode,e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return sOut;
	}
	
	
	/**
	 * Show a dialog with specified error message
	 * @param sCode
	 */
	public static void showErrorMessage(String sCode){
		JOptionPane.showMessageDialog(Main.jframe,Messages.getErrorMessage(sCode),Messages.getErrorMessage("102"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}
	
	/**
	 * Show a dialog with specified error message
	 * @param sMessage
	 */
	public static void showInfoMessage(String sMessage){
		JOptionPane.showMessageDialog(Main.jframe,sMessage,Messages.getString("Info"),JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}
	
	
	/**
	 * Show a dialog with specified warning message
	 * @param sMessage
	 */
	public static void showWarningMessage(String sMessage){
		JOptionPane.showMessageDialog(Main.jframe,sMessage,Messages.getString("Warning"),JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
	}
	
	/**
	 * Show a dialog with specified error message and an icon
	 * @param sMessage
	 */
	public static void showInfoMessage(String sMessage,Icon icon){
		JOptionPane.showMessageDialog(Main.jframe,sMessage,Messages.getString("Info"),JOptionPane.INFORMATION_MESSAGE,icon); //$NON-NLS-1$
	}
	
	/**
	 * Show a dialog with specified error message and infosup
	 * @param sCode
	 * @param sInfoSup
	 */
	public static void showErrorMessage(String sCode,String sInfoSup){
		JOptionPane.showMessageDialog(Main.jframe,Messages.getErrorMessage(sCode)+" : "+sInfoSup,Messages.getErrorMessage("102"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Show a dialog with specified error message with infos up
	 * @param sMessage
	 * @param sInfoSup
	 */
	public static void showInfoMessage(String sMessage,String sInfoSup){
		JOptionPane.showMessageDialog(Main.jframe,Messages.getString(sMessage)+" : "+sInfoSup,Messages.getString("Info"),JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	/**
	 * @return Returns the sLocal.
	 */
	public static String getLocal() {
		return sLocal;
	}

}
