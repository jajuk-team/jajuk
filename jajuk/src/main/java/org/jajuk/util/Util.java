/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */
package org.jajuk.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.jajuk.Main;
import org.jajuk.Main.MPlayerStatus;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.ui.widgets.JajukSystray;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.filters.KnownTypeFilter;
import org.jajuk.util.log.Log;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.theme.ThemeInfo;
import org.jvnet.substance.utils.SubstanceConstants;
import org.jvnet.substance.watermark.SubstanceImageWatermark;
import org.jvnet.substance.watermark.SubstanceNoneWatermark;
import org.jvnet.substance.watermark.WatermarkInfo;

/**
 * General use utilities methods
 */
public class Util implements ITechnicalStrings {

  /* Cursors */
  public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

  public static final Cursor LINK_CURSOR = new Cursor(Cursor.HAND_CURSOR);

  public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

  /** contains clipboard data */
  public static String copyData;

  /** Directory filter used in refresh */
  public static JajukFileFilter dirFilter = new JajukFileFilter(DirectoryFilter.getInstance());

  /** File filter used in refresh */
  public static JajukFileFilter fileFilter = new JajukFileFilter(KnownTypeFilter.getInstance());

  /** Icons cache */
  private static HashMap<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>(200);

  /** Mplayer exe path */
  private static File mplayerPath = null;

  /** Are we under Windows ? * */
  private static final boolean bUnderWindows;

  /** Are we under Windows 32 bits ? * */
  private static final boolean bUnderWindows32bits;

  /** Are we under Linux ? * */
  private static final boolean bUnderLinux;

  /** Are we under MAC OS intel ? * */
  private static final boolean bUnderOSXintel;

  /** Are we under MAC OS power ? * */
  private static final boolean bUnderOSXpower;

  /** Are we under Windows 64 bits ? * */
  private static final boolean bUnderWindows64bits;

  /** Today */
  static public final Date today = new Date();

  /** current class loader */
  private static ClassLoader classLoader = null;

  /**
   * Genres
   */
  public static final String[] genres = { "Blues", "Classic Rock", "Country", "Dance", "Disco",
      "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B",
      "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal",
      "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion",
      "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel",
      "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
      "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic",
      "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
      "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave",
      "Psychedelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz",
      "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock",
      "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass",
      "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock",
      "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
      "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Brass", "Primus",
      "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad",
      "Power Ballad", "Rhytmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "Acapella",
      "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror",
      "Indie", "BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal",
      "Black Metal", "Crossover", "Contemporary C", "Christian Rock", "Merengue", "Salsa",
      "Thrash Metal", "Anime", "JPop", "SynthPop" };

  // Computes OS detection operations for perf reasons (can be called in loop
  // in refresh method for ie)
  static {
    final String sOS = (String) System.getProperties().get("os.name");
    // os.name can be null with JWS under MacOS
    bUnderWindows = ((sOS != null) && (sOS.trim().toLowerCase().lastIndexOf("windows") != -1));
  }

  static {
    bUnderWindows32bits = Util.isUnderWindows()
        && System.getProperties().get("sun.arch.data.model").equals("32");
  }

  static {
    bUnderWindows64bits = Util.isUnderWindows()
        && !System.getProperties().get("sun.arch.data.model").equals("32");
  }

  static {
    final String sOS = (String) System.getProperties().get("os.name");
    // os.name can be null with JWS under MacOS
    bUnderLinux = ((sOS != null) && (sOS.trim().toLowerCase().lastIndexOf("linux") != -1));
  }

  static {
    final String sArch = System.getProperty("os.arch");
    bUnderOSXintel = org.jdesktop.swingx.util.OS.isMacOSX()
        && ((sArch != null) && sArch.matches(".*86"));
  }

  static {
    final String sArch = System.getProperty("os.arch");
    bUnderOSXpower = org.jdesktop.swingx.util.OS.isMacOSX()
        && ((sArch != null) && !sArch.matches(".*86"));
  }

  /**
   * Apply a pattern
   * 
   * @param file
   *          file to apply pattern to
   * @param sPattern
   * @param bMandatory
   *          are all needed tags mandatory ?
   * @return computed string
   * @return make sure the created string can be used as file name on target
   *         file system
   * @throws JajukException
   *           if some tags are missing
   */
  public static String applyPattern(final org.jajuk.base.File file, final String sPattern,
      final boolean bMandatory, final boolean normalize) throws JajukException {
    String out = sPattern;
    final Track track = file.getTrack();
    String sValue = null;
    // Check Author name
    if (sPattern.contains(ITechnicalStrings.PATTERN_AUTHOR)) {
      sValue = track.getAuthor().getName();
      if (normalize) {
        sValue = Util.getNormalizedFilename(sValue);
      }
      if (!sValue.equals(ITechnicalStrings.UNKNOWN_AUTHOR)) {
        out = out.replaceAll(ITechnicalStrings.PATTERN_AUTHOR, AuthorManager.format(sValue));
      } else {
        if (bMandatory) {
          throw new JajukException(150, file.getAbsolutePath());
        } else {
          out = out.replaceAll(ITechnicalStrings.PATTERN_AUTHOR, Messages
              .getString(ITechnicalStrings.UNKNOWN_AUTHOR));
        }
      }
    }
    // Check Style name
    if (sPattern.contains(ITechnicalStrings.PATTERN_STYLE)) {
      sValue = track.getStyle().getName();
      if (normalize) {
        sValue = Util.getNormalizedFilename(sValue);
      }
      if (!sValue.equals(ITechnicalStrings.UNKNOWN_STYLE)) {
        out = out.replace(ITechnicalStrings.PATTERN_STYLE, StyleManager.format(sValue));
      } else {
        if (bMandatory) {
          throw new JajukException(153, file.getAbsolutePath());
        } else {
          out = out.replace(ITechnicalStrings.PATTERN_STYLE, Messages
              .getString(ITechnicalStrings.UNKNOWN_STYLE));
        }
      }
    }
    // Check Album Name
    if (sPattern.contains(ITechnicalStrings.PATTERN_ALBUM)) {
      sValue = track.getAlbum().getName();
      if (normalize) {
        sValue = Util.getNormalizedFilename(sValue);
      }
      if (!sValue.equals(ITechnicalStrings.UNKNOWN_ALBUM)) {
        out = out.replace(ITechnicalStrings.PATTERN_ALBUM, AlbumManager.format(sValue));
      } else {
        if (bMandatory) {
          throw new JajukException(149, file.getAbsolutePath());
        } else {
          out = out.replace(ITechnicalStrings.PATTERN_ALBUM, Messages
              .getString(ITechnicalStrings.UNKNOWN_ALBUM));
        }
      }
    }
    // Check Track Order
    if (sPattern.contains(ITechnicalStrings.PATTERN_TRACKORDER)) {
      long lOrder = track.getOrder();
      if (lOrder == 0) {
        final String sFilename = file.getName();
        if (Character.isDigit(sFilename.charAt(0))) {
          final String sTo = file.getName().substring(0, 3).trim().replaceAll("[^0-9]", "");
          for (final char c : sTo.toCharArray()) {
            if (!Character.isDigit(c)) {
              throw new JajukException(152, file.getAbsolutePath());
            }
          }
          lOrder = Long.parseLong(sTo);
        } else {
          if (bMandatory) {
            throw new JajukException(152, file.getAbsolutePath());
          } else {
            lOrder = 0;
          }
        }
      }
      if (lOrder < 10) {
        out = out.replace(ITechnicalStrings.PATTERN_TRACKORDER, "0" + lOrder);
      } else {
        out = out.replace(ITechnicalStrings.PATTERN_TRACKORDER, lOrder + "");
      }
    }
    // Check Track name
    if (sPattern.contains(ITechnicalStrings.PATTERN_TRACKNAME)) {
      sValue = track.getName();
      if (normalize) {
        sValue = Util.getNormalizedFilename(sValue);
      }
      out = out.replace(ITechnicalStrings.PATTERN_TRACKNAME, sValue);
    }
    // Check Year Value
    if (sPattern.contains(ITechnicalStrings.PATTERN_YEAR)) {
      if (track.getYear().getValue() != 0) {
        out = out.replace(ITechnicalStrings.PATTERN_YEAR, track.getYear().getValue() + "");
      } else {
        if (bMandatory) {
          throw new JajukException(148, file.getAbsolutePath());
        } else {
          out = out.replace(ITechnicalStrings.PATTERN_YEAR, "?");
        }
      }
    }
    return out;
  }

  /**
   * @param alFiles
   * @return Given list to play with shuffle or others runles applied
   */
  @SuppressWarnings("unchecked")
  public static List<org.jajuk.base.File> applyPlayOption(final List<org.jajuk.base.File> alFiles) {
    if (ConfigurationManager.getBoolean(ITechnicalStrings.CONF_STATE_SHUFFLE)) {
      final List<org.jajuk.base.File> alFilesToPlay = (List<org.jajuk.base.File>) ((ArrayList<org.jajuk.base.File>) alFiles)
          .clone();
      Collections.shuffle(alFilesToPlay, new Random());
      return alFilesToPlay;
    }
    return alFiles;
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
      final ArrayList<File> alFiles = new ArrayList<File>(10);
      final File[] files = new File(file.getAbsolutePath()).getParentFile().listFiles();
      if (files != null) {
        for (final File element : files) {
          if (element.getName().indexOf(Util.removeExtension(file.getName())) != -1) {
            lUsedMB += element.length();
            alFiles.add(element);
          }
        }
        // sort found files
        alFiles.remove(file);
        Collections.sort(alFiles);
        if ((lUsedMB - file.length()) / 1048576 > iMB) {
          // too much backup files, delete older
          if (alFiles.size() > 0) {
            final File fileToDelete = alFiles.get(0);
            if (fileToDelete != null) {
              fileToDelete.delete();
            }
          }
        }
      }
      // backup itself using nio, file name is
      // collection-backup-yyyMMdd.xml
      final String sExt = new SimpleDateFormat("yyyyMMdd").format(new Date());
      final File fileNew = new File(Util.removeExtension(file.getAbsolutePath()) + "-backup-"
          + sExt + "." + Util.getExtension(file));
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
   * Clear locale images cache
   */
  public static void clearCache() {
    final File fCache = Util.getConfFileByPath(ITechnicalStrings.FILE_CACHE);
    final File[] files = fCache.listFiles();
    for (final File element : files) {
      element.delete();
    }
  }

  /**
   * @param s
   *          String to analyse
   * @return whether the given string contains non digit or letters chararcters
   */
  public static boolean containsNonDigitOrLetters(final String s) {
    boolean bOK = false;
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isLetterOrDigit(s.charAt(i))) {
        bOK = true;
        break;
      }
    }
    return bOK;
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
    if (!fNew.getParentFile().canWrite()) {
      throw new JajukException(24, file.getAbsolutePath(), null);
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
    if (!fileNew.getParentFile().canWrite()) {
      throw new JajukException(24, file.getAbsolutePath(), null);
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
   * @throws Exception
   */
  public static void copy(final URL src, final String dest) throws Exception {
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
      dst.mkdirs();
      final String list[] = src.list();
      for (final String element : list) {
        final String dest1 = dst.getAbsolutePath() + '/' + element;
        final String src1 = src.getAbsolutePath() + '/' + element;
        Util.copyRecursively(new File(src1), new File(dest1));
      }
    } else {
      Util.copy(src, dst);
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
    if (!fileNew.getParentFile().canWrite()) {
      throw new JajukException(24, file.getAbsolutePath(), null);
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
    while ((iDeep >= 0) && !Main.isExiting()) {
      // only directories
      final File[] files = fCurrent.listFiles(Util.dirFilter);
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
   * Convert a list of files into a list of StackItem
   * <p>
   * null files are ignored
   * </p>
   * 
   * @param alFiles
   * @param bRepeat
   * @param bUserLauched
   * @return
   */
  public static List<StackItem> createStackItems(final List<org.jajuk.base.File> alFiles,
      final boolean bRepeat, final boolean bUserLauched) {
    final ArrayList<StackItem> alOut = new ArrayList<StackItem>(alFiles.size());
    final Iterator it = alFiles.iterator();
    while (it.hasNext()) {
      final org.jajuk.base.File file = (org.jajuk.base.File) it.next();
      if (file != null) {
        try {
          final StackItem item = new StackItem(file);
          item.setRepeat(bRepeat);
          item.setUserLaunch(bUserLauched);
          alOut.add(item);
        } catch (final JajukException je) {
          Log.error(je);
        }
      }
    }
    return alOut;
  }

  /**
   * Reads an image in a file and creates a thumbnail in another file. Will be
   * created if necessary. the thumbnail must be maxDim pixels or less. Thanks
   * Marco Schmidt
   * http://schmidt.devlib.org/java/save-jpeg-thumbnail.html#source
   * 
   * @param orig
   *          source image
   * @param thumb
   *          destination file (jpg)
   * @param maxDim
   *          required size
   * @throws Exception
   */
  public static void createThumbnail(final File orig, final File thumb, final int maxDim)
      throws Exception {
    /*
     * do not use URL object has it can corrupt special paths
     */
    Util.createThumbnail(new ImageIcon(orig.getAbsolutePath()), thumb, maxDim);
  }

  /**
   * Reads an image in a file and creates a thumbnail in another file. Use this
   * method to get thumbs from images inside jar files, some bugs in URL
   * encoding makes impossible to create the image from a file. Will be created
   * if necessary. the thumbnail must be maxDim pixels or less. Thanks Marco
   * Schmidt http://schmidt.devlib.org/java/save-jpeg-thumbnail.html#source
   * 
   * @param orig
   *          source image
   * @param thumb
   *          destination file (jpg)
   * @param maxDim
   *          required size
   * @throws Exception
   */
  public static void createThumbnail(final ImageIcon ii, final File thumb, final int maxDim)
      throws Exception {
    final Image image = ii.getImage();
    // Wait for full image loading
    final MediaTracker mediaTracker = new MediaTracker(new Container());
    mediaTracker.addImage(image, 0);
    mediaTracker.waitForID(0);
    // determine thumbnail size from WIDTH and HEIGHT
    int thumbWidth = maxDim;
    int thumbHeight = maxDim;
    final double thumbRatio = (double) thumbWidth / (double) thumbHeight;
    final int imageWidth = image.getWidth(null);
    final int imageHeight = image.getHeight(null);
    final double imageRatio = (double) imageWidth / (double) imageHeight;
    if (thumbRatio < imageRatio) {
      thumbHeight = (int) (thumbWidth / imageRatio);
    } else {
      thumbWidth = (int) (thumbHeight * imageRatio);
    }
    // draw original image to thumbnail image object and
    // scale it to the new size on-the-fly
    final BufferedImage thumbImage = Util.toBufferedImage(image, !(Util.getExtension(thumb)
        .equalsIgnoreCase("jpg")), thumbWidth, thumbHeight);
    // Need alpha only for png and gif files
    // save thumbnail image to OUTFILE
    ImageIO.write(thumbImage, Util.getExtension(thumb), thumb);
    // Free thumb memory
    thumbImage.flush();
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
          Util.deleteDir(file);
        } else {
          Util.deleteFile(file);
        }
      }
      dir.delete();
    } else {
      Util.deleteFile(dir);
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
      file.delete();
      // check that file has been really deleted (sometimes,
      // we get no exception)
      if (file.exists()) {
        throw new Exception("");
      }
    } else {// not a file, must have a problem
      throw new Exception("");
    }
    return;
  }

  /**
   * Display a given image in a frame (for debuging purpose)
   * 
   * @param ii
   */
  public static void displayImage(final ImageIcon ii) {
    final JFrame jf = new JFrame();
    jf.add(new JLabel(ii));
    jf.pack();
    jf.setVisible(true);
  }

  /**
   * Encode URLS
   * 
   * @param s
   * @return
   */
  public static String encodeString(final String s) {
    return s.replaceAll(" +", "+");
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
      final File dir = new File(Util.getJarLocation(Main.class).toURI()).getParentFile();
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
          final FileOutputStream file = new FileOutputStream(Util
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
   * Write down a memory image to a file
   * 
   * @param src
   * @param dest
   */
  public static void extractImage(final Image src, final File dest) {
    final BufferedImage bi = Util.toBufferedImage(src, !(Util.getExtension(dest)
        .equalsIgnoreCase("jpg")));
    // Need alpha only for png and gif files);
    try {
      ImageIO.write(bi, Util.getExtension(dest), dest);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Filter a given file list by ambience
   * 
   * @param al
   *          file list
   * @param ambience
   *          ambience
   * @return the list filtered
   */
  public static List<org.jajuk.base.File> filterByAmbience(final List<org.jajuk.base.File> al,
      final Ambience ambience) {
    // Void filter, return the input
    if ((ambience == null) || (ambience.getStyles().size() == 0)) {
      return al;
    }
    // Filter by ambience
    final ArrayList<org.jajuk.base.File> out = new ArrayList<org.jajuk.base.File>(al.size() / 2);
    for (final org.jajuk.base.File file : al) {
      if (ambience.getStyles().contains(file.getTrack().getStyle())) {
        out.add(file);
      }
    }
    return out;
  }

  /**
   * Format an object to a string.
   * 
   * @param sValue
   * @param cType
   * @param bHuman
   *          is this string intended to be human-readable ?
   * @return
   * @throws Exception
   */
  public static String format(final Object oValue, final PropertyMetaInformation meta,
      final boolean bHuman) throws Exception {
    final Class cType = meta.getType();
    // default (works for strings, long and double)
    String sValue = oValue.toString();
    if (cType.equals(Date.class)) {
      if (bHuman) {
        sValue = Util.getLocaleDateFormatter().format((Date) oValue);
      } else {
        sValue = Util.getAdditionDateFormatter().format((Date)oValue);
      }
    } else if (cType.equals(Class.class)) {
      sValue = oValue.getClass().getName();
    }
    return sValue;
  }

  /**
   * Formatter for properties dialog window
   * 
   * @param sDesc
   * @return
   */
  public static String formatPropertyDesc(final String sDesc) {
    return "<HTML><center><b><font size=+0 color=#000000>" + sDesc + "</font></b><HTML>";
  }

  /**
   * format style: first letter uppercase and others lowercase
   * 
   * @param style
   * @return
   */
  public static String formatStyle(final String style) {
    if (style.length() == 0) {
      return "";
    }
    if (style.length() == 1) {
      return style.substring(0, 1).toUpperCase();
    }
    String sOut = style.toLowerCase().substring(1);
    sOut = style.substring(0, 1).toUpperCase() + sOut;
    return sOut;
  }

  /**
   * Performs some cleanups for strings comming from tag libs
   * 
   * @param s
   * @return
   */
  public static String formatTag(final String s) {
    // we delete all non char characters to avoid parsing errors
    char c;
    final StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      c = s.charAt(i);
      if (Util.isChar(c)) {
        sb.append(c);
      }
    }
    final String sOut = sb.toString().trim();
    return sOut;
  }

  /** Format a time from secs to a human readable format */
  public static String formatTimeBySec(final long lTime, final boolean bTrimZeros) {
    // Convert time to int for performance reasons
    int l = (int) lTime;
    if (l == -1) { // means we are in repeat mode
      return "--:--";
    } else if (l < 0) {
      // make sure to to get negative values
      l = 0;
    }
    final int hours = l / 3600;
    final int mins = l / 60 - (hours * 60);
    final int secs = l - (hours * 3600) - (mins * 60);
    final StringBuilder sbResult = new StringBuilder(8);
    if (hours > 0) {
      sbResult.append(Util.padNumber(hours, 2)).append(":");
    }
    return sbResult.append(Util.padNumber(mins, 2)).append(":").append(Util.padNumber(secs, 2))
        .toString();
  }

  /**
   * Format a string before XML write
   * <p>
   * see http://www.w3.org/TR/2000/REC-xml-20001006
   * <p>
   * substrings
   * <p>' to &apos;
   * <p>" to &quot;
   * <p>< to &lt;
   * <p>> to &gt;
   * <p>& to &amp;
   * 
   * @param s
   * @return
   */
  public static String formatXML(final String s) {
    String sOut = s;
    if (s.contains("&")) {
      sOut = sOut.replaceAll("&", "&amp;");
    }
    if (s.contains("\'")) {
      sOut = sOut.replaceAll("\'", "&apos;");
    }
    if (s.contains("\"")) {
      sOut = sOut.replaceAll("\"", "&quot;");
    }
    if (s.contains("<")) {
      sOut = sOut.replaceAll("<", "&lt;");
    }
    if (s.contains(">")) {
      sOut = sOut.replaceAll(">", "&gt;");
    }
    final StringBuilder sbOut = new StringBuilder(sOut.length());
    /*
     * Transform String to XML-valid characters. XML 1.0 specs ; Character Range
     * [2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
     * [#x10000-#x10FFFF] any Unicode character, excluding the surrogate blocks,
     * FFFE, and FFFF.
     */
    for (int i = 0; i < sOut.length(); i++) {
      final char c = sOut.charAt(i);
      if (Util.isChar(c)) {
        sbOut.append(c);
      }
    }
    return sbOut.toString();
  }

  /**
   * @return Anonymized Jajuk properties (for log or quality agent)
   */
  public static Properties getAnonymizedJajukProperties() {
    final Properties properties = (Properties) ConfigurationManager.getProperties().clone();
    // We remove sensible data from logs
    properties.remove("jajuk.network.proxy_login");
    properties.remove("jajuk.network.proxy_port");
    properties.remove("jajuk.network.proxy_hostname");
    properties.remove("jajuk.options.p2p.password");
    return properties;
  }

  /**
   * @return Anonymized System properties (for log or quality agent)
   */
  public static Properties getAnonymizedSystemProperties() {
    final Properties properties = (Properties) System.getProperties().clone();
    // We remove sensible data from logs
    /*
     * can contain external program paths
     */
    properties.remove("java.library.path");
    properties.remove("java.class.path");
    // user name is private
    properties.remove("user.name");
    properties.remove("java.ext.dirs");
    properties.remove("sun.boot.class.path");
    properties.remove("deployment.user.security.trusted.certs");
    properties.remove("deployment.user.security.trusted.clientauthcerts");
    properties.remove("jajuk.log");

    return properties;
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
      out = Util.getConfFileByPath(ITechnicalStrings.FILE_CACHE + '/'
          + Util.getOnlyFile(url.toString()));
    } else {
      out = Util.getConfFileByPath(ITechnicalStrings.FILE_CACHE + '/' + id + '_'
          + Util.getOnlyFile(url.toString()));
    }
    return out;
  }

  /**
   * @param jc
   * @return an horizontaly centred panel
   */
  public static JPanel getCentredPanel(final JComponent jc) {
    return Util.getCentredPanel(jc, BoxLayout.X_AXIS);
  }

  /**
   * @param jc
   * @param iOrientation :
   *          vertical or horizontal orientation, use BoxLayout.X_AXIS or
   *          BoxLayout.Y_AXIS
   * @return a centred panel
   */
  public static JPanel getCentredPanel(final JComponent jc, final int iOrientation) {
    final JPanel jpOut = new JPanel();
    jpOut.setLayout(new BoxLayout(jpOut, iOrientation));
    if (iOrientation == BoxLayout.X_AXIS) {
      jpOut.add(Box.createHorizontalGlue());
      jpOut.add(jc);
      jpOut.add(Box.createHorizontalGlue());
    } else {
      jpOut.add(Box.createVerticalGlue());
      jpOut.add(jc);
      jpOut.add(Box.createVerticalGlue());
    }
    jpOut.setMinimumSize(new Dimension(0, 0));
    // allow resing with info node
    return jpOut;
  }

  /**
   * 
   * @param sPATH
   *          Configuration file or directory path
   * @return the file relative to jajuk directory
   */
  public static final File getConfFileByPath(final String sPATH) {
    String sRoot = System.getProperty("user.home");
    if ((Main.workspace != null) && !Main.workspace.trim().equals("")) {
      sRoot = Main.workspace;
    }
    return new File(sRoot + '/'
        + (Main.bTestMode ? ".jajuk_test_" + ITechnicalStrings.TEST_VERSION : ".jajuk") + '/'
        + sPATH);
  }

  /**
   * Get a file extension
   * 
   * @param file
   * @return
   */
  public static String getExtension(final File file) {
    return Util.getExtension(file.getName());
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
   * Convenient method for getPlayableFiles(collection<item>)
   * 
   * @param item
   * @return files
   */
  public static ArrayList<org.jajuk.base.File> getPlayableFiles(Item item) {
    List<Item> list = new ArrayList<Item>(1);
    list.add(item);
    return getPlayableFiles(list);
  }

  /**
   * Computes file selection from item collection
   * <p>
   * We assume that the collection elements all own the same type
   * </p>
   * Unmounted files are selected according to the value of
   * CONF_OPTIONS_HIDE_UNMOUNTED option
   * 
   * @param selection
   *          an item selection (directories, files...)
   * @return the files (empty list if none matching)
   */
  public static ArrayList<org.jajuk.base.File> getPlayableFiles(List<Item> selection) {
    // computes selection
    ArrayList<org.jajuk.base.File> files = new ArrayList<org.jajuk.base.File>(100);
    if (selection == null || selection.size() == 0) {
      return files;
    }
    for (Item item : selection) {
      // computes logical selection if any
      if (item instanceof Track) {
        files.add(((Track) item).getPlayeableFile(ConfigurationManager
            .getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)));
      } else if (item instanceof Album || item instanceof Style || item instanceof Author
          || item instanceof Year) {
        Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item);
        for (Track track : tracks) {
          files.add(track.getPlayeableFile(ConfigurationManager
              .getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)));
        }
      }
      // computes physical selection if any
      else if (item instanceof org.jajuk.base.File) {
        files.add((org.jajuk.base.File) item);
      } else if (item instanceof Directory) {
        files = ((Directory) item).getFilesRecursively();
      } else if (item instanceof Device) {
        files = ((Device) item).getFilesRecursively();
      }
    }
    return files;
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
   * @param color
   *          java color
   * @return HTML RGB color ex: FF0000
   */
  public static String getHTMLColor(final Color color) {
    return Long.toString(color.getRed(), 16) + Long.toString(color.getGreen(), 16)
        + Long.toString(color.getBlue(), 16);

  }

  /**
   * Get required image with specified url
   * 
   * @param sURL
   * @return the image
   */
  public static ImageIcon getImage(final URL url) {
    ImageIcon ii = null;
    final String sURL = url.toString();
    try {
      if (Util.iconCache.containsKey(sURL)) {
        ii = Util.iconCache.get(sURL);
      } else {
        ii = new ImageIcon(url);
        Util.iconCache.put(sURL, ii);
      }

    } catch (final Exception e) {
      Log.error(e);
    }
    return ii;
  }

  /**
   * Return url of jar we are executing
   * 
   * @return URL of jar we are executing
   */
  public static URL getJarLocation(final Class cClass) {
    return cClass.getProtectionDomain().getCodeSource().getLocation();
  }

  /**
   * Make sure to reduce a string to the given size
   * 
   * @param sIn
   *          Input string, exemple: blabla
   * @param iSize
   *          max size, exemple: 3
   * @return bla...
   */
  public static String getLimitedString(final String sIn, final int iSize) {
    String sOut = sIn;
    if (sIn.length() > iSize) {
      sOut = sIn.substring(0, iSize) + "...";
    }
    return sOut;
  }

  /**
   * @return locale date formatter instance
   */
  public static DateFormat getLocaleDateFormatter() {
    return DateFormat.getDateInstance(DateFormat.DEFAULT, Locale
      .getDefault());
  }

  /**
   * @return Addition date simple format instance
   */
  public static DateFormat getAdditionDateFormatter() {
    return new SimpleDateFormat(
      ITechnicalStrings.ADDITION_DATE_FORMAT);
  }

  /**
   * @return MPLayer binary MAC full path
   */
  public static String getMPlayerOSXPath() {
    final String forced = ConfigurationManager
        .getProperty(ITechnicalStrings.CONF_MPLAYER_PATH_FORCED);
    if (!Util.isVoid(forced)) {
      return forced;
    } else if (Util.isUnderOSXintel()
        && new File(ITechnicalStrings.FILE_DEFAULT_MPLAYER_X86_OSX_PATH).exists()) {
      return ITechnicalStrings.FILE_DEFAULT_MPLAYER_X86_OSX_PATH;
    } else if (Util.isUnderOSXpower()
        && new File(ITechnicalStrings.FILE_DEFAULT_MPLAYER_POWER_OSX_PATH).exists()) {
      return ITechnicalStrings.FILE_DEFAULT_MPLAYER_POWER_OSX_PATH;
    } else {
      // Simply return mplayer from PATH, works if app is launch from CLI
      return "mplayer";
    }
  }

  public static MPlayerStatus getMplayerStatus(final String mplayerPATH) {
    Process proc = null;
    MPlayerStatus mplayerStatus = MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
    try {
      String fullPath = null;
      if ("".equals(mplayerPATH)) {
        fullPath = "mplayer";
      } else {
        fullPath = mplayerPATH;
      }
      Log.debug("Testing path: " + fullPath);
      // check MPlayer release : 1.0pre8 min
      proc = Runtime.getRuntime().exec(new String[] { fullPath, "-input", "cmdlist" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      final BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      String line = null;
      mplayerStatus = MPlayerStatus.MPLAYER_STATUS_WRONG_VERSION;
      for (; (line = in.readLine()) != null;) {
        if (line.matches("get_time_pos.*")) { //$NON-NLS-1$
          mplayerStatus = MPlayerStatus.MPLAYER_STATUS_OK;
          break;
        }
      }
    } catch (final Exception e) {
      mplayerStatus = MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
    }
    return mplayerStatus;
  }

  /**
   * @return MPlayer exe file
   */
  public static File getMPlayerWindowsPath() {
    // Use cache
    if (mplayerPath != null) {
      return mplayerPath;
    }
    File file = null;
    // Check in ~/.jajuk directory (used by webstart distribution
    // installers). Test exe size as well to detect unfinished downloads of
    // mplayer.exe in JNLP mode
    if ((file = Util.getConfFileByPath(FILE_MPLAYER_EXE)).exists()
        && file.length() == MPLAYER_EXE_SIZE) {
      mplayerPath = file;
      return mplayerPath;
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
        if (Main.bIdeMode) {
          // If under dev, take mplayer exe file from the packjaging
          // directory
          sPATH = "./src/packaging";
        } else {
          sPATH = new File(getJarLocation(Main.class).toURI()).getParentFile().getParentFile()
              .getAbsolutePath();
        }
        // Add MPlayer file name
        if ((file = new File(sPATH + '/' + ITechnicalStrings.FILE_MPLAYER_EXE)).exists()
            && file.length() == MPLAYER_EXE_SIZE) {
          Util.mplayerPath = file;
        } else {
          // For bundle project, Jajuk should check if mplayer was
          // installed along with aTunes. In this case, mplayer is
          // found in sPATH\win_tools\ directory. Hence, changed sPATH
          // Note that we don't test mplayer.exe size in this case
          if ((file = new File(sPATH + "/win_tools/" + ITechnicalStrings.FILE_MPLAYER_EXE))
              .exists())
            Util.mplayerPath = file;
        }

      } catch (Exception e) {
        return mplayerPath;
      }
    }
    return mplayerPath; // can be null if none suitable file found
  }

  /**
   * code from
   * http://java.sun.com/developer/onlineTraining/new2java/supplements/2005/July05.html#1
   * Used to correctly display long messages
   * 
   * @param maxCharactersPerLineCount
   * @return
   */
  public static JOptionPane getNarrowOptionPane(final int maxCharactersPerLineCount) {
    // Our inner class definition
    class NarrowOptionPane extends JOptionPane {
      private static final long serialVersionUID = 1L;

      int maxCharactersPerLineCount;

      NarrowOptionPane(final int maxCharactersPerLineCount) {
        this.maxCharactersPerLineCount = maxCharactersPerLineCount;
      }

      public int getMaxCharactersPerLineCount() {
        return maxCharactersPerLineCount;
      }
    }
    return new NarrowOptionPane(maxCharactersPerLineCount);
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
   * Resize an image
   * 
   * @param img
   *          image to resize
   * @param iNewWidth
   * @param iNewHeight
   * @return resized image
   */
  public static ImageIcon getResizedImage(final ImageIcon img, final int iNewWidth,
      final int iNewHeight) {
    // Wait for full image loading
    final MediaTracker mediaTracker = new MediaTracker(new Container());
    mediaTracker.addImage(img.getImage(), 0);
    try {
      mediaTracker.waitForID(0);
    } catch (final InterruptedException e) {
      Log.error(e);
    }
    final Image scaleImg = img.getImage().getScaledInstance(iNewWidth, iNewHeight,
        Image.SCALE_AREA_AVERAGING);
    // Leave image cache here as we may want to keep original image
    return new ImageIcon(scaleImg);
  }

  /**
   * Resource loading is done this way to meet the requirements for Web Start.
   * http://java.sun.com/j2se/1.5.0/docs/guide/javaws/developersguide/faq.html#211
   */
  public static URL getResource(final String name) {
    return Util.getClassLoader().getResource(name);
  }

  /**
   * @param img
   * @param iScale
   * @return a scaled image
   */
  public static ImageIcon getScaledImage(final ImageIcon img, final int iScale) {
    int iNewWidth;
    int iNewHeight;
    // Height is smaller or equal than width : try to optimize width
    iNewWidth = iScale; // take all possible width
    // we check now if height will be visible entirely with optimized width
    final float fWidthRatio = (float) iNewWidth / img.getIconWidth();
    if (img.getIconHeight() * (fWidthRatio) <= iScale) {
      iNewHeight = (int) (img.getIconHeight() * fWidthRatio);
    } else {
      // no? so we optimize width
      iNewHeight = iScale;
      iNewWidth = (int) (img.getIconWidth() * ((float) iNewHeight / img.getIconHeight()));
    }
    return Util.getResizedImage(img, iNewWidth, iNewHeight);
  }

  /**
   * @param col
   * @return a single shuffle element from a list, null if none element in
   *         provided collection
   */
  public static Object getShuffleItem(final Collection<? extends Object> col) {
    if (col.size() == 0) {
      return null;
    }
    List list = null;
    if (col instanceof List) {
      list = (List<? extends Object>) col;
    } else {
      list = new ArrayList<Object>(col);
    }
    return list.get((int) (Math.random() * list.size()));
  }

  /**
   * @param the
   *          rate
   * @return Number of stars for a given track rate
   */
  public static int getTrackStarsNumber(long lRate) {
    long lInterval = TrackManager.getInstance().getMaxRate() / 4;
    if (lRate <= lInterval) {
      return 1;
    } else if (lRate <= 2 * lInterval) {
      return 2;
    } else if (lRate <= 3 * lInterval) {
      return 3;
    } else {
      return 4;
    }
  }

  /**
   * 
   * @param lRate
   *          the rate
   * @return Number of stars for a given album rate
   */
  public static int getAlbumStarsNumber(long lRate) {
    long lInterval = AlbumManager.getInstance().getMaxRate() / 4;
    int nbStars = 1;
    if (lRate <= lInterval) {
      nbStars = 1;
    } else if (lRate <= 2 * lInterval) {
      nbStars = 2;
    } else if (lRate <= 3 * lInterval) {
      nbStars = 3;
    } else {
      nbStars = 4;
    }
    return nbStars;
  }

  /**
   * @return the stars icon
   */
  public static IconLabel getStars(Item item) {
    int starsNumber = 0;
    long rate = 0;
    if (item instanceof Track) {
      rate = ((Track) item).getRate();
      starsNumber = getTrackStarsNumber(rate);
    } else if (item instanceof Album) {
      rate = ((Album) item).getRate();
      starsNumber = getAlbumStarsNumber(rate);
    }
    IconLabel ilRate = null;
    switch (starsNumber) {
    case 1:
      ilRate = new IconLabel(IconLoader.ICON_STAR_1, "", null, null, null, Long.toString(rate));
      break;
    case 2:
      ilRate = new IconLabel(IconLoader.ICON_STAR_2, "", null, null, null, Long.toString(rate));
      break;
    case 3:
      ilRate = new IconLabel(IconLoader.ICON_STAR_3, "", null, null, null, Long.toString(rate));
      break;
    case 4:
      ilRate = new IconLabel(IconLoader.ICON_STAR_4, "", null, null, null, Long.toString(rate));
      break;
    default:
      return null;
    }
    ilRate.setInteger(true);
    return ilRate;
  }

  /** Return a genre string for a given genre id * */
  public static String getStringGenre(final int i) {
    if ((i >= 0) && (i < 126)) {
      return Util.genres[i];
    } else {
      return Messages.getString("unknown_style");
    }
  }

  /**
   * Try to compute time length in milliseconds using BasicPlayer API. (code
   * from jlGui 2.3)
   */
  public static long getTimeLengthEstimation(final Map properties) {
    long milliseconds = -1;
    int byteslength = -1;
    if (properties != null) {
      if (properties.containsKey("audio.length.bytes")) {
        byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
      }
      if (properties.containsKey("duration")) {
        milliseconds = (((Long) properties.get("duration")).longValue()) / 1000;
      } else {
        // Try to compute duration
        int bitspersample = -1;
        int channels = -1;
        float samplerate = -1.0f;
        int framesize = -1;
        if (properties.containsKey("audio.samplesize.bits")) {
          bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue();
        }
        if (properties.containsKey("audio.channels")) {
          channels = ((Integer) properties.get("audio.channels")).intValue();
        }
        if (properties.containsKey("audio.samplerate.hz")) {
          samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue();
        }
        if (properties.containsKey("audio.framesize.bytes")) {
          framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue();
        }
        if (bitspersample > 0) {
          milliseconds = (int) (1000.0f * byteslength / (samplerate * channels * (bitspersample / 8)));
        } else {
          milliseconds = (int) (1000.0f * byteslength / (samplerate * framesize));
        }
      }
    }
    return milliseconds;
  }

  /**
   * Tell whether a file is an absolute default cover or not
   * 
   * @param directory
   *          Jajuk Directory in which we analyze the given file name
   * @param sFileName
   * @return whether the given filename is an absolute default cover
   */
  public static boolean isAbsoluteDefaultCover(final Directory directory, final String sFilename) {
    final String sDefault = directory.getStringValue(ITechnicalStrings.XML_DIRECTORY_DEFAULT_COVER);
    if ((sDefault != null) && sDefault.equals(sFilename)) {
      return true;
    }
    return false;
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
   * @param ucs4char
   *          char to test
   * @return whether the char is valid, code taken from Apache sax
   *         implementation
   */
  public static boolean isChar(final int ucs4char) {
    return ((ucs4char >= 32) && (ucs4char <= 55295)) || (ucs4char == 10) || (ucs4char == 9)
        || (ucs4char == 13) || ((ucs4char >= 57344) && (ucs4char <= 65533))
        || ((ucs4char >= 0x10000) && (ucs4char <= 0x10ffff));
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
   * @param sFileName
   * @return whether the given filename is a standard cover or not
   */
  public static boolean isStandardCover(final String sFileName) {
    return sFileName.toLowerCase().matches(".*" + ITechnicalStrings.FILE_DEFAULT_COVER + ".*")
        || sFileName.toLowerCase().matches(".*" + ITechnicalStrings.FILE_DEFAULT_COVER_2 + ".*")
        // just for previous compatibility, now it is a directory
        // property
        || sFileName.toLowerCase().matches(
            ".*" + ITechnicalStrings.FILE_ABSOLUTE_DEFAULT_COVER + ".*");

  }

  /**
   * @return whether we are under Linux
   */
  public static boolean isUnderLinux() {
    return Util.bUnderLinux;
  }

  /**
   * @return whether we are under OS X Intel
   */
  public static boolean isUnderOSXintel() {
    return Util.bUnderOSXintel;
  }

  /**
   * @return whether we are under OS X Power
   */
  public static boolean isUnderOSXpower() {
    return Util.bUnderOSXpower;
  }

  /**
   * @return whether we are under Windows
   */
  public static boolean isUnderWindows() {
    return Util.bUnderWindows;
  }

  /**
   * @return whether we are under Windows 32 bits
   */
  public static boolean isUnderWindows32bits() {
    return Util.bUnderWindows32bits;
  }

  /**
   * @return whether we are under Windows 64 bits
   */
  public static boolean isUnderWindows64bits() {
    return Util.bUnderWindows64bits;
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
    if (!Util.containsNonDigitOrLetters(name)) {
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
   * 
   * @param s
   *          String to test
   * @return whether the string is void or not
   */
  public static boolean isVoid(final String s) {
    return (s == null) || s.trim().equals("");
  }

  /**
   * @param s
   * @return whether given string is XML-valid
   */
  public static boolean isXMLValid(final String s) {
    // check reserved chars
    if (s.contains("&") || s.contains("\'") || s.contains("\"") || s.contains("<")
        || s.contains(">")) {
      return false;
    }
    // check invalid chars
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      if (!Util.isChar(c)) {
        return false;
      }
    }
    return true;
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
   * Pad an int with zeros
   * 
   * @param l
   *          the number to be padded
   * @param size
   *          the targeted size
   * @return
   */
  public static String padNumber(final long l, final int size) {
    final StringBuilder sb = new StringBuilder(Long.toString(l));
    while (sb.length() < size) {
      sb.insert(0, '0');
    }
    return sb.toString();
  }

  /**
   * Parse a string to an object
   * 
   * @param sValue
   * @param cType
   * @return parsed item
   * @throws Exception
   */
  public static Object parse(final String sValue, final Class cType) throws Exception {
    Object oDefaultValue = sValue; // String by default
    if (cType.equals(Boolean.class)) {
      // "y" and "n" is an old boolean
      // attribute notation prior to 1.0
      if (sValue.equals("y")) {
        oDefaultValue = true;
      } else if (sValue.equals("n")) {
        oDefaultValue = false;
      } else {
        oDefaultValue = Boolean.parseBoolean(sValue);
      }
    } else if (cType.equals(Date.class)) {
        oDefaultValue = getAdditionDateFormatter().parseObject(sValue);
    } else if (cType.equals(Long.class)) {
      oDefaultValue = Long.parseLong(sValue);
    } else if (cType.equals(Double.class)) {
      oDefaultValue = Double.parseDouble(sValue);
    } else if (cType.equals(Class.class)) {
      oDefaultValue = Class.forName(sValue);
    }
    return oDefaultValue;
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
   * Make thumbnail file exists (album id.jpg or.gif or .png) in thumbs
   * directory if it doesn't exist yet
   * 
   * @param album
   * @return whether a new cover has been created
   */
  public static boolean refreshThumbnail(final Album album, final String size) {
    final File fThumb = Util.getConfFileByPath(ITechnicalStrings.FILE_THUMBS + '/' + size + '/'
        + album.getID() + '.' + ITechnicalStrings.EXT_THUMB);
    File fCover = null;
    if (!fThumb.exists()) {
      // search for local covers in all directories mapping the
      // current track to reach other
      // devices covers and display them together
      final Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
      if (tracks.size() == 0) {
        return false;
      }
      // take first track found to get associated directories as we
      // assume all tracks for an album are in the same directory
      final Track trackCurrent = tracks.iterator().next();
      fCover = trackCurrent.getAlbum().getCoverFile();
      if (fCover != null) {
        try {
          final int iSize = Integer.parseInt(new StringTokenizer(size, "x").nextToken());
          Util.createThumbnail(fCover, fThumb, iSize);
          return true;
        } catch (final Exception e) {
          Log.error(e);
        }
      }
    }
    return false; // thumb already exist
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

  /**
   * Rot13 encode/decode,
   * <p>
   * Thx
   * http://www.idevelopment.info/data/Programming/java/security/java_cryptography_extension/rot13.java
   * </p>
   * 
   * @param in
   *          text to encode / decode in rote 13
   * @return encoded /decoded text
   */
  public static String rot13(final String in) {
    if (Util.isVoid(in)) {
      return "";
    }
    int abyte = 0;
    final StringBuilder tempReturn = new StringBuilder();
    for (int i = 0; i < in.length(); i++) {
      abyte = in.charAt(i);
      int cap = abyte & 32;
      abyte &= ~cap;
      abyte = ((abyte >= 'A') && (abyte <= 'Z') ? ((abyte - 'A' + 13) % 26 + 'A') : abyte) | cap;
      tempReturn.append((char) abyte);
    }
    return tempReturn.toString();
  }

  /**
   * Set a look and feel. We always use Substance Look And Feel with various
   * themes
   * 
   * @param theme
   */
  public static void setLookAndFeel(String pTheme) {
    try {
      // Set substance laf
      UIManager.setLookAndFeel(ITechnicalStrings.LNF_SUBSTANCE_CLASS);
      // hide some useless elements such locker for not editable labels
      UIManager.put(SubstanceLookAndFeel.NO_EXTRA_ELEMENTS, Boolean.TRUE);
      UIManager.put(SubstanceLookAndFeel.WATERMARK_TO_BLEED, Boolean.TRUE);
      UIManager.put(SubstanceLookAndFeel.ENABLE_INVERTED_THEMES, Boolean.TRUE);
      UIManager.put(SubstanceLookAndFeel.ENABLE_NEGATED_THEMES, Boolean.TRUE);
      // Check the theme is known, if not take the default theme
      final Map<String, ThemeInfo> themes = SubstanceLookAndFeel.getAllThemes();
      if (themes.get(pTheme) == null) {
        pTheme = ITechnicalStrings.LNF_DEFAULT_THEME;
      }
      // Set substance theme
      SubstanceLookAndFeel.setCurrentTheme(themes.get(pTheme).getClassName());
    } catch (final Exception e) {
      Log.error(e);
    }
  }

  /**
   * Display given container at given position
   * 
   * @param container
   * @param iFromTop
   *          max number of pixels from top
   * @param iFromLeft
   *          max number of pixels from left
   */
  public static void setShuffleLocation(final Window window, final int iFromTop, final int iFromLeft) {
    window.setLocation((int) (Math.random() * iFromTop), (int) (Math.random() * iFromLeft));
  }

  /**
   * Set a watermark
   * 
   * @param watermark
   *          name
   */
  public static void setWatermark(final String pWatermark) {
    try {
      String watermark = pWatermark;
      // Check the watermark is known, if not take the default one
      final Map<String, WatermarkInfo> watermarks = SubstanceLookAndFeel.getAllWatermarks();
      if (watermarks.get(watermark) == null) {
        // the image watermark is not included in the list for unknown
        // reasons
        if (!"Image".equals(watermark)) {
          watermark = ITechnicalStrings.LNF_DEFAULT_WATERMARK;
        }
      }
      // Set the watermark
      final String image = ConfigurationManager
          .getProperty(ITechnicalStrings.CONF_OPTIONS_WATERMARK_IMAGE);
      if ("Image".equals(watermark)) {
        // Check that the backgroud image is readable
        if (new File(image).exists()) {
          SubstanceLookAndFeel.setCurrentWatermark(new SubstanceImageWatermark(image));
          SubstanceLookAndFeel
              .setImageWatermarkKind(SubstanceConstants.ImageWatermarkKind.SCREEN_CENTER_SCALE);
        } else {
          // None watermark
          SubstanceLookAndFeel.setCurrentWatermark(new SubstanceNoneWatermark());
        }
      } else {
        SubstanceLookAndFeel.setCurrentWatermark(watermarks.get(watermark).getClassName());
      }
    } catch (final Exception e) {
      Log.error(e);
      ConfigurationManager.setProperty(ITechnicalStrings.CONF_OPTIONS_WATERMARK,
          ITechnicalStrings.LNF_DEFAULT_WATERMARK);
    }
  }

  /**
   * Set current cursor as waiting cursor
   * <p>
   * Make sure the contentpane already exists to avoid strange behaviors
   * </p>
   * <p>
   * No need to execute in AWT thread
   * </p>
   */
  public static synchronized void waiting() {
    if (Main.getWindow() != null && Main.getWindow().getContentPane() != null
        && !(Main.getWindow().getContentPane().getCursor().equals(Util.WAIT_CURSOR))) {
      Main.getWindow().getContentPane().setCursor(Util.WAIT_CURSOR);
      Log.debug("** Waiting cursor");
    }
  }

  /**
   * Set current cursor as default cursor
   */
  public static synchronized void stopWaiting() {
    if (Main.getWindow() != null && Main.getWindow().getContentPane() != null
        && !(Main.getWindow().getContentPane().getCursor().equals(Util.DEFAULT_CURSOR))) {
      Main.getWindow().getContentPane().setCursor(Util.DEFAULT_CURSOR);
      Log.debug("** Default cursor");
    }
  }

  public static BufferedImage toBufferedImage(final Image image, final boolean alpha) {
    return Util.toBufferedImage(image, alpha, image.getWidth(null), image.getHeight(null));
  }

  /**
   * Transform an image to a BufferedImage
   * <p>
   * Thanks http://java.developpez.com/faq/java/?page=graphique_general_images
   * </p>
   * 
   * @param image
   * @param Do
   *          we need alpha (transparency) ?
   * @param new
   *          image width
   * @param height
   *          new image height
   * @return buffured image from an image
   */
  public static BufferedImage toBufferedImage(final Image image, final boolean alpha,
      final int width, final int height) {
    if (image instanceof BufferedImage) {
      return ((BufferedImage) image);
    } else {
      /** Create the new image */
      BufferedImage bufferedImage = null;
      if (alpha) {
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      } else {
        // Save memory
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      }
      final Graphics2D graphics2D = bufferedImage.createGraphics();
      graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      graphics2D.drawImage(image, 0, 0, width, height, null);
      image.flush();
      graphics2D.dispose();
      return (bufferedImage);
    }
  }

  /**
   * Method to attempt a dynamic update for any GUI accessible by this JVM. It
   * will filter through all frames and sub-components of the frames.
   */
  public static void updateAllUIs() {
    Frame frames[];
    frames = Frame.getFrames();

    for (final Frame element : frames) {
      Util.updateWindowUI(element);
    }
    // update tray
    if ((Main.getSystray() != null) && (JajukSystray.getInstance().jmenu != null)) {
      Util.updateComponentTreeUI(JajukSystray.getInstance().jmenu);
    }
  }

  /**
   * A simple minded look and feel change: ask each node in the tree to
   * <code>updateUI()</code> -- that is, to initialize its UI property with
   * the current look and feel. Based on the Sun
   * SwingUtilities.updateComponentTreeUI, but ensures that the update happens
   * on the components of a JToolbar before the JToolbar itself.
   */
  public static void updateComponentTreeUI(final Component c) {
    Util.updateComponentTreeUI0(c);
    c.invalidate();
    c.validate();
    c.repaint();
  }

  private static void updateComponentTreeUI0(final Component c) {

    Component[] children = null;

    if (c instanceof JToolBar) {
      children = ((JToolBar) c).getComponents();

      if (children != null) {
        for (final Component element : children) {
          Util.updateComponentTreeUI0(element);
        }
      }

      ((JComponent) c).updateUI();
    } else {
      if (c instanceof JComponent) {
        ((JComponent) c).updateUI();
      }

      if (c instanceof JMenu) {
        children = ((JMenu) c).getMenuComponents();
      } else if (c instanceof Container) {
        children = ((Container) c).getComponents();
      }

      if (children != null) {
        for (final Component element : children) {
          Util.updateComponentTreeUI0(element);
        }
      }
    }
  }

  /**
   * Method to attempt a dynamic update for all components of the given
   * <code>Window</code>.
   * 
   * @param window
   *          The <code>Window</code> for which the look and feel update has
   *          to be performed against.
   */
  public static void updateWindowUI(final Window window) {
    try {
      Util.updateComponentTreeUI(window);
    } catch (final Exception exception) {
    }

    final Window windows[] = window.getOwnedWindows();

    for (final Window element : windows) {
      Util.updateWindowUI(element);
    }
  }

  /**
   * No constructor
   */
  private Util() {
  }

  public static ClassLoader getClassLoader() {
    if (classLoader == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }
    return (classLoader);
  }

  /**
   * Filter a list.
   * <p>
   * The same collection is returned with non-matching items removed
   * </p>
   * <p>
   * This filter is not thread safe.
   * </p>
   * 
   * @param in
   *          input list
   * @param filter
   * @return filtered list, void list if none match
   */
  @SuppressWarnings("unchecked")
  public static List<Item> filterItems(List<? extends Item> list, Filter filter) {
    if (filter == null || filter.getValue() == null) {
      return (List<Item>) list;
    }
    // Check if property is not the "fake" any property
    boolean bAny = (filter.getProperty() == null || "any".equals(filter.getProperty()));

    String comparator = null;
    String checked = filter.getValue().toLowerCase();
    Iterator it = list.iterator();
    while (it.hasNext()) {
      Item item = (Item) it.next();
      // If none property set, the search if global "any"
      if (bAny) {
        comparator = item.getAny();
      } else {
        if (filter.isHuman()) {
          comparator = item.getHumanValue(filter.getProperty());
        } else {
          comparator = item.getStringValue(filter.getProperty());
        }
      }
      // perform the test
      boolean bMatch = false;
      if (filter.isExact()) {
        bMatch = (comparator.toLowerCase().equals(checked));
      } else {
        // Do not use Regexp matches() method, checked could contain string to
        // be escaped
        bMatch = (comparator.toLowerCase().indexOf(checked) != -1);
      }
      if (!bMatch) {
        it.remove();
      }
    }
    return (List<Item>) list;
  }

}
