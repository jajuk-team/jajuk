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

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.services.lyrics.providers.GenericWebLyricsProvider;
import org.jajuk.services.lyrics.providers.ILyricsProvider;
import org.jajuk.services.lyrics.providers.LyricWikiWebLyricsProvider;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.log.Log;

/**
 * Lyrics unit tests
 */
public class TestLyrics extends JajukTestCase {

  private final File tmp = new File("test.tmp");
  private static final String ARTIST = "Massive Attack";
  private static final String TITLE = "Dissolved Girl";
  private static final String TESTED_WORD = "Day, yesterday";

  // LyricsFly put a delay of 1500 ms before we are allowed to query again, we
  // need to take that into account for some of the tests
  private static final long FLY_DELAY = 1500 + 200;

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

  /**
   * Test provider web site url (shared code)
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
   * Test LyricWiki provider response to get lyrics
   */
  public void testLyricWikiService() {
    GenericWebLyricsProvider provider = new LyricWikiWebLyricsProvider();
    testWebService(provider);
  }

  /**
   * Test LyricWiki web url availability
   */
  public void testLyricWikiWeb() throws Exception {
    GenericWebLyricsProvider provider = new LyricWikiWebLyricsProvider();
    testWeb(provider);
  }

}
