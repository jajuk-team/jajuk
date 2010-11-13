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
 * An artist *
 * <p>
 * Logical item.
 */
public class Artist extends LogicalItem implements Comparable<Artist> {

  /**
   * Artist constructor.
   * 
   * @param sName DOCUMENT_ME
   * @param sId DOCUMENT_ME
   */
  Artist(String sId, String sName) {
    super(sId, sName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML_ARTIST;
  }

  /**
   * Return artist name, dealing with unknown for any language.
   * 
   * @return artist name
   */
  public String getName2() {
    if (isUnknown()) {
      return Messages.getString(UNKNOWN_ARTIST);
    }

    return getName();
  }

  /**
   * Alphabetical comparator used to display ordered lists.
   * 
   * @param otherItem DOCUMENT_ME
   * 
   * @return comparison result
   */
  @Override
  public int compareTo(Artist otherItem) {
    // not equal if other is null
    if (otherItem == null) {
      return 1;
    }

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
   * @return whether the artist is Unknown or not
   */
  public boolean isUnknown() {
    return this.getName().equals(UNKNOWN_ARTIST);
  }

  /**
   * Get item description.
   * 
   * @return the desc
   */
  @Override
  public String getDesc() {
    return Messages.getString("Item_Artist") + " : " + getName2();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(String sKey) {
    if (Const.XML_NAME.equals(sKey)) {
      return getName2();
    }
    // default
    return super.getHumanValue(sKey);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    return IconLoader.getIcon(JajukIcons.ARTIST);
  }
}
