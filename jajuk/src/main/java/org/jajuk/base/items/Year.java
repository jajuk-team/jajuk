/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 *  $Revision: 2118 $
 */
package org.jajuk.base.items;

import javax.swing.ImageIcon;

import org.jajuk.base.LogicalItem;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Resources.Unknown;
import org.jajuk.util.Resources.XML;

/**
 *
 * Year object
 */
public class Year extends LogicalItem implements Comparable {

  private static final long serialVersionUID = 1L;

  private final long value;

  /**
   * Year constructor
   *
   * @param id
   * @param sName
   */
  public Year(final String sId, final String sValue) {
    super(sId, sValue);
    value = Long.parseLong(sValue);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return XML.YEAR;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Year[ID=" + getID() + " Value=" + getName() + "]";
  }

  /**
   *
   * @return year as a long
   */
  public long getValue() {
    return value;
  }

  /**
   * Alphabetical comparator used to display ordered lists
   *
   * @param other
   *          item to be compared
   * @return comparaison result
   */
  public int compareTo(final Object o) {
    final Year other = (Year) o;
    return (int) (getValue() - other.getValue());
  }

  /**
   * Get item description
   */
  @Override
  public String getDescription() {
    return Messages.getString("Property_year") + " : " + getName();
  }

  /**
   *
   * @return a human-readable year format
   */
  public String getName2() {
    final String s = getName();
    if ("0".equals(s)) {
      return Messages.getString(Unknown.STYLE);
    }
    return s;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIcon()
   */
  @Override
  public ImageIcon getIcon() {
    return IconLoader.ICON_YEAR;
  }
}
