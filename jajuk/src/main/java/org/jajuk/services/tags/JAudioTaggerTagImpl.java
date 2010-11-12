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
package org.jajuk.services.tags;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import org.apache.commons.lang.StringUtils;
import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

/**
 * {@link ITagImpl} Implementation based on <a
 * href="https://jaudiotagger.dev.java.net">JAudiotagger</a>
 */
public class JAudioTaggerTagImpl implements ITagImpl, Const {

  /** DOCUMENT_ME. */
  private static List<String> tagFieldKeyArrayList = new ArrayList<String>();

  static {
    try {
      // Disable Jaudiotagger logs
      LogManager.getLogManager().readConfiguration(
          new ByteArrayInputStream("org.jaudiotagger.level = OFF".getBytes()));

      // get supported tags

      FieldKey[] tagFieldKeys = FieldKey.values();
      for (FieldKey tfk : tagFieldKeys) {
        if (!tfk.equals(FieldKey.DISC_NO) && !tfk.equals(FieldKey.ALBUM)
            && !tfk.equals(FieldKey.ALBUM_ARTIST) && !tfk.equals(FieldKey.ARTIST)
            && !tfk.equals(FieldKey.GENRE) && !tfk.equals(FieldKey.TITLE)
            && !tfk.equals(FieldKey.TRACK) && !tfk.equals(FieldKey.YEAR)
            && !tfk.equals(FieldKey.COMMENT)) {
          tagFieldKeyArrayList.add(tfk.name());
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /** the current audio file instance (set by {@link #setFile(File)}).<br> */
  private AudioFile audioFile;

  /**
   * the current {@linkplain Tag tag} ( {@link AudioFile#getTag()} ) set by
   * {@link #setFile(File)}.<br>
   */
  private Tag tag;

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#commit()
   */
  @Override
  public void commit() throws Exception {
    this.audioFile.commit();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getAlbumName()
   */
  @Override
  public String getAlbumName() throws Exception {
    return this.tag.getFirst(FieldKey.ALBUM);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getArtistName()
   */
  @Override
  public String getArtistName() throws Exception {
    return this.tag.getFirst(FieldKey.ARTIST);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getComment()
   */
  @Override
  public String getComment() throws Exception {
    return this.tag.getFirst(FieldKey.COMMENT);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getLength()
   */
  @Override
  public long getLength() throws Exception {
    return this.audioFile.getAudioHeader().getTrackLength();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getOrder()
   */
  @Override
  public long getOrder() throws Exception {
    String sOrder = this.tag.getFirst(FieldKey.TRACK);
    if (StringUtils.isBlank(sOrder)) {
      return 0;
    }
    if (sOrder.matches(".*/.*")) {
      sOrder = sOrder.substring(0, sOrder.indexOf('/'));
    }
    return Long.parseLong(sOrder);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getQuality()
   */
  @Override
  public long getQuality() throws Exception {
    return this.audioFile.getAudioHeader().getBitRateAsNumber();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getGenreName()
   */
  @Override
  public String getGenreName() throws Exception {
    String result = this.tag.getFirst(FieldKey.GENRE);
    if (StringUtils.isBlank(result) || "genre".equals(result)) {
      // the item will be the default jajuk unknown string
      return "";
    }
    // Sometimes, the genre has this form : (nb)
    if (result.matches("\\(.*\\).*")) {
      result = result.substring(1, result.indexOf(')'));
      try {
        result = UtilFeatures.GENRES[Integer.parseInt(result)];
      } catch (Exception e) {
        return ""; // error, return unknown
      }
    }
    // If genre is a number mapping a known genre, use this genre
    try {
      int number = Integer.parseInt(result);
      if (number >= 0 && number < UtilFeatures.GENRES.length) {
        result = UtilFeatures.GENRES[number];
      }
    } catch (NumberFormatException e) {
      // nothing wrong here
    }
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getTrackName()
   */
  @Override
  public String getTrackName() throws Exception {
    return this.tag.getFirst(FieldKey.TITLE);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getYear()
   */
  @Override
  public String getYear() throws Exception {
    String result = this.tag.getFirst(FieldKey.YEAR);
    if (StringUtils.isBlank(result)) {
      result = "0";
    } else {
      Long.parseLong(result);
    }
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getLyrics()
   */
  @Override
  public String getLyrics() throws Exception {
    String lyrics = tag.getFirst(FieldKey.LYRICS);
    if (StringUtils.isBlank(lyrics)) {
      return "";
    } else {
      return lyrics;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setAlbumName(java.lang.String)
   */
  @Override
  public void setAlbumName(String albumName) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.ALBUM, albumName);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setArtistName(java.lang.String)
   */
  @Override
  public void setArtistName(String artistName) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.ARTIST, artistName);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setComment(java.lang.String)
   */
  @Override
  public void setComment(String comment) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.COMMENT, comment);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setLyrics(java.lang.String)
   */
  @Override
  public void setLyrics(String sLyrics) throws Exception {
    createTagIfNeeded();
    TagField tagLyrics = tag.createField(FieldKey.LYRICS, sLyrics);
    tag.setField(tagLyrics);
    commit();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#deleteLyrics()
   */
  @Override
  public void deleteLyrics() throws Exception {
    tag.deleteField(FieldKey.LYRICS);
    commit();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setFile(java.io.File)
   */
  @Override
  public void setFile(File fio) throws Exception {
    try {
      audioFile = AudioFileIO.read(fio);
      // Jaudiotagger returns null if the track contains none tag, we work then
      // with null for getXXX() methods and we create a void tag in setXXX
      // methods
      tag = this.audioFile.getTag();
    } catch (Throwable t) { // can throw OutOfMemory errors
      Log.error(t);
      throw new JajukException(103, fio.toString(), t);
    }
  }

  /**
   * Create a void tag is needed and convert an ID3 V1.0 tag into V2.4 if any <br>
   * Tags are committed when leaving this method
   *
   * @throws Exception
   *           the exception
   */
  private void createTagIfNeeded() throws Exception {
    // No tag ? create one
    if (tag == null) {
      Log.info("No tag, try to create a void one");
      // Ignore this to force error when writing
      tag = audioFile.getTagOrCreateAndSetDefault();
      // Still null ? problem creating the tag
      if (tag == null) {
        throw new Exception("Cannot Create empty tag");
      }
    }
    // ID3 V1 (very old) tag ? convert it to ID3 V2.4 because it doesn't contain
    // the Track# field and we need it
    else if (tag instanceof ID3v1Tag) {
      Log.info("ID3 V1.0 tag found, convertion to V2.4");
      Tag newTag = new ID3v24Tag();
      newTag.setField(FieldKey.TITLE, tag.getFirst(FieldKey.TITLE));
      newTag.setField(FieldKey.ARTIST, tag.getFirst(FieldKey.ARTIST));
      newTag.setField(FieldKey.ALBUM, tag.getFirst(FieldKey.ALBUM));
      newTag.setField(FieldKey.COMMENT, tag.getFirst(FieldKey.COMMENT));
      newTag.setField(FieldKey.GENRE, tag.getFirst(FieldKey.GENRE));
      newTag.setField(FieldKey.YEAR, tag.getFirst(FieldKey.YEAR));
      newTag.setField(FieldKey.ALBUM_ARTIST, tag.getFirst(FieldKey.ALBUM_ARTIST));

      // only set the discnumber if we have a useful one
      String discno = tag.getFirst(FieldKey.DISC_NO);
      if (StringUtils.isNotEmpty(discno) && StringUtils.isNumeric(discno)) {
        newTag.setField(FieldKey.DISC_NO, discno);
      }

      // Delete the id3 V1 tag
      AudioFileIO.delete(audioFile);
      // Add the new one
      audioFile.setTag(newTag);
      this.tag = newTag;
    }

  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setOrder(long)
   */
  @Override
  public void setOrder(long order) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.TRACK, Long.toString(order));
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setGenreName(java.lang.String)
   */
  @Override
  public void setGenreName(String genre) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.GENRE, genre);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setTrackName(java.lang.String)
   */
  @Override
  public void setTrackName(String trackName) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.TITLE, trackName);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setYear(java.lang.String)
   */
  @Override
  public void setYear(String year) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.YEAR, year);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getAlbumArtist()
   */
  @Override
  public String getAlbumArtist() throws Exception {
    return this.tag.getFirst(FieldKey.ALBUM_ARTIST);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setTagField(java.lang.String, java.lang.String)
   */
  @Override
  public void setTagField(String tagFieldKey, String tagFieldValue) {
    try {
      this.tag.setField(tag.createField(FieldKey.valueOf(tagFieldKey), tagFieldValue));
    } catch (FieldDataInvalidException e) {
      Log.error(e);
    } catch (KeyNotFoundException e) {
      Log.error(e);
    }
  }

  /**
  * Gets the supported tag fields.
  *
  * @return the supported tag fields
  */
  @Override
  public List<String> getSupportedTagFields() {
    return tagFieldKeyArrayList;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getTagField(java.lang.String)
   */
  @Override
  public String getTagField(String tagFieldKey) throws Exception {
    return this.tag.getFirst(FieldKey.valueOf(tagFieldKey));
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getDiscNumber()
   */
  @Override
  public long getDiscNumber() throws Exception {

    String sDiscNumber = this.tag.getFirst(FieldKey.DISC_NO);
    if (StringUtils.isBlank(sDiscNumber)) {
      return 01;
    }
    if (sDiscNumber.matches(".*/.*")) {
      sDiscNumber = sDiscNumber.substring(0, sDiscNumber.indexOf('/'));
    }
    return Long.parseLong(sDiscNumber);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setAlbumArtist(java.lang.String)
   */
  @Override
  public void setAlbumArtist(String albumArtist) throws Exception {
    createTagIfNeeded();
    tag.setField(FieldKey.ALBUM_ARTIST, albumArtist);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setDiscNumber(int)
   */
  @Override
  public void setDiscNumber(long discnumber) throws Exception {
    createTagIfNeeded();
    tag.setField(FieldKey.DISC_NO, Long.toString(discnumber));
  }

}
