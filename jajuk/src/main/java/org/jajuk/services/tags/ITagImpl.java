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
 *  $Revision: 3156 $
 */
package org.jajuk.services.tags;

/**
 * Mandatory methods required for all tag implementations
 */
public interface ITagImpl {

  /**
   * @return track name as defined in tags are file name otherwise
   */
  public String getTrackName() throws Exception;

  /**
   * @return album name
   */
  public String getAlbumName() throws Exception;

  /**
   * @return author name
   */
  public String getAuthorName() throws Exception;

  /**
   * @return style name
   */
  public String getStyleName() throws Exception;

  /**
   * @return length in sec
   */
  public long getLength() throws Exception;

  /**
   * @return creation year
   */
  public String getYear() throws Exception;

  /**
   * @return quality
   */
  public long getQuality() throws Exception;

  /**
   * @return comment
   */
  public String getComment() throws Exception;

  /**
   * @return track order
   * @throws Exception
   */
  public long getOrder() throws Exception;

  /**
   * @return specific property or null if property not tagged
   */
  public String getTagItem(String sTagItem) throws Exception;

  /**
   * @param sTrackName
   */
  public void setTrackName(String sTrackName) throws Exception;

  /**
   * @param sAlbumName
   */
  public void setAlbumName(String sAlbumName) throws Exception;

  /**
   * @param sAuthorName
   */
  public void setAuthorName(String sAuthorName) throws Exception;

  /**
   * @param style
   */
  public void setStyleName(String style) throws Exception;

  /**
   * @param sYear
   */
  public void setYear(String sYear) throws Exception;

  /**
   * @param sComment
   */
  public void setComment(String sComment) throws Exception;

  /**
   * Set current file to work with.
   * 
   * @param fio
   */
  public void setFile(java.io.File fio) throws Exception;

  /**
   * Set track order
   * 
   * @param sOrder
   * @throws Exception
   */
  public void setOrder(long lOrder) throws Exception;

  /**
   * @return Set a specific property
   */
  public void setTagItem(String sTagItem, String sValue) throws Exception;

  /**
   * Commit all changes in the tag
   */
  public void commit() throws Exception;

}
