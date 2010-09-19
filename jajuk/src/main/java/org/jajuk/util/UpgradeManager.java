/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

package org.jajuk.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.jajuk.base.AlbumManager;
import org.jajuk.base.Collection;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.util.log.Log;

/**
 * Maintain all behavior needed upgrades from releases to releases.
 * 
 * Jajuk version sheme is XX.YY.ZZ (two digits possible for each part of the release)
 */
public final class UpgradeManager {

  /** Last jajuk release known from Internet (parsed from a pad file). */
  private static String newVersionName;

  /** Is it a minor or major X.Y upgrade */
  private static boolean bUpgraded = false;

  /** Is it the first session ever ?. */
  private static boolean bFirstSession = false;

  /** Is it an old migration (more than 1 major release) ?. */
  private static boolean majorMigration = false;

  /** List of versions that doesn't require perspective reset at upgrade */
  private static String[] versionsNoNeedPerspectiveReset = new String[] { "1.9" };

  /**
  * private constructor to avoid instantiating utility class.
  */
  private UpgradeManager() {
  }

  /**
   * Return Jajuk number version = integer format of the padded release
   * 
   * Jajuk version scheme is XX.YY.ZZ[RCn] (two digits possible for each part of the release)
   * 
   * @return Jajuk number version = integer format of the padded release
   */
  static int getNumberRelease(String pStringRelease) {
    if (pStringRelease == null) {
      // no string provided: use 1.0.0
      return 10000;
    }

    String stringRelease = pStringRelease;
    // We drop any RCx part of the release
    if (pStringRelease.contains("RC")) {
      stringRelease = pStringRelease.split("RC.*")[0];
    }
    // Add a trailing .0 if it is a main release like 1.X -> 1.X.0
    int countDot = stringRelease.replaceAll("[^.]", "").length();
    if (countDot == 1) {
      stringRelease = stringRelease + ".0";
    }
    // Analyze each part of the release, throw a runtime exception if
    // the format is wrong at this point
    StringTokenizer st = new StringTokenizer(stringRelease, ".");
    String main = UtilString.padNumber(Integer.parseInt(st.nextToken()), 2);
    String minor = UtilString.padNumber(Integer.parseInt(st.nextToken()), 2);

    String fix = UtilString.padNumber(Integer.parseInt(st.nextToken()), 2);
    return Integer.parseInt(main + minor + fix);
  }

  /**
   * Detect current release and if an upgrade occurred since last startup.
   */
  public static void detectRelease() {
    try {
      // Upgrade detection. Depends on: Configuration manager load
      final String sStoredRelease = Conf.getString(Const.CONF_RELEASE);

      // check if it is a new major 'x.y' release: 1.2 != 1.3 for instance
      if (!bFirstSession
      // if first session, not taken as an upgrade
          && (sStoredRelease == null || // null for jajuk releases < 1.2
          !sStoredRelease.substring(0, 3).equals(Const.JAJUK_VERSION.substring(0, 3)))
          // Each RC is seen as an upgrade to force RC users to re-run upgrade code at each new RC
          || Const.JAJUK_VERSION.matches(".*RC.*")) {
        bUpgraded = true;
        // Now check if this is an old migration.
        if (!SessionService.isTestMode()) {
          if (isMajorMigration(Const.JAJUK_VERSION, sStoredRelease)) {
            majorMigration = true;
          }
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
    if (SessionService.isTestMode()) {
      // In test mode, we are always in upgraded mode
      bUpgraded = true;
    }
    // Now set current release in the conf
    Conf.setProperty(Const.CONF_RELEASE, Const.JAJUK_VERSION);
  }

  /**
   * Checks if is first sesion.
   * 
   * @return true, if is first sesion
   */
  public static boolean isFirstSession() {
    return bFirstSession;
  }

  /**
   * Sets the first session.
   * DOCUMENT_ME
   */
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

        // for Jajuk < 1.9
        upgradeAlarmConfFile();

      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * For Jajuk < 0.2 : remove backup file : collection~.xml
   * @throws IOException 
   */
  private static void upgradeOldCollectionBackupFile() throws IOException {
    File file = SessionService.getConfFileByPath(Const.FILE_COLLECTION + "~");
    if (file.exists()) {
      UtilSystem.deleteFile(file);
    }
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
    String sPattern = Conf.getString(Const.CONF_PATTERN_REFACTOR);
    if (sPattern.contains("track")) {
      Conf.setProperty(Const.CONF_PATTERN_REFACTOR, sPattern.replaceAll("track", "title"));
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
        try {
          UtilSystem.deleteFile(element);
        } catch (IOException e) {
          Log.error(e);
        }
      }
    }
  }

  /**
   * For Jajuk < 1.3: force nocover thumb replacement
   */
  private static void upgradeNocover() {
    upgradeNoCoverDelete("50x50");
    upgradeNoCoverDelete("100x100");
    upgradeNoCoverDelete("150x150");
    upgradeNoCoverDelete("200x200");
  }

  /**
   * For Jajuk < 1.3: delete thumb for given size 
   */
  private static void upgradeNoCoverDelete(String size) {
    File fThumbs = SessionService.getConfFileByPath(Const.FILE_THUMBS + "/" + size + "/"
        + Const.FILE_THUMB_NO_COVER);
    if (fThumbs.exists()) {
      try {
        UtilSystem.deleteFile(fThumbs);
      } catch (IOException e) {
        Log.error(e);
      }
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
   * For jajuk < 1.9: Alarm configuration, file / webradio to be launched
   */
  private static void upgradeAlarmConfFile() {
    String conf = Conf.getString(Const.CONF_ALARM_FILE);
    if (conf.indexOf('/') == -1) {
      conf = SearchResultType.FILE.name() + '/' + conf;
      Conf.setProperty(Const.CONF_ALARM_FILE, conf);
    }
  }

  /**
   * For jajuk <1.4 (or early 1.4), some perspectives have been renamed
   */
  private static void upgradePerspectivesRename() {
    upgradePerspectivesRenameDelete("LogicalPerspective.xml");
    upgradePerspectivesRenameDelete("PhysicalPerspective.xml");
    upgradePerspectivesRenameDelete("CatalogPerspective.xml");
    upgradePerspectivesRenameDelete("PlayerPerspective.xml");
    upgradePerspectivesRenameDelete("HelpPerspective.xml");
  }

  /**
  * For jajuk <1.4 (or early 1.4), delete renamed perspectives names
  * @param name : perspective filename
  */
  private static void upgradePerspectivesRenameDelete(String name) {
    File fPerspective = SessionService.getConfFileByPath(name);
    if (fPerspective.exists()) {
      try {
        UtilSystem.deleteFile(fPerspective);
      } catch (IOException e) {
        Log.error(e);
      }
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
   */
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
  * For jajuk < 1.9, remove album artist property for albums
  */
  private static void upgradeNoMoreAlbumArtistsForAlbums() {
    if (AlbumManager.getInstance().getMetaInformation(Const.XML_ALBUM_ARTIST) != null) {
      AlbumManager.getInstance().removeProperty(Const.XML_ALBUM_ARTIST);
    }

  }

  /**
   * For any jajuk version, after major upgrade, force thumbs cleanup.
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
      }
    }.start();
  }

  /**
   * Actions to migrate an existing installation.
   * 
   * Step 2 after collection load
   */
  public static void upgradeStep2() {
    try {
      if (isUpgradeDetected()) {
        // For Jajuk < 1.7
        upgradeCollectionRating();
        // For Jajuk < 1.9
        upgradeNoMoreAlbumArtistsForAlbums();
      }
      // Major releases upgrade specific operations
      if (isMajorMigration()) {
        upgradeThumbRebuild();
      }
    } catch (Throwable e) {
      Log.error(e);
    }

  }

  /**
   * Actions to migrate an existing installation.
   * 
   * Step 3 after full jajuk startup
   */
  public static void upgradeStep3() {
    try {
      // Major releases upgrade specific operations
      if (isMajorMigration()) {
        deepScanRequest();
      }
    } catch (Throwable e) {
      Log.error(e);
    }

  }

  /**
   * Checks if is upgrade detected.
   * 
   * @return true if it is the first session after a minor or major upgrade
   * session
   */
  public static boolean isUpgradeDetected() {
    return bUpgraded;
  }

  /**
   * Check for a new Jajuk release.
   * 
   * @return true if a new release has been found
   */
  public static void checkForUpdate() {
    // If test mode, don't try to update
    if (SessionService.isTestMode()) {
      return;
    }
    // Try to download current jajuk PAD file
    String sPadRelease = null;
    try {
      String pad = DownloadManager.downloadText(new URL(Const.CHECK_FOR_UPDATE_URL));
      int beginIndex = pad.indexOf("<Program_Version>");
      int endIndex = pad.indexOf("</Program_Version>");
      sPadRelease = pad.substring(beginIndex + 17, endIndex);
      if (!Const.JAJUK_VERSION.equals(sPadRelease)
      // Don't use this in test
          && !("VERSION_REPLACED_BY_ANT".equals(Const.JAJUK_VERSION))
          // We display the upgrade icon only if PAD release is newer than current release
          && isNewer(Const.JAJUK_VERSION, sPadRelease)) {
        newVersionName = sPadRelease;
        return;
      }
    } catch (Exception e) {
      Log.debug("Cannot check for updates", e);
    }
    return;
  }

  /**
   * Gets the new version name.
   * 
   * @return new version name if nay
   * <p>
   * Example: "1.6", "1.7.8"
   */
  public static String getNewVersionName() {
    return newVersionName;
  }

  /**
   * Is it an old migration (more than 1 major release) ? *.
   * 
   * @return true, if checks if is major migration
   */
  public static boolean isMajorMigration() {
    return majorMigration;
  }

  /**
   * Return whether two releases switch is a major upgrade or not
   * @param currentRelease
   * @param comparedRelease
   * @return whether two releases switch is a major upgrade or not
   */
  protected static boolean isMajorMigration(String currentRelease, String comparedRelease) {
    int iCurrentRelease = getNumberRelease(currentRelease);
    int iComparedRelease = getNumberRelease(comparedRelease);
    return iComparedRelease / 100 != iCurrentRelease / 100;
  }

  /**
  * Return whether second release is newer than first
  * @param currentRelease
  * @param comparedRelease
  * @return whether second release is newer than first
  */
  protected static boolean isNewer(String currentRelease, String comparedRelease) {
    int iCurrentRelease = getNumberRelease(currentRelease);
    int iComparedRelease = getNumberRelease(comparedRelease);
    return iComparedRelease > iCurrentRelease;
  }

  /**
   * Require user to perform a deep scan
   */
  private static void deepScanRequest() {
    int reply = Messages.getChoice(Messages.getString("Warning.7"),
        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    if (reply == JOptionPane.CANCEL_OPTION || reply == JOptionPane.NO_OPTION) {
      return;
    }
    if (reply == JOptionPane.YES_OPTION) {
      final Thread t = new Thread("Device Refresh Thread after upgrade") {
        @Override
        public void run() {
          List<Device> devices = DeviceManager.getInstance().getDevices();
          for (Device device : devices) {
            if (device.isReady()) {
              device.manualRefresh(false, false, true);
            }
          }
        }
      };
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    }
  }

  /**
   * Return whether this version need a perspective reset at upgrade.
   * We reset perspectives only at major upgrade and if it comes with new views.  
   * @return whether this version need a perspective reset at upgrade
   */
  public static boolean doNeedPerspectiveResetAtUpgrade() {
    if (!isMajorMigration()) {
      return false;
    }
    for (String version : versionsNoNeedPerspectiveReset) {
      if (Const.JAJUK_VERSION.matches(version + ".*")) {
        return false;
      }
    }
    return true;
  }
}
