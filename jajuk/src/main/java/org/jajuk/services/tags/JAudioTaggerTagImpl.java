/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.services.tags;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.logging.LogManager;

import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagFieldKey;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

/**
 * {@link ITagImpl} Implementation based on <a
 * href="https://jaudiotagger.dev.java.net">JAudiotagger</a>
 */
public class JAudioTaggerTagImpl implements ITagImpl, Const {

  // Disable Jaudiotagger logs
  static {
    try {
      LogManager.getLogManager().readConfiguration(
          new ByteArrayInputStream("org.jaudiotagger.level = OFF".getBytes()));
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * the current audio file instance (set by {@link #setFile(File)}).<br>
   */
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
  public void commit() throws Exception {
    this.audioFile.commit();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getAlbumName()
   */
  public String getAlbumName() throws Exception {
    return this.tag.getFirstAlbum();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getAuthorName()
   */
  public String getAuthorName() throws Exception {
    return this.tag.getFirstArtist();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getComment()
   */
  public String getComment() throws Exception {
    return this.tag.getFirstComment();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getLength()
   */
  public long getLength() throws Exception {
    return this.audioFile.getAudioHeader().getTrackLength();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getOrder()
   */
  public long getOrder() throws Exception {
    String sOrder = this.tag.getFirstTrack();
    if (UtilString.isVoid(sOrder)) {
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
  public long getQuality() throws Exception {
    return this.audioFile.getAudioHeader().getBitRateAsNumber();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getStyleName()
   */
  public String getStyleName() throws Exception {
    String result = this.tag.getFirstGenre();
    if (UtilString.isVoid(result) || "genre".equals(result)) {
      // the item will be the default jajuk unknown string
      return "";
    }
    // Sometimes, the style has this form : (nb)
    if (result.matches("\\(.*\\).*")) {
      result = result.substring(1, result.indexOf(')'));
      try {
        result = UtilFeatures.GENRES[Integer.parseInt(result)];
      } catch (Exception e) {
        return ""; // error, return unknown
      }
    }
    // If genre is a number mapping a known style, use this style
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
  public String getTrackName() throws Exception {
    return this.tag.getFirstTitle();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getYear()
   */
  public String getYear() throws Exception {
    String result = this.tag.getFirstYear();
    if (UtilString.isVoid(result)) {
      result = "0";
    } else {
      Long.parseLong(result);
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setAlbumName(java.lang.String)
   */
  public void setAlbumName(String albumName) throws Exception {
    createTagIfNeeded();
    this.tag.setAlbum(albumName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setAuthorName(java.lang.String)
   */
  public void setAuthorName(String authorName) throws Exception {
    createTagIfNeeded();
    this.tag.setArtist(authorName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setComment(java.lang.String)
   */
  public void setComment(String comment) throws Exception {
    createTagIfNeeded();
    this.tag.setComment(comment);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setFile(java.io.File)
   */
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
      newTag.setTitle(tag.getFirstTitle());
      newTag.setArtist(tag.getFirstArtist());
      newTag.setAlbum(tag.getFirstAlbum());
      newTag.setComment(tag.getFirstComment());
      newTag.setGenre(tag.getFirstGenre());
      newTag.setYear(tag.getFirstYear());
      newTag.set(newTag.createTagField(TagFieldKey.ALBUM_ARTIST, tag
          .getFirst(TagFieldKey.ALBUM_ARTIST)));
      newTag.set(newTag.createTagField(TagFieldKey.DISC_NO, tag
          .getFirst(TagFieldKey.DISC_NO)));
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
  public void setOrder(long order) throws Exception {
    createTagIfNeeded();
    this.tag.setTrack(Long.toString(order));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setStyleName(java.lang.String)
   */
  public void setStyleName(String style) throws Exception {
    createTagIfNeeded();
    // Workaround for mp4 genre - Allows genres not in genre list to be written
    this.tag.deleteTagField(TagFieldKey.GENRE);
    this.tag.set(tag.createTagField(TagFieldKey.GENRE, style));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setTrackName(java.lang.String)
   */
  public void setTrackName(String trackName) throws Exception {
    createTagIfNeeded();
    this.tag.setTitle(trackName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setYear(java.lang.String)
   */
  public void setYear(String year) throws Exception {
    createTagIfNeeded();
    this.tag.setYear(year);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getAlbumArtist()
   */
  public String getAlbumArtist() throws Exception {
    return this.tag.getFirst(TagFieldKey.ALBUM_ARTIST);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getDiscNumber()
   */
  public long getDiscNumber() throws Exception {
    
    String sDiscNumber = this.tag.getFirst(TagFieldKey.DISC_NO);
    if (UtilString.isVoid(sDiscNumber)) {
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
  public void setAlbumArtist(String albumArtist) throws Exception {
    createTagIfNeeded();
    tag.set(tag.createTagField(TagFieldKey.ALBUM_ARTIST, albumArtist));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setDiscNumber(int)
   */
  public void setDiscNumber(long discnumber) throws Exception {
    createTagIfNeeded();
    tag.set(tag.createTagField(TagFieldKey.DISC_NO, Long.toString(discnumber)));
  }

}
