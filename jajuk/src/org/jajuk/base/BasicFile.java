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
 * Revision 1.1  2003/10/21 20:37:54  bflorat
 * 21/10/2003
 *
 */

package org.jajuk.base;

import org.jajuk.util.Util;

/**
 *  File not managed in collection ( selected by file/open or in a playlist file )
 *
 * @author     bflorat
 * @created    21 oct. 2003
 */
public class BasicFile extends org.jajuk.base.File {

	/**Physical file*/
	java.io.File fio;
	
	public BasicFile(java.io.File fio){
		super("-1",fio.getName(),null,new Track("-1",fio.getName(),null,null,null,0,null,0,TypeManager.getTypeByExtension(Util.getExtension(fio)),null,0,""),fio.length(),null) ;
		this.fio = fio;
	}
	
		/**
		 * Return full file path name
		 * @param file
		 * @return String
		 */
		public String getAbsolutePath(){
			return fio.getAbsolutePath();
		}

}
