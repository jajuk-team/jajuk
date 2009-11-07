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
 *  $Revision: 3132 $
 */
package org.jajuk.services.lyrics.providers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.File;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * 
 */
public class TxtLyricsProvider implements ILyricsProvider {

  private BufferedReader lyricsReader = null;
  private String readerPath = null;
  
  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics(java.lang.String, java.lang.String)
   */
  public String getLyrics(File audioFile) {
    String lyrics = "";
    readerPath = UtilSystem.removeExtension(audioFile.getAbsolutePath()) + ".txt";
    try {
      lyricsReader = getLyricsReader();
      String s = null;
      while ((s = lyricsReader.readLine()) != null) {
        if (!s.startsWith("#")) {
          lyrics += s + "\n";
        }
      }
      lyricsReader.close();
      lyricsReader = null; //So it will be instanced new
    } catch (FileNotFoundException e) {
      Log.debug("Not found approriate lyrics Txt file");
    } catch (IOException e) {
      Log.error(e);
    }
    if (StringUtils.isBlank(lyrics)) {
      return null;
    }    
   return lyrics;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getResponseEncoding()
   */
  public String getResponseEncoding() {
    return "UTF-8";
  }
  
  private BufferedReader getLyricsReader() throws FileNotFoundException {
    if (lyricsReader == null) {    
      lyricsReader = new BufferedReader(new FileReader(readerPath));
    }
    return lyricsReader;
  }

}
