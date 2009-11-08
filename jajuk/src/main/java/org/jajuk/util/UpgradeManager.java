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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;

import org.jajuk.base.Collection;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.thumbnails.ThumbnailsMaker;
import org.jajuk.util.log.Log;

/**
 * Maintain all behavior needed upgrades from releases to releases
 */
public final class UpgradeManager {

  private static String newVersionName;

  /** Is it a minor or major X.Y upgrade */
  private static boolean bUpgraded = false;

  /** Is it the first session ever ? */
  private static boolean bFirstSession = false;

  /** Is it an old migration (more than 1 major release) ? */
  private static boolean majorMigration = false;

  /**
   * private constructor to avoid instantiating utility class
   */
  private UpgradeManager() {
  }

  /**
   * Detect current release and if an upgrade occurred since last startup
   */
  public static void detectRelease() {
    try {
      // Upgrade detection. Depends on: Configuration manager load
      final String sRelease = Conf.getString(Const.CONF_RELEASE);

      // check if it is a new major 'x.y' release: 1.2 != 1.3 for instance
      if (!bFirstSession
      // if first session, not taken as an upgrade
          && ((sRelease == null) || // null for jajuk releases < 1.2
          !sRelease.substring(0, 3).equals(Const.JAJUK_VERSION.substring(0, 3)))) {
        bUpgraded = true;
        // Now check if this is an old migration. We assume than version goes
        // this way : x.0 -> x.9 -> y.0-> y.9 ... (no x.10 or later)
        if (!SessionService.isTestMode()) {
          int currentRelease = Integer.parseInt((sRelease == null ? "0.0" : sRelease).charAt(0)
              + "" + (sRelease == null ? "0.0" : sRelease).charAt(2));
          int newRelease = Integer.parseInt(Const.JAJUK_VERSION.charAt(0) + ""
              + Const.JAJUK_VERSION.charAt(2));
          if (Math.abs(newRelease - currentRelease) >= 1) {
            majorMigration = true;
          }
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
    // Now set current release in the conf
    Conf.setProperty(Const.CONF_RELEASE, Const.JAJUK_VERSION);
  }

  public static boolean isFirstSesion() {
    return bFirstSession;
  }

  public static void setFirstSession() {
    bFirstSession = true;
  }

  
  /**
   * Actions to migrate an existing installation.
   * 
   * Step 1 : before collection loading
   */
  public static void upgradeStep1() {
    // We ignore errors during upgrade
    try {
      if (isUpgradeDetected()) {
        // For jajuk < 0.2
        upgradeOldCollectionBackupFile();

        // For Jajuk < 1.2
        upgradeDefaultAmbience();

        // For Jajuk < 1.3
        upgradeTrackPattern();
        upgradeSerFiles();
        upgradeNocover();
        upgradeWrongHotketOption();

        // For Jajuk < 1.4
        upgradePerspectivesRename();

        // For Jajuk < 1.6
        upgradePerspectiveButtonsSize();
        upgradeDJClassChanges();

        // For Jajuk < 1.7
        upgradeElapsedTimeFormat();
       
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * For Jajuk < 0.2 : remove backup file : collection~.xml
   */
  private static void upgradeOldCollectionBackupFile() {
    File file = SessionService.getConfFileByPath(Const.FILE_COLLECTION + "~");
    file.delete();
  }

  /**
   * For Jajuk <1.2, set default ambiences
   */
  private static void upgradeDefaultAmbience() {
    String sRelease = Conf.getString(Const.CONF_RELEASE);
    if (sRelease == null || sRelease.matches("0..*") || sRelease.matches("1.0..*")
        || sRelease.matches("1.1.*")) {
      AmbienceManager.getInstance().createDefaultAmbiences();
    }
  }

  /**
   * For Jajuk < 1.3 : changed track pattern from %track to %title
   */
  private static void upgradeTrackPattern() {
    String sPattern = Conf.getString(Const.CONF_REFACTOR_PATTERN);
    if (sPattern.contains("track")) {
      Conf.setProperty(Const.CONF_REFACTOR_PATTERN, sPattern.replaceAll("track", "title"));
    }
  }

  /**
   * For Jajuk < 1.3: no more use of .ser files
   */
  private static void upgradeSerFiles() {
    File file = SessionService.getConfFileByPath("");
    File[] files = file.listFiles();
    for (File element : files) {
      // delete all .ser files
      if (UtilSystem.getExtension(element).equals("ser")) {
        element.delete();
      }
    }
  }

  /**
   * For Jajuk < 1.3: force nocover icon replacement
   */
  private static void upgradeNocover() {
    File fThumbs = SessionService.getConfFileByPath(Const.FILE_THUMBS + "/50x50/"
        + Const.FILE_THUMB_NO_COVER);
    if (fThumbs.exists()) {
      fThumbs.delete();
    }
    fThumbs = SessionService.getConfFileByPath(Const.FILE_THUMBS + "/100x100/"
        + Const.FILE_THUMB_NO_COVER);
    if (fThumbs.exists()) {
      fThumbs.delete();
    }
    fThumbs = SessionService.getConfFileByPath(Const.FILE_THUMBS + "/150x150/"
        + Const.FILE_THUMB_NO_COVER);
    if (fThumbs.exists()) {
      fThumbs.delete();
    }
    fThumbs = SessionService.getConfFileByPath(Const.FILE_THUMBS + "/200x200/"
        + Const.FILE_THUMB_NO_COVER);
    if (fThumbs.exists()) {
      fThumbs.delete();
    }
  }

  /**
   * jajuk 1.3: wrong option name: "false" instead of
   * "jajuk.options.use_hotkeys"
   */
  private static void upgradeWrongHotketOption() {
    String sUseHotkeys = Conf.getString("false");
    if (sUseHotkeys != null) {
      if (sUseHotkeys.equalsIgnoreCase(Const.FALSE) || sUseHotkeys.equalsIgnoreCase(Const.TRUE)) {
        Conf.setProperty(Const.CONF_OPTIONS_HOTKEYS, sUseHotkeys);
        Conf.removeProperty("false");
      } else {
        Conf.setProperty(Const.CONF_OPTIONS_HOTKEYS, Const.FALSE);
      }
    }
  }

  /**
   * For jajuk <1.4 (or early 1.4), some perspectives have been renamed
   */
  private static void upgradePerspectivesRename() {
    File fPerspective = SessionService.getConfFileByPath("LogicalPerspective.xml");
    if (fPerspective.exists()) {
      fPerspective.delete();
    }
    fPerspective = SessionService.getConfFileByPath("PhysicalPerspective.xml");
    if (fPerspective.exists()) {
      fPerspective.delete();
    }
    fPerspective = SessionService.getConfFileByPath("CatalogPerspective.xml");
    if (fPerspective.exists()) {
      fPerspective.delete();
    }
    fPerspective = SessionService.getConfFileByPath("PlayerPerspective.xml");
    if (fPerspective.exists()) {
      fPerspective.delete();
    }
    fPerspective = SessionService.getConfFileByPath("HelpPerspective.xml");
    if (fPerspective.exists()) {
      fPerspective.delete();
    }
  }

  /**
   * Jajuk < 1.6. Perspective buttons size changed.
   */
  private static void upgradePerspectiveButtonsSize() {
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) > 45) {
      Conf.setProperty(Const.CONF_PERSPECTIVE_ICONS_SIZE, "45");
    }
    // For Jajuk 1.5 and jajuk 1.6 columns conf id changed
    if (Conf.getString(Const.CONF_PLAYLIST_REPOSITORY_COLUMNS).matches(".*0.*")) {
      Conf.setDefaultProperty(Const.CONF_PLAYLIST_REPOSITORY_COLUMNS);
    }
    if (Conf.getString(Const.CONF_QUEUE_COLUMNS).matches(".*0.*")) {
      Conf.setDefaultProperty(Const.CONF_QUEUE_COLUMNS);
    }
    if (Conf.getString(Const.CONF_PLAYLIST_EDITOR_COLUMNS).matches(".*0.*")) {
      Conf.setDefaultProperty(Const.CONF_PLAYLIST_EDITOR_COLUMNS);
    }
  }

  /**
   * For Jajuk < 1.6 (DJ classes changed)
   */
  private static void upgradeDJClassChanges() {
    File[] files = SessionService.getConfFileByPath(Const.FILE_DJ_DIR).listFiles(new FileFilter() {
      public boolean accept(File file) {
        if (file.isFile() && file.getPath().endsWith('.' + Const.XML_DJ_EXTENSION)) {
          return true;
        }
        return false;
      }
    });
    for (File dj : files) {
      if (UtilSystem.replaceInFile(dj, "org.jajuk.dj.ProportionDigitalDJ",
          Const.XML_DJ_PROPORTION_CLASS, "UTF-8")) {
        Log.info("Migrated DJ file: {{" + dj.getName() + "}}");
      }
      if (UtilSystem.replaceInFile(dj, "org.jajuk.dj.TransitionDigitalDJ",
          Const.XML_DJ_TRANSITION_CLASS, "UTF-8")) {
        Log.info("Migrated DJ file: {{" + dj.getName() + "}}");
      }
      if (UtilSystem.replaceInFile(dj, "org.jajuk.dj.AmbienceDigitalDJ",
          Const.XML_DJ_AMBIENCE_CLASS, "UTF-8")) {
        Log.info("Migrated DJ file: {{" + dj.getName() + "}}");
      }
    }
  }

  /**
   * For Jajuk < 1.7, elapsed time format variable name changed
   */
  private static void upgradeElapsedTimeFormat() {
    if (Conf.containsProperty("format")) {
      Conf.setProperty(Const.CONF_FORMAT_TIME_ELAPSED, Conf.getString("format"));
    }
  }

  /**
   * For jajuk < 1.7, Update rating system
   **/
  private static void upgradeCollectionRating() {
    String sRelease = Conf.getString(Const.CONF_RELEASE);
    if (sRelease == null || sRelease.matches("0..*")
        || (sRelease.matches("1..*") && Integer.parseInt(sRelease.substring(2, 3)) < 7)) {
      Log.info("Migrating collection rating");
      // We keep current ratings and we recompute them on a 0 to 100 scale,
      // then we suggest user to reset the rates

      // Start by finding max (old) rating
      long maxRating = 0;
      ReadOnlyIterator<Track> tracks = TrackManager.getInstance().getTracksIterator();
      while (tracks.hasNext()) {
        Track track = tracks.next();
        if (track.getRate() > maxRating) {
          maxRating = track.getRate();
        }
      }
      // Then apply the new rating
      for (Track track : TrackManager.getInstance().getTracks()) {
        long newRate = (long) (100f * track.getRate() / maxRating);
        TrackManager.getInstance().changeTrackRate(track, newRate);
      }
      // Save collection
      try {
        Collection.commit(SessionService.getConfFileByPath(Const.FILE_COLLECTION));
      } catch (final IOException e) {
        Log.error(e);
      }
      Log.info("Migrating rating done");
      Messages.showInfoMessage(Messages.getString("Note.1"));
    }
  }

  /**
   * For any jajuk version, after major upgrade, force thumbs cleanup
   */
  private static void upgradeThumbRebuild() {
    // Rebuild thumbs when upgrading
    new Thread() {
      @Override
      public void run() {

        // Clean thumbs
        ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_50X50);
        ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_100X100);
        ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_150X150);
        ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_200X200);
        ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_250X250);
        ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_300X300);
        // Launch thumbs creation in another process
        ThumbnailsMaker.launchAllSizes(true);
      }
    }.start();
  }

  /**
   * Actions to migrate an existing installation.
   * 
   *  Step 2 after collection load
   */
  public static void upgradeStep2() {
    try {
      if (isUpgradeDetected()) {
        // For Jajuk < 1.7
        upgradeCollectionRating();
      }
      // Major releases upgrade specific operations
      if (isMajorMigration()) {
        upgradeThumbRebuild();
      }

    } catch (Exception e) {
      Log.error(e);
    }

  }

  /**
   * @return true if it is the first session after a minor or major upgrade
   *         session
   */
  public static boolean isUpgradeDetected() {
    return bUpgraded;
  }

  /**
   * Check for a new Jajuk release
   * 
   * @return true if a new release has been found
   */
  public static void checkForUpdate() {
    // If test mode, don't try to update
    if (SessionService.isTestMode()) {
      return;
    }
    // Try to download current jajuk PAD file
    String sRelease = null;
    try {
      String pad = DownloadManager.downloadText(new URL(Const.CHECK_FOR_UPDATE_URL));
      int beginIndex = pad.indexOf("<Program_Version>");
      int endIndex = pad.indexOf("</Program_Version>");
      sRelease = pad.substring(beginIndex + 17, endIndex);
      if (!Const.JAJUK_VERSION.equals(sRelease)
      // Don't use this in test
          && !("VERSION_REPLACED_BY_ANT".equals(Const.JAJUK_VERSION))) {
        newVersionName = sRelease;
        return;
      }
    } catch (Exception e) {
      Log.debug("Cannot check for updates", e);
    }
    return;
  }

  /**
   * 
   * @return new version name if nay
   *         <p>
   *         Example: "1.6", "1.7.8"
   */
  public static String getNewVersionName() {
    return newVersionName;
  }

  /** Is it an old migration (more than 1 major release) ? * */
  public static boolean isMajorMigration() {
    return majorMigration;
  }
}
