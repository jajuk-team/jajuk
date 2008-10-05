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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.UtilSystem;

/**
 * Convenient class to manage directories
 */
public final class DirectoryManager extends ItemManager {
  /** Self instance */
  private static DirectoryManager singleton;

  /**
   * Return hashcode for this item
   * 
   * @param sName
   *          directory name
   * @param device
   *          device
   * @param dParent
   *          parent directory
   * @return ItemManager ID
   */
  protected static String createID(final String sName, final Device device, final Directory dParent) {
    final StringBuilder sbAbs = new StringBuilder(device.getName());
    // Under windows, all files/directories with different cases should get
    // the same ID
    if (UtilSystem.isUnderWindows()) {
      if (dParent != null) {
        sbAbs.append(dParent.getRelativePath().toLowerCase());
      }
      sbAbs.append(sName.toLowerCase());
    } else {
      if (dParent != null) {
        sbAbs.append(dParent.getRelativePath());
      }
      sbAbs.append(sName);
    }
    return MD5Processor.hash(sbAbs.toString());
  }

  /**
   * @return singleton
   */
  public static DirectoryManager getInstance() {
    if (DirectoryManager.singleton == null) {
      DirectoryManager.singleton = new DirectoryManager();
    }
    return DirectoryManager.singleton;
  }

  /**
   * No constructor available, only static access
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
        String.class, null)); // edition to
    // yet
    // implemented
    // TBI
    // Parent
    registerProperty(new PropertyMetaInformation(Const.XML_DIRECTORY_PARENT, false, true, true,
        false, false, String.class, null));
    // Device
    registerProperty(new PropertyMetaInformation(Const.XML_DEVICE, false, true, true, false, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(Const.XML_EXPANDED, false, false, false, false,
        true, Boolean.class, false));
    // Synchonized directory
    registerProperty(new PropertyMetaInformation(Const.XML_DIRECTORY_SYNCHRONIZED, false, false,
        true, false, false, Boolean.class, true));
    // Default cover
    registerProperty(new PropertyMetaInformation(Const.XML_DIRECTORY_DEFAULT_COVER, false, false,
        true, false, false, String.class, null));
  }

  /**
   * Clean all references for the given device
   * 
   * @param sId :
   *          Device id
   */
  @SuppressWarnings("unchecked")
  public synchronized void cleanDevice(final String sId) {
    final Iterator<String> it = hmItems.keySet().iterator();
    while (it.hasNext()) {
      final Directory directory = getDirectoryByID(it.next());
      if (directory.getDevice().getID().equals(sId)) {
        it.remove();
      }
    }
  }

  /**
   * 
   * @return directories list
   */
  public synchronized Set<Directory> getDirectories() {
    final Set<Directory> directorySet = new LinkedHashSet<Directory>();
    for (final Item item : getItems()) {
      directorySet.add((Directory) item);
    }
    return directorySet;
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Directory getDirectoryByID(final String sID) {
    return (Directory) hmItems.get(sID);
  }

  @SuppressWarnings("unchecked")
  public synchronized Directory getDirectoryForIO(final java.io.File fio) {
    final Collection<Directory> dirs = hmItems.values();
    for (Directory dir : dirs) {
      if (dir.getFio().equals(fio)) {
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
   * Register a root device directory
   * 
   * @param device
   */
  public Directory registerDirectory(final Device device) {
    return registerDirectory(device.getID(), "", null, device);
  }

  /**
   * Register a directory
   * 
   * @param sName
   */
  public synchronized Directory registerDirectory(final String sName, final Directory dParent,
      final Device device) {
    return registerDirectory(DirectoryManager.createID(sName, device, dParent), sName, dParent,
        device);
  }

  /**
   * Register a directory with a known id
   * 
   * @param sName
   */
  public synchronized Directory registerDirectory(final String sId, final String sName,
      final Directory dParent, final Device device) {
    final Directory dir = (Directory) hmItems.get(sId);
    if (dir != null) {
      // Set name again because under Windows, dir name case could
      // have changed but
      // we keep the same directory object
      dir.setName(sName);
      return dir;
    }
    Directory directory = null;
    directory = new Directory(sId, sName, dParent, device);
    if (dParent != null) {
      // add the directory to parent
      dParent.addDirectory(directory);
    }
    hmItems.put(sId, directory);
    return directory;
  }

  /**
   * Remove a directory and all subdirectories from main directory repository.
   * Remove reference from parent directories as well.
   * 
   * @param sId
   */
  public synchronized void removeDirectory(final String sId) {
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
      PlaylistManager.getInstance().removeItem(plf.getID());
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
    hmItems.remove(dir.getID());
  }
}