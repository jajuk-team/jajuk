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

package org.jajuk.base.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.jajuk.base.Event;
import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.items.Item;
import org.jajuk.base.items.Style;
import org.jajuk.base.items.Track;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.Resources.Details;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage styles
 */
public class StyleManager extends ItemManager<Style> {
  /* List of all known styles */
  public static Vector<String> stylesList;

  /**
   * Return hashcode for this item
   *
   * @param sName
   *          item name
   * @return ItemManager ID
   */
  public static String createID(final String sName) {
    return MD5Processor.hash(sName);
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
  public static String format(final String sName) {
    String sOut = sName.trim(); // supress spaces at the begin and the end

    sOut = sOut.replace('-', ' '); // move - to space
    sOut = sOut.replace('_', ' '); // move _ to space
    sOut = sOut.toUpperCase();
    return sOut;
  }

  /**
   * No constructor available, only static access
   */
  public StyleManager() {
    super();
    // register properties
    // ID
    registerProperty(new MetaProperty(XML.ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new MetaProperty(XML.NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new MetaProperty(XML.EXPANDED, false, false, false, false, true,
        Boolean.class, false));
    // create default style list
    StyleManager.stylesList = new Vector<String>(Arrays.asList(Util.genres));
    Collections.sort(StyleManager.stylesList);
  }

  /**
   * Change the item name
   *
   * @param old
   * @param sNewName
   * @return new item
   */
  public synchronized Style changeStyleName(final Style old, final String sNewName)
      throws JajukException {
    final TrackManager trackManager = ((TrackManager) ItemType.Track.getManager());
    synchronized (trackManager.getLock()) {
      // check there is actually a change
      if (old.getName2().equals(sNewName)) {
        return old;
      }
      final Style newItem = registerStyle(sNewName);
      // re apply old properties from old item
      newItem.cloneProperties(old);
      // update tracks
      final ArrayList<Track> alTracks = new ArrayList<Track>(trackManager.getCarbonItems());
      // we need to create a new list to avoid concurrent exceptions
      for (final Track track : alTracks) {
        if (track.getStyle().equals(old)) {
          trackManager.changeTrackStyle(track, sNewName, null);
        }
      }
      // notify everybody for the file change
      final Properties properties = new Properties();
      properties.put(Details.OLD, old);
      properties.put(Details.NEW, newItem);
      // Notify interested items (like ambience manager)
      ObservationManager.notifySync(new Event(EventSubject.EVENT_STYLE_NAME_CHANGED, properties));
      return newItem;
    }
  }

  /**
   * Get styles associated with this item
   *
   * @param item
   * @return
   */
  public Set<Style> getAssociatedStyles(final Item item) {
    synchronized (getLock()) {
      final Set<Style> out = new TreeSet<Style>();
      for (final Style item2 : getItems().values()) {
        final Style style = item2;
        if ((item instanceof Track) && ((Track) item).getStyle().equals(style)) {
          out.add(style);
        } else {
          final Set<Track> tracks = ((TrackManager) ItemType.Track.getManager())
              .getAssociatedTracks(item);
          for (final Track track : tracks) {
            out.add(track.getStyle());
          }
        }
      }
      return out;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML.STYLES;
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public Style getStyleByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }

  /**
   * Return style by name
   *
   * @param sName
   * @return
   */
  public Style getStyleByName(final String sName) {
    return registerStyle(sName);
  }


  /**
   * @return Human readable registrated style list
   */
  public synchronized Vector<String> getStylesList() {
    synchronized (getLock()) {
      return StyleManager.stylesList;
    }
  }

  /**
   * Register a style
   *
   * @param sName
   */
  public Style registerStyle(final String sName) {
    final String sId = StyleManager.createID(sName);
    return registerStyle(sId, sName);
  }

  /**
   * Register a style with a known id
   *
   * @param sName
   */
  public Style registerStyle(final String sId, final String sName) {
    synchronized (getLock()) {
      Style style = getItems().get(sId);
      if (style != null) {
        return style;
      }
      style = new Style(sId, sName);
      getItems().put(sId, style);
      // add it in styles list if new
      if (!StyleManager.stylesList.contains(sName)) {
        StyleManager.stylesList.add(style.getName2());
      }
      // Sort items ignoring case
      Collections.sort(StyleManager.stylesList, new Comparator<String>() {

        public int compare(final String o1, final String o2) {
          return o1.compareToIgnoreCase(o2);
        }

      });
      return style;
    }
  }
}