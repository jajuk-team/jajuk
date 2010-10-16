/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

package org.jajuk.services.lyrics.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.jajuk.base.File;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.log.Log;

/**
 * Lyrics Provider extracting lyrics from www.lyrc.ar
 */
public class LyrcWebLyricsProvider extends GenericWebLyricsProvider {

  /** The Constant URL.  DOCUMENT_ME */
  private static final String URL = "http://www.lyrc.com.ar/en/tema1en.php?artist=%artist&songname=%title";

  /**
   * Instantiates a new lyrc web lyrics provider.
   */
  public LyrcWebLyricsProvider() {
    super(URL);
  }

  /**
   * Gets the tokenizer.
   * 
   * @param source DOCUMENT_ME
   * 
   * @return the tokenizer
   */
  private StringTokenizer getTokenizer(final String source) {
    return new StringTokenizer((source != null) ? source : "", " '?!-,");
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.services.lyrics.providers.GenericProvider#getLyrics(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public String getLyrics(final String artist, final String title) {
    String html = callProvider(artist, title);
    if (html == null) {
      return null;
    } else if (html.contains("Suggestions : <br>")) {
      final Map<String, String> suggestions = new HashMap<String, String>();
      final List<String> tokensToFind = new ArrayList<String>();
      final StringTokenizer artistTokens = getTokenizer(artist);
      final StringTokenizer titleTokens = getTokenizer(title);

      // More than one possibility, find the best one
      html = html.substring(html.indexOf("Suggestions : <br>"));
      html = html.substring(0, html.indexOf("<br><br"));

      // Find suggestions and add to a map
      while (html.contains("href=\"")) {
        // from html tag <a href="....
        String text = html.substring(html.indexOf("'white'>") + 8);
        // from font color='white'>...
        String uri = html.substring(html.indexOf("href=\"") + 6);

        uri = uri.substring(0, uri.indexOf("\">"));
        text = text.substring(0, text.indexOf("</font>"));
        suggestions.put(text, uri);
        // Skip element
        html = html.substring(html.indexOf("</font>") + 11);
      }

      // Get tokens from artist and song names
      while (artistTokens.hasMoreTokens()) {
        final String token = artistTokens.nextToken();

        if (validToken(token)) {
          tokensToFind.add(token);
        }
      }
      while (titleTokens.hasMoreTokens()) {
        final String token = titleTokens.nextToken();

        if (validToken(token)) {
          tokensToFind.add(token);
        }
      }

      // Now find at map, a string that contains all artist and song
      // tokens. This will be the selected lyric
      for (final Map.Entry<String, String> suggestion : suggestions.entrySet()) {
        boolean matches = true;

        for (final String token : tokensToFind) {
          if (!suggestion.getKey().toLowerCase(Locale.getDefault()).contains(token.toLowerCase(Locale.getDefault()))) {
            matches = false;
            break;
          }
        }
        if (matches) {
          final String suggestionURL = URL.concat(suggestion.getValue());
          Log.debug("Found suggestion {{" + suggestion.getKey() + "}}");
          try {
            final URL url = new URL(suggestionURL);
            String text = DownloadManager.getTextFromCachedFile(url, getResponseEncoding());
            return cleanLyrics(text);
          } catch (final MalformedURLException e) {
            Log.warn("Invalid lyrics source URL {{" + suggestionURL + "}}", e.getMessage());
          } catch (final Exception e) {
            Log.warn("Could not retrieve URL {{" + suggestionURL + "}}", e.getMessage());
          }
        }
      }
      Log.debug("No suitable suggestion found");
      return null;
    }
    return cleanLyrics(html);
  }

  /**
   * Gets the tag position.
   * 
   * @param html DOCUMENT_ME
   * @param tag DOCUMENT_ME
   * 
   * @return the tag position
   */
  private int getTagPosition(final String html, final String tag) {
    int pos = Integer.MAX_VALUE;

    if ((html != null) && (tag != null)) {
      pos = html.indexOf(tag);
      if (pos == -1) {
        pos = Integer.MAX_VALUE;
      }
    }
    return pos;
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
    int pPos = Integer.MAX_VALUE;
    int brPos = Integer.MAX_VALUE;
    if (ret.contains("</table>")) {
      ret = ret.substring(ret.indexOf("</table>") + 8);
      pPos = getTagPosition(ret, "<p>");
      brPos = getTagPosition(ret, "<br>");
      ret = ret.substring(0, (pPos < brPos) ? pPos : brPos);
      ret = ret.replaceAll("<br />", "");
      if (ret.contains("<head>")) {
        return null;
      }
      ret = ret.replaceAll("&#8217;", "'");
      ret = ret.replaceAll("&#8211;", "-");
      ret = ret.replaceAll("\u0092", "'");
      ret = ret.replaceAll("\u009c", "oe");
    }
    return ret;
  }

  /**
   * Returns true if a string is composed only by letters.
   * 
   * @param token DOCUMENT_ME
   * 
   * @return true, if valid token
   */
  private static boolean validToken(final String token) {
    return token.matches("[A-Za-z0-9]+");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getResponseEncoding()
   */
  @Override
  public String getResponseEncoding() {
    return "ISO-8859-1";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getWebURL(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public URL getWebURL(String artist, String title) {
    // for this provider, the web url equals the web url
    return getActualURL(artist, title);
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics(org.jajuk.base.File)
   */
  @Override
  public String getLyrics(File audioFile) {
    return getLyrics(audioFile.getTrack().getArtist().getName2(), 
        audioFile.getTrack().getName());
  }

}
