/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
package org.jajuk.services.lastfm.model;

import java.util.Date;

public class LastFmImage {

  private Date dateAdded;
  private String format;
  private String owner;
  private String title;
  private String url;

  public LastFmImage() {
  }

  public Date getDateAdded() {
    return this.dateAdded;
  }

  public String getFormat() {
    return this.format;
  }

  public String getOwner() {
    return owner;
  }

  public String getTitle() {
    return this.title;
  }

  public String getUrl() {
    return this.url;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDateAdded(Date dateAdded) {
    this.dateAdded = dateAdded;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
