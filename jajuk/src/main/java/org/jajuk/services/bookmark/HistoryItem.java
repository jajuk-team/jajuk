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
 *  $Revision$
 */

package org.jajuk.services.bookmark;

import java.util.Date;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;

/**
 * An history item.
 */
public class HistoryItem {

  /** File Id. */
  private String sFileId;

  /** Play date. */
  private long lDate;

  /**
   * Instantiates a new history item.
   * 
   * @param sFileId DOCUMENT_ME
   * @param lDate DOCUMENT_ME
   */
  public HistoryItem(String sFileId, long lDate) {
    this.sFileId = sFileId;
    this.lDate = lDate;
  }

  /**
   * Gets the date.
   * 
   * @return Returns the date.
   */
  public long getDate() {
    return lDate;
  }

  /**
   * Sets the date.
   * 
   * @param lDate DOCUMENT_ME
   */
  public void setDate(long lDate) {
    this.lDate = lDate;
  }

  /**
   * Gets the file id.
   * 
   * @return Returns the sFileId.
   */
  public String getFileId() {
    return sFileId;
  }

  /**
   * Sets the file id.
   * 
   * @param fileId The sFileId to set.
   */
  public void setFileId(String fileId) {
    sFileId = fileId;
  }

  /**
   * Human readable representation of this history item as read in the history
   * bar.
   * 
   * @return String
   */
  @Override
  public String toString() {
    File file = FileManager.getInstance().getFileByID(getFileId());
    if (file == null) {
      return "no file (" + getFileId() + ')';
    }

    String sDate = History.getInstance().getDateFormatter().format(new Date(getDate()));
    StringBuilder sb = new StringBuilder();
    sb.append('[').append(sDate).append("] ");
    sb.append(file.getTrack().getArtist().getName2());
    sb.append(" / ").append(file.getTrack().getName());
    return sb.toString();
  }
}
