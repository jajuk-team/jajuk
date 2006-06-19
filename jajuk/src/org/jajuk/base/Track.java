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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.IconLabel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;

/**
 *  A track
 *<p> Logical item
 * @Author   Bertrand Florat
 * @created    17 oct. 2003
 */
public class Track extends PropertyAdapter implements Comparable{
    
    /**Track album**/
    private final Album album;
    /**Track style*/
    private final Style style;
    /**Track author in sec*/
    private final Author author;
    /**Track length*/
    private final long length;
    /**Track year*/
    private final long lYear;
    /**Track order*/
    private final long lOrder;
    /**Track type*/
    private final Type type;
    /**Track associated files*/
    private ArrayList<File> alFiles = new ArrayList(1);
    /** Number of hits for current jajuk session */
    private int iSessionHits = 0;
    
    
    /**
     *  Track constructor
     * @param sId
     * @param sName
     * @param album
     * @param style
     * @param author
     * @param length
     * @param sYear
     * @param type
     * @param sAdditionDate
     */
    public Track(String sId,String sName,Album album,Style style,Author author,long length,long lYear,long lOrder,Type type) {
        super(sId,sName);
        //album
        this.album = album;
        setProperty(XML_ALBUM,album.getId());
        //style
        this.style = style;
        setProperty(XML_STYLE,style.getId());
        //author
        this.author = author;
        setProperty(XML_AUTHOR,author.getId());
        //Length
        this.length = length;
        setProperty(XML_TRACK_LENGTH,length);
        //Type
        this.type = type;
        setProperty(XML_TYPE,type.getId());
        //Year
        this.lYear = lYear;
        setProperty(XML_TRACK_YEAR,lYear);
        //Order
        this.lOrder = lOrder;
        setProperty(XML_TRACK_ORDER,lOrder);
        //Rate
        setProperty(XML_TRACK_RATE,0l);
        //Hits
        setProperty(XML_TRACK_HITS,0l);
    }
    
    /**
     * toString method
     */
    public String toString() {
        String sOut = "Track[ID="+sId+" Name={{" + getName() + "}} "+album+" "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        style+" "+author+" Length="+length+" Year="+lYear+" Rate="+getRate()+" "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        type+" Hits="+getHits()+" Addition date="+getAdditionDate()+" Comment="+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        getComment()+" order="+getOrder()+ " Nb of files="+alFiles.size()+"]";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        for (int i=0;i<alFiles.size();i++){
            sOut += '\n'+alFiles.get(i).toString();
        }
        return sOut; 
    }
    
    /**
     *Default comparator for tracks, not used for sorting (use TrackComparator for that)
     *But only for storage. We must make sure of unicity inside bidimap
     *@param other track to be compared
     *@return comparaison result 
     */
    public int compareTo(Object o){
        Track otherTrack = (Track)o;
        return getId().compareTo(otherTrack.getId());
    }
    
    /**
     * @return
     */
    public Album getAlbum() {
        return album;
    }
    
    /**
     * @return all associated files
     */
    public ArrayList<org.jajuk.base.File> getFiles() {
        return alFiles;
    }
    
    /**
     * @return ready files
     */
    public ArrayList<File> getReadyFiles() {
        ArrayList alReadyFiles = new ArrayList(alFiles.size());
        for (File file:alFiles){
            if (file.isReady()){
                alReadyFiles.add(file);
            }
        }
        return alReadyFiles;
    }
    
    /**
     * @return ready files with given filter
     * @param filter files we want to deal with, null means no filter
     */
    public ArrayList<File> getReadyFiles(HashSet filter) {
        ArrayList alReadyFiles = new ArrayList(alFiles.size());
        for (File file:alFiles){
            if (file.isReady() && 
                    (filter == null || filter.contains(file))){
                alReadyFiles.add(file);
            }
        }
        return alReadyFiles;
    }
    
    /**
     * Get additionned size of all files this track map to
     * @return the total size
     */
    public long getTotalSize(){
        long l = 0;
        Iterator it = alFiles.iterator();
        while ( it.hasNext()){
            File file = (File)it.next();
            l += file.lSize;
        }
        return l;
    }
    
    
     /**
     * @return best file to play for this track
     * bHideUnmounted
     */
    public File getPlayeableFile(boolean bHideUnmounted) {
        File fileOut = null;
        ArrayList<File> alMountedFiles = new ArrayList(2);
        //firstly, filter mounted files if needed
        Iterator it = alFiles.iterator();
        while ( it.hasNext()){
            File file = (File)it.next();
            if (!bHideUnmounted || file.isReady()){
                alMountedFiles.add(file);
            }
        }
        if (alMountedFiles.size() == 1){
            fileOut = alMountedFiles.get(0);
        }
        else if (alMountedFiles.size() > 0){
            //then keep best quality and mounted first
            Collections.sort(alMountedFiles,new Comparator() {
                public int compare(Object arg0, Object arg1) {
                    File file1 = (File)arg0;
                    long lQuality1 = file1.getQuality();
                    boolean bMounted1 = file1.isReady();
                    File file2 = (File)arg1;
                    long lQuality2 = file2.getQuality(); //quality for out file
                    boolean bMounted2 = file2.isReady();
                    if (bMounted1 && !bMounted2){//first item mounted, not second
                        return 1;
                    }
                    else if (!bMounted1 && bMounted2){ //second mounted, not the first
                        return -1;
                    }
                    else{ //both mounted or unmounted, compare quality
                        return (int)(lQuality1 - lQuality2);  
                    }
                }
            });
            fileOut = alMountedFiles.get(alMountedFiles.size()-1); //highest score last
        }
        return fileOut;
    }
    
    
    /**
     * @return
     */
    public long getHits() {
        return getLongValue(XML_TRACK_HITS);
    }
    
    /**
     * @return
     */
    public String getComment() {
        return getStringValue(XML_TRACK_COMMENT);
    }
    
    /**
     * Get track number
     * @return
     */
    public long getOrder(){
        return getLongValue(XML_TRACK_ORDER);
    }
    
    /**
     * @return
     */
    public long getYear() {
        return lYear;
    }
    
    
    /**
     * @return length in sec
     */
    public long getLength() {
        return length;
    }
    
    /**
     * @return
     */
    public long getRate() {
        return getLongValue(XML_TRACK_RATE);
    }
    
    /**
     * @return Number of stars
     */
    public int getStarsNumber() {
        long lRate = getRate();
        long lInterval = TrackManager.getInstance().getMaxRate() / 4;    
        if (lRate <= lInterval){
            return 1;
        }
        else if (lRate <= 2*lInterval){
            return 2;
        }
        else if (lRate <= 3*lInterval){
            return 3;
        }
        else {
            return 4;
        }
    }
    
    /**
     * @return the stars icon
     */
    public IconLabel getStars() {
        IconLabel ilRate = null;
        long lRate = getRate();
        switch(getStarsNumber()){
        case 1:
            ilRate = new IconLabel(Util.getIcon(ICON_STAR_1),"",null,null,null,Long.toString(getRate()));
            break;
        case 2:
            ilRate = new IconLabel(Util.getIcon(ICON_STAR_2),"",null,null,null,Long.toString(getRate()));
            break;
        case 3:
            ilRate = new IconLabel(Util.getIcon(ICON_STAR_3),"",null,null,null,Long.toString(getRate()));
            break;
        case 4:
            ilRate = new IconLabel(Util.getIcon(ICON_STAR_4),"",null,null,null,Long.toString(getRate()));
            break;
        }
        ilRate.setInteger(true);
        return ilRate;
    }
    
    /**
     * @return
     */
    public Date getAdditionDate() {
        return getDateValue(XML_TRACK_ADDED);
    }
    
    /**
     * @return
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Equal method to check two tracks are identical
     * @param otherTrack
     * @return
     */
    public boolean equals(Object otherTrack){
        if (otherTrack == null){
            return false;
        }
        return this.getId().equals(((Track)otherTrack).getId());
    }	
    
    /**
     * Track hashcode ( used by the equals method )
     */
    public int hashCode(){
        return getId().hashCode();
    }
    
    
    /**
     * @return
     */
    public Author getAuthor() {
        return author;
    }
    
    /**
     * @return
     */
    public Style getStyle() {
        return style;
    }
    
    /**
     * Add an associated file 
     * @param file
     */
    public void addFile(File file){
        if (!alFiles.contains(file) && file.getTrack().equals(this)){//make sure a file will be referenced by only one track (first found)
            alFiles.add(file);	
        }
    }
    
    /**
     * Remove an associated file 
     * @param file
     */
    public void removeFile(File file){
        alFiles.remove(file);
    }
    
    
    /**
     * @param hits The iHits to set.
     */
    public void setHits(long hits) {
        setProperty(XML_TRACK_HITS,hits);
    }
    
    public void incHits() {
        setHits(getHits()+1);
    }
    
    /**
     * @param rate The lRate to set.
     */
    public void setRate(long rate) {
        setProperty(XML_TRACK_RATE,rate);
        //Store max rate
        if (rate > TrackManager.getInstance().getMaxRate()){
            TrackManager.getInstance().setMaxRate(rate);
        }
    }
    
    /**
     * @param rate The lRate to set.
     */
    public void setComment(String sComment) {
        setProperty(XML_TRACK_COMMENT,sComment);
    }	
    
    /**
     * @param additionDate The sAdditionDate to set.
     */
    public void setAdditionDate(Date additionDate) {
        setProperty(XML_TRACK_ADDED,additionDate);
    }
    
    /**
     * @return Returns the iSessionHits.
     */
    public int getSessionHits() {
        return iSessionHits;
    }
    
    /**
     * @param sessionHits The iSessionHits to inc.
     */
    public void incSessionHits() {
        iSessionHits ++;
    }
    
    /**
     * Return whether this item should be hidden with hide option
     * @return whether this item should be hidden with hide option
     */
    public boolean shouldBeHidden(){
        if (getPlayeableFile(true) != null
                || ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED) == false){ //option "only display mounted devices "
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getIdentifier()
     */
    final public String getIdentifier() {
        return XML_TRACK;
    }
    
    /**
     * Get item description
     */
    public String getDesc(){
        return Messages.getString("Item_Track")+" : "+getName(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getHumanValue(java.lang.String)
     */
    public String getHumanValue(String sKey){
        if (XML_ALBUM.equals(sKey)){
            Album album = (Album)AlbumManager.getInstance().getItem(getStringValue(sKey));
            if (album != null){ //can be null after a fresh change
                return album.getName2();
            }
            return null;
        }
        else if (XML_AUTHOR.equals(sKey)){
            Author author = (Author)AuthorManager.getInstance().getItem(getStringValue(sKey));
            if (author != null){ //can be null after a fresh change
                return author.getName2();
            }
            return null;
        }
        else if (XML_STYLE.equals(sKey)){
            Style style = (Style)StyleManager.getInstance().getItem(getStringValue(sKey));
            if (style != null){ //can be null after a fresh change
                return style.getName2();
            }
            return null;
        }
        else if (XML_TRACK_LENGTH.equals(sKey)){
            return Util.formatTimeBySec(length,false);
        }
        else if (XML_TYPE.equals(sKey)){
            return ((Type)TypeManager.getInstance().getItem(getStringValue(sKey))).getName();
        }
        else if (XML_TRACK_YEAR.equals(sKey)){
            return Long.toString(lYear);
        }
        else if (XML_FILES.equals(sKey)){
            StringBuffer sbOut = new StringBuffer();
            Iterator it = alFiles.iterator();
            while (it.hasNext()){
                File file = (File)it.next();
                sbOut.append(file.getAbsolutePath()+","); //$NON-NLS-1$
            }
            return sbOut.substring(0,sbOut.length()-1); //remove last ','
        }
        else if (XML_TRACK_ADDED.equals(sKey)){
            DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT,Locale.getDefault());
            return dateFormatter.format(getAdditionDate());
        }
        else if (XML_ANY.equals(sKey)){
            return getAny();
        }
        else{//default
            return super.getHumanValue(sKey);
        }
    }
    
    
    
}
