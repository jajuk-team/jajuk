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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;


/**
 *  A playlist file
 * <p> Physical item
 * @Author     Bertrand Florat
 * @created    17 oct. 2003
 */
public class PlaylistFile extends PropertyAdapter implements Comparable {
	
	/**Playlist parent directory*/
	private Directory dParentDirectory;
	/**Files list, singleton*/
	private ArrayList<File> alFiles;
	/**Modification flag*/
	private boolean bModified = false;
	/**Associated physical file*/
	private java.io.File fio;
	/**Type type*/
	private int iType;
	/** pre-calculated absolute path for perf*/
	private String sAbs = null;
    /**Contains files outside device flag*/
    private boolean bContainsExtFiles = false;
   
	
	/**
	 * Playlist file constructor
	 * @param iType playlist file type
	 * @param sId
	 * @param sName
	 * @param sHashcode
	 * @param sParentDirectory
	 */
	public PlaylistFile(int iType,String sId, String sName,Directory dParentDirectory) {
        super(sId,sName);
        this.dParentDirectory = dParentDirectory;
        setProperty(XML_DIRECTORY,dParentDirectory==null?"-1":dParentDirectory.getId()); //$NON-NLS-1$
        this.iType = iType;
        if ( getDirectory() != null){  //test "new"playlist case
			this.fio = new java.io.File(getDirectory().getDevice().getUrl()+getDirectory().getRelativePath()+"/"+getName()); //$NON-NLS-1$
		}
   }
	
/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getIdentifier()
     */
    final public String getIdentifier() {
        return XML_PLAYLIST_FILE;
    }
    
	/**
	 * Playlist file constructor
	 * @param sId
	 * @param sName
	 * @param sHashcode
	 * @param sParentDirectory
	 */
	public PlaylistFile(String sId, String sName,Directory dParentDirectory) {
		this(PlaylistFileItem.PLAYLIST_TYPE_NORMAL,sId,sName,dParentDirectory);
	}
	
	
	/**
	 * toString method
	 */
	public String toString() {
		return "Playlist file[ID="+sId+" Name=" + getName() + " Hashcode="+getStringValue(XML_HASHCODE)+" Dir="+dParentDirectory.getId()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
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
			.append(dCurrent.getRelativePath()).append(java.io.File.separatorChar).append(this.getName());
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
		else if (getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){ 
			FIFO.getInstance().clear();
		}
		else{
		    alFiles.clear();
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
		return getStringValue(XML_HASHCODE);
	}
    
    
    /**
     * 
     * @return playlist file hashcode based on its content
     * @throws IOException
     */
    public String computesHashcode() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(this.getFio()));
        StringBuffer sbContent = new StringBuffer();
        String sTemp;
        do{
            sTemp = br.readLine();
            sbContent.append(sTemp);
        }
        while (sTemp != null);
        return MD5Processor.hash(sbContent.toString());
    }
	
      
	/**
	 * @return
	 */
	public Directory getDirectory() {
		return dParentDirectory;
	}
	
	/**
	 *Alphabetical comparator used to display ordered lists of playlist files
	 *<p>Sort ignoring cases but different items with different cases should be distinct
     * before being added into bidimap</p>
     *@param other playlistfile to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
	    PlaylistFile otherPlaylistFile = (PlaylistFile)o;
	    String sAbs = getName()+getAbsolutePath();
	    String sOtherAbs = otherPlaylistFile.getName()+otherPlaylistFile.getAbsolutePath();
	    if (sAbs.equalsIgnoreCase(sOtherAbs) && !sAbs.equals(sOtherAbs)){
	        return sAbs.compareTo(sOtherAbs);
        }
        else{
            return sAbs.compareToIgnoreCase(sOtherAbs);
        }
   }
	
	/**
	 * @return Returns the list of files this playlist maps to
	 */
	public synchronized ArrayList<File> getFiles() throws JajukException{
		//if normal playlist, propose to mount device if unmounted
        if (getType()==PlaylistFileItem.PLAYLIST_TYPE_NORMAL && !isReady()){
		    String sMessage = Messages.getString("Error.025") + " (" + getDirectory().getDevice().getName() + Messages.getString("FIFO.4"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    int i = Messages.getChoice(sMessage, JOptionPane.INFORMATION_MESSAGE);
		    if (i == JOptionPane.YES_OPTION) {
		        try {
		            //mount. Note that we don't refresh UI to keep selection on this playlist (otherwise the event reset selection).
                    getDirectory().getDevice().mount(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED));
		        } catch (Exception e) {
		            Log.error(e);
		            Messages.showErrorMessage(
		                    "011", getDirectory().getDevice().getName()); //$NON-NLS-1$
		            throw new JajukException("141",fio.getAbsolutePath(),null); //$NON-NLS-1$
		        }
		    }
		    else{
		        throw new JajukException("141",fio.getAbsolutePath(),null); //$NON-NLS-1$
		    }
		}
        if ( iType == PlaylistFileItem.PLAYLIST_TYPE_NORMAL 
                && alFiles == null){ //normal playlist, test if list is null for perfs (avoid reading again the m3u file)
			if ( fio.exists() && fio.canRead()){  //check device is mounted
				alFiles = load(); //populate playlist
                if (containsExtFiles()){
                    Messages.showWarningMessage(Messages.getErrorMessage("142")); //$NON-NLS-1$
                }
			}
			else{  //error accessing playlist file
				throw new JajukException("009",fio.getAbsolutePath(),new Exception()); //$NON-NLS-1$
			}
		}
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BESTOF){ //bestof playlist
			alFiles = new ArrayList(10);
			Iterator it = FileManager.getInstance().getBestOfFiles(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)).iterator(); //even unmounted files if required
			while ( it.hasNext()){
			    alFiles.add((File)it.next());
			}
		}
        else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES){ //novelties playlist
            alFiles = new ArrayList(10);
            ArrayList alNovelties = FileManager.getInstance().getGlobalNoveltiesPlaylist(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED));//even unmounted files if required 
            if (alNovelties == null){
                return alFiles;
            }
            Iterator it = alNovelties.iterator();
            while ( it.hasNext()){
                alFiles.add((File)it.next());
            }
        }
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){ //bookmark playlist
			alFiles = new ArrayList(10);
			Iterator it = Bookmarks.getInstance().getFiles().iterator();
			while ( it.hasNext()){
				alFiles.add((File)it.next());
			}
		}
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_NEW){ //new playlist
			if (alFiles == null ){
				alFiles = new ArrayList(10);
			}
		}
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){ //queue playlist
			//clean data
			alFiles = new ArrayList(10);
			if ( !FIFO.isStopped()){
				ArrayList alQueue = (ArrayList)FIFO.getInstance().getFIFO().clone();
				Iterator it = alQueue.iterator();
				while (it.hasNext()){
					alFiles.add(((StackItem)it.next()).getFile());
				}
			}
		}
		return alFiles;
	}
    
    /**
     * Force playlist re-read (don't use the cache). Can be used after a forced refresh
     *
     */
    protected synchronized void forceRefresh(){
        try{
            alFiles = load(); //populate playlist
        }
        catch(JajukException je){
            Log.error(je);
        }
    }
	
	/**
	 * Add a file to this playlist file
	 * @param index
	 * @param bf
	 */
	public synchronized void addFile(int index,File file) throws JajukException{
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
			Bookmarks.getInstance().addFile(index,file);
		}
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
            StackItem item = new StackItem(file);
            item.setUserLaunch(false);
            //set repeat mode : if previous item is repeated, repeat as well
            if (index > 0){
                StackItem itemPrevious = FIFO.getInstance().getItem(index-1);
                if (itemPrevious != null && itemPrevious.isRepeat()){
                    item.setRepeat(true);
                }
                else{
                    item.setRepeat(false);
                }
                FIFO.getInstance().insert(item,index); //insert this track in the fifo
            }
            else{ //start immediatly playing
                FIFO.getInstance().push(item,false);
            }
        }
        else {
            getFiles().add(index,file);
            setModified(true);
        }
    }
	
	
	/**
	 * Add a file to this playlist file
	 * @param bf
	 */
	public synchronized void addFile(File file) throws JajukException{
		ArrayList al = getFiles();
		int index = al.size();
		addFile(index,file);
  }
	
	/**
	 * Add some files to this playlist file. 
	 * @param alFilesToAdd : List of File
	 */
	public synchronized void addFiles(ArrayList alFilesToAdd) throws JajukException{
	    try{
	        Iterator it = alFilesToAdd.iterator();
	        while (it.hasNext()){
	            org.jajuk.base.File file = (org.jajuk.base.File)it.next();
	            addFile(file);
            }
	    }
	    catch(Exception e){
	        Log.error(e);
	    }
	    finally{
	        ObservationManager.notify(new Event(EVENT_PLAYLIST_REFRESH)); //refresh playlist editor    
	    }
	}
	
	
	/**
	 * Down a track in the playlist
	 * @param index
	 */
	public synchronized void down(int index){
		if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
			Bookmarks.getInstance().down(index);
		}
		else if(iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
		    FIFO.getInstance().down(index);
		}
		else if ( alFiles != null &&  index < alFiles.size()-1){ //the last track cannot go depper
			File file = (File)alFiles.get(index+1); //save n+1 file
			alFiles.set(index+1,alFiles.get(index));
			alFiles.set(index,file); //n+1 file becomes nth file
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
		else if(iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
		    FIFO.getInstance().up(index);
		}
		else if ( alFiles != null &&  index > 0){ //the first track cannot go further
			File bfile = (File)alFiles.get(index-1); //save n-1 file
			alFiles.set(index-1,alFiles.get(index));
			alFiles.set(index,bfile); //n-1 file becomes nth file
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
		else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
			FIFO.getInstance().remove(index,index);
		}
		else{
			alFiles.remove(index);
		}
		setModified(true);
  }

    /**
     * Remove a fiven file from the playlist (can have several occurences)
     * @param file to drop
     */
    public synchronized void remove(File file){
        if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
            Iterator it = Bookmarks.getInstance().getFiles().iterator();
            for (int i=0; it.hasNext(); i++){
                File fileToTest = (File)it.next();
                if (fileToTest.equals(file)){
                    Bookmarks.getInstance().remove(i);
                }
            }
        }
        else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
            Iterator it = FIFO.getInstance().getFIFO().iterator();
            for (int i=0; it.hasNext(); i++){
                File fileToTest = (File)it.next();
                if (fileToTest.equals(file)){
                    FIFO.getInstance().remove(i,i);
                }
            }
        }
        else{
            Iterator it = alFiles.iterator();
            for (int i=0; it.hasNext(); i++){
                File fileToTest = (File)it.next();
                if (fileToTest.equals(file)){
                    alFiles.remove(i);
                }
            }
        }
        setModified(true);
    }
    
    /**
     * Relace a file inside a playlist
     * @param fOld
     * @param fNew
     */
    public void replaceFile(File fOld,File fNew){
        if ( iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
            Iterator it = Bookmarks.getInstance().getFiles().iterator();
            for (int i=0; it.hasNext(); i++){
                File fileToTest = (File)it.next();
                if (fileToTest.equals(fOld)){
                    Bookmarks.getInstance().remove(i);
                    Bookmarks.getInstance().addFile(i,fNew);
                }
            }
        }
        else if ( iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
            Iterator it = FIFO.getInstance().getFIFO().iterator();
            for (int i=0; it.hasNext(); i++){
                File fileToTest = (File)it.next();
                if (fileToTest.equals(fOld)){
                    FIFO.getInstance().remove(i,i); //just emove
                    ArrayList al = new ArrayList(1);
                    al.add(fNew);
                    FIFO.getInstance().insert(al,i);
                }
            }
        }
        else{
            Iterator it = alFiles.iterator();
            for (int i=0; it.hasNext(); i++){
                File fileToTest = (File)it.next();
                if (fileToTest.equals(fOld)){
                    alFiles.remove(i);
                    alFiles.add(i,fNew);
                    try {
                        commit();//save changed playlist file
                    }
                    catch (JajukException e) {
                        Log.error(e);
                    }
                }
            }
        }
        setModified(true);
    }
	
	/**
	 * Update playlist file on disk if needed
	 */
	public void commit() throws JajukException{
		BufferedWriter bw = null;
		if ( isModified()){
			try {
				bw = new BufferedWriter(new FileWriter(fio));
				bw.write(PLAYLIST_NOTE); 
				bw.newLine();
                Iterator it = getFiles().iterator();
				while ( it.hasNext()){
					File bfile = (File)it.next();
					bw.write(bfile.getAbsolutePath());
                    bw.newLine();
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
                        setHashcode(computesHashcode());
                        PlaylistManager.getInstance().refreshPlaylist(this);
                        ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH));  //refresh repository list (mandatory for logical playlist collapse/merge)
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
	public ArrayList load() throws JajukException{
		ArrayList alFiles = new ArrayList(10);
		BufferedReader br = null;
		try {
		    br = new BufferedReader(new FileReader(fio));
		    String sLine = null;
		    boolean bUnknownDevicesMessage = false;
		    while ((sLine = br.readLine()) != null){
		        if (sLine.length() == 0){ //void line
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
		            java.io.File fileTrack = null;
		            StringBuffer sbFileDir = new StringBuffer(getDirectory().getDevice().getUrl()).append(getDirectory().getRelativePath());
		            if ( sLine.charAt(0)!='/'){
		                sb.insert(0,'/');
		            }
		            //take a look relatively to playlist directory to check files exists
		            fileTrack = new java.io.File(sbFileDir.append(sb).toString());
		            File file = FileManager.getInstance().getFileByPath(fileTrack.getAbsolutePath());
                    if ( file == null){  //check if this file is known in collection
		                fileTrack = new java.io.File(sLine); //check if given url is not absolute
		                file = FileManager.getInstance().getFileByPath(fileTrack.getAbsolutePath());
                        if ( file == null){ //no more ? leave
		                    bUnknownDevicesMessage = true;
                            continue;
		                }
                    }
                    alFiles.add(file);
		        }
		    }
		    //display a warning message if the playlist contains unknown items
		    if (bUnknownDevicesMessage){
		        this.bContainsExtFiles = true;
		    }
		}
		catch(Exception e){
		    Log.error("017",getName(),e); //$NON-NLS-1$
		    throw new JajukException("017",fio.getAbsolutePath(),e); //$NON-NLS-1$
		}
		finally{
		    if ( br != null){
		        try {
		            br.close();
		        } catch (IOException e1) {
		            Log.error(e1);
		            throw new JajukException("017",fio.getAbsolutePath(),e1); //$NON-NLS-1$
		        }
		    }
		}
		return alFiles;
	}
	
	
		
	/**Return true the file can be accessed right now 
	 * @return true the file can be accessed right now*/
	public boolean isReady(){
		if ( getDirectory().getDevice().isMounted()){
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
	public java.io.File getFio() {
		return fio;
	}
	
	/**
	 * @param fio The fio to set.
	 */
	public void setFio(java.io.File fio) {
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
		this.iType = type;
        setProperty(XML_TYPE,type);
	}
	
	/**
	 * Play a playlist file
	 *
	 */
	public void play() throws JajukException{
	    alFiles = getFiles();
        if ( alFiles == null || alFiles.size() == 0){
			Messages.showErrorMessage("018");	 //$NON-NLS-1$
		}
		else{
			FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),
					ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
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
	 * @param alFiles The alFiles to set.
	 */
	public void setFiles(ArrayList alFiles) {
		this.alFiles = alFiles;
	}
	
	/**
	 * Save as... the playlist file 
	 */
	public void saveAs() throws Exception{
		ArrayList alTypes = new ArrayList(1);
		alTypes.add(TypeManager.getInstance().getTypeByExtension(EXT_PLAYLIST));
		JajukFileChooser jfchooser = new JajukFileChooser(new JajukFileFilter(true,alTypes));
        String sPlaylist = DEFAULT_PLAYLIST_FILE;
        //computes new playlist file
        alFiles = getFiles();
        if (alFiles.size() > 0){
            File file = alFiles.get(0);
            sPlaylist = file.getDirectory().getAbsolutePath()+ java.io.File.separatorChar + file.getTrack().getHumanValue(XML_ALBUM);
        }
        else{
            return;
        }
        jfchooser.setSelectedFile(new java.io.File(sPlaylist+"."+EXT_PLAYLIST));//$NON-NLS-1$
		int returnVal = jfchooser.showSaveDialog(Main.getWindow());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    java.io.File file = jfchooser.getSelectedFile();
		    //add automaticaly the extension if required
		    if (file.getAbsolutePath().endsWith(EXT_PLAYLIST)){
		        file = new java.io.File(file.getAbsolutePath());  
		    }
		    else{
		        file = new java.io.File(file.getAbsolutePath()+"."+EXT_PLAYLIST);//$NON-NLS-1$
		    }
		    
		    this.setFio(file); //set new file path ( this playlist is a special playlist, just in memory )
		    this.commit(); //write it on the disk
		    java.io.File fDir = file.getParentFile();
		    Directory dir = DirectoryManager.getInstance().getDirectoryForIO(fDir);
		    if (dir != null){ //the new playlist file in inside collection
		        PlaylistFile plFile = PlaylistFileManager.getInstance().registerPlaylistFile(file,dir);
		        PlaylistManager.getInstance().registerPlaylist(plFile);
		        dir.addPlaylistFile(plFile);
		    }
    	}
    }

    /**
     * @param parentDirectory The dParentDirectory to set.
     */
    protected void setParentDirectory(Directory parentDirectory) {
        this.dParentDirectory = parentDirectory;
        setProperty(XML_DIRECTORY,parentDirectory==null?"-1":parentDirectory.getId()); //$NON-NLS-1$
    }

      /**
     * @param hashcode The sHashcode to set.
     */
    protected void setHashcode(String hashcode) {
        setProperty(XML_HASHCODE,hashcode);
    }

    /**
     * Get item description
     */
    public String getDesc(){
        return Messages.getString("Item_Playlist_File")+" : "+getName(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getHumanValue(java.lang.String)
     */
    public String getHumanValue(String sKey){
        if (XML_DIRECTORY.equals(sKey)){
            Directory dParent = (Directory)DirectoryManager.getInstance().getItem(getStringValue(sKey)); 
            return dParent.getFio().getAbsolutePath();
        }
        else{//default
            return super.getHumanValue(sKey);
        }
    }

    public boolean containsExtFiles() {
        return bContainsExtFiles;
    }
    
}
