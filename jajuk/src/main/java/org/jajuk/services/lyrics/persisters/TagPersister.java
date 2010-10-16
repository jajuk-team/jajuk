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
package org.jajuk.services.lyrics.persisters;

import org.jajuk.services.lyrics.providers.JajukLyricsProvider;
import org.jajuk.services.tags.Tag;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Class to read/write lyrics to Tag of Track.
 */
public class TagPersister implements ILyricsPersister {

  /** DOCUMENT_ME. */
  private JajukLyricsProvider provider = null;
  
  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#commitLyrics(java.lang.String, org.jajuk.base.File)
   */
  @Override
  public boolean commitLyrics(JajukLyricsProvider iProvider) {
    provider = iProvider;
    
    try {
      Tag g = Tag.getTagForFio(provider.getFile().getFIO(), true);
      g.setLyrics(provider.getLyrics());
      return true;
    } catch (JajukException e) {
      Log.error(e);
      Log.warn(e.getMessage());
      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#deleteLyrics(org.jajuk.services.lyrics.providers.ILyricsProvider)
   */
  @Override
  public void deleteLyrics(JajukLyricsProvider provider) {
    try {
      Tag g = Tag.getTagForFio(provider.getFile().getFIO(), true);
      g.deleteLyrics();
    } catch (JajukException e) {
      Log.error(e);
    }    
  }

}
