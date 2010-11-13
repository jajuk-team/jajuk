/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
package org.jajuk.util.filters;

import java.io.File;

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.UtilSystem;

/**
 * Known type filter.
 */
public final class KnownTypeFilter extends JajukFileFilter {

  /** Self instance. */
  private static KnownTypeFilter self = new KnownTypeFilter();

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static KnownTypeFilter getInstance() {
    return KnownTypeFilter.self;
  }

  /**
   * Singleton constructor (protected for testing purposes).
   */
  private KnownTypeFilter() {
    super(Type.getExtensionsFromTypes(TypeManager.getInstance().getTypes()));
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
    }
    return TypeManager.getInstance().isExtensionSupported(UtilSystem.getExtension(f));
  }

}
