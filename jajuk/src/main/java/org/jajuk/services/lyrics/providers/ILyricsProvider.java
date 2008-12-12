/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.services.lyrics.providers;

/**
 * Interface for lyrics providers to be used by the modified LyricsService
 */
public interface ILyricsProvider {

  /**
   * Return query URL template like http://..?artist=%artist&songname=%title
   * 
   * @return query URL template like http://..?artist=%artist&songname=%title
   */
  String getQueryURLTemplate();

  /**
   * Return the hostname of the lyrics provider, used as unique identifier for
   * the provider
   * 
   * @return
   */
  String getProviderHostname();

  /**
   * Return lyrics for a given artist and title <br>
   * Returns null if none lyrics found or technical error
   * 
   * @param artist
   * @param title
   * @return lyrics for a given artist and title
   */
  String getLyrics(final String artist, final String title);

  /**
   * Return lyrics provider response encoding (ISO8859-1, UTF-8..)
   * 
   * @return lyrics provider response encoding (ISO8859-1, UTF-8..)
   */
  String getResponseEncoding();

  /**
   * Return the URL from where the lyrics can be displayed from out of Jajuk
   * <br>
   * Note that this URL can be different from the jajuk used url for example if
   * a provider provides a web service interface (jajuk then uses the
   * corresponding URL) and a Web page (this is this URL that is returned from
   * this method)
   * 
   * @return the Web URL or null if a problem occurred
   */
  java.net.URL getWebURL(String artist, String title);

}
