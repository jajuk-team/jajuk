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
 *  $Revision:3266 $
 */

package org.jajuk.services.dj;

import org.jajuk.base.Style;
import org.jajuk.util.UtilFeatures;

/**
 * Represents a transition from one style to another (used by digital DJs)
 */
public class Transition {
  /** From styles */
  private Ambience from;

  /** To styles */
  private Ambience to;

  /** Nb of tracks */
  private int nb;

  /**
   * Constructor
   * 
   * @param from
   *          source styles
   * @param to
   *          destination style
   * @param nb
   *          number of tracks played before changing style
   */
  public Transition(Ambience from, Ambience to, int nb) {
    this.from = from;
    this.to = to;
    this.nb = nb;
  }

  /**
   * Constructor for void transition
   * 
   * @param nb
   *          initial number of tracks
   */
  public Transition(int nb) {
    this.from = new Ambience(Long.toString(System.currentTimeMillis()), "");
    this.to = new Ambience(Long.toString(System.currentTimeMillis() - 100), "");
    this.nb = nb;
  }

  /**
   * equals method
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

  /**
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
   */
  public String getFromString() {
    String out = "";
    for (Style s : from.getStyles()) {
      out += s.getName2() + ',';
    }
    if (out.length() > 0) {
      out = out.substring(0, out.length() - 1); // remove trailling ,
    }
    return out;
  }

  /**
   * "To" String, return style1,style2,...
   */
  public String getToString() {
    String out = "";
    for (Style s : to.getStyles()) {
      out += s.getName2() + ',';
    }
    if (out.length() > 0) {
      out = out.substring(0, out.length() - 1); // remove trailling ,
    }
    return out;
  }

  /**
   * @return Returns the to.
   */
  /**
   * @return
   */
  public Ambience getTo() {
    return this.to;
  }

  public void addFromStyle(Style style) {
    from.addStyle(style);
  }

  /**
   * @param style
   */
  public void removeFromStyle(Style style) {
    from.removeStyle(style);
  }

  public void addToStyle(Style style) {
    to.addStyle(style);
  }

  /**
   * @param style
   */
  public void removeToStyle(Style style) {
    to.removeStyle(style);
  }

  /**
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

  public int getNbTracks() {
    return this.nb;
  }

  public void setFrom(Ambience from) {
    this.from = from;
  }

  public void setTo(Ambience to) {
    this.to = to;
  }

  public void setNb(int nb) {
    this.nb = nb;
  }
}
