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
 * Revision 1.2  2003/10/24 15:44:53  bflorat
 * 24/10/2003
 *
 * Revision 1.1  2003/10/23 22:07:41  bflorat
 * 23/10/2003
 *
 */

package org.jajuk.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.jajuk.base.TypeManager;

/**
 *  Type description
 *
 * @author     bflorat
 * @created    22 oct. 2003
 */
/**
 *  Music oriented file filter ( mp3, ogg.. )
 * <p> Singleton
 *
 * @author     bflorat
 * @created    22 oct. 2003
 */
public class JajukFileFilter extends FileFilter implements java.io.FileFilter{
	/**Self instance*/
	private static JajukFileFilter jff;
	/**Display directories flag**/
	private static boolean bDirectories = true;
	/**Display files flag**/
	private static boolean bFiles = true;

	public static JajukFileFilter getInstance(boolean bDirectories,boolean bFiles){
		JajukFileFilter.bDirectories = bDirectories; 
		JajukFileFilter.bFiles = bFiles; 
		if (jff == null){
			jff = new JajukFileFilter(); 
		}
		return jff;
	}
	
	public static JajukFileFilter getInstance(){
			return getInstance(true,true);
		}
	
	private JajukFileFilter(){
	}

	/**Tells if a file is selected or not**/
	public boolean accept(File f) {
		if ((bFiles && TypeManager.isExtensionSupported(Util.getExtension(f)))
			|| (bDirectories && f.isDirectory())) {
			return true;
		} else {
			return false;
		}
	}
	public String getDescription() {
		return TypeManager.getTypeListString();
	}

}
