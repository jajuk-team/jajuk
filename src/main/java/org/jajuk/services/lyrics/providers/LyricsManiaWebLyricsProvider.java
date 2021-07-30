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

import java.net.MalformedURLException;
import java.text.Normalizer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

import ext.services.network.NetworkUtils;

/**
 * Lyrics Provider extracting lyrics from lyricsmania.com
 */
public class LyricsManiaWebLyricsProvider extends GenericWebLyricsProvider {
  /** URL pattern used by jajuk to retrieve lyrics. */
  private static final String URL = "https://www.lyricsmania.com/%title_lyrics_%artist.html";
  /** URL pattern to web page (see ILyricsProvider interface for details). */
  private static final String WEB_URL = "https://www.lyricsmania.com/%title_lyrics_%artist.html";

  /**
   * Instantiates a new lyricsmania web lyrics provider.
   */
  public LyricsManiaWebLyricsProvider() {
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
        // Specific rule for artist : lowercase, no space, no accent 
        String formattedArtist = removeAccent(artist).replace(" ", "_").toLowerCase();
        // Specific rule for title : lowercas, no space with accent
        String formattedTitle = title.replace(" ", "_").replace("(", "").replace(")", "");
        formattedTitle = formattedTitle.replace("'", "").toLowerCase();
        String html = callProvider(formattedArtist, formattedTitle);
        String result = cleanLyrics(html);
        if (StringUtils.isNotBlank(result)) {
          lyrics = result;
        } else {
            // Another trial without accent on title
            formattedTitle = removeAccent(title).replaceAll(" ", "_").replace("(", "");
            formattedTitle = formattedTitle.replace(")", "").replace("'", "").toLowerCase();
            html = callProvider(formattedArtist, formattedTitle);
            if (StringUtils.isNotBlank(html)) {
              lyrics = cleanLyrics(html);  
            } else {
              Log.debug("Empty return from callProvider().");
            }
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
      String searchString = "<div class=\"lyrics-body\"";
      int startIndex = html.indexOf(searchString);
      if (startIndex > -1) {
        ret = html.substring(startIndex + searchString.length());
        int secondIndex = ret.indexOf("</div>");
        if (secondIndex > -1) {
          ret = ret.substring(secondIndex + 7);
          int stopIndex = ret.indexOf("</div>");
          ret = ret.substring(0, stopIndex);
          ret = ret.replace('\r', '\n');
          ret = ret.replace("<div class=\"p402_premium\">", "");
          ret += "\n<-- LyricsMania -->";
          ret = cleanHtml(ret);
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
    String artist = removeAccent(pArtist).replaceAll(" ", "").toLowerCase();
    String title = removeAccent(pTitle).replaceAll(" ", "").replace("(", "").replace(")", "")
        .toLowerCase();
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

  /**
   * Replace each accent in the string with the non accent character.
   * @param s the string to process
   * @return the string without accents
   */
  public String removeAccent(String s) {
    String strTemp = Normalizer.normalize(s, Normalizer.Form.NFD);
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    return pattern.matcher(strTemp).replaceAll("");
  }
}
