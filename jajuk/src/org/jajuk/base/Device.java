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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;

/**
 *  A device ( music files repository )
 * <p>Some properties of a device are immuable : name, url and type
 * *<p> Physical item
 * @Author     Bertrand Florat
 * @created    17 oct. 2003
 */
public class Device extends Item implements ITechnicalStrings, Comparable{
    
    private static final long serialVersionUID = 1L;
	/**Device URL (used for perfs)*/
    private String sUrl;
    /** IO file for optimizations* */
    private java.io.File fio;
    /**Device mount point**/
    private String sMountPoint = ""; //$NON-NLS-1$
    /**Mounted device flag*/
    private boolean bMounted;
    /**directories*/
    private ArrayList<Directory> alDirectories = new ArrayList<Directory>(20);
    /**Already refreshing flag*/
    private volatile boolean bAlreadyRefreshing = false;
    /**Already synchronizing flag*/
    private volatile boolean bAlreadySynchronizing = false;
    /** Number of files in this device before refresh ( for refresh stats ) */
    public int iNbFilesBeforeRefresh;
    /** Number of dirs in this device before refresh*/
    public int iNbDirsBeforeRefresh;
    /** Number of new files found during refresh for stats*/
    public int iNbNewFiles;
    /** Number of corrupted files found during refresh for stats*/
    public int iNbCorruptedFiles;
    /** Number of created files on source device during synchro ( for stats ) */
    public int iNbCreatedFilesSrc;
    /** Number of created files on destination device during synchro ( for stats ) */
    public int iNbCreatedFilesDest;
    /** Number of deleted files during a synchro ( for stats ) */
    int iNbDeletedFiles = 0;
    /**Volume of created files during synchro */
    long lVolume = 0;
    /**date last refresh*/
    long lDateLastRefresh;
    /**Refresh message*/
    private String sFinalMessage = "";  //$NON-NLS-1$
    
    /**
     * Device constructor
     * @param sId
     * @param sName
     * @param iDeviceType
     * @param sUrl
     */
    public Device(String sId, String sName) {
        super(sId,sName);
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.Item#getIdentifier()
     */
    final public String getIdentifier() {
        return XML_DEVICE;
    }
    
    /**
     * toString method
     */
    public String toString() {
        return "Device[ID=" + sId + " Name=" + sName + " Type=" +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        DeviceManager.getInstance().getDeviceType(getLongValue(XML_TYPE)) +
        " URL=" + sUrl+ " Mount point="+sMountPoint + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
    }
    
    /**
     * Equal method to check two devices are identical
     * @param otherDevice
     * @return
     */
    public boolean equals(Object otherDevice) {
        if (otherDevice == null){
            return false;
        }
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
     * @param bAsynchronous : set asynchonous or synchronous mode
     * @param bAsk: should we ask user if he wants to perform a deep or fast scan? default=deep
     */
    public void refresh(boolean bAsynchronous) {
        refresh(bAsynchronous,false);
    }
    
    /**
     * Refresh : scan asynchronously the device to find tracks
     * @param bAsynchronous : set asynchonous or synchronous mode
     * @param bAsk: should we ask user if he wants to perform a deep or fast scan? default=deep
     */
    public void refresh(boolean bAsynchronous,final boolean bAsk) {
        final Device device = this;
        if ( !device.isMounted()){
            try{
                device.mount();  
            }
            catch(Exception e){
                Log.error("011","{{"+getName()+"}}",e);	//mount failed //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                Messages.showErrorMessage("011",getName()); //$NON-NLS-1$
                return;
            }
        }
        if (bAlreadyRefreshing){
            Messages.showErrorMessage("107"); //$NON-NLS-1$
            return;
        }
        if ( bAsynchronous){
            Thread t = new Thread(){
                public void run(){
                    manualRefresh(bAsk);
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
        else{
            manualRefresh(bAsk);
        }
        
    }
    
    /**
     * Manual refresh
     *@param bAsk: should we ask user if a deep or fast scan is required? default=deep
     */
    private void manualRefresh(boolean bAsk){
        int i = 1; 
        if (bAsk){
            Object[] possibleValues = { Messages.getString("PhysicalTreeView.60"),//fast //$NON-NLS-1$
                    Messages.getString("PhysicalTreeView.61"),//deep //$NON-NLS-1$
                    Messages.getString("Cancel")};//cancel //$NON-NLS-1$
            //0:fast, 1:deep, 2: cancel
            i = JOptionPane.showOptionDialog(null,
                Messages.getString("PhysicalTreeView.59"), //$NON-NLS-1$
                Messages.getString("Option"), //$NON-NLS-1$
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                possibleValues,
                possibleValues[0]);
            if (i == 2){ //Cancel
                return;
            }
        }
        //clean old files up
        cleanRemovedFiles();
        //Actual refresh
        refreshCommand((i == 1));
        InformationJPanel.getInstance().setMessage(sFinalMessage,InformationJPanel.INFORMATIVE); //$NON-NLS-1$
        //notify views to refresh
        ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH));
        //cleanup logical items
        TrackManager.getInstance().cleanup();
        StyleManager.getInstance().cleanup();
        AlbumManager.getInstance().cleanup();
        AuthorManager.getInstance().cleanup();
        PlaylistManager.getInstance().cleanup();
        //commit collection at each refresh (can be useful if application is closed brutally with control-C or shutdown and that exit hook have no time to perform commit)
        try {
            org.jajuk.base.Collection.commit(FILE_COLLECTION);
        }
        catch (IOException e) {
            Log.error(e);
        }
        
    }
    
    /**
     * The refresh itself
     * 
     * @return true if some changes occured in device
     * 
     */
    protected synchronized boolean refreshCommand(boolean bDeepScan){
        try{
            bAlreadyRefreshing = true;
            long lTime = System.currentTimeMillis();
            lDateLastRefresh = lTime;
            //check Jajuk is not exiting because a refresh cannot start in this state
            if (Main.bExiting){
                return false;
            }
            //check if this device is mounted (usefull when called by automatic refresh)
            if (!isMounted()){
                return false;
            }
            /*check target directory is not void because it could mean that the device is not actually system-mounted and
             then a refresh would clear the device, display a warning message*/
            File file = new File(getUrl());
            if ( file.exists() && (file.list() == null || file.list().length==0)){
                int i = Messages.getChoice(Messages.getString("Confirmation_void_refresh"),JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                if ( i != JOptionPane.OK_OPTION){
                    return false;
                }
            }
            iNbFilesBeforeRefresh = FileManager.getInstance().getElementCount();
            iNbDirsBeforeRefresh = DirectoryManager.getInstance().getElementCount();
            iNbNewFiles = 0;
            iNbCorruptedFiles = 0;
            if (bDeepScan && Log.isDebugEnabled()){
                Log.debug("Starting refresh of device : "+this); //$NON-NLS-1$    
            }
            File fTop = new File(getStringValue(XML_URL));
            if (!fTop.exists()) {
                Messages.showErrorMessage("101"); //$NON-NLS-1$
                return false;
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
            if (!getDeviceTypeS().equals(DEVICE_TYPE_REMOTE) 
                    || !getDeviceTypeS().equals(DEVICE_TYPE_AUDIO_CD)){
                Directory d = DirectoryManager.getInstance().registerDirectory(this);
                dParent = d;
                d.scan(bDeepScan);
            }
            //Start actual scan
            while (iDeep >= 0 && !Main.isExiting()) {
                File[] files = fCurrent.listFiles(Util.dirFilter); //only directories
                if (files== null || files.length == 0 ){  //files is null if fCurrent is a not a directory 
                    indexTab[iDeep] = -1;//re-init for next time we will reach this deep
                    iDeep--; //come up
                    fCurrent = fCurrent.getParentFile();
                    dParent = dParent.getParentDirectory();
                } else {
                    if (indexTab[iDeep] < files.length-1 ){  //enter sub-directory
                        indexTab[iDeep]++; //inc index for next time we will reach this deep
                        fCurrent = files[indexTab[iDeep]];
                        dParent = DirectoryManager.getInstance().registerDirectory(fCurrent.getName(),dParent,this);
                        if (bDeepScan){
                            InformationJPanel.getInstance().setMessage(new StringBuffer(Messages.getString("Device.21")).append(this.getName()).append(Messages.getString("Device.22")).append(dParent.getRelativePath()).append("]").toString(),InformationJPanel.INFORMATIVE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                        dParent.scan(bDeepScan);
                        iDeep++;
                    }
                    else{
                        indexTab[iDeep] = -1;
                        iDeep --;
                        fCurrent = fCurrent.getParentFile();
                        if (dParent != null){
                            dParent = dParent.getParentDirectory();
                        }
                    }
                }					
            }
            
            //Display end of refresh message with stats
            lTime = System.currentTimeMillis()-lTime;
            StringBuffer sbOut = new StringBuffer("[").append(getName()).append(Messages.getString("Device.25")). //$NON-NLS-1$ //$NON-NLS-2$
            append(((lTime < 1000)?lTime+" ms":lTime/1000+" s")). //$NON-NLS-1$ //$NON-NLS-2$
            append(" - ").append(iNbNewFiles).append(Messages.getString("Device.27")); //$NON-NLS-1$ //$NON-NLS-2$
            if (iNbCorruptedFiles > 0){
                sbOut.append(" - ").append(iNbCorruptedFiles).append(Messages.getString("Device.43")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            sFinalMessage = sbOut.toString();
            Log.debug(sFinalMessage); 
            //refresh requiered if nb of files or dirs changed
            if ((FileManager.getInstance().getElementCount()-iNbFilesBeforeRefresh) != 0
                    || (DirectoryManager.getInstance().getElementCount()-iNbDirsBeforeRefresh) != 0){
                return true;
            }
            return false;
        }
        catch(RuntimeException re){ //runtime error are thrown
            throw re;
        }
        catch(Exception e){ //and regular ones logged
            Log.error(e);
            return false;
        }
        finally{  //make sure to unlock refreshing even if an error occured
            bAlreadyRefreshing = false;
        }
    }
    
    
    /**
     * Synchroning asynchronously 
     * @param bAsynchronous : set asynchronous or synchronous mode
     * @return
     */
    public void synchronize(boolean bAsynchronous) {
        final Device device = this;
        if ( !device.isMounted()){
            try{
                device.mount();  
            }
            catch(Exception e){
                Log.error("011","{{"+getName()+"}}",e);	//mount failed //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                Messages.showErrorMessage("011",getName()); //$NON-NLS-1$
                return;
            }
        }
        if ( bAsynchronous){
            Thread t = new Thread(){
                public void  run() {
                    synchronizeCommand();
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
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
            bAlreadySynchronizing = true;
            long lTime = System.currentTimeMillis();				
            iNbCreatedFilesDest = 0;
            iNbCreatedFilesSrc = 0;
            iNbDeletedFiles = 0;
            lVolume = 0;
            boolean bidi = (getValue(XML_DEVICE_SYNCHRO_MODE).equals(DEVICE_SYNCHRO_MODE_BI));
            //check this device is synchronized
            String sIdSrc = (String)getValue(XML_DEVICE_SYNCHRO_SOURCE); 
            if ( sIdSrc == null || sIdSrc.equals(getId())){  //cannot synchro with itself
                return;
            }
            Device dSrc = (Device)DeviceManager.getInstance().getItem(sIdSrc);
             //perform a fast refresh
            this.refreshCommand(false);
            //if bidi sync, refresh the other device as well (new file can have been copied to it)
            if (bidi){
                dSrc.refreshCommand(false);
            }
            //start message
            InformationJPanel.getInstance().setMessage(new StringBuffer(Messages.getString("Device.31")). //$NON-NLS-1$
                append(dSrc.getName()).append(',').append(this.getName()).append("]"). //$NON-NLS-1$
                toString(),InformationJPanel.INFORMATIVE);
            //in both cases (bi or uni-directional), make an unidirectional sync from source device to this one
            iNbCreatedFilesDest = synchronizeUnidirectonal(dSrc,this);
            //now the other one if bidi
            iNbCreatedFilesDest += synchronizeUnidirectonal(this,dSrc);
            //end message
            lTime = System.currentTimeMillis()-lTime;
            String sOut = new StringBuffer(Messages.getString("Device.33")).append(((lTime < 1000)?lTime+" ms":lTime/1000+" s")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            .append(" - ").append(iNbCreatedFilesSrc+iNbCreatedFilesDest).append(Messages.getString("Device.35")). //$NON-NLS-1$ //$NON-NLS-2$
            append(lVolume/1048576).append(Messages.getString("Device.36")).toString(); //$NON-NLS-1$
            //perform a fast refresh
            this.refreshCommand(false);
            //if bidi sync, refresh the other device as well (new file can have been copied to it)
            if (bidi){
                dSrc.refreshCommand(false);
            }
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
     * Synchronize a device with another one (unidirectional)
     *@param device : device to synchronize
     *@return nb of created files
     */
    private int synchronizeUnidirectonal(Device dSrc,Device dest){
        Iterator it = null;
        HashSet<Directory> hsSourceDirs = new HashSet<Directory>(100);
        HashSet<String> hsDesynchroPaths = new HashSet<String>(10); //contains paths ( relative to device) of desynchronized dirs
        HashSet<Directory> hsDestDirs = new HashSet<Directory>(100);
        int iNbCreatedFiles = 0;
        synchronized(DirectoryManager.getInstance().getLock()){
           it = DirectoryManager.getInstance().getItems().iterator();
            while ( it.hasNext()){
                Directory dir = (Directory)it.next();
                if ( dir.getDevice().equals(dSrc)){
                    if ( dir.getBooleanValue(XML_DIRECTORY_SYNCHRONIZED)){  //don't take desynchronized dirs into account //$NON-NLS-1$
                        hsSourceDirs.add(dir);
                    }
                    else{
                        hsDesynchroPaths.add(dir.getRelativePath());	
                    }
                }
            }
            it = DirectoryManager.getInstance().getItems().iterator();
            while ( it.hasNext()){
                Directory dir = (Directory)it.next();
                if ( dir.getDevice().equals(dest)){
                    if ( dir.getBooleanValue(XML_DIRECTORY_SYNCHRONIZED)){  //don't take desynchronized dirs into account //$NON-NLS-1$
                        hsDestDirs.add(dir);
                    }
                    else{
                        hsDesynchroPaths.add(dir.getRelativePath());
                    }
                }
            }
        }
        it = hsSourceDirs.iterator();
        Iterator it2;
        FileFilter filter = new JajukFileFilter(false,new FileFilter[]{
                    JajukFileFilter.KnownTypeFilter.getInstance(),JajukFileFilter.ImageFilter.getInstance()}); //concidere known exte,sions and image files
        while ( it.hasNext()){
            //give a chance to exit during sync
            if (Main.isExiting()){
                return iNbCreatedFiles;
            }
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
                            Util.copyToDir(fSrcFiles[i],fileNewDir);
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
                            Log.error("020","{{"+fSrcFiles[i].getAbsolutePath()+"}}",e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
        return DeviceManager.getInstance().getDeviceType(getLongValue(XML_TYPE));
    }
    
    /**
     * @return
     */
    public long getDeviceType() {
        return getLongValue(XML_TYPE);
    }
    
    
    /**
     * @return
     */
    public String getUrl() {
        return sUrl;
    }
    
    /**
     * @param url The sUrl to set.
     */
    public void setUrl(String url) {
        synchronized (DeviceManager.getInstance().getLock()){
            this.sUrl = url;
            setProperty(XML_URL,url);
            this.fio = new File(url);
            /**Rest files*/
            synchronized(FileManager.getInstance().getLock()){
                Iterator it = FileManager.getInstance().getItems().iterator();
                while (it.hasNext()) {
                    org.jajuk.base.File file = (org.jajuk.base.File) it.next();
                    file.reset();
                }
            }
            /**Rest playlist files*/
            synchronized(PlaylistFileManager.getInstance().getLock()){
                Iterator it = PlaylistFileManager.getInstance().getItems().iterator();
                while (it.hasNext()) {
                    org.jajuk.base.PlaylistFile plf = (org.jajuk.base.PlaylistFile) it.next();
                    plf.reset();
                }
            }
        }
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
        try{
            if ( !Util.isUnderWindows() && !getMountPoint().trim().equals("")){  //not under windows //$NON-NLS-1$ //$NON-NLS-2$
                //look to see if the device is already mounted ( the mount command cannot say that )
                File file = new File(getMountPoint());
                if ( file.exists() && file.list().length == 0){//if none file in this directory, it probably means device is not mounted, try to mount it
                    Process process = Runtime.getRuntime().exec("mount "+getMountPoint());//run the actual mount command //$NON-NLS-1$
                    process.waitFor(); //just make a try, do not report error if it fails (linux 2.6 doesn't require anymore to mount devices)
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
        //Cannot mount void devices because of reference garbager thread
        File file = new File(getUrl());
        if (file.listFiles() != null && file.listFiles().length > 0){
            bMounted = true;    
        }
        //notify views to refresh if needed
        if ( bUIRefresh ){
            ObservationManager.notify(new Event(EVENT_DEVICE_MOUNT));
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
        new File(getMountPoint());
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
            ObservationManager.notify(new Event(EVENT_DEVICE_UNMOUNT));
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
            //just wait a moment so user feels something real happens (psychological)
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
            Util.stopWaiting();
            return false;
        }
        if ( getLongValue(XML_TYPE) != 5 ){ //not a remote device
            File file = new File(sUrl);
            if ( file.exists() && file.canRead()){ //see if the url exists and is readable
                //check if this device was void
                boolean bVoid = true;
                synchronized(FileManager.getInstance().getLock()){
                    Iterator it = FileManager.getInstance().getItems().iterator();
                    while (it.hasNext()){
                        org.jajuk.base.File f = (org.jajuk.base.File)it.next();
                        if (f.getDirectory().getDevice().equals(this)){ //at least one fiel in this device
                            bVoid = false;
                            break;
                        }
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
     *Alphabetical comparator used to display ordered lists of devices
     *@param other device to be compared
     *@return comparaison result 
     */
    public int compareTo(Object o){
        Device otherDevice = (Device)o;
        return getName().compareToIgnoreCase(otherDevice.getName());
    }
    
    /**
     * return child files recursively
     * @return child files recursively
     */
    public ArrayList getFilesRecursively() {
        synchronized(DirectoryManager.getInstance().getLock()){
            //looks for the root directory for this device
            Directory dirRoot = null;
            Collection dirs = DirectoryManager.getInstance().getItems();
            Iterator it = dirs.iterator();
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
    
    /**
     * Get item description
     */
    public String getDesc(){
        return Messages.getString("Item_Device")+" : "+getName(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
     */
    public String getHumanValue(String sKey){
        if (XML_TYPE.equals(sKey)){
            long lType = getLongValue(sKey);
            return DeviceManager.getInstance().getDeviceType(lType);
        }
        else{//default
            return super.getHumanValue(sKey);
        }
    }
    
    public long getDateLastRefresh() {
        return lDateLastRefresh;
    }
    
    /**
     * Scan directories to cleanup removed files and playlist files
     * @param device device to cleanup
     * @return whether some items have been removed
     */
    public boolean cleanRemovedFiles(){
        boolean bChanges = false;
        long l = System.currentTimeMillis();
        //need to use a shallow copy to avoid concurent exceptions
        ArrayList<Directory> alDirs = null;
        synchronized(DirectoryManager.getInstance().getLock()){
        	Collection<Item> items = DirectoryManager.getInstance().getItems();
            alDirs = new ArrayList<Directory>(Arrays.asList(items.toArray(new Directory[items.size()])));
        }
        for (Item item:alDirs){
            Directory dir = (Directory)item;
            if (!Main.isExiting()
                    &&dir.getDevice().equals(this) 
                    && dir.getDevice().isMounted()){ 
                if (!dir.getFio().exists()){
                    //note that associated files are removed too
                    synchronized(DirectoryManager.getInstance().getLock()){
                        DirectoryManager.getInstance().removeDirectory(dir.getId());
                    }
                    Log.debug("Removed: "+dir); //$NON-NLS-1$
                    bChanges = true;
                }
            }
        }
        
        ArrayList<org.jajuk.base.File> alFiles = null;
        synchronized(FileManager.getInstance().getLock()){
        	Collection<Item> items = FileManager.getInstance().getItems();
            alFiles = new ArrayList<org.jajuk.base.File>(Arrays.asList(items.toArray(new org.jajuk.base.File[items.size()])));
        }
        for (org.jajuk.base.File file:alFiles){
            if (!Main.isExiting() 
                    && file.getDirectory().getDevice().equals(this)
                    && file.isReady()){
                if (!file.getIO().exists()){
                    synchronized(FileManager.getInstance().getLock()){
                        FileManager.getInstance().removeFile(file);
                    }
                    Log.debug("Removed: "+file); //$NON-NLS-1$
                    bChanges = true;
                }
            }
        }
        ArrayList<PlaylistFile> alplf = null;
        synchronized(PlaylistFileManager.getInstance().getLock()){
        	Collection<Item> items = PlaylistFileManager.getInstance().getItems(); 
            alplf = new ArrayList<PlaylistFile>(Arrays.asList(items.toArray(new PlaylistFile[items.size()])));
        }
        for (PlaylistFile plf:alplf){
            if (!Main.isExiting()
                    && plf.getDirectory().getDevice().equals(this)
                    && plf.isReady()){
                if (!plf.getFio().exists()){
                    synchronized(PlaylistFileManager.getInstance().getLock()){
                        PlaylistFileManager.getInstance().removePlaylistFile(plf);
                    }
                    Log.debug("Removed: "+plf); //$NON-NLS-1$
                    bChanges = true;
                }
            }
            
        }    
        //clear history to remove olf files referenced in it
        History.getInstance().clear(Integer.parseInt(ConfigurationManager.getProperty(CONF_HISTORY))); //delete old history items
        
        l = System.currentTimeMillis()-l;
        Log.debug("Old file references cleaned in: " //$NON-NLS-1$
            +((l<1000)?l+" ms":l/1000+" s")); //$NON-NLS-1$ //$NON-NLS-2$
        return bChanges;
    }
    
    /**
     * Set all personnal properties of an XML file for an item (doesn't overwrite existing properties for perfs)
     * 
     * @param attributes :
     *                list of attributes for this XML item
     */
    public void populateProperties(Attributes attributes) {
        for (int i =0 ; i < attributes.getLength(); i++) {
            String sProperty = attributes.getQName(i);
            if (!getProperties().containsKey(sProperty)){
                String sValue = attributes.getValue(i);
                PropertyMetaInformation meta = getMeta(sProperty);
                //compatibility code for <1.1 : auto-refresh is now a double, no more a boolean
                if (meta.getName().equals(XML_DEVICE_AUTO_REFRESH)
                        && (sValue.equalsIgnoreCase(TRUE)||sValue.equalsIgnoreCase(FALSE))) {
                    switch ((int)((Device)this).getDeviceType()){
                    case 0: //directory
                        sValue = "0.5d"; //$NON-NLS-1$
                        break;
                    case 1: //file cd
                        sValue = "0d"; //$NON-NLS-1$
                        break;
                    case 2: //network drive
                        sValue = "0d"; //$NON-NLS-1$
                        break;
                    case 3: //ext dd
                        sValue = "3d";  //$NON-NLS-1$
                        break;
                    case 4: //player
                        sValue = "3d"; //$NON-NLS-1$
                        break;
                    case 5: //P2P
                        sValue = "0d"; //$NON-NLS-1$
                        break;
                    }
                }
                try {
                    setProperty(sProperty, Util.parse(sValue,meta.getType()));
                } catch (Exception e) {
                    Log.error("137",sProperty,e); //$NON-NLS-1$
                }    
            }
        }
    }
    
}


