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
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.thumbnails.ThumbnailsMaker;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Messages;
import org.jajuk.util.RefreshReporter;
import org.jajuk.util.Util;
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
public class Device extends PhysicalItem implements ITechnicalStrings, Comparable {

  private static final long serialVersionUID = 1L;

  private static final int OPTION_REFRESH_DEEP = 1;

  private static final int OPTION_REFRESH_CANCEL = 2;

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
  private final String sMountPoint = "";

  /** Mounted device flag */
  private boolean bMounted = false;

  /** directories */
  private final ArrayList<Directory> alDirectories = new ArrayList<Directory>(20);

  /** Already refreshing flag */
  private volatile boolean bAlreadyRefreshing = false;

  /** Already synchronizing flag */
  private volatile boolean bAlreadySynchronizing = false;

  /** Number of files in this device before refresh ( for refresh stats ) */
  public int iNbFilesBeforeRefresh;

  /** Number of dirs in this device before refresh */
  public int iNbDirsBeforeRefresh;

  /** Number of created files on source device during synchro ( for stats ) */
  public int iNbCreatedFilesSrc;

  /**
   * Number of created files on destination device during synchro ( for stats )
   */
  public int iNbCreatedFilesDest;

  /** Number of deleted files during a synchro ( for stats ) */
  int iNbDeletedFiles = 0;

  /** Volume of created files during synchro */
  long lVolume = 0;

  /** date last refresh */
  long lDateLastRefresh;

  RefreshReporter reporter;

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
   * Scan directories to cleanup removed files and playlist files
   * 
   * @param device
   *          device to cleanup
   * @return whether some items have been removed
   */
  public boolean cleanRemovedFiles() {
    boolean bChanges = false;
    long l = System.currentTimeMillis();
    // need to use a shallow copy to avoid concurrent exceptions
    final Set<Directory> dirs = DirectoryManager.getInstance().getDirectories();
    // directories cleanup
    for (final Item item : dirs) {
      final Directory dir = (Directory) item;
      if (!Main.isExiting() && dir.getDevice().equals(this) && dir.getDevice().isMounted()) {
        if (!dir.getFio().exists()) {
          // note that associated files are removed too
          DirectoryManager.getInstance().removeDirectory(dir.getID());
          Log.debug("Removed: " + dir);
          bChanges = true;
        }
      }
    }
    // files cleanup
    final Set<org.jajuk.base.File> files = FileManager.getInstance().getFiles();
    for (final org.jajuk.base.File file : files) {
      if (!Main.isExiting() && file.getDirectory().getDevice().equals(this) && file.isReady()) {
        // Remove file if it doesn't exist any more or if it is a iTunes
        // file (useful for jajuk < 1.4)
        if (!file.getIO().exists() || file.getName().startsWith("._")) {
          FileManager.getInstance().removeFile(file);
          Log.debug("Removed: " + file);
          bChanges = true;
        }
      }
    }
    // Playlist cleanup
    final Set<PlaylistFile> plfiles = PlaylistFileManager.getInstance().getPlaylistFiles();
    for (final PlaylistFile plf : plfiles) {
      if (!Main.isExiting() && plf.getDirectory().getDevice().equals(this) && plf.isReady()) {
        if (!plf.getFio().exists()) {
          PlaylistFileManager.getInstance().removePlaylistFile(plf);
          Log.debug("Removed: " + plf);
          bChanges = true;
        }
      }
    }
    // clear history to remove old files referenced in it
    if (ConfigurationManager.getProperty(ITechnicalStrings.CONF_HISTORY) != null) {
      History.getInstance().clear(
          Integer.parseInt(ConfigurationManager.getProperty(ITechnicalStrings.CONF_HISTORY)));
    }
    // delete old history items
    l = System.currentTimeMillis() - l;
    Log.debug("Old file references cleaned in: " + ((l < 1000) ? l + " ms" : l / 1000 + " s"));
    return bChanges;
  }

  /**
   * Alphabetical comparator used to display ordered lists of devices
   * 
   * @param other
   *          device to be compared
   * @return comparaison result
   */
  public int compareTo(final Object o) {
    final Device otherDevice = (Device) o;
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
    return DeviceManager.getInstance().getDeviceType(getLongValue(ITechnicalStrings.XML_TYPE));
  }

  /**
   * @return
   */
  public ArrayList<Directory> getDirectories() {
    return alDirectories;
  }

  /**
   * return child files recursively
   * 
   * @return child files recursively
   */
  public ArrayList<org.jajuk.base.File> getFilesRecursively() {
    // looks for the root directory for this device
    Directory dirRoot = null;
    final Collection dirs = DirectoryManager.getInstance().getDirectories();
    final Iterator it = dirs.iterator();
    while (it.hasNext()) {
      final Directory dir = (Directory) it.next();
      if (dir.getDevice().equals(this) && dir.getFio().equals(fio)) {
        dirRoot = dir;
      }
    }
    ArrayList<org.jajuk.base.File> alFiles = new ArrayList<org.jajuk.base.File>(100);
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
    if (ITechnicalStrings.XML_TYPE.equals(sKey)) {
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
    ImageIcon icon = null;
    switch ((int) getType()) {
    case 0:
      if (isMounted()) {
        icon = IconLoader.ICON_DEVICE_DIRECTORY_MOUNTED_SMALL;
      } else {
        icon = IconLoader.ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL;
      }
      break;
    case 1:
      if (isMounted()) {
        icon = IconLoader.ICON_DEVICE_CD_MOUNTED_SMALL;
      } else {
        icon = IconLoader.ICON_DEVICE_CD_UNMOUNTED_SMALL;
      }
      break;
    case 2:
      if (isMounted()) {
        icon = IconLoader.ICON_DEVICE_NETWORK_DRIVE_MOUNTED_SMALL;
      } else {
        icon = IconLoader.ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL;
      }
      break;
    case 3:
      if (isMounted()) {
        icon = IconLoader.ICON_DEVICE_EXT_DD_MOUNTED_SMALL;
      } else {
        icon = IconLoader.ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL;
      }
      break;
    case 4:
      if (isMounted()) {
        icon = IconLoader.ICON_DEVICE_PLAYER_MOUNTED_SMALL;
      } else {
        icon = IconLoader.ICON_DEVICE_PLAYER_UNMOUNTED_SMALL;
      }
      break;
    }
    return icon;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return ITechnicalStrings.XML_DEVICE;
  }

  /**
   * @return Returns the unix mount point.
   */
  public String getMountPoint() {
    return sMountPoint;
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
    return getLongValue(ITechnicalStrings.XML_TYPE);
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
      int i = Device.OPTION_REFRESH_DEEP;
      if (bAsk) {
        final Object[] possibleValues = { Messages.getString("FilesTreeView.60"),// fast
            Messages.getString("FilesTreeView.61"),// deep
            Messages.getString("Cancel") };// cancel
        i = JOptionPane.showOptionDialog(null, Messages.getString("FilesTreeView.59"), Messages
            .getString("Option"), JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
            possibleValues, possibleValues[0]);
        if (i == Device.OPTION_REFRESH_CANCEL) { // Cancel
          return;
        }
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
      if (bAlreadyRefreshing) {
        Messages.showErrorMessage(107);
        return;
      }
      bAlreadyRefreshing = true;
      reporter.startup();
      // clean old files up (takes a while)
      cleanRemovedFiles();
      reporter.cleanupDone();
      // Actual refresh
      refreshCommand((i == Device.OPTION_REFRESH_DEEP), true);
      // notify views to refresh
      ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
      // cleanup logical items
      TrackManager.getInstance().cleanup();
      StyleManager.getInstance().cleanup();
      AlbumManager.getInstance().cleanup();
      AuthorManager.getInstance().cleanup();
      PlaylistManager.getInstance().cleanup();
      // commit collection at each refresh (can be useful if application
      // is closed brutally with control-C or shutdown and that exit hook
      // have no time to perform commit)
      try {
        org.jajuk.base.Collection.commit(Util.getConfFileByPath(ITechnicalStrings.FILE_COLLECTION));
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
    // Here the device is conciderated as mounted
    bMounted = true;
    // notify views to refresh if needed
    if (bUIRefresh) {
      ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_MOUNT));
    }
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
        if (meta.getName().equals(ITechnicalStrings.XML_DEVICE_AUTO_REFRESH)
            && (sValue.equalsIgnoreCase(ITechnicalStrings.TRUE) || sValue
                .equalsIgnoreCase(ITechnicalStrings.FALSE))) {
          switch ((int) getType()) {
          case TYPE_DIRECTORY: // directory
            sValue = "0.5d"; //$NON-NLS-1$
            break;
          case TYPE_CD: // file cd
            sValue = "0d"; //$NON-NLS-1$
            break;
          case TYPE_NETWORK_DRIVE: // network drive
            sValue = "0d"; //$NON-NLS-1$
            break;
          case TYPE_EXT_DD: // ext dd
            sValue = "3d"; //$NON-NLS-1$
            break;
          case TYPE_PLAYER: // player
            sValue = "3d"; //$NON-NLS-1$
            break;
          }
        }
        try {
          setProperty(sProperty, Util.parse(sValue, meta.getType()));
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
   *          set asynchonous or synchronous mode
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
   *          set asynchonous or synchronous mode
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
   * @return true if some changes occured in device
   * 
   */
  public synchronized boolean refreshCommand(final boolean bDeepScan, final boolean bManual) {
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
      if (Main.bExiting) {
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

      if (bDeepScan && Log.isDebugEnabled()) {
        Log.debug("Starting refresh of device : " + this);
      }
      final File fTop = new File(getStringValue(ITechnicalStrings.XML_URL));
      if (!fTop.exists()) {
        Messages.showErrorMessage(101);
        return false;
      }
      // Create a directory for device itself and scan files to allow
      // files at the root of the device
      final Directory top = DirectoryManager.getInstance().registerDirectory(this);
      // Start actual scan
      scanRecursively(top, bDeepScan);
      // refresh required if nb of files or dirs changed
      if (((FileManager.getInstance().getElementCount() - iNbFilesBeforeRefresh) != 0)
          || ((DirectoryManager.getInstance().getElementCount() - iNbDirsBeforeRefresh) != 0)) {
        // Refresh thumbs for new albums
        ThumbnailsMaker.launchAllSizes(false);
        return true;
      }
      return false;
    } catch (final RuntimeException re) { // runtime error are thrown
      throw re;
    } catch (final Exception e) { // and regular ones logged
      Log.error(e);
      return false;
    } finally { // make sure to unlock refreshing even if an error
      // occured
      bAlreadyRefreshing = false;
      // Notify the reporter of the actual refresh startup
      reporter.refreshDone();
    }
  }

  private void scanRecursively(final Directory dir, final boolean bDeepScan) {
    System.out.println("Scanning :"+dir);
    dir.scan(bDeepScan, reporter);
    if (reporter != null) {
      reporter.updateState(dir);
    }
    final File[] files = dir.getFio().listFiles(Util.dirFilter);
    if (files != null) {
      for (final File element : files) {
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
    setProperty(ITechnicalStrings.XML_URL, url);
    fio = new File(url);
    /** Reset files */
    for (final org.jajuk.base.File file : FileManager.getInstance().getFiles()) {
      file.reset();
    }
    /** Reset playlist files */
    for (final PlaylistFile plf : PlaylistFileManager.getInstance().getPlaylistFiles()) {
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
    if (Util.isVoid((String) getValue(ITechnicalStrings.XML_DEVICE_SYNCHRO_SOURCE))) {
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
      final boolean bidi = (getValue(ITechnicalStrings.XML_DEVICE_SYNCHRO_MODE)
          .equals(ITechnicalStrings.DEVICE_SYNCHRO_MODE_BI));
      // check this device is synchronized
      final String sIdSrc = (String) getValue(ITechnicalStrings.XML_DEVICE_SYNCHRO_SOURCE);
      if ((sIdSrc == null) || sIdSrc.equals(getID())) {
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
      iNbCreatedFilesDest += synchronizeUnidirectonal(this, dSrc);
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
    } catch (final RuntimeException re) { // runtime error are thrown
      throw re;
    } catch (final Exception e) { // and regular ones logged
      Log.error(e);
    } finally {
      // make sure to unlock sychronizing even if an error occured
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
    Iterator it = null;
    final HashSet<Directory> hsSourceDirs = new HashSet<Directory>(100);
    // contains paths ( relative to device) of desynchronized dirs
    final HashSet<String> hsDesynchroPaths = new HashSet<String>(10);
    final HashSet<Directory> hsDestDirs = new HashSet<Directory>(100);
    int iNbCreatedFiles = 0;
    it = DirectoryManager.getInstance().getDirectories().iterator();
    while (it.hasNext()) {
      final Directory dir = (Directory) it.next();
      if (dir.getDevice().equals(dSrc)) {
        // don't take desynchronized dirs into account
        if (dir.getBooleanValue(ITechnicalStrings.XML_DIRECTORY_SYNCHRONIZED)) {
          hsSourceDirs.add(dir);
        } else {
          hsDesynchroPaths.add(dir.getRelativePath());
        }
      }
    }
    it = DirectoryManager.getInstance().getDirectories().iterator();
    while (it.hasNext()) {
      final Directory dir = (Directory) it.next();
      if (dir.getDevice().equals(dest)) {
        if (dir.getBooleanValue(ITechnicalStrings.XML_DIRECTORY_SYNCHRONIZED)) {
          // don't take desynchronized dirs into account
          hsDestDirs.add(dir);
        } else {
          hsDesynchroPaths.add(dir.getRelativePath());
        }
      }
    }

    it = hsSourceDirs.iterator();
    Iterator it2;
    // handle known extensions and image files
    final FileFilter filter = new JajukFileFilter(false, new JajukFileFilter[] {
        KnownTypeFilter.getInstance(), ImageFilter.getInstance() });
    while (it.hasNext()) {
      // give a chance to exit during sync
      if (Main.isExiting()) {
        return iNbCreatedFiles;
      }
      boolean bNeedCreate = true;
      final Directory dir = (Directory) it.next();
      final String sPath = dir.getRelativePath();
      // check the directory on source is not desynchronized. If it
      // is, leave without checking files
      if (hsDesynchroPaths.contains(sPath)) {
        continue;
      }
      it2 = hsDestDirs.iterator();
      while (it2.hasNext()) {
        final Directory dir2 = (Directory) it2.next();
        if (dir2.getRelativePath().equals(sPath)) {
          // directory already exists on this device
          bNeedCreate = false;
          break;
        }
      }
      // create it if needed
      final File fileNewDir = new File(new StringBuilder(dest.getUrl()).append(sPath).toString());
      if (bNeedCreate) {
        fileNewDir.mkdirs();
      }
      // synchronize files
      final File fileSrc = new File(new StringBuilder(dSrc.getUrl()).append(sPath).toString());
      final File[] fSrcFiles = fileSrc.listFiles(filter);
      if (fSrcFiles != null) {
        for (final File element : fSrcFiles) {
          final File[] files = fileNewDir.listFiles(filter);
          if (files == null) {
            // fileNewDir is not a directory or an error occured (
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
              Util.copyToDir(element, fileNewDir);
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
    Util.waiting(); // waiting cursor
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
      Util.stopWaiting();
      return false;
    }
    if (getLongValue(ITechnicalStrings.XML_TYPE) != 5) { // not a remote device
      final File file = new File(sUrl);
      if (file.exists() && file.canRead()) { // see if the url exists
        // and is readable
        // check if this device was void
        boolean bVoid = true;
        final Iterator it = FileManager.getInstance().getFiles().iterator();
        while (it.hasNext()) {
          final org.jajuk.base.File f = (org.jajuk.base.File) it.next();
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
    Util.stopWaiting();
    return bOK;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Device[ID=" + getID() + " Name=" + getName() + " Type="
        + DeviceManager.getInstance().getDeviceType(getLongValue(ITechnicalStrings.XML_TYPE))
        + " URL=" + sUrl + " Mount point=" + sMountPoint + "]";
  }

  /**
   * Unmount the device
   * 
   */
  public void unmount() throws Exception {
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
  public void unmount(final boolean bEjection, final boolean bUIRefresh) throws Exception {
    // look to see if the device is already mounted ( the unix 'mount'
    // command cannot say that )
    new File(getMountPoint());
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
      ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_UNMOUNT));
    }
  }
}
