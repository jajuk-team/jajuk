/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Convenient class to manage devices
 * @Author    Bertrand Florat
 * @created    17 oct. 2003
 */
public class DeviceManager extends ItemManager{
    /**Supported device types names*/
    private ArrayList alDevicesTypes = new ArrayList(10);
    /**Self instance*/
    private static DeviceManager singleton;
    /**Date last global refresh*/
    private long lDateLastGlobalRefresh = 0;
    /**Auto-refresh thread*/
    private Thread tAutoRefresh = new Thread(){
        public void run(){
            while (!Main.isExiting()){
                try{
                    Thread.sleep(AUTO_REFRESH_DELAY);
                    refreshAllDevices();
                }
                catch(Exception e){
                    Log.error(e);
                }
            }
        }
    };
    private boolean bGlobalRefreshing = false;
    
    /**
     * No constructor available, only static access
     */
    private DeviceManager() {
        super();
        //register properties
        //ID
        registerProperty(new PropertyMetaInformation(XML_ID,false,true,false,false,false,String.class,null,null));
        //Name
        registerProperty(new PropertyMetaInformation(XML_NAME,false,true,true,false,false,String.class,null,null));
        //Type
        registerProperty(new PropertyMetaInformation(XML_TYPE,false,true,true,false,false,Long.class,null,null));
        //URL
        registerProperty(new PropertyMetaInformation(XML_URL,false,true,true,false,false,Long.class,null,null));
        //Mount point
        registerProperty(new PropertyMetaInformation(XML_DEVICE_MOUNT_POINT,false,true,true,false,false,String.class,null,null));
        //Auto-mount
        registerProperty(new PropertyMetaInformation(XML_DEVICE_AUTO_MOUNT,false,true,true,false,false,Boolean.class,null,null));
        //Auto-refresh
        registerProperty(new PropertyMetaInformation(XML_DEVICE_AUTO_REFRESH,false,true,true,false,false,Double.class,null,0d));
        //Expand
        registerProperty(new PropertyMetaInformation(XML_EXPANDED,false,false,false,false,true,Boolean.class,null,false));
        //Synchro source
        registerProperty(new PropertyMetaInformation(XML_DEVICE_SYNCHRO_SOURCE,false,false,true,false,false,String.class,null,null));
        //Synchro mode
        registerProperty(new PropertyMetaInformation(XML_DEVICE_SYNCHRO_MODE,false,false,true,false,false,String.class,null,null));
    }
    
    public void startAutoRefreshThread(){
        tAutoRefresh.setPriority(Thread.MIN_PRIORITY);
        tAutoRefresh.start();
    }
    
    /**
     * @return singleton
     */
    public static DeviceManager getInstance(){
        if (singleton == null){
            singleton = new DeviceManager();
        }
        return singleton;
    }
    
    
    /**
     * Register a device
     *@param sName
     *@return device 
     */
    public Device  registerDevice(String sName,long lDeviceType,String sUrl){
        String sId = processId(sUrl,sName,lDeviceType);
        return registerDevice(sId,sName,lDeviceType,sUrl);
    }
    
    /**
     * Register a device with a known id
     *@param sName
     *@return device 
     */
    public Device registerDevice(String sId,String sName,long lDeviceType,String sUrl){
        synchronized(DeviceManager.getInstance().getLock()){
            Device device = new Device(sId,sName,lDeviceType,sUrl);
            hmItems.put(sId,device);
            return device;
        }
    }
    
    /**
     * Process to compute a device id
     * @param sUrl
     * @param sName
     * @param iDeviceType
     * @return An id
     */
    private String processId(String sUrl,String sName,long lDeviceType){
        return MD5Processor.hash(sUrl+ sName+lDeviceType); //reprocess id;
    }
    
    
    /**
     * Check none device already has this name or is a parent directory
     * @param sName
     * @param iDeviceType
     * @param sUrl
     * @param sMountPoint
     * @return 0:ok or error code 
     */
    public String checkDeviceAvailablity(String sName,int iDeviceType,String sUrl,String sMountPoint){
        synchronized(DeviceManager.getInstance().getLock()){
            //check name and path
            Iterator it = hmItems.values().iterator();
            while (it.hasNext()){
                Device deviceToCheck = (Device)it.next();
                if ( sName.toLowerCase().equals(deviceToCheck.getName().toLowerCase())){
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
    }
    
    /**
     * Register a device type
     * @param sDeviceType
     */
    public void registerDeviceType(String sDeviceType){
        alDevicesTypes.add(sDeviceType);
    }
    
    /**
     * @return number of registered devices
     */
    public int getDeviceTypesNumber(){
        return alDevicesTypes.size();
    }
    
    /**
     * @return Device types iteration
     */
    public Iterator getDeviceTypes(){
        return alDevicesTypes.iterator();
    }
    
    /**
     * Get a device type name for a given index
     * @param index
     * @return device name for a given index
     */
    public String getDeviceType(long index){
        return (String)alDevicesTypes.get((int)index);
    }
    
    /**
     * Remove a device
     * @param device
     */
    public void removeDevice(Device device){
        synchronized(DeviceManager.getInstance().getLock()){
            //show confirmation message if required
            if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_REMOVE_DEVICE)){
                int iResu = Messages.getChoice(Messages.getString("Confirmation_remove_device"),JOptionPane.WARNING_MESSAGE);  //$NON-NLS-1$ //$NON-NLS-2$
                if (iResu != JOptionPane.YES_OPTION){
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
            hmItems.remove(device.getId());
            DirectoryManager.getInstance().cleanDevice(device.getId());
            FileManager.getInstance().cleanDevice(device.getId());
            PlaylistFileManager.getInstance().cleanDevice(device.getId());
            //	Clean the collection up
            org.jajuk.base.Collection.cleanup();
            //remove synchronization if another device was synchronized with this device
            Iterator it = hmItems.values().iterator();
            while (it.hasNext()){
                Device deviceToCheck = (Device)it.next();
                if (deviceToCheck.containsProperty(XML_DEVICE_SYNCHRO_SOURCE)){
                    String sSyncSource = deviceToCheck.getStringValue(XML_DEVICE_SYNCHRO_SOURCE);
                    if ( sSyncSource.equals(device.getId())){
                        deviceToCheck.removeProperty(XML_DEVICE_SYNCHRO_SOURCE);
                    }
                }
            }
        }
    }
    
    /**
     * @return whether any device is currently refreshing
     */
    public boolean isAnyDeviceRefreshing(){
        synchronized(DeviceManager.getInstance().getLock()){
            boolean bOut = false;
            Iterator it = DeviceManager.getInstance().getItems().iterator();
            while ( it.hasNext()){
                Device device = (Device)it.next();
                if ( device.isRefreshing()){
                    bOut = true;
                    break;
                }
            }
            return bOut;
        }
    }
    
    /**
     * Clean all devices
     */
    public synchronized void cleanAllDevices() {
        synchronized(DeviceManager.getInstance().getLock()){
            Iterator it = hmItems.values().iterator();
            while (it.hasNext()){
                Device device = (Device)it.next();
                FileManager.getInstance().cleanDevice(device.getName());
                DirectoryManager.getInstance().cleanDevice(device.getName());
                PlaylistFileManager.getInstance().cleanDevice(device.getName());
            }
            hmItems.clear();
        }
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_DEVICES;
    }
    
    public long getDateLastGlobalRefresh() {
        return lDateLastGlobalRefresh;
    }
    
    /**
     * Refresh of all devices with auto-refresh enabled (used in automatic mode)
     * Must be the sortest possible
     */
    public void refreshAllDevices(){
        try{
            //check thread is not already refreshing
            if (bGlobalRefreshing){
                return;
            }
            bGlobalRefreshing = true;
            long l = System.currentTimeMillis();
            lDateLastGlobalRefresh = System.currentTimeMillis();
            boolean bNeedUIRefresh = false;
            for (IPropertyable item:getItems()){
                Device device = (Device)item;
                double frequency = 60000 * device.getDoubleValue(XML_DEVICE_AUTO_REFRESH);
                //check if this device needs auto-refresh
                if (frequency == 0d || 
                        device.getDateLastRefresh() > (System.currentTimeMillis() - frequency)){
                    continue;
                }
                //Check of mounted device contains files, otherwise it is not mounted
                //we have to check this because of the automatic cleaner thread musn't remove all references
                File[] files = new File(device.getUrl()).listFiles();
                if (!device.isRefreshing() && files !=null && files.length > 0){
                    //cleanup device
                    bNeedUIRefresh = bNeedUIRefresh | device.cleanRemovedFiles();//logical or, not an error !
                    //refresh it
                    bNeedUIRefresh = bNeedUIRefresh | device.refreshCommand(false); //logical or, not an error !
                }
            }
            
            //If something changed
            if (bNeedUIRefresh){
                //cleanup logical items
                TrackManager.getInstance().cleanup();
                StyleManager.getInstance().cleanup();
                AlbumManager.getInstance().cleanup();
                AuthorManager.getInstance().cleanup();
                PlaylistManager.getInstance().cleanup();
                //notify views to refresh
                ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH));
            }
            //Display end of refresh message with stats
            l = System.currentTimeMillis() -l;
            Log.debug("Global refresh done in: "+((l<1000)?l+" ms":l/1000+" s"));
        }
        catch(Exception e){
            Log.error(e);
        }
        finally{
            bGlobalRefreshing = false;
        }
        
    }
}


