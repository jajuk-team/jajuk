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
 * Revision 1.8  2003/11/13 18:56:55  bflorat
 * 13/11/2003
 *
 * Revision 1.7  2003/11/11 20:35:43  bflorat
 * 11/11/2003
 *
 * Revision 1.6  2003/11/03 06:08:05  bflorat
 * 03/11/2003
 *
 * Revision 1.5  2003/10/31 13:05:06  bflorat
 * 31/10/2003
 *
 * Revision 1.4  2003/10/28 21:34:37  bflorat
 * 28/10/2003
 *
 * Revision 1.3  2003/10/26 21:28:49  bflorat
 * 26/10/2003
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.jajuk.i18n.Messages;
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
	public static synchronized Device  registerDevice(String sName,int iDeviceType,String sUrl) {
		String sId = MD5Processor.hash(sUrl+sName+iDeviceType);
		return registerDevice(sId,sName,iDeviceType,sUrl);
	}
	
	/**
		 * Register a device with a known id
		 *@param sName
		 *@return device 
		 */
		public static synchronized Device  registerDevice(String sId,String sName,int iDeviceType,String sUrl) {
			Device device = new Device(sId,sName,iDeviceType,sUrl);
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
