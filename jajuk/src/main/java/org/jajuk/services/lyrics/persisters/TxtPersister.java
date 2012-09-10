/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Class to read/write lyrics to TXT file.
 */
public class TxtPersister implements ILyricsPersister {
  /** Audio file to set lyrics to. */
  private org.jajuk.base.File file = null;

  /* (non-Javadoc)
    * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#commitLyrics(String,String,String)
    */
  @Override
  public boolean commitLyrics(String artist, String title, String lyrics) {
    File lyricsFile = getDestinationFile();
    Writer lyricsWriter = null;
    try {
      lyricsWriter = new FileWriter(lyricsFile);
      lyricsWriter.write("# This is a Jajuk generated lyrics file\n");
      lyricsWriter.write("# Artist:\t" + artist + "\n");
      lyricsWriter.write("# Title:\t" + title + "\n#");
      lyricsWriter.write("\n" + lyrics + "\n");
      lyricsWriter.close();
      lyricsWriter = null;
      return true;
    } catch (Exception e) {
      Log.error(e);
      try {
        if (lyricsFile.exists()) {
          UtilSystem.deleteFile(lyricsFile);
        }
      } catch (IOException e1) {
        Log.error(e1);
      }
      lyricsFile = null;
      return false;
    } finally {
      if (lyricsWriter != null) {
        try {
          lyricsWriter.flush();
          lyricsWriter.close();
        } catch (IOException e) {
          Log.error(e);
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#deleteLyrics()
   */
  @Override
  public boolean deleteLyrics() {
    try {
      UtilSystem.deleteFile(getDestinationFile());
      return true;
    } catch (IOException e) {
      Log.error(e);
      return false;
    }
  }

  /**
   * Gets the lyrics file.
   * 
   * @return the lyrics file
   */
  @Override
  public java.io.File getDestinationFile() {
    return new java.io.File(UtilSystem.removeExtension(file.getAbsolutePath()) + ".txt");
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#setAudioFile(java.io.File)
   */
  @Override
  public void setAudioFile(org.jajuk.base.File file) {
    this.file = file;
  }
}
