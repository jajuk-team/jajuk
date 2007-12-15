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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.jajuk.util.DownloadManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 * GenericProvider is a basic processor for web-based lyrics providers. It
 * doesn't provide fine-grained processing and simply retrieves raw text data
 * from HTML pages.
 * 
 * The GenericProvider is used as a base class by other, more fine-grained
 * specific providers.
 */
public class GenericProvider implements IProvider {

  private String source = null;
  private String querySource = null;

  public GenericProvider(final String querySource) {
    this.querySource = querySource;
  }

  public String getQueryString(final String artist, final String title) {
    String queryString = getQuerySource();

    try {
      queryString = queryString.replace(ITechnicalStrings.PATTERN_AUTHOR,
          (artist != null) ? URLEncoder.encode(artist, "ISO-8859-1") : "");
      queryString = queryString.replace(ITechnicalStrings.PATTERN_TRACKNAME,
          (title != null) ? URLEncoder.encode(title, "ISO-8859-1") : "");
    } catch (final UnsupportedEncodingException e) {
      Log.warn("Could not URL encode artist {{" + artist + "}} and song title {{" + title + "}}");
    }
    return (queryString);
  }

  protected URL getQueryURL(final String artist, final String title) {
    try {
      return (new URL(getQueryString(artist, title)));
    } catch (final MalformedURLException e) {
      Log.warn("Invalid lyrics provider [" + querySource + "]");
    }
    return (null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.services.lyrics.providers.IProvider#getLyrics(java.lang.String,
   *      java.lang.String)
   */
  public String getLyrics(final String artist, final String title) {
    final URL url = getQueryURL(artist, title);
    String text = null;

    if (url != null) {
      try {
        text = DownloadManager.downloadHtml(url, "ISO-8859-1");
      } catch (final Exception e) {
        Log.warn("Could not retrieve URL [" + url + "]");
      }
    }
    return (text);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.services.lyrics.providers.IProvider#getQuerySource()
   */
  public String getQuerySource() {
    return (querySource);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.services.lyrics.providers.IProvider#getSource()
   */
  public String getSource() {
    if (source == null) {
      try {
        source = new URL(querySource).getHost();
      } catch (final MalformedURLException e) {
        Log.warn("Invalid lyrics provider [" + querySource + "]");
      }
    }
    return (source);
  }

}
