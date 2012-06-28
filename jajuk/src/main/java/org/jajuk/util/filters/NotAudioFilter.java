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

import org.jajuk.base.TypeManager;
import org.jajuk.util.Const;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.UtilSystem;

/**
 * Not Audio file filter (must be a file).
 */
public final class NotAudioFilter extends JajukFileFilter {
  /** Self instance. */
  private static NotAudioFilter self = new NotAudioFilter();

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static NotAudioFilter getInstance() {
    return NotAudioFilter.self;
  }

  /**
   * Singleton constructor (protected for testing purposes).
   */
  private NotAudioFilter() {
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
        // check it is an audio file
        return !(Boolean) mgr.getTypeByExtension(extension).getValue(Const.XML_TYPE_IS_MUSIC);
      }
    }
    // unknown type : not an audio file
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  @Override
  public String getDescription() {
    // No need to translate, is is used internal only
    return "Not audio";
  }
}
