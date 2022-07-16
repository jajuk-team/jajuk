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

import java.net.URL;
import java.util.List;

import org.jajuk.JajukTestCase;

/**
 * .
 */
public class TestDownloadManager extends JajukTestCase {
  /** The Constant ARTIST.   */
  private static final String ARTIST = "Massive Attack";
  /** The Constant ALBUM.   */
  private static final String ALBUM = "Collected";

  /**
   * Test method for {@link org.jajuk.util.DownloadManager#getRemoteCoversList(String)}.
   *
   * @throws Exception the exception
   * {@link org.jajuk.util.DownloadManager#getRemoteCoversList(String)}.
   */
  public void testGetRemoteCoversList() throws Exception {
    List<URL> res = DownloadManager.getRemoteCoversList(ARTIST + " " + ALBUM);
    assertEquals(true, res.size()>0);
  }

}
