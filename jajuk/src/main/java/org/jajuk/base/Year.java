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
 *  $Revision$
 */
package org.jajuk.base;

import javax.swing.ImageIcon;

import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;

/**
 * Year object.
 * <br>Logical item
 */
public class Year extends LogicalItem implements Comparable<Year> {

  /** The year that is stored in this object. */
  private final long value;

  /**
   * Year constructor.
   * 
   * @param sId The generated id (usually a md5hash)
   * @param sValue The Year-Value as string. Only a simple parsing is done, things
   * like AC, BC, ... are not supported.
   */
  Year(String sId, String sValue) {
    super(sId, sValue);
    if (sValue != null)
      this.value = UtilString.fastLongParser(sValue);
    else
      this.value = 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public final String getXMLTag() {
    return XML_YEAR;
  }

  /**
   * Gets the value.
   * 
   * @return year as a long
   */
  public long getValue() {
    return value;
  }

  /**
   * Alphabetical comparator used to display ordered lists.
   * 
   * @param other item to be compared
   * 
   * @return comparison result
   */
  @Override
  public int compareTo(Year other) {
    if (other == null) {
      return -1;
    }

    return (int) (getValue() - other.getValue());
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.Item#getTitle()
   */
  @Override
  public String getTitle() {
    return Messages.getHumanPropertyName(Const.XML_YEAR) + " : " + getName();
  }

  /**
   * Gets the name2.
   * 
   * @return a human-readable year format
   */
  public String getName2() {
    String s = getName();
    if ("0".equals(s)) {
      return Messages.getString(UNKNOWN_YEAR);
    }
    return s;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    return IconLoader.getIcon(JajukIcons.YEAR);
  }

  /**
   * Return whether this year looks valid or not.
   * 
   * @return true, if looks valid
   */
  boolean looksValid() {
    return value > 1000 && value < 3000;
  }
}
