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

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * abstract tag, independent from real implementation
 */
public class Tag implements ITechnicalStrings {

  /** Current tag impl* */
  private ITagImpl tagImpl;

  /** Current file* */
  private File fio;

  /** Is this tag corrupted ? */
  private boolean bCorrupted = false;

  /**
   * Tag constructor
   * 
   * @param fio
   */
  public Tag(java.io.File fio) throws JajukException {
    this(fio, false);
  }

  /**
   * Tag constructor
   * 
   * @bIgnoreError : ignore errror and keep instance
   * @param fio
   */
  public Tag(java.io.File fio, boolean bIgnoreErrors) throws JajukException {
    try {
      this.fio = fio;
      Type type = TypeManager.getInstance().getTypeByExtension(Util.getExtension(fio));
      tagImpl = type.getTagImpl();
      tagImpl.setFile(fio);
      bCorrupted = false;
    } catch (Exception e) {
      bCorrupted = true;
      if (!bIgnoreErrors)
        throw new JajukException(103, fio.getName(), e);
    }
  }

  /**
   * @return track name as defined in tags are file name otherwise
   */
  public String getTrackName() {
    // by default, track name is the file name without extension
    String sTrackName = Util.removeExtension(fio.getName()).intern();
    if (tagImpl == null) { // if the type doesn't support tags ( like wav )
      return sTrackName;
    }
    String sTemp = "".intern();
    try {
      sTemp = tagImpl.getTrackName().trim().intern();
      if (!"".equals(sTemp)) {
        sTrackName = Util.formatTag(sTemp).intern(); // remove the
        // extension
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
      return UNKNOWN_ALBUM;
    }
    String sAlbumlName = null;
    String sTemp = "".intern();
    try {
      sTemp = tagImpl.getAlbumName().trim().intern();
      if (Messages.getString(UNKNOWN_ALBUM).equals(sTemp)) {
        // it is done to avoid duplicates unknown styles if
        // the tag is the real string "unknown" in the
        // current language
        sAlbumlName = UNKNOWN_ALBUM;
      } else if (!"".equals(sTemp)) {
        sAlbumlName = sTemp;
      }
    } catch (Exception e) {
      Log.info("Wrong album name:{{" + fio.getName() + "}}");
    }
    if (sAlbumlName == null) { // album tag cannot be found
      if (Boolean.valueOf(ConfigurationManager.getProperty(CONF_TAGS_USE_PARENT_DIR))
          .booleanValue()) {
        sAlbumlName = fio.getParentFile().getName().intern();
        // if album is not found, take current directory as album name
      } else {
        sAlbumlName = Messages.getString(UNKNOWN_ALBUM);
        // unknwon album
      }
    }
    sAlbumlName = Util.formatTag(sAlbumlName).intern();
    return sAlbumlName;
  }

  /**
   * @return author name
   */
  public String getAuthorName() {
    String sAuthorName = UNKNOWN_AUTHOR;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return sAuthorName;
    }
    String sTemp = "".intern();
    try {
      sTemp = tagImpl.getAuthorName().trim().intern();
      if (Messages.getString(UNKNOWN_AUTHOR).equals(sTemp)) {
        // it is done to avoid duplicates unknown styles if
        // the tag is the real string "unknown" in the
        // current language
        sAuthorName = UNKNOWN_AUTHOR;
      } else if (!"".equals(sTemp)) {
        sAuthorName = Util.formatTag(sTemp).intern();
      }
    } catch (Exception e) {
      Log.info("Wrong author name:{{" + fio.getName() + "}}");
    }
    return sAuthorName;

  }

  /**
   * @return style name
   */
  public String getStyleName() {
    String style = UNKNOWN_STYLE;
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return style;
    }
    String sTemp = "".intern();
    try {
      sTemp = tagImpl.getStyleName().trim().intern();
      if (Messages.getString(UNKNOWN_STYLE).equals(sTemp)) {
        // it is done to avoid duplicates unknown styles if
        // the tag is the real string "unknown" in the
        // current language
        style = UNKNOWN_STYLE;
      } else if (!"".equals(sTemp)) {
        if (sTemp.equals("unknown")) {
          sTemp = style;
        }
        style = Util.formatTag(sTemp).intern();
      }
    } catch (Exception e) {
      Log.info("Wrong style name:" + fio.getName());
    }
    return style;

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
    return year;

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
    String sComment = "".intern();
    // if the type doesn't support tags ( like wav )
    if (tagImpl == null) {
      return sComment;
    }
    String sTemp = "".intern();
    try {
      sTemp = tagImpl.getComment().intern();
      if (sTemp != null && !sTemp.equals("")) {
        sComment = Util.formatTag(sTemp).intern();
      }
    } catch (Exception e) {
      Log.info("Wrong comment:{{" + fio.getName() + "}}");
    }
    return sComment;
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
  public void commit() throws JajukException {
    try {
      tagImpl.commit();
      InformationJPanel.getInstance().setMessage(
          Messages.getString("PropertiesWizard.11") + " " + fio.getName(),
          InformationJPanel.INFORMATIVE);
      if (Log.isDebugEnabled()) {
        Log.debug(Messages.getString("PropertiesWizard.11") + " " + fio.getName());
      }
    } catch (Exception e) {
      throw new JajukException(104, fio.getName() + "\n" + e.getMessage(), e);
    }
  }

  public boolean isCorrupted() {
    return bCorrupted;
  }

  public void setCorrupted(boolean corrupted) {
    bCorrupted = corrupted;
  }

}