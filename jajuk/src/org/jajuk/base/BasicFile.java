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

package org.jajuk.base;



/**
 *  File not managed in collection ( selected by file/open or in a playlist file )
 *
 * @author     bflorat
 * @created    21 oct. 2003
 */
public class BasicFile extends org.jajuk.base.File {
	
	/**Physical file*/
	java.io.File fio;
	
	/**
	 * Basic file constructor
	 * @param fio : system file 
	 */
	public BasicFile(java.io.File fio){
		super(fio);
		this.fio = fio;
	}
	
	/**
	 * Basic file constructor
	 * @param file : regular file  
	 */
	public BasicFile(org.jajuk.base.File file){
		super(file.getId(),file.getName(),file.getDirectory(),file.getTrack(),file.getSize(),file.getQuality());
		this.fio = file.getIO();
	}
	
	/**
	 * Return full file path name
	 * @param file
	 * @return String
	 */
	public String getAbsolutePath(){
		return fio.getAbsolutePath();
	}
	
	/**
	 * Equals method
	 * @return true wheter two basic files have same path
	 */
	public boolean equals(Object o){
		if ( o instanceof BasicFile){
			BasicFile bfile = (BasicFile)o;
			if ( bfile.getAbsolutePath().equals(getAbsolutePath())){
				return true;
			}
		}
		return false;
	}
	
}
