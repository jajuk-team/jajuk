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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;


/**
 *  A playlist file
 * <p> Physical item
 * @Author     bflorat
 * @created    17 oct. 2003
 */
public class PlaylistFile extends PropertyAdapter implements Comparable {

	/**ID. Ex:1,2,3...*/
	private String  sId;
	/**Playlist name */
	private String sName;
	/**Playlist hashcode*/
	private String  sHashcode;
	/**Playlist parent directory*/
	private Directory dParentDirectory;
	/**Basic Files list*/
	private ArrayList alBasicFiles = new ArrayList(10);
	/**Modification flag*/
	private boolean bModified = false;
	/**Associated physical file*/
	private File fio;
	
	
	/**
	 * Playlist file constructor
	 * @param sId
	 * @param sName
	 * @param sHashcode
	 * @param sParentDirectory
	 */
	public PlaylistFile(String sId, String sName,String sHashcode,Directory dParentDirectory) {
		this.sId = sId;
		this.sName = sName;
		this.sHashcode = sHashcode;
		this.dParentDirectory = dParentDirectory;
		this.fio = new File(getDirectory().getDevice().getUrl()+getDirectory().getRelativePath()+"/"+getName()); //$NON-NLS-1$
		if ( fio.exists() && fio.canRead()){  //check device is mounted
			load(); //populate playlist
		}
	}

	
	/**
	 * toString method
	 */
	public String toString() {
		return "Playlist file[ID="+sId+" Name=" + getName() + " Hashcode="+sHashcode+" Dir="+dParentDirectory.getId()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<playlist_file id='" + sId); //$NON-NLS-1$
		sb.append("' name='"); //$NON-NLS-1$
		sb.append(Util.formatXML(sName));
		sb.append("' hashcode='"); //$NON-NLS-1$
		sb.append(sHashcode);
		sb.append("' directory='"); //$NON-NLS-1$
		sb.append(dParentDirectory.getId()).append("' "); //$NON-NLS-1$
		sb.append(getPropertiesXml());
		sb.append("/>\n"); //$NON-NLS-1$
		return sb.toString();
	}


	/**
	 * Equal method to check two playlist files are identical
	 * @param otherPlaylistFile
	 * @return
	 */
	public boolean equals(Object otherPlaylistFile){
		return this.getHashcode().equals(((PlaylistFile)otherPlaylistFile).getHashcode());
	}	
	
	/**
	 * hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
	}

	/**
	 * @return
	 */
	public String getHashcode() {
		return sHashcode;
	}

	/**
	 * @return
	 */
	public String getId() {
		return sId;
	}

	/**
	 * @return
	 */
	public String getName() {
		return sName;
	}

	/**
	 * @return
	 */
	public Directory getDirectory() {
		return dParentDirectory;
	}
	
	/**
	 *Alphabetical comparator used to display ordered lists of playlist files
	 *@param other playlistfile to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
		PlaylistFile otherPlaylistFile = (PlaylistFile)o;
		return  getName().compareToIgnoreCase(otherPlaylistFile.getName());
	}

	/**
	 * @return Returns the list of basic files this playlist maps to
	 */
	public ArrayList getBasicFiles() {
		return alBasicFiles;
	}
	
	/**
	 * Add a basic file to this playlist file
	 * @param bf
	 */
	public void addBasicFile(BasicFile bf){
		alBasicFiles.add(bf);
		bModified = true;
	}
	
	/**
	 * Update playlist file on disk if needed
	 *
	 */
	public void commit(){
		BufferedWriter bw = null;
		if ( bModified){
			try {
				bw = new BufferedWriter(new FileWriter(fio));
				bw.write(PLAYLIST_NOTE+"\n"); //$NON-NLS-1$
				Iterator it = alBasicFiles.iterator();
				while ( it.hasNext()){
					BasicFile bfile = (BasicFile)it.next();
					bw.write(bfile.getAbsolutePath()+"\n"); //$NON-NLS-1$
				}
			}
			catch(Exception e){
				Log.error("017",getName(),e); //$NON-NLS-1$
			}
			finally{
				if ( bw != null){
					try {
						bw.flush();
						bw.close();
					} catch (IOException e1) {
						Log.error(e1);
					}
				}
			}
		}
	}
	
	/**
	 * Parse a playlist file
	 */
	public void load(){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fio));
			String sLine = null;
			while ((sLine = br.readLine()) != null){
				sLine = sLine.replace('\\','/'); //replace '\' by '/'
				if ( sLine.charAt(0) == '.'){ //deal with url begining by "./something"
					sLine = sLine.substring(1,sLine.length());
				}
				StringBuffer sb = new StringBuffer(sLine);
				if ( sb.charAt(0) == '#'){  //comment
					continue;
				}
				else{
					File fileTrack = null;
					StringBuffer sbFileDir = new StringBuffer(getDirectory().getDevice().getUrl()).append(getDirectory().getRelativePath());
					if ( sLine.charAt(0)!='/'){
						sb.insert(0,'/');
					}
					//take a look relatively to playlist directory to check files exists
					fileTrack = new File(sbFileDir.append(sb).toString());
					if ( !fileTrack.exists()){  //check if this file exists
						fileTrack = new File(sb.toString()); //check if given url is not absolute
						if ( !fileTrack.exists()){ //no more ? leave
							continue;
						}	
					}
					BasicFile bfile = new BasicFile(fileTrack);
					alBasicFiles.add(bfile);
				}
			}
		}
		catch(Exception e){
			Log.error("017",getName(),e); //$NON-NLS-1$
		}
		finally{
			if ( br != null){
				try {
					br.close();
				} catch (IOException e1) {
					Log.error(e1);
				}
			}
		}
	}
	
	
	/**
	 * Delete this playlist file
	 *
	 */
	public void delete(){
		if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE)){  //file delete confirmation
			String sFileToDelete = getDirectory().getFio().getAbsoluteFile()+"/"+getName(); //$NON-NLS-1$
			String sMessage = Messages.getString("Confirmation_delete")+"\n"+sFileToDelete; //$NON-NLS-1$ //$NON-NLS-2$
			int i = JOptionPane.showConfirmDialog(Main.jframe,sMessage,Messages.getString("Warning"),JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
			if ( i == JOptionPane.OK_OPTION){
				File fileToDelete = new File(sFileToDelete);
				if ( fileToDelete.exists()){
					fileToDelete.delete();
					PlaylistFileManager.delete(getId());
					ObservationManager.notify(EVENT_DEVICE_REFRESH);  //requires device refresh
				}
			}
		}
	}
	
	/**Return true the file can be accessed right now 
	 * @return true the file can be accessed right now*/
	public boolean isReady(){
		if ( getDirectory().getDevice().isMounted() && !getDirectory().getDevice().isRefreshing() && !getDirectory().getDevice().isSynchronizing()){
			return true;
		}
		return false;
	}
	

}
