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
 *  
 */
package org.jajuk.util.filters;

import java.io.File;

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.util.Const;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.UtilSystem;

/**
 * Playlist filter.
 */
public final class PlaylistFilter extends JajukFileFilter {

  /** Self instance. */
  private static PlaylistFilter self = new PlaylistFilter();

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static PlaylistFilter getInstance() {
    return PlaylistFilter.self;
  }

  /**
   * Singleton constructor (protected for testing purposes).
   */
  private PlaylistFilter() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(final File f) {
    // Force directories acceptation if user wants to navigate into
    // directories
    if (f.isDirectory()) {
      return bShowDirectories;
    } else {
      final String extension = UtilSystem.getExtension(f);
      final TypeManager mgr = TypeManager.getInstance();

      // check extension is known
      if (mgr.isExtensionSupported(extension)) {
        // check it is a playlist
        final Type playlist = mgr.getTypeByExtension(Const.EXT_PLAYLIST);
        return mgr.getTypeByExtension(extension).equals(playlist);
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  @Override
  public String getDescription() {
    return Const.EXT_PLAYLIST;
  }
}
