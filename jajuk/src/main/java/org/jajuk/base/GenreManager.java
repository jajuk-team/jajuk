/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.base;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.Const;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage genres.
 */
public final class GenreManager extends ItemManager {
  /** Self instance. */
  private static GenreManager singleton = new GenreManager();
  /* List of all known genres */
  private Vector<String> genresList; // NOPMD
  /** note if we have already fully loaded the Collection to speed up initial startup */
  private volatile boolean orderedState = false;

  /**
   * No constructor available, only static access.
   */
  private GenreManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(Const.XML_EXPANDED, false, false, false, false,
        true, Boolean.class, false));
    // Add preset genres
    registerPresetGenres();
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static GenreManager getInstance() {
    return singleton;
  }

  /**
   * Register a genre.
   * 
   * @param sName 
   * 
   * @return the genre
   */
  public Genre registerGenre(String sName) {
    String sId = createID(sName);
    return registerGenre(sId, sName);
  }

  /**
   * Register a genre with a known id.
   *
   * @param sId 
   * @param sName 
   * @return the genre
   */
  Genre registerGenre(String sId, String sName) {
    Genre genre = getGenreByID(sId);
    if (genre != null) {
      return genre;
    }
    genre = new Genre(sId, sName);
    registerItem(genre);
    // add it in genres list if new
    if (!genresList.contains(sName)) {
      genresList.add(genre.getName2());
      // only sort as soon as we have the Collection fully loaded
      if (orderedState) {
        sortGenreList();
      }
    }
    return genre;
  }

  /**
   * 
   */
  private void sortGenreList() {
    // Sort items ignoring case
    Collections.sort(genresList, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
      }
    });
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.ItemManager#switchToOrderState()
   */
  @Override
  public void switchToOrderState() {
    // bring this Manager to ordered state when Collection is fully loaded
    orderedState = true;
    sortGenreList();
    super.switchToOrderState();
  }

  /**
   * Register preset genres.
   * 
   */
  private void registerPresetGenres() {
    // create default genre list
    genresList = new Vector<String>(Arrays.asList(UtilFeatures.GENRES));
    Collections.sort(genresList);
    for (String genre : genresList) {
      registerGenre(genre.intern());
    }
  }

  /**
   * Return genre by name.
   * 
   * @param name 
   * 
   * @return the genre by name
   */
  public Genre getGenreByName(String name) {
    Genre out = null;
    for (Genre genre : getGenres()) {
      if (genre.getName().equals(name)) {
        out = genre;
        break;
      }
    }
    return out;
  }

  /**
   * Change the item name.
   * 
   * @param old 
   * @param sNewName 
   * 
   * @return new item
   * 
   * @throws JajukException the jajuk exception
   */
  Genre changeGenreName(Genre old, String sNewName) throws JajukException {
    // check there is actually a change
    if (old.getName2().equals(sNewName)) {
      return old;
    }
    Genre newItem = registerGenre(sNewName);
    // re apply old properties from old item
    newItem.cloneProperties(old);
    // update tracks
    List<Track> alTracks = TrackManager.getInstance().getTracks();
    // we need to create a new list to avoid concurrent exceptions
    Iterator<Track> it = alTracks.iterator();
    while (it.hasNext()) {
      Track track = it.next();
      if (track.getGenre().equals(old)) {
        TrackManager.getInstance().changeTrackGenre(track, sNewName, null);
      }
    }
    // notify everybody for the file change
    Properties properties = new Properties();
    properties.put(Const.DETAIL_OLD, old);
    properties.put(Const.DETAIL_NEW, newItem);
    // Notify interested items (like ambience manager)
    ObservationManager.notifySync(new JajukEvent(JajukEvents.GENRE_NAME_CHANGED, properties));
    return newItem;
  }

  /**
   * Format the Genre name to be normalized :
   * <p>
   * -no underscores or other non-ascii characters
   * <p>
   * -no spaces at the begin and the end
   * <p>
   * -All in upper case
   * <p>
   * example: "ROCK".
   * 
   * @param sName 
   * 
   * @return the string
   */
  public static String format(String sName) {
    String sOut;
    sOut = sName.trim(); // supress spaces at the begin and the end
    sOut = sOut.replace('-', ' '); // move - to space
    sOut = sOut.replace('_', ' '); // move _ to space
    sOut = sOut.toUpperCase(Locale.getDefault());
    return sOut;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getXMLTag() {
    return Const.XML_GENRES;
  }

  /**
   * Gets the genres list.
   * 
   * @return Human readable list of registrated genres <br>
   * ordered (alphabeticaly)
   */
  public Vector<String> getGenresList() {
    return genresList;
  }

  /**
   * Gets the genre by id.
   * 
   * @param sID Item ID
   * 
   * @return item
   */
  public Genre getGenreByID(String sID) {
    return (Genre) getItemByID(sID);
  }

  /**
   * Gets the genres.
   * 
   * @return ordered genres list
   */
  @SuppressWarnings("unchecked")
  public List<Genre> getGenres() {
    return (List<Genre>) getItems();
  }

  /**
   * Gets the genres iterator.
   * 
   * @return genres iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<Genre> getGenresIterator() {
    return new ReadOnlyIterator<Genre>((Iterator<Genre>) getItemsIterator());
  }
}