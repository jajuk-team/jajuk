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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jajuk.util.ConfigurationManager;

/**
 *  Manages bookmarks
 *
 * @author     bflorat
 * @created    21 févr. 2004
 */
public class Bookmarks implements ITechnicalStrings {

	/**Singleton self-instance*/
	private static Bookmarks bookmarks; 
	
	/** Bookmarks files*/
	ArrayList alFiles = new ArrayList(100);
	
	/** Singleton accessor
	 * @return the singleton*/
	public static Bookmarks getInstance(){
		if ( bookmarks == null){
			bookmarks = new Bookmarks();
		}
		return bookmarks;
	}
	
	/**Private constructor*/
	private Bookmarks(){
		String sBookmarks = ConfigurationManager.getProperty(CONF_BOOKMARKS);
		if ( sBookmarks == null || "".equals(sBookmarks.trim())){ //$NON-NLS-1$
			return;
		}
		StringTokenizer stFiles = new StringTokenizer(sBookmarks,","); //$NON-NLS-1$
		while ( stFiles.hasMoreTokens()){
			String sId = stFiles.nextToken();
			File file = FileManager.getFile(sId);
			if ( file != null){
				alFiles.add(file);
			}
		}
	}
	
	/** Return bookmarked files*/
	public ArrayList getFiles(){
		return alFiles;	
	}
	
	public void addFile(File file){
		alFiles.add(file);
		String sOut ;
		String sBookmarks = ConfigurationManager.getProperty(CONF_BOOKMARKS);
		if ( sBookmarks == null || "".equals(sBookmarks.trim())){ //$NON-NLS-1$
			sOut = file.getId(); 
		}
		else{
			sOut = new StringBuffer(sBookmarks).append(',').append(file.getId()).toString();
		}
		ConfigurationManager.setProperty(CONF_BOOKMARKS,sOut);
	}
		
}
