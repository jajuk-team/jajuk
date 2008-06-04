/*
 *  Jajuk
 *  Copyright (C) 2004 The Jajuk Team
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
 *  $Revision:3266 $
 */

package org.jajuk.services.dj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.jajuk.Main;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;

/**
 * Ambience manager
 */
public class AmbienceManager implements ITechnicalStrings, Observer {

  /** Ambience id-> ambience */
  private HashMap<String, Ambience> ambiences = new HashMap<String, Ambience>(10);

  /** Self instance */
  private static AmbienceManager self;

  /**
   * No direct constructor
   */
  private AmbienceManager() {
    ObservationManager.register(this);
  }

  public Set<JajukEvents> getRegistrationKeys() {
    HashSet<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EVENT_STYLE_NAME_CHANGED);
    return eventSubjectSet;
  }

  /**
   * 
   * @return singleton
   */
  public static AmbienceManager getInstance() {
    if (self == null) {
      self = new AmbienceManager();
    }
    return self;
  }

  /**
   * Load properties from in file Format: jajuk.ambience.<ID>/<name>=style1,style2,...
   */
  public void load() {
    // if first startup, define default ambiences
    if (Main.bFirstSession) {
      createDefaultAmbiences();
      return;
    }
    Properties properties = ConfigurationManager.getProperties();
    Enumeration<Object> e = properties.keys();
    while (e.hasMoreElements()) {
      String sKey = (String) e.nextElement();
      if (sKey.matches(AMBIENCE_PREFIX + ".*")) {
        HashSet<Style> styles = new HashSet<Style>(10);
        StringTokenizer st = new StringTokenizer((String) properties.get(sKey), ",");
        while (st.hasMoreTokens()) {
          Style style = StyleManager.getInstance().getStyleByID(st.nextToken());
          if (style != null) {
            styles.add(style);
          }
        }
        String ambienceDesc = sKey.substring(AMBIENCE_PREFIX.length());
        int index = ambienceDesc.indexOf('/');
        if (index == -1) {
          continue;
        }
        String ambienceID = ambienceDesc.substring(0, index);
        String ambienceName = ambienceDesc.substring(index + 1);
        Ambience ambience = new Ambience(ambienceID, ambienceName, styles);
        ambiences.put(ambienceID, ambience);
      }
    }
    // If none ambience, means ambience can have been reset after a style
    // hashcode computation change, reset to defaults
    if (ambiences.size() == 0) {
      createDefaultAmbiences();
    }
  }

  /**
   * 
   * @return list of registated ambiences
   */
  public Collection<Ambience> getAmbiences() {
    ArrayList<Ambience> al = new ArrayList<Ambience>(ambiences.values());
    Collections.sort(al);
    return al;
  }

  /**
   * 
   * @param sID
   *          Ambience id
   * @return registrated ambience
   */
  public Ambience getAmbience(String sID) {
    return ambiences.get(sID);
  }

  /**
   * 
   * @param sName
   *          Ambience name
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
   * Register a new ambience
   * 
   * @param ambience
   *          ambience to register
   */
  public void registerAmbience(Ambience ambience) {
    ambiences.put(ambience.getID(), ambience);
  }

  /**
   * 
   * @return currently selected ambience or null if "all" ambience selected
   */
  public Ambience getSelectedAmbience() {
    String sDefault = ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE);
    return getAmbience(sDefault);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(Event event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.EVENT_STYLE_NAME_CHANGED.equals(subject)) {
      Properties properties = event.getDetails();
      Style old = (Style) properties.get(DETAIL_OLD);
      Style newStyle = (Style) properties.get(DETAIL_NEW);
      // replace style into all styles
      for (Ambience ambience : ambiences.values()) {
        if (ambience.getStyles().contains(old)) {
          ambience.removeStyle(old);
          ambience.addStyle(newStyle);
        }
      }
    }
  }

  /**
   * Perform required operations before exit
   * 
   */
  public void commit() {
    // first, remove all ambiences from configuration
    Properties properties = ConfigurationManager.getProperties();
    Iterator<Object> it = properties.keySet().iterator();
    while (it.hasNext()) {
      String sKey = (String) it.next();
      if (sKey.startsWith(AMBIENCE_PREFIX)) {
        it.remove();
      }
    }
    // now create and set each ambience
    for (Ambience ambience : ambiences.values()) {
      if (ambience.getStyles().size() > 0) {
        String styles = "";
        for (Style style : ambience.getStyles()) {
          styles += style.getID() + ',';
        }
        styles = styles.substring(0, styles.length() - 1);
        ConfigurationManager.setProperty(AMBIENCE_PREFIX + ambience.getID() + '/'
            + ambience.getName(), styles);
      }
    }
  }

  /**
   * Remove a ambience
   * 
   * @param sAmbienceID
   *          the ambience to remove
   */
  public void removeAmbience(String sAmbienceID) {
    this.ambiences.remove(sAmbienceID);
    // Propagate the event
    Properties properties = new Properties();
    properties.put(DETAIL_CONTENT, sAmbienceID);
    ObservationManager.notify(new Event(JajukEvents.EVENT_AMBIENCE_REMOVED, properties));
  }

  /**
   * Create out of the box ambiences
   * 
   */
  public void createDefaultAmbiences() {
    // Define default amience by style name
    String[] stylesRockPop = new String[] { "Classic Rock", "Pop", "Rock", "Ska", "AlternRock",
        "Instrumental Pop", "Instrumental Rock", "Southern Rock", "Pop/Funk", "Folk-Rock",
        "Rock & Roll", "Symphonic Rock", "Ballad", "Christian Rock", "JPop", "SynthPop" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("0", Messages.getString("Ambience.0"), stylesRockPop));
    String[] stylesRap = new String[] { "Hip-Hop", "R&B", "Rap", "Fusion", "Gangsta",
        "Christian Rap", "Porn Groove", "Rhytmic Soul", "Christian Gangsta" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("1", Messages.getString("Ambience.1"), stylesRap));
    String[] stylesHardRock = new String[] { "Grunge", "Metal", "Industrial", "Death Metal",
        "Fusion", "Punk", "Gothic", "Darkwave", "Fast Fusion", "Hard Rock", "Gothic Rock",
        "Progressive Rock", "Punk Rock", "Terror", "Negerpunk", "Polsk Punk", "Heavy Metal",
        "Black Metal", "Thrash Metal" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("2", Messages.getString("Ambience.2"), stylesHardRock));
    String[] stylesTechno = new String[] { "Dance", "New Age", "Techno", "Euro-Techno", "Ambient",
        "Trance", "House", "Game", "Space", "Techno-Industrial", "Eurodance", "Dream", "Jungle",
        "Rave", "Euro-House", "Goa", "Club-House", "Hardcore", "Beat" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("3", Messages.getString("Ambience.3"), stylesTechno));
    String[] stylesElectro = new String[] { "Trip-Hop", "Acid", "Electronic", "Club" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("4", Messages.getString("Ambience.4"), stylesElectro));
    String[] stylesClassical = new String[] { "Classical", "Chorus", "Opera", "Chamber Music",
        "Sonata", "Symphony" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("5", Messages.getString("Ambience.5"), stylesClassical));
    String[] stylesSoft = new String[] { "Reggae", "Acid Jazz", "Slow Rock", "Jazz",
        "Easy Listening", "Acoustic", "Ballad" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("6", Messages.getString("Ambience.6"), stylesSoft));
    String[] stylesParty = new String[] { "Dance", "Disco", "Funk", "Ska", "Soul", "Eurodance",
        "Big Band", "Club", "Rhytmic Soul", "Dance Hall", "Club-House" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("7", Messages.getString("Ambience.7"), stylesParty));
    String[] stylesJazzBlues = new String[] { "Jazz", "Jazz+Funk", "Bass", "Acid Jazz" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("8", Messages.getString("Ambience.8"), stylesJazzBlues));
    String[] stylesWorld = new String[] { "Ethnic", "Native American", "Tribal", "Polka", "Celtic",
        "Folklore", "Indie" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("9", Messages.getString("Ambience.9"), stylesWorld));
    String[] stylesOthers = new String[] { "Other", "Alternative", "Soundtrack", "Vocal",
        "Meditative", "Comedy", "Humour", "Speech", "Anime" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("10", Messages.getString("Ambience.10"), stylesOthers));
    String[] stylesFolkOldies = new String[] { "Country", "Oldies", "Gospel", "Pop-Folk",
        "Southern Rock", "Cabaret", "Retro", "Folk-Rock", "National Folk", "Swing", "Rock & Roll",
        "Folk", "Revival", "Chanson" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("11", Messages.getString("Ambience.11"), stylesFolkOldies));
    String[] stylesInde = new String[] { "Noise", "AlternRock", "New Wave", "Psychedelic",
        "Acid Punk", "Avantgarde", "Psychedelic Rock", "Freestyle", "Drum Solo", "Drum & Bass" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("12", Messages.getString("Ambience.12"), stylesInde));
    String[] stylesLatin = new String[] { "Latin", "Tango", "Samba", "Acapella", "Salsa" };
    AmbienceManager.getInstance().registerAmbience(
        new Ambience("13", Messages.getString("Ambience.13"), stylesLatin));
  }

}
