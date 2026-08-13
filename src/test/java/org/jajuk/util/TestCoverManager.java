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
package org.jajuk.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.jajuk.JajukTestCase;

import java.net.URL;
import java.util.List;

public class TestCoverManager extends JajukTestCase {
  /** The Constant ARTIST. */
  private static final String ARTIST = "Massive Attack";
  /** The Constant ALBUM. */
  private static final String ALBUM = "Collected";

  /**
   * Test method for {@link org.jajuk.util.CoverManager#getRemoteCoversList(List, int)}.
   *
   * @throws Exception the exception
   *                   {@link org.jajuk.util.CoverManager#getRemoteCoversList(List, int)}.
   */
  @Test
  public void testGetRemoteCoversList() throws Exception {
    List<String> artistsParts = List.of(ARTIST, ALBUM);
    List<URL> urls = CoverManager.getRemoteCoversList(artistsParts, 5);
    assertFalse(urls.isEmpty());
    assertTrue(urls.size() <= 5);
    // Act & Assert
    for (int i = 0; i < urls.size(); i++) {
      assertNotNull(urls.get(i), "URL at index " + i + " must not be null");

      String urlStr = urls.get(i).toString();
      assertTrue(urlStr.startsWith("http://") || urlStr.startsWith("https://"), "URL at index " + i + " must use http or https protocol: " + urlStr);
    }
  }

  @Test
  public void testFetchFromWikimedia() throws Exception {
    List<URL> urls = CoverManager.fetchFromWikimedia("Ben Harper", "The Will to Live", 5);
    assertFalse(urls.isEmpty());
    assertTrue(urls.size() <= 5);
    // Act & Assert
    for (int i = 0; i < urls.size(); i++) {
      assertNotNull(urls.get(i), "URL at index " + i + " must not be null");

      String urlStr = urls.get(i).toString();
      assertTrue(urlStr.startsWith("http://") || urlStr.startsWith("https://"), "URL at index " + i + " must use http or https protocol: " + urlStr);
    }
  }

}
