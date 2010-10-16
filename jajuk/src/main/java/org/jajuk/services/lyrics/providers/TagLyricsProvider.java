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
package org.jajuk.services.lyrics.providers;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.File;
import org.jajuk.services.tags.Tag;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * DOCUMENT_ME.
 */
public class TagLyricsProvider implements ILyricsProvider {
  
  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics(java.lang.String, java.lang.String)
   */
  @Override
  public String getLyrics(File audioFile) {
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

}
