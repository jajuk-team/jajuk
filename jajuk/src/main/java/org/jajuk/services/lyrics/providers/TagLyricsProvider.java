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
 *  
 */
package org.jajuk.services.lyrics.providers;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.File;
import org.jajuk.services.tags.Tag;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * .
 */
public class TagLyricsProvider implements ILyricsProvider {
  /** audio file we search lyrics for. */
  private File audioFile = null;

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics()
   */
  @Override
  public String getLyrics() {
    String lyrics = null;
    try {
      Tag g = Tag.getTagForFio(audioFile.getFIO(), true);
      lyrics = g.getLyrics();
      if (StringUtils.isBlank(lyrics)) {
        return null;
      }
    } catch (JajukException e) {
      Log.error(e);
      Log.warn(e.getMessage());
      return null;
    }
    return lyrics;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getResponseEncoding()
   */
  @Override
  public String getResponseEncoding() {
    return "UTF-8";
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#setAudioFile(org.jajuk.base.File)
   */
  @Override
  public void setAudioFile(File file) {
    this.audioFile = file;
  }

  /* (non-Javadoc)
  * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getSourceAddress()
  */
  @Override
  public String getSourceAddress() {
    return audioFile.getAbsolutePath();
  }
}
