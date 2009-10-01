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
 *  
 *  Comes initially from aTunes 1.8
 */

package org.jajuk.services.lyrics.providers;

import ext.services.network.NetworkUtils;

import java.net.MalformedURLException;

import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Lyrics Provider extracting lyrics from lyricwiki.org
 * 
 */
public class LyricWikiProvider extends GenericProvider {

  /** URL pattern used by jajuk to retrieve lyrics */
  private static final String URL = "http://lyricwiki.org/%artist:%title";

  /** URL pattern to web page (see ILyricsProvider interface for details) */
  private static final String WEB_URL = "http://lyricwiki.org/%artist:%title";

  public LyricWikiProvider() {
    super(URL);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.services.lyrics.providers.GenericProvider#getLyrics(java.lang.String,
   *      java.lang.String)
   */
  public String getLyrics(final String artist, final String title) {
    try {
      // This provider waits for '_' instead of regular '+' for spaces in URL
      String formattedArtist = artist.replaceAll(" ", "_");
      String formattedTitle = title.replaceAll(" ", "_");
      String html = callProvider(formattedArtist, formattedTitle);
      if (html == null || html.indexOf("") == -1) {
        return null;
      }
      return cleanLyrics(html);
    } catch (Exception e) {
      Log.debug("Cannot fetch lyrics for: {{" + artist + "/" + title + "}}");
      return null;
    }
  }

  /**
   * Extracts lyrics from the HTML page. The correct subsection is to be
   * extracted first, before being cleaned and stripped from useless HTML tags.
   * 
   * @return the lyrics
   */
  private String cleanLyrics(final String html) {
    String ret = html;
    if (ret.contains("<div class='lyricbox' >")) {
      int startIndex = html.indexOf("<div class='lyricbox' >");
      ret = html.substring(startIndex + 23);
      int stopIndex = ret.indexOf("<!--");
      ret = ret.substring(0, stopIndex);
      ret = ret.replaceAll("<br />", "\n");
      ret = ret.replaceAll("&#8217;", "'");
      ret = ret.replaceAll("&#8211;", "-");
      ret = ret.replaceAll("\u0092", "'");
      ret = ret.replaceAll("\u009c", "oe");
      ret = ret.replaceAll("<p>", "\n");
      ret = ret.replaceAll("<i>", "");
      ret = ret.replaceAll("</i>", "");
      ret = ret.replaceAll("<b>", "");
      ret = ret.replaceAll("</b>", "");
      return ret;

    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getResponseEncoding()
   */
  public String getResponseEncoding() {
    return "UTF-8";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getWebURL(java.lang.String,
   *      java.lang.String)
   */
  public java.net.URL getWebURL(final String pArtist, final String pTitle) {
    String queryString = WEB_URL;
    // Replace spaces by _
    String artist = pArtist.replaceAll(" ", "_");
    String title = pTitle.replaceAll(" ", "_");

    queryString = queryString.replace(Const.PATTERN_AUTHOR, (artist != null) ? NetworkUtils
        .encodeString(artist) : "");
    queryString = queryString.replace(Const.PATTERN_TRACKNAME, (title != null) ? NetworkUtils
        .encodeString(title) : "");

    java.net.URL out = null;
    try {
      out = new java.net.URL(queryString);
    } catch (MalformedURLException e) {
      Log.error(e);
    }
    return out;
  }

}
