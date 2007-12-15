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
 *  $Revision$
 */

package org.jajuk.base;

/**
 * A web Radio
 */
public class WebRadio implements Comparable<WebRadio> {

  private String name;

  private String url;

  public WebRadio(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public String getName() {
    return this.name;
  }

  public String getUrl() {
    return this.url;
  }

  public boolean equals(Object other) {
    if (!(other instanceof WebRadio)) {
      return false;
    }
    return name.equals(((WebRadio) other).getName());
  }

  public int compareTo(WebRadio other) {
    // make null url web radio to appear first (useful for the wizard)
    if (getUrl() == null) {
      return -1;
    }
    return name.compareTo((other).getName());
  }

  public String toString() {
    return name + " (" + url + ")";
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return associated type, we always use mplayer for web radios
   * 
   * public Type getType() { return
   * TypeManager.getInstance().getTypeByExtension(EXT_RADIO); }
   */

}
