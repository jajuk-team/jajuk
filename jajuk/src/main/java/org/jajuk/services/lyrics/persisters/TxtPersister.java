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


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Class to read/write lyrics to TXT file.
 */
public class TxtPersister implements ILyricsPersister {

  /** DOCUMENT_ME. */
  private java.io.File lyricsFile = null;

  /** DOCUMENT_ME. */
  private Writer lyricsWriter = null;

  /** Audio file to set lyrics to. */
  private org.jajuk.base.File file = null;

  /* (non-Javadoc)
    * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#commitLyrics(String,String,String)
    */
  @Override
  public boolean commitLyrics(String artist, String title, String lyrics) {
    try {
      lyricsWriter = getLyricsWriter();
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
    }

  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#deleteLyrics()
   */
  @Override
  public boolean deleteLyrics() {
    lyricsFile = getDestinationFile();
    try {
      UtilSystem.deleteFile(lyricsFile);
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
  public java.io.File getDestinationFile() {
    if (lyricsFile == null) {
      lyricsFile = new java.io.File(UtilSystem.removeExtension(file.getAbsolutePath()) + ".txt");
    }
    return lyricsFile;
  }

  /**
   * Gets the lyrics writer.
   * 
   * @return the lyrics writer
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Writer getLyricsWriter() throws IOException {
    lyricsFile = getDestinationFile();
    if (lyricsWriter == null) {
      lyricsWriter = new FileWriter(lyricsFile);
    }
    return lyricsWriter;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#setAudioFile(java.io.File)
   */
  @Override
  public void setAudioFile(org.jajuk.base.File file) {
    this.file = file;
  }

}
