/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision$
 */
package org.jajuk.base;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.helpers.ManualDeviceRefreshReporter;
import org.jajuk.ui.helpers.RefreshReporter;
import org.jajuk.ui.thumbnails.ThumbnailsMaker;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
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
 * Some properties of a device are immuatable : name, url and type *
 * <p>
 * Physical item.
 */
public class Device extends PhysicalItem implements Comparable<Device> {

  /** The Constant OPTION_REFRESH_DEEP.  DOCUMENT_ME */
  protected static final int OPTION_REFRESH_DEEP = 1;

  /** The Constant OPTION_REFRESH_CANCEL.  DOCUMENT_ME */
  protected static final int OPTION_REFRESH_CANCEL = 2;

  // Device type constants
  // Note: these need to correspond with the static array in @see
  // org.jajuk.base.DeviceManager !!
  /** The Constant TYPE_DIRECTORY.  DOCUMENT_ME */
  public static final int TYPE_DIRECTORY = 0;

  /** The Constant TYPE_CD.  DOCUMENT_ME */
  public static final int TYPE_CD = 1;

  /** The Constant TYPE_NETWORK_DRIVE.  DOCUMENT_ME */
  public static final int TYPE_NETWORK_DRIVE = 2;

  /** The Constant TYPE_EXT_DD.  DOCUMENT_ME */
  public static final int TYPE_EXT_DD = 3;

  /** The Constant TYPE_PLAYER.  DOCUMENT_ME */
  public static final int TYPE_PLAYER = 4;

  /** Device URL (performances). */
  private String sUrl;

  /** IO file for optimizations*. */
  private java.io.File fio;

  /** Mounted device flag. */
  private boolean bMounted = false;

  /** directories. */
  private final List<Directory> alDirectories = new ArrayList<Directory>(20);

  /** Already refreshing flag. */
  private volatile boolean bAlreadyRefreshing = false;

  /** Already synchronizing flag. */
  private volatile boolean bAlreadySynchronizing = false;

  /** Volume of created files during synchronization. */
  private long lVolume = 0;

  /** date last refresh. */
  private long lDateLastRefresh;

  /** Progress reporter *. */
  private RefreshReporter reporter;

  /** Refresh deepness choice *. */
  private int choice = Device.OPTION_REFRESH_DEEP;

  /**
   * Device constructor.
   * 
   * @param sId DOCUMENT_ME
   * @param sName DOCUMENT_ME
   */
  Device(final String sId, final String sName) {
    super(sId, sName);
  }

  /**
   * Adds the directory.
   * 
   * @param directory DOCUMENT_ME
   */
  public void addDirectory(final Directory directory) {
    alDirectories.add(directory);
  }

  /**
   * Scan directories to cleanup removed files and playlists.
   * 
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
    Log.debug("{{" + getName() + "}} Old file references cleaned in: "
        + ((l < 1000) ? l + " ms" : l / 1000 + " s, changes: " + bChanges));

    return bChanges;
  }

  /**
   * Walk through all Playlists and remove the ones for the current device.
   * 
   * @return true if there was any playlist removed
   */
  private boolean cleanPlaylist() {
    boolean bChanges = false;
    final List<Playlist> plfiles = PlaylistManager.getInstance().getPlaylists();
    for (final Playlist plf : plfiles) {
      if (!ExitService.isExiting() && plf.getDirectory().getDevice().equals(this) && plf.isReady()
          && !plf.getFIO().exists()) {
        PlaylistManager.getInstance().removeItem(plf);
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
    final List<org.jajuk.base.File> files = FileManager.getInstance().getFiles();
    for (final org.jajuk.base.File file : files) {
      if (!ExitService.isExiting() && file.getDirectory().getDevice().equals(this)
          && file.isReady() &&
          // Remove file if it doesn't exist any more or if it is a iTunes
          // file (useful for jajuk < 1.4)
          (!file.getFIO().exists() || file.getName().startsWith("._"))) {
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
    final List<Directory> dirs = DirectoryManager.getInstance().getDirectories();

    for (final Directory dir : dirs) {
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
   * Alphabetical comparator used to display ordered lists of devices.
   * 
   * @param otherDevice DOCUMENT_ME
   * 
   * @return comparison result
   */
  public int compareTo(final Device otherDevice) {
    // should handle null
    if (otherDevice == null) {
      return -1;
    }

    // We must be consistent with equals, see
    // http://java.sun.com/javase/6/docs/api/java/lang/Comparable.html
    int comp = getName().compareToIgnoreCase(otherDevice.getName());
    if (comp == 0) {
      return getName().compareTo(otherDevice.getName());
    } else {
      return comp;
    }
  }

  /**
   * Gets the date last refresh.
   * 
   * @return the date last refresh
   */
  public long getDateLastRefresh() {
    return lDateLastRefresh;
  }

  /**
   * Get item description.
   * 
   * @return the desc
   */
  @Override
  public String getDesc() {
    return Messages.getString("Item_Device") + " : " + getName();
  }

  /**
   * Gets the device type s.
   * 
   * @return the device type s
   */
  public String getDeviceTypeS() {
    return DeviceManager.getInstance().getDeviceType(getType());
  }

  /**
   * Gets the directories directly under the device root (not recursive)
   * 
   * @return the directories
   */
  public List<Directory> getDirectories() {
    return alDirectories;
  }

  /**
   * return ordered child files recursively.
   * 
   * @return child files recursively
   */
  public List<org.jajuk.base.File> getFilesRecursively() {
    // looks for the root directory for this device
    Directory dirRoot = getRootDirectory();
    if (dirRoot != null) {
      return dirRoot.getFilesRecursively();
    }

    // nothing found, return empty list
    return new ArrayList<org.jajuk.base.File>();
  }

  /**
   * Gets the fio.
   * 
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
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_DIRECTORY_MOUNTED_SMALL), IconLoader
          .getIcon(JajukIcons.DEVICE_DIRECTORY_UNMOUNTED_SMALL));
    case 1:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_CD_MOUNTED_SMALL), IconLoader
          .getIcon(JajukIcons.DEVICE_CD_UNMOUNTED_SMALL));
    case 2:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_NETWORK_DRIVE_MOUNTED_SMALL), IconLoader
          .getIcon(JajukIcons.DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL));
    case 3:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_EXT_DD_MOUNTED_SMALL), IconLoader
          .getIcon(JajukIcons.DEVICE_EXT_DD_UNMOUNTED_SMALL));
    case 4:
      return setIcon(IconLoader.getIcon(JajukIcons.DEVICE_PLAYER_MOUNTED_SMALL), IconLoader
          .getIcon(JajukIcons.DEVICE_PLAYER_UNMOUNTED_SMALL));
    default:
      Log.warn("Unknown type of device detected: " + getType());
      return null;
    }
  }

  /**
   * Sets the icon.
   * 
   * @param mountedIcon The icon to return for a mounted device
   * @param unmountedIcon The icon to return for an unmounted device
   * 
   * @return Returns either of the two provided icons depending on the state of
   * the device
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
   * Gets the root directory.
   * 
   * @return Associated root directory
   */
  public Directory getRootDirectory() {
    return DirectoryManager.getInstance().getDirectoryForIO(getFio(), this);
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public long getType() {
    return getLongValue(Const.XML_TYPE);
  }

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl() {
    return sUrl;
  }

  /**
   * Checks if is mounted.
   * 
   * @return true, if is mounted
   */
  public boolean isMounted() {
    return bMounted;
  }

  /**
   * Return true if the device can be accessed right now.
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
   * Tells if a device is refreshing.
   * 
   * @return true, if checks if is refreshing
   */
  public boolean isRefreshing() {
    return bAlreadyRefreshing;
  }

  /**
   * Tells if a device is synchronizing.
   * 
   * @return true, if checks if is synchronizing
   */
  public boolean isSynchronizing() {
    return bAlreadySynchronizing;
  }

  /**
   * Manual refresh.
   * 
   * @param bAsk DOCUMENT_ME
   * @param bAfterMove is this refresh done after a device location change ?
   * @param forcedDeep : override bAsk and force a deep refresh
   */
  private void manualRefresh(final boolean bAsk, final boolean bAfterMove, final boolean forcedDeep) {
    int i = 0;
    try {
      i = prepareRefresh(bAsk);
      if (i == OPTION_REFRESH_CANCEL) {
        return;
      }
      // Check that device is still available
      boolean readyToMount = checkDevice(true);
      if (!readyToMount) {
        return;
      }
      bAlreadyRefreshing = true;
    } catch (JajukException je) {
      Messages.showErrorMessage(je.getCode());
      Log.debug(je);
      return;
    }
    try {
      reporter = new ManualDeviceRefreshReporter(this);
      reporter.startup();
      // clean old files up (takes a while)
      if (!bAfterMove) {
        cleanRemovedFiles();
      }
      reporter.cleanupDone();

      // Actual refresh
      refreshCommand(((i == Device.OPTION_REFRESH_DEEP) || forcedDeep), true);
      // cleanup logical items
      org.jajuk.base.Collection.cleanupLogical();

      // if it is a move, clean old files *after* the refresh
      if (bAfterMove) {
        cleanRemovedFiles();
      }

      // notify views to refresh
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
      // Commit collection at each refresh (can be useful if
      // application
      // is closed brutally with control-C or shutdown and that
      // exit hook has no time to perform commit).
      // But don't commit when any device is refreshing to avoid collisions.
      if (!DeviceManager.getInstance().isAnyDeviceRefreshing()) {
        try {
          org.jajuk.base.Collection.commit(SessionService.getConfFileByPath(Const.FILE_COLLECTION));
        } catch (final IOException e) {
          Log.error(e);
        }
      }
    } finally {
      // Do not let current reporter as a manual reporter because it would fail
      // in NPE with auto-refresh
      reporter = null;
      // Make sure to unlock refreshing
      bAlreadyRefreshing = false;
    }
  }

  /**
   * Prepare manual refresh.
   * 
   * @param bAsk DOCUMENT_ME
   * 
   * @return the user choice (deep or fast)
   * 
   * @throws JajukException if user canceled, device cannot be refreshed or device already
   * refreshing
   */
  public int prepareRefresh(final boolean bAsk) throws JajukException {
    if (bAsk) {
      final Object[] possibleValues = { Messages.getString("FilesTreeView.60"),// fast
          Messages.getString("FilesTreeView.61"),// deep
          Messages.getString("Cancel") };// cancel
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            choice = JOptionPane.showOptionDialog(null, Messages.getString("FilesTreeView.59"),
                Messages.getString("Option"), JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, possibleValues, possibleValues[0]);
          }
        });
      } catch (Exception e) {
        Log.error(e);
        choice = Device.OPTION_REFRESH_CANCEL;
      }
      if (choice == Device.OPTION_REFRESH_CANCEL) { // Cancel
        return choice;
      }
    }
    final Device device = this;
    if (!device.isMounted()) {
      try {
        // Leave if user canceled device mounting
        if (!device.mount(true)) {
          return Device.OPTION_REFRESH_CANCEL;
        }
      } catch (final Exception e) {
        Log.error(11, getName(), e); // mount failed
        Messages.showErrorMessage(11, getName());
        throw new JajukException(11);
      }
    }
    if (bAlreadyRefreshing) {
      throw new JajukException(107);
    }
    return choice;
  }

  /**
   * Check that the device is available and not void
   * @return whether the device is ready for mounting
   */
  private boolean checkDevice(boolean bManual) throws JajukException {
    try {
      final File file = new File(getUrl());
      if (!file.exists()) {
        throw new Exception("Path does not exist: " + file.toString());
      }
    } catch (final Exception e) {
      throw new JajukException(11, getName(), e);
    }
    /*
     * Cannot mount void devices because of reference garbager thread ( a refresh would clear the
     * device)
     */
    final File file = new File(getUrl());
    if ((file.listFiles() == null) || (file.listFiles().length == 0)) {
      if (bManual) {
        final int answer = Messages.getChoice("[" + getName() + "] "
            + Messages.getString("Confirmation_void_refresh"), JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
        // leave if user doesn't confirm to mount the void device
        return (answer == JOptionPane.YES_OPTION);
      } else {
        // In auto mode, never mount a void device
        return false;
      }
    } else {
      // Device is not void
      return true;
    }
  }

  /**
   * Mount the device.
   * 
   * @param bManual set whether mount is manual or auto
   * @return whether the device has been mounted
   * @throws Exception if device cannot be mounted
   */
  public boolean mount(final boolean bManual) throws Exception {
    if (bMounted) {
      throw new JajukException(111);
    }
    // Check if we can mount the device. It can throw a JajukException if void or unavailable
    // device
    boolean readyToMount = checkDevice(bManual);
    // Effective mounting if available.
    if (readyToMount) {
      bMounted = true;
    }
    // notify views to refresh if needed
    ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_MOUNT));

    return bMounted;
  }

  /**
   * Set all personal properties of an XML file for an item (doesn't overwrite
   * existing properties for perfs).
   * 
   * @param attributes :
   * list of attributes for this XML item
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
   * Refresh : scan the device to find tracks. 
   * This method is only called from GUI. auto-refresh uses refreshCommand() directly.
   * 
   * @param bAsynchronous :
   * set asynchronous or synchronous mode
   * @param bAsk DOCUMENT_ME
   * @param bAfterMove DOCUMENT_ME
   */
  public void refresh(final boolean bAsynchronous, final boolean bAsk, final boolean bAfterMove) {
    if (bAsynchronous) {
      final Thread t = new Thread("Device Refresh Thread for : " + name) {
        @Override
        public void run() {
          manualRefresh(bAsk, bAfterMove, false);
        }
      };
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    } else {
      manualRefresh(bAsk, bAfterMove, false);
    }
  }

  /**
   * Deep Refresh with GUI 
   * 
   */
  public void manualRefreshDeep() {
    final Thread t = new Thread("Device Deep Refresh Thread for : " + name) {
      @Override
      public void run() {
        manualRefresh(false, false, true);
      }
    };
    t.setPriority(Thread.MIN_PRIORITY);
    t.start();
  }

  /**
   * The refresh itself.
   * 
   * @param bDeepScan whether it is a deep refresh request or only fast
   * @param bManual whether it is a manual refresh or auto
   * 
   * @return true if some changes occurred in device
   */
  public synchronized boolean refreshCommand(final boolean bDeepScan, final boolean bManual) {
    try {
      // Check if this device is mounted (useful when called by
      // automatic refresh)
      if (!isMounted()) {
        return false;
      }

      // Check that device is still available
      boolean readyToMount = checkDevice(bManual);
      if (!readyToMount) {
        return false;
      }

      bAlreadyRefreshing = true;
      // reporter is already set in case of manual refresh
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
      int iNbFilesBeforeRefresh = FileManager.getInstance().getElementCount();
      int iNbDirsBeforeRefresh = DirectoryManager.getInstance().getElementCount();
      int iNbPlaylistsBeforeRefresh = PlaylistManager.getInstance().getElementCount();

      if (bDeepScan && Log.isDebugEnabled()) {
        Log.debug("Starting refresh of device : " + this);
      }
      // Create a directory for device itself and scan files to allow
      // files at the root of the device
      final Directory top = DirectoryManager.getInstance().registerDirectory(this);
      if (!getDirectories().contains(top)) {
        addDirectory(top);
      }

      // Start actual scan
      scanRecursively(top, bDeepScan);

      // Force cover detection (after done once, the cover file is cached as album property)
      // We need this to avoid bug #1550 : if the device is created, then unplugged, catalog
      // view cover/nocover filter is doomed because the findCover() method always return null
      for (Album album : AlbumManager.getInstance().getAlbums()) {
        album.findCoverFile();
      }

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
      reporter.done();
      // Reset the reporter as next time, it could be another type
      reporter = null;
    }
  }

  /**
   * Scan recursively.
   * DOCUMENT_ME
   * 
   * @param dir DOCUMENT_ME
   * @param bDeepScan DOCUMENT_ME
   */
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
   * Sets the url.
   * 
   * @param url The sUrl to set.
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
   * Synchroning asynchronously.
   * 
   * @param bAsynchronous :
   * set asynchronous or synchronous mode
   */
  public void synchronize(final boolean bAsynchronous) {
    // Check a source device is defined
    if (StringUtils.isBlank((String) getValue(Const.XML_DEVICE_SYNCHRO_SOURCE))) {
      Messages.showErrorMessage(171);
      return;
    }
    final Device device = this;
    if (!device.isMounted()) {
      try {
        device.mount(true);
      } catch (final Exception e) {
        Log.error(11, getName(), e); // mount failed
        Messages.showErrorMessage(11, getName());
        return;
      }
    }
    if (bAsynchronous) {
      final Thread t = new Thread("Device Synchronize Thread") {
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
   * Synchronize action itself.
   */
  public void synchronizeCommand() {
    try {
      bAlreadySynchronizing = true;
      long lTime = System.currentTimeMillis();
      int iNbCreatedFilesDest = 0;
      int iNbCreatedFilesSrc = 0;
      lVolume = 0;
      final boolean bidi = getValue(Const.XML_DEVICE_SYNCHRO_MODE).equals(
          Const.DEVICE_SYNCHRO_MODE_BI);
      // check this device is synchronized
      final String sIdSrc = (String) getValue(Const.XML_DEVICE_SYNCHRO_SOURCE);
      if (StringUtils.isBlank(sIdSrc) || sIdSrc.equals(getID())) {
        // cannot synchro with itself
        return;
      }
      final Device dSrc = DeviceManager.getInstance().getDeviceByID(sIdSrc);
      // perform a fast refresh
      refreshCommand(false, true);
      // if bidi sync, refresh the other device as well (new file can
      // have been copied to it)
      if (bidi) {
        dSrc.refreshCommand(false, true);
      }
      // start message
      InformationJPanel.getInstance().setMessage(
          new StringBuilder(Messages.getString("Device.31")).append(dSrc.getName()).append(',')
              .append(getName()).append("]").toString(), InformationJPanel.INFORMATIVE);
      // in both cases (bi or uni-directional), make an unidirectional
      // sync from source device to this one
      iNbCreatedFilesDest = synchronizeUnidirectonal(dSrc, this);
      // now the other one if bidi
      if (bidi) {
        iNbCreatedFilesDest += synchronizeUnidirectonal(this, dSrc);
      }
      // end message
      lTime = System.currentTimeMillis() - lTime;
      final String sOut = new StringBuilder(Messages.getString("Device.33")).append(
          ((lTime < 1000) ? lTime + " ms" : lTime / 1000 + " s")).append(" - ").append(
          iNbCreatedFilesSrc + iNbCreatedFilesDest).append(Messages.getString("Device.35")).append(
          lVolume / 1048576).append(Messages.getString("Device.36")).toString();
      // perform a fast refresh
      refreshCommand(false, true);
      // if bidi sync, refresh the other device as well (new file can
      // have been copied to it)
      if (bidi) {
        dSrc.refreshCommand(false, true);
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
      // Refresh GUI
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    }
  }

  /**
   * Synchronize a device with another one (unidirectional).
   * 
   * @param dSrc DOCUMENT_ME
   * @param dest DOCUMENT_ME
   * 
   * @return nb of created files
   */
  private int synchronizeUnidirectonal(final Device dSrc, final Device dest) {
    final Set<Directory> hsSourceDirs = new HashSet<Directory>(100);
    // contains paths ( relative to device) of desynchronized dirs
    final Set<String> hsDesynchroPaths = new HashSet<String>(10);
    final Set<Directory> hsDestDirs = new HashSet<Directory>(100);
    int iNbCreatedFiles = 0;
    List<Directory> dirs = DirectoryManager.getInstance().getDirectories();
    for (Directory dir : dirs) {
      if (dir.getDevice().equals(dSrc)) {
        // don't take desynchronized dirs into account
        if (dir.getBooleanValue(Const.XML_DIRECTORY_SYNCHRONIZED)) {
          hsSourceDirs.add(dir);
        } else {
          hsDesynchroPaths.add(dir.getRelativePath());
        }
      }
    }
    for (Directory dir : dirs) {
      if (dir.getDevice().equals(dest)) {
        if (dir.getBooleanValue(Const.XML_DIRECTORY_SYNCHRONIZED)) {
          // don't take desynchronized dirs into account
          hsDestDirs.add(dir);
        } else {
          hsDesynchroPaths.add(dir.getRelativePath());
        }
      }
    }
    // handle known extensions and image files
    final FileFilter filter = new JajukFileFilter(false, new JajukFileFilter[] {
        KnownTypeFilter.getInstance(), ImageFilter.getInstance() });
    for (Directory dir : hsSourceDirs) {
      // give a chance to exit during sync
      if (ExitService.isExiting()) {
        return iNbCreatedFiles;
      }
      boolean bNeedCreate = true;
      final String sPath = dir.getRelativePath();
      // check the directory on source is not desynchronized. If it
      // is, leave without checking files
      if (hsDesynchroPaths.contains(sPath)) {
        continue;
      }
      for (Directory dir2 : hsDestDirs) {
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
          File[] filesArray = fileNewDir.listFiles(filter);
          if (filesArray == null) {
            // fileNewDir is not a directory or an error occurred (
            // read/write right ? )
            continue;
          }
          final List<File> files = Arrays.asList(filesArray);
          // Sort so files are copied in the filesystem order
          Collections.sort(files);
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
   * Test device accessibility.
   * 
   * @return true if the device is available
   */
  public boolean test() {
    UtilGUI.waiting(); // waiting cursor
    boolean bOK = false;
    boolean bWasMounted = bMounted; // store mounted state of device before
    // mount test
    try {
      if (!bMounted) {
        mount(true);
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
        ReadOnlyIterator<org.jajuk.base.File> it = FileManager.getInstance().getFilesIterator();
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
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return "Device[ID=" + getID() + " Name=" + getName() + " Type="
        + DeviceManager.getInstance().getDeviceType(getLongValue(Const.XML_TYPE)) + " URL=" + sUrl
        + "]";
  }

  /**
   * Unmount the device.
   */
  public void unmount() {
    unmount(false, true);
  }

  /**
   * Unmount the device with ejection.
   * 
   * @param bEjection set whether the device must be ejected
   * @param bUIRefresh set wheter the UI should be refreshed
   */
  public void unmount(final boolean bEjection, final boolean bUIRefresh) {
    // look to see if the device is already mounted
    if (!bMounted) {
      Messages.showErrorMessage(125); // already unmounted
      return;
    }
    // ask fifo if it doens't use any track from this device
    if (!QueueModel.canUnmount(this)) {
      Messages.showErrorMessage(121);
      return;
    }
    bMounted = false;
    if (bUIRefresh) {
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_UNMOUNT));
    }
  }

}
