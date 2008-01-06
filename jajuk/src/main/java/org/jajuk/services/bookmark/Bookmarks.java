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
 * $Revision$
 */

package org.jajuk.services.bookmark;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 * Manages bookmarks
 * 
 */
public class Bookmarks implements ITechnicalStrings {

  /** Singleton self-instance */
  private static Bookmarks bookmarks;

  /** Bookmarks files */
  ArrayList<File> alFiles = new ArrayList<File>(100);

  public static synchronized Bookmarks getInstance() {
    if (bookmarks == null) {
      bookmarks = new Bookmarks();
    }
    return bookmarks;
  }

  /** Private constructor */
  private Bookmarks() {
    String sBookmarks = ConfigurationManager.getProperty(CONF_BOOKMARKS);
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
   * Return bookmarks as a colon separated list of file ids
   */
  public String toString() {
    StringBuilder sbOut = new StringBuilder();
    for (File file : alFiles) {
      sbOut.append(file.getID()).append(',');
    }
    int i = sbOut.length();
    return sbOut.substring(0, i - 1);// remove last ','
  }

  /** Return bookmarked files */
  public ArrayList<File> getFiles() {
    return alFiles;
  }

  /**
   * Clear bookmarks
   * 
   */
  public void clear() {
    alFiles.clear();
    ConfigurationManager.setProperty(CONF_BOOKMARKS, "");
  }

  /**
   * Down a track in the playlist
   * 
   * @param index
   */
  public synchronized void down(int index) {
    if (index < alFiles.size() - 1) { // the last track cannot go
      // depper
      File file = alFiles.get(index + 1); // save n+1 file
      alFiles.set(index + 1, alFiles.get(index));
      alFiles.set(index, file); // n+1 file becomes nth file
      ConfigurationManager.setProperty(CONF_BOOKMARKS, toString());
    }
  }

  /**
   * Up a track in the playlist
   * 
   * @param index
   */
  public synchronized void up(int index) {
    if (index > 0) { // the first track cannot go further
      File file = alFiles.get(index - 1); // save n-1 file
      alFiles.set(index - 1, alFiles.get(index));
      alFiles.set(index, file); // n-1 file becomes nth file
      ConfigurationManager.setProperty(CONF_BOOKMARKS, toString());
    }
  }

  /**
   * Remove a track from the playlist
   * 
   * @param index
   */
  public synchronized void remove(int index) {
    alFiles.remove(index);
    ConfigurationManager.setProperty(CONF_BOOKMARKS, toString());
  }

  /**
   * Add a track from the playlist
   * 
   * @param index
   */
  public synchronized void addFile(int index, File file) {
    alFiles.add(index, file);
    ConfigurationManager.setProperty(CONF_BOOKMARKS, toString());
  }

  /**
   * Add a file to this playlist
   * 
   * @param file
   */
  public void addFile(File file) {
    int index = alFiles.size();
    addFile(index, file);
  }

  /**
   * Add files to this playlist
   * 
   * @param alFilesToAdd
   */
  public void addFiles(List<File> alFilesToAdd) {
    try {
      for (File file : alFilesToAdd) {
        addFile(file);
      }
    } catch (Exception e) {
      Log.error(e);
    } finally {
      // refresh playlist editor
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
    }
  }
}
