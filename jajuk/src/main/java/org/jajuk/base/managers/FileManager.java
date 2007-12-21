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

package org.jajuk.base.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.iterators.FilterIterator;
import org.jajuk.base.Event;
import org.jajuk.base.IItem;
import org.jajuk.base.ItemType;
import org.jajuk.base.JajukPredicates;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.Observer;
import org.jajuk.base.items.Album;
import org.jajuk.base.items.Directory;
import org.jajuk.base.items.File;
import org.jajuk.base.items.Track;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.Resources;
import org.jajuk.util.Util;
import org.jajuk.util.Resources.ConfKeys;
import org.jajuk.util.Resources.Details;
import org.jajuk.util.Resources.ShuffleModes;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage files
 */
public class FileManager extends ItemManager<File> implements Observer {
  /** Best of files */
  private ArrayList<File> alBestofFiles = new ArrayList<File>(20);

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
  public FileManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new MetaProperty(XML.ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new MetaProperty(XML.NAME, false, true, true, true, false,
        String.class, null));
    // Directory
    registerProperty(new MetaProperty(XML.DIRECTORY, false, true, true, false, true,
        String.class, null));
    // Track
    registerProperty(new MetaProperty(XML.TRACK, false, true, true, false, false,
        String.class, null));
    // Size
    registerProperty(new MetaProperty(XML.SIZE, false, true, true, false, false,
        Long.class, null));
    // Quality
    registerProperty(new MetaProperty(XML.QUALITY, false, true, true, false, false,
        Long.class, 0));
    // Date
    registerProperty(new MetaProperty(XML.FILE_DATE, false, false, true, false, false,
        Date.class, new Date()));
  }

  /**
   * Register an File with a known id
   *
   * @param sName
   */
  public File registerFile(final String sId, final String sName, final Directory directory, final Track track, final long lSize,
      final long lQuality) {
    synchronized (getLock()) {
      File file = getItems().get(sId);
      if (file == null) {
        file = new File(sId, sName, directory, track, lSize, lQuality);
        getItems().put(sId, file);
        // add to directory
        file.getDirectory().addFile(file);
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
      // make sure the file is added
      track.addFile(file);
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
  public static String createID(final String sName, final Directory dir) {
    String id = null;
    // Under windows, all files/directories with different cases should get
    // the same ID
    if (Util.isUnderWindows()) {
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
  public File changeFileName(final File fileOld, final String sNewName) throws JajukException {
    synchronized (getLock()) {
      // check given name is different

      if (fileOld.getName().equals(sNewName)) {
        return fileOld;
      }
      // check if this file still exists
      if (!fileOld.getIO().exists()) {
        throw new CannotRenameException(135);
      }
      final java.io.File fileNew = new java.io.File(fileOld.getIO().getParentFile().getAbsolutePath()
          + java.io.File.separator + sNewName);
      // recalculate file ID
      final Directory dir = fileOld.getDirectory();
      final String sNewId = MD5Processor.hash(new StringBuilder(dir.getDevice().getName()).append(
          dir.getDevice().getUrl()).append(dir.getRelativePath()).append(sNewName).toString());
      // create a new file (with own fio and sAbs)
      final File fNew = new File(sNewId, sNewName, fileOld.getDirectory(), fileOld
          .getTrack(), fileOld.getSize(), fileOld.getQuality());
      // transfert all properties (inc id and name)
      fNew.setProperties(fileOld.getProperties());
      fNew.getProperties().set(XML.ID, sNewId); // reset new id and name
      fNew.getProperties().set(XML.NAME, sNewName); // reset new id and name
      // check file name and extension
      if (!(Util.getExtension(fileNew).equals(Util.getExtension(fileOld.getIO())))) {
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
        fileOld.getIO().renameTo(fileNew);
      } catch (final Exception e) {
        throw new CannotRenameException(134);
      }
      // OK, remove old file and register this new file
      removeFile(fileOld);
      if (!getItems().containsKey(sNewId)) {
        getItems().put(sNewId, fNew);
      }
      // notify everybody for the file change
      final Properties properties = new Properties();
      properties.put(Details.OLD, fileOld);
      properties.put(Details.NEW, fNew);
      // change directory reference
      dir.changeFile(fileOld, fNew);
      // Notify interested items (like history manager)
      ObservationManager.notifySync(new Event(EventSubject.EVENT_FILE_NAME_CHANGED, properties));
      return fNew;
    }
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
  public File changeFileDirectory(final File old, final Directory newDir) {
    synchronized (getLock()) {
      // recalculate file ID
      final String sNewId = MD5Processor.hash(new StringBuilder(newDir.getDevice().getName()).append(
          newDir.getDevice().getUrl()).append(newDir.getRelativePath()).append(old.getName())
          .toString());
      // create a new file (with own fio and sAbs)
      final File fNew = new File(sNewId, old.getName(), newDir, old.getTrack(), old.getSize(), old
          .getQuality());
      fNew.setProperties(old.getProperties()); // transfert all
      // properties (inc id)
      fNew.getProperties().set(XML.ID, sNewId); // reset new id and name
      // OK, remove old file and register this new file
      removeFile(old);
      if (!getItems().containsKey(sNewId)) {
        getItems().put(sNewId, fNew);
      }
      return fNew;
    }
  }

  /**
   * Clean all references for the given device
   *
   * @param sId :
   *          Device id
   */
  public void cleanDevice(final String sId) {
    synchronized (getLock()) {
      Iterator it = getItems().values().iterator();
      while (it.hasNext()) {
        final File file = (File) it.next();
        if ((file.getDirectory() == null) || file.getDirectory().getDevice().getID().equals(sId)) {
          it.remove(); // this is the right way to remove entry
          // in the hashmap
        }
      }
      // cleanup sorted array
      it = getItems().values().iterator();
      while (it.hasNext()) {
        final File file = (File) it.next();
        if ((file.getDirectory() == null) || file.getDirectory().getDevice().getID().equals(sId)) {
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
  public void removeFile(final File file) {
    synchronized (getLock()) {
      getItems().remove(file.getID());
      file.getDirectory().removeFile(file);
    }
  }

  /**
   * Return file by full path
   *
   * @param sPath :
   *          full path
   * @return file or null if given path is not known
   */

  public File getFileByPath(final String sPath) {
    synchronized (getLock()) {
      File fOut = null;
      final java.io.File fToCompare = new java.io.File(sPath);
      final Iterator it = getItems().values().iterator();
      while (it.hasNext()) {
        final File file = (File) it.next();
        // we compare io files and not paths
        // to avoid dealing with path name issues
        if (file.getIO().equals(fToCompare)) {
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
    files = getCarbonItems();
    final Iterator it = new FilterIterator(files.iterator(), new JajukPredicates.ReadyFilePredicate());
    final List<File> out = new ArrayList<File>(files.size() / 2);
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
    final int index = (int) (new Random().nextFloat() * getItems().size());
    final ArrayList<File> files = new ArrayList<File>(getCarbonItems());
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
    final List<File> alEligibleFiles = getReadyFiles();
    Collections.shuffle(alEligibleFiles, new Random());
    // song level, just shuffle full collection
    if (ConfigurationManager.getProperty(ConfKeys.GLOBAL_RANDOM_MODE).equals(ShuffleModes.TRACK)) {
      return alEligibleFiles;
    }
    // (not shuffle) Album / album
    else if (ConfigurationManager.getProperty(ConfKeys.GLOBAL_RANDOM_MODE).equals(ShuffleModes.ALBUM2)) {
      final ArrayList<Album> albums = new ArrayList<Album>(((AlbumManager) ItemType.Album.getManager()).getCarbonItems());
      Collections.shuffle(albums, new Random());
      // We need an index (bennch: 45* faster)
      final HashMap<Album, Integer> index = new HashMap<Album, Integer>();
      for (final Album album : albums) {
        index.put(album, albums.indexOf(album));
      }
      Collections.sort(alEligibleFiles, new Comparator<File>() {

        public int compare(final File f1, final File f2) {
          if (f1.getTrack().getAlbum().equals(f2.getTrack().getAlbum())) {
            final int comp = (int) (f1.getTrack().getOrder() - f2.getTrack().getOrder());
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
   * Return a shuffle mounted file from the noveties
   *
   * @return
   */
  public synchronized File getNoveltyFile() {
    synchronized (getLock()) {
      final ArrayList alEligibleFiles = getGlobalNoveltiesPlaylist();
      return (File) alEligibleFiles.get((int) (Math.random() * alEligibleFiles.size()));
    }
  }

  /**
   * Return a playlist with the entire accessible shuffled novelties collection
   *
   * @return The entire accessible novelties collection (can return a void
   *         collection)
   */
  public ArrayList<File> getGlobalNoveltiesPlaylist() {
    synchronized (getLock()) {
      return getGlobalNoveltiesPlaylist(true);
    }
  }

  /**
   * Return a playlist with the entire accessible novelties collection
   *
   * @param bHideUnmounted
   * @return The entire accessible novelties collection
   */
  public ArrayList<File> getGlobalNoveltiesPlaylist(final boolean bHideUnmounted) {
    final ArrayList<File> alEligibleFiles = new ArrayList<File>(1000);
    // take tracks matching required age
    final Set<Track> tracks = ((TrackManager) ItemType.Track.getManager()).getCarbonItems();
    final Iterator it = new FilterIterator(tracks.iterator(), new JajukPredicates.AgePredicate(
        ConfigurationManager.getInt(ConfKeys.OPTIONS_NOVELTIES_AGE)));
    while (it.hasNext()) {
      final Track track = (Track) it.next();
      final File file = track.getPlayeableFile(bHideUnmounted);
      // try to get a mounted file
      // (can return null)
      if (file == null) {// none mounted file, take first file we find
        continue;
      }
      alEligibleFiles.add(file);
    }
    // sort alphabetically and by date, newest first
    Collections.sort(alEligibleFiles, new Comparator<File>() {
      public int compare(final File file1, final File file2) {
        final String sCompared1 = file1.getTrack().getAdditionDate().getTime() + file1.getAbsolutePath();
        final String sCompared2 = file2.getTrack().getAdditionDate().getTime() + file2.getAbsolutePath();
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
    final ArrayList<File> alEligibleFiles = getGlobalNoveltiesPlaylist(true);
    // song level, just shuffle full collection
    if (ConfigurationManager.getProperty(ConfKeys.NOVELTIES_MODE).equals(ShuffleModes.TRACK)) {
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
  private List<File> getShuffledFilesByAlbum(final List<File> alEligibleFiles) {
    // start with filling a set of albums containing
    // at least one ready file
    final HashMap<Album, ArrayList<File>> albumsFiles = new HashMap<Album, ArrayList<File>>(
        alEligibleFiles.size() / 10);
    for (final File file : alEligibleFiles) {
      // maintain a map between each albums and
      // eligible files
      final Album album = file.getTrack().getAlbum();
      ArrayList<File> files = albumsFiles.get(album);
      if (files == null) {
        files = new ArrayList<File>(10);
      }
      files.add(file);
      albumsFiles.put(album, files);
    }
    // build output
    final List<File> out = new ArrayList<File>(alEligibleFiles.size());
    final ArrayList<Album> albums = new ArrayList<Album>(albumsFiles.keySet());
    // we need to force a new shuffle as internal hashmap arrange items
    Collections.shuffle(albums, new Random());
    for (final Album album : albums) {
      final ArrayList<File> files = albumsFiles.get(album);
      Collections.shuffle(files, new Random());
      out.addAll(files);
    }
    return out;
  }

  /**
   * @return a sorted set of the collection by rate, highest first
   */
  private List<File> getSortedByRate() {
    // use only mounted files
    final List<File> alEligibleFiles = getReadyFiles();
    // now sort by rate
    Collections.sort(alEligibleFiles, rateComparator);
    return alEligibleFiles;
  }

  /**
   * Return a playlist with the entire accessible bestof collection, best first
   *
   * @return Shuffled best tracks (n% of favorite)
   */
  public ArrayList<File> getGlobalBestofPlaylist() {
    final List<File> al = getSortedByRate();
    ArrayList<File> alBest = new ArrayList<File>();
    if (al.size() > 0) {
      // find superior interval value
      int sup = (int) ((Resources.BESTOF_PROPORTION) * al.size());
      if (sup < 0) {
        sup = al.size();
      }
      alBest = new ArrayList<File>(al.subList(0, sup - 1));
      Collections.shuffle(alBest, new Random()); // shufflelize
    }
    return alBest;
  }

  /**
   * Return bestof files
   *
   * @param bHideUnmounted
   *          if true, unmounted files are not choosen
   * @param iNbBestofFiles
   *          nb of items to return
   * @return top files
   */
  public ArrayList<File> getBestOfFiles() {
    if (alBestofFiles == null) {
      refreshBestOfFiles();
    }
    return alBestofFiles;
  }

  public void refreshBestOfFiles() {
    final int iNbBestofFiles = Integer
        .parseInt(ConfigurationManager.getProperty(ConfKeys.BESTOF_TRACKS_SIZE));
    // clear data
    alBestofFiles.clear();
    // create a temporary table to remove unmounted files
    final ArrayList<File> alEligibleFiles = new ArrayList<File>(iNbBestofFiles);
    for (final Track track : ((TrackManager) ItemType.Track.getManager()).getCarbonItems()) {
      final File file = track.getPlayeableFile(ConfigurationManager
          .getBoolean(ConfKeys.OPTIONS_HIDE_UNMOUNTED));
      if (file != null) {
        alEligibleFiles.add(file);
      }
    }
    Collections.sort(alEligibleFiles, rateComparator);
    int i = 0;
    while ((i < alEligibleFiles.size()) && (i < iNbBestofFiles)) {
      final File file = alEligibleFiles.get(i);
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
  public File getNextFile(final File file) {
    synchronized (getLock()) {
      final Collection<File> files = getItems().values();
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
   *          a file
   * @return previous file from entire collection
   */
  public File getPreviousFile(final File file) {
    synchronized (getLock()) {
      final Collection<File> files = getItems().values();
      if (file == null) {
        return null;
      }
      File filePrevious = null;
      final ArrayList<File> alSortedFiles = new ArrayList<File>(files);
      final int i = alSortedFiles.indexOf(file);
      // test if this file is the very first one
      if (i == 0) {
        Messages.showErrorMessage(128);
        return null;
      }
      // look for a correct file from index to collection begin
      boolean bOk = false;
      for (int index = i - 1; index >= 0; index--) {
        filePrevious = alSortedFiles.get(index);
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
  public boolean isVeryfirstFile(final File file) {
    synchronized (getLock()) {
      final Collection<File> files = getItems().values();

      return (((file == null) || (files.isEmpty())) ? false : file.equals(files.iterator().next()));
    }
  }

  /**
   * @param file
   * @return All files in the same directory than the given one
   */
  public Set<File> getAllDirectory(final File file) {
    synchronized (getLock()) {
      final Collection<File> files = getItems().values();

      if (file == null) {
        return null;
      }
      final Set<File> out = new TreeSet<File>();
      final Directory dir = file.getDirectory();
      for (final IItem item : files) {
        final File f = (File) item;

        final Directory d = f.getDirectory();
        if (d.equals(dir)) {
          out.add(f);
        }
      }
      return out;
    }
  }

  /**
   * @param file
   * @return All files in the same directory from the given one (includes the
   *         one)
   */
  public Set<File> getAllDirectoryFrom(final File file) {
    synchronized (getLock()) {
      if (file == null) {
        return null;
      }
      final Set<File> out = new TreeSet<File>();
      final Directory dir = file.getDirectory();
      boolean bSeenTheOne = false;
      for (final IItem item : getItems().values()) {
        final File f = (File) item;

        if (f.equals(file)) {
          bSeenTheOne = true;
          out.add(f);
        } else {
          final Directory d = f.getDirectory();
          if (d.equals(dir) && bSeenTheOne) {
            out.add(f);
          }
        }
      }
      return out;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML.FILES;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(final Event event) {
  }

  public Set<EventSubject> getRegistrationKeys() {
    return new HashSet<EventSubject>();
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public File getFileByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }

}
