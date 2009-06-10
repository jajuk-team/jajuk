/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.services.lyrics.providers;

import ext.XMLUtils;

import java.net.MalformedURLException;
import java.net.URL;

import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.w3c.dom.Document;

/**
 * Fly (http://www.lyricsfly.com/) lyrics provider <br>
 * Initially written from aTunes's code
 */
public class FlyProvider extends GenericProvider {

  /** The Constant USER_ID. */
  private static final String USER_ID = "79o116n89n93sr93p-wnwhx.vasb";

  /** URL pattern used by jajuk to retrieve lyrics */
  private static final String URL = "http://lyricsfly.com/api/api.php?i="
      + UtilString.rot13(USER_ID) + "&a=%artist&t=%title";
  
  /** URL pattern to web page (see ILyricsProvider interface for details) */
  private static final String WEB_URL = "http://www.lyricsfly.com/";

  /**
   * @param queryUrlTemplate
   */
  public FlyProvider() {
    super(URL);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics(java.lang.String,
   *      java.lang.String)
   */
  public String getLyrics(String artist, String title) {
    String lyrics = null;
    try {
      String xml = callProvider(artist, title);
      Document document = XMLUtils.getDocument(xml);
      lyrics = XMLUtils.getChildElementContent(document.getDocumentElement(), "tx");
      lyrics = lyrics.replace("[br]", "");

      if (UtilString.isVoid(lyrics)) {
        return null;
      }
      return lyrics;
    } catch (Exception e) {
      Log.debug("Cannot fetch lyrics for: " + artist + "/" + title);
    }
    return lyrics;
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
  public URL getWebURL(String artist, String title) {
    URL out = null;
    try {
      // No simple way to access HTML page for given artist and song, we simply
      // return the website URL
      out = new URL(WEB_URL);
    } catch (MalformedURLException e) {
      Log.error(e);
    }
    return out;
  }

}
