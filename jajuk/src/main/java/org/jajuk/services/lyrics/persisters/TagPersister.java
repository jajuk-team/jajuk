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
package org.jajuk.services.lyrics.persisters;

import org.jajuk.base.File;
import org.jajuk.services.tags.Tag;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Class to read/write lyrics to Tag of Track.
 */
public class TagPersister implements ILyricsPersister {
  /** Audio file to set lyrics to. */
  private File file = null;

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#commitLyrics(String,String,String)
   */
  @Override
  public boolean commitLyrics(String artist, String title, String lyrics) {
    try {
      Tag g = Tag.getTagForFio(file.getFIO(), true);
      g.setLyrics(lyrics);
      return true;
    } catch (JajukException e) {
      Log.error(e);
      Log.warn(e.getMessage());
      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#deleteLyrics()
   */
  @Override
  public boolean deleteLyrics() {
    try {
      Tag g = Tag.getTagForFio(file.getFIO(), true);
      g.deleteLyrics();
      return true;
    } catch (JajukException e) {
      Log.error(e);
      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#getDestinationFile()
   */
  @Override
  public java.io.File getDestinationFile() {
    // For tag persister, destination file is audio file itself
    return file.getFIO();
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#setAudioFile(java.io.File)
   */
  @Override
  public void setAudioFile(File file) {
    this.file = file;
  }
}
