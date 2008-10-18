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

import org.jajuk.Main;
import org.jajuk.base.Collection;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.services.dj.AmbienceManager;
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

  /**
   * private constructor to avoid instantiating utility class
   */
  private UpgradeManager() {
  }

  /**
   * Detect current release and if an upgrade occurred since last startup
   */
  public static void detectRelease() {
    // Upgrade detection. Depends on: Configuration manager load
    final String sRelease = Conf.getString(Const.CONF_RELEASE);

    // check if it is a new major 'x.y' release: 1.2 != 1.3 for instance
    if (!bFirstSession
    // if first session, not taken as an upgrade
        && ((sRelease == null) || // null for jajuk releases < 1.2
        !sRelease.substring(0, 3).equals(Const.JAJUK_VERSION.substring(0, 3)))) {
      bUpgraded = true;
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
   * Actions to migrate an existing installation Step1 just at startup
   */
  public static void upgradeStep1() throws Exception {
    // We ignore errors during upgrade
    try {
      if (isUpgradeDetected()) {
        // Migrate djs from jajuk < 1.6 (DJ classes changed)
        File[] files = UtilSystem.getConfFileByPath(Const.FILE_DJ_DIR).listFiles(new FileFilter() {
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
            Log.info("Migrated DJ file: " + dj.getName());
          }
          if (UtilSystem.replaceInFile(dj, "org.jajuk.dj.TransitionDigitalDJ",
              Const.XML_DJ_TRANSITION_CLASS, "UTF-8")) {
            Log.info("Migrated DJ file: " + dj.getName());
          }
          if (UtilSystem.replaceInFile(dj, "org.jajuk.dj.AmbienceDigitalDJ",
              Const.XML_DJ_AMBIENCE_CLASS, "UTF-8")) {
            Log.info("Migrated DJ file: " + dj.getName());
          }
        }
        // --For jajuk < 0.2 : remove backup file : collection~.xml
        File file = UtilSystem.getConfFileByPath(Const.FILE_COLLECTION + "~");
        if (!file.delete()) {
          Log.warn("Could not delete file " + file);
        }
        // upgrade code; if upgrade from <1.2, set default ambiences
        String sRelease = Conf.getString(Const.CONF_RELEASE);
        if (sRelease == null || sRelease.matches("0..*") || sRelease.matches("1.0..*")
            || sRelease.matches("1.1.*")) {
          AmbienceManager.getInstance().createDefaultAmbiences();
        }
        // - For Jajuk < 1.3 : changed track pattern from %track to %title
        String sPattern = Conf.getString(Const.CONF_REFACTOR_PATTERN);
        if (sPattern.contains("track")) {
          Conf.setProperty(Const.CONF_REFACTOR_PATTERN, sPattern.replaceAll("track", "title"));
        }
        // - for Jajuk < 1.3: no more use of .ser files
        file = UtilSystem.getConfFileByPath("");
        files = file.listFiles();
        for (File element : files) {
          // delete all .ser files
          if (UtilSystem.getExtension(element).equals("ser")) {
            element.delete();
          }
        }
        // - for jajuk 1.3: wrong option name: "false" instead of
        // "jajuk.options.use_hotkeys"
        String sUseHotkeys = Conf.getString("false");
        if (sUseHotkeys != null) {
          if (sUseHotkeys.equalsIgnoreCase(Const.FALSE) || sUseHotkeys.equalsIgnoreCase(Const.TRUE)) {
            Conf.setProperty(Const.CONF_OPTIONS_HOTKEYS, sUseHotkeys);
            Conf.removeProperty("false");
          } else {
            Conf.setProperty(Const.CONF_OPTIONS_HOTKEYS, Const.FALSE);
          }
        }
        // for jajuk <1.4 (or early 1.4), some perspectives have been renamed
        File fPerspective = UtilSystem.getConfFileByPath("LogicalPerspective.xml");
        if (fPerspective.exists()) {
          fPerspective.delete();
        }
        fPerspective = UtilSystem.getConfFileByPath("PhysicalPerspective.xml");
        if (fPerspective.exists()) {
          fPerspective.delete();
        }
        fPerspective = UtilSystem.getConfFileByPath("CatalogPerspective.xml");
        if (fPerspective.exists()) {
          fPerspective.delete();
        }
        fPerspective = UtilSystem.getConfFileByPath("PlayerPerspective.xml");
        if (fPerspective.exists()) {
          fPerspective.delete();
        }
        fPerspective = UtilSystem.getConfFileByPath("HelpPerspective.xml");
        if (fPerspective.exists()) {
          fPerspective.delete();
        }
        // For Jajuk < 1.6
        // Perspective buttons
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

        // For Jajuk < 1.7, elapsed time format variable name changed
        if (Conf.containsProperty("format")) {
          Conf.setProperty(Const.CONF_FORMAT_TIME_ELAPSED, Conf.getString("format"));
        }

        // - for Jajuk < 1.3: force nocover icon replacement
        File fThumbs = UtilSystem.getConfFileByPath(Const.FILE_THUMBS + "/50x50/"
            + Const.FILE_THUMB_NO_COVER);
        if (fThumbs.exists()) {
          fThumbs.delete();
        }
        fThumbs = UtilSystem.getConfFileByPath(Const.FILE_THUMBS + "/100x100/"
            + Const.FILE_THUMB_NO_COVER);
        if (fThumbs.exists()) {
          fThumbs.delete();
        }
        fThumbs = UtilSystem.getConfFileByPath(Const.FILE_THUMBS + "/150x150/"
            + Const.FILE_THUMB_NO_COVER);
        if (fThumbs.exists()) {
          fThumbs.delete();
        }
        fThumbs = UtilSystem.getConfFileByPath(Const.FILE_THUMBS + "/200x200/"
            + Const.FILE_THUMB_NO_COVER);
        if (fThumbs.exists()) {
          fThumbs.delete();
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Actions to migrate an existing installation Step 2 after collection load
   */
  public static void upgradeStep2() {
    try {
      if (isUpgradeDetected()) {
        // For Jajuk < 1.7, Update rating system
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
            Collection.commit(UtilSystem.getConfFileByPath(Const.FILE_COLLECTION));
          } catch (final IOException e) {
            Log.error(e);
          }
          Log.info("Migrating rating done");
          Messages.showInfoMessage(Messages.getString("Note.1"));
        }
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
    if (Main.isTestMode()) {
      return;
    }
    // Try to download current jajuk PAD file
    String sRelease = null;
    try {
      String pad = DownloadManager.downloadHtml(new URL(Const.CHECK_FOR_UPDATE_URL));
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
}
