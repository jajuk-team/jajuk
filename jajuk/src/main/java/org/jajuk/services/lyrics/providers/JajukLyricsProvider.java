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

import org.jajuk.base.File;

/**
 * Class container of lyrics written by user from Jajuk GUI itself.
 */
public class JajukLyricsProvider implements ILyricsProvider {
  private String sLyrics = null;
  private String sArtist = null;
  private String sTitle = null;
  private File audioFile = null;

  /**
   * Instantiates a new jajuk lyrics provider.
   */
  public JajukLyricsProvider() {
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics()
   */
  /**
   * Gets the lyrics.
   * 
   * @return the lyrics
   */
  @Override
  public String getLyrics() {
    return sLyrics;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getResponseEncoding()
   */
  /**
   * Gets the response encoding.
   * 
   * @return the response encoding
   */
  @Override
  public String getResponseEncoding() {
    return "UTF-8";
  }

  /**
   * Gets the artist.
   * 
   * @return the artist
   */
  public String getArtist() {
    return sArtist;
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle() {
    return sTitle;
  }

  /**
   * Gets the file.
   * 
   * @return the file
   */
  public File getFile() {
    return audioFile;
  }

  /**
   * Sets the lyrics.
   * 
   * @param sLyrics the new lyrics
   */
  public void setLyrics(String sLyrics) {
    this.sLyrics = sLyrics;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#setAudioFile(org.jajuk.base.File)
   */
  @Override
  public void setAudioFile(File audioFile) {
    this.audioFile = audioFile;
    sArtist = this.audioFile.getTrack().getArtist().getName2();
    sTitle = this.audioFile.getTrack().getName();
  }

  /* (non-Javadoc)
  * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getSourceAddress()
  */
  @Override
  public String getSourceAddress() {
    return "<Jajuk>";
  }
}
