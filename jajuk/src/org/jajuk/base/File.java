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

import java.util.Iterator;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;


/**
 *  A music file to be played
 *<p> Physical item
 * @author     Bertrand Florat
 * @created    12 oct. 2003
 */
public class File extends PropertyAdapter implements Comparable,ITechnicalStrings{
	/**Parent directory*/
	protected Directory directory;
	/**Associated track */
	protected Track track;
	/**File size in bytes*/
	protected long lSize;
	/**File quality. Ex: 192 for 192kb/s*/
	protected String sQuality;
	/** pre-calculated absolute path for perf*/
	private String sAbs = null;
	/** IO file associated with this file*/
	private java.io.File fio;
    /**Flag used to sort by date (default=sort alphabeticaly)*/
    static private boolean bSortByDate = false;
    
	/**
	 * File instanciation 
	 * @param sId
	 * @param sName
	 * @param directory
	 * @param track
	 * @param lSize
	 * @param sQuality
	 */
	public File(String sId,String sName,Directory directory,Track track,long lSize,String sQuality) {
        super(sId,sName);
        this.directory = directory;
        setProperty(XML_DIRECTORY,directory.getId());
        alConstructorElements.add(XML_DIRECTORY);
        
        this.track = track;
        setProperty(XML_TRACK,track.getId());
        alConstructorElements.add(XML_TRACK);
        
        this.lSize = lSize;
        setProperty(XML_SIZE,Long.toString(lSize));
        alConstructorElements.add(XML_SIZE);
        
        this.sQuality = sQuality;
        setProperty(XML_QUALITY,sQuality);
        alConstructorElements.add(XML_QUALITY);
        
	}
		
/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getIdentifier()
     */
    public String getIdentifier() {
        return XML_FILE;
    }
    
    /**
	 * toString method
	 */
	public String toString() {
		return "File[ID="+sId+" Name=" + sName + " Dir="+directory+" Size="+lSize+" Quality="+sQuality+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$
	}
	
	/**
	 * String representation as displayed in a search result 
	 */
	public String toStringSearch() {
		StringBuffer sb = new StringBuffer(track.getStyle().getName2()).append('/').append(track.getAuthor().getName2()).append('/').
			append(track.getAlbum().getName2()).append('/').append(track.getName()).append(" [").append(directory.getName()).append('/').append(this.sName).append(']'); //$NON-NLS-1$
		return sb.toString();
	}
	
	/**
	 * Return true is the specified directory is an ancestor for this file
	 * @param directory
	 * @return
	 */
	public boolean hasAncestor(Directory directory){
		Directory dirTested = getDirectory();
		while (true){
			if ( dirTested.equals(directory)){
				return true;
			}
			else{
				dirTested = dirTested.getParentDirectory();
				if (dirTested == null ){
					return false;
				}
			}
		}
	}
	
	/**
	 * @return
	 */
	public long getSize() {
		return lSize;
	}
		
	/**
	 * @return
	 */
	public Directory getDirectory() {
		return directory;
	}
	
	/**
	 * @return
	 */
	public String getQuality() {
		return sQuality;
	}
	
	/**
	 * @return user-formatted quality string
	 */
	public String getQuality2() {
		if (UNKNOWN_QUALITY.equals(sQuality)){
		    return Messages.getString(UNKNOWN_QUALITY);
		}
	    return sQuality;
	}
	/**
	 * @return
	 */
	public Track getTrack() {
		return track;
	}
	
	/**
	 * Equal method to check two files are identical
	 * @param otherFile
	 * @return
	 */
	public boolean equals(Object otherFile){
		return this.getId().equals(((File)otherFile).getId() );
	}	
	
	
	/**
	 * hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
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
	 *Alphabetical comparator used to display ordered lists of files
	 *@param other file to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
        File otherFile = (File)o;
        if (bSortByDate){ //sort by date, last first
            return  otherFile.getTrack().getAdditionDate().compareTo(getTrack().getAdditionDate());            
        }
        else{ //default, sort alphabeticaly
            return  getAbsolutePath().compareToIgnoreCase(otherFile.getAbsolutePath());            
        }
	}
	
	/**Return true if the file can be accessed right now 
	 * @return true the file can be accessed right now*/
	public boolean isReady(){
		if ( getDirectory().getDevice().isMounted() && !getDirectory().getDevice().isRefreshing() && !getDirectory().getDevice().isSynchronizing()){
			return true;
		}
		return false;
	}
	
	/**Return true if the file is currently refreshed or synchronized 
	 * @return true if the file is currently refreshed or synchronized*/
	public boolean isScanned(){
		if ( getDirectory().getDevice().isRefreshing() || getDirectory().getDevice().isSynchronizing()){
			return true;
		}
		return false;
	}
	
	/**
	 * Return Io file associated with this file
	 * @return
	 */
	public java.io.File getIO(){
		if ( fio == null){
			fio = new java.io.File(getAbsolutePath());
		}
		return fio;
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
	 * Clone method
	 * @return a clonned file
	 */
	public Object clone(){
		File fClone = new File(sId,sName,directory,track,lSize,sQuality);
		fClone.fio = fio;
		fClone.sAbs = sAbs;
		return fClone;
	}

    /**
     * @return Returns the bSortByDate.
     */
    public static boolean isSortedByDate() {
        return bSortByDate;
    }

    /**
     * @param sortByDate The bSortByDate to set.
     */
    public static void setSortByDate(boolean sortByDate) {
        bSortByDate = sortByDate;
    }

    /**
     * @param directory The directory to set.
     */
    public void setDirectory(Directory directory) {
        this.directory = directory;
        setProperty(XML_DIRECTORY,directory.getId());
    }

    /**
     * @param size The lSize to set.
     */
    public void setSize(long size) {
        this.lSize = size;
        setProperty(XML_SIZE,Long.toString(size));
    }

    
    /**
     * @param track The track to set.
     */
    public void setTrack(Track track) {
        this.track = track;
        setProperty(XML_TRACK,track.getId());
    }
	
    /**
     * Get item description
     */
    public String getDesc(){
        return Util.formatPropertyDesc(Messages.getString("Item_File")+" : "+getName());
    }
    
/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#isPropertyEditable()
     */
    public boolean isPropertyEditable(String sProperty){
        if (XML_ID.equals(sProperty)){
            return false;
        }
        else if (XML_NAME.equals(sProperty)){
            return true;
        }
        else if (XML_DIRECTORY.equals(sProperty)){
            return false;
        }
        else if (XML_TRACK.equals(sProperty)){
            return false;
        }
        else if (XML_SIZE.equals(sProperty)){
            return false;
        }
        else if (XML_QUALITY.equals(sProperty)){
            return false;
        }
        else if (XML_EXPANDED.equals(sProperty)){
            return true;
        }
         else{
            return true;
        }
    }  
    
/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getHumanValue(java.lang.String)
     */
    public String getHumanValue(String sKey){
        if (XML_DIRECTORY.equals(sKey)){
            Directory dParent = DirectoryManager.getDirectory(getValue(sKey)); 
            return dParent.getFio().getAbsolutePath();
        }
        else if (XML_TRACK.equals(sKey)){
            return getTrack().getName();
        }
        else if (XML_SIZE.equals(sKey)){
            return (lSize/1048576)+Messages.getString("PhysicalTreeView.54");
        }
        else if (XML_QUALITY.equals(sKey)){
            return getQuality2()+Messages.getString("FIFO.13");
        }
        else if (XML_ALBUM.equals(sKey)){
            return getTrack().getAlbum().getName2();
        }
        else if (XML_STYLE.equals(sKey)){
            return getTrack().getStyle().getName2();
        }
        else if (XML_AUTHOR.equals(sKey)){
            return getTrack().getAuthor().getName2();
        }
        else if (XML_TRACK_LENGTH.equals(sKey)){
            return Util.formatTimeBySec(getTrack().getLength(),false);
        }
        else if (XML_TRACK_RATE.equals(sKey)){
            return Long.toString(getTrack().getRate());
        }
        else if (XML_DEVICE.equals(sKey)){
            return getDirectory().getDevice().getName();
        }
        else if (XML_ANY.equals(sKey)){
            return getAny();
        }
        else{//default
            return getValue(sKey);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getAny()
     */
    public String getAny(){
        if (bNeedRefresh){
            //rebuild any
            StringBuffer sb  = new StringBuffer();
            File file = (File)this;
            Track track = file.getTrack();
            sb.append(file.getName());
            sb.append(file.getDirectory().getDevice().getName());
            sb.append(track.getName());
            sb.append(track.getStyle().getName2());
            sb.append(track.getAuthor().getName2());
            sb.append(track.getAlbum().getName2());
            sb.append(track.getLength());
            sb.append(track.getRate());
            sb.append(track.getValue(XML_COMMENT));//custom properties now
            Iterator it = FileManager.getInstance().getCustomProperties().iterator();
            while (it.hasNext()){
                sb.append((String)it.next());
            }
            this.sAny = sb.toString();
            bNeedRefresh = false;
        }
        return this.sAny;
    }
  
}
