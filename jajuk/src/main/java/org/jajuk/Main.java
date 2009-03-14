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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.vlsolutions.swing.docking.ui.DockingUISettings;

import ext.JSplash;
import ext.JVM;

import java.awt.BorderLayout;
import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jajuk.base.AlbumManager;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Collection;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.ItemManager;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.StyleManager;
import org.jajuk.base.TrackManager;
import org.jajuk.base.TypeManager;
import org.jajuk.base.YearManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.core.StartupService;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.lastfm.LastFmManager;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.CommandJPanel;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJMenuBar;
import org.jajuk.ui.widgets.JajukSystray;
import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.ui.widgets.PerspectiveBarJPanel;
import org.jajuk.ui.wizard.FirstTimeWizard;
import org.jajuk.ui.wizard.TipOfTheDayWizard;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;
import org.jvnet.substance.skin.SubstanceBusinessLookAndFeel;

/**
 * Jajuk launching class
 */
public final class Main {

  /** Left side perspective selection panel */
  private static PerspectiveBarJPanel perspectiveBar;

  /** Main frame panel */
  private static JPanel jpFrame;

  /** specific perspective panel */
  private static JPanel perspectivePanel;

  /** splash screen */
  private static JSplash sc;

  /** Debug mode */
  private static boolean bIdeMode = false;

  /** Test mode */
  private static boolean bTestMode = false;

  /** Jukebox power pack flag* */
  private static boolean bPowerPack = false;

  /** Does a collection parsing error occurred ? * */
  private static boolean bCollectionLoadRecover = true;

  /** UI launched flag */
  private static boolean bUILauched = false;

  /** default perspective to choose, if null, we take the configuration one */
  private static String sPerspective;

  /** Lock used to trigger a first time wizard device creation and refresh * */
  private static short[] canLaunchRefresh = new short[0];

  /** Lock used to trigger first time wizard window close* */
  private static short[] isFirstTimeWizardClosed = new short[0];

  /** Mplayer state */
  private static UtilSystem.MPlayerStatus mplayerStatus;

  /** Workspace PATH* */
  private static String workspace;

  /** DeviceTypes Identification strings */
  public static final String[] DEVICE_TYPES = { "Device_type.directory", "Device_type.file_cd",
      "Device_type.network_drive", "Device_type.extdd", "Device_type.player" };

  private static final String[] CONFIG_CHECKS = { Const.FILE_CONFIGURATION, Const.FILE_HISTORY };

  private static final String[] DIR_CHECKS = {
      // internal pictures cache directory
      Const.FILE_CACHE + '/' + Const.FILE_INTERNAL_CACHE,
      // thumbnails directories and sub-directories
      Const.FILE_THUMBS, Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_50X50,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_100X100,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_150X150,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_200X200,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_250X250,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_300X300,
      // DJs directories
      Const.FILE_DJ_DIR };

  /** Directory used to flag the current jajuk session */
  private static File sessionIdFile;

  /**
   * private constructor to avoid instantiating utility class
   */
  private Main() {
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
      if (!JVM.current().isOrLater(JVM.JDK1_6)) {
        System.out.println("Java Runtime Environment 1.6 minimum required." + " You use a JVM "
            + JVM.current());
        System.exit(2); // error code 2 : wrong JVM
      }
      // set flags from command line options
      handleCommandline(args);

      // Set substance theme (for raw error windows displayed by initial
      // checkups only)
      // (must be done out of EDT)
      UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());

      // Set default fonts
      FontManager.getInstance().setDefaultFont();

      // perform initial checkups and create needed files
      initialCheckups();

      // log startup depends on : initialCheckups
      Log.getInstance();
      Log.setVerbosity(Log.DEBUG);

      // Load user configuration. Depends on: initialCheckups
      Conf.load();

      // Full substance configuration now (must be done out of EDT)
      UtilGUI.setLookAndFeel(Conf.getString(Const.CONF_OPTIONS_LNF));

      // Detect current release
      UpgradeManager.detectRelease();

      // Set actual log verbosity. Depends on:
      // Conf.load
      if (!bTestMode) {
        // test mode is always in debug mode
        Log.setVerbosity(Integer.parseInt(Conf.getString(Const.CONF_OPTIONS_LOG_LEVEL)));
      }
      // Set locale. setSystemLocal
      Messages.setLocal(Conf.getString(Const.CONF_OPTIONS_LANGUAGE));

      // Launch splashscreen. Depends on: log.setVerbosity,
      // configurationManager.load (for local)
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          // Set default fonts
          FontManager.getInstance().setDefaultFont();
          sc = new JSplash(Const.IMAGES_SPLASHSCREEN, true, true, false, Const.JAJUK_COPYRIGHT,
              Const.JAJUK_VERSION + " \"" + Const.JAJUK_CODENAME + "\"" + " "
                  + Const.JAJUK_VERSION_DATE, FontManager.getInstance().getFont(JajukFont.SPLASH));
          sc.setTitle(Messages.getString("JajukWindow.3"));
          sc.setProgress(0, Messages.getString("SplashScreen.0"));
          // Actually show the splashscreen only if required
          if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_WINDOW_TRAY) {
            sc.splashOn();
          }
        }
      });

      // Apply any proxy (requires load conf)
      DownloadManager.setDefaultProxySettings();

      // Registers ItemManager managers
      registerItemManagers();

      // Upgrade configuration from previous releases
      UpgradeManager.upgradeStep1();

      // Display user system configuration
      Log.debug("Workspace used: " + workspace);
      Log.debug(UtilString.getAnonymizedSystemProperties().toString());

      // Display user Jajuk configuration
      Log.debug(UtilString.getAnonymizedJajukProperties().toString());

      // check for another session (needs setLocal)
      checkOtherSession();

      // Set a session file
      sessionIdFile = getSessionIdFile();
      if (!sessionIdFile.mkdir()) {
        Log.warn("Could not create directory for session: " + sessionIdFile);
      }

      // Register device types
      for (final String deviceTypeId : DEVICE_TYPES) {
        DeviceManager.getInstance().registerDeviceType(Messages.getString(deviceTypeId));
      }
      // registers supported audio supports and default properties
      registerTypes();

      // Display progress
      // sc can be null if not already loaded. Done for perfs
      sc.setProgress(10, Messages.getString("SplashScreen.1"));

      // Load collection
      loadCollection();

      // Upgrade step2
      UpgradeManager.upgradeStep2();

      // Clean the collection up
      Collection.cleanupLogical();

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

      // Various asynchronous startup actions that needs collection load
      StartupService.startupAsyncAfterCollectionLoad(bCollectionLoadRecover);

      // Auto mount devices, freeze for SMB drives
      // if network is not reachable
      // Do not start this if first session, it is causes concurrency with
      // first refresh thread
      if (!UpgradeManager.isFirstSesion()) {
        autoMount();
      }

      // Create automatically a Music device if we are packaging a
      // JukeboxPowerPack distribution
      powerPack();

      // Launch startup track if any (but don't start it if firsdt session
      // because the first refresh is probably still running)
      if (!UpgradeManager.isFirstSesion()) {
        StartupService.launchInitialTrack();
      }

      // Start up action manager. To be done before launching ui and
      // tray
      ActionManager.getInstance();

      // show window if set in the systray conf.
      if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_WINDOW_TRAY) {
        // Display progress
        sc.setProgress(80, Messages.getString("SplashScreen.3"));
        launchWindow();
      }

      // start the tray
      launchTray();

      // Start the slimbar if required
      if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_SLIMBAR_TRAY) {
        launchSlimbar();
      }

    } catch (final JajukException je) { // last chance to catch any error for
      // logging purpose
      Log.error(je);
      if (je.getCode() == 5) {
        Messages.getChoice(Messages.getErrorMessage(5), JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE);
        ExitService.exit(1);
      }
    } catch (final Exception e) { // last chance to catch any error for logging
      // purpose
      e.printStackTrace();
      Log.error(106, e);
      ExitService.exit(1);
    } catch (final Error error) { // last chance to catch any error for logging
      // purpose
      error.printStackTrace();
      Log.error(106, error);
      ExitService.exit(1);
    } finally { // make sure to close splashscreen in all cases
      // (i.e. if UI is not started)
      if (sc != null) {
        sc.setProgress(100);
        sc.splashOff();
        sc = null;
      }
    }
  }

  /**
   * Register all the different managers for the types of items that we know
   * about
   * 
   */
  public static void registerItemManagers() {
    ItemManager.registerItemManager(org.jajuk.base.Album.class, AlbumManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Author.class, AuthorManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Device.class, DeviceManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.File.class, FileManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Directory.class, DirectoryManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Playlist.class, PlaylistManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Style.class, StyleManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Track.class, TrackManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Type.class, TypeManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Year.class, YearManager.getInstance());
  }

  /**
   * Walks through the command line arguments and sets flags for any one that we
   * recognize.
   * 
   * @param args
   *          The list of command line arguments that is passed to main()
   */
  private static void handleCommandline(final String[] args) {
    // walk through all arguments and check if there is one that we recognize
    for (final String element : args) {
      // Tells jajuk it is inside the IDE (useful to find right
      // location for images and jar resources)
      if (element.equals("-" + Const.CLI_IDE)) {
        bIdeMode = true;
      }
      // Tells jajuk to use a .jajuk_test repository
      // The information can be given from CLI using
      // -test=[test|notest] option
      if (element.equals("-" + Const.CLI_TEST)) {
        bTestMode = true;
      }
      if (element.equals("-" + Const.CLI_POWER_PACK)) {
        bPowerPack = true;
      }
    }
  }

  /**
   * Performs some basic startup tests
   * 
   * @throws Exception
   */
  public static void initialCheckups() throws Exception {
    // Check for bootstrap file presence
    final File bootstrap = new File(Const.FILE_BOOTSTRAP);
    // Default workspace: ~/.jajuk
    final File fDefaultWorkspace = UtilSystem.getConfFileByPath("");
    if (bootstrap.canRead()) {
      try {
        final BufferedReader br = new BufferedReader(new FileReader(bootstrap));
        // Bootstrap file should contain a single line containing the
        // path to jajuk workspace
        final String sPath = br.readLine();
        br.close();
        // Check if the repository can be found
        if (new File(sPath + '/'
            + (Main.bTestMode ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk")).canRead()) {
          Main.workspace = sPath;
        }
      } catch (final IOException e) {
        System.out.println("Cannot read bootstrap file, using ~ directory");
        Main.workspace = System.getProperty("user.home");
      }
    }
    // No bootstrap or unreadable or the path included inside is not
    // readable, show a wizard to select it
    if ((!bootstrap.canRead() || (Main.workspace == null))
    // don't launch the first time wizard if a previous release .jajuk dir
        // exists (upgrade from < 1.4)
        && !fDefaultWorkspace.canRead()) {
      // First time session ever
      UpgradeManager.setFirstSession();
      // display the first time wizard
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          new FirstTimeWizard();
        }
      });
      // Lock until first time wizard is closed
      synchronized (isFirstTimeWizardClosed) {
        isFirstTimeWizardClosed.wait();
      }
    }
    // In all cases, make sure to set a workspace
    if (workspace == null) {
      workspace = System.getProperty("user.home");
    }
    // check for jajuk directory
    final File fWorkspace = new File(workspace);
    if (!fWorkspace.exists() && !fWorkspace.mkdirs()) { // create the directory
      // if it doesn't exist
      Log.warn("Could not create directory " + fWorkspace.toString());
    }
    // check for image cache presence and create the workspace/.jajuk
    // directory
    final File fCache = UtilSystem.getConfFileByPath(Const.FILE_CACHE);
    if (!fCache.exists()) {
      if (!fCache.mkdirs()) {
        Log.warn("Could not cretae directory structure " + fCache.toString());
      }
    } else {
      // Empty cache
      final File[] cacheFiles = fCache.listFiles();
      for (final File element : cacheFiles) {
        if (element.isFile() && !element.delete()) {
          Log.warn("Could not delete file " + element.toString());
        }
      }
    }

    // checking required internal configuration files
    for (final String check : CONFIG_CHECKS) {
      final File file = UtilSystem.getConfFileByPath(check);

      if (!file.exists()) {
        // if config file doesn't exit, create
        // it with default values
        org.jajuk.util.Conf.commit();
      }
    }

    // checking required internal directories
    for (final String check : DIR_CHECKS) {
      final File file = UtilSystem.getConfFileByPath(check);

      if (!file.exists() && !file.mkdir()) {
        Log.warn("Could not create missing required directory [" + check + "]");
      }
    }

    // Extract star icons (used by some HTML panels)
    for (int i = 0; i <= 4; i++) {
      final File star = UtilSystem.getConfFileByPath("cache/internal/star" + i + "_16x16.png");
      if (!star.exists()) {
        ImageIcon ii = null;
        switch (i) {
        case 0:
          ii = IconLoader.getIcon(JajukIcons.STAR_0);
          break;
        case 1:
          ii = IconLoader.getIcon(JajukIcons.STAR_1);
          break;
        case 2:
          ii = IconLoader.getIcon(JajukIcons.STAR_2);
          break;
        case 3:
          ii = IconLoader.getIcon(JajukIcons.STAR_3);
          break;
        case 4:
          ii = IconLoader.getIcon(JajukIcons.STAR_4);
          break;
        default:
          throw new IllegalArgumentException(
              "Unexpected code position reached, the switch values should match the for-loop!");
        }
        UtilGUI.extractImage(ii.getImage(), star);
      }
    }
  }

  /**
   * Registers supported audio supports and default properties
   */
  public static void registerTypes() {
    try {
      // test mplayer presence in PATH
      mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_OK;
      if (UtilSystem.isUnderWindows()) {
        final File mplayerPath = UtilSystem.getMPlayerWindowsPath();
        // try to find mplayer executable in know locations first
        if (mplayerPath == null) {
          try {
            if (sc != null) {
              sc.setProgress(5, Messages.getString("Main.22"));
            }
            Log.debug("Download Mplayer from: " + Const.URL_MPLAYER);
            File fMPlayer = UtilSystem.getConfFileByPath(Const.FILE_MPLAYER_EXE);
            DownloadManager.download(new URL(Const.URL_MPLAYER), fMPlayer);
            // make sure to delete corrupted mplayer in case of
            // download problem
            if (fMPlayer.length() != Const.MPLAYER_EXE_SIZE) {
              if (!fMPlayer.delete()) {
                Log.warn("Could not delete file " + fMPlayer);
              }
              mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_JNLP_DOWNLOAD_PBM;
            }
          } catch (IOException e) {
            mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_JNLP_DOWNLOAD_PBM;
          }
        }
      }
      // Under non-windows OS, we assume mplayer has been installed
      // using external standard distributions
      else {
        // If a forced mplayer path is defined, test it
        final String forced = Conf.getString(Const.CONF_MPLAYER_PATH_FORCED);
        if (!UtilString.isVoid(forced)) {
          // Test forced path
          mplayerStatus = UtilSystem.getMplayerStatus(forced);
        } else {
          mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
        }
        if (mplayerStatus != UtilSystem.MPlayerStatus.MPLAYER_STATUS_OK) {
          // try to find a correct mplayer from the path
          // Under OSX, it will work only if jajuk is launched from
          // command line
          mplayerStatus = UtilSystem.getMplayerStatus("");
          if (mplayerStatus != UtilSystem.MPlayerStatus.MPLAYER_STATUS_OK) {
            // OK, try to find MPlayer in standards OSX directories
            if (UtilSystem.isUnderOSXpower()) {
              mplayerStatus = UtilSystem
                  .getMplayerStatus(Const.FILE_DEFAULT_MPLAYER_POWER_OSX_PATH);
            } else {
              mplayerStatus = UtilSystem.getMplayerStatus(Const.FILE_DEFAULT_MPLAYER_X86_OSX_PATH);
            }
          }
        }
      }
      // Choose player according to mplayer presence or not
      if (mplayerStatus != UtilSystem.MPlayerStatus.MPLAYER_STATUS_OK) {
        // No mplayer, show mplayer warnings
        Log.debug("Mplayer status=" + mplayerStatus);
        if (mplayerStatus != UtilSystem.MPlayerStatus.MPLAYER_STATUS_OK) {
          // Test if user didn't already select "don't show again"
          if (!Conf.getBoolean(Const.CONF_NOT_SHOW_AGAIN_PLAYER)) {
            if (mplayerStatus == UtilSystem.MPlayerStatus.MPLAYER_STATUS_NOT_FOUND) {
              // No mplayer
              Messages.showHideableWarningMessage(Messages.getString("Warning.0"),
                  Const.CONF_NOT_SHOW_AGAIN_PLAYER);
            } else if (mplayerStatus == UtilSystem.MPlayerStatus.MPLAYER_STATUS_WRONG_VERSION) {
              // wrong mplayer release
              Messages.showHideableWarningMessage(Messages.getString("Warning.1"),
                  Const.CONF_NOT_SHOW_AGAIN_PLAYER);
            }
          } else if (mplayerStatus == UtilSystem.MPlayerStatus.MPLAYER_STATUS_JNLP_DOWNLOAD_PBM) {
            // wrong mplayer release
            Messages.showHideableWarningMessage(Messages.getString("Warning.3"),
                Const.CONF_NOT_SHOW_AGAIN_PLAYER);
          }
        }
        TypeManager.registerTypesNoMplayer();
      } else { // mplayer enabled
        TypeManager.registerTypesMplayerAvailable();
      }
    } catch (final Exception e1) {
      Log.error(26, e1);
    }
  }

  /**
   * check if another session is already started
   * 
   */
  private static void checkOtherSession() {

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          // Check for remote concurrent users using the same configuration
          // files. Create concurrent session directory if needed
          File sessions = UtilSystem.getConfFileByPath(Const.FILE_SESSIONS);
          if (!sessions.exists() && !sessions.mkdir()) {
            Log.warn("Could not create directory " + sessions.toString());
          }
          // Check for concurrent session
          File[] files = sessions.listFiles();
          // display a warning if sessions directory contains some others users
          // We ignore presence of ourself session id that can be caused by a
          // crash
          if (files.length > 0) {
            StringBuilder details = new StringBuilder();
            for (File element : files) {
              details.append(element.getName());
              details.append('\n');
            }
            JOptionPane optionPane = UtilGUI.getNarrowOptionPane(72);
            optionPane.setMessage(UtilGUI.getLimitedMessage(Messages.getString("Warning.2")
                + details.toString(), 20));
            Object[] options = { Messages.getString("Ok"), Messages.getString("Hide"),
                Messages.getString("Purge") };
            optionPane.setOptions(options);
            optionPane.setMessageType(JOptionPane.WARNING_MESSAGE);
            JDialog dialog = optionPane.createDialog(null, Messages.getString("Warning"));
            dialog.setAlwaysOnTop(true);
            // keep it modal (useful at startup)
            dialog.setModal(true);
            dialog.pack();
            dialog.setIconImage(IconLoader.getIcon(JajukIcons.LOGO_FRAME).getImage());
            dialog.setLocationRelativeTo(JajukWindow.getInstance());
            dialog.setVisible(true);
            if (Messages.getString("Hide").equals(optionPane.getValue())) {
              // Not show again
              Conf.setProperty(Const.CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION, Const.TRUE);
            } else if (Messages.getString("Purge").equals(optionPane.getValue())) {
              // Clean up old locks directories in session folder
              files = sessions.listFiles();
              for (int i = 0; i < files.length; i++) {
                if (!files[i].delete()) {
                  Messages.showDetailedErrorMessage(131, "Cannot delete : "
                      + files[i].getAbsolutePath(), "");
                  Log.error(131);
                  break;
                }
              }
            }
          }
        }
      });
    } catch (InterruptedException e) {
      Log.error(e);
    } catch (InvocationTargetException e) {
      Log.error(e);
    }
  }

  /**
   * Load persisted collection file
   */
  private static void loadCollection() {
    if (UpgradeManager.isFirstSesion()) {
      Log.info("First session, collection will be created");
      return;
    }
    final File fCollection = UtilSystem.getConfFileByPath(Const.FILE_COLLECTION);
    try {
      Collection.load(UtilSystem.getConfFileByPath(Const.FILE_COLLECTION));
      bCollectionLoadRecover = false;
    } catch (final Exception e) {
      Log.error(5, fCollection.getAbsolutePath(), e);
      Log.debug("Jajuk was not closed properly during previous session, "
          + "we try to load a backup file");
      // try to restore a backup file
      final File[] fBackups = UtilSystem.getConfFileByPath("").listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          if (name.indexOf("backup") != -1) {
            return true;
          }
          return false;
        }
      });
      final List<File> alBackupFiles = new ArrayList<File>(Arrays.asList(fBackups));
      Collections.sort(alBackupFiles); // sort alphabetically (newest
      // last)
      Collections.reverse(alBackupFiles); // newest first now
      final Iterator<File> it = alBackupFiles.iterator();
      // parse all backup files, newest first
      boolean bParsingOK = false;
      while (!bParsingOK && it.hasNext()) {
        final File file = it.next();
        try {
          // Clear all previous collection
          Collection.clearCollection();
          // Load the backup file
          Collection.load(file);
          bParsingOK = true;
          final int i = Messages.getChoice(Messages.getString("Error.133") + ":\n"
              + file.getAbsolutePath(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
          if (i == JOptionPane.CANCEL_OPTION) {
            System.exit(-1);
          }
          break;
        } catch (final Exception e2) {
          Log.error(5, file.getAbsolutePath(), e2);
        }
      }
      if (!bParsingOK) { // not better? ok, commit a void
        // collection (and a void collection is loaded)
        Collection.clearCollection();
        System.gc();
        try {
          Collection.commit(UtilSystem.getConfFileByPath(Const.FILE_COLLECTION));
        } catch (final Exception e2) {
          Log.error(e2);
        }
      }
    }
  }

  /**
   * Auto-Mount required devices
   * 
   */
  public static void autoMount() {
    for (final Device device : DeviceManager.getInstance().getDevices()) {
      if (device.getBooleanValue(Const.XML_DEVICE_AUTO_MOUNT)) {
        try {
          device.mount(false);
        } catch (final Exception e) {
          Log.error(112, device.getName(), e);
          continue;
        }
      }
    }
  }

  /**
   * Launch jajuk window
   */
  public static void launchWindow() throws Exception {
    if (bUILauched) {
      return;
    }
    // ui init
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          // Light drag and drop for VLDocking
          UIManager.put("DragControler.paintBackgroundUnderDragRect", Boolean.FALSE);
          DockingUISettings.getInstance().installUI();

          // Set windows decoration to look and feel
          JFrame.setDefaultLookAndFeelDecorated(true);
          JDialog.setDefaultLookAndFeelDecorated(true);

          // starts ui
          JajukWindow.getInstance();

          // Creates the panel
          jpFrame = (JPanel) JajukWindow.getInstance().getContentPane();
          jpFrame.setOpaque(true);
          jpFrame.setLayout(new BorderLayout());

          // create the command bar
          CommandJPanel command = CommandJPanel.getInstance();
          command.initUI();

          // Create the information bar panel
          InformationJPanel information = InformationJPanel.getInstance();

          // Add information panel
          jpFrame.add(information, BorderLayout.SOUTH);

          // Create the perspective manager
          PerspectiveManager.load();
          perspectivePanel = new JXPanel();
          // Make this panel extensible
          perspectivePanel.setLayout(new BoxLayout(perspectivePanel, BoxLayout.X_AXIS));

          // Set menu bar to the frame
          JajukWindow.getInstance().setJMenuBar(JajukJMenuBar.getInstance());

          // Create the perspective tool bar panel
          perspectiveBar = PerspectiveBarJPanel.getInstance();
          jpFrame.add(perspectiveBar, BorderLayout.WEST);

          // Apply size and location BEFORE setVisible
          JajukWindow.getInstance().applyStoredSize();

          // Display the frame
          JajukWindow.getInstance().setVisible(true);

          // Apply size and location again
          // (required by Gnome for ie to fix the 0-sized maximized
          // frame)
          JajukWindow.getInstance().applyStoredSize();

          // Initialize and add the desktop
          PerspectiveManager.init();

          // Add main container (contains toolbars + desktop)
          final FormLayout layout = new FormLayout("f:d:grow", // columns
              "f:d:grow, 0dlu, d"); // rows
          final PanelBuilder builder = new PanelBuilder(layout);
          final CellConstraints cc = new CellConstraints();
          // Add items
          builder.add(command, cc.xy(1, 3));
          builder.add(perspectivePanel, cc.xy(1, 1));
          jpFrame.add(builder.getPanel(), BorderLayout.CENTER);

          // Display tip of the day if required (not at the first
          // session to avoid displaying too many windows once)
          if (Conf.getBoolean(Const.CONF_SHOW_TIP_ON_STARTUP) && !UpgradeManager.isFirstSesion()) {
            final TipOfTheDayWizard tipsView = new TipOfTheDayWizard();
            tipsView.setLocationRelativeTo(JajukWindow.getInstance());
            tipsView.setVisible(true);
          }

        } catch (final Exception e) { // last chance to catch any error for
          // logging purpose
          e.printStackTrace();
          Log.error(106, e);
        } finally {
          if (sc != null) {
            // Display progress
            sc.setProgress(100);
            sc.splashOff();

            // free resources
            sc = null;
          }
          bUILauched = true;
          // Notify any first time wizard to startup refresh
          synchronized (canLaunchRefresh) {
            canLaunchRefresh.notify();
          }
        }
      }
    });

  }

  /** Launch tray */
  private static void launchTray() throws Exception {
    // Skip the tray launching if user forced it to hide
    if (Conf.getBoolean(Const.CONF_FORCE_TRAY_SHUTDOWN)) {
      Log.debug("Tray shutdown forced");
      return;
    }
    // Now check if try is supported on this platform
    if (!SystemTray.isSupported()) {
      Log.debug("Tray unsupported");
      return;
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JajukSystray.getInstance();
      }
    });
  }

  /** Launch slimbar */
  private static void launchSlimbar() throws Exception {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          ActionManager.getAction(JajukActions.SLIM_JAJUK).perform(null);
        } catch (Exception e) {
          Log.error(e);
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
   *          The sPerspective to set.
   */
  public static void setDefaultPerspective(final String perspective) {
    sPerspective = perspective;
  }

  /**
   * Create automatically a free music directory (currently ../Music directory
   * relatively to jajuk.jar file) that contains free music packaged with
   * Jukebox Power Pack releases
   */
  private static void powerPack() {
    if (bPowerPack) {
      try {
        // Check if this device don't already exit
        for (Device device : DeviceManager.getInstance().getDevices()) {
          if (Const.FREE_MUSIC_DEVICE_NAME.equals(device.getName())) {
            return;
          }
        }
        // Check for ../Music file presence
        String music = new File(UtilSystem.getJarLocation(Main.class).toURI()).getParentFile()
            .getParentFile().getAbsolutePath();
        music += '/' + Const.FREE_MUSIC_DIR;
        File fMusic = new File(music);
        Log.debug("Powerpack detected, tested path: " + fMusic.getAbsolutePath());
        if (fMusic.exists()) {
          Device device = DeviceManager.getInstance().registerDevice(Const.FREE_MUSIC_DEVICE_NAME,
              Device.TYPE_DIRECTORY, fMusic.getAbsolutePath());
          device.setProperty(Const.XML_DEVICE_AUTO_MOUNT, true);
          device.setProperty(Const.XML_DEVICE_AUTO_REFRESH, 0.5d);
          device.mount(false);
          device.refreshCommand(true);
        }
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }

  public static File getSessionIdFile() {
    if (sessionIdFile == null) {
      String sHostname;
      try {
        sHostname = InetAddress.getLocalHost().getHostName();
      } catch (final UnknownHostException e) {
        sHostname = "localhost";
      }
      sessionIdFile = UtilSystem.getConfFileByPath(Const.FILE_SESSIONS + '/' + sHostname + '_'
          + System.getProperty("user.name") + '_'
          + new SimpleDateFormat("yyyyMMdd-kkmmss").format(UtilSystem.TODAY));
    }
    return sessionIdFile;
  }

  public static JPanel getPerspectivePanel() {
    return perspectivePanel;
  }

  public static boolean isIdeMode() {
    return bIdeMode;
  }

  public static boolean isTestMode() {
    return bTestMode;
  }

  public static String getWorkspace() {
    return workspace;
  }

  public static void setWorkspace(String workspace) {
    Main.workspace = workspace;
  }

  public static void initializeFromThumbnailsMaker(final boolean bTest, final String workspace) {
    Main.bTestMode = bTest;
    Main.workspace = workspace;
  }

  /**
   * Notify the system about the first time wizard being closed.
   * 
   */
  public static void notifyFirstTimeWizardClosed() {
    synchronized (Main.isFirstTimeWizardClosed) {
      Main.isFirstTimeWizardClosed.notify();
    }
  }

  /**
   * 
   */
  public static void waitForLaunchRefresh() {
    synchronized (Main.canLaunchRefresh) {
      try {
        Main.canLaunchRefresh.wait();
      } catch (final InterruptedException e) {
        Log.error(e);
      }
    }
  }
}
