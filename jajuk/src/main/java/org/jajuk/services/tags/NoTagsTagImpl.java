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
import java.util.List;
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import org.jajuk.services.covers.Cover;
import org.jajuk.util.UtilFeatures;

/**
 * Tagger implementation for formats without tags and read by BasicPlayer API.
 */
public class NoTagsTagImpl implements ITagImpl {

  /** Analyzed file. */
  private File fio;

  /** Current file data. */
  private Map<String, Object> mapInfo;

  /** DOCUMENT_ME. */
  private static List<String> tagFieldKeyArrayList = new ArrayList<String>();

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getTrackName()
   */
  @Override
  public String getTrackName() {
    return ""; // doing that, the item wil be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getAlbumName()
   */
  @Override
  public String getAlbumName() {
    return ""; // doing that, the item will be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getArtistName()
   */
  @Override
  public String getArtistName() {
    return ""; // doing that, the item will be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getGenreName()
   */
  @Override
  public String getGenreName() {
    return ""; // doing that, the item will be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getLength()
   */
  @Override
  public long getLength() throws BasicPlayerException {
    // we have to open the file to get length
    BasicPlayer player = new BasicPlayer();
    player.addBasicPlayerListener(new BasicPlayerListener() {
      @Override
      @SuppressWarnings({ "unchecked", "rawtypes" })
      public void opened(Object arg0, Map mProperties) {
        NoTagsTagImpl.this.mapInfo = mProperties;
      }

      @Override
      @SuppressWarnings({ "rawtypes", "unchecked" })
      public void progress(int iBytesread, long lMicroseconds, byte[] bPcmdata, Map mProperties) {
        // required by interface, but nothing to do here...
      }

      @Override
      public void stateUpdated(BasicPlayerEvent bpe) {
        // required by interface, but nothing to do here...
      }

      @Override
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
  @Override
  public String getComment() {
    return ""; // by doing that, the item will be the default jajuk
    // unknown string
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setTrackName(java.lang.String)
   */
  @Override
  public void setTrackName(String sTrackName) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setAlbumName(java.lang.String)
   */
  @Override
  public void setAlbumName(String sAlbumName) {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setComment(java.lang.String)
   */
  @Override
  public void setComment(String sComment) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setArtistName(java.lang.String)
   */
  @Override
  public void setArtistName(String sArtistName) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setGenreName(java.lang.String)
   */
  @Override
  public void setGenreName(String genre) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setFile(java.io.File)
   */
  @Override
  public void setFile(File fio) {
    this.fio = fio;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.tag.ITagImpl#commit()
   */
  @Override
  public void commit() {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.tag.ITagImpl#getOrder()
   */
  @Override
  public long getOrder() {
    return 0l;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.tag.ITagImpl#setOrder(java.lang.String)
   */
  @Override
  public void setOrder(long lOrder) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.tag.ITagImpl#setYear(int)
   */
  @Override
  public void setYear(String year) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.tag.ITagImpl#getYear()
   */
  @Override
  public String getYear() {
    return "0";
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.tag.ITagImpl#getQuality()
   */
  @Override
  public long getQuality() {
    return 0l;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getAlbumArtist()
   */
  @Override
  public String getAlbumArtist() {
    return "";
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getDiscNumber()
   */
  @Override
  public long getDiscNumber() {
    return 01;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setAlbumArtist(java.lang.String)
   */
  @Override
  public void setAlbumArtist(String albumArtist) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setDiscNumber(int)
   */
  @Override
  public void setDiscNumber(long discnumber) {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#deleteLyrics()
   */
  @Override
  public void deleteLyrics() throws Exception {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#getLyrics()
   */
  @Override
  public String getLyrics() throws Exception {
    return "";
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.services.tags.ITagImpl#setLyrics(java.lang.String)
   */
  @Override
  public void setLyrics(String sLyrics) throws Exception {
    // nothing to do here
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

  /* (non-Javadoc)
   * @see org.jajuk.services.tags.ITagImpl#getSupportedTagFields()
   */
  @Override
  public List<String> getSupportedTagFields() {
    return tagFieldKeyArrayList;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.tags.ITagImpl#getCovers()
   */
  @Override
  public List<Cover> getCovers() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
