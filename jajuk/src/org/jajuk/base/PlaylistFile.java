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
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
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
	/**Basic Files list, singletton*/
	private ArrayList alBasicFiles;
	/**Modification flag*/
	private boolean bModified = false;
	/**Associated physical file*/
	private File fio;
	/**Type type*/
	private int iType;
	/** pre-calculated absolute path for perf*/
	private String sAbs = null;

	
	/**
	 * Playlist file constructor
	 * @param iType playlist file type
	 * @param sId
	 * @param sName
	 * @param sHashcode
	 * @param sParentDirectory
	 */
	public PlaylistFile(int iType,String sId, String sName,String sHashcode,Directory dParentDirectory) {
		this.iType = iType;
		this.sId = sId;
		this.sName = sName;
		this.sHashcode = sHashcode;
		this.dParentDirectory = dParentDirectory;
		if ( getDirectory() != null){  //test "new"playlist case
			this.fio = new File(getDirectory().getDevice().getUrl()+getDirectory().getRelativePath()+"/"+getName()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Playlist file constructor
	 * @param sId
	 * @param sName
	 * @param sHashcode
	 * @param sParentDirectory
	 */
	public PlaylistFile(String sId, String sName,String sHashcode,Directory dParentDirectory) {
		this(PlaylistFileItem.PLAYLIST_TYPE_NORMAL,sId,sName,sHashcode,dParentDirectory);
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
		PlaylistFile plfOther = (PlaylistFile)otherPlaylistFile;
		return (this.getId().equals(plfOther.getId()) && plfOther.getType() == this.iType);
	}	
	
	
	/**
	 * Return absolute file path name
	 * @return String
	 */
	public String getAbsolutePath(){
		if (sAbs!=null){
			return sAbs;
		}
		Directory dCurrent = getDirectory();
		StringBuffer sbOut = new StringBuffer(getDirectory().getDevice().getUrl())
			.append(dCurrent.getRelativePath()).append('/').append(this.getName());
		sAbs = sbOut.toString();
		return sAbs;
	}
	
	/**
	* Clear playlist file
	*
	*/
	public synchronized void clear(){
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){ //bookmark playlist
			Bookmarks.getInstance().clear();
		}
		else{
			alBasicFiles.clear();
		}
		setModified(true);
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
	public synchronized ArrayList getBasicFiles() throws JajukException{
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_NORMAL && alBasicFiles == null){ //normal playlist
			if ( fio.exists() && fio.canRead()){  //check device is mounted
				alBasicFiles = load(); //populate playlist
			}
			else{  //error accessing playlist file
				throw new JajukException("009",fio.getAbsolutePath(),new Exception()); //$NON-NLS-1$
			}
		}
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BESTOF){ //bestof playlist
			alBasicFiles = new ArrayList(10);
			Iterator it = FileManager.getBestOfFiles().iterator();
			while ( it.hasNext()){
				alBasicFiles.add(new BasicFile((org.jajuk.base.File)it.next()));
			}
		}
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){ //bookmark playlist
			alBasicFiles = new ArrayList(10);
			Iterator it = Bookmarks.getInstance().getFiles().iterator();
			while ( it.hasNext()){
				alBasicFiles.add(new BasicFile((org.jajuk.base.File)it.next()));
			}
		}
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_NEW){ //new playlist
			if (alBasicFiles == null ){
				alBasicFiles = new ArrayList(10);
			}
		}
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){ //queue playlist
			//clean data
			alBasicFiles = new ArrayList(10);
			if ( !FIFO.isStopped()){
				//add currently played file
				org.jajuk.base.File file = FIFO.getInstance().getCurrentFile();
				if ( file != null){
					alBasicFiles.add(new BasicFile(file));  
				}
				//next files
				ArrayList alQueue = (ArrayList)FIFO.getInstance().getFIFO().clone();
				Iterator it = alQueue.iterator();
				while (it.hasNext()){
					BasicFile bfile = new BasicFile((org.jajuk.base.File)it.next()); 
					alBasicFiles.add(bfile);
				}
			}
		}
		for (int i =0;i<alBasicFiles.size();i++){
			BasicFile bfile = (BasicFile)alBasicFiles.get(i);
			//set indexes to enable selection in editor
			bfile.setProperty(OPTION_PLAYLIST_INDEX,Integer.toString(i));
			//set associated playlist for " go to current playlist" function
			bfile.setProperty(OPTION_PLAYLIST,this.getId());
		}
		return alBasicFiles;
	}
	
	/**
	 * Add a basic file to this playlist file
	 * @param index
	 * @param bf
	 */
	public synchronized void addBasicFile(int index,BasicFile bf) throws JajukException{
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
			Bookmarks.getInstance().addFile(index,bf);
		}
		else {
			//get max index value for tracks
			Iterator it = getBasicFiles().iterator();
			int iMax = -1;
			while ( it.hasNext()){
				BasicFile bfTest = (BasicFile)it.next();
				int i = Integer.parseInt(bfTest.getProperty(OPTION_PLAYLIST_INDEX));
				if ( i>iMax){
					iMax = i;
				}
			}
			ArrayList al = getBasicFiles(); 
			bf.setProperty(OPTION_PLAYLIST_INDEX,Integer.toString(iMax+1));
			al.add(index,bf);
			setModified(true);
		}
	}
	
	
	/**
	 * Add a basic file to this playlist file
	 * @param bf
	 */
	public synchronized void addBasicFile(BasicFile bf) throws JajukException{
		ArrayList al = getBasicFiles();
		int index = al.size();
		addBasicFile(index,bf);
	}
	
	
	/**
	 * Down a track in the playlist
	 * @param index
	 */
	public synchronized void down(int index){
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
			Bookmarks.getInstance().down(index);
		}
		else if ( index < alBasicFiles.size()-1){ //the last track cannot go depper
			BasicFile bfile = (BasicFile)alBasicFiles.get(index+1); //save n+1 file
			alBasicFiles.set(index+1,alBasicFiles.get(index));
			alBasicFiles.set(index,bfile); //n+1 file becomes nth file
			setModified(true);
		}
	}
	
	/**
	 * Up a track in the playlist
	 * @param index
	 */
	public synchronized void up(int index){
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
			Bookmarks.getInstance().up(index);
		}
		else if ( index > 0){ //the first track cannot go further
			BasicFile bfile = (BasicFile)alBasicFiles.get(index-1); //save n-1 file
			alBasicFiles.set(index-1,alBasicFiles.get(index));
			alBasicFiles.set(index,bfile); //n-1 file becomes nth file
			setModified(true);
		}
	}
	
	
	/**
	 * Remove a track from the playlist
	 * @param index
	 */
	public synchronized void remove(int index){
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
			Bookmarks.getInstance().remove(index);
		}
		alBasicFiles.remove(index);
		setModified(true);
	}
	
	/**
	 * Update playlist file on disk if needed
	 *
	 */
	public void commit() throws JajukException{
		BufferedWriter bw = null;
		if ( isModified()){
			try {
				bw = new BufferedWriter(new FileWriter(fio));
				bw.write(PLAYLIST_NOTE+"\n"); //$NON-NLS-1$
				Iterator it = getBasicFiles().iterator();
				while ( it.hasNext()){
					BasicFile bfile = (BasicFile)it.next();
					bw.write(bfile.getAbsolutePath()+"\n"); //$NON-NLS-1$
				}
			}
			catch(Exception e){
				throw new JajukException("028",getName(),e); //$NON-NLS-1$
			}
			finally{
				if ( bw != null){
					try {
						bw.flush();
						bw.close();
					} catch (IOException e1) {
						throw new JajukException("028",getName(),e1); //$NON-NLS-1$
					}
				}
			}
		}
	}
	
	
	
	
	/**
	 * Parse a playlist file
	 */
	public ArrayList load(){
		ArrayList alFiles = new ArrayList(10);
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
					alFiles.add(bfile);
				}
			}
		}
		catch(Exception e){
			Log.error("017",getName(),e); //$NON-NLS-1$
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
		return alFiles;
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
	
	
	/**
	 * Return whether this item should be hidden with hide option
	 * @return whether this item should be hidden with hide option
	 */
	public boolean shouldBeHidden(){
		if (getDirectory().getDevice().isMounted() ||
				ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED) == false){ //option "only display mounted devices "
			return false;
		}
		return true;
	}
	
	/**
	 * @return Returns the fio.
	 */
	public File getFio() {
		return fio;
	}
	
	/**
	 * @param fio The fio to set.
	 */
	public void setFio(File fio) {
		setModified(true);
		this.fio = fio;
	}
	
	/**
	 * @return Returns the iType.
	 */
	public int getType() {
		return iType;
	}
	
	/**
	 * @param type The iType to set.
	 */
	public void setType(int type) {
		iType = type;
	}
	
	/**
	 * Play a playlist file
	 *
	 */
	public void play(){
		ArrayList alFiles = null;
		try{
			alFiles = getBasicFiles();
		}
		catch(JajukException je){
			Log.error("009",getName(),new Exception()); //$NON-NLS-1$
			Messages.showErrorMessage("009",getName()); //$NON-NLS-1$
			return;
		}
		if ( alFiles == null || alFiles.size() == 0){
			Messages.showErrorMessage("018");	 //$NON-NLS-1$
		}
		else{
			FIFO.getInstance().push(alFiles,false);
		}
	}
	
	
	/**
	 * @return Returns the bModified.
	 */
	public boolean isModified() {
		return bModified;
	}

	/**
	 * @param modified The bModified to set.
	 */
	public void setModified(boolean modified) {
		bModified = modified;
	}

	/**
	 * @param alBasicFiles The alBasicFiles to set.
	 */
	public void setBasicFiles(ArrayList alBasicFiles) {
		this.alBasicFiles = alBasicFiles;
	}

}
