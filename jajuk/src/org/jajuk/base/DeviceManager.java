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

import java.util.ArrayList;
import java.util.HashMap;

import org.jajuk.util.MD5Processor;

/**
 *  Convenient class to manage devices
 * @Author    bflorat
 * @created    17 oct. 2003
 */
public class DeviceManager {
	/**Device collection**/
	static HashMap hmDevices = new HashMap(100);
	
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
	public static synchronized Device  registerDevice(String sName,int iDeviceType,String sUrl,String sMountPoint) {
		String sId = MD5Processor.hash(sUrl+sName+iDeviceType);
		return registerDevice(sId,sName,iDeviceType,sUrl,sMountPoint);
	}
	
	/**
		 * Register a device with a known id
		 *@param sName
		 *@return device 
		 */
		public static synchronized Device  registerDevice(String sId,String sName,int iDeviceType,String sUrl,String sMountPoint) {
			Device device = new Device(sId,sName,iDeviceType,sUrl,sMountPoint);
			hmDevices.put(sId,device);
			return device;
		}


	/**Return all registred devices*/
	public static synchronized ArrayList getDevices() {
		return new ArrayList(hmDevices.values());
	}

	/**
	 * Return device by id
	 * @param sName
	 * @return
	 */
	public static synchronized Device getDevice(String sId) {
		return (Device) hmDevices.get(sId);
	}
	
	/**
	 * Remove a device
	 * @param device
	 */
	public static synchronized void removeDevice(Device device){
		hmDevices.remove(device.getId());
		DirectoryManager.cleanDevice(device.getId());
		FileManager.cleanDevice(device.getId());
		PlaylistFileManager.cleanDevice(device.getId());
		//	Clean the collection up
		org.jajuk.base.Collection.cleanup();
	}

}
