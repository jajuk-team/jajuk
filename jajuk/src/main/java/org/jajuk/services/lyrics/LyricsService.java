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

import ext.services.xml.XMLBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.jajuk.services.lyrics.providers.IProvider;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Lyrics retrieval service. This service will retrieves lyrics from various
 * providers, querying all of them until one returns some valid input. (TODO:
 * user-selectable multi-input-sources. edit the LyricsService so that it will
 * notify about various valid sources, so we could propose various inputs to the
 * user)
 */
public class LyricsService {

  private static HashMap<String, IProvider> providers = null;
  private static IProvider current = null;

  /**
   * Loads the appropriate providers from the providers XML definition file.
   * 
   * @return a map of providers loaded from the XML configuration file
   */
  @SuppressWarnings("unchecked")
  public static HashMap<String, IProvider> loadProviders() {
    final HashMap<String, IProvider> providers = new HashMap<String, IProvider>();

    Log.debug("Loading Providers");
    try {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(ITechnicalStrings.FILE_LYRICS_CONF_PATH.openStream()));
      final StringBuilder xml = new StringBuilder();

      for (String line = null; (line = reader.readLine()) != null;) {
        xml.append(line);
      }
      final Document xmlDoc = XMLBuilder.getXMLDocument(xml.toString());
      final NodeList children = xmlDoc.getDocumentElement().getChildNodes();
      final int length = children.getLength();

      for (int i = 0; (i < length); i++) {
        final Node n = children.item(i);

        if (n instanceof Element) {
          final Element e = ((Element) n);
          final String key = e.getTagName();

          if ((key != null) && key.toLowerCase().equals("provider")) {
            final String className = e.getAttribute("class");
            final String url = e.getAttribute("url");

            Log.debug(" provider class='" + className + "' url='" + url + "'");
            if ((className != null) && (className.length() != 0)) {
              try {
                final Class<IProvider> clazz = (Class<IProvider>) Class.forName(className);
                final Constructor<IProvider> constructor = clazz.getConstructor(String.class);
                final IProvider provider = constructor.newInstance(url);

                if (provider.getSource() != null) {
                  providers.put(provider.getSource(), provider);
                  Log.debug(" added provider " + provider.getSource());
                }
              } catch (final ClassNotFoundException ex) {
                Log.warn("Class [" + className + "] could not be found: " + ex);
              } catch (final SecurityException ex) {
                Log.warn("Security-related problem while loading constructor: " + ex);
              } catch (final NoSuchMethodException ex) {
                Log.warn("A matching constructor could not be found: " + ex);
              } catch (final IllegalArgumentException ex) {
                Log.warn("Wrong parameters for constructor: " + ex);
              } catch (final InstantiationException ex) {
                Log.warn("Could not create an instance: " + ex);
              } catch (final IllegalAccessException ex) {
                Log.warn("Forbidden access to constructor: " + ex);
              } catch (final InvocationTargetException ex) {
                Log.warn("Wrong invocation sequence for constructor: " + ex);
                Log.warn("Target constructor threw: " + ex.getCause());
              }
            }
          }
        }
      }
    } catch (final FileNotFoundException e) {
      Log.warn("File " + ITechnicalStrings.FILE_LYRICS_CONF_PATH + " was not found.");
    } catch (final IOException e) {
      Log.warn("IO Exception while loading " + ITechnicalStrings.FILE_LYRICS_CONF_PATH);
    }
    return (providers);
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
    return (lyrics);
  }

  /**
   * Returns the lazy-instantiated providers collection
   * 
   * @return the map of loaded providers
   */
  public static synchronized HashMap<String, IProvider> getProviders() {
    if (providers == null) {
      providers = loadProviders();
    }
    return (providers);
  }

  public static synchronized IProvider getCurrentProvider() {
    return (current);
  }
}
