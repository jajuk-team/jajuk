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
 * $Revision$
 */

package org.jajuk.base;

import org.jajuk.services.webradio.WebRadio;

/**
 * A search result, contains a file and a search description
 */
public class SearchResult implements Comparable<SearchResult> {

  /** Result type * */
  public enum SearchResultType {
    FILE, WEBRADIO
  }

  /** The associated file */
  File file;

  /** The associated web radio */
  WebRadio radio;

  /** Pre-calculated search string */
  String sResu;

  public SearchResult(File file) {
    this(file, file.toStringSearch());
  }

  public SearchResult(File file, String sResu) {
    this.file = file;
    this.sResu = sResu;
  }

  public SearchResult(WebRadio radio, String sResu) {
    this.radio = radio;
    this.sResu = sResu;
  }

  /**
   * Return hashcode, used during sorting
   */
  public int hashCode() {
    return sResu.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(SearchResult sr) {
    return sResu.compareToIgnoreCase(sr.getResu());
  }

  /**
   * @return Returns the file.
   */
  public File getFile() {
    return file;
  }

  /**
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
   * @return Returns the webradio.
   */
  public WebRadio getWebradio() {
    return radio;
  }

  /**
   * @return Returns the sResu.
   */
  public String getResu() {
    return sResu;
  }

}
