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

package org.jajuk.ui;

import java.util.Collection;
import java.util.HashMap;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComboBoxUI;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.log.Log;

/**
 *  Manages Look and Feel 
 *
 * @author     bflorat
 * @created    16 mai 2003
 */
public class LNFManager implements ITechnicalStrings{
	/** Contains look and feel displayed name and associated class name*/
	private static HashMap hmNameClass = new HashMap(5);
	/**Current Look and feel*/
	private static String sCurrent = ""; //$NON-NLS-1$
	
	/**
	 * Set current look and feel
	 * @param sLaf
	 */
	public static void setLookAndFeel(String sLaf){
		if (sLaf.equals(LNFManager.sCurrent)){
			return;
		}
		sCurrent = sLaf;
		try{
		    String sClassName = (String)hmNameClass.get(sCurrent);
			UIManager.setLookAndFeel(sClassName);	
		}
		catch(Exception e){
			Log.error("123",sCurrent,e); //$NON-NLS-1$
			Messages.showErrorMessage("123",sCurrent);//$NON-NLS-1$
		}
	}
	
	/**Return list of available fool and feels
	 * @return collection
	 **/
	public static Collection getSupportedLNF(){
		return hmNameClass.keySet();
	}
	
	/**
	 * Register look and feel
	 * @param sName
	 * @param sClass
	 */
	public static void register(String sName,String sClass){
		try {
            LookAndFeel laf = (LookAndFeel)Class.forName(sClass).newInstance(); //try to load the look and fell to make sure it exists in this system
            if (laf.isSupportedLookAndFeel()){
                hmNameClass.put(sName,sClass);
                return;
            }
		} catch (Exception e) {
        }
    }
	
	/**
	 * @return Returns the current look and feel.
	 */
	public static String getCurrent() {
		return sCurrent;
	}
	
	/**
	 * 
	 * @return A comboBoxUI used to get a navigator-like history bar 
	 */
	public static ComboBoxUI getSteppedComboBoxClass(){
		try{
			if (getCurrent().equals(LNF_LIQUID)){
				return (ComboBoxUI)Class.forName(LNF_LIQUID_CBUI).newInstance();
			}
			else if(getCurrent().equals(LNF_KUNSTSTOFF)){
				return (ComboBoxUI)Class.forName(LNF_KUNSTSTOFF_CBUI).newInstance();
			}
			else if(getCurrent().equals(LNF_METAL)){
				return (ComboBoxUI)Class.forName(LNF_METAL_CBUI).newInstance();
			}
			else{ //default
			  	return (ComboBoxUI)Class.forName(LNF_METAL_CBUI).newInstance();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
