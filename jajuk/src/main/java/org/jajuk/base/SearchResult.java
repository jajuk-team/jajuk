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

import org.jajuk.services.webradio.WebRadio;

/**
 * A search result, contains a file and a search description.
 */
public class SearchResult implements Comparable<SearchResult> {

  /**
   * Result type *.
   */
  public enum SearchResultType {

    /** DOCUMENT_ME. */
    FILE,
    /** DOCUMENT_ME. */
    WEBRADIO
  }

  /** The associated file. */
  File file;

  /** The associated web radio. */
  WebRadio radio;

  /** Pre-calculated search string. */
  String sResu;

  /**
   * Instantiates a new search result.
   * 
   * @param file DOCUMENT_ME
   */
  public SearchResult(File file) {
    this(file, file.toStringSearch());
  }

  /**
   * Instantiates a new search result.
   * 
   * @param file DOCUMENT_ME
   * @param sResu DOCUMENT_ME
   */
  public SearchResult(File file, String sResu) {
    this.file = file;
    this.sResu = sResu;
  }

  /**
   * Instantiates a new search result.
   * 
   * @param radio DOCUMENT_ME
   * @param sResu DOCUMENT_ME
   */
  public SearchResult(WebRadio radio, String sResu) {
    this.radio = radio;
    this.sResu = sResu;
  }

  /**
   * Return hashcode, used during sorting.
   * 
   * @return the int
   */
  @Override
  public int hashCode() {
    return sResu.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(SearchResult sr) {
    if (sr == null) {
      return -1;
    }

    return sResu.compareToIgnoreCase(sr.getResu());
  }

  /**
   * Gets the file.
   * 
   * @return Returns the file.
   */
  public File getFile() {
    return file;
  }

  /**
   * Gets the type.
   * 
   * @return result type: file or web radio
   */
  public SearchResultType getType() {
    if (file != null) {
      return SearchResultType.FILE;
    } else {
      return SearchResultType.WEBRADIO;
    }
  }

  /**
   * Gets the webradio.
   * 
   * @return Returns the webradio.
   */
  public WebRadio getWebradio() {
    return radio;
  }

  /**
   * Gets the resu.
   * 
   * @return Returns the sResu.
   */
  public String getResu() {
    return sResu;
  }

}
