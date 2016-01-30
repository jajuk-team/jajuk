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
package org.jajuk.services.lyrics;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.services.lyrics.providers.AzLyricsWebLyricsProvider;
import org.jajuk.services.lyrics.providers.GenericWebLyricsProvider;
import org.jajuk.services.lyrics.providers.ILyricsProvider;
import org.jajuk.services.lyrics.providers.LyricsManiaWebLyricsProvider;
import org.jajuk.services.lyrics.providers.LyricsWikiaWebLyricsProvider;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.log.Log;

/**
 * Lyrics unit tests.
 */
public class TestLyrics extends JajukTestCase {
  /** The Constant ARTIST.   */
  private static final String ARTIST = "Massive Attack";
  // LyricsFly put a delay of 1500 ms before we are allowed to query again, we
  // need to take that into account for some of the tests
  /** The Constant FLY_DELAY.   */
  private static final long FLY_DELAY = 1500 + 200;
  /** The Constant TESTED_WORD.   */
  private static final String TESTED_WORD = "Day, yesterday";
  /** The Constant TITLE.   */
  private static final String TITLE = "Dissolved Girl";
  private File tmp = null;

  @Override
  public void specificSetUp() throws Exception {
    // to first cover this method while no providers are loaded yet
    LyricsService.getProviders();
    tmp = TestHelpers.getFile("test.tmp", true).getFIO();
  }

  /**
   * Test AZLyrics provider response to get lyrics.
   */
  public void testAZLyricsWebProvider() {
    GenericWebLyricsProvider provider = new AzLyricsWebLyricsProvider();
    testWebService(provider);
  }

  /**
   * Test Lyricsmania provider response to get lyrics.
   */
  public void testLyricsManiaWeb() throws Exception {
    GenericWebLyricsProvider provider = new LyricsManiaWebLyricsProvider();
    testWeb(provider);
  }

  /**
   * Test Lyricsmania provider response to get lyrics.
   */
  public void testLyricsManiaWebService() {
    GenericWebLyricsProvider provider = new LyricsManiaWebLyricsProvider();
    testWebService(provider);
  }

  /**
   * Test LyricsWikia provider response to get lyrics.
   */
  public void testLyricsWikiaService() {
    GenericWebLyricsProvider provider = new LyricsWikiaWebLyricsProvider();
    testWebService(provider);
  }

  /**
   * Test LyricWiki web url availability.
   *
   * @throws Exception the exception
   */
  public void testLyricsWikiaWeb() throws Exception {
    GenericWebLyricsProvider provider = new LyricsWikiaWebLyricsProvider();
    testWeb(provider);
  }

  // helper method to emma-coverage of the unused constructor
  /**
   * Test private constructor.
   * 
   *
   * @throws Exception the exception
   */
  public void testPrivateConstructor() throws Exception {
    // For EMMA code-coverage tests
    TestHelpers.executePrivateConstructor(LyricsService.class);
  }

  /**
   * Test provider loading.
   */
  public void testProvidersLoading() {
    LyricsService.loadProviders();
    List<ILyricsProvider> providers = LyricsService.getProviders();
    assertNotNull(providers);
    assertFalse(providers.size() == 0);
  }

  /**
   * Test provider web site url (shared code).
   *
   * @param provider 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void testWeb(GenericWebLyricsProvider provider) throws IOException {
    URL url = provider.getWebURL(ARTIST, TITLE);
    assertNotNull(url);
    try {
      DownloadManager.download(url, tmp);
    } catch (SocketTimeoutException e) {
      Log.fatal("In Sonar this exception occurs, seems we do not have internet access there...");
      return;
    }
    assertTrue(tmp.exists());
    assertTrue(tmp.length() > 0);
  }

  /**
   * Test provider response to get lyrics (shared code).
   *
   * @param provider 
   */
  private void testWebService(GenericWebLyricsProvider provider) {
    String lyrics = provider.getLyrics(ARTIST, TITLE);
    Log.debug("Resulting Lyrics(" + provider.getProviderHostname() + "): " + lyrics);
    if (provider.getProviderHostname().equals("api.lyricsfly.com") && lyrics == null) {
      Log.fatal("In Sonar this can happen, seems we do not have internet access there...");
      return;
    }
    assertTrue("Lyrics(" + provider.getProviderHostname() + "): " + lyrics,
        StringUtils.isNotBlank(lyrics));
    assertTrue("Lyrics(" + provider.getProviderHostname() + "): " + lyrics,
        lyrics.indexOf(TESTED_WORD) != -1);
  }
}
