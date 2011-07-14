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
 *  $Revision$
 */

package org.jajuk.services.lyrics.persisters;

import org.jajuk.base.File;

/**
 * Interface for lyrics persisters to be used by the modified LyricsService.
 */
public interface ILyricsPersister {

  /**
   * Sets the audio file to set lyrics to.
   * 
   * @param file the new audio file
   */
  void setAudioFile(File file);

  /**
   * Commit lyrics for a given filename <br>
   * Returns true if commited correctly, false otherwise.
   *
   * @param artist DOCUMENT_ME
   * @param title DOCUMENT_ME
   * @param lyrics lyrics as a string
   * @return true if OK, false otherwise
   */
  boolean commitLyrics(String artist, String title, String lyrics);

  /**
   * Deletes Lyrics that user has saved <br>
   * in Tag or in a Txt file.
   * Returns true if deleted correctly, false otherwise.
   * 
   * @return true, if delete lyrics
   */
  boolean deleteLyrics();

  /**
   * Gets the destination file.
   * 
   * @return the destination file
   */
  java.io.File getDestinationFile();

}
