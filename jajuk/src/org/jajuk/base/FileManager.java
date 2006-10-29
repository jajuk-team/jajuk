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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.iterators.FilterIterator;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage files
 * 
 * @Author Bertrand Florat
 * @created 17 oct. 2003
 */
public class FileManager extends ItemManager implements Observer {
    /**
         * Flag the fact a rate has change for a track, used by bestof view
         * refresh for perfs
         */
    private boolean bRateHasChanged = true;

    /** Best of files */
    private ArrayList<File> alBestofFiles = new ArrayList<File>(20);

    /** Self instance */
    private static FileManager singleton;

    /** File comparator based on rate */
    private Comparator<File> rateComparator = new Comparator<File>() {
	public int compare(File file1, File file2) {
	    long lRate1 = file1.getTrack().getRate();
	    long lRate2 = file2.getTrack().getRate();
	    if (lRate1 == lRate2) {
		return 0;
	    } else if (lRate1 < lRate2) {
		return 1;
	    } else {
		return -1;
	    }
	}
    };

    /**
         * No constructor available, only static access
         */
    private FileManager() {
	super();
	// ---register properties---
	// ID
	registerProperty(new PropertyMetaInformation(XML_ID, false, true,
		false, false, false, String.class, null));
	// Name
	registerProperty(new PropertyMetaInformation(XML_NAME, false, true,
		true, true, false, String.class, null));
	// Directory
	registerProperty(new PropertyMetaInformation(XML_DIRECTORY, false,
		true, true, false, true, String.class, null));
	// Track
	registerProperty(new PropertyMetaInformation(XML_TRACK, false, true,
		true, false, false, String.class, null));
	// Size
	registerProperty(new PropertyMetaInformation(XML_SIZE, false, true,
		true, false, false, Long.class, null));
	// Quality
	registerProperty(new PropertyMetaInformation(XML_QUALITY, false, true,
		true, false, false, Long.class, 0));
	// Date
	registerProperty(new PropertyMetaInformation(XML_FILE_DATE, false,
		false, true, false, false, Date.class, new Date()));
    }

    /**
         * @return singleton
         */
    public static FileManager getInstance() {
	if (singleton == null) {
	    singleton = new FileManager();
	}
	return singleton;
    }

    /**
         * Register an File with a known id
         * 
         * @param sName
         */
    public File registerFile(String sId, String sName, Directory directory,
	    Track track, long lSize, long lQuality) {
	synchronized (FileManager.getInstance().getLock()) {
	    File file = null;
	    if (!hmItems.containsKey(sId)) {
		file = new File(sId, sName, directory, track, lSize, lQuality);
		hmItems.put(sId, file);
		// add to directory
		file.getDirectory().addFile(file);
		if (directory.getDevice().isRefreshing()
			&& Log.isDebugEnabled()) {
		    Log.debug("registrated new file: " + file); //$NON-NLS-1$
		}
	    } else {
		// If file already exist and the track has changed, make changes
		file = (File) hmItems.get(sId);
	    }
	    // add this file to track
	    file.setTrack(track);
	    track.addFile(file);// make sure the file is added
	    return file;
	}
    }

    /**
         * Get file hashcode (ID)
         * 
         * @param sName
         * @param device
         * @param dir
         * @return file ID
         */
    protected static String getID(String sName, Directory dir) {
	return MD5Processor.hash(new StringBuffer(dir.getDevice().getName())
		.append(dir.getRelativePath()).append(sName).toString());
    }

    /**
         * Change a file name
         * 
         * @param fileOld
         * @param sNewName
         * @return new file
         */
    public File changeFileName(org.jajuk.base.File fileOld, String sNewName)
	    throws JajukException {
	synchronized (FileManager.getInstance().getLock()) {
	    // check given name is different

	    if (fileOld.getName().equals(sNewName)) {
		return fileOld;
	    }
	    // check if this file still exists
	    if (!fileOld.getIO().exists()) {
		throw new CannotRenameException("135"); //$NON-NLS-1$
	    }
	    java.io.File fileNew = new java.io.File(fileOld.getIO()
		    .getParentFile().getAbsolutePath()
		    + java.io.File.separator + sNewName);
	    // recalculate file ID
	    Directory dir = fileOld.getDirectory();
	    String sNewId = MD5Processor.hash(new StringBuffer(dir.getDevice()
		    .getName()).append(dir.getDevice().getUrl()).append(
		    dir.getRelativePath()).append(sNewName).toString());
	    // create a new file (with own fio and sAbs)
	    org.jajuk.base.File fNew = new File(sNewId, sNewName, fileOld
		    .getDirectory(), fileOld.getTrack(), fileOld.getSize(),
		    fileOld.getQuality());
	    fNew.setProperties(fileOld.getProperties()); // transfert all
                                                                // properties
                                                                // (inc id and
	    // name)
	    fNew.setProperty(XML_ID, sNewId); // reset new id and name
	    fNew.setProperty(XML_NAME, sNewName); // reset new id and name
	    // check file name and extension
	    if (!(Util.getExtension(fileNew).equals(Util.getExtension(fileOld
		    .getIO())))) { // no
		// extension
		// change
		throw new CannotRenameException("134"); //$NON-NLS-1$
	    }
	    // check if future file exists (under windows, file.exists
                // return true even with
	    // different case so we test file name is different)
	    if (!fileNew.getName().equalsIgnoreCase(fileOld.getName())
		    && fileNew.exists()) {
		throw new CannotRenameException("134"); //$NON-NLS-1$
	    }
	    // try to rename file on disk
	    try {
		fileOld.getIO().renameTo(fileNew);
	    } catch (Exception e) {
		throw new CannotRenameException("134"); //$NON-NLS-1$
	    }
	    // OK, remove old file and register this new file
	    removeFile(fileOld);
	    if (!hmItems.containsKey(sNewId)) {
		hmItems.put(sNewId, fNew);
	    }
	    // notify everybody for the file change
	    Properties properties = new Properties();
	    properties.put(DETAIL_OLD, fileOld);
	    properties.put(DETAIL_NEW, fNew);
	    // change directory reference
	    dir.changeFile(fileOld, fNew);
	    // Notify interested items (like history manager)
	    ObservationManager.notifySync(new Event(
		    EventSubject.EVENT_FILE_NAME_CHANGED, properties));
	    return fNew;
	}
    }

    /**
         * Change a file directory
         * 
         * @param old
         *                old file
         * @param newDir
         *                new dir
         * @return new file or null if an error occurs
         */
    public File changeFileDirectory(File old, Directory newDir) {
	synchronized (FileManager.getInstance().getLock()) {
	    // recalculate file ID
	    String sNewId = MD5Processor.hash(new StringBuffer(newDir
		    .getDevice().getName()).append(newDir.getDevice().getUrl())
		    .append(newDir.getRelativePath()).append(old.getName())
		    .toString());
	    // create a new file (with own fio and sAbs)
	    File fNew = new File(sNewId, old.getName(), newDir, old.getTrack(),
		    old.getSize(), old.getQuality());
	    fNew.setProperties(old.getProperties()); // transfert all
                                                        // properties (inc id)
	    fNew.setProperty(XML_ID, sNewId); // reset new id and name
	    // OK, remove old file and register this new file
	    removeFile(old);
	    if (!hmItems.containsKey(sNewId)) {
		hmItems.put(sNewId, fNew);
	    }
	    return fNew;
	}
    }

    /**
         * Clean all references for the given device
         * 
         * @param sId :
         *                Device id
         */
    public void cleanDevice(String sId) {
	synchronized (FileManager.getInstance().getLock()) {
	    Iterator it = hmItems.values().iterator();
	    while (it.hasNext()) {
		File file = (File) it.next();
		if (file.getDirectory() == null
			|| file.getDirectory().getDevice().getId().equals(sId)) {
		    it.remove(); // this is the right way to remove entry
                                        // in the hashmap
		}
	    }
	    // cleanup sorted array
	    it = hmItems.values().iterator();
	    while (it.hasNext()) {
		File file = (File) it.next();
		if (file.getDirectory() == null
			|| file.getDirectory().getDevice().getId().equals(sId)) {
		    it.remove(); // this is the right way to remove entry
		}
	    }
	}
    }

    /**
         * Remove a file reference
         * 
         * @param file
         */
    public void removeFile(File file) {
	synchronized (FileManager.getInstance().getLock()) {
	    hmItems.remove(file.getId());
	    file.getDirectory().removeFile(file);
	}
    }

    /**
         * Return file by full path
         * 
         * @param sPath :
         *                full path
         * @return file or null if given path is not known
         */

    public File getFileByPath(String sPath) {
	synchronized (FileManager.getInstance().getLock()) {
	    File fOut = null;
	    java.io.File fToCompare = new java.io.File(sPath);
	    Iterator it = hmItems.values().iterator();
	    while (it.hasNext()) {
		File file = (File) it.next();
		if (file.getIO().equals(fToCompare)) { // we compare io files
                                                        // and not paths
		    // to avoid dealing with path name issues
		    fOut = file;
		    break;
		}
	    }
	    return fOut;
	}
    }

    /**
         * @return All accessible files of the collection
         */
    public List<File> getReadyFiles() {
	Set<File> files = null;
	files = FileManager.getInstance().getFiles();
	Iterator it = new FilterIterator(files.iterator(),
		new JajukPredicates.ReadyFilePredicate());
	List<File> out = new ArrayList<File>(files.size() / 2);
	while (it.hasNext()) {
	    out.add((File) it.next());
	}
	return out;
    }

    /**
         * Return a shuffle mounted file from the entire collection
         * 
         * @return
         */
    public File getShuffleFile() {
	int index = (int) (new Random(System.currentTimeMillis()).nextFloat() * hmItems
		.size());
	ArrayList<File> files = new ArrayList<File>(FileManager.getInstance()
		.getFiles());
	if (files.size() == 0) {
	    return null;
	}
	return files.get(index);
    }

    /**
         * Return a playlist with the entire accessible shuffle collection
         * 
         * @return The entire accessible shuffle collection (can return a void
         *         collection)
         */
    public List<File> getGlobalShufflePlaylist() {
	List<File> alEligibleFiles = getReadyFiles();
	Collections.shuffle(alEligibleFiles, new Random(System
		.currentTimeMillis()));
	// song level, just shuffle full collection
	if (ConfigurationManager.getProperty(CONF_GLOBAL_RANDOM_MODE).equals(
		MODE_TRACK)) {
	    return alEligibleFiles;
	}
	// else return shuffle albums
	else {
	    return getShuffledFilesByAlbum(alEligibleFiles);
	}
    }

    /**
         * Return a shuffle mounted file from the noveties
         * 
         * @return
         */
    public synchronized File getNoveltyFile() {
	synchronized (FileManager.getInstance().getLock()) {
	    ArrayList alEligibleFiles = getGlobalNoveltiesPlaylist();
	    return (File) alEligibleFiles
		    .get((int) (Math.random() * alEligibleFiles.size()));
	}
    }

    /**
         * Return a playlist with the entire accessible shuffled novelties
         * collection
         * 
         * @return The entire accessible novelties collection (can return a void
         *         collection)
         */
    public ArrayList<org.jajuk.base.File> getGlobalNoveltiesPlaylist() {
	synchronized (FileManager.getInstance().getLock()) {
	    return getGlobalNoveltiesPlaylist(true);
	}
    }

    /**
         * Return a playlist with the entire accessible novelties collection
         * 
         * @param bHideUnmounted
         * @return The entire accessible novelties collection
         */
    public ArrayList<File> getGlobalNoveltiesPlaylist(boolean bHideUnmounted) {
	ArrayList<File> alEligibleFiles = new ArrayList<File>(1000);
	// take tracks matching required age
	Set<Track> tracks = TrackManager.getInstance().getTracks();
	Iterator it = new FilterIterator(tracks.iterator(),
		new JajukPredicates.AgePredicate(ConfigurationManager
			.getInt(CONF_OPTIONS_NOVELTIES_AGE)));
	while (it.hasNext()) {
	    Track track = (Track) it.next();
	    File file = track.getPlayeableFile(bHideUnmounted);
	    // try to get a mounted file
	    // (can return null)
	    if (file == null) {// none mounted file, take first file we find
		continue;
	    }
	    alEligibleFiles.add(file);
	}
	// sort alphabetinaly and by date, newest first
	Collections.sort(alEligibleFiles, new Comparator<File>() {
	    public int compare(File file1, File file2) {
		String sCompared1 = file1.getTrack().getAdditionDate()
			.getTime()
			+ file1.getAbsolutePath();
		String sCompared2 = file2.getTrack().getAdditionDate()
			.getTime()
			+ file2.getAbsolutePath();
		return sCompared2.compareTo(sCompared1);
	    }
	});
	return alEligibleFiles;
    }

    /**
         * Return a shuffled playlist with the entire accessible novelties
         * collection
         * 
         * @return The entire accessible novelties collection
         */
    public List<File> getShuffleNoveltiesPlaylist() {
	ArrayList<File> alEligibleFiles = getGlobalNoveltiesPlaylist(true);
	// song level, just shuffle full collection
	if (ConfigurationManager.getProperty(CONF_NOVELTIES_MODE).equals(
		MODE_TRACK)) {
	    return alEligibleFiles;
	}
	// else return shuffle albums
	else {
	    return getShuffledFilesByAlbum(alEligibleFiles);
	}
    }

    /**
         * Convenient method used to return shuffled files by album
         * 
         * @param alEligibleFiles
         * @return Shuffled tracks by album
         */
    private List<File> getShuffledFilesByAlbum(List<File> alEligibleFiles) {
	// start with filling a set of albums containing
	// at least one ready file
	HashMap<Album, ArrayList<File>> albumsFiles = new HashMap<Album, ArrayList<File>>(
		alEligibleFiles.size() / 10);
	for (File file : alEligibleFiles) {
	    // maintain a map between each albums and
	    // eligible files
	    Album album = file.getTrack().getAlbum();
	    ArrayList<File> files = albumsFiles.get(album);
	    if (files == null) {
		files = new ArrayList<File>(10);
	    }
	    files.add(file);
	    albumsFiles.put(album, files);
	}
	// build output
	List<File> out = new ArrayList<File>(alEligibleFiles.size());
	ArrayList<Album> albums = new ArrayList<Album>(albumsFiles.keySet());
	// we need to force a new shuffle as internal hashmap arrange items
	Collections.shuffle(albums, new Random(System.currentTimeMillis()));
	for (Album album : albums) {
	    ArrayList<File> files = albumsFiles.get(album);
	    Collections.shuffle(files, new Random(System.currentTimeMillis()));
	    out.addAll(files);
	}
	return out;
    }

    /**
         * @return a sorted set of the collection by rate, highest first
         */
    private List<File> getSortedByRate() {
	// use only mounted files
	List<File> alEligibleFiles = getReadyFiles();
	// now sort by rate
	Collections.sort(alEligibleFiles, rateComparator);
	return alEligibleFiles;
    }

    /**
         * Return a playlist with the entire accessible bestof collection, best
         * first
         * 
         * @return Shuffled best tracks (n% of favorite)
         */
    public ArrayList<File> getGlobalBestofPlaylist() {
	List<File> al = getSortedByRate();
	ArrayList<File> alBest = new ArrayList<File>();
	if (al.size() > 0) {
	    int sup = (int) ((BESTOF_PROPORTION) * al.size()); // find superior
                                                                // interval
                                                                // value
	    if (sup < 0) {
		sup = al.size();
	    }
	    alBest = new ArrayList<File>(al.subList(0, sup - 1));
	    Collections.shuffle(alBest, new Random(System.currentTimeMillis())); // shufflelize
	}
	return alBest;
    }

    /**
         * Return CONF_BESTOF_SIZE top files
         * 
         * @return top files
         */
    public ArrayList getBestOfFiles() {
	return getBestOfFiles(true);
    }

    /**
         * Return CONF_BESTOF_SIZE top files
         * 
         * @param bHideUnmounted
         * @return top files
         */
    public ArrayList getBestOfFiles(boolean bHideUnmounted) {
	// test a rate has changed for perfs
	if (FileManager.getInstance().hasRateChanged() || alBestofFiles == null) {
	    // clear data
	    alBestofFiles.clear();
	    int iNbBestofFiles = Integer.parseInt(ConfigurationManager
		    .getProperty(CONF_BESTOF_SIZE));
	    // create a tempory table to remove unmounted files
	    ArrayList<File> alEligibleFiles = new ArrayList<File>(
		    iNbBestofFiles);
	    Iterator it = TrackManager.getInstance().getTracks().iterator();
	    while (it.hasNext()) {
		Track track = (Track) it.next();
		File file = track.getPlayeableFile(bHideUnmounted);
		if (file != null) {
		    alEligibleFiles.add(file);
		}
	    }
	    Collections.sort(alEligibleFiles, rateComparator);
	    int i = 0;
	    while (i < alEligibleFiles.size() && i < iNbBestofFiles) {
		File file = alEligibleFiles.get(i);
		alBestofFiles.add(file);
		i++;
	    }
	    setRateHasChanged(false);
	}
	return alBestofFiles;
    }

    /**
         * Return next mounted file ( used in continue mode )
         * 
         * @param file :
         *                a file
         * @return next file from entire collection
         */
    public File getNextFile(File file) {
	synchronized (FileManager.getInstance().getLock()) {
	    Set<Item> files = getItems();
	    File fileNext = null;
	    if (file == null) {
		return fileNext;
	    }
	    // look for a correct file from index to collection end
	    boolean bStarted = false;
	    Iterator it = files.iterator();
	    while (it.hasNext()) {
		fileNext = (File) it.next();
		if (bStarted) {
		    if (fileNext.isReady()) {
			return fileNext;
		    }
		} else {
		    if (fileNext.equals(file)) {
			bStarted = true; // OK, begin to concidere files
                                                // from this one
		    }
		}
	    }
	    // ok restart from collection from begining
	    it = files.iterator();
	    while (it.hasNext()) {
		fileNext = (File) it.next();
		if (fileNext.isReady()) { // file must be on a mounted
                                                // device not refreshing
		    return fileNext;
		}
	    }
	    // none ready file
	    return null;
	}
    }

    /**
         * Return previous mounted file
         * 
         * @param file :
         *                a file
         * @return previous file from entire collection
         */
    public File getPreviousFile(File file) {
	synchronized (FileManager.getInstance().getLock()) {
	    Set<Item> files = getItems();
	    if (file == null) {
		return null;
	    }
	    File filePrevious = null;
	    ArrayList<Item> alSortedFiles = new ArrayList<Item>(files);
	    int i = alSortedFiles.indexOf(file);
	    // test if this file is the very first one
	    if (i == 0) {
		Messages.showErrorMessage("128"); //$NON-NLS-1$
		return null;
	    }
	    // look for a correct file from index to collection begin
	    boolean bOk = false;
	    for (int index = i - 1; index >= 0; index--) {
		filePrevious = (File) alSortedFiles.get(index);
		if (filePrevious.isReady()) { // file must be on a mounted
                                                // device not refreshing
		    bOk = true;
		    break;
		}
	    }
	    if (bOk) {
		return filePrevious;
	    }
	    return null;
	}
    }

    /**
         * Return whether the given file is the very first file from collection
         * 
         * @param file
         * @return
         */
    public boolean isVeryfirstFile(File file) {
	synchronized (FileManager.getInstance().getLock()) {
	    Set<Item> files = getItems();
	    if (file == null || hmItems.size() == 0) {
		return false;
	    }
	    Iterator it = files.iterator();
	    File first = (File) it.next();
	    return (file.equals(first));
	}
    }

    /**
         * @param file
         * @return All files in the same directory than the given one
         */
    public Set<File> getAllDirectory(File file) {
	synchronized (getLock()) {
	    Set<Item> files = getItems();
	    if (file == null) {
		return null;
	    }
	    Set<File> out = new TreeSet<File>();
	    Directory dir = file.getDirectory();
	    Iterator it = files.iterator();
	    while (it.hasNext()) {
		File f = (File) it.next();
		Directory d = f.getDirectory();
		if (d.equals(dir)) {
		    out.add(f);
		}
	    }
	    return out;
	}
    }

    /**
         * @param file
         * @return All files in the same directory from the given one (includes
         *         the one)
         */
    public Set<File> getAllDirectoryFrom(File file) {
	synchronized (getLock()) {
	    if (file == null) {
		return null;
	    }
	    Set<Item> files = getItems();
	    Set<File> out = new TreeSet<File>();
	    Directory dir = file.getDirectory();
	    Iterator it = files.iterator();
	    boolean bSeenTheOne = false;
	    while (it.hasNext()) {
		File f = (File) it.next();
		if (f.equals(file)) {
		    bSeenTheOne = true;
		    out.add(f);
		} else {
		    Directory d = f.getDirectory();
		    if (d.equals(dir) && bSeenTheOne) {
			out.add(f);
		    }
		}
	    }
	    return out;
	}
    }

    /**
         * Perform a search in all files names with given criteria
         * 
         * @param sCriteria
         * @return
         */
    public TreeSet<SearchResult> search(String sCriteria) {
	synchronized (FileManager.getInstance().getLock()) {
	    TreeSet<SearchResult> tsResu = new TreeSet<SearchResult>();
	    String criteria = sCriteria.toLowerCase();
	    Iterator it = hmItems.values().iterator();
	    while (it.hasNext()) {
		File file = (File) it.next();
		if (ConfigurationManager
			.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)
			&& // if search in
			(!file.getDirectory().getDevice().isMounted() || file
				.getDirectory().getDevice().isRefreshing())) {
		    continue;
		}
		String sResu = file.getAny();
		if (new StringBuffer(sResu.toLowerCase()).lastIndexOf(criteria) != -1) {
		    tsResu.add(new SearchResult(file, file.toStringSearch()));
		}
	    }
	    return tsResu;
	}
    }

    /**
         * @return Returns the bRateHasChanged.
         */
    public boolean hasRateChanged() {
	return bRateHasChanged;
    }

    /**
         * @param rateHasChanged
         *                The bRateHasChanged to set.
         */
    public void setRateHasChanged(boolean rateHasChanged) {
	bRateHasChanged = rateHasChanged;
	if (bRateHasChanged) {
	    ObservationManager
		    .notify(new Event(EventSubject.EVENT_RATE_CHANGED));// refresh
                                                                        // to
                                                                        // update
                                                                        // rates
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.jajuk.base.ItemManager#getIdentifier()
         */
    public String getIdentifier() {
	return XML_FILES;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
         */
    public void update(Event event) {
    }

    public Set<EventSubject> getRegistrationKeys() {
	return new HashSet<EventSubject>();
    }

    /**
         * @param sID
         *                Item ID
         * @return item
         */
    public File getFileByID(String sID) {
	synchronized (getLock()) {
	    return (File) hmItems.get(sID);
	}
    }

    /**
         * 
         * @return files set
         */
    public Set<File> getFiles() {
	Set<File> fileSet = new LinkedHashSet<File>();
	synchronized (getLock()) {
	    for (Item item : getItems()) {
		fileSet.add((File) item);
	    }
	}
	return fileSet;
    }

}
