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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * A physical directory
 * <p>
 * Physical item
 * @Author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class Directory extends PropertyAdapter implements Comparable{
    
   /** Parent directory ID* */
    private Directory dParent;
    /** Directory device */
    private Device device;
    /** Child directories */
    private ArrayList alDirectories = new ArrayList(20);
    /** Child files */
    private ArrayList alFiles = new ArrayList(20);
    /** Playlist files */
    private ArrayList alPlaylistFiles = new ArrayList(20);
    /** IO file for optimizations* */
    private java.io.File fio;
    /** pre-calculated absolute path for perf*/
    private String sAbs = null;

    /**
     * Direcotry constructor
     * 
     * @param id
     * @param sName
     * @param style
     * @param author
     */
    public Directory(String sId, String sName, Directory dParent, Device device) {
        super(sId,sName);
        this.dParent = dParent;
        setProperty(XML_DIRECTORY_PARENT,(dParent==null?"-1":dParent.getId()));
        this.device = device;
        setProperty(XML_DEVICE,device.getId());
        this.fio = new File(device.getUrl() + getRelativePath());
    }

/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getIdentifier()
     */
    public String getIdentifier() {
        return XML_DIRECTORY;
    }
    /**
     * toString method
     */
    public String toString() {
        return "Directory[ID=" + sId + " Name=" + getRelativePath() + " ParentID=" + (dParent == null ? "null" : dParent.getId()) + " Device=" + device.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
    }
    
    /**
     * Equal method to check two directories are identical
     * 
     * @param otherDirectory
     * @return
     */
    public boolean equals(Object otherDirectory) {
        return this.getId().equals(((Directory)otherDirectory).getId() );
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
    public Device getDevice() {
        return device;
    }
    
    /**
     * @return
     */
    public Directory getParentDirectory() {
        return dParent;
    }
    
    /**
     * @return
     */
    public ArrayList<Directory> getDirectories() {
        return alDirectories;
    }
    
    /**
     * Add a child directory in local refences
     * @param directory
     */
    public void addDirectory(Directory directory) {
        alDirectories.add(directory);
    }
    
    /**
     * Add a playlist file in local refences
     * @param playlist file
     */
    public void addPlaylistFile(PlaylistFile plf) {
        alPlaylistFiles.add(plf);
    }
    
    /**
     * Remove a child directory from local refences
     * @param directory
     */
    public void removeDirectory(Directory directory) {
        alDirectories.remove(directory);
    }
    
    
    /**
     * return child files
     * @return child files
     */
    public ArrayList<org.jajuk.base.File> getFiles() {
        return alFiles;
    }
    
    /**
     * return playlist files
     * @return playlist files
     */
    public ArrayList<PlaylistFile> getPlaylistFiles() {
        return alPlaylistFiles;
    }
    
    /**
     * return child files from a given file in album included
     * @return child files
     */
    public ArrayList getFilesFromFile(org.jajuk.base.File fileStart) {
        Iterator it = alFiles.iterator();
        ArrayList alOut = new ArrayList(alFiles.size());
        boolean bOK = false;
        while (it.hasNext()){
            org.jajuk.base.File file = (org.jajuk.base.File)it.next();
            if (bOK || file.equals(fileStart)){
                alOut.add(file);
                bOK = true;
            }
        }
        return alOut;
    }
    
    /**
     * return child files recursively
     * @return child files recursively
     */
    public ArrayList getFilesRecursively() {
        ArrayList alFiles = new ArrayList(100);
        Iterator it = FileManager.getInstance().getItems().iterator();
        while ( it.hasNext()){
            org.jajuk.base.File file = (org.jajuk.base.File)it.next();
            if ( file.hasAncestor(this)){
                alFiles.add(file);
            }
        }
        Collections.sort(alFiles);
        return alFiles;
    }
    
    /**
     * @param directory
     */
    public void addFile(org.jajuk.base.File file) {
        alFiles.add(file);
    }
    
    /**
     * @param directory
     */
    public void changeFile(org.jajuk.base.File fileOld,org.jajuk.base.File fileNew) {
        alFiles.set(alFiles.indexOf(fileOld),fileNew);
    }
        
    /**
     * Scan all files in a directory
     * @param
     */
    public void scan() {
        java.io.File[] files = getFio().listFiles(new JajukFileFilter(false, true));
        if (files == null || files.length==0){  //none file, leave
            return;
        }
        for (int i = 0; i < files.length; i++) {
            try{ //check errors for each file
                if (files[i].isDirectory()){ //if it is a directory, continue
                    continue;
                }
                boolean bIsMusic = (Boolean)TypeManager.getInstance().getTypeByExtension(Util.getExtension(files[i])).getValue(XML_TYPE_IS_MUSIC);
                if (bIsMusic) {
                    //check the file is not already known in old database
                    org.jajuk.base.File fileRef = null;
                    String sId = MD5Processor.hash(new StringBuffer(getDevice().getName()).append(getDevice().getUrl()).append(getRelativePath()).append(files[i].getName()).toString());
                    Iterator it = TrackManager.getInstance().getItems().iterator();
                    Track track = null;
                    while (it.hasNext()){
                        track = (Track)it.next();
                        Iterator it2 =  track.getFiles().iterator();
                        while (it2.hasNext()){
                            org.jajuk.base.File file = (org.jajuk.base.File)it2.next();
                            if (file.getId().equals(sId)){
                                fileRef = file;
                                break;
                            }
                        }
                    }
                    if (fileRef == null){  //new file
                        device.iNbNewFiles ++;  //stats
                    }
                    else if ( !ConfigurationManager.getBoolean(CONF_TAGS_DEEP_SCAN)){  //read tag data from database, no real read from file for performances reasons if only the deep scan is disable{
                        org.jajuk.base.File file = FileManager.getInstance().registerFile(fileRef.getId(),fileRef.getName(), 
                            this, fileRef.getTrack(), fileRef.getSize(),fileRef.getQuality());
                        addFile(file);
                        FileManager.getInstance().restorePropertiesAfterRefresh(file);
                        continue;
                    }
                    Tag tag = new Tag(files[i]);
                    String sTrackName = tag.getTrackName();
                    String sAlbumName = tag.getAlbumName();
                    String sAuthorName = tag.getAuthorName();
                    String sStyle = tag.getStyleName();
                    long length = tag.getLength(); //length in sec
                    int iYear = tag.getYear();
                    int iQuality = tag.getQuality();
                    String sComment = tag.getComment();
                    int iOrder = tag.getOrder();
                    
                    Album album = AlbumManager.getInstance().registerAlbum(sAlbumName);
                    Style style = StyleManager.getInstance().registerStyle(sStyle);
                    Author author = AuthorManager.getInstance().registerAuthor(sAuthorName);
                    Type type = TypeManager.getInstance().getTypeByExtension(Util.getExtension(files[i]));
                    track = TrackManager.getInstance().registerTrack(sTrackName, album, style, author, length, iYear, type);
                    org.jajuk.base.File newFile = FileManager.getInstance().registerFile(sId,files[i].getName(), this, track, 
                        files[i].length(), iQuality);
                    addFile(newFile);
                    FileManager.getInstance().restorePropertiesAfterRefresh(newFile);
                    track.addFile(newFile);
                    track.setComment(sComment); 
                    /*comment is at the track level, note that we take last found file comment but we changing
                    a comment, we will apply to all files for a track*/
                    track.setOrder(iOrder);
                    TrackManager.getInstance().restorePropertiesAfterRefresh(track);
                }
                else{  //playlist file
                    String sName = files[i].getName();
                    String sId = MD5Processor.hash(new StringBuffer(this.getDevice().getUrl()).append(this.	getRelativePath()).append(sName).toString());
                    BufferedReader br = new BufferedReader(new FileReader(files[i]));
                    StringBuffer sbContent = new StringBuffer();
                    String sTemp;
                    do{
                        sTemp = br.readLine();
                        sbContent.append(sTemp);
                    }
                    while (sTemp != null);
                    String sHashcode =MD5Processor.hash(sbContent.toString()); 
                    PlaylistFile plFile = PlaylistFileManager.getInstance().registerPlaylistFile(sId,sName,sHashcode,this);
                    PlaylistManager.getInstance().registerPlaylist(plFile);
                    addPlaylistFile(plFile);
                }
            }
            catch(Exception e){ 
                Log.error("103",files.length>0?files[i].toString():"",e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
    
    /**
     * Return full directory path name relative to device url
     * 
     * @return String
     */
    public String getRelativePath() {
        if (sAbs!=null){
            return sAbs;
        }
        if (getName().equals("")){  //if this directory is a root device directory //$NON-NLS-1$
            sAbs = ""; //$NON-NLS-1$
            return sAbs;
        }
        StringBuffer sbOut = new StringBuffer().append(java.io.File.separatorChar).append(getName());
        boolean bTop = false;
        Directory dCurrent = this;
        while (!bTop) {
            dCurrent = dCurrent.getParentDirectory();
            if (dCurrent != null && !dCurrent.getName().equals("")) { //if it is the root directory, no parent //$NON-NLS-1$
                sbOut.insert(0, java.io.File.separatorChar).insert(1, dCurrent.getName());
            } else {
                bTop = true;
            }
        }
        sAbs = sbOut.toString();
        return sAbs;
    }
    
    /**
     * @return Returns the IO file reference to this directory.
     */
    public File getFio() {
        return fio;
    }
    
    
    /**
     *Alphabetical comparator used to display ordered lists of directories
     *@param other directory to be compared
     *@return comparaison result 
     */
    public int compareTo(Object o){
        Directory otherDirectory = (Directory)o;
        return  getRelativePath().compareToIgnoreCase(otherDirectory.getRelativePath());
    }
    
    /**
     * Return whether this item should be hidden with hide option
     * @return whether this item should be hidden with hide option
     */
    public boolean shouldBeHidden(){
        if (getDevice().isMounted() ||
                ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED) == false){ //option "only display mounted devices "
            return false;
        }
        return true;
    }
    
   
    /**
     * Get item description
      */
    public String getDesc(){
        String sName = null;
        if (getParentDirectory() == null){
            sName = getDevice().getUrl();
        }
        else{
            sName= getFio().getAbsolutePath();
        }
        return Messages.getString("Item_Directory")+" : "+sName;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getHumanValue(java.lang.String)
     */
    public String getHumanValue(String sKey){
        if (XML_DIRECTORY_PARENT.equals(sKey)){
            Directory dParent = (Directory)DirectoryManager.getInstance().getItem((String)getValue(sKey)); 
            if (dParent == null){
              return ""; //no parent directory          
            }
            else{
                return dParent.getFio().getAbsolutePath();
            }
        }
        else if (XML_DEVICE.equals(sKey)){
            return ((Device)DeviceManager.getInstance().getItem((String)getValue(sKey))).getName();
        }
        if (XML_NAME.equals(sKey)){
            if (dParent == null){ //if no parent, take device name
                return getDevice().getUrl();
            }
            else{
                return getName();          
            }
        }
        else{//default
            return getValue(sKey).toString();
        }
    }
    
}
