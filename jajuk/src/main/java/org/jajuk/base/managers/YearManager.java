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
 *  $Revision: 2315 $
 **/

package org.jajuk.base.managers;

import java.util.Set;
import java.util.TreeSet;

import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.items.Item;
import org.jajuk.base.items.Track;
import org.jajuk.base.items.Year;
import org.jajuk.util.Resources.XML;

/**
 * Convenient class to manage years
 */
public class YearManager extends ItemManager<Year> {
  /** Self instance */
  private static YearManager singleton;

  /**
   * No constructor available, only static access
   */
  public YearManager() {
    super();
    // register properties
    // ID
    registerProperty(new MetaProperty(XML.ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new MetaProperty(XML.NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new MetaProperty(XML.EXPANDED, false, false, false, false, true,
        Boolean.class, false));
  }

  /**
   * Register a year
   *
   * @param sName
   */
  public Year registerYear(final String pYear) {
    final String sId = pYear;
    return registerYear(sId, pYear);
  }

  /**
   * Register a year with a known id
   *
   * @param sName
   */
  public Year registerYear(final String sId, final String pYear) {
    synchronized (getLock()) {
      Year year = getItems().get(sId);
      if (year != null) {
        return year;
      }
      year = new Year(sId, pYear);
      getItems().put(sId, year);
      return year;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML.YEARS;
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Year getYearByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }


  /**
   * Get years associated with this item
   *
   * @param item
   * @return
   */
  public Set<Year> getAssociatedYears(final Item item) {
    synchronized (getLock()) {
      final Set<Year> out = new TreeSet<Year>();
      for (final Year item2 : getItems().values()) {
        final Year year = item2;
        if ((item instanceof Track) && ((Track) item).getYear().equals(year)) {
          out.add(year);
        } else {
          final Set<Track> tracks = ((TrackManager) ItemType.Track.getManager()).getAssociatedTracks(item);
          for (final Track track : tracks) {
            out.add(track.getYear());
          }
        }
      }
      return out;
    }
  }

}
