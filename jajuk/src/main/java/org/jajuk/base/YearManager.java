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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Convenient class to manage years
 */
public class YearManager extends ItemManager {
  /** Self instance */
  private static YearManager singleton;

  /**
   * No constructor available, only static access
   */
  private YearManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(XML_EXPANDED, false, false, false, false, true,
        Boolean.class, false));
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
  public Year registerYear(String sId, String pYear) {
    synchronized (YearManager.getInstance().getLock()) {
      Year year = (Year) hmItems.get(sId);
      if (year != null) {
        return year;
      }
      year = new Year(sId, pYear);
      hmItems.put(sId, year);
      return year;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  public String getLabel() {
    return XML_YEARS;
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Year getYearByID(String sID) {
    synchronized (getLock()) {
      return (Year) hmItems.get(sID);
    }
  }

  /**
   * 
   * @return years list
   */
  public Set<Year> getYears() {
    Set<Year> yearSet = new LinkedHashSet<Year>();
    synchronized (getLock()) {
      for (Item item : getItems()) {
        yearSet.add((Year) item);
      }
    }
    return yearSet;
  }

  /**
   * Get years associated with this item
   * 
   * @param item
   * @return
   */
  public Set<Year> getAssociatedYears(Item item) {
    synchronized (YearManager.getInstance().getLock()) {
      Set<Year> out = new TreeSet<Year>();
      for (Object item2 : hmItems.values()) {
        Year year = (Year) item2;
        if (item instanceof Track && ((Track) item).getYear().equals(year)) {
          out.add(year);
        } else {
          Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item);
          for (Track track : tracks) {
            out.add(track.getYear());
          }
        }
      }
      return out;
    }
  }

}
