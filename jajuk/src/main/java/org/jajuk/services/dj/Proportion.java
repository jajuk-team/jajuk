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

package org.jajuk.services.dj;

import java.util.Set;

import org.jajuk.base.Style;

/**
 * Represent a style proportion (used by digital DJs).
 */
public class Proportion {
  
  /** styles. */
  private Ambience ambience;

  /** Proportion*. */
  private float proportion;

  /**
   * Constructor.
   * 
   * @param proportion style proportion in %. Ex: 0.1
   * @param ambience DOCUMENT_ME
   */
  public Proportion(Ambience ambience, float proportion) {
    this.ambience = ambience;
    this.proportion = proportion;
  }

  /**
   * Constructor for void proportion.
   */
  public Proportion() {
    this.ambience = new Ambience(Long.toString(System.currentTimeMillis()), "");
    this.proportion = 0.2f;
  }

  /**
   * equals method.
   * 
   * @param other DOCUMENT_ME
   * 
   * @return whether two object are equals
   */
  @Override
  public boolean equals(Object other) {
    // also catches null
    if (!(other instanceof Proportion)) {
      return false;
    }
    return getProportion() == ((Proportion) other).getProportion()
        && getStyles().equals(((Proportion) other).getStyles());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    // for now just use ambience for the hashCode, not sure if we should include
    // the proportion value as well...
    return ambience.hashCode();
  }

  /**
   * Gets the styles.
   * 
   * @return Returns the styles
   */
  public Set<Style> getStyles() {
    return this.ambience.getStyles();
  }

  /**
   * Add a style.
   * 
   * @param style DOCUMENT_ME
   */
  public void addStyle(Style style) {
    ambience.addStyle(style);
  }

  /**
   * To string.
   * 
   * @return String representation of this proportion
   */
  @Override
  public String toString() {
    return "" + proportion;
  }

  /**
   * From String, return style1,style2,...
   * 
   * @return the styles desc
   */
  public String getStylesDesc() {
    String out = "";
    for (Style s : ambience.getStyles()) {
      out += s.getName2() + ',';
    }
    if (out.length() > 0) {
      out = out.substring(0, out.length() - 1); // remove trailing ','
    }
    return out;
  }

  /**
   * Gets the next style.
   * 
   * @return next style to be played or null if no idea
   */
  public Style getNextStyle() {
    return null;
  }

  /**
   * Gets the proportion.
   * 
   * @return the proportion
   */
  public float getProportion() {
    return this.proportion;
  }

  /**
   * Sets the style.
   * 
   * @param ambience the new style
   */
  public void setStyle(Ambience ambience) {
    this.ambience = ambience;
  }

  /**
   * Sets the proportion.
   * 
   * @param proportion the new proportion
   */
  public void setProportion(float proportion) {
    this.proportion = proportion;
  }
}
