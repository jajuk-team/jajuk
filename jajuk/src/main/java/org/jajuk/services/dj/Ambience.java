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

import java.util.HashSet;
import java.util.Set;

import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;

/**
 * An ambience is a set of genres <br>
 * Note that an ambience is identified by an id and not its name Several
 * ambiences can eventually share the same name. This is because ambience is
 * translated and can change with current locale
 */
public class Ambience implements Comparable<Ambience> {

  /** List of genres. */
  private Set<Genre> genres;

  /** Ambience name. */
  private String sName;

  /** Ambience ID. */
  private final String sID;

  /**
   * Constructor.
   * 
   * @param sID Ambience unique id
   * @param sName Ambience name
   * @param genres list of genres
   */
  public Ambience(String sID, String sName, Set<Genre> genres) {
    this.sID = sID;
    this.sName = sName;
    this.genres = genres;
  }

  /**
   * Constructor.
   * 
   * @param sID Ambience unique id
   * @param sName Ambience name
   * @param genres list by name
   */
  public Ambience(String sID, String sName, String[] genres) {
    Set<Genre> hgenres = new HashSet<Genre>(genres.length);
    for (String element : genres) {
      Genre genre = GenreManager.getInstance().getGenreByName(element);
      if (genre != null) {
        hgenres.add(genre);
      }
    }
    this.sID = sID;
    this.sName = sName;
    this.genres = hgenres;
  }

  /**
   * Constructor.
   * 
   * @param sName Ambience name
   * @param sID DOCUMENT_ME
   */
  public Ambience(String sID, String sName) {
    this(sID, sName, new HashSet<Genre>(10));
  }

  /**
   * Constructor.
   */
  public Ambience() {
    this.sID = "" + System.currentTimeMillis();
    this.sName = "";
    this.genres = new HashSet<Genre>(10);
  }

  /**
   * Adds the genre.
   * DOCUMENT_ME
   * 
   * @param genre DOCUMENT_ME
   */
  public void addGenre(Genre genre) {
    if (genre != null) {
      genres.add(genre);
    }
  }

  /**
   * Removes the genre.
   * DOCUMENT_ME
   * 
   * @param genre DOCUMENT_ME
   */
  public void removeGenre(Genre genre) {
    genres.remove(genre);
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return this.sName;
  }

  /**
   * Gets the iD.
   * 
   * @return the iD
   */
  public String getID() {
    return this.sID;
  }

  /**
   * Sets the name.
   * 
   * @param name the new name
   */
  public void setName(String name) {
    this.sName = name;
  }

  /**
   * Gets the genres.
   * 
   * @return the genres
   */
  public Set<Genre> getGenres() {
    return this.genres;
  }

  /**
   * Sets the genres.
   * 
   * @param genres the new genres
   */
  public void setGenres(Set<Genre> genres) {
    this.genres = genres;
  }

  /**
   * From String, return genre1,genre2,...
   * 
   * @return the genres desc
   */
  public String getGenresDesc() {
    // check if we have genres at all
    if (getGenres().size() == 0) {
      return "";
    }

    StringBuilder out = new StringBuilder();
    for (Genre s : getGenres()) {
      out.append(s.getName2()).append(',');
    }

    return out.substring(0, out.length() - 1); // remove trailling ,
  }

  /**
   * toString method.
   * 
   * @return String representation of this item
   */
  @Override
  public String toString() {
    return sName + " " + genres;
  }

  /**
   * Equals method.
   * 
   * @param o DOCUMENT_ME
   * 
   * @return true if ambience have the same same and contains the same genres
   */
  @Override
  public boolean equals(Object o) {
    // also catches null by definition
    if (!(o instanceof Ambience)) {
      return false;
    }
    Ambience ambienceOther = (Ambience) o;
    return this.sName.equals(ambienceOther.getName())
        && this.genres.equals(ambienceOther.getGenres());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    // for now just combine name and id to a hashcode
    // idea token from
    // http://www.geocities.com/technofundo/tech/java/equalhash.html
    int hash = 7;
    hash = 31 * hash + sName.hashCode();
    hash = 31 * hash + sID.hashCode();
    return hash;
  }

  /**
   * Compare to method : alphabetical.
   * 
   * @param ambience DOCUMENT_ME
   * 
   * @return the int
   */
  @Override
  public int compareTo(Ambience ambience) {
    // check for null
    if (ambience == null) {
      return -1;
    }

    // otherwise just compare on the name
    return this.getName().compareToIgnoreCase(ambience.getName());
  }

  /**
   * return "genre1,genre2,..,genre_n"
   * 
   * @return String used in DJ XML representation
   */
  public String toXML() {
    // check if we have genres at all 
    if (getGenres().size() == 0) {
      return "";
    }

    StringBuilder s = new StringBuilder();
    for (Genre genre : getGenres()) {
      s.append(genre.getID()).append(',');
    }

    return s.substring(0, s.length() - 1); // remove last coma
  }
}
