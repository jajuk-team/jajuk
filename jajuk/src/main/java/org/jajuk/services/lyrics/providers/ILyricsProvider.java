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
 * Interface for lyrics providers to be used by the modified LyricsService.
 */
public interface ILyricsProvider {

  /**
   * Return lyrics for given audio file <br>
   * Returns null if none lyrics found or technical error.
   * 
   * 
   * @return the lyrics
   */
  String getLyrics();

  /**
   * Return lyrics provider response encoding (ISO8859-1, UTF-8..)
   * 
   * @return lyrics provider response encoding (ISO8859-1, UTF-8..)
   */
  String getResponseEncoding();

  /**
   * Sets the audio file to search lyrics for.
   * 
   * @param file the audio file
   */
  void setAudioFile(File file);

  /**
   * Gets the lyrics source address.
   * <p>
   * We don't use an URL here as it's better for users to get paths for further search in files explorers
   * 
   * @return the lyrics source address
   */
  String getSourceAddress();

}
