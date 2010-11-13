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

import org.jajuk.util.JajukFileFilter;

/**
 * Gif filter.
 */
public class GIFFilter extends JajukFileFilter {

  /** Self instance. */
  private static GIFFilter self = new GIFFilter();

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static GIFFilter getInstance() {
    return GIFFilter.self;
  }

  /**
   * Singleton constructor.
   */
  private GIFFilter() {
    super(new String[] { "gif" });
  }
}
