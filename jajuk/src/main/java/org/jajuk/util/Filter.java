/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.util;


/**
 * Filter on meta information
 */
public class Filter {

  /** Key */
  String key;

  /** Value* */
  String sValue;

  /** Human* */
  boolean bHuman = false;

  /** Exact* */
  boolean bExact = false;

  /**
   * Filter constructor
   * 
   * @param key
   *          key (property name). null if the filter is on any property
   * @param sValue
   *          value
   * @param bHuman
   *          is the filter apply value itself or its human representation if
   *          different ?
   * @param bExact
   *          is the filter should match exactly the value ?
   */
  public Filter(String key, String sValue, boolean bHuman, boolean bExact) {
    this.key = key;
    this.sValue = sValue;
    this.bHuman = bHuman;
    this.bExact = bExact;
  }

  public boolean isExact() {
    return bExact;
  }

  public boolean isHuman() {
    return bHuman;
  }

  public String getProperty() {
    return key;
  }

  public String getValue() {
    return sValue;
  }
}
