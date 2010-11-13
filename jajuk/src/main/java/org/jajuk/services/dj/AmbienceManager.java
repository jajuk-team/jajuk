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

package org.jajuk.services.dj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.log.Log;

/**
 * Ambience manager.
 */
public final class AmbienceManager implements Observer {

  /** Ambience id-> ambience. */
  private final Map<String, Ambience> ambiences = new HashMap<String, Ambience>(10);

  /** Self instance. */
  private static AmbienceManager self = new AmbienceManager();

  /**
   * No direct constructor.
   */
  private AmbienceManager() {
    ObservationManager.register(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.GENRE_NAME_CHANGED);
    return eventSubjectSet;
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static AmbienceManager getInstance() {
    return self;
  }

  /**
   * Load properties from in file Format: jajuk.ambience.<ID>/<name>=genre1,genre2,...
   */
  public void load() {
    // if first startup, define default ambiences
    if (UpgradeManager.isFirstSession()) {
      Log.debug("First start, creating default Ambiences.");
      createDefaultAmbiences();
      return;
    }
    Properties properties = Conf.getProperties();
    Enumeration<Object> e = properties.keys();
    while (e.hasMoreElements()) {
      String sKey = (String) e.nextElement();
      if (sKey.matches(Const.AMBIENCE_PREFIX + ".*")) {
        Set<Genre> genres = new HashSet<Genre>(10);
        StringTokenizer st = new StringTokenizer((String) properties.get(sKey), ",");
        while (st.hasMoreTokens()) {
          Genre genre = GenreManager.getInstance().getGenreByID(st.nextToken());
          if (genre != null) {
            genres.add(genre);
          }
        }
        String ambienceDesc = sKey.substring(Const.AMBIENCE_PREFIX.length());
        int index = ambienceDesc.indexOf('/');
        if (index == -1) {
          continue;
        }
        String ambienceID = ambienceDesc.substring(0, index);
        String ambienceName = ambienceDesc.substring(index + 1);
        Ambience ambience = new Ambience(ambienceID, ambienceName, genres);
        ambiences.put(ambienceID, ambience);
      }
    }
    // If none ambience, means ambience can have been reset after a genre
    // hashcode computation change, reset to defaults
    if (ambiences.size() == 0) {
      Log.debug("No ambiences loaded, creating default Ambiences.");
      createDefaultAmbiences();
    }
  }

  /**
   * Gets the ambiences.
   * 
   * @return sorted list of registered ambiences
   */
  public List<Ambience> getAmbiences() {
    List<Ambience> al = new ArrayList<Ambience>(ambiences.values());
    Collections.sort(al);
    return al;
  }

  /**
   * Gets the ambience.
   * 
   * @param sID Ambience id
   * 
   * @return registrated ambience
   */
  public Ambience getAmbience(String sID) {
    return ambiences.get(sID);
  }

  /**
   * Gets the ambience by name.
   * 
   * @param sName Ambience name
   * 
   * @return registrated ambience or null if no matching name
   */
  public Ambience getAmbienceByName(String sName) {
    for (Ambience ambience : ambiences.values()) {
      if (ambience.getName().equals(sName)) {
        return ambience;
      }
    }
    return null;
  }

  /**
   * Register a new ambience.
   * 
   * @param ambience ambience to register
   */
  public void registerAmbience(Ambience ambience) {
    ambiences.put(ambience.getID(), ambience);
  }

  /**
   * Gets the selected ambience.
   * 
   * @return currently selected ambience or null if "all" ambience selected
   */
  public Ambience getSelectedAmbience() {
    String sDefault = Conf.getString(Const.CONF_DEFAULT_AMBIENCE);
    return getAmbience(sDefault);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.GENRE_NAME_CHANGED.equals(subject)) {
      Properties properties = event.getDetails();
      Genre old = (Genre) properties.get(Const.DETAIL_OLD);
      Genre newGenre = (Genre) properties.get(Const.DETAIL_NEW);
      // replace genre into all genres
      for (Ambience ambience : ambiences.values()) {
        if (ambience.getGenres().contains(old)) {
          ambience.removeGenre(old);
          ambience.addGenre(newGenre);
        }
      }
    }
  }

  /**
   * Perform required operations before exit.
   */
  public void commit() {
    // first, remove all ambiences from configuration
    Properties properties = Conf.getProperties();
    Iterator<Object> it = properties.keySet().iterator();
    while (it.hasNext()) {
      String sKey = (String) it.next();
      if (sKey.startsWith(Const.AMBIENCE_PREFIX)) {
        it.remove();
      }
    }
    // now create and set each ambience
    for (Ambience ambience : ambiences.values()) {
      if (ambience.getGenres().size() > 0) {
        StringBuilder genres = new StringBuilder();
        for (Genre genre : ambience.getGenres()) {
          genres.append(genre.getID()).append(',');
        }
        Conf.setProperty(Const.AMBIENCE_PREFIX + ambience.getID() + '/' + ambience.getName(),
            genres.toString().substring(0, genres.length() - 1));
      }
    }
  }

  /**
   * Remove a ambience.
   * 
   * @param sAmbienceID the ambience to remove
   */
  public void removeAmbience(String sAmbienceID) {
    this.ambiences.remove(sAmbienceID);
    // Propagate the event
    Properties properties = new Properties();
    properties.put(Const.DETAIL_CONTENT, sAmbienceID);
    ObservationManager.notify(new JajukEvent(JajukEvents.AMBIENCE_REMOVED, properties));
  }

  /**
   * Create out of the box ambiences.
   */
  public void createDefaultAmbiences() {
    // Define default ambience by genre name
    String[] genresRockPop = new String[] { "Classic Rock", "Pop", "Rock", "Ska", "AlternRock",
        "Instrumental Pop", "Instrumental Rock", "Southern Rock", "Pop/Funk", "Folk-Rock",
        "Rock & Roll", "Symphonic Rock", "Ballad", "Christian Rock", "JPop", "SynthPop" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("0", Messages.getString("Ambience.0"), genresRockPop));
    String[] genresRap = new String[] { "Hip-Hop", "R&B", "Rap", "Fusion", "Gangsta",
        "Christian Rap", "Porn Groove", "Rhytmic Soul", "Christian Gangsta" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("1", Messages.getString("Ambience.1"), genresRap));
    String[] genresHardRock = new String[] { "Grunge", "Metal", "Industrial", "Death Metal",
        "Fusion", "Punk", "Gothic", "Darkwave", "Fast Fusion", "Hard Rock", "Gothic Rock",
        "Progressive Rock", "Punk Rock", "Terror", "Negerpunk", "Polsk Punk", "Heavy Metal",
        "Black Metal", "Thrash Metal" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("2", Messages.getString("Ambience.2"), genresHardRock));
    String[] genresTechno = new String[] { "Dance", "New Age", "Techno", "Euro-Techno", "Ambient",
        "Trance", "House", "Game", "Space", "Techno-Industrial", "Eurodance", "Dream", "Jungle",
        "Rave", "Euro-House", "Goa", "Club-House", "Hardcore", "Beat" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("3", Messages.getString("Ambience.3"), genresTechno));
    String[] genresElectro = new String[] { "Trip-Hop", "Acid", "Electronic", "Club" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("4", Messages.getString("Ambience.4"), genresElectro));
    String[] genresClassical = new String[] { "Classical", "Chorus", "Opera", "Chamber Music",
        "Sonata", "Symphony" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("5", Messages.getString("Ambience.5"), genresClassical));
    String[] genresSoft = new String[] { "Reggae", "Acid Jazz", "Slow Rock", "Jazz",
        "Easy Listening", "Acoustic", "Ballad" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("6", Messages.getString("Ambience.6"), genresSoft));
    String[] genresParty = new String[] { "Dance", "Disco", "Funk", "Ska", "Soul", "Eurodance",
        "Big Band", "Club", "Rhytmic Soul", "Dance Hall", "Club-House" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("7", Messages.getString("Ambience.7"), genresParty));
    String[] genresJazzBlues = new String[] { "Jazz", "Jazz+Funk", "Bass", "Acid Jazz" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("8", Messages.getString("Ambience.8"), genresJazzBlues));
    String[] genresWorld = new String[] { "Ethnic", "Native American", "Tribal", "Polka", "Celtic",
        "Folklore", "Indie" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("9", Messages.getString("Ambience.9"), genresWorld));
    String[] genresOthers = new String[] { "Other", "Alternative", "Soundtrack", "Vocal",
        "Meditative", "Comedy", "Humour", "Speech", "Anime" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("10", Messages.getString("Ambience.10"), genresOthers));
    String[] genresFolkOldies = new String[] { "Country", "Oldies", "Gospel", "Pop-Folk",
        "Southern Rock", "Cabaret", "Retro", "Folk-Rock", "National Folk", "Swing", "Rock & Roll",
        "Folk", "Revival", "Chanson" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("11", Messages.getString("Ambience.11"), genresFolkOldies));
    String[] genresInde = new String[] { "Noise", "AlternRock", "New Wave", "Psychedelic",
        "Acid Punk", "Avantgarde", "Psychedelic Rock", "Freegenre", "Drum Solo", "Drum & Bass" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("12", Messages.getString("Ambience.12"), genresInde));
    String[] genresLatin = new String[] { "Latin", "Tango", "Samba", "Acapella", "Salsa" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("13", Messages.getString("Ambience.13"), genresLatin));
  }

}
