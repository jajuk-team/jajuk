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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;

/**
 *  Convenient class to manage devices
 * @Author    bflorat
 * @created    17 oct. 2003
 */
public class DeviceManager implements ITechnicalStrings{
	/**Device collection**/
	static ArrayList alDevices = new ArrayList(100);
	/**Device ids*/
	static ArrayList alDeviceIds = new ArrayList(100);
	/**Supported device types names*/
	static private ArrayList alDevicesTypes = new ArrayList(10);
	
	/**
	 * No constructor available, only static access
	 */
	private DeviceManager() {
		super();
	}

	/**
	 * Register a device
	 *@param sName
	 *@return device 
	 */
	public static synchronized Device  registerDevice(String sName,int iDeviceType,String sUrl,String sMountPoint){
		String sId = processId(sUrl,sName,iDeviceType);
		return registerDevice(sId,sName,iDeviceType,sUrl,sMountPoint);
	}
	
	/**
	 * Process to compute a device id
	 * @param sUrl
	 * @param sName
	 * @param iDeviceType
	 * @return An id
	 */
	private static String processId(String sUrl,String sName,int iDeviceType){
	    return MD5Processor.hash(sUrl+ sName+iDeviceType); //reprocess id;
	}
	
	
	/**
	 * Check none device already has this name or is a parent directory
	 * @param sName
	 * @param iDeviceType
	 * @param sUrl
	 * @param sMountPoint
	 * @return 0:ok or error code 
	 */
	public static String checkDeviceAvailablity(String sName,int iDeviceType,String sUrl,String sMountPoint){
		//check name and path
	    Iterator it = alDevices.iterator();
		while (it.hasNext()){
		    Device deviceToCheck = (Device)it.next();
			if ( sName.equals(deviceToCheck.getName())){
				return "019" ; //$NON-NLS-1$
			}
			String sUrlChecked = deviceToCheck.getUrl();
			//check it is not a sub-directory of an existing device
			File fNew = new File(sUrl);
			File fChecked = new File(sUrlChecked);
			if (fNew.equals(fChecked) || Util.isDescendant(fNew,fChecked) || Util.isAncestor(fNew,fChecked)){
			    return "029"; //$NON-NLS-1$
			}
		}
		//check availability
		if ( iDeviceType != 2 ){ //not a remote device, TBI for remote
		    //make sure it's mounted if under unix
		    if (!Util.isUnderWindows() && sMountPoint != null && !sMountPoint.equals("")){ //$NON-NLS-1$
		       try {
		           Process process = Runtime.getRuntime().exec("mount "+sMountPoint); //run the actual mount command //$NON-NLS-1$
		           process.waitFor();
		       } catch (Exception e) {
		       }
		    }
		    //test directory is available
		    File file = new File(sUrl);
		    if ( !file.exists() || !file.canRead()){ //see if the url exists and is readable
		        return "101"; //$NON-NLS-1$
		    }
		}
		return "0"; //$NON-NLS-1$
	}
	
	/**
	 * Register a device with a known id
	 *@param sName
	 *@return device 
	 */
	public static synchronized Device  registerDevice(String sId,String sName,int iDeviceType,String sUrl,String sMountPoint){
		Device device = new Device(sId,sName,iDeviceType,sUrl,sMountPoint);
		alDeviceIds.add(sId);
		alDevices.add(device);
		return device;
	}
	
	/**
	 * Register a device type
	 * @param sDeviceType
	 */
	public static void registerDeviceType(String sDeviceType){
	    alDevicesTypes.add(sDeviceType);
	}
	
	/**
	 * @return number of registered devices
	 */
	public static int getDeviceTypesNumber(){
	    return alDevicesTypes.size();
	}
	
	/**
	 * @return Device types iteration
	 */
	public static Iterator getDeviceTypes(){
	    return alDevicesTypes.iterator();
	}
	
	/**
	 * Get a device type name for a given index
	 * @param index
	 * @return device name for a given index
	 */
	public static String getDeviceType(int index){
	    return (String)alDevicesTypes.get(index);
	}
	
	
	/**Return all registred devices*/
	public static synchronized ArrayList getDevices() {
		return alDevices;
	}
	
	/**
	 * Return device by id
	 * @param sName
	 * @return
	 */
	public static synchronized Device getDevice(String sId) {
		Device device = null;
		int index = alDeviceIds.indexOf(sId);
		if (index != -1){
			device = (Device) alDevices.get(index);
		}
		return device;
	}
	
	
	
	/**
	 * Remove a device
	 * @param device
	 */
	public static synchronized void removeDevice(Device device){
		//show confirmation message if required
	    if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_REMOVE_DEVICE)){
	        int iResu = JOptionPane.showConfirmDialog(Main.getWindow(),Messages.getString("Confirmation_remove_device"),Messages.getString("Main.21"),JOptionPane.YES_NO_OPTION);  //$NON-NLS-1$ //$NON-NLS-2$
			if (iResu == JOptionPane.NO_OPTION){
				return;
			}
	    }
	    
	    //if device is refreshing or synchronizing, just leave
		if (device.isSynchronizing() || device.isRefreshing()){
			Messages.showErrorMessage("013"); //$NON-NLS-1$
			return;
		}
		//check if device can be unmounted
		if (!FIFO.canUnmount(device)){
			Messages.showErrorMessage("121"); //$NON-NLS-1$
			return;
		}
		//if it is mounted, try to unmount it
		if (device.isMounted()){ 
			try{
				device.unmount();
			}
			catch(Exception e){
				Messages.showErrorMessage("013"); //$NON-NLS-1$
				return;
			}
		}
		alDevices.remove(device);
		alDeviceIds.remove(device.getId());
		DirectoryManager.cleanDevice(device.getId());
		FileManager.cleanDevice(device.getId());
		PlaylistFileManager.cleanDevice(device.getId());
		//	Clean the collection up
		org.jajuk.base.Collection.cleanup();
		//remove synchronization if another device was synchronized with this device
		Iterator it = alDevices.iterator();
		while (it.hasNext()){
			Device deviceToCheck = (Device)it.next();
			String sSyncSource = deviceToCheck.getProperty(DEVICE_OPTION_SYNCHRO_SOURCE);
			if ( sSyncSource != null && sSyncSource.equals(device.getId())){
				deviceToCheck.setProperty(DEVICE_OPTION_SYNCHRO_SOURCE,null);
			}
		}
		//Sort collection
		FileManager.sortFiles();//resort collection in case of
		
		//refresh views
		ObservationManager.notify(EVENT_DEVICE_REFRESH);
	}
	
	/**
	 * @return whether any device is currently refreshing
	 */
	public static boolean isAnyDeviceRefreshing(){
		boolean bOut = false;
		Iterator it = DeviceManager.getDevices().iterator();
		while ( it.hasNext()){
			Device device = (Device)it.next();
			if ( device.isRefreshing()){
				bOut = true;
				break;
			}
		}
		return bOut;
	}
	
	
	/**
	 * Change mount point  for a given device
	 * @param device to set
	 * @param mountPoint The sMountPoint to set.
	 */
	public static void setMountPoint(Device device,String sMountPoint) {
		device.setMountPoint(sMountPoint);
	}


}
