/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;


/**
 *  A playlist file not managed by jajuk
 * @Author     Bertrand Florat
 * @created    17 oct. 2003
 */
public class BasicPlaylistFile  {
	
	/**Associated physical file*/
	private File fio;
	/**Contents ( BasicFiles )*/
	 ArrayList alBasicFiles = new ArrayList(10);
	 
	/**
	 * Playlist file constructor for direct use outside collection( file / open )
	 * @param m3u fio io file to read
	 */
	public BasicPlaylistFile(File fio) {
		this.fio = fio;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fio));
			String sLine = null;
			while ((sLine = br.readLine()) != null){
				if ( sLine.length() == 0){ //test void ligne
					continue;
				}
				sLine = sLine.replace('\\','/'); //replace '\' by '/'
				if ( sLine.charAt(0) == '.'){ //deal with url begining by "./something"
					sLine = sLine.substring(1,sLine.length());
				}
				StringBuffer sb = new StringBuffer(sLine);
				if ( sb.charAt(0) == '#'){  //comment
					continue;
				}
				else{
					//check if the specified file is reachable ( absolute path )
					File fileTrack =new File(sb.toString()); 
					if ( fileTrack.exists()){
						alBasicFiles.add(new BasicFile(fileTrack));
						continue;
					}
					else{ //no ? check relatively to playlist file itself
						if ( sLine.charAt(0)!='/'){
							sb.insert(0,'/');
						}
						sb.insert(0,fio.getParentFile().getAbsoluteFile());
						fileTrack = new File(sb.toString());
						if ( fileTrack.exists()){
							alBasicFiles.add(new BasicFile(fileTrack));
							continue;
						}
					}
				}
			}
		}
		catch(Exception e){
			Log.error("017",fio.getAbsolutePath(),e); //$NON-NLS-1$
			Messages.showErrorMessage("017",fio.getAbsolutePath()); //$NON-NLS-1$
		}
		finally{
			if ( br != null){
				try {
					br.close();
				} catch (IOException e1) {
					Log.error(e1);
					Messages.showErrorMessage("017",fio.getAbsolutePath()); //$NON-NLS-1$
				}
			}
		}

	}
	
	/**
	 * @return Returns the list of basic files this playlist maps to
	 */
	public synchronized ArrayList getBasicFiles() throws JajukException{
		return alBasicFiles;
	}
	


}
