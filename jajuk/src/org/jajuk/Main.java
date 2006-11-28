/*
 * Jajuk Copyright (C) 2003 Bertrand Florat
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

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
import org.jajuk.dj.AmbienceManager;
import org.jajuk.dj.DigitalDJManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.FirstTimeWizard;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.ui.JajukSystray;
import org.jajuk.ui.JajukWindow;
import org.jajuk.ui.LNFManager;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.TipOfTheDay;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.util.JVM;

import com.vlsolutions.swing.docking.DockingPreferences;
import com.vlsolutions.swing.docking.ui.DockingUISettings;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarIO;

import ext.JSplash;

/**
 * Jajuk launching class
 * 
 * @author Bertrand Florat
 * @created 3 oct. 2003
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

	/** No taskbar presence flag when window is minimized (only tray) */
	private static boolean bNoTaskBar = false;

	/** default perspective to shoose, if null, we take the configuration one */
	private static String sPerspective;

	/** Server socket used to check other sessions */
	private static ServerSocket ss;

	/** Is it a minor or major X.Y upgrade */
	private static boolean bUpgraded = false;

	/** Is it the first seesion ever ? */
	private static boolean bFirstSession = false;

	/** Mplayer state */
	private static MPlayerStatus mplayerStatus;

	/** MPlayer status possible values * */
	public static enum MPlayerStatus {
		MPLAYER_STATUS_OK, MPLAYER_STATUS_NOT_FOUND, MPLAYER_STATUS_WRONG_VERSION
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
				System.out
						.println("Java Runtime Environment 1.5 minimum required."
								+ " You use a JVM " + JVM.current()); //$NON-NLS-1$
				System.exit(2); // error code 2 : wrong JVM
			}
			// set command line options
			for (int i = 0; i < args.length; i++) {
				// Tells jajuk it is inside the IDE (usefull to find right
				// location for images and jar resources)
				if (args[i].equals("-" + CLI_IDE)) {//$NON-NLS-1$
					bIdeMode = true;
				}
				// if selected, no jajuk window at startup, only tray
				if (args[i].equals("-" + CLI_NOTASKBAR)) {//$NON-NLS-1$
					bNoTaskBar = true;
				}
				// Tells jajuk to use a .jajuk_test repository
				if (args[i].equals("-" + CLI_TEST)) {//$NON-NLS-1$
					bTestMode = true;
				}
			}

			// perform initial checkups and create needed files
			initialCheckups();

			// log startup depends on : setExecLocation, initialCheckups
			Log.getInstance();
			Log.setVerbosity(Log.DEBUG);

			// Configuration manager startup. Depends on: initialCheckups
			org.jajuk.util.ConfigurationManager.getInstance();

			// Register locals, needed by ConfigurationManager to choose
			// default language
			Messages.getInstance().registerLocal("en", "Language_desc_en"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("fr", "Language_desc_fr"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("de", "Language_desc_de"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("it", "Language_desc_it"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("sv", "Language_desc_sv"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("nl", "Language_desc_nl"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("zh", "Language_desc_zh"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("es", "Language_desc_es"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("ca", "Language_desc_ca"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("ko", "Language_desc_ko"); //$NON-NLS-1$ //$NON-NLS-2$

			// Set default local (from system). Depends on registerLocal
			ConfigurationManager.getInstance().setSystemLocal();

			// Load user configuration. Depends on: initialCheckups,
			// setSystemLocal
			ConfigurationManager.load();

			// Upgrade detection. Depends on: Configuration manager load
			String sRelease = ConfigurationManager.getProperty(CONF_RELEASE);
			/*
			 * See if it is a new major 'x.y' release: 1.2 != 1.3 for instance
			 */
			if (!bFirstSession
			/*
			 * if first session, not conciderated as an upgrade
			 */
			&& (sRelease == null || // null for jajuk releases < 1.2
					!sRelease.substring(0, 3).equals(
							JAJUK_VERSION.substring(0, 3)))) {
				bUpgraded = true;
			}
			// Now set current release in the conf
			ConfigurationManager.setProperty(CONF_RELEASE, JAJUK_VERSION);

			// Set actual log verbosity. Depends on:
			// ConfigurationManager.load
			// test mode is always in debug mode
			if (!bTestMode) {
				Log.setVerbosity(Integer.parseInt(ConfigurationManager
						.getProperty(CONF_OPTIONS_LOG_LEVEL)));
			}
			// Set locale. setSystemLocal
			Messages.getInstance().setLocal(
					ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE));

			// Registers supported look and feels
			LNFManager.register(LNF_METAL, LNF_METAL_CLASS);
			LNFManager.register(LNF_WINDOWS, LNF_WINDOWS_CLASS);
			LNFManager.register(LNF_KUNSTSTOFF, LNF_KUNSTSTOFF_CLASS);
			LNFManager.register(LNF_LIQUID, LNF_LIQUID_CLASS);
			LNFManager.register(LNF_PLASTIC, LNF_PLASTIC_CLASS);
			LNFManager.register(LNF_PLASTICXP, LNF_PLASTICXP_CLASS);
			LNFManager.register(LNF_PLASTIC3D, LNF_PLASTIC3D_CLASS);
			LNFManager.register(LNF_INFONODE, LNF_INFONODE_CLASS);
			LNFManager.register(LNF_SQUARENESS, LNF_SQUARENESS_CLASS);
			LNFManager.register(LNF_TINY, LNF_TINY_CLASS);
			LNFManager.register(LNF_LOOKS, LNF_LOOKS_CLASS);

			// Launch splashscreen. Depends on: log.setVerbosity,
			// configurationManager.load (for local)
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					// Set look and feel, needs local to be set for error
					// messages
					LNFManager.setLookAndFeel(ConfigurationManager
							.getProperty(CONF_OPTIONS_LNF));
					sc = new JSplash(IMAGES_SPLASHSCREEN, true, true, false,
							JAJUK_COPYRIGHT, JAJUK_VERSION + " "
									+ JAJUK_VERSION_DATE, new Font("Dialog",
									Font.TRUETYPE_FONT, 12), null); //$NON-NLS-1$
					sc.setTitle(Messages.getString("JajukWindow.3")); //$NON-NLS-1$
					sc.splashOn();
				}
			});

			// Display progress
			sc.setProgress(0, Messages.getString("SplashScreen.0")); //$NON-NLS-1$

			// Registers ItemManager managers
			ItemManager.registerItemManager(org.jajuk.base.Album.class,
					AlbumManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Author.class,
					AuthorManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Device.class,
					DeviceManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.File.class,
					FileManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Directory.class,
					DirectoryManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.PlaylistFile.class,
					PlaylistFileManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Playlist.class,
					PlaylistManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Style.class,
					StyleManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Track.class,
					TrackManager.getInstance());
			ItemManager.registerItemManager(org.jajuk.base.Type.class,
					TypeManager.getInstance());

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
					Messages.getString("Device_type.directory"));//$NON-NLS-1$
			DeviceManager.getInstance().registerDeviceType(
					Messages.getString("Device_type.file_cd"));//$NON-NLS-1$
			DeviceManager.getInstance().registerDeviceType(
					Messages.getString("Device_type.network_drive"));//$NON-NLS-1$
			DeviceManager.getInstance().registerDeviceType(
					Messages.getString("Device_type.extdd"));//$NON-NLS-1$
			DeviceManager.getInstance().registerDeviceType(
					Messages.getString("Device_type.player"));//$NON-NLS-1$
			DeviceManager.getInstance().registerDeviceType(
					Messages.getString("Device_type.remote"));//$NON-NLS-1$

			// registers supported audio supports and default properties
			registerTypes();

			// Display progress
			sc.setProgress(10, Messages.getString("SplashScreen.1")); //$NON-NLS-1$

			// Load collection
			loadCollection();

			// Clean the collection up
			Collection.cleanup();

			// Display progress
			sc.setProgress(70, Messages.getString("SplashScreen.2")); //$NON-NLS-1$

			// Load history
			History.load();

			// Load ambiences
			AmbienceManager.getInstance().load();

			// Load djs
			DigitalDJManager.getInstance().loadAllDJs();

			// start exit hook
			Thread tHook = new Thread() {
				public void run() {
					Log.debug("Exit Hook begin");//$NON-NLS-1$
					try {
						Player.stop(true); // stop sound ASAP
					} catch (Exception e) {
						e.printStackTrace();
						// no log to make sure to reach collection commit
					}
					try {
						if (iExitCode == 0) {
							/*
							 * commit only if exit is safe (to avoid commiting
							 * empty collection) commit ambiences
							 */
							AmbienceManager.getInstance().commit();
							// commit configuration
							org.jajuk.util.ConfigurationManager.commit();
							// commit history
							History.commit();
							// commit perspectives
							PerspectiveManager.commit();
							// Commit collection if not refreshing ( fix for
							// 939816 )
							if (!DeviceManager.getInstance()
									.isAnyDeviceRefreshing()) {
								Collection.commit(FILE_COLLECTION_EXIT);
								// create a proof file
								Util
										.createEmptyFile(FILE_COLLECTION_EXIT_PROOF);
							}
							// Commit toolbars
							ToolBarIO tbIO = new ToolBarIO(tbcontainer);
							FileOutputStream out = new FileOutputStream(
									FILE_TOOLBARS_CONF);
							tbIO.writeXML(out);
							out.flush();
							out.close();
							/* release intellipad resources */
							if (Util.isUnderWindows()) {
								org.jajuk.ui.action.ActionBase.cleanup();
							}
						}
					} catch (Exception e) {
						Log.error(e); //$NON-NLS-1$
					} finally {
						Log.debug("Exit Hook end");//$NON-NLS-1$
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

			// Start up action manager. TO be done before lauching ui and
			// tray
			ActionManager.getInstance();

			// show window if set in the systray conf.
			if (ConfigurationManager.getBoolean(CONF_SHOW_AT_STARTUP)) {
				// Display progress
				sc.setProgress(80, Messages.getString("SplashScreen.3")); //$NON-NLS-1$
				launchUI();
			}

			// start the tray
			launchTray();

		} catch (JajukException je) { // last chance to catch any error for
			// logging purpose
			Log.error(je);
			if (je.getCode().equals("005")) { //$NON-NLS-1$
				Messages.getChoice(Messages.getErrorMessage("005"),
						JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				exit(1);
			}
		} catch (Exception e) { // last chance to catch any error for logging
			// purpose
			e.printStackTrace();
			Log.error("106", e); //$NON-NLS-1$
			exit(1);
		} catch (Error error) { // last chance to catch any error for logging
			// purpose
			error.printStackTrace();
			Log.error("106", error); //$NON-NLS-1$
			exit(1);
		} finally { // make sure to close splashscreen in all cases (ie if
			// UI is not started)
			if (!ConfigurationManager.getBoolean(CONF_SHOW_AT_STARTUP)
					&& sc != null) {
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
		// check for jajuk directory
		File fJajukDir = new File(FILE_JAJUK_DIR);
		if (!fJajukDir.exists()) {
			bFirstSession = true; // first session ever
			fJajukDir.mkdir(); // create the directory if it doesn't exist
		}
		// check for configuration file presence
		File fConfig = new File(FILE_CONFIGURATION);
		if (!fConfig.exists()) { // if config file doesn't exit, create
			// it with default values
			org.jajuk.util.ConfigurationManager.commit();
		}
		// check for history.xml file
		File fHistory = new File(FILE_HISTORY);
		if (!fHistory.exists()) { // if history file doesn't exit, create
			// it empty
			History.commit();
		}
		// check for image cache presence
		File fCache = new File(FILE_IMAGE_CACHE);
		if (!fCache.exists()) {
			fCache.mkdir();
		}
		// check for thumbnails cache presence
		File fThumbs = new File(FILE_THUMBS);
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = new File(FILE_THUMBS + "/50x50"); //$NON-NLS-1$
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = new File(FILE_THUMBS + "/100x100"); //$NON-NLS-1$
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = new File(FILE_THUMBS + "/150x150"); //$NON-NLS-1$
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		fThumbs = new File(FILE_THUMBS + "/200x200"); //$NON-NLS-1$
		if (!fThumbs.exists()) {
			fThumbs.mkdir();
		}
		// check for default covers
		fThumbs = new File(FILE_THUMBS + "/50x50/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$
		if (!fThumbs.exists()) {
			Util.createThumbnail(Util.getIcon(IMAGE_NO_COVER), fThumbs, 50);
		}
		fThumbs = new File(FILE_THUMBS + "/100x100/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$
		if (!fThumbs.exists()) {
			Util.createThumbnail(Util.getIcon(IMAGE_NO_COVER), fThumbs, 100);
		}
		fThumbs = new File(FILE_THUMBS + "/150x150/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$
		if (!fThumbs.exists()) {
			Util.createThumbnail(Util.getIcon(IMAGE_NO_COVER), fThumbs, 150);
		}
		fThumbs = new File(FILE_THUMBS + "/200x200/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$
		if (!fThumbs.exists()) {
			Util.createThumbnail(Util.getIcon(IMAGE_NO_COVER), fThumbs, 200);
		}
		// check for djs directory
		File fdjs = new File(FILE_DJ_DIR);
		if (!fdjs.exists()) {
			fdjs.mkdir();
		}
	}

	/**
	 * Registers supported audio supports and default properties
	 */
	private static void registerTypes() {
		try {
			// mplayer tests: 0: OK, 1: no mplayer in path, 2: wrong mplayer
			// release
			// test mplayer presence in PATH
			mplayerStatus = MPlayerStatus.MPLAYER_STATUS_OK;
			if (Util.isUnderWindows()) {
				// try to find mplayer executable in know locations first
				if (Util.getMPlayerPath() == null ||
				// if file exists, test size
						new File(Util.getMPlayerPath()).length() != MPLAYER_EXE_SIZE) {
					// probably in JNLP mode or wrong size,
					// try to download static mplayer distro if needed
					try {
						Log.debug("Download Mplayer from: "
								+ ConfigurationManager
										.getProperty(CONF_MPLAYER_URL));
						DownloadManager.download(new URL(ConfigurationManager
								.getProperty(CONF_MPLAYER_URL)), new File(
								FILE_JAJUK_DIR + "/" + FILE_MPLAYER_EXE));
					} catch (Exception e) {
						mplayerStatus = MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
					}
				}
			}
			// Under non-windows OS, we assume mplayer has been installed
			// using external standard distributions
			else {
				Process proc = null;
				mplayerStatus = MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
				try {
					proc = Runtime.getRuntime().exec("mplayer");
					proc.waitFor();
					// check Mplayer release : 1.0pre8 min
					proc = Runtime.getRuntime().exec(
							new String[] { "mplayer", "-input", "cmdlist" });
					BufferedReader in = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					String line = null;
					mplayerStatus = MPlayerStatus.MPLAYER_STATUS_WRONG_VERSION;
					for (; (line = in.readLine()) != null;) {
						if (line.matches("get_time_pos.*")) {
							mplayerStatus = MPlayerStatus.MPLAYER_STATUS_OK;
							break;
						}
					}
				} catch (IOException ioe) {
					mplayerStatus = MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
				}
			}
			// Choose player according to mplayer presence or not
			if (mplayerStatus != MPlayerStatus.MPLAYER_STATUS_OK) { // no
				// mplayer
				// Show mplayer warnings
				if (mplayerStatus != MPlayerStatus.MPLAYER_STATUS_OK) { // no
					// mplayer
					// Test if user didn't already select "don't show again"
					if (!ConfigurationManager
							.getBoolean(CONF_NOT_SHOW_AGAIN_PLAYER)) {
						if (mplayerStatus == MPlayerStatus.MPLAYER_STATUS_NOT_FOUND) {
							// No mplayer
							Messages.showHideableWarningMessage(Messages
									.getString("Warning.0"),
									CONF_NOT_SHOW_AGAIN_PLAYER);
						} else if (mplayerStatus == MPlayerStatus.MPLAYER_STATUS_WRONG_VERSION) {
							// wrong mplayer release
							Messages.showHideableWarningMessage(Messages
									.getString("Warning.1"),
									CONF_NOT_SHOW_AGAIN_PLAYER);
						}
					}
				}
				// mp3
				Type type = TypeManager.getInstance().registerType(
						Messages.getString("Type.mp3"), EXT_MP3,
						Class.forName(PLAYER_IMPL_JAVALAYER),
						Class.forName(TAG_IMPL_ENTAGGED)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_MP3);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_MP3);
				// playlists
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.playlist"), EXT_PLAYLIST,
						Class.forName(PLAYER_IMPL_JAVALAYER), null); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, false); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, false); //$NON-NLS-1$
				// Ogg vorbis
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.ogg"), EXT_OGG,
						Class.forName(PLAYER_IMPL_JAVALAYER),
						Class.forName(TAG_IMPL_ENTAGGED)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, false); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_OGG);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_OGG);
				// Wave
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.wav"), EXT_WAV,
						Class.forName(PLAYER_IMPL_JAVALAYER),
						Class.forName(TAG_IMPL_NO_TAGS)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_WAVE);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_WAV);
				// au
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.au"), EXT_AU,
						Class.forName(PLAYER_IMPL_JAVALAYER),
						Class.forName(TAG_IMPL_NO_TAGS)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, false); //$NON-NLS-1$
				type
						.setProperty(XML_TYPE_TECH_DESC,
								TYPE_PROPERTY_TECH_DESC_AU);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_AU);
			} else { // mplayer enabled
				// mp3
				Type type = TypeManager.getInstance().registerType(
						Messages.getString("Type.mp3"), EXT_MP3,
						Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_MP3);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_MP3);
				// playlists
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.playlist"), EXT_PLAYLIST,
						Class.forName(PLAYER_IMPL_JAVALAYER), null); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, false); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, false); //$NON-NLS-1$
				// Ogg vorbis
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.ogg"), EXT_OGG,
						Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_OGG);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_OGG);
				// Wave
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.wav"), EXT_WAV,
						Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_NO_TAGS)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_WAVE);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_WAV);
				// au
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.au"), EXT_AU,
						Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_NO_TAGS)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type
						.setProperty(XML_TYPE_TECH_DESC,
								TYPE_PROPERTY_TECH_DESC_AU);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_AU);
				// flac
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.flac"), EXT_FLAC,
						Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_FLAC);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_FLAC);
				// WMA
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.wma"), EXT_WMA,
						Class.forName(PLAYER_IMPL_MPLAYER),
						Class.forName(TAG_IMPL_ENTAGGED)); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_WMA);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_WMA);
				// AAC
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.aac"), EXT_AAC,
						Class.forName(PLAYER_IMPL_MPLAYER), null); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_AAC);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_AAC);
				// Real audio
				type = TypeManager.getInstance().registerType(
						Messages.getString("Type.real"), EXT_REAL,
						Class.forName(PLAYER_IMPL_MPLAYER), null); //$NON-NLS-1$ //$NON-NLS-2$
				type.setProperty(XML_TYPE_IS_MUSIC, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_SEEK_SUPPORTED, true); //$NON-NLS-1$
				type.setProperty(XML_TYPE_TECH_DESC,
						TYPE_PROPERTY_TECH_DESC_RAM);
				type.setProperty(XML_TYPE_ICON, ICON_TYPE_RAM);
			}
		} catch (Exception e1) {
			Log.error("026", e1); //$NON-NLS-1$
		}
	}

	/**
	 * check if another session is already started
	 * 
	 */
	private static void checkOtherSession() {
		// check for a concurrent jajuk session, try to create a new server
		// socket
		try {
			ss = new ServerSocket(PORT);
			// No error? jajuk was not started, leave
		} catch (IOException e) { // error? looks like Jajuk is already
			// started
			if (sc != null) {
				sc.dispose();
			}
			Log.error("124"); //$NON-NLS-1$
			Messages.getChoice(Messages.getErrorMessage("124"), //$NON-NLS-1$
					JOptionPane.DEFAULT_OPTION); //$NON-NLS-1$
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
		if (Boolean.valueOf(
				ConfigurationManager.getProperty(CONF_CONFIRMATIONS_EXIT))
				.booleanValue()) {
			int iResu = Messages.getChoice(Messages
					.getString("Confirmation_exit"),
					JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			if (iResu != JOptionPane.YES_OPTION) {
				return;
			}
		}
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
		ObservationManager
				.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
		if (jw != null)
			jw.setShown(false);
		// hide systray
		if (jsystray != null)
			jsystray.closeSystray();
		// display a message
		Log.debug("Exit with code: " + iExitCode); //$NON-NLS-1$
		System.exit(iExitCode);
	}

	/**
	 * Load persisted collection file
	 */
	private static void loadCollection() {
		if (ConfigurationManager.getBoolean(CONF_FIRST_CON)) {
			Log.info("First session, collection will be created");//$NON-NLS-1$
			return;
		}
		File fCollection = new File(FILE_COLLECTION);
		File fCollectionExit = new File(FILE_COLLECTION_EXIT);
		File fCollectionExitProof = new File(FILE_COLLECTION_EXIT_PROOF);
		// check if previous exit was OK
		boolean bParsingOK = true;
		try {
			if (fCollectionExit.exists() && fCollectionExitProof.exists()) {
				fCollectionExitProof.delete(); // delete this file created just
				// after collection exit commit
				Collection.load(FILE_COLLECTION_EXIT);
				// parsing of collection exit ok, use this collection file as
				// final collection
				fCollectionExit.renameTo(fCollection);
				// backup the collection
				Util.backupFile(new File(FILE_COLLECTION), ConfigurationManager
						.getInt(CONF_BACKUP_SIZE));
			} else {
				throw new JajukException("005"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			Log.error("005", fCollectionExit.getAbsolutePath(), e); //$NON-NLS-1$
			Log
					.debug("Jajuk was not closed properly during previous session, try to load previous collection file"); //$NON-NLS-1$
			if (fCollectionExit.exists()) {
				fCollectionExit.delete();
			}
			try {
				// try to load "official" collection file, should be OK but not
				// always up-to-date
				Collection.load(FILE_COLLECTION);
			} catch (Exception e2) {
				// not better? strange
				Log.error("005", fCollection.getAbsolutePath(), e2); //$NON-NLS-1$
				bParsingOK = false;
			}
		}
		if (!bParsingOK) { // even final collection file parsing failed
			// (very unlikely), try to restore a backup file
			File[] fBackups = new File(FILE_JAJUK_DIR)
					.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							if (name.indexOf("backup") != -1) { //$NON-NLS-1$
								return true;
							}
							return false;
						}
					});
			ArrayList<File> alBackupFiles = new ArrayList<File>(Arrays
					.asList(fBackups));
			Collections.sort(alBackupFiles); // sort alphabeticaly (newest
			// last)
			Collections.reverse(alBackupFiles); // newest first now
			Iterator it = alBackupFiles.iterator();
			// parse all backup files, newest first
			while (!bParsingOK && it.hasNext()) {
				File file = (File) it.next();
				try {
					Collection.load(file.getAbsolutePath());
					bParsingOK = true;
					int i = Messages
							.getChoice(
									Messages.getString("Error.133") + ":\n" + file.getAbsolutePath(), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					if (i == JOptionPane.CANCEL_OPTION) {
						System.exit(-1);
					}
					break;
				} catch (Exception e2) {
					Log.error("005", file.getAbsolutePath(), e2); //$NON-NLS-1$
				}
			}
			if (!bParsingOK) { // not better? ok, commit and load a void
				// collection
				Collection.cleanup();
				DeviceManager.getInstance().cleanAllDevices();
				try {
					Collection.commit(FILE_COLLECTION);
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
		if (!ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
				STARTUP_MODE_NOTHING)) {
			if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
					STARTUP_MODE_LAST)
					|| ConfigurationManager.getProperty(CONF_STARTUP_MODE)
							.equals(STARTUP_MODE_LAST_KEEP_POS)
					|| ConfigurationManager.getProperty(CONF_STARTUP_MODE)
							.equals(STARTUP_MODE_FILE)) {

				if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
						STARTUP_MODE_FILE)) {
					fileToPlay = FileManager.getInstance()
							.getFileByID(
									ConfigurationManager
											.getProperty(CONF_STARTUP_FILE));
				} else {
					// last file from begining or last file keep position
					if (ConfigurationManager.getBoolean(CONF_STATE_WAS_PLAYING)
							&& History.getInstance().getHistory().size() > 0) {
						// make sure user didn't exit jajuk in the stopped state
						// and that history is not void
						fileToPlay = FileManager.getInstance().getFileByID(
								History.getInstance().getLastFile());
					} else {
						// do not try to lauch anything, stay in stop state
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
								+ ", try to mount it"); //$NON-NLS-1$
						try {
							fileToPlay.getDevice().mount(true);
							Log.debug("Mount OK"); //$NON-NLS-1$
							alToPlay.add(fileToPlay);
						} catch (Exception e) {
							Log.debug("Mount failed"); //$NON-NLS-1$
							Properties pDetail = new Properties();
							pDetail.put(DETAIL_CURRENT_FILE, fileToPlay);
							pDetail.put(DETAIL_REASON, "010");//$NON-NLS-1$
							ObservationManager.notify(new Event(
									EventSubject.EVENT_PLAY_ERROR, pDetail));
							FIFO.setFirstFile(false); // no more first file
						}
					}
				} else {
					// file no more exists
					Messages.getChoice(Messages.getErrorMessage("023"),
							JOptionPane.DEFAULT_OPTION); //$NON-NLS-1$
					FIFO.setFirstFile(false);
					// no more first file
					return;
				}
				// For last tracks playing, add all ready files from last
				// session stored FIFO
				if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
						STARTUP_MODE_LAST)
						|| ConfigurationManager.getProperty(CONF_STARTUP_MODE)
								.equals(STARTUP_MODE_LAST_KEEP_POS)) {
					File fifo = new File(FILE_FIFO);
					if (!fifo.exists()) {
						Log.debug("No fifo file");
					} else {
						try {
							BufferedReader br = new BufferedReader(
									new FileReader(FILE_FIFO));
							String s = null;
							for (; (s = br.readLine()) != null;) {
								org.jajuk.base.File file = FileManager
										.getInstance().getFileByID(s);
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
			} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE)
					.equals(STARTUP_MODE_SHUFFLE)) {
				alToPlay = FileManager.getInstance().getGlobalShufflePlaylist();
			} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE)
					.equals(STARTUP_MODE_BESTOF)) {
				alToPlay = FileManager.getInstance().getGlobalBestofPlaylist();
			} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE)
					.equals(STARTUP_MODE_NOVELTIES)) {
				alToPlay = FileManager.getInstance()
						.getGlobalNoveltiesPlaylist();
				if (alToPlay != null && alToPlay.size() > 0) {
					Collections.shuffle(alToPlay, new Random(System
							.currentTimeMillis()));// shuffle the selection
				} else {
					//Alert user that no novelties have been found
					InformationJPanel.getInstance().setMessage(
							Messages.getString("Error.127"),
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
					Log.error("112", device.getName(), e); //$NON-NLS-1$
					// show a confirm dialog if the device can't be mounted,
					// we can't use regular Messages.showErrorMessage
					// because main window is not yet displayed
					String sError = Messages.getErrorMessage("112") + " : " + device.getName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-2$
					InformationJPanel.getInstance().setMessage(sError,
							InformationJPanel.ERROR); //$NON-NLS-1$
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
					// Init heavyweight support (for jdic)
					// have to be done here, not after
					DockingPreferences.initHeavyWeightUsage();
					DockingPreferences.setSingleHeavyWeightComponent(true);

					// Set look and feel, needs local to be set for error
					// messages
					LNFManager.setLookAndFeel(ConfigurationManager
							.getProperty(CONF_OPTIONS_LNF));

					// Prepare toolbars
					DockingUISettings.getInstance().installUI();
					tbcontainer = ToolBarContainer.createDefaultContainer(true,
							true, true, true);
					tbcontainer.setOpaque(false);
					
					// starts ui
					jw = JajukWindow.getInstance();
					jw.setCursor(Util.WAIT_CURSOR);

					// Creates the panel
					jpFrame = (JPanel) jw.getContentPane();
					jpFrame.setOpaque(true);
					jpFrame.setLayout(new BorderLayout());
					
					// Set menu bar to the frame
					jw.setJMenuBar(JajukJMenuBar.getInstance());

					// create the command bar
					command = CommandJPanel.getInstance();
					command.initUI();

					// Create the information bar panel
					information = InformationJPanel.getInstance();

					// Add static panels
					jpFrame.add(command, BorderLayout.NORTH);
					jpFrame.add(information, BorderLayout.SOUTH);

					// Create the perspective manager
					PerspectiveManager.load();

					// Create the perspective tool bar panel
					perspectiveBar = PerspectiveBarJPanel.getInstance();
					jpFrame.add(perspectiveBar, BorderLayout.WEST);

					/*
					 * display main window We have to apply position and size
					 * before and after window made visible - If position/size
					 * are set after, we can see a small window on screen at
					 * random position because in some cases, the window is
					 * displayed at random position - If position/size are set
					 * only after, position can be lost this way, the window is
					 * always at right position
					 */
					jw.applyStoredSize(); // apply size and position as
					// stored in the user properties
					jw.setVisible(true); // show main window
					jw.applyStoredSize(); // apply size and position as
					// stored in the user properties

					// Display info message if first session
					if (ConfigurationManager.getBoolean(CONF_FIRST_CON)
							&& DeviceManager.getInstance().getElementCount() == 0) {
						/*
						 * make none device already exist to avoid checking
						 * availability
						 */
						sc.dispose(); // make sure to hide splashscreen
						// First time wizard
						FirstTimeWizard fsw = new FirstTimeWizard();
						fsw.pack();
						fsw.setLocationRelativeTo(jw);
						fsw.setVisible(true);
						ConfigurationManager.setProperty(CONF_FIRST_CON, FALSE);
					}

					// Initialize and add the desktop
					PerspectiveManager.init();

					// Add main container (contains toolbars + desktop)
					jpFrame.add(tbcontainer, BorderLayout.CENTER);
					jw.setCursor(Util.DEFAULT_CURSOR);
					jw.addComponentListener();

					// Upgrade step2
					UpgradeManager.upgradeStep2();

					// Display tip of the day if required
					if (ConfigurationManager
							.getBoolean(CONF_SHOW_TIP_ON_STARTUP)) {
						TipOfTheDay tipsView = new TipOfTheDay();
						tipsView.setLocationRelativeTo(jw);
						tipsView.setVisible(true);
					}

				} catch (Exception e) { // last chance to catch any error for
					// logging purpose
					e.printStackTrace();
					Log.error("106", e); //$NON-NLS-1$
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

	/** Lauch tray, only for linux and windows, not mac for the moment */
	private static void launchTray() throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (Util.isUnderLinux() || Util.isUnderWindows()) {
					LNFManager.setLookAndFeel(ConfigurationManager
							.getProperty(CONF_OPTIONS_LNF));
					jsystray = JajukSystray.getInstance();
				}
			}
		});
	}

	/**
	 * @return Returns the bUILauched.
	 */
	public static boolean isUILauched() {
		return bUILauched;
	}

	/**
	 * @return Returns the bForceTaskBar.
	 */
	public static boolean isNoTaskBar() {
		return bNoTaskBar;
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

	/**
	 * @return true if it first seession ever
	 */
	public static boolean isVeryFirstSession() {
		return bFirstSession;
	}

}
