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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 *  A device ( music files repository )
 * <p>Some properties of a device are immuable : name, url and type
 * *<p> Physical item
 * @Author     bflorat
 * @created    17 oct. 2003
 */
public class Device extends PropertyAdapter implements ITechnicalStrings, Comparable{
	
	/** ID. Ex:1,2,3...*/
	private String sId;
	/**Device name*/
	private String sName;
	/**Device type id*/
	int iDeviceType;
	/**Device url**/
	private String sUrl;
	/** IO file for optimizations* */
	private java.io.File fio;
	/**Device mount point**/
	private String sMountPoint;
	/**Mounted device flag*/
	private boolean bMounted;
	/**directories*/
	private ArrayList alDirectories = new ArrayList(20);
	/**Already refreshing flag*/
	private boolean bAlreadyRefreshing = false;
	/**Already synchronizing flag*/
	private boolean bAlreadySynchronizing = false;
	/**Convenient lock */
	public static byte[] bLock = new byte[0];
	/** Number of files in this device before refresh ( for refresh stats ) */
	public int iNbFilesBeforeRefresh;
	/** Number of new files found during refresh for stats ) */
	public int iNbNewFiles;
	/** Number of created files on source device during synchro ( for stats ) */
	public int iNbCreatedFilesSrc;
	/** Number of created files on destination device during synchro ( for stats ) */
	public int iNbCreatedFilesDest;
	/** Number of deleted files during a synchro ( for stats ) */
	int iNbDeletedFiles = 0;
	/**Volume of created files during synchro */
	long lVolume = 0;
	
	
	/**
	 * Device constructor
	 * @param sId
	 * @param sName
	 * @param iDeviceType
	 * @param sUrl
	 */
	public Device(String sId, String sName, int iDeviceType, String sUrl, String sMountPoint) {
		this.sId = sId;
		this.sName = sName;
		this.iDeviceType = iDeviceType;
		this.sUrl = sUrl;
		this.sMountPoint = sMountPoint;
		this.fio = new File(getUrl());
	}
	
	
	
	/**
	 * toString method
	 */
	public String toString() {
		return "Device[ID=" + sId + " Name=" + sName + " Type=" + DeviceManager.getDeviceType(iDeviceType) + " URL=" + sUrl+ " Mount point="+sMountPoint + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
	}
	
	
	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml() {
		StringBuffer sb = new StringBuffer("\t\t<device id='" + sId); //$NON-NLS-1$
		sb.append("' name='"); //$NON-NLS-1$
		sb.append(Util.formatXML(sName));
		sb.append("' type='"); //$NON-NLS-1$
		sb.append(getDeviceType());
		sb.append("' url='"); //$NON-NLS-1$
		sb.append(sUrl);
		sb.append("' mount_point='"); //$NON-NLS-1$
		sb.append(getMountPoint()).append("' "); //$NON-NLS-1$
		sb.append(getPropertiesXml());
		sb.append("/>\n"); //$NON-NLS-1$
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
	 * hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
	}
	
	
	/**
	 * Refresh : scan asynchronously the device to find tracks
	 * @param bAsynchronous : set asyncrhonous or synchronous mode
	 * @return
	 */
	public void refresh(boolean bAsynchronous) {
		final Device device = this;
		if ( !device.isMounted()){
			try{
				device.mount();  
			}
			catch(Exception e){
				Log.error("011",getName(),e);	//mount failed //$NON-NLS-1$
				Messages.showErrorMessage("011",getName()); //$NON-NLS-1$
				return;
			}
		}
		if (bAlreadyRefreshing){
			Messages.showErrorMessage("107"); //$NON-NLS-1$
			return;
		}
		bAlreadyRefreshing = true; //set ths flag outside the lock scope to maek it as refreshing even if the refresh didn't actually started and is locked by another refresh
		if ( bAsynchronous){
			new Thread(){
				public void run(){
					refreshCommand(device);
				}
			}.start();
		}
		else{
			refreshCommand(device);
		}
		
	}
	
	
	/**
	 * The refresh itself
	 * @param device device to refresh
	 */
	private void refreshCommand(Device device){
	    try{
	        //lock the synchro
	        synchronized(bLock){
	            //check Jajuk is not exiting because a refresh cannot start in this state
	            if (Main.bExiting){
	                return;
	            }
	            /*check target directory is not void because it could mean that the device is not actually system-mounted and
	             then a refresh would clear the device, display a warning message*/
	            File file = new File(getUrl());
	            if ( file.exists() && (file.list() == null || file.list().length==0)){
	                int i = JOptionPane.showConfirmDialog(Main.getWindow(),Messages.getString("Confirmation_void_refresh"),Messages.getString("Warning"),JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	                if ( i != JOptionPane.OK_OPTION){
	                    return;
	                }
	            }
	            /*Remove all directories, playlist files and files for this device before rescan. 
	             Note  that logical item ( tracks, styles...) are device independant and connot be cleared.
	             They will be clean up at next jajuk restart and old track data is used to populate device without full tag scan
	             */ 
	            iNbFilesBeforeRefresh = FileManager.getFiles().size();
	            iNbNewFiles = 0;
	            FileManager.cleanDevice(device.getId());
	            PlaylistFileManager.cleanDevice(device.getId());
	            DirectoryManager.cleanDevice(device.getId());
	            long lTime = System.currentTimeMillis();
	            Log.debug("Starting refresh of device : "+device); //$NON-NLS-1$
	            
	            File fTop = new File(device.sUrl);
	            if (!fTop.exists()) {
	                Messages.showErrorMessage("101"); //$NON-NLS-1$
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
	            if (!device.getDeviceTypeS().equals(DEVICE_TYPE_REMOTE) || !device.getDeviceTypeS().equals(DEVICE_TYPE_AUDIO_CD)){
	                Directory d = DirectoryManager.registerDirectory(device);
	                dParent = d;
	                d.scan();
	            }
	            //Start actual scan
	            while (iDeep >= 0) {
	                //Log.debug("entering :"+fCurrent);
	                File[] files = fCurrent.listFiles(new JajukFileFilter(true,false)); //only directories
	                if (files== null || files.length == 0 ){  //files is null if fCurrent is a not a directory 
	                    indexTab[iDeep] = -1;//re-init for next time we will reach this deep
	                    iDeep--; //come up
	                    fCurrent = fCurrent.getParentFile();
	                    dParent = dParent.getParentDirectory();
	                } else {
	                    if (indexTab[iDeep] < files.length-1 ){  //enter sub-directory
	                        indexTab[iDeep]++; //inc index for next time we will reach this deep
	                        fCurrent = files[indexTab[iDeep]];
	                        dParent = DirectoryManager.registerDirectory(fCurrent.getName(),dParent,device);
	                        InformationJPanel.getInstance().setMessage(new StringBuffer(Messages.getString("Device.21")).append(device.getName()).append(Messages.getString("Device.22")).append(dParent.getRelativePath()).append("]").toString(),InformationJPanel.INFORMATIVE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	            String sOut = new StringBuffer("[").append(device.getName()).append(Messages.getString("Device.25")).append((int)((System.currentTimeMillis()-lTime)/1000)). //$NON-NLS-1$ //$NON-NLS-2$
	            append(Messages.getString("Device.26")).append(iNbNewFiles).append(Messages.getString("Device.27")). //$NON-NLS-1$ //$NON-NLS-2$
	            append(iNbFilesBeforeRefresh - (FileManager.getFiles().size()-iNbNewFiles)).append(Messages.getString("Device.28")).toString(); //$NON-NLS-1$
	            InformationJPanel.getInstance().setMessage(sOut,InformationJPanel.INFORMATIVE); //$NON-NLS-1$
	            Log.debug(sOut); 
	            //clear history to remove olf files referenced in it
	            History.getInstance().clear(Integer.parseInt(ConfigurationManager.getProperty(CONF_HISTORY))); //delete old history items
	            //notify views to refresh
	            ObservationManager.notify(EVENT_DEVICE_REFRESH);		
	        }
	    }
	    catch(RuntimeException re){ //runtime error are thrown
	        throw re;
	    }
	    catch(Exception e){ //and regular ones logged
	        Log.error(e);
	    }
	    finally{  //make sure to unlock refreshing even if an error occured
	        bAlreadyRefreshing = false;
	    }
	}
	
	
	/**
	 * Synchroning asynchronously 
	 * @param bAsynchronous : set asyncrhonous or synchronous mode
	 * @return
	 */
	public void synchronize(boolean bAsynchronous) {
		final Device device = this;
		if ( !device.isMounted()){
			try{
				device.mount();  
			}
			catch(Exception e){
				Log.error("011",getName(),e);	//mount failed //$NON-NLS-1$
				Messages.showErrorMessage("011",getName()); //$NON-NLS-1$
				return;
			}
		}
		bAlreadySynchronizing = true;
		if ( bAsynchronous){
			new Thread(){
				public void  run() {
					synchronizeCommand();
				}
			}.start();
		}
		else{
			synchronizeCommand();
		}
	}
	
	
	/**
	 * Synchronize action itself
	 *@param device : device to synchronize
	 */
	public void synchronizeCommand(){
	    try{
	        long lTime = System.currentTimeMillis();				
	        iNbCreatedFilesDest = 0;
	        iNbCreatedFilesSrc = 0;
	        iNbDeletedFiles = 0;
	        lVolume = 0;
	        //check this device is synchronized
	        String sIdSrc = getProperty(DEVICE_OPTION_SYNCHRO_SOURCE); 
	        if ( sIdSrc == null || sIdSrc.equals(getId())){  //cannot synchro with itself
	            return;
	        }
	        //force a refresh of this device and src device before any sync
	        refresh(false);
	        Device dSrc = DeviceManager.getDevice(sIdSrc);
	        dSrc.refresh(false);
	        //start message
	        InformationJPanel.getInstance().setMessage(new StringBuffer(Messages.getString("Device.31")). //$NON-NLS-1$
	                append(dSrc.getName()).append(',').append(this.getName()).append("]"). //$NON-NLS-1$
	                toString(),InformationJPanel.INFORMATIVE);
	        //in both cases ( bi or uni-directional ), make an unidirectional sync from source device to this one
	        iNbCreatedFilesDest = synchronizeUnidirectonal(dSrc,this);
	        if ( iNbCreatedFilesDest > 0 ){
	            refresh(false); //refresh this device if needed
	        }
	        //if it is bidirectional, make an additional sync from this device to the source one
	        if ( DEVICE_OPTION_SYNCHRO_MODE_BI.equals(getProperty(DEVICE_OPTION_SYNCHRO_MODE))){
	            iNbCreatedFilesSrc = synchronizeUnidirectonal(this,dSrc);
	            if ( iNbCreatedFilesSrc > 0){
	                dSrc.refresh(false);  //refresh source device if needed
	            }
	        }
	        //end message
	        String sOut = new StringBuffer(Messages.getString("Device.33")).append((System.currentTimeMillis()-lTime)/1000) //$NON-NLS-1$
	        .append(Messages.getString("Device.34")).append(iNbCreatedFilesSrc+iNbCreatedFilesDest).append(Messages.getString("Device.35")). //$NON-NLS-1$ //$NON-NLS-2$
	        append(lVolume/1048576).append(Messages.getString("Device.36")).toString(); //$NON-NLS-1$
	        InformationJPanel.getInstance().setMessage(sOut,InformationJPanel.INFORMATIVE);
	        Log.debug(sOut);
	    }
	    catch(RuntimeException re){ //runtime error are thrown
	        throw re;
	    }
	    catch(Exception e){ //and regular ones logged
	        Log.error(e);
	    }
	    finally{  //make sure to unlock sychronizing even if an error occured
	        bAlreadySynchronizing = false;
	    }
	}
	
	
	/**
	 * Synchronize a device with another one ( unidirectional )
	 *@param device : device to synchronize
	 *@return nb of created files
	 */
	private int synchronizeUnidirectonal(Device dSrc,Device dest){
		int iNbCreatedFiles = 0;
		//copy new directories from source device
		HashSet hsSourceDirs = new HashSet(100);
		HashSet hsDesynchroPaths = new HashSet(10); //contains paths ( relative to device) of desynchronized dirs
		Iterator it = DirectoryManager.getDirectories().iterator();
		while ( it.hasNext()){
			Directory dir = (Directory)it.next();
			if ( dir.getDevice().equals(dSrc)){
				if (  "n".equals(dir.getProperty(DIRECTORY_OPTION_SYNCHRO_MODE))){  //don't take desynchronized dirs into account //$NON-NLS-1$
					hsDesynchroPaths.add(dir.getRelativePath());
				}
				else{
					hsSourceDirs.add(dir);	
				}
			}
		}
		HashSet hsDestDirs = new HashSet(100);
		it = DirectoryManager.getDirectories().iterator();
		while ( it.hasNext()){
			Directory dir = (Directory)it.next();
			if ( dir.getDevice().equals(dest)){
				if (  "n".equals(dir.getProperty(DIRECTORY_OPTION_SYNCHRO_MODE))){  //don't take desynchronized dirs into account //$NON-NLS-1$
					hsDesynchroPaths.add(dir.getRelativePath());
				}
				else{
					hsDestDirs.add(dir);
				}
			}
		}
		it = hsSourceDirs.iterator();
		Iterator it2;
		while ( it.hasNext()){
			boolean bNeedCreate = true;
			Directory dir = (Directory)it.next();
			String sPath = dir.getRelativePath();
			//check the directory on source is not desynchronized. If it is, leave without checking files
			if ( hsDesynchroPaths.contains(sPath)){
				continue;
			}
			it2 = hsDestDirs.iterator(); 
			while ( it2.hasNext()){
				Directory dir2 = (Directory)it2.next();
				if ( dir2.getRelativePath().equals(sPath)){  //directory already exists on this device
					bNeedCreate = false;
					break;
				}
			}
			//create it if needed
			File fileNewDir  = new File(new StringBuffer(dest.getUrl()).append(sPath).toString());
			if ( bNeedCreate ){ 
				fileNewDir.mkdirs();
			}
			//synchronize files 
			File fileSrc = new File(new StringBuffer(dSrc.getUrl()).append(sPath).toString());
			FileFilter filter = new FileFilter() {
				public boolean accept(File file) {
					String sExt = Util.getExtension(file).toLowerCase();
					if (TypeManager.isExtensionSupported(sExt) || "jpg".equals(sExt) || "gif".equals(sExt)){  //$NON-NLS-1$ //$NON-NLS-2$
						return true;
					}
					return false;
				}
			};
			File[] fSrcFiles = fileSrc.listFiles(filter);
			if ( fSrcFiles != null){
				for (int i=0; i<fSrcFiles.length; i++){
					File[] files = fileNewDir.listFiles(filter);
					if ( files == null){  //fileNewDir is not a directory or an error occured ( read/write right ? )
						continue;
					}
					boolean bNeedCopy = true;
					for (int j=0;j<files.length;j++){
						if ( fSrcFiles[i].getName().equalsIgnoreCase(files[j].getName())){
							bNeedCopy = false;
						}
					}
					if ( bNeedCopy) {
						try{
							Util.copy(fSrcFiles[i],fileNewDir);
							iNbCreatedFiles ++;
							lVolume += fSrcFiles[i].length();
							InformationJPanel.getInstance().setMessage(new StringBuffer(Messages.getString("Device.41")). //$NON-NLS-1$
									append(dSrc.getName()).append(',').append(dest.getName()).append(Messages.getString("Device.42")) //$NON-NLS-1$
									.append(fSrcFiles[i].getAbsolutePath()).append("]"). //$NON-NLS-1$
									toString(),InformationJPanel.INFORMATIVE);
						}
						catch(JajukException je){
							Messages.showErrorMessage(je.getCode(),fSrcFiles[i].getAbsolutePath());
							Messages.showErrorMessage("027"); //$NON-NLS-1$
							Log.error(je);
							return iNbCreatedFiles;
						}
						catch(Exception e){
							Messages.showErrorMessage("020",fSrcFiles[i].getAbsolutePath()); //$NON-NLS-1$
							Messages.showErrorMessage("027"); //$NON-NLS-1$
							Log.error("020",fSrcFiles[i].getAbsolutePath(),e); //$NON-NLS-1$
							return iNbCreatedFiles;
						}
					}
				}
			}
		}
		return iNbCreatedFiles;
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
	public String getDeviceTypeS() {
		return DeviceManager.getDeviceType(iDeviceType);
	}
	
	/**
	 * @return
	 */
	public int getDeviceType() {
		return iDeviceType;
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
	
	/** Tells if a device is refreshing
	 */
	public boolean isRefreshing(){
		return bAlreadyRefreshing;
	}
	
	
	/** Tells if a device is synchronizing
	 */
	public boolean isSynchronizing(){
		return bAlreadySynchronizing;
	}
	
	/**
	 * Mount the device
	 */
	public  void mount() throws Exception{
	    mount(true);
	}
	
	
	/**
	 * Mount the device
	 * @param bUIRefresh set wheter the UI should be refreshed
	 */
	public  void mount(boolean bUIRefresh) throws Exception{
		if (bMounted){
			Messages.showErrorMessage("111"); //$NON-NLS-1$
		}
		String sOS = (String)System.getProperties().get("os.name"); //$NON-NLS-1$
		int iExit = 0;
		try{
			if ( !Util.isUnderWindows() && !getMountPoint().trim().equals("")){  //not under windows //$NON-NLS-1$ //$NON-NLS-2$
				//look to see if the device is already mounted ( the mount command cannot say that )
				File file = new File(getMountPoint());
				if ( file.exists() && file.list().length == 0){//if none file in this directory, it probably means device is not mounted, try to mount it
					Process process = Runtime.getRuntime().exec("mount "+getMountPoint());//run the actual mount command //$NON-NLS-1$
					iExit = process.waitFor(); //just make a try, do not report error if it fails (linux 2.6 doesn't require anymore to mount devices)
				}
			}
			else{  //windows mount point or mount point not given, check if path exists 
				File file = new File(getUrl());
				if ( !file.exists() ){
					throw new Exception();
				}
			}
		}
		catch(Exception e){
			throw new JajukException("011",getName(),e); //$NON-NLS-1$
		}
		bMounted = true;
		//notify views to refresh if needed
		if ( bUIRefresh ){
		    ObservationManager.notify(EVENT_DEVICE_MOUNT);
		}
	}
	
	/**
	 * Unmount the device
	 *
	 */
	public  void unmount() throws Exception{
		unmount(false,true);
	}
	
	
	/**
	 * Unmount the device with ejection 
	 * @param bEjection set whether the device must be ejected
	 * @param bUIRefresh set wheter the UI should be refreshed
	 */
	public  void unmount(boolean bEjection,boolean bUIRefresh) throws Exception{
		//look to see if the device is already mounted ( the unix 'mount' command cannot say that )
		File file = new File(getMountPoint());
		if (!bMounted ){
			Messages.showErrorMessage("125"); //already unmounted //$NON-NLS-1$
			return;
		}
		//ask fifo if it doens't use any track from this device
		if (!FIFO.canUnmount(this)){ 
			Messages.showErrorMessage("121"); //$NON-NLS-1$
			return;
		}
		String sOS = (String)System.getProperties().get("os.name"); //$NON-NLS-1$
		int iExit = 0;
		if (sOS.trim().toLowerCase().lastIndexOf("windows")==-1 && !getMountPoint().trim().equals("")){  //not a windows //$NON-NLS-1$ //$NON-NLS-2$
			try{
				//we try to unmount the device if under Unix. Note that this is useless most of the time with Linux 2.6+, so it's just a try and we don't check exit code anymore
				Process process = Runtime.getRuntime().exec("umount "+getMountPoint()); //$NON-NLS-1$
				iExit = process.waitFor();
				if ( bEjection){  //jection if required
					process = Runtime.getRuntime().exec("eject "+getMountPoint()); //$NON-NLS-1$
					process.waitFor();
				}
			}
			catch(Exception e){
				Log.error("012",Integer.toString(iExit),e);	//mount failed //$NON-NLS-1$
				Messages.showErrorMessage("012",getName()); //$NON-NLS-1$
				return;
			}
		}
		bMounted = false;
		if (bUIRefresh) {
		    ObservationManager.notify(EVENT_DEVICE_UNMOUNT);
		}
	}
	
	
	
	/**
	 * Test device accessibility
	 *@return true if the device is available
	 */
	public boolean test(){
		Util.waiting(); //waiting cursor
	    boolean bOK = false;
		try {
            //just wait a moment  so user feels something real happens (psychological)
            Thread.sleep(250);
        } catch (InterruptedException e2) {
            Log.error(e2);
        }
		boolean bWasMounted = bMounted;  //store mounted state of device before mount test
		try{
			if (!bMounted){
				mount(false);  //try to mount
			}
		}
		catch(Exception e){
			Messages.showErrorMessage("112"); //$NON-NLS-1$
			Util.stopWaiting();
			return false;
		}
		if ( iDeviceType != 2 ){ //not a remote device
			File file = new File(sUrl);
			if ( file.exists() && file.canRead()){ //see if the url exists and is readable
				//check if this device was void
				boolean bVoid = true;
				Iterator it = FileManager.getFiles().iterator();
				while (it.hasNext()){
					org.jajuk.base.File f = (org.jajuk.base.File)it.next();
					if (f.getDirectory().getDevice().equals(this)){ //at least one fiel in this device
						bVoid = false;
						break;
					}
				}
				if (!bVoid){  //if the device is not supposed to be void, check if it is the case, if no, the device must not be unix-mounted
					if (file.list().length > 0){
						bOK = true; 
					}
				}
				else{  //device is void, OK we assume it is accessible
					bOK = true;
				}
			}
		}
		else{
			bOK = false; //TBI
		}
		//unmount the device if it was mounted only for the test 
		if (!bWasMounted){
			try {
				unmount(false,false);
			} catch (Exception e1) {
				Log.error(e1);
			}
		}
		Util.stopWaiting();
		return bOK;
	}
	
	/**
	 * @return Returns the unix mount point.
	 */
	public String getMountPoint() {
		return sMountPoint;
	}
	
	
	/**
	 * @param mountPoint The sMountPoint to set.
	 */
	protected void setMountPoint(String sMountPoint) {
		this.sMountPoint = sMountPoint;
	}
	
	
	/**
	 *Alphabetical comparator used to display ordered lists of devices
	 *@param other device to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
		Device otherDevice = (Device)o;
		return  getName().compareToIgnoreCase(otherDevice.getName());
	}
	
	/**
	 * return child files recursively
	 * @return child files recursively
	 */
	public ArrayList getFilesRecursively() {
		//looks for the root directory for this device
		Directory dirRoot = null;
		ArrayList alDirs = DirectoryManager.getDirectories();
		Iterator it = alDirs.iterator();
		while (it.hasNext()){
			Directory dir = (Directory)it.next();
			if ( dir.getDevice().equals(this) && dir.getFio().equals(fio)){
				dirRoot = dir;
			}
		}
		ArrayList alFiles = new ArrayList(100);
		if (dirRoot != null){
			alFiles = dirRoot.getFilesRecursively();
		}
		return alFiles;
	}
	
	/**Return true if the device can be accessed right now 
	 * @return true the file can be accessed right now*/
	public boolean isReady(){
		if ( this.isMounted() && !this.isRefreshing() && !this.isSynchronizing()){
			return true;
		}
		return false;
	}
	
	/**
	 * @return Returns the IO file reference to this directory.
	 */
	public File getFio() {
		return fio;
	}
	
}
