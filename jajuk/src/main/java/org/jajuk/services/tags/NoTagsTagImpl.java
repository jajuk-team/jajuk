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
import java.util.ArrayList;
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import org.jajuk.util.UtilFeatures;

/**
 * Tagger implementation for formats without tags and read by BasicPlayer API
 */
public class NoTagsTagImpl implements ITagImpl {
  /** Analyzed file */
  File fio;

  /** Current file data */
  Map<String, Object> mapInfo;

  private static ArrayList<String> supportedTags = new ArrayList<String>();

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getTrackName()
   */
  public String getTrackName() {
    return ""; // doing that, the item wil be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getAlbumName()
   */
  public String getAlbumName() {
    return ""; // doing that, the item will be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getAuthorName()
   */
  public String getAuthorName() {
    return ""; // doing that, the item will be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getStyleName()
   */
  public String getStyleName() {
    return ""; // doing that, the item will be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getLength()
   */
  public long getLength() throws BasicPlayerException {
    // we have to open the file to get length
    BasicPlayer player = new BasicPlayer();
    player.addBasicPlayerListener(new BasicPlayerListener() {
      @SuppressWarnings("unchecked")
      public void opened(Object arg0, Map mProperties) {
        NoTagsTagImpl.this.mapInfo = mProperties;
      }

      @SuppressWarnings("unchecked")
      public void progress(int iBytesread, long lMicroseconds, byte[] bPcmdata, Map mProperties) {
        // required by interface, but nothing to do here...
      }

      public void stateUpdated(BasicPlayerEvent bpe) {
        // required by interface, but nothing to do here...
      }

      public void setController(BasicController arg0) {
        // required by interface, but nothing to do here...
      }
    });
    if (fio != null) {
      player.open(fio);
      return UtilFeatures.getTimeLengthEstimation(mapInfo) / 1000;
    }
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getComment()
   */
  public String getComment() {
    return ""; // by doing that, the item will be the default jajuk
    // unknown string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setTrackName(java.lang.String)
   */
  public void setTrackName(String sTrackName) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setAlbumName(java.lang.String)
   */
  public void setAlbumName(String sAlbumName) {
    // required by interface, but nothing to do here...
  }

  public void setComment(String sComment) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setAuthorName(java.lang.String)
   */
  public void setAuthorName(String sAuthorName) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setStyleName(java.lang.String)
   */
  public void setStyleName(String style) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setFile(java.io.File)
   */
  public void setFile(File fio) {
    this.fio = fio;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#commit()
   */
  public void commit() {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#getOrder()
   */
  public long getOrder() {
    return 0l;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#setOrder(java.lang.String)
   */
  public void setOrder(long lOrder) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#setYear(int)
   */
  public void setYear(String year) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#getYear()
   */
  public String getYear() {
    return "0";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#getQuality()
   */
  public long getQuality() {
    return 0l;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getAlbumArtist()
   */
  public String getAlbumArtist() {
    return "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getDiscNumber()
   */
  public long getDiscNumber() {
    return 01;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setAlbumArtist(java.lang.String)
   */
  public void setAlbumArtist(String albumArtist) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setDiscNumber(int)
   */
  public void setDiscNumber(long discnumber) {
    // nothing to do here
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.tags.ITagImpl#deleteLyrics()
   */
  @Override
  public void deleteLyrics() throws Exception {
    // nothing to do here
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.tags.ITagImpl#getLyrics()
   */
  @Override
  public String getLyrics() throws Exception {
    return "";
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.tags.ITagImpl#setLyrics(java.lang.String)
   */
  @Override
  public void setLyrics(String sLyrics) throws Exception {
    // nothing to do here    
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getSupportedTagFields()
   */
  @Override
  public ArrayList<String> getSupportedTagFields() {
    return supportedTags;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#getTagField(java.lang.String)
   */
  @Override
  public String getTagField(String tagFieldKey) throws Exception {
    return "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.tags.ITagImpl#setTagField(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void setTagField(String tagFieldKey, String tagFieldValue) {
    return;
  }

}
