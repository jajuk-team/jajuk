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
 * Revision 1.1  2003/10/12 21:08:12  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.util;

import java.io.File;
import java.util.StringTokenizer;

/**
 *  General use utilities methods  
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class Util {
	
	/**
	 * No constructor
	 */
	private Util(){
	}
	
	/**
	 * Get a file extension
	 * @param file
	 * @return
	 */
	public static String getExtension(File file){
		String s = file.getName();
		StringTokenizer st = new StringTokenizer(s, "."); //$NON-NLS-1$
		String sExt = ""; //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			sExt = st.nextToken();
		}
		return sExt;	 
	}

	
}
