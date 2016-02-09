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
package org.jajuk.services.lyrics.providers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.File;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Lyrics provide from text file in the same directory of the audio file.
 */
public class TxtLyricsProvider implements ILyricsProvider {
  private BufferedReader lyricsReader = null;
  private String readerPath = null;
  /** audio file we search lyrics for. */
  private File audioFile = null;

  /**
   * {@inheritDoc}
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics()
   */
  @Override
  public String getLyrics() {
    readerPath = UtilSystem.removeExtension(audioFile.getAbsolutePath()) + ".txt";
    if (!new java.io.File(readerPath).exists()) {
      Log.debug("Lyrics Txt file not found, can not read lyrics for Txt-Provider");
      return null;
    }
    try {
      String lyrics = "";
      lyricsReader = getLyricsReader();
      String s = null;
      while ((s = lyricsReader.readLine()) != null) {
        if (!s.startsWith("#")) {
          lyrics += s + "\n";
        }
      }
      lyricsReader.close();
      lyricsReader = null; // So it will be instanced new
      if (StringUtils.isBlank(lyrics)) {
        return null;
      }
      return lyrics;
    } catch (FileNotFoundException e) {
      Log.debug("Not found approriate lyrics Txt file");
      return null;
    } catch (IOException e) {
      Log.error(e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getResponseEncoding()
   */
  @Override
  public String getResponseEncoding() {
    return "UTF-8";
  }

  /**
   * Gets the lyrics reader.
   * 
   * @return the lyrics reader
   * 
   * @throws FileNotFoundException the file not found exception
   */
  private BufferedReader getLyricsReader() throws FileNotFoundException {
    if (lyricsReader == null) {
      lyricsReader = new BufferedReader(new FileReader(readerPath));
    }
    return lyricsReader;
  }

  /**
   * {@inheritDoc}
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#setAudioFile(org.jajuk.base.File)
   */
  @Override
  public void setAudioFile(File file) {
    this.audioFile = file;
  }

  /**
   * {@inheritDoc}
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getSourceAddress()
   */
  @Override
  public String getSourceAddress() {
    return UtilSystem.removeExtension(audioFile.getAbsolutePath()) + ".txt";
  }
}
