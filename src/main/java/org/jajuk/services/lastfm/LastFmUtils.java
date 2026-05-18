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
package org.jajuk.services.lastfm;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LastFmUtils {
  public static final String BASE_URL = "https://ws.audioscrobbler.com/2.0/";
  // IMPORTANT: MusicBrainz REQUIRES a unique and descriptive User-Agent.
  public static final String USER_AGENT = "Jajuk/12.0 (contact jajuk-developers@lists.sourceforge.net)";

  public static String encode(String s) {
    if (s == null) {
      return null;
    }
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}