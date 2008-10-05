/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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

package org.jajuk.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage styles
 */
public final class StyleManager extends ItemManager {
  /** Self instance */
  private static StyleManager singleton;

  /* List of all known styles */
  private static Vector<String> stylesList;

  /**
   * No constructor available, only static access
   */
  private StyleManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(XML_EXPANDED, false, false, false, false, true,
        Boolean.class, false));
    // create default style list
    stylesList = new Vector<String>(Arrays.asList(UtilFeatures.GENRES));
    Collections.sort(stylesList);
  }

  /**
   * @return singleton
   */
  public static StyleManager getInstance() {
    if (singleton == null) {
      singleton = new StyleManager();
    }
    return singleton;
  }

  /**
   * Register a style
   * 
   * @param sName
   */
  public Style registerStyle(String sName) {
    String sId = createID(sName);
    return registerStyle(sId, sName);
  }

  /**
   * Return hashcode for this item
   * 
   * @param sName
   *          item name
   * @return ItemManager ID
   */
  protected static String createID(String sName) {
    return MD5Processor.hash(sName);
  }

  /**
   * Register a style with a known id
   * 
   * @param sName
   */
  public synchronized Style registerStyle(String sId, String sName) {
    Style style = (Style) hmItems.get(sId);
    if (style != null) {
      return style;
    }
    style = new Style(sId, sName);
    hmItems.put(sId, style);
    // add it in styles list if new
    if (!stylesList.contains(sName)) {
      stylesList.add(style.getName2());
    }
    // Sort items ignoring case
    Collections.sort(stylesList, new Comparator<String>() {

      public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
      }

    });
    return style;
  }

  /**
   * Return style by name
   * 
   * @param sName
   * @return
   */
  public Style getStyleByName(String sName) {
    return registerStyle(sName);
  }

  /**
   * Change the item name
   * 
   * @param old
   * @param sNewName
   * @return new item
   */
  public Style changeStyleName(Style old, String sNewName) throws JajukException {
    synchronized (TrackManager.getInstance()) {
      // check there is actually a change
      if (old.getName2().equals(sNewName)) {
        return old;
      }
      Style newItem = registerStyle(sNewName);
      // re apply old properties from old item
      newItem.cloneProperties(old);
      // update tracks
      List<Track> alTracks = new ArrayList<Track>(TrackManager.getInstance().getTracks());
      // we need to create a new list to avoid concurrent exceptions
      Iterator<Track> it = alTracks.iterator();
      while (it.hasNext()) {
        Track track = it.next();
        if (track.getStyle().equals(old)) {
          TrackManager.getInstance().changeTrackStyle(track, sNewName, null);
        }
      }
      // notify everybody for the file change
      Properties properties = new Properties();
      properties.put(DETAIL_OLD, old);
      properties.put(DETAIL_NEW, newItem);
      // Notify interested items (like ambience manager)
      ObservationManager.notifySync(new Event(JajukEvents.STYLE_NAME_CHANGED, properties));
      return newItem;
    }
  }

  /**
   * Format the Style name to be normalized :
   * <p>
   * -no underscores or other non-ascii characters
   * <p>
   * -no spaces at the begin and the end
   * <p>
   * -All in upper case
   * <p>
   * exemple: "ROCK"
   * 
   * @param sName
   * @return
   */
  public static String format(String sName) {
    String sOut;
    sOut = sName.trim(); // supress spaces at the begin and the end
    sOut = sOut.replace('-', ' '); // move - to space
    sOut = sOut.replace('_', ' '); // move _ to space
    sOut = sOut.toUpperCase();
    return sOut;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML_STYLES;
  }

  /**
   * @return Human readable registrated style list
   */
  public Vector<String> getStylesList() {
    return stylesList;
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public Style getStyleByID(String sID) {
    return (Style) hmItems.get(sID);
  }

  /**
   * 
   * @return styles list
   */
  public synchronized Set<Style> getStyles() {
    Set<Style> styleSet = new LinkedHashSet<Style>();
    for (Item item : getItems()) {
      styleSet.add((Style) item);
    }
    return styleSet;
  }

  /**
   * Get styles associated with this item
   * 
   * @param item
   * @return
   */
  public synchronized Set<Style> getAssociatedStyles(Item item) {
    Set<Style> out = new TreeSet<Style>();
    for (Object item2 : hmItems.values()) {
      Style style = (Style) item2;
      if (item instanceof Track && ((Track) item).getStyle().equals(style)) {
        out.add(style);
      } else {
        Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item);
        for (Track track : tracks) {
          out.add(track.getStyle());
        }
      }
    }
    return out;
  }
}