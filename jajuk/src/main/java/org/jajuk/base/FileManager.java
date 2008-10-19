/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.JajukPredicates;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage files
 */
public final class FileManager extends ItemManager implements Observer {
  /** Best of files */
  private List<File> alBestofFiles = new ArrayList<File>(20);

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
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    // Directory
    registerProperty(new PropertyMetaInformation(Const.XML_DIRECTORY, false, true, true, false,
        true, String.class, null));
    // Track
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK, false, true, true, false, false,
        String.class, null));
    // Size
    registerProperty(new PropertyMetaInformation(Const.XML_SIZE, false, true, true, false, false,
        Long.class, null));
    // Quality
    registerProperty(new PropertyMetaInformation(Const.XML_QUALITY, false, true, true, false,
        false, Long.class, 0));
    // Date
    registerProperty(new PropertyMetaInformation(Const.XML_FILE_DATE, false, false, true, false,
        false, Date.class, new Date()));
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
  public synchronized File registerFile(String sId, String sName, Directory directory, Track track,
      long lSize, long lQuality) {
    File file = new File(sId, sName, directory, track, lSize, lQuality);
    registerItem(file);
    // add to directory
    file.getDirectory().addFile(file);
    if (directory.getDevice().isRefreshing() && Log.isDebugEnabled()) {
      Log.debug("registrated new file: " + file);
    }
    // add this file to track
    file.setTrack(track);
    // make sure the file is added
    track.addFile(file);
    return file;
  }

  /**
   * Get file hashcode (ID)
   * 
   * @param sName
   * @param device
   * @param dir
   * @return file ID
   */
  protected static String createID(String sName, Directory dir) {
    String id = null;
    // Under windows, all files/directories with different cases should get
    // the same ID
    if (UtilSystem.isUnderWindows()) {
      id = MD5Processor.hash(new StringBuilder(dir.getDevice().getName()).append(
          dir.getRelativePath().toLowerCase()).append(sName.toLowerCase()).toString());
    } else {
      id = MD5Processor.hash(new StringBuilder(dir.getDevice().getName()).append(
          dir.getRelativePath()).append(sName).toString());
    }
    return id;
  }

  /**
   * Change a file name
   * 
   * @param fileOld
   * @param sNewName
   * @return new file
   */
  public synchronized File changeFileName(org.jajuk.base.File fileOld, String sNewName)
      throws JajukException {
    // check given name is different

    if (fileOld.getName().equals(sNewName)) {
      return fileOld;
    }
    // check if this file still exists
    if (!fileOld.getIO().exists()) {
      throw new CannotRenameException(135);
    }
    java.io.File fileNew = new java.io.File(fileOld.getIO().getParentFile().getAbsolutePath()
        + java.io.File.separator + sNewName);
    // recalculate file ID
    Directory dir = fileOld.getDirectory();
    String sNewId = MD5Processor.hash(new StringBuilder(dir.getDevice().getName()).append(
        dir.getDevice().getUrl()).append(dir.getRelativePath()).append(sNewName).toString());
    // create a new file (with own fio and sAbs)
    org.jajuk.base.File fNew = new File(sNewId, sNewName, fileOld.getDirectory(), fileOld
        .getTrack(), fileOld.getSize(), fileOld.getQuality());
    // transfert all properties (inc id and name)
    fNew.setProperties(fileOld.getProperties());
    fNew.setProperty(Const.XML_ID, sNewId); // reset new id and name
    fNew.setProperty(Const.XML_NAME, sNewName); // reset new id and name
    // check file name and extension
    if (!(UtilSystem.getExtension(fileNew).equals(UtilSystem.getExtension(fileOld.getIO())))) {
      // no extension change
      throw new CannotRenameException(134);
    }
    // check if future file exists (under windows, file.exists
    // return true even with
    // different case so we test file name is different)
    if (!fileNew.getName().equalsIgnoreCase(fileOld.getName()) && fileNew.exists()) {
      throw new CannotRenameException(134);
    }
    // try to rename file on disk
    try {
      if (!fileOld.getIO().renameTo(fileNew))
        throw new CannotRenameException(134);
    } catch (Exception e) {
      throw new CannotRenameException(134, e);
    }

    // OK, remove old file and register this new file
    removeFile(fileOld);
    registerItem(fNew);
    // notify everybody for the file change
    Properties properties = new Properties();
    properties.put(Const.DETAIL_OLD, fileOld);
    properties.put(Const.DETAIL_NEW, fNew);
    // change directory reference
    dir.changeFile(fileOld, fNew);
    // Notify interested items (like history manager)
    ObservationManager.notifySync(new Event(JajukEvents.FILE_NAME_CHANGED, properties));
    return fNew;
  }

  /**
   * Change a file directory
   * 
   * @param old
   *          old file
   * @param newDir
   *          new dir
   * @return new file or null if an error occurs
   */
  public synchronized File changeFileDirectory(File old, Directory newDir) {
    // recalculate file ID
    String sNewId = MD5Processor.hash(new StringBuilder(newDir.getDevice().getName()).append(
        newDir.getDevice().getUrl()).append(newDir.getRelativePath()).append(old.getName())
        .toString());
    // create a new file (with own fio and sAbs)
    File fNew = new File(sNewId, old.getName(), newDir, old.getTrack(), old.getSize(), old
        .getQuality());
    fNew.setProperties(old.getProperties()); // transfert all
    // properties (inc id)
    fNew.setProperty(Const.XML_ID, sNewId); // reset new id and name
    // OK, remove old file and register this new file
    removeFile(old);
    registerItem(fNew);
    return fNew;
  }

  /**
   * Clear all references for the given device
   * 
   * @param sId :
   *          Device id
   */
  @SuppressWarnings("unchecked")
  public synchronized void clearDevice(String sId) {
    for (File file : getFiles()) {
      if (file.getDirectory() == null || file.getDirectory().getDevice().getID().equals(sId)) {
        removeItem(file);
      }
    }
  }

  /**
   * Remove a file reference
   * 
   * @param file
   */
  public synchronized void removeFile(File file) {
    removeItem(file);
    file.getDirectory().removeFile(file);
  }

  /**
   * Return file by full path
   * 
   * @param sPath :
   *          full path
   * @return file or null if given path is not known
   */

  @SuppressWarnings("unchecked")
  public synchronized File getFileByPath(String sPath) {
    File fOut = null;
    java.io.File fToCompare = new java.io.File(sPath);
    ReadOnlyIterator<File> it = getFilesIterator();
    while (it.hasNext()) {
      File file = it.next();
      // we compare io files and not paths
      // to avoid dealing with path name issues
      if (file.getIO().equals(fToCompare)) {
        fOut = file;
        break;
      }
    }
    return fOut;
  }

  /**
   * @return All accessible files of the collection
   */
  @SuppressWarnings("unchecked")
  public List<File> getReadyFiles() {
    List<File> files = null;
    files = FileManager.getInstance().getFiles();
    CollectionUtils.filter(files, new JajukPredicates.ReadyFilePredicate());
    return files;
  }

  /**
   * Return a shuffle mounted file from the entire collection
   * 
   * @return
   */
  public File getShuffleFile() {
    int index = UtilSystem.getRandom().nextInt(getElementCount());
    Log.debug("Randomly choosing " + index + " for next file.");
    List<File> files = FileManager.getInstance().getFiles();
    if (files.size() == 0) {
      return null;
    }
    return files.get(index);
  }

  /**
   * Return an ordered playlist with the entire accessible shuffle collection
   * 
   * @return The entire accessible shuffle collection (can return a void
   *         collection)
   */
  public List<File> getGlobalShufflePlaylist() {
    List<File> alEligibleFiles = getReadyFiles();
    // filter banned files
    CollectionUtils.filter(alEligibleFiles, new JajukPredicates.BannedFilePredicate());
    // shuffle
    Collections.shuffle(alEligibleFiles, UtilSystem.getRandom());
    // song level, just shuffle full collection
    if (Conf.getString(Const.CONF_GLOBAL_RANDOM_MODE).equals(Const.MODE_TRACK)) {
      return alEligibleFiles;
    }
    // (not shuffle) Album / album
    else if (Conf.getString(Const.CONF_GLOBAL_RANDOM_MODE).equals(Const.MODE_ALBUM2)) {
      final List<Album> albums = AlbumManager.getInstance().getAlbums();
      Collections.shuffle(albums, UtilSystem.getRandom());
      // We need an index (bench: 45* faster)
      final Map<Album, Integer> index = new HashMap<Album, Integer>();
      for (Album album : albums) {
        index.put(album, albums.indexOf(album));
      }
      Collections.sort(alEligibleFiles, new Comparator<File>() {

        public int compare(File f1, File f2) {
          if (f1.getTrack().getAlbum().equals(f2.getTrack().getAlbum())) {
            int comp = (int) (f1.getTrack().getOrder() - f2.getTrack().getOrder());
            if (comp == 0) {
              // If no track number is given, try to sort by
              // filename than can contain the track
              return f1.getName().compareTo(f2.getName());
            }
            return comp;
          }
          return index.get(f1.getTrack().getAlbum()) - index.get(f2.getTrack().getAlbum());
        }

      });
      return alEligibleFiles;
      // else return shuffle albums
    } else {
      return getShuffledFilesByAlbum(alEligibleFiles);
    }
  }

  /**
   * Return a shuffle mounted file from the novelties
   * 
   * @return
   */
  public File getNoveltyFile() {
    List<File> alEligibleFiles = getGlobalNoveltiesPlaylist();
    return alEligibleFiles.get((int) (Math.random() * alEligibleFiles.size()));
  }

  /**
   * Return a shuffled playlist with the entire accessible novelties collection
   * 
   * @return The entire accessible novelties collection (can return a void
   *         collection)
   */
  public List<org.jajuk.base.File> getGlobalNoveltiesPlaylist() {
    return getGlobalNoveltiesPlaylist(true);
  }

  /**
   * Return an ordered playlist with the entire accessible novelties collection
   * 
   * @param bHideUnmounted
   * @return The entire accessible novelties collection
   */
  @SuppressWarnings("unchecked")
  public List<File> getGlobalNoveltiesPlaylist(boolean bHideUnmounted) {
    List<File> alEligibleFiles = new ArrayList<File>(1000);
    List<Track> tracks = TrackManager.getInstance().getTracks();
    // Filter by age
    CollectionUtils.filter(tracks, new JajukPredicates.AgePredicate(Conf
        .getInt(Const.CONF_OPTIONS_NOVELTIES_AGE)));
    // filter banned tracks
    CollectionUtils.filter(tracks, new JajukPredicates.BannedTrackPredicate());
    for (Track track : tracks) {
      File file = track.getPlayeableFile(bHideUnmounted);
      // try to get a mounted file
      // (can return null)
      if (file == null) {// none mounted file, take first file we find
        continue;
      }
      alEligibleFiles.add(file);
    }
    // sort alphabetically and by date, newest first
    Collections.sort(alEligibleFiles, new Comparator<File>() {
      public int compare(File file1, File file2) {
        String sCompared1 = file1.getTrack().getDiscoveryDate().getTime() + file1.getAbsolutePath();
        String sCompared2 = file2.getTrack().getDiscoveryDate().getTime() + file2.getAbsolutePath();
        return sCompared2.compareTo(sCompared1);
      }
    });
    return alEligibleFiles;
  }

  /**
   * Return a shuffled playlist with the entire accessible novelties collection
   * 
   * @return The entire accessible novelties collection
   */
  public List<File> getShuffleNoveltiesPlaylist() {
    List<File> alEligibleFiles = getGlobalNoveltiesPlaylist(true);
    // song level, just shuffle full collection
    if (Conf.getString(Const.CONF_NOVELTIES_MODE).equals(Const.MODE_TRACK)) {
      Collections.shuffle(alEligibleFiles);
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
    Map<Album, List<File>> albumsFiles = new HashMap<Album, List<File>>(alEligibleFiles.size() / 10);
    for (File file : alEligibleFiles) {
      // maintain a map between each albums and
      // eligible files
      Album album = file.getTrack().getAlbum();
      List<File> files = albumsFiles.get(album);
      if (files == null) {
        files = new ArrayList<File>(10);
      }
      files.add(file);
      albumsFiles.put(album, files);
    }
    // build output
    List<File> out = new ArrayList<File>(alEligibleFiles.size());
    List<Album> albums = new ArrayList<Album>(albumsFiles.keySet());
    // we need to force a new shuffle as internal hashmap arrange items
    Collections.shuffle(albums, UtilSystem.getRandom());
    for (Album album : albums) {
      List<File> files = albumsFiles.get(album);
      Collections.shuffle(files, UtilSystem.getRandom());
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
   * Return a shuffled playlist with the entire accessible bestof collection,
   * best first
   * 
   * @return Shuffled best tracks (n% of favorite)
   */
  public List<File> getGlobalBestofPlaylist() {
    List<File> al = getSortedByRate();
    // Filter banned files
    CollectionUtils.filter(al, new JajukPredicates.BannedFilePredicate());
    List<File> alBest = null;
    if (al.size() > 0) {
      // find superior interval value
      int sup = (int) ((Const.BESTOF_PROPORTION) * al.size());
      if (sup < 0) {
        sup = al.size();
      }
      alBest = al.subList(0, sup - 1);
      Collections.shuffle(alBest, UtilSystem.getRandom());
    }
    return alBest;
  }

  /**
   * Return ordered (by rate) bestof files
   * 
   * @param bHideUnmounted
   *          if true, unmounted files are not chosen
   * @param iNbBestofFiles
   *          nb of items to return
   * @return top files
   */
  public List<File> getBestOfFiles() {
    refreshBestOfFiles();
    return alBestofFiles;
  }

  public void refreshBestOfFiles() {
    int iNbBestofFiles = Integer.parseInt(Conf.getString(Const.CONF_BESTOF_TRACKS_SIZE));
    // clear data
    alBestofFiles.clear();
    // create a temporary table to remove unmounted files
    List<File> alEligibleFiles = new ArrayList<File>(iNbBestofFiles);
    List<Track> tracks = TrackManager.getInstance().getTracks();
    // filter banned tracks
    CollectionUtils.filter(tracks, new JajukPredicates.BannedTrackPredicate());
    for (Track track : tracks) {
      File file = track.getPlayeableFile(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED));
      if (file != null) {
        alEligibleFiles.add(file);
      }
    }
    Collections.sort(alEligibleFiles, rateComparator);
    // Keep as much items as we can
    int i = 0;
    while (i < alEligibleFiles.size() && i < iNbBestofFiles) {
      File file = alEligibleFiles.get(i);
      alBestofFiles.add(file);
      i++;
    }
  }

  /**
   * Return next mounted file ( used in continue mode )
   * 
   * @param file :
   *          a file
   * @return next file from entire collection
   */
  public synchronized File getNextFile(File file) {
    List<File> files = getFiles();
    if (file == null) {
      return null;
    }
    // look for a correct file from index to collection end
    boolean bStarted = false;
    for (File fileNext : files) {
      if (bStarted) {
        if (fileNext.isReady()) {
          return fileNext;
        }
      } else {
        if (fileNext.equals(file)) {
          bStarted = true;
          // Begin to consider files
          // from this one
        }
      }
    }
    // Restart from collection from beginning
    for (File fileNext : files) {
      if (fileNext.isReady()) { // file must be on a mounted
        // device not refreshing
        return fileNext;
      }
    }
    // none ready file
    return null;
  }

  /**
   * Return previous mounted file
   * 
   * @param file :
   *          a file
   * @return previous file from entire collection
   */
  public synchronized File getPreviousFile(File file) {
    List<File> files = getFiles();
    // Collections.sort(files);
    if (file == null) {
      return null;
    }
    File filePrevious = null;
    List<Item> alSortedFiles = new ArrayList<Item>(files);
    int i = alSortedFiles.indexOf(file);
    // test if this file is the very first one
    if (i == 0) {
      Messages.showErrorMessage(128);
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

  /**
   * Return whether the given file is the very first file from collection
   * 
   * @param file
   * @return
   */
  public boolean isVeryfirstFile(File file) {
    List<File> files = getFiles();
    if (file == null || files.size() == 0) {
      return false;
    }
    return (file.equals(files.get(0)));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return Const.XML_FILES;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(Event event) {
  }

  public Set<JajukEvents> getRegistrationKeys() {
    return new HashSet<JajukEvents>();
  }

  /**
   * @param sID
   *          Item ID
   * @return File matching the id
   */
  public File getFileByID(String sID) {
    return (File) getItemByID(sID);
  }

  /**
   * 
   * @return ordered files list
   */
  @SuppressWarnings("unchecked")
  public synchronized List<File> getFiles() {
    return (List<File>) getItems();
  }

  /**
   * 
   * @return files iterator
   */
  @SuppressWarnings("unchecked")
  public synchronized ReadOnlyIterator<File> getFilesIterator() {
    return new ReadOnlyIterator<File>((Iterator<File>) getItemsIterator());
  }

}
