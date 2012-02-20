/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.services.core;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.wizard.FirstTimeWizard;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukRuntimeException;
import org.jajuk.util.log.Log;

/**
 * Multi-session and test/final mode facilities.
 */
public class SessionService {

  /** Debug mode. */
  private static boolean bIdeMode = false;

  /** Test mode. */
  private static boolean bTestMode = false;

  /** Workspace PATH*. */
  private static String workspace;

  /** Forced workspace location (required for some special packaging like portableapps) *. */
  private static String forcedWorkspacePath = null;

  /** Directory used to flag the current jajuk session. */
  private static File sessionIdFile;

  /** Lock used to trigger first time wizard window close*. */
  private static short[] isFirstTimeWizardClosed = new short[0];

  /** Bootstrap file content as key/value format. */
  private static Properties bootstrapContent = new Properties();

  /** For performances, store conf root path. */
  private static String confRoot;

  /** Boostrap file test workspace path key. */
  private static final String KEY_TEST = "test";

  /** Boostrap file final workspace path key. */
  private static final String KEY_FINAL = "final";

  /** First time wizard instance if required. */
  private static FirstTimeWizard ftw;

  /**
   * private constructor for utility class with only static methods.
   */
  private SessionService() {
    super();
  }

  /**
   * check if another session is already started.
   */
  public static void checkOtherSession() {

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          // Check for remote concurrent users using the same
          // configuration
          // files. Create concurrent session directory if needed
          File sessions = getConfFileByPath(Const.FILE_SESSIONS);
          if (!sessions.exists() && !sessions.mkdir()) {
            Log.warn("Could not create directory " + sessions.toString());
          }
          // Check for concurrent session
          File[] files = sessions.listFiles();
          // display a warning if sessions directory contains some
          // others users
          // We ignore presence of ourself session id that can be
          // caused by a
          // crash
          if (files.length > 0 && !Conf.getBoolean(Const.CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION)) {
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
            dialog.setLocationRelativeTo(JajukMainWindow.getInstance());
            dialog.setVisible(true);
            if (Messages.getString("Hide").equals(optionPane.getValue())) {
              // Not show again
              Conf.setProperty(Const.CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION, Const.TRUE);
            } else if (Messages.getString("Purge").equals(optionPane.getValue())) {
              // Clean up old locks directories in session folder
              files = sessions.listFiles();
              for (int i = 0; i < files.length; i++) {
                if (!files[i].delete()) {
                  Messages.showDetailedErrorMessage(131,
                      "Cannot delete : " + files[i].getAbsolutePath(), "");
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
   * Checks if is ide mode.
   * 
   * @return true, if is ide mode
   */
  public static boolean isIdeMode() {
    return bIdeMode;
  }

  /**
   * Checks if is test mode.
   * 
   * @return true, if is test mode
   */
  public static boolean isTestMode() {
    return bTestMode;
  }

  /**
   * Gets the workspace.
   * 
   * @return the workspace
   */
  public static String getWorkspace() {
    return workspace;
  }

  /**
   * Sets the test mode.
   * 
   * @param bTestMode the new test mode
   */
  public static void setTestMode(boolean bTestMode) {
    SessionService.bTestMode = bTestMode;
  }

  /**
   * Sets the workspace.
   * 
   * @param workspace the new workspace
   */
  public static void setWorkspace(String workspace) {
    SessionService.workspace = workspace;
    if (isTestMode()) {
      bootstrapContent.put(KEY_TEST, workspace);
    } else {
      bootstrapContent.put(KEY_FINAL, workspace);
    }
    // Make sure to set all paths
    if (!bootstrapContent.containsKey(KEY_FINAL)) {
      bootstrapContent.put(KEY_FINAL, workspace);
    }
    if (!bootstrapContent.containsKey(KEY_TEST)) {
      bootstrapContent.put(KEY_TEST, workspace);
    }
  }

  /**
   * Gets the session id file.
   * 
   * @return the session id file
   */
  public static File getSessionIdFile() {
    if (sessionIdFile == null) {
      String sHostname;
      try {
        sHostname = InetAddress.getLocalHost().getHostName();
      } catch (final UnknownHostException e) {
        sHostname = "localhost";
      }
      sessionIdFile = getConfFileByPath(Const.FILE_SESSIONS + '/' + sHostname + '_'
          + System.getProperty("user.name") + '_'
          + new SimpleDateFormat("yyyyMMdd-kkmmss", Locale.getDefault()).format(UtilSystem.TODAY));
    }
    return sessionIdFile;
  }

  /**
   * Walks through the command line arguments and sets flags for any one that we
   * recognize.
   * 
   * @param args The list of command line arguments that is passed to main()
   */
  public static void handleCommandline(final String[] args) {
    // walk through all arguments and check if there is one that we
    // recognize
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
      // Handle special workspace location
      // Format : -workspace=<url of the workspace>
      if (element.matches("-" + Const.CLI_WORKSPACE_LOCATION + "=.*")) {
        String testedForcedWorkspace = null;
        try {
          StringTokenizer st = new StringTokenizer(element, "=");
          st.nextToken();
          testedForcedWorkspace = st.nextToken();
        } catch (Exception e) {
          throw new JajukRuntimeException("[BOOT] Wrong forced workspace location : "
              + testedForcedWorkspace);
        }
        if (testedForcedWorkspace == null || !new File(testedForcedWorkspace).canRead()) {
          // Leave jajuk
          throw new JajukRuntimeException("[BOOT] Wrong forced workspace location : "
              + testedForcedWorkspace);
        } else {
          forcedWorkspacePath = testedForcedWorkspace;
        }
      }
    }
  }

  /**
   * Load system properties provided when calling jvm (-Dxxx=yyy) <br>
   * This is usefull for unit tests.
   */
  public static void handleSystemProperties() {
    // walk through all system properties and check if there is one that we
    // recognize
    for (final Object element : System.getProperties().keySet()) {
      String key = (String) element;
      String value = System.getProperty(key);

      // Tells jajuk it is inside the IDE (useful to find right
      // location for images and jar resources)
      if (Const.CLI_IDE.equals(key) && Const.TRUE.equalsIgnoreCase(value)) {
        bIdeMode = true;
      }
      // Tells jajuk to use a .jajuk_test repository
      if (Const.CLI_TEST.equals(key) && Const.TRUE.equalsIgnoreCase(value)) {
        bTestMode = true;
      }
    }
  }

  /**
   * Creates the session file.
   * DOCUMENT_ME
   */
  public static void createSessionFile() {
    if (!getSessionIdFile().mkdir()) {
      Log.warn("Could not create directory for session: " + sessionIdFile);
    }
  }

  /**
   * Discover the jajuk workspace by reading the bootstrap file.<br>
   * Searched in this order :
   * <ul>
   * <li>Forced workspace path provided on command line (-workspace=...)</li>
   * <li>Bootstrap file content</li>
   * <li>Default path presence</li>
   * <li>Human selection</li>
   * </ul>
   * 
   * @throws InterruptedException the interrupted exception
   */
  public static void discoverWorkspace() throws InterruptedException {
    try {
      // Upgrade the bootstrap file if it exists (must be done here, not in upgrade step 1
      // because of the boot sequence dependencies)
      UpgradeManager.upgradeBootstrapFile();

      // Use any forced workspace location given in CLI. Note that this path has
      // already been validated in the handleCommand() method
      if (forcedWorkspacePath != null) {
        System.out.println("[BOOT] Forced workspace location : " + forcedWorkspacePath);
        // If the workspace not yet exists, display the first time wizard with workspace
        // location selection disabled
        String forcedCollectionPath = forcedWorkspacePath + '/'
            + (isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk");
        if (!new File(forcedCollectionPath).exists()) {
          humanWorkspaceSelection();
        } else {
          setWorkspace(forcedWorkspacePath);
        }
      } else {
        // Check for bootstrap file presence
        final File bootstrap = new File(getBootstrapPath());
        // Default collection path : ~/.jajuk
        final File fDefaultCollectionPath = getDefaultCollectionPath();
        // Try to find the workspace path in a bootstrap file
        if (bootstrap.canRead()) {
          try {
            // Parse bootstrap file (XML format)
            FileInputStream fis = new FileInputStream(bootstrap);
            bootstrapContent.loadFromXML(fis);

            String workspacePath = null;

            // Compute the final workspace path
            if (isTestMode()) {
              workspacePath = (String) bootstrapContent.get(KEY_TEST);
            } else {
              workspacePath = (String) bootstrapContent.get(KEY_FINAL);
            }

            // Check if the repository can be found
            if (new File(workspacePath + '/'
                + (isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk")).canRead()) {
              setWorkspace(workspacePath);
            } else {
              // Use default directory but do not commit the bootstrap file because the workspace could
              // be available again later, especially if located in a detachable device
              System.out
                  .println("[BOOT] Workspace given in bootstrap file is not accessible, using home directory as a workspace");
              setWorkspace(UtilSystem.getUserHome());
            }
            // Bootstrap file corrupted
          } catch (final Exception e) {
            Log.error(e);
            System.out
                .println("[BOOT] Bootstrap file corrupted, using home directory as a workspace");
            setWorkspace(UtilSystem.getUserHome());
            // Commit the bootstrap file
            commitBootstrapFile();
          }
        }
        // No bootstrap file ? Try default directory
        else if (fDefaultCollectionPath.canRead()) {
          System.out
              .println("[BOOT] Bootstrap file does not exist or is not readable, using home directory as a workspace");
          setWorkspace(UtilSystem.getUserHome());
          // Commit the bootstrap file
          commitBootstrapFile();
        }
        // Not better ? Show a first time wizard and let user select
        // the workspace (~/.jajuk by default)
        else {
          System.out
              .println("[BOOT] Bootstrap file does not exist or is not readable and home directory is not readable neither");
          humanWorkspaceSelection();
          // Commit the bootstrap file
          commitBootstrapFile();
        }
      }
    } finally {
      // In all cases, make sure to set a workspace
      if (getWorkspace() == null) {
        setWorkspace(UtilSystem.getUserHome());
      }
    }
  }

  /**
   * Let user select himself the workspace path.
   */
  private static void humanWorkspaceSelection() {
    // If running in headless env, force /tmp directory
    if (GraphicsEnvironment.isHeadless()) {
      setWorkspace("/tmp");
    } else {
      // First time session ever
      UpgradeManager.setFirstSession();
      // display the first time wizard
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          // default workspace displayed in the first time wizard is either the user home 
          // or the forced path if provided (can't be changed by the user from the wizard anyway)
          String defaultWorkspacePath = UtilSystem.getUserHome();
          if (forcedWorkspacePath != null) {
            defaultWorkspacePath = forcedWorkspacePath;
          }
          ftw = new FirstTimeWizard(defaultWorkspacePath);
          ftw.initUI();
        }
      });
      // Lock until first time wizard is closed
      synchronized (isFirstTimeWizardClosed) {
        try {
          isFirstTimeWizardClosed.wait();
        } catch (InterruptedException e) {
          Log.error(e);
        }
      }
      setWorkspace(ftw.getUserWorkspacePath());
    }
  }

  /**
   * Write down the bootstrap file.
   *
   * @param prop : the properties to write to the file
   */
  public static void commitBootstrapFile(Properties prop) {
    File bootstrap = null;
    try {
      bootstrap = new File(getBootstrapPath());
      FileOutputStream fos = new FileOutputStream(bootstrap);
      // Write down the new bootstrap file
      String comment = "Jajuk bootsrap file, do not edit manually, use Preference view / Advanced tab to set the workspace";
      prop.storeToXML(fos, comment);
      System.out.println("[BOOT] Bootstrap file written at : " + bootstrap.getAbsolutePath());
    } catch (Exception e) {
      // Log facilities not yet available
      e.printStackTrace();
      System.out.println("[BOOT] Cannot write down the boostrap file at : "
          + bootstrap.getAbsolutePath());
    }
  }

  /**
   * Write down the bootstrap file.
   */
  public static void commitBootstrapFile() {
    commitBootstrapFile(bootstrapContent);
  }

  /**
   * Notify the system about the first time wizard being closed.
   */
  public static void notifyFirstTimeWizardClosed() {
    synchronized (isFirstTimeWizardClosed) {
      isFirstTimeWizardClosed.notify();
    }
  }

  /**
   * Return destination file in cache for a given URL <br>
   * We store the file using the URL's MD3 5 hash to ensure unicity and avoid
   * unexpected characters in file names.
   * 
   * @param url resource URL
   * 
   * @return File in cache if any or null otherwise
   */
  public static File getCachePath(final URL url) {
    File out = null;
    out = getConfFileByPath(Const.FILE_CACHE + '/' + MD5Processor.hash(url.toString()));
    return out;
  }

  /**
   * Gets the conf file by path.
   * 
   * @param sPATH Configuration file or directory path
   * 
   * @return the file relative to jajuk directory
   */
  public static final File getConfFileByPath(final String sPATH) {
    if (confRoot == null) {
      String home = UtilSystem.getUserHome();
      if ((getWorkspace() != null) && !getWorkspace().trim().equals("")) {
        home = getWorkspace();
      }
      confRoot = home + '/' + (isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk") + '/';
    }
    return new File(confRoot + sPATH);
  }

  /**
   * Return whether user provided a forced workspace on command line.
   * 
   * @return whether user provided a forced workspace.
   */
  public static boolean isForcedWorkspace() {
    return (forcedWorkspacePath != null);
  }

  /**
   * Return default workspace location.
   * 
   * @return default workspace location
   */
  public static final File getDefaultCollectionPath() {
    String home = UtilSystem.getUserHome();
    return new File(home + '/' + (isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk")
        + '/');
  }

  /**
   * Clear locale images cache once a given size is reached.
   */
  public static void clearCache() {
    final File fCache = getConfFileByPath(Const.FILE_CACHE);
    final File[] files = fCache.listFiles();
    long totalSize = 0l;
    for (final File element : files) {
      totalSize += element.length();
    }
    if ((totalSize / 1048576) > Const.MAX_IMAGES_CACHE_SIZE) {
      for (final File element : files) {
        // note that this will not delete non-empty directories like last.fm cache in purpose
        element.delete();
      }
    }

  }

  /**
   * Return bootstrap file absolute path
   * 
   * 
   * 
   * This bootstrap file location can be overridden by providing -bootstrap=<URL> CLI option.
   * 
   * @param filename DOCUMENT_ME
   * 
   * @return bootstrap file absolute path
   * 
   * @filename filename of the bootstrap path
   */
  public static String getBootstrapPath(String filename) {
    return UtilSystem.getUserHome() + "/" + filename;
  }

  /**
   * Return bootstrap file absolute path
   * 
   * It also fixes #1473 by moving if required the bootstrap file (see See
   * https://trac.jajuk.info/ticket/1473)
   * 
   * 
   * This bootstrap file location can be overridden by providing -bootstrap=<URL> CLI option
   * 
   * @return bootstrap file absolute path
   */
  public static String getBootstrapPath() {
    return getBootstrapPath(Const.FILE_BOOTSTRAP);
  }

}
