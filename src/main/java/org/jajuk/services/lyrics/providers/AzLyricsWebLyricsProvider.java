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

import ext.services.network.NetworkUtils;

import java.net.MalformedURLException;

import org.apache.commons.lang3.StringUtils;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Lyrics Provider extracting lyrics from azlyrics.com
 */
public class AzLyricsWebLyricsProvider extends GenericWebLyricsProvider {
  /** URL pattern used by jajuk to retrieve lyrics. */
  private static final String URL = "http://www.azlyrics.com/lyrics/%artist/%title.html";
  /** URL pattern to web page (see ILyricsProvider interface for details). */
  private static final String WEB_URL = "http://www.azlyrics.com/lyrics/%artist/%title.html";

  /**
   * Instantiates a new azlyrics web lyrics provider.
   */
  public AzLyricsWebLyricsProvider() {
    super(URL);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ext.services.lyrics.providers.GenericProvider#getLyrics(java.lang.String,
   * java.lang.String)
   */
  @Override
  public String getLyrics(final String artist, final String title) {
    try {
      String lyrics = null;
      if (StringUtils.isNotBlank(artist) && StringUtils.isNotBlank(title)) {
        // Specific rule : lowercase, no space, remove "the " par ex for "The Beatles "
        String formattedArtist = artist.toLowerCase().replace(" ", "").replaceFirst("the ", "").replace("/", "").replace("&","");
        // Specific rule : lowercase, no space, no extra signs ()'-,
        String formattedTitle = title.replace(" ", "");
        formattedTitle = formattedTitle.replace("(", "");
        formattedTitle = formattedTitle.replace(")", "");
        formattedTitle = formattedTitle.replace("´", "");
        formattedTitle = formattedTitle.replace("-", "");
        formattedTitle = formattedTitle.replace(",", "");
        formattedTitle = formattedTitle.replace("'", "").toLowerCase();
        String html = callProvider(formattedArtist, formattedTitle);
        if (StringUtils.isNotBlank(html)) {
          // Remove html part
          lyrics = cleanLyrics(html);
        } else {
          Log.debug("Empty return from callProvider().");
        }
      }
      return lyrics;
    } catch (Exception e) {
      Log.debug("Cannot fetch lyrics for: {{" + artist + "/" + title + "}}");
      return null;
    }
  }

  /**
   * Extracts lyrics from the HTML page. The correct subsection is to be
   * extracted first, before being cleaned and stripped from useless HTML tags.
   * 
   * @param html 
   * 
   * @return the lyrics
   */
  private String cleanLyrics(final String html) {
    String ret = null;
    if (html != null) {
      String searchStart = "Sorry about that. -->";
      int startIndex = html.indexOf(searchStart);
      if (startIndex > -1) {
        ret = html.substring(startIndex + searchStart.length());
        int stopIndex = ret.indexOf("</div>");
        if (stopIndex > -1) {
          ret = ret.substring(0, stopIndex);
          ret += "\n<-- AZLyrics -->";
          return cleanHtml(ret);
        } else {
          ret = null;
        }
      }
    }
    return ret;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getResponseEncoding()
   */
  @Override
  public String getResponseEncoding() {
    return "UTF-8";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getWebURL(java.lang .String,
   * java.lang.String)
   */
  @Override
  public java.net.URL getWebURL(final String pArtist, final String pTitle) {
    String queryString = WEB_URL;
    // Replace spaces by _
    String artist = pArtist.replaceAll(" ", "").toLowerCase();
    String title = pTitle.replaceAll(" ", "").replace("(", "").replace(")", "").toLowerCase();
    queryString = queryString.replace(Const.PATTERN_ARTIST,
        (artist != null) ? NetworkUtils.encodeString(artist) : "");
    queryString = queryString.replace(Const.PATTERN_TRACKNAME,
        (title != null) ? NetworkUtils.encodeString(title) : "");
    java.net.URL out = null;
    try {
      out = new java.net.URL(queryString);
    } catch (MalformedURLException e) {
      Log.error(e);
    }
    return out;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics()
   */
  @Override
  public String getLyrics() {
    return getLyrics(audioFile.getTrack().getArtist().getName2(), audioFile.getTrack().getName());
  }
}
