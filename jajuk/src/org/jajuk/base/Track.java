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

import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.util.Util;

/**
 *  A track
 *<p> Logical item
 * @Author   bflorat
 * @created    17 oct. 2003
 */
public class Track extends PropertyAdapter implements Comparable{

	/** Track ID. Ex:1,2,3...*/
	private String sId;
	/**Track name */
	private String sName;
	/**Track album**/
	private Album album;
	/**Track style*/
	private Style style;
	/**Track author*/
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
		this.sId = sId;
		this.sName = sName;
		this.album = album;
		this.style = style;
		this.author = author;
		this.length = length;
		this.sYear = sYear;
		this.type = type;
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return sName;
	}

	/**
	 * toString method
	 */
	public String toString() {
		String sOut = "Track[ID="+sId+" Name=" + getName() + " "+album+" "+style+" "+author+" Length="+length+" Year="+sYear+" Rate="+lRate+" "+type+" Hits="+iHits+" Addition date="+sAdditionDate+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0;i<alFiles.size();i++){
			sOut += '\n'+alFiles.get(i).toString();
		}
		return sOut; 
	}
	
	
	public Object clone(){
		Track track = new Track(sId,sName,album,style,author,length,sYear,type);
		track.setAdditionDate(sAdditionDate);
		track.setRate(lRate);
		track.setHits(iHits);
		track.setProperties(getProperties());
		track.alFiles = (ArrayList)alFiles.clone();
		return track;
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<track id='" + sId);//$NON-NLS-1$
		sb.append("' name='");//$NON-NLS-1$
		sb.append(Util.formatXML(sName)).append("' album='");//$NON-NLS-1$
		sb.append(album.getId()).append("' style='");//$NON-NLS-1$
		sb.append(style.getId()).append("' author='");//$NON-NLS-1$
		sb.append(author.getId()).append("' length='");//$NON-NLS-1$
		sb.append(length).append("' type='");//$NON-NLS-1$
		sb.append(type.getId()).append("' year='");//$NON-NLS-1$
		sb.append(sYear).append("' rate='");//$NON-NLS-1$
		sb.append(lRate).append("' files='");//$NON-NLS-1$
		for (int i=0;i<alFiles.size();i++){
			sb.append(((File)alFiles.get(i)).getId()).append(",");//$NON-NLS-1$
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("' hits='");//$NON-NLS-1$
		sb.append(iHits).append("' added='");//$NON-NLS-1$
		sb.append(sAdditionDate).append("' ");//$NON-NLS-1$
		sb.append(getPropertiesXml());
		sb.append("/>\n");//$NON-NLS-1$
		return sb.toString();
	}

	/**
	* @return
	 */
	public String getId() {
		return sId;
	}

	
	/**
	 *Alphabetical comparator used to display ordered lists of tracks
	 *@param other track to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
		Track otherTrack = (Track)o;
		StringBuffer sbCuurent = new StringBuffer(style.getName2()).append(author.getName2()).append(album.getName2()).append(sName);
		StringBuffer sbOther = new StringBuffer(otherTrack.getStyle().getName2()).append(otherTrack.getAuthor().getName2()).append(otherTrack.getAlbum().getName2()).append(otherTrack.getName());
		return  sbCuurent.toString().compareToIgnoreCase(sbOther.toString());
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
	 * @return best file to play for this track
	 */
	public File getPlayeableFile() {
		File fileOut = null;
		ArrayList alMountedFiles = new ArrayList(2);
		//firstly, keep only mounted files
		Iterator it = alFiles.iterator();
		while ( it.hasNext()){
			File file = (File)it.next();
			if (file.getDirectory().getDevice().isMounted()){
				alMountedFiles.add(file);
			}
		}
		//then keep best quality
		it = alMountedFiles.iterator();
		while ( it.hasNext()){
			File file = (File)it.next();
			if (fileOut == null || Integer.parseInt(file.getQuality()) > Integer.parseInt(fileOut.getQuality())){
				fileOut = file;
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
	 * @return
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
		iHits = hits;
	}
	
		public void incHits() {
			iHits++;
		}


	/**
	 * @param rate The lRate to set.
	 */
	public void setRate(long rate) {
		lRate = rate;
	}

	

	/**
	 * @param additionDate The sAdditionDate to set.
	 */
	public void setAdditionDate(String additionDate) {
		sAdditionDate = additionDate;
	}

	

}
