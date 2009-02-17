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

package org.jajuk.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jajuk.util.Const;
import org.jajuk.util.ReadOnlyIterator;

/**
 * Convenient class to manage years
 */
public final class YearManager extends ItemManager {
  /** Self instance */
  private static YearManager singleton;

  /**
   * No constructor available, only static access
   */
  private YearManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(Const.XML_EXPANDED, false, false, false, false,
        true, Boolean.class, false));
  }

  /**
   * @return singleton
   */
  public static YearManager getInstance() {
    if (singleton == null) {
      singleton = new YearManager();
    }
    return singleton;
  }

  /**
   * Register a year
   * 
   * @param sName
   */
  public Year registerYear(String pYear) {
    String sId = pYear;
    return registerYear(sId, pYear);
  }

  /**
   * Register a year with a known id
   * 
   * @param sName
   */
  public synchronized Year registerYear(String sId, String pYear) {
    Year year = getYearByID(sId);
    if (year != null) {
      return year;
    }
    year = new Year(sId, pYear);
    registerItem(year);
    return year;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return Const.XML_YEARS;
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Year getYearByID(String sID) {
    return (Year) getItemByID(sID);
  }

  /**
   * 
   * @return ordered years list
   */
  @SuppressWarnings("unchecked")
  public synchronized List<Year> getYears() {
    return (List<Year>) getItems();
  }

  /**
   * 
   * @return years iterator
   */
  @SuppressWarnings("unchecked")
  public synchronized ReadOnlyIterator<Year> getYearsIterator() {
    return new ReadOnlyIterator<Year>((Iterator<Year>) getItemsIterator());
  }

  /**
   * Get ordered years associated with this item
   * 
   * @param item
   * @return
   */
  public synchronized List<Year> getAssociatedYears(Item item) {
    List<Year> out = new ArrayList<Year>(1);
    // [Perf] If item is a track, just return its Year
    if (item instanceof Track) {
      out.add(((Track) item).getYear());
    } else {
      List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item,true);
      for (Track track : tracks) {
        out.add(track.getYear());
      }
    }
    return out;
  }

}
