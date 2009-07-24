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
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.jajuk.JUnitHelpers;
import org.jajuk.services.lyrics.providers.FlyProvider;
import org.jajuk.services.lyrics.providers.ILyricsProvider;
import org.jajuk.services.lyrics.providers.LyrcProvider;
import org.jajuk.services.lyrics.providers.LyricWikiProvider;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * Lyrics unit tests
 */
public class TestLyrics extends TestCase {

  private final File tmp = new File("test.tmp");
  private static final String artist = "Massive Attack";
  private static final String title = "Dissolved Girl";

  /**
   * Test setup
   */
  @Override
  public void setUp() throws IOException {
    if (tmp.exists()) {
      tmp.delete();
    }
    
    JUnitHelpers.createSessionDirectory();
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
    String lyrics = provider.getLyrics(artist, title);
    assertFalse("Lyrics: " + lyrics, UtilString.isVoid(lyrics));
  }

  /**
   * Test provider web site url (shared code)
   */
  private void testWeb(ILyricsProvider provider) {
    URL url = provider.getWebURL(artist, title);
    assertNotNull(url);
    try {
      DownloadManager.download(url, tmp);
    } catch (IOException e) {
      Log.error(e);
      fail();
    }
    assertTrue(tmp.exists());
    assertTrue(tmp.length() > 0);
  }

  /**
   * Test Lyrc provider response to get lyrics
   *
  public void testLyrcService() {
    ILyricsProvider provider = new LyrcProvider();
    testService(provider);
  }*/

  /**
   * Test Lyrc web url availability
   *
  public void testLyrcWeb() {
    ILyricsProvider provider = new LyrcProvider();
    testWeb(provider);
  }*/

  /**
   * Test Fly provider response to get lyrics
   */
  public void testFlyService() {
    ILyricsProvider provider = new FlyProvider();
    testService(provider);
  }

  /**
   * Test Fly web url availability
   */
  public void testFlyWeb() {
    ILyricsProvider provider = new FlyProvider();
    testWeb(provider);
  }

  /**
   * Test LyricWiki provider response to get lyrics
   */
  public void testLyricWikiService() {
    ILyricsProvider provider = new LyricWikiProvider();
    testService(provider);
  }

  /**
   * Test LyricWiki web url availability
   */
  public void testLyricWikiWeb() {
    ILyricsProvider provider = new LyricWikiProvider();
    testWeb(provider);
  }

  /**
   * Test providers order For each provider, we test the class and then we
   * remove it from the providers list to allow the others to run
   */
  public void testProvidersOrder() {
    LyricsService.getLyrics(artist, title);
    assertTrue("Instance: " + LyricsService.getCurrentProvider().getClass(), 
        LyricsService.getCurrentProvider() instanceof LyricWikiProvider);

    LyricsService.getProviders().remove(0);
    LyricsService.getLyrics(artist, title);
    assertTrue(LyricsService.getCurrentProvider() instanceof FlyProvider);

    LyricsService.getProviders().remove(0);
    LyricsService.getLyrics(artist, title);
    assertTrue(LyricsService.getCurrentProvider() instanceof LyrcProvider);
  }

}
