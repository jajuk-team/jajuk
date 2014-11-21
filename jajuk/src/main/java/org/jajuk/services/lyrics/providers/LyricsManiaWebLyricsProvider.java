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
import java.text.Normalizer;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Lyrics Provider extracting lyrics from lyricsmania.com
 */
public class LyricsManiaWebLyricsProvider extends GenericWebLyricsProvider {
  /** URL pattern used by jajuk to retrieve lyrics. */
  private static final String URL = "http://www.lyricsmania.com/%title_lyrics_%artist.html";
  /** URL pattern to web page (see ILyricsProvider interface for details). */
  private static final String WEB_URL = "http://www.lyricsmania.com/%title_lyrics_%artist.html";

  /**
   * Instantiates a new lyricsmania web lyrics provider.
   */
  public LyricsManiaWebLyricsProvider() {
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
      String formattedArtist = removeAccent(artist).replace(" ", "_").toLowerCase();
      String formattedTitle = title.replace(" ", "_").replace("(", "").replace(")", "")
          .replace("'", "").toLowerCase();
      String html = callProvider(formattedArtist, formattedTitle);
      if (StringUtils.isBlank(html)) {
        Log.debug("Empty return from callProvider().");
        return null;
      }
      // Remove html part
      String result = cleanLyrics(html);
      if (result == null) {
        // Another trial without accent
        formattedTitle = removeAccent(title).replaceAll(" ", "_").replace("(", "")
            .replace(")", "").replace("'", "").toLowerCase();
        html = callProvider(formattedArtist, formattedTitle);
        if (StringUtils.isBlank(html)) {
          Log.debug("Empty return from callProvider().");
          return null;
        }
        result = cleanLyrics(html);
      }
      return result;
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
    String ret = html;
    String searchString = "<div class=\"lyrics-body\"";
    if (ret.contains(searchString)) {
      int startIndex = html.indexOf("<div class=\"lyrics-body\"");
      ret = html.substring(startIndex + searchString.length());
      int secondIndex = ret.indexOf("</strong>");
      ret = ret.substring(secondIndex + 10);
      int stopIndex = ret.indexOf("</div>");
      ret = ret.substring(0, stopIndex);
      ret = ret.replaceAll("\t", "");
      ret = ret.replaceAll("&#146;", "'");
      ret = ret.replaceAll("&#133;", "…");
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

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics()
   */
  @Override
  public String getLyrics() {
    return getLyrics(audioFile.getTrack().getArtist().getName2(), audioFile.getTrack().getName());
  }

  public String removeAccent(String s) {
    String strTemp = Normalizer.normalize(s, Normalizer.Form.NFD);
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    return pattern.matcher(strTemp).replaceAll("");
  }
}
