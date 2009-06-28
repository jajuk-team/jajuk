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
 *  $Revision: 3216 $
 */
package org.jajuk.services.tags;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
 * abstract tag, independent from real implementation
 */
public class Tag {

  /** Current tag impl* */
  private ITagImpl tagImpl;

  /** Current file* */
  private File fio;

  /** Is this tag corrupted ? */
  private boolean bCorrupted = false;

  /**
   * File -> tag cache This is required by the autocommit=false operations
   */
  static private Map<File, Tag> tagsCache = new HashMap<File, Tag>(10);

  /**
   * Tag constructor
   * 
   * @bIgnoreError : ignore error and keep instance
   * @param fio
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
        throw new JajukException(103, fio.getName(), e);
      }
    }
  }

  /**
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
   * @return album name
   */
  public String getAlbumName() {
    if (tagImpl == null) { // if the type doesn't support tags ( like wav )
      return Const.UNKNOWN_ALBUM;
    }
    String sAlbumlName = null;

    try {
      String sTemp = tagImpl.getAlbumName().trim();
      if (Messages.getString(Const.UNKNOWN_ALBUM).equals(sTemp)) {
        // it is done to avoid duplicates unknown styles if
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
      if (Boolean.valueOf(Conf.getString(Const.CONF_TAGS_USE_PARENT_DIR)).booleanValue()) {
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
   * @return author name
   */
  public String getAuthorName() {
    String sAuthorName = Const.UNKNOWN_AUTHOR;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return sAuthorName;
    }

    try {
      String sTemp = tagImpl.getAuthorName().trim();
      if (Messages.getString(Const.UNKNOWN_AUTHOR).equals(sTemp)) {
        // it is done to avoid duplicates unknown styles if
        // the tag is the real string "unknown" in the
        // current language
        sAuthorName = Const.UNKNOWN_AUTHOR;
      } else if (!"".equals(sTemp)) {
        sAuthorName = UtilString.formatTag(sTemp);
      }
    } catch (Exception e) {
      Log.info("Wrong author name:{{" + fio.getName() + "}}");
    }
    // We internalize the author name for memory saving reasons
    return sAuthorName.intern();

  }

  /**
   * @return album artist
   */
  public String getAlbumArtist() {
    String sAlbumArtist = "";
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return sAlbumArtist;
    }

    try {
      String sTemp = tagImpl.getAlbumArtist().trim();
      if (Messages.getString(Const.UNKNOWN_AUTHOR).equals(sTemp)) {
        sAlbumArtist = "";
      } else if (!"".equals(sTemp)) {
        sAlbumArtist = UtilString.formatTag(sTemp);
      }
    } catch (Exception e) {
      Log.info("Wrong album artist:{{" + fio.getName() + "}}");
    }
    // We internalize the author name for memory saving reasons
    return sAlbumArtist.intern();

  }

  /**
   * @return style name
   */
  public String getStyleName() {
    String style = Const.UNKNOWN_STYLE;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return style;
    }

    try {
      String sTemp = tagImpl.getStyleName().trim();
      if (Messages.getString(Const.UNKNOWN_STYLE).equals(sTemp)) {
        // it is done to avoid duplicates unknown styles if
        // the tag is the real string "unknown" in the
        // current language
        style = Const.UNKNOWN_STYLE;
      } else if (!"".equals(sTemp)) {
        if ("unknown".equals(sTemp)) {
          sTemp = style;
        }
        style = UtilString.formatTag(sTemp);
      }
    } catch (Exception e) {
      Log.info("Wrong style name:" + fio.getName());
    }
    // We internalize the style name for memory saving reasons
    return style.intern();

  }

  /**
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
      Log.info("Wrong length:" + fio.getName());
    }
    return length;
  }

  /**
   * @return disc number, by default, return 0
   */
  public long getDiscNumber() {
    long l = 0l;
    try {
      l = tagImpl.getDiscNumber();

    } catch (Exception e) {
      // just debug, no warn because wrong order are too often and
      // generate too much traces
      Log.info("Wrong disc number:" + fio.getName());
      l = 01;
    }
    return l;
  }

  /**
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
      Log.info("Wrong year:" + fio.getName());
    }
    // We internalize the year name for memory saving reasons
    return year.intern();

  }

  /**
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
      Log.info("Wrong quality:" + fio.getName());
    }
    return lQuality;
  }

  /**
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
      Log.info("Wrong order:" + fio.getName());
      l = 0;
    }
    return l;
  }

  /**
   * @param sTrackName
   */
  public void setTrackName(String sTrackName) throws JajukException {
    try {
      tagImpl.setTrackName(sTrackName);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * @param sAlbumName
   */
  public void setAlbumName(String sAlbumName) throws JajukException {
    try {
      tagImpl.setAlbumName(sAlbumName);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * @param sAuthorName
   */
  public void setAuthorName(String sAuthorName) throws JajukException {
    try {
      tagImpl.setAuthorName(sAuthorName);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * @param sAlbumArtist
   */
  public void setAlbumArtist(String sAlbumArtist) throws JajukException {
    try {
      tagImpl.setAlbumArtist(sAlbumArtist);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * @param style
   */
  public void setStyleName(String style) throws JajukException {
    try {
      tagImpl.setStyleName(style);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * @param style
   */
  public void setOrder(long lOrder) throws JajukException {
    try {
      tagImpl.setOrder(lOrder);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * @param sYear
   */
  public void setYear(String sYear) throws JajukException {
    try {
      tagImpl.setYear(sYear);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * @param discnumber
   */
  public void setDiscNumber(long discnumber) throws JajukException {
    try {
      tagImpl.setDiscNumber(discnumber);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * @param sComment
   */
  public void setComment(String sComment) throws JajukException {
    try {
      tagImpl.setComment(sComment);
    } catch (Exception e) {
      throw new JajukException(104, fio.getName(), e);
    }
  }

  /**
   * Commit tags
   */
  public synchronized void commit() throws JajukException {
    try {
      // Show a commit message except if the tag impl is "no tag" (does nothing)
      if (Log.isDebugEnabled() && !(tagImpl.getClass().equals(NoTagsTagImpl.class))) {
        Log.debug(Messages.getString("PropertiesWizard.11") + " " + fio.getAbsolutePath());
      }
      tagImpl.commit();
      // Display written file full path. Note that we use a limited string for
      // parent to make sure the file name itself is visible in information
      // panel
      InformationJPanel.getInstance().setMessage(
          Messages.getString("PropertiesWizard.11") + " "
              + UtilString.getLimitedString(fio.getParentFile().getAbsolutePath(), 60)
              + File.separatorChar + fio.getName(), InformationJPanel.INFORMATIVE);

    } catch (Exception e) {
      // reset information panel to avoid leaving with a "writting xxx message"
      InformationJPanel.getInstance().setMessage("", InformationJPanel.INFORMATIVE);
      throw new JajukException(104, fio.getName() + "\n" + e.getMessage(), e);
    }
  }

  public boolean isCorrupted() {
    return bCorrupted;
  }

  public void setCorrupted(boolean corrupted) {
    bCorrupted = corrupted;
  }

  public File getFio() {
    return this.fio;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Tag)) {
      return false;
    }
    return this.fio.equals(((Tag) other).getFio());
  }

  @Override
  public int hashCode() {
    return fio.getAbsolutePath().hashCode();
  }

  @Override
  public String toString() {
    return "Tag of : " + fio.getAbsolutePath();
  }

  /**
   * Return cached tag or new tag if non already in cache
   * 
   * @param fio
   *          the audio file containing the tag
   * @bIgnoreError : ignore any error and keep instance in cache
   * @return cached tag or new tag if non already in cache
   * @throws JajukException
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
   * Clear the tags cache
   */
  public static void clearCache() {
    tagsCache.clear();
  }

}