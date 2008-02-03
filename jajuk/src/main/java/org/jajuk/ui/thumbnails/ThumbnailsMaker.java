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
 *  $Revision$
 */

package org.jajuk.ui.thumbnails;

import ext.ProcessLauncher;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Appender;
import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Collection;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.ItemManager;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.StyleManager;
import org.jajuk.base.TrackManager;
import org.jajuk.base.TypeManager;
import org.jajuk.base.YearManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.filters.JarFilter;
import org.jajuk.util.log.Log;

/**
 * Creating thumbs use a large amount of RAM. This executable class is intended
 * to build all thumbs in a separated java process to free memory when the
 * process is done.
 * 
 */
public class ThumbnailsMaker implements ITechnicalStrings {

  /**
   * 
   * @return separator between a '-cp' argument
   */
  private static char getJarSeparator() {
    if (Util.isUnderWindows()) {
      return ';';
    } else {
      return ':';
    }
  }

  /**
   * Convenient method to launch all thumb makers, one for each size
   * 
   * @param bSynchronous
   *          do you have to wait all process done ?
   */
  public static void launchAllSizes(final boolean bSynchronous) {
    final Thread t = new Thread() {
      @Override
      public void run() {
        for (int i = 50; i <= 300; i += 50) {
          try {
            ThumbnailsMaker.launchProcessus(i);
          } catch (Exception e) { // TODO Auto-generated
            e.printStackTrace();
          }
        }
      }
    };
    // Set min prority to avoid using too much CPU
    t.setPriority(Thread.MIN_PRIORITY);
    t.start();
    if (bSynchronous) {
      try {
        t.join();
      } catch (final InterruptedException e) { // TODO Auto-generated catch
        e.printStackTrace();
      }

    }
  }

  /**
   * Convenient method to launch the thumb creation in another JVM
   * 
   * @param size
   * @return status : 0 if OK, 1 ok error
   */
  public static int launchProcessus(final int size) throws Exception {
    final String jvmPath = System.getProperty("java.home") + File.separatorChar + "bin"
        + File.separatorChar + "java";
    final String jarPath = new File(Util.getJarLocation(Main.class).toURI()).getAbsolutePath()
        + File.separator + "jajuk.jar";
    final ArrayList<String> commands = new ArrayList<String>(10);
    commands.add(jvmPath);
    commands.add("-Xms50M");
    commands.add("-Xmx600M");
    commands.add("-cp");
    // Add the bin directory that contains classes
    String cp = new File(Util.getJarLocation(Main.class).toURI()).getAbsolutePath();
    cp += ThumbnailsMaker.getJarSeparator();
    final File libDir = new File(Util.getJarLocation(Appender.class).toURI()).getParentFile();
    final File[] files = libDir.listFiles(JarFilter.getInstance());
    for (final File element : files) {
      cp += element.getAbsolutePath() + ThumbnailsMaker.getJarSeparator();
    }
    // If we are not in dev, add jajuk jar itself to the classpath
    if (!Main.bIdeMode) {
      cp += ThumbnailsMaker.getJarSeparator() + jarPath;
    }
    // remove last separator
    cp = cp.substring(0, cp.length() - 1);
    commands.add(cp);

    // Add this class self name (with package)
    commands.add(ThumbnailsMaker.class.getCanonicalName());
    // Add size as a program argument
    commands.add(Integer.toString(size));
    // Add the test status
    commands.add(Boolean.toString(Main.bTestMode));
    // Add the workspace value
    commands.add(Main.workspace);
    Log.debug("Use command:" + commands);
    // We use this class and not ProcessBuilder as it hangs under windows
    // probably because of stream reading
    final ProcessLauncher launcher = new ProcessLauncher();
    return launcher.exec(commands.toArray(new String[commands.size()]));
  }

  /**
   * @param args :
   *          <p>
   *          size: thumb size like 100, or 300
   */
  public static void main(final String[] args) {
    new ThumbnailsMaker(Integer.parseInt(args[0]), Boolean.parseBoolean(args[1]), args[2]
        .toString());
  }

  private int size = 0;

  private int stat = 0;

  private boolean bTest = false;

  private final String workspace;

  /**
   * No instances
   */
  private ThumbnailsMaker(final int pSize, final boolean pTest, final String pWorkspace) {
    size = pSize;
    bTest = pTest;
    workspace = pWorkspace;
    try {
      buildThumbs();
    } catch (final Exception e) {
      e.printStackTrace();
      // Leave in error
      System.exit(1);
    }
    // Leave successfully
    System.exit(0);
  }

  private void buildThumbs() throws Exception {
    final long lTime = System.currentTimeMillis();
    Main.bTestMode = bTest;
    Main.workspace = workspace;
    Main.bThumbMaker = true;
    // log startup depends on : setExecLocation, initialCheckups
    Log.getInstance();
    Log.setVerbosity(Log.FATAL);
    Main.initialCheckups();
    ItemManager.registerItemManager(org.jajuk.base.Album.class, AlbumManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Author.class, AuthorManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Device.class, DeviceManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.File.class, FileManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Directory.class, DirectoryManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.PlaylistFile.class, PlaylistFileManager
        .getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Playlist.class, PlaylistManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Style.class, StyleManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Track.class, TrackManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Type.class, TypeManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Year.class, YearManager.getInstance());
    // Register device types
    DeviceManager.getInstance().registerDeviceType(Messages.getString("Device_type.directory"));
    DeviceManager.getInstance().registerDeviceType(Messages.getString("Device_type.file_cd"));
    DeviceManager.getInstance().registerDeviceType(Messages.getString("Device_type.network_drive"));
    DeviceManager.getInstance().registerDeviceType(Messages.getString("Device_type.extdd"));
    DeviceManager.getInstance().registerDeviceType(Messages.getString("Device_type.player"));
    // Load conf (required to get forced mplayer path for ie)
    ConfigurationManager.getInstance();
    // registers supported audio supports and default properties
    Main.registerTypes();
    // load collection
    Collection.load(Util.getConfFileByPath(ITechnicalStrings.FILE_COLLECTION));
    // Mount devices
    Main.autoMount();
    final java.util.Set<Album> albums = AlbumManager.getInstance().getAlbums();
    // For each album, create the associated thumb
    for (final Album album : albums) {
      // Leave if jajuk leaved
      if (Util.getConfFileByPath(ITechnicalStrings.FILE_COLLECTION_EXIT_PROOF).exists()) {
        Log.debug("Parent Jajuk closed, leaving now...");
        return;
      }
      if (ThumbnailManager.refreshThumbnail(album, size + "x" + size)) {
        stat++;
      }
      // Call GC to avoid increasing too much memory
      if ((stat > 0) && (stat % 30 == 0)) {
        System.gc();
      }
    }
    Log.setVerbosity(Log.DEBUG);
    Log.debug("[Thumb maker] " + stat + " thumbs created for size: " + size + " in "
        + (System.currentTimeMillis() - lTime) + " ms");
  }

}
