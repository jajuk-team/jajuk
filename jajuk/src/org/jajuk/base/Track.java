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
 * $Log$
 * Revision 1.6  2003/10/31 13:05:06  bflorat
 * 31/10/2003
 *
 * Revision 1.5  2003/10/28 21:34:37  bflorat
 * 28/10/2003
 *
 * Revision 1.4  2003/10/26 21:28:49  bflorat
 * 26/10/2003
 *
 * Revision 1.3  2003/10/24 15:44:25  bflorat
 * 24/10/2003
 *
 * Revision 1.2  2003/10/23 22:07:40  bflorat
 * 23/10/2003
 *
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;

import org.jajuk.util.Util;

/**
 *  A track
 *<p> Logical item
 * @Author   bflorat
 * @created    17 oct. 2003
 */
public class Track extends PropertyAdapter {

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
	public Track(String sId, String sName,Album album,Style style,Author author,long length,String sYear,Type type,String sAdditionDate) {
		this.sId = sId;
		this.sName = sName;
		this.album = album;
		this.style = style;
		this.author = author;
		this.length = length;
		this.sYear = sYear;
		this.type = type;
		this.sAdditionDate=sAdditionDate;
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
		sb.append(length).append("' year='");//$NON-NLS-1$
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
	public boolean equals(Track otherTrack){
		return this.getId().equals(otherTrack.getId() );
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
