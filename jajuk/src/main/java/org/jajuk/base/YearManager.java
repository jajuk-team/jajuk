/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

import java.util.Iterator;
import java.util.List;

import org.jajuk.util.Const;
import org.jajuk.util.ReadOnlyIterator;

/**
 * Convenient class to manage years.
 */
public final class YearManager extends ItemManager {

  /** Self instance. */
  private static YearManager singleton = new YearManager();

  /**
   * No constructor available, only static access.
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
   * Gets the instance.
   * 
   * @return singleton
   */
  public static YearManager getInstance() {
    return singleton;
  }

  /**
   * Register a year.
   * 
   * @param pYear DOCUMENT_ME
   * 
   * @return the year
   */
  public Year registerYear(String pYear) {
    String sId = pYear;
    return registerYear(sId, pYear);
  }

  /**
   * Register a year with a known id.
   * 
   * @param sId DOCUMENT_ME
   * @param pYear DOCUMENT_ME
   * 
   * @return the year
   */
  public Year registerYear(String sId, String pYear) {
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
   * Gets the year by id.
   * 
   * @param sID Item ID
   * 
   * @return Element
   */
  public Year getYearByID(String sID) {
    return (Year) getItemByID(sID);
  }

  /**
   * Gets the years.
   * 
   * @return ordered years list
   */
  @SuppressWarnings("unchecked")
  public List<Year> getYears() {
    return (List<Year>) getItems();
  }

  /**
   * Gets the years iterator.
   * 
   * @return years iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<Year> getYearsIterator() {
    return new ReadOnlyIterator<Year>((Iterator<Year>) getItemsIterator());
  }

}
