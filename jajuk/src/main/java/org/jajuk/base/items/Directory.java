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

import static org.jajuk.util.Resources.XML.DEVICE;
import static org.jajuk.util.Resources.XML.DIRECTORY;
import static org.jajuk.util.Resources.XML.DIRECTORY_PARENT;
import static org.jajuk.util.Resources.XML.DIRECTORY_SYNCHRONIZED;
import static org.jajuk.util.Resources.XML.FILE_DATE;
import static org.jajuk.util.Resources.XML.NAME;
import static org.jajuk.util.Resources.XML.TYPE_IS_MUSIC;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import org.jajuk.base.ItemType;
import org.jajuk.base.PhysicalItem;
import org.jajuk.base.Tag;
import org.jajuk.base.managers.AlbumManager;
import org.jajuk.base.managers.AuthorManager;
import org.jajuk.base.managers.DeviceManager;
import org.jajuk.base.managers.DirectoryManager;
import org.jajuk.base.managers.FileManager;
import org.jajuk.base.managers.PlaylistFileManager;
import org.jajuk.base.managers.PlaylistManager;
import org.jajuk.base.managers.StyleManager;
import org.jajuk.base.managers.TrackManager;
import org.jajuk.base.managers.TypeManager;
import org.jajuk.base.managers.YearManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.RefreshReporter;
import org.jajuk.util.Util;
import org.jajuk.util.Resources.ConfKeys;
import org.jajuk.util.log.Log;

/**
 * A physical directory
 * <p>
 * Physical item
 */
public class Directory extends PhysicalItem implements Comparable<Directory> {

  private static final long serialVersionUID = 1L;

  /** Parent directory ID* */
  private Directory dParent;

  /** Directory device */
  private Device device;

  /** Child directories */
  private TreeSet<Directory> directories = new TreeSet<Directory>();

  /** Child files */
  private TreeSet<org.jajuk.base.items.File> files = new TreeSet<org.jajuk.base.items.File>();

  /** Playlist files */
  private TreeSet<PlaylistFile> playlistFiles = new TreeSet<PlaylistFile>();

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
  public Directory(final String sId, final String sName, final Directory dParent, final Device device) {
    super(sId, sName);
    this.dParent = dParent;
    getProperties().set(DIRECTORY_PARENT, (dParent == null ? "-1" : dParent.getID()));
    this.device = device;
    getProperties().set(DEVICE, device.getID());
    fio = new File(device.getUrl() + getRelativePath());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return DIRECTORY;
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
    final StringBuilder sbOut = new StringBuilder(getDevice().getUrl()).append(getRelativePath());
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
    return directories;
  }

  /**
   * Add a child directory in local references
   *
   * @param directory
   */
  public void addDirectory(final Directory directory) {
    directories.add(directory);
  }

  /**
   * Remove a file from local references
   *
   * @param file
   */
  public void removeFile(final org.jajuk.base.items.File file) {
    if (files.contains(file)) {
      files.remove(file);
    }
  }

  /**
   * Add a playlist file in local refences
   *
   * @param playlist
   *          file
   */
  public void addPlaylistFile(final PlaylistFile plf) {
    playlistFiles.add(plf);
  }

  /**
   * Remove a playlist file from local refences
   *
   * @param playlist
   *          file
   */
  public void removePlaylistFile(final PlaylistFile plf) {
    if (playlistFiles.contains(plf)) {
      playlistFiles.remove(plf);
    }
  }

  /**
   * Remove a child directory from local refences
   *
   * @param directory
   */
  public void removeDirectory(final Directory directory) {
    directories.remove(directory);
  }

  /**
   * return child files
   *
   * @return child files
   */
  public Set<org.jajuk.base.items.File> getFiles() {
    return files;
  }

  /**
   * return playlist files
   *
   * @return playlist files
   */
  public Set<PlaylistFile> getPlaylistFiles() {
    return playlistFiles;
  }

  /**
   * return child files from a given file in album included
   *
   * @return child files
   */
  public ArrayList<org.jajuk.base.items.File> getFilesFromFile(final org.jajuk.base.items.File fileStart) {
    final Iterator it = files.iterator();
    final ArrayList<org.jajuk.base.items.File> alOut = new ArrayList<org.jajuk.base.items.File>(files.size());
    boolean bOK = false;
    while (it.hasNext()) {
      final org.jajuk.base.items.File file = (org.jajuk.base.items.File) it.next();
      if (bOK || file.equals(fileStart)) {
        alOut.add(file);
        bOK = true;
      }
    }
    return alOut;
  }

  /**
   * return child files recursively
   *
   * @return child files recursively
   */
  public ArrayList<org.jajuk.base.items.File> getFilesRecursively() {
    final ArrayList<org.jajuk.base.items.File> alFiles = new ArrayList<org.jajuk.base.items.File>(100);
    for (final Item item : ((FileManager) ItemType.File.getManager()).getCarbonItems()) {
      final org.jajuk.base.items.File file = (org.jajuk.base.items.File) item;
      if (file.hasAncestor(this)) {
        alFiles.add(file);
      }
    }
    return alFiles;
  }

  /**
   * @param directory
   */
  public void addFile(final org.jajuk.base.items.File file) {
    files.add(file);
  }

  /**
   * @param directory
   */
  public void changePlaylistFile(final PlaylistFile plfOld, final PlaylistFile plfNew) {
    playlistFiles.remove(plfOld);
    playlistFiles.add(plfNew);
  }

  /**
   * @param directory
   */
  public void changeFile(final org.jajuk.base.items.File fileOld, final org.jajuk.base.items.File fileNew) {
    files.remove(fileOld);
    files.add(fileNew);
  }

  /**
   * Scan all files in a directory
   *
   * @param bDeepScan:
   *          force files tag read
   * @param reporter
   *          Refresh handler
   */
  public void scan(boolean bDeepScan, final RefreshReporter reporter) {
    final java.io.File[] files = getFio().listFiles(Util.fileFilter);
    if ((files == null) || (files.length == 0)) { // none file, leave
      return;
    }
    for (int i = 0; i < files.length; i++) {
      try { // check errors for each file
        // Note date for file date property. CAUTION: do not try to
        // check current date to accelerate refreshing if file has not
        // been modified since last refresh as user can rename a parent
        // directory and the files times under it are not modified
        final long lastModified = files[i].lastModified();
        // Check file name is correct (useful to fix name encoding
        // issues)
        if (!new File(files[i].getAbsolutePath()).exists()) {
          Log.warn("Cannot read file name (please rename it): {{" + files[i].getAbsolutePath()
              + "}}");
          continue;
        }
        final boolean bIsMusic = (Boolean) ((TypeManager) ItemType.Type.getManager()).getTypeByExtension(
            Util.getExtension(files[i])).getValue(TYPE_IS_MUSIC);
        // Ignore iTunes files
        if (files[i].getName().startsWith("._")) {
          continue;
        }
        if (bIsMusic) {
          final String name = files[i].getName();
          final String sId = FileManager.createID(name, this).intern();
          // check the file is not already known in database
          final org.jajuk.base.items.File fileRef = ((FileManager) ItemType.File.getManager()).getFileByID(sId);
          // Set name again to make sure Windows users will see actual
          // name with right case
          if (Util.isUnderWindows() && (fileRef != null)) {
            fileRef.setName(name);
          }
          // if known file and no deep scan, just leave
          if ((fileRef != null) && !bDeepScan) {
            continue;
          }
          // New file or deep scan case
          Tag tag = null;
          // ignore tag error to make sure to get a
          // tag object in all cases
          tag = new Tag(files[i], true);
          if (tag.isCorrupted()) {
            if (reporter != null) {
              reporter.notifyCorruptedFile();
            }
            Log.error(103, "{{" + files[i].getAbsolutePath() + "}}", null);
          }
          // if an error occurs, just notice it but keep the track
          final String sTrackName = tag.getTrackName();
          final String sAlbumName = tag.getAlbumName();
          final String sAuthorName = tag.getAuthorName();
          final String sStyle = tag.getStyleName();
          final long length = tag.getLength(); // length in sec
          final String sYear = tag.getYear();
          final long lQuality = tag.getQuality();
          final String sComment = tag.getComment();
          final long lOrder = tag.getOrder();
          if ((fileRef == null) && (reporter != null)) {
            // stats, do it here and not
            // before because we ignore the
            // file if we cannot read it
            reporter.notifyNewFile();
          }
          final Album album = ((AlbumManager) ItemType.Album.getManager()).registerAlbum(sAlbumName);
          final Style style = ((StyleManager) ItemType.Style.getManager()).registerStyle(sStyle);
          final Year year = ((YearManager) ItemType.Year.getManager()).registerYear(sYear);
          final Author author = ((AuthorManager) ItemType.Author.getManager()).registerAuthor(sAuthorName);
          final Type type = ((TypeManager) ItemType.Type.getManager()).getTypeByExtension(Util.getExtension(files[i]));
          // Store number of tracks in collection (note that the
          // collection is locked)
          final long trackNumber = ((TrackManager) ItemType.Track.getManager()).getElementCount();
          final Track track = ((TrackManager) ItemType.Track.getManager()).registerTrack(sTrackName, album, style, author,
              length, year, lOrder, type);
          // Update discovery date only if it is a new track
          if (((TrackManager) ItemType.Track.getManager()).getElementCount() > trackNumber) {
            // A new track has been created, we can safely update
            // the track date
            // We don't want to update date if the track is already
            // known, even if
            // it is a nex file because a track can map several
            // files and discovery date
            // is a track attribute, not file one
            track.setAdditionDate(new Date());
          }
          final org.jajuk.base.items.File file = ((FileManager) ItemType.File.getManager()).registerFile(sId,
              files[i].getName(), this, track, files[i].length(), lQuality);
          // Set file date
          file.getProperties().set(FILE_DATE, new Date(lastModified));
          // Comment is at the track level, note that we take last
          // found file comment but we changing a comment, we will
          // apply to all files for a track
          track.setComment(sComment);
        } else { // playlist file
          final String sId = PlaylistFileManager.createID(files[i].getName(), this);
          final PlaylistFile plfRef = ((PlaylistFileManager) ItemType.PlaylistFile.getManager()).getPlaylistFileByID(sId);
          // if known playlist file and no deep scan, just leave
          if ((plfRef != null) && !bDeepScan) {
            continue;
          }
          final PlaylistFile plFile = ((PlaylistFileManager) ItemType.PlaylistFile.getManager()).registerPlaylistFile(files[i],
              this);
          // set hashcode to this playlist file
          final String sHashcode = plFile.computesHashcode();
          plFile.forceRefresh(); // force refresh
          plFile.setHashcode(sHashcode);
          // create associated playlist
          ((PlaylistManager) ItemType.Playlist.getManager()).registerPlaylist(plFile);
          // add playlist file to current directory
          addPlaylistFile(plFile);
          if (plfRef == null) {
            // stats, do it here and not
            // before because we ignore the
            // file if we cannot read it
            if (reporter != null) {
              reporter.notifyNewFile();
            }

          }
        }
      } catch (final Exception e) {
        Log.error(103, files.length > 0 ? "{{" + files[i].toString() + "}}" : "", e);
      }
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
  public String getRelativePath() {
    if (getName().equals("")) {
      // if this directory is a root device
      // directory
      return "";
    }
    final StringBuilder sbOut = new StringBuilder().append(java.io.File.separatorChar).append(getName());
    boolean bTop = false;
    Directory dCurrent = this;
    while (!bTop && (dCurrent != null)) {
      dCurrent = dCurrent.getParentDirectory();
      if ((dCurrent != null) && !dCurrent.getName().equals("")) {
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
   * Alphabetical comparator used to display ordered lists of directories *
   * <p>
   * Sort ignoring cases but different items with different cases should be
   * distinct before being added into bidimap
   * </p>
   *
   * @param other
   *          directory to be compared
   * @return comparaison result
   */
  public int compareTo(final Directory otherDirectory) {
    // Perf: leave if directories are equals
    if (otherDirectory.equals(this)) {
      return 0;
    }
    final String abs = new StringBuilder(getDevice().getName()).append(getAbsolutePath()).toString();
    final String otherAbs = new StringBuilder(otherDirectory.getDevice().getName()).append(
        otherDirectory.getAbsolutePath()).toString();
    // should ignore case to get a B c ... and not Bac
    // Never return 0 here, because bidimap needs to distinct items
    final int comp = abs.compareToIgnoreCase(otherAbs);
    if (comp == 0) {
      return abs.compareTo(otherAbs);
    }
    return comp;
  }

  /**
   * Return whether this item should be hidden with hide option
   *
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getDevice().isMounted()
        || (ConfigurationManager.getBoolean(ConfKeys.OPTIONS_HIDE_UNMOUNTED) == false)) {
      return false;
    }
    return true;
  }

  /**
   * Get item description
   */
  @Override
  public String getDescription() {
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
  public String getHumanValue(final String sKey) {
    if (DIRECTORY_PARENT.equals(sKey)) {
      final Directory dParent = ((DirectoryManager) ItemType.Directory.getManager()).getDirectoryByID((String) getValue(sKey));
      if (dParent == null) {
        return ""; // no parent directory
      } else {
        return dParent.getFio().getAbsolutePath();
      }
    } else if (DEVICE.equals(sKey)) {
      return (((DeviceManager) ItemType.Device.getManager()).getDeviceByID((String) getValue(sKey))).getName();
    }
    if (NAME.equals(sKey)) {
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
   * @see org.jajuk.base.Item#getIcon()
   */
  @Override
  public ImageIcon getIcon() {
    ImageIcon icon = null;
    // is this device synchronized?
    if (getBooleanValue(DIRECTORY_SYNCHRONIZED)) {
      icon = IconLoader.ICON_DIRECTORY_SYNCHRO;
    } else {
      icon = IconLoader.ICON_DIRECTORY_DESYNCHRO;
    }
    return icon;
  }

}
