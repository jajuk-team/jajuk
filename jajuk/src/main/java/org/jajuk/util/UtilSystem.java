/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.ImageIcon;

import org.jajuk.Main;
import org.jajuk.services.core.ExitService;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.filters.KnownTypeFilter;
import org.jajuk.util.log.Log;

/**
 * Set of convenient methods for system and IO
 */
public final class UtilSystem implements ITechnicalStrings {

  /** MPlayer status possible values * */
  public static enum MPlayerStatus {
    MPLAYER_STATUS_OK, MPLAYER_STATUS_NOT_FOUND, MPLAYER_STATUS_WRONG_VERSION, MPLAYER_STATUS_JNLP_DOWNLOAD_PBM
  }

  /** Current date cached (for performances) * */
  public static final Date TODAY = new Date();

  /**
   * Are we under Linux ? *
   */
  private static final boolean UNDER_LINUX;
  /**
   * Are we under MAC OS Intel ? *
   */
  private static final boolean UNDER_OSX_INTEL;
  /**
   * Are we under MAC OS power ? *
   */
  private static final boolean UNDER_OSX_POWER;
  /**
   * Are we under Windows ? *
   */
  private static final boolean UNDER_WINDOWS;
  /**
   * Are we under Windows 32 bits ? *
   */
  private static final boolean UNDER_WINDOWS_32BIT;
  /**
   * Are we under Windows 64 bits ? *
   */
  private static final boolean UNDER_WINDOWS_64BIT;
  /**
   * Directory filter used in refresh
   */
  private static JajukFileFilter dirFilter = new JajukFileFilter(DirectoryFilter.getInstance());
  /**
   * File filter used in refresh
   */
  private static JajukFileFilter fileFilter = new JajukFileFilter(KnownTypeFilter.getInstance());

  // Computes OS detection operations for perf reasons (can be called in loop
  // in refresh method for ie)
  static {
    final String sOS = (String) System.getProperties().get("os.name");
    // os.name can be null with JWS under MacOS
    UNDER_WINDOWS = ((sOS != null) && (sOS.trim().toLowerCase().lastIndexOf("windows") != -1));
  }

  static {
    UNDER_WINDOWS_32BIT = UtilSystem.isUnderWindows()
        && System.getProperties().get("sun.arch.data.model").equals("32");
  }

  static {
    UNDER_WINDOWS_64BIT = UtilSystem.isUnderWindows()
        && !System.getProperties().get("sun.arch.data.model").equals("32");
  }

  static {
    final String sOS = (String) System.getProperties().get("os.name");
    // os.name can be null with JWS under MacOS
    UNDER_LINUX = ((sOS != null) && (sOS.trim().toLowerCase().lastIndexOf("linux") != -1));
  }

  static {
    final String sArch = System.getProperty("os.arch");
    UNDER_OSX_INTEL = org.jdesktop.swingx.util.OS.isMacOSX()
        && ((sArch != null) && sArch.matches(".*86"));
  }

  static {
    final String sArch = System.getProperty("os.arch");
    UNDER_OSX_POWER = org.jdesktop.swingx.util.OS.isMacOSX()
        && ((sArch != null) && !sArch.matches(".*86"));
  }

  /** Icons cache */
  static Map<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>(200);
  /** Mplayer exe path */
  private static File mplayerPath = null;
  /** current class loader */
  private static ClassLoader classLoader = null;

  /**
   * private constructor to avoid instantiating utility class
   */
  private UtilSystem() {
  }

  /**
   * Save a file in the same directory with name <filename>_YYYYmmddHHMM.xml and
   * with a given maximum Mb size for the file and its backup files
   * 
   * @param file
   */
  public static void backupFile(final File file, final int iMB) {
    try {
      if (Integer.parseInt(ConfigurationManager.getProperty(ITechnicalStrings.CONF_BACKUP_SIZE)) <= 0) { // 0 or
        // less
        // means
        // no backup
        return;
      }
      // calculates total size in MB for the file to backup and its
      // backup
      // files
      long lUsedMB = 0;
      final List<File> alFiles = new ArrayList<File>(10);
      final File[] files = new File(file.getAbsolutePath()).getParentFile().listFiles();
      if (files != null) {
        for (final File element : files) {
          if (element.getName().indexOf(UtilSystem.removeExtension(file.getName())) != -1) {
            lUsedMB += element.length();
            alFiles.add(element);
          }
        }
        // sort found files
        alFiles.remove(file);
        Collections.sort(alFiles);
        // too much backup files, delete older
        if (((lUsedMB - file.length()) / 1048576 > iMB) && (alFiles.size() > 0)) {
          final File fileToDelete = alFiles.get(0);
          if (fileToDelete != null) {
            if(!fileToDelete.delete()) {
              Log.warn("Could not delete file " + fileToDelete);
            }
          }
        }
      }
      // backup itself using nio, file name is
      // collection-backup-yyyMMdd.xml
      final String sExt = new SimpleDateFormat("yyyyMMdd").format(new Date());
      final File fileNew = new File(UtilSystem.removeExtension(file.getAbsolutePath()) + "-backup-"
          + sExt + "." + UtilSystem.getExtension(file));
      final FileChannel fcSrc = new FileInputStream(file).getChannel();
      final FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
      fcDest.transferFrom(fcSrc, 0, fcSrc.size());
      fcSrc.close();
      fcDest.close();
    } catch (final IOException ie) {
      Log.error(ie);
    }
  }

  /**
   * Copy a file to another file
   * 
   * @param file :
   *          file to copy
   * @param fNew :
   *          destination file
   */
  public static void copy(final File file, final File fNew) throws Exception {
    Log.debug("Copying: " + file.getAbsolutePath() + "  to : " + fNew.getAbsolutePath());
    if (!file.exists() || !file.canRead()) {
      throw new JajukException(9, file.getAbsolutePath(), null);
    }
    final FileChannel fcSrc = new FileInputStream(file).getChannel();
    final FileChannel fcDest = new FileOutputStream(fNew).getChannel();
    fcDest.transferFrom(fcSrc, 0, fcSrc.size());
    fcSrc.close();
    fcDest.close();
    // Display a warning if copied file is void as it can happen with full
    // disks
    if (fNew.length() == 0) {
      Log.warn("Copied file is void: " + file.getAbsolutePath());
    }
  }

  /**
   * Copy a file
   * 
   * @param file :
   *          source file
   * @param sNewName :
   *          dest file
   */
  public static void copy(final File file, final String sNewName) throws Exception {
    Log.debug("Renaming: " + file.getAbsolutePath() + "  to : " + sNewName);
    final File fileNew = new File(new StringBuilder(file.getParentFile().getAbsolutePath()).append(
        '/').append(sNewName).toString());
    if (!file.exists() || !file.canRead()) {
      throw new JajukException(9, file.getAbsolutePath(), null);
    }
    final FileChannel fcSrc = new FileInputStream(file).getChannel();
    final FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
    fcDest.transferFrom(fcSrc, 0, fcSrc.size());
    fcSrc.close();
    fcDest.close();
  }

  /**
   * Copy a URL resource to a file We don't use nio but Buffered Reader / writer
   * because we can only get channels from a FileInputStream that can be or not
   * be in a Jar (production / test)
   * 
   * @param src
   *          source designed by URL
   * @param dest
   *          destination file full path
   * @throws IOException
   *           If the src or dest cannot be opened/created.
   */
  public static void copy(final URL src, final String dest) throws IOException {
    final BufferedReader br = new BufferedReader(new InputStreamReader(src.openStream()));
    final BufferedWriter bw = new BufferedWriter(new FileWriter(dest));
    String sLine = null;
    do {
      sLine = br.readLine();
      if (sLine != null) {
        bw.write(sLine);
        bw.newLine();
      }
    } while (sLine != null);
    br.close();
    bw.flush();
    bw.close();
  }

  /**
   * Copy recursively files and directories
   * 
   * @param str
   * @param dst
   * @throws IOException
   */
  public static void copyRecursively(final File src, final File dst) throws Exception {
    if (src.isDirectory()) {
      if(!dst.mkdirs()) {
        Log.warn("Could not create directory structure " + dst.toString());
      }
      final String list[] = src.list();
      for (final String element : list) {
        final String dest1 = dst.getAbsolutePath() + '/' + element;
        final String src1 = src.getAbsolutePath() + '/' + element;
        UtilSystem.copyRecursively(new File(src1), new File(dest1));
      }
    } else {
      UtilSystem.copy(src, dst);
    }
  }

  /**
   * Copy a file to given directory
   * 
   * @param file :
   *          file to copy
   * @param directory :
   *          destination directory
   */
  public static void copyToDir(final File file, final File directory) throws Exception {
    Log.debug("Copying: " + file.getAbsolutePath() + "  to : " + directory.getAbsolutePath());
    final File fileNew = new File(new StringBuilder(directory.getAbsolutePath()).append("/")
        .append(file.getName()).toString());
    if (!file.exists() || !file.canRead()) {
      throw new JajukException(9, file.getAbsolutePath(), null);
    }
    final FileChannel fcSrc = new FileInputStream(file).getChannel();
    final FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
    fcDest.transferFrom(fcSrc, 0, fcSrc.size());
    fcSrc.close();
    fcDest.close();
  }

  public static int countDirectories(final File root) {
    int count = 0;
    // index init
    File fCurrent = root;
    final int[] indexTab = new int[100]; // directory index
    for (int i = 0; i < 100; i++) { // init
      indexTab[i] = -1;
    }
    int iDeep = 0; // deep
    File dParent = root;
    // Start actual scan
    while ((iDeep >= 0) && !ExitService.isExiting()) {
      // only directories
      final File[] files = fCurrent.listFiles(UtilSystem.dirFilter);
      // files is null if fCurrent is a not a directory
      if ((files == null) || (files.length == 0)) {
        // re-init for next time we will reach this deep
        indexTab[iDeep] = -1;
        iDeep--; // come up
        fCurrent = fCurrent.getParentFile();
        if (dParent != null) {
          dParent = dParent.getParentFile();
        }
      } else {
        if (indexTab[iDeep] < files.length - 1) {
          // enter sub-directory
          indexTab[iDeep]++; // inc index for next time we
          // will reach this deep
          fCurrent = files[indexTab[iDeep]];
          count++;
          iDeep++;
        } else {
          indexTab[iDeep] = -1;
          iDeep--;
          fCurrent = fCurrent.getParentFile();
          if (dParent != null) {
            dParent = dParent.getParentFile();
          }
        }
      }
    }
    return count;
  }

  /**
   * Create empty file
   * 
   * @param sFullPath
   * @throws Exception
   */
  public static void createEmptyFile(final File file) throws IOException {
    final FileOutputStream fos = new FileOutputStream(file);
    fos.write(new byte[0]);
    fos.close();
  }

  /**
   * Delete a directory
   * 
   * @param dir :
   *          source directory
   */
  public static void deleteDir(final File dir) throws Exception {
    Log.debug("Deleting: " + dir.getAbsolutePath());
    if (dir.isDirectory()) {
      for (final File file : dir.listFiles()) {
        if (file.isDirectory()) {
          UtilSystem.deleteDir(file);
        } else {
          UtilSystem.deleteFile(file);
        }
      }
      if(!dir.delete()) {
        Log.warn("Could not delete directory " + dir);
      }
    } else {
      UtilSystem.deleteFile(dir);
    }
    return;
  }

  /**
   * Delete a file
   * 
   * @param file :
   *          source file
   */
  public static void deleteFile(final File file) throws Exception {
    Log.debug("Deleting: " + file.getAbsolutePath());
    if (file.isFile() && file.exists()) {
      if(!file.delete()) {
        Log.warn("Could not delete file " + file);
      }
      // check that file has been really deleted (sometimes,
      // we get no exception)
      if (file.exists()) {
        throw new Exception("File" + file.getAbsolutePath() + " still exists");
      }
    } else {// not a file, must have a problem
      throw new Exception("File " + file.getAbsolutePath() + " didn't exist");
    }
    return;
  }

  /**
   * Extract files from current jar to "cache/internal" directory
   * <p>
   * Thanks several websites, especially
   * http://www.developer.com/java/other/article.php/607931
   * 
   * @param entryName
   *          name of the file to extract. Example: img.png
   * @param file
   *          destination PATH
   * @throws Exception
   */
  public static void extractFile(final String entryName, final String destName) throws Exception {
    JarFile jar = null;
    // Open the jar.
    try {
      final File dir = new File(UtilSystem.getJarLocation(Main.class).toURI()).getParentFile();
      // We have to call getParentFile() method because the toURI() method
      // returns an URI than is not always valid (contains %20 for spaces
      // for instance)
      final File jarFile = new File(dir.getAbsolutePath() + "/jajuk.jar");
      Log.debug("Open jar: " + jarFile.getAbsolutePath());
      jar = new JarFile(jarFile);
    } catch (final Exception e) {
      Log.error(e);
      return;
    }
    try {
      // Get the entry and its input stream.
      final JarEntry entry = jar.getJarEntry(entryName);
      // If the entry is not null, extract it. Otherwise, print a
      // message.
      if (entry != null) {
        // Get an input stream for the entry.
        final InputStream entryStream = jar.getInputStream(entry);
        try {
          // Create the output file (clobbering the file if it
          // exists).
          final FileOutputStream file = new FileOutputStream(UtilSystem
              .getConfFileByPath(ITechnicalStrings.FILE_CACHE + '/'
                  + ITechnicalStrings.FILE_INTERNAL_CACHE + '/' + destName));
          try {
            // Allocate a buffer for reading the entry data.
            final byte[] buffer = new byte[1024];
            int bytesRead;
            // Read the entry data and write it to the output file.
            while ((bytesRead = entryStream.read(buffer)) != -1) {
              file.write(buffer, 0, bytesRead);
            }
          } catch (final Exception e) {
            Log.error(e);
          } finally {
            file.flush();
            file.close();
          }
        } catch (final Exception e) {
          Log.error(e);
        } finally {
          entryStream.close();
        }
      } else {
        Log.debug(entryName + " not found.");
      } // end if
    } catch (final Exception e) {
      Log.error(e);
    } finally {
      jar.close();
    }
  }

  /**
   * @param url
   *          resource URL
   * @param id
   *          unique identifier for the file
   * @return Cache directory
   */
  public static File getCachePath(final URL url, final String id) {
    File out = null;
    if (id == null) {
      out = UtilSystem.getConfFileByPath(ITechnicalStrings.FILE_CACHE + '/'
          + UtilSystem.getOnlyFile(url.toString()));
    } else {
      out = UtilSystem.getConfFileByPath(ITechnicalStrings.FILE_CACHE + '/' + id + '_'
          + UtilSystem.getOnlyFile(url.toString()));
    }
    return out;
  }

  /**
   * 
   * @param sPATH
   *          Configuration file or directory path
   * @return the file relative to jajuk directory
   */
  public static final File getConfFileByPath(final String sPATH) {
    String sRoot = System.getProperty("user.home");
    if ((Main.getWorkspace() != null) && !Main.getWorkspace().trim().equals("")) {
      sRoot = Main.getWorkspace();
    }
    return new File(sRoot + '/'
        + (Main.isTestMode() ? ".jajuk_test_" + ITechnicalStrings.TEST_VERSION : ".jajuk") + '/'
        + sPATH);
  }

  /**
   * Get a file extension
   * 
   * @param file
   * @return
   */
  public static String getExtension(final File file) {
    return UtilSystem.getExtension(file.getName());
  }

  /**
   * Get a file extension
   * 
   * @param filename
   * @return
   */
  public static String getExtension(final String filename) {
    final StringTokenizer st = new StringTokenizer(filename, ".");
    String sExt = "";
    if (st.countTokens() > 1) {
      while (st.hasMoreTokens()) {
        sExt = st.nextToken();
      }
    }
    return sExt.toLowerCase();
  }

  /**
   * Additional file checkusm used to prevent bug 886098. Simply return some
   * bytes read at the middle of the file
   * <p>
   * uses nio api for performances
   * 
   * @return
   */
  public static String getFileChecksum(final File fio) throws JajukException {
    try {
      String sOut = "";
      final FileChannel fc = new FileInputStream(fio).getChannel();
      final ByteBuffer bb = ByteBuffer.allocate(500);
      fc.read(bb, fio.length() / 2);
      fc.close();
      sOut = new String(bb.array());
      return MD5Processor.hash(sOut);
    } catch (final Exception e) {
      throw new JajukException(103, e);
    }
  }

  /**
   * 
   * @return This box hostname
   */
  public static String getHostName() {
    String sHostname = null;
    // Try to get hostname using the standard way
    try {
      sHostname = InetAddress.getLocalHost().getHostName();
    } catch (final Exception e) {
      Log.debug("Cannot get Hostname using the standard way");
    }
    if (sHostname == null) {
      // Try using IP now
      try {
        final java.net.InetAddress inetAdd = java.net.InetAddress.getByName("127.0.0.1");
        sHostname = inetAdd.getHostName();
      } catch (final Exception e) {
        Log.debug("Cannot get Hostname by IP");
      }
    }
    // If still no hostname, return a default value
    if (sHostname == null) {
      sHostname = ITechnicalStrings.DEFAULT_HOSTNAME;
    }
    return sHostname;
  }

  /**
   * Return url of jar we are executing
   * 
   * @return URL of jar we are executing
   */
  public static URL getJarLocation(final Class<?> cClass) {
    return cClass.getProtectionDomain().getCodeSource().getLocation();
  }

  /**
   * @return MPLayer binary MAC full path
   */
  public static String getMPlayerOSXPath() {
    final String forced = ConfigurationManager
        .getProperty(ITechnicalStrings.CONF_MPLAYER_PATH_FORCED);
    if (!UtilString.isVoid(forced)) {
      return forced;
    } else if (UtilSystem.isUnderOSXintel()
        && new File(ITechnicalStrings.FILE_DEFAULT_MPLAYER_X86_OSX_PATH).exists()) {
      return ITechnicalStrings.FILE_DEFAULT_MPLAYER_X86_OSX_PATH;
    } else if (UtilSystem.isUnderOSXpower()
        && new File(ITechnicalStrings.FILE_DEFAULT_MPLAYER_POWER_OSX_PATH).exists()) {
      return ITechnicalStrings.FILE_DEFAULT_MPLAYER_POWER_OSX_PATH;
    } else {
      // Simply return mplayer from PATH, works if app is launch from CLI
      return "mplayer";
    }
  }

  public static UtilSystem.MPlayerStatus getMplayerStatus(final String mplayerPATH) {
    Process proc = null;
    UtilSystem.MPlayerStatus mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
    try {
      String fullPath = null;
      if ("".equals(mplayerPATH)) {
        fullPath = "mplayer";
      } else {
        fullPath = mplayerPATH;
      }
      Log.debug("Testing path: " + fullPath);
      // check MPlayer release : 1.0pre8 min
      proc = Runtime.getRuntime().exec(new String[] { fullPath, "-input", "cmdlist" }); //$NON-NLS-2$ 
      final BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      try {
        String line = null;
        mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_WRONG_VERSION;
        for (;;) {
          line = in.readLine();
          if (line == null) {
            break;
          }

          if (line.matches("get_time_pos.*")) {
            mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_OK;
            break;
          }
        }
      } finally {
        in.close();
      }
    } catch (final Exception e) {
      mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
    }
    return mplayerStatus;
  }

  /**
   * @return MPlayer exe file
   */
  public static File getMPlayerWindowsPath() {
    // Use cache
    if (UtilSystem.mplayerPath != null) {
      return UtilSystem.mplayerPath;
    }
    File file = null;
    // Check in ~/.jajuk directory (used by webstart distribution
    // installers). Test exe size as well to detect unfinished downloads of
    // mplayer.exe in JNLP mode
    file = UtilSystem.getConfFileByPath(FILE_MPLAYER_EXE);
    if (file.exists() && file.length() == MPLAYER_EXE_SIZE) {
      UtilSystem.mplayerPath = file;
      return UtilSystem.mplayerPath;
    } else {
      // Check in the path where jajuk.jar is executed (all others
      // distributions)
      String sPATH = null;
      try {
        // Extract file name from URL. URI returns jar path, its parent
        // is the bin directory and the right dir is the parent of bin
        // dir
        // Note: When starting from jnlp, next line throws an exception
        // as URI is invalid (contains %20), the method returns null and
        // the file is downloaded again. This url is used only when
        // using
        // stand-alone version
        if (Main.isIdeMode()) {
          // If under dev, take mplayer exe file from the packjaging
          // directory
          sPATH = "./src/packaging";
        } else {
          sPATH = new File(getJarLocation(Main.class).toURI()).getParentFile().getParentFile()
              .getAbsolutePath();
        }
        // Add MPlayer file name
        file = new File(sPATH + '/' + ITechnicalStrings.FILE_MPLAYER_EXE);
        if (file.exists() && file.length() == MPLAYER_EXE_SIZE) {
          UtilSystem.mplayerPath = file;
        } else {
          // For bundle project, Jajuk should check if mplayer was
          // installed along with aTunes. In this case, mplayer is
          // found in sPATH\win_tools\ directory. Hence, changed sPATH
          // Note that we don't test mplayer.exe size in this case
          file = new File(sPATH + "/win_tools/" + ITechnicalStrings.FILE_MPLAYER_EXE);
          if (file.exists()) {
            UtilSystem.mplayerPath = file;
          }
        }

      } catch (Exception e) {
        return UtilSystem.mplayerPath;
      }
    }
    return UtilSystem.mplayerPath; // can be null if none suitable file found
  }

  /**
   * This method intends to cleanup a future filename so it can be created on
   * all operating systems. Windows forbids characters : /\"<>|:*?
   * 
   * @param in
   *          filename
   * @return filename with forbidden characters replaced at best
   */
  public static String getNormalizedFilename(final String in) {
    String out = in.trim();
    // Replace / : < > and \ by -
    out = in.replaceAll("[/:<>\\\\]", "-");
    // Replace * and | by spaces
    out = out.replaceAll("[\\*|]", " ");
    // Remove " and ? characters
    out = out.replaceAll("[\"\\?]", "");
    return out;
  }

  /**
   * Return only the name of a file from a complete URL
   * 
   * @param sPath
   * @return
   */
  public static String getOnlyFile(final String sPath) {
    return new File(sPath).getName();
  }

  /**
   * Resource loading is done this way to meet the requirements for Web Start.
   * http://java.sun.com/j2se/1.5.0/docs/guide/javaws/developersguide/faq.html#211
   */
  public static URL getResource(final String name) {
    return UtilSystem.getClassLoader().getResource(name);
  }

  /**
   * @param file1
   * @param file2
   * @return whether file1 is a file2 ancestor
   */
  public static boolean isAncestor(final File file1, final File file2) {
    File fParent = file2.getParentFile();
    boolean bOut = false;
    while (fParent != null) {
      if (fParent.equals(file1)) {
        bOut = true;
        break;
      }
      fParent = fParent.getParentFile();
    }
    return bOut;
  }

  /**
   * @param file1
   * @param file2
   * @return whether file1 is a file2 descendant
   */
  public static boolean isDescendant(final File file1, final File file2) {
    File fParent = file1.getParentFile();
    boolean bOut = false;
    while (fParent != null) {
      if (fParent.equals(file2)) {
        bOut = true;
        break;
      }
      fParent = fParent.getParentFile();
    }
    return bOut;
  }

  /**
   * @return whether we are under Linux
   */
  public static boolean isUnderLinux() {
    return UtilSystem.UNDER_LINUX;
  }

  /**
   * @return whether we are under OS X Intel
   */
  public static boolean isUnderOSXintel() {
    return UtilSystem.UNDER_OSX_INTEL;
  }

  /**
   * @return whether we are under OS X Power
   */
  public static boolean isUnderOSXpower() {
    return UtilSystem.UNDER_OSX_POWER;
  }

  /**
   * @return whether we are under Windows
   */
  public static boolean isUnderWindows() {
    return UtilSystem.UNDER_WINDOWS;
  }

  /**
   * @return whether we are under Windows 32 bits
   */
  public static boolean isUnderWindows32bits() {
    return UtilSystem.UNDER_WINDOWS_32BIT;
  }

  /**
   * @return whether we are under Windows 64 bits
   */
  public static boolean isUnderWindows64bits() {
    return UtilSystem.UNDER_WINDOWS_64BIT;
  }

  /**
   * @param parent
   *          parent directory
   * @param name
   *          file name
   * @return whether the file name is correct on the current filesystem
   */
  public static boolean isValidFileName(final File parent, final String name) {
    // General tests
    if ((parent == null) || (name == null)) {
      return false;
    }
    // only digits or letters, OK, no need to test
    if (!UtilString.containsNonDigitOrLetters(name)) {
      return true;
    }
    final File f = new File(parent, name);
    if (!f.exists()) {
      try {
        // try to create the file
        f.createNewFile();
        // test if the file is seen into the directory
        final File[] files = parent.listFiles();
        boolean b = false;
        for (final File element : files) {
          if (element.getName().equals(name)) {
            b = true;
            break;
          }
        }
        // remove test file
        if (f.exists()) {
          f.delete();
        }
        return b;
      } catch (final IOException ioe) {
        return false;
      }
    } else { // file already exists
      return true;
    }
  }

  /**
   * @return whether we need a full gc or not
   */
  public static boolean needFullFC() {
    final float fTotal = Runtime.getRuntime().totalMemory();
    final float fFree = Runtime.getRuntime().freeMemory();
    final float fLevel = (fTotal - fFree) / fTotal;
    return fLevel >= ITechnicalStrings.NEED_FULL_GC_LEVEL;
  }

  /**
   * Open a file and return a string buffer with the file content.
   * 
   * @param path
   *          -File path
   * @return StringBuilder - File content.
   * @throws JajukException -
   *           Throws a JajukException if a problem occurs during the file
   *           access.
   */
  public static StringBuilder readFile(final String path) throws JajukException {
    // Read
    final File file = null;
    try {
      new File(path);
    } catch (final Exception e) {
      throw new JajukException(9, e);
    }
    FileReader fileReader;
    try {
      fileReader = new FileReader(file);
    } catch (final FileNotFoundException e) {
      final JajukException te = new JajukException(9, path, e);
      throw te;
    }
    final BufferedReader input = new BufferedReader(fileReader);

    // Read
    final StringBuilder strColl = new StringBuilder();
    String line = null;
    try {
      while ((line = input.readLine()) != null) {
        strColl.append(line);
      }
    } catch (final IOException e) {
      final JajukException te = new JajukException(9, path, e);
      throw te;
    }

    // Close the bufferedReader
    try {
      input.close();
    } catch (final IOException e) {
      final JajukException te = new JajukException(9, path, e);
      throw te;
    }

    return strColl;
  }

  /**
   * Open a file from current jar and return a string buffer with the file
   * content.
   * 
   * @param sUrl :
   *          relative file url
   * @return StringBuilder - File content.
   * @throws JajukException
   *           -Throws a JajukException if a problem occurs during the file
   *           access.
   */
  public static StringBuilder readJarFile(final String sURL) throws JajukException {
    // Read
    InputStream is;
    StringBuilder sb = null;
    try {
      is = Main.class.getResourceAsStream(sURL);
      // Read
      final byte[] b = new byte[200];
      sb = new StringBuilder();
      int i = 0;
      do {
        i = is.read(b, 0, b.length);
        sb.append(new String(b));
      } while (i > 0);
      // Close the bufferedReader
      is.close();
    } catch (final IOException e) {
      final JajukException te = new JajukException(9, e);
      throw te;
    }
    return sb;

  }

  /**
   * Remove an extension from a file name
   * 
   * @param filename
   * @return filename without extension
   */
  public static String removeExtension(final String sFilename) {
    return sFilename.substring(0, sFilename.lastIndexOf('.'));
  }

  public static ClassLoader getClassLoader() {
    if (UtilSystem.classLoader == null) {
      UtilSystem.classLoader = Thread.currentThread().getContextClassLoader();
    }
    return (UtilSystem.classLoader);
  }

  /**
   * Clear locale images cache
   */
  public static void clearCache() {
    final File fCache = getConfFileByPath(ITechnicalStrings.FILE_CACHE);
    final File[] files = fCache.listFiles();
    for (final File element : files) {
      element.delete();
    }
  }

  public static JajukFileFilter getDirFilter() {
    return dirFilter;
  }

  public static JajukFileFilter getFileFilter() {
    return fileFilter;
  }

}
