/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
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
 * Convenient class to manage files.
 */
public final class FileManager extends ItemManager {

  /** Best of files. */
  private final List<File> alBestofFiles = new ArrayList<File>(20);

  /** Self instance. */
  private static FileManager singleton = new FileManager();

  /** File comparator based on rate. */
  private final Comparator<File> rateComparator = new Comparator<File>() {
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
   * No constructor available, only static access.
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
   * Gets the instance.
   * 
   * @return singleton
   */
  public static FileManager getInstance() {
    return singleton;
  }

  /**
   * Register an File with a known id.
   * 
   * @param sId DOCUMENT_ME
   * @param sName DOCUMENT_ME
   * @param directory DOCUMENT_ME
   * @param track DOCUMENT_ME
   * @param lSize DOCUMENT_ME
   * @param lQuality DOCUMENT_ME
   * 
   * @return the file
   */
  public File registerFile(String sId, String sName, Directory directory, Track track, long lSize,
      long lQuality) {
    lock.writeLock().lock();
    try {
      File file = getFileByID(sId);
      if (file == null) {
        file = new File(sId, sName, directory, track, lSize, lQuality);
        registerItem(file);
        if (directory.getDevice().isRefreshing() && Log.isDebugEnabled()) {
          Log.debug("registrated new file: " + file);
        }
      } else {
        // If file already exist and the track has changed, make changes
        // Set name again because under Windows, the file name case
        // could have changed but we keep the same file object
        file.setName(sName);
      }
      // add this file to track
      file.setTrack(track);
      // Add file to track
      track.addFile(file);
      return file;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Register an File without known id.
   * 
   * @param sName DOCUMENT_ME
   * @param directory DOCUMENT_ME
   * @param track DOCUMENT_ME
   * @param lSize DOCUMENT_ME
   * @param lQuality DOCUMENT_ME
   * 
   * @return the file
   */
  public File registerFile(String sName, Directory directory, Track track, long lSize, long lQuality) {
    String sId = createID(sName);
    return registerFile(sId, sName, directory, track, lSize, lQuality);
  }

  /**
   * Get file hashcode (ID).
   * 
   * @param sName DOCUMENT_ME
   * @param dir DOCUMENT_ME
   * 
   * @return file ID
   */
  protected static String createID(String sName, Directory dir) {
    String id = null;
    // Under windows, all files/directories with different cases should get
    // the same ID
    if (UtilSystem.isUnderWindows()) {
      id = MD5Processor.hash(new StringBuilder(dir.getDevice().getName()).append(
          dir.getRelativePath().toLowerCase(Locale.getDefault())).append(
          sName.toLowerCase(Locale.getDefault())).toString());
    } else {
      id = MD5Processor.hash(new StringBuilder(dir.getDevice().getName()).append(
          dir.getRelativePath()).append(sName).toString());
    }
    return id;
  }

  /**
   * Change a file name.
   * 
   * @param fileOld DOCUMENT_ME
   * @param sNewName DOCUMENT_ME
   * 
   * @return new file
   * 
   * @throws JajukException the jajuk exception
   */
  public File changeFileName(org.jajuk.base.File fileOld, String sNewName) throws JajukException {
    lock.writeLock().lock();
    try {
      // check given name is different
      if (fileOld.getName().equals(sNewName)) {
        return fileOld;
      }
      // check if this file still exists
      if (!fileOld.getFIO().exists()) {
        throw new CannotRenameException(135);
      }
      // check that the file is not currently played
      if (QueueModel.getCurrentItem() != null
          && QueueModel.getCurrentItem().getFile().equals(fileOld) && QueueModel.isPlayingTrack()) {
        throw new CannotRenameException(172);
      }

      java.io.File fileNew = new java.io.File(fileOld.getFIO().getParentFile().getAbsolutePath()
          + java.io.File.separator + sNewName);

      // check file name and extension
      if (!(UtilSystem.getExtension(fileNew).equals(UtilSystem.getExtension(fileOld.getFIO())))) {
        // no extension change
        throw new CannotRenameException(134);
      }
      // check if destination file already exists (under windows, file.exists
      // return true even with different case so we test file name is different)
      if (!fileNew.getName().equalsIgnoreCase(fileOld.getName()) && fileNew.exists()) {
        throw new CannotRenameException(134);
      }
      // try to rename file on disk
      try {
        if (!fileOld.getFIO().renameTo(fileNew)) {
          throw new CannotRenameException(134);
        }
      } catch (Exception e) {
        throw new CannotRenameException(134, e);
      }

      // OK, remove old file and register this new file
      // Compute file ID
      Directory dir = fileOld.getDirectory();
      String sNewId = createID(sNewName, dir);
      // create a new file (with own fio and sAbs)
      Track track = fileOld.getTrack();
      // Remove old file from associated track
      track.removeFile(fileOld);
      org.jajuk.base.File fNew = new File(sNewId, sNewName, fileOld.getDirectory(), track, fileOld
          .getSize(), fileOld.getQuality());
      // transfer all properties and reset id and name
      // We use a shallow copy of properties to avoid any properties share between
      // two items
      fNew.setProperties(fileOld.getShallowProperties());
      fNew.setProperty(Const.XML_ID, sNewId); // reset new id and name
      fNew.setProperty(Const.XML_NAME, sNewName); // reset new id and name

      removeFile(fileOld);
      registerItem(fNew);
      track.addFile(fNew);
      // notify everybody for the file change
      Properties properties = new Properties();
      properties.put(Const.DETAIL_OLD, fileOld);
      properties.put(Const.DETAIL_NEW, fNew);
      // Notify interested items (like history manager)
      ObservationManager.notifySync(new JajukEvent(JajukEvents.FILE_NAME_CHANGED, properties));
      return fNew;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a file directory.
   * 
   * @param old old file
   * @param newDir new dir
   * 
   * @return new file or null if an error occurs
   */
  public File changeFileDirectory(File old, Directory newDir) {
    lock.writeLock().lock();
    try {
      // recalculate file ID
      String sNewId = FileManager.createID(old.getName(), newDir);
      Track track = old.getTrack();
      // create a new file (with own fio and sAbs)
      File fNew = new File(sNewId, old.getName(), newDir, track, old.getSize(), old.getQuality());
      // Transfer all properties (including id), then set right id and directory
      // We use a shallow copy of properties to avoid any properties share between
      // two items
      fNew.setProperties(old.getShallowProperties());
      fNew.setProperty(Const.XML_ID, sNewId);
      fNew.setProperty(Const.XML_DIRECTORY, newDir.getID());

      // OK, remove old file and register this new file
      removeFile(old);
      registerItem(fNew);
      track.addFile(fNew);
      return fNew;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Clean all references for the given device.
   * 
   * @param sId :
   * Device id
   */
  public void cleanDevice(String sId) {
    lock.writeLock().lock();
    try {
      for (File file : getFiles()) {
        if (file.getDirectory() == null || file.getDirectory().getDevice().getID().equals(sId)) {
          removeItem(file);
        }
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Remove a file reference.
   * 
   * @param file DOCUMENT_ME
   */
  public void removeFile(File file) {
    lock.writeLock().lock();
    try {
      // We need to remove the file from the track !
      TrackManager.getInstance().removefile(file.getTrack(), file);
      removeItem(file);
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Return file by full path.
   * 
   * @param sPath :
   * full path
   * 
   * @return file or null if given path is not known
   */

  public File getFileByPath(String sPath) {
    lock.readLock().lock();
    try {
      File fOut = null;
      java.io.File fToCompare = new java.io.File(sPath);
      ReadOnlyIterator<File> it = getFilesIterator();
      while (it.hasNext()) {
        File file = it.next();
        // we compare io files and not paths
        // to avoid dealing with path name issues
        if (file.getFIO().equals(fToCompare)) {
          fOut = file;
          break;
        }
      }
      // Fix  #1717 (Cannot load some playlists) : if the file is not found, second chance ignoring the case
      // This can happen under Unix when using an SMB drive
      if (fOut == null) {
        it = getFilesIterator();
        while (it.hasNext()) {
          File file = it.next();
          if (file.getFIO().getAbsolutePath().equalsIgnoreCase(fToCompare.getAbsolutePath())) {
            fOut = file;
            break;
          }
        }
      }
      return fOut;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Gets the ready files.
   * 
   * @return All accessible files of the collection
   */
  public List<File> getReadyFiles() {
    List<File> files = FileManager.getInstance().getFiles();
    CollectionUtils.filter(files, new JajukPredicates.ReadyFilePredicate());
    return files;
  }

  /**
   * Return a shuffle mounted and unbaned file from the entire collection or
   * null if none available using these criterias.
   * 
   * @return the file
   */
  public File getShuffleFile() {
    List<File> alEligibleFiles = getReadyFiles();
    // filter banned files
    CollectionUtils.filter(alEligibleFiles, new JajukPredicates.BannedFilePredicate());
    if (alEligibleFiles.size() > 0) {
      int index = UtilSystem.getRandom().nextInt(alEligibleFiles.size() - 1);
      return alEligibleFiles.get(index);
    } else {
      return null;
    }
  }

  /**
   * Return an ordered playlist with the entire accessible shuffle collection.
   * 
   * @return The entire accessible shuffle collection (can return a void
   * collection)
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
   * Return a shuffle mounted file from the novelties.
   * 
   * @return the novelty file
   */
  public File getNoveltyFile() {
    List<File> alEligibleFiles = getGlobalNoveltiesPlaylist();
    return alEligibleFiles.get((int) (Math.random() * alEligibleFiles.size()));
  }

  /**
   * Return a shuffled playlist with the entire accessible novelties collection.
   * 
   * @return The entire accessible novelties collection (can return a void
   * collection)
   */
  public List<org.jajuk.base.File> getGlobalNoveltiesPlaylist() {
    return getGlobalNoveltiesPlaylist(true);
  }

  /**
   * Return an ordered playlist with the accessible novelties collection The
   * number of returned items is limited to NB_TRACKS_ON_ACTION for performance
   * reasons.
   * 
   * @param bHideUnmounted DOCUMENT_ME
   * 
   * @return The entire accessible novelties collection
   */
  public List<File> getGlobalNoveltiesPlaylist(boolean bHideUnmounted) {
    List<File> alEligibleFiles = new ArrayList<File>(1000);
    List<Track> tracks = TrackManager.getInstance().getTracks();
    // Filter by age
    CollectionUtils.filter(tracks, new JajukPredicates.AgePredicate(Conf
        .getInt(Const.CONF_OPTIONS_NOVELTIES_AGE)));
    // filter banned tracks
    CollectionUtils.filter(tracks, new JajukPredicates.BannedTrackPredicate());
    for (Track track : tracks) {
      if (alEligibleFiles.size() > Const.NB_TRACKS_ON_ACTION) {
        break;
      }
      File file = track.getBestFile(bHideUnmounted);
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
   * Return a shuffled playlist with the entire accessible novelties collection.
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
   * Convenient method used to return shuffled files by album.
   * 
   * @param alEligibleFiles DOCUMENT_ME
   * 
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
   * Gets the sorted by rate.
   * 
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
   * best first.
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
      if (sup < 2) {
        sup = al.size();
      }
      alBest = al.subList(0, sup - 1);
      Collections.shuffle(alBest, UtilSystem.getRandom());
    }
    return alBest;
  }

  /**
   * Return ordered (by rate) bestof files.
   * 
   * @return top files
   */
  public List<File> getBestOfFiles() {
    // Don't refresh best of files at each call because it makes  the playlist view
    // unusable for bestof files : each time a file is played, the view is changed
    if (alBestofFiles.size() == 0) {
      refreshBestOfFiles();
    }
    return alBestofFiles;
  }

  /**
   * Refresh best of files.
   */
  public void refreshBestOfFiles() {
    Log.debug("Invoking Refresh of BestOf-Files");

    // clear data
    alBestofFiles.clear();

    // create a temporary table to remove unmounted files
    int iNbBestofFiles = Integer.parseInt(Conf.getString(Const.CONF_BESTOF_TRACKS_SIZE));
    List<File> alEligibleFiles = new ArrayList<File>(iNbBestofFiles);
    List<Track> tracks = TrackManager.getInstance().getTracks();

    // filter banned tracks
    CollectionUtils.filter(tracks, new JajukPredicates.BannedTrackPredicate());
    for (Track track : tracks) {
      File file = track.getBestFile(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED));
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
   * Return next mounted file ( used in continue mode ).
   * 
   * @param file :
   * a file
   * 
   * @return next file from entire collection
   */
  public File getNextFile(File file) {
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
   * Return next mounted file from a different album than the provided file.
   * 
   * @param file :
   * a file
   * 
   * @return next file from entire collection
   */
  public File getNextAlbumFile(File file) {
    File testedFile = file;
    if (DirectoryManager.getInstance().getDirectories().size() > 1) {
      while (testedFile.getDirectory().equals(file.getDirectory())) {
        testedFile = getNextFile(testedFile);
      }
    }
    if (!testedFile.getDirectory().equals(file.getDirectory())) {
      return testedFile;
    }
    // Should not happen
    else {
      return null;
    }
  }

  /**
   * Return previous mounted file.
   * 
   * @param file :
   * a file
   * 
   * @return previous file from entire collection
   */
  public File getPreviousFile(File file) {
    List<File> files = getFiles();
    if (file == null) {
      return null;
    }
    File filePrevious = null;
    int i = files.indexOf(file);
    // test if this file is the very first one
    if (i == 0) {
      Messages.showErrorMessage(128);
      return null;
    }
    // look for a correct file from index to collection begin
    boolean bOk = false;
    for (int index = i - 1; index >= 0; index--) {
      filePrevious = files.get(index);
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
   * Return whether the given file is the very first file from collection.
   * 
   * @param file DOCUMENT_ME
   * 
   * @return true, if checks if is very first file
   */
  public boolean isVeryfirstFile(File file) {
    List<File> files = getFiles();
    if (file == null || files.size() == 0) {
      return false;
    }
    return file.equals(files.get(0));
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

  /**
   * Gets the file by id.
   * 
   * @param sID Item ID
   * 
   * @return File matching the id
   */
  public File getFileByID(String sID) {
    return (File) getItemByID(sID);
  }

  /**
   * Gets the files.
   * 
   * @return ordered files list
   */
  @SuppressWarnings("unchecked")
  public List<File> getFiles() {
    return (List<File>) getItems();
  }

  /**
   * Gets the files iterator.
   * 
   * @return files iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<File> getFilesIterator() {
    return new ReadOnlyIterator<File>((Iterator<File>) getItemsIterator());
  }

}
