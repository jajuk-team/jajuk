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
 * $Revision$
 */

package org.jajuk.ui;

import java.util.Collection;
import java.util.HashMap;

import javax.swing.LookAndFeel;

import org.jajuk.base.ITechnicalStrings;

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
	private static String sCurrent = LNF_METAL;
	
	/**
	 * Set current look and feel
	 * @param sLaf
	 */
	public static void setLookAndFeel(String sLaf){
		if (sLaf.equals(sCurrent)){
			//TODO set look and feel for sLaf	
			sCurrent = sLaf;
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
		hmNameClass.put(sName,sClass);
	}
	
}
