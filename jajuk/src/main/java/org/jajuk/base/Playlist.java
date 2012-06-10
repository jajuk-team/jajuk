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
 *  
 */
package org.jajuk.base;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jajuk.services.bookmark.Bookmarks;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.PlaylistFilter;
import org.jajuk.util.log.Log;

/**
 * A playlist
 * <p>
 * Physical item
 * 
 * 
 * TODO: refactoring items for this class:
 * - split up the code into separate implementations for the different types
 * - create a base abstract playlist class that is used in the various places and
 * have separate implementations for each of the types to separate the code better.
 */
public class Playlist extends PhysicalItem implements Comparable<Playlist> {

  /**
   * playlist type.
   */
  public enum Type {

    NORMAL,

    QUEUE,

    NEW,

    BOOKMARK,

    BESTOF,

    NOVELTIES
  }

  /** Playlist parent directory. */
  private Directory dParentDirectory;

  /** Files list, singleton. */
  private List<File> alFiles;

  /** Associated physical file. */
  private java.io.File fio;

  /** Playlist type. */
  private final Type type;

  /** pre-calculated absolute path for perf. */
  private String sAbs = null;

  /** Contains files outside device flag. */
  private boolean bContainsExtFiles = false;

  /** Whether we ask for device mounting if required. */
  private boolean askForMounting = true;

  /**
   * playlist constructor.
   * 
   * @param type playlist type
   * @param sId 
   * @param sName 
   * @param dParentDirectory 
   */
  Playlist(final Type type, final String sId, final String sName, final Directory dParentDirectory) {
    super(sId, sName);
    this.dParentDirectory = dParentDirectory;
    setProperty(Const.XML_DIRECTORY, dParentDirectory == null ? "-1" : dParentDirectory.getID());
    this.type = type;
  }

  /**
   * playlist constructor.
   * 
   * @param sId 
   * @param sName 
   * @param dParentDirectory 
   */
  public Playlist(final String sId, final String sName, final Directory dParentDirectory) {
    this(Playlist.Type.NORMAL, sId, sName, dParentDirectory);
  }

  /**
   * Gets the type.
   * 
   * @return Returns the Type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Add a file at the end of this playlist.
   * 
   * @param file 
   * 
   * @throws JajukException the jajuk exception
   */
  public void addFile(final File file) throws JajukException {
    final List<File> al = getFiles();
    final int index = al.size();
    addFile(index, file);
  }

  /**
   * Add a file to this playlist. at a given index.
   * 
   * @param index 
   * @param file 
   * 
   * @throws JajukException the jajuk exception
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
        final StackItem itemPrevious = QueueModel.getItem(index - 1);
        if ((itemPrevious != null) && itemPrevious.isRepeat()) {
          item.setRepeat(true);
        } else {
          item.setRepeat(false);
        }
        // insert this track in the fifo
        QueueModel.insert(item, index);
      } else {
        // start immediately playing
        QueueModel.push(item, false);
      }

      // we don't need to adjust the alFiles here because for playlist type QUEUE
      // the contents is taken directly from the QueueModel in case of
    } else {
      getFiles().add(index, file);
    }
  }

  /**
   * Add some files to this playlist.
   * 
   * @param alFilesToAdd :   List of Files
   * @param position 
   */
  public void addFiles(final List<File> alFilesToAdd, int position) {
    try {
      int offset = 0;
      for (File file : alFilesToAdd) {
        addFile(position + offset, file);
        offset++;
      }
    } catch (final Exception e) {
      Log.error(e);
    }
  }

  /**
   * Clear playlist.
   */
  public void clear() {
    if (type == Type.BOOKMARK) { // bookmark
      // playlist
      Bookmarks.getInstance().clear();
    } else if (getType() == Type.QUEUE) {
      QueueModel.clear();
    } else {
      if (alFiles == null) {
        return;
      }

      alFiles.clear();
    }
  }

  /**
   * Update playlist on disk if needed.
   * 
   * @throws JajukException the jajuk exception
   */
  public void commit() throws JajukException {
    java.io.File temp = null;
    try {
      /*
       * Due to bug #1046, we use a temporary file In some special cases (reproduced under Linux,
       * JRE SUN 1.6.0_04, CIFS mount, 777 rights file), probably due to a JRE bug, files cannot be
       * opened (FileNotFound? Exception, permission denied) and the file is voided (0 bytes) and is
       * closed (checked with lsof).
       */
      temp = new java.io.File(getAbsolutePath() + '~');
      BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
      try {
        bw.write(Const.PLAYLIST_NOTE);
        bw.newLine();
        final Iterator<File> it = getFiles().iterator();
        while (it.hasNext()) {
          final File file = it.next();
          if (file.getFIO().getParent().equals(getFIO().getParent())) {
            bw.write(file.getName());
          } else {
            bw.write(file.getAbsolutePath());
          }
          bw.newLine();
        }
        bw.flush();
      } finally {
        bw.close();
      }
    } catch (final IOException e) {
      throw new JajukException(28, getName(), e);
    }

    // Now move the temp file to final one if everything seems ok
    moveTempPlaylistFile(temp);
  }

  /**
   * Move temp playlist file.
   * 
   * @param temp 
   * 
   * @throws JajukException the jajuk exception
   */
  private void moveTempPlaylistFile(java.io.File temp) throws JajukException {

    if (temp.exists() && temp.length() > 0) {
      try {
        UtilSystem.copy(temp, getFIO());
        UtilSystem.deleteFile(temp);
      } catch (final Exception e1) {
        throw new JajukException(28, getName(), e1);
      }
    } else {
      try {
        // Try to remove the temp file
        UtilSystem.deleteFile(temp);
      } catch (final Exception e1) {
        Log.error(e1);
      }
      throw new JajukException(28, getName());
    }
  }

  /**
   * Alphabetical comparator used to display ordered lists of playlists
   * <p>
   * Sort ignoring cases
   * </p>.
   * 
   * @param o 
   * 
   * @return comparison result
   */
  @Override
  public int compareTo(final Playlist o) {
    // not equal if other is null
    if (o == null) {
      return -1;
    }

    // Perf: leave if items are equals
    if (o.equals(this)) {
      return 0;
    }

    final Playlist otherPlaylistFile = o;
    final String abs = getName() + (getDirectory() != null ? getAbsolutePath() : "");
    final String sOtherAbs = otherPlaylistFile.getName()
        + (otherPlaylistFile.getDirectory() != null ? otherPlaylistFile.getAbsolutePath() : "");
    // We must be consistent with equals, see
    // http://java.sun.com/javase/6/docs/api/java/lang/Comparable.html
    int comp = abs.compareToIgnoreCase(sOtherAbs);
    if (comp == 0) {
      return abs.compareTo(sOtherAbs);
    } else {
      return comp;
    }
  }

  /**
   * Contains ext files.
   * 
   * @return whether this playlist contains files located out of known devices
   */
  public boolean containsExtFiles() {
    return bContainsExtFiles;
  }

  /**
   * Down a track in the playlist.
   * 
   * @param index 
   */
  public void down(final int index) {
    if (type == Type.BOOKMARK) {
      Bookmarks.getInstance().down(index);
    } else if (type == Type.QUEUE) {
      QueueModel.down(index);
    } else if ((alFiles != null) && (index < alFiles.size() - 1)) {
      // the last track cannot go deeper

      // n+1 file becomes nth file
      Collections.swap(alFiles, index, index + 1);
    }
  }

  /**
   * Equal method to check two playlists are identical.
   * 
   * @param otherPlaylistFile 
   * 
   * @return true, if equals
   */
  @Override
  public boolean equals(final Object otherPlaylistFile) {
    // also catches null by definition
    if (!(otherPlaylistFile instanceof Playlist)) {
      return false;
    }

    final Playlist plfOther = (Playlist) otherPlaylistFile;
    return getID().equals(plfOther.getID()) && plfOther.getType() == type;
  }

  /**
   * Return absolute file path name.
   * 
   * @return String
   */
  public String getAbsolutePath() {
    if (type == Type.NORMAL) {
      if (sAbs != null) {
        return sAbs;
      }
      final Directory dCurrent = getDirectory();
      final StringBuilder sbOut = new StringBuilder(dCurrent.getDevice().getUrl())
          .append(dCurrent.getRelativePath()).append(java.io.File.separatorChar).append(getName());
      sAbs = sbOut.toString();
    } else {
      // smart playlist path depends on the user selected from the save as
      // file chooser and has been set using the setFio() method just before
      // that don't use "getFIO()" here, as otherwise we can cause an endless
      // loop as getFIO() calls this method as well
      if (fio == null) {
        return "";
      }

      sAbs = fio.getAbsolutePath();
    }
    return sAbs;
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.Item#getTitle()
   */
  @Override
  public String getTitle() {
    return Messages.getString("Item_Playlist_File") + " : " + getName();
  }

  /**
   * Gets the directory.
   * 
   * @return the directory
   */
  public Directory getDirectory() {
    return dParentDirectory;
  }

  /**
   * Gets the files.
   * 
   * @return Returns the list of files this playlist maps to
   * 
   * @throws JajukException if the playlist cannot be mounted or cannot be read
   */
  public List<File> getFiles() throws JajukException {
    // if normal playlist, propose to mount device if unmounted
    if ((getType() == Type.NORMAL) && !isReady()) {
      // We already asked but user didn't want to mount the device -> leave
      if (!askForMounting) {
        throw new JajukException(141, getFIO().getAbsolutePath());
      }
      // No more ask for mounting
      askForMounting = false;
      final String sMessage = Messages.getString("Error.025") + " ("
          + getDirectory().getDevice().getName() + Messages.getString("FIFO.4");
      final int i = Messages.getChoice(sMessage, JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.INFORMATION_MESSAGE);
      if (i == JOptionPane.YES_OPTION) {
        try {
          // mount the device is required
          getDirectory().getDevice().mount(true);
        } catch (final Exception e) {
          throw new JajukException(141, getFIO().getAbsolutePath(), e);
        }
      } else {
        throw new JajukException(141, getFIO().getAbsolutePath());
      }
    }
    if ((type == Type.NORMAL) && (alFiles == null)) {
      // normal playlist, test if list is null for performances (avoid
      // reading the m3u file twice)
      if (getFIO().exists() && getFIO().canRead()) {
        // check device is mounted
        load(); // populate playlist
      } else { // error accessing playlist
        throw new JajukException(9, getFIO().getAbsolutePath());
      }
    } else if (type.equals(Type.BESTOF)) {
      alFiles = FileManager.getInstance().getBestOfFiles();
    } else if (type.equals(Type.NOVELTIES)) {
      alFiles = FileManager.getInstance().getGlobalNoveltiesPlaylist(
          Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED));
    } else if (type.equals(Type.BOOKMARK)) {
      alFiles = Bookmarks.getInstance().getFiles();
    } else if (type.equals(Type.QUEUE)) {
      List<StackItem> items = QueueModel.getQueue();
      List<File> files = new ArrayList<File>(items.size());
      for (StackItem si : items) {
        files.add(si.getFile());
      }
      alFiles = files;
    } else if (type.equals(Type.NEW) && alFiles == null) {
      alFiles = new ArrayList<File>(10);
    }
    return alFiles;
  }

  /**
   * Gets the fio.
   * 
   * @return Returns the fio.
   */
  public java.io.File getFIO() {
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
    if (Const.XML_DIRECTORY.equals(sKey)) {
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
    return IconLoader.getIcon(JajukIcons.PLAYLIST_FILE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public final String getXMLTag() {
    return Const.XML_PLAYLIST_FILE;
  }

  /**
   * Return true the file can be accessed right now.
   * 
   * @return true the file can be accessed right now
   */
  public boolean isReady() {
    if (getDirectory() != null && getDirectory().getDevice() != null
        && getDirectory().getDevice().isMounted()) {
      return true;
    }
    return false;
  }

  /**
   * Parse a playlist.
   * 
   * @throws JajukException the jajuk exception
   */
  public void load() throws JajukException {
    final List<File> files = new ArrayList<File>(10);
    try {
      BufferedReader br = new BufferedReader(new FileReader(getFIO()));
      try {
        String sLine = null;
        boolean bUnknownDevicesMessage = false;
        while ((sLine = br.readLine()) != null) {
          if (sLine.length() == 0) { // void line
            continue;
          }
          // replace '\' by '/'
          sLine = sLine.replace('\\', '/');
          // deal with url beginning by "./something"
          if (sLine.startsWith("./")) {
            sLine = sLine.substring(1, sLine.length());
          }
          // comment
          if (sLine.charAt(0) == '#') {
            continue;
          } else {
            java.io.File fio = null;
            final StringBuilder sbFileDir = new StringBuilder(getDirectory().getAbsolutePath());
            // Add a trailing / at the end of the url if required
            if (sLine.charAt(0) != '/') {
              sbFileDir.append("/");
            }
            // take a look relatively to playlist directory to check if the file exists
            fio = new java.io.File(sbFileDir.append(sLine).toString());
            String fioAbsPath = fio.getAbsolutePath();
            // Check for file existence in jajuk collection using Guava Files.simplyPath
            // Don't use File.getAbsolutePath() because its result can contain ./ or ../
            // Don't use File.getCanonicalPath() because it resolves symlinks under unix.
            File jajukFile = FileManager.getInstance()
                .getFileByPath(Files.simplifyPath(fioAbsPath));
            if (jajukFile == null) { // check if this file is known in collection
              fio = new java.io.File(sLine); // check if given url is not absolute
              jajukFile = FileManager.getInstance().getFileByPath(fio.getAbsolutePath());
              if (jajukFile == null) { // no more ? leave
                bUnknownDevicesMessage = true;
                continue;
              }
            }
            files.add(jajukFile);
          }
        }
        // display a warning message if the playlist contains unknown
        // items
        if (bUnknownDevicesMessage) {
          bContainsExtFiles = true;
        }
      } finally {
        br.close();
      }
    } catch (final IOException e) {
      Log.error(17, "{{" + getName() + "}}", e);
      throw new JajukException(17, (getDirectory() != null && getFIO() != null ? getFIO()
          .getAbsolutePath() : "<unknown>"), e);
    }
    this.alFiles = files;
  }

  /**
   * Play a playlist.
   * 
   * @throws JajukException the jajuk exception
   */
  public void play() throws JajukException {
    alFiles = getFiles();
    if ((alFiles == null) || (alFiles.size() == 0)) {
      Messages.showErrorMessage(18);
    } else {
      QueueModel.push(
          UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(alFiles),
              Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), true), false);
    }
  }

  /**
   * Remove a track from the playlist.
   * We expect at this point that the playlist has already been loaded once at least.
   * 
   * @param index 
   */
  public void remove(final int index) {
    if (type == Type.BOOKMARK) {
      Bookmarks.getInstance().remove(index);
    } else if (type == Type.QUEUE) {
      QueueModel.remove(index, index);
    } else {
      alFiles.remove(index);
    }
  }

  /**
   * Replace a file inside a playlist.
   * 
   * @param fOld 
   * @param fNew 
   * 
   * @throws JajukException the jajuk exception
   */
  void replaceFile(final File fOld, final File fNew) throws JajukException {
    if (type == Type.BOOKMARK) {
      List<File> files = Bookmarks.getInstance().getFiles();
      final Iterator<File> it = files.iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = it.next();
        if (fileToTest.equals(fOld)) {
          files.set(i, fNew);
          /*
           * this leads to ConcurrentModificationException: Bookmarks.getInstance().remove(i);
           * Bookmarks.getInstance().addFile(i, fNew);
           */
        }
      }
    } else if (type == Type.QUEUE) {
      final Iterator<StackItem> it = QueueModel.getQueue().iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = it.next().getFile();
        if (fileToTest.equals(fOld)) {
          QueueModel.remove(i, i); // just remove
          final List<StackItem> al = new ArrayList<StackItem>(1);
          al.add(new StackItem(fNew));
          QueueModel.insert(al, i);
        }
      }
    } else {
      final Iterator<File> it = alFiles.iterator();
      for (int i = 0; it.hasNext(); i++) {
        final File fileToTest = it.next();
        if (fileToTest.equals(fOld)) {
          alFiles.set(i, fNew);
          try {
            commit();// save changed playlist
          } catch (final JajukException e) {
            Log.error(e);
          }
        }
      }
    }
  }

  /**
   * Reset pre-calculated paths*.
   */
  protected void reset() {
    sAbs = null;
    fio = null;
  }

  /**
   * Save as... the playlist
   *
   * @throws JajukException the jajuk exception
   * @throws InterruptedException the interrupted exception
   * @throws InvocationTargetException the invocation target exception
   */
  public void saveAs() throws JajukException, InterruptedException, InvocationTargetException {
    FileChooserRunnable runnable = new FileChooserRunnable();

    SwingUtilities.invokeLater(runnable);

    if (runnable.getException() != null) {
      throw runnable.getException();
    }
  }

  /**
   * Sets the files.
   * 
   * @param alFiles The alFiles to set.
   */
  public void setFiles(final List<File> alFiles) {
    this.alFiles = alFiles;
  }

  /**
   * Sets the fio.
   * 
   * @param fio The fio to set.
   */
  public void setFIO(final java.io.File fio) {
    this.fio = fio;
  }

  /**
   * Return whether this item should be hidden with hide option.
   * 
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getDirectory().getDevice().isMounted()
        || (!Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED))) {
      // option "only display mounted devices"
      return false;
    }
    return true;
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    if (dParentDirectory == null) {
      return "playlist[ID=" + getID() + " Name={{" + getName() + "}} " + " Dir=<null>]";
    } else {
      return "playlist[ID=" + getID() + " Name={{" + getName() + "}} " + " Dir="
          + dParentDirectory.getID() + "]";
    }
  }

  /**
   * Up a track in the playlist.
   * 
   * @param index 
   */
  public void up(final int index) {
    if (type == Type.BOOKMARK) {
      Bookmarks.getInstance().up(index);
    } else if (type == Playlist.Type.QUEUE) {
      QueueModel.up(index);
    } else if ((alFiles != null) && (index > 0)) { // the first track
      // cannot go further

      // n-1 file becomes nth file
      Collections.swap(alFiles, index, index - 1);
    }
  }

  /**
   * Gets the playlist average rating.
   * 
   * @return playlist average rating
   */
  @Override
  public long getRate() {
    if (alFiles == null) {
      return 0;
    }

    float rate = 0f;
    int nb = 0;
    for (File file : alFiles) {
      rate += file.getTrack().getRate();
      nb++;
    }
    return Math.round(rate / nb);
  }

  /**
   * Gets the hits.
   * 
   * @return total nb of hits
   */
  public long getHits() {
    if (alFiles == null) {
      return 0;
    }

    int hits = 0;
    for (File file : alFiles) {
      hits += file.getTrack().getHits();
    }
    return hits;
  }

  /**
   * Return full playlist length in secs.
   * 
   * @return the duration
   */
  public long getDuration() {
    if (alFiles == null) {
      return 0;
    }

    long length = 0;
    for (File file : alFiles) {
      length += file.getTrack().getDuration();
    }
    return length;
  }

  /**
   * Gets the nb of tracks.
   * 
   * @return playlist nb of tracks
   */
  public int getNbOfTracks() {
    if (alFiles == null) {
      return 0;
    }
    return alFiles.size();
  }

  /**
   * Return true is the specified directory is an ancestor for this playlist.
   * 
   * @param directory 
   * 
   * @return true, if checks for ancestor
   */
  public boolean hasAncestor(Directory directory) {
    Directory dirTested = getDirectory();
    while (true) {
      if (dirTested.equals(directory)) {
        return true;
      } else {
        dirTested = dirTested.getParentDirectory();
        if (dirTested == null) {
          return false;
        }
      }
    }
  }

  /**
   * Small helper class to be able to run
   * the FileChoose inside the EDT thread of Swing.
   */
  private final class FileChooserRunnable implements Runnable {
    // records if there are exceptions during doing the call

    JajukException ex = null;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      try {
        final JajukFileChooser jfchooser = new JajukFileChooser(new JajukFileFilter(
            PlaylistFilter.getInstance()));
        jfchooser.setDialogType(JFileChooser.SAVE_DIALOG);
        jfchooser.setAcceptDirectories(true);
        String sPlaylist = Const.DEFAULT_PLAYLIST_FILE;
        // computes new playlist
        alFiles = getFiles();
        if (alFiles.size() > 0) {
          final File file = alFiles.get(0);
          if (getType() == Type.BESTOF) {
            sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
                + Const.FILE_DEFAULT_BESTOF_PLAYLIST;
          } else if (getType() == Type.BOOKMARK) {
            sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
                + Const.FILE_DEFAULT_BOOKMARKS_PLAYLIST;
          } else if (getType() == Type.NOVELTIES) {
            sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
                + Const.FILE_DEFAULT_NOVELTIES_PLAYLIST;
          } else if (getType() == Type.QUEUE) {
            sPlaylist = file.getDevice().getUrl() + java.io.File.separatorChar
                + Const.FILE_DEFAULT_QUEUE_PLAYLIST
                + UtilString.getAdditionDateFormatter().format(new Date());
          } else {
            sPlaylist = file.getDirectory().getAbsolutePath() + java.io.File.separatorChar
                + file.getTrack().getHumanValue(Const.XML_ALBUM);
          }
        } else {
          return;
        }
        jfchooser.setSelectedFile(new java.io.File(sPlaylist + "." + Const.EXT_PLAYLIST));
        final int returnVal = jfchooser.showSaveDialog(JajukMainWindow.getInstance());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          java.io.File file = jfchooser.getSelectedFile();
          // add automatically the extension if required
          if (file.getAbsolutePath().endsWith(Const.EXT_PLAYLIST)) {
            file = new java.io.File(file.getAbsolutePath());
          } else {
            file = new java.io.File(file.getAbsolutePath() + "." + Const.EXT_PLAYLIST);
          }

          // set new file path ( this playlist is a special playlist, just in
          // memory )
          setFIO(file);
          commit(); // write it on the disk
        }
      } catch (JajukException e) {
        ex = e;
      }
    }

    /**
     * Returns any exception caught during running the file chooser.
     * 
     * @return null if no exception was caught, the actual exception otherwise.
     */
    public JajukException getException() {
      return ex;
    }
  }
}
