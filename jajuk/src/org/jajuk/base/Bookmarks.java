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
import java.util.Iterator;
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
	public static synchronized Bookmarks getInstance(){
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
				file.setProperty(OPTION_PLAYLIST_INDEX,Integer.toString(alFiles.size()));
				alFiles.add(file);
			}
		}
	}
	
	/**
	 * Return bookmarks as a colon separeted list of file ids
	 */
	public String toString(){
		Iterator it = alFiles.iterator();
		StringBuffer sbOut = new StringBuffer();
		while (it.hasNext()){
			File file = (File)it.next();
			sbOut.append(file.getId()).append(',');
		}
		int i = sbOut.length();
		return sbOut.substring(0,i-1);//remove last ','
	}
	
	/** Return bookmarked files*/
	public ArrayList getFiles(){
		return alFiles;	
	}
	
	
	/**
	 * Clear bookmarks
	 *
	 */
	public void clear(){
		alFiles.clear();
		ConfigurationManager.setProperty(CONF_BOOKMARKS,""); //$NON-NLS-1$
	}

	/**
	 * Down a track in the playlist
	 * @param index
	 */
	public synchronized void down(int index){
		if ( index < alFiles.size()-1){ //the last track cannot go depper
			File file = (File)alFiles.get(index+1); //save n+1 file
			alFiles.set(index+1,alFiles.get(index));
			alFiles.set(index,file); //n+1 file becomes nth file
			ConfigurationManager.setProperty(CONF_BOOKMARKS,toString());
		}
	}
	
	/**
	 * Up a track in the playlist
	 * @param index
	 */
	public synchronized void up(int index){
		if ( index > 0){ //the first track cannot go further
			File file = (File)alFiles.get(index-1); //save n-1 file
			alFiles.set(index-1,alFiles.get(index));
			alFiles.set(index,file); //n-1 file becomes nth file
			ConfigurationManager.setProperty(CONF_BOOKMARKS,toString());
		}
	}

	/**
	 * Remove a track from the playlist
	 * @param index
	 */
	public synchronized void remove(int index){
		alFiles.remove(index);
		ConfigurationManager.setProperty(CONF_BOOKMARKS,toString());
	}
	
	/**
	 * Add a track from the playlist
	 * @param index
	 */
	public synchronized void addFile(int index,File file){
		//get max index value for tracks
		Iterator it = alFiles.iterator();
		int iMax = -1;
		while ( it.hasNext()){
			File fileTest = (File)it.next();
			int i = Integer.parseInt(fileTest.getProperty(OPTION_PLAYLIST_INDEX));
			if ( i>iMax){
				iMax = i;
			}
		}
		file.setProperty(OPTION_PLAYLIST_INDEX,Integer.toString(iMax+1));
		alFiles.add(index,file);
		ConfigurationManager.setProperty(CONF_BOOKMARKS,toString());
	}
	
	/**
	 * Add a file to this playlist
	 * @param file
	 */
	public void addFile(File file){
		int index = alFiles.size();
		addFile(index,file);
	}
	
}
