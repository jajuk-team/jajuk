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
package org.jajuk.services.covers.providers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jajuk.util.log.Log;

/**
 * Covers provider extracting covers links from discogs.com
 */
public class DiscogsCoversProvider implements ICoversProvider {

  /**
   * {@inheritDoc}
   */
  public List<URL> getRemoteCoversList(String search) throws IOException {
    List<URL> alOut = new ArrayList<URL>(20); // URL list
    // check void searches
    if (search == null || search.trim().equals("")) {
      return alOut;
    }
    // Example : https://www.discogs.com/search/?q=AC+DC&type=all
    URL url = new URL("https://www.discogs.com/search/?q=" + URLEncoder.encode(search, "ISO-8859-1") + "&type=all");
    Log.debug(url.toString());
    final URLConnection connection = url.openConnection();
    // User-Agent
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");

    // Retrieve response
    String line;
    final StringBuilder builder = new StringBuilder();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }

    // Analyse response with a pattern to extract image url
    final Pattern pattern = Pattern.compile("img data-src=\"https://[^ ]*\"");
    final Matcher matcher = pattern.matcher(builder);
    while (matcher.find()) {
      final String sUrl = matcher.group();
      if (sUrl.length() > 11) {
        String substring = sUrl.substring(14, sUrl.length() - 1);
        url = new URL(substring);
        // Remove duplicates
        if (!alOut.contains(url)) {
          alOut.add(url);
        }
      }
    }
    return alOut;
  }

}
