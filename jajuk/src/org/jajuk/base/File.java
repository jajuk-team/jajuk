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
 * Revision 1.3  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 * Revision 1.2  2003/10/17 20:36:45  bflorat
 * 17/10/2003
 *
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.base;


/**
 *  A music file to be played
 *<p> Physical item
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class File extends PropertyAdapter{
	/** author ID. Ex:1,2,3...*/
	private String sId;
	/**File name */
	private String sName;
	/**Parent directory*/
	private Directory directory;
	/**Associated track */
	private Track track;
	/**File size in bytes*/
	private String size;
	/**File quality. Ex: 192 for 192kb/s*/
	private String sQuality;
	
	
	/**
	 * File instanciation 
	 * @param sId
	 * @param sName
	 * @param directory
	 * @param track
	 * @param size
	 * @param sQuality
	 */
	public File(String sId,String sName,Directory directory,Track track,String size,String sQuality) {
		this.sId = sId;
		this.sName = sName;
		this.directory = directory;
		this.track = track;
		this.size = size;
		this.sQuality = sQuality;
	}

	
	/**
		 * toString method
		 */
		public String toString() {
			return "File[ID="+sId+" Name=" + sName + " Dir="+directory+" Size="+size+" Quality="+sQuality+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$
		}

		/**
		 * Return an XML representation of this item  
		 * @return
		 */
		public String toXml() {
			StringBuffer sb = new StringBuffer("\t\t<file id='" + sId);//$NON-NLS-1$
			sb.append("' name=' ");//$NON-NLS-1$
			sb.append(sName).append("' ");//$NON-NLS-1$
			sb.append("' directory=' ");//$NON-NLS-1$
			sb.append(directory).append("' ");//$NON-NLS-1$
			sb.append("' track=' ");//$NON-NLS-1$
			sb.append(track.getId()).append("' ");//$NON-NLS-1$
			sb.append("' size=' ");//$NON-NLS-1$
			sb.append(size).append("' ");//$NON-NLS-1$
			sb.append("' quality=' ");//$NON-NLS-1$
			sb.append(sQuality).append("' ");//$NON-NLS-1$
			sb.append(getPropertiesXml());
			sb.append(sName).append("/>\n");//$NON-NLS-1$
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
	public String getSize() {
		return size;
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
		public boolean equals(File otherFile){
			if ( this.getName().equals(otherFile.getName())
			&& (this.getDirectory().equals(otherFile.getDirectory()))){
					return true;
			}
			return false;
		}	
		
	/**
		 * Return full file path name
		 * @param file
		 * @return String
		 */
		public String getAbsolutePath(){
			Directory dCurrent = getDirectory();
			StringBuffer sbOut = new StringBuffer(dCurrent.getDevice().getName());
			sbOut.append(java.io.File.separatorChar).append(dCurrent.getName());
			boolean bTop = false;
			while (!bTop){
				dCurrent = dCurrent.getParentDirectory();
				if (dCurrent != null){  //if it is the root directory, no parent
					sbOut.append(java.io.File.separatorChar).append(dCurrent.getName());
				}
				else{
					bTop = true;
				}
			}
			return sbOut.toString();
		}

}
