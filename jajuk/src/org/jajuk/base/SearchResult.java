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
 * $Release$
 */

package org.jajuk.base;

/**
 *  A search result, contains a file and a search description
 *
 * @author     bflorat
 * @created    16 janv. 2004
 */
public class SearchResult implements Comparable{
	
	/** The associated file*/
	File file;
	/** Pre-calculated search string */
	String sResu;
	
	 
	public SearchResult(File file){
		this(file,file.toStringSearch()); 	
	}
	
	public SearchResult(File file,String sResu){
	 	this.file = file;
	 	this.sResu = sResu;
	}
	
	/**
	 * Return hashcode, used during sorting
	 */
	public int hashCode(){
		return sResu.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		SearchResult sr = (SearchResult)o;
		return  sResu.compareToIgnoreCase(sr.getResu());
	}
	
	/**
	 * @return Returns the file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return Returns the sResu.
	 */
	public String getResu() {
		return sResu;
	}

}
