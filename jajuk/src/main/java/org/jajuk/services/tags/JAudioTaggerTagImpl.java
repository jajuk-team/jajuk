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
package org.jajuk.services.tags;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.covers.Cover;
import org.jajuk.services.covers.Cover.CoverType;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
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
import org.jaudiotagger.tag.images.Artwork;

/**
 * .
 *
 * {@link ITagImpl} Implementation based on <a
 * href="https://jaudiotagger.dev.java.net">JAudiotagger</a>
 * 
 * We expect that tag is not null when calling getXXX() methods
 */
public class JAudioTaggerTagImpl implements ITagImpl, Const {
  private static List<String> tagFieldKeyArrayList = new ArrayList<String>();
  private static final Pattern PATTERN_NON_DIGIT = Pattern.compile(".*[^0-9].*");
  private static final Pattern PATTERN_FOUR_DIGITS = Pattern.compile("\\D*(\\d{4})\\D*");
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
  /** the current {@linkplain Tag tag} ( {@link AudioFile#getTag()} ) set by {@link #setFile(File)}.<br> */
  private Tag tag;

  @Override
  public void commit() throws Exception {
    this.audioFile.commit();
  }

  @Override
  public String getAlbumName() throws Exception {
    return this.tag.getFirst(FieldKey.ALBUM);
  }

  @Override
  public String getArtistName() throws Exception {
    return this.tag.getFirst(FieldKey.ARTIST);
  }

  @Override
  public String getLyrics() throws Exception {
    String lyrics = tag.getFirst(FieldKey.LYRICS);
    if (StringUtils.isBlank(lyrics)) {
      return "";
    } else {
      return lyrics;
    }
  }

  @Override
  public String getAlbumArtist() throws Exception {
    return this.tag.getFirst(FieldKey.ALBUM_ARTIST);
  }

  @Override
  public String getComment() throws Exception {
    return this.tag.getFirst(FieldKey.COMMENT);
  }

  @Override
  public long getLength() throws Exception {
    return this.audioFile.getAudioHeader().getTrackLength();
  }

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

  @Override
  public long getQuality() throws Exception {
    return this.audioFile.getAudioHeader().getBitRateAsNumber();
  }

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

  @Override
  public String getTrackName() throws Exception {
    return this.tag.getFirst(FieldKey.TITLE);
  }

  @Override
  public String getYear() throws Exception {
    String result = this.tag.getFirst(FieldKey.YEAR);
    if (StringUtils.isBlank(result)) {
      return "0";
    }
    // The string contains at least a single character other than a digit, 
    // then try to parse the first four digits if any 
    if (PATTERN_NON_DIGIT.matcher(result).matches()) {
      Matcher matcher = PATTERN_FOUR_DIGITS.matcher(result);
      if (matcher.find()) {
        return matcher.group(1);
      } else {
        throw new NumberFormatException("Wrong year or date format");
      }
    } else {
      // Only digits
      return result;
    }
  }

  @Override
  public void setAlbumName(String albumName) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.ALBUM, albumName);
  }

  @Override
  public void setArtistName(String artistName) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.ARTIST, artistName);
  }

  @Override
  public void setComment(String comment) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.COMMENT, comment);
  }

  @Override
  public void setLyrics(String sLyrics) throws Exception {
    createTagIfNeeded();
    TagField tagLyrics = tag.createField(FieldKey.LYRICS, sLyrics);
    tag.setField(tagLyrics);
    commit();
  }

  @Override
  public void deleteLyrics() throws Exception {
    tag.deleteField(FieldKey.LYRICS);
    commit();
  }

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
   * @throws Exception the exception
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

  @Override
  public void setOrder(long order) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.TRACK, Long.toString(order));
  }

  @Override
  public void setGenreName(String genre) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.GENRE, genre);
  }

  @Override
  public void setTrackName(String trackName) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.TITLE, trackName);
  }

  @Override
  public void setYear(String year) throws Exception {
    createTagIfNeeded();
    this.tag.setField(FieldKey.YEAR, year);
  }

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

  @Override
  public String getTagField(String tagFieldKey) throws Exception {
    return this.tag.getFirst(FieldKey.valueOf(tagFieldKey));
  }

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

  @Override
  public void setAlbumArtist(String albumArtist) throws Exception {
    createTagIfNeeded();
    tag.setField(FieldKey.ALBUM_ARTIST, albumArtist);
  }

  @Override
  public void setDiscNumber(long discnumber) throws Exception {
    createTagIfNeeded();
    tag.setField(FieldKey.DISC_NO, Long.toString(discnumber));
  }

  @Override
  public List<Cover> getCovers() throws Exception {
    List<Cover> covers = new ArrayList<Cover>(1);
    List<Artwork> artworkList = tag.getArtworkList();
    // index : prefix for cover file extracted into the cache directory
    int index = 1;
    for (Artwork artwork : artworkList) {
      File coverFile = buildTagCacheFile(index);
      // [PERF] Only extract artworks if the cache file doesn't yet exist.
      if (!coverFile.exists()) {
        byte[] imageRawData = artwork != null ? artwork.getBinaryData() : null;
        if (imageRawData != null) {
          BufferedImage bi = ImageIO.read(new ByteArrayInputStream(imageRawData));
          if (bi != null) {
            ImageIO.write(bi, "png", coverFile);
          }
        }
      }
      // test if the cover has actually been created, it is not if the tag image was corrupted
      if (coverFile.exists()) {
        Cover cover = new Cover(coverFile, CoverType.TAG_COVER);
        covers.add(cover);
      }
      index++;
    }
    return covers;
  }

  /**
   * Build target cover path. The cover tag is extracted and copied to a file in the cache
   * it is uniquely identified by absolute filename AND file last change so we force recreating the 
   * cache if user add/remove cover tags from audio files using another tool.
   * To avoid overriding of tags between files, each potentially containing several artworks.
   * @param index index of the tag
   * @return file for upcoming cache
   */
  private File buildTagCacheFile(int index) {
    File fio = audioFile.getFile();
    String absolutePath = fio.getAbsolutePath();
    long lastChange = fio.lastModified();
    String hash = MD5Processor.hash(absolutePath + lastChange);
    File coverFile = SessionService.getConfFileByPath(Const.FILE_CACHE + '/' + hash + "_" + index
        + "_" + Const.TAG_COVER_FILE);
    return coverFile;
  }

  @Override
  public boolean isTagAvailable() {
    return (tag != null);
  }
}
