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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Manages Digital DJs
 * <p>
 * Singleton
 * </p>
 */
public class DigitalDJManager implements ITechnicalStrings, Observer {

  /** List of registated DJs ID->DJ */
  private HashMap<String, DigitalDJ> djs;

  /** self instance */
  private static DigitalDJManager dj;

  /** Currently selected DJ */
  private static DigitalDJ current;

  /**
   * no instantiation
   */
  private DigitalDJManager() {
    djs = new HashMap<String, DigitalDJ>();
    ObservationManager.register(this);
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_AMBIENCE_REMOVED);
    return eventSubjectSet;
  }

  /**
   * @return self instance
   */
  public static DigitalDJManager getInstance() {
    if (dj == null) {
      dj = new DigitalDJManager();
    }
    return dj;
  }

  /**
   * 
   * @return DJs iteration
   */
  public Collection<DigitalDJ> getDJs() {
    return djs.values();
  }

  /**
   * 
   * @return DJs names iteration
   */
  public Set<String> getDJNames() {
    HashSet<String> hsNames = new HashSet<String>(10);
    for (DigitalDJ dj : djs.values()) {
      hsNames.add(dj.getName());
    }
    return hsNames;
  }

  /**
   * 
   * @return DJ by name
   */
  public DigitalDJ getDJByName(String sName) {
    for (DigitalDJ dj : djs.values()) {
      if (dj.getName().equals(sName)) {
        return dj;
      }
    }
    return null;
  }

  /**
   * 
   * @return DJ by ID
   */
  public DigitalDJ getDJByID(String sID) {
    return djs.get(sID);
  }

  /**
   * Commit given dj on disk
   * 
   * @param dj
   */
  public static void commit(DigitalDJ dj) {
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(Util.getConfFileByPath(FILE_DJ_DIR
          + "/" + dj.getID() + "." + XML_DJ_EXTENSION)));
      bw.write(dj.toXML());
      bw.flush();
      bw.close();
    } catch (Exception e) {
      Log.error(145, (dj != null) ? "{{" + dj.getName() + "}}" : null, e);
    }
  }

  /**
   * Remove a DJ
   * 
   * @param DJ
   */
  public void remove(DigitalDJ dj) {
    djs.remove(dj.getID());
    Util.getConfFileByPath(FILE_DJ_DIR + "/" + dj.getID() + "." + XML_DJ_EXTENSION).delete();
    // reset default DJ if this DJ was default
    if (ConfigurationManager.getProperty(CONF_DEFAULT_DJ).equals(dj.getID())) {
      ConfigurationManager.setProperty(CONF_DEFAULT_DJ, "");
    }
    // alert command panel
    ObservationManager.notify(new Event(EventSubject.EVENT_DJS_CHANGE));
  }

  /**
   * Register a DJ
   * 
   * @param DJ
   */
  public void register(DigitalDJ dj) {
    djs.put(dj.getID(), dj);
    // alert command panel
    ObservationManager.notify(new Event(EventSubject.EVENT_DJS_CHANGE));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(Event event) {
    if (EventSubject.EVENT_AMBIENCE_REMOVED.equals(event.getSubject())) {
      Properties properties = event.getDetails();
      String sID = (String) properties.get(DETAIL_CONTENT);
      for (DigitalDJ dj : djs.values()) {
        if (dj instanceof AmbienceDigitalDJ
            && ((AmbienceDigitalDJ) dj).getAmbience().getID().equals(sID)) {
          int i = Messages.getChoice(Messages.getString("DigitalDJWizard.61") + " " + dj.getName()
              + " ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
          if (i == JOptionPane.YES_OPTION) {
            remove(dj);
          } else {
            return;
          }
        }
      }
    }
  }

  /**
   * Load all DJs (.dj files) found in jajuk home directory
   * 
   */
  public void loadAllDJs() {
    try {
      File[] files = Util.getConfFileByPath(FILE_DJ_DIR).listFiles(new FileFilter() {
        public boolean accept(File file) {
          if (file.isFile() && file.getPath().endsWith('.' + XML_DJ_EXTENSION)) {
            return true;
          }
          return false;
        }
      });
      for (int i = 0; i < files.length; i++) {
        try { // try each DJ to continue others if one fails
          DigitalDJFactory factory = DigitalDJFactory.getFactory(files[i]);
          DigitalDJ dj = factory.getDJ(files[i]);
          djs.put(dj.getID(), dj);
          if (dj.getID().equals(ConfigurationManager.getProperty(CONF_DEFAULT_DJ))) {
            current = dj;
          }
        } catch (Exception e) {
          Log.error(144, "{{" + files[i].getAbsolutePath() + "}}", e);
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  static public DigitalDJ getCurrentDJ() {
    return current;
  }

  static public void setCurrentDJ(DigitalDJ dj) {
    current = dj;
  }

}

/**
 * This class is responsible for creating different factories
 */
abstract class DigitalDJFactory extends DefaultHandler implements ITechnicalStrings {

  /** Factory type (class name) */
  private static String factoryType;

  /** DJ type (class name) */
  protected String type;

  /** DJ name */
  protected String name;

  /** DJ ID */
  protected String id;

  /** DJ Fade duration */
  protected int fadeDuration;

  /** Rating level */
  protected int iRatingLevel;

  /** Startup style */
  protected Style startupStyle;

  /** Track unicity */
  protected boolean bTrackUnicity = false;

  /** General parameters handlers */
  abstract class GeneralDefaultHandler extends DefaultHandler {

    /**
     * Called when we start an element
     * 
     */
    public void startElement(String sUri, String s, String sQName, Attributes attributes)
        throws SAXException {
      if (XML_DJ_DJ.equals(sQName)) {
        id = attributes.getValue(attributes.getIndex(XML_ID));
        name = attributes.getValue(attributes.getIndex(XML_NAME));
        type = attributes.getValue(attributes.getIndex(XML_TYPE));
      } else if (XML_DJ_GENERAL.equals(sQName)) {
        bTrackUnicity = Boolean.parseBoolean(attributes.getValue(attributes
            .getIndex(XML_DJ_UNICITY)));
        iRatingLevel = Integer.parseInt(attributes.getValue(attributes
            .getIndex(XML_DJ_RATING_LEVEL)));
        fadeDuration = Integer.parseInt(attributes.getValue(attributes
            .getIndex(XML_DJ_FADE_DURATION)));
      } else {// others implementation dependant-operation
        othersTags(sQName, attributes);
      }
    }

    /** Non geenral tags operations */
    abstract protected void othersTags(String sQname, Attributes attributes);
  }

  /**
   * 
   * @param file
   *          DJ configuration file (XML)
   * @return the right factory
   */
  protected static DigitalDJFactory getFactory(File file) throws Exception {
    // Parse the file to get DJ type
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser saxParser = spf.newSAXParser();
    try {
      saxParser.parse(file, new DefaultHandler() {
        /**
         * Called when we start an element
         */
        public void startElement(String sUri, String s, String sQName, Attributes attributes)
            throws SAXException {
          if (XML_DJ_DJ.equals(sQName)) {
            factoryType = attributes.getValue(attributes.getIndex(XML_TYPE));
          }
        }
      });
    }
    // Error parsing the DJ ? delete it
    catch (Exception e) {
      Log.error(e);
      Log.debug("Corrupted DJ: " + file.getAbsolutePath() + " deleted");
      file.delete();
    }
    if (XML_DJ_PROPORTION_CLASS.equals(factoryType)) {
      return new DigitalDJFactoryProportionImpl();
    } else if (XML_DJ_TRANSITION_CLASS.equals(factoryType)) {
      return new DigitalDJFactoryTransitionImpl();
    } else if (XML_DJ_AMBIENCE_CLASS.equals(factoryType)) {
      return new DigitalDJFactoryAmbienceImpl();
    }
    return null;
  }

  /**
   * 
   * @return DigitalDJ from associated factory
   * @param file
   *          DJ file
   */
  abstract DigitalDJ getDJ(File file) throws Exception;

}

/**
 * Proportion dj factory
 * 
 */
class DigitalDJFactoryProportionImpl extends DigitalDJFactory {

  /** Intermediate styles variable used during parsing */
  private String styles;

  /** Intermediate proportion variable used during parsing */
  private float proportion;

  private ArrayList<Proportion> proportions = new ArrayList<Proportion>(5);

  @Override
  DigitalDJ getDJ(File file) throws Exception {
    // Parse XML file to populate the DJ
    DefaultHandler handler = new GeneralDefaultHandler() {

      @Override
      protected void othersTags(String sQname, Attributes attributes) {
        if (XML_DJ_PROPORTION.equals(sQname)) {
          styles = attributes.getValue(attributes.getIndex(XML_DJ_STYLES));
          proportion = Float.parseFloat(attributes.getValue(attributes.getIndex(XML_DJ_VALUE)));
          StringTokenizer st = new StringTokenizer(styles, ",");
          Ambience ambience = new Ambience(Long.toString(System.currentTimeMillis()), "");
          while (st.hasMoreTokens()) {
            ambience.addStyle(StyleManager.getInstance().getStyleByID(st.nextToken()));
          }
          proportions.add(new Proportion(ambience, proportion));
        }

      }
    };
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser saxParser = spf.newSAXParser();
    saxParser.parse(file, handler);
    ProportionDigitalDJ dj = new ProportionDigitalDJ(id);
    dj.setName(name);
    dj.setFadingDuration(fadeDuration);
    dj.setRatingLevel(iRatingLevel);
    dj.setTrackUnicity(bTrackUnicity);
    dj.setProportions(proportions);
    return dj;
  }

  /** No direct constructor */
  DigitalDJFactoryProportionImpl() {

  }
}

/**
 * Ambience dj factory
 */
class DigitalDJFactoryAmbienceImpl extends DigitalDJFactory {

  private Ambience ambience;

  @Override
  DigitalDJ getDJ(File file) throws Exception {
    // Parse XML file to populate the DJ
    DefaultHandler handler = new GeneralDefaultHandler() {
      @Override
      protected void othersTags(String sQname, Attributes attributes) {
        if (XML_DJ_AMBIENCE.equals(sQname)) {
          String sAmbienceID = attributes.getValue(attributes.getIndex(XML_DJ_VALUE));
          ambience = AmbienceManager.getInstance().getAmbience(sAmbienceID);
        }
      }
    };
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser saxParser = spf.newSAXParser();
    saxParser.parse(file, handler);
    AmbienceDigitalDJ dj = new AmbienceDigitalDJ(id);
    dj.setName(name);
    dj.setFadingDuration(fadeDuration);
    dj.setRatingLevel(iRatingLevel);
    dj.setTrackUnicity(bTrackUnicity);
    dj.setAmbience(ambience);
    return dj;
  }

  /** No direct constructor */
  DigitalDJFactoryAmbienceImpl() {

  }
}

/**
 * Transition dj factory
 * 
 */
class DigitalDJFactoryTransitionImpl extends DigitalDJFactory {

  /** Intermediate transition list */
  private ArrayList<Transition> transitions = new ArrayList<Transition>(10);

  @Override
  DigitalDJ getDJ(File file) throws Exception {
    // Parse XML file to populate the DJ
    DefaultHandler handler = new GeneralDefaultHandler() {
      @Override
      protected void othersTags(String sQname, Attributes attributes) {
        if (XML_DJ_TRANSITION.equals(sQname)) {
          int number = Integer.parseInt(attributes.getValue(attributes.getIndex(XML_DJ_NUMBER)));
          String fromStyles = attributes.getValue(attributes.getIndex(XML_DJ_FROM));
          StringTokenizer st = new StringTokenizer(fromStyles, ",");
          Ambience fromAmbience = new Ambience();
          while (st.hasMoreTokens()) {
            fromAmbience.addStyle(StyleManager.getInstance().getStyleByID(st.nextToken()));
          }
          String toStyles = attributes.getValue(attributes.getIndex(XML_DJ_TO));
          Ambience toAmbience = new Ambience();
          st = new StringTokenizer(toStyles, ",");
          while (st.hasMoreTokens()) {
            toAmbience.addStyle(StyleManager.getInstance().getStyleByID(st.nextToken()));
          }
          transitions.add(new Transition(fromAmbience, toAmbience, number));
        } else if (XML_DJ_TRANSITIONS.equals(sQname)) {
          startupStyle = StyleManager.getInstance().getStyleByID(
              attributes.getValue(attributes.getIndex(XML_DJ_STARTUP_STYLE)));
        }
      }
    };
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser saxParser = spf.newSAXParser();
    saxParser.parse(file, handler);
    TransitionDigitalDJ dj = new TransitionDigitalDJ(id);
    dj.setName(name);
    dj.setFadingDuration(fadeDuration);
    dj.setRatingLevel(iRatingLevel);
    dj.setTrackUnicity(bTrackUnicity);
    dj.setTransitions(transitions);
    dj.setStartupStyle(startupStyle);
    return dj;
  }

  /** No direct constructor */
  DigitalDJFactoryTransitionImpl() {

  }
}