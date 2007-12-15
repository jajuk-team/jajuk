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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.jajuk.util.DownloadManager;
import org.jajuk.util.log.Log;

/**
 * Lyrics Provider extracting lyrics from www.lyrc.ar
 */
public class LyrcProvider extends GenericProvider {

  private static final String suggestionsURL = "http://www.lyrc.com.ar/en/";

  /**
   * @param querySource
   */
  public LyrcProvider(final String querySource) {
    super("http://www.lyrc.com.ar/en/tema1en.php?artist=%artist&songname=%title");
  }

  private StringTokenizer getTokenizer(final String source) {
    return (new StringTokenizer((source != null) ? source : "", " '?!-,"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.services.lyrics.providers.GenericProvider#getLyrics(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public String getLyrics(final String artist, final String title) {
    String html = super.getLyrics(artist, title);
    if (html == null) {
      return null;
    } else if (html.contains("Suggestions : <br>")) {
      final HashMap<String, String> suggestions = new HashMap<String, String>();
      final ArrayList<String> tokensToFind = new ArrayList<String>();
      final StringTokenizer artistTokens = getTokenizer(artist);
      final StringTokenizer titleTokens = getTokenizer(title);

      // More than one posibility, find the best one
      html = html.substring(html.indexOf("Suggestions : <br>"));
      html = html.substring(0, html.indexOf("<br><br"));

      // Find suggestions and add to a map
      while (html.indexOf("href=\"") != -1) {
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
      for (final String suggestion : suggestions.keySet()) {
        boolean matches = true;

        for (final String token : tokensToFind) {
          if (!suggestion.toLowerCase().contains(token.toLowerCase())) {
            matches = false;
            break;
          }
        }
        if (matches) {
          final String suggestionURL = suggestionsURL.concat(suggestions.get(suggestion));

          Log.debug("Found suggestion " + suggestion);
          try {
            final URL url = new URL(suggestionURL);

            return (cleanLyrics(DownloadManager.downloadHtml(url, "ISO-8859-1")));
          } catch (final MalformedURLException e) {
            Log.warn("Invalid lyrics source URL [" + suggestionURL + "]");
          } catch (final Exception e) {
            Log.warn("Could not retrieve URL [" + suggestionURL + "]");
          }
        }
      }
      Log.debug("No suitable suggestion found");
      return (null);
    }
    return (cleanLyrics(html));
  }

  private int getTagPosition(final String html, final String tag) {
    int pos = Integer.MAX_VALUE;

    if ((html != null) && (tag != null)) {
      pos = html.indexOf(tag);
      if (pos == -1) {
        pos = Integer.MAX_VALUE;
      }
    }
    return (pos);
  }

  /**
   * Extracts lyrics from the HTML page. The correct subsection is to be
   * extracted first, before being cleaned and stripped from useless HTML tags.
   * 
   * @return the lyrics
   */
  private String cleanLyrics(String html) {
    int pPos = Integer.MAX_VALUE;
    int brPos = Integer.MAX_VALUE;

    html = html.substring(html.indexOf("</table>") + 8);
    pPos = getTagPosition(html, "<p>");
    brPos = getTagPosition(html, "<br>");
    html = html.substring(0, (pPos < brPos) ? pPos : brPos);
    html = html.replaceAll("<br />", "");
    if (html.contains("<head>")) {
      return (null);
    }
    html = html.replaceAll("&#8217;", "'");
    html = html.replaceAll("&#8211;", "-");
    html = html.replaceAll("\u0092", "'");
    html = html.replaceAll("\u009c", "oe");
    return (html);
  }

  /**
   * Returns true if a string is composed only by letters
   */
  private static boolean validToken(final String token) {
    return (token.matches("[A-Za-z0-9]+"));
  }

}
