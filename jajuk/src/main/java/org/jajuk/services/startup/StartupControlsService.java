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
package org.jajuk.services.startup;

import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Startup facilities for configuration controls.
 */
public class StartupControlsService {

  /** The Constant DIR_CHECKS.  DOCUMENT_ME */
  private static final String[] DIR_CHECKS = {
      // internal pictures cache directory
      Const.FILE_CACHE + '/' + Const.FILE_INTERNAL_CACHE,
      // thumbnails directories and sub-directories
      Const.FILE_THUMBS, Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_50X50,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_100X100,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_150X150,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_200X200,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_250X250,
      Const.FILE_THUMBS + "/" + Const.THUMBNAIL_SIZE_300X300,
      // DJs directories
      Const.FILE_DJ_DIR };

  /**
   * Instantiates a new startup controls service.
   */
  private StartupControlsService() {
    // private constructor to hide it from the outside
  }

  /**
   * Performs some basic startup tests.
   * 
   * @throws InterruptedException the interrupted exception
   * @throws Exception    * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void initialCheckups() throws IOException, InterruptedException {
    // Populate workspace path
    SessionService.discoverWorkspace();

    // check for jajuk directory
    final File fWorkspace = new File(SessionService.getWorkspace());
    if (!fWorkspace.exists() && !fWorkspace.mkdirs()) { // create the
      // directory
      // if it doesn't exist
      Log.warn("Could not create directory " + fWorkspace.toString());
    }
    // check for image cache presence and create the workspace/.jajuk
    // directory
    final File fCache = SessionService.getConfFileByPath(Const.FILE_CACHE);
    if (!fCache.exists()) {
      if (!fCache.mkdirs()) {
        Log.warn("Could not cretae directory structure " + fCache.toString());
      }
    } else {
      // Empty cache if age > CACHE_MAX_AGE
      final File[] cacheFiles = fCache.listFiles();
      for (final File element : cacheFiles) {
        long fileAge = System.currentTimeMillis() - element.lastModified();
        if (element.isFile() && fileAge > Const.CACHE_MAX_AGE ) {
          UtilSystem.deleteFile(element);
        }
      }
    }

    // checking preference file
    File file = SessionService.getConfFileByPath(Const.FILE_CONFIGURATION);
    if (!file.exists()) {
      // if config file doesn't exit, create
      // it with default values
      Log.warn("Create missing preference file");
      org.jajuk.util.Conf.commit();
    }

    // checking required history file
    file = SessionService.getConfFileByPath(Const.FILE_HISTORY);
    if (!file.exists()) {
      // if config file doesn't exit, create
      // it with default values
      Log.warn("Create missing history file");
      History.commit();
    }

    // checking required internal directories
    for (final String check : DIR_CHECKS) {
      final File dir = SessionService.getConfFileByPath(check);
      if (!dir.exists() && !dir.mkdir()) {
        Log.warn("Could not create missing required directory [" + check + "]");
      }
    }

    // Extract star icons (used by some HTML panels)
    for (int i = 0; i <= 4; i++) {
      final File star = SessionService.getConfFileByPath("cache/internal/star" + i + "_16x16.png");
      if (!star.exists()) {
        ImageIcon ii = null;
        switch (i) {
        case 0:
          ii = IconLoader.getIcon(JajukIcons.STAR_0);
          break;
        case 1:
          ii = IconLoader.getIcon(JajukIcons.STAR_1);
          break;
        case 2:
          ii = IconLoader.getIcon(JajukIcons.STAR_2);
          break;
        case 3:
          ii = IconLoader.getIcon(JajukIcons.STAR_3);
          break;
        case 4:
          ii = IconLoader.getIcon(JajukIcons.STAR_4);
          break;
        default:
          throw new IllegalArgumentException(
              "Unexpected code position reached, the switch values should match the for-loop!");
        }
        UtilGUI.extractImage(ii.getImage(), star);
      }
    }
  }

}
