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

import static org.jajuk.util.Resources.Unknown.STYLE;
import static org.jajuk.util.Resources.XML.NAME;

import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.jajuk.base.LogicalItem;
import org.jajuk.base.ItemType;
import org.jajuk.base.managers.TrackManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

/**
 * A music style ( jazz, rock...)
 * <p>
 * Logical item
 */
public class Style extends LogicalItem implements Comparable<Style> {

  private static final long serialVersionUID = 1L;

  /**
   * Style constructor
   *
   * @param id
   * @param sName
   */
  public Style(final String sId, final String sName) {
    super(sId, sName);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public String getLabel() {
    return STYLE;
  }

  /**
   * Return style name, dealing with unkwnown for any language
   *
   * @return author name
   */
  public String getName2() {
    String sOut = getName();
    if (sOut.equals(STYLE)) {
      sOut = Messages.getString(STYLE);
    }
    return sOut;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Style[ID=" + getID() + " Name={{" + getName() + "}}]";
  }

  /**
   * Alphabetical comparator used to display ordered lists
   *
   * @param other
   *          item to be compared
   * @return comparison result
   */
  public int compareTo(final Style otherItem) {
    // compare using name and id to differentiate unknown items
    final StringBuilder current = new StringBuilder(getName2());
    current.append(getID());
    final StringBuilder other = new StringBuilder(otherItem.getName2());
    other.append(otherItem.getID());
    return current.toString().compareToIgnoreCase(other.toString());
  }

  /**
   * @return Number of tracks for this style from the collection
   */
  public int getCount() {
    return ((TrackManager) ItemType.Track.getManager()).getAssociatedTracks(this).size();
  }

  /**
   * @return whether the style is Unknown or not
   */
  public boolean isUnknown() {
    return getName().equals(STYLE);
  }

  /**
   * Get item description
   */
  @Override
  public String getDescription() {
    return Messages.getString("Item_Style") + " : " + getName2();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  final public String getHumanValue(final String sKey) {
    if (NAME.equals(sKey)) {
      return getName2();
    } else {// default
      return super.getHumanValue(sKey);
    }
  }

  /**
   *
   * @return all tracks associated with this style
   */
  public ArrayList<Track> getTracksRecursively() {
    final ArrayList<Track> alTracks = new ArrayList<Track>(1000);
    for (final Item item : ((TrackManager) ItemType.Track.getManager()).getCarbonItems()) {
      final Track track = (Track) item;
      if (track.getStyle().equals(this)) {
        alTracks.add(track);
      }
    }
    return alTracks;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIcon()
   */
  @Override
  public ImageIcon getIcon() {
    return IconLoader.ICON_STYLE;
  }

}
