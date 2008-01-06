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
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import org.jajuk.util.Util;

/**
 * Tager implementationfor formats without tags and read by BasicPlayer API
 */
public class NoTagsTagImpl implements ITagImpl {
  /** Analysed file */
  File fio;

  /** Current file data */
  Map mapInfo;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#getTrackName()
   */
  public String getTrackName() throws Exception {
    return ""; // doing that, the item wil be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#getAlbumName()
   */
  public String getAlbumName() throws Exception {
    return ""; // doing that, the item wil be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#getAuthorName()
   */
  public String getAuthorName() throws Exception {
    return ""; // doing that, the item wil be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#getStyleName()
   */
  public String getStyleName() throws Exception {
    return ""; // doing that, the item wil be the default jajuk unknown
    // string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#getLength()
   */
  public long getLength() throws Exception {
    // we have to open the file to get length
    BasicPlayer player = new BasicPlayer();
    player.addBasicPlayerListener(new BasicPlayerListener() {
      public void opened(Object arg0, Map mProperties) {
        NoTagsTagImpl.this.mapInfo = mProperties;
      }

      public void progress(int iBytesread, long lMicroseconds, byte[] bPcmdata,
          java.util.Map mProperties) {
      }

      public void stateUpdated(BasicPlayerEvent bpe) {
      }

      public void setController(BasicController arg0) {
      }
    });
    if (fio != null) {
      player.open(fio);
      return Util.getTimeLengthEstimation(mapInfo) / 1000;
    }
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#getComment()
   */
  public String getComment() throws Exception {
    return ""; // by doing that, the item wil be the default jajuk
    // unknown string
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#setTrackName(java.lang.String)
   */
  public void setTrackName(String sTrackName) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#setAlbumName(java.lang.String)
   */
  public void setAlbumName(String sAlbumName) throws Exception {
  }

  public void setComment(String sComment) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#setAuthorName(java.lang.String)
   */
  public void setAuthorName(String sAuthorName) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#setStyleName(java.lang.String)
   */
  public void setStyleName(String style) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#setLength(long)
   */
  public void setLength(long length) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#setQuality(java.lang.String)
   */
  public void setQuality(String sQuality) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ITagImpl#setFile(java.io.File)
   */
  public void setFile(File fio) throws Exception {
    this.fio = fio;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#getTagItem(java.lang.String)
   */
  public String getTagItem(String sTagItem) throws Exception {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#setTagItem(java.lang.String, java.lang.String)
   */
  public void setTagItem(String sTagItem, String sValue) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#commit()
   */
  public void commit() throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#getOrder()
   */
  public long getOrder() throws Exception {
    return 0l;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#setOrder(java.lang.String)
   */
  public void setOrder(long lOrder) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#setYear(int)
   */
  public void setYear(String year) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#getYear()
   */
  public String getYear() throws Exception {
    return "0";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.tag.ITagImpl#getQuality()
   */
  public long getQuality() throws Exception {
    return 0l;
  }

}
