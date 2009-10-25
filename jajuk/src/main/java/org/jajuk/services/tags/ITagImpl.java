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

import java.util.ArrayList;

import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;

/**
 * Mandatory methods required for all tag implementations
 */
public interface ITagImpl {

  /**
   * @return track name as defined in tags are file name otherwise
   */
  String getTrackName() throws Exception;

  /**
   * @return album name
   */
  String getAlbumName() throws Exception;

  /**
   * @return author name
   */
  String getAuthorName() throws Exception;

  /**
   * @return album artist
   */
  String getAlbumArtist() throws Exception;

  /**
   * @return disc number
   */
  long getDiscNumber() throws Exception;

  /**
   * @return style name
   */
  String getStyleName() throws Exception;

  /**
   * @return length in sec
   */
  long getLength() throws Exception;

  /**
   * @return creation year
   */
  String getYear() throws Exception;

  /**
   * @return quality
   */
  long getQuality() throws Exception;

  /**
   * @return comment
   */
  String getComment() throws Exception;

  /**
   * @return track order
   * @throws Exception
   */
  long getOrder() throws Exception;

  /**
   * @param sTrackName
   */
  void setTrackName(String sTrackName) throws Exception;

  /**
   * @param sAlbumName
   */
  void setAlbumName(String sAlbumName) throws Exception;

  /**
   * @param sAuthorName
   */
  void setAuthorName(String sAuthorName) throws Exception;

  /**
   * @param sAlbumArtist
   */
  void setAlbumArtist(String sAlbumArtist) throws Exception;

  /**
   * @param discnumber
   */
  void setDiscNumber(long discnumber) throws Exception;

  /**
   * @param style
   */
  void setStyleName(String style) throws Exception;

  /**
   * @param sYear
   */
  void setYear(String sYear) throws Exception;

  /**
   * @param sComment
   */
  void setComment(String sComment) throws Exception;

  /**
   * Set current file to work with.
   * 
   * @param fio
   */
  void setFile(java.io.File fio) throws Exception;

  /**
   * Set track order
   * 
   * @param sOrder
   * @throws Exception
   */
  void setOrder(long lOrder) throws Exception;

  /**
   * Commit all changes in the tag
   */
  void commit() throws Exception;

  /**
   * Get value of tagFieldKey
   */
  public String getTagField(String tagFieldKey) throws Exception;

  /**
   * Set value of tagFieldKey
   */
  public void setTagField(String tagFieldKey, String tagFieldValue)
      throws FieldDataInvalidException, KeyNotFoundException;

  /**
   * get all supported tags
   */
  public ArrayList<String> getSupportedTagFields();

}
