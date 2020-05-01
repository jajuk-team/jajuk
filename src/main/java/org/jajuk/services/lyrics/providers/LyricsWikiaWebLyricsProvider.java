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

import org.apache.commons.lang3.StringUtils;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

import ext.services.network.NetworkUtils;

/**
 * Lyrics Provider extracting lyrics from lyrics.fandom.com (former lyrics.wikia.com)
 */
public class LyricsWikiaWebLyricsProvider extends GenericWebLyricsProvider {
  /** URL pattern used by jajuk to retrieve lyrics. */
  private static final String URL = "https://lyrics.fandom.com/api.php?action=lyrics&artist=%artist&song=%title";
  /** URL pattern to web page (see ILyricsProvider interface for details). */
  private static final String WEB_URL = "https://lyrics.fandom.com/api.php?action=lyrics&artist=%artist&song=%title";

  /**
   * Instantiates a new lyric wiki web lyrics provider.
   */
  public LyricsWikiaWebLyricsProvider() {
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
        // This provider waits for '_' instead of regular '+' for spaces in URL
        String formattedArtist = artist.replaceAll(" ", "_");
        String formattedTitle = title.replaceAll(" ", "_").replace(",", "_");
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
      int startIndex = html.indexOf("<pre>");
      if (startIndex > -1) {
        ret = html.substring(startIndex + 5);
        int stopIndex = ret.indexOf("</pre>");
        if (stopIndex > -1) {
          ret = ret.substring(0, stopIndex);
          if (ret.length()<15) {
            return null;
          } else {
            ret += "\n<-- LyricsFandom (former LyricsWikia) -->";
        	ret = cleanHtml(ret);
          }
        }
      } else {
        ret = null;
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
    String artist = pArtist.replaceAll(" ", "_");
    String title = pTitle.replaceAll(" ", "_");
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
