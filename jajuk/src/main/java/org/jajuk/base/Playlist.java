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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jajuk.services.bookmark.Bookmarks;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.filters.PlaylistFilter;
import org.jajuk.util.log.Log;

/**
 * A playlist
 * <p>
 * Physical item
 */
public class Playlist extends PhysicalItem implements Comparable<Playlist> {

  private static final long serialVersionUID = 1L;

  /** playlist type */
  public enum Type {
    NORMAL, QUEUE, NEW, BOOKMARK, BESTOF, NOVELTIES
  }

  /** Playlist parent directory */
  private Directory dParentDirectory;

  /** Files list, singleton */
  private ArrayList<File> alFiles;

  /** Modification flag */
  private boolean bModified = false;

  /** Associated physical file */
  private java.io.File fio;

  /** Playlist type */
  private Type type;

  /** pre-calculated absolute path for perf */
  private String sAbs = null;

  /** Contains files outside device flag */
  private boolean bContainsExtFiles = false;

  /**
   * playlist constructor
   * 
   * @param type
   *          playlist type
   * @param sId
   * @param sName
   * @param sHashcode
   * @param sParentDirectory
   */
  public Playlist(final Type type, final String sId, final String sName,
      final Directory dParentDirectory) {
    super(sId, sName);
    this.dParentDirectory = dParentDirectory;
    setProperty(ITechnicalStrings.XML_DIRECTORY, dParentDirectory == null ? "-1" : dParentDirectory
        .getID().intern());
    this.type = type;
  }

  /**
   * playlist constructor
   * 
   * @param sId
   * @param sName
   * @param sHashcode
   * @param sParentDirectory
   */
  public Playlist(final String sId, final String sName, final Directory dParentDirectory) {
    this(Playlist.Type.NORMAL, sId, sName, dParentDirectory);
  }

  /**
   * Add a file to this playlist
   * 
   * @param bf
   */
  public void addFile(final File file) throws JajukException {
    final ArrayList<File> al = getFiles();
    final int index = al.size();
    addFile(index, file);
  }

  /**
   * @return Returns the Type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Add a file to this playlist
   * 
   * @param index
   * @param bf
   */
  public void addFile(final int index, final File file) throws JajukException {
    if (type == Playlist.Type.BOOKMARK) {
      Bookmarks.getInstance().addFile(index, file);
    } else if (type == Type.QUEUE) {
      final StackItem item = new StackItem(file);
      item.setUserLaunch(false);
      // set repeat mode : if previous item is repeated, repeat as
      // well
      if (index > 0) {
        final StackItem itemPrevious = FIFO.getInstance().getItem(index - 1);
        if ((itemPrevious != null) && itemPrevious.isRepeat()) {
          item.setRepeat(true);
        } else {
          item.setRepeat(false);
        }
        // insert this track in the fifo
        FIFO.getInstance().insert(item, index);
      } else {
        // start immediately playing
        FIFO.getInstance().push(item, false);
      }
    } else {
      getFiles().add(index, file);
      setModified(true);
    }
  }

  /**
   * Add some files to this playlist.
   * 
   * @param alFilesToAdd :
   *          List of File
   */
  public void addFiles(final List<File> alFilesToAdd) {
    try {
      for (File file : alFilesToAdd) {
        addFile(file);
      }
    } catch (final Exception e) {
      Log.error(e);
    }
  }

  /**
   * @return Returns the bModified.
   */
  public boolean isModified() {
    return bModified;
  }

  /**
   * Clear playlist
   * 
   */
  public void clear() {
    if (type == Type.BOOKMARK) { // bookmark
      // playlist
      Bookmarks.getInstance().clear();
    } else if (getType() == Type.QUEUE) {
      FIFO.getInstance().clear();
    } else {
      alFiles.clear();
    }
    setModified(true);
  }

  /**
   * Update playlist on disk if needed
   */
  public void commit() throws JajukException {
    BufferedWriter bw = null;
    java.io.File temp = null;
    if (isModified()) {
      try {
        /*
         * Due to bug #1046, we use a temporary file In some special cases
         * (reproduced under Linux, JRE SUN 1.6.0_04, CIFS mount, 777 rights
         * file), probably due to a JRE bug, files cannot be opened
         * (FileNotFound? Exception, permission denied) and the file is voided
         * (0 bytes) and is closed (checked with lsof).
         */
        temp = new java.io.File(getAbsolutePath() + '~');
        bw = new BufferedWriter(new FileWriter(temp));
        bw.write(ITechnicalStrings.PLAYLIST_NOTE);
        bw.newLine();
        final Iterator<File> it = getFiles().iterator();
        while (it.hasNext()) {
          final File file = it.next();
          if (file.getIO().getParent().equals(fio.getParent())) {
            bw.write(file.getName());
          } else {
            bw.write(file.getAbsolutePath());
          }
          bw.newLine();
        }
      } catch (final Exception e) {
        throw new JajukException(28, getName(), e);
      } finally {
        if (bw != null) {
          try {
            bw.flush();
            bw.close();
          } catch (final IOException e1) {
            throw new JajukException(28, getName(), e1);
          }
        }
      }
      // Now move the temp file to final one if everything seems ok
      if (temp != null && temp.exists() && temp.length() > 0) {
        try {
          Util.copy(temp, getFio());
          temp.delete();
        } catch (final Exception e1) {
          throw new JajukException(28, getName(), e1);
        }
      } else {
        try {
          // Try to remove the temp file
          temp.delete();
        } catch (final Exception e1) {
          Log.error(e1);
        }
        throw new JajukException(28, getName());
      }
    }
  }

  /**
   * Alphabetical comparator used to display ordered lists of playlists
   * <p>
   * Sort ignoring cases but different items with different cases should be
   * distinct before being added into bidimap
   * </p>
   * 
   * @param other
   *          playlistfile to be compared
   * @return comparison result
   */
  public int compareTo(final Playlist o) {
    // Perf: leave if items are equals
    if (o.equals(this)) {
      return 0;
    }
    final Playlist otherPlaylistFile = o;
    final String sAbs = getName() + getAbsolutePath();
    final String sOtherAbs = otherPlaylistFile.getName() + otherPlaylistFile.getAbsolutePath();
    // never return 0 here, because bidimap needs to distinct items
    final int comp = sAbs.compareToIgnoreCase(sOtherAbs);
    if (comp == 0) {
      return sAbs.compareTo(sOtherAbs);
    }
    return comp;
  }

  /**
   * 
   * @return whether this playlist contains files located out of known devices
   */
  public boolean containsExtFiles() {
    return bContainsExtFiles;
  }

  /**
   * Down a track in the playlist
   * 
   * @param index
   */
  public void down(final int index) {
    if (type == Type.BOOKMARK) {
      Bookmarks.getInstance().down(index);
    } else if (type == Type.QUEUE) {
      FIFO.getInstance().down(index);
    } else if ((alFiles != null) && (index < alFiles.size() - 1)) {
      // the last track cannot go depper
      final File file = alFiles.get(index + 1); // save n+1 file
      alFiles.set(index + 1, alFiles.get(index));
      // n+1 file becomes nth file
      alFiles.set(index, file);
      setModified(true);
    }
  }

  /**
   * Equal method to check two playlists are identical
   * 
   * @param otherPlaylistFile
   * @return
   */
  @Override
  public boolean equals(final Object otherPlaylistFile) {
    if (otherPlaylistFile == null) {
      return false;
    }
    final Playlist plfOther = (Playlist) otherPlaylistFile;
    return (getID().equals(plfOther.getID()) && (plfOther.getType() == type));
  }

  /**
   * Force playlist re-read (don't use the cache). Can be used after a forced
   * refresh
   * 
   */
  public void forceRefresh() throws JajukException {
    alFiles = load(); // populate playlist
  }

  /**
   * Return absolute file path name
   * 
   * @return String
   */
  public String getAbsolutePath() {
    if (type == Type.NORMAL) {
      if (sAbs != null) {
        return sAbs;
      }
      final Directory dCurrent = getDirectory();
      final StringBuilder sbOut = new StringBuilder(dCurrent.getDevice().getUrl()).append(
          dCurrent.getRelativePath()).append(java.io.File.separatorChar).append(getName());
      sAbs = sbOut.toString();
    } else {
      // smart playlist path depends on the user selected from the save as file
      // chooser and has been set using the setFio() method just before that
      sAbs = getFio().getAbsolutePath();
    }
    return sAbs;
  }

  /**
   * Get item description
   */
  @Override
  public String getDesc() {
    return Messages.getString("Item_Playlist_File") + " : " + getName();
  }

  /**
   * @return
   */
  public Directory getDirectory() {
    return dParentDirectory;
  }

  /**
   * @return Returns the list of files this playlist maps to
   */
  public ArrayList<File> getFiles() throws JajukException {
    // if normal playlist, propose to mount device if unmounted
    if ((getType() == Type.NORMAL) && !isReady()) {
      final String sMessage = Messages.getString("Error.025") + " ("
          + getDirectory().getDevice().getName() + Messages.getString("FIFO.4");
      final int i = Messages.getChoice(sMessage, JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.INFORMATION_MESSAGE);
      if (i == JOptionPane.YES_OPTION) {
        try {
          // mount. Note that we don't refresh UI to keep
          // selection on this playlist (otherwise the event reset
          // selection).
          getDirectory().getDevice().mount(
              ConfigurationManager.getBoolean(ITechnicalStrings.CONF_OPTIONS_HIDE_UNMOUNTED));
        } catch (final Exception e) {
          Log.error(e);
          throw new JajukException(141, getFio().getAbsolutePath(), null);
        }
      } else {
        throw new JajukException(141, getFio().getAbsolutePath(), null);
      }
    }
    if ((type == Type.NORMAL) && (alFiles == null)) {
      // normal playlist, test if list is null for perfs(avoid reading
      // again the m3u file)
      if (getFio().exists() && getFio().canRead()) {
        // check device is mounted
        alFiles = load(); // populate playlist
        if (containsExtFiles()) {
          Messages.showWarningMessage(Messages.getErrorMessage(142));
        }
      } else { // error accessing playlist
        throw new JajukException(9, getFio().getAbsolutePath(), new Exception());
      }
    } else if (type.equals(Type.BESTOF)) {
      alFiles = FileManager.getInstance().getBestOfFiles();
    } else if (type.equals(Type.NOVELTIES)) {
      alFiles = FileManager.getInstance().getGlobalNoveltiesPlaylist(
          ConfigurationManager.getBoolean(ITechnicalStrings.CONF_OPTIONS_HIDE_UNMOUNTED));
    } else if (type.equals(Type.BOOKMARK)) {
      alFiles = Bookmarks.getInstance().getFiles();
    } else if (type.equals(Type.NEW)) {
      if (alFiles == null) {
        alFiles = new ArrayList<File>(10);
      }

    }
    return alFiles;
  }

  /**
   * @return Returns the fio.
   */
  public java.io.File getFio() {
    if (fio == null) {
      fio = new java.io.File(getAbsolutePath());
    }
    return fio;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(final String sKey) {
    if (ITechnicalStrings.XML_DIRECTORY.equals(sKey)) {
      final Directory dParent = DirectoryManager.getInstance().getDirectoryByID(
          getStringValue(sKey));
      return dParent.getFio().getAbsolutePath();
    } else {// default
      return super.getHumanValue(sKey);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    return IconLoader.ICON_PLAYLIST_FILE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return ITechnicalStrings.XML_PLAYLIST_FILE;
  }

  /**
   * Return true the file can be accessed right now
   * 
   * @return true the file can be accessed right now
   */
  public boolean isReady() {
    if (getDirectory().getDevice().isMounted()) {
      return true;
    }
    return false;
  }

  /**
   * Parse a playlist
   */
  public ArrayList<File> load() throws JajukException {
    final ArrayList<File> alFiles = new ArrayList<File>(10);
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(getFio()));
      String sLine = null;
      boolean bUnknownDevicesMessage = false;
      while ((sLine = br.readLine()) != null) {
        if (sLine.length() == 0) { // void line
          continue;
        }
        // replace '\' by '/'
        sLine = sLine.replace('\\', '/');
        // deal with url begining by "./something"
        if (sLine.charAt(0) == '.') {
          sLine = sLine.substring(1, sLine.length());
        }
        final StringBuilder sb = new StringBuilder(sLine);
        // comment
        if (sb.charAt(0) == '#') {
          continue;
        } else {
          java.io.File fileTrack = null;
          final StringBuilder sbFileDir = new StringBuilder(getDirectory().getDevice().getUrl())
              .append(getDirectory().getRelativePath());
          if (sLine.charAt(0) != '/') {
            sb.insert(0, '/');
          }
          // take a look relatively to playlist directory to check
          // files exists
          fileTrack = new java.io.File(sbFileDir.append(sb).toString());
          File file = FileManager.getInstance().getFileByPath(fileTrack.getAbsolutePath());
          if (file == null) { // check if this file is known in
            // collection
            fileTrack = new java.io.File(sLine); // check if
            // given url is
            // not absolute
            file = FileManager.getInstance().getFileByPath(fileTrack.getAbsolutePath());
            if (file == null) { // no more ? leave
              bUnknownDevicesMessage = true;
              continue;
            }
          }
          alFiles.add(file);
        }
      }
      // display a warning message if the playlist contains unknown
      // items
      if (bUnknownDevicesMessage) {
        bContainsExtFiles = true;
      }
    } catch (final Exception e) {
      Log.error(17, "{{" + getName() + "}}", e);
      throw new JajukException(17, getFio().getAbsolutePath(), e);
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (final IOException e1) {
          Log.error(e1);
          throw new JajukException(17, getFio().getAbsolutePath(), e1);
        }
      }
    }
    return alFiles;
  }

  /**
   * Play a playlist
   * 
   */
  public void play() throws JajukException {
    alFiles = getFiles();
    if ((alFiles == null) || (alFiles.size() == 0)) {
      Messages.showErrorMessage(18);
    } else {
      FIFO.getInstance().push(
          Util.createStackItems(Util.applyPlayOption(alFiles), ConfigurationManager
              .getBoolean(ITechnicalStrings.CONF_STATE_REPEAT), true), false);
    }
  }

  /**
   * Remove a track from the playlist
   * 
   * @param index
   */
  public void remove(final int index) {
    if (type == Type.BOOKMARK) {
      Bookmarks.getInstance().remove(index);
    } else if (type == Type.QUEUE) {
      FIFO.getInstance().remove(index, index);
    } else {
      alFiles.remove(index);
    }
    setModified(true);
  }

  /**
   * Replace a file inside a playlist
   * 
   * @param fOld
   * @param fNew
   * @throws JajukException
   */
  public void replaceFile(final File fOld, final File fNew) throws JajukException {
    if (type == Type.BOOKMARK) {
      final Iterator<File> it = Bookmarks.getInstance().getFiles().iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = it.next();
        if (fileToTest.equals(fOld)) {
          Bookmarks.getInstance().remove(i);
          Bookmarks.getInstance().addFile(i, fNew);
        }
      }
    } else if (type == Type.QUEUE) {
      final Iterator<StackItem> it = FIFO.getInstance().getFIFO().iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = it.next().getFile();
        if (fileToTest.equals(fOld)) {
          FIFO.getInstance().remove(i, i); // just remove
          final ArrayList<StackItem> al = new ArrayList<StackItem>(1);
          al.add(new StackItem(fNew));
          FIFO.getInstance().insert(al, i);
        }
      }
    } else {
      final Iterator<File> it = alFiles.iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = it.next();
        if (fileToTest.equals(fOld)) {
          alFiles.remove(i);
          alFiles.add(i, fNew);
          try {
            commit();// save changed playlist
          } catch (final JajukException e) {
            Log.error(e);
          }
        }
      }
    }
    setModified(true);
  }

  /** Reset pre-calculated paths* */
  protected void reset() {
    sAbs = null;
    fio = null;
  }

  /**
   * Save as... the playlist
   */
  public void saveAs() throws Exception {
    final JajukFileChooser jfchooser = new JajukFileChooser(new JajukFileFilter(PlaylistFilter
        .getInstance()));
    jfchooser.setDialogType(JFileChooser.SAVE_DIALOG);
    jfchooser.setAcceptDirectories(true);
    String sPlaylist = ITechnicalStrings.DEFAULT_PLAYLIST_FILE;
    // computes new playlist
    alFiles = getFiles();
    if (alFiles.size() > 0) {
      final File file = alFiles.get(0);
      if (getType() == Type.BESTOF) {
        sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
            + ITechnicalStrings.FILE_DEFAULT_BESTOF_PLAYLIST;
      } else if (getType() == Type.BOOKMARK) {
        sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
            + ITechnicalStrings.FILE_DEFAULT_BOOKMARKS_PLAYLIST;
      } else if (getType() == Type.NOVELTIES) {
        sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
            + ITechnicalStrings.FILE_DEFAULT_NOVELTIES_PLAYLIST;
      } else if (getType() == Type.QUEUE) {
        sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
            + ITechnicalStrings.FILE_DEFAULT_QUEUE_PLAYLIST
            + Util.getAdditionDateFormatter().format(new Date());
      } else {
        sPlaylist = file.getDirectory().getAbsolutePath() + java.io.File.separatorChar
            + file.getTrack().getHumanValue(ITechnicalStrings.XML_ALBUM);
      }
    } else {
      return;
    }
    jfchooser.setSelectedFile(new java.io.File(sPlaylist + "." + ITechnicalStrings.EXT_PLAYLIST));
    final int returnVal = jfchooser.showSaveDialog(JajukWindow.getInstance());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      java.io.File file = jfchooser.getSelectedFile();
      // add automatically the extension if required
      if (file.getAbsolutePath().endsWith(ITechnicalStrings.EXT_PLAYLIST)) {
        file = new java.io.File(file.getAbsolutePath());
      } else {
        file = new java.io.File(file.getAbsolutePath() + "." + ITechnicalStrings.EXT_PLAYLIST);
      }

      // set new file path ( this playlist is a special playlist, just in
      // memory )
      setFio(file);
      commit(); // write it on the disk
      final java.io.File fDir = file.getParentFile();
      final Directory dir = DirectoryManager.getInstance().getDirectoryForIO(fDir);
      if (dir != null) { // the new playlist in inside collection
        final Playlist plFile = PlaylistManager.getInstance().registerPlaylistFile(file, dir);
        dir.addPlaylistFile(plFile);
      }
    }
  }

  /**
   * @param alFiles
   *          The alFiles to set.
   */
  public void setFiles(final ArrayList<File> alFiles) {
    this.alFiles = alFiles;
  }

  /**
   * @param fio
   *          The fio to set.
   */
  public void setFio(final java.io.File fio) {
    setModified(true);
    this.fio = fio;
  }

  /**
   * @param modified
   *          The bModified to set.
   */
  public void setModified(final boolean modified) {
    bModified = modified;
  }

  /**
   * @param parentDirectory
   *          The dParentDirectory to set.
   */
  protected void setParentDirectory(final Directory parentDirectory) {
    dParentDirectory = parentDirectory;
    setProperty(ITechnicalStrings.XML_DIRECTORY, parentDirectory == null ? "-1" : parentDirectory
        .getID());
  }

  /**
   * Return whether this item should be hidden with hide option
   * 
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getDirectory().getDevice().isMounted()
        || (ConfigurationManager.getBoolean(ITechnicalStrings.CONF_OPTIONS_HIDE_UNMOUNTED) == false)) {
      // option "only display mounted devices"
      return false;
    }
    return true;
  }

  /**
   * Stores all playlist and mapped files into an external device
   */
  public void prepareParty() throws Exception {
    final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(DirectoryFilter
        .getInstance()));
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    jfc.setDialogTitle(Messages.getString("FirstTimeWizard.5"));
    jfc.setMultiSelectionEnabled(false);
    final int returnVal = jfc.showDialog(JajukWindow.getInstance(), "Ok");
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      new Thread() {
        @Override
        public void run() {
          Util.waiting();
          final java.io.File fDir = jfc.getSelectedFile();
          final Date curDate = new Date();
          final SimpleDateFormat Stamp = new SimpleDateFormat("ddMMyyyy-HHmm");
          final String dirName = "Party-" + Stamp.format(curDate);
          final java.io.File destDir = new java.io.File(fDir.getAbsolutePath() + "/" + dirName);
          destDir.mkdir();
          final java.io.File file = new java.io.File(destDir.getAbsolutePath() + "/playlist.m3u");
          try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(ITechnicalStrings.PLAYLIST_NOTE);
            for (final File entry : alFiles) {
              Util.copyToDir(entry.getIO(), destDir);
              bw.newLine();
              bw.write(entry.getAbsolutePath());
              // Notify that a file has been copied
              Properties properties = new Properties();
              properties.put(DETAIL_CONTENT, entry.getName());
              ObservationManager.notify(new Event(EventSubject.EVENT_FILE_COPIED, properties));
            }

            bw.flush();
            bw.close();
            // Send a last event with null properties to inform the client that
            // the party is done
            ObservationManager.notify(new Event(EventSubject.EVENT_FILE_COPIED));

          } catch (final Exception e) {
            Log.error(e);
          }
          Util.stopWaiting();
          Messages.showInfoMessage(dirName + " "
              + Messages.getString("AbstractPlaylistEditorView.28") + " " + fDir.getAbsolutePath());
        }
      }.start();
    }
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "playlist[ID=" + getID() + " Name={{" + getName() + "}} " + " Dir="
        + dParentDirectory.getID() + "]";
  }

  /**
   * Up a track in the playlist
   * 
   * @param index
   */
  public void up(final int index) {
    if (type == Type.BOOKMARK) {
      Bookmarks.getInstance().up(index);
    } else if (type == Playlist.Type.QUEUE) {
      FIFO.getInstance().up(index);
    } else if ((alFiles != null) && (index > 0)) { // the first track
      // cannot go further
      final File file = alFiles.get(index - 1); // save n-1 file
      alFiles.set(index - 1, alFiles.get(index));
      alFiles.set(index, file); // n-1 file becomes nth file
      setModified(true);
    }
  }

  /**
   * @return playlist average rating
   */
  public long getRate() {
    float rate = 0f;
    int nb = 0;
    for (File file : alFiles) {
      rate += file.getTrack().getRate();
      nb++;
    }
    return Math.round(rate / nb);
  }

  /**
   * @return total nb of hits
   */
  public long getHits() {
    int hits = 0;
    for (File file : alFiles) {
      hits += file.getTrack().getHits();
    }
    return hits;
  }

  /**
   * Return full playlist length in secs
   */
  public long getDuration() {
    long length = 0;
    for (File file : alFiles) {
      length += file.getTrack().getDuration();
    }
    return length;
  }

  /**
   * @return playlist nb of tracks
   */
  public int getNbOfTracks() {
    return alFiles.size();
  }

}
