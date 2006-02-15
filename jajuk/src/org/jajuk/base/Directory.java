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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
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
    private TreeSet<org.jajuk.base.File> files = new TreeSet();
    /** Playlist files */
    private TreeSet<PlaylistFile> playlistFiles = new TreeSet();
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
        setProperty(XML_DIRECTORY_PARENT,(dParent==null?"-1":dParent.getId())); //$NON-NLS-1$
        this.device = device;
        setProperty(XML_DEVICE,device.getId());
        this.fio = new File(device.getUrl() + getRelativePath());
    }

/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getIdentifier()
     */
    final public String getIdentifier() {
        return XML_DIRECTORY;
    }
    /**
     * toString method
     */
    public String toString() {
        return "Directory[ID=" + sId + " Name={{" + getRelativePath() + "}} ParentID=" + (dParent == null ? "null" : dParent.getId()) + " Device=" + device.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
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
    
    public String getAbsolutePath(){
        return this.fio.getAbsolutePath();    
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
        if (!alDirectories.contains(directory)){
            alDirectories.add(directory);    
        }
    }
    
     /**
     * Remove a file from local refences
     * @param file
     */
    public void removeFile(org.jajuk.base.File file) {
        if (files.contains(file)){
            files.remove(file);    
        }
    }
    
    /**
     * Add a playlist file in local refences
     * @param playlist file
     */
    public void addPlaylistFile(PlaylistFile plf) {
        playlistFiles.add(plf);
    }
    
    /**
     * Remove a playlist file from local refences
     * @param playlist file
     */
    public void removePlaylistFile(PlaylistFile plf) {
        if(playlistFiles.contains(plf)){
            playlistFiles.remove(plf);    
        }
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
    public Set<org.jajuk.base.File> getFiles() {
        return files;
    }
    
    /**
     * return playlist files
     * @return playlist files
     */
    public Set<PlaylistFile> getPlaylistFiles() {
        return playlistFiles;
    }
    
    /**
     * return child files from a given file in album included
     * @return child files
     */
    public ArrayList getFilesFromFile(org.jajuk.base.File fileStart) {
        Iterator it = files.iterator();
        ArrayList alOut = new ArrayList(files.size());
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
    public ArrayList<org.jajuk.base.File> getFilesRecursively() {
        ArrayList alFiles = new ArrayList(100);
        Iterator it = FileManager.getInstance().getItems().iterator();
        while ( it.hasNext()){
            org.jajuk.base.File file = (org.jajuk.base.File)it.next();
            if ( file.hasAncestor(this)){
                alFiles.add(file);
            }
        }
        return alFiles;
    }
    
    /**
     * @param directory
     */
    public void addFile(org.jajuk.base.File file) {
        files.add(file);
    }
                
    /**
     * @param directory
     */
    public void changePlaylistFile(PlaylistFile plfOld,PlaylistFile plfNew) {
        playlistFiles.remove(plfOld);
        playlistFiles.add(plfNew);
    }
    
    /**
     * @param directory
     */
    public void changeFile(org.jajuk.base.File fileOld,org.jajuk.base.File fileNew) {
        files.remove(fileOld);
        files.add(fileNew);
    }
        
    /**
     * Scan all files in a directory
     * @param bDeepScan: force files tag read
     * @param
     */
    public void scan(boolean bDeepScan) {
        java.io.File[] files = getFio().listFiles(Util.fileFilter);
        if (files == null || files.length==0 ){  //none file, leave
            return;
        }
        for (int i = 0; i < files.length; i++) {
            try{ //check errors for each file
                if (files[i].isDirectory()){ //if it is a directory, continue
                    continue;
                }
                //check date, file modified before
                long lastModified = files[i].lastModified();
                if (lastModified > DeviceManager.getInstance().getDateLastGlobalRefresh()
                        && !bDeepScan){
                   continue; 
                }
                //Check file name is correct (usefull to fix name encoding issues)
                if (!new File(files[i].getAbsolutePath()).exists()){
                    Log.warn("Cannot read file name (please rename it): "+files[i].getAbsolutePath()); //$NON-NLS-1$
                    continue;
                }
                boolean bIsMusic = (Boolean)TypeManager.getInstance().getTypeByExtension(Util.getExtension(files[i])).getValue(XML_TYPE_IS_MUSIC);
                if (bIsMusic) {
                    String sId = FileManager.getID(files[i].getName(),device,this);
                    //check the file is not already known in database
                    org.jajuk.base.File fileRef = (org.jajuk.base.File)FileManager.getInstance().getItem(sId);
                    //if known file and no deep scan, just leave
                    if (fileRef != null && !bDeepScan){
                        continue;
                    }
                    //New file or deep scan case
                    Tag tag = null;
                    tag = new Tag(files[i],true); //ignore tag error to make sure to get a tag object in all cases
                    if (tag.isCorrupted()){
                        device.iNbCorruptedFiles ++; //stats
                        Log.error("103",files[i].getAbsolutePath(),null); //$NON-NLS-1$
                    }
                    //if an error occurs, just notice it but keep the track
                    String sTrackName = tag.getTrackName();
                    String sAlbumName = tag.getAlbumName();
                    String sAuthorName = tag.getAuthorName();
                    String sStyle = tag.getStyleName();
                    long length = tag.getLength(); //length in sec
                    long lYear = tag.getYear();
                    long lQuality = tag.getQuality();
                    String sComment = tag.getComment();
                    long lOrder = tag.getOrder();
                    if (fileRef == null){
                        device.iNbNewFiles ++;  //stats, do it here and not before because we ignore the file if we cannot read it
                    }
                    Album album = AlbumManager.getInstance().registerAlbum(sAlbumName);
                    Style style = StyleManager.getInstance().registerStyle(sStyle);
                    Author author = AuthorManager.getInstance().registerAuthor(sAuthorName);
                    Type type = TypeManager.getInstance().getTypeByExtension(Util.getExtension(files[i]));
                    Track track = TrackManager.getInstance().registerTrack(sTrackName,album,style,author,length,lYear,lOrder,type);
                    track.setAdditionDate(new Date());
                    org.jajuk.base.File file = FileManager.getInstance().registerFile(sId,files[i].getName(),this,track, 
                        files[i].length(),lQuality);   
                    //Set file date
                    file.setProperty(XML_FILE_DATE,new Date(lastModified));
                    /*comment is at the track level, note that we take last found file comment but we changing
                    a comment, we will apply to all files for a track*/
                    track.setComment(sComment); 
                }
                else{  //playlist file
                    String sId = PlaylistFileManager.getID(files[i].getName(),this);
                    PlaylistFile plfRef = (PlaylistFile)PlaylistFileManager.getInstance().getItem(sId);
                    //if known playlist file and no deep scan, just leave
                    if (plfRef != null && !bDeepScan){
                        continue;
                    }
                    PlaylistFile plFile = PlaylistFileManager.getInstance().registerPlaylistFile(files[i],this);
                    //set hashcode to this playlist file
                    String sHashcode = plFile.computesHashcode();
                    plFile.forceRefresh(); //force refresh
                    plFile.setHashcode(sHashcode);
                    //create associated playlist
                    PlaylistManager.getInstance().registerPlaylist(plFile);
                    //add playlist file to current directory
                    addPlaylistFile(plFile);
                    if (plfRef == null){
                        device.iNbNewFiles ++;  //stats, do it here and not before because we ignore the file if we cannot read it
                    }
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
     **<p>Sort ignoring cases but different items with different cases should be distinct
     * before being added into bidimap</p>
     *@param other directory to be compared
     *@return comparaison result 
     */
    public int compareTo(Object o){
        Directory otherDirectory = (Directory)o;
        String sAbs = getAbsolutePath();
        String sOtherAbs = otherDirectory.getAbsolutePath();
        if (sAbs.equalsIgnoreCase(sOtherAbs) && !sAbs.equals(sOtherAbs)){
            return sAbs.compareTo(sOtherAbs);
        }
        else{
            return sAbs.compareToIgnoreCase(sOtherAbs);
        }
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
        return Messages.getString("Item_Directory")+" : "+sName; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getHumanValue(java.lang.String)
     */
    public String getHumanValue(String sKey){
        if (XML_DIRECTORY_PARENT.equals(sKey)){
            Directory dParent = (Directory)DirectoryManager.getInstance().getItem((String)getValue(sKey)); 
            if (dParent == null){
              return ""; //no parent directory           //$NON-NLS-1$
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
            return super.getHumanValue(sKey);
        }
    }
    
}
