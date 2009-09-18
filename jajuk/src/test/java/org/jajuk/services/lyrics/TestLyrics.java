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
package org.jajuk.services.lyrics;

import ext.XMLUtils;
import ext.services.network.NetworkUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.services.lyrics.providers.FlyProvider;
import org.jajuk.services.lyrics.providers.ILyricsProvider;
import org.jajuk.services.lyrics.providers.LyrcProvider;
import org.jajuk.services.lyrics.providers.LyricWikiProvider;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.w3c.dom.Document;

/**
 * Lyrics unit tests
 */
public class TestLyrics extends TestCase {

  private final File tmp = new File("test.tmp");
  private static final String ARTIST = "Massive Attack";
  private static final String TITLE = "Dissolved Girl";
  private static final String TESTED_WORD = "Day, yesterday";

  // helper method to emma-coverage of the unused constructor
  public void testPrivateConstructor() throws Exception {
    // For EMMA code-coverage tests
    JUnitHelpers.executePrivateConstructor(LyricsService.class);
  }

  /**
   * Test setup
   */
  @Override
  public void setUp() throws IOException {
    if (tmp.exists()) {
      tmp.delete();
    }

    JUnitHelpers.createSessionDirectory();

    // to first cover this method while no providers are loaded yet
    LyricsService.getProviders();
  }

  /**
   * Test provider loading
   */
  public void testProvidersLoading() {
    LyricsService.loadProviders();
    List<ILyricsProvider> providers = LyricsService.getProviders();
    assertNotNull(providers);
    assertFalse(providers.size() == 0);
  }

  /**
   * Test provider response to get lyrics (shared code)
   */
  private void testService(ILyricsProvider provider) {
    String lyrics = provider.getLyrics(ARTIST, TITLE);
    assertTrue("Lyrics: " + lyrics, StringUtils.isNotBlank(lyrics)
        && lyrics.indexOf(TESTED_WORD) != -1);
  }

  /**
   * Test provider web site url (shared code)
   */
  private void testWeb(ILyricsProvider provider) throws IOException {
    URL url = provider.getWebURL(ARTIST, TITLE);
    assertNotNull(url);
    DownloadManager.download(url, tmp);

    assertTrue(tmp.exists());
    assertTrue(tmp.length() > 0);
  }

  /**
   * Test Lyrc provider response to get lyrics
   * 
   * public void testLyrcService() { ILyricsProvider provider = new
   * LyrcProvider(); testService(provider); }
   */

  /**
   * Test Lyrc web url availability
   * 
   * public void testLyrcWeb() { ILyricsProvider provider = new LyrcProvider();
   * testWeb(provider); }
   */

  /**
   * Test Fly provider response to get lyrics
   */
  public void testFlyService() {
    ILyricsProvider provider = new FlyProvider();
    testService(provider);
  }

  private static final String USER_ID = "55593623089-wnwhx.vasb";

  /** URL pattern used by jajuk to retrieve lyrics */
  private static final String URL = "http://lyricsfly.com/api/api.php?i="
      + UtilString.rot13(USER_ID) + "&a=%artist&t=%title";

  public void testFlyServiceSonar() throws Exception {
    // ensure that this is not configured somehow
    assertFalse(Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS));

    // do some in-depth test here to find out why this fails in Sonar

    String queryString = URL;

    queryString = queryString.replace(Const.PATTERN_AUTHOR, (ARTIST != null) ? NetworkUtils
        .encodeString(ARTIST) : "");

    queryString = queryString.replace(Const.PATTERN_TRACKNAME, (TITLE != null) ? NetworkUtils
        .encodeString(TITLE) : "");

    URL url = new URL(queryString);

    Log.info("Downloading: " + url);

    String xml = null;
    //xml = DownloadManager.getTextFromCachedFile(url, "UTF-8");
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    assertFalse(Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS));

    File file = DownloadManager.downloadToCache(url);
    assertNotNull(file);
    StringBuilder builder = new StringBuilder();
    InputStream input = new BufferedInputStream(new FileInputStream(file));
    try {
      byte[] array = new byte[1024];
      int read;
      while ((read = input.read(array)) > 0) {
        builder.append(new String(array, 0, read, "UTF-8"));
      }
    } finally {
      input.close();
    }
    xml = builder.toString();
    assertTrue(xml, StringUtils.isNotBlank(xml));

    Document document = XMLUtils.getDocument(xml);
    assertNotNull(document);

    String lyrics = null;
    lyrics = XMLUtils.getChildElementContent(document.getDocumentElement(), "tx");
    lyrics = lyrics.replace("[br]", "");

    assertTrue(StringUtils.isNotBlank(lyrics));

  }

  /**
   * Test Fly web url availability
   */
  public void testFlyWeb() throws Exception {
    ILyricsProvider provider = new FlyProvider();
    testWeb(provider);
  }

  /**
   * Test LyricWiki provider response to get lyrics
   */
  public void testLyricWikiService() throws Exception {
    ILyricsProvider provider = new LyricWikiProvider();
    testService(provider);
  }

  /**
   * Test LyricWiki web url availability
   */
  public void testLyricWikiWeb() throws Exception {
    ILyricsProvider provider = new LyricWikiProvider();
    testWeb(provider);
  }

  /**
   * Test providers order For each provider, we test the class and then we
   * remove it from the providers list to allow the others to run
   */
  public void testProvidersOrder() {
    LyricsService.getLyrics(ARTIST, TITLE);
    assertTrue("Instance: " + LyricsService.getCurrentProvider().getClass(), LyricsService
        .getCurrentProvider() instanceof LyricWikiProvider);

    LyricsService.getProviders().remove(0);
    LyricsService.getLyrics(ARTIST, TITLE);
    assertTrue("Instance: " + LyricsService.getCurrentProvider().getClass(), LyricsService
        .getCurrentProvider() instanceof FlyProvider);

    LyricsService.getProviders().remove(0);
    LyricsService.getLyrics(ARTIST, TITLE);
    assertTrue("Instance: " + LyricsService.getCurrentProvider().getClass(), LyricsService
        .getCurrentProvider() instanceof LyrcProvider);
  }

}
