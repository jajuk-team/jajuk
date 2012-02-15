/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
 *  
 */

package org.jajuk.services.dj;

import java.util.Set;

import org.jajuk.base.Genre;

/**
 * Represent a genre proportion (used by digital DJs).
 */
public class Proportion {

  /** genres. */
  private Ambience ambience;

  /** Proportion*. */
  private float proportion;

  /**
   * Constructor.
   *
   * @param ambience DOCUMENT_ME
   * @param proportion genre proportion in %. Ex: 0.1
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
        && getGenres().equals(((Proportion) other).getGenres());
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
   * Gets the genres.
   * 
   * @return Returns the genres
   */
  public Set<Genre> getGenres() {
    return this.ambience.getGenres();
  }

  /**
   * Add a genre.
   * 
   * @param genre DOCUMENT_ME
   */
  public void addGenre(Genre genre) {
    ambience.addGenre(genre);
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
   * From String, return genre1,genre2,...
   * 
   * @return the genres desc
   */
  public String getGenresDesc() {
    String out = "";
    for (Genre s : ambience.getGenres()) {
      out += s.getName2() + ',';
    }
    if (out.length() > 0) {
      out = out.substring(0, out.length() - 1); // remove trailing ','
    }
    return out;
  }

  /**
   * Gets the next genre.
   * 
   * @return next genre to be played or null if no idea
   */
  public Genre getNextGenre() {
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
   * Sets the genre.
   * 
   * @param ambience the new genre
   */
  public void setGenre(Ambience ambience) {
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
