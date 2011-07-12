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
package org.jajuk.services.tags;

import java.util.List;

import org.jajuk.services.covers.Cover;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;

/**
 * Mandatory methods required for all tag implementations.
 */
public interface ITagImpl {

  /**
   * Gets the track name.
   * 
   * @return track name as defined in tags are file name otherwise
   * 
   * @throws Exception the exception
   */
  String getTrackName() throws Exception;

  /**
   * Gets the album name.
   * 
   * @return album name
   * 
   * @throws Exception the exception
   */
  String getAlbumName() throws Exception;

  /**
   * Gets the artist name.
   * 
   * @return artist name
   * 
   * @throws Exception the exception
   */
  String getArtistName() throws Exception;

  /**
   * Gets the album artist.
   * 
   * @return album artist
   * 
   * @throws Exception the exception
   */
  String getAlbumArtist() throws Exception;

  /**
   * Gets the disc number.
   * 
   * @return disc number
   * 
   * @throws Exception the exception
   */
  long getDiscNumber() throws Exception;

  /**
   * Gets the genre name.
   * 
   * @return genre name
   * 
   * @throws Exception the exception
   */
  String getGenreName() throws Exception;

  /**
   * Gets the length.
   * 
   * @return length in sec
   * 
   * @throws Exception the exception
   */
  long getLength() throws Exception;

  /**
   * Gets the year.
   * 
   * @return creation year
   * 
   * @throws Exception the exception
   */
  String getYear() throws Exception;

  /**
   * Gets the quality.
   * 
   * @return quality
   * 
   * @throws Exception the exception
   */
  long getQuality() throws Exception;

  /**
   * Gets the comment.
   * 
   * @return comment
   * 
   * @throws Exception the exception
   */
  String getComment() throws Exception;

  /**
   * Gets the order.
   * 
   * @return track order
   * 
   * @throws Exception the exception
   */
  long getOrder() throws Exception;

  /**
   * Gets the lyrics.
   * 
   * @return Lyrics read from Tag
   * 
   * @throws Exception the exception
   */
  String getLyrics() throws Exception;

  /**
   * Sets the track name.
   * 
   * @param sTrackName DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setTrackName(String sTrackName) throws Exception;

  /**
   * Sets the album name.
   * 
   * @param sAlbumName DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setAlbumName(String sAlbumName) throws Exception;

  /**
   * Sets the artist name.
   * 
   * @param sArtistName DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setArtistName(String sArtistName) throws Exception;

  /**
   * Sets the album artist.
   * 
   * @param sAlbumArtist DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setAlbumArtist(String sAlbumArtist) throws Exception;

  /**
   * Sets the disc number.
   * 
   * @param discnumber DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setDiscNumber(long discnumber) throws Exception;

  /**
   * Sets the genre name.
   * 
   * @param genre DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setGenreName(String genre) throws Exception;

  /**
   * Sets the year.
   * 
   * @param sYear DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setYear(String sYear) throws Exception;

  /**
   * Sets the comment.
   * 
   * @param sComment DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setComment(String sComment) throws Exception;

  /**
   * Sets the lyrics.
   * 
   * @param sLyrics DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setLyrics(String sLyrics) throws Exception;

  /**
   * Deletes lyrics saved in Tag.
   * 
   * @throws Exception the exception
   */
  void deleteLyrics() throws Exception;

  /**
   * Set current file to work with.
   * 
   * @param fio DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setFile(java.io.File fio) throws Exception;

  /**
   * Set track order.
   * 
   * @param lOrder DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  void setOrder(long lOrder) throws Exception;

  /**
   * Commit all changes in the tag.
   * 
   * @throws Exception the exception
   */
  void commit() throws Exception;

  /**
   * Get value of tagFieldKey.
   * 
   * @param tagFieldKey DOCUMENT_ME
   * 
   * @return the tag field
   * 
   * @throws Exception the exception
   */
  public String getTagField(String tagFieldKey) throws Exception;

  /**
   * Set value of tagFieldKey.
   *
   * @param tagFieldKey DOCUMENT_ME
   * @param tagFieldValue DOCUMENT_ME
   * @throws Exception the exception
   */
  public void setTagField(String tagFieldKey, String tagFieldValue) throws Exception;

  /**
   * Gets the supported tag fields.
   * 
   * @return the supported tag fields
   */
  public List<String> getSupportedTagFields();

  /**
   * Gets the embedded covers.
   *
   * @return the covers or a void list if none.
   * @throws Exception the exception
   */
  public List<Cover> getCovers() throws Exception;

}
