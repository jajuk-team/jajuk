/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
 *
 *  Originally taken from:
 *   aTunes 1.6.0
 *   Copyright (C) 2006-2007 Alex Aranda (fleax) alex.aranda@gmail.com
 *
 *   http://www.atunes.org
 *   http://sourceforge.net/projects/atunes
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

package org.jajuk.services.lyrics;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.jajuk.services.lyrics.providers.IProvider;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * Lyrics retrieval service. This service will retrieves lyrics from various
 * providers, querying all of them until one returns some valid input. (TODO:
 * user-selectable multi-input-sources. edit the LyricsService so that it will
 * notify about various valid sources, so we could propose various inputs to the
 * user)
 */
public final class LyricsService implements Const {

  private static Map<String, IProvider> providers = null;
  private static IProvider current = null;

  /**
   * Empty private constructor to avoid instantiating utility class
   */
  private LyricsService() {

  }

  /**
   * Loads the appropriate providers from the properties file.
   * 
   * @return a map of providers loaded from the properties file
   */
  @SuppressWarnings("unchecked")
  public static Map<String, IProvider> loadProviders() {
    final Map<String, IProvider> lProviders = new HashMap<String, IProvider>();
    try {
      StringTokenizer st = new StringTokenizer(Conf
          .getString(CONF_LYRICS_PROVIDERS), ",");
      while (st.hasMoreTokens()) {
        String providerClass = st.nextToken();
        if (!UtilString.isVoid(providerClass)) {
          Class<IProvider> clazz = (Class<IProvider>) Class.forName(providerClass);
          Constructor<IProvider> constructor = clazz.getConstructor();
          IProvider provider = constructor.newInstance();
          lProviders.put(provider.getSource(), provider);
          Log.debug("Added Lyrics provider " + providerClass);
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
    return lProviders;
  }

  /**
   * Cycles through lyrics providers to return the best matching lyrics.
   * 
   * @param artist
   *          the song's artist
   * @param title
   *          the song's title
   * 
   * @return the song's lyrics
   */
  public static String getLyrics(final String artist, final String title) {
    String lyrics = null;

    current = null;
    Log.debug("Retrieving lyrics for artist {{" + artist + "}} and song {{" + title + "}}");
    for (final IProvider provider : getProviders().values()) {
      lyrics = provider.getLyrics(artist, title);
      current = provider;
      if (lyrics != null) {
        break;
      }
    }
    return lyrics;
  }

  /**
   * Returns the lazy-instantiated providers collection
   * 
   * @return the map of loaded providers
   */
  public static synchronized Map<String, IProvider> getProviders() {
    if (providers == null) {
      providers = loadProviders();
    }
    return providers;
  }

  public static synchronized IProvider getCurrentProvider() {
    return current;
  }
}
