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
 * $Revision$
 */
package org.jajuk.base;

import org.jajuk.util.Util;


/**
 *  A music file to be played
 *<p> Physical item
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class File extends PropertyAdapter{
	/** author ID. Ex:1,2,3...*/
	protected String sId;
	/**File name */
	protected String sName;
	/**Parent directory*/
	protected Directory directory;
	/**Associated track */
	protected Track track;
	/**File size in bytes*/
	protected long lSize;
	/**File quality. Ex: 192 for 192kb/s*/
	protected String sQuality;
	
	
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
		this.sId = sId;
		this.sName = sName;
		this.directory = directory;
		this.track = track;
		this.lSize = lSize;
		this.sQuality = sQuality;
	}
	
	
	/**Void constructor*/
	public File(){
	}
	
	/**
	 * toString method
	 */
	public String toString() {
		return "File[ID="+sId+" Name=" + sName + " Dir="+directory+" Size="+lSize+" Quality="+sQuality+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$
	}
	
	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<file id='" + sId);//$NON-NLS-1$
		sb.append("' name='");//$NON-NLS-1$
		sb.append(Util.formatXML(sName)).append("' directory='");//$NON-NLS-1$
		sb.append(directory.getId()).append("' track='");//$NON-NLS-1$
		sb.append(track.getId()).append("' size='");//$NON-NLS-1$
		sb.append(lSize).append("' quality='");//$NON-NLS-1$
		sb.append(sQuality).append("' ");//$NON-NLS-1$
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
	public long getSize() {
		return lSize;
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
	 * Return full file path name
	 * @return String
	 */
	public String getAbsolutePath(){
		Directory dCurrent = getDirectory();
		StringBuffer sbOut = new StringBuffer(this.getName());
		do{
			if (dCurrent == null || dCurrent.getName().equals("")){  //if it is the root directory, no parent
				break;
			}
			sbOut.insert(0,'/');
			sbOut.insert(0,dCurrent.getName());
			dCurrent = dCurrent.getParentDirectory();
		}
		while(true);
		sbOut.insert(0,'/');
		sbOut.insert(0,getDirectory().getDevice().getUrl());
		return sbOut.toString();
	}
	
	
}
