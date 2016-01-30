/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
package org.jajuk.base;

/**
 * A logical Item (genre, artist, year, album...)
 */
public abstract class LogicalItem extends Item {
  /**
   * The Constructor.
   * 
   * @param sId 
   * @param sName 
   */
  LogicalItem(String sId, String sName) {
    super(sId, sName);
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    // Use class metadata because this method is used by several logical items
    return getClass().getCanonicalName() + "[ID=" + getID() + " Name={{" + getName() + "}}]";
  }
}
