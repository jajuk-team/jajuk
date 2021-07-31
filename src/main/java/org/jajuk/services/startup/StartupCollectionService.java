/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.services.startup;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
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
import org.jajuk.services.core.SessionService;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Startup facilities of the collection.
 */
public final class StartupCollectionService {
  /** MPlayer state. */
  private static UtilSystem.MPlayerStatus mplayerStatus;

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
    ItemManager.registerItemManager(org.jajuk.base.AlbumArtist.class,
        AlbumArtistManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Device.class, DeviceManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.File.class, FileManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Directory.class, DirectoryManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Playlist.class, PlaylistManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Genre.class, GenreManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Track.class, TrackManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Type.class, TypeManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.base.Year.class, YearManager.getInstance());
    ItemManager.registerItemManager(org.jajuk.services.webradio.WebRadio.class,
        WebRadioManager.getInstance());
  }

  /**
   * Registers supported audio supports and default properties.
   */
  public static void registerTypes() {
    try {
      // test mplayer presence in PATH
      mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_OK;
      if (UtilSystem.isUnderWindows() || UtilSystem.isUnderOSX()) {
        final File mplayerPath = UtilSystem.getMPlayerWindowsPath();
        // try to find mplayer executable in know locations first
        if (mplayerPath == null) {
            Log.warn("Mplayer not found, installation probably corrupted, please reinstall");
            mplayerStatus = UtilSystem.MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;          
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

  /**
   * Load persisted collection file.
   */
  public static void loadCollection() {
    if (UpgradeManager.isFirstSession()) {
      Log.info("First session, collection will be created");
      return;
    }
    final File fCollection = SessionService.getConfFileByPath(Const.FILE_COLLECTION);
    try {
      Collection.load(fCollection);
      backupCollectionFileAsynchronously();
    } catch (final Exception e) {
      handleCollectionParsingError(fCollection, e);
      tryToParseABackupFile();
    }
    Log.debug("Loaded " + FileManager.getInstance().getElementCount() + " files with "
        + TrackManager.getInstance().getElementCount() + " tracks, "
        + AlbumManager.getInstance().getElementCount() + " albums, "
        + ArtistManager.getInstance().getElementCount() + " artists, "
        + AlbumArtistManager.getInstance().getElementCount() + " album-artists, "
        + PlaylistManager.getInstance().getElementCount() + " playlists in "
        + DirectoryManager.getInstance().getElementCount() + " directories on "
        + DeviceManager.getInstance().getElementCount() + " devices.");
  }

  private static void tryToParseABackupFile() {
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
    boolean parsingOK = false;
    while (!parsingOK && it.hasNext()) {
      final File file = it.next();
      try {
        // Clear all previous collection
        Collection.clearCollection();
        // Load the backup file
        Collection.load(file);
        parsingOK = true;
        // Show a message telling user that we use a backup file
        final int i = Messages.getChoice(
            Messages.getString("Error.133") + ":\n" + file.getAbsolutePath(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        if (i == JOptionPane.CANCEL_OPTION) {
          System.exit(-1); //NOSONAR
        }
        break;
      } catch (final Exception e2) {
        Log.error(5, file.getAbsolutePath(), e2);
      }
    }
  }

  private static void handleCollectionParsingError(final File fCollection, final Exception e) {
    Log.error(5, fCollection.getAbsolutePath(), e);
    Log.debug("Jajuk was not closed properly during previous session, "
        + "we try to load a backup file");
    Messages.showErrorMessage(5, e.getMessage());
  }

  private static void backupCollectionFileAsynchronously() {
    new Thread() {
      @Override
      public void run() {
        UtilSystem.backupFile(SessionService.getConfFileByPath(Const.FILE_COLLECTION),
            Conf.getInt(Const.CONF_BACKUP_SIZE));
      }
    }.start();
  }
}
