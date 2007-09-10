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
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.RefreshReporter;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
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

	/** Device URL (used for perfs) */
	private String sUrl;

	/** IO file for optimizations* */
	private java.io.File fio;

	/** Device mount point* */
	private String sMountPoint = "";

	/** Mounted device flag */
	private boolean bMounted = false;

	/** directories */
	private ArrayList<Directory> alDirectories = new ArrayList<Directory>(20);

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

	/** Number of directories computes from reading the filesystem */
	private int dirTotal = 0;

	/** date last refresh */
	long lDateLastRefresh;

	// Refresh Options
	private static final int OPTION_REFRESH_FAST = 0;

	private static final int OPTION_REFRESH_DEEP = 1;

	private static final int OPTION_REFRESH_CANCEL = 2;

	// Device type constants
	public static final int TYPE_DIRECTORY = 0;

	public static final int TYPE_CD = 1;

	public static final int TYPE_NETWORK_DRIVE = 2;

	public static final int TYPE_EXT_DD = 3;

	public static final int TYPE_PLAYER = 4;

	private int dirCount = 0;

	RefreshReporter reporter;

	/**
	 * Device constructor
	 * 
	 * @param sId
	 * @param sName
	 * @param iDeviceType
	 * @param sUrl
	 */
	public Device(String sId, String sName) {
		super(sId, sName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIdentifier()
	 */
	final public String getLabel() {
		return XML_DEVICE;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Device[ID=" + sId + " Name=" + sName + " Type="
				+ DeviceManager.getInstance().getDeviceType(getLongValue(XML_TYPE)) + " URL="
				+ sUrl + " Mount point=" + sMountPoint + "]";
	}

	/**
	 * Refresh : scan asynchronously the device to find tracks
	 * 
	 * @param bAsynchronous :
	 *            set asynchonous or synchronous mode
	 * @param bAsk:
	 *            should we ask user if he wants to perform a deep or fast scan?
	 *            default=deep
	 */
	public void refresh(boolean bAsynchronous) {
		refresh(bAsynchronous, false);
	}

	/**
	 * Refresh : scan asynchronously the device to find tracks
	 * 
	 * @param bAsynchronous :
	 *            set asynchonous or synchronous mode
	 * @param bAsk:
	 *            should we ask user if he wants to perform a deep or fast scan?
	 *            default=deep
	 */
	public void refresh(boolean bAsynchronous, final boolean bAsk) {
		if (bAsynchronous) {
			Thread t = new Thread() {
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
	 * Manual refresh
	 * 
	 * @param bAsk:
	 *            Should we ask user if a deep or fast scan is required?
	 *            default=deep
	 */
	private void manualRefresh(boolean bAsk) {
		try {
			reporter = new RefreshReporter(this);
			int i = OPTION_REFRESH_DEEP;
			if (bAsk) {
				Object[] possibleValues = { Messages.getString("FilesTreeView.60"),// fast
						Messages.getString("FilesTreeView.61"),// deep
						Messages.getString("Cancel") };// cancel
				i = JOptionPane.showOptionDialog(null, Messages.getString("FilesTreeView.59"),
						Messages.getString("Option"), JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, possibleValues, possibleValues[0]);
				if (i == OPTION_REFRESH_CANCEL) { // Cancel
					return;
				}
			}
			final Device device = this;
			if (!device.isMounted()) {
				try {
					device.mount();
				} catch (Exception e) {
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
			refreshCommand((i == OPTION_REFRESH_DEEP), true);
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
				org.jajuk.base.Collection.commit(Util.getConfFileByPath(FILE_COLLECTION));
			} catch (IOException e) {
				Log.error(e);
			}
			reporter.done();
		} finally {
			//Make sure to unlock refreshing
			bAlreadyRefreshing = false;
		}
	}

	/**
	 * The refresh itself
	 * 
	 * @return true if some changes occured in device
	 * 
	 */
	protected synchronized boolean refreshCommand(boolean bDeepScan, boolean bManual) {
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
			File file = new File(getUrl());
			if (file.exists() && (file.list() == null || file.list().length == 0)) {
				int i = Messages.getChoice(Messages.getString("Confirmation_void_refresh"),
						JOptionPane.WARNING_MESSAGE);
				if (i != JOptionPane.OK_OPTION) {
					return false;
				}
			}
			iNbFilesBeforeRefresh = FileManager.getInstance().getElementCount();
			iNbDirsBeforeRefresh = DirectoryManager.getInstance().getElementCount();

			if (bDeepScan && Log.isDebugEnabled()) {
				Log.debug("Starting refresh of device : " + this);
			}
			File fTop = new File(getStringValue(XML_URL));
			if (!fTop.exists()) {
				Messages.showErrorMessage(101);
				return false;
			}
			// Create a directory for device itself and scan files to allow
			// files at the root of the device
			Directory top = DirectoryManager.getInstance().registerDirectory(this);
			// Start actual scan
			scanRecursively(top, bDeepScan);
			// refresh required if nb of files or dirs changed
			if ((FileManager.getInstance().getElementCount() - iNbFilesBeforeRefresh) != 0
					|| (DirectoryManager.getInstance().getElementCount() - iNbDirsBeforeRefresh) != 0) {
				return true;
			}
			return false;
		} catch (RuntimeException re) { // runtime error are thrown
			throw re;
		} catch (Exception e) { // and regular ones logged
			Log.error(e);
			return false;
		} finally { // make sure to unlock refreshing even if an error
			// occured
			bAlreadyRefreshing = false;
			// Notify the reporter of the actual refresh startup
			reporter.refreshDone();
		}
	}

	private void scanRecursively(Directory dir, boolean bDeepScan) {
		dir.scan(bDeepScan, reporter);
		if (reporter != null) {
			reporter.updateState(dir);
		}
		File[] files = dir.getFio().listFiles(Util.dirFilter);
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				Directory subDir = DirectoryManager.getInstance().registerDirectory(
						files[i].getName(), dir, this);
				scanRecursively(subDir, bDeepScan);
			}
		}
	}

	/**
	 * Synchroning asynchronously
	 * 
	 * @param bAsynchronous :
	 *            set asynchronous or synchronous mode
	 * @return
	 */
	public void synchronize(boolean bAsynchronous) {
		// Check a source device is defined
		if (Util.isVoid((String) getValue(XML_DEVICE_SYNCHRO_SOURCE))) {
			Messages.showErrorMessage(171);
			return;
		}
		final Device device = this;
		if (!device.isMounted()) {
			try {
				device.mount();
			} catch (Exception e) {
				Log.error(11, "{{" + getName() + "}}", e); // mount failed
				Messages.showErrorMessage(011, getName());
				return;
			}
		}
		if (bAsynchronous) {
			Thread t = new Thread() {
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
	 *            device to synchronize
	 */
	public void synchronizeCommand() {
		try {
			bAlreadySynchronizing = true;
			long lTime = System.currentTimeMillis();
			iNbCreatedFilesDest = 0;
			iNbCreatedFilesSrc = 0;
			iNbDeletedFiles = 0;
			lVolume = 0;
			boolean bidi = (getValue(XML_DEVICE_SYNCHRO_MODE).equals(DEVICE_SYNCHRO_MODE_BI));
			// check this device is synchronized
			String sIdSrc = (String) getValue(XML_DEVICE_SYNCHRO_SOURCE);
			if (sIdSrc == null || sIdSrc.equals(getId())) {
				// cannot synchro with itself
				return;
			}
			Device dSrc = DeviceManager.getInstance().getDeviceByID(sIdSrc);
			// perform a fast refresh
			this.refreshCommand(false, true);
			// if bidi sync, refresh the other device as well (new file can
			// have been copied to it)
			if (bidi) {
				dSrc.refreshCommand(false, true);
			}
			// start message
			InformationJPanel.getInstance().setMessage(
					new StringBuffer(Messages.getString("Device.31")).append(dSrc.getName())
							.append(',').append(this.getName()).append("]").toString(),
					InformationJPanel.INFORMATIVE);
			// in both cases (bi or uni-directional), make an unidirectional
			// sync from source device to this one
			iNbCreatedFilesDest = synchronizeUnidirectonal(dSrc, this);
			// now the other one if bidi
			iNbCreatedFilesDest += synchronizeUnidirectonal(this, dSrc);
			// end message
			lTime = System.currentTimeMillis() - lTime;
			String sOut = new StringBuffer(Messages.getString("Device.33")).append(
					((lTime < 1000) ? lTime + " ms" : lTime / 1000 + " s")).append(" - ").append(
					iNbCreatedFilesSrc + iNbCreatedFilesDest).append(
					Messages.getString("Device.35")).append(lVolume / 1048576).append(
					Messages.getString("Device.36")).toString();
			// perform a fast refresh
			this.refreshCommand(false, true);
			// if bidi sync, refresh the other device as well (new file can
			// have been copied to it)
			if (bidi) {
				dSrc.refreshCommand(false, true);
			}
			InformationJPanel.getInstance().setMessage(sOut, InformationJPanel.INFORMATIVE);
			Log.debug(sOut);
		} catch (RuntimeException re) { // runtime error are thrown
			throw re;
		} catch (Exception e) { // and regular ones logged
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
	 *            device to synchronize
	 * @return nb of created files
	 */
	private int synchronizeUnidirectonal(Device dSrc, Device dest) {
		Iterator it = null;
		HashSet<Directory> hsSourceDirs = new HashSet<Directory>(100);
		// contains paths ( relative to device) of desynchronized dirs
		HashSet<String> hsDesynchroPaths = new HashSet<String>(10);
		HashSet<Directory> hsDestDirs = new HashSet<Directory>(100);
		int iNbCreatedFiles = 0;
		it = DirectoryManager.getInstance().getDirectories().iterator();
		while (it.hasNext()) {
			Directory dir = (Directory) it.next();
			if (dir.getDevice().equals(dSrc)) {
				// don't take desynchronized dirs into account
				if (dir.getBooleanValue(XML_DIRECTORY_SYNCHRONIZED)) {
					hsSourceDirs.add(dir);
				} else {
					hsDesynchroPaths.add(dir.getRelativePath());
				}
			}
		}
		it = DirectoryManager.getInstance().getDirectories().iterator();
		while (it.hasNext()) {
			Directory dir = (Directory) it.next();
			if (dir.getDevice().equals(dest)) {
				if (dir.getBooleanValue(XML_DIRECTORY_SYNCHRONIZED)) {
					// don't take desynchronized dirs into account
					hsDestDirs.add(dir);
				} else {
					hsDesynchroPaths.add(dir.getRelativePath());
				}
			}
		}

		it = hsSourceDirs.iterator();
		Iterator it2;
		// handle known extesions and image files
		FileFilter filter = new JajukFileFilter(false, new JajukFileFilter[] {
				JajukFileFilter.KnownTypeFilter.getInstance(),
				JajukFileFilter.ImageFilter.getInstance() });
		while (it.hasNext()) {
			// give a chance to exit during sync
			if (Main.isExiting()) {
				return iNbCreatedFiles;
			}
			boolean bNeedCreate = true;
			Directory dir = (Directory) it.next();
			String sPath = dir.getRelativePath();
			// check the directory on source is not desynchronized. If it
			// is, leave without checking files
			if (hsDesynchroPaths.contains(sPath)) {
				continue;
			}
			it2 = hsDestDirs.iterator();
			while (it2.hasNext()) {
				Directory dir2 = (Directory) it2.next();
				if (dir2.getRelativePath().equals(sPath)) {
					// directory already exists on this device
					bNeedCreate = false;
					break;
				}
			}
			// create it if needed
			File fileNewDir = new File(new StringBuffer(dest.getUrl()).append(sPath).toString());
			if (bNeedCreate) {
				fileNewDir.mkdirs();
			}
			// synchronize files
			File fileSrc = new File(new StringBuffer(dSrc.getUrl()).append(sPath).toString());
			File[] fSrcFiles = fileSrc.listFiles(filter);
			if (fSrcFiles != null) {
				for (int i = 0; i < fSrcFiles.length; i++) {
					File[] files = fileNewDir.listFiles(filter);
					if (files == null) {
						// fileNewDir is not a directory or an error occured (
						// read/write right ? )
						continue;
					}
					boolean bNeedCopy = true;
					for (int j = 0; j < files.length; j++) {
						if (fSrcFiles[i].getName().equalsIgnoreCase(files[j].getName())) {
							bNeedCopy = false;
						}
					}
					if (bNeedCopy) {
						try {
							Util.copyToDir(fSrcFiles[i], fileNewDir);
							iNbCreatedFiles++;
							lVolume += fSrcFiles[i].length();
							InformationJPanel.getInstance().setMessage(
									new StringBuffer(Messages.getString("Device.41")).append(
											dSrc.getName()).append(',').append(dest.getName())
											.append(Messages.getString("Device.42")).append(
													fSrcFiles[i].getAbsolutePath()).append("]")
											.toString(), InformationJPanel.INFORMATIVE);
						} catch (JajukException je) {
							Messages.showErrorMessage(je.getCode(), fSrcFiles[i].getAbsolutePath());
							Messages.showErrorMessage(27);
							Log.error(je);
							return iNbCreatedFiles;
						} catch (Exception e) {
							Messages.showErrorMessage(20, fSrcFiles[i].getAbsolutePath());
							Messages.showErrorMessage(27);
							Log.error(20, "{{" + fSrcFiles[i].getAbsolutePath() + "}}", e);
							return iNbCreatedFiles;
						}
					}
				}
			}
		}
		return iNbCreatedFiles;
	}

	/**
	 * @return
	 */
	public boolean isMounted() {
		return bMounted;
	}

	/**
	 * @return
	 */
	public String getDeviceTypeS() {
		return DeviceManager.getInstance().getDeviceType(getLongValue(XML_TYPE));
	}

	/**
	 * @return
	 */
	public long getType() {
		return getLongValue(XML_TYPE);
	}

	/**
	 * @return
	 */
	public String getUrl() {
		return sUrl;
	}

	/**
	 * @param url
	 *            The sUrl to set.
	 */
	public void setUrl(String url) {
		this.sUrl = url;
		setProperty(XML_URL, url);
		this.fio = new File(url);
		/** Reset files */
		Iterator it = FileManager.getInstance().getFiles().iterator();
		while (it.hasNext()) {
			org.jajuk.base.File file = (org.jajuk.base.File) it.next();
			file.reset();
		}
		/** Reset playlist files */
		it = PlaylistFileManager.getInstance().getPlaylistFiles().iterator();
		while (it.hasNext()) {
			org.jajuk.base.PlaylistFile plf = (org.jajuk.base.PlaylistFile) it.next();
			plf.reset();
		}
	}

	/**
	 * @return
	 */
	public ArrayList<Directory> getDirectories() {
		return alDirectories;
	}

	/**
	 * @param directory
	 */
	public void addDirectory(Directory directory) {
		alDirectories.add(directory);
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
	 * Mount the device
	 */
	public void mount() throws Exception {
		mount(true);
	}

	/**
	 * Mount the device
	 * 
	 * @param bUIRefresh
	 *            set whether the UI should be refreshed
	 * @throws Exception
	 *             if device cannot be mounted
	 */
	public void mount(boolean bUIRefresh) throws Exception {
		if (bMounted) {
			Messages.showErrorMessage(111);
		}
		try {
			if (!Util.isUnderWindows() && !getMountPoint().trim().equals("")) {
				// look to see if the device is already mounted ( the mount
				// command cannot say that )
				File file = new File(getMountPoint());
				if (file.exists() && file.list().length == 0) {
					// if none file in this directory, it probably
					// means device is not mounted, try to mount it

					// run the actual mount command
					Process process = Runtime.getRuntime().exec(
							new String[] { "mount", getMountPoint() });
					// just make a try, do not report error
					// if it fails (linux 2.6 doesn't
					// require anymore to mount devices)
					process.waitFor();
				}
			} else { // windows mount point or mount point not given, check
				// if path exists
				File file = new File(getUrl());
				if (!file.exists()) {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			throw new JajukException(11, getName(), e);
		}
		// Cannot mount void devices because of reference garbager thread
		File file = new File(getUrl());
		if (file.listFiles() != null && file.listFiles().length > 0) {
			bMounted = true;
		}
		// notify views to refresh if needed
		if (bMounted && bUIRefresh) {
			ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_MOUNT));
		}
		// Still not mounted ? throw an exception
		if (!bMounted) {
			throw new Exception();
		}

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
	 *            set whether the device must be ejected
	 * @param bUIRefresh
	 *            set wheter the UI should be refreshed
	 */
	public void unmount(boolean bEjection, boolean bUIRefresh) throws Exception {
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
		int iExit = 0;
		if (!Util.isUnderWindows() && !getMountPoint().trim().equals("")) {
			// not a windows
			try {
				// we try to unmount the device if under Unix. Note that this is
				// useless most of the time with Linux 2.6+, so it's just a try
				// and we don't check exit code anymore
				Process process = Runtime.getRuntime().exec(
						new String[] { "umount", getMountPoint() });
				iExit = process.waitFor();
				if (bEjection) { // jection if required
					process = Runtime.getRuntime().exec(new String[] { "eject", getMountPoint() });
					process.waitFor();
				}
			} catch (Exception e) {
				Log.error(12, Integer.toString(iExit), e); // mount failed
				Messages.showErrorMessage(12, getName());
				return;
			}
		}
		bMounted = false;
		if (bUIRefresh) {
			ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_UNMOUNT));
		}
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
		} catch (InterruptedException e2) {
			Log.error(e2);
		}
		boolean bWasMounted = bMounted; // store mounted state of device before
		// mount test
		try {
			if (!bMounted) {
				mount(false); // try to mount
			}
		} catch (Exception e) {
			Util.stopWaiting();
			return false;
		}
		if (getLongValue(XML_TYPE) != 5) { // not a remote device
			File file = new File(sUrl);
			if (file.exists() && file.canRead()) { // see if the url exists
				// and is readable
				// check if this device was void
				boolean bVoid = true;
				Iterator it = FileManager.getInstance().getFiles().iterator();
				while (it.hasNext()) {
					org.jajuk.base.File f = (org.jajuk.base.File) it.next();
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
			} catch (Exception e1) {
				Log.error(e1);
			}
		}
		Util.stopWaiting();
		return bOK;
	}

	/**
	 * @return Returns the unix mount point.
	 */
	public String getMountPoint() {
		return sMountPoint;
	}

	/**
	 * Alphabetical comparator used to display ordered lists of devices
	 * 
	 * @param other
	 *            device to be compared
	 * @return comparaison result
	 */
	public int compareTo(Object o) {
		Device otherDevice = (Device) o;
		return getName().compareToIgnoreCase(otherDevice.getName());
	}

	/**
	 * return child files recursively
	 * 
	 * @return child files recursively
	 */
	public ArrayList<org.jajuk.base.File> getFilesRecursively() {
		// looks for the root directory for this device
		Directory dirRoot = null;
		Collection dirs = DirectoryManager.getInstance().getDirectories();
		Iterator it = dirs.iterator();
		while (it.hasNext()) {
			Directory dir = (Directory) it.next();
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
	 * Return true if the device can be accessed right now
	 * 
	 * @return true the file can be accessed right now
	 */
	public boolean isReady() {
		if (this.isMounted() && !this.isRefreshing() && !this.isSynchronizing()) {
			return true;
		}
		return false;
	}

	/**
	 * @return Returns the IO file reference to this directory.
	 */
	public File getFio() {
		return fio;
	}

	/**
	 * Get item description
	 */
	public String getDesc() {
		return Messages.getString("Item_Device") + " : " + getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
	 */
	public String getHumanValue(String sKey) {
		if (XML_TYPE.equals(sKey)) {
			long lType = getLongValue(sKey);
			return DeviceManager.getInstance().getDeviceType(lType);
		} else {// default
			return super.getHumanValue(sKey);
		}
	}

	public long getDateLastRefresh() {
		return lDateLastRefresh;
	}

	/**
	 * Scan directories to cleanup removed files and playlist files
	 * 
	 * @param device
	 *            device to cleanup
	 * @return whether some items have been removed
	 */
	public boolean cleanRemovedFiles() {
		boolean bChanges = false;
		long l = System.currentTimeMillis();
		// need to use a shallow copy to avoid concurrent exceptions
		Set<Directory> dirs = DirectoryManager.getInstance().getDirectories();
		// directories cleanup
		for (Item item : dirs) {
			Directory dir = (Directory) item;
			if (!Main.isExiting() && dir.getDevice().equals(this) && dir.getDevice().isMounted()) {
				if (!dir.getFio().exists()) {
					// note that associated files are removed too
					DirectoryManager.getInstance().removeDirectory(dir.getId());
					Log.debug("Removed: " + dir);
					bChanges = true;
				}
			}
		}
		// files cleanup
		Set<org.jajuk.base.File> files = FileManager.getInstance().getFiles();
		for (org.jajuk.base.File file : files) {
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
		Set<PlaylistFile> plfiles = PlaylistFileManager.getInstance().getPlaylistFiles();
		for (PlaylistFile plf : plfiles) {
			if (!Main.isExiting() && plf.getDirectory().getDevice().equals(this) && plf.isReady()) {
				if (!plf.getFio().exists()) {
					PlaylistFileManager.getInstance().removePlaylistFile(plf);
					Log.debug("Removed: " + plf);
					bChanges = true;
				}
			}
		}
		// clear history to remove old files referenced in it
		if (ConfigurationManager.getProperty(CONF_HISTORY) != null) {
			History.getInstance().clear(
					Integer.parseInt(ConfigurationManager.getProperty(CONF_HISTORY)));
		}
		// delete old history items
		l = System.currentTimeMillis() - l;
		Log.debug("Old file references cleaned in: " + ((l < 1000) ? l + " ms" : l / 1000 + " s"));
		return bChanges;
	}

	/**
	 * Set all personnal properties of an XML file for an item (doesn't
	 * overwrite existing properties for perfs)
	 * 
	 * @param attributes :
	 *            list of attributes for this XML item
	 */
	public void populateProperties(Attributes attributes) {
		for (int i = 0; i < attributes.getLength(); i++) {
			String sProperty = attributes.getQName(i);
			if (!getProperties().containsKey(sProperty)) {
				String sValue = attributes.getValue(i);
				PropertyMetaInformation meta = getMeta(sProperty);
				// compatibility code for <1.1 : auto-refresh is now a double,
				// no more a boolean
				if (meta.getName().equals(XML_DEVICE_AUTO_REFRESH)
						&& (sValue.equalsIgnoreCase(TRUE) || sValue.equalsIgnoreCase(FALSE))) {
					switch ((int) this.getType()) {
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
				} catch (Exception e) {
					Log.error(137, sProperty, e);
				}
			}
		}
	}

	/**
	 * 
	 * @return Associated root directory
	 */
	public Directory getRootDirectory() {
		return DirectoryManager.getInstance().getDirectoryForIO(getFio());
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
}
