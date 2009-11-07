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

import org.jajuk.base.File;

/**
 * Class container of lyrics written by user
 */
public class JajukLyricsProvider {
  private String sLyrics = null;
  private String sArtist = null;
  private String sTitle = null;
  private File audioFile = null;
  
  public JajukLyricsProvider() {
    
  }  

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics(org.jajuk.base.File)
   */
  public String getLyrics() {
    return sLyrics;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getResponseEncoding()
   */
  public String getResponseEncoding() {
    return "UTF-8";
  }

  public String getArtist() {
    return sArtist;
  }
  
  public String getTitle() {
    return sTitle;
  }
  
  public File getFile() {
    return audioFile;
    
  }
  
  public void setLyrics(String sLyrics) {
    this.sLyrics = sLyrics;
  }
  
  public void setFile(File audioFile) {
    this.audioFile = audioFile;
    sArtist = this.audioFile.getTrack().getAuthor().getName2();
    sTitle = this.audioFile.getTrack().getName();
  }

}
