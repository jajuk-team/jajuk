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
 * Revision 1.7  2003/11/03 06:08:05  bflorat
 * 03/11/2003
 *
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.i18n.Messages;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  A device ( music files repository )
 * *<p> Physical item
 * @Author     bflorat
 * @created    17 oct. 2003
 */
public class Device extends PropertyAdapter implements ITechnicalStrings{

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
	/**Already refreshing flag*/
	private boolean bAlreadyRefreshing = false;

	/**
	 * Device constructor
	 * @param sId
	 * @param sName
	 * @param sDeviceType
	 * @param sUrl
	 */
	public Device(String sId, String sName, String sDeviceType, String sUrl) {
		this.sId = sId;
		this.sName = sName;
		this.sDeviceType = sDeviceType;
		this.sUrl = sUrl;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Device[ID=" + sId + " Name=" + sName + " Type=" + sDeviceType + " URL=" + sUrl + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
	}

	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<device id='" + sId);
		sb.append("' name='");
		sb.append(Util.formatXML(sName));
		sb.append("' type='");
		sb.append(sDeviceType);
		sb.append("' url='");
		sb.append(sUrl).append("' ");
		sb.append(getPropertiesXml());
		sb.append("/>\n");
		return sb.toString();
	}

	/**
	 * Equal method to check two devices are identical
	 * @param otherDevice
	 * @return
	 */
	public boolean equals(Object otherDevice) {
		return this.getId().equals(((Device)otherDevice).getId() );
	}

	/**
	 * Refresh : scan asynchronously the device to find tracks
	 * @return
	 */
	public void refresh() {
		org.jajuk.base.Collection.setModified(true); //note the change so the collection will be persisted as XML file
		final Device device = this;
		//current reference to the inner thread class
		new Thread() {
			public void  run() {
				
				/*Remove all directories, playlist files and files for this device before rescan. 
				Note  that logical item ( tracks, styles...) are device independant and connot be cleared.
				They will be clean up at next jajuk restart and old track data is used to populate device without full tag scan
				*/ 
				FileManager.cleanDevice(device.getId());
				PlaylistFileManager.cleanDevice(device.getId());
				DirectoryManager.cleanDevice(device.getId());
				
				long lTime = System.currentTimeMillis();
				if (bAlreadyRefreshing){
					Messages.showErrorMessage("107");
					return;
				}
				bAlreadyRefreshing = true;
				Log.debug("Starting refresh of device : "+device);
				
				File fTop = new File(device.sUrl);
				if (!fTop.exists()) {
					Messages.showErrorMessage("101");
					return;
				}
				
				//index init
				File fCurrent = fTop;
				int[] indexTab = new int[100]; //directory index  
				for (int i = 0; i < 100; i++) { //init
					indexTab[i] = -1;
				}
				int iDeep = 0; //deep
				Directory dParent = null;
				
				//Create a directory for device itself and scan files to allow files at the root of the device
				if (!device.getDeviceType().equals(DEVICE_TYPE_REMOTE) || !device.getDeviceType().equals(DEVICE_TYPE_AUDIO_CD)){
					Directory d = DirectoryManager.registerDirectory(device);
					dParent = d;
					d.scan();
				}
				//Start actual scan
				while (iDeep >= 0) {
					//Log.debug("entering :"+fCurrent);
					File[] files = fCurrent.listFiles(JajukFileFilter.getInstance(true,false)); //only directories
					if (files.length == 0 ){
						indexTab[iDeep] = -1;//re-init for next time we will reach this deep
						iDeep--; //come up
						fCurrent = fCurrent.getParentFile();
						dParent = dParent.getParentDirectory();
					} else {
						if (indexTab[iDeep] < files.length-1 ){  //enter sub-directory
							indexTab[iDeep]++; //inc index for next time we will reach this deep
							fCurrent = files[indexTab[iDeep]];
							dParent = DirectoryManager.registerDirectory(fCurrent.getName(),dParent,device);
							dParent.scan();
							iDeep++;
						}
						else{
							indexTab[iDeep] = -1;
							iDeep --;
							fCurrent = fCurrent.getParentFile();
							if (dParent!=null){
								dParent = dParent.getParentDirectory();
							}
						}
					}					
				}
				Log.debug("Refresh done. Found : "+FileManager.getFiles().size()+" files in "+(int)((System.currentTimeMillis()-lTime)/1000)+" sec");
				bAlreadyRefreshing = false;
				
				//Clean the collection up
				Collection.cleanup();
			}
		}
		.start();

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
