/*
 * Jajuk Copyright (C) 2003 The Jajuk Team
 * 
 * This program is free software; you can redistribute 
 * it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,USA
 * $Revision$
 */
package org.jajuk;

import org.jajuk.base.AlbumManager;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Collection;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Player;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.StyleManager;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.base.WebRadio;
import org.jajuk.base.YearManager;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.dj.DigitalDJManager;
import org.jajuk.i18n.Messages;
import org.jajuk.services.lastfm.LastFmManager;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.FontManager;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.ui.JajukSystray;
import org.jajuk.ui.JajukWindow;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.FontManager.JajukFont;
import org.jajuk.ui.action.ActionBase;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.RestoreAllViewsAction;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.wizard.FirstTimeWizard;
import org.jajuk.ui.wizard.TipOfTheDay;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jajuk.webradio.WebRadioManager;
import org.jvnet.substance.SubstanceLookAndFeel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.vlsolutions.swing.docking.ui.DockingUISettings;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarIO;

import ext.JSplash;
import ext.JVM;

/**
 * Jajuk launching class
 */
public class Main implements ITechnicalStrings {

	/** Main window */
	private static JajukWindow jw;

	/** Top command panel */
	public static CommandJPanel command;

	/** Toolbar container that can be serialized */
	private static ToolBarContainer tbcontainer;

	/** Left side perspective selection panel */
	public static PerspectiveBarJPanel perspectiveBar;

	/** Lower information panel */
	public static InformationJPanel information;

	/** Main frame panel */
	public static JPanel jpFrame;

	/** splashscreen */
	public static JSplash sc;

	/** Exit code */
	private static int iExitCode = 0;

	/** Debug mode */
	public static boolean bIdeMode = false;

	/** Test mode */
	public static boolean bTestMode = false;

	/** Exiting flag */
	public static boolean bExiting = false;

	/** Systray */
	private static JajukSystray jsystray;

	/** UI lauched flag */
	private static boolean bUILauched = false;

	/** default perspective to shoose, if null, we take the configuration one */
	private static String sPerspective;

	/** Server socket used to check other sessions */
	private static ServerSocket ss;

	/** Is it a minor or major X.Y upgrade */
	private static boolean bUpgraded = false;

	/** Is it the first session ever ? */
	public static boolean bFirstSession = false;

	/** Does this session follows a crash revover ? */
	private static boolean bCrashRecover = false;

	/** Mplayer state */
	private static MPlayerStatus mplayerStatus;

	/** Workspace PATH* */
	public static String workspace;

	/** Lock used to trigger a first time wizard device creation and refresh * */
	public static short[] canLaunchRefresh = new short[0];

	/** MPlayer status possible values * */
	public static enum MPlayerStatus {
		MPLAYER_STATUS_OK, MPLAYER_STATUS_NOT_FOUND, MPLAYER_STATUS_WRONG_VERSION, MPLAYER_STATUS_JNLP_DOWNLOAD_PBM
	}

	/**
	 * Main entry
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		// non ui init
		try {
			// check JVM version
			if (!JVM.current().isOrLater(JVM.JDK1_5)) {
				System.out.println("Java Runtime Environment 1.5 minimum required."
						+ " You use a JVM " + JVM.current());
				System.exit(2); // error code 2 : wrong JVM
			}
			// set command line options
			for (int i = 0; i < args.length; i++) {
				// Tells jajuk it is inside the IDE (useful to find right
				// location for images and jar resources)
				if (args[i].equals("-" + CLI_IDE)) {
					bIdeMode = true;
				}
				// Tells jajuk to use a .jajuk_test repository
				// The information can be given from CLI using
				// -test=[test|notest] option
				// or using the "test" env variable
				String test = System.getProperty("test");
				if (args[i].equals("-" + CLI_TEST) || (test != null && test.equals("test"))) {
					bTestMode = true;
				}
			}

			// Set look and feel, needs local to be set for error
			// messages
			try {
				UIManager.setLookAndFeel(LNF_SUBSTANCE_CLASS);
				UIManager.put(SubstanceLookAndFeel.NO_EXTRA_ELEMENTS, Boolean.TRUE);
				// Set default fonts
				FontManager.setDefaultFont();
			} catch (Exception e) {
				// Get an exception with JRE 1.7 beta, some code is not yet
				// implemented
				Log.error(e);
			}

			// perform initial checkups and create needed files
			initialCheckups();

			// Set a session file
			File sessionUser = Util.getConfFileByPath(FILE_SESSIONS + '/' + Util.getHostName()
					+ '_' + System.getProperty("user.name"));
			sessionUser.mkdir();

			// log startup depends on : setExecLocation, initialCheckups
			Log.getInstance();
			Log.setVerbosity(Log.DEBUG);

			// Configuration manager startup. Depends on: initialCheckups
			org.jajuk.util.ConfigurationManager.getInstance();

			// Register locals, needed by ConfigurationManager to choose
			// default language
			Messages.getInstance().registerLocal("en", "Language_desc_en");
			Messages.getInstance().registerLocal("fr", "Language_desc_fr");
			Messages.getInstance().registerLocal("de", "Language_desc_de");
			Messages.getInstance().registerLocal("it", "Language_desc_it");
			Messages.getInstance().registerLocal("sv", "Language_desc_sv");
			Messages.getInstance().registerLocal("nl", "Language_desc_nl");
			Messages.getInstance().registerLocal("zh", "Language_desc_zh");
			Messages.getInstance().registerLocal("es", "Language_desc_es");
			Messages.getInstance().registerLocal("ca", "Language_desc_ca");
			Messages.getInstance().registerLocal("ko", "Language_desc_ko");
			Messages.getInstance().registerLocal("el", "Language_desc_el");

			// Set default local (from system). Depends on registerLocal
			ConfigurationManager.getInstance().setSystemLocal();

			// Load user configuration. Depends on: initialCheckups,
			// setSystemLocal
			ConfigurationManager.load();

			// Upgrade detection. Depends on: Configuration manager load
			String sRelease = ConfigurationManager.getProperty(CONF_RELEASE);

			// check if it is a new major 'x.y' release: 1.2 != 1.3 for instance
			if (!bFirstSession
			// if first session, not taken as an upgrade
					&& (sRelease == null || // null for jajuk releases < 1.2
					!sRelease.substring(0, 3).equals(JAJUK_VERSION.substring(0, 3)))) {
				bUpgraded = true;
			}
			// Now set current release in the conf
			ConfigurationManager.setProperty(CONF_RELEASE, JAJUK_VERSION);

			// Set actual log verbosity. Depends on:
			// ConfigurationManager.load
			if (!bTestMode) {
				// test mode is always in debug mode
				Log.setVerbosity(Integer.parseInt(ConfigurationManager
						.getProperty(CONF_OPTIONS_LOG_LEVEL)));
			}
			// Set locale. setSystemLocal
			Messages.getInstance()
					.setLocal(ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE));

			// Launch splashscreen. Depends on: log.setVerbosity,
			// configurationManager.load (for local)
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					// Set window look and feel and watermarks
					Util.setLookAndFeel(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));

					sc = new JSplash(IMAGES_SPLASHSCREEN, true, true, false, JAJUK_COPYRIGHT,
							JAJUK_VERSION + " " + JAJUK_VERSION_DATE, FontManager.getInstance()
									.getFont(JajukFont.SPLASH), null);
					sc.setTitle(Messages.getString("JajukWindow.3"));
					sc.splashOn();
				}
			});

			// Apply any proxy (requires load conf)
			DownloadManager.setDefaultProxySettings();

			// Display progress
			sc.setProgress(0, Messages.getString("SplashScreen.0"));

			// Registers ItemManager managers
			ItemManager.registerItemManager(org.jajuk.base.Album.class, AlbumManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Author.class, AuthorManager
					.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Device.class, DeviceManager
					.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.File.class, FileManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Directory.class, DirectoryManager
					.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.PlaylistFile.class, PlaylistFileManager
					.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Playlist.class, PlaylistManager
					.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Style.class, StyleManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Track.class, TrackManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Type.class, TypeManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Year.class, YearManager.getInstance());

			// Upgrade configuration from previous releases
			UpgradeManager.upgradeStep1();

			// Display user system configuration
			Log.debug(Util.getAnonymizedSystemProperties().toString());

			// Display user Jajuk configuration
			Log.debug(Util.getAnonymizedJajukProperties().toString());

			// check for another session (needs setLocal)
			checkOtherSession();

			// Register device types
			DeviceManager.getInstance().registerDeviceType(
					Messages.getString("Device_type.directory"));
			DeviceManager.getInstance().registerDeviceType(
					Messages.getString("Device_type.file_cd"));
			DeviceManager.getInstance().registerDeviceType(
					Messages.getString("Device_type.network_drive"));
			DeviceManager.getInstance().registerDeviceType(Messages.getString("Device_type.extdd"));
			DeviceManager.getInstance()
					.registerDeviceType(Messages.getString("Device_type.player"));
			// registers supported audio supports and default properties
			registerTypes();

			// Display progress
			sc.setProgress(10, Messages.getString("SplashScreen.1"));

			// Load collection
			loadCollection();

			// Clean the collection up
			Collection.cleanup();

			// Unlock pending First time wizard if any
			synchronized (canLaunchRefresh) {
				canLaunchRefresh.notify();
			}

			// Display progress
			sc.setProgress(70, Messages.getString("SplashScreen.2"));

			// Load history
			History.load();

			// Load ambiences
			AmbienceManager.getInstance().load();

			// Start LastFM support
			LastFmManager.getInstance();

			// Load djs
			DigitalDJManager.getInstance().loadAllDJs();

			// Various asynchronous startup actions
			startupAsync();

			// Start check for update thread if required
			if (ConfigurationManager.getBoolean(CONF_CHECK_FOR_UPDATE)) {
				new Thread() {
					public void run() {
						// Wait 10 min before checking
						try {
							Thread.sleep(600000);
							UpgradeManager.checkForUpdate(false);
						} catch (InterruptedException e) {
							Log.error(e);
						}
					}
				}.start();
			}

			// start exit hook
			Thread tHook = new Thread() {
				public void run() {
					Log.debug("Exit Hook begin");
					try {
						Player.stop(true); // stop sound ASAP
					} catch (Exception e) {
						e.printStackTrace();
						// no log to make sure to reach collection commit
					}
					try {
						if (iExitCode == 0) {
							// Remove session flag
							File sessionUser = Util.getConfFileByPath(FILE_SESSIONS + '/'
									+ InetAddress.getLocalHost().getHostName() + '_'
									+ System.getProperty("user.name"));
							sessionUser.delete();

							// commit only if exit is safe (to avoid commiting
							// empty collection) commit ambiences
							AmbienceManager.getInstance().commit();
							// Commit webradios
							WebRadioManager.getInstance().commit();
							// commit configuration
							org.jajuk.util.ConfigurationManager.commit();
							// commit history
							History.commit();
							// commit perspectives if no full restore engaged
							if (!RestoreAllViewsAction.fullRestore) {
								PerspectiveManager.commit();
							}
							// Commit collection if not refreshing ( fix for
							// 939816 )
							if (!DeviceManager.getInstance().isAnyDeviceRefreshing()) {
								Collection.commit(Util.getConfFileByPath(FILE_COLLECTION_EXIT));
								// create a proof file
								Util.createEmptyFile(Util
										.getConfFileByPath(FILE_COLLECTION_EXIT_PROOF));
							}
							// Commit toolbars (only if it is visible to avoid
							// commiting void screen)
							if (!RestoreAllViewsAction.fullRestore && getWindow() != null
									&& getWindow().isWindowVisible()) {
								ToolBarIO tbIO = new ToolBarIO(tbcontainer);
								FileOutputStream out = new FileOutputStream(Util
										.getConfFileByPath(FILE_TOOLBARS_CONF));
								tbIO.writeXML(out);
								out.flush();
								out.close();
							}
							/* release keystrokes resources */
							ActionBase.cleanup();
						}
					} catch (Exception e) {
						Log.error(e);
					} finally {
						Log.debug("Exit Hook end");
					}
				}
			};
			tHook.setPriority(Thread.MAX_PRIORITY);

			Runtime.getRuntime().addShutdownHook(tHook);

			// Auto mount devices, freeze for SMB drives
			// if network is not reacheable
			autoMount();

			// Launch auto-refresh thread
			DeviceManager.getInstance().startAutoRefreshThread();

			// Launch startup track if any
			launchInitialTrack();

			// Start up action manager. TO be done before launching ui and
			// tray
			ActionManager.getInstance();

			// show window if set in the systray conf.
			if (ConfigurationManager.getBoolean(CONF_UI_SHOW_AT_STARTUP)) {
				// Display progress
				sc.setProgress(80, Messages.getString("SplashScreen.3"));
				launchUI();
			}

			// start the tray
			launchTray();

		} catch (JajukException je) { // last chance to catch any error for
			// logging purpose
			Log.error(je);
			if (je.getCode() == 5) {
				Messages.getChoice(Messages.getErrorMessage(5), JOptionPane.ERROR_MESSAGE);
				exit(1);
			}
		} catch (Exception e) { // last chance to catch any error for logging
			// purpose
			e.printStackTrace();
			Log.error(106, e);
			exit(1);
		} catch (Error error) { // last chance to catch any error for logging
			// purpose
			error.printStackTrace();
			Log.error(106, error);
			exit(1);
		} finally { // make sure to close splashscreen in all cases (ie if
			// UI is not started)
			if (!ConfigurationManager.getBoolean(CONF_UI_SHOW_AT_STARTUP) && sc != null) {
				sc.setProgress(100);
				sc.splashOff();
			}
		}
	}

	/**
	 * Performs some basic startup tests
	 * 
	 * @throws Exception
	 */
	private static void initialCheckups() throws Exception {
		// Check for bootstrap file presence
		File bootstrap = new File(FILE_BOOTSTRAP);
		File fJajukDir = Util.getConfFileByPath("");
		if (bootstrap.canRead()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(bootstrap));
				// Bootstrap file should contain a single line containing the
				// path to jajuk workspace
				String sPath = br.readLine();
				br.close();
				if (new File(sPath).canRead()) {
					Main.workspace = sPath;
				}
			} catch (Exception e) {
				System.out.println("Cannot read bootstrap file, using ~ directory");
				Main.workspace = System.getProperty("user.home");
			}
		}
		// No bootstrap or unreadable or the path included inside is not
		// readable, show a wizard to select it
		if ((!bootstrap.canRead() || Main.workspace == null)
		// don't launch the first time wizard if a previous release .jajuk dir
				// exists (upgrade)
				&& !fJajukDir.canRead()) {
			// First time session ever
			bFirstSession = true;
			final FirstTimeWizard fsw = new FirstTimeWizard();
			// display the first time wizard, keep the invokeAndWait as we have
			// to block in the next while block
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					fsw.setAlwaysOnTop(true);
					Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					fsw.setLocation(((int) dim.getWidth() / 3), ((int) dim.getHeight() / 3));
					fsw.pack();
					fsw.setVisible(true);
				}

			});
			// Wait until user closed the wizard
			while (fsw.isVisible()) {
				Thread.sleep(1000);
			}
		}

		// In all cases, make sure to set a workspace
		if (workspace == null) {
			workspace = System.getProperty("user.home");
		}
		// check for jajuk directory
		if (!fJajukDir.exists()) {
			fJajukDir.mkdir(); // create the directory if it doesn't exist
		}
		// check for configuration file presence
		File fConfig = Util.getConfFileByPath(FILE_CONFIGURATION);
		if (!fConfig.exists()) { // if config file doesn't exit, create
			// it with default values
			org.jajuk.util.ConfigurationManager.commit();
		}
		// check for history.xml file
		File fHistory = Util.getConfFileByPath(FILE_HISTORY);
		if (!fHistory.exists()) { // if history file doesn't exit, create
			// it empty
			History.commit();
		}
		// check for image cache presence
		File fCache = Util.getConfFileByPath(FILE_CACHE);
		if (!fCache.exists()) {
			fCache.mkdir();
		}
		// check for thumbnails cache presence
		File fThumbs = Util.getConfFileByPath(FILE_THUMBS);
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/" + THUMBNAIL_SIZE_50x50);
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/" + THUMBNAIL_SIZE_100x100);
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/" + THUMBNAIL_SIZE_150x150);
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/" + THUMBNAIL_SIZE_200x200);
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/" + THUMBNAIL_SIZE_250x250);
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = Util.getConfFileByPath(FILE_THUMBS + "/" + THUMBNAIL_SIZE_300x300);
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		// check for djs directory
		File fdjs = Util.getConfFileByPath(FILE_DJ_DIR);
		if (!fdjs.exists()) {
			fdjs.mkdir();
		}
		// Extract default background picture if required
		if (!Util.getConfFileByPath("cache/internal/" + FILE_BACKGROUND_IMAGE).exists()) {
			if (bIdeMode) {
				Util.copy(new File("src/main/resources/images/included/" + FILE_BACKGROUND_IMAGE),
						Util.getConfFileByPath("cache/internal/" + FILE_BACKGROUND_IMAGE));
			} else {
				Util.extractFile("images/" + FILE_BACKGROUND_IMAGE, FILE_BACKGROUND_IMAGE);
			}
		}
	}

	/**
	 * Asynchronous tasks executed at startup
	 */
	private static void startupAsync() {
		new Thread() {
			public void run() {
				try {
					// Extract star icons (used in HTML panels)
					Util.getConfFileByPath("/cache/internal").mkdir();
					for (int i = 1; i <= 4; i++) {
						if (bIdeMode) {
							Util.copy(new File("src/main/resources/icons/16x16/star" + i
									+ "_16x16.png"), Util.getConfFileByPath("cache/internal/star"
									+ i + "_16x16.png"));
						} else {
							Util.extractFile("icons/16x16/star" + i + "_16x16.png", "star" + i
									+ "_16x16.png");
						}
					}
					// Refresh max album rating
					AlbumManager.getInstance().refreshMaxRating();
				} catch (Exception e) {
					Log.error(e);
				}
			}
		}.start();
	}

	/**
	 * Registers supported audio supports and default properties
	 */
	private static void registerTypes() {
		try {
			// test mplayer presence in PATH
			mplayerStatus = MPlayerStatus.MPLAYER_STATUS_OK;
			if (Util.isUnderWindows()) {
				// try to find mplayer executable in know locations first
				if (Util.getMPlayerWindowsPath() == null ||
				// if file exists, test size
						new File(Util.getMPlayerWindowsPath()).length() != MPLAYER_EXE_SIZE) {
					// probably in JNLP mode or wrong size,
					// try to download static mplayer distro if needed
					try {
						Log.debug("Download Mplayer from: " + URL_MPLAYER); //$NON-NLS-1$
						File fMPlayer = Util.getConfFileByPath(FILE_MPLAYER_EXE);
						DownloadManager.download(new URL(URL_MPLAYER), fMPlayer);
						// make sure to delete corrupted mplayer in case of
						// download problem
						if (fMPlayer.length() != MPLAYER_EXE_SIZE) {
							fMPlayer.delete();
							throw new Exception("MPlayer corrupted"); //$NON-NLS-1$
						}
					} catch (Exception e) {
						mplayerStatus = MPlayerStatus.MPLAYER_STATUS_JNLP_DOWNLOAD_PBM;
					}
				}
			}
			// Under non-windows OS, we assume mplayer has been installed
			// using external standard distributions
			else {
				// If a forced mplayer path is defined, test it
				String forced = ConfigurationManager.getProperty(CONF_MPLAYER_PATH_FORCED);
				if (forced != null && !"".equals(forced)) {
					// Test forced path
					mplayerStatus = Util.getMplayerStatus(forced);
				} else {
					mplayerStatus = MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
				}
				if (mplayerStatus != MPlayerStatus.MPLAYER_STATUS_OK) {
					// try to find a correct mplayer from the path
					// Under OSX, it will work only if jajuk is launched from
					// command line
					mplayerStatus = Util.getMplayerStatus("");
					if (mplayerStatus != MPlayerStatus.MPLAYER_STATUS_OK) {
						// OK, try to find MPlayer in standards OSX directories
						if (Util.isUnderOSXpower()) {
							mplayerStatus = Util
									.getMplayerStatus(FILE_DEFAULT_MPLAYER_POWER_OSX_PATH);
						} else {
							mplayerStatus = Util
									.getMplayerStatus(FILE_DEFAULT_MPLAYER_X86_OSX_PATH);
						}
					}
				}
			}
			// Choose player according to mplayer presence or not
			if (mplayerStatus != MPlayerStatus.MPLAYER_STATUS_OK) {
				Log.debug("Mplayer status=" + mplayerStatus);
				// No mplayer, show mplayer warnings
				if (mplayerStatus != MPlayerStatus.MPLAYER_STATUS_OK) {
					// Test if user didn't already select "don't show again"
					if (!ConfigurationManager.getBoolean(CONF_NOT_SHOW_AGAIN_PLAYER)) {
						if (mplayerStatus == MPlayerStatus.MPLAYER_STATUS_NOT_FOUND) {
							// No mplayer
							Messages.showHideableWarningMessage(Messages.getString("Warning.0"), //$NON-NLS-1$
									CONF_NOT_SHOW_AGAIN_PLAYER);
						} else if (mplayerStatus == MPlayerStatus.MPLAYER_STATUS_WRONG_VERSION) {
							// wrong mplayer release
							Messages.showHideableWarningMessage(Messages.getString("Warning.1"), //$NON-NLS-1$
									CONF_NOT_SHOW_AGAIN_PLAYER);
						}
					} else if (mplayerStatus == MPlayerStatus.MPLAYER_STATUS_JNLP_DOWNLOAD_PBM) {
						// wrong mplayer release
						Messages.showHideableWarningMessage(Messages.getString("Warning.3"), //$NON-NLS-1$
								CONF_NOT_SHOW_AGAIN_PLAYER);
					}
				}
				// mp3
				Type type = TypeManager.getInstance().registerType(Messages.getString("Type.mp3"),
						EXT_MP3, Class.forName(PLAYER_IMPL_JAVALAYER),
						Class.forName(TAG_IMPL_ENTAGGED));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_MP3);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_MP3.getUrl().toExternalForm());
				// playlists
				type = TypeManager.getInstance().registerType(Messages.getString("Type.playlist"),
						EXT_PLAYLIST, Class.forName(PLAYER_IMPL_JAVALAYER), null);
				type.setProperty(XML_TYPE_IS_MUSIC, false);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, false);
				// Ogg vorbis
				type = TypeManager.getInstance().registerType(Messages.getString("Type.ogg"),
						EXT_OGG, Class.forName(PLAYER_IMPL_JAVALAYER),
						Class.forName(TAG_IMPL_ENTAGGED));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, false);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_OGG);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_OGG.getUrl().toExternalForm());
				// Wave
				type = TypeManager.getInstance().registerType(Messages.getString("Type.wav"),
						EXT_WAV, Class.forName(PLAYER_IMPL_JAVALAYER),
						Class.forName(TAG_IMPL_NO_TAGS));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_WAVE);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_WAV.getUrl().toExternalForm());
				// au
				type = TypeManager.getInstance().registerType(Messages.getString("Type.au"),
						EXT_AU, Class.forName(PLAYER_IMPL_JAVALAYER),
						Class.forName(TAG_IMPL_NO_TAGS));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, false);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_AU);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_AU.getUrl().toExternalForm());
			} else { // mplayer enabled
				// mp3
				Type type = TypeManager.getInstance().registerType(Messages.getString("Type.mp3"),
						EXT_MP3, Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_MP3);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_MP3.getUrl().toExternalForm());
				// playlists
				type = TypeManager.getInstance().registerType(Messages.getString("Type.playlist"),
						EXT_PLAYLIST, Class.forName(PLAYER_IMPL_JAVALAYER), null);
				type.setProperty(XML_TYPE_IS_MUSIC, false);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, false);
				// Ogg vorbis
				type = TypeManager.getInstance().registerType(Messages.getString("Type.ogg"),
						EXT_OGG, Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_OGG);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_OGG.getUrl().toExternalForm());
				// Wave
				type = TypeManager.getInstance().registerType(Messages.getString("Type.wav"),
						EXT_WAV, Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_NO_TAGS));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_WAVE);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_WAV.getUrl().toExternalForm());
				// au
				type = TypeManager
						.getInstance()
						.registerType(Messages.getString("Type.au"), EXT_AU,
								Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_NO_TAGS));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_AU);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_AU.getUrl().toExternalForm());
				// flac
				type = TypeManager.getInstance().registerType(Messages.getString("Type.flac"),
						EXT_FLAC, Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_FLAC);
				type
						.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_FLAC.getUrl()
								.toExternalForm());
				// WMA
				type = TypeManager.getInstance().registerType(Messages.getString("Type.wma"),
						EXT_WMA, Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_WMA);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_WMA.getUrl().toExternalForm());
				// AAC
				type = TypeManager.getInstance().registerType(Messages.getString("Type.aac"),
						EXT_AAC, Class.forName(PLAYER_IMPL_MPLAYER), null);
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_AAC);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_AAC.getUrl().toExternalForm());
				// M4A (=AAC)
				type = TypeManager.getInstance().registerType(Messages.getString("Type.aac"),
						EXT_M4A, Class.forName(PLAYER_IMPL_MPLAYER), null);
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_AAC);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_AAC.getUrl().toExternalForm());
				// Real audio
				type = TypeManager.getInstance().registerType(Messages.getString("Type.real"),
						EXT_REAL, Class.forName(PLAYER_IMPL_MPLAYER), null);
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_RAM);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_RAM.getUrl().toExternalForm());
				// mp2
				type = TypeManager.getInstance().registerType(Messages.getString("Type.mp2"),
						EXT_MP2, Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED));
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_MP2);
				type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_MP2.getUrl().toExternalForm());
				// web radios
				type = TypeManager.getInstance().registerType(Messages.getString("Type.radio"),
						EXT_RADIO, Class.forName(PLAYER_IMPL_WEBRADIOS), null);
				type.setProperty(XML_TYPE_IS_MUSIC, true);
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
				type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_RADIO);
			}
			// Types not only supported by mplayer but supported by basicplayer
			// APE
			Type type = TypeManager.getInstance()
					.registerType(Messages.getString("Type.ape"), EXT_APE,
							Class.forName(PLAYER_IMPL_JAVALAYER), Class.forName(TAG_IMPL_ENTAGGED));
			type.setProperty(XML_TYPE_IS_MUSIC, true);
			type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
			type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_APE);
			type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_APE.getUrl().toExternalForm());
			// MAC
			type = TypeManager.getInstance().registerType(Messages.getString("Type.mac"), EXT_MAC,
					Class.forName(PLAYER_IMPL_JAVALAYER), Class.forName(TAG_IMPL_ENTAGGED));
			type.setProperty(XML_TYPE_IS_MUSIC, true);
			type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
			type.setProperty(XML_TYPE_TECH_DESC, TYPE_PROPERTY_TECH_DESC_APE);
			type.setProperty(XML_TYPE_ICON, IconLoader.ICON_TYPE_APE.getUrl().toExternalForm());
		} catch (Exception e1) {
			Log.error(26, e1);
		}
	}

	/**
	 * check if another session is already started
	 * 
	 */
	private static void checkOtherSession() {
		// check for a concurrent jajuk session on local box, try to create a
		// new server socket
		try {
			ss = new ServerSocket(PORT);
			// No error? jajuk was not started, leave
		} catch (IOException e) { // error? looks like Jajuk is already
			// started
			if (sc != null) {
				sc.dispose();
			}
			Log.error(124);
			Messages.getChoice(Messages.getErrorMessage(124), JOptionPane.DEFAULT_OPTION);
			System.exit(-1);
		}
		// start listening
		new Thread() {
			public void run() {
				try {
					ss.accept();
				} catch (IOException e) {
					Log.error(e);
				}
			}
		}.start();
		// Now check for remote concurrent users using the same configuration
		// files
		// Create concurrent session directory if needed
		File sessions = Util.getConfFileByPath(FILE_SESSIONS);
		if (!sessions.exists()) {
			sessions.mkdir();
		}
		// Check for concurrent session
		File[] files = sessions.listFiles();
		String sHostname;
		try {
			sHostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			sHostname = "";
		}
		// display a warning if sessions directory contains some others users
		// We ignore presence of ourself session id that can be caused by a
		// crash
		if (files.length > 0
				&& !(files.length == 1 && files[0].getName().equals(
						sHostname + '_' + System.getProperty("user.name")))) {
			Messages.showHideableWarningMessage(Messages.getString("Warning.2"),
					CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION);
		}
	}

	/**
	 * Exit code, then system will execute the exit hook
	 * 
	 * @param iExitCode
	 *            exit code
	 *            <p>
	 *            0 : normal exit
	 *            <p>
	 *            1: unexpected error
	 */
	public static void exit(int iExitCode) {
		// Store current FIFO for next session
		try {
			FIFO.getInstance().commit();
		} catch (IOException e) {
			Log.error(e);
		}
		// check if a confirmation is needed
		if (Boolean.valueOf(ConfigurationManager.getProperty(CONF_CONFIRMATIONS_EXIT))
				.booleanValue()) {
			int iResu = Messages.getChoice(Messages.getString("Confirmation_exit"),
					JOptionPane.INFORMATION_MESSAGE);
			if (iResu != JOptionPane.YES_OPTION) {
				return;
			}
		}
		// Store webradio state
		ConfigurationManager.setProperty(CONF_WEBRADIO_WAS_PLAYING, Boolean.toString(FIFO
				.getInstance().isPlayingRadio()));
		// stop sound to avoid strange crash when stopping
		Player.mute(true);
		// set exiting flag
		bExiting = true;
		// store exit code to be read by the system hook
		Main.iExitCode = iExitCode;
		// force sound to stop quickly
		FIFO.getInstance().stopRequest();
		/*
		 * alert playlists editors ( queue playlist ) something changed for him
		 * hide window
		 */
		ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
		if (jw != null)
			jw.display(false);
		// hide systray
		if (jsystray != null)
			jsystray.closeSystray();
		// display a message
		Log.debug("Exit with code: " + iExitCode);
		System.exit(iExitCode);
	}

	/**
	 * Load persisted collection file
	 */
	private static void loadCollection() {
		if (Main.bFirstSession) {
			Log.info("First session, collection will be created");
			return;
		}
		File fCollection = Util.getConfFileByPath(FILE_COLLECTION);
		File fCollectionExit = Util.getConfFileByPath(FILE_COLLECTION_EXIT);
		File fCollectionExitProof = Util.getConfFileByPath(FILE_COLLECTION_EXIT_PROOF);
		// check if previous exit was OK
		boolean bParsingOK = true;
		try {
			if (fCollectionExit.exists() && fCollectionExitProof.exists()) {
				fCollectionExitProof.delete(); // delete this file created just
				// after collection exit commit
				Collection.load(Util.getConfFileByPath(FILE_COLLECTION_EXIT));
				// parsing of collection exit ok, use this collection file as
				// final collection
				fCollectionExit.renameTo(fCollection);
				// backup the collection
				Util.backupFile(Util.getConfFileByPath(FILE_COLLECTION), ConfigurationManager
						.getInt(CONF_BACKUP_SIZE));
			} else {
				bCrashRecover = true;
				throw new JajukException(5);
			}
		} catch (Exception e) {
			Log.error(5, fCollectionExit.getAbsolutePath(), e);
			Log
					.debug("Jajuk was not closed properly during previous session, try to load previous collection file");
			if (fCollectionExit.exists()) {
				fCollectionExit.delete();
			}
			try {
				// try to load "official" collection file, should be OK but not
				// always up-to-date
				Collection.load(Util.getConfFileByPath(FILE_COLLECTION));
			} catch (Exception e2) {
				// not better? strange
				Log.error(5, fCollection.getAbsolutePath(), e2);
				bParsingOK = false;
			}
		}
		if (!bParsingOK) { // even final collection file parsing failed
			// (very unlikely), try to restore a backup file
			File[] fBackups = Util.getConfFileByPath("").listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.indexOf("backup") != -1) {
						return true;
					}
					return false;
				}
			});
			ArrayList<File> alBackupFiles = new ArrayList<File>(Arrays.asList(fBackups));
			Collections.sort(alBackupFiles); // sort alphabetically (newest
			// last)
			Collections.reverse(alBackupFiles); // newest first now
			Iterator<File> it = alBackupFiles.iterator();
			// parse all backup files, newest first
			while (!bParsingOK && it.hasNext()) {
				File file = it.next();
				try {
					Collection.load(file);
					bParsingOK = true;
					int i = Messages.getChoice(Messages.getString("Error.133") + ":\n"
							+ file.getAbsolutePath(), JOptionPane.WARNING_MESSAGE);
					if (i == JOptionPane.CANCEL_OPTION) {
						System.exit(-1);
					}
					break;
				} catch (Exception e2) {
					Log.error(5, file.getAbsolutePath(), e2);
				}
			}
			if (!bParsingOK) { // not better? ok, commit and load a void
				// collection
				Collection.cleanup();
				DeviceManager.getInstance().cleanAllDevices();
				try {
					Collection.commit(Util.getConfFileByPath(FILE_COLLECTION));
				} catch (Exception e2) {
					Log.error(e2);
				}
			}
		}
	}

	/**
	 * Launch initial track at startup
	 */
	private static void launchInitialTrack() {
		List<org.jajuk.base.File> alToPlay = new ArrayList<org.jajuk.base.File>();
		org.jajuk.base.File fileToPlay = null;
		if (!ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOTHING)) {
			if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST)
					|| ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
							STARTUP_MODE_LAST_KEEP_POS)
					|| ConfigurationManager.getProperty(CONF_STARTUP_MODE)
							.equals(STARTUP_MODE_FILE)) {

				if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_FILE)) {
					fileToPlay = FileManager.getInstance().getFileByID(
							ConfigurationManager.getProperty(CONF_STARTUP_FILE));
				} else {
					// If we were playing a webradio when leaving, launch it
					if (ConfigurationManager.getBoolean(CONF_WEBRADIO_WAS_PLAYING)) {
						final WebRadio radio = WebRadioManager.getInstance().getWebRadioByName(
								ConfigurationManager.getProperty(CONF_DEFAULT_WEB_RADIO));
						if (radio != null) {
							new Thread() {
								public void run() {
									FIFO.getInstance().launchRadio(radio);
								}
							}.start();
						}
						return;
					}
					// last file from beginning or last file keep position
					else if (ConfigurationManager.getBoolean(CONF_STATE_WAS_PLAYING)
							&& History.getInstance().getHistory().size() > 0) {
						// make sure user didn't exit jajuk in the stopped state
						// and that history is not void
						fileToPlay = FileManager.getInstance().getFileByID(
								History.getInstance().getLastFile());
					} else {
						// do not try to launch anything, stay in stop state
						return;
					}
				}
				if (fileToPlay != null) {
					if (fileToPlay.isReady()) {
						// we try to launch at startup only existing and mounted
						// files
						alToPlay.add(fileToPlay);
					} else {
						// file exists but is not mounted, just notify the error
						// without anoying dialog at each startup try to mount
						// device
						Log.debug("Startup file located on an unmounted device"
								+ ", try to mount it");
						try {
							fileToPlay.getDevice().mount(true);
							Log.debug("Mount OK");
							alToPlay.add(fileToPlay);
						} catch (Exception e) {
							Log.debug("Mount failed");
							Properties pDetail = new Properties();
							pDetail.put(DETAIL_CONTENT, fileToPlay);
							pDetail.put(DETAIL_REASON, "010");
							ObservationManager.notify(new Event(EventSubject.EVENT_PLAY_ERROR,
									pDetail));
							FIFO.setFirstFile(false); // no more first file
						}
					}
				} else {
					// file no more exists
					Messages.getChoice(Messages.getErrorMessage(23), JOptionPane.DEFAULT_OPTION);
					FIFO.setFirstFile(false);
					// no more first file
					return;
				}
				// For last tracks playing, add all ready files from last
				// session stored FIFO
				if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST)
						|| ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
								STARTUP_MODE_LAST_KEEP_POS)) {
					File fifo = Util.getConfFileByPath(FILE_FIFO);
					if (!fifo.exists()) {
						Log.debug("No fifo file");
					} else {
						try {
							BufferedReader br = new BufferedReader(new FileReader(Util
									.getConfFileByPath(FILE_FIFO)));
							String s = null;
							for (; (s = br.readLine()) != null;) {
								org.jajuk.base.File file = FileManager.getInstance().getFileByID(s);
								if (file != null && file.isReady()) {
									alToPlay.add(file);
								}
							}
							br.close();
						} catch (IOException ioe) {
							Log.error(ioe);
						}
					}
				}
			} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
					STARTUP_MODE_SHUFFLE)) {
				alToPlay = FileManager.getInstance().getGlobalShufflePlaylist();
			} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
					STARTUP_MODE_BESTOF)) {
				alToPlay = FileManager.getInstance().getGlobalBestofPlaylist();
			} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
					STARTUP_MODE_NOVELTIES)) {
				alToPlay = FileManager.getInstance().getGlobalNoveltiesPlaylist();
				if (alToPlay != null && alToPlay.size() > 0) {
					// shuffle the selection
					Collections.shuffle(alToPlay, new Random());
				} else {
					// Alert user that no novelties have been found
					InformationJPanel.getInstance().setMessage(Messages.getString("Error.127"),
							InformationJPanel.ERROR);
				}
			}
			// launch selected file
			if (alToPlay != null && alToPlay.size() > 0) {
				FIFO.getInstance().push(
						Util.createStackItems(alToPlay, ConfigurationManager
								.getBoolean(CONF_STATE_REPEAT), false), false);
			}
		}
	}

	/**
	 * Auto-Mount required devices
	 * 
	 */
	private static void autoMount() {
		for (Device device : DeviceManager.getInstance().getDevices()) {
			if (device.getBooleanValue(XML_DEVICE_AUTO_MOUNT)) {
				try {
					device.mount();
				} catch (Exception e) {
					Log.error(112, device.getName(), e);
					// show a confirm dialog if the device can't be mounted,
					// we can't use regular Messages.showErrorMessage
					// because main window is not yet displayed
					String sError = Messages.getErrorMessage(112) + " : " + device.getName();
					InformationJPanel.getInstance().setMessage(sError, InformationJPanel.ERROR);
					continue;
				}
			}
		}
	}

	/**
	 * @return Returns the main window.
	 */
	public static JajukWindow getWindow() {
		return jw;
	}

	/**
	 * @return Returns whether jajuk is in exiting state
	 */
	public static boolean isExiting() {
		return bExiting;
	}

	/**
	 * Lauch UI
	 */
	public static void launchUI() throws Exception {
		if (bUILauched) {
			return;
		}
		// ui init
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// Light drag and drop for VLDocking
					UIManager.put("DragControler.paintBackgroundUnderDragRect", Boolean.FALSE);

					// Set windows decoration to look and feel
					JFrame.setDefaultLookAndFeelDecorated(true);
					JDialog.setDefaultLookAndFeelDecorated(true);

					// Prepare toolbars
					DockingUISettings.getInstance().installUI();
					tbcontainer = ToolBarContainer.createDefaultContainer(true, false, true, false);

					// starts ui
					jw = JajukWindow.getInstance();
					jw.setCursor(Util.WAIT_CURSOR);

					// Creates the panel
					jpFrame = (JPanel) jw.getContentPane();
					jpFrame.setOpaque(true);
					jpFrame.setLayout(new BorderLayout());

					// create the command bar
					command = CommandJPanel.getInstance();
					command.initUI();

					// Create the information bar panel
					information = InformationJPanel.getInstance();

					// Add information panel
					jpFrame.add(information, BorderLayout.SOUTH);

					// Create the perspective manager
					PerspectiveManager.load();

					// Set menu bar to the frame
					jw.setJMenuBar(JajukJMenuBar.getInstance());

					// Create the perspective tool bar panel
					perspectiveBar = PerspectiveBarJPanel.getInstance();
					jpFrame.add(perspectiveBar, BorderLayout.WEST);

					// Apply size and location BEFORE setVisible
					jw.applyStoredSize();

					// Display the frame
					jw.setVisible(true);

					// Apply watermark
					Util.setWatermark(ConfigurationManager.getProperty(CONF_OPTIONS_WATERMARK));

					// Apply size and location again
					// (required by Gnome for ie to fix the 0-sized maximized
					// frame)
					jw.applyStoredSize();

					// Initialize and add the desktop
					PerspectiveManager.init();

					// Add main container (contains toolbars + desktop)
					FormLayout layout = new FormLayout("f:d:grow", // columns
							"f:d:grow, 0dlu, d"); // rows
					PanelBuilder builder = new PanelBuilder(layout);
					CellConstraints cc = new CellConstraints();
					// Add items
					builder.add(tbcontainer, cc.xy(1, 1));
					builder.add(command, cc.xy(1, 3));
					jpFrame.add(builder.getPanel(), BorderLayout.CENTER);
					jw.setCursor(Util.DEFAULT_CURSOR);

					// Upgrade step2
					UpgradeManager.upgradeStep2();

					// Display tip of the day if required
					if (ConfigurationManager.getBoolean(CONF_SHOW_TIP_ON_STARTUP)) {
						TipOfTheDay tipsView = new TipOfTheDay();
						tipsView.setLocationRelativeTo(jw);
						tipsView.setVisible(true);
					}

				} catch (Exception e) { // last chance to catch any error for
					// logging purpose
					e.printStackTrace();
					Log.error(106, e);
				} finally {
					if (sc != null) {
						// Display progress
						sc.setProgress(100);
						sc.splashOff();
					}
					bUILauched = true;
					Util.stopWaiting();
				}
			}
		});

	}

	/** Launch tray, only for linux and windows, not mac for the moment */
	private static void launchTray() throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (Util.isUnderLinux() || Util.isUnderWindows()) {
					jsystray = JajukSystray.getInstance();
				}
			}
		});
	}

	/**
	 * @return Returns the bUILauched.
	 */
	public static boolean isUILaunched() {
		return bUILauched;
	}

	/**
	 * @return Returns the sPerspective.
	 */
	public static String getDefaultPerspective() {
		return sPerspective;
	}

	/**
	 * @param perspective
	 *            The sPerspective to set.
	 */
	public static void setDefaultPerspective(String perspective) {
		sPerspective = perspective;
	}

	/**
	 * 
	 * @return the systray
	 */
	public static JajukSystray getSystray() {
		return jsystray;
	}

	/**
	 * @return toolbar container
	 */
	public static ToolBarContainer getToolbarContainer() {
		return tbcontainer;
	}

	/**
	 * @return true if it is the first session after a minor or major upgrade
	 *         session
	 */
	public static boolean isUpgradeDetected() {
		return bUpgraded;
	}

	public static boolean isCrashRecover() {
		return bCrashRecover;
	}

}
