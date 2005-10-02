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
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.jajuk.i18n.Messages;
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
	private Album album;
	/**Track style*/
	private Style style;
	/**Track author in sec*/
	private Author author;
	/**Track length*/
	private long length;
	/**Track year*/
	private long lYear;
	/**Track type*/
	private Type type;
	/**Track addition date format:YYYYMMDD*/
	private Date dAdditionDate;
	/**Track associated files*/
	private ArrayList alFiles = new ArrayList(1);
	/**Track compare hash for perfs*/
	private String sHashCompare;
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
	public Track(String sId,String sName,Album album,Style style,Author author,long length,long lYear,Type type) {
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
            //Rate
            setProperty(XML_TRACK_RATE,0l);
            //Files reset
            setProperty(XML_FILES,null); //need this to respect attributes order
            //Hits
            setProperty(XML_TRACK_HITS,0l);
            //Addition date
            this.dAdditionDate = new Date();
            setProperty(XML_TRACK_ADDED,dAdditionDate);
            //Hashcode
            this.sHashCompare = new StringBuffer(style.getName2()).append(author.getName2()).append(album.getName2()).append(sName).toString();
    }
        
	
	
	/**
	 * toString method
	 */
	public String toString() {
		String sOut = "Track[ID="+sId+" Name=" + getName() + " "+album+" "+
            style+" "+author+" Length="+length+" Year="+lYear+" Rate="+getRate()+" "+
            type+" Hits="+getHits()+" Addition date="+dAdditionDate+" Comment="+getComment()+" order="+getOrder()+"]"; 
		for (int i=0;i<alFiles.size();i++){
			sOut += '\n'+alFiles.get(i).toString();
		}
		return sOut; 
	}

	/**
	 *Alphabetical comparator used to display ordered lists of tracks
	 *@param other track to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
		Track otherTrack = (Track)o;
        //if track # is given, sort by # in a same album, otherwise, sort alphabeticaly
        if (otherTrack.getAlbum().equals(album) && (getOrder() != otherTrack.getOrder()) ){
            return (int)(getOrder() - otherTrack.getOrder()); 
        }
        return  sHashCompare.compareToIgnoreCase(otherTrack.getHashCompare());
	}
	
	/**
	 * @return
	 */
	public Album getAlbum() {
		return album;
	}

	/**
	 * @return
	 */
	public ArrayList<File> getFiles() {
		return alFiles;
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
     * @param bHideUnmounted : get even unmounted files?
     * @return best file to play for this track
     */
    public File getPlayeableFile(boolean bHideUnmounted) {
        File file = getPlayeableFile();
        if (file == null && !bHideUnmounted){
            file = (File)getFiles().get(0); //take the first file we find
        }
        return file;
    }
    
    /**
	 * @return best file to play for this track
	 */
	public File getPlayeableFile() {
		File fileOut = null;
		ArrayList alMountedFiles = new ArrayList(2);
		//firstly, keep only mounted files
		Iterator it = alFiles.iterator();
		while ( it.hasNext()){
			File file = (File)it.next();
			if (file.isReady()){
				alMountedFiles.add(file);
			}
		}
		//then keep best quality
		if (alMountedFiles.size() > 0){
			it = alMountedFiles.iterator();
			fileOut = (File)it.next();  //for the moment, the out file is the first found
			while ( it.hasNext()){
				File file = (File)it.next();
				long lQuality = 0;
				long lQualityOut = 0; //quality for out file
				try {
					lQuality = file.getQuality();
				}
				catch(NumberFormatException nfe){}//quality string can be something like "error", in this case, we considere quality=0
				try{
					lQualityOut = fileOut.getQuality();
				}
				catch(NumberFormatException nfe){}//quality string can be something like "error", in this case, we considere quality=0
				
				if (lQuality > lQualityOut){
					fileOut = file;
				}
			}
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
	 * @return
	 */
	public Date getAdditionDate() {
		return dAdditionDate;
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
			String sFiles = file.getId();
			if (this.containsProperty(XML_FILES)){ //already some files 
				sFiles += ","+getValue(XML_FILES);
			}
			setProperty(XML_FILES,sFiles);
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
	}

    /**
     * @param order to set
     */
    public void setOrder(long lOrder) {
        setProperty(XML_TRACK_ORDER,lOrder);
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
		this.dAdditionDate = additionDate;
        setProperty(XML_TRACK_ADDED,additionDate);
	}

	/**
	 * @return Returns the sHashCompare.
	 */
	public String getHashCompare() {
		return sHashCompare;
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
		if (getPlayeableFile() != null
			 ||ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED) == false){ //option "only display mounted devices "
			return false;
		}
		return true;
	}

    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getIdentifier()
     */
    public String getIdentifier() {
        return XML_TRACK;
    }

    /**
     * Get item description
     */
    public String getDesc(){
        return Messages.getString("Item_Track")+" : "+getName();
    }

   
    
/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getHumanValue(java.lang.String)
     */
    public String getHumanValue(String sKey){
        if (XML_ALBUM.equals(sKey)){
            return ((Album)AlbumManager.getInstance().getItem(getStringValue(sKey))).getName2();
        }
        else if (XML_AUTHOR.equals(sKey)){
            return ((Author)AuthorManager.getInstance().getItem(getStringValue(sKey))).getName2();
        }
        else if (XML_STYLE.equals(sKey)){
            return ((Style)StyleManager.getInstance().getItem(getStringValue(sKey))).getName2();
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
                sbOut.append(file.getAbsolutePath()+",");
            }
            return sbOut.substring(0,sbOut.length()-1); //remove last ','
        }
        else if (XML_TRACK_ADDED.equals(sKey)){
            DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT,Locale.getDefault());
            return dateFormatter.format(dAdditionDate);
        }
        else if (XML_ANY.equals(sKey)){
            return getAny();
        }
        else{//default
            return super.getHumanValue(sKey);
        }
    }
    
}
