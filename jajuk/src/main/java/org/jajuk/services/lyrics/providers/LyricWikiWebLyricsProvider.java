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

import ext.services.network.NetworkUtils;

import java.net.MalformedURLException;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Lyrics Provider extracting lyrics from lyricwiki.org
 */
public class LyricWikiWebLyricsProvider extends GenericWebLyricsProvider {

  /** URL pattern used by jajuk to retrieve lyrics. */
  private static final String URL = "http://lyrics.wikia.com/%artist:%title";

  /** URL pattern to web page (see ILyricsProvider interface for details). */
  private static final String WEB_URL = "http://lyrics.wikia.com/%artist:%title";

  /**
   * Instantiates a new lyric wiki web lyrics provider.
   */
  public LyricWikiWebLyricsProvider() {
    super(URL);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.services.lyrics.providers.GenericProvider#getLyrics(java.lang.String,
   * java.lang.String)
   */
  @Override
  public String getLyrics(final String artist, final String title) {
    try {
      // This provider waits for '_' instead of regular '+' for spaces in URL
      String formattedArtist = artist.replaceAll(" ", "_");
      String formattedTitle = title.replaceAll(" ", "_");
      String html = callProvider(formattedArtist, formattedTitle);
      if (StringUtils.isBlank(html)) {
        Log.debug("Empty return from callProvider().");
        return null;
      }
      // Remove html part
      html = cleanLyrics(html);
      // From oct 2009, lyrics wiki returns lyrics encoded as HTML chars
      // like &#83;&#104;&#97; ...
      StringBuffer sbFinalHtml = new StringBuffer(1000);
      StringTokenizer st = new StringTokenizer(html, "&#");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        // Remove trailing ';'
        if (token.endsWith("\n")) {
          String trailing = token.substring(token.indexOf(';') + 1);
          token = token.substring(0, token.indexOf(';'));
          sbFinalHtml.append((char) Integer.parseInt(token, 10));
          // Re-add carriage returns
          sbFinalHtml.append(trailing);
        } else {
          token = token.substring(0, token.length() - 1);
          sbFinalHtml.append((char) Integer.parseInt(token, 10));
        }
      }
      return sbFinalHtml.toString();
    } catch (Exception e) {
      Log.debug("Cannot fetch lyrics for: {{" + artist + "/" + title + "}}");
      return null;
    }
  }

  /**
   * Extracts lyrics from the HTML page. The correct subsection is to be
   * extracted first, before being cleaned and stripped from useless HTML tags.
   * 
   * @param html DOCUMENT_ME
   * 
   * @return the lyrics
   */
  private String cleanLyrics(final String html) {
    String ret = html;
    // LyricWiki uses this with and without blank sometimes, maybe we should use
    // a regular expression instead...
    if (ret.contains("<div class='lyricbox' >") || ret.contains("<div class='lyricbox'>")) {
      int startIndex = html.indexOf("<div class='lyricbox' >");
      if (startIndex == -1) {
        startIndex = html.indexOf("<div class='lyricbox'>");
        ret = html.substring(startIndex + 22);

        // LyricWiki added some additional div class now...
        if (ret.startsWith("<div class='rtMatcher'>")) {
          startIndex = ret.indexOf("</div>");
          ret = ret.substring(startIndex + 6);
        }
      } else {
        ret = html.substring(startIndex + 23);
      }
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
  @Override
  public String getResponseEncoding() {
    return "UTF-8";
  }

  /*
   * (non-Javadoc)
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

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics()
   */
  @Override
  public String getLyrics() {
    return getLyrics(audioFile.getTrack().getArtist().getName2(), audioFile.getTrack().getName());
  }

}
