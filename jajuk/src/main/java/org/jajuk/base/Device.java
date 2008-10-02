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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.helpers.RefreshReporter;
import org.jajuk.ui.thumbnails.ThumbnailsMaker;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.ImageFilter;
import org.jajuk.util.filters.KnownTypeFilter;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;

/**
 * A device ( music files repository )
 * <p>
 * Some properties of a device are immuable : name, url and type *
 * <p>
 * Physical item
 */
public class Device extends PhysicalItem implements Const, Comparable<Device> {

  private static final long serialVersionUID = 1L;

  protected static final int OPTION_REFRESH_DEEP = 1;

  protected static final int OPTION_REFRESH_CANCEL = 2;

  // Device type constants
  public static final int TYPE_DIRECTORY = 0;

  public static final int TYPE_CD = 1;

  public static final int TYPE_NETWORK_DRIVE = 2;

  public static final int TYPE_EXT_DD = 3;

  public static final int TYPE_PLAYER = 4;

  /** Device URL (used for perfs) */
  private String sUrl;

  /** IO file for optimizations* */
  private java.io.File fio;

  /** Device mount point* */
  private static final String MOUNT_POINT = "";

  /** Mounted device flag */
  private boolean bMounted = false;

  /** directories */
  private final List<Directory> alDirectories = new ArrayList<Directory>(20);

  /** Already refreshing flag */
  private volatile boolean bAlreadyRefreshing = false;

  /** Already synchronizing flag */
  private volatile boolean bAlreadySynchronizing = false;

  /** Number of files in this device before refresh ( for refresh stats ) */
  private int iNbFilesBeforeRefresh;

  /** Number of dirs in this device before refresh */
  private int iNbDirsBeforeRefresh;

  /** Number of playlists in this device before refresh */
  private int iNbPlaylistsBeforeRefresh;

  /** Number of created files on source device during synchro ( for stats ) */
  private int iNbCreatedFilesSrc;

  /**
   * Number of created files on destination device during synchro ( for stats )
   */
  private int iNbCreatedFilesDest;

  /** Number of deleted files during a synchro ( for stats ) */
  int iNbDeletedFiles = 0;

  /** Volume of created files during synchro */
  long lVolume = 0;

  /** date last refresh */
  long lDateLastRefresh;

  /** Progess reporter **/
  private RefreshReporter reporter;

  /**
   * Device constructor
   * 
   * @param sId
   * @param sName
   * @param iDeviceType
   * @param sUrl
   */
  public Device(final String sId, final String sName) {
    super(sId, sName);
  }

  /**
   * @param directory
   */
  public void addDirectory(final Directory directory) {
    alDirectories.add(directory);
  }

  /**
   * Scan directories to cleanup removed files and playlists
   * 
   * @param device
   *          device to cleanup
   * @return whether some items have been removed
   */
  public boolean cleanRemovedFiles() {
    long l = System.currentTimeMillis();
    // directories cleanup
    boolean bChanges = cleanDirectories();

    // files cleanup
    bChanges = bChanges | cleanFiles();

    // Playlist cleanup
    bChanges = bChanges | cleanPlaylist();

    // clear history to remove old files referenced in it
    if (Conf.getString(Const.CONF_HISTORY) != null) {
      History.getInstance().clear(Integer.parseInt(Conf.getString(Const.CONF_HISTORY)));
    }

    // delete old history items
    l = System.currentTimeMillis() - l;
    Log.debug("Old file references cleaned in: "
        + ((l < 1000) ? l + " ms" : l / 1000 + " s, changes: " + bChanges));

    return bChanges;
  }

  /**
   * Walk through all Playlists and remove the ones for the current device
   * 
   * @return true if there was any playlist removed
   */
  private boolean cleanPlaylist() {
    boolean bChanges = false;
    final Set<Playlist> plfiles = PlaylistManager.getInstance().getPlaylists();
    for (final Playlist plf : plfiles) {
      if (!ExitService.isExiting() && plf.getDirectory().getDevice().equals(this) && plf.isReady()
          && !plf.getFio().exists()) {
        PlaylistManager.getInstance().removePlaylistFile(plf);
        Log.debug("Removed: " + plf);
        bChanges = true;
      }
    }
    return bChanges;
  }

  /**
   * Walk through tall Files and remove the ones for the current device.
   * 
   * @return true if there was any file removed.
   */
  private boolean cleanFiles() {
    boolean bChanges = false;
    final Set<org.jajuk.base.File> files = FileManager.getInstance().getFiles();
    for (final org.jajuk.base.File file : files) {
      if (!ExitService.isExiting() && file.getDirectory().getDevice().equals(this)
          && file.isReady() &&
          // Remove file if it doesn't exist any more or if it is a iTunes
          // file (useful for jajuk < 1.4)
          (!file.getIO().exists() || file.getName().startsWith("._"))) {
        FileManager.getInstance().removeFile(file);
        Log.debug("Removed: " + file);
        bChanges = true;
      }
    }

    return bChanges;
  }

  /**
   * Walks through all directories and removes the ones for this device.
   * 
   * @return true if there was any directory removed
   */
  private boolean cleanDirectories() {
    boolean bChanges = false;
    // need to use a shallow copy to avoid concurrent exceptions
    final Set<Directory> dirs = DirectoryManager.getInstance().getDirectories();

    for (final Item item : dirs) {
      final Directory dir = (Directory) item;
      if (!ExitService.isExiting() && dir.getDevice().equals(this) && dir.getDevice().isMounted()
          && !dir.getFio().exists()) {
        // note that associated files are removed too
        DirectoryManager.getInstance().removeDirectory(dir.getID());
        Log.debug("Removed: " + dir);
        bChanges = true;
      }
    }
    return bChanges;
  }

  /**
   * Alphabetical comparator used to display ordered lists of devices
   * 
   * @param other
   *          device to be compared
   * @return comparaison result
   */
  public int compareTo(final Device otherDevice) {
    return getName().compareToIgnoreCase(otherDevice.getName());
  }

  public long getDateLastRefresh() {
    return lDateLastRefresh;
  }

  /**
   * Get item description
   */
  @Override
  public String getDesc() {
    return Messages.getString("Item_Device") + " : " + getName();
  }

  /**
   * @return
   */
  public String getDeviceTypeS() {
    return DeviceManager.getInstance().getDeviceType(getLongValue(Const.XML_TYPE));
  }

  /**
   * @return
   */
  public List<Directory> getDirectories() {
    return alDirectories;
  }

  /**
   * return child files recursively
   * 
   * @return child files recursively
   */
  public List<org.jajuk.base.File> getFilesRecursively() {
    // looks for the root directory for this device
    Directory dirRoot = null;
    final Collection<Directory> dirs = DirectoryManager.getInstance().getDirectories();
    final Iterator<Directory> it = dirs.iterator();
    while (it.hasNext()) {
      final Directory dir = it.next();
      if (dir.getDevice().equals(this) && dir.getFio().equals(fio)) {
        dirRoot = dir;
      }
    }
    List<org.jajuk.base.File> alFiles = new ArrayList<org.jajuk.base.File>(100);
    if (dirRoot != null) {
      alFiles = dirRoot.getFilesRecursively();
    }
    return alFiles;
  }

  /**
   * @return Returns the IO file reference to this directory.
   */
  public File getFio() {
    return fio;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(final String sKey) {
    if (Const.XML_TYPE.equals(sKey)) {
      final long lType = getLongValue(sKey);
      return DeviceManager.getInstance().getDeviceType(lType);
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
    switch ((int) getType()) {
    case 0:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_DIRECTORY_MOUNTED_SMALL),
          IconLoader.getIcon(JajukIcons.DEVICE_DIRECTORY_UNMOUNTED_SMALL));
    case 1:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_CD_MOUNTED_SMALL),
          IconLoader.getIcon(JajukIcons.DEVICE_CD_UNMOUNTED_SMALL));
    case 2:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_NETWORK_DRIVE_MOUNTED_SMALL),
          IconLoader.getIcon(JajukIcons.DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL));
    case 3:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_EXT_DD_MOUNTED_SMALL),
          IconLoader.getIcon(JajukIcons.DEVICE_EXT_DD_UNMOUNTED_SMALL));
    case 4:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_PLAYER_MOUNTED_SMALL),
          IconLoader.getIcon(JajukIcons.DEVICE_PLAYER_UNMOUNTED_SMALL));
    default:
      Log.warn("Unknown type of device detected: " + getType());
      return null;
    }
  }

  /**
   * @param mountedIcon
   *          TODO
   * @param unmountedIcon
   *          TODO
   * @return
   */
  private ImageIcon setIcon(ImageIcon mountedIcon, ImageIcon unmountedIcon) {
    if (isMounted()) {
      return mountedIcon;
    } else {
      return unmountedIcon;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public final String getLabel() {
    return Const.XML_DEVICE;
  }

  /**
   * @return Returns the unix mount point.
   */
  public String getMountPoint() {
    return MOUNT_POINT;
  }

  /**
   * 
   * @return Associated root directory
   */
  public Directory getRootDirectory() {
    return DirectoryManager.getInstance().getDirectoryForIO(getFio());
  }

  /**
   * @return
   */
  public long getType() {
    return getLongValue(Const.XML_TYPE);
  }

  /**
   * @return
   */
  public String getUrl() {
    return sUrl;
  }

  /**
   * @return
   */
  public boolean isMounted() {
    return bMounted;
  }

  /**
   * Return true if the device can be accessed right now
   * 
   * @return true the file can be accessed right now
   */
  public boolean isReady() {
    if (isMounted() && !isRefreshing() && !isSynchronizing()) {
      return true;
    }
    return false;
  }

  /**
   * Tells if a device is refreshing
   */
  public boolean isRefreshing() {
    return bAlreadyRefreshing;
  }

  /**
   * Tells if a device is synchronizing
   */
  public boolean isSynchronizing() {
    return bAlreadySynchronizing;
  }

  /**
   * Manual refresh
   * 
   * @param bAsk:
   *          Should we ask user if a deep or fast scan is required?
   *          default=deep
   */
  private void manualRefresh(final boolean bAsk) {
    try {
      reporter = new RefreshReporter(this);
      int i = 0;
      try {
        i = prepareRefresh(bAsk);
        if (i == OPTION_REFRESH_CANCEL) {
          return;
        }
        bAlreadyRefreshing = true;
      } catch (JajukException je) {
        Messages.showErrorMessage(je.getCode());
        Log.debug(je);
        return;
      }
      reporter.startup();
      // clean old files up (takes a while)
      cleanRemovedFiles();
      reporter.cleanupDone();
      // Actual refresh
      refreshCommand((i == Device.OPTION_REFRESH_DEEP));
      // notify views to refresh
      ObservationManager.notify(new Event(JajukEvents.DEVICE_REFRESH));
      // cleanup logical items
      org.jajuk.base.Collection.cleanupLogical();
      // commit collection at each refresh (can be useful if application
      // is closed brutally with control-C or shutdown and that exit hook
      // have no time to perform commit)
      try {
        org.jajuk.base.Collection.commit(UtilSystem.getConfFileByPath(Const.FILE_COLLECTION));
      } catch (final IOException e) {
        Log.error(e);
      }
      reporter.done();
    } finally {
      // Make sure to unlock refreshing
      bAlreadyRefreshing = false;
    }
  }

  /**
   * Prepare manual refresh
   * 
   * @param bAsk
   * @return the user choice (deep or fast)
   * @throws JajukException
   *           if user canceled, device cannot be refreshed or device already
   *           refreshing
   */
  public int prepareRefresh(final boolean bAsk) throws JajukException {
    int i = Device.OPTION_REFRESH_DEEP;
    if (bAsk) {
      final Object[] possibleValues = { Messages.getString("FilesTreeView.60"),// fast
          Messages.getString("FilesTreeView.61"),// deep
          Messages.getString("Cancel") };// cancel
      i = JOptionPane.showOptionDialog(null, Messages.getString("FilesTreeView.59"), Messages
          .getString("Option"), JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
          possibleValues, possibleValues[0]);
      if (i == Device.OPTION_REFRESH_CANCEL) { // Cancel
        return Device.OPTION_REFRESH_CANCEL;
      }
    }
    final Device device = this;
    if (!device.isMounted()) {
      try {
        device.mount();
      } catch (final Exception e) {
        Log.error(11, "{{" + getName() + "}}", e); // mount failed
        Messages.showErrorMessage(11, getName());
        throw new JajukException(11);
      }
    }
    if (bAlreadyRefreshing) {
      throw new JajukException(107);
    }
    return i;
  }

  /**
   * Mount the device
   */
  public void mount() throws Exception {
    mount(true);
  }

  /**
   * Mount the device
   * 
   * @param bUIRefresh
   *          set whether the UI should be refreshed
   * @throws Exception
   *           if device cannot be mounted
   */
  public void mount(final boolean bUIRefresh) throws Exception {
    if (bMounted) {
      Messages.showErrorMessage(111);
      return;
    }
    try {
      final File file = new File(getUrl());
      if (!file.exists()) {
        throw new Exception("Path does not exist: " + file.toString());
      }
    } catch (final Exception e) {
      throw new JajukException(11, getName(), e);
    }
    // Cannot mount void devices because of reference garbager thread
    final File file = new File(getUrl());
    if ((file.listFiles() == null) || (file.listFiles().length == 0)) {
      final int answer = Messages.getChoice("[" + getName() + "] "
          + Messages.getString("Confirmation_void_refresh"), JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.WARNING_MESSAGE);
      if (answer != JOptionPane.YES_OPTION) {
        // leave if user doesn't confirm to mount the void device
        return;
      }
    }
    // Here the device is considered as mounted
    bMounted = true;
    // notify views to refresh if needed
    if (bUIRefresh) {
      ObservationManager.notify(new Event(JajukEvents.DEVICE_MOUNT));
    }
    ObservationManager.notify(new Event(JajukEvents.DEVICE_REFRESH));
  }

  /**
   * Set all personnal properties of an XML file for an item (doesn't overwrite
   * existing properties for perfs)
   * 
   * @param attributes :
   *          list of attributes for this XML item
   */
  @Override
  public void populateProperties(final Attributes attributes) {
    for (int i = 0; i < attributes.getLength(); i++) {
      final String sProperty = attributes.getQName(i);
      if (!getProperties().containsKey(sProperty)) {
        String sValue = attributes.getValue(i);
        final PropertyMetaInformation meta = getMeta(sProperty);
        // compatibility code for <1.1 : auto-refresh is now a double,
        // no more a boolean
        if (meta.getName().equals(Const.XML_DEVICE_AUTO_REFRESH)
            && (sValue.equalsIgnoreCase(Const.TRUE) || sValue.equalsIgnoreCase(Const.FALSE))) {
          switch ((int) getType()) {
          case TYPE_DIRECTORY: // directory
            sValue = "0.5d";
            break;
          case TYPE_CD: // file cd
            sValue = "0d";
            break;
          case TYPE_NETWORK_DRIVE: // network drive
            sValue = "0d";
            break;
          case TYPE_EXT_DD: // ext dd
            sValue = "3d";
            break;
          case TYPE_PLAYER: // player
            sValue = "3d";
            break;
          }
        }
        try {
          setProperty(sProperty, UtilString.parse(sValue, meta.getType()));
        } catch (final Exception e) {
          Log.error(137, sProperty, e);
        }
      }
    }
  }

  /**
   * Refresh : scan asynchronously the device to find tracks
   * 
   * @param bAsynchronous :
   *          set asynchronous or synchronous mode
   * @param bAsk:
   *          should we ask user if he wants to perform a deep or fast scan?
   *          default=deep
   */
  public void refresh(final boolean bAsynchronous) {
    refresh(bAsynchronous, false);
  }

  /**
   * Refresh : scan asynchronously the device to find tracks
   * 
   * @param bAsynchronous :
   *          set asynchronous or synchronous mode
   * @param bAsk:
   *          should we ask user if he wants to perform a deep or fast scan?
   *          default=deep
   */
  public void refresh(final boolean bAsynchronous, final boolean bAsk) {
    if (bAsynchronous) {
      final Thread t = new Thread() {
        @Override
        public void run() {
          manualRefresh(bAsk);
        }
      };
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    } else {
      manualRefresh(bAsk);
    }

  }

  /**
   * The refresh itself
   * 
   * @return true if some changes occurred in device
   * 
   */
  public synchronized boolean refreshCommand(final boolean bDeepScan) {
    try {
      bAlreadyRefreshing = true;
      if (reporter == null) {
        reporter = new RefreshReporter(this);
      }
      // Notify the reporter of the actual refresh startup
      reporter.refreshStarted();
      lDateLastRefresh = System.currentTimeMillis();
      // check Jajuk is not exiting because a refresh cannot start in
      // this state
      if (ExitService.isExiting()) {
        return false;
      }
      // check if this device is mounted (useful when called by
      // automatic refresh)
      if (!isMounted()) {
        return false;
      }
      // check target directory is not void because it could mean that the
      // device is not actually system-mounted and then a refresh would
      // clear the device, display a warning message
      final File file = new File(getUrl());
      if (file.exists() && ((file.list() == null) || (file.list().length == 0))) {
        final int i = Messages.getChoice(Messages.getString("Confirmation_void_refresh"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (i != JOptionPane.OK_OPTION) {
          return false;
        }
      }
      iNbFilesBeforeRefresh = FileManager.getInstance().getElementCount();
      iNbDirsBeforeRefresh = DirectoryManager.getInstance().getElementCount();
      iNbPlaylistsBeforeRefresh = PlaylistManager.getInstance().getElementCount();

      if (bDeepScan && Log.isDebugEnabled()) {
        Log.debug("Starting refresh of device : " + this);
      }
      final File fTop = new File(getStringValue(Const.XML_URL));
      if (!fTop.exists()) {
        Messages.showErrorMessage(101);
        return false;
      }
      // Create a directory for device itself and scan files to allow
      // files at the root of the device
      final Directory top = DirectoryManager.getInstance().registerDirectory(this);
      // Start actual scan
      scanRecursively(top, bDeepScan);
      // refresh thumbs required if nb of files or dirs changed, but it is
      // costly, do it only if many albums was discovered
      if (((FileManager.getInstance().getElementCount() - iNbFilesBeforeRefresh) > 200)
          || ((DirectoryManager.getInstance().getElementCount() - iNbDirsBeforeRefresh) > 20)) {
        // Refresh thumbs for new albums
        ThumbnailsMaker.launchAllSizes(false);
        return true;
      }
      // force a GUI refresh if new files or directories discovered or have been
      // removed
      else if (((FileManager.getInstance().getElementCount() - iNbFilesBeforeRefresh) != 0)
          || ((DirectoryManager.getInstance().getElementCount() - iNbDirsBeforeRefresh) != 0)
          || ((PlaylistManager.getInstance().getElementCount() - iNbPlaylistsBeforeRefresh) != 0)) {
        return true;
      }
      return false;
    } catch (final RuntimeException e) {
      // runtime errors are thrown
      throw e;
    } catch (final Exception e) {
      // and regular ones logged
      Log.error(e);
      return false;
    } finally {
      // make sure to unlock refreshing even if an error occurred
      bAlreadyRefreshing = false;
      // Notify the reporter of the actual refresh startup
      reporter.refreshDone();
    }
  }

  private void scanRecursively(final Directory dir, final boolean bDeepScan) {
    dir.scan(bDeepScan, reporter);
    if (reporter != null) {
      reporter.updateState(dir);
    }
    final File[] files = dir.getFio().listFiles(UtilSystem.getDirFilter());
    if (files != null) {
      for (final File element : files) {
        // Leave ASAP if exit request
        if (ExitService.isExiting()) {
          return;
        }
        final Directory subDir = DirectoryManager.getInstance().registerDirectory(
            element.getName(), dir, this);
        scanRecursively(subDir, bDeepScan);
      }
    }
  }

  /**
   * @param url
   *          The sUrl to set.
   */
  public void setUrl(final String url) {
    sUrl = url;
    setProperty(Const.XML_URL, url);
    fio = new File(url);
    /** Reset files */
    for (final org.jajuk.base.File file : FileManager.getInstance().getFiles()) {
      file.reset();
    }
    /** Reset playlists */
    for (final Playlist plf : PlaylistManager.getInstance().getPlaylists()) {
      plf.reset();
    }
    /** Reset directories */
    for (final Directory dir : DirectoryManager.getInstance().getDirectories()) {
      dir.reset();
    }
  }

  /**
   * Synchroning asynchronously
   * 
   * @param bAsynchronous :
   *          set asynchronous or synchronous mode
   * @return
   */
  public void synchronize(final boolean bAsynchronous) {
    // Check a source device is defined
    if (UtilString.isVoid((String) getValue(Const.XML_DEVICE_SYNCHRO_SOURCE))) {
      Messages.showErrorMessage(171);
      return;
    }
    final Device device = this;
    if (!device.isMounted()) {
      try {
        device.mount();
      } catch (final Exception e) {
        Log.error(11, "{{" + getName() + "}}", e); // mount failed
        Messages.showErrorMessage(11, getName());
        return;
      }
    }
    if (bAsynchronous) {
      final Thread t = new Thread() {
        @Override
        public void run() {
          synchronizeCommand();
        }
      };
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    } else {
      synchronizeCommand();
    }
  }

  /**
   * Synchronize action itself
   * 
   * @param device :
   *          device to synchronize
   */
  public void synchronizeCommand() {
    try {
      bAlreadySynchronizing = true;
      long lTime = System.currentTimeMillis();
      iNbCreatedFilesDest = 0;
      iNbCreatedFilesSrc = 0;
      iNbDeletedFiles = 0;
      lVolume = 0;
      final boolean bidi = (getValue(Const.XML_DEVICE_SYNCHRO_MODE)
          .equals(Const.DEVICE_SYNCHRO_MODE_BI));
      // check this device is synchronized
      final String sIdSrc = (String) getValue(Const.XML_DEVICE_SYNCHRO_SOURCE);
      if ((sIdSrc == null) || sIdSrc.equals(getID())) {
        // cannot synchro with itself
        return;
      }
      final Device dSrc = DeviceManager.getInstance().getDeviceByID(sIdSrc);
      // perform a fast refresh
      refreshCommand(false);
      // if bidi sync, refresh the other device as well (new file can
      // have been copied to it)
      if (bidi) {
        dSrc.refreshCommand(false);
      }
      // start message
      InformationJPanel.getInstance().setMessage(
          new StringBuilder(Messages.getString("Device.31")).append(dSrc.getName()).append(',')
              .append(getName()).append("]").toString(), InformationJPanel.INFORMATIVE);
      // in both cases (bi or uni-directional), make an unidirectional
      // sync from source device to this one
      iNbCreatedFilesDest = synchronizeUnidirectonal(dSrc, this);
      // now the other one if bidi
      iNbCreatedFilesDest += synchronizeUnidirectonal(this, dSrc);
      // end message
      lTime = System.currentTimeMillis() - lTime;
      final String sOut = new StringBuilder(Messages.getString("Device.33")).append(
          ((lTime < 1000) ? lTime + " ms" : lTime / 1000 + " s")).append(" - ").append(
          iNbCreatedFilesSrc + iNbCreatedFilesDest).append(Messages.getString("Device.35")).append(
          lVolume / 1048576).append(Messages.getString("Device.36")).toString();
      // perform a fast refresh
      refreshCommand(false);
      // if bidi sync, refresh the other device as well (new file can
      // have been copied to it)
      if (bidi) {
        dSrc.refreshCommand(false);
      }
      InformationJPanel.getInstance().setMessage(sOut, InformationJPanel.INFORMATIVE);
      Log.debug(sOut);
    } catch (final RuntimeException e) {
      // runtime errors are thrown
      throw e;
    } catch (final Exception e) {
      // and regular ones logged
      Log.error(e);
    } finally {
      // make sure to unlock synchronizing even if an error occurred
      bAlreadySynchronizing = false;
    }
  }

  /**
   * Synchronize a device with another one (unidirectional)
   * 
   * @param device :
   *          device to synchronize
   * @return nb of created files
   */
  private int synchronizeUnidirectonal(final Device dSrc, final Device dest) {
    final Set<Directory> hsSourceDirs = new HashSet<Directory>(100);
    // contains paths ( relative to device) of desynchronized dirs
    final Set<String> hsDesynchroPaths = new HashSet<String>(10);
    final Set<Directory> hsDestDirs = new HashSet<Directory>(100);
    int iNbCreatedFiles = 0;
    Iterator<Directory> it = DirectoryManager.getInstance().getDirectories().iterator();
    while (it.hasNext()) {
      final Directory dir = it.next();
      if (dir.getDevice().equals(dSrc)) {
        // don't take desynchronized dirs into account
        if (dir.getBooleanValue(Const.XML_DIRECTORY_SYNCHRONIZED)) {
          hsSourceDirs.add(dir);
        } else {
          hsDesynchroPaths.add(dir.getRelativePath());
        }
      }
    }
    it = DirectoryManager.getInstance().getDirectories().iterator();
    while (it.hasNext()) {
      final Directory dir = it.next();
      if (dir.getDevice().equals(dest)) {
        if (dir.getBooleanValue(Const.XML_DIRECTORY_SYNCHRONIZED)) {
          // don't take desynchronized dirs into account
          hsDestDirs.add(dir);
        } else {
          hsDesynchroPaths.add(dir.getRelativePath());
        }
      }
    }

    it = hsSourceDirs.iterator();
    Iterator<Directory> it2;
    // handle known extensions and image files
    final FileFilter filter = new JajukFileFilter(false, new JajukFileFilter[] {
        KnownTypeFilter.getInstance(), ImageFilter.getInstance() });
    while (it.hasNext()) {
      // give a chance to exit during sync
      if (ExitService.isExiting()) {
        return iNbCreatedFiles;
      }
      boolean bNeedCreate = true;
      final Directory dir = it.next();
      final String sPath = dir.getRelativePath();
      // check the directory on source is not desynchronized. If it
      // is, leave without checking files
      if (hsDesynchroPaths.contains(sPath)) {
        continue;
      }
      it2 = hsDestDirs.iterator();
      while (it2.hasNext()) {
        final Directory dir2 = it2.next();
        if (dir2.getRelativePath().equals(sPath)) {
          // directory already exists on this device
          bNeedCreate = false;
          break;
        }
      }
      // create it if needed
      final File fileNewDir = new File(new StringBuilder(dest.getUrl()).append(sPath).toString());
      if (bNeedCreate && !fileNewDir.mkdirs()) {
        Log.warn("Could not create directory " + fileNewDir);
      }
      // synchronize files
      final File fileSrc = new File(new StringBuilder(dSrc.getUrl()).append(sPath).toString());
      final File[] fSrcFiles = fileSrc.listFiles(filter);
      if (fSrcFiles != null) {
        for (final File element : fSrcFiles) {
          final File[] files = fileNewDir.listFiles(filter);
          if (files == null) {
            // fileNewDir is not a directory or an error occurred (
            // read/write right ? )
            continue;
          }
          boolean bNeedCopy = true;
          for (final File element2 : files) {
            if (element.getName().equalsIgnoreCase(element2.getName())) {
              bNeedCopy = false;
            }
          }
          if (bNeedCopy) {
            try {
              UtilSystem.copyToDir(element, fileNewDir);
              iNbCreatedFiles++;
              lVolume += element.length();
              InformationJPanel.getInstance().setMessage(
                  new StringBuilder(Messages.getString("Device.41")).append(dSrc.getName()).append(
                      ',').append(dest.getName()).append(Messages.getString("Device.42")).append(
                      element.getAbsolutePath()).append("]").toString(),
                  InformationJPanel.INFORMATIVE);
            } catch (final JajukException je) {
              Messages.showErrorMessage(je.getCode(), element.getAbsolutePath());
              Messages.showErrorMessage(27);
              Log.error(je);
              return iNbCreatedFiles;
            } catch (final Exception e) {
              Messages.showErrorMessage(20, element.getAbsolutePath());
              Messages.showErrorMessage(27);
              Log.error(20, "{{" + element.getAbsolutePath() + "}}", e);
              return iNbCreatedFiles;
            }
          }
        }
      }
    }
    return iNbCreatedFiles;
  }

  /**
   * Test device accessibility
   * 
   * @return true if the device is available
   */
  public boolean test() {
    UtilGUI.waiting(); // waiting cursor
    boolean bOK = false;
    try {
      // just wait a moment so user feels something real happens
      // (psychological)
      Thread.sleep(250);
    } catch (final InterruptedException e2) {
      Log.error(e2);
    }
    boolean bWasMounted = bMounted; // store mounted state of device before
    // mount test
    try {
      if (!bMounted) {
        mount(false); // try to mount
      }
    } catch (final Exception e) {
      UtilGUI.stopWaiting();
      return false;
    }
    if (getLongValue(Const.XML_TYPE) != 5) { // not a remote device
      final File file = new File(sUrl);
      if (file.exists() && file.canRead()) { // see if the url exists
        // and is readable
        // check if this device was void
        boolean bVoid = true;
        final Iterator<org.jajuk.base.File> it = FileManager.getInstance().getFiles().iterator();
        while (it.hasNext()) {
          final org.jajuk.base.File f = it.next();
          if (f.getDirectory().getDevice().equals(this)) {
            // at least one field in this device
            bVoid = false;
            break;
          }
        }
        if (!bVoid) { // if the device is not supposed to be void,
          // check if it is the case, if no, the device
          // must not be unix-mounted
          if (file.list().length > 0) {
            bOK = true;
          }
        } else { // device is void, OK we assume it is accessible
          bOK = true;
        }
      }
    } else {
      bOK = false; // TBI
    }
    // unmount the device if it was mounted only for the test
    if (!bWasMounted) {
      try {
        unmount(false, false);
      } catch (final Exception e1) {
        Log.error(e1);
      }
    }
    UtilGUI.stopWaiting();
    return bOK;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Device[ID=" + getID() + " Name=" + getName() + " Type="
        + DeviceManager.getInstance().getDeviceType(getLongValue(Const.XML_TYPE)) + " URL=" + sUrl
        + " Mount point=" + MOUNT_POINT + "]";
  }

  /**
   * Unmount the device
   * 
   */
  public void unmount() {
    unmount(false, true);
  }

  /**
   * Unmount the device with ejection
   * 
   * @param bEjection
   *          set whether the device must be ejected
   * @param bUIRefresh
   *          set wheter the UI should be refreshed
   */
  public void unmount(final boolean bEjection, final boolean bUIRefresh) {
    // look to see if the device is already mounted
    if (!bMounted) {
      Messages.showErrorMessage(125); // already unmounted
      return;
    }
    // ask fifo if it doens't use any track from this device
    if (!FIFO.canUnmount(this)) {
      Messages.showErrorMessage(121);
      return;
    }
    bMounted = false;
    if (bUIRefresh) {
      ObservationManager.notify(new Event(JajukEvents.DEVICE_UNMOUNT));
    }
  }

}
