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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.filechooser.FileFilter;

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;

/**
 *  Music oriented file filter ( mp3, ogg.. )
 * @author     bflorat
 * @created    22 oct. 2003
 */

public class JajukFileFilter extends FileFilter implements java.io.FileFilter{
	/**Display directories flag**/
	private boolean bDirectories = true;
	/**Display files flag**/
	private boolean bFiles = true;
	/**Accepted types**/
	private ArrayList alTypes;
	
	/**
	 * Constructor
	 * @param bDirectories can we show directories
	 * @param bFiles can we show files
	 * @param types which type do we show
	 */	
	public JajukFileFilter(boolean bDirectories,ArrayList alTypes){
		this.bDirectories = bDirectories; 
		this.alTypes = alTypes;
	}

	/**
	 * Constructor, no type specified
	 * @param bDirectories
	 * @param bFiles
	 */
	public  JajukFileFilter(boolean bDirectories,boolean bFiles){
		this.bDirectories = bDirectories;
		this.bFiles = bFiles;
	}

	/**
	 * Default Constructor, true for files, true for directories
	 *
	 */
	public JajukFileFilter(){
		this(true,true);
	}

	/**Tells if a file is selected or not**/
	public boolean accept(File f) {
		//directories case
		if ( bDirectories && f.isDirectory()) {				
			return true;
		}
		//file cases
		if ( alTypes != null){ //one or more types is specified
			Iterator it = alTypes.iterator();
			while (it.hasNext()){
				Type type = (Type)it.next();
				if ( Util.getExtension(f).equals(type.getExtension())){
					return true;
				}
			}
		}
		else{
			if ((bFiles && TypeManager.isExtensionSupported(Util.getExtension(f)))){
				return true;
			}
		}
		return false;
	}
	
	
	public String getDescription() {
		String sOut = ""; //$NON-NLS-1$
		if ( !bFiles ){ //only dirs
			return sOut;
		}
		if ( alTypes == null){ //if no type specified, we considere all Jajuk known files
			sOut+=TypeManager.getTypeListString();
		}
		else{
			Iterator it = alTypes.iterator();
			while(it.hasNext()){
				Type type = (Type)it.next();
				sOut += type.getExtension()+',';
			}
			sOut = sOut.substring(0,sOut.length()-1);
		}
		return sOut;
	}
}
