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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.tags.Tag;
import org.jajuk.ui.helpers.ManualDirectoryRefreshReporter;
import org.jajuk.ui.helpers.RefreshReporter;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * A physical directory
 * <p>
 * Physical item
 */
public class Directory extends PhysicalItem implements Comparable<Directory> {

  /** Parent directory ID* */
  private Directory dParent;

  /** Directory device */
  private Device device;

  /** IO file for optimizations* */
  private java.io.File fio;

  /**
   * Directory constructor
   * 
   * @param id
   * @param sName
   * @param style
   * @param author
   */
  public Directory(String sId, String sName, Directory dParent, Device device) {
    super(sId, sName);
    this.dParent = dParent;
    setProperty(Const.XML_DIRECTORY_PARENT, (dParent == null ? "-1" : dParent.getID()));
    this.device = device;
    setProperty(Const.XML_DEVICE, device.getID());
    this.fio = new File(device.getUrl() + getRelativePath());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public final String getLabel() {
    return XML_DIRECTORY;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Directory[ID=" + getID() + " Name={{" + getRelativePath() + "}} ParentID="
        + (dParent == null ? "null" : dParent.getID()) + " Device={{" + device.getName() + "}}]";
  }

  public String getAbsolutePath() {
    StringBuilder sbOut = new StringBuilder(getDevice().getUrl()).append(getRelativePath());
    return sbOut.toString();
  }

  /**
   * @return
   */
  public Device getDevice() {
    return device;
  }

  /**
   * @return
   */
  public Directory getParentDirectory() {
    return dParent;
  }

  /**
   * @return
   */
  public Set<Directory> getDirectories() {
    Set<Directory> out = new LinkedHashSet<Directory>(2);
    ReadOnlyIterator<Directory> it = DirectoryManager.getInstance().getDirectoriesIterator();
    while (it.hasNext()) {
      Directory directory = it.next();
      if (directory.getFio().getParent().equals(this.getFio())) {
        out.add(directory);
      }
    }
    return out;
  }

  /**
   * return child files
   * 
   * @return child files
   */
  public Set<org.jajuk.base.File> getFiles() {
    Set<org.jajuk.base.File> out = new LinkedHashSet<org.jajuk.base.File>(2);
    ReadOnlyIterator<org.jajuk.base.File> it = FileManager.getInstance().getFilesIterator();
    while (it.hasNext()) {
      org.jajuk.base.File file = it.next();
      if (file.getFIO().getParentFile().equals(this.getFio())) {
        out.add(file);
      }
    }
    return out;
  }

  /**
   * return playlists
   * 
   * @return playlists
   */
  public Set<Playlist> getPlaylistFiles() {
    Set<Playlist> out = new LinkedHashSet<Playlist>(2);
    ReadOnlyIterator<Playlist> it = PlaylistManager.getInstance().getPlaylistsIterator();
    while (it.hasNext()) {
      Playlist plf = it.next();
      if (plf.getFio().getParent().equals(this.getFio())) {
        out.add(plf);
      }
    }
    return out;
  }

  /**
   * return child files from a given file in album included
   * 
   * @return child files
   */
  public List<org.jajuk.base.File> getFilesFromFile(org.jajuk.base.File fileStart) {
    Set<org.jajuk.base.File> files = getFiles();
    List<org.jajuk.base.File> alOut = new ArrayList<org.jajuk.base.File>(files.size());
    boolean bOK = false;
    for (org.jajuk.base.File file : files) {
      if (bOK || file.equals(fileStart)) {
        alOut.add(file);
        bOK = true;
      }
    }
    return alOut;
  }

  /**
   * return ordered child files recursively
   * 
   * @return child files recursively
   */
  public List<org.jajuk.base.File> getFilesRecursively() {
    List<org.jajuk.base.File> alFiles = new ArrayList<org.jajuk.base.File>(100);
    for (Item item : FileManager.getInstance().getFiles()) {
      org.jajuk.base.File file = (org.jajuk.base.File) item;
      if (file.hasAncestor(this)) {
        alFiles.add(file);
      }
    }
    return alFiles;
  }

  /**
   * Scan all files in a directory
   * 
   * @param bDeepScan:
   *          force files tag read
   * @param reporter
   *          Refresh handler
   */
  public void scan(boolean bDeepScan, RefreshReporter reporter) {
    java.io.File[] filelist = getFio().listFiles(UtilSystem.getFileFilter());
    if (filelist == null || filelist.length == 0) { // none file, leave
      return;
    }

    for (int i = 0; i < filelist.length; i++) {
      // Leave ASAP if exit request
      if (ExitService.isExiting()) {
        return;
      }
      try { // check errors for each file
        // Check file name is correct (useful to fix name encoding
        // issues)
        if (!new File(filelist[i].getAbsolutePath()).exists()) {
          Log.warn("Cannot read file name (please rename it): {{" + filelist[i].getAbsolutePath()
              + "}}");
          continue;
        }

        // Ignore iTunes files
        if (filelist[i].getName().startsWith("._")) {
          continue;
        }

        // check if we recognize the file as music file
        String extension = UtilSystem.getExtension(filelist[i]);
        Type type = TypeManager.getInstance().getTypeByExtension(extension);
        boolean bIsMusic = (Boolean) type.getValue(Const.XML_TYPE_IS_MUSIC);

        if (bIsMusic) {
          scanMusic(filelist[i], bDeepScan, reporter);
        } else { // playlist
          scanPlaylist(filelist[i], bDeepScan, reporter);
        }
      } catch (Exception e) {
        Log.error(103, filelist.length > 0 ? "{{" + filelist[i].toString() + "}}" : "", e);
      }
    }
  }

  /**
   * @param filelist
   * @param bDeepScan
   * @param reporter
   * @param value
   * @throws JajukException
   */
  private void scanMusic(java.io.File music, boolean bDeepScan, RefreshReporter reporter)
      throws JajukException {
    String name = music.getName();
    String sId = FileManager.createID(name, this);
    // check the file is not already known in database
    org.jajuk.base.File fileRef = FileManager.getInstance().getFileByID(sId);
    // Set name again to make sure Windows users will see actual
    // name with right case
    if (UtilSystem.isUnderWindows() && fileRef != null) {
      fileRef.setName(name);
    }

    // if known file and no deep scan, just leave
    if (fileRef != null && !bDeepScan) {
      return;
    }

    // ignore tag error to make sure to get a
    // tag object in all cases
    Tag tag = new Tag(music, true);
    if (tag.isCorrupted()) {
      if (reporter != null) {
        reporter.notifyCorruptedFile();
      }
      Log.error(103, "{{" + music.getAbsolutePath() + "}}", null);
    }

    // if an error occurs, just notice it but keep the track
    String sTrackName = tag.getTrackName();
    String sAlbumName = tag.getAlbumName();
    String sAuthorName = tag.getAuthorName();
    String sStyle = tag.getStyleName();
    long length = tag.getLength(); // length in sec
    String sYear = tag.getYear();
    long lQuality = tag.getQuality();
    String sComment = tag.getComment();
    long lOrder = tag.getOrder();

    if (fileRef == null && reporter != null) {
      // stats, do it here and not
      // before because we ignore the
      // file if we cannot read it
      reporter.notifyNewFile();
    }

    registerFile(music, sId, sTrackName, sAlbumName, sAuthorName, sStyle, length, sYear, lQuality,
        sComment, lOrder);
  }

  /**
   * @param music
   * @param sId
   * @param sTrackName
   * @param sAlbumName
   * @param sAuthorName
   * @param sStyle
   * @param length
   * @param sYear
   * @param lQuality
   * @param sComment
   * @param lOrder
   */
  private void registerFile(java.io.File music, String sId, String sTrackName, String sAlbumName,
      String sAuthorName, String sStyle, long length, String sYear, long lQuality, String sComment,
      long lOrder) {
    Album album = AlbumManager.getInstance().registerAlbum(sAlbumName);
    Style style = StyleManager.getInstance().registerStyle(sStyle);
    Year year = YearManager.getInstance().registerYear(sYear);
    Author author = AuthorManager.getInstance().registerAuthor(sAuthorName);
    Type type = TypeManager.getInstance().getTypeByExtension(UtilSystem.getExtension(music));
    // Store number of tracks in collection
    long trackNumber = TrackManager.getInstance().getElementCount();
    Track track = TrackManager.getInstance().registerTrack(sTrackName, album, style, author,
        length, year, lOrder, type);

    // Note date for file date property. CAUTION: do not try to
    // check current date to accelerate refreshing if file has not
    // been modified since last refresh as user can rename a parent
    // directory and the files times under it are not modified
    long lastModified = music.lastModified();

    // Use file date if the "force file date" option is used
    if (Conf.getBoolean(Const.CONF_FORCE_FILE_DATE)) {
      track.setDiscoveryDate(new Date(lastModified));
    } else if (TrackManager.getInstance().getElementCount() > trackNumber) {
      // Update discovery date only if it is a new track

      // A new track has been created, we can safely update
      // the track date
      // We don't want to update date if the track is already
      // known, even if
      // it is a new file because a track can map several
      // files and discovery date
      // is a track attribute, not file one
      track.setDiscoveryDate(new Date());
    }

    org.jajuk.base.File file = FileManager.getInstance().registerFile(sId, music.getName(), this,
        track, music.length(), lQuality);
    // Set file date
    file.setProperty(Const.XML_FILE_DATE, new Date(lastModified));
    // Comment is at the track level, note that we take last
    // found file comment but we changing a comment, we will
    // apply to all files for a track
    track.setComment(sComment);
    // Make sure to refresh file size
    file.setProperty(Const.XML_SIZE, music.length());
  }

  private void scanPlaylist(final java.io.File file, final boolean bDeepScan,
      final RefreshReporter reporter) throws Exception {
    String sId = PlaylistManager.createID(file.getName(), this);
    Playlist plfRef = PlaylistManager.getInstance().getPlaylistByID(sId);
    // if known playlist and no deep scan, just leave
    if (plfRef != null && !bDeepScan) {
      return;
    }
    Playlist plFile = PlaylistManager.getInstance().registerPlaylistFile(file, this);
    plFile.forceRefresh(); // force refresh
    if (plfRef == null && reporter != null) {
      // stats, do it here and not
      // before because we ignore the
      // file if we cannot read it
      reporter.notifyNewFile();
    }
  }

  /** Reset pre-calculated paths* */
  protected void reset() {
    fio = null;
  }

  /**
   * Return full directory path name relative to device url
   * 
   * @return String
   */
  public final String getRelativePath() {
    if (getName().equals("")) {
      // if this directory is a root device
      // directory
      return "";
    }
    StringBuilder sbOut = new StringBuilder().append(java.io.File.separatorChar).append(getName());
    boolean bTop = false;
    Directory dCurrent = this;
    while (!bTop && dCurrent != null) {
      dCurrent = dCurrent.getParentDirectory();
      if (dCurrent != null && !dCurrent.getName().equals("")) {
        // if it is the root directory, no parent
        sbOut.insert(0, java.io.File.separatorChar).insert(1, dCurrent.getName());
      } else {
        bTop = true;
      }
    }
    return sbOut.toString();
  }

  /**
   * @return Returns the IO file reference to this directory.
   */
  public File getFio() {
    if (fio == null) {
      fio = new java.io.File(getAbsolutePath());
    }
    return fio;
  }

  /**
   * Alphabetical comparator used to display ordered lists of directories
   * <p>
   * Sort ignoring cases
   * </p>
   * 
   * @param other
   *          directory to be compared
   * @return comparison result
   */
  public int compareTo(Directory otherDirectory) {
    // Perf: leave if directories are equals
    if (otherDirectory.equals(this)) {
      return 0;
    }
    String abs = new StringBuilder(getDevice().getName()).append(getAbsolutePath()).toString();
    String otherAbs = new StringBuilder(otherDirectory.getDevice().getName()).append(
        otherDirectory.getAbsolutePath()).toString();
    // should ignore case to get a B c ... and not Bac
    // We must be consistent with equals, see
    // http://java.sun.com/javase/6/docs/api/java/lang/Comparable.html
    int comp = abs.compareToIgnoreCase(otherAbs);
    if (comp == 0) {
      return abs.compareTo(otherAbs);
    } else {
      return comp;
    }
  }

  /**
   * Return whether this item should be hidden with hide option
   * 
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getDevice().isMounted() || !Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)) {
      return false;
    }
    return true;
  }

  /**
   * Get item description
   */
  @Override
  public String getDesc() {
    String sName = null;
    if (getParentDirectory() == null) {
      sName = getDevice().getUrl();
    } else {
      sName = getFio().getAbsolutePath();
    }
    return Messages.getString("Item_Directory") + " : " + sName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(String sKey) {
    if (Const.XML_DIRECTORY_PARENT.equals(sKey)) {
      Directory parentdir = DirectoryManager.getInstance()
          .getDirectoryByID((String) getValue(sKey));
      if (parentdir == null) {
        return ""; // no parent directory
      } else {
        return parentdir.getFio().getAbsolutePath();
      }
    } else if (Const.XML_DEVICE.equals(sKey)) {
      return (DeviceManager.getInstance().getDeviceByID((String) getValue(sKey))).getName();
    }
    if (Const.XML_NAME.equals(sKey)) {
      if (dParent == null) { // if no parent, take device name
        return getDevice().getUrl();
      } else {
        return getName();
      }
    } else {
      // default
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
    ImageIcon icon = null;
    // is this device synchronized?
    if (getBooleanValue(Const.XML_DIRECTORY_SYNCHRONIZED)) {
      icon = IconLoader.getIcon(JajukIcons.DIRECTORY_SYNCHRO);
    } else {
      icon = IconLoader.getIcon(JajukIcons.DIRECTORY_DESYNCHRO);
    }
    return icon;
  }

  /**
   * Set name (useful for Windows because same object can have different cases)
   * 
   * @param name
   *          Item name
   */
  protected void setName(String name) {
    setProperty(Const.XML_NAME, name);
    this.name = name;
  }

  /**
   * Refresh synchronously the directory recursively
   * 
   * @param deep
   *          refresh ?
   * @param a
   *          refresh reporter or null
   */
  public void refresh(boolean deep, RefreshReporter reporter) throws JajukException {
    scan(deep, reporter);
    final java.io.File[] lFiles = getFio().listFiles(UtilSystem.getDirFilter());
    if (lFiles != null) {
      for (final java.io.File element : lFiles) {
        final Directory subdir = DirectoryManager.getInstance().registerDirectory(
            element.getName(), this, getDevice());
        subdir.refresh(deep, reporter);
      }
    }
  }

  /**
   * Refresh : scan asynchronously the directory to find tracks
   * 
   * @param bAsynchronous :
   *          set asynchronous or synchronous mode
   * @param bAsk:
   *          should we ask user if he wants to perform a deep or fast scan?
   *          default=deep
   */
  public void manualRefresh(final boolean bAsynchronous, final boolean bAsk) {
    final RefreshReporter reporter = new ManualDirectoryRefreshReporter(getDevice());
    final Thread t = new Thread() {
      @Override
      public void run() {
        try {
          int result = getDevice().prepareRefresh(bAsk);
          if (result == Device.OPTION_REFRESH_CANCEL) {
            return;
          }
          InformationJPanel.getInstance().setMessage(
              Messages.getString("ActionRefresh.1") + " : " + Directory.this.getName(), 1);
          boolean deep = (result == Device.OPTION_REFRESH_DEEP);
          // start counting
          reporter.startup();
          // Cleanup old files/directories/playlists
          cleanRemovedFiles();
          // Actual refresh
          refresh(deep, reporter);
          // cleanup logical items
          org.jajuk.base.Collection.cleanupLogical();
          ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
          reporter.done();
          // commit collection at each refresh (can be useful if application
          // is closed brutally with control-C or shutdown and that exit hook
          // have no time to perform commit)
          try {
            org.jajuk.base.Collection.commit(UtilSystem.getConfFileByPath(Const.FILE_COLLECTION));
          } catch (final IOException e) {
            Log.error(e);
          }
        } catch (JajukException e) {
          Messages.showErrorMessage(e.getCode());
          Log.debug(e);
          return;
        }
      }
    };
    if (bAsynchronous) {
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    } else {
      // simply call the run method
      t.run();
    }
  }

  /**
   * Scan directory to cleanup removed files and playlists
   * 
   * @return whether some items have been removed
   */
  public boolean cleanRemovedFiles() {
    boolean bChanges = false;
    // need to use a shallow copy to avoid concurrent exceptions
    final List<Directory> dirs = DirectoryManager.getInstance().getDirectories();
    // directories cleanup
    for (final Item item : dirs) {
      final Directory dir = (Directory) item;
      if (!ExitService.isExiting() && dir.getDevice().isMounted() && dir.isChildOf(this)) {
        if (!dir.getFio().exists()) {
          // note that associated files are removed too
          DirectoryManager.getInstance().removeDirectory(dir.getID());
          Log.debug("Removed: " + dir);
          bChanges = true;
        }
      }
    }
    // files cleanup
    final List<org.jajuk.base.File> lFiles = FileManager.getInstance().getFiles();
    for (final org.jajuk.base.File file : lFiles) {
      if (!ExitService.isExiting()
          // Only take into consideration files from this directory or from
          // sub-directories
          && (file.getDirectory().equals(this) || file.getDirectory().isChildOf(this))
          && file.isReady()) {
        // Remove file if it doesn't exist any more or if it is a iTunes
        // file (useful for jajuk < 1.4)
        if (!file.getFIO().exists() || file.getName().startsWith("._")) {
          FileManager.getInstance().removeFile(file);
          Log.debug("Removed: " + file);
          bChanges = true;
        }
      }
    }
    // Playlist cleanup
    final List<Playlist> plfiles = PlaylistManager.getInstance().getPlaylists();
    for (final Playlist plf : plfiles) {
      if (!ExitService.isExiting()
          // Only take into consideration files from this directory or from
          // sub-directories
          && (plf.getDirectory().equals(this) || plf.getDirectory().isChildOf(this))
          && plf.isReady()) {
        if (!plf.getFio().exists()) {
          PlaylistManager.getInstance().removePlaylistFile(plf);
          Log.debug("Removed: " + plf);
          bChanges = true;
        }
      }
    }
    // clear history to remove old files referenced in it
    if (Conf.getString(Const.CONF_HISTORY) != null) {
      History.getInstance().clear(Integer.parseInt(Conf.getString(Const.CONF_HISTORY)));
    }
    return bChanges;
  }

  /**
   * Return true is this is a child directory of the specified directory
   * 
   * @param directory
   *          ancestor directory
   * @return
   */
  public boolean isChildOf(Directory directory) {
    Directory dirTested = getParentDirectory();
    if (dirTested == null) {
      return false;
    }
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

}