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
package org.jajuk.base.items;

import static org.jajuk.util.Resources.ConfKeys.OPTIONS_HIDE_UNMOUNTED;
import static org.jajuk.util.Resources.ConfKeys.STATE_REPEAT;
import static org.jajuk.util.Resources.XML.DIRECTORY;
import static org.jajuk.util.Resources.XML.HASHCODE;
import static org.jajuk.util.Resources.XML.PLAYLIST_FILE;
import static org.jajuk.util.Resources.XML.TYPE;

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

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ItemType;
import org.jajuk.base.PhysicalItem;
import org.jajuk.base.StackItem;
import org.jajuk.base.managers.DirectoryManager;
import org.jajuk.base.managers.FileManager;
import org.jajuk.base.managers.ObservationManager;
import org.jajuk.base.managers.PlaylistFileManager;
import org.jajuk.base.managers.PlaylistManager;
import org.jajuk.ui.helpers.PlaylistFileItem;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.Resources;
import org.jajuk.util.Util;
import org.jajuk.util.Resources.Extensions;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.filters.PlaylistFilter;
import org.jajuk.util.log.Log;

/**
 * A playlist file
 * <p>
 * Physical item
 */
public class PlaylistFile extends PhysicalItem implements Comparable {

  private static final long serialVersionUID = 1L;

  /** Playlist parent directory */
  private Directory dParentDirectory;

  /** Files list, singleton */
  private ArrayList<File> alFiles;

  /** Modification flag */
  private boolean bModified = false;

  /** Associated physical file */
  private java.io.File fio;

  /** Type type */
  private int iType;

  /** pre-calculated absolute path for perf */
  private String sAbs = null;

  /** Contains files outside device flag */
  private boolean bContainsExtFiles = false;

  /**
   * Playlist file constructor
   *
   * @param iType
   *          playlist file type
   * @param sId
   * @param sName
   * @param sHashcode
   * @param sParentDirectory
   */
  public PlaylistFile(final int iType, final String sId, final String sName,
      final Directory dParentDirectory) {
    super(sId, sName);
    this.dParentDirectory = dParentDirectory;
    getProperties().set(DIRECTORY, dParentDirectory == null ? "-1" : dParentDirectory.getID().intern());
    this.iType = iType;
  }

  /**
   * Playlist file constructor
   *
   * @param sId
   * @param sName
   * @param sHashcode
   * @param sParentDirectory
   */
  public PlaylistFile(final String sId, final String sName, final Directory dParentDirectory) {
    this(PlaylistFileItem.PLAYLIST_TYPE_NORMAL, sId, sName, dParentDirectory);
  }

  /**
   * Add a file to this playlist file
   *
   * @param bf
   */
  public synchronized void addFile(final File file) throws JajukException {
    final ArrayList al = getFiles();
    final int index = al.size();
    addFile(index, file);
  }

  /**
   * Add a file to this playlist file
   *
   * @param index
   * @param bf
   */
  public synchronized void addFile(final int index, final File file) throws JajukException {
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
      Bookmarks.getInstance().addFile(index, file);
    }
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
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
        // start immediatly playing
        FIFO.getInstance().push(item, false);
      }
    } else {
      getFiles().add(index, file);
      setModified(true);
    }
  }

  /**
   * Add some files to this playlist file.
   *
   * @param alFilesToAdd :
   *          List of File
   */
  public synchronized void addFiles(final List<File> alFilesToAdd) {
    try {
      final Iterator it = alFilesToAdd.iterator();
      while (it.hasNext()) {
        final org.jajuk.base.items.File file = (org.jajuk.base.items.File) it.next();
        addFile(file);
      }
    } catch (final Exception e) {
      Log.error(e);
    } finally {
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
    }
  }

  /**
   * Clear playlist file
   *
   */
  public synchronized void clear() {
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) { // bookmark
      // playlist
      Bookmarks.getInstance().clear();
    } else if (getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
      FIFO.getInstance().clear();
    } else {
      alFiles.clear();
    }
    setModified(true);
  }

  /**
   * Update playlist file on disk if needed
   */
  public void commit() throws JajukException {
    BufferedWriter bw = null;
    if (isModified()) {
      try {
        bw = new BufferedWriter(new FileWriter(getFio()));
        bw.write(Resources.PLAYLIST_NOTE);
        bw.newLine();
        final Iterator it = getFiles().iterator();
        while (it.hasNext()) {
          final File bfile = (File) it.next();
          if (bfile.getDirectory().equals(getDirectory())) {
            bw.write(bfile.getName());
          } else {
            bw.write(bfile.getAbsolutePath());
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
            setHashcode(computesHashcode());
            // Associated logical playlist is null for special
            // playlists
            if (((PlaylistManager) ItemType.Playlist.getManager()).getPlayList(this) != null) {
              ((PlaylistManager) ItemType.Playlist.getManager()).refreshPlaylist(this);
            }
            ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
            // refresh repository list(mandatory for logical
            // playlist collapse/merge)
          } catch (final IOException e1) {
            throw new JajukException(28, getName(), e1);
          }
        }
      }
    }
  }

  /**
   * Alphabetical comparator used to display ordered lists of playlist files
   * <p>
   * Sort ignoring cases but different items with different cases should be
   * distinct before being added into bidimap
   * </p>
   *
   * @param other
   *          playlistfile to be compared
   * @return comparaison result
   */
  public int compareTo(final Object o) {
    // Perf: leave if items are equals
    if (o.equals(this)) {
      return 0;
    }
    final PlaylistFile otherPlaylistFile = (PlaylistFile) o;
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
   * @return playlist file hashcode based on its content
   * @throws IOException
   */
  public String computesHashcode() throws IOException {
    final BufferedReader br = new BufferedReader(new FileReader(getFio()));
    final StringBuilder sbContent = new StringBuilder();
    String sTemp;
    do {
      sTemp = br.readLine();
      sbContent.append(sTemp);
    } while (sTemp != null);
    return MD5Processor.hash(sbContent.toString());
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
  public synchronized void down(final int index) {
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
      Bookmarks.getInstance().down(index);
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
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
   * Equal method to check two playlist files are identical
   *
   * @param otherPlaylistFile
   * @return
   */
  @Override
  public boolean equals(final Object otherPlaylistFile) {
    if (otherPlaylistFile == null) {
      return false;
    }
    final PlaylistFile plfOther = (PlaylistFile) otherPlaylistFile;
    return (getID().equals(plfOther.getID()) && (plfOther.getType() == iType));
  }

  /**
   * Force playlist re-read (don't use the cache). Can be used after a forced
   * refresh
   *
   */
  protected synchronized void forceRefresh() {
    try {
      alFiles = load(); // populate playlist
    } catch (final JajukException je) {
      Log.error(je);
    }
  }

  /**
   * Return absolute file path name
   *
   * @return String
   */
  public String getAbsolutePath() {
    if (sAbs != null) {
      return sAbs;
    }
    final Directory dCurrent = getDirectory();
    final StringBuilder sbOut = new StringBuilder(getDirectory().getDevice().getUrl()).append(
        dCurrent.getRelativePath()).append(java.io.File.separatorChar).append(getName());
    sAbs = sbOut.toString();
    return sAbs;
  }

  /**
   * Get item description
   */
  @Override
  public String getDescription() {
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
  public synchronized ArrayList<File> getFiles() throws JajukException {
    // if normal playlist, propose to mount device if unmounted
    if ((getType() == PlaylistFileItem.PLAYLIST_TYPE_NORMAL) && !isReady()) {
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
              ConfigurationManager.getBoolean(OPTIONS_HIDE_UNMOUNTED));
        } catch (final Exception e) {
          Log.error(e);
          Messages.showErrorMessage(11, getDirectory().getDevice().getName());
          throw new JajukException(141, getFio().getAbsolutePath(), null);
        }
      } else {
        throw new JajukException(141, getFio().getAbsolutePath(), null);
      }
    }
    if ((iType == PlaylistFileItem.PLAYLIST_TYPE_NORMAL) && (alFiles == null)) {
      // normal playlist, test if list is null for perfs(avoid reading
      // again the m3u file)
      if (getFio().exists() && getFio().canRead()) {
        // check device is mounted
        alFiles = load(); // populate playlist
        if (containsExtFiles()) {
          Messages.showWarningMessage(Messages.getErrorMessage(142));
        }
      } else { // error accessing playlist file
        throw new JajukException(9, getFio().getAbsolutePath(), new Exception());
      }
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_BESTOF) {
      // bestof playlist
      alFiles = new ArrayList<File>(10);
      // even unmounted files if required
      for (final File file : ((FileManager) ItemType.File.getManager()).getBestOfFiles()) {
        alFiles.add(file);
      }
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES) {
      // novelties playlist
      alFiles = new ArrayList<File>(10);
      for (final File file : ((FileManager) ItemType.File.getManager()).getGlobalNoveltiesPlaylist(
          ConfigurationManager.getBoolean(OPTIONS_HIDE_UNMOUNTED))) {
        alFiles.add(file);
      }
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
      // bookmark playlist
      alFiles = Bookmarks.getInstance().getFiles();
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_NEW) {
      // new playlist
      if (alFiles == null) {
        alFiles = new ArrayList<File>(10);
      }
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
      // queue playlist clean data
      if (!FIFO.isStopped()) {
        for (final StackItem item : FIFO.getInstance().getFIFO()) {
          alFiles.add(item.getFile());
        }
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

  /**
   * @return
   */
  public String getHashcode() {
    return getStringValue(HASHCODE);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(final String sKey) {
    if (DIRECTORY.equals(sKey)) {
      final Directory dParent = ((DirectoryManager) ItemType.Directory.getManager()).getDirectoryByID(
          getStringValue(sKey));
      return dParent.getFio().getAbsolutePath();
    } else {// default
      return super.getHumanValue(sKey);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIcon()
   */
  @Override
  public ImageIcon getIcon() {
    return IconLoader.ICON_PLAYLIST_FILE;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return PLAYLIST_FILE;
  }


  /**
   * @return Returns the bModified.
   */
  public boolean isModified() {
    return bModified;
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
   * Parse a playlist file
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
          File file = ((FileManager) ItemType.File.getManager()).getFileByPath(fileTrack.getAbsolutePath());
          if (file == null) { // check if this file is known in
            // collection
            fileTrack = new java.io.File(sLine); // check if
            // given url is
            // not absolute
            file = ((FileManager) ItemType.File.getManager()).getFileByPath(fileTrack.getAbsolutePath());
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
   * Play a playlist file
   *
   */
  public void play() throws JajukException {
    alFiles = getFiles();
    if ((alFiles == null) || (alFiles.size() == 0)) {
      Messages.showErrorMessage(18);
    } else {
      FIFO.getInstance().push(
          Util.createStackItems(Util.applyPlayOption(alFiles), ConfigurationManager
              .getBoolean(STATE_REPEAT), true), false);
    }
  }

  /**
   * Remove a fiven file from the playlist (can have several occurences)
   *
   * @param file
   *          to drop
   */
  public synchronized void remove(final File file) {
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
      final Iterator it = Bookmarks.getInstance().getFiles().iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = (File) it.next();
        if (fileToTest.equals(file)) {
          Bookmarks.getInstance().remove(i);
        }
      }
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
      final Iterator it = FIFO.getInstance().getFIFO().iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = (File) it.next();
        if (fileToTest.equals(file)) {
          FIFO.getInstance().remove(i, i);
        }
      }
    } else {
      final Iterator it = alFiles.iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = (File) it.next();
        if (fileToTest.equals(file)) {
          alFiles.remove(i);
        }
      }
    }
    setModified(true);
  }

  /**
   * Remove a track from the playlist
   *
   * @param index
   */
  public synchronized void remove(final int index) {
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
      Bookmarks.getInstance().remove(index);
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
      FIFO.getInstance().remove(index, index);
    } else {
      alFiles.remove(index);
    }
    setModified(true);
  }

  /**
   * Relace a file inside a playlist
   *
   * @param fOld
   * @param fNew
   * @throws JajukException
   */
  public void replaceFile(final File fOld, final File fNew) throws JajukException {
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
      final Iterator it = Bookmarks.getInstance().getFiles().iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = (File) it.next();
        if (fileToTest.equals(fOld)) {
          Bookmarks.getInstance().remove(i);
          Bookmarks.getInstance().addFile(i, fNew);
        }
      }
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
      final Iterator it = FIFO.getInstance().getFIFO().iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = (File) it.next();
        if (fileToTest.equals(fOld)) {
          FIFO.getInstance().remove(i, i); // just emove
          final ArrayList<StackItem> al = new ArrayList<StackItem>(1);
          al.add(new StackItem(fNew));
          FIFO.getInstance().insert(al, i);
        }
      }
    } else {
      final Iterator it = alFiles.iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = (File) it.next();
        if (fileToTest.equals(fOld)) {
          alFiles.remove(i);
          alFiles.add(i, fNew);
          try {
            commit();// save changed playlist file
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
   * Save as... the playlist file
   */
  public void saveAs() throws Exception {
    final JajukFileChooser jfchooser = new JajukFileChooser(new JajukFileFilter(PlaylistFilter
        .getInstance()));
    jfchooser.setDialogType(JFileChooser.SAVE_DIALOG);
    jfchooser.setAcceptDirectories(true);
    String sPlaylist = Resources.DEFAULT_PLAYLIST_FILE;
    // computes new playlist file
    alFiles = getFiles();
    if (alFiles.size() > 0) {
      final File file = alFiles.get(0);
      if (getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF) {
        sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
            + Resources.FILE_DEFAULT_BESTOF_PLAYLIST;
      } else if (getType() == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
        sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
            + Resources.FILE_DEFAULT_BOOKMARKS_PLAYLIST;
      } else if (getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES) {
        sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
            + Resources.FILE_DEFAULT_BOOKMARKS_PLAYLIST;
      } else if (getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
        sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
            + Resources.FILE_DEFAULT_QUEUE_PLAYLIST;
      } else {
        sPlaylist = file.getDirectory().getAbsolutePath() + java.io.File.separatorChar
            + file.getTrack().getHumanValue(Resources.XML.ALBUM);
      }
    } else {
      return;
    }
    jfchooser.setSelectedFile(new java.io.File(sPlaylist + "." + Extensions.PLAYLIST));
    final int returnVal = jfchooser.showSaveDialog(Main.getWindow());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      java.io.File file = jfchooser.getSelectedFile();
      // add automaticaly the extension if required
      if (file.getAbsolutePath().endsWith(Extensions.PLAYLIST)) {
        file = new java.io.File(file.getAbsolutePath());
      } else {
        file = new java.io.File(file.getAbsolutePath() + "." + Extensions.PLAYLIST);
      }

      // set new file path ( this playlist is a special playlist, just in
      // memory )
      setFio(file);
      commit(); // write it on the disk
      final java.io.File fDir = file.getParentFile();
      final Directory dir = ((DirectoryManager) ItemType.Directory.getManager()).getDirectoryForIO(fDir);
      if (dir != null) { // the new playlist file in inside collection
        final PlaylistFile plFile = ((PlaylistFileManager) ItemType.PlaylistFile.getManager()).registerPlaylistFile(file,
            dir);
        ((PlaylistManager) ItemType.Playlist.getManager()).registerPlaylist(plFile);
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
   * @param hashcode
   *          The sHashcode to set.
   */
  protected void setHashcode(final String hashcode) {
    getProperties().set(HASHCODE, hashcode);
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
    getProperties().set(DIRECTORY, parentDirectory == null ? "-1" : parentDirectory
        .getID());
  }

  /**
   * @param type
   *          The iType to set.
   */
  public void setType(final int type) {
    iType = type;
    getProperties().set(TYPE, type);
  }

  /**
   * Return whether this item should be hidden with hide option
   *
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getDirectory().getDevice().isMounted()
        || (ConfigurationManager.getBoolean(OPTIONS_HIDE_UNMOUNTED) == false)) {
      // option "only display mounted devices"
      return false;
    }
    return true;
  }

  /**
   * Stores all the files and the playlist in external device
   */
  public void storePlaylist() throws Exception {
    final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(DirectoryFilter
        .getInstance()));
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    jfc.setDialogTitle(Messages.getString("FirstTimeWizard.5"));
    jfc.setMultiSelectionEnabled(false);
    final int returnVal = jfc.showDialog(Main.getWindow(), "Ok");
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      new Thread() {
        @Override
        public void run() {
          Util.waiting();
          final java.io.File fDir = jfc.getSelectedFile();
          final Date curDate = new Date();
          final SimpleDateFormat Stamp = new SimpleDateFormat("ddMMyyyy-HH:mm");
          final String dirName = "Party-" + Stamp.format(curDate);
          final java.io.File destDir = new java.io.File(fDir.getAbsolutePath() + "/" + dirName);
          destDir.mkdir();
          final java.io.File file = new java.io.File(destDir.getAbsolutePath() + "/playlist.m3u");
          try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(Resources.PLAYLIST_NOTE);
            for (final File plf : alFiles) {
              Util.copyToDir(plf.getIO(), destDir);
              bw.newLine();
              bw.write(plf.getAbsolutePath());
            }
            bw.flush();
            bw.close();
          } catch (final Exception e) {
            Log.error(e);
          }
          Util.stopWaiting();
          Messages.showInfoMessage(dirName + " "
              + Messages.getString("AbstractPlaylistEditorView.27") + " " + fDir.getAbsolutePath());
        }
      }.start();
    }
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Playlist file[ID=" + getID() + " Name={{" + getName() + "}} Hashcode="
        + getStringValue(HASHCODE) + " Dir=" + dParentDirectory.getID() + "]";
  }

  /**
   * Up a track in the playlist
   *
   * @param index
   */
  public synchronized void up(final int index) {
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK) {
      Bookmarks.getInstance().up(index);
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
      FIFO.getInstance().up(index);
    } else if ((alFiles != null) && (index > 0)) { // the first track
      // cannot go further
      final File bfile = alFiles.get(index - 1); // save n-1 file
      alFiles.set(index - 1, alFiles.get(index));
      alFiles.set(index, bfile); // n-1 file becomes nth file
      setModified(true);
    }
  }
}
