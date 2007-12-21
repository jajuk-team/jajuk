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

import static org.jajuk.util.Resources.XML.DEVICE;
import static org.jajuk.util.Resources.XML.DIRECTORIES;
import static org.jajuk.util.Resources.XML.DIRECTORY_DEFAULT_COVER;
import static org.jajuk.util.Resources.XML.DIRECTORY_PARENT;
import static org.jajuk.util.Resources.XML.DIRECTORY_SYNCHRONIZED;
import static org.jajuk.util.Resources.XML.EXPANDED;
import static org.jajuk.util.Resources.XML.ID;
import static org.jajuk.util.Resources.XML.NAME;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jajuk.base.IItem;
import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.items.Device;
import org.jajuk.base.items.Directory;
import org.jajuk.base.items.File;
import org.jajuk.base.items.PlaylistFile;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.filters.DirectoryFilter;

/**
 * Convenient class to manage directories
 */
public class DirectoryManager extends ItemManager<Directory> {

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
  public static String createID(final String sName, final Device device, final Directory dParent) {
    final StringBuilder sbAbs = new StringBuilder(device.getName());
    // Under windows, all files/directories with different cases should get
    // the same ID
    if (Util.isUnderWindows()) {
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
    final String sId = MD5Processor.hash(sbAbs.toString());
    return sId;
  }

  /**
   * Register a directory for refreshing
   *
   * @param Directory
   *          d
   */
  public void refreshDirectory(final Directory d) {
    final java.io.File dirList[] = d.getFio().listFiles(
        new JajukFileFilter(DirectoryFilter.getInstance()));
    if ((dirList != null) && (dirList.length != 0)) {
      for (final java.io.File f : dirList) {
        final Directory dir = registerDirectory(f.getName(), d,
            d.getDevice());
        refreshDirectory(dir);
      }
    } else {
      d.scan(true, null);
    }
  }

  /**
   * No constructor available, only static access
   */
  public DirectoryManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new MetaProperty(ID, false, true, false,
        false, false, String.class, null));
    // Name test with (getParentDirectory() != null); //name editable only
    // for standard
    // directories, not root
    registerProperty(new MetaProperty(NAME, false, true, true,
        false, false, String.class, null)); // edition to
    // yet
    // implemented
    // TBI
    // Parent
    registerProperty(new MetaProperty(DIRECTORY_PARENT, false,
        true, true, false, false, String.class, null));
    // Device
    registerProperty(new MetaProperty(DEVICE, false, true, true,
        false, false, String.class, null));
    // Expand
    registerProperty(new MetaProperty(EXPANDED, false, false,
        false, false, true, Boolean.class, false));
    // Synchonized directory
    registerProperty(new MetaProperty(DIRECTORY_SYNCHRONIZED,
        false, false, true, false, false, Boolean.class, true));
    // Default cover
    registerProperty(new MetaProperty(DIRECTORY_DEFAULT_COVER,
        false, false, true, false, false, String.class, null));
  }

  /**
   * Clean all references for the given device
   *
   * @param sId :
   *          Device id
   */
  public void cleanDevice(final String sId) {
    synchronized (getLock()) {
      for (final Entry<String, Directory> entry : getItems().entrySet()) {
        final Directory directory = getDirectoryByID(entry.getKey());

        if (directory.getDevice().getID().equals(sId)) {
          getItems().remove(entry.getKey());
        }
      }
    }
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Directory getDirectoryByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }

  public Directory getDirectoryForIO(final java.io.File fio) {
    synchronized (getLock()) {
      Directory directory = null;

      for (final IItem item : getItems().values()) {
        final Directory dir = (Directory) item;

        if (dir.getFio().equals(fio)) {
          directory = dir;
          break;
        }
      }
      return (directory);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return DIRECTORIES;
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
  public Directory registerDirectory(final String sName, final Directory dParent,
      final Device device) {
    synchronized (getLock()) {
      return registerDirectory(DirectoryManager.createID(sName, device, dParent), sName, dParent,
          device);
    }
  }

  /**
   * Register a directory with a known id
   *
   * @param sName
   */
  public Directory registerDirectory(final String sId, final String sName, final Directory dParent,
      final Device device) {
    synchronized (getLock()) {
      final Directory dir = getItems().get(sId);
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
      getItems().put(sId, directory);
      return directory;
    }
  }

  /**
   * Remove a directory and all subdirectories from main directory repository.
   * Remove reference from parent directories as well.
   *
   * @param sId
   */
  public void removeDirectory(final String sId) {
    synchronized (getLock()) {
      final Directory dir = getDirectoryByID(sId);
      if (dir == null) {// check the directory has not already been
        // removed
        return;
      }
      synchronized (((FileManager) ItemType.File.getManager()).getLock()) {
        // remove all files
        // need to use a shallow copy to avoid concurent exceptions
        final ArrayList<File> alFiles = new ArrayList<File>(dir.getFiles());
        for (final File file : alFiles) {
          ((FileManager) ItemType.File.getManager()).removeFile(file);
        }
      }
      synchronized (((PlaylistFileManager) ItemType.PlaylistFile.getManager()).getLock()) {
        // remove all playlists
        for (final PlaylistFile plf : dir.getPlaylistFiles()) {
          ((PlaylistFileManager) ItemType.PlaylistFile.getManager()).removeItem(plf.getID());
        }
      }
      // remove all sub dirs
      final Iterator it = dir.getDirectories().iterator();
      while (it.hasNext()) {
        final Directory dSub = (Directory) it.next();
        removeDirectory(dSub.getID()); // self call
        // remove it
        it.remove();
      }
      // remove this dir from collection
      getItems().remove(dir.getID());
    }
  }
}