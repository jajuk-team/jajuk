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
package org.jajuk.base;

import javax.swing.ImageIcon;

import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * A music genre ( jazz, rock...)
 * <p>
 * Logical item
 */
public class Genre extends LogicalItem implements Comparable<Genre> {

  /**
   * Genre constructor.
   * 
   * @param sName DOCUMENT_ME
   * @param sId DOCUMENT_ME
   */
  Genre(String sId, String sName) {
    super(sId, sName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML_GENRE;
  }

  /**
   * Return genre name, dealing with unknown for any language.
   * 
   * @return artist name
   */
  public String getName2() {
    String sOut = getName();
    if (sOut.equals(UNKNOWN_GENRE)) {
      sOut = Messages.getString(UNKNOWN_GENRE);
    }
    return sOut;
  }

  /**
   * Alphabetical comparator used to display ordered lists.
   * 
   * @param otherItem DOCUMENT_ME
   * 
   * @return comparison result
   */
  @Override
  public int compareTo(Genre otherItem) {
    // compare using name and id to differentiate unknown items
    StringBuilder current = new StringBuilder(getName2());
    current.append(getID());
    StringBuilder other = new StringBuilder(otherItem.getName2());
    other.append(otherItem.getID());
    return current.toString().compareToIgnoreCase(other.toString());
  }

  /**
   * Checks if is unknown.
   * 
   * @return whether the genre is Unknown or not
   */
  public boolean isUnknown() {
    return this.getName().equals(UNKNOWN_GENRE);
  }

  /**
   * Get item description.
   * 
   * @return the desc
   */
  @Override
  public String getDesc() {
    return Messages.getString("Item_Genre") + " : " + getName2();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public final String getHumanValue(String sKey) {
    if (Const.XML_NAME.equals(sKey)) {
      return getName2();
    } else {// default
      return super.getHumanValue(sKey);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    return IconLoader.getIcon(JajukIcons.GENRE);
  }

}
