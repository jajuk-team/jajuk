/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.services.bookmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * Manages bookmarks.
 */
public final class Bookmarks {
  /** Singleton self-instance. */
  private static Bookmarks bookmarks = new Bookmarks();
  /** Bookmarked files. */
  private List<File> alFiles = new ArrayList<File>(100);

  /**
   * Gets the single instance of Bookmarks.
   * 
   * @return single instance of Bookmarks
   */
  public static Bookmarks getInstance() {
    return bookmarks;
  }

  /**
   * Private constructor.
   */
  private Bookmarks() {
    String sBookmarks = Conf.getString(Const.CONF_BOOKMARKS);
    if (sBookmarks == null || "".equals(sBookmarks.trim())) {
      return;
    }
    StringTokenizer stFiles = new StringTokenizer(sBookmarks, ",");
    while (stFiles.hasMoreTokens()) {
      String sId = stFiles.nextToken();
      File file = FileManager.getInstance().getFileByID(sId);
      if (file != null) {
        alFiles.add(file);
      }
    }
  }

  /**
   * Return bookmarks as a colon separated list of file ids.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    // concatenate all files
    StringBuilder sbOut = new StringBuilder();
    for (File file : alFiles) {
      sbOut.append(file.getID()).append(',');
    }
    if (sbOut.length() > 0) {
      return sbOut.substring(0, sbOut.length() - 1);// remove last ','
    } else {
      return "";
    }
  }

  /**
   * Return bookmarked files.
   * 
   * @return the files
   */
  public List<File> getFiles() {
    return alFiles;
  }

  /**
   * Clear bookmarks.
   */
  public void clear() {
    alFiles.clear();
    Conf.setProperty(Const.CONF_BOOKMARKS, "");
  }

  /**
   * Down a track in the playlist.
   * 
   * @param index 
   */
  public synchronized void down(int index) {
    if (index < alFiles.size() - 1) { // the last track cannot go
      // deeper
      Collections.swap(alFiles, index, index + 1);
      Conf.setProperty(Const.CONF_BOOKMARKS, toString());
    }
  }

  /**
   * Up a track in the playlist.
   * 
   * @param index 
   */
  public synchronized void up(int index) {
    if (index > 0) { // the first track cannot go further
      Collections.swap(alFiles, index, index - 1);
      Conf.setProperty(Const.CONF_BOOKMARKS, toString());
    }
  }

  /**
   * Remove a track from the playlist.
   * 
   * @param index 
   */
  public synchronized void remove(int index) {
    alFiles.remove(index);
    Conf.setProperty(Const.CONF_BOOKMARKS, toString());
  }

  /**
   * Add a track from the playlist.
   * 
   * @param index 
   * @param file 
   */
  public synchronized void addFile(int index, File file) {
    alFiles.add(index, file);
    Conf.setProperty(Const.CONF_BOOKMARKS, toString());
  }

  /**
   * Add a file to this playlist.
   * 
   * @param file 
   */
  public void addFile(File file) {
    int index = alFiles.size();
    addFile(index, file);
  }

  /**
   * Add files to this playlist.
   * 
   * @param alFilesToAdd 
   */
  public void addFiles(List<File> alFilesToAdd) {
    for (File file : alFilesToAdd) {
      addFile(file);
    }
  }
}
