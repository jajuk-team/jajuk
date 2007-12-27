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

import java.io.File;
import java.util.StringTokenizer;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Manage thumbnails
 */
public class ThumbnailManager implements ITechnicalStrings {

  /** No instances */
  private ThumbnailManager() {
  }

  /**
   * Delete all thumbs for a given size
   * 
   * @param size
   *          size, eg: ITechnicalStrings.THUMBNAIL_SIZE_150x150
   */
  public static void cleanThumbs(String size) {
    File fThumb = Util.getConfFileByPath(FILE_THUMBS + '/' + size);
    if (fThumb.exists()) {
      File[] files = fThumb.listFiles();
      for (File file : files) {
        if (!file.getAbsolutePath().matches(".*" + FILE_THUMB_NO_COVER)) {
          file.delete();
        }
      }
      // Refresh default cover
      File fDefault = Util.getConfFileByPath(FILE_THUMBS + "/" + size + "/" + FILE_THUMB_NO_COVER);
      fDefault.delete();
      try {
        int iSize = Integer.parseInt(new StringTokenizer(size, "x").nextToken());
        Util.createThumbnail(IconLoader.ICON_NO_COVER, fDefault, iSize);
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }
}
