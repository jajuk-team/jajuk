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
 *  $Revision$
 */
package org.jajuk.services.lyrics.providers;

import ext.services.xml.XMLUtils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.w3c.dom.Document;

/**
 * Fly (http://www.lyricsfly.com/) lyrics provider <br>
 * Initially written from aTunes's code
 */
public class FlyWebLyricsProvider extends GenericWebLyricsProvider {

  /** The Constant USER_ID. DO NOT USE THESE KEYS FOR OTHER APPLICATIONS THAN Jajuk ! */
  private static final String USER_ID = "55593623089-wnwhx.vasb";

  /** URL pattern used by jajuk to retrieve lyrics. */
  private static final String URL = "http://api.lyricsfly.com/api/api.php?i="
      + UtilString.rot13(USER_ID) + "&a=%artist&t=%title";

  /** URL pattern to web page (see ILyricsProvider interface for details). */
  private static final String WEB_URL = "http://www.lyricsfly.com/";

  /**
   * The Constructor.
   */
  public FlyWebLyricsProvider() {
    super(URL);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public String getLyrics(String artist, String title) {
    String lyrics = null;
    try {
      String xml = callProvider(artist, title);
      if (StringUtils.isBlank(xml)) {
        Log.debug("No lyrics found for: {{" + artist + "/" + title + "}}");
        return null;
      }
      Document document = XMLUtils.getDocument(xml);
      lyrics = XMLUtils.getChildElementContent(document.getDocumentElement(), "tx");
      // Fix for #1700, seems that in some case, we get <line1>[BR]<line2> without \n
      // so we drop every \n and we replace every [BR] by a \n to make sure to get
      // a single carriage return
      lyrics = lyrics.replace("\n", "");
      lyrics = lyrics.replace("[br]", "\n");
      if (StringUtils.isBlank(lyrics)) {
        return null;
      }
      return lyrics;
    } catch (Exception e) {
      Log.debug("Cannot fetch lyrics for: {{" + artist + "/" + title + "}}");
    }
    return lyrics;
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
   * see org.jajuk.services.lyrics.providers.GenericWebLyricsProvider#getWebURL(java.lang.String, java.lang.String)
   */
  @Override
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

  /* (non-Javadoc)
   * @see org.jajuk.services.lyrics.providers.ILyricsProvider#getLyrics(org.jajuk.base.File)
   */
  @Override
  public String getLyrics() {
    return getLyrics(audioFile.getTrack().getArtist().getName2(), audioFile.getTrack().getName());
  }

}
