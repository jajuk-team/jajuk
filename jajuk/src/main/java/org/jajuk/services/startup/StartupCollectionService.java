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
package org.jajuk.services.startup;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import net.miginfocom.layout.LinkHandler;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.AlbumArtistManager;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.Collection;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.GenreManager;
import org.jajuk.base.ItemManager;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.TrackManager;
import org.jajuk.base.TypeManager;
import org.jajuk.base.YearManager;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.tags.Tag;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.SAXException;

/**
 * Startup facilities of the collection.
 */
public class StartupCollectionService {

  /** Mplayer state. */
  private static UtilSystem.MPlayerStatus mplayerStatus;

  /** Does a collection parsing error occurred ? *. */
  private static boolean bCollectionLoadRecover = true;

  /** Lock used to trigger a first time wizard device creation and refresh *. */
  static short[] canLaunchRefresh = new short[0];

  /**
   * Instantiates a new startup collection service.
   */
  private StartupCollectionService() {
    // private constructor to hide it from the outside
  }

   /**
   * Register all the different managers for the types of items that we know
   * about.
   */
  public static void registerItemManagers() {
    ItemManager.registerItemManager(org.jajuk.base.Album.class, AlbumManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Artist.class, ArtistManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.AlbumArtist.class, AlbumArtistManager
        .getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Device.class, DeviceManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.File.class, FileManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Directory.class, DirectoryManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Playlist.class, PlaylistManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Genre.class, GenreManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Track.class, TrackManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Type.class, TypeManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Year.class, YearManager.getInstance());
  }

  /**
   * Registers supported audio supports and default properties.
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
            Log.debug("Download Mplayer from: " + Const.URL_MPLAYER_WINDOWS);
            File fMPlayer = SessionService.getConfFileByPath(Const.FILE_MPLAYER_WINDOWS_EXE);
            DownloadManager.download(new URL(Const.URL_MPLAYER_WINDOWS), fMPlayer);
            // make sure to delete corrupted mplayer in case of
            // download problem
            if (fMPlayer.length() != Const.MPLAYER_WINDOWS_EXE_SIZE) {
              if (!fMPlayer.delete()) {
                Log.warn("Could not delete file " + fMPlayer);
              }
              mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_JNLP_DOWNLOAD_PBM;
            }
          } catch (IOException e) {
            mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_JNLP_DOWNLOAD_PBM;
          }
        }
      } else if (UtilSystem.isUnderOSX()) {
        final File mplayerPath = UtilSystem.getMPlayerOSXPath();
        // try to find mplayer executable in known locations first
        if (mplayerPath == null) {
          try {
            Log.debug("Download Mplayer from: " + Const.URL_MPLAYER_OSX);
            File fMPlayer = SessionService.getConfFileByPath(Const.FILE_MPLAYER_OSX_EXE);
            DownloadManager.download(new URL(Const.URL_MPLAYER_OSX), fMPlayer);
            fMPlayer.setExecutable(true);
            if (fMPlayer.length() != Const.MPLAYER_OSX_EXE_SIZE) {
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
      // Under others OS, we assume mplayer has been installed
      // using external standard distributions
      else {
        // If a forced mplayer path is defined, test it
        final String forced = Conf.getString(Const.CONF_MPLAYER_PATH_FORCED);
        if (!StringUtils.isBlank(forced)) {
          // Test forced path
          mplayerStatus = UtilSystem.getMplayerStatus(forced);
        } else {
          mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
        }
        if (mplayerStatus != UtilSystem.MPlayerStatus.MPLAYER_STATUS_OK) {
          // try to find a correct mplayer from the path
          mplayerStatus = UtilSystem.getMplayerStatus("");
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
    } catch (final ClassNotFoundException e1) {
      Log.error(26, e1);
    }
  }

  /** Auto commit thread. */
  private static Thread tAutoCommit = new Thread("Auto Commit Thread") {
    @Override
    public void run() {
      while (!ExitService.isExiting()) {
        try {
          Thread.sleep(Const.AUTO_COMMIT_DELAY);
          Log.debug("Auto commit");

          // call the overall "commit" to store things like Queue and
          // configuration periodically as well
          ExitService.commit(false);

          // workaround to free space in MigLayout
          // see http://migcalendar.com/forum/viewtopic.php?f=8&t=3236&p=7012
          LinkHandler.getValue("", "", 1); // simulated read

          // Clear the tag cache to avoid growing memory usage over time
          Tag.clearCache();
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }
  };

  /**
   * Load persisted collection file.
   */
  public static void loadCollection() {
    if (UpgradeManager.isFirstSession()) {
      Log.info("First session, collection will be created");
      return;
    }
    final File fCollection = SessionService.getConfFileByPath(Const.FILE_COLLECTION);
    final File fCollectionExit = SessionService.getConfFileByPath(Const.FILE_COLLECTION_EXIT);
    final File fCollectionExitProof = SessionService
        .getConfFileByPath(Const.FILE_COLLECTION_EXIT_PROOF);
    boolean bParsingOK = false;

    // Keep this complex proof / multiple collection file code, it is required
    // (see #1362)
    // The problem is that a bad shutdown can write down corrupted collection
    // file that would overwrite at exit good collection.xml automatically
    // commited during last jajuk session
    try {
      if (fCollectionExit.exists() && fCollectionExitProof.exists()) {
        // delete this file created just
        // after collection exit commit
        UtilSystem.deleteFile(fCollectionExitProof);
        Collection.load(fCollectionExit);
        // Remove the collection (required by renameTo next line under
        // Windows)
        UtilSystem.deleteFile(fCollection);
        // parsing of collection exit ok, use this collection file as
        // final collection
        if (!fCollectionExit.renameTo(fCollection)) {
          Log.warn("Cannot rename collection file");
        }
        bCollectionLoadRecover = false;
        bParsingOK = true;
      } else {
        bCollectionLoadRecover = true;
        throw new JajukException(5);
      }
    } catch (final Exception e) {
      Log.error(5, fCollectionExit.getAbsolutePath(), e);
      Log.debug("Jajuk was not closed properly during previous session, "
          + "we try to load a backup file");
      // Remove the corrupted collection file
      if (fCollectionExit.exists()) {
        try {
          UtilSystem.deleteFile(fCollectionExit);
        } catch (IOException e1) {
          Log.error(e1);
        }
      }

    }
    // If regular collection_exit.xml file parsing failed, try to parse
    // collection.xml. should be OK but not
    // always up-to-date.
    if (!bParsingOK) {
      try {
        Collection.load(fCollection);
        bParsingOK = true;
      } catch (final SAXException e1) {
        Log.error(5, fCollection.getAbsolutePath(), e1);
        bParsingOK = false;
      } catch (final ParserConfigurationException e1) {
        Log.error(5, fCollection.getAbsolutePath(), e1);
        bParsingOK = false;
      } catch (final JajukException e1) {
        Log.error(5, fCollection.getAbsolutePath(), e1);
        bParsingOK = false;
      } catch (final IOException e1) {
        Log.error(5, fCollection.getAbsolutePath(), e1);
        bParsingOK = false;
      }
    }

    // If even final collection file parsing failed
    // (very unlikely), try to restore a backup file
    if (!bParsingOK) {
      // try to restore a backup file
      final File[] fBackups = SessionService.getConfFileByPath("").listFiles(new FilenameFilter() {
        @Override
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
      while (!bParsingOK && it.hasNext()) {
        final File file = it.next();
        try {
          // Clear all previous collection
          Collection.clearCollection();
          // Load the backup file
          Collection.load(file);
          bParsingOK = true;
          // Show a message telling user that we use a backup file
          final int i = Messages.getChoice(Messages.getString("Error.133") + ":\n"
              + file.getAbsolutePath(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
          if (i == JOptionPane.CANCEL_OPTION) {
            System.exit(-1);
          }
          break;
        } catch (final SAXException e2) {
          Log.error(5, file.getAbsolutePath(), e2);
        } catch (final ParserConfigurationException e2) {
          Log.error(5, file.getAbsolutePath(), e2);
        } catch (final JajukException e2) {
          Log.error(5, file.getAbsolutePath(), e2);
        } catch (final IOException e2) {
          Log.error(5, file.getAbsolutePath(), e2);
        }
      }
    }

    // Still not better? ok, commit a void
    // collection (and a void collection is loaded)
    if (!bParsingOK) {
      Collection.clearCollection();
      System.gc();
      try {
        Collection.commit(SessionService.getConfFileByPath(Const.FILE_COLLECTION));
      } catch (final Exception e2) {
        Log.error(e2);
      }
    }

    Log.debug("Loaded " + FileManager.getInstance().getElementCount() + " files with "
        + TrackManager.getInstance().getElementCount() + " tracks, "
        + AlbumManager.getInstance().getElementCount() + " albums, "
        + ArtistManager.getInstance().getElementCount() + " artists, "
        + AlbumArtistManager.getInstance().getElementCount() + " album-artists, "
        + PlaylistManager.getInstance().getElementCount() + " playlists in "
        + DirectoryManager.getInstance().getElementCount() + " directories on "
        + DeviceManager.getInstance().getElementCount() + "devices.");

    // start auto commit thread
    tAutoCommit.start();
  }

  /**
   * Wait until user selected a device path in first time wizard.
   */
  public static void waitForLaunchRefresh() {
    synchronized (canLaunchRefresh) {
      try {
        canLaunchRefresh.wait();
      } catch (final InterruptedException e) {
        Log.error(e);
      }
    }
  }

  /**
   * Checks if is collection load recover.
   * 
   * @return true, if is collection load recover
   */
  public static boolean isCollectionLoadRecover() {
    return bCollectionLoadRecover;
  }

}
