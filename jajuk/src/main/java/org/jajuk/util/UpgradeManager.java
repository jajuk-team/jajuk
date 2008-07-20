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
import java.net.URL;

import org.jajuk.Main;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.util.log.Log;

/**
 * Maintain all behavior needed upgrades from releases to releases
 */
public final class UpgradeManager implements Const {

  private static String newVersionName;

  /** 
   * private constructor to avoid instantiating utility class
   */
  private UpgradeManager() {
  }
  
  /**
   * Actions to migrate an existing installation Step1 just at startup
   */
  public static void upgradeStep1() throws Exception {
    // --For jajuk < 0.2 : remove backup file : collection~.xml
    File file = UtilSystem.getConfFileByPath(FILE_COLLECTION + "~");
    if(!file.delete()) {
      Log.warn("Could not delete file " + file);
    }
    // upgrade code; if upgrade from <1.2, set default ambiences
    String sRelease = Conf.getString(CONF_RELEASE);
    if (sRelease == null || sRelease.matches("0..*") || sRelease.matches("1.0..*")
        || sRelease.matches("1.1.*")) {
      AmbienceManager.getInstance().createDefaultAmbiences();
    }
    // - For Jajuk < 1.3 : changed track pattern from %track to %title
    String sPattern = Conf.getString(CONF_REFACTOR_PATTERN);
    if (sPattern.contains("track")) {
      Conf
          .setProperty(CONF_REFACTOR_PATTERN, sPattern.replaceAll("track", "title"));
    }
    // - for Jajuk < 1.3: no more use of .ser files
    file = UtilSystem.getConfFileByPath("");
    File[] files = file.listFiles();
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
      if (sUseHotkeys.equalsIgnoreCase(FALSE) || sUseHotkeys.equalsIgnoreCase(TRUE)) {
        Conf.setProperty(CONF_OPTIONS_HOTKEYS, sUseHotkeys);
        Conf.removeProperty("false");
      } else {
        Conf.setProperty(CONF_OPTIONS_HOTKEYS, FALSE);
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
    if (Conf.getString(CONF_PLAYLIST_REPOSITORY_COLUMNS).matches(".*0.*")) {
      Conf.setDefaultProperty(CONF_PLAYLIST_REPOSITORY_COLUMNS);
    }
    if (Conf.getString(CONF_QUEUE_COLUMNS).matches(".*0.*")) {
      Conf.setDefaultProperty(CONF_QUEUE_COLUMNS);
    }
    if (Conf.getString(CONF_PLAYLIST_EDITOR_COLUMNS).matches(".*0.*")) {
      Conf.setDefaultProperty(CONF_PLAYLIST_EDITOR_COLUMNS);
    }

    // For Jajuk < 1.7, elaspsed time format variable name changed
    if (Conf.containsProperty("format")) {
      Conf.setProperty(CONF_FORMAT_TIME_ELAPSED, Conf
          .getString("format"));
    }

    // TO DO AFTER AN UPGRADE
    if (Main.isUpgradeDetected()) {
      // - for Jajuk < 1.3: force nocover icon replacement
      File fThumbs = UtilSystem.getConfFileByPath(FILE_THUMBS + "/50x50/" + FILE_THUMB_NO_COVER);
      if (fThumbs.exists()) {
        fThumbs.delete();
      }
      fThumbs = UtilSystem.getConfFileByPath(FILE_THUMBS + "/100x100/" + FILE_THUMB_NO_COVER);
      if (fThumbs.exists()) {
        fThumbs.delete();
      }
      fThumbs = UtilSystem.getConfFileByPath(FILE_THUMBS + "/150x150/" + FILE_THUMB_NO_COVER);
      if (fThumbs.exists()) {
        fThumbs.delete();
      }
      fThumbs = UtilSystem.getConfFileByPath(FILE_THUMBS + "/200x200/" + FILE_THUMB_NO_COVER);
      if (fThumbs.exists()) {
        fThumbs.delete();
      }
    }

  }

  /**
   * Actions to migrate an existing installation Step 2 at the end of UI startup
   */
  public static void upgradeStep2() {

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
      String pad = DownloadManager.downloadHtml(new URL(CHECK_FOR_UPDATE_URL));
      int beginIndex = pad.indexOf("<Program_Version>");
      int endIndex = pad.indexOf("</Program_Version>");
      sRelease = pad.substring(beginIndex + 17, endIndex);
      if (!JAJUK_VERSION.equals(sRelease)
      // Don't use this in test
          && !("VERSION_REPLACED_BY_ANT".equals(JAJUK_VERSION))) {
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
