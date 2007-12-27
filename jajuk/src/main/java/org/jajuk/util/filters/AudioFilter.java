/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.util.filters;

import java.io.File;

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;

/**
 * 
 * Audio filter
 */
public class AudioFilter extends JajukFileFilter {

  /** Self instance */
  protected static AudioFilter self = null;

  /**
   * 
   * @return singleton
   */
  public static AudioFilter getInstance() {
    if (AudioFilter.self == null) {
      AudioFilter.self = new AudioFilter();
    }
    return AudioFilter.self;
  }

  /**
   * Singleton constructor (protected for testing purposes)
   */
  private AudioFilter() {

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
    if (bShowDirectories && f.isDirectory()) {
      return true;
    } else {
      if (f.isDirectory()) {
        return false;
      }
      final TypeManager mgr = TypeManager.getInstance();
      final String extension = Util.getExtension(f);

      // check extension is known
      if (TypeManager.getInstance().isExtensionSupported(extension)) {
        // check it is an audio file
        return (Boolean) mgr.getTypeByExtension(extension).getValue(XML_TYPE_IS_MUSIC);
      }
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  @Override
  public String getDescription() {
    final StringBuilder s = new StringBuilder();

    for (final Type type : TypeManager.getInstance().getAllMusicTypes()) {
      s.append(type.getExtension());
      s.append(',');
    }
    // Remove last coma
    return ((s.length() > 0) ? s.substring(0, s.length() - 1).toString() : "");
  }
}
