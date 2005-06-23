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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.SequentialMap;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage files
 * @Author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class FileManager extends ItemManager{
	/** Files collection : id-> file*/ 
	private static HashMap hmIdFile = new HashMap(1000);
	/** Map ids and properties, survives to a refresh, is used to recover old properties after refresh */
	private static HashMap hmIdProperties = new HashMap(1000);
	/**Flag the fact a rate has change for a track, used by bestof view refresh for perfs*/
	private static boolean bRateHasChanged = true;
	/**Best of files*/
	private static ArrayList alBestofFiles = new ArrayList(20);
    /**Novelties files*/
    private static ArrayList alNovelties = new ArrayList(20);
    /**Sorted files*/
	private static ArrayList alSortedFiles = new ArrayList(1000);
    /**Self instance*/
    static FileManager singleton;
	
	/**
	 * No constructor available, only static access
	 */
	private FileManager() {
		super();
	}

    /**
     * @return singleton
     */
    public static ItemManager getInstance(){
      if (singleton == null){
          singleton = new FileManager();
      }
        return singleton;
    }
    
	/**
	 * Register an File with a known id
	 * 
	 * @param sName
	 */
	public static synchronized File registerFile(String sId, String sName, Directory directory, Track track, long lSize, String sQuality) {
		File file = new File(sId, sName, directory, track, lSize, sQuality);
		if ( !hmIdFile.containsKey(sId)){
			hmIdFile.put(sId,file);
			alSortedFiles.add(file);
			if ( directory.getDevice().isRefreshing() && Log.isDebugEnabled()){
				Log.debug("registrated new file: "+ file); //$NON-NLS-1$
			}
            SequentialMap properties = (SequentialMap)hmIdProperties.get(sId); 
			if ( properties  == null){  //new file
				hmIdProperties.put(sId,file.getProperties());
			}
			else{  //reset properties before refresh
				file.setProperties(properties);
			}
		}
		return file;
	}

	/**
	 * Clean all references for the given device
	 * @param sId : Device id
	 */
	public static synchronized void cleanDevice(String sId) {
		Iterator it = hmIdFile.values().iterator();
		while (it.hasNext()) {
			File file = (File) it.next();
			if (file.getDirectory()==null || file.getDirectory().getDevice().getId().equals(sId)) {
				it.remove();  //this is the right way to remove entry in the hashmap
			}
		}
		//cleanup sorted array
		it = alSortedFiles.iterator();
		while (it.hasNext()){
			File file = (File) it.next();
			if (file.getDirectory() == null || file.getDirectory().getDevice().getId().equals(sId)) {
				it.remove();  //this is the right way to remove entry 
			}
		}
	}

	/** Return all registred files */
	public static synchronized ArrayList getFiles() {
		if (alSortedFiles.size() == 0){
		    alSortedFiles = new ArrayList(hmIdFile.values());
		    sortFiles();
		}
	    return alSortedFiles;
	}
	
	/** Sorts collection*/
	public static synchronized void sortFiles() {
		Collections.sort(alSortedFiles);
		Log.debug("Collection sorted"); //$NON-NLS-1$
	} 

	
	/**
	   * Return file by id
	   * 
	   * @param id
	   * @return
	   */
	public static synchronized File getFileById(String sId) {
		return (File)hmIdFile.get(sId);
	}
    
    /**
       * Return file by full path
       * @param sPath : full path
       * @return file or null if given path is not known
       */
    
    public static synchronized File getFileByPath(String sPath) {
        File fOut = null;
        java.io.File fToCompare = new java.io.File(sPath);
        if (!fToCompare.exists()){ //check that file exists
            return null;
        }
        Iterator it = hmIdFile.values().iterator();
        while ( it.hasNext()){
            File file = (File)it.next();
            if (file.getIO().equals(fToCompare)){ //we compare io files and not paths
                //to avoid dealing with path name issues
                fOut = file;
                break;
            }
        }
        return fOut;
    }
	
	/**
	 * @return All accessible files of the collection
	 */
	public static synchronized ArrayList getReadyFiles(){
	    // create a tempory table to remove unmounted files
		ArrayList alEligibleFiles = new ArrayList(1000);
		Iterator it = hmIdFile.values().iterator();
		while ( it.hasNext()){
			File file = (File)it.next();
			if (file.isReady()){
				alEligibleFiles.add(file);
			}
		}
		return alEligibleFiles;
	}
	
	/**
	 * Return a shuffle mounted file from the entire collection
	 * @return
	 */
	public static synchronized File getShuffleFile(){
	    ArrayList alEligibleFiles = getReadyFiles();
		if (alEligibleFiles.size() ==0 ){
			return null;
		}
		return (File)alEligibleFiles.get((int)(Math.random()*alEligibleFiles.size()));
	}
	
	/**
	 * Return a playlist with the entire accessible shuffle collection 
	 * @return The entire accessible shuffle collection
	 */
	public static synchronized ArrayList getGlobalShufflePlaylist(){
	    ArrayList alEligibleFiles = getReadyFiles();
	    Collections.shuffle(alEligibleFiles);
	    return alEligibleFiles;
	}
	
	/**
	 * Return a shuffle mounted file from the noveties
	 * @return
	 */
	public static synchronized File getNoveltyFile(){
		ArrayList alEligibleFiles = getGlobalNoveltiesPlaylist();
	    return (File)alEligibleFiles.get((int)(Math.random()*alEligibleFiles.size()));
	}
	
    /**
     * Return a playlist with the entire accessible shuffled novelties collection 
     * @return The entire accessible novelties collection or null if none track in given time interval
     */
    public static synchronized ArrayList getGlobalNoveltiesPlaylist(){
        return getGlobalNoveltiesPlaylist(true);
    }
    
    /**
	 * Return a playlist with the entire accessible novelties collection 
	 * @param bHideUnmounted 
     * @return The entire accessible novelties collection or null if none track in given time interval
	 */
	public static synchronized ArrayList getGlobalNoveltiesPlaylist(boolean bHideUnmounted){
	    ArrayList alEligibleFiles = new ArrayList(1000);
        Iterator it = TrackManager.getTracks().iterator(); //search in tracks, not files to avoid duplicates items
		while ( it.hasNext()){
            Track track = (Track)it.next();
            File file = track.getPlayeableFile(bHideUnmounted); //try to get a mounted file (can return null)
			if (file == null){//none mounted file, take first file we find
		        continue;
            }
        	int iTrackAge = 0;
			try{
				int iYear = Integer.parseInt(file.getTrack().getAdditionDate().substring(0,4));
				int iMounth = Integer.parseInt(file.getTrack().getAdditionDate().substring(4,6))-1; //mounth is zero based : jan=0
				int iDay = Integer.parseInt(file.getTrack().getAdditionDate().substring(6,8));
				GregorianCalendar gc = new GregorianCalendar(iYear,iMounth,iDay);
				iTrackAge = (int)((new Date().getTime()-gc.getTime().getTime())/86400000); //)/1000/60/60/24;
			}
			catch(Exception e){ //error like a wrong added date 
			    continue;
			}
			if ( iTrackAge <= ConfigurationManager.getInt(CONF_OPTIONS_NOVELTIES_AGE)){
				alEligibleFiles.add(file);
			}
		}
		if (alEligibleFiles.size() ==0 ){
			return null;
		}
        Collections.sort(alEligibleFiles); //sort alphabeticaly
        //now sort by date
        File.setSortByDate(true);
        Collections.sort(alEligibleFiles); //sort by date
        File.setSortByDate(false);//reset to default sorting method
        return alEligibleFiles;
	}
	
	/**
	 * Return a shuffle mounted file from the entire collection with rate weight ( best of mode )
	 * @return
	 */
	public static synchronized File getBestOfFile(){
		TreeSet ts = getSortedByRate();
		FileScore fscore = (FileScore)ts.last();
	    return fscore.getFile(); //return highest score file
	}
	
	/**
	 * 
	 * @return a sorted set of the collection by rate, lowest first
	 */
	private static synchronized TreeSet getSortedByRate(){
		//create a tempory table to remove unmounted files
		TreeSet tsEligibleFiles = new TreeSet();
		Iterator it = TrackManager.getTracks().iterator(); //search in tracks, not files to avoid duplicates items
		while ( it.hasNext()){
			File file = ((Track)it.next()).getPlayeableFile(); //can return null
			if (file!= null && file.isReady()){ //test if file is null!
				long lRate = file.getTrack().getRate();
				long lScore = (long)(Math.random()*(100/(file.getTrack().getSessionHits()+1))*Math.log(lRate));  //computes score for each file ( part of shuffleness, part of hits weight )
				tsEligibleFiles.add(new FileScore(file,lScore));
			}
		}
        return tsEligibleFiles;
	}
	
	/**
	 * Return a playlist with the entire accessible bestof collection, best first
	 * @return The entire accessible bestof collection
	 */
	public static synchronized ArrayList getGlobalBestofPlaylist(){
        TreeSet ts = getSortedByRate();
        ArrayList al = new ArrayList(ts.size());
        Iterator it = ts.iterator();
        while (it.hasNext()){
            FileScore fs = (FileScore)it.next();
            al.add(fs.getFile());
        }
        Collections.reverse(al); //reverse to have best first
        return al;
    }
	
    /**
     * Return CONF_BESTOF_SIZE top files
     * @return top files
     */
    public static synchronized ArrayList getBestOfFiles(){
        return getBestOfFiles(true);
    }
    
    /**
	 * Return CONF_BESTOF_SIZE top files
     * @param bHideUnmounted 
	 * @return top files
	 */
	public static synchronized ArrayList getBestOfFiles(boolean bHideUnmounted){
		if (FileManager.hasRateChanged() || alBestofFiles == null){  //test a rate has changed for perfs
			//clear data
			alBestofFiles.clear();
		    int iNbBestofFiles = Integer.parseInt(ConfigurationManager.getProperty(CONF_BESTOF_SIZE));
			//create a tempory table to remove unmounted files
			ArrayList alEligibleFiles = new ArrayList(iNbBestofFiles);
			Iterator it = TrackManager.getTracks().iterator();
			while ( it.hasNext()){
				Track track = (Track)it.next();
				File file = track.getPlayeableFile(bHideUnmounted);
				if (file != null){
					long lRate = file.getTrack().getRate();
					alEligibleFiles.add(new FileScore(file,lRate));
				}
			}
			Collections.sort(alEligibleFiles);
			Collections.reverse(alEligibleFiles); //reverse score
			int i = 0;
			while (i<alEligibleFiles.size() && i<iNbBestofFiles){
			    File file = ((FileScore)alEligibleFiles.get(i)).getFile();
			    alBestofFiles.add(file);
			    i++;
			}
			FileManager.setRateHasChanged(false);
		}
		return alBestofFiles;
	}
    
  	/** Return next mounted file ( used in continue mode )
	 * @param file : a file
	 * @return next file from entire collection
	 */
	public static synchronized File getNextFile(File file){
	    if (file  == null){
		    return null;
		}
	    File fileNext = null;
		ArrayList alSortedFiles = getFiles();
		//look for a correct file from index to collection end
		boolean bOk = false;
		for (int index=alSortedFiles.indexOf(file)+1;index<alSortedFiles.size();index++){
			fileNext = (File)alSortedFiles.get(index);
			if (fileNext.isReady()){  //file must be on a mounted device not refreshing
				bOk = true;
				break;
			}
		}
		if ( bOk){
			return fileNext;
		}
		else if (!ConfigurationManager.getBoolean(CONF_OPTIONS_RESTART)){
			return null;
		}
		//ok, if we are in restart collection mode, restart from collection begin to file index
		int indexFile = alSortedFiles.indexOf(file);
		for (int index=0; index<indexFile; index++){
		    fileNext = (File)alSortedFiles.get(index);
			if (fileNext.isReady()){  //file must be on a mounted device not refreshing
				bOk = true;
				break;
			}
		}
		if ( bOk){
			return fileNext;
		}
		return null;
	}

	
	/** Return previous mounted file 
	 * @param file : a file
	 * @return previous file from entire collection
	 */
	public static synchronized File getPreviousFile(File file){
		if (file  == null){
		    return null;
		}
	    File filePrevious = null;
		ArrayList alSortedFiles = getFiles();
		//test if this file is the very first one
		if (alSortedFiles.indexOf(file) == 0){
		    Messages.showErrorMessage("128"); //$NON-NLS-1$
		    return null;
		}
		//look for a correct file from index to collection begin
		boolean bOk = false;
		for (int index=alSortedFiles.indexOf(file)-1;index>=0;index--){
			filePrevious = (File)alSortedFiles.get(index);
			if (filePrevious.isReady()){  //file must be on a mounted device not refreshing
				bOk = true;
				break;
			}
		}
		if ( bOk){
			return filePrevious;
		}
		return null;
	}
	
	
	/**
	 * Return whether the given file is the very first file from collection
	 * @param file
	 * @return
	 */
	public static boolean isVeryfirstFile(File file){
	    if (file == null){
	        return false;
	    }
	    return  (alSortedFiles.indexOf(file) == 0);
	}
	
	/**
	 * 
	 * @param file
	 * @return All files in the same directory than the given one
	 */
	public static ArrayList getAllDirectory(File file){
	    if (file == null){
	        return null;
	    }
	    ArrayList alResu = new ArrayList(10);
	    Directory dir = file.getDirectory();
	    Iterator it = getFiles().iterator();
	    while ( it.hasNext()){
	        File f = (File)it.next();
	        Directory d = f.getDirectory();
	        if (d.equals(dir)){
	            alResu.add(f);
	        }
	    }
	    return alResu;
	}
	
	/**
	 * 
	 * @param file
	 * @return All files in the same directory from the given one (includes the one)
	 */
	public static ArrayList getAllDirectoryFrom(File file){
	    if (file == null){
	        return null;
	    }
	    ArrayList alResu = new ArrayList(10);
	    Directory dir = file.getDirectory();
	    Iterator it = getFiles().iterator();
	    boolean bSeenTheOne = false;
	    while ( it.hasNext()){
	        File f = (File)it.next();
	        if (f.equals(file)){
	            bSeenTheOne = true;
	            alResu.add(f);
	        }
	        else{
	            Directory d = f.getDirectory();
		        if (d.equals(dir) && bSeenTheOne){
		            alResu.add(f);
		        }    
	        }
	    }
	    return alResu;
	}
	
	/**
	 * Perform a search in all files names with given criteria
	 * @param sCriteria
	 * @return
	 */
	public static synchronized TreeSet search(String sCriteria){
	 	TreeSet tsResu = new TreeSet(); 
		sCriteria = sCriteria.toLowerCase();
	 	Iterator it = hmIdFile.values().iterator();
	 	while ( it.hasNext()){
	 		File file = (File)it.next();
	 		if ( ConfigurationManager.getBoolean(CONF_OPTIONS_SEARCH_ONLY_MOUNTED) && //if  search in only in mounted devices
	 		        (!file.getDirectory().getDevice().isMounted() || file.getDirectory().getDevice().isRefreshing())){
	 			continue;
	 		}
	 		String sResu = file.toStringSearch();
	 		if ( new StringBuffer(sResu.toLowerCase()).lastIndexOf(sCriteria) != -1 ){
	 			tsResu.add(new SearchResult(file,sResu));
	 		}
	 	}
	 	return tsResu;
	}
	
	
	/**
	 * Return properties assiated to an id
	 * @param sId the id
	 * @return
	 */
	public static synchronized Properties getProperties(String sId){
		return (Properties)hmIdProperties.get(sId);
	}

	/**
	 * @return Returns the bRateHasChanged.
	 */
	public static boolean hasRateChanged() {
		return bRateHasChanged;
	}

	/**
	 * @param rateHasChanged The bRateHasChanged to set.
	 */
	public static void setRateHasChanged(boolean rateHasChanged) {
		bRateHasChanged = rateHasChanged;
	}
    
	/* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_FILES;
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#applyNewProperty()
     */
    public void applyNewProperty(String sProperty){
        Iterator it = getFiles().iterator();
        while (it.hasNext()){
            IPropertyable item = (IPropertyable)it.next();
            item.setProperty(sProperty,null);
        }
    }
     /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#applyRemoveProperty(java.lang.String)
     */
    public void applyRemoveProperty(String sProperty) {
        Iterator it = getFiles().iterator();
        while (it.hasNext()){
            IPropertyable item = (IPropertyable)it.next();
            item.removeProperty(sProperty);
        }
    }
}

/** File score*/
class FileScore implements Comparable{

    /** The score */
	long lScore;

    /**The file*/
    File file;

    /** A File scrore*/
	public FileScore(File file,long lScore){
		this.lScore = lScore;
		this.file = file;
	}

    /**
     * @return Returns the file.*/
    public File getFile() {
        return file;
    }

    /**
     * @return Returns the lScore.*/
    public long getLScore() {
        return lScore;
    }

	
	/** ToString method*/
	public String toString(){
		return new StringBuffer(file.toString()).append(',').append(lScore).toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		FileScore fscore = (FileScore)o;
		if ( fscore.getLScore() < lScore){
			return 1;
		}
		else if ( fscore.getLScore() > lScore){
			return -1;
		}
		return 0;
	}
 
    
    
}
