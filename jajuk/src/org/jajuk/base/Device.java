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

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.JajukFileFilter;

/**
 *  A device ( music files repository )
 * *<p> Physical item
 * @Author     bflorat
 * @created    17 oct. 2003
 */
public class Device extends PropertyAdapter {

	/** ID. Ex:1,2,3...*/
	private String sId;
	/**Device name*/
	private String sName;
	/**Device type. ex:directory, remote...*/
	private String sDeviceType;
	/**Device url**/
	private String sUrl;
	/**Mounted device flag*/
	private boolean bMounted;
	/**directories*/
	private ArrayList alDirectories = new ArrayList(20);
	

	/**
	 * Device constructor
	 * @param sId
	 * @param sName
	 * @param sDeviceType
	 * @param sUrl
	 */
	public Device(String sId, String sName,String sDeviceType,String sUrl) {
		this.sId = sId;
		this.sName = sName;
		this.sDeviceType = sDeviceType;
		this.sUrl = sUrl;
	}

	
	/**
	 * toString method
	 */
	public String toString() {
		return "Device[ID="+sId+" Name=" + sName + " Type="+sDeviceType+" URL="+sUrl+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<device id='" + sId);
		sb.append("' name='");
		sb.append(sName);
		sb.append("'' type='");
		sb.append(sDeviceType);
		sb.append("'' url='");
		sb.append(sUrl);
		sb.append(sName).append("' ");
		sb.append(getPropertiesXml());
		sb.append("/>\n");
		return sb.toString();
	}

		
	/**
	 * Equal method to check two devices are identical
	 * @param otherDevice
	 * @return
	 */
	public boolean equals(Device otherDevice){
		if (
			(this.getName().equals(otherDevice.getName())) &&	
			(this.getDeviceType().equals(otherDevice.getDeviceType())) &&
			(this.getUrl().equals(otherDevice.getUrl())) ){
				return true;
		}
		return false;
	}	
	
	/**
	 * Refresh : scan the device to find tracks
	 * @return
	 */
	public void refresh(){
		//TODO do it in a thread
		File fTop = new File(this.sUrl);
		if (!fTop.exists()){
			Messages.showErrorMessage("101");
			return;
		}
		//scanDirectory(fTop);
		File fParent = fTop;
		int[] indexTab = new int[1000]; //directories index  
		for (int i=0;i<1000;i++){ //init
			indexTab[i] = -1;
		}
		int iDeep = 0; //deep
		while (iDeep >= 0 ){
			File[] files = fParent.listFiles(JajukFileFilter.getInstance());
			for (int i=0;i<files.length;i++){
				System.out.println(files[i]);
			}
			if (files.length == 0){
				indexTab[iDeep] = 0; //re-init for next time we will reach this deep
				iDeep --;  //come up
				fParent = fParent.getParentFile();
			}
			else{
				indexTab[iDeep] ++;
				iDeep ++;
				fParent = files[indexTab[iDeep]];
			}
			
		}
	}
	/*
	 * Directory directory = DirectoryManager.registerDirectory(files[i].getName(),null,this);
				scanDirectory(files[i]);
				File[] files2 = files[i].listFiles(JajukFileFilter.getInstance());
	 */
	/**
	 * Scan all files in a directory
	 * @param 
	 */
	private void scanDirectory(File fDirectory){
		File[] files = fDirectory.listFiles(JajukFileFilter.getInstance());
		for (int i=0;i<files.length;i++){
			if (!files[i].isDirectory()){
				System.out.println(files[i]);
			}
		}
	}


	/**
	 * @return
	 */
	public boolean isMounted() {
		return bMounted;
	}

	/**
	 * @return
	 */
	public String getDeviceType() {
		return sDeviceType;
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
	public String getUrl() {
		return sUrl;
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
