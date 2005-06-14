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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;

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
	private String sYear;
	/**Track rate*/
	private long lRate;
	/**Track type*/
	private Type type;
	/**Track hits number*/
	private int iHits;
	/**Track addition date format:YYYYMMDD*/
	private String sAdditionDate;
	/**Track associated files*/
	private ArrayList alFiles = new ArrayList(1);
	/**Track compare hash for perfs*/
	private String sHashCompare;
	/** Number of hits for current jajuk session */
	private int iSessionHits = 0;
    /**Date format*/
    private static SimpleDateFormat sdf= new SimpleDateFormat(DATE_FILE);
 	
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
	public Track(String sId, String sName,Album album,Style style,Author author,long length,String sYear,Type type) {
        super(sId,sName);
		setAlbum(album);
        setStyle(style);
        setAuthor(author);
        setLength(length);
        setType(type);
        setYear(sYear);
        setRate(0);
        setProperty(XML_FILES,null); //need this to respect attributes order
        setHits(0);
        setAdditionDate(sdf.format(new Date()));
        this.sHashCompare = new StringBuffer(style.getName2()).append(author.getName2()).append(album.getName2()).append(sName).toString();
	}
	
	/**
	 * toString method
	 */
	public String toString() {
		String sOut = "Track[ID="+sId+" Name=" + getName() + " "+album+" "+style+" "+author+" Length="+length+" Year="+sYear+" Rate="+lRate+" "+type+" Hits="+iHits+" Addition date="+sAdditionDate+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
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
	public ArrayList getFiles() {
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
				int iQuality = 0;
				int iQualityOut = 0; //quality for out file
				try {
					iQuality = Integer.parseInt(file.getQuality());
				}
				catch(NumberFormatException nfe){}//quality string can be something like "error", in this case, we considere quality=0
				try{
					iQualityOut = Integer.parseInt(fileOut.getQuality());
				}
				catch(NumberFormatException nfe){}//quality string can be something like "error", in this case, we considere quality=0
				
				if (iQuality > iQualityOut){
					fileOut = file;
				}
			}
		}
		return fileOut;
	}

	
	/**
	 * @return
	 */
	public int getHits() {
		return iHits;
	}

	/**
	 * @return
	 */
	public String getYear() {
		return sYear;
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
		return lRate;
	}

	/**
	 * @return
	 */
	public String getAdditionDate() {
		return sAdditionDate;
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
		return this.getId().equals(((Track)otherTrack).getId() );
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
		alFiles.add(file);	
        String sFiles = file.getId();
        if (this.containsProperty(XML_FILES)){ //already some files 
            sFiles += ","+getProperty(XML_FILES);
        }
        setProperty(XML_FILES,sFiles);
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
	public void setHits(int hits) {
		this.iHits = hits;
        setProperty(XML_TRACK_HITS,Integer.toString(hits));
	}
	
		public void incHits() {
			setHits(getHits()+1);
		}


	/**
	 * @param rate The lRate to set.
	 */
	public void setRate(long rate) {
		this.lRate = rate;
        setProperty(XML_TRACK_RATE,Long.toString(rate));
	}

	

	/**
	 * @param additionDate The sAdditionDate to set.
	 */
	public void setAdditionDate(String additionDate) {
		this.sAdditionDate = additionDate;
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
     * @param album The album to set.
     */
    protected void setAlbum(Album album) {
        this.album = album;
        setProperty(XML_ALBUM,album.getId());
    }

    /**
     * @param author The author to set.
     */
    protected void setAuthor(Author author) {
        this.author = author;
        setProperty(XML_AUTHOR,author.getId());
    }

    /**
     * @param length The length to set.
     */
    protected void setLength(long length) {
        this.length = length;
        setProperty(XML_TRACK_LENGTH,Long.toString(length));
    }

    /**
     * @param style The style to set.
     */
    protected void setStyle(Style style) {
        this.style = style;
        setProperty(XML_STYLE,style.getId());
    }

    /**
     * @param year The sYear to set.
     */
    protected void setYear(String year) {
        sYear = year;
        setProperty(XML_TRACK_YEAR,year);
    }

    /**
     * @param type The type to set.
     */
    protected void setType(Type type) {
        this.type = type;
        setProperty(XML_TYPE,type.getId());
    }

    /**
     * Get item description
     */
    public String getDesc(){
        return "<HTML><b>"+Messages.getString("LogicalTableView.1")+" : "+getName()+"</b><HTML>";
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
        else if (XML_ALBUM.equals(sProperty)){
            return true;
        }
        else if (XML_STYLE.equals(sProperty)){
            return true;
        }
        else if (XML_AUTHOR.equals(sProperty)){
            return true;
        }
        else if (XML_TRACK_LENGTH.equals(sProperty)){
            return false;
        }
        else if (XML_TYPE.equals(sProperty)){
            return false;
        }
        else if (XML_TRACK_YEAR.equals(sProperty)){
            return true;
        }
        else if (XML_TRACK_RATE.equals(sProperty)){
            return true;
        }
        else if (XML_FILES.equals(sProperty)){
            return false;
        }
        else if (XML_TRACK_HITS.equals(sProperty)){
            return true;
        }
        else if (XML_EXPANDED.equals(sProperty)){
            return true;
        }
        else if (XML_TRACK_ADDED.equals(sProperty)){
            return false;
        }
        else{
            return true;
        }
    }
}
