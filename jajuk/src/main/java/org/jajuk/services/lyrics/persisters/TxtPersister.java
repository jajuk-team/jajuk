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

import org.jajuk.services.lyrics.providers.JajukLyricsProvider;
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
  
  /** DOCUMENT_ME. */
  private JajukLyricsProvider provider = null;
  
  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#commitLyrics(java.lang.String, java.io.File)
   */
  @Override
  public boolean commitLyrics(JajukLyricsProvider iProvider) {
    provider = iProvider;
    try {
      lyricsWriter = getLyricsWriter();      
      lyricsWriter.write("# This is a Jajuk generated lyrics file\n");
      lyricsWriter.write("# Artist:\t" + provider.getArtist() + "\n");
      lyricsWriter.write("# Title:\t" + provider.getTitle() + "\n#");
      lyricsWriter.write("\n" + provider.getLyrics() + "\n");
      lyricsWriter.close();
      lyricsWriter = null;
      System.out.println("POTSON2 ");
      return true;
    } catch (Exception e) {
      Log.error(e);
      try {
        UtilSystem.deleteFile(lyricsFile);
      } catch (IOException e1) {
        Log.error(e1);
      }
      lyricsFile = null;
      return false;
    }
    
  }
  
  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.persisters.ILyricsPersister#deleteLyrics(org.jajuk.services.lyrics.providers.JajukLyricsProvider)
   */
  @Override
  public void deleteLyrics(JajukLyricsProvider jProvider) throws IOException{
    provider = jProvider;
    lyricsFile = getLyricsFile();
    UtilSystem.deleteFile(lyricsFile);    
  }

  /**
   * Gets the lyrics file.
   * 
   * @return the lyrics file
   */
  private java.io.File getLyricsFile() {
    if (lyricsFile == null) {
      lyricsFile = new java.io.File(UtilSystem.removeExtension(
          provider.getFile().getAbsolutePath()) + ".txt");
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
    lyricsFile = getLyricsFile();
    if (lyricsWriter == null) {
      lyricsWriter = new FileWriter(lyricsFile);
    }
    return lyricsWriter;
  }

}
