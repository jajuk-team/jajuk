/*
 *  Jajuk
 *  Copyright (C) 2004 The Jajuk Team
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

package org.jajuk.dj;

import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.util.log.Log;

import java.util.HashSet;

/**
 * An ambience is a set of styles
 */
public class Ambience implements Comparable<Ambience> {

  /** List of styles */
  private HashSet<Style> styles;

  /** Ambience name */
  private String sName;

  /** Ambience ID */
  private String sID;

  /**
   * Constructor
   * 
   * @param sID
   *          Ambience uniq id
   * @param sName
   *          Ambience name
   * @param styles
   *          list of styles
   */
  public Ambience(String sID, String sName, HashSet<Style> styles) {
    this.sID = sID;
    this.sName = sName;
    this.styles = styles;
  }

  /**
   * Constructor
   * 
   * @param sID
   *          Ambience uniq id
   * @param sName
   *          Ambience name
   * @param styles
   *          list by name
   */
  public Ambience(String sID, String sName, String[] styles) {
    HashSet<Style> hstyles = new HashSet<Style>(styles.length);
    for (int i = 0; i < styles.length; i++) {
      Style style = StyleManager.getInstance().getStyleByName(styles[i]);
      if (style != null) {
        hstyles.add(style);
      } else {
        Log.debug("Unknown style");
      }
    }
    this.sID = sID;
    this.sName = sName;
    this.styles = hstyles;
  }

  /**
   * Constructor
   * 
   * @param sName
   *          Ambience name
   */
  public Ambience(String sID, String sName) {
    this(sID, sName, new HashSet<Style>(10));
  }

  /**
   * Constructor
   */
  public Ambience() {
    this.sName = "";
    this.styles = new HashSet<Style>(10);
  }

  public void addStyle(Style style) {
    if (style != null) {
      styles.add(style);
    }
  }

  public void removeStyle(Style style) {
    styles.remove(style);
  }

  public String getName() {
    return this.sName;
  }

  public String getID() {
    return this.sID;
  }

  public void setName(String name) {
    this.sName = name;
  }

  public HashSet<Style> getStyles() {
    return this.styles;
  }

  public void setStyles(HashSet<Style> styles) {
    this.styles = styles;
  }

  /**
   * From String, return style1,style2,...
   */
  public String getStylesDesc() {
    String out = "";
    for (Style s : styles) {
      out += s.getName2() + ',';
    }
    if (out.length() > 0) {
      out = out.substring(0, out.length() - 1); // remove trailling ,
    }
    return out;
  }

  /**
   * toString method
   * 
   * @return String representation of this item
   */
  public String toString() {
    return sName + " " + styles;
  }

  /**
   * Equals method
   * 
   * @return true if ambience have the same same and contains the same styles
   */
  public boolean equals(Object o) {
    Ambience ambienceOther = (Ambience) o;
    if (o == null) {
      return false;
    }
    return this.sName.equals(ambienceOther.getName())
        && this.styles.equals(ambienceOther.getStyles());
  }

  /**
   * Compare to method : alphabetical
   */
  public int compareTo(Ambience ambience) {
    return this.getName().compareToIgnoreCase(ambience.getName());
  }

  /**
   * return "style1,style2,..,style_n"
   * 
   * @return String used in DJ XML representation
   */
  public String toXML() {
    String s = "";
    for (Style style : getStyles()) {
      s += style.getID() + ",";
    }
    return s.substring(0, s.length() - 1); // remove last coma
  }

}
