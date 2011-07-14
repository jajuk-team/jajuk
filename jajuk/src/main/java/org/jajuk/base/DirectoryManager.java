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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilSystem;

/**
 * Convenient class to manage directories.
 */
public final class DirectoryManager extends ItemManager {

  /** Self instance. */
  private static DirectoryManager singleton = new DirectoryManager();

  /**
   * Return hashcode for this item.
   * 
   * @param sName directory name
   * @param device device
   * @param dParent parent directory
   * 
   * @return ItemManager ID
   */
  protected static String createID(final String sName, final Device device, final Directory dParent) {
    final StringBuilder sbAbs = new StringBuilder(device.getName());
    // Under windows, all files/directories with different cases should get
    // the same ID
    if (UtilSystem.isUnderWindows()) {
      if (dParent != null) {
        sbAbs.append(dParent.getRelativePath().toLowerCase(Locale.getDefault()));
      }
      sbAbs.append(sName.toLowerCase(Locale.getDefault()));
    } else {
      if (dParent != null) {
        sbAbs.append(dParent.getRelativePath());
      }
      sbAbs.append(sName);
    }
    return MD5Processor.hash(sbAbs.toString());
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static DirectoryManager getInstance() {
    return DirectoryManager.singleton;
  }

  /**
   * No constructor available, only static access.
   */
  private DirectoryManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name test with (getParentDirectory() != null); //name editable only
    // for standard
    // directories, not root
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, false, false,
        String.class, null));
    // @TODO edition
    // Parent
    registerProperty(new PropertyMetaInformation(Const.XML_DIRECTORY_PARENT, false, true, true,
        false, false, String.class, null));
    // Device
    registerProperty(new PropertyMetaInformation(Const.XML_DEVICE, false, true, true, false, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(Const.XML_EXPANDED, false, false, false, false,
        true, Boolean.class, false));
    // Synchronized directory
    registerProperty(new PropertyMetaInformation(Const.XML_DIRECTORY_SYNCHRONIZED, false, false,
        true, false, false, Boolean.class, true));
  }

  /**
   * Clean all references for the given device.
   * 
   * @param sId :
   * Device id
   */
  public void cleanDevice(final String sId) {
    lock.writeLock().lock();
    try {
      for (Directory directory : getDirectories()) {
        if (directory.getDevice().getID().equals(sId)) {
          removeItem(directory);
        }
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Gets a shallow copy of all directories.
   * 
   * @return ordered directories list
   */
  @SuppressWarnings("unchecked")
  public List<Directory> getDirectories() {
    return (List<Directory>) getItems();
  }

  /**
   * Gets the directories iterator.
   * 
   * @return directories iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<Directory> getDirectoriesIterator() {
    return new ReadOnlyIterator<Directory>((Iterator<Directory>) getItemsIterator());
  }

  /**
   * Gets the directory by id.
   * 
   * @param sID Item ID
   * 
   * @return Directory matching the id
   */
  public Directory getDirectoryByID(final String sID) {
    return (Directory) getItemByID(sID);
  }

  /**
   * Gets the directory for io.
   *
   * @param fio DOCUMENT_ME
   * @param device Associated device
   * @return Directory matching the io file
   */
  public Directory getDirectoryForIO(final java.io.File fio, Device device) {
    for (Directory dir : getDirectories()) {
      // we have to test the device because of cdroms : all CD have the same IO
      if (dir.getFio().equals(fio) && dir.getDevice().equals(device)) {
        return dir;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return Const.XML_DIRECTORIES;
  }

  /**
   * Register a root device directory.
   * 
   * @param device DOCUMENT_ME
   * 
   * @return the directory
   */
  public Directory registerDirectory(final Device device) {
    return registerDirectory(device.getID(), "", null, device);
  }

  /**
   * Register a directory.
   * 
   * @param sName DOCUMENT_ME
   * @param dParent DOCUMENT_ME
   * @param device DOCUMENT_ME
   * 
   * @return the directory
   */
  public Directory registerDirectory(final String sName, final Directory dParent,
      final Device device) {
    return registerDirectory(DirectoryManager.createID(sName, device, dParent), sName, dParent,
        device);
  }

  /**
   * Register a directory with a known id.
   *
   * @param sId DOCUMENT_ME
   * @param sName DOCUMENT_ME
   * @param dParent DOCUMENT_ME
   * @param device DOCUMENT_ME
   * @return the directory
   */
  public Directory registerDirectory(final String sId, final String sName, final Directory dParent,
      final Device device) {
    Directory directory = getDirectoryByID(sId);
    if (directory != null) {
      return directory;
    }
    directory = new Directory(sId, sName, dParent, device);
    registerItem(directory);
    return directory;
  }

  /**
   * Remove a directory and all subdirectories from main directory repository.
   * Remove reference from parent directories as well.
   * 
   * @param sId DOCUMENT_ME
   */
  public void removeDirectory(final String sId) {
    lock.writeLock().lock();
    try {
      final Directory dir = getDirectoryByID(sId);
      if (dir == null) {// check the directory has not already been
        // removed
        return;
      }
      // remove all files
      // need to use a shallow copy to avoid concurrent exceptions
      final List<File> alFiles = new ArrayList<File>(dir.getFiles());
      for (final File file : alFiles) {
        FileManager.getInstance().removeFile(file);
      }
      // remove all playlists
      for (final Playlist plf : dir.getPlaylistFiles()) {
        PlaylistManager.getInstance().removeItem(plf);
      }
      // remove all sub dirs
      final Iterator<Directory> it = dir.getDirectories().iterator();
      while (it.hasNext()) {
        final Directory dSub = it.next();
        removeDirectory(dSub.getID()); // self call
        // remove it
        it.remove();
      }
      // remove this dir from collection
      removeItem(dir);
    } finally {
      lock.writeLock().unlock();
    }
  }
}