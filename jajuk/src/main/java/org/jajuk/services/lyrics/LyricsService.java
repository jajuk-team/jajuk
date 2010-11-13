/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
 *  $Revision$
 */

package org.jajuk.services.lyrics;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.File;
import org.jajuk.services.lyrics.persisters.ILyricsPersister;
import org.jajuk.services.lyrics.providers.ILyricsProvider;
import org.jajuk.services.lyrics.providers.JajukLyricsProvider;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.InformationJPanel.MessageType;
import org.jajuk.util.Messages;
import org.jajuk.util.error.LyricsPersistenceException;
import org.jajuk.util.log.Log;

/**
 * Lyrics retrieval service. This service will retrieves lyrics from various
 * providers, querying all of them until one returns some valid input.
 * 
 * TODO: user-selectable multi-input-sources. edit the LyricsService so that it will
 * notify about various valid sources, so we could propose various inputs to the
 * user. For now the lyrics providers list is static and stored directly in this class.
 */
public final class LyricsService {

  /** DOCUMENT_ME. */
  private static List<ILyricsProvider> providers = null;

  /** DOCUMENT_ME. */
  private static ILyricsProvider current = null;

  /** DOCUMENT_ME. */
  private static List<ILyricsPersister> persisters = null;

  /** Providers list. */
  private static String[] providersClasses = new String[] {
      "org.jajuk.services.lyrics.providers.TagLyricsProvider",
      "org.jajuk.services.lyrics.providers.TxtLyricsProvider",
      "org.jajuk.services.lyrics.providers.LyricWikiWebLyricsProvider",
      "org.jajuk.services.lyrics.providers.FlyWebLyricsProvider", };

  /** Persisters list. */
  private static String[] persisterClasses = new String[] {
      "org.jajuk.services.lyrics.persisters.TagPersister",
      "org.jajuk.services.lyrics.persisters.TxtPersister" };

  /**
   * Empty private constructor to avoid instantiating utility class.
   */
  private LyricsService() {

  }

  /**
   * Loads the appropriate providers from the properties file. For now,
   * providers order is static and the providersClasses array reflect jajuk
   * artists service preferred ordering
   * 
   * @TODO this behavior could eventually be switched to a shuffle provider list
   * for performance or better resources usage reasons
   */
  @SuppressWarnings("unchecked")
  public static void loadProviders() {
    providers = new ArrayList<ILyricsProvider>(2);
    try {
      for (String providerClass : providersClasses) {
        if (!StringUtils.isBlank(providerClass)) {
          Class<ILyricsProvider> clazz = (Class<ILyricsProvider>) Class.forName(providerClass);
          ILyricsProvider provider = clazz.newInstance();
          providers.add(provider);
          Log.debug("Added Lyrics provider " + providerClass);
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Load persisters.
   * DOCUMENT_ME
   */
  @SuppressWarnings("unchecked")
  public static void loadPersisters() {
    persisters = new ArrayList<ILyricsPersister>(2);
    try {
      for (String persisterClass : persisterClasses) {
        if (!StringUtils.isBlank(persisterClass)) {
          Class<ILyricsPersister> clazz = (Class<ILyricsPersister>) Class.forName(persisterClass);
          ILyricsPersister persister = clazz.newInstance();
          persisters.add(persister);
          Log.debug("Added Lyrics persister " + persisterClass);
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Cycles through lyrics providers to return the best matching lyrics.
   * 
   * @param audioFile DOCUMENT_ME
   * 
   * @return the song's lyrics
   */
  public static String getLyrics(final File audioFile) {
    String lyrics = null;
    current = null;
    Log.debug("Retrieving lyrics for file {{" + audioFile + "}}");
    for (final ILyricsProvider provider : getProviders()) {
      provider.setAudioFile(audioFile);
      lyrics = provider.getLyrics();
      current = provider;
      if (lyrics != null) {
        break;
      }
    }
    // None provider found lyrics so reset current
    if (lyrics == null) {
      current = null;
    }
    return lyrics;
  }

  /**
   * Commit lyrics for a jajuk lyrics provider (jajuk GUI).
   * 
   * @param provider the JajukLyricsProvider
   * 
   * @throws LyricsPersistenceException if lyrics cannot be written
   */
  public static void commitLyrics(JajukLyricsProvider provider) throws LyricsPersistenceException {
    boolean commitOK = false;
    String destinationPath = null;
    Log.debug("Commiting lyrics for file {{" + provider.getFile().getAbsolutePath() + "}}");
    // Try each persister until we actually persist lyrics
    for (final ILyricsPersister persister : getPersisters()) {
      persister.setAudioFile(provider.getFile());
      destinationPath = persister.getDestinationFile().getAbsolutePath();
      commitOK = persister.commitLyrics(provider.getArtist(), provider.getTitle(), provider
          .getLyrics());
      if (commitOK) {
        break;
      }
    }
    if (commitOK) {
      Log.info("Lyrics successfully commited for file : " + provider.getFile().getAbsolutePath());
      InformationJPanel.getInstance().setMessage(
          Messages.getString("Success") + " [" + destinationPath + "]", MessageType.INFORMATIVE);
    } else {
      throw new LyricsPersistenceException("Lyrics could not be commited to "
          + provider.getFile().getAbsolutePath());
    }
  }

  /**
   * Delete lyrics from any persister support.
   * 
   * @param provider DOCUMENT_ME
   * 
   * @throws LyricsPersistenceException if the lyrics cannot be removed
   */
  public static void deleteLyrics(JajukLyricsProvider provider) throws LyricsPersistenceException {
    boolean deleteOK = false;
    String destinationPath = null;
    Log.debug("deleting lyrics for file {{" + provider.getFile().getAbsolutePath() + "}}");
    for (final ILyricsPersister persister : getPersisters()) {
      persister.setAudioFile(provider.getFile());
      destinationPath = persister.getDestinationFile().getAbsolutePath();
      deleteOK = persister.deleteLyrics();
      if (deleteOK) {
        break;
      }
    }
    if (deleteOK) {
      Log.info("Lyrics successfully deleted for file : " + provider.getFile().getAbsolutePath());
      InformationJPanel.getInstance().setMessage(
          Messages.getString("Success") + " [" + destinationPath + "]", MessageType.INFORMATIVE);
    } else {
      throw new LyricsPersistenceException("Lyrics could not be deleted from "
          + provider.getFile().getName());
    }

  }

  /**
   * Returns the lazy-instantiated providers collection.
   * 
   * @return the map of loaded providers
   */
  public static List<ILyricsProvider> getProviders() {
    if (providers == null) {
      loadProviders();
    }
    return providers;
  }

  /**
   * Gets the current provider.
   * 
   * @return the current provider
   */
  public static ILyricsProvider getCurrentProvider() {
    return current;
  }

  /**
   * Gets the persisters.
   * 
   * @return the persisters
   */
  public static List<ILyricsPersister> getPersisters() {
    if (persisters == null) {
      loadPersisters();
    }
    return persisters;
  }

}
