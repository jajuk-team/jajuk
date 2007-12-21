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
 *  $Revision$
 */
package org.jajuk.base.items;

import javax.swing.ImageIcon;

import org.jajuk.base.LogicalItem;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Resources.Unknown;
import org.jajuk.util.Resources.XML;

/**
 * An author *
 * <p>
 * Logical item
 */
public class Author extends LogicalItem implements Comparable<Author> {

  private static final long serialVersionUID = 1L;

  /**
   * Author constructor
   *
   * @param id
   * @param sName
   */
  public Author(final String sId, final String sName) {
    super(sId, sName);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return XML.AUTHOR;
  }

  /**
   * Return author name, dealing with unkwnown for any language
   *
   * @return author name
   */
  public String getName2() {
    String sOut = getName();
    if (sOut.equals(Unknown.AUTHOR)) {
      sOut = Messages.getString(Unknown.AUTHOR);
    }
    return sOut;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Author[ID=" + getID() + " Name={{" + getName() + "}}]";
  }

  /**
   * Alphabetical comparator used to display ordered lists
   *
   * @param other
   *          item to be compared
   * @return comparison result
   */
  public int compareTo(final Author otherItem) {
    // compare using name and id to differentiate unknown items
    final StringBuilder current = new StringBuilder(getName2());
    current.append(getID());
    final StringBuilder other = new StringBuilder(otherItem.getName2());
    other.append(otherItem.getID());
    return current.toString().compareToIgnoreCase(other.toString());
  }

  /**
   * @return whether the author is Unknown or not
   */
  public boolean isUnknown() {
    return getName().equals(Unknown.AUTHOR);
  }

  /**
   * Get item description
   */
  @Override
  public String getDescription() {
    return Messages.getString("Item_Author") + " : " + getName2();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(final String sKey) {
    if (XML.NAME.equals(sKey)) {
      return getName2();
    }
    // default
    return super.getHumanValue(sKey);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIcon()
   */
  @Override
  public ImageIcon getIcon() {
    return IconLoader.ICON_AUTHOR;
  }
}
