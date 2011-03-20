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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * abstract tag, independent from real implementation.
 */
public class Tag {

  /** Current tag impl*. */
  private ITagImpl tagImpl;

  /** Current file*. */
  private File fio;

  /** Is this tag corrupted ?. */
  private boolean bCorrupted = false;

  /** File -> tag cache This is required by the autocommit=false operations. */
  static private Map<File, Tag> tagsCache = new HashMap<File, Tag>(10);

  /** DOCUMENT_ME. */
  private static List<String> supportedTagFields = null;

  /**
   * Tag constructor.
   * 
   * @param fio DOCUMENT_ME
   * @param bIgnoreErrors DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   * 
   * @bIgnoreError : ignore error and keep instance
   */
  public Tag(java.io.File fio, boolean bIgnoreErrors) throws JajukException {
    try {
      this.fio = fio;
      Type type = TypeManager.getInstance().getTypeByExtension(UtilSystem.getExtension(fio));
      tagImpl = type.getTagImpl();
      tagImpl.setFile(fio);
      bCorrupted = false;
    } catch (Exception e) {
      bCorrupted = true;
      if (!bIgnoreErrors) {
        throw new JajukException(103, (fio == null ? "<null>" : fio.getName()), e);
      }
    }
  }

  /**
   * Gets the track name.
   * 
   * @return track name as defined in tags are file name otherwise
   */
  public String getTrackName() {
    // by default, track name is the file name without extension
    String sTrackName = UtilSystem.removeExtension(fio.getName());
    if (tagImpl == null) { // if the type doesn't support tags ( like wav )
      return sTrackName;
    }

    try {
      String sTemp = tagImpl.getTrackName().trim();
      if (!"".equals(sTemp)) {
        // remove the extension
        sTrackName = UtilString.formatTag(sTemp);
      }
    } catch (Exception e) {
      Log.info("Wrong track name:{{" + fio.getName() + "}}");
    }
    return sTrackName;
  }

  /**
   * Gets the album name.
   * 
   * @return album name
   */
  public String getAlbumName() {
    if (tagImpl == null) { // if the type doesn't support tags ( like wav )
      if (Conf.getBoolean(Const.CONF_TAGS_USE_PARENT_DIR)) {
        return fio.getParentFile().getName();
        // if album is not found, take current directory as album name
      } else {
        return Messages.getString(Const.UNKNOWN_ALBUM);
      }
    }
    String sAlbumlName = null;
    try {
      String sTemp = tagImpl.getAlbumName().trim();
      if (Messages.getString(Const.UNKNOWN_ALBUM).equals(sTemp)) {
        // it is done to avoid duplicates unknown genres if
        // the tag is the real string "unknown" in the
        // current language
        sAlbumlName = Const.UNKNOWN_ALBUM;
      } else if (!"".equals(sTemp)) {
        sAlbumlName = sTemp;
      }
    } catch (Exception e) {
      Log.info("Wrong album name:{{" + fio.getName() + "}}");
    }

    if (sAlbumlName == null) { // album tag cannot be found
      if (Conf.getBoolean(Const.CONF_TAGS_USE_PARENT_DIR)) {
        sAlbumlName = fio.getParentFile().getName();
        // if album is not found, take current directory as album name
      } else {
        sAlbumlName = Messages.getString(Const.UNKNOWN_ALBUM);
      }
    }
    sAlbumlName = UtilString.formatTag(sAlbumlName);
    // We internalize the album name for memory saving reasons
    return sAlbumlName.intern();
  }

  /**
   * Gets the artist name.
   * 
   * @return artist name
   */
  public String getArtistName() {
    String sArtistName = Const.UNKNOWN_ARTIST;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return sArtistName;
    }
    try {
      String sTemp = tagImpl.getArtistName().trim();
      if (Messages.getString(Const.UNKNOWN_ARTIST).equals(sTemp)) {
        // it is done to avoid duplicates unknown genres if
        // the tag is the real string "unknown" in the
        // current language
        sArtistName = Const.UNKNOWN_ARTIST;
      } else if (!"".equals(sTemp)) {
        sArtistName = UtilString.formatTag(sTemp);
      }
    } catch (Exception e) {
      Log.info("Wrong artist name:{{" + fio.getName() + "}}");
    }
    // We internalize the artist name for memory saving reasons
    return sArtistName.intern();
  }

  /**
   * Gets the album artist.
   * 
   * @return album artist
   */
  public String getAlbumArtist() {
    String sAlbumArtist = Const.UNKNOWN_ARTIST;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return sAlbumArtist;
    }

    try {
      String sTemp = tagImpl.getAlbumArtist().trim();
      if (Messages.getString(Const.UNKNOWN_ARTIST).equals(sTemp)) {
        sAlbumArtist = Const.UNKNOWN_ARTIST;
      } else if (!"".equals(sTemp)) {
        sAlbumArtist = UtilString.formatTag(sTemp);
      }
    } catch (Exception e) {
      Log.info("Wrong album artist:{{" + fio.getName() + "}}");
    }
    // We internalize the artist name for memory saving reasons
    return sAlbumArtist.intern();

  }

  /**
   * Gets the genre name.
   * 
   * @return genre name
   */
  public String getGenreName() {
    String genre = Const.UNKNOWN_GENRE;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return genre;
    }

    try {
      String sTemp = tagImpl.getGenreName().trim();
      if (Messages.getString(Const.UNKNOWN_GENRE).equals(sTemp)) {
        // it is done to avoid duplicates unknown genres if
        // the tag is the real string "unknown" in the
        // current language
        genre = Const.UNKNOWN_GENRE;
      } else if (!"".equals(sTemp)) {
        if ("unknown".equals(sTemp)) {
          sTemp = genre;
        }
        genre = UtilString.formatTag(sTemp);
      }
    } catch (Exception e) {
      Log.info("Wrong genre name: {{" + fio.getName() + "}}");
    }
    // We internalize the genre name for memory saving reasons
    return genre.intern();

  }

  /**
   * Gets the length.
   * 
   * @return length in sec
   */
  public long getLength() {
    long length = 0;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return 0;
    }
    try {
      length = tagImpl.getLength();
    } catch (Exception e) {
      Log.info("Wrong length:{{" + fio.getName() + "}}");
    }
    return length;
  }

  /**
   * Gets the disc number.
   * 
   * @return disc number, by default, return 0
   */
  public long getDiscNumber() {
    long l = 0l;
    try {
      l = tagImpl.getDiscNumber();

    } catch (Exception e) {
      // just debug, no warn because wrong order are too often and
      // generate too much traces
      Log.info("Wrong disc number:{{" + fio.getName() + "}}");
      l = 01;
    }
    return l;
  }

  /**
   * Gets the year.
   * 
   * @return creation year
   */
  public String getYear() {
    String year = "0";
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return year;
    }
    try {
      year = tagImpl.getYear(); // check it is an integer
    } catch (Exception e) {
      Log.info("Wrong year:{{" + fio.getName() + "}}");
    }
    // We internalize the year name for memory saving reasons
    return year.intern();

  }

  /**
   * Gets the quality.
   * 
   * @return quality
   */
  public long getQuality() {
    long lQuality = 0l;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return lQuality;
    }
    try {
      lQuality = tagImpl.getQuality();
    } catch (Exception e) {
      Log.info("Wrong quality:{{" + fio.getName() + "}}");
    }
    return lQuality;
  }

  /**
   * Gets the comment.
   * 
   * @return comment
   */
  public String getComment() {
    String sComment = "";
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return sComment;
    }

    try {
      String sTemp = tagImpl.getComment();
      if (sTemp != null && !sTemp.equals("")) {
        sComment = UtilString.formatTag(sTemp);
      }
    } catch (Exception e) {
      Log.info("Wrong comment:{{" + fio.getName() + "}}");
    }
    // We internalize the comments for memory saving reasons
    return sComment.intern();
  }

  /**
   * Gets the order.
   * 
   * @return comment
   */
  public long getOrder() {
    long l = 0l;
    try {
      l = tagImpl.getOrder();
      if (l < 0) {
        throw new Exception("Negative Order");
      }
    } catch (Exception e) {
      // just debug, no warn because wrong order are too often and
      // generate too much traces
      Log.info("Wrong order:{{" + fio.getName() + "}}");
      l = 0;
    }
    return l;
  }

  /**
   * Gets the lyrics.
   * 
   * @return the lyrics
   */
  public String getLyrics() {
    String sLyrics = "";
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return sLyrics;
    }

    try {
      sLyrics = tagImpl.getLyrics();
    } catch (Exception e) {
      Log.info("Wrong lyrics:{{" + fio.getName() + "}}");
    }

    return sLyrics;
  }

  /**
   * Sets the track name.
   * 
   * @param sTrackName DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setTrackName(String sTrackName) throws JajukException {
    try {
      tagImpl.setTrackName(sTrackName);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the album name.
   * 
   * @param sAlbumName DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setAlbumName(String sAlbumName) throws JajukException {
    try {
      tagImpl.setAlbumName(sAlbumName);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the artist name.
   * 
   * @param sArtistName DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setArtistName(String sArtistName) throws JajukException {
    try {
      tagImpl.setArtistName(sArtistName);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the album artist.
   * 
   * @param sAlbumArtist DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setAlbumArtist(String sAlbumArtist) throws JajukException {
    try {
      tagImpl.setAlbumArtist(sAlbumArtist);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the genre name.
   * 
   * @param genre DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setGenreName(String genre) throws JajukException {
    try {
      tagImpl.setGenreName(genre);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the order.
   * 
   * @param lOrder DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setOrder(long lOrder) throws JajukException {
    try {
      tagImpl.setOrder(lOrder);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the year.
   * 
   * @param sYear DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setYear(String sYear) throws JajukException {
    try {
      tagImpl.setYear(sYear);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the disc number.
   * 
   * @param discnumber DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setDiscNumber(long discnumber) throws JajukException {
    try {
      tagImpl.setDiscNumber(discnumber);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the comment.
   * 
   * @param sComment DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void setComment(String sComment) throws JajukException {
    try {
      tagImpl.setComment(sComment);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Sets the lyrics.
   * 
   * @param sLyrics the new lyrics
   * 
   * @throws JajukException the jajuk exception
   */
  public void setLyrics(String sLyrics) throws JajukException {
    try {
      if (NoTagsTagImpl.class.equals(tagImpl.getClass())) {
        throw new Exception();
      }
      tagImpl.setLyrics(sLyrics);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Delete lyrics. DOCUMENT_ME
   * 
   * @throws JajukException the jajuk exception
   */
  public void deleteLyrics() throws JajukException {
    try {
      tagImpl.deleteLyrics();
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Commit tags.
   * 
   * @throws JajukException the jajuk exception
   */
  public synchronized void commit() throws JajukException {
    try {
      // Show a commit message except if the tag impl is "no tag" (does nothing)
      if (Log.isDebugEnabled() && !(tagImpl.getClass().equals(NoTagsTagImpl.class))) {
        Log.debug(Messages.getString("PropertiesWizard.11") + " {{" + fio.getAbsolutePath() + "}}");
      }
      tagImpl.commit();
      // Display written file full path. Note that we use a limited string for
      // parent to make sure the file name itself is visible in information
      // panel
      InformationJPanel.getInstance().setMessage(
          Messages.getString("PropertiesWizard.11") + " "
              + UtilString.getLimitedString(fio.getParentFile().getAbsolutePath(), 60)
              + File.separatorChar + fio.getName(), InformationJPanel.MessageType.INFORMATIVE);

    } catch (Exception e) {
      // reset information panel to avoid leaving with a "writting xxx message"
      InformationJPanel.getInstance().setMessage("", InformationJPanel.MessageType.INFORMATIVE);
      throw new JajukException(104, fio.getName() + "\n" + e.getMessage(), e);
    }
  }

  /**
   * Checks if is corrupted.
   * 
   * @return true, if is corrupted
   */
  public boolean isCorrupted() {
    return bCorrupted;
  }

  /**
   * Sets the corrupted.
   * 
   * @param corrupted the new corrupted
   */
  public void setCorrupted(boolean corrupted) {
    bCorrupted = corrupted;
  }

  /**
   * Gets the fio.
   * 
   * @return the fio
   */
  public File getFio() {
    return this.fio;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Tag)) {
      return false;
    }
    return this.fio.equals(((Tag) other).getFio());
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return fio.getAbsolutePath().hashCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Tag of : " + fio.getAbsolutePath();
  }

  /**
   * Return cached tag or new tag if non already in cache.
   * 
   * @param fio the audio file containing the tag
   * @param bIgnoreErrors DOCUMENT_ME
   * 
   * @return cached tag or new tag if non already in cache
   * 
   * @throws JajukException the jajuk exception
   * 
   * @bIgnoreError : ignore any error and keep instance in cache
   */
  public static Tag getTagForFio(File fio, boolean bIgnoreErrors) throws JajukException {
    Tag tag = tagsCache.get(fio);
    if (tag == null) {
      tag = new Tag(fio, bIgnoreErrors);
      // Cache the tag
      tagsCache.put(fio, tag);
    }
    return tag;
  }

  /**
   * Clear the tags cache.
   */
  public static void clearCache() {
    tagsCache.clear();
  }

  /**
   * Gets the tag field.
   * 
   * @param tagFieldKey DOCUMENT_ME
   * 
   * @return the tag field
   */
  public String getTagField(String tagFieldKey) {
    try {
      return tagImpl.getTagField(tagFieldKey).trim();
    } catch (Exception e) {
      // this file does not support this tag, so we do nothing
    }
    return "";
  }

  /**
   * Gets the supported tag fields.
   * 
   * @return the supported tag fields
   */
  public static List<String> getSupportedTagFields() {
    if (supportedTagFields == null) {
      supportedTagFields = new ArrayList<String>();

      // get all available tag impls
      List<ITagImpl> tagImplList = new ArrayList<ITagImpl>(2);
      for (Type t : TypeManager.getInstance().getAllMusicTypes()) {
        if (t.getTaggerClass() != null && !tagImplList.contains(t.getTaggerClass())) {
          try {
            tagImplList.add(t.getTaggerClass().newInstance());
          } catch (InstantiationException e) {
            Log.error(e);
          } catch (IllegalAccessException e) {
            Log.error(e);
          }
        }
      }

      for (ITagImpl t : tagImplList) {
        for (String s : t.getSupportedTagFields()) {
          if (!supportedTagFields.contains(s)) {
            supportedTagFields.add(s);
          }
        }
      }
    }

    return supportedTagFields;
  }

  /**
   * Gets the activated extra tags.
   * 
   * @return the activatedExtraTags
   */
  public static List<String> getActivatedExtraTags() {

    List<String> activeExtraTagsArrayList = new ArrayList<String>();

    // check all custom properties
    for (PropertyMetaInformation m : TrackManager.getInstance().getCustomProperties()) {
      if (getSupportedTagFields().contains(m.getName())) {
        activeExtraTagsArrayList.add(m.getName());
      }
    }
    return activeExtraTagsArrayList;
  }
}