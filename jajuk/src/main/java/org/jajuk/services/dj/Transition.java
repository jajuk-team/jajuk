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

import org.jajuk.base.Style;
import org.jajuk.util.UtilFeatures;

/**
 * Represents a transition from one style to another (used by digital DJs).
 */
public class Transition {
  
  /** From styles. */
  private Ambience from;

  /** To styles. */
  private Ambience to;

  /** Nb of tracks. */
  private int nb;

  /**
   * Constructor.
   * 
   * @param from source styles
   * @param to destination style
   * @param nb number of tracks played before changing style
   */
  public Transition(Ambience from, Ambience to, int nb) {
    this.from = from;
    this.to = to;
    this.nb = nb;
  }

  /**
   * Constructor for void transition.
   * 
   * @param nb initial number of tracks
   */
  public Transition(int nb) {
    this.from = new Ambience(Long.toString(System.currentTimeMillis()), "");
    this.to = new Ambience(Long.toString(System.currentTimeMillis() - 100), "");
    this.nb = nb;
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
    if (!(other instanceof Transition)) {
      return false;
    }
    return getFrom().equals(((Transition) other).getFrom())
        && getTo().equals(((Transition) other).getTo());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    // for now just combine name and id to a hashcode
    // idea taken from
    // http://www.geocities.com/technofundo/tech/java/equalhash.html
    int hash = 7;
    hash = 31 * hash + getFrom().hashCode();
    hash = 31 * hash + getTo().hashCode();
    return hash;
  }

  /**
   * Gets the from.
   * 
   * @return Returns the from.
   */
  /**
   * @return
   */
  public Ambience getFrom() {
    return this.from;
  }

  /**
   * From String, return style1,style2,...
   * 
   * @return the from string
   */
  public String getFromString() {
    String out = "";
    for (Style s : from.getStyles()) {
      out += s.getName2() + ',';
    }
    if (out.length() > 0) {
      out = out.substring(0, out.length() - 1); // remove trailing ,
    }
    return out;
  }

  /**
   * "To" String, return style1,style2,...
   * 
   * @return the to string
   */
  public String getToString() {
    String out = "";
    for (Style s : to.getStyles()) {
      out += s.getName2() + ',';
    }
    if (out.length() > 0) {
      out = out.substring(0, out.length() - 1); // remove trailing ,
    }
    return out;
  }

  /**
   * Gets the to.
   * 
   * @return Returns the to.
   */
  /**
   * @return
   */
  public Ambience getTo() {
    return this.to;
  }

  /**
   * Adds the from style.
   * DOCUMENT_ME
   * 
   * @param style DOCUMENT_ME
   */
  public void addFromStyle(Style style) {
    from.addStyle(style);
  }

  /**
   * Removes the from style.
   * 
   * @param style DOCUMENT_ME
   */
  public void removeFromStyle(Style style) {
    from.removeStyle(style);
  }

  /**
   * Adds the to style.
   * DOCUMENT_ME
   * 
   * @param style DOCUMENT_ME
   */
  public void addToStyle(Style style) {
    to.addStyle(style);
  }

  /**
   * Removes the to style.
   * 
   * @param style DOCUMENT_ME
   */
  public void removeToStyle(Style style) {
    to.removeStyle(style);
  }

  /**
   * Gets the next style.
   * 
   * @return next style to be played or null if no idea
   */
  public Style getNextStyle() {
    if (to.getStyles().size() == 0) {
      return null;
    } else if (to.getStyles().size() == 1) {
      return to.getStyles().iterator().next();
    } else {
      // several destination styles, return a shuffle one
      return (Style) UtilFeatures.getShuffleItem(to.getStyles());
    }
  }

  /**
   * Gets the nb tracks.
   * 
   * @return the nb tracks
   */
  public int getNbTracks() {
    return this.nb;
  }

  /**
   * Sets the from.
   * 
   * @param from the new from
   */
  public void setFrom(Ambience from) {
    this.from = from;
  }

  /**
   * Sets the to.
   * 
   * @param to the new to
   */
  public void setTo(Ambience to) {
    this.to = to;
  }

  /**
   * Sets the nb.
   * 
   * @param nb the new nb
   */
  public void setNb(int nb) {
    this.nb = nb;
  }
}
