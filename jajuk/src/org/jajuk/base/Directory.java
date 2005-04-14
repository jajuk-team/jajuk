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
    
    /** ID. Ex:1,2,3... */
    private String sId;
    /** directory name. Ex: rock */
    private String sName;
    /** Parent directory ID* */
    private Directory dParent;
    /** Directory device */
    private Device device;
    /** Child directories */
    private ArrayList alDirectories = new ArrayList(20);
    /** Child files */
    private ArrayList alFiles = new ArrayList(20);
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
        this.sId = sId;
        this.sName = sName;
        this.dParent = dParent;
        this.device = device;
        this.fio = new File(device.getUrl() + getRelativePath());
    }
    
    /**
     * toString method
     */
    public String toString() {
        return "Directory[ID=" + sId + " Name=" + getRelativePath() + " Parent ID=" + (dParent == null ? "null" : dParent.getId()) + " Device=" + device.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
    }
    
    /**
     * Return an XML representation of this item
     * 
     * @return
     */
    public String toXml() {
        StringBuffer sb = new StringBuffer("\t\t<directory id='" + sId); //$NON-NLS-1$
        sb.append("' name='"); //$NON-NLS-1$
        sb.append(Util.formatXML(sName));
        sb.append("' parent='"); //$NON-NLS-1$
        String sParent = "-1"; //$NON-NLS-1$
        if (dParent!=null){
            sParent = dParent.getId();
        }
        sb.append(sParent);
        sb.append("' device='"); //$NON-NLS-1$
        sb.append(device.getId()).append("' "); //$NON-NLS-1$
        sb.append(getPropertiesXml());
        sb.append("/>\n"); //$NON-NLS-1$
        return sb.toString();
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
    public Directory getParentDirectory() {
        return dParent;
    }
    
    /**
     * @return
     */
    public ArrayList getDirectories() {
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
    public ArrayList getFiles() {
        return alFiles;
    }
    
    /**
     * return child files recursively
     * @return child files recursively
     */
    public ArrayList getFilesRecursively() {
        ArrayList alFiles = new ArrayList(100);
        Iterator it = FileManager.getFiles().iterator();
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
                boolean bIsMusic = Boolean.valueOf(TypeManager.getTypeByExtension(Util.getExtension(files[i])).getProperty(TYPE_PROPERTY_IS_MUSIC)).booleanValue();
                if (bIsMusic) {
                    //check the file is not already known in old database
                    org.jajuk.base.File fileRef = null;
                    String sId = MD5Processor.hash(new StringBuffer(getDevice().getName()).append(getDevice().getUrl()).append(getRelativePath()).append(files[i].getName()).toString());
                    Iterator it = TrackManager.getTracks().iterator();
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
                        org.jajuk.base.File file = FileManager.registerFile(fileRef.getId(),fileRef.getName(), this, fileRef.getTrack(), fileRef.getSize(),fileRef.getQuality());
                        addFile(file);
                        continue;
                    }
                    Tag tag = new Tag(files[i]);
                    String sTrackName = tag.getTrackName();
                    String sAlbumName = tag.getAlbumName();
                    String sAuthorName = tag.getAuthorName();
                    String sStyle = tag.getStyleName();
                    long length = tag.getLength(); //length in sec
                    String sYear = tag.getYear();
                    String sQuality = tag.getQuality();
                    
                    Album album = AlbumManager.registerAlbum(sAlbumName);
                    Style style = StyleManager.registerStyle(sStyle);
                    Author author = AuthorManager.registerAuthor(sAuthorName);
                    Type type = TypeManager.getTypeByExtension(Util.getExtension(files[i]));
                    track = TrackManager.registerTrack(sTrackName, album, style, author, length, sYear, type);
                    org.jajuk.base.File newFile = FileManager.registerFile(sId,files[i].getName(), this, track, files[i].length(), sQuality);
                    addFile(newFile);
                    track.addFile(newFile);
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
                    PlaylistFile plFile = PlaylistFileManager.registerPlaylistFile(sId,sName,sHashcode,this);
                    PlaylistManager.registerPlaylist(plFile);
                }
            }
            catch(Exception e){ 
                Log.error("103",files.length>0?files[i].toString():"",e);
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
    
}
