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
package org.jajuk.base;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.jajuk.services.core.ExitService;
import org.jajuk.services.tags.Tag;
import org.jajuk.ui.helpers.RefreshReporter;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.JajukRuntimeException;
import org.jajuk.util.filters.AudioFilter;
import org.jajuk.util.log.Log;

/**
 * A physical directory
 * <p>
 * Physical item.
 */
public class Directory extends PhysicalItem implements Comparable<Directory> {
  /** Parent directory ID*. */
  private final Directory dParent;
  /** Directory device. */
  private final Device device;
  /** IO file for optimizations*. */
  private java.io.File fio;
  private long discID = -1l;

  /**
   * Directory constructor.
   *
   * @param sId 
   * @param sName 
   * @param dParent 
   * @param device 
   */
  Directory(String sId, String sName, Directory dParent, Device device) {
    super(sId, sName);
    // check that top directories name is void
    if (dParent == null && !"".equals(sName)) {
      throw new JajukRuntimeException("Top directory name should be a void string");
    }
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
  public final String getXMLTag() {
    return XML_DIRECTORY;
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return "Directory[ID=" + getID() + " Relative path={{" + getRelativePath() + "}} ParentID="
        + (dParent == null ? "null" : dParent.getID()) + " Device={{" + device.getName() + "}}]";
  }

  /**
   * Gets the absolute path.
   * 
   * @return the absolute path
   */
  public String getAbsolutePath() {
    StringBuilder sbOut = new StringBuilder(getDevice().getUrl()).append(getRelativePath());
    return sbOut.toString();
  }

  /**
   * Gets the device.
   * 
   * @return the device
   */
  public Device getDevice() {
    return device;
  }

  /**
   * Gets the parent directory (null if this directory is a top directory).
   * 
   * @return the parent directory
   */
  public Directory getParentDirectory() {
    return dParent;
  }

  /**
   * Gets the sub-directories.
   * 
   * @return all sub directories
   */
  public Set<Directory> getDirectories() {
    Set<Directory> out = new LinkedHashSet<Directory>(2);
    // Iterate against a copy of directories, not a ReadOnlyIterator to avoid handling 
    // synchronization issues, this method is not in a critical sequence
    List<Directory> dirs = DirectoryManager.getInstance().getDirectories();
    for (Directory directory : dirs) {
      if (directory.getFio().getParentFile() != null
          && directory.getFio().getParentFile().equals(this.getFio())
          // check the device of the tested directory to handle directories
          // from cdroms for ie
          && directory.getDevice().equals(getDevice())) {
        out.add(directory);
      }
    }
    return out;
  }

  /**
   * return child files.
   * 
   * @return child files
   */
  public Set<org.jajuk.base.File> getFiles() {
    Set<org.jajuk.base.File> out = new LinkedHashSet<org.jajuk.base.File>(2);
    for (org.jajuk.base.File file : FileManager.getInstance().getFiles()) {
      if (file.getFIO().getParentFile().equals(this.getFio())) {
        out.add(file);
      }
    }
    return out;
  }

  /**
   * return playlists.
   * 
   * @return playlists
   */
  public Set<Playlist> getPlaylistFiles() {
    Set<Playlist> out = new LinkedHashSet<Playlist>(2);
    for (Playlist plf : PlaylistManager.getInstance().getPlaylists()) {
      if (plf.getFIO().getParentFile().equals(this.getFio())) {
        out.add(plf);
      }
    }
    return out;
  }

  /**
   * return ordered sibling files from the given file index.
   * 
   * @param fileStart 
   * 
   * @return files or null if the given file is unknown
   */
  public List<org.jajuk.base.File> getFilesFromFile(org.jajuk.base.File fileStart) {
    Set<org.jajuk.base.File> files = getFiles();
    List<org.jajuk.base.File> alOut = new ArrayList<org.jajuk.base.File>(files);
    int indexOfStartingItem = alOut.indexOf(fileStart);
    if (indexOfStartingItem < 0) {
      return null;
    }
    return alOut.subList(indexOfStartingItem + 1, alOut.size());
  }

  /**
   * return ordered child files recursively.
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
   * return ordered child directories recursively.
   * 
   * @return child directories recursively
   */
  public List<Directory> getDirectoriesRecursively() {
    List<Directory> alDirs = new ArrayList<Directory>(10);
    for (Item item : DirectoryManager.getInstance().getDirectories()) {
      Directory dir = (Directory) item;
      if (dir.hasAncestor(this)) {
        alDirs.add(dir);
      }
    }
    return alDirs;
  }

  /**
   * Return true is the specified directory is an ancestor for this directory.
   * 
   * @param directory directory to check
   * 
   * @return true, if given directory is a parent directory of this directory
   */
  boolean hasAncestor(Directory directory) {
    Directory dirTested = this;
    while (true) {
      if (!dirTested.equals(this) && dirTested.equals(directory)) {
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
   * return ordered child playlists recursively.
   * 
   * @return child playlists recursively
   */
  public List<Playlist> getPlaylistsRecursively() {
    List<Playlist> alPlaylists = new ArrayList<Playlist>(100);
    for (Item item : PlaylistManager.getInstance().getPlaylists()) {
      Playlist playlist = (Playlist) item;
      if (playlist.hasAncestor(this)) {
        alPlaylists.add(playlist);
      }
    }
    return alPlaylists;
  }

  /**
   * Scan all files in a directory.
   * 
   * @param bDeepScan :
   * force files tag read
   * @param reporter Refresh handler
   */
  void scan(boolean bDeepScan, RefreshReporter reporter) {
    // Make sure to reset the disc ID
    this.discID = -1;
    java.io.File[] filelist = getFio().listFiles(UtilSystem.getFileFilter());
    if (filelist == null || filelist.length == 0) { // none file, leave
      return;
    }
    // Create a list of music files and playlist files to consider
    List<File> musicFiles = new ArrayList<File>(filelist.length);
    List<File> playlistFiles = new ArrayList<File>(filelist.length);
    for (int i = 0; i < filelist.length; i++) {
      // Leave ASAP if exit request
      if (ExitService.isExiting()) {
        return;
      }
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
      // Now, compute disc ID and cache tags 
      boolean bIsMusic = (Boolean) type.getValue(Const.XML_TYPE_IS_MUSIC);
      if (bIsMusic) {
        musicFiles.add(filelist[i]);
      } else { // playlist
        playlistFiles.add(filelist[i]);
      }
    }
    // Compute the disc id 
    this.discID = computeDiscID();
    // Perform actual scan and check errors for each file
    for (File musicfile : musicFiles) {
      try {
        scanMusic(musicfile, bDeepScan, reporter);
      } catch (Exception e) {
        Log.error(103, filelist.length > 0 ? "{{" + musicfile.toString() + "}}" : "", e);
      }
    }
    for (File playlistFile : playlistFiles) {
      try {
        scanPlaylist(playlistFile, bDeepScan, reporter);
      } catch (Exception e) {
        Log.error(103, filelist.length > 0 ? "{{" + playlistFile.toString() + "}}" : "", e);
      }
    }
    // Clear the tag cache so tags are actually read at next deep refresh
    Tag.clearCache();
    // Force cover detection (after done once, the cover file is cached as album property)
    // We need this to avoid bug #1550 : if the device is created, then unplugged, catalog
    // view cover/no-cover filter is messed-up because the findCover() method always return null.
    Set<Album> albumsToCheck = getAlbums();
    for (Album album : albumsToCheck) {
      album.findCover();
    }
  }

  /**
   * Compute the directory disc id (simply the sum in bits of the audio files lengths)
   * @return the directory disc id
   */
  private long computeDiscID() {
    long out = 0;
    JajukFileFilter filter = new JajukFileFilter(AudioFilter.getInstance());
    File fDir = getFio();
    java.io.File[] files = fDir.listFiles();// null if none file
    // found
    for (int i = 0; files != null && i < files.length; i++) {
      if (filter.accept(files[i])) {
        out += files[i].length();
      }
    }
    return out;
  }

  /**
   * Return list of albums for current directory.
   *
   * @return list of albums for current directory
   */
  public Set<Album> getAlbums() {
    Set<Album> out = new HashSet<Album>(1);
    Set<org.jajuk.base.File> files = this.getFiles();
    for (org.jajuk.base.File file : files) {
      out.add(file.getTrack().getAlbum());
    }
    return out;
  }

  /**
   * Scan music.
   *
   * @param music 
   * @param bDeepScan 
   * @param reporter 
   * @throws JajukException the jajuk exception
   */
  private void scanMusic(java.io.File music, boolean bDeepScan, RefreshReporter reporter)
      throws JajukException {
    String lName = music.getName();
    String sId = FileManager.createID(lName, this);
    // check the file is not already known in database
    org.jajuk.base.File fileRef = FileManager.getInstance().getFileByID(sId);
    // Set name again to make sure Windows users will see actual
    // name with right case
    if (UtilSystem.isUnderWindows() && fileRef != null) {
      fileRef.setName(lName);
    }
    // if known file and no deep scan, just leave
    if (fileRef != null && !bDeepScan) {
      return;
    }
    // Is this format tag readable ?
    Type type = TypeManager.getInstance().getTypeByExtension(UtilSystem.getExtension(music));
    boolean tagSupported = (type.getTaggerClass() != null);
    // Deep refresh : if the audio file format doesn't support tagging (like wav) and the file
    // is already known, continue, no need to try to read tags
    if (!tagSupported && fileRef != null) {
      return;
    }
    // Ignore tag error to make sure to get a
    // tag object in all cases.
    Tag tag = Tag.getTagForFio(music, true);
    // We need a tag instance even for unsupported formats but it that
    // case, we don't notify tag reading errors
    if (tag.isCorrupted() && tagSupported) {
      if (reporter != null) {
        reporter.notifyCorruptedFile();
      }
      // if an error occurs, just display a message but keep the track
      Log.error(103, "{{" + music.getAbsolutePath() + "}}", null);
    }
    String sTrackName = tag.getTrackName();
    String sAlbumName = tag.getAlbumName();
    String sArtistName = tag.getArtistName();
    String sGenre = tag.getGenreName();
    long length = tag.getLength(); // length in sec
    String sYear = tag.getYear();
    long lQuality = tag.getQuality();
    String sComment = tag.getComment();
    long lOrder = tag.getOrder();
    String sAlbumArtist = tag.getAlbumArtist();
    long discNumber = tag.getDiscNumber();
    if (fileRef == null && reporter != null) {
      // stats, do it here and not
      // before because we ignore the
      // file if we cannot read it
      reporter.notifyNewFile();
    }
    // Store oldDiscID, it is used to clone album and track
    // properties when album disc ID was unset to avoid loosing ratings or custom properties
    long oldDiscID = 0;
    if (fileRef != null) {
      oldDiscID = fileRef.getTrack().getAlbum().getDiscID();
    }
    Track track = registerFile(music, sId, sTrackName, sAlbumName, sArtistName, sGenre, length,
        sYear, lQuality, sComment, lOrder, sAlbumArtist, oldDiscID, discID, discNumber);
    for (String s : Tag.getActivatedExtraTags()) {
      track.setProperty(s, tag.getTagField(s));
    }
  }

  private Track registerFile(java.io.File music, String sFileId, String sTrackName,
      String sAlbumName, String sArtistName, String sGenre, long length, String sYear,
      long lQuality, String sComment, long lOrder, String sAlbumArtist, long oldDiskID,
      long discID, long discNumber) {
    Album album = AlbumManager.getInstance().registerAlbum(sAlbumName, discID);
    Genre genre = GenreManager.getInstance().registerGenre(sGenre);
    Year year = YearManager.getInstance().registerYear(sYear);
    Artist artist = ArtistManager.getInstance().registerArtist(sArtistName);
    AlbumArtist albumArtist = AlbumArtistManager.getInstance().registerAlbumArtist(sAlbumArtist);
    Type type = TypeManager.getInstance().getTypeByExtension(UtilSystem.getExtension(music));
    // Store number of tracks in collection
    long trackNumber = TrackManager.getInstance().getElementCount();
    Track track = TrackManager.getInstance().registerTrack(sTrackName, album, genre, artist,
        length, year, lOrder, type, discNumber);
    cloneTrackPropertiesIfRemovedMeanwhile(track);
    cloneAlbumAndTrackPropertiesIfAlbumIDChanged(sTrackName, sAlbumName, length, lOrder, oldDiskID,
        discID, discNumber, album, genre, year, artist, type, track);
    org.jajuk.base.File file = FileManager.getInstance().registerFile(sFileId, music.getName(),
        this, track, music.length(), lQuality);
    updateDateInformation(file, music, trackNumber, track);
    // Comment is at the track level, note that we take last
    // found file comment but we changing a comment, we will
    // apply to all files for a track
    track.setComment(sComment);
    // Apply the album artist
    track.setAlbumArtist(albumArtist);
    // Make sure to refresh file size
    file.setProperty(Const.XML_SIZE, music.length());
    return track;
  }

  /**
   * Force to reset track properties if the track name has been changed so its associated track has been moved to a the attic
   * @param track
   */
  private void cloneTrackPropertiesIfRemovedMeanwhile(Track track) {
    Track removedTrack = TrackManager.getInstance().getTrackFromAttic(track.getID());
    if (removedTrack != null) {
      track.cloneProperties(removedTrack);
    }
  }

  private void updateDateInformation(org.jajuk.base.File file, java.io.File music,
      long trackNumber, Track track) {
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
      // A new track has been created, we can safely update  the track date
      // We don't want to update date if the track is already known, even if
      // it is a new file because a track can map several files and discovery date
      // is a track attribute, not file one.
      track.setDiscoveryDate(new Date());
    }
    // Set file date
    file.setProperty(Const.XML_FILE_DATE, new Date(lastModified));
  }

  private void cloneAlbumAndTrackPropertiesIfAlbumIDChanged(String sTrackName, String sAlbumName,
      long length, long lOrder, long oldDiskID, long discID, long discNumber, Album album,
      Genre genre, Year year, Artist artist, Type type, Track track) {
    // Fix for #1630 : we clone the properties from the old track mapped with the old album id so we keep rating
    // (among other data)
    if (oldDiskID != discID) {
      String oldAlbumID = AlbumManager.createID(sAlbumName, oldDiskID);
      Album oldAlbum = AlbumManager.getInstance().getAlbumByID(oldAlbumID);
      if (oldAlbum != null) {
        // Also clone album properties (useful to keep custom tags)
        album.cloneProperties(oldAlbum);
        String oldTrackID = TrackManager.createID(sTrackName, oldAlbum, genre, artist, length,
            year, lOrder, type, discNumber);
        Track oldTrack = TrackManager.getInstance().getTrackByID(oldTrackID);
        if (oldTrack != null) {
          track.cloneProperties(oldTrack);
        }
      }
    }
  }

  /**
   * Scan playlist.
   * 
   * 
   * @param file 
   * @param bDeepScan 
   * @param reporter 
   * 
   * @throws JajukException the jajuk exception
   */
  private void scanPlaylist(final java.io.File file, final boolean bDeepScan,
      final RefreshReporter reporter) throws JajukException {
    String sId = PlaylistManager.createID(file.getName(), this);
    Playlist plfRef = PlaylistManager.getInstance().getPlaylistByID(sId);
    // if known playlist and no deep scan, just leave
    if (plfRef != null && !bDeepScan) {
      return;
    }
    Playlist plFile = PlaylistManager.getInstance().registerPlaylistFile(file, this);
    plFile.load(); // force refresh
    if (plfRef == null && reporter != null) {
      // stats, do it here and not
      // before because we ignore the
      // file if we cannot read it
      reporter.notifyNewFile();
    }
  }

  /**
   * Reset pre-calculated paths*.
   */
  protected void reset() {
    fio = null;
  }

  /**
   * Return full directory path name relative to device url.
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
   * Gets the fio.
   * 
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
   * </p>.
   * 
   * @param otherDirectory 
   * 
   * @return comparison result
   */
  @Override
  public int compareTo(Directory otherDirectory) {
    if (otherDirectory == null) {
      return -1;
    }
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
   * Return whether this item should be hidden with hide option.
   * 
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getDevice().isMounted() || !Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.Item#getTitle()
   */
  @Override
  public String getTitle() {
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
      Device dev = DeviceManager.getInstance().getDeviceByID((String) getValue(sKey));
      if (dev == null) {
        return "";
      }
      return dev.getName();
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
   * Refresh the directory synchronously, no dialog. <br>
   * This method is only a wrapper to Device.refreshCommand() method
   * 
   * @param bDeepScan whether it is a deep refresh request or only fast
   * 
   * @return true if some changes occurred in device
   */
  public synchronized boolean refresh(final boolean bDeepScan) {
    List<Directory> dirsToRefresh = new ArrayList<Directory>(1);
    dirsToRefresh.add(this);
    return getDevice().refreshCommand(bDeepScan, false, dirsToRefresh);
  }

  /**
   * Return true is this is a child directory of the specified directory.
   * 
   * @param directory ancestor directory
   * 
   * @return true, if checks if is child of
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
