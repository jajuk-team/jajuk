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
 * Revision 1.2  2003/10/23 22:07:40  bflorat
 * 23/10/2003
 *
 * Revision 1.1  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;

/**
 *  A physical directory 
 *<p> Physical item
 * @Author     bflorat
 * @created    17 oct. 2003
 */
public class Directory extends PropertyAdapter {

	/**ID. Ex:1,2,3...*/
	private String sId;
	/**directory name. Ex: rock */
	private String sName;
	/**Parent directory ID**/
	private Directory directory;
	/**Directory device*/
	private Device device;
	/**Child directories*/
	private ArrayList alDirectories = new ArrayList(20);
	

	/**
	 * Album constructor
	 * @param id
	 * @param sName
	 * @param style
	 * @param author
	 */
	public Directory(String sId, String sName,Directory directory,Device device) {
		this.sId = sId;
		this.sName = sName;
		this.directory = directory;
		this.device  = device;
	}

	
	/**
	 * toString method
	 */
	public String toString() {
		return "Directory[ID=" + sId + " Name="+sName+" Parent dir="+directory.getName()+" Device="+device.getName()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<directory id='" + sId);
		sb.append("' name='");
		sb.append(sName);
		sb.append("'' parent='");
		sb.append(directory.getName());
		sb.append("'' device='");
		sb.append(device.getName());
		sb.append(sName).append("' ");
		sb.append(getPropertiesXml());
		sb.append("/>\n");
		return sb.toString();
	}

		/**
	 * Equal method to check two directories are identical
	 * @param otherDirectory
	 * @return
	 */
	public boolean equals(Directory otherDirectory){
		if (
			(this.getName().equals(otherDirectory.getName())) &&	
			(this.getParentDirectory().equals(otherDirectory.getParentDirectory())) &&
			(this.getDevice().equals(otherDirectory.getDevice())) ){
				return true;
		}
		return false;
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
		return directory;
	}
	
	/**
		 * @return
		 */
		public ArrayList getDirectories() {
			return alDirectories;
		}

		/**
		 * @param directory
		 */
		public void addDirectory(Directory directory) {
			alDirectories.add(directory);
		}


}
